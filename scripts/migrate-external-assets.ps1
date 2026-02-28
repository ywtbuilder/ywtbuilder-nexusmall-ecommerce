<#
.SYNOPSIS
  Migrate external media URLs to local MySQL assets (/api/asset/image/{hash}).
.DESCRIPTION
  Modes:
    - default: dry-run (no DB mutation)
    - -Apply: write changes
    - -RollbackBatch <batch_no>: restore changed fields by batch
#>
param(
    [string]$DbHost = "localhost",
    [int]$DbPort = 13306,
    [string]$DbUser = "root",
    [string]$DbPassword = "root",
    [string]$DbName = "mall",
    [string]$DbContainer = "mallv3-mysql",
    [string]$BatchNo = "",
    [int]$MaxWorkers = 6,
    [int]$ConnectTimeoutSec = 5,
    [int]$ReadTimeoutSec = 15,
    [int]$Retries = 2,
    [string]$Output = "runtime-logs/external-asset-migration-report.json",
    [string]$BaselineOutput = "runtime-logs/external-url-baseline.json",
    [switch]$Apply,
    [string]$RollbackBatch = ""
)

$ErrorActionPreference = "Stop"
$SCRIPT_ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $SCRIPT_ROOT
$RUNTIME_DIR = Join-Path $PROJECT_ROOT "runtime-logs"
if (-not (Test-Path -LiteralPath $RUNTIME_DIR)) {
    New-Item -ItemType Directory -Path $RUNTIME_DIR -Force | Out-Null
}

if (-not $BatchNo -or -not $BatchNo.Trim()) {
    $BatchNo = "EXT{0}" -f (Get-Date -Format "yyyyMMddHHmmss")
}

$toolPath = Join-Path $PROJECT_ROOT "tools/scripts/migrate-external-assets-to-mysql.py"
if (-not (Test-Path -LiteralPath $toolPath)) {
    throw "tool not found: $toolPath"
}

Push-Location $PROJECT_ROOT
try {
    if ($RollbackBatch -and $RollbackBatch.Trim()) {
        Write-Host "===== external asset rollback =====" -ForegroundColor Cyan
        Write-Host "batch: $RollbackBatch"
        $argsList = @(
            $toolPath,
            "--db-host", $DbHost,
            "--db-port", "$DbPort",
            "--db-user", $DbUser,
            "--db-password", $DbPassword,
            "--db-name", $DbName,
            "--output", $Output,
            "--rollback-batch", $RollbackBatch
        )
        & python @argsList
        if ($LASTEXITCODE -ne 0) {
            throw "rollback failed with exit code $LASTEXITCODE"
        }
        exit 0
    }

    if ($Apply) {
        $dumpPath = Join-Path $RUNTIME_DIR ("external-asset-baseline-{0}.sql" -f $BatchNo)
        Write-Host "===== baseline snapshot =====" -ForegroundColor Cyan
        Write-Host "dump: $dumpPath"
        $dumpTables = @(
            "pms_brand",
            "ums_member",
            "ums_admin",
            "sms_home_advertise",
            "pms_product",
            "pms_asset"
        )
        & docker exec $DbContainer mysqldump "-u$DbUser" "-p$DbPassword" $DbName @dumpTables | Out-File -LiteralPath $dumpPath -Encoding UTF8
        if ($LASTEXITCODE -ne 0) {
            throw "baseline mysqldump failed with exit code $LASTEXITCODE"
        }
    }

    Write-Host "===== external asset migration =====" -ForegroundColor Cyan
    Write-Host ("mode: {0}" -f ($(if ($Apply) { "apply" } else { "dry-run" })))
    Write-Host "batch: $BatchNo"

    $pyArgs = @(
        $toolPath,
        "--db-host", $DbHost,
        "--db-port", "$DbPort",
        "--db-user", $DbUser,
        "--db-password", $DbPassword,
        "--db-name", $DbName,
        "--batch-no", $BatchNo,
        "--max-workers", "$MaxWorkers",
        "--connect-timeout", "$ConnectTimeoutSec",
        "--read-timeout", "$ReadTimeoutSec",
        "--retries", "$Retries",
        "--output", $Output,
        "--baseline-output", $BaselineOutput
    )
    if ($Apply) {
        $pyArgs += "--apply"
    }

    & python @pyArgs
    if ($LASTEXITCODE -ne 0) {
        throw "migration failed with exit code $LASTEXITCODE"
    }
} finally {
    Pop-Location
}

