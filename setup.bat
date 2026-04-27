@echo off
REM TaterPhysics Setup & Build Script - Windows

setlocal enabledelayedexpansion

echo ==========================================
echo TaterPhysics Build Setup
echo ==========================================
echo.

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo [!] Java is not installed!
    echo Please install Java 11+ from: https://adoptium.net/
    pause
    exit /b 1
)

for /f "tokens=*" %%i in ('java -version 2^>^&1 ^| findstr /R ".*"') do set JAVA_VERSION=%%i
echo [OK] Found Java: %JAVA_VERSION%

REM Check javac
javac -version >nul 2>&1
if errorlevel 1 (
    echo [!] Java compiler (javac) is not installed!
    echo Please install JDK (not just JRE) from: https://adoptium.net/
    pause
    exit /b 1
)

echo [OK] Found Java Compiler
echo.

REM Clean and setup directories
echo [*] Cleaning previous builds...
if exist bin rmdir /s /q bin
if exist build rmdir /s /q build
if exist META-INF rmdir /s /q META-INF

mkdir bin build\libs META-INF

REM Create manifest
(
echo Manifest-Version: 1.0
echo Main-Class: Main
echo Implementation-Title: TaterPhysics
echo Implementation-Version: 1.0.0
) > META-INF\MANIFEST.MF

REM Compile
echo [*] Compiling TaterPhysics...
setlocal enabledelayedexpansion
for /r src %%f in (*.java) do (
    set "files=!files! "%%f""
)
javac --release 21 -d bin !files!

if errorlevel 1 (
    echo [!] Compilation failed!
    pause
    exit /b 1
)

echo [OK] Compilation successful!

REM Create JAR
echo [*] Creating JAR file...
jar cfm build\libs\TaterPhysics-1.0.0-all.jar META-INF\MANIFEST.MF -C bin .

REM Copy resources
if exist src\images (
    xcopy src\images build\libs\images /E /I /Y >nul 2>&1
)

echo.
echo [OK] Build complete!
echo.
echo [*] JAR created at: build\libs\TaterPhysics-1.0.0-all.jar
echo.
echo [*] To run: java -jar build\libs\TaterPhysics-1.0.0-all.jar
echo.
pause



