@echo off
REM TaterPhysics Demo Launcher - Windows

REM Check if Java is installed
java -version >nul 2>&1
if errorlevel 1 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java 11 or later
    echo Visit: https://adoptium.net/ or https://www.oracle.com/java/technologies/downloads/
    pause
    exit /b 1
)

echo Starting TaterPhysics Demo...
echo.

REM Find the built JAR file
set "JAR_FILE=build\TaterPhysics.jar"

if not exist "%JAR_FILE%" (
    echo JAR file not found at %JAR_FILE%
    echo Building project first...

    if exist "build.bat" (
        (echo 1) | call build.bat
    ) else (
        echo Error: build.bat not found. Cannot build project automatically.
        pause
        exit /b 1
    )
)

REM Run the application
java -jar "%JAR_FILE%"
pause


