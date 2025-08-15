param()

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

if (-not (Get-Command wt -ErrorAction SilentlyContinue)) {
  Write-Error "Windows Terminal (wt.exe) not found. Please install Windows Terminal or run the commands manually."
  exit 1
}

# Open two tabs: Backend and Frontend
$args = @(
  'new-tab', '--title', 'Backend',  '-d', $backendDir,  'pwsh', '-NoExit', '-NoLogo', '-ExecutionPolicy', 'Bypass', '-Command', 'mvn clean quarkus:dev',
  ';',
  'new-tab', '--title', 'Frontend', '-d', $frontendDir, 'pwsh', '-NoExit', '-NoLogo', '-ExecutionPolicy', 'Bypass', '-Command', 'npm start'
)

Start-Process -FilePath 'wt.exe' -ArgumentList $args | Out-Null
