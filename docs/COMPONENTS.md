# Audil Component Reference

This document provides a detailed breakdown of the Audil application structure, organizing components by layer.

## 🏗️ Application Structure

The application is structured following Clean Architecture principles, divided into the following main packages in `com.audil`:

### 1. Presentation Layer (`presentation/`)
Handles UI and user interaction.

- **`home/`**: Main dashboard screen.
- **`recording/`**: Audio recording interface.
- **`history/`**: Meeting history list.
- **`detail/`**: Meeting details view.
- **`summary/`**: AI summary generation and display.
- **`transcription/`**: Transcription viewer.
- **`settings/`**: Application settings.

### 2. Domain Layer (`domain/`)
Contains business logic and models.

- **`model/`**: Core data structures used throughout the app (e.g., `Meeting`, `Speaker`).

### 3. Data Layer (`data/`)
Manages data retrieval and storage.

- **`local/`**: Local database and file storage.
  - `AppDatabase`: Room database definition.
  - `MeetingEntity`: Database schema for meetings.
  - `AudioRecorder`: Handles interaction with Android `MediaRecorder`.
- **`remote/`**: Network operations.
  - `OpenAiApiClient`: Client for OpenAI API.
- **`repository/`**: implementation of logic to coordinate data flow.
  - `HistoryRepository`: Manages meeting records.
  - `TranscriptionRepository`: Orchestrates transcription.
  - `DiarizationRepository`: Handles speaker identification.
  - `SummaryRepository`: Manages summary generation.

### 4. Service Layer (`service/`)
Background processing.
- `RecordingService`: Foreground service to keep recording active when the app is backgrounded.

### 5. Dependency Injection (`di/`)
Hilt modules for providing dependencies.

- `DatabaseModule.kt`: Provides `AppDatabase` and DAOs.
- `AudioModule.kt`: Provides audio recording and processing components.

### 6. Native Library (`nativelib/`)
JNI wrappers for native C++ code (e.g., Sherpa-ONNX).
