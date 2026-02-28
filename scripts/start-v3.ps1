# Mall V3 - 启动脚本 (PowerShell)
# 用法: .\start-v3.ps1 [-SkipBuild] [-SkipInfra] [-Frontend] [-BackendHealthTimeoutSec 60]
# 参数说明:
#   -SkipBuild    跳过 Maven 构建（使用已有 JAR）
#   -SkipInfra    跳过 Docker Compose 启动
#   -Frontend     同时启动前端开发服务器
#   -BackendHealthTimeoutSec  每个后端服务健康检查超时秒数

param(
    [switch]$SkipBuild,
    [switch]$SkipInfra,
    [switch]$Frontend,
    [int]$BackendHealthTimeoutSec = 60
)

$ErrorActionPreference = "Stop"
$ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $ROOT
$LOG_DIR = Join-Path $PROJECT_ROOT "runtime-logs"
$PID_FILE = Join-Path $LOG_DIR "backend-pids.txt"
$FRONTEND_PID_FILE = Join-Path $LOG_DIR "frontend-pids.txt"
$BUILD_FINGERPRINT_FILE = Join-Path $LOG_DIR "backend-build.fingerprint"

function Get-BackendBuildFingerprint {
    param(
        [Parameter(Mandatory = $true)]
        [string]$BackendDir
    )

    $backendRoot = [System.IO.Path]::GetFullPath($BackendDir).TrimEnd('\')
    $sourceFiles = Get-ChildItem -Path $BackendDir -Recurse -File | Where-Object {
        $_.FullName -notmatch '\\target\\' -and (
            $_.Name -eq "pom.xml" -or
            $_.FullName -match '\\src\\'
        )
    }

    if (-not $sourceFiles) { return "no-source-files" }

    $fingerprintRows = $sourceFiles | ForEach-Object {
        $fullPath = [System.IO.Path]::GetFullPath($_.FullName)
        $relativePath = $fullPath.Substring($backendRoot.Length).TrimStart('\').Replace('\', '/')
        "{0}|{1}|{2}" -f $relativePath, $_.LastWriteTimeUtc.Ticks, $_.Length
    } | Sort-Object

    $payload = [string]::Join("`n", $fingerprintRows)
    $payloadBytes = [System.Text.Encoding]::UTF8.GetBytes($payload)
    $sha = [System.Security.Cryptography.SHA256]::Create()
    try {
        $hashBytes = $sha.ComputeHash($payloadBytes)
        return ([System.BitConverter]::ToString($hashBytes)).Replace("-", "").ToLowerInvariant()
    } finally {
        $sha.Dispose()
    }
}

function Get-LogTailText {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,
        [int]$TailLines = 30
    )

    if (-not (Test-Path $Path)) {
        return "(log file not found: $Path)"
    }

    try {
        return [string]::Join("`n", (Get-Content -Path $Path -Tail $TailLines))
    } catch {
        return "(failed to read log file: $Path)"
    }
}

function Get-ListeningProcessInfo {
    param(
        [Parameter(Mandatory = $true)]
        [int]$Port
    )

    $listener = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if (-not $listener) { return $null }

    $processId = [int]$listener.OwningProcess
    $processName = $null
    $commandLine = $null

    try {
        $proc = Get-Process -Id $processId -ErrorAction Stop
        $processName = $proc.ProcessName
    } catch {}

    try {
        $procDetail = Get-CimInstance Win32_Process -Filter "ProcessId = $processId" -ErrorAction Stop
        $commandLine = $procDetail.CommandLine
    } catch {}

    return @{
        Port = $Port
        ProcessId = $processId
        ProcessName = $processName
        CommandLine = $commandLine
    }
}

function Test-CommandLineContainsAnyToken {
    param(
        [string]$CommandLine,
        [string[]]$Tokens
    )

    if (-not $CommandLine) { return $false }
    foreach ($token in $Tokens) {
        if ($CommandLine -match [regex]::Escape($token)) { return $true }
    }
    return $false
}

function Wait-FrontendReady {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Name,
        [Parameter(Mandatory = $true)]
        [System.Diagnostics.Process]$Process,
        [Parameter(Mandatory = $true)]
        [string]$StdoutLog,
        [Parameter(Mandatory = $true)]
        [string]$StderrLog,
        [Parameter(Mandatory = $true)]
        [int]$ExpectedPort,
        [int]$TimeoutSec = 45
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    $resolvedPort = $null

    while ((Get-Date) -lt $deadline) {
        if (Test-Path $StdoutLog) {
            $matches = Select-String -Path $StdoutLog -Pattern 'http://localhost:(\d+)/' -AllMatches -ErrorAction SilentlyContinue
            if ($matches) {
                $lastMatch = $matches | Select-Object -Last 1
                if ($lastMatch.Matches.Count -gt 0) {
                    $resolvedPort = [int]$lastMatch.Matches[0].Groups[1].Value
                }
            }
        }

        if ($resolvedPort) {
            try {
                $resp = Invoke-WebRequest -Uri "http://localhost:$resolvedPort" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
                if ($resp.StatusCode -ge 200 -and $resp.StatusCode -lt 500) {
                    return @{
                        Success = $true
                        Port = $resolvedPort
                        Message = $null
                    }
                }
            } catch {}
        }

        $alive = Get-Process -Id $Process.Id -ErrorAction SilentlyContinue
        if (-not $alive) {
            $stdoutTail = Get-LogTailText -Path $StdoutLog -TailLines 20
            $stderrTail = Get-LogTailText -Path $StderrLog -TailLines 20
            return @{
                Success = $false
                Port = $resolvedPort
                Message = "process exited before ready.`n      stdout:`n$stdoutTail`n      stderr:`n$stderrTail"
            }
        }

        Start-Sleep -Seconds 1
    }

    if (-not $resolvedPort) { $resolvedPort = $ExpectedPort }

    try {
        $resp = Invoke-WebRequest -Uri "http://localhost:$resolvedPort" -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
        if ($resp.StatusCode -ge 200 -and $resp.StatusCode -lt 500) {
            return @{
                Success = $true
                Port = $resolvedPort
                Message = $null
            }
        }
    } catch {}

    $stdoutTail = Get-LogTailText -Path $StdoutLog -TailLines 20
    $stderrTail = Get-LogTailText -Path $StderrLog -TailLines 20
    return @{
        Success = $false
        Port = $resolvedPort
        Message = "timeout waiting for http://localhost:$resolvedPort/.`n      stdout:`n$stdoutTail`n      stderr:`n$stderrTail"
    }
}

function Get-ActiveUsernamesFromMysql {
    param(
        [Parameter(Mandatory = $true)]
        [string]$TableName,
        [string]$ContainerName = "mallv3-mysql"
    )

    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) { return @() }

    try {
        $running = (& docker inspect -f "{{.State.Running}}" $ContainerName 2>$null)
        if ($LASTEXITCODE -ne 0 -or -not $running -or $running.Trim() -ne "true") { return @() }
    } catch {
        return @()
    }

    $query = "SELECT username FROM $TableName WHERE status = 1 ORDER BY id LIMIT 30;"
    try {
        $rows = (& docker exec $ContainerName mysql -uroot -proot mall -N -e "$query" 2>$null)
        if ($LASTEXITCODE -ne 0 -or -not $rows) { return @() }
        return @(
            $rows |
            ForEach-Object { $_.ToString().Trim() } |
            Where-Object { $_ } |
            Select-Object -Unique
        )
    } catch {
        return @()
    }
}

function Get-MergedUniqueStrings {
    param(
        [string[]]$Primary,
        [string[]]$Secondary,
        [int]$Limit = 20
    )

    $seen = New-Object 'System.Collections.Generic.HashSet[string]' ([System.StringComparer]::OrdinalIgnoreCase)
    $merged = New-Object 'System.Collections.Generic.List[string]'

    foreach ($source in @($Primary, $Secondary)) {
        foreach ($item in @($source)) {
            if (-not $item) { continue }
            $value = $item.Trim()
            if (-not $value) { continue }

            if ($seen.Add($value)) {
                $merged.Add($value)
                if ($merged.Count -ge $Limit) {
                    return @($merged)
                }
            }
        }
    }

    return @($merged)
}

function Test-LoginCredential {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Url,
        [Parameter(Mandatory = $true)]
        [string]$Username,
        [Parameter(Mandatory = $true)]
        [string]$Password,
        [int]$TimeoutSec = 6
    )

    try {
        $payload = @{ username = $Username; password = $Password } | ConvertTo-Json -Compress
        $resp = Invoke-RestMethod -Uri $Url -Method Post -ContentType "application/json" -Body $payload -TimeoutSec $TimeoutSec -ErrorAction Stop
        return ($resp -and $resp.code -eq 200 -and $resp.data -and $resp.data.token)
    } catch {
        return $false
    }
}

function Find-LoggableCredentials {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Url,
        [Parameter(Mandatory = $true)]
        [string[]]$Usernames,
        [Parameter(Mandatory = $true)]
        [string[]]$Passwords,
        [int]$MaxResults = 8
    )

    $results = @()
    foreach ($username in $Usernames) {
        foreach ($password in $Passwords) {
            if (Test-LoginCredential -Url $Url -Username $username -Password $password) {
                $results += [PSCustomObject]@{
                    Username = $username
                    Password = $password
                }
                break
            }
        }
        if ($results.Count -ge $MaxResults) { break }
    }
    return $results
}

function Show-CurrentLoggableAccounts {
    $adminApiLogin = "http://localhost:18081/admin/login"
    $appApiLogin = "http://localhost:18080/sso/login"

    $adminDefaultUsers = @("admin", "macro", "test", "productAdmin", "orderAdmin", "test123")
    $memberDefaultUsers = @("test", "windy", "member", "guest")
    $adminPasswordCandidates = @("macro123", "123456", "admin", "password")
    $memberPasswordCandidates = @("123456", "test123456", "password")

    $adminDbUsers = Get-ActiveUsernamesFromMysql -TableName "ums_admin"
    $memberDbUsers = Get-ActiveUsernamesFromMysql -TableName "ums_member"
    $adminUsers = Get-MergedUniqueStrings -Primary $adminDefaultUsers -Secondary $adminDbUsers -Limit 25
    $memberUsers = Get-MergedUniqueStrings -Primary $memberDefaultUsers -Secondary $memberDbUsers -Limit 25

    Write-Host "`n[诊断] 已验证的登录账户（实际 API 登录验证）:" -ForegroundColor Yellow
    $adminMatches = Find-LoggableCredentials -Url $adminApiLogin -Usernames $adminUsers -Passwords $adminPasswordCandidates -MaxResults 10
    $memberMatches = Find-LoggableCredentials -Url $appApiLogin -Usernames $memberUsers -Passwords $memberPasswordCandidates -MaxResults 10

    if ($adminMatches.Count -gt 0) {
        Write-Host "  后台管理 (http://localhost:8090/login):" -ForegroundColor Cyan
        foreach ($entry in $adminMatches) {
            Write-Host "    $($entry.Username) / $($entry.Password)" -ForegroundColor Green
        }
    } else {
        Write-Host "  后台管理 (http://localhost:8090/login): 候选账户中未找到可用凭据。" -ForegroundColor DarkYellow
    }

    if ($memberMatches.Count -gt 0) {
        Write-Host "  用户端登录 (http://localhost:8091/login):" -ForegroundColor Cyan
        foreach ($entry in $memberMatches) {
            Write-Host "    $($entry.Username) / $($entry.Password)" -ForegroundColor Green
        }
    } else {
        Write-Host "  用户端登录 (http://localhost:8091/login): 候选账户中未找到可用凭据。" -ForegroundColor DarkYellow
    }
}

# Ensure log directory exists
if (-not (Test-Path $LOG_DIR)) { New-Item -ItemType Directory -Path $LOG_DIR -Force | Out-Null }

Write-Host "===== Mall V3 启动 =====" -ForegroundColor Green
Write-Host "日志目录: $LOG_DIR" -ForegroundColor DarkGray

# ── 1. 环境预检 ──
Write-Host "`n[1/5] 运行环境预检..." -ForegroundColor Yellow
& "$ROOT\preflight-v3.ps1"
if ($LASTEXITCODE -ne 0) {
    Write-Host "环境预检失败，已中止启动。" -ForegroundColor Red
    exit 1
}

# ── 2. 启动基础设施 (Docker Compose) ──
if (-not $SkipInfra) {
    Write-Host "`n[2/5] 启动基础设施容器..." -ForegroundColor Yellow
    Push-Location (Join-Path $PROJECT_ROOT "infra")
    docker compose -f docker-compose.local.yml up -d
    if ($LASTEXITCODE -ne 0) {
        Write-Host "Docker Compose 启动失败" -ForegroundColor Red
        Pop-Location; exit 1
    }
    Pop-Location
} else {
    Write-Host "`n[2/5] 跳过基础设施启动 (--SkipInfra)" -ForegroundColor DarkGray
}

# ── 3. 等待 MySQL & Redis 就绪 ──
if (-not $SkipInfra) {
    Write-Host "`n[3/5] 等待基础设施就绪..." -ForegroundColor Yellow

    # MySQL
    Write-Host -NoNewline "  MySQL (13306): "
    $maxRetries = 30
    $ready = $false
    for ($i = 0; $i -lt $maxRetries; $i++) {
        try {
            # --connect-timeout 5 防止 TCP 挂起无法返回
            $pingOut = docker exec mallv3-mysql mysqladmin ping -h 127.0.0.1 --silent --connect-timeout 5 2>&1
            if ($LASTEXITCODE -eq 0) { $ready = $true; break }
        } catch {}
        if ($i -gt 0 -and ($i % 3 -eq 0)) { Write-Host -NoNewline "." }
        Start-Sleep -Seconds 2
    }
    if ($ready) { Write-Host " 就绪" -ForegroundColor Green }
    else {
        Write-Host " 超时 - MySQL 60s 内无响应" -ForegroundColor Red
        exit 1
    }

    # Redis
    Write-Host -NoNewline "  Redis (16379): "
    $ready = $false
    for ($i = 0; $i -lt 20; $i++) {
        try {
            $pong = docker exec mallv3-redis redis-cli ping 2>&1
            if ($LASTEXITCODE -eq 0 -and $pong -match 'PONG') { $ready = $true; break }
        } catch {}
        if ($i -gt 0 -and ($i % 3 -eq 0)) { Write-Host -NoNewline "." }
        Start-Sleep -Seconds 1
    }
    if ($ready) { Write-Host " 就绪" -ForegroundColor Green }
    else {
        Write-Host " 超时 - Redis 20s 内无响应" -ForegroundColor Red
        exit 1
    }

    # Elasticsearch — 直接 HTTP 轮询，比等待 Docker healthcheck (interval=30s) 快得多
    Write-Host -NoNewline "  Elasticsearch (9201): "
    $ready = $false
    for ($i = 0; $i -lt 60; $i++) {
        try {
            $resp = Invoke-WebRequest -Uri "http://localhost:9201/_cluster/health" `
                -UseBasicParsing -TimeoutSec 3 -ErrorAction Stop
            if ($resp.StatusCode -eq 200) {
                $health = $resp.Content | ConvertFrom-Json -ErrorAction SilentlyContinue
                if ($health -and $health.status -in @('yellow', 'green')) {
                    $ready = $true; break
                }
            }
        } catch {}
        if ($i -gt 0 -and ($i % 5 -eq 0)) { Write-Host -NoNewline "." }
        Start-Sleep -Seconds 2
    }
    if ($ready) { Write-Host " 就绪" -ForegroundColor Green }
    else {
        Write-Host " 超时 - Elasticsearch 120s 内未就绪" -ForegroundColor Red
        Write-Host "  [提示] 可手动检查: curl http://localhost:9201/_cluster/health" -ForegroundColor DarkYellow
        exit 1
    }
} else {
    Write-Host "`n[3/5] 跳过基础设施就绪检查 (--SkipInfra)" -ForegroundColor DarkGray
}

# 鈹€鈹€ 4. Build and start backend 鈹€鈹€
$BACKEND_DIR = Join-Path $PROJECT_ROOT "backend"
Push-Location $BACKEND_DIR

# Start backend services
$services = @(
    @{ Name = "mall-app-api";   Port = 18080; Jar = "mall-app-api\target\mall-app-api-3.0.0-SNAPSHOT.jar" },
    @{ Name = "mall-admin-api"; Port = 18081; Jar = "mall-admin-api\target\mall-admin-api-3.0.0-SNAPSHOT.jar" },
    @{ Name = "mall-job";       Port = 18082; Jar = "mall-job\target\mall-job-3.0.0-SNAPSHOT.jar" }
)

if (-not $SkipBuild) {
    Write-Host "`n[4/5] 检测是否需要重新构建后端..." -ForegroundColor Yellow

    $missingJarServices = @()
    foreach ($svc in $services) {
        $jarPath = Join-Path $BACKEND_DIR $svc.Jar
        if (-not (Test-Path $jarPath)) { $missingJarServices += $svc.Name }
    }

    $shouldBuild = $false
    $buildReason = ""
    $currentFingerprint = Get-BackendBuildFingerprint -BackendDir $BACKEND_DIR

    if ($missingJarServices.Count -gt 0) {
        $shouldBuild = $true
        $buildReason = "缺少 JAR: $($missingJarServices -join ', ')"
    } elseif (-not (Test-Path $BUILD_FINGERPRINT_FILE)) {
        $shouldBuild = $true
        $buildReason = "未找到上次构建指纹记录。"
    } else {
        $previousFingerprint = (Get-Content -Path $BUILD_FINGERPRINT_FILE -Raw -ErrorAction SilentlyContinue).Trim()
        if ($previousFingerprint -ne $currentFingerprint) {
            $shouldBuild = $true
            $buildReason = "检测到后端源码变化。"
        }
    }

    if ($shouldBuild) {
        Write-Host "  需要构建: $buildReason" -ForegroundColor Yellow
        Write-Host "  正在使用 Maven Wrapper 构建后端（增量构建 + 并行编译）..." -ForegroundColor Yellow
        & .\mvnw.cmd package -DskipTests -B -T 1C
        if ($LASTEXITCODE -ne 0) {
            Write-Host "后端构建失败" -ForegroundColor Red
            Pop-Location; exit 1
        }
        $currentFingerprint | Out-File -FilePath $BUILD_FINGERPRINT_FILE -Encoding UTF8
        Write-Host "  构建完成。" -ForegroundColor Green
    } else {
        Write-Host "  未检测到后端源码变化，跳过 Maven 构建。" -ForegroundColor Green
    }
} else {
    Write-Host "`n[4/5] 跳过构建 (--SkipBuild)" -ForegroundColor DarkGray
}

$startedBackendPids = @()
$managedBackendPids = @()
$missingJars = @()
$backendPortConflicts = @()
foreach ($svc in $services) {
    $jarPath = Join-Path $BACKEND_DIR $svc.Jar
    if (-not (Test-Path $jarPath)) {
        Write-Host "  JAR 不存在: $jarPath — $($svc.Name) 无法启动！" -ForegroundColor Red
        $missingJars += $svc.Name
        continue
    }

    $existingListener = Get-ListeningProcessInfo -Port $svc.Port
    if ($existingListener) {
        $jarName = [System.IO.Path]::GetFileName($svc.Jar)
        $isSameService = Test-CommandLineContainsAnyToken -CommandLine $existingListener.CommandLine -Tokens @($jarName)

        if ($isSameService) {
            Write-Host "  $($svc.Name) 已在端口 $($svc.Port) 运行 (PID $($existingListener.ProcessId))，跳过重复启动。" -ForegroundColor DarkYellow
            $svc.ProcessId = $existingListener.ProcessId
            $svc.LogFile = Join-Path $LOG_DIR "$($svc.Name).log"
            $svc.ErrorLogFile = Join-Path $LOG_DIR "$($svc.Name)-error.log"
            $managedBackendPids += "$($svc.Name)=$($existingListener.ProcessId)"
            continue
        }

        $displayName = if ($existingListener.ProcessName) { $existingListener.ProcessName } else { "unknown" }
        $backendPortConflicts += "  $($svc.Name): 端口 $($svc.Port) 被 PID $($existingListener.ProcessId) ($displayName) 占用"
        continue
    }

    $logFile = Join-Path $LOG_DIR "$($svc.Name).log"
    $errorLogFile = Join-Path $LOG_DIR "$($svc.Name)-error.log"
    Write-Host "  正在启动 $($svc.Name)，端口 $($svc.Port)... " -NoNewline -ForegroundColor Cyan
    $proc = Start-Process -FilePath "java" `
        -ArgumentList "-XX:TieredStopAtLevel=1", "-XX:+UseParallelGC", "-jar", $jarPath, "--spring.profiles.active=local" `
        -RedirectStandardOutput $logFile `
        -RedirectStandardError $errorLogFile `
        -WindowStyle Hidden -PassThru
    $svc.ProcessId = $proc.Id
    $svc.LogFile = $logFile
    $svc.ErrorLogFile = $errorLogFile
    $startedBackendPids += "$($svc.Name)=$($proc.Id)"
    $managedBackendPids += "$($svc.Name)=$($proc.Id)"
    Write-Host "PID $($proc.Id)" -ForegroundColor Green
}

if ($missingJars.Count -gt 0) {
    Write-Host "`n  致命错误：以下服务缺少 JAR：$($missingJars -join ', ')" -ForegroundColor Red
    Write-Host "  请先在 backend/ 目录执行 'mvn clean package -DskipTests'，或去掉 -SkipBuild 参数。" -ForegroundColor Red
    # Kill any services that were already started
    foreach ($pidEntry in $startedBackendPids) {
        $parts = $pidEntry -split '='
        try { Stop-Process -Id ([int]$parts[1]) -Force -ErrorAction SilentlyContinue } catch {}
    }
    Pop-Location; exit 1
}

if ($backendPortConflicts.Count -gt 0) {
    Write-Host "`n  致命错误：启动后端时检测到端口冲突。" -ForegroundColor Red
    $backendPortConflicts | ForEach-Object { Write-Host $_ -ForegroundColor Red }
    Write-Host "  请先执行 '.\stop-v3.ps1'，或手动停止冲突进程。" -ForegroundColor Yellow
    foreach ($pidEntry in $startedBackendPids) {
        $parts = $pidEntry -split '='
        try { Stop-Process -Id ([int]$parts[1]) -Force -ErrorAction SilentlyContinue } catch {}
    }
    Pop-Location; exit 1
}

# Save PIDs
if ($managedBackendPids.Count -gt 0) {
    $managedBackendPids | Out-File -FilePath $PID_FILE -Encoding UTF8
} elseif (Test-Path $PID_FILE) {
    Remove-Item $PID_FILE -Force -ErrorAction SilentlyContinue
}
Pop-Location

# Health check
Write-Host "`n  正在等待后端服务健康就绪..." -ForegroundColor Yellow
$backendHealthFailed = $false
foreach ($svc in $services) {
    Write-Host -NoNewline "  $($svc.Name) (port $($svc.Port)): "
    $healthy = $false
    $exited = $false
    $deadline = (Get-Date).AddSeconds($BackendHealthTimeoutSec)
    while ((Get-Date) -lt $deadline) {
        if ($svc.ContainsKey("ProcessId")) {
            $alive = Get-Process -Id $svc.ProcessId -ErrorAction SilentlyContinue
            if (-not $alive) {
                $exited = $true
                break
            }

            $listener = Get-NetTCPConnection -LocalPort $svc.Port -State Listen -ErrorAction SilentlyContinue |
                Where-Object { $_.OwningProcess -eq $svc.ProcessId } |
                Select-Object -First 1
            if ($listener) {
                $healthy = $true
                break
            }
        }
        Start-Sleep -Seconds 2
    }
    if ($healthy) { Write-Host "健康" -ForegroundColor Green }
    elseif ($exited) {
        Write-Host "进程已提前退出（请检查 $LOG_DIR\$($svc.Name).log）" -ForegroundColor Yellow
        $backendHealthFailed = $true
    } else {
        Write-Host "${BackendHealthTimeoutSec}s 内未响应（请检查 $LOG_DIR\$($svc.Name).log）" -ForegroundColor Yellow
        $backendHealthFailed = $true
    }

    if ($backendHealthFailed) {
        $logTail = Get-LogTailText -Path (Join-Path $LOG_DIR "$($svc.Name).log") -TailLines 30
        Write-Host "    最近日志：" -ForegroundColor DarkYellow
        Write-Host $logTail -ForegroundColor DarkYellow
        break
    }
}

if ($backendHealthFailed) {
    Write-Host "`n后端启动失败，正在停止已启动的进程..." -ForegroundColor Red
    foreach ($pidEntry in $startedBackendPids) {
        $parts = $pidEntry -split '='
        try { Stop-Process -Id ([int]$parts[1]) -Force -ErrorAction SilentlyContinue } catch {}
    }
    if (Test-Path $PID_FILE) { Remove-Item $PID_FILE -Force -ErrorAction SilentlyContinue }
    exit 1
}

# ─── 预热后端 API（消除 spring.main.lazy-initialization 导致的浏览器首次访问卡顿）────
# Spring Boot 端口开放时 Bean 尚未初始化，第一次 API 调用会触发全量初始化（10-30s）。
# 此处主动发 warmup 请求，让用户打开浏览器时响应已经是毫秒级。
Write-Host ""
Write-Host "  [预热] 正在预热 App API Bean（消除浏览器首次访问延迟）..." -ForegroundColor Cyan
$warmupEndpoints = @(
    "http://localhost:18080/home/content",
    "http://localhost:18080/home/productCateList/0"
)
foreach ($wUrl in $warmupEndpoints) {
    try {
        Invoke-WebRequest -Uri $wUrl -UseBasicParsing -TimeoutSec 60 -ErrorAction Stop | Out-Null
        Write-Host "  [预热 OK] $wUrl" -ForegroundColor Green
    } catch {
        Write-Host "  [预热 WARN] $wUrl : $($_.Exception.Message)" -ForegroundColor DarkYellow
    }
}

# 鈹€鈹€ 5. Frontend 鈹€鈹€
$adminWebUrl = "http://localhost:8090  (if -Frontend)"
$appWebUrl = "http://localhost:8091  (if -Frontend)"

if ($Frontend) {
    Write-Host "`n[5/5] 正在启动前端开发服务器..." -ForegroundColor Yellow
    $FRONTEND_DIR = Join-Path $PROJECT_ROOT "frontend"
    $adminStdoutLog = Join-Path $LOG_DIR "mall-admin-web.log"
    $adminStderrLog = Join-Path $LOG_DIR "mall-admin-web-error.log"
    $appStdoutLog = Join-Path $LOG_DIR "mall-app-web.log"
    $appStderrLog = Join-Path $LOG_DIR "mall-app-web-error.log"

    $startedFrontendProcesses = @()
    $frontendPidEntries = @()
    $frontendStartupFailed = $false
    $frontendFailures = @()
    $adminResult = $null
    $appResult = $null

    $adminExisting = Get-ListeningProcessInfo -Port 8090
    if ($adminExisting) {
        $isAdminWeb = Test-CommandLineContainsAnyToken -CommandLine $adminExisting.CommandLine -Tokens @("mall-admin-web", "vite")
        if ($isAdminWeb) {
            Write-Host "  admin-web 已在端口 8090 运行 (PID $($adminExisting.ProcessId))，跳过。" -ForegroundColor DarkYellow
            $adminResult = @{ Success = $true; Port = 8090; Message = $null }
            $frontendPidEntries += "mall-admin-web=$($adminExisting.ProcessId)"
        } else {
            $displayName = if ($adminExisting.ProcessName) { $adminExisting.ProcessName } else { "unknown" }
            $frontendStartupFailed = $true
            $frontendFailures += "admin-web: 端口 8090 被 PID $($adminExisting.ProcessId) ($displayName) 占用"
        }
    } else {
        foreach ($logPath in @($adminStdoutLog, $adminStderrLog)) {
            if (Test-Path $logPath) { Remove-Item $logPath -Force -ErrorAction SilentlyContinue }
        }

        Write-Host "  正在启动 admin-web（预期端口 8090）..." -ForegroundColor Cyan
        $adminProc = Start-Process -FilePath "cmd.exe" `
            -ArgumentList "/c", "cd /d `"$FRONTEND_DIR`" && pnpm dev:admin > `"$adminStdoutLog`" 2> `"$adminStderrLog`"" `
            -WindowStyle Hidden -PassThru
        $startedFrontendProcesses += $adminProc

        Write-Host "  正在验证 admin-web 就绪状态..." -ForegroundColor Yellow
        $adminResult = Wait-FrontendReady -Name "admin-web" -Process $adminProc -StdoutLog $adminStdoutLog -StderrLog $adminStderrLog -ExpectedPort 8090
        if (-not $adminResult.Success) {
            $frontendStartupFailed = $true
            $frontendFailures += "admin-web: $($adminResult.Message)"
        } else {
            $adminResolved = Get-ListeningProcessInfo -Port $adminResult.Port
            $adminPid = if ($adminResolved) { $adminResolved.ProcessId } else { $adminProc.Id }
            $frontendPidEntries += "mall-admin-web=$adminPid"
        }
    }

    $appExisting = Get-ListeningProcessInfo -Port 8091
    if ($appExisting) {
        $isAppWeb = Test-CommandLineContainsAnyToken -CommandLine $appExisting.CommandLine -Tokens @("mall-app-web", "vite")
        if ($isAppWeb) {
            Write-Host "  app-web 已在端口 8091 运行 (PID $($appExisting.ProcessId))，跳过。" -ForegroundColor DarkYellow
            $appResult = @{ Success = $true; Port = 8091; Message = $null }
            $frontendPidEntries += "mall-app-web=$($appExisting.ProcessId)"
        } else {
            $displayName = if ($appExisting.ProcessName) { $appExisting.ProcessName } else { "unknown" }
            $frontendStartupFailed = $true
            $frontendFailures += "app-web: 端口 8091 被 PID $($appExisting.ProcessId) ($displayName) 占用"
        }
    } else {
        foreach ($logPath in @($appStdoutLog, $appStderrLog)) {
            if (Test-Path $logPath) { Remove-Item $logPath -Force -ErrorAction SilentlyContinue }
        }

        Write-Host "  正在启动 app-web（预期端口 8091）..." -ForegroundColor Cyan
        $appProc = Start-Process -FilePath "cmd.exe" `
            -ArgumentList "/c", "cd /d `"$FRONTEND_DIR`" && pnpm dev:app > `"$appStdoutLog`" 2> `"$appStderrLog`"" `
            -WindowStyle Hidden -PassThru
        $startedFrontendProcesses += $appProc

        Write-Host "  正在验证 app-web 就绪状态..." -ForegroundColor Yellow
        $appResult = Wait-FrontendReady -Name "app-web" -Process $appProc -StdoutLog $appStdoutLog -StderrLog $appStderrLog -ExpectedPort 8091
        if (-not $appResult.Success) {
            $frontendStartupFailed = $true
            $frontendFailures += "app-web: $($appResult.Message)"
        } else {
            $appResolved = Get-ListeningProcessInfo -Port $appResult.Port
            $appPid = if ($appResolved) { $appResolved.ProcessId } else { $appProc.Id }
            $frontendPidEntries += "mall-app-web=$appPid"
        }
    }

    if ($frontendStartupFailed -or -not $adminResult -or -not $appResult) {
        Write-Host "前端启动失败。" -ForegroundColor Red
        $frontendFailures | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
        Write-Host "  日志: $adminStdoutLog, $adminStderrLog, $appStdoutLog, $appStderrLog" -ForegroundColor Yellow

        foreach ($proc in $startedFrontendProcesses) {
            if ($proc -and (Get-Process -Id $proc.Id -ErrorAction SilentlyContinue)) {
                taskkill /PID $proc.Id /T /F *> $null
            }
        }
        if (Test-Path $FRONTEND_PID_FILE) { Remove-Item $FRONTEND_PID_FILE -Force -ErrorAction SilentlyContinue }
        exit 1
    }

    $frontendPidEntries | Sort-Object -Unique | Out-File -FilePath $FRONTEND_PID_FILE -Encoding UTF8
    $adminWebUrl = "http://localhost:$($adminResult.Port)"
    $appWebUrl = "http://localhost:$($appResult.Port)"

    if ($adminResult.Port -ne 8090) {
        Write-Host "  admin-web 请求 8090 但实际运行在 $($adminResult.Port)。" -ForegroundColor Yellow
    }
    if ($appResult.Port -ne 8091) {
        Write-Host "  app-web 请求 8091 但实际运行在 $($appResult.Port)。" -ForegroundColor Yellow
    }
} else {
    Write-Host "`n[5/5] 前端：请在 frontend/ 目录运行 'pnpm dev:admin' / 'pnpm dev:app'" -ForegroundColor DarkGray
    Write-Host "       或重新以 -Frontend 参数运行本脚本。" -ForegroundColor DarkGray
}

Write-Host "`n===== Mall V3 已启动 =====" -ForegroundColor Green
Write-Host "  Admin API:  http://localhost:18081" -ForegroundColor Cyan
Write-Host "  App API:    http://localhost:18080" -ForegroundColor Cyan
Write-Host "  Job:        http://localhost:18082" -ForegroundColor Cyan
Write-Host "  Admin 前端: $adminWebUrl" -ForegroundColor Cyan
Write-Host "  App 前端:   $appWebUrl" -ForegroundColor Cyan
Write-Host "  日志目录:   $LOG_DIR" -ForegroundColor DarkGray
Write-Host "  停止命令:   .\stop-v3.ps1" -ForegroundColor DarkGray

try {
    Show-CurrentLoggableAccounts
} catch {
    Write-Host "`n[诊断] 无法检测登录账户: $($_.Exception.Message)" -ForegroundColor DarkYellow
}
