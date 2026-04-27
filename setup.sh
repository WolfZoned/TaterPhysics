#!/bin/bash
# TaterPhysics Setup & Build Script - Linux/macOS

set -e

echo "=========================================="
echo "TaterPhysics Build Setup"
echo "=========================================="
echo ""

# Check Java
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed!"
    echo "Please install Java 11+ from: https://adoptium.net/"
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | head -1)
echo "✓ Found Java: $JAVA_VERSION"

# Check javac
if ! command -v javac &> /dev/null; then
    echo "❌ Java compiler (javac) is not installed!"
    echo "Please install JDK (not just JRE) from: https://adoptium.net/"
    exit 1
fi

echo "✓ Found Java Compiler"
echo ""

# Clean and setup directories
echo "🧹 Cleaning previous builds..."
rm -rf bin build .gradle

mkdir -p bin build/libs META-INF

# Create manifest
cat > META-INF/MANIFEST.MF << 'EOF'
Manifest-Version: 1.0
Main-Class: Main
Implementation-Title: TaterPhysics
Implementation-Version: 1.0.0
EOF

# Compile
echo "🔨 Compiling TaterPhysics..."
javac --release 21 -d bin src/*.java

if [ $? -ne 0 ]; then
    echo "❌ Compilation failed!"
    exit 1
fi

echo "✓ Compilation successful!"

# Create JAR
echo "📦 Creating JAR file..."
jar cfm build/libs/TaterPhysics-1.0.0-all.jar META-INF/MANIFEST.MF -C bin .

# Copy resources
if [ -d "src/images" ]; then
    cp -r src/images build/libs/ 2>/dev/null || true
fi

echo ""
echo "✅ Build complete!"
echo ""
echo "📍 Executable JAR created at: build/libs/TaterPhysics-1.0.0-all.jar"
echo "📦 Size: $(du -h build/libs/TaterPhysics-1.0.0-all.jar | cut -f1)"
echo ""
echo "▶️  To run: java -jar build/libs/TaterPhysics-1.0.0-all.jar"
echo ""




