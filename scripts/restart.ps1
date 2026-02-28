<#
.SYNOPSIS
  Mall V3 - Restart Full Stack (Backend + Frontend)
.DESCRIPTION
  Usage: .\restart.ps1 [-SkipBuild] [-SkipFrontend] [-Prod] [-Service all|app|admin|job] [-App all|app|admin]

    .\restart.ps1                   Restart everything (dev frontend)
    .\restart.ps1 -Prod             Restart with production frontend (recommended for users)
    .\restart.ps1 -SkipBuild        Restart with existing JARs
    .\restart.ps1 -SkipFrontend     Only restart backend
    .\restart.ps1 -Service app      Only restart mall-app-api + all frontend
#>
param(
    [switch]$SkipBuild,
    [switch]$SkipFrontend,
    [switch]$SkipBackend,
    [switch]$Prod,
    [ValidateSet("all","app","admin","job")]
    [string]$Service = "all",
    [ValidateSet("all","app","admin")]
    [string]$App = "all",
    [string]$Profile = "local",
    [int]$HealthTimeoutSec = 90
)

$ErrorActionPreference = "Continue"
$ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
$T0   = Get-Date

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Mall V3 - 全栈重启" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

$beOk = $true
$feOk = $true

if (-not $SkipBackend) {
    Write-Host ""
    Write-Host "--- [ 后端 ] ---" -ForegroundColor Yellow

    $beArgs = @{
        Service          = $Service
        Profile          = $Profile
        HealthTimeoutSec = $HealthTimeoutSec
    }
    if ($SkipBuild) { $beArgs["SkipBuild"] = $true }

    & "$ROOT\restart-be.ps1" @beArgs
    $beOk = ($LASTEXITCODE -eq 0)
}

if (-not $SkipFrontend) {
    Write-Host ""
    Write-Host "--- [ 前端 ] ---" -ForegroundColor Yellow

    if ($Prod) {
        Write-Host "  模式: 生产构建 (prod)" -ForegroundColor Green
        $feArgs = @{ App = $App }
        if ($SkipBuild) { $feArgs["SkipBuild"] = $true }
        & "$ROOT\start-fe-prod.ps1" @feArgs
    } else {
        Write-Host "  模式: 开发服务器 (dev)" -ForegroundColor DarkYellow
        $feArgs = @{ App = $App }
        & "$ROOT\restart-fe.ps1" @feArgs
    }
    $feOk = ($LASTEXITCODE -eq 0)
}

$elapsed = [int]((Get-Date) - $T0).TotalSeconds

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  重启汇总  (总耗时 ${elapsed}s)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

if (-not $SkipBackend) {
    $beIcon = if ($beOk) { "[v]" } else { "[x]" }
    $beCol  = if ($beOk) { "Green" } else { "Red" }
    Write-Host ("  {0} 后端   (Service={1})" -f $beIcon, $Service) -ForegroundColor $beCol
}
if (-not $SkipFrontend) {
    $feIcon = if ($feOk) { "[v]" } else { "[x]" }
    $feCol  = if ($feOk) { "Green" } else { "Red" }
    Write-Host ("  {0} 前端   (App={1})" -f $feIcon, $App) -ForegroundColor $feCol
}

Write-Host ""
Write-Host "  快速检查:" -ForegroundColor DarkGray
Write-Host "    Admin UI  http://localhost:8090" -ForegroundColor DarkGray
Write-Host "    App UI    http://localhost:8091" -ForegroundColor DarkGray
Write-Host "    App API   http://localhost:18080" -ForegroundColor DarkGray
Write-Host "    Admin API http://localhost:18081" -ForegroundColor DarkGray
Write-Host "    状态    .\status.ps1" -ForegroundColor DarkGray
Write-Host "========================================" -ForegroundColor Cyan

if (-not $beOk -or -not $feOk) { exit 1 }
