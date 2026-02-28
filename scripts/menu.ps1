<#
.SYNOPSIS
  Mall V3 - Interactive Operations Menu
.DESCRIPTION
  Usage: .\menu.ps1

  Opens an interactive numbered menu. Type a number and press Enter.
  Type 'q' or '0' to exit.

  Drag this file to a terminal and press Enter to start.
#>

$ErrorActionPreference = "Continue"
$ROOT         = Split-Path -Parent $MyInvocation.MyCommand.Path
$PROJECT_ROOT = Split-Path -Parent $ROOT
$LOG_DIR      = Join-Path $PROJECT_ROOT "runtime-logs"

function Test-PortListening([int]$port) {
    $r = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
    return ($null -ne $r)
}

function Get-QuickStatus {
    $parts = @()
    if (Test-PortListening 18080) { $parts += "[+] app-api:18080" }   else { $parts += "[-] app-api:18080" }
    if (Test-PortListening 18081) { $parts += "[+] admin-api:18081" } else { $parts += "[-] admin-api:18081" }
    if (Test-PortListening 8090)  { $parts += "[+] admin-web:8090" }  else { $parts += "[-] admin-web:8090" }
    if (Test-PortListening 8091)  { $parts += "[+] app-web:8091" }    else { $parts += "[-] app-web:8091" }
    return $parts -join "   "
}

function Show-Menu {
    Clear-Host
    $statusLine = Get-QuickStatus
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host "  Mall V3 - 操作菜单" -ForegroundColor Cyan
    Write-Host "========================================" -ForegroundColor Cyan
    Write-Host ("  {0}" -f $statusLine) -ForegroundColor DarkGray
    Write-Host ""
      Write-Host "  --- 启动 / 停止 [生产模式，TTI<3s] ---" -ForegroundColor Yellow
      Write-Host "   1  全栈重启（生产前端）[★ 日常推荐]" -ForegroundColor Green
      Write-Host "   2  停止所有服务" -ForegroundColor White
      Write-Host "   3  仅重启后端" -ForegroundColor White
      Write-Host "   4  仅启动生产前端" -ForegroundColor White
      Write-Host ""
      Write-Host "  --- 重启 [生产模式] ---" -ForegroundColor Yellow
      Write-Host "   5  重启全栈（生产前端，自动构建）" -ForegroundColor White
      Write-Host "   6  重启全栈（生产前端，跳过构建）" -ForegroundColor White
      Write-Host "   7  仅重启后端" -ForegroundColor White
      Write-Host "   8  仅重启后端（跳过构建）" -ForegroundColor White
      Write-Host "   9  仅重启生产前端（跳过构建）" -ForegroundColor White
    Write-Host "  10  重启 mall-app-api" -ForegroundColor White
    Write-Host "  11  重启 mall-admin-api" -ForegroundColor White
    Write-Host "  12  重启 mall-job" -ForegroundColor White
    Write-Host ""
      Write-Host "  --- 开发者模式（需要 HMR 热更新时使用）---" -ForegroundColor DarkYellow
      Write-Host "  36  启动前端（dev/HMR 模式）" -ForegroundColor White
      Write-Host "  37  重启全栈（dev 前端）" -ForegroundColor White
      Write-Host "  38  停止生产前端服务" -ForegroundColor White
      Write-Host "  39  首次完整初始化（含基础设施启动）" -ForegroundColor White
    Write-Host ""
    Write-Host "  --- 构建 ---" -ForegroundColor Yellow
    Write-Host "  13  构建全部后端（package）" -ForegroundColor White
    Write-Host "  14  构建全部后端（clean package）" -ForegroundColor White
    Write-Host "  15  构建全部后端（含单元测试）" -ForegroundColor White
    Write-Host ""
    Write-Host "  --- 状态 & 日志 ---" -ForegroundColor Yellow
    Write-Host "  16  查看完整状态面板" -ForegroundColor White
    Write-Host "  17  实时监控状态（自动刷新）" -ForegroundColor White
    Write-Host "  18  追踪 mall-app-api 日志" -ForegroundColor White
    Write-Host "  19  追踪 mall-admin-api 日志" -ForegroundColor White
    Write-Host "  20  追踪 mall-job 日志" -ForegroundColor White
    Write-Host "  21  追踪前端 app 日志" -ForegroundColor White
    Write-Host "  22  追踪前端 admin 日志" -ForegroundColor White
    Write-Host "  23  查看所有日志（各取最后10行）" -ForegroundColor White
    Write-Host ""
    Write-Host "  --- 数据库 & 基础设施 ---" -ForegroundColor Yellow
    Write-Host "  24  初始化数据库（init-db.ps1）" -ForegroundColor White
    Write-Host "  25  环境预检（preflight-v3.ps1）" -ForegroundColor White
    Write-Host "  26  运行集成测试" -ForegroundColor White
    Write-Host "  27  校验文档规范（check-docs.ps1）" -ForegroundColor White
    Write-Host ""
    Write-Host "  --- 调试与审计 ---" -ForegroundColor Yellow
    Write-Host "  28  资源来源审计（audit-resource-origin.ps1）" -ForegroundColor White
    Write-Host "  29  API 冒烟检查（smoke-api-v3.ps1）" -ForegroundColor White
    Write-Host "  30  首页慢加载诊断（diagnose-home-slow.ps1）" -ForegroundColor White
    Write-Host "  33  首页网络主机审计（audit-home-network-hosts.ps1）" -ForegroundColor White
    Write-Host "  34  外链资源迁移（dry-run）" -ForegroundColor White
    Write-Host "  35  外链资源迁移（apply）" -ForegroundColor White
    Write-Host ""
    Write-Host "  --- 工具治理 ---" -ForegroundColor Yellow
    Write-Host "  31  工具脚本体检（toolbox doctor）" -ForegroundColor White
    Write-Host "  32  生成工具文档（toolbox docs）" -ForegroundColor White
    Write-Host ""
    Write-Host "   0  退出菜单" -ForegroundColor DarkGray
    Write-Host "========================================" -ForegroundColor Cyan
}

function Run-Script([string]$script, [hashtable]$params) {
    $path = Join-Path $ROOT $script
    if (Test-Path $path) {
        if ($null -ne $params -and $params.Count -gt 0) {
            & $path @params
        } else {
            & $path
        }
    } else {
        Write-Host "  脚本未找到：$path" -ForegroundColor Red
    }
    Write-Host ""
    Read-Host "  按 Enter 返回菜单"
}

# ================================================================
while ($true) {
    Show-Menu
    $choice = Read-Host "  请输入编号"
    $choice = $choice.Trim()

    switch ($choice) {
        "0"  { Write-Host "  再见！" -ForegroundColor Cyan; break }
        "q"  { Write-Host "  再见！" -ForegroundColor Cyan; break }
          "1"  { Run-Script "restart.ps1"      @{ Prod = $true } }
          "2"  { Run-Script "stop-v3.ps1"       $null }
          "3"  { Run-Script "restart-be.ps1"    $null }
          "4"  { Run-Script "start-fe-prod.ps1" $null }
          "5"  { Run-Script "restart.ps1"       @{ Prod = $true } }
          "6"  { Run-Script "restart.ps1"       @{ Prod = $true; SkipBuild = $true } }
          "7"  { Run-Script "restart-be.ps1"    $null }
          "8"  { Run-Script "restart-be.ps1"    @{ SkipBuild = $true } }
          "9"  { Run-Script "start-fe-prod.ps1" @{ SkipBuild = $true } }
        "10" { Run-Script "restart-be.ps1"   @{ Service = "app" } }
        "11" { Run-Script "restart-be.ps1"   @{ Service = "admin" } }
        "12" { Run-Script "restart-be.ps1"   @{ Service = "job" } }
        "13" { Run-Script "build-be.ps1"     $null }
        "14" { Run-Script "build-be.ps1"     @{ Clean = $true } }
        "15" { Run-Script "build-be.ps1"     @{ Test = $true } }
        "16" { Run-Script "status.ps1"       $null }
        "17" { Run-Script "status.ps1"       @{ Watch = $true } }
        "18" { Run-Script "logs.ps1"         @{ Service = "app"; Lines = 50 } }
        "19" { Run-Script "logs.ps1"         @{ Service = "admin"; Lines = 50 } }
        "20" { Run-Script "logs.ps1"         @{ Service = "job"; Lines = 50 } }
        "21" { Run-Script "logs.ps1"         @{ Service = "fe-app"; Lines = 50 } }
        "22" { Run-Script "logs.ps1"         @{ Service = "fe-admin"; Lines = 50 } }
        "23" { Run-Script "logs.ps1"         @{ Service = "all"; Lines = 10 } }
        "24" { Run-Script "init-db.ps1"      $null }
        "25" { Run-Script "preflight-v3.ps1" $null }
        "26" { Run-Script "run-tests.ps1"    $null }
        "27" { Run-Script "check-docs.ps1"   $null }
        "28" { Run-Script "audit-resource-origin.ps1" @{ FailOnViolation = $true } }
        "29" { Run-Script "smoke-api-v3.ps1"          $null }
        "30" { Run-Script "diagnose-home-slow.ps1"    $null }
        "33" { Run-Script "audit-home-network-hosts.ps1" @{ FailOnViolation = $true } }
        "34" { Run-Script "migrate-external-assets.ps1"  $null }
        "35" { Run-Script "migrate-external-assets.ps1"  @{ Apply = $true } }
        "31" { Run-Script "toolbox.ps1"               @{ Command = "doctor" } }
        "32" { Run-Script "toolbox.ps1"               @{ Command = "docs" } }
          "36" { Run-Script "restart-fe.ps1"            $null }
          "37" { Run-Script "restart.ps1"               $null }
          "38" { Run-Script "stop-fe-prod.ps1"          $null }
          "39" { Run-Script "start-v3.ps1"              @{ Frontend = $true } }
        default {
            Write-Host "  无效选项：$choice" -ForegroundColor Red
            Start-Sleep -Seconds 1
        }
    }

    if ($choice -eq "0" -or $choice -eq "q") { break }
}
