# TODO

> **Prioritized task list for Audil development**

This document tracks pending tasks, known issues, and planned features for the Audil project.

---

## 🔴 Critical Issues

### Build & Compilation Errors

- [ ] **Fix RecordingService.kt** - Missing R class references
  - Issue: Cannot resolve R.drawable and R.string references
  - Impact: Recording service won't compile
  - Priority: HIGH

- [ ] **Fix MeetingDetailScreen.kt** - Missing Pause icon
  - Issue: Missing import for Icons.Filled.Pause
  - Impact: UI compilation error
  - Priority: HIGH

- [ ] **Fix DiarizationRepository.kt** - Type mismatches
  - Issue: Type inference problems in diarization logic
  - Impact: Speaker identification won't work
  - Priority: MEDIUM

- [ ] **Fix TranscriptionRepository.kt** - Type inference issues
  - Issue: Generic type mismatches in transcription flow
  - Impact: Transcription feature broken
  - Priority: HIGH

- [ ] **Fix OpenAiApiClient.kt** - Type mismatch
  - Issue: API response type doesn't match expected type
  - Impact: AI summarization won't work
  - Priority: HIGH

---

## 🟡 Feature Development

### Core Features

- [ ] **Implement Sherpa-ONNX Integration**
  - Add Sherpa-ONNX AAR to project
  - Uncomment dependency in build.gradle.kts
  - Test on-device transcription
  - See: `SHERPA_ONNX_SETUP.md`

- [ ] **Complete Speaker Diarization**
  - Improve speaker identification accuracy
  - Add speaker labeling UI
  - Allow manual speaker name assignment
  - Export diarized transcripts

- [ ] **Real-time Transcription**
  - Stream audio to transcription engine during recording
  - Display live transcript in RecordingScreen
  - Handle partial results and corrections

- [ ] **Audio Playback**
  - Implement audio player in MeetingDetailScreen
  - Add playback controls (play, pause, seek)
  - Sync transcript highlighting with playback
  - Support speed adjustment (0.5x - 2x)

### AI & Summarization

- [ ] **Expand Summary Templates**
  - Add more meeting types (retrospective, brainstorm, interview)
  - Allow custom template creation
  - Template sharing/export

- [ ] **On-Device Summarization**
  - Integrate on-device LLM (e.g., Gemini Nano)
  - Reduce dependency on OpenAI API
  - Improve privacy and offline capability

- [ ] **Action Item Extraction**
  - Automatically detect action items from transcripts
  - Extract due dates and assignees
  - Create task list view

- [ ] **Meeting Insights**
  - Speaking time analytics per participant
  - Topic detection and categorization
  - Sentiment analysis

### Export & Sharing

- [ ] **Export Formats**
  - PDF export with formatting
  - DOCX export for editing
  - Plain text export
  - Markdown export

- [ ] **Sharing Options**
  - Email integration
  - Cloud storage (Google Drive, Dropbox)
  - Direct share to messaging apps
  - Generate shareable links

### Settings & Configuration

- [ ] **Model Management UI**
  - Download/delete transcription models
  - Show model size and storage usage
  - Model quality comparison

- [ ] **Recording Settings**
  - Audio quality selection (bitrate, sample rate)
  - Noise reduction toggle
  - Auto-stop recording after silence

- [ ] **Privacy Settings**
  - Local-only mode (disable all API calls)
  - Auto-delete recordings after X days
  - Passcode/biometric lock

---

## 🟢 UI/UX Improvements

### Design Polish

- [ ] **Animations & Transitions**
  - Add screen transition animations
  - Implement micro-interactions
  - Loading state animations

- [ ] **Accessibility**
  - Add content descriptions for all UI elements
  - Test with TalkBack
  - Support dynamic font sizing
  - High contrast mode

- [ ] **Onboarding Flow**
  - Create welcome screens for new users
  - Explain key features
  - Guide through initial setup

- [ ] **Empty States**
  - Design empty state for history screen
  - Add helpful tips and CTAs
  - Illustrate key features

### User Experience

- [ ] **Search & Filter**
  - Search meetings by title, date, or content
  - Filter by meeting type or date range
  - Sort options (newest, oldest, alphabetical)

- [ ] **Batch Operations**
  - Select multiple meetings
  - Bulk delete
  - Bulk export

- [ ] **Offline Support**
  - Handle network errors gracefully
  - Queue API requests for later
  - Show offline indicator

---

## 🔵 Technical Debt

### Code Quality

- [ ] **Add Unit Tests**
  - Repository tests
  - ViewModel tests
  - Use case tests
  - Target 80% coverage

- [ ] **Add UI Tests**
  - Compose UI tests for all screens
  - Navigation flow tests
  - Integration tests

- [ ] **Code Documentation**
  - Add KDoc comments to public APIs
  - Document complex algorithms
  - Create inline code examples

- [ ] **Refactoring**
  - Extract reusable composables
  - Simplify complex ViewModels
  - Reduce code duplication

### Performance

- [ ] **Optimize Database Queries**
  - Add database indices
  - Use pagination for large lists
  - Implement incremental loading

- [ ] **Memory Management**
  - Profile memory usage
  - Fix potential memory leaks
  - Optimize image loading

- [ ] **Build Optimization**
  - Enable R8/ProGuard for release builds
  - Reduce APK size
  - Optimize dependencies

### Security

- [ ] **Security Audit**
  - Review API key storage
  - Validate input sanitization
  - Check for SQL injection risks

- [ ] **Certificate Pinning**
  - Implement SSL pinning for API calls
  - Prevent man-in-the-middle attacks

---

## 📚 Documentation

- [x] **Architecture Documentation** - Completed
- [x] **README** - Completed
- [x] **TODO** - Completed (this file)
- [ ] **CHANGELOG** - In progress
- [ ] **API Documentation**
  - Document OpenAI integration
  - Document Sherpa-ONNX usage
  - Create integration guides

- [ ] **User Guide**
  - How to record meetings
  - How to generate summaries
  - How to export transcripts
  - FAQ section

- [ ] **Developer Guide**
  - Setup instructions for contributors
  - Code style guide
  - Testing guidelines
  - Release process

---

## 🚀 Future Enhancements

### Advanced Features

- [ ] **Multi-Language Support**
  - Support multiple languages for transcription
  - Translate transcripts
  - Localize UI

- [ ] **Cloud Sync**
  - Sync meetings across devices
  - Cloud backup
  - Multi-device access

- [ ] **Calendar Integration**
  - Import meetings from calendar
  - Auto-populate meeting context
  - Schedule recordings

- [ ] **Collaboration**
  - Share meetings with team members
  - Collaborative editing of summaries
  - Comments and annotations

- [ ] **Meeting Analytics Dashboard**
  - Weekly/monthly meeting statistics
  - Time spent in meetings
  - Most active participants
  - Productivity insights

### Platform Expansion

- [ ] **Tablet Optimization**
  - Responsive layouts for tablets
  - Multi-pane UI
  - Stylus support

- [ ] **Wear OS Companion**
  - Quick recording from watch
  - View recent summaries
  - Voice commands

- [ ] **Web Dashboard**
  - View meetings on web
  - Advanced search and filtering
  - Export and sharing

---

## 📊 Priority Matrix

| Priority | Category | Count |
|----------|----------|-------|
| 🔴 HIGH | Critical Issues | 5 |
| 🟡 MEDIUM | Feature Development | 12 |
| 🟢 LOW | UI/UX Improvements | 8 |
| 🔵 TECH DEBT | Code Quality & Performance | 10 |

---

## 📝 Notes

- **Sherpa-ONNX**: Currently disabled to allow builds. Enable when ready for on-device transcription.
- **OpenAI API**: Required for summarization. Users must provide their own API key.
- **Testing**: Prioritize testing for critical user flows (record → transcribe → summarize).
- **Performance**: Monitor app performance on low-end devices (Android 10+).

---

**Last Updated**: 2025-12-16
