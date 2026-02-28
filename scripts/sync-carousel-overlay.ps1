#!/usr/bin/env pwsh
[CmdletBinding()]
param(
    [string]$DbHost = "localhost",
    [int]$DbPort = 13306,
    [string]$DbUser = "root",
    [string]$DbPassword = "root",
    [string]$DbName = "mall",
    [string]$Output = "runtime-logs/carousel-overlay-map.json",
    [switch]$SkipAdvertiseUpdate
)

$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectRoot = (Resolve-Path -LiteralPath (Join-Path $scriptDir "..")).Path
$pythonScript = Join-Path $projectRoot "tools/scripts/sync-carousel-overlay-assets.py"

if (-not (Test-Path -LiteralPath $pythonScript)) {
    throw "脚本不存在: $pythonScript"
}

$pythonCmd = Get-Command python -ErrorAction Stop

$args = @(
    $pythonScript,
    "--db-host", $DbHost,
    "--db-port", "$DbPort",
    "--db-user", $DbUser,
    "--db-password", $DbPassword,
    "--db-name", $DbName,
    "--output", $Output
)

if ($SkipAdvertiseUpdate) {
    $args += "--skip-advertise-update"
}

Push-Location -LiteralPath $projectRoot
try {
    & $pythonCmd.Path @args
    if ($LASTEXITCODE -ne 0) {
        throw "sync-carousel-overlay-assets.py 执行失败，退出码: $LASTEXITCODE"
    }
}
finally {
    Pop-Location
}
