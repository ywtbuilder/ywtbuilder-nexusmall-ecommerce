[CmdletBinding()]
param(
  [Parameter(Position = 0)]
  [ValidateSet('list', 'info', 'run', 'doctor', 'export', 'graph', 'docs')]
  [string]$Command = 'list',

  [string]$Id,
  [string]$Scope,
  [string]$Category,
  [string]$Status,
  [ValidateSet('table', 'json', 'md', 'csv')]
  [string]$Format = 'table',
  [switch]$IncludeVendor,
  [string]$OutFile,
  [string]$PassThruArgs,
  [switch]$DryRun,
  [switch]$Force,
  [ValidateSet('dependency', 'duplicate', 'path')]
  [string]$Type = 'dependency'
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Get-RepoContext {
  $scriptDir = Split-Path -Parent $PSCommandPath
  $repoRoot = Split-Path -Parent $scriptDir
  $workspaceRoot = Split-Path -Parent $repoRoot
  [pscustomobject]@{
    ScriptDir     = $scriptDir
    RepoRoot      = $repoRoot
    WorkspaceRoot = $workspaceRoot
  }
}

function Get-TargetRoots {
  param(
    [Parameter(Mandatory = $true)]$Context
  )

  @(
    [pscustomobject]@{
      Scope      = 'root_tools'
      Label      = 'workspace TOOLS'
      Path       = (Join-Path $Context.WorkspaceRoot 'TOOLS')
      Required   = $true
      SearchMode = 'direct'
    }
    [pscustomobject]@{
      Scope      = 'v3_scripts'
      Label      = 'project_mall_v3 scripts'
      Path       = $Context.ScriptDir
      Required   = $true
      SearchMode = 'direct'
    }
    [pscustomobject]@{
      Scope      = 'assets'
      Label      = 'project_mall_v3 assets'
      Path       = (Join-Path $Context.RepoRoot 'assets')
      Required   = $true
      SearchMode = 'direct'
    }
    [pscustomobject]@{
      Scope      = 'assets_scraped_tools_legacy'
      Label      = 'legacy scraped tools path'
      Path       = (Join-Path $Context.RepoRoot 'assets/爬取商品数据/TOOLS')
      Required   = $false
      SearchMode = 'direct'
    }
    [pscustomobject]@{
      Scope      = 'assets_scraped_tools_current'
      Label      = 'current scraped tools path'
      Path       = (Join-Path $Context.RepoRoot 'assets/淘宝爬取商品数据/TOOLS')
      Required   = $false
      SearchMode = 'direct'
    }
  )
}

function Convert-ToToolId {
  param(
    [Parameter(Mandatory = $true)][string]$Scope,
    [Parameter(Mandatory = $true)][string]$RelativePath
  )

  $clean = $RelativePath.ToLowerInvariant().Replace('\', '/')
  $clean = [Regex]::Replace($clean, '[^a-z0-9/_\.-]', '-')
  $clean = $clean.Replace('/', '__')
  "{0}__{1}" -f $Scope, $clean
}

function Get-RunTemplate {
  param(
    [Parameter(Mandatory = $true)]$Item
  )

  $path = $Item.AbsolutePath.Replace('\', '/')
  switch ($Item.Extension.ToLowerInvariant()) {
    '.ps1' { return "pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File `"$path`"" }
    '.py' { return "python `"$path`"" }
    '.cmd' { return "cmd /c `"$path`"" }
    '.bat' { return "cmd /c `"$path`"" }
    '.sh' { return "bash `"$path`"" }
    '.js' { return "node `"$path`"" }
    default { return "pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -Command `"Get-Content -LiteralPath '$path' | Out-Null`"" }
  }
}

function Get-ScopeRank {
  param([Parameter(Mandatory = $true)][string]$Scope)
  switch ($Scope) {
    'v3_scripts' { return 1 }
    'assets_scraped_tools_current' { return 2 }
    'assets_scraped_tools_legacy' { return 3 }
    'root_tools' { return 4 }
    'assets' { return 5 }
    default { return 9 }
  }
}

function Get-CategoryFromItem {
  param(
    [Parameter(Mandatory = $true)]$Item
  )

  $name = $Item.Name.ToLowerInvariant()
  $base = [IO.Path]::GetFileNameWithoutExtension($name)
  $rel = $Item.RelativePath.ToLowerInvariant().Replace('\', '/')
  $scope = $Item.Scope.ToLowerInvariant()

  if ($base -eq 'toolbox') { return 'tool-governance' }
  if ($rel -match 'node_modules') { return 'third-party-vendor' }
  if ($base -match '^(start|stop|restart|preflight|status|logs|menu|dev-start)') { return 'environment-lifecycle' }
  if ($base -match '(build|run-tests|smoke|check-docs|doc-catalog|lint|test)') { return 'build-test-docs' }
  if ($base -match '(init-db|import|seed|migrate|sync|check-product-detail-data)') { return 'data-integration' }
  if ($base -match '^(rs_|rewrite|extract|scan|api_analyze|resource|asset)') { return 'resource-offline' }
  if ($base -match '(audit|diagnose|find_|search|read_|check_)') { return 'diagnose-audit' }
  if ($base -match '(fix_|inject|patch)') { return 'patch-hotfix' }
  if ($base -match '(server|ctl|local_server)') { return 'local-service' }
  if ($scope -eq 'root_tools') { return 'legacy-other' }
  return 'legacy-other'
}

function Get-StatusFromItem {
  param(
    [Parameter(Mandatory = $true)]$Item
  )

  $base = [IO.Path]::GetFileNameWithoutExtension($Item.Name.ToLowerInvariant())
  $rel = $Item.RelativePath.ToLowerInvariant().Replace('\', '/')
  if ($rel -match 'node_modules') { return 'vendor' }
  if ($base -match '(report|snapshot|generated|tmp)') { return 'generated' }
  switch ($Item.Scope) {
    'v3_scripts' { return 'active' }
    'root_tools' { return 'legacy' }
    'assets_scraped_tools_current' { return 'snapshot' }
    'assets_scraped_tools_legacy' { return 'snapshot' }
    'assets' { return 'snapshot' }
    default { return 'unknown' }
  }
}

function Get-SideEffectFromItem {
  param(
    [Parameter(Mandatory = $true)]$Item
  )

  $base = [IO.Path]::GetFileNameWithoutExtension($Item.Name.ToLowerInvariant())
  if ($base -match '(import|init-db|migrate|seed|sync)') { return 'write_db' }
  if ($base -match '(fix|inject|rewrite|extract|patch)') { return 'write_fs' }
  if ($base -match '(start|stop|restart|status|logs|smoke|audit|diagnose)') { return 'runtime_ops' }
  return 'read_only'
}

function Get-HardcodedPathHint {
  param(
    [Parameter(Mandatory = $true)]$AbsolutePath
  )

  try {
    $snippet = Get-Content -LiteralPath $AbsolutePath -TotalCount 200 -ErrorAction Stop
  } catch {
    return [pscustomobject]@{
      HasHardcodedPath = $false
      Hint             = $null
    }
  }

  $patterns = @(
    '(?<![A-Za-z0-9_])[A-Za-z]:[\\/](?![\\/])'
  )

  foreach ($line in $snippet) {
    foreach ($pattern in $patterns) {
      if ($line -match $pattern) {
        if ($line -match '^https?://') {
          continue
        }
        return [pscustomobject]@{
          HasHardcodedPath = $true
          Hint             = $line.Trim()
        }
      }
    }
  }

  [pscustomobject]@{
    HasHardcodedPath = $false
    Hint             = $null
  }
}

function Get-ToolInventory {
  param(
    [switch]$IncludeVendorItems
  )

  $context = Get-RepoContext
  $roots = Get-TargetRoots -Context $context
  $extensions = @('.ps1', '.py', '.cmd', '.bat', '.sh', '.js')
  $missingRoots = [System.Collections.Generic.List[object]]::new()
  $items = [System.Collections.Generic.List[object]]::new()

  foreach ($root in $roots) {
    if (-not (Test-Path -LiteralPath $root.Path)) {
      $missingRoots.Add([pscustomobject]@{
          Scope    = $root.Scope
          Label    = $root.Label
          Path     = $root.Path
          Required = $root.Required
        })
      continue
    }

    $files = Get-ChildItem -LiteralPath $root.Path -Recurse -File -ErrorAction SilentlyContinue |
      Where-Object { $extensions -contains $_.Extension.ToLowerInvariant() }

    foreach ($file in $files) {
      $relative = [IO.Path]::GetRelativePath($root.Path, $file.FullName)
      if ($relative -eq '.') { $relative = $file.Name }

      $hardcoded = Get-HardcodedPathHint -AbsolutePath $file.FullName
      $item = [pscustomobject]@{
        Id               = Convert-ToToolId -Scope $root.Scope -RelativePath $relative
        Name             = $file.Name
        Scope            = $root.Scope
        ScopeLabel       = $root.Label
        AbsolutePath     = $file.FullName
        RelativePath     = $relative.Replace('\', '/')
        Extension        = $file.Extension.ToLowerInvariant()
        Category         = $null
        Status           = $null
        SideEffect       = $null
        RunTemplate      = $null
        Sha256           = (Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
        DuplicateGroup   = $null
        CanonicalId      = $null
        SizeBytes        = $file.Length
        LastWriteUtc     = $file.LastWriteTimeUtc.ToString('yyyy-MM-ddTHH:mm:ssZ')
        HasHardcodedPath = $hardcoded.HasHardcodedPath
        HardcodedPathHint = $hardcoded.Hint
      }

      $item.Category = Get-CategoryFromItem -Item $item
      $item.Status = Get-StatusFromItem -Item $item
      $item.SideEffect = Get-SideEffectFromItem -Item $item
      $item.RunTemplate = Get-RunTemplate -Item $item
      $items.Add($item)
    }
  }

  $groups = $items | Group-Object -Property Sha256
  foreach ($group in $groups) {
    $sorted = $group.Group |
      Sort-Object @{
        Expression = { Get-ScopeRank -Scope $_.Scope }
      }, @{
        Expression = { $_.RelativePath.Length }
      }, @{
        Expression = { $_.RelativePath }
      }

    $canonical = $sorted[0]
    if ($group.Count -gt 1) {
      $dupId = 'dup-{0}' -f $group.Name.Substring(0, 12)
      foreach ($item in $group.Group) {
        $item.DuplicateGroup = $dupId
        $item.CanonicalId = $canonical.Id
      }
    } else {
      $canonical.CanonicalId = $canonical.Id
    }
  }

  if (-not $IncludeVendorItems) {
    $items = [System.Collections.Generic.List[object]]($items | Where-Object { $_.Status -ne 'vendor' })
  }

  [pscustomobject]@{
    GeneratedAt = (Get-Date).ToString('yyyy-MM-ddTHH:mm:ssK')
    Context     = $context
    Roots       = $roots
    MissingRoots = $missingRoots
    Items       = $items
  }
}

function Apply-Filters {
  param(
    [Parameter(Mandatory = $true)]$Items,
    [string]$Scope,
    [string]$Category,
    [string]$Status
  )

  $result = $Items
  if ($Scope) { $result = $result | Where-Object { $_.Scope -eq $Scope } }
  if ($Category) { $result = $result | Where-Object { $_.Category -eq $Category } }
  if ($Status) { $result = $result | Where-Object { $_.Status -eq $Status } }
  $result
}

function Convert-ItemsToMarkdown {
  param(
    [Parameter(Mandatory = $true)]$Items
  )

  $lines = [System.Collections.Generic.List[string]]::new()
  $lines.Add('| Id | Scope | Category | Status | SideEffect | RelativePath |')
  $lines.Add('|---|---|---|---|---|---|')
  foreach ($item in ($Items | Sort-Object Scope, Category, RelativePath)) {
    $safeRel = $item.RelativePath.Replace('|', '\|')
    $lines.Add("| ``$($item.Id)`` | ``$($item.Scope)`` | ``$($item.Category)`` | ``$($item.Status)`` | ``$($item.SideEffect)`` | ``$safeRel`` |")
  }
  $lines -join [Environment]::NewLine
}

function Write-ContentFile {
  param(
    [Parameter(Mandatory = $true)][string]$Path,
    [Parameter(Mandatory = $true)][string]$Content
  )
  $parent = Split-Path -Parent $Path
  if (-not (Test-Path -LiteralPath $parent)) {
    New-Item -ItemType Directory -Path $parent -Force | Out-Null
  }
  Set-Content -LiteralPath $Path -Value $Content -Encoding UTF8
}

function Add-DocFrontmatter {
  param(
    [Parameter(Mandatory = $true)][string]$Content,
    [Parameter(Mandatory = $true)][string]$DocType
  )

  $today = (Get-Date).ToString('yyyy-MM-dd')
  @(
    '---'
    'owner: tools'
    "updated: $today"
    'scope: mall-v3'
    'audience: dev,qa,ops'
    "doc_type: $DocType"
    '---'
    ''
    $Content
  ) -join [Environment]::NewLine
}

function Build-FileStructureMarkdown {
  param(
    [Parameter(Mandatory = $true)]$Inventory
  )

  $lines = [System.Collections.Generic.List[string]]::new()
  $lines.Add('# File Structure')
  $lines.Add('')
  $lines.Add('```text')

  $roots = $Inventory.Roots | Sort-Object Scope
  foreach ($root in $roots) {
    $exists = Test-Path -LiteralPath $root.Path
    $state = if ($exists) { 'exists' } else { 'missing' }
    $lines.Add("$($root.Scope) ($state)")
    if ($exists) {
      $children = $Inventory.Items | Where-Object { $_.Scope -eq $root.Scope } | Sort-Object RelativePath
      foreach ($child in $children) {
        $lines.Add("  - $($child.RelativePath)")
      }
    }
  }

  $lines.Add('```')
  $lines -join [Environment]::NewLine
}

function Build-DependencyMarkdown {
  param(
    [Parameter(Mandatory = $true)]$Inventory
  )

  $lines = [System.Collections.Generic.List[string]]::new()
  $lines.Add('# Dependency Graph')
  $lines.Add('')
  $lines.Add('```mermaid')
  $lines.Add('graph TD')
  $lines.Add('  V3Scripts[project_mall_v3/scripts]')
  $lines.Add('  RootTools[workspace/TOOLS]')
  $lines.Add('  Assets[project_mall_v3/assets]')
  $lines.Add('  Toolbox[toolbox.ps1]')
  $lines.Add('  Docs[docs/tools/*]')
  $lines.Add('  Toolbox --> V3Scripts')
  $lines.Add('  Toolbox --> RootTools')
  $lines.Add('  Toolbox --> Assets')
  $lines.Add('  Toolbox --> Docs')

  $menu = $Inventory.Items | Where-Object { $_.Scope -eq 'v3_scripts' -and $_.Name -eq 'menu.ps1' }
  if ($menu) {
    $lines.Add('  Menu[menu.ps1] --> V3Scripts')
    $lines.Add('  Toolbox -. discover .-> Menu')
  }

  $dupGroups = $Inventory.Items | Where-Object { $_.DuplicateGroup } | Group-Object DuplicateGroup
  foreach ($dup in $dupGroups) {
    $groupLabel = $dup.Name.Replace('-', '_')
    $lines.Add("  $groupLabel[$($dup.Name)]")
    foreach ($item in $dup.Group) {
      $nodeId = ('n' + ([Regex]::Replace($item.Id, '[^a-zA-Z0-9_]', '_')))
      $lines.Add("  $nodeId[`"$($item.RelativePath)`"]")
      $lines.Add("  $groupLabel --> $nodeId")
    }
  }

  $lines.Add('```')
  $lines -join [Environment]::NewLine
}

function Build-ResourcePanoramaMarkdown {
  param(
    [Parameter(Mandatory = $true)]$Inventory
  )

  $items = $Inventory.Items
  $categoryCount = $items | Group-Object Category | Sort-Object Count -Descending

  $lines = [System.Collections.Generic.List[string]]::new()
  $lines.Add('# Resource Panorama')
  $lines.Add('')
  $lines.Add('## Scope Summary')
  $lines.Add('')
  $lines.Add('| Scope | Path | Exists | ToolCount |')
  $lines.Add('|---|---|---|---|')
  foreach ($root in $Inventory.Roots) {
    $exists = Test-Path -LiteralPath $root.Path
    $count = ($items | Where-Object { $_.Scope -eq $root.Scope } | Measure-Object).Count
    $safePath = $root.Path.Replace('\', '/')
    $lines.Add("| ``$($root.Scope)`` | ``$safePath`` | ``$exists`` | ``$count`` |")
  }

  $lines.Add('')
  $lines.Add('## Category Coverage')
  $lines.Add('')
  $lines.Add('| Category | Count |')
  $lines.Add('|---|---|')
  foreach ($row in $categoryCount) {
    $lines.Add("| ``$($row.Name)`` | ``$($row.Count)`` |")
  }

  $lines.Add('')
  $lines.Add('## Hardcoded Path Risk')
  $lines.Add('')
  $lines.Add('| ToolId | Scope | Hint |')
  $lines.Add('|---|---|---|')
  $riskItems = $items | Where-Object { $_.HasHardcodedPath } | Sort-Object Scope, RelativePath
  if ($riskItems) {
    foreach ($risk in $riskItems) {
      $hint = if ($risk.HardcodedPathHint) { $risk.HardcodedPathHint.Replace('|', '\|') } else { '' }
      $lines.Add("| ``$($risk.Id)`` | ``$($risk.Scope)`` | ``$hint`` |")
    }
  } else {
    $lines.Add('| _none_ | - | - |')
  }

  $lines -join [Environment]::NewLine
}

function Build-DataflowInterfaceMarkdown {
  param(
    [Parameter(Mandatory = $true)]$Inventory
  )

  $lines = [System.Collections.Generic.List[string]]::new()
  $lines.Add('# Dataflow And Interface Catalog')
  $lines.Add('')
  $lines.Add('| ToolId | Category | CommandTemplate | SideEffect | Canonical |')
  $lines.Add('|---|---|---|---|---|')
  foreach ($item in ($Inventory.Items | Sort-Object Scope, Category, RelativePath)) {
    $safeCommand = $item.RunTemplate.Replace('|', '\|')
    $lines.Add("| ``$($item.Id)`` | ``$($item.Category)`` | ``$safeCommand`` | ``$($item.SideEffect)`` | ``$($item.CanonicalId)`` |")
  }

  $lines -join [Environment]::NewLine
}

function Build-DeprecationMarkdown {
  param(
    [Parameter(Mandatory = $true)]$Inventory
  )

  $legacy = $Inventory.Items | Where-Object { $_.Status -eq 'legacy' } | Sort-Object Scope, RelativePath
  $highRisk = $legacy | Where-Object { $_.SideEffect -in @('write_fs', 'write_db', 'runtime_ops') -or $_.HasHardcodedPath }

  $lines = [System.Collections.Generic.List[string]]::new()
  $lines.Add('# Deprecation Plan')
  $lines.Add('')
  $lines.Add('## Goal')
  $lines.Add('- Keep existing behavior stable while converging to a single managed entrypoint.')
  $lines.Add('- Use `toolbox.ps1` as the only discovery and execution entrypoint.')
  $lines.Add('')
  $lines.Add('## Legacy Inventory')
  $lines.Add('')
  $lines.Add('| ToolId | SideEffect | HardcodedPath | SuggestedAction |')
  $lines.Add('|---|---|---|---|')
  if ($legacy) {
    foreach ($item in $legacy) {
      $action = if ($item.SideEffect -in @('write_fs', 'write_db')) { 'migrate-first' } else { 'parameterize-path' }
      $lines.Add("| ``$($item.Id)`` | ``$($item.SideEffect)`` | ``$($item.HasHardcodedPath)`` | ``$action`` |")
    }
  } else {
    $lines.Add('| _none_ | - | - | - |')
  }

  $lines.Add('')
  $lines.Add('## High Risk First')
  $lines.Add('')
  $lines.Add('| ToolId | Reason |')
  $lines.Add('|---|---|')
  if ($highRisk) {
    foreach ($item in $highRisk) {
      $reason = @()
      if ($item.SideEffect -in @('write_fs', 'write_db')) { $reason += $item.SideEffect }
      if ($item.HasHardcodedPath) { $reason += 'hardcoded-path' }
      $lines.Add("| ``$($item.Id)`` | ``$($reason -join ',')`` |")
    }
  } else {
    $lines.Add('| _none_ | - |')
  }

  $lines.Add('')
  $lines.Add('## Execution Order')
  $lines.Add('1. Freeze new additions in legacy directories (`TOOLS`).')
  $lines.Add('2. Parameterize hardcoded absolute paths.')
  $lines.Add('3. Move stable scripts into `project_mall_v3/scripts/legacy/` with compatibility wrapper.')
  $lines.Add('4. Mark old IDs as deprecated in catalog and stop direct usage.')
  $lines.Add('5. Remove deprecated scripts after two iterations without usage.')

  $lines -join [Environment]::NewLine
}

function Build-ToolReadmeMarkdown {
  param(
    [Parameter(Mandatory = $true)]$Inventory
  )

  $total = ($Inventory.Items | Measure-Object).Count
  $dupGroups = ($Inventory.Items | Where-Object { $_.DuplicateGroup } | Group-Object DuplicateGroup | Measure-Object).Count
  $missing = ($Inventory.MissingRoots | Measure-Object).Count

  $lines = [System.Collections.Generic.List[string]]::new()
  $lines.Add('# Tools Governance')
  $lines.Add('')
  $lines.Add('## Overview')
  $lines.Add('')
  $lines.Add("- Total tools indexed: ``$total``")
  $lines.Add("- Duplicate groups: ``$dupGroups``")
  $lines.Add("- Missing roots: ``$missing``")
  $lines.Add('')
  $lines.Add('## Usage')
  $lines.Add('')
  $lines.Add('```powershell')
  $lines.Add('pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\project_mall_v3\scripts\toolbox.ps1 list')
  $lines.Add('pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\project_mall_v3\scripts\toolbox.ps1 doctor')
  $lines.Add('pwsh -NoLogo -NoProfile -ExecutionPolicy Bypass -File .\project_mall_v3\scripts\toolbox.ps1 docs')
  $lines.Add('```')
  $lines.Add('')
  $lines.Add('## Output Docs')
  $lines.Add('')
  $lines.Add('- `TOOL_CATALOG.md`')
  $lines.Add('- `DEPENDENCY_GRAPH.md`')
  $lines.Add('- `RESOURCE_PANORAMA.md`')
  $lines.Add('- `FILE_STRUCTURE.md`')
  $lines.Add('- `DATAFLOW_INTERFACE.md`')
  $lines.Add('- `DEPRECATION_PLAN.md`')

  $lines -join [Environment]::NewLine
}

function Write-DocsBundle {
  param(
    [Parameter(Mandatory = $true)]$Inventory
  )

  $repoRoot = $Inventory.Context.RepoRoot
  $docsRoot = Join-Path $repoRoot 'docs/tools'
  $generatedRoot = Join-Path $repoRoot 'docs/_generated/tools'
  New-Item -ItemType Directory -Path $docsRoot -Force | Out-Null
  New-Item -ItemType Directory -Path $generatedRoot -Force | Out-Null

  Write-ContentFile -Path (Join-Path $docsRoot 'README.md') -Content (Add-DocFrontmatter -Content (Build-ToolReadmeMarkdown -Inventory $Inventory) -DocType 'index')
  Write-ContentFile -Path (Join-Path $docsRoot 'TOOL_CATALOG.md') -Content (Add-DocFrontmatter -Content (Convert-ItemsToMarkdown -Items $Inventory.Items) -DocType 'catalog')
  Write-ContentFile -Path (Join-Path $docsRoot 'DEPENDENCY_GRAPH.md') -Content (Add-DocFrontmatter -Content (Build-DependencyMarkdown -Inventory $Inventory) -DocType 'architecture')
  Write-ContentFile -Path (Join-Path $docsRoot 'RESOURCE_PANORAMA.md') -Content (Add-DocFrontmatter -Content (Build-ResourcePanoramaMarkdown -Inventory $Inventory) -DocType 'mapping')
  Write-ContentFile -Path (Join-Path $docsRoot 'FILE_STRUCTURE.md') -Content (Add-DocFrontmatter -Content (Build-FileStructureMarkdown -Inventory $Inventory) -DocType 'structure')
  Write-ContentFile -Path (Join-Path $docsRoot 'DATAFLOW_INTERFACE.md') -Content (Add-DocFrontmatter -Content (Build-DataflowInterfaceMarkdown -Inventory $Inventory) -DocType 'interface')
  Write-ContentFile -Path (Join-Path $docsRoot 'DEPRECATION_PLAN.md') -Content (Add-DocFrontmatter -Content (Build-DeprecationMarkdown -Inventory $Inventory) -DocType 'governance')
  Write-ContentFile -Path (Join-Path $generatedRoot 'toolbox.catalog.json') -Content (($Inventory.Items | ConvertTo-Json -Depth 8))
  Write-ContentFile -Path (Join-Path $generatedRoot 'toolbox.summary.json') -Content (([pscustomobject]@{
        generatedAt = $Inventory.GeneratedAt
        total       = ($Inventory.Items | Measure-Object).Count
        byScope     = $Inventory.Roots | ForEach-Object {
          $scopeName = $_.Scope
          [pscustomobject]@{
            scope = $scopeName
            path = $_.Path
            exists = (Test-Path -LiteralPath $_.Path)
            count = (($Inventory.Items | Where-Object { $_.Scope -eq $scopeName }) | Measure-Object).Count
          }
        }
        byCategory  = $Inventory.Items | Group-Object Category | ForEach-Object { [pscustomobject]@{ category = $_.Name; count = $_.Count } }
        missingRoots = $Inventory.MissingRoots
      } | ConvertTo-Json -Depth 8))
}

function Invoke-ToolCommand {
  param(
    [Parameter(Mandatory = $true)]$Item,
    [string]$PassThruArgs,
    [switch]$DryRun,
    [switch]$Force
  )

  if ($Item.Status -eq 'vendor' -and -not $Force) {
    throw "Tool [$($Item.Id)] is vendor scoped. Use -Force to run."
  }

  if ($Item.SideEffect -in @('write_db', 'write_fs', 'runtime_ops') -and -not $Force -and -not $DryRun) {
    throw "Tool [$($Item.Id)] has side effect [$($Item.SideEffect)]. Use -Force or -DryRun."
  }

  $commandLine = $Item.RunTemplate
  if ($PassThruArgs) {
    $commandLine = "{0} {1}" -f $commandLine, $PassThruArgs
  }

  if ($DryRun) {
    $commandLine
    return
  }

  Write-Host ("[toolbox] running: {0}" -f $commandLine) -ForegroundColor Cyan
  Invoke-Expression $commandLine
  if ($LASTEXITCODE -ne $null -and $LASTEXITCODE -ne 0) {
    exit $LASTEXITCODE
  }
}

function Write-DoctorSummary {
  param(
    [Parameter(Mandatory = $true)]$Inventory
  )

  $requiredMissing = $Inventory.MissingRoots | Where-Object { $_.Required }
  $duplicateGroups = $Inventory.Items | Where-Object { $_.DuplicateGroup } | Group-Object DuplicateGroup
  $hardcoded = $Inventory.Items | Where-Object { $_.HasHardcodedPath }

  $summary = [pscustomobject]@{
    generatedAt      = $Inventory.GeneratedAt
    totalTools       = ($Inventory.Items | Measure-Object).Count
    byScope          = $Inventory.Roots | ForEach-Object {
      $scopeName = $_.Scope
      [pscustomobject]@{
        scope = $scopeName
        path = $_.Path
        exists = (Test-Path -LiteralPath $_.Path)
        count = (($Inventory.Items | Where-Object { $_.Scope -eq $scopeName }) | Measure-Object).Count
      }
    }
    byCategory       = $Inventory.Items | Group-Object Category | ForEach-Object { [pscustomobject]@{ category = $_.Name; count = $_.Count } }
    duplicateGroups  = $duplicateGroups | ForEach-Object { [pscustomobject]@{ duplicateGroup = $_.Name; count = $_.Count } }
    hardcodedPath    = $hardcoded | Select-Object Id, Scope, RelativePath, HardcodedPathHint
    missingRoots     = $Inventory.MissingRoots
    requiredMissing  = $requiredMissing
  }

  if ($Format -eq 'json') {
    $summary | ConvertTo-Json -Depth 8
    return
  }

  $summary
  if ($requiredMissing.Count -gt 0) {
    Write-Warning "One or more required roots are missing."
  }
}

$inventory = Get-ToolInventory -IncludeVendorItems:$IncludeVendor
$items = Apply-Filters -Items $inventory.Items -Scope $Scope -Category $Category -Status $Status

switch ($Command) {
  'list' {
    switch ($Format) {
      'json' { $items | ConvertTo-Json -Depth 8 }
      'md' { Convert-ItemsToMarkdown -Items $items }
      'csv' { $items | ConvertTo-Csv -NoTypeInformation }
      default {
        $items |
          Sort-Object Scope, Category, RelativePath |
          Select-Object Id, Scope, Category, Status, SideEffect, RelativePath
      }
    }
  }
  'info' {
    if (-not $Id) { throw 'Please provide -Id for info command.' }
    $item = $inventory.Items | Where-Object { $_.Id -eq $Id } | Select-Object -First 1
    if (-not $item) { throw "Tool id not found: $Id" }
    $item | ConvertTo-Json -Depth 8
  }
  'run' {
    if (-not $Id) { throw 'Please provide -Id for run command.' }
    $item = $inventory.Items | Where-Object { $_.Id -eq $Id } | Select-Object -First 1
    if (-not $item) { throw "Tool id not found: $Id" }
    Invoke-ToolCommand -Item $item -PassThruArgs $PassThruArgs -DryRun:$DryRun -Force:$Force
  }
  'doctor' {
    Write-DoctorSummary -Inventory $inventory
  }
  'export' {
    if (-not $OutFile) { throw 'Please provide -OutFile for export command.' }
    $payload = switch ($Format) {
      'json' { $items | ConvertTo-Json -Depth 8 }
      'md' { Convert-ItemsToMarkdown -Items $items }
      'csv' { ($items | ConvertTo-Csv -NoTypeInformation) -join [Environment]::NewLine }
      default { $items | ConvertTo-Json -Depth 8 }
    }
    Write-ContentFile -Path $OutFile -Content $payload
    Write-Host ("[toolbox] exported to {0}" -f $OutFile)
  }
  'graph' {
    $content = switch ($Type) {
      'dependency' { Build-DependencyMarkdown -Inventory $inventory }
      'path' { Build-FileStructureMarkdown -Inventory $inventory }
      'duplicate' {
        $lines = [System.Collections.Generic.List[string]]::new()
        $lines.Add('# Duplicate Graph')
        $lines.Add('')
        $lines.Add('```mermaid')
        $lines.Add('graph TD')
        $dupGroups = $inventory.Items | Where-Object { $_.DuplicateGroup } | Group-Object DuplicateGroup
        foreach ($dup in $dupGroups) {
          $dupNode = $dup.Name.Replace('-', '_')
          $lines.Add("  $dupNode[$($dup.Name)]")
          foreach ($item in $dup.Group) {
            $node = 'n' + ([Regex]::Replace($item.Id, '[^a-zA-Z0-9_]', '_'))
            $lines.Add("  $node[`"$($item.RelativePath)`"]")
            $lines.Add("  $dupNode --> $node")
          }
        }
        $lines.Add('```')
        $lines -join [Environment]::NewLine
      }
      default { Build-DependencyMarkdown -Inventory $inventory }
    }

    if ($OutFile) {
      Write-ContentFile -Path $OutFile -Content $content
      Write-Host ("[toolbox] graph exported: {0}" -f $OutFile)
    } else {
      $content
    }
  }
  'docs' {
    Write-DocsBundle -Inventory $inventory
    Write-Host '[toolbox] docs generated under project_mall_v3/docs/tools and docs/_generated/tools'
  }
  default {
    throw "Unknown command: $Command"
  }
}
