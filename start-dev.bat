@echo off
title Generatore Orario - Dev Mode
echo ============================================
echo   DEV MODE - Backend + Frontend separati
echo ============================================
echo.
echo Backend: http://localhost:8080
echo Frontend: http://localhost:5173
echo.

:: Start backend in background
start "Backend" cmd /c "cd /d "%~dp0backend" && mvnw.cmd spring-boot:run"

:: Install and start frontend
cd /d "%~dp0frontend"
call npm install --silent
start "Frontend" cmd /c "npm run dev"

echo Entrambi i server sono in avvio.
echo Chiudi le finestre dei terminali per fermarli.
pause
