<#
.SYNOPSIS
  检查 frontend apps 下 src 目录是否存在影子 .js 文件（与 .ts/.vue 并存风险）。
.DESCRIPTION
  默认扫描：
    - frontend/apps/mall-app-web/src
    - frontend/apps/mall-admin-web/src
  发现任意 .js 文件时返回退出码 1（可通过 -NoFail 覆盖）。
#>
param(
    [string]$ReportPath = "",
    [switch]$NoFail,
    [switch]$Fix
)

$ErrorActionPreference = "Stop"
$SCRIPT_ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $SCRIPT_ROOT

if ([string]::IsNullOrWhiteSpace($ReportPath)) {
    $ReportPath = Join-Path $PROJECT_ROOT "runtime-logs/shadow-js-check.json"
} elseif (-not [System.IO.Path]::IsPathRooted($ReportPath)) {
    $ReportPath = Join-Path $PROJECT_ROOT $ReportPath
}

$targets = @(
    (Join-Path $PROJECT_ROOT "frontend/apps/mall-app-web/src")
    (Join-Path $PROJECT_ROOT "frontend/apps/mall-admin-web/src")
)

function Get-ShadowHits {
    param([string[]]$ScanTargets)

    $items = @()
    foreach ($target in $ScanTargets) {
        if (-not (Test-Path -LiteralPath $target)) {
            continue
        }
        Get-ChildItem -LiteralPath $target -Recurse -File -Filter *.js | ForEach-Object {
            $items += [PSCustomObject]@{
                full_path = $_.FullName
                path = $_.FullName.Replace('\', '/')
                size = $_.Length
            }
        }
    }
    return $items
}

$beforeHits = Get-ShadowHits -ScanTargets $targets
$fixedPaths = @()

if ($Fix -and $beforeHits.Count -gt 0) {
    foreach ($hit in $beforeHits) {
        if (Test-Path -LiteralPath $hit.full_path) {
            Remove-Item -LiteralPath $hit.full_path -Force
            $fixedPaths += $hit.path
        }
    }
}

$afterHits = Get-ShadowHits -ScanTargets $targets

$report = [ordered]@{
    checked_at = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
    target_count = $targets.Count
    before_fix_count = $beforeHits.Count
    after_fix_count = $afterHits.Count
    fixed_count = $fixedPaths.Count
    hit_count = $afterHits.Count
    fixed_paths = $fixedPaths
    hits = @($afterHits | Select-Object path, size)
}

$reportDir = Split-Path -Parent $ReportPath
if ($reportDir -and -not (Test-Path -LiteralPath $reportDir)) {
    New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
}
$report | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath $ReportPath -Encoding UTF8

if ($afterHits.Count -eq 0) {
    if ($Fix -and $beforeHits.Count -gt 0) {
        Write-Host "[OK] shadow js fixed: $($fixedPaths.Count)" -ForegroundColor Green
    } else {
        Write-Host "[OK] no shadow js under frontend src directories." -ForegroundColor Green
    }
    Write-Host "report: $ReportPath" -ForegroundColor DarkGray
    exit 0
}

if ($NoFail) {
    Write-Host "[WARN] shadow js found: $($afterHits.Count)" -ForegroundColor Yellow
    $afterHits | ForEach-Object { Write-Host "  - $($_.path)" -ForegroundColor DarkYellow }
    Write-Host "report: $ReportPath" -ForegroundColor DarkGray
    exit 0
}

Write-Host "[FAIL] shadow js found: $($afterHits.Count)" -ForegroundColor Red
$afterHits | ForEach-Object { Write-Host "  - $($_.path)" -ForegroundColor DarkYellow }
Write-Host "report: $ReportPath" -ForegroundColor DarkGray
exit 1
