@echo off
echo ========================================================
echo       Starting SmartCart Application via Docker
echo ========================================================
echo.

docker --version >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Docker is not installed or not running.
    echo Please install Docker Desktop for Windows from https://www.docker.com/products/docker-desktop/
    echo and ensure it is running before executing this script.
    echo.
    pause
    exit /b
)

echo [INFO] Docker found. Building and starting application...
docker compose up --build

echo.
pause
