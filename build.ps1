Write-Host "================================" -ForegroundColor Cyan
Write-Host "Audil Android Build Script (WSL Wrapper)" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "This script will build the project using the Linux environment in WSL." -ForegroundColor Yellow
Write-Host ""

# Check if WSL is available
if (Get-Command "wsl" -ErrorAction SilentlyContinue) {
    Write-Host "Running build.sh in WSL..." -ForegroundColor Green
    wsl ./build.sh
} else {
    Write-Host "Error: WSL (Windows Subsystem for Linux) is not found." -ForegroundColor Red
    Write-Host "Please install WSL to build this project." -ForegroundColor Red
    exit 1
}
