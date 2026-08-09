# Changelog

All notable changes to the Audil project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.1.0] - 2025-12-16

### Added
- **UI Design System**: Complete "Modern Minimalist" redesign with high-contrast Dark Mode and clean Light Mode.
- **Model Downloads**: Real network download support for Sherpa-ONNX models (tar.bz2 automatic extraction).
- **Meeting Types**: Added "Personal Note" meeting type with specialized prompt.
- **Exports**: File export and sharing functionality for Meeting Summaries.
- **Languages**: Language selection dialog in Settings.

### Changed
- **Navigation**: Improved Settings navigation with explicit Back button.
- **Components**: Restyled buttons (Pill shape) and cards (Flat/Minimal) to match new design.
- **Architecture**: `ModelManager` now handles `tar.bz2` extraction using `commons-compress`.

### Fixed
- **Model Selection**: Fixed empty file generation by replacing simulated downloads with real GitHub Release downloads.
- **Theme**: Fixed theme application delay (changes now apply immediately).
- **Crash**: Fixed crash when exporting summaries (Context misuse).

## [Unreleased]

### Added — Linear-Inspired Dark-First Redesign (`9990c29`)
- **Canvas**: Near-black `#08090A` background with luminance-stepped surfaces — no shadows, no elevation
- **Borders**: Ultra-thin `rgba(255,255,255,0.05)` semi-transparent borders on all cards
- **Accent**: Single indigo palette (`#5E6AD2` / `#7170FF`) replacing scattered Material colors
- **Typography**: Three-tier weight system (400/500/600), aggressive negative letter-spacing on display text
- **Button radius**: 8dp precise (no pill shapes), card radius 12dp
- **All screens rewritten**: Home, Record, History, Detail, Summary, Model Selection
- **System-theme aware**: Dark and light modes verified on Pixel 6
- **Net reduction**: 275 lines removed (465 added, 740 deleted)

### Added — Modern Material 3 UI (`46663a7`)
- Complete Compose Material 3 rewrite with proper `MaterialTheme.colorScheme` tokens
- No hardcoded colors — all surfaces, text, and accents driven by theme
- Outfit → Default font family (Outfit crashes on some devices)

### Added — Real OpenAI Transcription (`f8a82c4`)
- OpenAI-compatible API transcription endpoint with streaming
- Structured meeting summaries (decisions, action items, key points)
- Secure API key storage via EncryptedSharedPreferences

### Fixed
- Gradle wrapper regenerated (missing `gradle-wrapper.jar`)
- `Color.White` nullable-safe arithmetic (`Color.kt:28`)
- `AutoMirrored` → `Default` icon imports (Compose 1.5.1 compat)
- JVM target 17 enforced across all modules

---

## [1.0.0] - Initial Development

### Added

#### Core Features
- **Audio Recording**
  - High-quality audio recording using MediaRecorder
  - Foreground service for background recording
  - Recording notification with controls
  - Audio file management and storage

- **Meeting Management**
  - Room database for persistent storage
  - Meeting CRUD operations
  - Meeting history with search and filter
  - Meeting detail view with metadata

- **Transcription System**
  - Sherpa-ONNX integration (optional)
  - On-device speech recognition
  - Model download and management
  - Transcription progress tracking

- **Speaker Diarization (Beta)**
  - Speaker identification
  - Speaker segment extraction
  - Timeline visualization

- **AI Summarization**
  - OpenAI API integration
  - Multiple summary templates
  - Meeting context support
  - Custom prompt generation

#### User Interface
- **Jetpack Compose UI**
  - Material Design 3 components
  - Dark mode support
  - Responsive layouts
  - Custom theme system

- **Screens**
  - Home screen with quick actions
  - Recording screen with live status
  - History screen with meeting list
  - Meeting detail screen
  - Summary screen with AI output
  - Transcription viewer
  - Settings screen

#### Architecture
- **Clean Architecture**
  - Presentation layer (UI + ViewModels)
  - Domain layer (business models)
  - Data layer (repositories + database)
  - Service layer (background services)

- **Dependency Injection**
  - Hilt setup
  - Database module
  - Audio module
  - ViewModel injection

#### Security
- **Encrypted Storage**
  - EncryptedSharedPreferences for API keys
  - Secure key storage
  - DataStore for preferences

- **Permissions**
  - Runtime permission handling
  - Microphone access
  - Storage access
  - Notification permissions

#### Build & Configuration
- **Gradle Setup**
  - Kotlin DSL build scripts
  - Version catalog
  - KSP for annotation processing
  - Multi-module support

- **Build Scripts**
  - Linux/WSL build script (build.sh)
  - Windows PowerShell script (build.ps1)
  - Gradle wrapper configuration

#### Documentation
- **Build Guides**
  - README_BUILD.md - Quick start guide
  - WINDOWS_BUILD_GUIDE.md - Windows-specific instructions
  - SHERPA_ONNX_SETUP.md - Speech recognition setup

- **Project Documentation**
  - ARCHITECTURE.md - Technical architecture
  - README.md - Project overview
  - TODO.md - Task tracking
  - CHANGELOG.md - Version history

### Technical Details

#### Dependencies
- AndroidX Core KTX
- Lifecycle & ViewModel
- Jetpack Compose (BOM)
- Material Design 3
- Material Icons Extended
- Hilt (Dependency Injection)
- Room (Database)
- Retrofit & OkHttp (Networking)
- Security Crypto
- DataStore Preferences

#### Build Configuration
- Compile SDK: 34
- Min SDK: 29
- Target SDK: 34
- Java Version: 1.8
- Kotlin Compiler: 1.5.1

#### Permissions
- RECORD_AUDIO
- FOREGROUND_SERVICE
- FOREGROUND_SERVICE_MICROPHONE
- INTERNET
- READ_EXTERNAL_STORAGE (API ≤ 32)
- WRITE_EXTERNAL_STORAGE (API ≤ 29)
- POST_NOTIFICATIONS

---

## Development Timeline

### Phase 1: Foundation (Completed)
- ✅ Project setup and Gradle configuration
- ✅ Clean architecture implementation
- ✅ Dependency injection with Hilt
- ✅ Room database setup
- ✅ Basic UI with Jetpack Compose

### Phase 2: Core Features (Completed)
- ✅ Audio recording implementation
- ✅ Foreground service for recording
- ✅ Meeting history and management
- ✅ Settings and configuration

### Phase 3: Advanced Features (In Progress)
- ✅ Sherpa-ONNX integration (optional)
- ✅ OpenAI API integration
- ✅ Summary generation
- ⏳ Speaker diarization (Beta)
- ⏳ Real-time transcription

### Phase 4: UI/UX Polish (In Progress)
- ✅ Material Design 3 theme
- ✅ Custom design system
- ✅ Bottom navigation
- ⏳ Animations and transitions
- ⏳ Accessibility improvements

### Phase 5: Testing & Optimization (Planned)
- ⏳ Unit test coverage
- ⏳ UI test automation
- ⏳ Performance optimization
- ⏳ Memory profiling

### Phase 6: Release Preparation (Planned)
- ⏳ Bug fixes
- ⏳ Documentation completion
- ⏳ Release build optimization
- ⏳ Play Store preparation

---

## Migration Notes

### From Previous Versions

This is the initial release, no migration required.

### Breaking Changes

None yet.

### Deprecations

None yet.

---

## Contributors

- Development Team
- UI/UX Design
- Documentation

---

## Links

- [Repository](https://github.com/nicolasestrem/audil)
- [Issues](https://github.com/nicolasestrem/audil/issues)
- [Discussions](https://github.com/nicolasestrem/audil/discussions)

---

**Note**: This project is under active development. Features and APIs may change.
