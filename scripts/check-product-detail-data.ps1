param(
    [string]$DbContainer = "mallv3-mysql",
    [string]$DbUser = "root",
    [string]$DbPassword = "root",
    [string]$DbName = "mall",
    [string]$ReportPath = "./runtime-logs/product-detail-data-check.json",
    [switch]$AutoFix,
    [switch]$FailOnViolation
)

$ErrorActionPreference = "Stop"
$ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $ROOT
$RUNTIME_DIR = Join-Path $PROJECT_ROOT "runtime-logs"

function Ensure-Directory([string]$path) {
    if (-not (Test-Path -LiteralPath $path)) {
        New-Item -ItemType Directory -Path $path -Force | Out-Null
    }
}

function Resolve-PathInProject([string]$path) {
    if ([System.IO.Path]::IsPathRooted($path)) {
        return [System.IO.Path]::GetFullPath($path)
    }
    return [System.IO.Path]::GetFullPath((Join-Path $PROJECT_ROOT $path))
}

function Invoke-ContainerSql([string]$sqlText) {
    Ensure-Directory $RUNTIME_DIR
    $stamp = Get-Date -Format "yyyyMMddHHmmssfff"
    $localSql = Join-Path $RUNTIME_DIR "tmp-product-detail-check-$stamp.sql"
    $containerSql = "/tmp/product-detail-check-$stamp.sql"

    Set-Content -LiteralPath $localSql -Value $sqlText -Encoding UTF8

    try {
        & docker cp $localSql "$DbContainer`:$containerSql" | Out-Null
        $output = & docker exec $DbContainer mysql "-u$DbUser" "-p$DbPassword" "--default-character-set=utf8mb4" -N -e "source $containerSql" $DbName 2>&1
        if ($LASTEXITCODE -ne 0) {
            $errorText = ($output | Out-String).Trim()
            throw "MySQL query failed: $errorText"
        }
        return @($output | ForEach-Object { "$_" } | Where-Object { $_ -and ($_ -notmatch '^mysql: \[Warning\]') })
    } finally {
        Remove-Item -LiteralPath $localSql -Force -ErrorAction SilentlyContinue
        & docker exec $DbContainer rm -f $containerSql | Out-Null
    }
}

function Parse-TabLine([string]$line, [int]$maxParts = 8) {
    if ([string]::IsNullOrWhiteSpace($line)) {
        return @()
    }
    return $line -split "`t", $maxParts
}

function To-ProductRows([object[]]$lines) {
    $rows = @()
    foreach ($line in $lines) {
        $parts = Parse-TabLine -line $line -maxParts 3
        if ($parts.Count -lt 3) {
            continue
        }
        $rows += [ordered]@{
            id = [int]$parts[0]
            sku = $parts[1]
            name = $parts[2]
        }
    }
    return $rows
}

function Invoke-AutoFix {
    $sql = @'
-- 缺详情图: 复制第一张简介图(type=2)为详情图(type=1)
INSERT INTO pms_product_image (product_id, asset_id, image_type, sort_order, created_at)
SELECT s.product_id, s.asset_id, 1, 0, NOW()
FROM (
    SELECT p.id AS product_id, MIN(pi.asset_id) AS asset_id
    FROM pms_product p
    JOIN pms_product_image pi ON pi.product_id = p.id AND pi.image_type = 2
    LEFT JOIN pms_product_image d ON d.product_id = p.id AND d.image_type = 1
    WHERE p.publish_status = 1 AND p.delete_status = 0 AND d.id IS NULL
    GROUP BY p.id
) s;

-- 缺简介图: 复制第一张详情图(type=1)为简介图(type=2)
INSERT INTO pms_product_image (product_id, asset_id, image_type, sort_order, created_at)
SELECT s.product_id, s.asset_id, 2, 0, NOW()
FROM (
    SELECT p.id AS product_id, MIN(pi.asset_id) AS asset_id
    FROM pms_product p
    JOIN pms_product_image pi ON pi.product_id = p.id AND pi.image_type = 1
    LEFT JOIN pms_product_image d ON d.product_id = p.id AND d.image_type = 2
    WHERE p.publish_status = 1 AND p.delete_status = 0 AND d.id IS NULL
    GROUP BY p.id
) s;

-- 重建缺失/异常 detail_html
SET SESSION group_concat_max_len = 1048576;
UPDATE pms_product p
JOIN (
    SELECT pi.product_id,
           CONCAT(
               '<div class="product-detail-images">',
               GROUP_CONCAT(
                   CONCAT('<img src="/api/asset/image/', a.image_hash,
                          '" loading="lazy" decoding="async" style="width:100%;display:block;margin:0 auto;" />')
                   ORDER BY pi.sort_order, pi.id
                   SEPARATOR ''
               ),
               '</div>'
           ) AS html
    FROM pms_product_image pi
    JOIN pms_asset a ON a.id = pi.asset_id
    WHERE pi.image_type = 1
    GROUP BY pi.product_id
) h ON h.product_id = p.id
SET p.detail_html = h.html,
    p.detail_mobile_html = h.html
WHERE p.publish_status = 1
  AND p.delete_status = 0
  AND (
      p.detail_html IS NULL
      OR p.detail_html = ''
      OR p.detail_html NOT LIKE '%/api/asset/image/%'
  );
'@
    Invoke-ContainerSql -sqlText $sql | Out-Null
}

Ensure-Directory $RUNTIME_DIR
$reportFile = Resolve-PathInProject $ReportPath
Ensure-Directory (Split-Path -Parent $reportFile)

if ($AutoFix) {
    Invoke-AutoFix
}

$summarySql = @'
SELECT
  (SELECT COUNT(*) FROM pms_product WHERE publish_status = 1 AND delete_status = 0) AS published_total,
  (SELECT COUNT(DISTINCT product_id) FROM pms_product_image WHERE image_type = 2) AS intro_covered,
  (SELECT COUNT(DISTINCT product_id) FROM pms_product_image WHERE image_type = 1) AS detail_covered,
  (SELECT COUNT(*) FROM pms_product_spec) AS spec_total;
'@
$summaryLine = (Invoke-ContainerSql -sqlText $summarySql | Select-Object -First 1)
$summaryParts = Parse-TabLine -line $summaryLine -maxParts 4
$publishedTotal = [int]($summaryParts[0] ?? 0)
$introCovered = [int]($summaryParts[1] ?? 0)
$detailCovered = [int]($summaryParts[2] ?? 0)
$specTotal = [int]($summaryParts[3] ?? 0)

$missingIntro = To-ProductRows (Invoke-ContainerSql -sqlText @'
SELECT p.id, p.product_sn, p.name
FROM pms_product p
LEFT JOIN (SELECT DISTINCT product_id FROM pms_product_image WHERE image_type = 2) x ON x.product_id = p.id
WHERE p.publish_status = 1 AND p.delete_status = 0 AND x.product_id IS NULL
ORDER BY p.id
LIMIT 200;
'@)

$missingDetail = To-ProductRows (Invoke-ContainerSql -sqlText @'
SELECT p.id, p.product_sn, p.name
FROM pms_product p
LEFT JOIN (SELECT DISTINCT product_id FROM pms_product_image WHERE image_type = 1) x ON x.product_id = p.id
WHERE p.publish_status = 1 AND p.delete_status = 0 AND x.product_id IS NULL
ORDER BY p.id
LIMIT 200;
'@)

$missingCoreSpec = @()
$coreSpecLines = Invoke-ContainerSql -sqlText @'
SELECT p.id, p.product_sn, p.name,
       SUM(CASE WHEN ps.spec_name = '品牌' THEN 1 ELSE 0 END) AS brand_cnt,
       SUM(CASE WHEN ps.spec_name = '型号' THEN 1 ELSE 0 END) AS model_cnt,
       SUM(CASE WHEN ps.spec_name = '商品编号' THEN 1 ELSE 0 END) AS sn_cnt,
       SUM(CASE WHEN ps.spec_name = '特色功能' THEN 1 ELSE 0 END) AS feature_cnt,
       SUM(CASE WHEN ps.spec_name = '包装清单' THEN 1 ELSE 0 END) AS pack_cnt
FROM pms_product p
LEFT JOIN pms_product_spec ps ON ps.product_id = p.id
WHERE p.publish_status = 1 AND p.delete_status = 0
GROUP BY p.id, p.product_sn, p.name
HAVING brand_cnt = 0 OR model_cnt = 0 OR sn_cnt = 0 OR feature_cnt = 0 OR pack_cnt = 0
ORDER BY p.id
LIMIT 200;
'@
foreach ($line in $coreSpecLines) {
    $parts = Parse-TabLine -line $line -maxParts 8
    if ($parts.Count -lt 8) { continue }
    $missing = @()
    if ([int]$parts[3] -eq 0) { $missing += '品牌' }
    if ([int]$parts[4] -eq 0) { $missing += '型号' }
    if ([int]$parts[5] -eq 0) { $missing += '商品编号' }
    if ([int]$parts[6] -eq 0) { $missing += '特色功能' }
    if ([int]$parts[7] -eq 0) { $missing += '包装清单' }
    $missingCoreSpec += [ordered]@{
        id = [int]$parts[0]
        sku = $parts[1]
        name = $parts[2]
        missing_fields = $missing
    }
}

$tinyDetailFirst = @()
$tinyLines = Invoke-ContainerSql -sqlText @'
SELECT p.id, p.product_sn, p.name, a.image_hash, a.width, a.height, a.file_size
FROM pms_product p
JOIN pms_product_image pi ON pi.product_id = p.id AND pi.image_type = 1 AND pi.sort_order = 0
JOIN pms_asset a ON a.id = pi.asset_id
WHERE p.publish_status = 1
  AND p.delete_status = 0
  AND (a.width <= 120 OR a.height <= 120 OR a.file_size < 5000)
ORDER BY p.id
LIMIT 200;
'@
foreach ($line in $tinyLines) {
    $parts = Parse-TabLine -line $line -maxParts 7
    if ($parts.Count -lt 7) { continue }
    $tinyDetailFirst += [ordered]@{
        id = [int]$parts[0]
        sku = $parts[1]
        name = $parts[2]
        hash = $parts[3]
        width = [int]$parts[4]
        height = [int]$parts[5]
        file_size = [int]$parts[6]
    }
}

$invalidPic = To-ProductRows (Invoke-ContainerSql -sqlText @'
SELECT id, product_sn, name
FROM pms_product
WHERE publish_status = 1
  AND delete_status = 0
  AND (pic IS NULL OR pic = '' OR pic NOT LIKE '/api/asset/image/%')
ORDER BY id
LIMIT 200;
'@)

$topFirstHash = @()
$firstHashLines = Invoke-ContainerSql -sqlText @'
SELECT first_hash, COUNT(*) AS cnt
FROM (
    SELECT REGEXP_SUBSTR(detail_html, '/api/asset/image/[0-9a-f]{64}') AS first_hash
    FROM pms_product
    WHERE publish_status = 1 AND delete_status = 0
) t
WHERE first_hash IS NOT NULL AND first_hash <> ''
GROUP BY first_hash
ORDER BY cnt DESC
LIMIT 5;
'@
foreach ($line in $firstHashLines) {
    $parts = Parse-TabLine -line $line -maxParts 2
    if ($parts.Count -lt 2) { continue }
    $topFirstHash += [ordered]@{
        hash = $parts[0]
        count = [int]$parts[1]
    }
}

$violations = @()
if ($missingIntro.Count -gt 0) {
    $violations += [ordered]@{ name = "missing_intro_images"; count = $missingIntro.Count; sample = @($missingIntro | Select-Object -First 20) }
}
if ($missingDetail.Count -gt 0) {
    $violations += [ordered]@{ name = "missing_detail_images"; count = $missingDetail.Count; sample = @($missingDetail | Select-Object -First 20) }
}
if ($missingCoreSpec.Count -gt 0) {
    $violations += [ordered]@{ name = "missing_core_spec_fields"; count = $missingCoreSpec.Count; sample = @($missingCoreSpec | Select-Object -First 20) }
}
if ($tinyDetailFirst.Count -gt 0) {
    $violations += [ordered]@{ name = "tiny_detail_first_image"; count = $tinyDetailFirst.Count; sample = @($tinyDetailFirst | Select-Object -First 20) }
}
if ($invalidPic.Count -gt 0) {
    $violations += [ordered]@{ name = "invalid_product_pic_path"; count = $invalidPic.Count; sample = @($invalidPic | Select-Object -First 20) }
}
if ($topFirstHash.Count -gt 0 -and [int]$topFirstHash[0].count -ge 10) {
    $violations += [ordered]@{ name = "detail_first_hash_reused"; count = [int]$topFirstHash[0].count; sample = @($topFirstHash) }
}

$report = [ordered]@{
    generated_at = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ssK")
    parameters = [ordered]@{
        db_container = $DbContainer
        db_name = $DbName
        report_path = $reportFile
        auto_fix = [bool]$AutoFix
        fail_on_violation = [bool]$FailOnViolation
    }
    summary = [ordered]@{
        published_total = $publishedTotal
        intro_covered = $introCovered
        detail_covered = $detailCovered
        spec_total = $specTotal
        missing_intro = $missingIntro.Count
        missing_detail = $missingDetail.Count
        missing_core_spec = $missingCoreSpec.Count
        tiny_detail_first = $tinyDetailFirst.Count
        invalid_pic_path = $invalidPic.Count
        max_reused_first_hash = if ($topFirstHash.Count -gt 0) { [int]$topFirstHash[0].count } else { 0 }
    }
    violation_count = $violations.Count
    violations = $violations
}

$reportJson = $report | ConvertTo-Json -Depth 8
Set-Content -LiteralPath $reportFile -Value $reportJson -Encoding UTF8

Write-Host "===== product detail data check =====" -ForegroundColor Cyan
Write-Host ("published products: {0}" -f $publishedTotal)
Write-Host ("intro coverage: {0}/{1}" -f $introCovered, $publishedTotal)
Write-Host ("detail coverage: {0}/{1}" -f $detailCovered, $publishedTotal)
Write-Host ("missing intro: {0}" -f $missingIntro.Count)
Write-Host ("missing detail: {0}" -f $missingDetail.Count)
Write-Host ("missing core spec: {0}" -f $missingCoreSpec.Count)
Write-Host ("tiny detail first image: {0}" -f $tinyDetailFirst.Count)
Write-Host ("invalid pic path: {0}" -f $invalidPic.Count)
if ($topFirstHash.Count -gt 0) {
    Write-Host ("max reused first hash count: {0}" -f $topFirstHash[0].count)
}
Write-Host ("report: {0}" -f $reportFile)

if ($violations.Count -gt 0) {
    Write-Host ("violations: {0}" -f $violations.Count) -ForegroundColor Yellow
    foreach ($item in $violations) {
        Write-Host (" - {0}: {1}" -f $item.name, $item.count) -ForegroundColor Yellow
    }
    if ($FailOnViolation) {
        exit 1
    }
} else {
    Write-Host "violations: 0" -ForegroundColor Green
}

exit 0
