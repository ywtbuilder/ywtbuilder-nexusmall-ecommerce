<#
.SYNOPSIS
  Mall V3 - Restart Frontend Dev Servers
.DESCRIPTION
  Usage: .\restart-fe.ps1 [-App all|app|admin] [-WaitSec 60] [-NoWait]

  Drag to terminal and press Enter.
    .\restart-fe.ps1               Restart both frontend dev servers
    .\restart-fe.ps1 -App admin    Only restart mall-admin-web
    .\restart-fe.ps1 -App app      Only restart mall-app-web
    .\restart-fe.ps1 -NoWait       Start without waiting for ready
#>
param(
    [ValidateSet("all","app","admin")]
    [string]$App = "all",
    [int]$WaitSec = 60,
    [switch]$NoWait
)

$ErrorActionPreference = "Continue"
$ROOT         = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $ROOT
$FRONTEND_DIR = Join-Path $PROJECT_ROOT "frontend"
$LOG_DIR      = Join-Path $PROJECT_ROOT "runtime-logs"
$PID_FILE     = Join-Path $LOG_DIR "frontend-pids.txt"

. (Join-Path $ROOT "_lib.ps1")
Ensure-LogDir $LOG_DIR

$ALL_APPS = @(
    @{ Key="admin"; Name="mall-admin-web"; Port=8090; Cmd="dev:admin"; Log="fe-admin.log"; ErrLog="fe-admin-error.log" },
    @{ Key="app";   Name="mall-app-web";  Port=8091; Cmd="dev:app";   Log="fe-app.log";   ErrLog="fe-app-error.log" }
)

$TARGETS = if ($App -eq "all") { $ALL_APPS } else {
    @($ALL_APPS | Where-Object { $_.Key -eq $App })
}

# -- 工具函数由 _lib.ps1 提供（Write-Step / Invoke-KillPort / Invoke-KillPidFile /
#    Kill-FrontendByCmdline / Wait-AllPortsOpen / Show-LogTail / Ensure-LogDir）------

# ================================================================
$T0       = Get-Date
$appNames = ($TARGETS | ForEach-Object { $_.Name }) -join ", "

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Mall V3 - 重启前端  [$App]" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  目标:  $appNames" -ForegroundColor DarkGray

# -- 步骤 1：停止 --
Write-Step "[ 1/3 ] 停止现有前端进程..."
Invoke-KillPidFile -path $PID_FILE
foreach ($a in $TARGETS) {
    Invoke-KillPort -port $a.Port -label $a.Name
    Kill-FrontendByCmdline -keyword $a.Cmd
}
Write-Host "  旧进程已清理。" -ForegroundColor Green

# -- 步骤 2：启动 --
Write-Step "[ 2/3 ] 启动前端开发服务器..."
if (-not (Test-Path $FRONTEND_DIR)) {
    Write-Host "  错误: frontend/ 目录不存在: $FRONTEND_DIR" -ForegroundColor Red
    exit 1
}

$startedPids = @()
$pidLines    = @()

foreach ($a in $TARGETS) {
    $logPath = Join-Path $LOG_DIR $a.Log
    $errPath = Join-Path $LOG_DIR $a.ErrLog
    @($logPath, $errPath) | ForEach-Object {
        if (Test-Path $_) { Clear-Content $_ -ErrorAction SilentlyContinue }
    }
    Write-Host "  正在启动 $($a.Name)  :$($a.Port)  " -NoNewline -ForegroundColor Cyan
    $proc = Start-Process -FilePath "cmd.exe" `
        -ArgumentList "/c `"cd /d `"$FRONTEND_DIR`" && pnpm $($a.Cmd)`"" `
        -RedirectStandardOutput $logPath `
        -RedirectStandardError  $errPath `
        -WindowStyle Hidden -PassThru
    $startedPids += $proc.Id
    $pidLines    += "$($a.Name)=$($proc.Id)"
    $a.ProcId     = $proc.Id
    Write-Host "pid=$($proc.Id)" -ForegroundColor Green
}

$pidLines | Out-File -FilePath $PID_FILE -Encoding UTF8

# -- Step 3: Health check ------------------------------------------
$results = @()

if ($NoWait) {
    Write-Host "  -NoWait: 跳过就绪检查" -ForegroundColor DarkGray
    foreach ($a in $TARGETS) {
        $results += [PSCustomObject]@{ Name=$a.Name; Port=$a.Port; OK=$true }
    }
} else {
    Write-Step "[ 3/3 ] 等待开发服务器就绪..."
    # 并行等待（admin + app 同时检测，不串行累加等待时间）
    $portResults = Wait-AllPortsOpen -Services ($TARGETS | ForEach-Object {
        @{ Name=$_.Name; Port=$_.Port; ProcId=$_.ProcId }
    }) -TimeoutSec $WaitSec

    foreach ($a in $TARGETS) {
        $ok = $portResults[[int]$a.Port]
        if (-not $ok) {
            Write-Host "  $($a.Name) 日志尾部:" -ForegroundColor Red
            Show-LogTail -path (Join-Path $LOG_DIR $a.Log) -n 15
        }
        $results += [PSCustomObject]@{ Name=$a.Name; Port=$a.Port; OK=$ok }
    }
}

$allOk  = $results | Where-Object { -not $_.OK } | Measure-Object | Select-Object -ExpandProperty Count
$elapsed = [int]((Get-Date) - $T0).TotalSeconds

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
if ($allOk -eq 0) {
    Write-Host "  [✓] 前端重启完成，耗时 ${elapsed}s" -ForegroundColor Green
} else {
    Write-Host "  [⚠] 部分应用启动失败 (${elapsed}s)" -ForegroundColor Red
}
foreach ($r in $results) {
    $icon = if ($r.OK) { "[v]" } else { "[x]" }
    $col  = if ($r.OK) { "Green" } else { "Red" }
    Write-Host ("  {0} {1,-22} http://localhost:{2}" -f $icon, $r.Name, $r.Port) -ForegroundColor $col
}
Write-Host "  日志:  $LOG_DIR" -ForegroundColor DarkGray
Write-Host "========================================" -ForegroundColor Cyan

if ($allOk -gt 0) { exit 1 }
