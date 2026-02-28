<#
.SYNOPSIS
  停止前端生产模式静态服务（start-fe-prod.ps1 启动的 node 进程）。
#>
param()

$ErrorActionPreference = "Continue"
$SCRIPT_ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $SCRIPT_ROOT
$RUNTIME_LOG_DIR = Join-Path $PROJECT_ROOT "runtime-logs"
$PID_FILE = Join-Path $RUNTIME_LOG_DIR "frontend-prod-pids.txt"

if (Test-Path -LiteralPath $PID_FILE) {
    Get-Content -LiteralPath $PID_FILE -ErrorAction SilentlyContinue | ForEach-Object {
        $parts = $_ -split "="
        if ($parts.Count -lt 2) { return }
        $pidVal = 0
        if ([int]::TryParse($parts[1].Trim(), [ref]$pidVal) -and $pidVal -gt 0) {
            try {
                taskkill /PID $pidVal /T /F 2>&1 | Out-Null
                Write-Host "stopped pid=$pidVal" -ForegroundColor Green
            } catch {
                Write-Host "skip pid=$pid (already stopped)" -ForegroundColor DarkGray
            }
        }
    }
    Remove-Item -LiteralPath $PID_FILE -Force -ErrorAction SilentlyContinue
}

foreach ($port in @(8090, 8091)) {
    $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($conn) {
        try {
            taskkill /PID $conn.OwningProcess /T /F 2>&1 | Out-Null
            Write-Host "killed listener on :$port" -ForegroundColor Yellow
        } catch {}
    }
}

Write-Host "frontend prod servers stopped." -ForegroundColor Green
