# Features Update - December 2025 (v1.1.0)

This document details the major changes introduced in the v1.1.0 "Feedback Fixes" update.

## 1. Speech Recognition Model Management
We have overhauled the model management system to support real on-device model deployment.

### How it works
- **Source**: Models are downloaded directly from `k2-fsa/sherpa-onnx` GitHub Releases.
- **Format**: Models are downloaded as `.tar.bz2` archives to ensure integrity and minimal size.
- **Extraction**: The app uses `commons-compress` to extract the archives on-the-fly to the app's internal storage (`/data/user/0/com.audil/files/models/`).
- **Validation**: The system checks for the presence of `*.onnx` files before declaring a model "Ready".

### Supported Models
- **Tiny (Standard)**: ~40MB. Fast, multilingual. Good for most dictation.
- **Base/Small**: Larger models for higher accuracy (placeholder URLs in code).

## 2. UI/UX Redesign ("Modern Minimalist")
The application has adopted a new design language focused on readability and contrast.

### Design Tokens (`Color.kt`)
- **Primary**: Electric Blue (`#3B82F6`)
- **Dark Background**: Unix Black (`#121212`) - A true deep dark mode.
- **Light Background**: Paper White (`#FAFAFA`) - Clean and airy.
- **Components**:
    - **Buttons**: Pill-shaped (50% corner radius) for a modern feel.
    - **Cards**: Flat elevation with subtle borders or tonal separation.

### Navigation
- **Settings**: Now features a standard Back arrow for consistent navigation logic.
- **Bottom Bar**: Refined aesthetics with no tonal elevation in dark mode for a seamless look.

## 3. Meeting Improvements
### New Meeting Type: Personal Note
- **Purpose**: For solo voice memos or thoughts.
- **AI Behavior**: The summarization prompt is tuned to extract "Key Ideas, Tasks, and Reminders" rather than "Action Items" or "Speakers".

### Export & Share
- **Function**: Users can now Share summaries to other apps (Keep, Gmail, Drive) via the system Share Sheet.
- **Format**: Plain text export.

## 4. Technical Dependencies
- Added `org.apache.commons:commons-compress:1.26.1` for archive handling.

## 5. High-Priority Regression Fixes (Dec 16, 2025)
Following user feedback on the "Midnight" redesign, several critical regressions were addressed to restore usability and stability.

### UI Restoration
- **Reverted Light Theme**: The "Premium Gradient" and transparency effects were removed. The Light Theme now uses a clean `PaperWhite` background, correcting issues with text readability and layout artifacts ("white rectangle").
- **Dark Mode Correction**: Forced Dark Mode was reverted to System Default. The Dark Theme now uses a standard high-contrast dark palette (`UnixBlack`, `#121212`) instead of the blue-tinted slate gradient.
- **Navigation Bar**: Fixed "blob" artifacts by ensuring solid background colors for the bottom navigation.

### Functional Fixes
- **Qwen Model Support**: Fixed the "Failed to load model" error. `ModelManager` now supports direct `.gguf` file downloads without requiring tarball extraction, enabling the use of `qwen2.5-0.5b-instruct-q4_k_m.gguf`.
- **Equalizer Removal**: The `Waveform` visualization was removed from the Recording Screen as requested.

### OpenAI Compatible API
- **New Feature**: Added support for OpenAI-compatible remote APIs.
- **Configuration**: Users can now toggle `use_remote_generation` in Settings (UI pending) and configure a custom Base URL and API Key.
- **Flexibility**: This setting allows connection to:
    - OpenAI (`https://api.openai.com/v1`)
    - Local LLMs via LM Studio / Ollama (`http://localhost:1234/v1`)
    - Other compatible providers (OpenRouter, etc.)
