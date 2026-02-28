<#
.SYNOPSIS
  Diagnose localhost:8091 home slow-loading symptoms.
.DESCRIPTION
  Collects lightweight runtime signals:
    - home page HTTP timing
    - /home/content and /product/search API timing + payload size
    - frontend log keyword hits
    - same-name config/router risk scan
  Generates JSON + Markdown report in runtime-logs.

  Usage:
    pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\diagnose-home-slow.ps1
#>

param(
    [string]$WebBase = "http://localhost:8091",
    [string]$ApiBase = "http://localhost:18080",
    [int]$LogTailLines = 200,
    [string]$ReportPath = "./runtime-logs/home-slow-diagnosis.json",
    [string]$MarkdownPath = "./runtime-logs/home-slow-diagnosis.md"
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

function Invoke-TimedWebRequest([string]$url, [int]$timeoutSec = 12) {
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    try {
        $resp = Invoke-WebRequest -Uri $url -Method GET -UseBasicParsing -TimeoutSec $timeoutSec -ErrorAction Stop
        $sw.Stop()
        return [ordered]@{
            url = $url
            status_code = [int]$resp.StatusCode
            duration_ms = [int]$sw.ElapsedMilliseconds
            content_length = ($resp.Content ?? "").Length
            error = $null
        }
    } catch {
        $sw.Stop()
        $statusCode = 0
        if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
            $statusCode = [int]$_.Exception.Response.StatusCode.value__
        }
        return [ordered]@{
            url = $url
            status_code = $statusCode
            duration_ms = [int]$sw.ElapsedMilliseconds
            content_length = 0
            error = $_.Exception.Message
        }
    }
}

function Get-LogKeywordHits([string]$logPath, [int]$tailLines) {
    if (-not (Test-Path -LiteralPath $logPath)) {
        return [ordered]@{
            exists = $false
            path = $logPath
            hits = @()
            scanned_lines = 0
        }
    }

    $lines = @(Get-Content -LiteralPath $logPath -Tail $tailLines -ErrorAction SilentlyContinue)
    $pattern = '(ERROR|Exception|timed out|Timeout|EADDRINUSE|ECONNREFUSED|Failed to fetch|Unhandled)'
    $hits = @()
    foreach ($line in $lines) {
        if ($line -match $pattern) {
            $hits += $line.Trim()
        }
    }

    return [ordered]@{
        exists = $true
        path = $logPath
        hits = @($hits | Select-Object -First 30)
        scanned_lines = $lines.Count
    }
}

function Build-Risk {
    param(
        [hashtable]$WebHome,
        [hashtable]$HomeApi,
        [hashtable]$SearchApi,
        [hashtable]$LogScan,
        [bool]$RouterDuplicate,
        [bool]$ViteDuplicate
    )

    $score = 0
    $factors = @()

    if ($WebHome.status_code -ne 200) {
        $score += 3
        $factors += "web_home_status_not_200"
    }
    if ($WebHome.duration_ms -gt 5000) {
        $score += 3
        $factors += "web_home_duration_gt_5000ms"
    } elseif ($WebHome.duration_ms -gt 2500) {
        $score += 2
        $factors += "web_home_duration_gt_2500ms"
    } elseif ($WebHome.duration_ms -gt 1200) {
        $score += 1
        $factors += "web_home_duration_gt_1200ms"
    }

    if ($HomeApi.duration_ms -gt 3000) {
        $score += 2
        $factors += "home_api_duration_gt_3000ms"
    } elseif ($HomeApi.duration_ms -gt 1500) {
        $score += 1
        $factors += "home_api_duration_gt_1500ms"
    }

    if ($SearchApi.duration_ms -gt 3000) {
        $score += 2
        $factors += "search_api_duration_gt_3000ms"
    } elseif ($SearchApi.duration_ms -gt 1500) {
        $score += 1
        $factors += "search_api_duration_gt_1500ms"
    }

    if ($HomeApi.content_length -gt 250000) {
        $score += 1
        $factors += "home_api_payload_gt_250kb"
    }
    if ($SearchApi.content_length -gt 100000) {
        $score += 1
        $factors += "search_api_payload_gt_100kb"
    }

    if (($LogScan.hits | Measure-Object).Count -gt 0) {
        $score += 2
        $factors += "frontend_log_error_keywords_detected"
    }
    if ($RouterDuplicate) {
        $score += 2
        $factors += "router_index_js_ts_duplicate"
    }
    if ($ViteDuplicate) {
        $score += 2
        $factors += "vite_config_js_ts_duplicate"
    }

    $level = if ($score -ge 7) {
        "HIGH"
    } elseif ($score -ge 4) {
        "MEDIUM"
    } else {
        "LOW"
    }

    return [ordered]@{
        score = $score
        level = $level
        factors = $factors
    }
}

function Write-MarkdownReport {
    param(
        [string]$FilePath,
        [hashtable]$Payload
    )

    $lines = @(
        "# Home Slow Diagnosis Report",
        "",
        ("Generated at: {0}" -f $Payload.generated_at),
        "",
        "## Summary",
        "",
        "| Item | Value |",
        "|---|---|",
        ("| Risk level | {0} |" -f $Payload.risk.level),
        ("| Risk score | {0} |" -f $Payload.risk.score),
        ("| Web home ms | {0} |" -f $Payload.metrics.web_home.duration_ms),
        ("| Home API ms | {0} |" -f $Payload.metrics.home_api.duration_ms),
        ("| Search API ms | {0} |" -f $Payload.metrics.search_api.duration_ms),
        ("| Home API bytes | {0} |" -f $Payload.metrics.home_api.content_length),
        ("| Search API bytes | {0} |" -f $Payload.metrics.search_api.content_length),
        "",
        "## Duplicate Risk",
        "",
        ("- router index.js + index.ts: {0}" -f $Payload.duplicate_risk.router_index_js_ts),
        ("- vite config.js + config.ts: {0}" -f $Payload.duplicate_risk.vite_config_js_ts),
        "",
        "## Risk Factors",
        ""
    )

    if (($Payload.risk.factors | Measure-Object).Count -eq 0) {
        $lines += "- none"
    } else {
        foreach ($factor in $Payload.risk.factors) {
            $lines += "- $factor"
        }
    }

    $lines += @(
        "",
        "## Frontend Log Keyword Hits",
        ""
    )

    if (-not $Payload.log_scan.exists) {
        $lines += "- log file not found"
    } elseif (($Payload.log_scan.hits | Measure-Object).Count -eq 0) {
        $lines += "- no keyword hit in scanned tail"
    } else {
        $lines += '```text'
        foreach ($hit in $Payload.log_scan.hits) {
            $lines += $hit
        }
        $lines += '```'
    }

    Set-Content -LiteralPath $FilePath -Value ($lines -join "`r`n") -Encoding UTF8
}

Ensure-Directory $RUNTIME_DIR
$reportFile = Resolve-PathInProject $ReportPath
$markdownFile = Resolve-PathInProject $MarkdownPath
Ensure-Directory (Split-Path -Parent $reportFile)
Ensure-Directory (Split-Path -Parent $markdownFile)

$webHome = Invoke-TimedWebRequest -url "$WebBase/"
$homeApi = Invoke-TimedWebRequest -url "$ApiBase/home/content"
$searchApi = Invoke-TimedWebRequest -url "$ApiBase/product/search?pageNum=1&pageSize=10"

$frontendLog = Join-Path $PROJECT_ROOT "runtime-logs/mall-app-web.log"
$logScan = Get-LogKeywordHits -logPath $frontendLog -tailLines $LogTailLines

$routerTs = Join-Path $PROJECT_ROOT "frontend/apps/mall-app-web/src/router/index.ts"
$routerJs = Join-Path $PROJECT_ROOT "frontend/apps/mall-app-web/src/router/index.js"
$viteTs = Join-Path $PROJECT_ROOT "frontend/apps/mall-app-web/vite.config.ts"
$viteJs = Join-Path $PROJECT_ROOT "frontend/apps/mall-app-web/vite.config.js"

$routerDup = (Test-Path -LiteralPath $routerTs) -and (Test-Path -LiteralPath $routerJs)
$viteDup = (Test-Path -LiteralPath $viteTs) -and (Test-Path -LiteralPath $viteJs)

$risk = Build-Risk -WebHome $webHome -HomeApi $homeApi -SearchApi $searchApi -LogScan $logScan -RouterDuplicate $routerDup -ViteDuplicate $viteDup

$payload = [ordered]@{
    generated_at = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ssK")
    parameters = [ordered]@{
        web_base = $WebBase
        api_base = $ApiBase
        log_tail_lines = $LogTailLines
        report_path = $reportFile
        markdown_path = $markdownFile
    }
    metrics = [ordered]@{
        web_home = $webHome
        home_api = $homeApi
        search_api = $searchApi
    }
    duplicate_risk = [ordered]@{
        router_index_js_ts = $routerDup
        vite_config_js_ts = $viteDup
        router_ts_path = $routerTs
        router_js_path = $routerJs
        vite_ts_path = $viteTs
        vite_js_path = $viteJs
    }
    log_scan = $logScan
    risk = $risk
}

Set-Content -LiteralPath $reportFile -Value ($payload | ConvertTo-Json -Depth 8) -Encoding UTF8
Write-MarkdownReport -FilePath $markdownFile -Payload $payload

Write-Host "===== home slow diagnosis =====" -ForegroundColor Cyan
Write-Host ("risk level: {0} (score={1})" -f $risk.level, $risk.score)
Write-Host ("web home: status={0}, ms={1}, bytes={2}" -f $webHome.status_code, $webHome.duration_ms, $webHome.content_length)
Write-Host ("home api: status={0}, ms={1}, bytes={2}" -f $homeApi.status_code, $homeApi.duration_ms, $homeApi.content_length)
Write-Host ("search api: status={0}, ms={1}, bytes={2}" -f $searchApi.status_code, $searchApi.duration_ms, $searchApi.content_length)
Write-Host ("router duplicate: {0}, vite duplicate: {1}" -f $routerDup, $viteDup)
Write-Host ("report: {0}" -f $reportFile)
Write-Host ("markdown: {0}" -f $markdownFile)

exit 0
