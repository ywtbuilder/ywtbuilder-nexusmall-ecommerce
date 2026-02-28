# Mall V3 -- 环境预检
# 用法: .\preflight-v3.ps1

$ErrorActionPreference = "Stop"
$ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $ROOT
$errors = @()
$warnings = @()

Write-Host "===== Mall V3 环境预检 =====" -ForegroundColor Cyan

# Java 17+
Write-Host -NoNewline "Java 17+:      "
try {
    $javaVersionLine = (java --version 2>$null | Select-Object -First 1)
    if (-not $javaVersionLine) { throw "Unable to read java version" }
    $javaMatch = [regex]::Match($javaVersionLine, "(\d+)\.")
    if (-not $javaMatch.Success) { throw "Unable to parse java version: $javaVersionLine" }
    $javaVer = $javaMatch.Groups[1].Value
    if ([int]$javaVer -ge 17) { Write-Host "就绪 ($javaVer)" -ForegroundColor Green }
    else { $errors += "需要 Java 17+，当前 $javaVer"; Write-Host "失败" -ForegroundColor Red }
} catch { $errors += "未找到 Java"; Write-Host "失败" -ForegroundColor Red }

# Maven Wrapper
Write-Host -NoNewline "Maven Wrapper: "
$mvnwPath = Join-Path $PROJECT_ROOT "backend\mvnw.cmd"
if (Test-Path $mvnwPath) { Write-Host "就绪" -ForegroundColor Green }
else {
    # 回退到系统 Maven
    try { mvn --version | Out-Null; Write-Host "就绪 (系统 mvn)" -ForegroundColor Yellow; $warnings += "使用系统 mvn，建议添加 mvnw" }
    catch { $errors += "未找到 mvnw.cmd 或 mvn"; Write-Host "失败" -ForegroundColor Red }
}

# Docker
Write-Host -NoNewline "Docker:        "
try {
    docker --version | Out-Null
    # Check Docker daemon running
    docker info 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) { Write-Host "就绪" -ForegroundColor Green }
    else { $errors += "Docker 守护进程未运行"; Write-Host "未运行" -ForegroundColor Red }
} catch { $errors += "未找到 Docker"; Write-Host "失败" -ForegroundColor Red }

# Node.js 20+
Write-Host -NoNewline "Node.js 20+:   "
try {
    $nodeVer = (node --version) -replace 'v',''
    $major = [int]($nodeVer.Split('.')[0])
    if ($major -ge 20) { Write-Host "就绪 ($nodeVer)" -ForegroundColor Green }
    else { $warnings += "建议前端使用 Node 20+，当前 $nodeVer"; Write-Host "警告 ($nodeVer)" -ForegroundColor Yellow }
} catch { $warnings += "未找到 Node.js（仅前端开发需要）"; Write-Host "未找到 (可选)" -ForegroundColor Yellow }

# pnpm
Write-Host -NoNewline "pnpm:          "
try { pnpm --version | Out-Null; Write-Host "就绪" -ForegroundColor Green }
catch { $warnings += "未找到 pnpm（仅前端开发需要）"; Write-Host "未找到 (可选)" -ForegroundColor Yellow }

# 基础设施端口检查
Write-Host "`n基础设施端口:" -ForegroundColor Cyan
$infraPorts = @(
    @{ Port = 13306; Name = "MySQL" },
    @{ Port = 16379; Name = "Redis" },
    @{ Port = 27018; Name = "MongoDB" },
    @{ Port = 5673;  Name = "RabbitMQ" },
    @{ Port = 9201;  Name = "Elasticsearch" },
    @{ Port = 19090; Name = "MinIO API" },
    @{ Port = 19001; Name = "MinIO Console" }
)
foreach ($p in $infraPorts) {
    Write-Host -NoNewline "  $($p.Name) ($($p.Port)): "
    $conn = Get-NetTCPConnection -LocalPort $p.Port -State Listen -ErrorAction SilentlyContinue
    if ($conn) { Write-Host "占用中" -ForegroundColor Yellow }
    else { Write-Host "空闲" -ForegroundColor Green }
}

# 后端端口检查
Write-Host "后端端口:" -ForegroundColor Cyan
$backendPorts = @(
    @{ Port = 18080; Name = "App API" },
    @{ Port = 18081; Name = "Admin API" },
    @{ Port = 18082; Name = "Job" }
)
foreach ($p in $backendPorts) {
    Write-Host -NoNewline "  $($p.Name) ($($p.Port)): "
    $conn = Get-NetTCPConnection -LocalPort $p.Port -State Listen -ErrorAction SilentlyContinue
    if ($conn) { Write-Host "占用中" -ForegroundColor Yellow }
    else { Write-Host "空闲" -ForegroundColor Green }
}

# 前端端口检查
Write-Host "前端端口:" -ForegroundColor Cyan
$frontendPorts = @(
    @{ Port = 8090; Name = "Admin Web" },
    @{ Port = 8091; Name = "App Web" }
)
foreach ($p in $frontendPorts) {
    Write-Host -NoNewline "  $($p.Name) ($($p.Port)): "
    $conn = Get-NetTCPConnection -LocalPort $p.Port -State Listen -ErrorAction SilentlyContinue
    if ($conn) { Write-Host "占用中" -ForegroundColor Yellow }
    else { Write-Host "空闲" -ForegroundColor Green }
}

# 汇总
if ($warnings.Count -gt 0) {
    Write-Host "`n===== 警告 =====" -ForegroundColor Yellow
    $warnings | ForEach-Object { Write-Host "  $_" -ForegroundColor Yellow }
}

if ($errors.Count -gt 0) {
    Write-Host "`n===== 错误 =====" -ForegroundColor Red
    $errors | ForEach-Object { Write-Host "  $_" -ForegroundColor Red }
    exit 1
}

Write-Host "`n===== 所有检查均通过 =====" -ForegroundColor Green
