# Development environment setup script for Librarie project
# This script configures TestContainers reuse for faster development

$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$RepoRoot = Split-Path -Parent $ScriptDir
$TestContainersFile = Join-Path $env:USERPROFILE ".testcontainers.properties"
$TemplateFile = Join-Path $RepoRoot "docs\testcontainers.properties.template"

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "Librarie Development Setup" -ForegroundColor Cyan
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

# Check if Docker is running
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "❌ Docker is not installed or not in PATH" -ForegroundColor Red
    Write-Host "   Please install Docker Desktop and try again" -ForegroundColor Yellow
    exit 1
}

& docker info *> $null
if ($LASTEXITCODE -ne 0) {
    Write-Host "❌ Docker is not running" -ForegroundColor Red
    Write-Host "   Please start Docker Desktop and try again" -ForegroundColor Yellow
    exit 1
}

Write-Host "✅ Docker is running" -ForegroundColor Green
Write-Host ""

# Check if TestContainers config already exists
if (Test-Path $TestContainersFile) {
    Write-Host "⚠️  TestContainers configuration already exists at:" -ForegroundColor Yellow
    Write-Host "   $TestContainersFile" -ForegroundColor Gray
    Write-Host ""
    $response = Read-Host "Do you want to overwrite it? (y/N)"
    if ($response -notmatch '^[Yy]$') {
        Write-Host "Setup cancelled. Existing configuration preserved." -ForegroundColor Yellow
        exit 0
    }
}

# Copy template file
if (-not (Test-Path $TemplateFile)) {
    Write-Host "❌ Template file not found at:" -ForegroundColor Red
    Write-Host "   $TemplateFile" -ForegroundColor Gray
    exit 1
}

Copy-Item $TemplateFile $TestContainersFile -Force
Write-Host "✅ TestContainers configuration created at:" -ForegroundColor Green
Write-Host "   $TestContainersFile" -ForegroundColor Gray
Write-Host ""

# Display the configuration
Write-Host "Configuration content:" -ForegroundColor Cyan
Write-Host "---" -ForegroundColor Gray
Get-Content $TestContainersFile | ForEach-Object { Write-Host $_ -ForegroundColor Gray }
Write-Host "---" -ForegroundColor Gray
Write-Host ""

Write-Host "✅ Setup complete!" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Start the backend: cd backend && .\mvnw quarkus:dev" -ForegroundColor White
Write-Host "2. PostgreSQL containers will be reused between restarts" -ForegroundColor White
Write-Host "3. Check the logs for 'Reusing container' messages" -ForegroundColor White
Write-Host ""
Write-Host "Performance improvements:" -ForegroundColor Cyan
Write-Host "- PostgreSQL startup: ~0.4s (with reuse) vs ~2s (without reuse)" -ForegroundColor White
Write-Host "- Overall startup: ~27s (vs ~30s without reuse)" -ForegroundColor White
Write-Host ""
Write-Host "For more information, see: docs\development-setup.md" -ForegroundColor Cyan
