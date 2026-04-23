@echo off
title Campus Fast Transfer Launcher
cd /d "%~dp0"

echo Starting Campus Fast Transfer...
echo Project folder: %cd%
echo.

powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0start-local.ps1"
set EXIT_CODE=%ERRORLEVEL%

echo.
if not "%EXIT_CODE%"=="0" (
    echo Startup failed. Exit code: %EXIT_CODE%
    echo Please keep this window open and read the error message above.
    pause
    exit /b %EXIT_CODE%
)

echo Startup command finished.
echo If the browser did not open automatically, visit:
echo http://127.0.0.1:8080/login
echo.
pause
