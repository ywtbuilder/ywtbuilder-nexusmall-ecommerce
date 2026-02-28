# Mall V3 — Run Integration Tests
# Usage: .\scripts\run-tests.ps1
# Prerequisite: App API (18080) and Admin API (18081) must be running

$ErrorActionPreference = "Stop"
$ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $ROOT
$TESTS_DIR = Join-Path $PROJECT_ROOT "tests"

Write-Host "===== Mall V3 集成测试 =====" -ForegroundColor Green

# ── Preflight: check services are up ──
Write-Host "`n正在检查服务..." -ForegroundColor Yellow
$services = @(
    @{ Name = "App API";   Port = 18080 },
    @{ Name = "Admin API"; Port = 18081 }
)
$allUp = $true
foreach ($svc in $services) {
    Write-Host -NoNewline "  $($svc.Name) ($($svc.Port)): "
    try {
        $resp = Invoke-WebRequest -Uri "http://localhost:$($svc.Port)/actuator/health" `
            -UseBasicParsing -TimeoutSec 3 -ErrorAction SilentlyContinue
        if ($resp.StatusCode -eq 200) { Write-Host "正常" -ForegroundColor Green }
        else { Write-Host "不健康" -ForegroundColor Red; $allUp = $false }
    } catch {
        Write-Host "无法访问" -ForegroundColor Red; $allUp = $false
    }
}
if (-not $allUp) {
    Write-Host "`n服务未运行，请先启动：.\scripts\start-v3.ps1" -ForegroundColor Red
    exit 1
}

# ── Compile tests ──
Write-Host "`n正在编译测试..." -ForegroundColor Yellow

$contractSrc = Join-Path $TESTS_DIR "contract\AppApiContractTest.java"
$integrationSrc = Join-Path $TESTS_DIR "integration\OrderFlowIntegrationTest.java"
$contractOut = Join-Path $TESTS_DIR "contract"
$integrationOut = Join-Path $TESTS_DIR "integration"

javac -d $contractOut $contractSrc 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "  契约测试编译失败" -ForegroundColor Red
    exit 1
}
Write-Host "  契约测试编译完成" -ForegroundColor Green

javac -d $integrationOut $integrationSrc 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "  集成测试编译失败" -ForegroundColor Red
    exit 1
}
Write-Host "  集成测试编译完成" -ForegroundColor Green

# ── Run contract tests ──
Write-Host "`n" -NoNewline
java -cp $contractOut com.mall.tests.contract.AppApiContractTest
$contractExit = $LASTEXITCODE

# ── Run integration tests ──
Write-Host ""
java -cp $integrationOut com.mall.tests.integration.OrderFlowIntegrationTest
$integrationExit = $LASTEXITCODE

# ── Summary ──
Write-Host "`n===== 测试汇总 =====" -ForegroundColor Green
if ($contractExit -eq 0) { Write-Host "  契约测试:    通过" -ForegroundColor Green }
else { Write-Host "  契约测试:    失败" -ForegroundColor Red }

if ($integrationExit -eq 0) { Write-Host "  集成测试: 通过" -ForegroundColor Green }
else { Write-Host "  集成测试: 失败" -ForegroundColor Red }

if ($contractExit -ne 0 -or $integrationExit -ne 0) { exit 1 }
