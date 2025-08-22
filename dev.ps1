param(
  [switch]$UseWindowsTerminal
)

$repoRoot   = if ($PSScriptRoot) { $PSScriptRoot } else { Split-Path -Parent $MyInvocation.MyCommand.Path }
$backendDir = Join-Path $repoRoot 'backend'
$frontendDir = Join-Path $repoRoot 'frontend'

function Ensure-DockerRunning {
  if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Error "Docker CLI ('docker') not found in PATH. Please install Docker Desktop."
    exit 1
  }

  # Quick check
  & docker info *> $null
  if ($LASTEXITCODE -eq 0) {
    Write-Host "Docker is running." -ForegroundColor Green
    return
  }

  Write-Host "Docker is not running. Starting Docker Desktop..." -ForegroundColor Yellow

  # Build candidate paths without Join-Path to avoid parameter binding issues
  $pf  = $env:ProgramFiles
  $pf86 = ${env:ProgramFiles(x86)}
  $candidatePaths = @()
  if ($pf)  { $candidatePaths += "$pf\Docker\Docker\Docker Desktop.exe" }
  if ($pf86){ $candidatePaths += "$pf86\Docker\Docker\Docker Desktop.exe" }
  $candidatePaths += @(
    'C:\\Program Files\\Docker\\Docker\\Docker Desktop.exe',
    'C:\\Program Files (x86)\\Docker\\Docker\\Docker Desktop.exe'
  )
  $candidatePaths = $candidatePaths | Where-Object { $_ -and (Test-Path $_) }

  $dockerDesktop = $candidatePaths | Select-Object -First 1
  if (-not $dockerDesktop) {
    Write-Error "Could not locate 'Docker Desktop.exe'. Please start Docker manually, then rerun this script."
    exit 1
  }

  try {
    Start-Process -FilePath $dockerDesktop | Out-Null
  } catch {
    Write-Error "Failed to start Docker Desktop: $($_.Exception.Message)"
    exit 1
  }

  # Wait until Docker responds
  Write-Host "Waiting for Docker to be ready (up to 2 minutes)..." -ForegroundColor Cyan
  $maxAttempts = 60
  for ($i = 1; $i -le $maxAttempts; $i++) {
    Start-Sleep -Seconds 2
    & docker info *> $null
    if ($LASTEXITCODE -eq 0) {
      Write-Host "Docker is ready." -ForegroundColor Green
      return
    }
  }

  Write-Error "Docker did not become ready within the timeout."
  exit 1
}

Ensure-DockerRunning

# If explicitly requested, use Windows Terminal tabs
if ($UseWindowsTerminal) {
  if (-not (Get-Command wt -ErrorAction SilentlyContinue)) {
    Write-Error "Windows Terminal (wt.exe) not found. Re-run without -UseWindowsTerminal to stream output in this terminal."
    exit 1
  }
  $args = @(
    'new-tab', '--title', 'Backend',  '-d', $backendDir,  'pwsh', '-NoExit', '-NoLogo', '-ExecutionPolicy', 'Bypass', '-Command', '.\mvnw quarkus:dev',
    ';',
    'new-tab', '--title', 'Frontend', '-d', $frontendDir, 'pwsh', '-NoExit', '-NoLogo', '-ExecutionPolicy', 'Bypass', '-Command', 'npm start'
  )
  Start-Process -FilePath 'wt.exe' -ArgumentList $args | Out-Null
  return
}

# Integrated streaming mode: run both processes and live-stream outputs with prefixes
function Start-DevProcess {
  param(
    [Parameter(Mandatory)] [string]$Name,
    [Parameter(Mandatory)] [string]$WorkingDirectory,
    [Parameter(Mandatory)] [string]$Command,
    [Parameter(Mandatory)] [string]$Args
  )

  $logDir = Join-Path $env:TEMP "librarie-dev-logs"
  if (-not (Test-Path $logDir)) { New-Item -ItemType Directory -Path $logDir | Out-Null }
  $outFile = Join-Path $logDir ("${Name}.out.log")
  $errFile = Join-Path $logDir ("${Name}.err.log")
  # Clear previous logs
  '' | Set-Content -Path $outFile -Encoding utf8
  '' | Set-Content -Path $errFile -Encoding utf8

  $psi = New-Object System.Diagnostics.ProcessStartInfo
  $psi.FileName = $Command
  $psi.Arguments = $Args
  $psi.WorkingDirectory = $WorkingDirectory
  $psi.RedirectStandardOutput = $true
  $psi.RedirectStandardError  = $true
  $psi.UseShellExecute = $false
  $psi.CreateNoWindow = $true

  $proc = New-Object System.Diagnostics.Process
  $proc.StartInfo = $psi
  [void]$proc.Start()

  # Async stream readers writing to files
  $stdoutTask = $proc.StandardOutput.BaseStream.CopyToAsync([System.IO.File]::Open($outFile, [System.IO.FileMode]::OpenOrCreate, [System.IO.FileAccess]::Write, [System.IO.FileShare]::Read))
  $stderrTask = $proc.StandardError.BaseStream.CopyToAsync([System.IO.File]::Open($errFile, [System.IO.FileMode]::OpenOrCreate, [System.IO.FileAccess]::Write, [System.IO.FileShare]::Read))

  return [pscustomobject]@{
    Name = $Name
    Process = $proc
    OutFile = $outFile
    ErrFile = $errFile
  }
}

function Start-LogTailJob {
  param(
    [Parameter(Mandatory)] [string]$Name,
    [Parameter(Mandatory)] [string]$File,
    [Parameter(Mandatory)] [ConsoleColor]$Color
  )

  $jobName = "tail:{0}:{1}" -f $Name, ([IO.Path]::GetFileName($File))
  Start-ThreadJob -Name $jobName -ScriptBlock {
    param($n, $f, $c)
    try {
      # Wait until file exists and is ready
      while (-not (Test-Path $f)) { Start-Sleep -Milliseconds 100 }
      Get-Content -Path $f -Encoding UTF8 -Wait | ForEach-Object {
        if ($_ -ne $null) {
          $old = $Host.UI.RawUI.ForegroundColor
          try { $Host.UI.RawUI.ForegroundColor = $c } catch {}
          Write-Host ("[{0}] {1}" -f $n, $_)
          try { $Host.UI.RawUI.ForegroundColor = $old } catch {}
        }
      }
    } catch {}
  } -ArgumentList $Name, $File, $Color | Out-Null
}

Write-Host "Starting dev services... (press Ctrl+C to stop)" -ForegroundColor Cyan

# Clean up any previous tail jobs from earlier runs in this session
Get-Job | Where-Object { $_.Name -like 'tail:*' } | Remove-Job -Force -ErrorAction SilentlyContinue | Out-Null

$backend = Start-DevProcess -Name 'backend' -WorkingDirectory $backendDir -Command 'pwsh' -Args "-NoLogo -NoProfile -Command .\mvnw quarkus:dev"
$frontend = Start-DevProcess -Name 'frontend' -WorkingDirectory $frontendDir -Command 'pwsh' -Args "-NoLogo -NoProfile -Command npm start"

# Tail logs concurrently
Start-LogTailJob -Name 'backend'  -File $backend.OutFile -Color ([ConsoleColor]::Yellow)
Start-LogTailJob -Name 'backend!' -File $backend.ErrFile -Color ([ConsoleColor]::DarkYellow)
Start-LogTailJob -Name 'frontend'  -File $frontend.OutFile -Color ([ConsoleColor]::Cyan)
Start-LogTailJob -Name 'frontend!' -File $frontend.ErrFile -Color ([ConsoleColor]::DarkCyan)

# Cleanup handler on Ctrl+C/exit
$script:cleanup = {
  try {
  if ($backend.Process -and -not $backend.Process.HasExited) { $backend.Process.Kill($true) }
  } catch {}
  try {
  if ($frontend.Process -and -not $frontend.Process.HasExited) { $frontend.Process.Kill($true) }
  } catch {}
  Get-Job | Where-Object { $_.Name -like 'tail:*' } | Stop-Job -Force -ErrorAction SilentlyContinue | Out-Null
}
Register-EngineEvent PowerShell.Exiting -Action $cleanup | Out-Null
try {
  # Wait while either process is running; refresh every 500ms
  while (-not ($backend.Process.HasExited -and $frontend.Process.HasExited)) {
    Start-Sleep -Milliseconds 500
  }
} finally {
  & $cleanup
}

Write-Host "Dev services stopped." -ForegroundColor Green
