#!/bin/bash
# SwiftHarvest Build Script for Linux/Mac
# This script builds the plugin JAR file

echo "========================================"
echo "Building SwiftHarvest Plugin"
echo "========================================"
echo ""

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "[ERROR] Maven is not installed or not in PATH"
    echo "Please install Maven from: https://maven.apache.org/download.cgi"
    exit 1
fi

# Check Java
if ! command -v java &> /dev/null; then
    echo "[ERROR] Java is not installed or not in PATH"
    echo "Please install JDK 21 from: https://adoptium.net/"
    exit 1
fi

echo "[INFO] Java and Maven detected"
java -version 2>&1 | head -n 1
echo ""

# Clean and build
echo "[INFO] Running: mvn clean package"
echo ""
mvn clean package

if [ $? -ne 0 ]; then
    echo ""
    echo "[ERROR] Build failed!"
    exit 1
fi

echo ""
echo "========================================"
echo "Build successful!"
echo "========================================"
echo ""
echo "Output JAR location:"
echo "  target/SwiftHarvest-*.jar"
echo ""
