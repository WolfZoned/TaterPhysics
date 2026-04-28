@echo off
REM TaterPhysics Build Script - Windows
REM Interactive builder: JAR, EXE, or Bundled EXE

setlocal enabledelayedexpansion

echo ==========================================
echo TaterPhysics Builder
echo ==========================================
echo.

REM Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo Java is not installed!
    echo Please install Java 25+ from: https://adoptium.net/
    pause
    exit /b 1
)

for /f "tokens=*" %%i in ('java -version 2^>^&1 ^| findstr /R ".*"') do set JAVA_VERSION=%%i
echo Found Java: %JAVA_VERSION%

javac -version >nul 2>&1
if errorlevel 1 (
    echo Java compiler (javac) is not installed!
    echo Please install JDK from: https://adoptium.net/
    pause
    exit /b 1
)

echo Found Java Compiler
echo.

REM Clean and compile JAR (always needed)
echo Cleaning previous builds...
if exist bin rmdir /s /q bin
if exist build rmdir /s /q build
if exist META-INF rmdir /s /q META-INF

mkdir bin build META-INF

(
echo Manifest-Version: 1.0
echo Main-Class: Main
echo Implementation-Title: TaterPhysics
echo Implementation-Version: 1.0.0
) > META-INF\MANIFEST.MF

echo Compiling TaterPhysics...
setlocal enabledelayedexpansion
for /r src %%f in (*.java) do (
    set "files=!files! "%%f""
)
javac --release 21 -d bin !files!

if errorlevel 1 (
    echo Compilation failed!
    pause
    exit /b 1
)

echo Compilation successful!

if exist src\images (
    xcopy src\images bin\images /E /I /Y >nul 2>&1
)

echo Creating JAR file...
jar cfm build\TaterPhysics.jar META-INF\MANIFEST.MF -C bin .
echo JAR created: build\TaterPhysics.jar
echo.

REM Ask user what to build
echo What would you like to build?
echo 1) JAR only
echo 2) Lightweight EXE (requires Java 25 on user machine)
echo 3) Bundled EXE (self-contained, includes Java)
echo 4) All of the above
echo.

set /p choice="Enter your choice (1-4): "

if "%choice%"=="1" (
    echo JAR package complete!
    echo Output: build\libs\TaterPhysics-1.0.0-all.jar
    goto end
)

if "%choice%"=="2" (
    call :build_lightweight
    goto end
)

if "%choice%"=="3" (
    call :build_bundled
    del build\TaterPhysics.exe
    goto end
)

if "%choice%"=="4" (
    echo.
    echo [1/2] Building lightweight EXE...
    call :build_lightweight
    del build\TaterPhysics.exe

    echo.
    echo [2/2] Building bundled EXE...
    call :build_bundled

    rmdir /s /q bin
    echo.
    echo JAR: build\TaterPhysics.jar
    echo Bundled distribution: build\TaterPhysics-exe-bundled.zip
    goto end
)

echo Invalid choice
pause
exit /b 1

REM Subroutines
:build_lightweight
echo.
echo Building lightweight EXE...
launch4j launch4j-config.xml
if errorlevel 1 (
    echo Error: launch4j failed
    pause
    exit /b 1
)
rmdir /s /q bin
echo Lightweight EXE created: build\TaterPhysics.exe
exit /b 0

:build_bundled
echo.
echo Building bundled EXE...
echo Setting up Java 25 JRE...
if not exist "build\jre" mkdir build\jre
cd build

echo Downloading Java 25 JRE...
powershell -NoProfile -Command "& {[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri 'https://github.com/adoptium/temurin25-binaries/releases/download/jdk-25.0.1%%2B1/OpenJDK25U-jre_x64_windows_hotspot_25.0.1_1.zip' -OutFile '%cd%\java-jre.zip'}"

if not exist "java-jre.zip" (
    echo Error: Download failed
    cd ..
    pause
    exit /b 1
)

echo Extracting Java...
powershell -NoProfile -Command "& {Expand-Archive -Path '%cd%\java-jre.zip' -DestinationPath '%cd%'}"

for /d %%D in (jdk-* OpenJDK*) do (
    set "EXTRACTED_DIR=%%D"
    goto found_bundled
)

echo Error: Could not find extracted JRE
del java-jre.zip
cd ..
pause
exit /b 1

:found_bundled
if exist "jre" rmdir /s /q jre
move "!EXTRACTED_DIR!" "jre"
del java-jre.zip
cd ..

echo Building EXE with bundled Java...
launch4j launch4j-config-bundled.xml
if errorlevel 1 (
    echo Error: launch4j failed
    pause
    exit /b 1
)

echo Creating distribution package...
cd build
powershell -NoProfile -Command "& {Compress-Archive -Path 'TaterPhysics.exe','jre' -DestinationPath 'TaterPhysics-exe-bundled.zip' -Force}"
cd ..

rmdir /s /q bin
echo Distribution package created: build\TaterPhysics-exe-bundled.zip
exit /b 0

:end
echo.
echo ==========================================
echo Build complete!
echo ==========================================
echo.
pause

