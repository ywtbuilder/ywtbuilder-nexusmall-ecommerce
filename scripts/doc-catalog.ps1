param(
    [string]$WorkspaceRoot,
    [string]$SnapshotPath,
    [string]$ReportPath,
    [switch]$Refresh,
    [switch]$CheckOnly,
    [switch]$Watch,
    [int]$IntervalSec = 5
)

$ErrorActionPreference = "Stop"

$projectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
if (-not $WorkspaceRoot) {
    $WorkspaceRoot = (Resolve-Path (Join-Path $projectRoot "..")).Path
}
if (-not $SnapshotPath) {
    $SnapshotPath = Join-Path $projectRoot "docs/_generated/markdown_catalog.snapshot.json"
}
if (-not $ReportPath) {
    $ReportPath = Join-Path $projectRoot "docs/_generated/markdown_catalog.md"
}

$excludeGlobs = @(
    "**/node_modules/**",
    "**/.git/**",
    "**/.vite/**",
    "**/dist/**",
    "**/target/**",
    "**/coverage/**",
    "**/docs/_generated/markdown_catalog.md"
)
$excludeSegments = @(
    "/node_modules/",
    "/.git/",
    "/.vite/",
    "/dist/",
    "/target/",
    "/coverage/"
)

function Normalize-RelPath {
    param([string]$Path)
    $normalized = $Path -replace "\\", "/"
    if ($normalized.StartsWith("./")) {
        $normalized = $normalized.Substring(2)
    }
    return $normalized
}

function Get-DocCategory {
    param([string]$RelPath)

    switch -Regex ($RelPath) {
        '^project_mall_v3/docs/_generated/' { return "generated-docs" }
        '^project_mall_v3/docs/'            { return "global-docs" }
        '^project_mall_v3/assets/'          { return "assets-docs" }
        '^project_mall_v3/backend/'         { return "backend-docs" }
        '^project_mall_v3/frontend/'        { return "frontend-docs" }
        '^project_mall_v3/scripts/'         { return "scripts-docs" }
        '^project_mall_v3/data/'            { return "data-docs" }
        '^project_mall_v3/tests/'           { return "tests-docs" }
        '^project_mall_v3/tools/'           { return "tools-docs" }
        '^project_mall_v3/infra/'           { return "infra-docs" }
        '^project_mall_v3/.+'               { return "project-root-docs" }
        default                             { return "workspace-root-docs" }
    }
}

function Get-IsManagedDoc {
    param([string]$Category)

    return @(
        "workspace-root-docs",
        "project-root-docs",
        "global-docs",
        "backend-docs",
        "frontend-docs",
        "scripts-docs",
        "data-docs",
        "tests-docs",
        "tools-docs",
        "infra-docs"
    ) -contains $Category
}

function Get-MarkdownRelPaths {
    param(
        [string]$Root,
        [string[]]$ExcludeGlobs,
        [string[]]$ExcludeSegments
    )

    $rg = Get-Command rg -ErrorAction SilentlyContinue
    if ($null -ne $rg) {
        $args = @("--files", "-uu", "-g", "*.md")
        foreach ($glob in $ExcludeGlobs) {
            $args += @("-g", "!$glob")
        }
        $args += "."

        Push-Location $Root
        try {
            $raw = & rg @args
        } finally {
            Pop-Location
        }

        return @(
            $raw |
                Where-Object { $_ -and $_.Trim().Length -gt 0 } |
                ForEach-Object { Normalize-RelPath $_ } |
                Sort-Object -Unique
        )
    }

    $allFiles = Get-ChildItem -LiteralPath $Root -Recurse -File -Filter "*.md"
    $result = @()
    foreach ($file in $allFiles) {
        $rel = Normalize-RelPath ([System.IO.Path]::GetRelativePath($Root, $file.FullName))
        $fullRel = "/" + $rel + "/"

        $skip = $false
        foreach ($segment in $ExcludeSegments) {
            if ($fullRel.Contains($segment)) {
                $skip = $true
                break
            }
        }
        if (-not $skip) {
            $result += $rel
        }
    }
    return @($result | Sort-Object -Unique)
}

function Get-CatalogData {
    param(
        [string]$Root,
        [string[]]$ExcludeGlobs,
        [string[]]$ExcludeSegments
    )

    $rawRelPaths = Get-MarkdownRelPaths -Root $Root -ExcludeGlobs @() -ExcludeSegments @()
    $relPaths = Get-MarkdownRelPaths -Root $Root -ExcludeGlobs $ExcludeGlobs -ExcludeSegments $ExcludeSegments
    $entries = @()

    foreach ($relPath in $relPaths) {
        $relativeNative = $relPath -replace "/", [System.IO.Path]::DirectorySeparatorChar
        $absPath = Join-Path $Root $relativeNative
        if (-not (Test-Path -LiteralPath $absPath)) {
            continue
        }

        $info = Get-Item -LiteralPath $absPath
        $category = Get-DocCategory -RelPath $relPath
        $managed = Get-IsManagedDoc -Category $category

        $entries += [pscustomobject]@{
            rel_path = $relPath
            category = $category
            managed = $managed
            size_bytes = [int64]$info.Length
            last_write_utc = $info.LastWriteTimeUtc.ToString("yyyy-MM-ddTHH:mm:ssZ")
        }
    }

    $sortedEntries = @($entries | Sort-Object rel_path)
    $categoryCounts = @(
        $sortedEntries |
            Group-Object category |
            Sort-Object Name |
            ForEach-Object {
                [pscustomobject]@{
                    category = $_.Name
                    count = $_.Count
                }
            }
    )

    return [ordered]@{
        schema_version = 1
        generated_at = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ssK")
        workspace_root = ($Root -replace "\\", "/")
        exclude_globs = $ExcludeGlobs
        summary = [ordered]@{
            raw_total_files = $rawRelPaths.Count
            total_files = $sortedEntries.Count
            managed_files = ($sortedEntries | Where-Object { $_.managed }).Count
            unmanaged_files = ($sortedEntries | Where-Object { -not $_.managed }).Count
            category_counts = $categoryCounts
        }
        files = $sortedEntries
    }
}

function Get-ComparableCatalog {
    param($CatalogData)

    $clone = ConvertFrom-Json (($CatalogData | ConvertTo-Json -Depth 20)) -AsHashtable -DateKind String
    $null = $clone.Remove("generated_at")
    return $clone
}

function Ensure-ParentDirectory {
    param([string]$FilePath)

    $parent = Split-Path -Parent $FilePath
    if ($parent -and -not (Test-Path -LiteralPath $parent)) {
        New-Item -Path $parent -ItemType Directory -Force | Out-Null
    }
}

function Write-CatalogReport {
    param(
        $CatalogData,
        [string]$Path
    )

    $lines = @(
        "---",
        "owner: docs",
        "updated: $(Get-Date -Format 'yyyy-MM-dd')",
        "scope: mall-v3",
        "audience: dev,qa,ops",
        "doc_type: inventory",
        "---",
        "",
        "# Markdown 文档清单（自动生成）",
        "",
        '> 由 `scripts/doc-catalog.ps1` 生成。默认剔除 `node_modules/.git/.vite/dist/target/coverage`。',
        "",
        "## 1. 总览",
        "",
        "| 指标 | 当前值 |",
        "|---|---:|",
        "| 全量 Markdown 文件数（含依赖目录） | $($CatalogData.summary.raw_total_files) |",
        "| 全部 Markdown 文件数 | $($CatalogData.summary.total_files) |",
        "| 核心治理文档数 | $($CatalogData.summary.managed_files) |",
        "| 非治理文档数（资产/生成） | $($CatalogData.summary.unmanaged_files) |",
        "",
        "## 2. 分类统计",
        "",
        "| 分类 | 数量 |",
        "|---|---:|"
    )

    $sortedCategories = @(
        $CatalogData.summary.category_counts |
            Sort-Object -Property @(
                @{ Expression = "count"; Descending = $true },
                @{ Expression = "category"; Descending = $false }
            )
    )
    foreach ($item in $sortedCategories) {
        $lines += "| $($item.category) | $($item.count) |"
    }

    $lines += @(
        "",
        "## 3. 文件清单",
        "",
        "| 路径 | 分类 | 治理 | 大小(KB) | 最后修改(UTC) |",
        "|---|---|---|---:|---|"
    )

    foreach ($file in $CatalogData.files) {
        $sizeKb = [Math]::Round(([double]$file.size_bytes / 1KB), 2)
        $managedText = if ($file.managed) { "yes" } else { "no" }
        $lines += "| $($file.rel_path) | $($file.category) | $managedText | $sizeKb | $($file.last_write_utc) |"
    }

    Ensure-ParentDirectory -FilePath $Path
    Set-Content -LiteralPath $Path -Value ($lines -join "`r`n") -Encoding UTF8
}

function Save-CatalogArtifacts {
    param(
        $CatalogData,
        [string]$SnapshotFile,
        [string]$ReportFile
    )

    Ensure-ParentDirectory -FilePath $SnapshotFile
    $CatalogData | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $SnapshotFile -Encoding UTF8
    Write-CatalogReport -CatalogData $CatalogData -Path $ReportFile
}

function Test-CatalogDrift {
    param(
        $CurrentData,
        $SnapshotData
    )

    $currentComparable = Get-ComparableCatalog -CatalogData $CurrentData
    $snapshotComparable = Get-ComparableCatalog -CatalogData $SnapshotData
    $currentJson = $currentComparable | ConvertTo-Json -Depth 20 -Compress
    $snapshotJson = $snapshotComparable | ConvertTo-Json -Depth 20 -Compress
    return $currentJson -ne $snapshotJson
}

function Show-CatalogDiffHint {
    param(
        $CurrentData,
        $SnapshotData
    )

    Write-Host "Markdown 清单快照与当前事实不一致。" -ForegroundColor Red
    Write-Host " - 当前文件数: $($CurrentData.summary.total_files)" -ForegroundColor Red
    Write-Host " - 快照文件数: $($SnapshotData.summary.total_files)" -ForegroundColor Red

    $currentPaths = @($CurrentData.files | ForEach-Object { $_.rel_path })
    $snapshotPaths = @($SnapshotData.files | ForEach-Object { $_.rel_path })
    $diff = Compare-Object -ReferenceObject $snapshotPaths -DifferenceObject $currentPaths

    $added = @($diff | Where-Object { $_.SideIndicator -eq "=>" } | Select-Object -ExpandProperty InputObject)
    $removed = @($diff | Where-Object { $_.SideIndicator -eq "<=" } | Select-Object -ExpandProperty InputObject)

    if ($added.Count -gt 0) {
        Write-Host " - 新增文件:" -ForegroundColor Yellow
        $added | Select-Object -First 10 | ForEach-Object { Write-Host "   + $_" -ForegroundColor Yellow }
    }
    if ($removed.Count -gt 0) {
        Write-Host " - 移除文件:" -ForegroundColor Yellow
        $removed | Select-Object -First 10 | ForEach-Object { Write-Host "   - $_" -ForegroundColor Yellow }
    }

    Write-Host "请运行以下命令刷新清单：" -ForegroundColor Yellow
    Write-Host "pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\doc-catalog.ps1 -Refresh" -ForegroundColor Yellow
}

if ($IntervalSec -lt 1) {
    throw "IntervalSec 不能小于 1"
}

if ($Watch) {
    Write-Host "进入 Markdown 清单实时模式（$IntervalSec 秒轮询）..." -ForegroundColor Cyan
    Write-Host "监控根目录：$WorkspaceRoot" -ForegroundColor Cyan
    $lastComparableJson = ""

    while ($true) {
        $current = Get-CatalogData -Root $WorkspaceRoot -ExcludeGlobs $excludeGlobs -ExcludeSegments $excludeSegments
        $comparable = Get-ComparableCatalog -CatalogData $current
        $json = $comparable | ConvertTo-Json -Depth 20 -Compress

        if ($json -ne $lastComparableJson) {
            Save-CatalogArtifacts -CatalogData $current -SnapshotFile $SnapshotPath -ReportFile $ReportPath
            $lastComparableJson = $json
            Write-Host "[$(Get-Date -Format 'HH:mm:ss')] 已刷新：$($current.summary.total_files) 个 Markdown 文件" -ForegroundColor Green
        }

        Start-Sleep -Seconds $IntervalSec
    }
}

$currentCatalog = Get-CatalogData -Root $WorkspaceRoot -ExcludeGlobs $excludeGlobs -ExcludeSegments $excludeSegments

if ($Refresh) {
    Save-CatalogArtifacts -CatalogData $currentCatalog -SnapshotFile $SnapshotPath -ReportFile $ReportPath
    Write-Host "已刷新 Markdown 清单：" -ForegroundColor Green
    Write-Host " - $SnapshotPath"
    Write-Host " - $ReportPath"
    exit 0
}

if (-not (Test-Path -LiteralPath $SnapshotPath)) {
    Write-Host "缺少 Markdown 清单快照：$SnapshotPath" -ForegroundColor Red
    Write-Host "请先运行：pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\doc-catalog.ps1 -Refresh" -ForegroundColor Yellow
    exit 1
}

$snapshotCatalog = Get-Content -LiteralPath $SnapshotPath -Raw | ConvertFrom-Json -AsHashtable -DateKind String
$drift = Test-CatalogDrift -CurrentData $currentCatalog -SnapshotData $snapshotCatalog

if ($drift) {
    Show-CatalogDiffHint -CurrentData $currentCatalog -SnapshotData $snapshotCatalog
    exit 1
}

if ($CheckOnly -or (-not $Refresh)) {
    Write-Host "Markdown 清单检查通过：$($currentCatalog.summary.total_files) 个文件。" -ForegroundColor Green
}
