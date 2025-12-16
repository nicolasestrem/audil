# Audil

> **AI-Powered Meeting Assistant for Android**
> 
> Record, transcribe, and summarize your meetings with on-device speech recognition and AI-powered insights.

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-blue.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-brightgreen.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📱 Overview

**Audil** is a modern Android application designed to enhance meeting productivity through intelligent audio recording, transcription, and summarization. Built with the latest Android technologies, Audil provides a seamless experience for capturing and analyzing meeting content.

### ✨ Key Features

- 🎙️ **High-Quality Audio Recording** - Capture meetings with crystal-clear audio quality
- 📝 **On-Device Transcription** - Convert speech to text using Sherpa-ONNX models (optional)
- 👥 **Speaker Diarization** - Identify and separate different speakers (Beta)
- 🤖 **AI-Powered Summaries** - Generate intelligent meeting summaries with OpenAI
- 📋 **Customizable Templates** - Tailor summaries with meeting context and templates
- 💾 **Persistent Storage** - Local database for secure meeting history
- 🎨 **Beautiful UI** - Modern Material Design 3 interface with dark mode
- 🔒 **Privacy-Focused** - All data stored locally, API keys encrypted

---

## 🚀 Getting Started

### Prerequisites

Before building Audil, ensure you have:

- **Android Studio** Hedgehog (2023.1.1) or later
- **JDK** 17 or later
- **Android SDK** with API level 34
- **Gradle** 8.x (included via wrapper)
- **Git** for version control

### Quick Start

#### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/audil.git
cd audil
```

#### 2. Build the Project

**On Linux/WSL (Recommended):**

```bash
./build.sh
```

**On Windows:**

```powershell
.\build.ps1
```

**Or open in Android Studio:**

1. Open Android Studio
2. Select "Open an Existing Project"
3. Navigate to the `audil` directory
4. Wait for Gradle sync to complete
5. Click "Run" or press `Shift + F10`

#### 3. Configure API Keys (Optional)

For AI summarization features, you'll need an OpenAI API key:

1. Launch the app
2. Navigate to **Settings**
3. Enter your OpenAI API key
4. The key is encrypted and stored securely

> **Note**: The app works without an API key, but summarization features will be unavailable.

---

## 📂 Project Structure

```
audil/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/audil/
│   │   │   │   ├── data/              # Data layer (repositories, database, API)
│   │   │   │   ├── di/                # Dependency injection modules
│   │   │   │   ├── domain/            # Business models
│   │   │   │   ├── nativelib/         # JNI wrappers for native code
│   │   │   │   ├── presentation/      # UI screens and ViewModels
│   │   │   │   ├── service/           # Background services
│   │   │   │   └── ui/                # Reusable UI components & theme
│   │   │   ├── res/                   # Android resources
│   │   │   └── AndroidManifest.xml
│   │   └── test/                      # Unit tests
│   └── build.gradle.kts
├── gradle/                            # Gradle wrapper
├── ARCHITECTURE.md                    # Architecture documentation
├── README_BUILD.md                    # Detailed build instructions
├── SHERPA_ONNX_SETUP.md              # Speech recognition setup
├── WINDOWS_BUILD_GUIDE.md            # Windows-specific build guide
└── build.gradle.kts
```

---

## 🏗️ Architecture

Audil follows **Clean Architecture** principles with **MVVM** pattern:

- **Presentation Layer**: Jetpack Compose UI + ViewModels
- **Domain Layer**: Business models and use cases
- **Data Layer**: Repositories, Room database, Retrofit API clients
- **Service Layer**: Foreground recording service

For detailed architecture documentation, see [`ARCHITECTURE.md`](file:///home/nicol/audil/ARCHITECTURE.md).

---

## 🛠️ Technology Stack

### Core
- **Kotlin** - Primary programming language
- **Jetpack Compose** - Modern declarative UI
- **Material Design 3** - Latest design system
- **Hilt** - Dependency injection
- **Coroutines & Flow** - Asynchronous programming

### Data & Storage
- **Room** - Local database
- **DataStore** - Preferences storage
- **Security Crypto** - Encrypted SharedPreferences

### Networking
- **Retrofit** - REST API client
- **OkHttp** - HTTP client with logging
- **Gson** - JSON serialization

### Audio & ML
- **MediaRecorder** - Audio recording
- **Sherpa-ONNX** - On-device speech recognition (optional)
- **OpenAI API** - AI-powered summarization

---

## 📋 Build Guides

Detailed build instructions are available in separate guides:

- **[Quick Build Guide](file:///home/nicol/audil/README_BUILD.md)** - Fast setup for Linux/WSL
- **[Windows Build Guide](file:///home/nicol/audil/WINDOWS_BUILD_GUIDE.md)** - Windows-specific instructions
- **[Sherpa-ONNX Setup](file:///home/nicol/audil/SHERPA_ONNX_SETUP.md)** - Optional speech recognition setup

---

## ⚙️ Configuration

### Required Permissions

The app requires the following permissions:

- `RECORD_AUDIO` - Microphone access for recording
- `FOREGROUND_SERVICE` - Background recording capability
- `FOREGROUND_SERVICE_MICROPHONE` - Android 14+ requirement
- `INTERNET` - API calls for summarization
- `POST_NOTIFICATIONS` - Recording status notifications

### Optional Features

#### Sherpa-ONNX Speech Recognition

The Sherpa-ONNX dependency is currently commented out in `build.gradle.kts`. To enable on-device transcription:

1. Follow instructions in [`SHERPA_ONNX_SETUP.md`](file:///home/nicol/audil/SHERPA_ONNX_SETUP.md)
2. Uncomment the dependency in `app/build.gradle.kts`
3. Rebuild the project

#### OpenAI Integration

To use AI summarization:

1. Obtain an API key from [OpenAI](https://platform.openai.com/api-keys)
2. Enter the key in the app's Settings screen
3. The key is encrypted using Android Security Crypto

---

## 🐛 Known Issues

### Current Compilation Errors

The following Kotlin compilation errors need to be addressed:

- `RecordingService.kt` - Missing R class references
- `MeetingDetailScreen.kt` - Missing Pause icon import
- `DiarizationRepository.kt` - Type mismatches
- `TranscriptionRepository.kt` - Type inference issues
- `OpenAiApiClient.kt` - Type mismatch

> **Note**: These are code-level issues, not Gradle configuration problems. The build system is fully functional.

### Workarounds

- **Gradle Build**: All Gradle issues are resolved. Use `./build.sh` or `.\build.ps1`
- **Android Studio**: Open the project to see detailed error messages and suggestions
- **Sherpa-ONNX**: Currently disabled to allow builds. See setup guide to enable.

---

## 🧪 Testing

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumentation tests
./gradlew connectedAndroidTest

# All tests
./gradlew testDebug connectedAndroidTest
```

### Test Coverage

- Unit tests for repositories and ViewModels
- Compose UI tests for screens
- Integration tests for database operations

---

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit your changes**: `git commit -m 'Add amazing feature'`
4. **Push to the branch**: `git push origin feature/amazing-feature`
5. **Open a Pull Request**

### Code Style

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add comments for complex logic
- Write tests for new features

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 🙏 Acknowledgments

- [Sherpa-ONNX](https://github.com/k2-fsa/sherpa-onnx) - On-device speech recognition
- [OpenAI](https://openai.com) - AI-powered summarization
- [Android Jetpack](https://developer.android.com/jetpack) - Modern Android development
- [Material Design](https://m3.material.io) - Design system

---

## 📞 Support

For issues, questions, or suggestions:

- **Issues**: [GitHub Issues](https://github.com/yourusername/audil/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourusername/audil/discussions)
- **Email**: support@audil.app

---

## 🗺️ Roadmap

See [`TODO.md`](file:///home/nicol/audil/TODO.md) for planned features and improvements.

---

**Made with ❤️ for better meetings**
