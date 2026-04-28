#!/bin/bash
# TaterPhysics Demo Launcher - Linux/macOS

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed or not in PATH"
    echo "Please install Java 11 or later"
    echo "Visit: https://adoptium.net/ or https://www.oracle.com/java/technologies/downloads/"
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F. 'NR==1 {print $1}' | grep -oE '[0-9]+')
if [ -z "$JAVA_VERSION" ] || [ "$JAVA_VERSION" -lt 11 ]; then
    echo "Warning: Unable to verify Java version, but proceeding anyway..."
fi

echo "Starting TaterPhysics Demo..."

# Find the built JAR file
JAR_FILE="build/TaterPhysics.jar"

if [ ! -f "$JAR_FILE" ]; then
    echo "JAR file not found at $JAR_FILE"
    echo "Building project first..."

    # Check if build script exists
    if [ -f "./build.sh" ]; then
        bash ./build.sh << EOF
1
EOF
    else
        echo "Error: build.sh not found. Cannot build project automatically."
        echo "Please run ./build.sh to build the project."
        exit 1
    fi
fi

# Run the application
echo ""
java -jar "$JAR_FILE"


