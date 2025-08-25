# PowerShell Dev Orchestrator for Librarie
# - Cleans logs
# - Ensures Docker is running
# - Starts backend (Quarkus dev) and frontend (Angular start) with live logs to console and files

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
Import-Module ThreadJob -ErrorAction SilentlyContinue

function Write-Section($text) {
	Write-Host "`n==== $text ==== `n" -ForegroundColor Cyan
}

function New-LogsDir {
	$logsDir = Join-Path $PSScriptRoot 'logs'
	# Stop lingering jobs to release any file locks on logs
	Get-Job -Name 'backend-dev','frontend-dev' -ErrorAction SilentlyContinue | % {
		try { Stop-Job $_ -ErrorAction SilentlyContinue } catch {}
		try { Remove-Job $_ -ErrorAction SilentlyContinue } catch {}
	}
	if (Test-Path $logsDir) {
		try {
			Remove-Item -Path $logsDir -Recurse -Force -ErrorAction Stop
		} catch {
			Write-Warning "Failed to remove logs directory: $($_.Exception.Message). Attempting to empty it."
			Get-ChildItem -Path $logsDir -Force -Recurse | Remove-Item -Recurse -Force -ErrorAction SilentlyContinue
		}
	}
	New-Item -ItemType Directory -Path $logsDir -Force | Out-Null
	return $logsDir
}

function Test-DockerReady {
	try {
		$null = docker info --format '{{json .ServerVersion}}' 2>$null
		return $true
	} catch {
		return $false
	}
}

function Start-DockerDesktopIfNeeded {
	if (Test-DockerReady) { return $true }

	Write-Section 'Docker not ready. Attempting to start Docker Desktop'

	# Try Windows service first
	$svc = Get-Service -Name 'com.docker.service' -ErrorAction SilentlyContinue
	if ($null -ne $svc -and $svc.Status -ne 'Running') {
		try {
			Start-Service -Name 'com.docker.service'
		} catch {
			Write-Warning "Could not start com.docker.service: $($_.Exception.Message)"
		}
	}

	# Try launching Docker Desktop app if service approach not sufficient
	$candidates = @(
		(Join-Path $Env:ProgramFiles 'Docker\Docker\Docker Desktop.exe'),
		(Join-Path ${Env:ProgramFiles(x86)} 'Docker\Docker\Docker Desktop.exe')
	)
	foreach ($exe in $candidates) {
		if ($exe -and (Test-Path $exe)) {
			Write-Host "Starting Docker Desktop: $exe"
			try {
				Start-Process -FilePath $exe | Out-Null
			} catch {
				Write-Warning "Failed to start Docker Desktop ($exe): $($_.Exception.Message)"
			}
			break
		}
	}

	# Wait up to 2 minutes for Docker to become ready
	$timeoutSec = 120
	$sw = [Diagnostics.Stopwatch]::StartNew()
	while ($sw.Elapsed.TotalSeconds -lt $timeoutSec) {
		if (Test-DockerReady) {
			Write-Host 'Docker is ready.' -ForegroundColor Green
			return $true
		}
		Start-Sleep -Seconds 3
		Write-Host 'Waiting for Docker to be ready…'
	}

	Write-Warning 'Docker did not become ready within the expected time. Continuing anyway.'
	return $false
}

function Start-BackendDev {
	param(
		[Parameter(Mandatory)] [string] $LogPath
	)
	$backendDir = Join-Path $PSScriptRoot 'backend'
	if (-not (Test-Path (Join-Path $backendDir 'mvnw.cmd'))) {
		throw "Backend wrapper not found at $backendDir\mvnw.cmd"
	}

	$sb = {
		param($backendDir,$logPath)
		Set-Location -Path $backendDir
	"[backend] starting at $(Get-Date -Format o) in $backendDir" | Out-File -FilePath $logPath -Append -Encoding UTF8
		& .\mvnw.cmd quarkus:dev 2>&1 |
			Tee-Object -FilePath $logPath -Encoding UTF8 |
			Out-Host
	}

	# Use ThreadJob to stream host output to current console
	Start-ThreadJob -Name 'backend-dev' -StreamingHost $Host -ScriptBlock $sb -ArgumentList $backendDir, $LogPath
}

function Start-FrontendDev {
	param(
		[Parameter(Mandatory)] [string] $LogPath
	)
	$frontendDir = Join-Path $PSScriptRoot 'frontend'
	if (-not (Test-Path (Join-Path $frontendDir 'package.json'))) {
		throw "Frontend package.json not found at $frontendDir"
	}

	$sb = {
		param($frontendDir,$logPath)
		Set-Location -Path $frontendDir
	"[frontend] starting at $(Get-Date -Format o) in $frontendDir" | Out-File -FilePath $logPath -Append -Encoding UTF8
		# Install deps: prefer ci when lockfile exists, otherwise fallback to install
		if (Test-Path -Path 'package-lock.json') {
			& npm ci 2>&1 |
				Tee-Object -FilePath $logPath -Encoding UTF8 |
				Out-Host
		} else {
			"[frontend] package-lock.json missing; running npm install instead of npm ci" |
				Out-File -FilePath $logPath -Append -Encoding UTF8
			& npm install 2>&1 |
				Tee-Object -FilePath $logPath -Encoding UTF8 |
				Out-Host
		}

	# Start Angular using workspace task-equivalent command (proxy-friendly)
	& npm run start --prefix "$frontendDir" 2>&1 |
			Tee-Object -FilePath $logPath -Encoding UTF8 |
			Out-Host
	}

	Start-ThreadJob -Name 'frontend-dev' -StreamingHost $Host -ScriptBlock $sb -ArgumentList $frontendDir, $LogPath
}

# --- Main ---
Write-Section 'Preparing logs'
$logsDir = New-LogsDir
$backendLog = Join-Path $logsDir 'backend.log'
$frontendLog = Join-Path $logsDir 'frontend.log'

Write-Section 'Checking Docker'
$null = Start-DockerDesktopIfNeeded

Write-Section 'Starting Backend (Quarkus dev)'
$backendJob = Start-BackendDev -LogPath $backendLog

Write-Section 'Starting Frontend (Angular)'
$frontendJob = Start-FrontendDev -LogPath $frontendLog

Write-Host "Backend log:    $backendLog"
Write-Host "Frontend log:   $frontendLog"
Write-Host "Jobs started. Press Ctrl+C to stop."

try {
	while ($true) {
		# Keep the script alive while jobs run; report once if any job exits
		$jobs = Get-Job -Name 'backend-dev','frontend-dev' -ErrorAction SilentlyContinue
		if (-not $jobs) { break }

		$endedNow = $jobs | Where-Object { $_.State -in 'Completed','Failed','Stopped' }
		foreach ($j in $endedNow) {
			Write-Warning "Job '$($j.Name)' finished with state $($j.State)."
			Receive-Job -Job $j -Keep -ErrorAction SilentlyContinue | Out-Null
			# Remove finished jobs so we don't repeatedly warn
			Remove-Job -Job $j -ErrorAction SilentlyContinue
		}

		$running = Get-Job -Name 'backend-dev','frontend-dev' -ErrorAction SilentlyContinue | Where-Object { $_.State -eq 'Running' -or $_.State -eq 'NotStarted' }
		if (-not $running -or @($running).Count -eq 0) { break }

		Wait-Job -Job $running -Any -Timeout 2 -ErrorAction SilentlyContinue | Out-Null
	}
} finally {
	Write-Host 'Stopping jobs…'
	Get-Job -Name 'backend-dev','frontend-dev' -ErrorAction SilentlyContinue | Stop-Job -ErrorAction SilentlyContinue
	Get-Job -Name 'backend-dev','frontend-dev' -ErrorAction SilentlyContinue | Remove-Job -ErrorAction SilentlyContinue
}

