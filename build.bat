@echo off
REM SwiftHarvest Build Script for Windows
REM This script builds the plugin JAR file

echo ========================================
echo Building SwiftHarvest Plugin
echo ========================================
echo.

REM Check if Maven is installed
where mvn >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven is not installed or not in PATH
    echo Please install Maven from: https://maven.apache.org/download.cgi
    pause
    exit /b 1
)

REM Check Java
java -version >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java is not installed or not in PATH
    echo Please install JDK 21 from: https://adoptium.net/
    pause
    exit /b 1
)

echo [INFO] Java and Maven detected
echo.

REM Clean and build
echo [INFO] Running: mvn clean package
echo.
call mvn clean package

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Build failed!
    pause
    exit /b 1
)

echo.
echo ========================================
echo Build successful!
echo ========================================
echo.
echo Output JAR location:
echo   target\SwiftHarvest-*.jar
echo.
pause
