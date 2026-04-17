Never mark a task complete without pasting the verbatim output of the verification command (tests, build, lint, or run). No summary, no paraphrase � the raw output.
Never stub, mock, hardcode expected values, or insert placeholder code (TODO, pass, ...) to make something appear to work. If blocked, stop and ask.

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Audil** is an Android application for AI-powered meeting management. It records meetings, transcribes audio with on-device speech recognition, identifies speakers, and generates AI-powered summaries. All data is stored locally with a privacy-first design.

- **Platform**: Android (Native Kotlin)
- **Architecture**: Clean Architecture + MVVM
- **UI**: Jetpack Compose with Material Design 3
- **Min SDK**: 29 (Android 10)
- **Target SDK**: 34 (Android 14)

## Common Development Commands

### Building

```bash
# Quick build (Linux/WSL - recommended)
./build.sh

# Windows PowerShell
.\build.ps1

# Gradle commands
./gradlew clean build        # Clean and build
./gradlew assembleDebug      # Debug APK
./gradlew assembleRelease    # Release APK
./gradlew installDebug       # Install debug to device
```

### Testing

```bash
# Run unit tests
./gradlew test
./gradlew testDebugUnitTest

# Run instrumentation tests
./gradlew connectedAndroidTest

# Run specific test
./gradlew test --tests "com.audil.data.repository.HistoryRepositoryTest"

# Generate test report
./gradlew testDebugUnitTest --info
```

### Code Quality

```bash
# Kotlin lint
./gradlew ktlintCheck

# Android lint
./gradlew lint
./gradlew lintDebug
```

### Gradle Sync & Clean

```bash
# Force Gradle sync
./gradlew --refresh-dependencies

# Clean build cache
./gradlew clean
rm -rf .gradle build app/build
```

## Architecture Overview

### Four-Layer Clean Architecture

```
Presentation Layer (UI)     → ViewModels + Jetpack Compose screens
        ↓
Domain Layer               → Business models (MeetingContext, MeetingType)
        ↓
Data Layer                 → Repositories + Room database + Retrofit API
        ↓
Service Layer             → RecordingService (foreground service)
```

### Key Architectural Patterns

1. **Repository Pattern**: Single source of truth for data operations
   - `HistoryRepository` - Meeting CRUD
   - `TranscriptionRepository` - Audio transcription
   - `DiarizationRepository` - Speaker identification
   - `SummaryRepository` - AI summary generation
   - `SettingsRepository` - Preferences and API keys

2. **Dependency Injection**: Hilt with `@Singleton` and `@HiltViewModel`
   - All repositories are singletons
   - ViewModels auto-injected via Hilt
   - Modules: `DatabaseModule`, `AudioModule`

3. **State Management**: `StateFlow` for reactive UI updates
   - ViewModels expose `StateFlow<UiState>`
   - Compose screens collect and observe state
   - Unidirectional data flow

4. **Navigation**: Jetpack Navigation Compose with type-safe routes

## Critical Implementation Details

### Data Flow Patterns

**Recording Flow**:
```
RecordingViewModel → RecordingService (foreground) → AudioRecorder (MediaRecorder)
→ HistoryRepository → Room Database
```

**Transcription Flow**:
```
TranscriptionRepository → ModelManager (download model) → SherpaOnnxWrapper (JNI)
→ Optional: DiarizationRepository → Room Database
```

**Summarization Flow**:
```
SummaryViewModel → SummaryRepository → OpenAiApiClient (Retrofit)
→ OpenAI API → Display result
```

### Security & Permissions

- **API Keys**: Stored using `EncryptedSharedPreferences` (never hardcode)
- **Required Permissions**:
  - `RECORD_AUDIO` - Microphone access
  - `FOREGROUND_SERVICE` - Background recording
  - `FOREGROUND_SERVICE_MICROPHONE` - Android 14+ requirement
  - `INTERNET` - API calls
  - `POST_NOTIFICATIONS` - Recording notifications

### Native Integration

- **Sherpa-ONNX**: Currently commented out in `build.gradle.kts` (not available in Maven)
- **JNI Wrappers**: `SherpaOnnxWrapper`, `LlamaCppWrapper`, `DiarizationEngine`
- **CMake Support**: Configured but native builds disabled until library available

## Known Issues & Current State

### Critical Compilation Errors (Must Fix Before Build)

From `TODO.md` and git status:

1. **RecordingService.kt** - Missing R class references (`R.drawable`, `R.string`)
2. **MeetingDetailScreen.kt** - Missing `Icons.Filled.Pause` import
3. **TranscriptionRepository.kt** - Type inference issues
4. **DiarizationRepository.kt** - Type mismatches
5. **OpenAiApiClient.kt** - API response type mismatch

### Sherpa-ONNX Integration

- Dependency commented out (lines 90-104 in `app/build.gradle.kts`)
- See `SHERPA_ONNX_SETUP.md` for integration instructions
- Options: Build from source or use third-party Maven wrapper

### Testing

- **Current Status**: Test infrastructure present but no tests written
- **Priority**: Add unit tests for repositories (HIGH)
- **Goal**: 80% code coverage

## File Organization

```
app/src/main/java/com/audil/
├── data/
│   ├── local/           # Room database, AudioRecorder
│   ├── remote/          # OpenAiApiClient (Retrofit)
│   └── repository/      # Repository implementations
├── di/                  # Hilt modules
├── domain/              # Business models
├── nativelib/           # JNI wrappers
├── presentation/        # Screens + ViewModels
│   ├── home/
│   ├── recording/
│   ├── history/
│   ├── detail/
│   ├── summary/
│   ├── transcription/
│   └── settings/
├── service/             # RecordingService
└── ui/
    ├── components/      # Reusable UI components
    └── theme/           # Material Design 3 theme
```

## Development Guidelines

### When Adding New Features

1. **Create Repository First**: Business logic belongs in repositories, not ViewModels
2. **Use Hilt DI**: Inject dependencies, don't instantiate manually
3. **Follow Layer Separation**:
   - UI (Compose) → ViewModel → Repository → Data Source
   - Never access database or API directly from ViewModel
4. **State Management**: Use `StateFlow` for UI state, `Flow` for data streams
5. **Error Handling**: Use `Result<T>` or sealed classes for operation results

### Testing Strategy

1. **Repository Tests**: Mock database/API, test business logic
2. **ViewModel Tests**: Mock repositories, test state transformations
3. **Compose Tests**: Use `composeTestRule` for UI testing
4. **Integration Tests**: Test Room database operations

### Code Style

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful names: `generateMeetingSummary()` not `gen()`
- Prefer immutability: Use `val` over `var`
- Use coroutines with `viewModelScope` and `lifecycleScope`

### When Modifying UI

- **Compose Previews**: Add `@Preview` annotations for quick iteration
- **Material 3**: Use components from `androidx.compose.material3`
- **Theme Colors**: Use `MaterialTheme.colorScheme` (Electric Blue, Vibrant Teal)
- **Custom Components**: Reuse `AudilButton`, `AudilCard`, `AudilScaffold`

### When Working with Audio

- **Recording**: Use `RecordingService` (foreground service with notification)
- **File Storage**: Save to `context.getExternalFilesDir(Environment.DIRECTORY_MUSIC)`
- **Permissions**: Check runtime permissions before recording

### When Integrating APIs

- **Retrofit**: Define interfaces in `data/remote/`
- **API Keys**: Store in `EncryptedSharedPreferences` via `SettingsRepository`
- **Error Handling**: Handle network errors gracefully with user feedback

## Dependencies & Version Catalog

Dependencies managed via `gradle/libs.versions.toml`:

- **Jetpack Compose BOM**: Manages Compose versions
- **Hilt**: 2.50
- **Room**: Latest stable
- **Retrofit**: Latest stable
- **Security Crypto**: 1.0.0

When adding dependencies, add to version catalog first, then reference via `libs.*`.

## Build Configuration

- **Java Version**: 1.8 (required for Android compatibility)
- **Kotlin Compiler**: 1.5.1
- **Gradle**: 8.x (wrapper included)
- **Build Features**: Compose, BuildConfig, CMake (disabled)
- **Proguard**: Disabled (enable for production release)

## Documentation References

- **Architecture**: See `ARCHITECTURE.md` for detailed architecture documentation
- **Build Instructions**: See `README_BUILD.md` (Linux/WSL) and `WINDOWS_BUILD_GUIDE.md`
- **Sherpa-ONNX Setup**: See `SHERPA_ONNX_SETUP.md`
- **Task List**: See `TODO.md` for prioritized tasks and known issues
- **Changelog**: See `CHANGELOG.md` for version history

## Git Workflow

- **Main Branch**: All work should start on a new feature branch (per user's global instructions)
- **Never commit to main** unless explicitly specified
- **Ignored Files**: `.gitignore` excludes recordings, APKs, local.properties, and IDE files
- **Sensitive Data**: Never commit API keys, credentials, or user data
