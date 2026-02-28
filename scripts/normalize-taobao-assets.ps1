# Mall V3 - 淘宝素材标准化
# 用法:
#   .\normalize-taobao-assets.ps1
#   .\normalize-taobao-assets.ps1 -DryRun

param(
    [switch]$DryRun
)

$ErrorActionPreference = "Stop"
$ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $ROOT
$SCRIPT = Join-Path $PROJECT_ROOT "tools/scripts/normalize-taobao-assets.py"

if (-not (Test-Path -LiteralPath $SCRIPT)) {
    throw "Script not found: $SCRIPT"
}

$argsList = @($SCRIPT)
if ($DryRun) {
    $argsList += "--dry-run"
}

Write-Host "[normalize-taobao-assets] python $($argsList -join ' ')" -ForegroundColor Cyan
python @argsList
if ($LASTEXITCODE -ne 0) {
    throw "normalize-taobao-assets failed with exit code $LASTEXITCODE"
}

Write-Host "[normalize-taobao-assets] completed" -ForegroundColor Green

