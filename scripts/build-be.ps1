<#
.SYNOPSIS
  Mall V3 - Backend Build Only (no restart)
.DESCRIPTION
  Usage: .\build-be.ps1 [-Module all|app|admin|job] [-Clean] [-Test]

    .\build-be.ps1              Build all backend modules
    .\build-be.ps1 -Clean       Clean then build
    .\build-be.ps1 -Test        Include unit tests (default: skip tests)
    .\build-be.ps1 -Module app  Build only mall-app-api
#>
param(
    [ValidateSet("all","app","admin","job")]
    [string]$Module = "all",
    [switch]$Clean,
    [switch]$Test
)

$ErrorActionPreference = "Continue"
$ROOT         = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $ROOT
$BACKEND_DIR  = Join-Path $PROJECT_ROOT "backend"
$LOG_DIR      = Join-Path $PROJECT_ROOT "runtime-logs"
$FINGERPRINT  = Join-Path $LOG_DIR "backend-build.fingerprint"

. (Join-Path $ROOT "_lib.ps1")
Ensure-LogDir $LOG_DIR

# -- Get-Fingerprint 由 _lib.ps1 提供 -----------------------------------

# ================================================================
$T0 = Get-Date

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Mall V3 - 后端构建  [$Module]" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  后端目录:  $BACKEND_DIR" -ForegroundColor DarkGray
Write-Host "  清理构建:  $Clean" -ForegroundColor DarkGray
Write-Host "  运行测试:  $Test" -ForegroundColor DarkGray

if (-not (Test-Path (Join-Path $BACKEND_DIR "pom.xml"))) {
    Write-Host "  错误: 未在 $BACKEND_DIR 找到 pom.xml" -ForegroundColor Red
    exit 1
}

# Build module filter
$mvnGoal = if ($Clean) { @("clean", "package") } else { @("package") }
$mvnArgs = @()
if (-not $Test)     { $mvnArgs += "-DskipTests" }
$mvnArgs += @("-B", "-T", "1C")   # batch mode + 并行编译（1 线程/CPU 核）

if ($Module -ne "all") {
    $MODULE_MAP = @{
        "app"   = "mall-app-api"
        "admin" = "mall-admin-api"
        "job"   = "mall-job"
    }
    $moduleName = $MODULE_MAP[$Module]
    $mvnArgs += @("-pl", "mall-shared,mall-modules,$moduleName", "--also-make")
}

$logFile = Join-Path $LOG_DIR "build-be.log"
$errFile = "$logFile.err"

Write-Host ""
Write-Host "  正在运行: mvnw $($mvnGoal -join ' ') $($mvnArgs -join ' ')" -ForegroundColor Yellow
Write-Host "  实时输出并保存日志 -> $logFile" -ForegroundColor DarkGray
Write-Host ""

Push-Location $BACKEND_DIR
& .\mvnw.cmd @mvnGoal @mvnArgs 2>&1 | Tee-Object -FilePath $logFile
$exitCode = $LASTEXITCODE
Pop-Location
$elapsed  = [int]((Get-Date) - $T0).TotalSeconds

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan

if ($exitCode -eq 0) {
    Write-Host ("  [✓] 构建成功，耗时 {0}s" -f $elapsed) -ForegroundColor Green
    $fp = Get-Fingerprint -dir $BACKEND_DIR
    $fp | Out-File -FilePath $FINGERPRINT -Encoding UTF8 -NoNewline
    Write-Host "  指纹已更新。" -ForegroundColor DarkGray
} else {
    Write-Host ("  [x] 构建失败 (exit {0}, {1}s)" -f $exitCode, $elapsed) -ForegroundColor Red
    Write-Host "  最后 30 行错误:" -ForegroundColor Red
    if (Test-Path $logFile) {
        Get-Content $logFile -Tail 30 | Where-Object { $_ -match 'ERROR|FATAL|\[ERROR\]' } | ForEach-Object {
            Write-Host "    $_" -ForegroundColor Red
        }
    }
    Write-Host "  完整日志: $logFile" -ForegroundColor DarkGray
    exit 1
}

Write-Host "  完整日志: $logFile" -ForegroundColor DarkGray
Write-Host "  部署:   .\restart-be.ps1 -SkipBuild" -ForegroundColor DarkGray
Write-Host "========================================" -ForegroundColor Cyan
