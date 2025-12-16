# Building on Windows - Complete Guide

## Prerequisites

Before building this Android project on Windows, ensure you have:

1. **Android Studio** (latest stable version recommended)
   - Download from: https://developer.android.com/studio

2. **Java Development Kit (JDK) 17**
   - Download from: https://adoptium.net/temurin/releases/?version=17
   - Or use Android Studio's embedded JDK (recommended)

3. **Android SDK** (installed via Android Studio)
   - Android Studio will prompt you to install this on first launch

## Step-by-Step Build Instructions

### 1. Configure Java 17

The project requires Java 17 due to Android Gradle Plugin 8.3.0.

**Option A: Use Android Studio's Embedded JDK (Recommended)**

1. Open Android Studio
2. Go to `File` → `Settings` (or `Ctrl+Alt+S`)
3. Navigate to `Build, Execution, Deployment` → `Build Tools` → `Gradle`
4. Set `Gradle JDK` to `Embedded JDK (JetBrains Runtime version 17)`

**Option B: Configure gradle.properties**

1. Open `gradle.properties` in the project root
2. Find the Java version section (around line 14)
3. Uncomment and update the Java home path:
   ```properties
   org.gradle.java.home=C:/Program Files/Android Studio/jbr
   ```
   Or if you installed JDK 17 separately:
   ```properties
   org.gradle.java.home=C:/Program Files/Eclipse Adoptium/jdk-17.0.x-hotspot
   ```

4. **IMPORTANT**: Remove or comment out the WSL/Linux line:
   ```properties
   # org.gradle.java.home=/usr/lib/jvm/java-17-openjdk-amd64
   ```

### 2. Configure Android SDK Path

1. Open `local.properties` in the project root
2. Update the SDK path to your Windows installation:
   ```properties
   sdk.dir=C:/Users/YOUR_USERNAME/AppData/Local/Android/Sdk
   ```
   Or use double backslashes:
   ```properties
   sdk.dir=C:\\Users\\YOUR_USERNAME\\AppData\\Local\\Android\\Sdk
   ```

3. **IMPORTANT**: Comment out the WSL path:
   ```properties
   # sdk.dir=/mnt/c/Users/nicol/AppData/Local/Android/Sdk
   ```

### 3. Open Project in Android Studio

1. Launch Android Studio
2. Click `File` → `Open`
3. Navigate to the project folder and select it
4. Click `OK`
5. Wait for Gradle sync to complete

**Note**: The sherpa-onnx dependency will be automatically downloaded from Maven Central during the sync. No manual installation needed!

### 4. Sync and Build

1. Android Studio will automatically start syncing. If not, click the elephant icon (🐘) or:
   - `File` → `Sync Project with Gradle Files`

2. Once sync completes, build the project:
   - `Build` → `Make Project` (or `Ctrl+F9`)

3. Or build from command line:
   ```cmd
   gradlew.bat clean build
   ```

## Common Issues and Solutions

### Issue: "Android Gradle plugin requires Java 17"

**Solution**: Follow Step 1 above to configure Java 17.

### Issue: "SDK location not found"

**Solution**: 
1. Ensure Android SDK is installed via Android Studio
2. Update `local.properties` with correct Windows path (see Step 2)
3. Or let Android Studio generate it automatically by opening the project

### Issue: Sherpa-ONNX dependency issues

**Solution**: The project uses a Maven Central wrapper which should download automatically. If you encounter issues:
1. Ensure you have internet connection for Gradle to download dependencies
2. Try `Build` → `Clean Project` then rebuild
3. For the official version, see `SHERPA_ONNX_SETUP.md`

### Issue: Gradle sync fails with "Unsupported class file major version"

**Solution**: You're using the wrong Java version. Ensure Java 17 is configured (see Step 1).

### Issue: Build is very slow

**Solution**: The `gradle.properties` file already includes performance optimizations:
- Increased heap size (2GB)
- Parallel builds enabled
- Gradle caching enabled

If still slow, you can increase heap size further:
```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
```

### Issue: "Cannot resolve symbol" errors in Android Studio

**Solution**:
1. `File` → `Invalidate Caches / Restart`
2. Choose `Invalidate and Restart`
3. Wait for re-indexing to complete

## Building APK

Once the project builds successfully:

1. **Debug APK**:
   - `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
   - APK location: `app/build/outputs/apk/debug/`

2. **Release APK** (requires signing):
   - `Build` → `Generate Signed Bundle / APK`
   - Follow the wizard to create/use a keystore

## Running on Device/Emulator

1. **Emulator**:
   - `Tools` → `Device Manager`
   - Create a new virtual device or use existing
   - Click the play button

2. **Physical Device**:
   - Enable Developer Options on your Android device
   - Enable USB Debugging
   - Connect via USB
   - Select your device from the device dropdown
   - Click the play button

## Key Files Modified for Windows Compatibility

The following files have been updated to work on Windows:

1. **`gradle.properties`**
   - Added Java 17 configuration
   - Added performance optimizations
   - Added Windows path examples

2. **`local.properties`**
   - Added Windows SDK path examples
   - Documented path format options

3. **`app/build.gradle.kts`**
   - Added Maven Central dependency for sherpa-onnx
   - No manual AAR installation needed!

4. **`build.gradle.kts`**
   - Fixed deprecated `buildDir` API (Gradle 8.x compatibility)

## Sherpa-ONNX Library

The project now uses a Maven Central wrapper for sherpa-onnx:
```kotlin
implementation("com.bihe0832.android:lib-sherpa-onnx:1.0.0")
```

This is automatically downloaded by Gradle - no manual installation required!

**Note**: This is a third-party wrapper. For the official version or advanced options, see `SHERPA_ONNX_SETUP.md`.

## Need Help?

If you encounter issues not covered here:

1. Check the build log in Android Studio's `Build` tab
2. Run with stacktrace: `gradlew.bat build --stacktrace`
3. Check the sherpa-onnx documentation: https://k2-fsa.github.io/sherpa/onnx/android/
4. Ensure all prerequisites are correctly installed

## Next Steps

After successful build:
1. Download required AI models (see ModelManager.kt)
2. Test the app on an emulator or device
3. Review `SHERPA_ONNX_SETUP.md` for advanced sherpa-onnx options if needed
