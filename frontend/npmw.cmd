@echo off

REM ##############################################################################
REM ##
REM ##  npmw script for Windows
REM ##
REM ##############################################################################

setlocal enabledelayedexpansion

REM Check if npm is available
where npm >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: npm is not installed or not in PATH.
    echo Please install Node.js and npm from https://nodejs.org/
    exit /b 1
)

REM Check if node is available
where node >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: node is not installed or not in PATH.
    echo Please install Node.js from https://nodejs.org/
    exit /b 1
)

REM Check Node.js version
for /f "tokens=1 delims=v" %%i in ('node --version') do set NODE_VERSION=%%i
for /f "tokens=1 delims=." %%i in ("!NODE_VERSION!") do set NODE_MAJOR=%%i
if !NODE_MAJOR! lss 18 (
    echo Warning: Node.js version 18 or higher is recommended. Current version: !NODE_VERSION!
)

REM Check npm version  
for /f %%i in ('npm --version') do set NPM_VERSION=%%i
for /f "tokens=1 delims=." %%i in ("!NPM_VERSION!") do set NPM_MAJOR=%%i
if !NPM_MAJOR! lss 8 (
    echo Warning: npm version 8 or higher is recommended. Current version: !NPM_VERSION!
)

REM Execute npm with all arguments
npm %*