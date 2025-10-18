# Feature Specification: Native Mobile Apps

**Feature ID:** F002  
**Priority:** High  
**Effort:** Large (12-16 weeks)  
**Competitors:** Audiobookshelf, Booksonic-Air, Kavita, Komga

## Overview

Native iOS and Android apps with offline download, background sync, and optimized mobile UX.

## Why It Matters

- 70%+ of users access content on mobile devices
- Native apps provide better performance than web
- Offline access is critical for commuters/travelers
- App stores increase discoverability

## User Stories

### US-001: Download for Offline
**As a** mobile user  
**I want** to download books for offline reading  
**So that** I can read without internet

**Acceptance:**
- Download books/audiobooks to device storage
- Queue multiple downloads
- Manage offline library
- Auto-sync when online

### US-002: Background Playback
**As a** audiobook listener  
**I want** playback to continue when app is backgrounded  
**So that** I can multitask

**Acceptance:**
- Continue playback when screen locked
- Show media controls in notification/control center
- Save progress when interrupted

### US-003: Push Notifications
**As a** user  
**I want** notifications for new content  
**So that** I don't miss updates

**Acceptance:**
- Push notifications for new books in library
- Reading reminders
- Download completion alerts

## Architecture

### Technology Stack

**Option A: React Native**
- Pros: Code sharing, faster development, large community
- Cons: Larger app size, performance for complex UX

**Option B: Flutter**
- Pros: Performance, modern UI, single codebase
- Cons: Smaller ecosystem than React Native

**Recommendation:** Flutter for better performance and modern UI toolkit

### Key Components

1. **Authentication Module**
   - OAuth/OIDC integration with backend
   - Token refresh handling
   - Biometric authentication

2. **Offline Storage**
   - SQLite for metadata
   - File system for book/audio files
   - IndexedDB equivalent for web data

3. **Sync Engine**
   - Differential sync algorithm
   - Conflict resolution
   - Background sync with WorkManager (Android) / Background Tasks (iOS)

4. **Media Player**
   - ExoPlayer (Android) / AVPlayer (iOS)
   - Media session support
   - Picture-in-picture for video (future)

## Data Model

```dart
class OfflineBook {
  String id;
  String title;
  String author;
  String localPath;
  DownloadStatus status;
  int downloadProgress;
  DateTime downloadedAt;
  Map<String, dynamic> metadata;
}

enum DownloadStatus {
  pending,
  downloading,
  completed,
  failed,
  paused
}
```

## API Requirements

```
# New endpoints needed:
GET /api/v1/mobile/sync
  - Returns delta since last sync
  - Client sends last sync timestamp

POST /api/v1/devices
  - Register device for push notifications
  - Store FCM/APNS token

POST /api/v1/downloads/prepare/{bookId}
  - Prepare download package (optimize images, etc.)
  - Return download URL with temp token
```

## Implementation Tasks

### Phase 1: Foundation (4 weeks)

1. **Setup & Architecture** (1 week)
   - Initialize Flutter project
   - Configure CI/CD for iOS & Android
   - Setup app signing
   - Create base architecture (BLoC/Provider)

2. **Authentication** (1 week)
   - Implement OAuth flow
   - Token storage (secure keychain)
   - Biometric authentication
   - Logout/session management

3. **Basic UI** (2 weeks)
   - Home screen with book grid
   - Book detail screen
   - Navigation structure
   - Settings screen

### Phase 2: Core Features (6 weeks)

4. **Book Library** (2 weeks)
   - Display books from API
   - Infinite scroll/pagination
   - Search and filtering
   - Pull-to-refresh

5. **Offline Downloads** (2 weeks)
   - Download manager
   - Background downloads
   - Storage management
   - Progress UI

6. **Reading Experience** (2 weeks)
   - Integrate Foliate or similar EPUB reader
   - Audio player for audiobooks
   - Progress tracking
   - Settings (font, theme, etc.)

### Phase 3: Advanced Features (4 weeks)

7. **Sync Engine** (2 weeks)
   - Background sync service
   - Conflict resolution
   - Offline-first architecture
   - Data persistence layer

8. **Notifications & Polish** (2 weeks)
   - Push notifications
   - Local notifications (reminders)
   - App icon badges
   - Splash screen, onboarding

### Phase 4: Testing & Release (2 weeks)

9. **Testing** (1 week)
   - Unit tests
   - Widget tests
   - Integration tests
   - Beta testing (TestFlight, Play Console)

10. **Release** (1 week)
    - App store listings
    - Screenshots
    - Privacy policy
    - Submit to App Store & Play Store

## Test Plan

### Unit Tests

```dart
test('should download book and update status', () async {
  final downloader = BookDownloader();
  final book = Book(id: '123', title: 'Test Book');
  
  await downloader.download(book);
  
  expect(book.downloadStatus, DownloadStatus.completed);
  expect(await fileExists(book.localPath), isTrue);
});
```

### Integration Tests

```dart
testWidgets('should display offline books when offline', (tester) async {
  await tester.pumpWidget(MyApp());
  await tester.tap(find.text('Offline Library'));
  await tester.pumpAndSettle();
  
  expect(find.byType(BookCard), findsWidgets);
  expect(find.text('No internet connection'), findsOneWidget);
});
```

### Platform-Specific Tests

- **iOS**: Background audio playback, CarPlay integration
- **Android**: Background sync with Doze mode, media controls

## Dependencies & Risks

| Dependency | Purpose | Risk |
|------------|---------|------|
| flutter_secure_storage | Secure token storage | Platform-specific issues |
| dio | HTTP client | - |
| hive/isar | Local database | Migration complexity |
| audio_service | Background audio | Platform API changes |
| flutter_local_notifications | Push notifications | Permission handling |

### Risks

1. **App Store Rejection**: Ensure compliance with guidelines
2. **Platform Updates**: iOS/Android API changes
3. **Large Downloads**: Network failures, storage limits
4. **Battery Drain**: Background processes optimization
5. **Maintenance**: Two platform codebases (even with Flutter)

## Success Metrics

- **Downloads**: 5,000+ installs in first 3 months
- **Rating**: 4.0+ stars on both stores
- **DAU/MAU**: >30% (daily/monthly active users ratio)
- **Retention**: 40% day-7 retention
- **Offline Usage**: 30% of sessions use offline mode

## Rollout Strategy

1. **Alpha**: Internal testing (2 weeks)
2. **Beta**: TestFlight/Play Console beta (4 weeks, 100 users)
3. **Soft Launch**: Release to one country (2 weeks)
4. **Global Launch**: All regions

## Future Enhancements

- CarPlay / Android Auto integration
- Apple Watch / Wear OS apps
- Widgets (iOS 14+, Android)
- Shortcuts / Siri integration
- Cross-device handoff

---
