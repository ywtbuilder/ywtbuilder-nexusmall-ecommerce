<#
.SYNOPSIS
  Mall V3 - Restart Backend Services
.DESCRIPTION
  Usage: .\restart-be.ps1 [-Service all|app|admin|job] [-SkipBuild] [-Profile local] [-HealthTimeoutSec 90]

  Drag to terminal and press Enter.
    .\restart-be.ps1                  Restart all backends (auto-detect code change)
    .\restart-be.ps1 -SkipBuild       Skip Maven build, use existing JAR
    .\restart-be.ps1 -Service app     Only restart mall-app-api
    .\restart-be.ps1 -Service admin   Only restart mall-admin-api
#>
param(
    [ValidateSet("all","app","admin","job")]
    [string]$Service = "all",
    [switch]$SkipBuild,
    [string]$Profile = "local",
    [int]$HealthTimeoutSec = 90,
    [switch]$ForceRebuild
)

$ErrorActionPreference = "Continue"
$ROOT         = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $ROOT
$BACKEND_DIR  = Join-Path $PROJECT_ROOT "backend"
$LOG_DIR      = Join-Path $PROJECT_ROOT "runtime-logs"
$PID_FILE     = Join-Path $LOG_DIR "backend-pids.txt"
$FINGERPRINT  = Join-Path $LOG_DIR "backend-build.fingerprint"

. (Join-Path $ROOT "_lib.ps1")
Ensure-LogDir $LOG_DIR

# -- Service table -------------------------------------------------
$ALL_SERVICES = @(
    @{ Key="app";   Name="mall-app-api";   Port=18080; Jar="mall-app-api\target\mall-app-api-3.0.0-SNAPSHOT.jar" },
    @{ Key="admin"; Name="mall-admin-api"; Port=18081; Jar="mall-admin-api\target\mall-admin-api-3.0.0-SNAPSHOT.jar" },
    @{ Key="job";   Name="mall-job";       Port=18082; Jar="mall-job\target\mall-job-3.0.0-SNAPSHOT.jar" }
)

$TARGETS = if ($Service -eq "all") { $ALL_SERVICES } else {
    @($ALL_SERVICES | Where-Object { $_.Key -eq $Service })
}

# -- 工具函数由 _lib.ps1 提供（Write-Step / Invoke-KillPort / Invoke-KillPidFile /
#    Wait-AllPortsOpen / Get-Fingerprint / Show-LogTail / Ensure-LogDir）-----------

# ================================================================
$T0 = Get-Date
$targetNames = ($TARGETS | ForEach-Object { $_.Name }) -join ", "

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Mall V3 - 重启后端  [$Service]" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  目标服务:   $targetNames" -ForegroundColor DarkGray
Write-Host "  配置文件:   $Profile" -ForegroundColor DarkGray
Write-Host "  跳过构建: $SkipBuild" -ForegroundColor DarkGray

# -- 步骤 1：停止旧进程 --
Write-Step "[ 1/5 ] 停止现有后端进程..."
Invoke-KillPidFile -path $PID_FILE
foreach ($svc in $TARGETS) {
    Invoke-KillPort -port $svc.Port -label $svc.Name
}
Write-Host "  旧进程已清理。" -ForegroundColor Green

# -- 步骤 2：构建 --
Write-Step "[ 2/5 ] Maven 构建..."

$needBuild = $true
if ($SkipBuild) {
    $needBuild = $false
    Write-Host "  跳过构建 (-SkipBuild)" -ForegroundColor DarkGray
    $missingJars = $TARGETS | Where-Object { -not (Test-Path (Join-Path $BACKEND_DIR $_.Jar)) }
    if ($missingJars) {
        Write-Host "  警告: 以下 JAR 缺失，强制构建:" -ForegroundColor Red
        $missingJars | ForEach-Object { Write-Host "    $($_.Jar)" -ForegroundColor Red }
        $needBuild = $true
    }
}

if ($needBuild) {
    $curFp = Get-Fingerprint -dir $BACKEND_DIR
    $preFp = if (Test-Path $FINGERPRINT) { (Get-Content $FINGERPRINT -Raw).Trim() } else { "" }
    $missingJars = $TARGETS | Where-Object { -not (Test-Path (Join-Path $BACKEND_DIR $_.Jar)) }

    if (-not $ForceRebuild -and $curFp -eq $preFp -and -not $missingJars) {
        Write-Host "  未检测到代码变化，跳过构建。" -ForegroundColor DarkGray
        $needBuild = $false
    } else {
        if ($ForceRebuild) { Write-Host "  强制重生建。" -ForegroundColor Yellow }
        elseif ($missingJars) { Write-Host "  检测到 JAR 缺失。" -ForegroundColor Yellow }
        else { Write-Host "  检测到代码变化。" -ForegroundColor Yellow }
    }

    if ($needBuild) {
        Write-Host "  正在使用 Maven Wrapper 构建（请耐心等待，约 5-10 分钟）..." -ForegroundColor Yellow
        $t = Get-Date
        Push-Location $BACKEND_DIR
        & .\mvnw.cmd clean package -DskipTests -B -T 1C
        $exitCode = $LASTEXITCODE
        Pop-Location
        if ($exitCode -ne 0) {
            Write-Host "  Maven 构建失败 (exit $exitCode)" -ForegroundColor Red
            exit 1
        }
        $elapsed = [int]((Get-Date) - $t).TotalSeconds
        Write-Host "  构建完成，耗时 ${elapsed}s。" -ForegroundColor Green
        $curFp | Out-File -FilePath $FINGERPRINT -Encoding UTF8 -NoNewline
    }
}

# -- 步骤 3：等待基础设施端口就绪（ES:9201 / MySQL:13306）--
Write-Step "[ 3/5 ] 检查基础设施状态（ES:9201 / MySQL:13306）..."

# ① 先展示 Docker 容器当前状态，让用户知道情况
$dockerOk = $false
try {
    $containers = docker ps --format "{{.Names}}\t{{.Status}}" 2>$null
    if ($LASTEXITCODE -eq 0 -and $containers) {
        $dockerOk = $true
        Write-Host "  Docker 容器状态:" -ForegroundColor DarkGray
        $containers | ForEach-Object {
            $parts = $_ -split "`t"
            $icon  = if ($parts[1] -match "^Up") { "[UP]" } else { "[--]" }
            $col   = if ($parts[1] -match "^Up") { "Green" } else { "Red" }
            Write-Host ("    {0,-8} {1,-22} {2}" -f $icon, $parts[0], $parts[1]) -ForegroundColor $col
        }
    } else {
        Write-Host "  Docker 无运行中容器。" -ForegroundColor Red
    }
} catch {
    Write-Host "  Docker 不可达，跳过容器状态检查。" -ForegroundColor Yellow
}

# ② 检测每个基础设施端口，已就绪直接通过，未就绪才进入等待循环
$INFRA_PORTS = @(
    @{ Name="Elasticsearch"; Port=9201;  Container="mallv3-es";    HealthUrl="http://localhost:9201/_cluster/health" },
    @{ Name="MySQL";         Port=13306; Container="mallv3-mysql";  HealthUrl=$null }
)
$infraWaitSec = 90
$infraAllOk   = $true
foreach ($inf in $INFRA_PORTS) {
    # 快速检测：ES 用 HTTP health，MySQL 用 TCP
    $quickOk = $false
    if ($inf.HealthUrl) {
        try {
            $r = Invoke-WebRequest -Uri $inf.HealthUrl -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
            $quickOk = ($r.StatusCode -lt 500)
        } catch { }
    } else {
        try {
            $tc = [System.Net.Sockets.TcpClient]::new()
            $tc.Connect("127.0.0.1", $inf.Port)
            $tc.Close()
            $quickOk = $true
        } catch { }
    }

    if ($quickOk) {
        Write-Host "  [OK]   $($inf.Name) :$($inf.Port) (已就绪)" -ForegroundColor Green
        continue
    }

    # 端口不通 → 检查容器是否存在
    $cStatus = ""
    try { $cStatus = (docker inspect --format "{{.State.Status}}" $inf.Container 2>$null) } catch {}
    if ($cStatus -notin @("running","")) {
        Write-Host "  [ERR]  $($inf.Container) 状态: $cStatus — 请先启动基础设施:" -ForegroundColor Red
        Write-Host "         docker compose -f infra/docker-compose.local.yml up -d" -ForegroundColor Yellow
        $infraAllOk = $false
        continue
    }

    # 容器正在启动中 → 等待 HTTP/TCP 就绪
    $deadline = (Get-Date).AddSeconds($infraWaitSec)
    $checkDesc = if ($inf.HealthUrl) { "HTTP $($inf.HealthUrl)" } else { "TCP :$($inf.Port)" }
    Write-Host "  等待 $($inf.Name) ($checkDesc)" -NoNewline -ForegroundColor DarkGray
    $ready = $false
    while ((Get-Date) -lt $deadline) {
        Start-Sleep -Milliseconds 3000
        Write-Host "." -NoNewline
        try {
            if ($inf.HealthUrl) {
                $r = Invoke-WebRequest -Uri $inf.HealthUrl -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
                if ($r.StatusCode -lt 500) { $ready = $true; break }
            } else {
                $tc = [System.Net.Sockets.TcpClient]::new()
                $tc.Connect("127.0.0.1", $inf.Port)
                $tc.Close()
                $ready = $true; break
            }
        } catch { }
    }
    Write-Host ""
    if ($ready) {
        Write-Host "  [OK]   $($inf.Name) :$($inf.Port)" -ForegroundColor Green
    } else {
        Write-Host "  [WARN] $($inf.Name) :$($inf.Port) 等待 ${infraWaitSec}s 超时，Spring Boot 可能启动失败" -ForegroundColor Red
        $infraAllOk = $false
    }
}

if (-not $infraAllOk) {
    Write-Host ""
    Write-Host "  基础设施未就绪，中止后端启动。请先运行:" -ForegroundColor Red
    Write-Host "    docker compose -f infra/docker-compose.local.yml up -d" -ForegroundColor Yellow
    Write-Host "  然后重新执行此脚本。" -ForegroundColor Yellow
    exit 1
}

# -- 步骤 4：启动 JAR --
Write-Step "[ 4/5 ] 启动后端服务..."
$startedPids   = @()
$managedWrites = @()
$startFailed   = $false

foreach ($svc in $TARGETS) {
    $jarPath = Join-Path $BACKEND_DIR $svc.Jar
    if (-not (Test-Path $jarPath)) {
        Write-Host "  JAR 未找到: $jarPath" -ForegroundColor Red
        Write-Host "  请运行 .\build-be.ps1 或去掉 -SkipBuild 参数" -ForegroundColor Yellow
        $startFailed = $true
        continue
    }
    $logFile = Join-Path $LOG_DIR "$($svc.Name).log"
    $errFile = Join-Path $LOG_DIR "$($svc.Name)-error.log"
    @($logFile, $errFile) | ForEach-Object {
        if (Test-Path $_) { Clear-Content $_ -ErrorAction SilentlyContinue }
    }
    Write-Host "  正在启动 $($svc.Name)  :$($svc.Port)  " -NoNewline -ForegroundColor Cyan
    $proc = Start-Process -FilePath "java" `
        -ArgumentList "-jar", $jarPath, "--spring.profiles.active=$Profile" `
        -RedirectStandardOutput $logFile `
        -RedirectStandardError  $errFile `
        -WindowStyle Hidden -PassThru
    $startedPids   += $proc.Id
    $managedWrites += "$($svc.Name)=$($proc.Id)"
    $svc.ProcId     = $proc.Id
    Write-Host "pid=$($proc.Id)" -ForegroundColor Green
}

if ($startFailed) {
    Write-Host "  致命错误: 部分 JAR 缺失，已中止。" -ForegroundColor Red
    foreach ($pid2 in $startedPids) { try { taskkill /PID $pid2 /T /F 2>&1|Out-Null } catch {} }
    exit 1
}

# -- 步骤 5：健康检查 --
Write-Step "[ 5/5 ] 等待服务就绪（最多 ${HealthTimeoutSec}s）..."
$allOk   = $true
$results = @()

# 并行等待所有端口（单轮询循环，同时检测所有服务）
$portResults = Wait-AllPortsOpen -Services $TARGETS -TimeoutSec $HealthTimeoutSec
foreach ($svc in $TARGETS) {
    $ok = $portResults[[int]$svc.Port]
    if (-not $ok) {
        $allOk = $false
        Write-Host "  $($svc.Name) 启动失败，最近日志:" -ForegroundColor Red
        Show-LogTail -path (Join-Path $LOG_DIR "$($svc.Name).log")      -n 20
        Show-LogTail -path (Join-Path $LOG_DIR "$($svc.Name)-error.log") -n 10
    }
    $results += [PSCustomObject]@{ Name=$svc.Name; Port=$svc.Port; OK=$ok }
}

$managedWrites | Out-File -FilePath $PID_FILE -Encoding UTF8

# ─── 预热后端 API（消除 spring.main.lazy-initialization 导致的浏览器首次访问卡顿）────
# Spring Boot 端口开放时 Bean 尚未初始化，第一次 API 调用会触发全量初始化（10-30s）。
# 此处在脚本结束前主动发 warmup 请求，让用户打开浏览器时响应已经是毫秒级。
if ($allOk -and ($Service -eq "all" -or $Service -eq "app")) {
    Write-Host ""
    Write-Host "  [预热] 正在预热 App API Bean（消除浏览器首次访问延迟）..." -ForegroundColor Cyan
    $warmupUrls = @(
        "http://localhost:18080/home/content",
        "http://localhost:18080/home/productCateList/0"
    )
    # 并行发出预热请求，缩短总预热时间
    $warmupJobs = $warmupUrls | ForEach-Object {
        $wUrl = $_
        Start-Job -ScriptBlock {
            param($url)
            try {
                $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 60 -ErrorAction Stop
                return @{ Url=$url; OK=$true; Code=$r.StatusCode }
            } catch {
                return @{ Url=$url; OK=$false; Err=$_.Exception.Message }
            }
        } -ArgumentList $wUrl
    }
    $warmupJobs | Wait-Job | Out-Null
    foreach ($j in $warmupJobs) {
        $r = Receive-Job $j
        if ($r.OK) {
            Write-Host "  [预热 OK] $($r.Url)  HTTP $($r.Code)" -ForegroundColor Green
        } else {
            Write-Host "  [预热 WARN] $($r.Url) : $($r.Err)" -ForegroundColor DarkYellow
        }
        Remove-Job $j -Force
    }
}

$elapsed = [int]((Get-Date) - $T0).TotalSeconds
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan

if ($allOk) {
    Write-Host "  [✓] 重启完成，耗时 ${elapsed}s" -ForegroundColor Green
} else {
    Write-Host "  [⚠] 部分服务失败 (${elapsed}s)" -ForegroundColor Red
}

foreach ($r in $results) {
    $icon = if ($r.OK) { "[v]" } else { "[x]" }
    $col  = if ($r.OK) { "Green" } else { "Red" }
    Write-Host ("  {0} {1,-22} http://localhost:{2}" -f $icon, $r.Name, $r.Port) -ForegroundColor $col
}
Write-Host "  日志:  $LOG_DIR" -ForegroundColor DarkGray
Write-Host "  状态:  .\status.ps1" -ForegroundColor DarkGray
Write-Host "========================================" -ForegroundColor Cyan

if (-not $allOk) { exit 1 }
