<#
.SYNOPSIS
  Mall V3 - Log Viewer (color-coded)
.DESCRIPTION
  Usage: .\logs.ps1 -Service <name> [-Lines 50] [-Follow] [-IncludeErrors]

  Service names:
    app        mall-app-api
    admin      mall-admin-api
    job        mall-job
    fe-app     mall-app-web (frontend)
    fe-admin   mall-admin-web (frontend)
    all        Show all log paths and tail each

  Examples:
    .\logs.ps1 -Service app
    .\logs.ps1 -Service app -Follow
    .\logs.ps1 -Service app -Lines 100
    .\logs.ps1 -Service all -Lines 10
#>
param(
    [Parameter(Mandatory=$false)]
    [ValidateSet("app","admin","job","fe-app","fe-admin","all")]
    [string]$Service = "app",
    [int]$Lines = 50,
    [switch]$Follow,
    [switch]$IncludeErrors
)

$ErrorActionPreference = "Continue"
$ROOT         = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $ROOT
$LOG_DIR      = Join-Path $PROJECT_ROOT "runtime-logs"

$SERVICE_MAP = @{
    "app"      = @("mall-app-api.log",   "mall-app-api-error.log")
    "admin"    = @("mall-admin-api.log", "mall-admin-api-error.log")
    "job"      = @("mall-job.log",       "mall-job-error.log")
    "fe-app"   = @("fe-app.log",         "fe-app-error.log")
    "fe-admin" = @("fe-admin.log",       "fe-admin-error.log")
}

function Write-ColorLine([string]$line) {
    $lower = $line.ToLower()
    if ($line -match '\bERROR\b' -or $line -match 'Exception' -or $line -match 'FATAL') {
        Write-Host $line -ForegroundColor Red
    } elseif ($line -match '\bWARN(ING)?\b') {
        Write-Host $line -ForegroundColor Yellow
    } elseif ($lower -match '\bdebug\b') {
        Write-Host $line -ForegroundColor DarkGray
    } elseif ($line -match '\bINFO\b' -or $line -match '\bStarted\b') {
        Write-Host $line -ForegroundColor White
    } else {
        Write-Host $line -ForegroundColor Gray
    }
}

function Show-LogFile([string]$path, [int]$n, [bool]$follow, [bool]$includeErrors) {
    if (-not (Test-Path $path)) {
        Write-Host "  [x] 文件未找到: $path" -ForegroundColor Red
        return
    }
    $size = (Get-Item $path).Length
    Write-Host ("  [日志] {0}  ({1:N0} 字节)" -f $path, $size) -ForegroundColor Cyan

    if ($follow) {
        Write-Host "  (实时追踪 - 按 Ctrl+C 停止)" -ForegroundColor DarkGray
        Get-Content $path -Tail $n -Wait | ForEach-Object { Write-ColorLine $_ }
    } else {
        Get-Content $path -Tail $n | ForEach-Object { Write-ColorLine $_ }
    }
}

# ================================================================
Write-Host "==============================" -ForegroundColor Cyan
Write-Host "  Mall V3 日志查看器" -ForegroundColor Cyan
Write-Host "==============================" -ForegroundColor Cyan

if ($Service -eq "all") {
    foreach ($key in @("app","admin","job","fe-app","fe-admin")) {
        $pair = $SERVICE_MAP[$key]
        $main = Join-Path $LOG_DIR $pair[0]
        Write-Host ""
        Write-Host "--- [ $key ] ---" -ForegroundColor Yellow
        Write-Host "  $main" -ForegroundColor DarkGray
        if (Test-Path $main) {
            $sz = (Get-Item $main).Length
            $lastLines = Get-Content $main -Tail $Lines -ErrorAction SilentlyContinue
            Write-Host ("  (展示 {0} 行，共 {1:N0} 字节)" -f ($lastLines | Measure-Object).Count, $sz) -ForegroundColor DarkGray
            $lastLines | ForEach-Object { Write-ColorLine $_ }
        } else {
            Write-Host "  (暂无日志文件)" -ForegroundColor DarkGray
        }
    }
} else {
    $pair = $SERVICE_MAP[$Service]
    if (-not $pair) {
        Write-Host "  未知服务: $Service" -ForegroundColor Red
        exit 1
    }
    $mainLog  = Join-Path $LOG_DIR $pair[0]
    $errorLog = Join-Path $LOG_DIR $pair[1]

    # 主日志
    Show-LogFile -path $mainLog -n $Lines -follow $Follow.IsPresent -includeErrors $false

    # 错误日志
    if ($IncludeErrors -and (Test-Path $errorLog)) {
        Write-Host ""
        Write-Host "--- [ 错误输出 ] ---" -ForegroundColor Red
        Show-LogFile -path $errorLog -n ([Math]::Min($Lines, 50)) -follow $false -includeErrors $false
    }
}
