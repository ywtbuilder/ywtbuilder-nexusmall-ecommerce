<#
.SYNOPSIS
  Smoke test for Mall V3 app/admin APIs.
.DESCRIPTION
  Runs lightweight HTTP checks for key endpoints and validates image source fields.

  Usage:
    pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\scripts\smoke-api-v3.ps1
#>

param(
    [string]$AppBase = "http://localhost:18080",
    [string]$AdminBase = "http://localhost:18081",
    [string]$AdminUser = "admin",
    [string]$AdminPassword = "macro123",
    [string]$MemberUser = "test",
    [string]$MemberPassword = "test123456",
    [string]$ReportPath = "./runtime-logs/api-smoke-v3.json"
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

function Convert-JsonSafe([string]$text) {
    if ([string]::IsNullOrWhiteSpace($text)) {
        return $null
    }
    try {
        return $text | ConvertFrom-Json -Depth 20
    } catch {
        return $null
    }
}

function Invoke-JsonRequest {
    param(
        [ValidateSet("GET", "POST")]
        [string]$Method,
        [string]$Url,
        [hashtable]$Headers = @{},
        [object]$Body = $null,
        [int]$TimeoutSec = 10
    )

    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    $statusCode = 0
    $rawBody = ""
    $requestError = $null

    try {
        $params = @{
            Uri = $Url
            Method = $Method
            TimeoutSec = $TimeoutSec
            UseBasicParsing = $true
            ErrorAction = "Stop"
        }
        if ($Headers.Count -gt 0) {
            $params["Headers"] = $Headers
        }
        if ($Method -eq "POST" -and $null -ne $Body) {
            $params["ContentType"] = "application/json"
            $params["Body"] = ($Body | ConvertTo-Json -Depth 20 -Compress)
        }

        $resp = Invoke-WebRequest @params
        $statusCode = [int]$resp.StatusCode
        if ($resp.Content -is [byte[]]) {
            $rawBody = [System.Text.Encoding]::UTF8.GetString($resp.Content)
        } else {
            $rawBody = [string]$resp.Content
        }
    } catch {
        $requestError = $_.Exception.Message
        if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
            $statusCode = [int]$_.Exception.Response.StatusCode.value__
        }
        if ($_.ErrorDetails -and $_.ErrorDetails.Message) {
            $rawBody = [string]$_.ErrorDetails.Message
        }
    } finally {
        $sw.Stop()
    }

    $json = Convert-JsonSafe -text $rawBody
    return [ordered]@{
        method = $Method
        url = $Url
        status_code = $statusCode
        duration_ms = [int]$sw.ElapsedMilliseconds
        body_length = ($rawBody ?? "").Length
        error = $requestError
        json = $json
        raw = $rawBody
    }
}

function Get-PropertyValue {
    param(
        [object]$InputObject,
        [string[]]$Path
    )
    $cursor = $InputObject
    foreach ($part in $Path) {
        if ($null -eq $cursor) {
            return $null
        }
        $prop = $cursor.PSObject.Properties[$part]
        if ($null -eq $prop) {
            return $null
        }
        $cursor = $prop.Value
    }
    return $cursor
}

function Is-AllowedImageUrl([string]$value) {
    if ([string]::IsNullOrWhiteSpace($value)) {
        return $false
    }
    return $value -match '^/api/asset/image/[0-9a-fA-F]{64}$' -or $value -match '^data:image/'
}

function Add-Check {
    param(
        [ref]$Checks,
        [string]$Name,
        [bool]$Pass,
        [bool]$Critical,
        [string]$Message,
        [hashtable]$Request = @{}
    )
    $Checks.Value += [ordered]@{
        name = $Name
        pass = $Pass
        critical = $Critical
        message = $Message
        request = $Request
    }
}

Ensure-Directory $RUNTIME_DIR
$reportFile = Resolve-PathInProject $ReportPath
Ensure-Directory (Split-Path -Parent $reportFile)

$checks = @()
$context = [ordered]@{
    sampled_product_id = $null
    advertise_total = 0
    advertise_compliant = 0
    product_list_count = 0
}

$appHealth = Invoke-JsonRequest -Method GET -Url "$AppBase/actuator/health"
$appHealthStatus = [string](Get-PropertyValue -InputObject $appHealth.json -Path @("status"))
$appHealthPass = ($appHealth.status_code -eq 200 -and $appHealthStatus -eq "UP")
Add-Check -Checks ([ref]$checks) -Name "app.health" -Pass $appHealthPass -Critical $true `
    -Message ("status_code={0}, health={1}" -f $appHealth.status_code, $appHealthStatus) -Request $appHealth

$adminHealth = Invoke-JsonRequest -Method GET -Url "$AdminBase/actuator/health"
$adminHealthStatus = [string](Get-PropertyValue -InputObject $adminHealth.json -Path @("status"))
$adminHealthPass = ($adminHealth.status_code -eq 200 -and $adminHealthStatus -eq "UP")
Add-Check -Checks ([ref]$checks) -Name "admin.health" -Pass $adminHealthPass -Critical $true `
    -Message ("status_code={0}, health={1}" -f $adminHealth.status_code, $adminHealthStatus) -Request $adminHealth

$homeResp = Invoke-JsonRequest -Method GET -Url "$AppBase/home/content"
$homeCode = [int](Get-PropertyValue -InputObject $homeResp.json -Path @("code"))
$advertiseList = @(Get-PropertyValue -InputObject $homeResp.json -Path @("data", "advertiseList"))
$context.advertise_total = $advertiseList.Count
$advertiseCompliant = @($advertiseList | Where-Object { Is-AllowedImageUrl -value ([string]$_.pic) }).Count
$context.advertise_compliant = $advertiseCompliant
$homePass = ($homeResp.status_code -eq 200 -and $homeCode -eq 200 -and $advertiseList.Count -gt 0 -and $advertiseCompliant -eq $advertiseList.Count)
Add-Check -Checks ([ref]$checks) -Name "app.home.content" -Pass $homePass -Critical $true `
    -Message ("code={0}, advertise={1}, compliant={2}" -f $homeCode, $advertiseList.Count, $advertiseCompliant) -Request $homeResp

$searchResp = Invoke-JsonRequest -Method GET -Url "$AppBase/product/search?pageNum=1&pageSize=5"
$searchCode = [int](Get-PropertyValue -InputObject $searchResp.json -Path @("code"))
$productList = @(Get-PropertyValue -InputObject $searchResp.json -Path @("data", "list"))
$context.product_list_count = $productList.Count
$searchPass = ($searchResp.status_code -eq 200 -and $searchCode -eq 200 -and $productList.Count -gt 0)
Add-Check -Checks ([ref]$checks) -Name "app.product.search" -Pass $searchPass -Critical $true `
    -Message ("code={0}, list={1}" -f $searchCode, $productList.Count) -Request $searchResp

$sampleProduct = $productList | Where-Object { $_.id -and (Is-AllowedImageUrl -value ([string]$_.pic)) } | Select-Object -First 1
if ($null -eq $sampleProduct) {
    $sampleProduct = $productList | Select-Object -First 1
}
$sampleProductId = if ($sampleProduct) { [int]$sampleProduct.id } else { $null }
$context.sampled_product_id = $sampleProductId

if ($null -ne $sampleProductId) {
    $detailResp = Invoke-JsonRequest -Method GET -Url "$AppBase/product/detail/$sampleProductId"
    $detailCode = [int](Get-PropertyValue -InputObject $detailResp.json -Path @("code"))
    $detailPic = [string](Get-PropertyValue -InputObject $detailResp.json -Path @("data", "pic"))
    $detailAlbum = [string](Get-PropertyValue -InputObject $detailResp.json -Path @("data", "albumPics"))
    $detailHtml = [string](Get-PropertyValue -InputObject $detailResp.json -Path @("data", "detailHtml"))
    $detailPicPass = Is-AllowedImageUrl -value $detailPic
    $detailAlbumPass = ($detailAlbum -match '^/api/asset/image/[0-9a-fA-F]{64}(,/api/asset/image/[0-9a-fA-F]{64})*$')
    $detailHtmlPass = ($detailHtml -match '/api/asset/image/[0-9a-fA-F]{64}' -or $detailHtml -match 'data:image/')
    $detailPass = ($detailResp.status_code -eq 200 -and $detailCode -eq 200 -and $detailPicPass -and $detailAlbumPass -and $detailHtmlPass)
    Add-Check -Checks ([ref]$checks) -Name "app.product.detail" -Pass $detailPass -Critical $true `
        -Message ("code={0}, pic={1}, album={2}, detail_html={3}" -f $detailCode, $detailPicPass, $detailAlbumPass, $detailHtmlPass) -Request $detailResp
} else {
    Add-Check -Checks ([ref]$checks) -Name "app.product.detail" -Pass $false -Critical $true `
        -Message "no sample product id from /product/search"
}

$memberLoginBody = @{
    username = $MemberUser
    password = $MemberPassword
}
$memberLoginResp = Invoke-JsonRequest -Method POST -Url "$AppBase/sso/login" -Body $memberLoginBody
$memberCode = [int](Get-PropertyValue -InputObject $memberLoginResp.json -Path @("code"))
$memberToken = [string](Get-PropertyValue -InputObject $memberLoginResp.json -Path @("data", "token"))
$memberLoginPass = ($memberLoginResp.status_code -eq 200 -and $memberCode -eq 200 -and -not [string]::IsNullOrWhiteSpace($memberToken))
Add-Check -Checks ([ref]$checks) -Name "app.sso.login" -Pass $memberLoginPass -Critical $false `
    -Message ("code={0}, has_token={1}" -f $memberCode, (-not [string]::IsNullOrWhiteSpace($memberToken))) -Request $memberLoginResp

$adminLoginBody = @{
    username = $AdminUser
    password = $AdminPassword
}
$adminLoginResp = Invoke-JsonRequest -Method POST -Url "$AdminBase/admin/login" -Body $adminLoginBody
$adminCode = [int](Get-PropertyValue -InputObject $adminLoginResp.json -Path @("code"))
$adminToken = [string](Get-PropertyValue -InputObject $adminLoginResp.json -Path @("data", "token"))
$adminLoginPass = ($adminLoginResp.status_code -eq 200 -and $adminCode -eq 200 -and -not [string]::IsNullOrWhiteSpace($adminToken))
Add-Check -Checks ([ref]$checks) -Name "admin.login" -Pass $adminLoginPass -Critical $true `
    -Message ("code={0}, has_token={1}" -f $adminCode, (-not [string]::IsNullOrWhiteSpace($adminToken))) -Request $adminLoginResp

if ($adminLoginPass) {
    $headers = @{ Authorization = "Bearer $adminToken" }
    $adminInfoResp = Invoke-JsonRequest -Method GET -Url "$AdminBase/admin/info" -Headers $headers
    $adminInfoCode = [int](Get-PropertyValue -InputObject $adminInfoResp.json -Path @("code"))
    $adminInfoPass = ($adminInfoResp.status_code -eq 200 -and $adminInfoCode -eq 200)
    Add-Check -Checks ([ref]$checks) -Name "admin.info" -Pass $adminInfoPass -Critical $true `
        -Message ("code={0}" -f $adminInfoCode) -Request $adminInfoResp
} else {
    Add-Check -Checks ([ref]$checks) -Name "admin.info" -Pass $false -Critical $true `
        -Message "skipped because admin.login failed"
}

$criticalFailed = @($checks | Where-Object { $_.critical -and -not $_.pass }).Count
$allFailed = @($checks | Where-Object { -not $_.pass }).Count

$report = [ordered]@{
    generated_at = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ssK")
    parameters = [ordered]@{
        app_base = $AppBase
        admin_base = $AdminBase
        admin_user = $AdminUser
        member_user = $MemberUser
        report_path = $reportFile
    }
    context = $context
    summary = [ordered]@{
        total_checks = $checks.Count
        failed_checks = $allFailed
        critical_failed_checks = $criticalFailed
    }
    checks = $checks
}

Set-Content -LiteralPath $reportFile -Value ($report | ConvertTo-Json -Depth 8) -Encoding UTF8

Write-Host "===== api smoke v3 =====" -ForegroundColor Cyan
foreach ($item in $checks) {
    $statusText = if ($item.pass) { "PASS" } else { "FAIL" }
    $color = if ($item.pass) { "Green" } else { "Yellow" }
    Write-Host ("[{0}] {1} - {2}" -f $statusText, $item.name, $item.message) -ForegroundColor $color
}
Write-Host ("report: {0}" -f $reportFile)

if ($criticalFailed -gt 0) {
    Write-Host ("critical failures: {0}" -f $criticalFailed) -ForegroundColor Red
    exit 1
}

Write-Host "critical failures: 0" -ForegroundColor Green
exit 0
