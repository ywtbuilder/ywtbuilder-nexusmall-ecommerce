# Mall V3 - 淘宝目录全量导入并替换商品域
# 用法:
#   .\import-taobao-catalog.ps1
#   .\import-taobao-catalog.ps1 -DryRun

param(
    [switch]$DryRun,
    [int]$Limit = 0
)

$ErrorActionPreference = "Stop"
$ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $ROOT
$SCRIPT = Join-Path $PROJECT_ROOT "tools/scripts/import-taobao-catalog-to-db.py"

if (-not (Test-Path -LiteralPath $SCRIPT)) {
    throw "Script not found: $SCRIPT"
}

$argsList = @($SCRIPT)
if ($DryRun) {
    $argsList += "--dry-run"
}
if ($Limit -gt 0) {
    $argsList += @("--limit", "$Limit")
}

Write-Host "[import-taobao-catalog] python $($argsList -join ' ')" -ForegroundColor Cyan
python @argsList
if ($LASTEXITCODE -ne 0) {
    throw "import-taobao-catalog failed with exit code $LASTEXITCODE"
}

Write-Host "[import-taobao-catalog] completed" -ForegroundColor Green

