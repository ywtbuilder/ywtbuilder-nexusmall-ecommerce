# Mall V3 - 停止脚本 (PowerShell)
# 用法: .\stop-v3.ps1 [-KeepInfra] [-ForcePortKill]
# 参数说明:
#   -KeepInfra    保持 Docker 容器运行（仅停止应用进程）
#   -ForcePortKill  强制杀死占用已知端口的所有进程

param(
    [switch]$KeepInfra,
    [switch]$ForcePortKill
)

$ErrorActionPreference = "Continue"
$ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $ROOT
$LOG_DIR = Join-Path $PROJECT_ROOT "runtime-logs"
$BACKEND_PID_FILE = Join-Path $LOG_DIR "backend-pids.txt"
$FRONTEND_PID_FILE = Join-Path $LOG_DIR "frontend-pids.txt"

$backendServicePattern = "(?i)mall-(app-api|admin-api|job)"
$backendProjectPath = Join-Path $PROJECT_ROOT "backend"
$backendProjectPattern = "(?i)$([regex]::Escape($backendProjectPath).Replace("\\", "[\\/]"))"
$backendPorts = @(18080, 18081, 18082)
$frontendDefaultPorts = @(8090, 8091)
$frontendLogFiles = @("mall-admin-web.log", "mall-app-web.log")
$frontendProcessNames = @("node", "pnpm", "cmd")

function Normalize-Text {
    param([string]$Text)
    if (-not $Text) { return "" }
    return $Text.ToLowerInvariant().Replace("/", "\")
}

$frontendPathToken = Normalize-Text (Join-Path $PROJECT_ROOT "frontend")
$frontendKeywords = @("mall-admin-web", "mall-app-web", "dev:admin", "dev:app")

# Protect current terminal process chain to avoid killing VS Code integrated terminal.
$protectedPids = New-Object 'System.Collections.Generic.HashSet[int]'
$null = $protectedPids.Add([int]$PID)
try {
    $cursor = Get-CimInstance Win32_Process -Filter "ProcessId = $PID" -ErrorAction Stop
    while ($cursor -and $cursor.ParentProcessId -gt 0) {
        $parentPid = [int]$cursor.ParentProcessId
        if ($protectedPids.Contains($parentPid)) { break }
        $null = $protectedPids.Add($parentPid)
        $cursor = Get-CimInstance Win32_Process -Filter "ProcessId = $parentPid" -ErrorAction SilentlyContinue
    }
} catch {}

$stoppedPids = New-Object 'System.Collections.Generic.HashSet[int]'

function Get-ProcessCommandLine {
    param([int]$ProcessId)
    $wmiProc = Get-CimInstance Win32_Process -Filter "ProcessId = $ProcessId" -ErrorAction SilentlyContinue
    if ($wmiProc) { return $wmiProc.CommandLine }
    return ""
}

function Test-FrontendCommandLine {
    param([string]$CommandLine)

    $cmdNorm = Normalize-Text $CommandLine
    if (-not $cmdNorm) { return $false }

    $hasKeyword = $false
    foreach ($token in $frontendKeywords) {
        if ($cmdNorm.Contains($token)) {
            $hasKeyword = $true
            break
        }
    }

    $hasFrontendPath = $cmdNorm.Contains($frontendPathToken)
    $hasFrontendRuntimeToken = $cmdNorm.Contains("pnpm") -or $cmdNorm.Contains("node") -or $cmdNorm.Contains("vite")

    return $hasKeyword -or ($hasFrontendPath -and $hasFrontendRuntimeToken)
}

function Get-FrontendPortsFromLogs {
    $ports = New-Object 'System.Collections.Generic.HashSet[int]'
    foreach ($logName in $frontendLogFiles) {
        $logPath = Join-Path $LOG_DIR $logName
        if (-not (Test-Path $logPath)) { continue }
        try {
            $matches = Select-String -Path $logPath -Pattern 'http://localhost:(\d+)/' -AllMatches -ErrorAction SilentlyContinue
            foreach ($lineMatch in $matches) {
                foreach ($singleMatch in $lineMatch.Matches) {
                    $portStr = $singleMatch.Groups[1].Value
                    $parsedPort = 0
                    if ($portStr -and [int]::TryParse($portStr, [ref]$parsedPort)) {
                        if ($parsedPort -gt 0) { $null = $ports.Add($parsedPort) }
                    }
                }
            }
        } catch {}
    }
    return @($ports | Sort-Object)
}

function Get-PortListeners {
    param([int[]]$Ports)

    $results = @()
    if (-not $Ports -or $Ports.Count -eq 0) { return @() }

    foreach ($port in ($Ports | Sort-Object -Unique)) {
        $listeners = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        $uniqueListeners = $listeners | Sort-Object OwningProcess -Unique

        foreach ($listener in $uniqueListeners) {
            $ownerPid = [int]$listener.OwningProcess
            $proc = Get-Process -Id $ownerPid -ErrorAction SilentlyContinue
            $wmiProc = Get-CimInstance Win32_Process -Filter "ProcessId = $ownerPid" -ErrorAction SilentlyContinue

            $results += [PSCustomObject]@{
                Port        = $port
                ProcessId   = $ownerPid
                ProcessName = if ($proc) { $proc.ProcessName } elseif ($wmiProc -and $wmiProc.Name) { $wmiProc.Name.Replace(".exe", "") } else { "unknown" }
                Path        = if ($proc -and $proc.Path) { $proc.Path } elseif ($wmiProc) { $wmiProc.ExecutablePath } else { "" }
                CommandLine = if ($wmiProc) { $wmiProc.CommandLine } else { "" }
            }
        }
    }

    return $results
}

function Write-PortDiagnostics {
    param(
        [string]$Header,
        [object[]]$Listeners
    )

    if (@($Listeners).Count -eq 0) {
        Write-Host "  ${Header}: 无占用。" -ForegroundColor DarkGray
        return
    }

    Write-Host "  ${Header}:" -ForegroundColor Yellow
    foreach ($entry in ($Listeners | Sort-Object Port, ProcessId)) {
        $pathText = if ($entry.Path) { $entry.Path } else { "路径未知" }
        Write-Host "    端口 $($entry.Port) -> PID $($entry.ProcessId) [$($entry.ProcessName)] $pathText" -ForegroundColor DarkYellow

        if ($entry.CommandLine) {
            $commandLine = $entry.CommandLine.Trim()
            if ($commandLine.Length -gt 220) {
                $commandLine = $commandLine.Substring(0, 220) + "..."
            }
            Write-Host "      CMD: $commandLine" -ForegroundColor DarkGray
        }
    }
}

function Stop-ProcessTreeIfMatch {
    param(
        [int]$ProcessId,
        [string]$Label,
        [string[]]$AllowedProcessNames,
        [string]$CommandPattern
    )

    if ($ProcessId -le 0) { return $false }
    $proc = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
    if (-not $proc) { return $false }

    if ($AllowedProcessNames -and ($AllowedProcessNames -notcontains $proc.ProcessName)) {
        return $false
    }
    if ($protectedPids.Contains($ProcessId)) {
        Write-Host "  跳过受保护的进程 PID $ProcessId ($Label)。" -ForegroundColor DarkYellow
        return $false
    }
    if ($stoppedPids.Contains($ProcessId)) { return $false }

    if ($CommandPattern) {
        $cmdLine = Get-ProcessCommandLine -ProcessId $ProcessId
        if (-not ($cmdLine -match $CommandPattern)) {
            return $false
        }
    }

    try {
        taskkill /PID $ProcessId /T /F *> $null
        Start-Sleep -Milliseconds 120
    } catch {}

    $alive = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
    if ($alive) {
        try {
            Stop-Process -Id $ProcessId -Force -ErrorAction SilentlyContinue
            Start-Sleep -Milliseconds 120
        } catch {}
        $alive = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
    }

    if (-not $alive) {
        $null = $stoppedPids.Add($ProcessId)
        Write-Host "  已停止 $Label (PID $ProcessId)。" -ForegroundColor Green
        return $true
    }

    Write-Host "  停止 $Label (PID $ProcessId) 失败。" -ForegroundColor Red
    return $false
}

function Stop-PidsFromFile {
    param(
        [string]$PidFilePath,
        [string[]]$AllowedProcessNames,
        [hashtable]$ServiceCommandPatterns,
        [string]$DefaultPattern
    )

    $count = 0
    if (-not (Test-Path $PidFilePath)) { return $count }

    $pidLines = Get-Content $PidFilePath -ErrorAction SilentlyContinue
    foreach ($line in $pidLines) {
        if ($line -match "^\s*([^=]+)=(\d+)\s*$") {
            $svcName = $Matches[1].Trim()
            $targetPid = [int]$Matches[2]
            $pattern = if ($ServiceCommandPatterns -and $ServiceCommandPatterns.ContainsKey($svcName)) {
                $ServiceCommandPatterns[$svcName]
            } else {
                $DefaultPattern
            }
            if (Stop-ProcessTreeIfMatch -ProcessId $targetPid -Label $svcName -AllowedProcessNames $AllowedProcessNames -CommandPattern $pattern) {
                $count++
            }
        }
    }

    Remove-Item $PidFilePath -Force -ErrorAction SilentlyContinue
    return $count
}

Write-Host "===== Mall V3 停止 =====" -ForegroundColor Red

# 1) 停止后端服务
Write-Host "[1/3] 停止后端服务..." -ForegroundColor Yellow
$backendStoppedCount = 0

$backendServicePatterns = @{
    "mall-app-api"   = "(?i)mall-app-api"
    "mall-admin-api" = "(?i)mall-admin-api"
    "mall-job"       = "(?i)mall-job"
}

$backendStoppedCount += Stop-PidsFromFile `
    -PidFilePath $BACKEND_PID_FILE `
    -AllowedProcessNames @("java", "javaw") `
    -ServiceCommandPatterns $backendServicePatterns `
    -DefaultPattern $backendServicePattern

# Fallback A: scan Java processes for mall service jars or backend workspace path.
Get-Process -Name "java", "javaw" -ErrorAction SilentlyContinue | ForEach-Object {
    $cmdLine = Get-ProcessCommandLine -ProcessId $_.Id
    if (($cmdLine -match $backendServicePattern) -or ($cmdLine -match $backendProjectPattern)) {
        if (Stop-ProcessTreeIfMatch -ProcessId $_.Id -Label "backend-java-fallback" -AllowedProcessNames @("java", "javaw") -CommandPattern $null) {
            $backendStoppedCount++
        }
    }
}

# Fallback B: stop listeners on known backend ports.
foreach ($port in $backendPorts) {
    $listeners = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    foreach ($listener in $listeners) {
        if (Stop-ProcessTreeIfMatch -ProcessId $listener.OwningProcess -Label "backend-port-$port" -AllowedProcessNames @("java", "javaw") -CommandPattern $null) {
            $backendStoppedCount++
        }
    }
}

if ($ForcePortKill) {
    Write-Host "  已开启后端端口强制清理: $($backendPorts -join ', ')" -ForegroundColor DarkYellow
    foreach ($port in $backendPorts) {
        $listeners = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        foreach ($listener in $listeners) {
            if (Stop-ProcessTreeIfMatch -ProcessId $listener.OwningProcess -Label "backend-force-port-$port" -AllowedProcessNames $null -CommandPattern $null) {
                $backendStoppedCount++
            }
        }
    }
}

if ($backendStoppedCount -eq 0) { Write-Host "  未发现后端进程。" -ForegroundColor DarkGray }
else { Write-Host "  已停止 $backendStoppedCount 个后端进程。" -ForegroundColor Green }

# 2) 停止前端开发服务器
Write-Host "[2/3] 停止前端开发服务器..." -ForegroundColor Yellow
$frontendStoppedCount = 0

$frontendStoppedCount += Stop-PidsFromFile `
    -PidFilePath $FRONTEND_PID_FILE `
    -AllowedProcessNames $frontendProcessNames `
    -ServiceCommandPatterns $null `
    -DefaultPattern $null

$portsFromLogs = Get-FrontendPortsFromLogs
$frontendPorts = @($frontendDefaultPorts + $portsFromLogs | Sort-Object -Unique)
if ($portsFromLogs.Count -gt 0) {
    Write-Host "  从日志检测到的前端端口: $($portsFromLogs -join ', ')" -ForegroundColor DarkGray
}

foreach ($port in $frontendPorts) {
    $listeners = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    foreach ($listener in $listeners) {
        if (Stop-ProcessTreeIfMatch -ProcessId $listener.OwningProcess -Label "frontend-port-$port" -AllowedProcessNames $frontendProcessNames -CommandPattern $null) {
            $frontendStoppedCount++
        }
    }
}

# Fallback: stop command-line matched frontend processes, even when they moved to random ports.
Get-CimInstance Win32_Process -ErrorAction SilentlyContinue | ForEach-Object {
    $procName = if ($_.Name) { $_.Name.ToLowerInvariant().Replace(".exe", "") } else { "" }
    if (($frontendProcessNames -contains $procName) -and $_.CommandLine -and (Test-FrontendCommandLine -CommandLine $_.CommandLine)) {
        if (Stop-ProcessTreeIfMatch -ProcessId ([int]$_.ProcessId) -Label "frontend-cmd-fallback" -AllowedProcessNames $frontendProcessNames -CommandPattern $null) {
            $frontendStoppedCount++
        }
    }
}

if ($ForcePortKill) {
    Write-Host "  已开启前端端口强制清理: $($frontendPorts -join ', ')" -ForegroundColor DarkYellow
    foreach ($port in $frontendPorts) {
        $listeners = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
        foreach ($listener in $listeners) {
            if (Stop-ProcessTreeIfMatch -ProcessId $listener.OwningProcess -Label "frontend-force-port-$port" -AllowedProcessNames $null -CommandPattern $null) {
                $frontendStoppedCount++
            }
        }
    }
}

if ($frontendStoppedCount -eq 0) { Write-Host "  未发现前端开发服务器。" -ForegroundColor DarkGray }
else { Write-Host "  已停止 $frontendStoppedCount 个前端进程。" -ForegroundColor Green }

$allKnownPorts = @($backendPorts + $frontendPorts | Sort-Object -Unique)
$remainingBackendListeners = @(Get-PortListeners -Ports $backendPorts)
$remainingFrontendListeners = @(Get-PortListeners -Ports $frontendPorts)
$remainingAllListeners = @(Get-PortListeners -Ports $allKnownPorts)

if ($remainingAllListeners.Count -gt 0) {
    Write-Host "[诊断] 停止后端口占用情况:" -ForegroundColor Yellow
    Write-PortDiagnostics -Header "后端端口仍被占用" -Listeners $remainingBackendListeners
    Write-PortDiagnostics -Header "前端端口仍被占用" -Listeners $remainingFrontendListeners

    if (-not $ForcePortKill) {
        Write-Host "  提示: 使用 '.\stop-v3.ps1 -ForcePortKill' 可强制清理剩余监听器。" -ForegroundColor DarkYellow
    }
} else {
    Write-Host "[诊断] 所有已知 Mall 端口均已释放。" -ForegroundColor DarkGray
}

# 3) 停止基础设施
if (-not $KeepInfra) {
    Write-Host "[3/3] 停止基础设施容器..." -ForegroundColor Yellow
    $infraDir = Join-Path $PROJECT_ROOT "infra"
    $composeFile = Join-Path $infraDir "docker-compose.local.yml"

    if (-not (Test-Path $composeFile)) {
        Write-Host "  Compose 文件未找到: $composeFile" -ForegroundColor DarkYellow
    } elseif (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
        Write-Host "  未找到 Docker CLI，跳过基础设施停止。" -ForegroundColor DarkYellow
    } else {
        Push-Location $infraDir
        try {
            docker compose -f docker-compose.local.yml down --remove-orphans
            if ($LASTEXITCODE -eq 0) {
                Write-Host "  基础设施已停止。" -ForegroundColor Green
            } else {
                Write-Host "  docker compose down 返回退出码 $LASTEXITCODE。" -ForegroundColor Yellow
            }
        } catch {
            Write-Host "  停止基础设施失败: $($_.Exception.Message)" -ForegroundColor Yellow
        } finally {
            Pop-Location
        }
    }
} else {
    Write-Host "[3/3] 保持基础设施运行 (--KeepInfra)" -ForegroundColor DarkGray
}

# 清理旧日志文件（保留最新 18 个）
if (Test-Path $LOG_DIR) {
    $keepLogCount = 18
    $oldLogs = Get-ChildItem -Path $LOG_DIR -Filter "*.log" | Sort-Object LastWriteTime -Descending | Select-Object -Skip $keepLogCount
    if ($oldLogs) {
        $oldLogs | Remove-Item -Force -ErrorAction SilentlyContinue
        Write-Host "  已清理 $($oldLogs.Count) 个旧日志文件。" -ForegroundColor DarkGray
    }
}

Write-Host "===== Mall V3 已停止 =====" -ForegroundColor Green

