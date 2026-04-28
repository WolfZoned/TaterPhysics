#!/bin/bash
# TaterPhysics Build Script - Linux/macOS
# Interactive builder: JAR, EXE, or Bundled EXE

set -e

echo "=========================================="
echo "TaterPhysics Builder"
echo "=========================================="
echo ""

# Check Java
if ! command -v java &> /dev/null; then
    echo "Java is not installed!"
    echo "Please install Java 25+ from: https://adoptium.net/"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -1)
echo "Found Java: $JAVA_VERSION"

if ! command -v javac &> /dev/null; then
    echo "Java compiler (javac) is not installed!"
    echo "Please install JDK from: https://adoptium.net/"
    exit 1
fi

echo "Found Java Compiler"
echo ""

# Compile JAR (always needed)
echo "Cleaning previous builds..."
rm -rf bin build .gradle
mkdir -p bin build META-INF

cat > META-INF/MANIFEST.MF << 'EOF'
Manifest-Version: 1.0
Main-Class: Main
Implementation-Title: TaterPhysics
Implementation-Version: 1.0.0
EOF

echo "Compiling TaterPhysics..."
javac --release 21 -d bin src/*.java

if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

echo "Compilation successful!"

if [ -d "src/images" ]; then
    cp -r src/images bin/
fi

echo "Creating JAR file..."
jar cfm build/TaterPhysics.jar META-INF/MANIFEST.MF -C bin .
echo "JAR created: build/TaterPhysics.jar ($(du -h build/TaterPhysics.jar | cut -f1))"
echo ""

# Functions
build_lightweight_exe() {
    echo "Building lightweight EXE..."
    if ! command -v launch4j &> /dev/null; then
        echo "Error: launch4j not found"
        echo "Install via paru: paru -S launch4j"
        exit 1
    fi
    launch4j launch4j-config.xml
    echo "Lightweight EXE created: build/TaterPhysics.exe"
}

build_bundled_exe() {
    echo "Setting up Java 25 JRE..."
    mkdir -p build/jre
    cd build

    if ! command -v curl &> /dev/null && ! command -v wget &> /dev/null; then
        echo "Error: curl or wget not found"
        exit 1
    fi

    if command -v curl &> /dev/null; then
        curl -L -o java-jre.zip "https://api.adoptium.net/v3/binary/latest/25/ga/windows/x64/jre/hotspot/normal/eclipse?project=jdk"
    else
        wget -q -O java-jre.zip "https://api.adoptium.net/v3/binary/latest/25/ga/windows/x64/jre/hotspot/normal/eclipse?project=jdk"
    fi

    if [ ! -f "java-jre.zip" ]; then
        echo "Error: Java download failed"
        exit 1
    fi

    echo "Extracting Java..."
    unzip -q java-jre.zip
    EXTRACTED_DIR=$(ls -d jdk-* OpenJDK* 2>/dev/null | head -1)
    if [ -z "$EXTRACTED_DIR" ]; then
        echo "Error: Could not find extracted JRE"
        exit 1
    fi
    rm -rf jre
    mv "$EXTRACTED_DIR" jre
    rm -f java-jre.zip
    cd ..

    echo "Building EXE with bundled Java..."
    if ! command -v launch4j &> /dev/null; then
        echo "Error: launch4j not found"
        echo "Install via paru: paru -S launch4j"
        exit 1
    fi
    launch4j launch4j-config-bundled.xml
    echo "Bundled EXE created: build/TaterPhysics.exe"
}

create_distribution_zip() {
    if ! command -v zip &> /dev/null; then
        echo "Error: zip command not found"
        echo "Please install zip:"
        echo "  Arch: sudo pacman -S zip"
        echo "  Ubuntu: sudo apt install zip"
        echo "  Fedora: sudo dnf install zip"
        echo "  macOS: brew install zip"
        exit 1
    fi

    echo "Creating distribution package..."
    cd build
    zip -r -q TaterPhysics-exe-bundled.zip TaterPhysics.exe jre/
    cd ..
    echo "✓ Distribution package created: build/TaterPhysics-exe-bundled.zip"
}

# Ask user what to build
echo "What would you like to build?"
echo "1) JAR only"
echo "2) Lightweight EXE (requires Java 25 on user machine)"
echo "3) Bundled EXE (self-contained, includes Java)"
echo "4) All of the above"
echo ""

read -p "Enter your choice (1-4): " choice

case $choice in
    1)
        echo "JAR package complete!"
        echo "Output: build/TaterPhysics.jar"
        ;;
    2)
        echo ""
        build_lightweight_exe
        rm -rf bin
        ;;
    3)
        echo ""
        build_bundled_exe
        create_distribution_zip
        rm -f build/TaterPhysics.exe
        rm -rf bin
        ;;
    4)
        echo ""
        echo "[1/2] Building lightweight EXE..."
        build_lightweight_exe
        rm build/TaterPhysics.exe

        echo ""
        echo "[2/2] Building bundled EXE..."
        build_bundled_exe
        create_distribution_zip

        rm -rf bin
        echo ""
        echo "JAR: build/TaterPhysics.jar"
        echo "Bundled distribution: build/TaterPhysics-exe-bundled.zip"
        ;;
    *)
        echo "Invalid choice"
        exit 1
        ;;
esac

echo ""
echo "=========================================="
echo "Build complete!"
echo "=========================================="

