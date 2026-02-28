# Mall V3 - Dev Start Script (PowerShell)
# Usage: .\dev-start-v3.ps1 [-Service all|app|admin|job] [-Profile local] [-SkipInfra] [-SkipPrepare] [-StopFirst] [-HealthTimeoutSec 90]
# Purpose: start backend services via spring-boot:run so Spring DevTools restart can work during development.

param(
    [ValidateSet("all", "app", "admin", "job")]
    [string]$Service = "all",
    [string]$Profile = "local",
    [switch]$SkipInfra,
    [switch]$SkipPrepare,
    [switch]$StopFirst,
    [int]$HealthTimeoutSec = 90
)

$ErrorActionPreference = "Stop"

$ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $ROOT
$BACKEND_DIR = Join-Path $PROJECT_ROOT "backend"
$INFRA_DIR = Join-Path $PROJECT_ROOT "infra"
$LOG_DIR = Join-Path $PROJECT_ROOT "runtime-logs"
$PID_FILE = Join-Path $LOG_DIR "backend-pids.txt"

if (-not (Test-Path $LOG_DIR)) {
    New-Item -ItemType Directory -Path $LOG_DIR -Force | Out-Null
}

function Get-LogTailText {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path,
        [int]$TailLines = 20
    )

    if (-not (Test-Path $Path)) {
        return "(日志文件未找到：$Path)"
    }

    try {
        return [string]::Join("`n", (Get-Content -Path $Path -Tail $TailLines))
    } catch {
        return "(读取日志失败：$Path)"
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

    try {
        $proc = Get-Process -Id $processId -ErrorAction Stop
        $processName = $proc.ProcessName
    } catch {}

    return @{
        Port = $Port
        ProcessId = $processId
        ProcessName = $processName
    }
}

function Wait-BackendReady {
    param(
        [Parameter(Mandatory = $true)]
        [hashtable]$Svc,
        [Parameter(Mandatory = $true)]
        [System.Diagnostics.Process]$Process,
        [Parameter(Mandatory = $true)]
        [string]$StdoutLog,
        [Parameter(Mandatory = $true)]
        [string]$StderrLog,
        [int]$TimeoutSec = 90
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    while ((Get-Date) -lt $deadline) {
        $alive = Get-Process -Id $Process.Id -ErrorAction SilentlyContinue
        if (-not $alive) {
            $stdoutTail = Get-LogTailText -Path $StdoutLog
            $stderrTail = Get-LogTailText -Path $StderrLog
            return @{
                Success = $false
                Message = "进程在就绪前意外退出。`n      标准输出：`n$stdoutTail`n      标准错误：`n$stderrTail"
            }
        }

        try {
            $resp = Invoke-WebRequest -Uri $Svc.Health -UseBasicParsing -TimeoutSec 2 -ErrorAction SilentlyContinue
            if ($resp -and $resp.StatusCode -ge 200 -and $resp.StatusCode -lt 500) {
                return @{
                    Success = $true
                    Message = $null
                }
            }
        } catch {}

        $listener = Get-ListeningProcessInfo -Port $Svc.Port
        if ($listener) {
            return @{
                Success = $true
                Message = $null
            }
        }

        Start-Sleep -Seconds 1
    }

    $stdoutTail = Get-LogTailText -Path $StdoutLog
    $stderrTail = Get-LogTailText -Path $StderrLog
    return @{
        Success = $false
        Message = "等待 $($Svc.Name)（端口 $($Svc.Port)）超时。`n      标准输出：`n$stdoutTail`n      标准错误：`n$stderrTail"
    }
}

function Stop-StartedProcesses {
    param([System.Collections.ArrayList]$Processes)

    foreach ($proc in $Processes) {
        if (-not $proc) { continue }
        if (Get-Process -Id $proc.Id -ErrorAction SilentlyContinue) {
            try {
                taskkill /PID $proc.Id /T /F *> $null
            } catch {}
        }
    }
}

$allServices = @(
    @{
        Key = "app"
        Name = "mall-app-api"
        Module = "mall-app-api"
        MainClass = "com.mall.app.MallAppApiApplication"
        Port = 18080
        Health = "http://localhost:18080/actuator/health"
    },
    @{
        Key = "admin"
        Name = "mall-admin-api"
        Module = "mall-admin-api"
        MainClass = "com.mall.admin.MallAdminApiApplication"
        Port = 18081
        Health = "http://localhost:18081/actuator/health"
    },
    @{
        Key = "job"
        Name = "mall-job"
        Module = "mall-job"
        MainClass = "com.mall.job.MallJobApplication"
        Port = 18082
        Health = "http://localhost:18082/actuator/health"
    }
)

$selectedServices = if ($Service -eq "all") {
    $allServices
} else {
    @($allServices | Where-Object { $_.Key -eq $Service })
}

if (-not (Test-Path (Join-Path $BACKEND_DIR "mvnw.cmd"))) {
    throw "未找到 Maven Wrapper：$BACKEND_DIR\\mvnw.cmd"
}

Write-Host "===== Mall V3 开发启动 =====" -ForegroundColor Green
Write-Host "  服务组: $Service" -ForegroundColor DarkGray
Write-Host "  配置:   $Profile" -ForegroundColor DarkGray

if ($StopFirst) {
    $stopScript = Join-Path $ROOT "stop-v3.ps1"
    if (Test-Path $stopScript) {
        Write-Host "[0/5] 正在停止应用/前端进程（保留基础设施）..." -ForegroundColor Yellow
        & $stopScript -KeepInfra
    }
}

if (-not $SkipInfra) {
    if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        throw "未找到 docker 命令，请安装 Docker Desktop 或使用 -SkipInfra 跳过基础设施启动。"
    }

    Write-Host "[1/5] 正在启动基础设施容器..." -ForegroundColor Yellow
    Push-Location $INFRA_DIR
    try {
        & docker compose -f docker-compose.local.yml up -d
        if ($LASTEXITCODE -ne 0) {
            throw "docker compose 失败，退出码：$LASTEXITCODE"
        }
    } finally {
        Pop-Location
    }
} else {
    Write-Host "[1/5] 已跳过基础设施启动（-SkipInfra）。" -ForegroundColor DarkGray
}

if (-not $SkipPrepare) {
    $moduleList = ($selectedServices | ForEach-Object { $_.Module }) -join ","
    Write-Host "[2/5] 正在通过 Maven install 准备后端模块（$moduleList）..." -ForegroundColor Yellow
    Push-Location $BACKEND_DIR
    try {
        & .\mvnw.cmd -pl $moduleList -am -DskipTests -q -B install
        if ($LASTEXITCODE -ne 0) {
            throw "Maven prepare 步骤失败，退出码：$LASTEXITCODE。"
        }
    } finally {
        Pop-Location
    }
} else {
    Write-Host "[2/5] 已跳过 Maven prepare（-SkipPrepare）。" -ForegroundColor DarkGray
}

Write-Host "[3/5] 正在通过 spring-boot:run 启动后端服务..." -ForegroundColor Yellow
$startedProcesses = New-Object System.Collections.ArrayList
$pidEntries = @()

foreach ($svc in $selectedServices) {
    $existingListener = Get-ListeningProcessInfo -Port $svc.Port
    if ($existingListener) {
        $displayName = if ($existingListener.ProcessName) { $existingListener.ProcessName } else { "unknown" }
        Stop-StartedProcesses -Processes $startedProcesses
        throw "$($svc.Name)：端口 $($svc.Port) 已被 PID $($existingListener.ProcessId)（$displayName）占用，请先运行 .\scripts\stop-v3.ps1 或使用 -StopFirst。"
    }

    $logFile = Join-Path $LOG_DIR "$($svc.Name).log"
    $errorLogFile = Join-Path $LOG_DIR "$($svc.Name)-error.log"

    foreach ($path in @($logFile, $errorLogFile)) {
        if (Test-Path $path) {
            Remove-Item $path -Force -ErrorAction SilentlyContinue
        }
    }

    $moduleDir = Join-Path $BACKEND_DIR $svc.Module
    if (-not (Test-Path (Join-Path $moduleDir "pom.xml"))) {
        Stop-StartedProcesses -Processes $startedProcesses
        throw "$($svc.Name)：在 $moduleDir 未找到模块 pom.xml"
    }

    $mavenCmd = "..\mvnw.cmd spring-boot:run -Dspring-boot.run.mainClass=$($svc.MainClass) -Dspring-boot.run.profiles=$Profile"
    $command = "cd /d `"$moduleDir`" && $mavenCmd > `"$logFile`" 2> `"$errorLogFile`""

    Write-Host "  正在启动 $($svc.Name)（端口 $($svc.Port)）..." -NoNewline -ForegroundColor Cyan
    $proc = Start-Process -FilePath "cmd.exe" -ArgumentList "/c", $command -WindowStyle Hidden -PassThru
    $null = $startedProcesses.Add($proc)
    Write-Host " 包装进程 PID $($proc.Id)" -ForegroundColor Green

    $ready = Wait-BackendReady -Svc $svc -Process $proc -StdoutLog $logFile -StderrLog $errorLogFile -TimeoutSec $HealthTimeoutSec
    if (-not $ready.Success) {
        Stop-StartedProcesses -Processes $startedProcesses
        throw "$($svc.Name) 启动失败：$($ready.Message)"
    }

    $listener = Get-ListeningProcessInfo -Port $svc.Port
    $listenerPid = if ($listener) { $listener.ProcessId } else { $proc.Id }
    $pidEntries += "$($svc.Name)=$listenerPid"
}

$pidEntries | Sort-Object -Unique | Out-File -FilePath $PID_FILE -Encoding UTF8

Write-Host "[4/5] DevTools 提示（VS Code）：Java 代码变更后需重新编译/构建输出才能触发热重载。" -ForegroundColor DarkGray
Write-Host "[5/5] 后端启动完成。" -ForegroundColor Yellow

Write-Host ""
Write-Host "===== Mall V3 开发模式已启动 =====" -ForegroundColor Green
foreach ($svc in $selectedServices) {
    Write-Host ("  {0,-13} http://localhost:{1}" -f "$($svc.Name):", $svc.Port) -ForegroundColor Cyan
}
Write-Host "  日志目录:  $LOG_DIR" -ForegroundColor DarkGray
Write-Host "  停止命令:  .\scripts\stop-v3.ps1 -KeepInfra" -ForegroundColor DarkGray
