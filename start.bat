@echo off
title Generatore Orario Scolastico
echo ============================================
echo   GENERATORE ORARIO SCOLASTICO
echo ============================================
echo.

:: Check Java
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERRORE: Java non trovato. Installa Java 17 o superiore.
    echo Scarica da: https://adoptium.net/
    pause
    exit /b 1
)

:: Check Node.js
node -v >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ERRORE: Node.js non trovato. Installa Node.js 18 o superiore.
    echo Scarica da: https://nodejs.org/
    pause
    exit /b 1
)

echo [1/4] Installazione dipendenze frontend...
cd /d "%~dp0frontend"
call npm install --silent
if %ERRORLEVEL% NEQ 0 (
    echo ERRORE: npm install fallito
    pause
    exit /b 1
)

echo [2/4] Build frontend...
call npm run build --silent
if %ERRORLEVEL% NEQ 0 (
    echo ERRORE: build frontend fallita
    pause
    exit /b 1
)

echo [3/4] Copia frontend nel backend...
cd /d "%~dp0"
if exist "backend\src\main\resources\static" rmdir /s /q "backend\src\main\resources\static"
xcopy /s /e /q /y "frontend\dist\*" "backend\src\main\resources\static\" >nul

echo [4/4] Avvio server...
echo.
echo ============================================
echo   Server in avvio su http://localhost:8080
echo   Premi Ctrl+C per fermare
echo ============================================
echo.

cd /d "%~dp0backend"
call mvnw.cmd spring-boot:run
pause
