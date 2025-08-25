Param(
    [string]$Path = "frontend",                   # Root folder to scan
    [string[]]$AllowFiles = @("frontend/src/styles.css"), # Files allowed to contain color literals
    [switch]$Json,                                  # Output as JSON
    [switch]$Summary                                # Print summary counts only
)

$ErrorActionPreference = 'Stop'

function Resolve-RepoRoot {
    param([string]$Start)
    $dir = Resolve-Path -LiteralPath $Start
    while ($dir) {
        if (Test-Path -LiteralPath (Join-Path $dir ".git")) { return $dir }
        $parent = Split-Path -LiteralPath $dir -Parent
        if ($parent -eq $dir) { break }
        $dir = $parent
    }
    # Fallback to current directory
    return (Get-Location).Path
}

$repoRoot = Resolve-RepoRoot -Start (Get-Location)

# Normalize allow-list to absolute, case-insensitive compare
$allowAbs = @()
foreach ($f in $AllowFiles) {
    try {
        $p = Join-Path $repoRoot $f | Resolve-Path -ErrorAction Stop
        $allowAbs += $p.Path.ToLowerInvariant()
    } catch {
        # Ignore missing allow-list entries
    }
}

# Build regex patterns
# Hex colors that are not part of longer identifiers (avoid Angular template refs like #beforePicker)
$hexPattern = '(?i)#(?:[0-9a-f]{3}|[0-9a-f]{4}|[0-9a-f]{6}|[0-9a-f]{8})(?![0-9a-f])(?=[^a-zA-Z0-9_-]|$)'
# Numeric rgb/rgba only (avoids rgba(var(--x-rgb), a))
$rgbPattern = '\brgba?\(\s*(?:\d{1,3}%?\s*,\s*){2}\d{1,3}%?(?:\s*,\s*(?:0|1|0?\.\d+%?))?\s*\)'
# Numeric hsl/hsla only (avoids hsla(var(...)))
$hslPattern = '\bhsla?\(\s*\d{1,3}(?:deg|rad|grad|turn)?\s*,\s*\d{1,3}%\s*,\s*\d{1,3}%(?:\s*,\s*(?:0|1|0?\.\d+%?))?\s*\)'

# CSS named colors (CSS Color Module Level 4) except 'transparent' and 'currentColor' (allowed)
$namedColors = @(
    'aliceblue','antiquewhite','aqua','aquamarine','azure','beige','bisque','black','blanchedalmond','blue','blueviolet','brown','burlywood','cadetblue','chartreuse','chocolate','coral','cornflowerblue','cornsilk','crimson','cyan','darkblue','darkcyan','darkgoldenrod','darkgray','darkgreen','darkgrey','darkkhaki','darkmagenta','darkolivegreen','darkorange','darkorchid','darkred','darksalmon','darkseagreen','darkslateblue','darkslategray','darkslategrey','darkturquoise','darkviolet','deeppink','deepskyblue','dimgray','dimgrey','dodgerblue','firebrick','floralwhite','forestgreen','fuchsia','gainsboro','ghostwhite','gold','goldenrod','gray','green','greenyellow','grey','honeydew','hotpink','indianred','indigo','ivory','khaki','lavender','lavenderblush','lawngreen','lemonchiffon','lightblue','lightcoral','lightcyan','lightgoldenrodyellow','lightgray','lightgreen','lightgrey','lightpink','lightsalmon','lightseagreen','lightskyblue','lightslategray','lightslategrey','lightsteelblue','lightyellow','lime','limegreen','linen','magenta','maroon','mediumaquamarine','mediumblue','mediumorchid','mediumpurple','mediumseagreen','mediumslateblue','mediumspringgreen','mediumturquoise','mediumvioletred','midnightblue','mintcream','mistyrose','moccasin','navajowhite','navy','oldlace','olive','olivedrab','orange','orangered','orchid','palegoldenrod','palegreen','paleturquoise','palevioletred','papayawhip','peachpuff','peru','pink','plum','powderblue','purple','rebeccapurple','red','rosybrown','royalblue','saddlebrown','salmon','sandybrown','seagreen','seashell','sienna','silver','skyblue','slateblue','slategray','slategrey','snow','springgreen','steelblue','tan','teal','thistle','tomato','turquoise','violet','wheat','white','whitesmoke','yellow','yellowgreen'
)
# Named colors must appear in a color context (after :, =, (, or ,) to reduce false positives in class names, etc.
# Named colors not immediately followed by '-' (avoids matching white in white-space)
$namedPattern = '(?i)(?<=[:=\s\(,])(?:' + ($namedColors -join '|') + ')\b(?!-)'

$patterns = @($hexPattern, $rgbPattern, $hslPattern, $namedPattern)

# File globs to scan
$include = @('*.css','*.scss','*.sass','*.less','*.html','*.ts','*.tsx','*.js','*.jsx','*.svg')
$excludeDirPatterns = @('\\node_modules\\','\\dist\\','\\target\\','\\.git\\','\\coverage\\','\\.angular\\')

$scanRoot = Join-Path $repoRoot $Path
if (-not (Test-Path -LiteralPath $scanRoot)) {
    Write-Error "Path not found: $scanRoot"
    exit 2
}

$files = Get-ChildItem -LiteralPath $scanRoot -Include $include -Recurse -File -ErrorAction SilentlyContinue |
    Where-Object {
        $full = $_.FullName
        # Exclude common build/output dirs
        foreach ($p in $excludeDirPatterns) { if ($full -imatch [Regex]::Escape($p)) { return $false } }
        # Exclude allow-list files
        if ($allowAbs -contains $full.ToLowerInvariant()) { return $false }
        return $true
    }

$results = @()
foreach ($file in $files) {
    try {
    $fileMatches = Select-String -Path $file.FullName -Pattern $patterns -AllMatches -CaseSensitive:$false -Encoding UTF8 -ErrorAction SilentlyContinue
    foreach ($m in $fileMatches) {
            foreach ($mm in $m.Matches) {
                $text = $mm.Value
                $lineRaw = $m.Line
                # Skip allowed keywords explicitly
                if ($text -match '^(?i)(transparent|currentColor|inherit)$') { continue }
                # Skip rgba(var(--x), y) that may slip via namedPattern context
                if ($text -match '^\s*var\(') { continue }
                # Skip matches on commented lines
                $leading = $lineRaw.TrimStart()
                if ($leading.StartsWith('/*') -or $leading.StartsWith('//') -or $leading.StartsWith('*') -or $leading.StartsWith('<!--')) { continue }
                # Skip words (like 'white') when immediately followed by '-' in source (e.g., white-space)
                $nextChar = $null
                if ($lineRaw.Length -gt ($mm.Index + $mm.Length)) { $nextChar = $lineRaw[$mm.Index + $mm.Length] }
                if ($nextChar -eq '-') { continue }
                # Build a normalized relative path
                $rel = try { [System.IO.Path]::GetRelativePath($repoRoot, $file.FullName) } catch { $file.FullName.Replace($repoRoot, '') }
                if (-not $rel) { $rel = $file.FullName }
                $rel = $rel -replace '^^[\\/]+',''
                $rel = $rel -replace '\\','/'

                $results += [pscustomobject]@{
                    file   = $rel
                    line   = $m.LineNumber
                    column = $mm.Index + 1
                    match  = $text
                    lineText = $lineRaw.TrimEnd()
                }
            }
        }
    } catch {
        Write-Warning "Failed to scan $($file.FullName): $($_.Exception.Message)"
    }
}

if ($Json) {
    $results | ConvertTo-Json -Depth 4
} elseif ($Summary) {
    $byFile = $results | Group-Object file | Sort-Object Count -Descending
    foreach ($g in $byFile) {
        Write-Host ("{0} x {1}" -f $g.Count.ToString().PadLeft(3), $g.Name)
    }
    Write-Host ("Total findings: {0}" -f $results.Count)
} else {
    foreach ($r in $results) {
        Write-Host ("{0}:{1}:{2} -> {3}" -f $r.file, $r.line, $r.column, $r.match)
        Write-Host ("  {0}" -f $r.lineText)
    }
    Write-Host ("`nTotal findings: {0}" -f $results.Count)
}

# Exit non-zero if findings exist (useful for CI)
if ($results.Count -gt 0) { exit 1 } else { exit 0 }
