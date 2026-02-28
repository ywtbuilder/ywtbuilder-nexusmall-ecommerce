<#
.SYNOPSIS
  Mall V3 - Service Status Dashboard
.DESCRIPTION
  Usage: .\status.ps1 [-Watch] [-IntervalSec 4]

    .\status.ps1           Show status snapshot once
    .\status.ps1 -Watch    Auto-refresh every 4 seconds (Ctrl+C to stop)
#>
param(
    [switch]$Watch,
    [int]$IntervalSec = 4
)

$ErrorActionPreference = "Continue"
$ROOT         = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $ROOT
$LOG_DIR      = Join-Path $PROJECT_ROOT "runtime-logs"

$DOCKER_CONTAINERS = @(
    @{ Name="mallv3-mysql";    Port=13306 },
    @{ Name="mallv3-redis";    Port=16379 },
    @{ Name="mallv3-es";       Port=9201  },
    @{ Name="mallv3-rabbitmq"; Port=5673  },
    @{ Name="mallv3-minio";    Port=19090 }
)

$BACKEND_SERVICES = @(
    @{ Name="mall-app-api";   Port=18080; Log="mall-app-api.log" },
    @{ Name="mall-admin-api"; Port=18081; Log="mall-admin-api.log" },
    @{ Name="mall-job";       Port=18082; Log="mall-job.log" }
)

$FRONTEND_APPS = @(
    @{ Name="mall-admin-web"; Port=8090; Log="fe-admin.log" },
    @{ Name="mall-app-web";   Port=8091; Log="fe-app.log" }
)

function Test-PortListening([int]$port) {
    $r = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    return ($null -ne $r)
}

function Get-PortPid([int]$port) {
    $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($conn) { return [int]$conn.OwningProcess }
    return 0
}

function Get-ProcessMemMB([int]$pid2) {
    if ($pid2 -le 0) { return 0 }
    $p = Get-Process -Id $pid2 -ErrorAction SilentlyContinue
    if ($p) { return [int]($p.WorkingSet64 / 1MB) }
    return 0
}

function Get-DockerStatus([string]$name) {
    try {
        $out = docker inspect --format "{{.State.Status}}" $name 2>&1
        if ($LASTEXITCODE -eq 0) { return $out.ToString().Trim() }
    } catch {}
    return "not-found"
}

function Show-Status {
    $ts = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ("  Mall V3 状态   {0}" -f $ts) -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan

    # -- Docker 容器 --
    Write-Host "  基础设施 (Docker)" -ForegroundColor Yellow
    Write-Host "  ----------------------------------------" -ForegroundColor DarkGray

    $dockerCmd = Get-Command docker -ErrorAction SilentlyContinue
    if (-not $dockerCmd) {
        Write-Host "  [x] 未在 PATH 中找到 Docker CLI" -ForegroundColor Red
    } else {
        foreach ($c in $DOCKER_CONTAINERS) {
            $portOk = Test-PortListening -port $c.Port
            if ($portOk) {
                Write-Host ("  [+] {0,-20} port {1}" -f $c.Name, $c.Port) -ForegroundColor Green
            } else {
                $status = Get-DockerStatus -name $c.Name
                Write-Host ("  [-] {0,-20} port {1}  (docker: {2})" -f $c.Name, $c.Port, $status) -ForegroundColor Red
            }
        }
    }

    # -- 后端服务 --
    Write-Host ""
    Write-Host "  后端 (Java / Spring Boot)" -ForegroundColor Yellow
    Write-Host "  ----------------------------------------" -ForegroundColor DarkGray
    foreach ($s in $BACKEND_SERVICES) {
        $ok  = Test-PortListening -port $s.Port
        $pid2 = Get-PortPid -port $s.Port
        $mem = if ($ok) { Get-ProcessMemMB -pid2 $pid2 } else { 0 }
        $logPath = Join-Path $LOG_DIR $s.Log
        $lastLine = if (Test-Path $logPath) {
            (Get-Content $logPath -Tail 1 -ErrorAction SilentlyContinue)
        } else { "" }
        if ($ok) {
            Write-Host ("  [+] {0,-20} :$($s.Port)  pid={1}  内存={2}MB" -f $s.Name, $pid2, $mem) -ForegroundColor Green
        } else {
            Write-Host ("  [-] {0,-20} :$($s.Port)  已停止" -f $s.Name) -ForegroundColor Red
        }
        if ($lastLine) {
            $truncated = if ($lastLine.Length -gt 90) { $lastLine.Substring(0,87) + '...' } else { $lastLine }
            Write-Host ("        log: {0}" -f $truncated) -ForegroundColor DarkGray
        }
    }

    # -- 前端应用 --
    Write-Host ""
    Write-Host "  前端 (Node / pnpm)" -ForegroundColor Yellow
    Write-Host "  ----------------------------------------" -ForegroundColor DarkGray
    foreach ($a in $FRONTEND_APPS) {
        $ok = Test-PortListening -port $a.Port
        $pid2 = Get-PortPid -port $a.Port
        if ($ok) {
            Write-Host ("  [+] {0,-20} http://localhost:{1}  pid={2}" -f $a.Name, $a.Port, $pid2) -ForegroundColor Green
        } else {
            Write-Host ("  [-] {0,-20} http://localhost:{1}  已停止" -f $a.Name, $a.Port) -ForegroundColor Red
        }
    }

    # -- 快速操作 --
    Write-Host ""
    Write-Host "  快速操作" -ForegroundColor Yellow
    Write-Host "  ----------------------------------------" -ForegroundColor DarkGray
    Write-Host "    .\scripts\restart-be.ps1   重启后端" -ForegroundColor DarkGray
    Write-Host "    .\scripts\restart-fe.ps1   重启前端" -ForegroundColor DarkGray
    Write-Host "    .\scripts\restart.ps1      重启全栈" -ForegroundColor DarkGray
    Write-Host "    .\scripts\logs.ps1         查看日志" -ForegroundColor DarkGray
    Write-Host "    .\scripts\stop-v3.ps1      停止所有" -ForegroundColor DarkGray
    Write-Host "========================================" -ForegroundColor Cyan
}

if ($Watch) {
    Write-Host "  监控模式 - 按 Ctrl+C 退出" -ForegroundColor Yellow
    while ($true) {
        Clear-Host
        Show-Status
        Start-Sleep -Seconds $IntervalSec
    }
} else {
    Show-Status
}
