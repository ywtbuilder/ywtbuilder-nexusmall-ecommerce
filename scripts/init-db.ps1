# Mall V3 — Database Initialization Script
# Usage: .\init-db.ps1 [-SeedOnly] [-Reset] [-SeedProfile minimal|full]
# Options:
#   -SeedOnly    Only run seed scripts (skip migration)
#   -Reset       Drop and recreate the database before importing
#   -SeedProfile Seed import profile:
#                minimal = only V100 + V101 (default, GitHub showcase baseline)
#                full    = all files under data/seed/*.sql
#
# Prerequisites: Docker container 'mallv3-mysql' must be running.

param(
    [switch]$SeedOnly,
    [switch]$Reset,
    [ValidateSet("minimal", "full")]
    [string]$SeedProfile = "minimal"
)

$ErrorActionPreference = "Stop"
$ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $ROOT
$DATA_DIR = Join-Path $PROJECT_ROOT "data"
$MIGRATION_DIR = Join-Path $DATA_DIR "migration"
$SEED_DIR = Join-Path $DATA_DIR "seed"

$MYSQL_CONTAINER = "mallv3-mysql"
$MYSQL_USER = "root"
$MYSQL_PASS = "root"
$MYSQL_DB = "mall"

$MINIMAL_SEED_FILES = @(
    "V100__seed_admin_and_base_data.sql",
    "V101__seed_sample_products.sql"
)

Write-Host "===== Mall V3 数据库初始化 =====" -ForegroundColor Green
Write-Host "SeedProfile: $SeedProfile" -ForegroundColor DarkGray

# Check MySQL container is running
Write-Host "正在检查 MySQL 容器..." -ForegroundColor Yellow
$containerState = docker inspect -f '{{.State.Running}}' $MYSQL_CONTAINER 2>$null
if ($containerState -ne "true") {
    Write-Host "错误：容器 '$MYSQL_CONTAINER' 未运行。" -ForegroundColor Red
    Write-Host "请先运行 'docker compose -f infra/docker-compose.local.yml up -d'。" -ForegroundColor Yellow
    exit 1
}

# Wait for MySQL readiness
Write-Host "等待 MySQL 就绪..." -ForegroundColor Yellow
$ready = $false
for ($i = 0; $i -lt 20; $i++) {
    docker exec $MYSQL_CONTAINER mysqladmin ping -h localhost --silent 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) { $ready = $true; break }
    Start-Sleep -Seconds 2
}
if (-not $ready) {
    Write-Host "错误：MySQL 40 秒内未就绪。" -ForegroundColor Red
    exit 1
}
Write-Host "  MySQL 已就绪。" -ForegroundColor Green

# Helper: Execute SQL file in container
function Invoke-SqlFile {
    param([string]$FilePath, [string]$Label)
    $fileName = Split-Path -Leaf $FilePath
    Write-Host "  正在执行 $Label ($fileName)..." -NoNewline -ForegroundColor Cyan
    $content = Get-Content $FilePath -Raw -Encoding UTF8
    $content | docker exec -i $MYSQL_CONTAINER mysql -u$MYSQL_USER -p$MYSQL_PASS $MYSQL_DB 2>$null
    if ($LASTEXITCODE -eq 0) { Write-Host " 成功" -ForegroundColor Green }
    else { Write-Host " 失败" -ForegroundColor Red; return $false }
    return $true
}

# Reset database if requested
if ($Reset) {
    Write-Host "`n正在重置数据库 '$MYSQL_DB'..." -ForegroundColor Red
    $resetSql = "DROP DATABASE IF EXISTS $MYSQL_DB; CREATE DATABASE $MYSQL_DB CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;"
    $resetSql | docker exec -i $MYSQL_CONTAINER mysql -u$MYSQL_USER -p$MYSQL_PASS 2>$null
    if ($LASTEXITCODE -ne 0) {
        Write-Host "错误：重置数据库失败。" -ForegroundColor Red
        exit 1
    }
    Write-Host "  数据库重置完成。" -ForegroundColor Green
}

# Run migration scripts
if (-not $SeedOnly) {
    Write-Host "`n── 迁移脚本 ──" -ForegroundColor Yellow
    if (Test-Path $MIGRATION_DIR) {
        $migrationFiles = Get-ChildItem -Path $MIGRATION_DIR -Filter "*.sql" | Sort-Object Name
        if ($migrationFiles.Count -eq 0) {
            Write-Host "  未找到迁移文件。" -ForegroundColor DarkGray
        }
        foreach ($f in $migrationFiles) {
            $result = Invoke-SqlFile -FilePath $f.FullName -Label "migration"
            if (-not $result) {
                Write-Host "  迁移因错误中止。" -ForegroundColor Red
                exit 1
            }
        }
    } else {
        Write-Host "  迁移目录不存在：$MIGRATION_DIR" -ForegroundColor Yellow
    }
}

# Run seed scripts
Write-Host "`n── 种子数据 ──" -ForegroundColor Yellow
if (Test-Path $SEED_DIR) {
    $seedFiles = @()
    if ($SeedProfile -eq "minimal") {
        foreach ($seedName in $MINIMAL_SEED_FILES) {
            $seedPath = Join-Path $SEED_DIR $seedName
            if (Test-Path -LiteralPath $seedPath) {
                $seedFiles += Get-Item -LiteralPath $seedPath
            } else {
                Write-Host "  警告：最小种子文件缺失：$seedName" -ForegroundColor Yellow
            }
        }
    } else {
        $seedFiles = Get-ChildItem -Path $SEED_DIR -Filter "*.sql" | Sort-Object Name
    }

    if ($seedFiles.Count -eq 0) {
        Write-Host "  未找到种子文件。" -ForegroundColor DarkGray
    }
    foreach ($f in $seedFiles) {
        $result = Invoke-SqlFile -FilePath $f.FullName -Label "seed"
        if (-not $result) {
            Write-Host "  警告：种子脚本失败，继续执行..." -ForegroundColor Yellow
        }
    }
} else {
    Write-Host "  种子目录不存在：$SEED_DIR" -ForegroundColor Yellow
}

# Summary
Write-Host "`n── 验证结果 ──" -ForegroundColor Yellow
$tableCount = docker exec $MYSQL_CONTAINER mysql -u$MYSQL_USER -p$MYSQL_PASS $MYSQL_DB -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$MYSQL_DB';" 2>$null
$productCount = docker exec $MYSQL_CONTAINER mysql -u$MYSQL_USER -p$MYSQL_PASS $MYSQL_DB -N -e "SELECT COUNT(*) FROM pms_product;" 2>$null
$adminCount = docker exec $MYSQL_CONTAINER mysql -u$MYSQL_USER -p$MYSQL_PASS $MYSQL_DB -N -e "SELECT COUNT(*) FROM ums_admin;" 2>$null

Write-Host "  数据表:   $($tableCount.Trim())" -ForegroundColor Cyan
Write-Host "  商品数:   $($productCount.Trim())" -ForegroundColor Cyan
Write-Host "  管理员数: $($adminCount.Trim())" -ForegroundColor Cyan

Write-Host "`n===== 数据库初始化完成 =====" -ForegroundColor Green
Write-Host "  连接命令: mysql -h127.0.0.1 -P13306 -uroot -proot mall" -ForegroundColor DarkGray
