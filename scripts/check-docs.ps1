param(
    [switch]$VerboseList,
    [switch]$RefreshFacts,
    [switch]$SkipCatalog
)

$ErrorActionPreference = "Stop"

$projectRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$manifestPath = Join-Path $projectRoot "docs/docsync_manifest.json"
$requiredKeys = @("owner", "updated", "scope", "audience", "doc_type")

function Test-HasFrontmatter {
    param(
        [string]$FilePath,
        [string[]]$RequiredKeys
    )

    $issues = @()
    $content = [System.IO.File]::ReadAllText($FilePath, [System.Text.Encoding]::UTF8)
    $text = $content.TrimStart()

    if (-not $text.StartsWith("---")) {
        return @("[MISSING_FRONTMATTER] $FilePath")
    }

    $lines = $content -split "`r?`n"
    $start = -1
    $end = -1

    for ($i = 0; $i -lt $lines.Length; $i++) {
        if ($lines[$i].Trim() -eq "---") {
            if ($start -eq -1) {
                $start = $i
            } else {
                $end = $i
                break
            }
        }
    }

    if ($start -eq -1 -or $end -eq -1 -or $end -le $start + 1) {
        return @("[INVALID_FRONTMATTER] $FilePath")
    }

    $fm = ($lines[($start + 1)..($end - 1)] -join "`n")
    foreach ($key in $RequiredKeys) {
        if ($fm -notmatch "(?m)^$key\s*:\s*.+$") {
            $issues += "[MISSING_KEY:$key] $FilePath"
        }
    }

    if ($fm -match "(?m)^scope\s*:\s*(.+)$") {
        $scope = $Matches[1].Trim()
        if ($scope -ne "mall-v3") {
            $issues += "[INVALID_SCOPE:$scope] $FilePath"
        }
    }

    return $issues
}

function Get-ControllerFacts {
    param([string]$ControllerDir)

    $files = Get-ChildItem -LiteralPath $ControllerDir -Filter "*Controller.java" -File -ErrorAction SilentlyContinue
    $endpointCount = 0

    foreach ($file in $files) {
        $lines = Get-Content -LiteralPath $file.FullName
        for ($i = 0; $i -lt $lines.Count; $i++) {
            $line = $lines[$i].Trim()
            if ($line -notmatch '^@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping|RequestMapping)\b') {
                continue
            }

            $j = $i + 1
            while ($j -lt $lines.Count) {
                $next = $lines[$j].Trim()
                if (
                    $next -eq "" -or
                    $next.StartsWith("//") -or
                    $next.StartsWith("/*") -or
                    $next.StartsWith("*") -or
                    $next.StartsWith("@")
                ) {
                    $j++
                    continue
                }
                break
            }

            if ($j -lt $lines.Count -and $lines[$j].Trim() -match '^public\s+class\s+\w+Controller\b') {
                continue
            }

            $endpointCount++
        }
    }

    return [ordered]@{
        controller_count = $files.Count
        endpoint_count_estimate = $endpointCount
    }
}

function Get-CurrentFacts {
    param([string]$Root)

    $appControllerDir = Join-Path $Root "backend/mall-app-api/src/main/java/com/mall/app/controller"
    $adminControllerDir = Join-Path $Root "backend/mall-admin-api/src/main/java/com/mall/admin/controller"

    $appStats = Get-ControllerFacts -ControllerDir $appControllerDir
    $adminStats = Get-ControllerFacts -ControllerDir $adminControllerDir

    $facts = [ordered]@{
        schema_version = 1
        generated_at = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ssK")
        data = [ordered]@{
            migration_files = @(
                Get-ChildItem -LiteralPath (Join-Path $Root "data/migration") -Filter "*.sql" -File |
                    Sort-Object Name |
                    ForEach-Object { $_.Name }
            )
            seed_files = @(
                Get-ChildItem -LiteralPath (Join-Path $Root "data/seed") -Filter "*.sql" -File |
                    Sort-Object Name |
                    ForEach-Object { $_.Name }
            )
        }
        backend = [ordered]@{
            app_controller_count = $appStats.controller_count
            app_endpoint_count_estimate = $appStats.endpoint_count_estimate
            admin_controller_count = $adminStats.controller_count
            admin_endpoint_count_estimate = $adminStats.endpoint_count_estimate
        }
        frontend = [ordered]@{
            app_view_count = (
                Get-ChildItem -LiteralPath (Join-Path $Root "frontend/apps/mall-app-web/src/views") -Filter "*.vue" -File |
                    Measure-Object |
                    Select-Object -ExpandProperty Count
            )
            admin_view_count = (
                Get-ChildItem -LiteralPath (Join-Path $Root "frontend/apps/mall-admin-web/src/views") -Filter "*.vue" -File -Recurse |
                    Measure-Object |
                    Select-Object -ExpandProperty Count
            )
            app_main_files = @(
                Get-ChildItem -LiteralPath (Join-Path $Root "frontend/apps/mall-app-web/src") -Filter "main.*" -File |
                    Sort-Object Name |
                    ForEach-Object { $_.Name }
            )
            admin_main_files = @(
                Get-ChildItem -LiteralPath (Join-Path $Root "frontend/apps/mall-admin-web/src") -Filter "main.*" -File |
                    Sort-Object Name |
                    ForEach-Object { $_.Name }
            )
            app_router_files = @(
                Get-ChildItem -LiteralPath (Join-Path $Root "frontend/apps/mall-app-web/src/router") -Filter "index.*" -File |
                    Sort-Object Name |
                    ForEach-Object { $_.Name }
            )
            admin_router_files = @(
                Get-ChildItem -LiteralPath (Join-Path $Root "frontend/apps/mall-admin-web/src/router") -Filter "index.*" -File |
                    Sort-Object Name |
                    ForEach-Object { $_.Name }
            )
            admin_vite_config_files = @(
                Get-ChildItem -LiteralPath (Join-Path $Root "frontend/apps/mall-admin-web") -Filter "vite.config.*" -File |
                    Sort-Object Name |
                    ForEach-Object { $_.Name }
            )
        }
        scripts = [ordered]@{
            script_files = @(
                Get-ChildItem -LiteralPath (Join-Path $Root "scripts") -Filter "*.ps1" -File |
                    Sort-Object Name |
                    ForEach-Object { $_.Name }
            )
            script_count = (
                Get-ChildItem -LiteralPath (Join-Path $Root "scripts") -Filter "*.ps1" -File |
                    Measure-Object |
                    Select-Object -ExpandProperty Count
            )
        }
    }

    return $facts
}

function Get-ComparableFacts {
    param($Facts)

    $clone = ConvertFrom-Json (($Facts | ConvertTo-Json -Depth 20)) -AsHashtable
    $null = $clone.Remove("generated_at")
    return $clone
}

function Write-FactsReport {
    param(
        $Facts,
        [string]$ReportPath
    )

    $lines = @(
        "---",
        "owner: docs",
        "updated: $(Get-Date -Format 'yyyy-MM-dd')",
        "scope: mall-v3",
        "audience: dev,qa,ops",
        "doc_type: analysis",
        "---",
        "",
        "# 文档联动事实快照（自动生成）",
        "",
        "> 由 `scripts/check-docs.ps1 -RefreshFacts` 自动生成，请勿手工修改。",
        "",
        "## 1. 数据基线",
        "",
        "| 指标 | 当前值 |",
        "|---|---|",
        "| migration 文件 | $($Facts.data.migration_files -join ', ') |",
        "| seed 文件 | $($Facts.data.seed_files -join ', ') |",
        "",
        "## 2. 后端基线",
        "",
        "| 指标 | 当前值 |",
        "|---|---:|",
        "| App Controller 数 | $($Facts.backend.app_controller_count) |",
        "| App 端点估算数 | $($Facts.backend.app_endpoint_count_estimate) |",
        "| Admin Controller 数 | $($Facts.backend.admin_controller_count) |",
        "| Admin 端点估算数 | $($Facts.backend.admin_endpoint_count_estimate) |",
        "",
        "## 3. 前端基线",
        "",
        "| 指标 | 当前值 |",
        "|---|---|",
        "| App 视图数 | $($Facts.frontend.app_view_count) |",
        "| Admin 视图数 | $($Facts.frontend.admin_view_count) |",
        "| App 入口文件 | $($Facts.frontend.app_main_files -join ', ') |",
        "| Admin 入口文件 | $($Facts.frontend.admin_main_files -join ', ') |",
        "| App 路由文件 | $($Facts.frontend.app_router_files -join ', ') |",
        "| Admin 路由文件 | $($Facts.frontend.admin_router_files -join ', ') |",
        "| Admin Vite 配置 | $($Facts.frontend.admin_vite_config_files -join ', ') |",
        "",
        "## 4. 脚本基线",
        "",
        "| 指标 | 当前值 |",
        "|---|---|",
        "| 脚本数量 | $($Facts.scripts.script_count) |",
        "| 脚本清单 | $($Facts.scripts.script_files -join ', ') |",
        "",
        "## 5. 生成时间",
        "",
        "- $($Facts.generated_at)"
    )

    Set-Content -LiteralPath $ReportPath -Value ($lines -join "`r`n") -Encoding UTF8
}

if (Test-Path -LiteralPath $manifestPath) {
    $manifest = Get-Content -LiteralPath $manifestPath -Raw | ConvertFrom-Json -AsHashtable
} else {
    throw "缺少文档联动清单：$manifestPath"
}

$excludePatterns = @()
if ($manifest.ContainsKey("excludeMarkdownPatterns")) {
    $excludePatterns += $manifest.excludeMarkdownPatterns
}

$mdFiles = Get-ChildItem -Path $projectRoot -Recurse -File -Filter "*.md" |
    Where-Object {
        $full = $_.FullName
        foreach ($pattern in $excludePatterns) {
            if ($full -match $pattern) {
                return $false
            }
        }
        return $true
    }

$issues = @()
foreach ($file in $mdFiles) {
    $issues += Test-HasFrontmatter -FilePath $file.FullName -RequiredKeys $requiredKeys
}

$facts = Get-CurrentFacts -Root $projectRoot
$factsSnapshot = Join-Path $projectRoot $manifest.factsSnapshot
$factsReport = Join-Path $projectRoot $manifest.factsReport

if ($RefreshFacts) {
    $factsOutputDir = Split-Path -Parent $factsSnapshot
    if (-not (Test-Path -LiteralPath $factsOutputDir)) {
        New-Item -Path $factsOutputDir -ItemType Directory -Force | Out-Null
    }
    $facts | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $factsSnapshot -Encoding UTF8
    Write-FactsReport -Facts $facts -ReportPath $factsReport
}

if (-not (Test-Path -LiteralPath $factsSnapshot)) {
    $issues += "[MISSING_FACTS_SNAPSHOT] $factsSnapshot"
    $issues += "[MISSING_FACTS_SNAPSHOT_HINT] 先运行 .\scripts\check-docs.ps1 -RefreshFacts 生成快照"
} else {
    $snapshot = Get-Content -LiteralPath $factsSnapshot -Raw | ConvertFrom-Json -AsHashtable
    $currentComparable = Get-ComparableFacts -Facts $facts
    $snapshotComparable = Get-ComparableFacts -Facts $snapshot

    $currentComparableJson = $currentComparable | ConvertTo-Json -Depth 20 -Compress
    $snapshotComparableJson = $snapshotComparable | ConvertTo-Json -Depth 20 -Compress

    if ($currentComparableJson -ne $snapshotComparableJson) {
        $issues += "[FACTS_DRIFT] 当前代码事实与文档快照不一致：$factsSnapshot"
        $issues += "[FACTS_DRIFT_HINT] 若为预期变更，请运行 .\scripts\check-docs.ps1 -RefreshFacts 后同步相关文档"

        foreach ($section in @("data", "backend", "frontend", "scripts")) {
            $currentSection = $currentComparable[$section] | ConvertTo-Json -Depth 20 -Compress
            $snapshotSection = $snapshotComparable[$section] | ConvertTo-Json -Depth 20 -Compress
            if ($currentSection -ne $snapshotSection) {
                $docsToSync = @()
                if ($manifest.factSectionDocs.ContainsKey($section)) {
                    $docsToSync = $manifest.factSectionDocs[$section]
                }
                if ($docsToSync.Count -gt 0) {
                    $issues += "[FACTS_DRIFT_SECTION:$section] 建议同步文档: $($docsToSync -join ', ')"
                } else {
                    $issues += "[FACTS_DRIFT_SECTION:$section] 请同步该领域文档"
                }
            }
        }
    }
}

if (-not $SkipCatalog) {
    $catalogScript = Join-Path $PSScriptRoot "doc-catalog.ps1"
    if (-not (Test-Path -LiteralPath $catalogScript)) {
        $issues += "[MISSING_MARKDOWN_CATALOG_SCRIPT] $catalogScript"
    } else {
        if ($RefreshFacts) {
            & $catalogScript -Refresh
        } else {
            & $catalogScript -CheckOnly
        }
        if ($LASTEXITCODE -ne 0) {
            if ($RefreshFacts) {
                $issues += "[MARKDOWN_CATALOG_REFRESH_FAILED] 刷新失败：$catalogScript"
            } else {
                $issues += "[MARKDOWN_CATALOG_DRIFT] Markdown 清单与当前事实不一致，运行 .\scripts\doc-catalog.ps1 -Refresh"
            }
        }
    }
}

if ($issues.Count -gt 0) {
    Write-Host "文档检查失败，发现以下问题：" -ForegroundColor Red
    $issues | Sort-Object -Unique | ForEach-Object { Write-Host " - $_" -ForegroundColor Red }
    exit 1
}

Write-Host "文档检查通过：共验证 $($mdFiles.Count) 个 Markdown 文件，并完成事实快照校验。" -ForegroundColor Green
if ($RefreshFacts) {
    Write-Host "已刷新文档事实快照：" -ForegroundColor Green
    Write-Host " - $factsSnapshot"
    Write-Host " - $factsReport"
}
if ($VerboseList) {
    $mdFiles | ForEach-Object { Write-Host " - $($_.FullName)" }
}
