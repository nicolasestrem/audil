# Audil - Architecture Documentation

## Overview

**Audil** is an Android application for meeting recording, transcription, speaker diarization, and AI-powered summarization. The app enables users to record meetings, automatically transcribe audio using on-device speech recognition, identify different speakers, and generate intelligent summaries with customizable templates.

### Key Features

- 🎙️ **Audio Recording**: High-quality meeting audio capture with foreground service
- 📝 **Speech Transcription**: On-device transcription using Sherpa-ONNX models
- 👥 **Speaker Diarization**: Identify and separate different speakers (Beta)
- 🤖 **AI Summarization**: Generate meeting summaries using OpenAI API
- 📋 **Meeting Context**: Customizable templates and context for better summaries
- 💾 **Local Storage**: Room database for persistent meeting history
- 🎨 **Modern UI**: Jetpack Compose with Material Design 3

---

## Technology Stack

### Core Technologies

- **Language**: Kotlin
- **Min SDK**: 29 (Android 10)
- **Target SDK**: 34 (Android 14)
- **Build System**: Gradle 8.x with Kotlin DSL

### Android Jetpack

- **Jetpack Compose**: Modern declarative UI framework
- **Material Design 3**: Latest Material Design components
- **Hilt**: Dependency injection framework
- **Room**: Local database persistence
- **Lifecycle & ViewModel**: Architecture components
- **Navigation Compose**: Type-safe navigation

### Networking & APIs

- **Retrofit**: HTTP client for REST APIs
- **OkHttp**: Network layer with logging interceptor
- **Gson**: JSON serialization/deserialization
- **OpenAI API**: AI-powered summarization

### Security & Storage

- **Security Crypto**: Encrypted SharedPreferences for API keys
- **DataStore**: Preferences storage
- **Room Database**: Encrypted local storage for meetings

### Audio & ML

- **MediaRecorder**: Android audio recording
- **Sherpa-ONNX**: On-device speech recognition (optional)
- **Native Libraries**: C++ integration via JNI for audio processing

---

## Architecture Pattern

Audil follows **Clean Architecture** principles with **MVVM (Model-View-ViewModel)** pattern, organized into distinct layers:

```
┌─────────────────────────────────────────────────────────┐
│                   Presentation Layer                     │
│  (UI Screens, ViewModels, Compose Components)           │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                     Domain Layer                         │
│              (Business Models, Use Cases)                │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                      Data Layer                          │
│  (Repositories, Database, API Clients, Data Sources)    │
└─────────────────────────────────────────────────────────┘
                          ↓
┌─────────────────────────────────────────────────────────┐
│                   Service Layer                          │
│           (Foreground Recording Service)                 │
└─────────────────────────────────────────────────────────┘
```

---

## Layer Breakdown

### 1. Presentation Layer (`presentation/`)

The UI layer built with Jetpack Compose, following the MVVM pattern.

#### Screens & Features

- **Home Screen** (`home/`): Dashboard with recent meetings and quick actions
- **Recording Screen** (`recording/`): Real-time audio recording interface
- **History Screen** (`history/`): List of all recorded meetings
- **Meeting Detail Screen** (`detail/`): View meeting details and transcripts
- **Summary Screen** (`summary/`): AI-generated meeting summaries
- **Transcription Screen** (`transcription/`): Transcription viewer
- **Settings Screen** (`settings/`): App configuration and API key management

#### ViewModels

Each screen has a corresponding ViewModel that:
- Manages UI state using `StateFlow`
- Handles business logic delegation to repositories
- Provides data to Composable functions
- Survives configuration changes

**Example**: `SummaryViewModel`
- Manages summary generation state
- Interacts with `SummaryRepository`
- Handles meeting context and templates
- Provides progress updates to UI

### 2. Domain Layer (`domain/`)

Contains business models and domain logic.

#### Models

- **`MeetingContext`**: Represents meeting metadata and context
  - Meeting type (standup, planning, review, etc.)
  - Participants
  - Agenda and goals
  - Custom context information

### 3. Data Layer (`data/`)

Handles data operations and business logic.

#### Repositories

Repositories act as single source of truth for data operations:

- **`HistoryRepository`**: Meeting CRUD operations
- **`TranscriptionRepository`**: Audio transcription orchestration
- **`DiarizationRepository`**: Speaker diarization processing
- **`SummaryRepository`**: AI summary generation
- **`SettingsRepository`**: User preferences and API key management
- **`ModelManager`**: ML model download and management

#### Local Data (`data/local/`)

- **`AppDatabase`**: Room database configuration
- **`MeetingEntity`**: Database entity for meetings
- **`AudioRecorder`**: Audio recording implementation
- **`Converters`**: Type converters for Room database

#### Remote Data (`data/remote/`)

- **`OpenAiApiClient`**: OpenAI API integration for summarization

### 4. Service Layer (`service/`)

Background services for long-running operations.

#### RecordingService

- **Type**: Foreground Service
- **Purpose**: Continuous audio recording in background
- **Features**:
  - Persistent notification during recording
  - Audio file management
  - Recording state management
  - Microphone permission handling

### 5. UI Components (`ui/`)

Reusable UI components and design system.

#### Design System

- **Theme** (`theme/`): Material Design 3 theme configuration
  - Color palette (Electric Blue, Vibrant Teal)
  - Typography (Outfit, Inter fonts)
  - Custom theme tokens

#### Components

- **`AudilButton`**: Branded button with gradient effects
- **`AudilCard`**: Consistent card styling
- **`AudilScaffold`**: App-wide scaffold with bottom navigation

### 6. Dependency Injection (`di/`)

Hilt modules for dependency injection.

- **`DatabaseModule`**: Provides Room database and DAOs
- **`AudioModule`**: Provides audio-related dependencies

### 7. Native Library (`nativelib/`)

JNI wrapper for native C++ audio processing.

- **`SherpaOnnxWrapper`**: Interface to Sherpa-ONNX speech recognition
- **`NativeAudioProcessor`**: Native audio processing utilities

---

## Data Flow

### Recording Flow

```
User Taps Record
      ↓
RecordingViewModel
      ↓
RecordingService (Foreground)
      ↓
AudioRecorder (MediaRecorder)
      ↓
Audio File Saved
      ↓
HistoryRepository
      ↓
Room Database
```

### Transcription Flow

```
User Requests Transcription
      ↓
TranscriptionRepository
      ↓
ModelManager (Download/Check Model)
      ↓
SherpaOnnxWrapper (Native)
      ↓
Transcription Result
      ↓
Optional: DiarizationRepository
      ↓
Final Transcript Saved
```

### Summarization Flow

```
User Requests Summary
      ↓
SummaryViewModel
      ↓
SummaryRepository
      ↓
OpenAiApiClient (Retrofit)
      ↓
OpenAI API
      ↓
AI-Generated Summary
      ↓
Display to User
```

---

## Dependency Injection

Audil uses **Hilt** for dependency injection, providing:

- **Singleton Scopes**: Database, repositories, API clients
- **ViewModel Injection**: Automatic ViewModel creation
- **Module Organization**: Separated by concern (Database, Audio, etc.)

### Key Modules

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(context: Context): AppDatabase
    
    @Provides
    fun provideMeetingDao(db: AppDatabase): MeetingDao
}
```

---

## Security Considerations

### API Key Storage

- **Encrypted SharedPreferences**: API keys stored using `EncryptedSharedPreferences`
- **No Hardcoding**: API keys never committed to version control
- **User-Provided**: Users must provide their own OpenAI API key

### Permissions

Required permissions:
- `RECORD_AUDIO`: Microphone access for recording
- `FOREGROUND_SERVICE`: Background recording
- `FOREGROUND_SERVICE_MICROPHONE`: Android 14+ requirement
- `INTERNET`: API calls for summarization
- `POST_NOTIFICATIONS`: Recording notifications

---

## Build Configuration

### Gradle Setup

- **Namespace**: `com.audil`
- **Application ID**: `com.audil`
- **Compile SDK**: 34
- **Min SDK**: 29
- **Target SDK**: 34
- **Java Version**: 1.8
- **Kotlin Compiler**: 1.5.1

### Build Variants

- **Debug**: Development builds with logging
- **Release**: Optimized production builds (ProGuard disabled)

### Known Issues

The Sherpa-ONNX dependency is currently commented out due to availability issues. See [`SHERPA_ONNX_SETUP.md`](file:///home/nicol/audil/SHERPA_ONNX_SETUP.md) for integration instructions.

---

## Testing Strategy

### Unit Tests

- Repository logic testing
- ViewModel state management
- Business logic validation

### Instrumentation Tests

- UI component testing with Compose Test
- Database operations
- Navigation flows

### Test Dependencies

- JUnit 4
- Espresso
- Compose UI Test

---

## Future Enhancements

- **Offline Summarization**: On-device LLM integration
- **Cloud Sync**: Multi-device meeting synchronization
- **Export Options**: PDF, DOCX, email integration
- **Real-time Transcription**: Live transcription during recording
- **Advanced Diarization**: Improved speaker identification
- **Meeting Analytics**: Insights and statistics

---

## References

- [Build Guide](file:///home/nicol/audil/README_BUILD.md)
- [Windows Build Guide](file:///home/nicol/audil/WINDOWS_BUILD_GUIDE.md)
- [Sherpa-ONNX Setup](file:///home/nicol/audil/SHERPA_ONNX_SETUP.md)
- [Android Developer Documentation](https://developer.android.com)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Hilt Documentation](https://dagger.dev/hilt/)
