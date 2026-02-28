<#
.SYNOPSIS
  构建并启动前端生产模式静态服务（同源代理 /api 与 /admin-api）。
.DESCRIPTION
  Usage:
    .\start-fe-prod.ps1
    .\start-fe-prod.ps1 -App app
    .\start-fe-prod.ps1 -SkipBuild
#>
param(
    [ValidateSet("all","app","admin")]
    [string]$App = "all",
    [switch]$SkipBuild,
    [int]$WaitSec = 60,
    [switch]$NoWait
)

$ErrorActionPreference = "Stop"
$SCRIPT_ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $SCRIPT_ROOT
$FRONTEND_ROOT = Join-Path $PROJECT_ROOT "frontend"
$RUNTIME_LOG_DIR = Join-Path $PROJECT_ROOT "runtime-logs"
$PID_FILE = Join-Path $RUNTIME_LOG_DIR "frontend-prod-pids.txt"
$SERVER_SCRIPT = Join-Path $PROJECT_ROOT "tools/scripts/serve-static-proxy.mjs"

. (Join-Path $SCRIPT_ROOT "_lib.ps1")
Ensure-LogDir $RUNTIME_LOG_DIR

$allTargets = @(
    @{
        key = "admin"
        name = "mall-admin-web-prod"
        port = 8090
        buildCmd = "build:admin"
        distPath = Join-Path $FRONTEND_ROOT "apps/mall-admin-web/dist"
        logFile = Join-Path $RUNTIME_LOG_DIR "fe-admin-prod.log"
        errFile = Join-Path $RUNTIME_LOG_DIR "fe-admin-prod-error.log"
    },
    @{
        key = "app"
        name = "mall-app-web-prod"
        port = 8091
        buildCmd = "build:app"
        distPath = Join-Path $FRONTEND_ROOT "apps/mall-app-web/dist"
        logFile = Join-Path $RUNTIME_LOG_DIR "fe-app-prod.log"
        errFile = Join-Path $RUNTIME_LOG_DIR "fe-app-prod-error.log"
    }
)

$targets = if ($App -eq "all") { $allTargets } else { @($allTargets | Where-Object { $_.key -eq $App }) }

# -- 工具函数由 _lib.ps1 提供（Invoke-KillPort / Invoke-KillPidFile /
#    Wait-AllPortsOpen / Show-LogTail / Ensure-LogDir）---------------------

Write-Host "===== Mall V3 前端生产模式启动 =====" -ForegroundColor Cyan
Write-Host "目标: $($targets.name -join ', ')" -ForegroundColor DarkGray

if (-not (Test-Path -LiteralPath $SERVER_SCRIPT)) {
    throw "缺少静态服务脚本: $SERVER_SCRIPT"
}

Invoke-KillPidFile -path $PID_FILE
foreach ($target in $targets) {
    Invoke-KillPort -port $target.port -label $target.name
}

if (-not $SkipBuild) {
    if ($targets.Count -le 1) {
        Write-Host "[1/3] 构建前端产物..." -ForegroundColor Yellow
        Push-Location $FRONTEND_ROOT
        try {
            Write-Host "  pnpm $($targets[0].buildCmd)" -ForegroundColor DarkGray
            & pnpm $targets[0].buildCmd
            if ($LASTEXITCODE -ne 0) { throw "前端构建失败: $($targets[0].buildCmd)" }
        } finally { Pop-Location }
    } else {
        Write-Host "[1/3] 并行构建前端产物 ($($targets.Count) 个目标)..." -ForegroundColor Yellow
        $buildJobs = $targets | ForEach-Object {
            $cmd = $_.buildCmd; $dir = $FRONTEND_ROOT
            Start-Job -ScriptBlock {
                param($feDir, $buildCmd)
                Set-Location $feDir
                $out = & pnpm $buildCmd 2>&1
                return @{ Cmd=$buildCmd; ExitCode=$LASTEXITCODE; Output=$out }
            } -ArgumentList $dir, $cmd
        }
        Write-Host "  等待 $($buildJobs.Count) 个并行构建完成..." -ForegroundColor DarkGray
        $buildJobs | Wait-Job | Out-Null
        $failed = @()
        foreach ($j in $buildJobs) {
            $r = Receive-Job $j -ErrorAction SilentlyContinue
            if ($r -and $r.ExitCode -ne 0) {
                $failed += $r.Cmd
                Write-Host "  [FAIL] $($r.Cmd)" -ForegroundColor Red
                $r.Output | Select-Object -Last 20 | ForEach-Object { Write-Host "    $_" -ForegroundColor DarkGray }
            } else {
                Write-Host "  [OK] $($r.Cmd)" -ForegroundColor Green
            }
            Remove-Job $j -Force
        }
        if ($failed.Count -gt 0) { throw "前端构建失败: $($failed -join ', ')" }
    }
} else {
    Write-Host "[1/3] 跳过构建 (-SkipBuild)" -ForegroundColor DarkGray
}

Write-Host "[2/3] 启动静态服务..." -ForegroundColor Yellow
$pidLines = @()
foreach ($target in $targets) {
    if (-not (Test-Path -LiteralPath $target.distPath)) {
        throw "缺少 dist 目录: $($target.distPath)"
    }
    if (Test-Path -LiteralPath $target.logFile) { Clear-Content -LiteralPath $target.logFile -ErrorAction SilentlyContinue }
    if (Test-Path -LiteralPath $target.errFile) { Clear-Content -LiteralPath $target.errFile -ErrorAction SilentlyContinue }

    $args = @(
        $SERVER_SCRIPT,
        "--root", $target.distPath,
        "--port", "$($target.port)",
        "--api-target", "http://localhost:18080",
        "--admin-api-target", "http://localhost:18081",
        "--name", $target.name
    )
    $proc = Start-Process -FilePath "node" -WorkingDirectory $PROJECT_ROOT `
        -ArgumentList $args `
        -RedirectStandardOutput $target.logFile `
        -RedirectStandardError $target.errFile `
        -WindowStyle Hidden -PassThru

    $target.ProcId = $proc.Id
    $pidLines += "$($target.name)=$($proc.Id)"
    Write-Host "  $($target.name) pid=$($proc.Id) port=$($target.port)" -ForegroundColor Green
}
$pidLines | Set-Content -LiteralPath $PID_FILE -Encoding UTF8

Write-Host "[3/3] 等待端口就绪..." -ForegroundColor Yellow
if (-not $NoWait) {
    # 并行等待所有端口（单轮询循环， admin + app 同时检测）
    $portResults = Wait-AllPortsOpen -Services ($targets | ForEach-Object {
        @{ Name=$_.name; Port=[int]$_.port; ProcId=[int]$(if ($_.ProcId) { $_.ProcId } else { 0 }) }
    }) -TimeoutSec $WaitSec

    foreach ($target in $targets) {
        if ($portResults[[int]$target.port]) {
            Write-Host "  [OK] http://localhost:$($target.port)" -ForegroundColor Green
        } else {
            Write-Host "  [FAIL] $($target.name) 端口 $($target.port) 未就绪" -ForegroundColor Red
            Show-LogTail -path $target.logFile -n 20
            Show-LogTail -path $target.errFile -n 20
            exit 1
        }
    }
}

Write-Host "完成。PID 文件: $PID_FILE" -ForegroundColor Green
