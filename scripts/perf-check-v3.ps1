<#
.SYNOPSIS
  Mall V3 核心链路性能采样脚本（HTTP 口径）。
.DESCRIPTION
  采样输出：
    - runtime-logs/perf-baseline.json
    - runtime-logs/perf-after.json
    - runtime-logs/perf-diff.md（两份都存在时）
#>
param(
    [ValidateSet("baseline","after")]
    [string]$Mode = "after",
    [string]$WebBase = "http://localhost:8091",
    [string]$AppBase = "http://localhost:18080",
    [int]$Samples = 30,
    [int]$TimeoutSec = 15
)

$ErrorActionPreference = "Stop"
$SCRIPT_ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $SCRIPT_ROOT
$RUNTIME_DIR = Join-Path $PROJECT_ROOT "runtime-logs"
if (-not (Test-Path -LiteralPath $RUNTIME_DIR)) {
    New-Item -ItemType Directory -Path $RUNTIME_DIR -Force | Out-Null
}

$baselinePath = Join-Path $RUNTIME_DIR "perf-baseline.json"
$afterPath = Join-Path $RUNTIME_DIR "perf-after.json"
$diffPath = Join-Path $RUNTIME_DIR "perf-diff.md"
$outputPath = if ($Mode -eq "baseline") { $baselinePath } else { $afterPath }

function Get-Percentile {
    param(
        [double[]]$Values,
        [int]$Percentile
    )
    if (-not $Values -or $Values.Count -eq 0) { return 0 }
    $sorted = $Values | Sort-Object
    $index = [Math]::Ceiling(($Percentile / 100.0) * $sorted.Count) - 1
    if ($index -lt 0) { $index = 0 }
    if ($index -ge $sorted.Count) { $index = $sorted.Count - 1 }
    return [Math]::Round([double]$sorted[$index], 2)
}

function Measure-RequestSamples {
    param(
        [string]$Name,
        [string]$Method,
        [string]$Url,
        [int]$Count,
        [int]$Timeout,
        [hashtable]$Headers = $null,
        [string]$Body = $null
    )

    $samples = @()
    $statusCodes = @()
    $failed = 0

    for ($i = 1; $i -le $Count; $i++) {
        $sw = [System.Diagnostics.Stopwatch]::StartNew()
        try {
            if ($Method -eq "POST") {
                $resp = Invoke-WebRequest -Uri $Url -Method Post -UseBasicParsing -TimeoutSec $Timeout `
                    -Headers $Headers -Body $Body -ContentType "application/json"
            } else {
                $resp = Invoke-WebRequest -Uri $Url -Method Get -UseBasicParsing -TimeoutSec $Timeout -Headers $Headers
            }
            $sw.Stop()
            $samples += [double]$sw.Elapsed.TotalMilliseconds
            $statusCodes += [int]$resp.StatusCode
        } catch {
            $sw.Stop()
            $samples += [double]($Timeout * 1000)
            $statusCodes += -1
            $failed++
        }
    }

    $avg = if ($samples.Count -gt 0) { [Math]::Round((($samples | Measure-Object -Average).Average), 2) } else { 0 }
    [ordered]@{
        name = $Name
        method = $Method
        url = $Url
        samples = $samples.Count
        failures = $failed
        status_codes = ($statusCodes | Select-Object -Unique)
        p50_ms = Get-Percentile -Values $samples -Percentile 50
        p95_ms = Get-Percentile -Values $samples -Percentile 95
        max_ms = Get-Percentile -Values $samples -Percentile 100
        avg_ms = $avg
    }
}

function Resolve-SampleProductId {
    param(
        [string]$AppBaseUrl
    )

    try {
        $resp = Invoke-WebRequest -UseBasicParsing -TimeoutSec 8 -Uri "$AppBaseUrl/search/product?pageNum=1&pageSize=1"
        $raw = $resp.Content
        $jsonText = if ($raw -is [byte[]]) { [System.Text.Encoding]::UTF8.GetString($raw) } else { [string]$raw }
        $obj = $jsonText | ConvertFrom-Json
        $id = $obj.data.list[0].id
        if ($id) { return [int64]$id }
    } catch {}
    return 20
}

function Write-DiffMarkdown {
    param(
        [string]$BaselineFile,
        [string]$AfterFile,
        [string]$OutFile
    )

    if (-not (Test-Path -LiteralPath $BaselineFile) -or -not (Test-Path -LiteralPath $AfterFile)) {
        return
    }

    $baseObj = Get-Content -LiteralPath $BaselineFile -Raw | ConvertFrom-Json -AsHashtable -DateKind String
    $afterObj = Get-Content -LiteralPath $AfterFile -Raw | ConvertFrom-Json -AsHashtable -DateKind String
    $baseMap = @{}
    foreach ($item in $baseObj.cases) {
        $baseMap[$item.name] = $item
    }

    $lines = @()
    $lines += "# Perf Diff"
    $lines += ""
    $lines += "| Case | Baseline P95(ms) | After P95(ms) | Delta(ms) |"
    $lines += "|---|---:|---:|---:|"

    foreach ($item in $afterObj.cases) {
        $name = $item.name
        if (-not $baseMap.ContainsKey($name)) { continue }
        $baseP95 = [double]$baseMap[$name].p95_ms
        $afterP95 = [double]$item.p95_ms
        $delta = [Math]::Round($afterP95 - $baseP95, 2)
        $lines += "| $name | $baseP95 | $afterP95 | $delta |"
    }

    $lines += ""
    $lines += "- generated_at: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')"
    $lines += "- baseline: $BaselineFile"
    $lines += "- after: $AfterFile"
    $lines | Set-Content -LiteralPath $OutFile -Encoding UTF8
}

$sampleProductId = Resolve-SampleProductId -AppBaseUrl $AppBase
$cases = @(
    @{ name = "home.page"; method = "GET"; url = "$WebBase/" },
    @{ name = "home.api"; method = "GET"; url = "$AppBase/home/content" },
    @{ name = "search.page"; method = "GET"; url = "$WebBase/search?keyword=手机" },
    @{ name = "search.api"; method = "GET"; url = "$AppBase/search/product?keyword=手机&pageNum=1&pageSize=20" },
    @{ name = "detail.page"; method = "GET"; url = "$WebBase/product/$sampleProductId" },
    @{ name = "detail.api"; method = "GET"; url = "$AppBase/product/detail/$sampleProductId" },
    @{ name = "cart.page"; method = "GET"; url = "$WebBase/cart" },
    @{ name = "order.confirm.page"; method = "GET"; url = "$WebBase/order/confirm" }
)

$results = @()
foreach ($case in $cases) {
    Write-Host "sampling $($case.name) ..." -ForegroundColor Yellow
    $results += Measure-RequestSamples -Name $case.name -Method $case.method -Url $case.url -Count $Samples -Timeout $TimeoutSec
}

$report = [ordered]@{
    generated_at = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
    mode = $Mode
    web_base = $WebBase
    app_base = $AppBase
    sample_product_id = $sampleProductId
    samples_per_case = $Samples
    timeout_sec = $TimeoutSec
    cases = $results
}

$report | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath $outputPath -Encoding UTF8
Write-Host "report: $outputPath" -ForegroundColor Green

Write-DiffMarkdown -BaselineFile $baselinePath -AfterFile $afterPath -OutFile $diffPath
if (Test-Path -LiteralPath $diffPath) {
    Write-Host "diff: $diffPath" -ForegroundColor Green
}
