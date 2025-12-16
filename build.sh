#!/bin/bash
# Build script for Audil Android App
# This script builds the Android project and handles common issues

set -e  # Exit on error

echo "================================"
echo "Audil Android Build Script"
echo "================================"
echo ""

# Set JAVA_HOME to Java 17
export JAVA_HOME="/usr/lib/jvm/java-17-openjdk-amd64"
export PATH=$JAVA_HOME/bin:$PATH

echo "Checking Java version..."
java -version 2>&1 | head -n 1

# Clean previous builds
echo ""
echo "Cleaning previous builds..."
./gradlew clean

# Build the project
echo ""
echo "Building the project..."
./gradlew assembleDebug

# Check if build was successful
if [ $? -eq 0 ]; then
    echo ""
    echo "================================"
    echo "✓ Build successful!"
    echo "================================"
    echo ""
    echo "APK location: app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    echo "To install on a connected device:"
    echo "  adb install app/build/outputs/apk/debug/app-debug.apk"
    echo ""
else
    echo ""
    echo "================================"
    echo "✗ Build failed!"
    echo "================================"
    echo ""
    echo "Check the error messages above for details."
    exit 1
fi
