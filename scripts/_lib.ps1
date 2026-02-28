<#
.SYNOPSIS
  Mall V3 - Shared Script Utilities (_lib.ps1)
.DESCRIPTION
  Dot-source this file at the top of any scripts/*.ps1:
    . (Join-Path $PSScriptRoot "_lib.ps1")
  Provides:
    Write-Step            - 颜色步骤标题打印
    Invoke-KillPort       - 按端口杀进程
    Invoke-KillPidFile    - 按 PID 文件批量杀进程
    Kill-FrontendByCmdline- 按 node.exe 命令行关键字杀进程
    Wait-AllPortsOpen     - 并行等待多个端口就绪（单轮询循环）
    Get-Fingerprint       - SHA-256 代码指纹
    Show-LogTail          - 打印日志尾部
    Ensure-LogDir         - 确保日志目录存在
#>

# -- 步骤标题颜色打印 -------------------------------------------
function Write-Step([string]$msg) {
    Write-Host ""
    Write-Host $msg -ForegroundColor Yellow
}

# -- 按端口杀进程 -----------------------------------------------
function Invoke-KillPort([int]$port, [string]$label = "") {
    $conn = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($conn) {
        $pid2 = [int]$conn.OwningProcess
        $lbl  = if ($label) { "$label " } else { "" }
        Write-Host "  [stop] ${lbl}pid=$pid2  port=$port" -ForegroundColor DarkYellow
        try { taskkill /PID $pid2 /T /F 2>&1 | Out-Null } catch {}
        Start-Sleep -Milliseconds 800
    }
}

# -- 按 PID 文件批量杀进程 ---------------------------------------
function Invoke-KillPidFile([string]$path) {
    if (-not (Test-Path $path)) { return }
    Get-Content $path -ErrorAction SilentlyContinue | ForEach-Object {
        $parts = $_ -split "="
        if ($parts.Count -ge 2) {
            $pid2 = 0
            if ([int]::TryParse($parts[1].Trim(), [ref]$pid2) -and $pid2 -gt 0) {
                try { taskkill /PID $pid2 /T /F 2>&1 | Out-Null } catch {}
            }
        }
    }
    Remove-Item $path -Force -ErrorAction SilentlyContinue
}

# -- 按 node.exe 命令行关键字杀进程 --------------------------------
function Kill-FrontendByCmdline([string]$keyword) {
    Get-WmiObject Win32_Process -Filter "Name='node.exe'" 2>$null |
        Where-Object { $_.CommandLine -like "*$keyword*" } |
        ForEach-Object {
            Write-Host "  [stop] node keyword=$keyword  pid=$($_.ProcessId)" -ForegroundColor DarkYellow
            try { taskkill /PID $_.ProcessId /T /F 2>&1 | Out-Null } catch {}
        }
}

# -- 并行等待多个端口就绪 ----------------------------------------
# $Services: array of @{Name="svc"; Port=18080; ProcId=1234}
#   ProcId=0 表示不检测进程存活
# 返回: @{ port -> $true/$false }
function Wait-AllPortsOpen {
    param(
        [array]$Services,
        [int]$TimeoutSec = 90
    )
    $deadline = (Get-Date).AddSeconds($TimeoutSec)
    $pending  = [System.Collections.Generic.List[hashtable]]::new()
    foreach ($s in $Services) { $pending.Add($s) }
    $results  = @{}
    foreach ($s in $Services) { $results[[int]$s.Port] = $false }

    # 打印等待列表
    $names = ($Services | ForEach-Object { "$($_.Name):$($_.Port)" }) -join ", "
    Write-Host "  Waiting for: $names" -ForegroundColor Cyan

    while ($pending.Count -gt 0 -and (Get-Date) -lt $deadline) {
        $next = [System.Collections.Generic.List[hashtable]]::new()
        foreach ($s in $pending) {
            # 检查进程是否还在运行
            if ($s.ProcId -gt 0 -and -not (Get-Process -Id $s.ProcId -ErrorAction SilentlyContinue)) {
                Write-Host "  [DIED] $($s.Name) (pid=$($s.ProcId))" -ForegroundColor Red
                continue  # 从 pending 中删除，results 保持 $false
            }
            if (Get-NetTCPConnection -LocalPort $s.Port -State Listen -ErrorAction SilentlyContinue) {
                $results[[int]$s.Port] = $true
                Write-Host "  [OK]   $($s.Name) :$($s.Port)" -ForegroundColor Green
            } else {
                $next.Add($s)
            }
        }
        $pending = $next
        if ($pending.Count -gt 0) {
            Write-Host "." -NoNewline -ForegroundColor DarkGray
            Start-Sleep -Seconds 2
        }
    }

    # 仍在 pending 的均已超时
    foreach ($s in $pending) {
        Write-Host "  [TIMEOUT] $($s.Name) :$($s.Port)" -ForegroundColor Red
    }
    Write-Host ""
    return $results
}

# -- SHA-256 代码指纹 -------------------------------------------
function Get-Fingerprint([string]$dir) {
    $root  = [System.IO.Path]::GetFullPath($dir).TrimEnd('\')
    $files = Get-ChildItem -Path $dir -Recurse -File |
        Where-Object { $_.FullName -notmatch '\\target\\' -and ($_.Name -eq "pom.xml" -or $_.FullName -match '\\src\\') }
    if (-not $files) { return "empty" }
    $rows  = $files | ForEach-Object {
        $rel = ([System.IO.Path]::GetFullPath($_.FullName)).Substring($root.Length).TrimStart('\')
        "$rel|$($_.LastWriteTimeUtc.Ticks)|$($_.Length)"
    } | Sort-Object
    $bytes = [System.Text.Encoding]::UTF8.GetBytes(($rows -join "`n"))
    $sha   = [System.Security.Cryptography.SHA256]::Create()
    try { return ([System.BitConverter]::ToString($sha.ComputeHash($bytes))).Replace("-","").ToLower() }
    finally { $sha.Dispose() }
}

# -- 打印日志文件尾部 -------------------------------------------
function Show-LogTail([string]$path, [int]$n = 20) {
    if (Test-Path $path) {
        Get-Content $path -Tail $n | ForEach-Object { Write-Host "    $_" -ForegroundColor DarkGray }
    }
}

# -- 确保日志目录存在 -------------------------------------------
function Ensure-LogDir([string]$path) {
    if (-not (Test-Path $path)) {
        New-Item -ItemType Directory -Path $path -Force | Out-Null
    }
}
