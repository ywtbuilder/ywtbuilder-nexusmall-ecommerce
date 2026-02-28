<#
.SYNOPSIS
  Audit home page network hosts (cold-style sampling).
.DESCRIPTION
  Samples homepage + /api/home/content multiple times, extracts media/font URLs,
  and checks whether non-local hosts still exist.
#>
param(
    [string]$WebBase = "http://localhost:8091",
    [int]$Samples = 30,
    [int]$TimeoutSec = 15,
    [string]$ReportPath = "./runtime-logs/home-network-host-audit.json",
    [switch]$FailOnViolation
)

$ErrorActionPreference = "Stop"
$SCRIPT_ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $SCRIPT_ROOT
$reportFullPath = if ([System.IO.Path]::IsPathRooted($ReportPath)) {
    $ReportPath
} else {
    Join-Path $PROJECT_ROOT $ReportPath
}
$reportDir = Split-Path -Parent $reportFullPath
if ($reportDir -and -not (Test-Path -LiteralPath $reportDir)) {
    New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
}

$allowedHosts = @("localhost", "127.0.0.1", "::1")
$mediaExtRegex = [regex]'\.(png|jpe?g|gif|webp|bmp|svg|ico|avif|mp4|webm|ogg|woff2?|ttf|otf|eot)(\?|$)'
$htmlAttrRegex = [regex]'(?i)(?:src|href|poster)\s*=\s*["'']([^"'']+)["'']'
$cssUrlRegex = [regex]'(?i)url\(([^)]+)\)'

function Get-Percentile([double[]]$values, [double]$p) {
    if (-not $values -or $values.Count -eq 0) { return 0.0 }
    $sorted = $values | Sort-Object
    $index = [int][Math]::Ceiling(($sorted.Count * $p) / 100.0) - 1
    if ($index -lt 0) { $index = 0 }
    if ($index -ge $sorted.Count) { $index = $sorted.Count - 1 }
    return [double]$sorted[$index]
}

function To-AbsoluteUrl([string]$baseUrl, [string]$rawUrl) {
    if ([string]::IsNullOrWhiteSpace($rawUrl)) { return $null }
    $u = $rawUrl.Trim(" `"`'")
    if ($u.StartsWith("data:", [System.StringComparison]::OrdinalIgnoreCase)) { return $null }
    if ($u.StartsWith("javascript:", [System.StringComparison]::OrdinalIgnoreCase)) { return $null }
    if ($u.StartsWith("mailto:", [System.StringComparison]::OrdinalIgnoreCase)) { return $null }
    if ($u.StartsWith("//")) { $u = "https:$u" }
    try {
        $baseUri = [uri]$baseUrl
        $abs = [uri]::new($baseUri, $u)
        if ($abs.Scheme -notin @("http", "https")) { return $null }
        return $abs.AbsoluteUri
    } catch {
        return $null
    }
}

function Extract-UrlsFromHomeHtml([string]$html, [string]$baseUrl) {
    $urls = New-Object System.Collections.Generic.HashSet[string]
    foreach ($m in $htmlAttrRegex.Matches($html)) {
        $abs = To-AbsoluteUrl -baseUrl $baseUrl -rawUrl $m.Groups[1].Value
        if ($abs) { $urls.Add($abs) | Out-Null }
    }
    foreach ($m in $cssUrlRegex.Matches($html)) {
        $abs = To-AbsoluteUrl -baseUrl $baseUrl -rawUrl $m.Groups[1].Value
        if ($abs) { $urls.Add($abs) | Out-Null }
    }
    return @($urls)
}

function Extract-UrlsFromJsonNode([object]$node, [string]$baseUrl, [System.Collections.Generic.HashSet[string]]$bucket) {
    if ($null -eq $node) { return }
    if ($node -is [string]) {
        $value = [string]$node
        if ($value -match '^https?://' -or $value.StartsWith("//") -or $value.StartsWith("/")) {
            $abs = To-AbsoluteUrl -baseUrl $baseUrl -rawUrl $value
            if ($abs) { $bucket.Add($abs) | Out-Null }
        }
        return
    }
    if ($node -is [System.Collections.IEnumerable] -and -not ($node -is [string])) {
        foreach ($item in $node) {
            Extract-UrlsFromJsonNode -node $item -baseUrl $baseUrl -bucket $bucket
        }
        return
    }
    foreach ($p in $node.PSObject.Properties) {
        Extract-UrlsFromJsonNode -node $p.Value -baseUrl $baseUrl -bucket $bucket
    }
}

function Is-MediaOrFontUrl([string]$absoluteUrl) {
    try {
        $uri = [uri]$absoluteUrl
        if ($uri.AbsolutePath -match '/api/asset/image/') { return $true }
        if ($mediaExtRegex.IsMatch($uri.AbsolutePath + $uri.Query)) { return $true }
        return $false
    } catch {
        return $false
    }
}

$sampleRows = @()
$allMediaHosts = New-Object System.Collections.Generic.HashSet[string]
$allMediaUrls = New-Object System.Collections.Generic.HashSet[string]
$homeMs = @()
$homeApiMs = @()

for ($i = 1; $i -le $Samples; $i++) {
    $sampleHosts = New-Object System.Collections.Generic.HashSet[string]
    $sampleUrls = New-Object System.Collections.Generic.HashSet[string]
    $homeDurationMs = 0.0
    $homeApiDurationMs = 0.0
    $homeStatus = 0
    $homeApiStatus = 0
    $sampleError = $null

    try {
        $sw = [System.Diagnostics.Stopwatch]::StartNew()
        $homeResp = Invoke-WebRequest -UseBasicParsing -Uri "$WebBase/" -TimeoutSec $TimeoutSec -Headers @{ "Cache-Control" = "no-cache" }
        $sw.Stop()
        $homeDurationMs = [double]$sw.Elapsed.TotalMilliseconds
        $homeStatus = [int]$homeResp.StatusCode

        $homeHtml = if ($homeResp.Content -is [byte[]]) { [System.Text.Encoding]::UTF8.GetString($homeResp.Content) } else { [string]$homeResp.Content }
        $homeUrls = Extract-UrlsFromHomeHtml -html $homeHtml -baseUrl "$WebBase/"
        foreach ($u in $homeUrls) {
            if (Is-MediaOrFontUrl -absoluteUrl $u) {
                $sampleUrls.Add($u) | Out-Null
            }
        }

        $swApi = [System.Diagnostics.Stopwatch]::StartNew()
        $homeApiResp = Invoke-WebRequest -UseBasicParsing -Uri "$WebBase/api/home/content" -TimeoutSec $TimeoutSec -Headers @{ "Cache-Control" = "no-cache" }
        $swApi.Stop()
        $homeApiDurationMs = [double]$swApi.Elapsed.TotalMilliseconds
        $homeApiStatus = [int]$homeApiResp.StatusCode

        $homeApiJson = if ($homeApiResp.Content -is [byte[]]) { [System.Text.Encoding]::UTF8.GetString($homeApiResp.Content) } else { [string]$homeApiResp.Content }
        $homeApiObj = $homeApiJson | ConvertFrom-Json
        $apiUrls = New-Object System.Collections.Generic.HashSet[string]
        Extract-UrlsFromJsonNode -node $homeApiObj -baseUrl "$WebBase/" -bucket $apiUrls
        foreach ($u in $apiUrls) {
            if (Is-MediaOrFontUrl -absoluteUrl $u) {
                $sampleUrls.Add($u) | Out-Null
            }
        }

        foreach ($u in $sampleUrls) {
            try {
                $host = ([uri]$u).Host.ToLowerInvariant()
                $sampleHosts.Add($host) | Out-Null
                $allMediaHosts.Add($host) | Out-Null
                $allMediaUrls.Add($u) | Out-Null
            } catch {}
        }
    } catch {
        $sampleError = $_.Exception.Message
    }

    $homeMs += $homeDurationMs
    $homeApiMs += $homeApiDurationMs
    $sampleRows += [ordered]@{
        sample = $i
        home_status = $homeStatus
        home_ms = [math]::Round($homeDurationMs, 2)
        home_api_status = $homeApiStatus
        home_api_ms = [math]::Round($homeApiDurationMs, 2)
        media_host_count = $sampleHosts.Count
        media_hosts = @($sampleHosts | Sort-Object)
        error = $sampleError
    }
}

$externalHosts = @($allMediaHosts | Where-Object { $allowedHosts -notcontains $_ } | Sort-Object)
$suspiciousDomainHits = @($externalHosts | Where-Object { $_ -match 'macro-oss|360buyimg|aliyuncs|jd\.com|taobao|tmall' })

$report = [ordered]@{
    generated_at = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ssK")
    parameters = [ordered]@{
        web_base = $WebBase
        samples = $Samples
        timeout_sec = $TimeoutSec
        fail_on_violation = [bool]$FailOnViolation
    }
    summary = [ordered]@{
        home_p50_ms = [math]::Round((Get-Percentile -values $homeMs -p 50), 2)
        home_p95_ms = [math]::Round((Get-Percentile -values $homeMs -p 95), 2)
        home_api_p50_ms = [math]::Round((Get-Percentile -values $homeApiMs -p 50), 2)
        home_api_p95_ms = [math]::Round((Get-Percentile -values $homeApiMs -p 95), 2)
        media_hosts_total = $allMediaHosts.Count
        media_urls_total = $allMediaUrls.Count
        external_hosts = $externalHosts
        suspicious_external_hosts = $suspiciousDomainHits
    }
    samples = $sampleRows
}

$report | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath $reportFullPath -Encoding UTF8

Write-Host "===== home network host audit =====" -ForegroundColor Cyan
Write-Host ("samples: {0}" -f $Samples)
Write-Host ("home p95(ms): {0}" -f $report.summary.home_p95_ms)
Write-Host ("home api p95(ms): {0}" -f $report.summary.home_api_p95_ms)
Write-Host ("external hosts: {0}" -f (($externalHosts -join ", ")))
Write-Host ("report: {0}" -f $reportFullPath)

if ($externalHosts.Count -gt 0 -and $FailOnViolation) {
    exit 1
}
exit 0

