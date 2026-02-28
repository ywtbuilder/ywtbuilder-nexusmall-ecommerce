<#
.SYNOPSIS
  Audit visible resource origins for Mall V3.
.DESCRIPTION
  Valid sources:
    1) /api/asset/image/{hash}
    2) data:image/*
    3) relative local paths for non-media fields

  This script audits:
    - sms_home_advertise.pic
    - pms_product.pic / album_pics / detail_html
    - pms_brand.logo / big_pic
    - ums_member.icon / avatar_url
    - ums_admin.icon
    - frontend source code external URLs
    - runtime /home/content and home HTML external hosts
#>

param(
    [string]$DbContainer = "mallv3-mysql",
    [string]$DbUser = "root",
    [string]$DbPassword = "root",
    [string]$DbName = "mall",
    [string]$WebBase = "http://localhost:8091",
    [string]$ReportPath = "./runtime-logs/resource-origin-audit.json",
    [switch]$FailOnViolation
)

$ErrorActionPreference = "Stop"
$ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $ROOT
$RUNTIME_DIR = Join-Path $PROJECT_ROOT "runtime-logs"

function Ensure-Directory([string]$path) {
    if (-not (Test-Path -LiteralPath $path)) {
        New-Item -ItemType Directory -Path $path -Force | Out-Null
    }
}

function Resolve-PathInProject([string]$path) {
    if ([System.IO.Path]::IsPathRooted($path)) {
        return [System.IO.Path]::GetFullPath($path)
    }
    return [System.IO.Path]::GetFullPath((Join-Path $PROJECT_ROOT $path))
}

function Invoke-ContainerSql([string]$sqlText) {
    Ensure-Directory $RUNTIME_DIR
    $stamp = Get-Date -Format "yyyyMMddHHmmssfff"
    $localSql = Join-Path $RUNTIME_DIR "tmp-resource-origin-audit-$stamp.sql"
    $containerSql = "/tmp/resource-origin-audit-$stamp.sql"

    Set-Content -LiteralPath $localSql -Value $sqlText -Encoding UTF8
    try {
        & docker cp $localSql "$DbContainer`:$containerSql" | Out-Null
        $output = & docker exec $DbContainer mysql "-u$DbUser" "-p$DbPassword" "--default-character-set=utf8mb4" "-N" "-e" "source $containerSql" $DbName 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "MySQL query failed: $($output -join "`n")"
        }
        return @($output | ForEach-Object { "$_" } | Where-Object { $_ -and ($_ -notmatch '^mysql: \[Warning\]') })
    } finally {
        Remove-Item -LiteralPath $localSql -Force -ErrorAction SilentlyContinue
        & docker exec $DbContainer rm -f $containerSql | Out-Null
    }
}

function Parse-TabLine([string]$line, [int]$maxParts = 3) {
    if ([string]::IsNullOrWhiteSpace($line)) { return @() }
    return $line -split "`t", $maxParts
}

function Get-SummaryStats {
    $sql = @'
SELECT JSON_OBJECT(
  'adv_total', (
    SELECT COUNT(*) FROM sms_home_advertise WHERE status = 1
  ),
  'adv_compliant', (
    SELECT SUM(CASE WHEN pic LIKE '/api/asset/image/%' OR pic LIKE 'data:image/%' THEN 1 ELSE 0 END)
    FROM sms_home_advertise WHERE status = 1
  ),
  'prod_pic_total', (
    SELECT COUNT(*) FROM pms_product WHERE pic IS NOT NULL AND pic <> ''
  ),
  'prod_pic_compliant', (
    SELECT COALESCE(SUM(CASE WHEN pic REGEXP '^https?://' THEN 0 ELSE 1 END), 0)
    FROM pms_product WHERE pic IS NOT NULL AND pic <> ''
  ),
  'prod_album_total', (
    SELECT COUNT(*) FROM pms_product WHERE album_pics IS NOT NULL AND album_pics <> ''
  ),
  'prod_album_compliant', (
    SELECT COALESCE(SUM(CASE
      WHEN album_pics REGEXP 'https?://' THEN 0 ELSE 1 END), 0)
    FROM pms_product WHERE album_pics IS NOT NULL AND album_pics <> ''
  ),
  'prod_detail_total', (
    SELECT COUNT(*) FROM pms_product WHERE detail_html IS NOT NULL AND detail_html <> ''
  ),
  'prod_detail_compliant', (
    SELECT COALESCE(SUM(CASE
      WHEN detail_html REGEXP 'https?://' THEN 0 ELSE 1 END), 0)
    FROM pms_product WHERE detail_html IS NOT NULL AND detail_html <> ''
  ),
  'brand_logo_total', (
    SELECT COUNT(*) FROM pms_brand WHERE logo IS NOT NULL AND logo <> ''
  ),
  'brand_logo_compliant', (
    SELECT SUM(CASE WHEN logo REGEXP '^https?://' THEN 0 ELSE 1 END)
    FROM pms_brand WHERE logo IS NOT NULL AND logo <> ''
  ),
  'brand_big_pic_total', (
    SELECT COUNT(*) FROM pms_brand WHERE big_pic IS NOT NULL AND big_pic <> ''
  ),
  'brand_big_pic_compliant', (
    SELECT SUM(CASE WHEN big_pic REGEXP '^https?://' THEN 0 ELSE 1 END)
    FROM pms_brand WHERE big_pic IS NOT NULL AND big_pic <> ''
  ),
  'member_icon_total', (
    SELECT COUNT(*) FROM ums_member WHERE icon IS NOT NULL AND icon <> ''
  ),
  'member_icon_compliant', (
    SELECT SUM(CASE WHEN icon REGEXP '^https?://' THEN 0 ELSE 1 END)
    FROM ums_member WHERE icon IS NOT NULL AND icon <> ''
  ),
  'member_avatar_total', (
    SELECT COUNT(*) FROM ums_member WHERE avatar_url IS NOT NULL AND avatar_url <> ''
  ),
  'member_avatar_compliant', (
    SELECT SUM(CASE WHEN avatar_url REGEXP '^https?://' THEN 0 ELSE 1 END)
    FROM ums_member WHERE avatar_url IS NOT NULL AND avatar_url <> ''
  ),
  'admin_icon_total', (
    SELECT COUNT(*) FROM ums_admin WHERE icon IS NOT NULL AND icon <> ''
  ),
  'admin_icon_compliant', (
    SELECT SUM(CASE WHEN icon REGEXP '^https?://' THEN 0 ELSE 1 END)
    FROM ums_admin WHERE icon IS NOT NULL AND icon <> ''
  )
) AS payload;
'@
    $lines = Invoke-ContainerSql -sqlText $sql
    $jsonLine = ($lines | Select-Object -Last 1)
    if ([string]::IsNullOrWhiteSpace($jsonLine)) {
        throw "Summary query returned empty result."
    }
    return $jsonLine | ConvertFrom-Json -AsHashtable -DateKind String
}

function Get-FieldViolations([string]$tableName, [string]$idExpr, [string]$fieldExpr, [string]$whereClause, [int]$maxRows = 20) {
    $sql = @"
SELECT $idExpr, $fieldExpr
FROM $tableName
WHERE $whereClause
ORDER BY 1
LIMIT $maxRows;
"@
    $lines = Invoke-ContainerSql -sqlText $sql
    $rows = @()
    foreach ($line in $lines) {
        $parts = Parse-TabLine -line $line -maxParts 2
        if ($parts.Count -ge 2) {
            $rows += [ordered]@{
                id = [string]$parts[0]
                value = [string]$parts[1]
            }
        }
    }
    return $rows
}

function Add-Violation([ref]$bucket, [string]$name, [int]$count, [object[]]$samples) {
    if ($count -le 0) { return }
    $bucket.Value += [ordered]@{
        name = $name
        count = $count
        samples = $samples
    }
}

function Is-AllowedExternalSourceUrl([string]$url) {
    if ([string]::IsNullOrWhiteSpace($url)) { return $true }
    if ($url -match '^https?://www\.w3\.org/') { return $true }
    if ($url -match '^https?://(localhost|127\.0\.0\.1)(:\d+)?/') { return $true }
    return $false
}

function Run-SourceScan {
    $targets = @(
        (Join-Path $PROJECT_ROOT "frontend/apps/mall-app-web/src"),
        (Join-Path $PROJECT_ROOT "frontend/apps/mall-app-web/index.html"),
        (Join-Path $PROJECT_ROOT "frontend/apps/mall-admin-web/src"),
        (Join-Path $PROJECT_ROOT "frontend/apps/mall-admin-web/index.html")
    )
    $urlRegex = [regex]'https?://[^\s"''<>)]+' 
    $hits = @()
    foreach ($target in $targets) {
        if (-not (Test-Path -LiteralPath $target)) { continue }
        $files = @()
        $item = Get-Item -LiteralPath $target
        if ($item.PSIsContainer) {
            $files = Get-ChildItem -LiteralPath $target -Recurse -File
        } else {
            $files = @($item)
        }

        foreach ($file in $files) {
            $lineNo = 0
            foreach ($line in (Get-Content -LiteralPath $file.FullName -ErrorAction SilentlyContinue)) {
                $lineNo += 1
                $matches = $urlRegex.Matches($line)
                foreach ($m in $matches) {
                    $candidate = $m.Value
                    if (Is-AllowedExternalSourceUrl $candidate) { continue }
                    $hits += "{0}:{1}:{2}" -f $file.FullName, $lineNo, $candidate
                }
            }
        }
    }
    return @($hits | Select-Object -Unique)
}

function Get-ExternalHostsFromRuntime {
    param(
        [string]$WebBaseUrl
    )
    $hosts = New-Object System.Collections.Generic.HashSet[string]
    $samples = @()
    $urlRegex = [regex]'https?://[^\s"''<>)]+' 
    $allowedHosts = @("localhost", "127.0.0.1", "::1")

    try {
        $homeResp = Invoke-WebRequest -UseBasicParsing -Uri "$WebBaseUrl/" -TimeoutSec 12 -ErrorAction Stop
        $homeHtml = if ($homeResp.Content -is [byte[]]) { [System.Text.Encoding]::UTF8.GetString($homeResp.Content) } else { [string]$homeResp.Content }
        foreach ($m in $urlRegex.Matches($homeHtml)) {
            $u = $m.Value
            try {
                $uri = [uri]$u
                if ($allowedHosts -notcontains $uri.Host.ToLowerInvariant()) {
                    $hosts.Add($uri.Host.ToLowerInvariant()) | Out-Null
                    if ($samples.Count -lt 20) { $samples += $u }
                }
            } catch {}
        }
    } catch {}

    try {
        $apiResp = Invoke-WebRequest -UseBasicParsing -Uri "$WebBaseUrl/api/home/content" -TimeoutSec 15 -ErrorAction Stop
        $jsonText = if ($apiResp.Content -is [byte[]]) { [System.Text.Encoding]::UTF8.GetString($apiResp.Content) } else { [string]$apiResp.Content }
        $obj = $jsonText | ConvertFrom-Json

        function Walk-Object([object]$node) {
            if ($null -eq $node) { return }
            if ($node -is [string]) {
                if ($node -match '^https?://') {
                    try {
                        $uri = [uri]$node
                        if ($allowedHosts -notcontains $uri.Host.ToLowerInvariant()) {
                            $hosts.Add($uri.Host.ToLowerInvariant()) | Out-Null
                            if ($samples.Count -lt 20) { $samples += $node }
                        }
                    } catch {}
                }
                return
            }

            if ($node -is [System.Collections.IEnumerable] -and -not ($node -is [string])) {
                foreach ($item in $node) { Walk-Object $item }
                return
            }

            $props = $node.PSObject.Properties
            foreach ($p in $props) {
                Walk-Object $p.Value
            }
        }

        Walk-Object $obj
    } catch {}

    return [ordered]@{
        hosts = @($hosts | Sort-Object)
        samples = @($samples | Select-Object -Unique | Select-Object -First 20)
    }
}

Ensure-Directory $RUNTIME_DIR
$reportFile = Resolve-PathInProject $ReportPath
Ensure-Directory (Split-Path -Parent $reportFile)

$summary = Get-SummaryStats

$advTotal = [int]($summary["adv_total"] ?? 0)
$advCompliant = [int]($summary["adv_compliant"] ?? 0)
$prodPicTotal = [int]($summary["prod_pic_total"] ?? 0)
$prodPicCompliant = [int]($summary["prod_pic_compliant"] ?? 0)
$prodAlbumTotal = [int]($summary["prod_album_total"] ?? 0)
$prodAlbumCompliant = [int]($summary["prod_album_compliant"] ?? 0)
$prodDetailTotal = [int]($summary["prod_detail_total"] ?? 0)
$prodDetailCompliant = [int]($summary["prod_detail_compliant"] ?? 0)

$brandLogoTotal = [int]($summary["brand_logo_total"] ?? 0)
$brandLogoCompliant = [int]($summary["brand_logo_compliant"] ?? 0)
$brandBigPicTotal = [int]($summary["brand_big_pic_total"] ?? 0)
$brandBigPicCompliant = [int]($summary["brand_big_pic_compliant"] ?? 0)
$memberIconTotal = [int]($summary["member_icon_total"] ?? 0)
$memberIconCompliant = [int]($summary["member_icon_compliant"] ?? 0)
$memberAvatarTotal = [int]($summary["member_avatar_total"] ?? 0)
$memberAvatarCompliant = [int]($summary["member_avatar_compliant"] ?? 0)
$adminIconTotal = [int]($summary["admin_icon_total"] ?? 0)
$adminIconCompliant = [int]($summary["admin_icon_compliant"] ?? 0)

$violations = @()

$advViolationCount = [Math]::Max(0, $advTotal - $advCompliant)
if ($advViolationCount -gt 0) {
    $samples = Get-FieldViolations -tableName "sms_home_advertise" -idExpr "id" -fieldExpr "pic" -whereClause "status=1 AND NOT (pic LIKE '/api/asset/image/%' OR pic LIKE 'data:image/%')"
    Add-Violation -bucket ([ref]$violations) -name "sms_home_advertise.pic" -count $advViolationCount -samples $samples
}

$picViolationCount = [Math]::Max(0, $prodPicTotal - $prodPicCompliant)
if ($picViolationCount -gt 0) {
    $samples = Get-FieldViolations -tableName "pms_product" -idExpr "id" -fieldExpr "pic" -whereClause "pic IS NOT NULL AND pic<>'' AND pic REGEXP '^https?://'"
    Add-Violation -bucket ([ref]$violations) -name "pms_product.pic" -count $picViolationCount -samples $samples
}

$albumViolationCount = [Math]::Max(0, $prodAlbumTotal - $prodAlbumCompliant)
if ($albumViolationCount -gt 0) {
    $samples = Get-FieldViolations -tableName "pms_product" -idExpr "id" -fieldExpr "album_pics" -whereClause "album_pics IS NOT NULL AND album_pics<>'' AND album_pics REGEXP 'https?://'"
    Add-Violation -bucket ([ref]$violations) -name "pms_product.album_pics" -count $albumViolationCount -samples $samples
}

$detailViolationCount = [Math]::Max(0, $prodDetailTotal - $prodDetailCompliant)
if ($detailViolationCount -gt 0) {
    $samples = Get-FieldViolations -tableName "pms_product" -idExpr "id" -fieldExpr "LEFT(detail_html,120)" -whereClause "detail_html IS NOT NULL AND detail_html<>'' AND detail_html REGEXP 'https?://'"
    Add-Violation -bucket ([ref]$violations) -name "pms_product.detail_html" -count $detailViolationCount -samples $samples
}

$brandLogoViolation = [Math]::Max(0, $brandLogoTotal - $brandLogoCompliant)
if ($brandLogoViolation -gt 0) {
    $samples = Get-FieldViolations -tableName "pms_brand" -idExpr "id" -fieldExpr "logo" -whereClause "logo IS NOT NULL AND logo<>'' AND logo REGEXP '^https?://'"
    Add-Violation -bucket ([ref]$violations) -name "pms_brand.logo" -count $brandLogoViolation -samples $samples
}

$brandBigPicViolation = [Math]::Max(0, $brandBigPicTotal - $brandBigPicCompliant)
if ($brandBigPicViolation -gt 0) {
    $samples = Get-FieldViolations -tableName "pms_brand" -idExpr "id" -fieldExpr "big_pic" -whereClause "big_pic IS NOT NULL AND big_pic<>'' AND big_pic REGEXP '^https?://'"
    Add-Violation -bucket ([ref]$violations) -name "pms_brand.big_pic" -count $brandBigPicViolation -samples $samples
}

$memberIconViolation = [Math]::Max(0, $memberIconTotal - $memberIconCompliant)
if ($memberIconViolation -gt 0) {
    $samples = Get-FieldViolations -tableName "ums_member" -idExpr "id" -fieldExpr "icon" -whereClause "icon IS NOT NULL AND icon<>'' AND icon REGEXP '^https?://'"
    Add-Violation -bucket ([ref]$violations) -name "ums_member.icon" -count $memberIconViolation -samples $samples
}

$memberAvatarViolation = [Math]::Max(0, $memberAvatarTotal - $memberAvatarCompliant)
if ($memberAvatarViolation -gt 0) {
    $samples = Get-FieldViolations -tableName "ums_member" -idExpr "id" -fieldExpr "avatar_url" -whereClause "avatar_url IS NOT NULL AND avatar_url<>'' AND avatar_url REGEXP '^https?://'"
    Add-Violation -bucket ([ref]$violations) -name "ums_member.avatar_url" -count $memberAvatarViolation -samples $samples
}

$adminIconViolation = [Math]::Max(0, $adminIconTotal - $adminIconCompliant)
if ($adminIconViolation -gt 0) {
    $samples = Get-FieldViolations -tableName "ums_admin" -idExpr "id" -fieldExpr "icon" -whereClause "icon IS NOT NULL AND icon<>'' AND icon REGEXP '^https?://'"
    Add-Violation -bucket ([ref]$violations) -name "ums_admin.icon" -count $adminIconViolation -samples $samples
}

$sourceScanMatches = Run-SourceScan
if ($sourceScanMatches.Count -gt 0) {
    Add-Violation -bucket ([ref]$violations) -name "frontend_source_scan" -count $sourceScanMatches.Count -samples @($sourceScanMatches | Select-Object -First 30)
}

$runtimeHosts = Get-ExternalHostsFromRuntime -WebBaseUrl $WebBase
if ($runtimeHosts.hosts.Count -gt 0) {
    Add-Violation -bucket ([ref]$violations) -name "runtime_home_external_hosts" -count $runtimeHosts.hosts.Count -samples $runtimeHosts.samples
}

$report = [ordered]@{
    generated_at = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ssK")
    parameters = [ordered]@{
        db_container = $DbContainer
        db_name = $DbName
        web_base = $WebBase
        report_path = $reportFile
        fail_on_violation = [bool]$FailOnViolation
    }
    summary = [ordered]@{
        advertise_total = $advTotal
        advertise_compliant = $advCompliant
        product_pic_total = $prodPicTotal
        product_pic_compliant = $prodPicCompliant
        product_album_total = $prodAlbumTotal
        product_album_compliant = $prodAlbumCompliant
        product_detail_total = $prodDetailTotal
        product_detail_compliant = $prodDetailCompliant
        brand_logo_total = $brandLogoTotal
        brand_logo_compliant = $brandLogoCompliant
        brand_big_pic_total = $brandBigPicTotal
        brand_big_pic_compliant = $brandBigPicCompliant
        member_icon_total = $memberIconTotal
        member_icon_compliant = $memberIconCompliant
        member_avatar_total = $memberAvatarTotal
        member_avatar_compliant = $memberAvatarCompliant
        admin_icon_total = $adminIconTotal
        admin_icon_compliant = $adminIconCompliant
        source_scan_matches = $sourceScanMatches.Count
        runtime_external_hosts = $runtimeHosts.hosts
    }
    violation_count = $violations.Count
    violations = $violations
}

$reportJson = $report | ConvertTo-Json -Depth 10
Set-Content -LiteralPath $reportFile -Value $reportJson -Encoding UTF8

Write-Host "===== resource origin audit =====" -ForegroundColor Cyan
Write-Host ("advertise compliant: {0}/{1}" -f $advCompliant, $advTotal)
Write-Host ("product.pic compliant: {0}/{1}" -f $prodPicCompliant, $prodPicTotal)
Write-Host ("product.album_pics compliant: {0}/{1}" -f $prodAlbumCompliant, $prodAlbumTotal)
Write-Host ("product.detail_html compliant: {0}/{1}" -f $prodDetailCompliant, $prodDetailTotal)
Write-Host ("brand.logo compliant: {0}/{1}" -f $brandLogoCompliant, $brandLogoTotal)
Write-Host ("brand.big_pic compliant: {0}/{1}" -f $brandBigPicCompliant, $brandBigPicTotal)
Write-Host ("member.icon compliant: {0}/{1}" -f $memberIconCompliant, $memberIconTotal)
Write-Host ("member.avatar_url compliant: {0}/{1}" -f $memberAvatarCompliant, $memberAvatarTotal)
Write-Host ("admin.icon compliant: {0}/{1}" -f $adminIconCompliant, $adminIconTotal)
Write-Host ("source scan matches: {0}" -f $sourceScanMatches.Count)
Write-Host ("runtime external hosts: {0}" -f (($runtimeHosts.hosts -join ", ")))
Write-Host ("report: {0}" -f $reportFile)

if ($violations.Count -gt 0) {
    Write-Host ("violations: {0}" -f $violations.Count) -ForegroundColor Yellow
    foreach ($item in $violations) {
        Write-Host (" - {0}: {1}" -f $item.name, $item.count) -ForegroundColor Yellow
    }
    if ($FailOnViolation) { exit 1 }
} else {
    Write-Host "violations: 0" -ForegroundColor Green
}

exit 0
