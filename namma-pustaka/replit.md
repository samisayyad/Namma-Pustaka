# Namma Pustaka — AI-Powered Smart Rural Library Assistant

A production-ready native Android application for managing school libraries in rural Karnataka, India. Students can discover and borrow books, chat with an AI librarian powered by Gemini, and track their reading progress. Teachers get a full dashboard with analytics, QR-based book management, and student activity monitoring.

## Quick Start

1. Open `namma-pustaka/` in Android Studio
2. Complete the steps in `SETUP_INSTRUCTIONS.md`
3. Run on an Android device (API 24+) or emulator

## Stack

- **Language**: Kotlin
- **UI**: XML layouts + Material Design 3
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt (Dagger)
- **Backend**: Firebase (Auth, Firestore, Storage, FCM)
- **AI**: Google Gemini 1.5 Flash (book summaries + chat)
- **QR Scanning**: ML Kit + CameraX
- **Local DB**: Room (offline book cache)
- **Image Loading**: Glide
- **Charts**: MPAndroidChart
- **Animations**: Lottie
- **Navigation**: Jetpack Navigation Component + Safe Args
- **Async**: Kotlin Coroutines + Flow

## Package Structure

```
com.nammapustaka/
├── NammaPustakaApp.kt          @HiltAndroidApp
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt      Room database
│   │   └── BookDao.kt          DAO for offline book cache
│   ├── model/
│   │   ├── Book.kt             @Entity + @Parcelize + @DocumentId
│   │   ├── BookTransaction.kt  Borrow/return records
│   │   └── User.kt             User + Badge + LeaderboardEntry + ChatMessage
│   └── repository/
│       ├── BookRepository.kt   Firestore books, reviews, transactions
│       ├── UserRepository.kt   Auth, user profile, leaderboard
│       └── AiRepository.kt     Gemini AI chat + summaries
├── di/
│   ├── AppModule.kt            Firebase DI providers
│   └── DatabaseModule.kt       Room DI providers
├── service/
│   └── NammaFcmService.kt      Push notification handling
├── ui/
│   ├── MainActivity.kt         NavController + BottomNavigationView
│   ├── splash/SplashActivity.kt
│   ├── onboarding/             ViewPager2 + Lottie onboarding
│   ├── auth/                   Login + Register (Hilt + StateFlow)
│   ├── home/                   Home feed with shimmer loading
│   ├── catalog/                Book grid/list + genre chips + search
│   ├── bookdetail/             AI summary, borrow, reviews
│   ├── qrscanner/              CameraX + ML Kit QR scan
│   ├── aichat/                 Gemini AI chat with history
│   ├── leaderboard/            School leaderboard with rank
│   ├── profile/                Student profile + stats
│   ├── teacher/                Teacher dashboard + analytics
│   └── analytics/              MPAndroidChart pie chart
├── utils/
│   ├── Resource.kt             Loading/Success/Error sealed class
│   └── Extensions.kt           View helpers, Glide loaders, date utils
└── viewmodel/
    ├── AuthViewModel.kt
    ├── HomeViewModel.kt
    ├── CatalogViewModel.kt
    ├── BookDetailViewModel.kt
    ├── AiChatViewModel.kt
    ├── LeaderboardViewModel.kt
    ├── QrScannerViewModel.kt
    ├── AnalyticsViewModel.kt
    └── TeacherViewModel.kt     (includes DashboardStats data class)
```

## Where Things Live

- **DB schema** → `data/local/AppDatabase.kt`, `data/model/Book.kt` (@Entity)
- **API contract** → `data/repository/*.kt` (Firestore collections: books, users, transactions, reviews)
- **Nav graph** → `res/navigation/nav_graph.xml`
- **Theme / colors** → `res/values/themes.xml`, `res/values/colors.xml`
- **Gemini key** → `app/build.gradle` → `buildConfigField "String", "GEMINI_API_KEY"`

## Setup Required Before Build

See `SETUP_INSTRUCTIONS.md` for full step-by-step. In summary:
1. Firebase: `google-services.json` from Firebase Console
2. Gemini: API key in `app/build.gradle`
3. Fonts: Poppins .ttf files → `res/font/`
4. Lottie: JSON animation files → `res/raw/`

## Architecture Decisions

- **Dual DB strategy**: Firestore for real-time sync + Room for offline book cache
- **Safe Args**: Navigation type-safety via `bookId: String` argument in nav graph
- **Sealed Resource**: `Resource<T>` (Loading/Success/Error) for all async state
- **Hilt everywhere**: All ViewModels, Repositories, and Services injected via Hilt
- **Firestore collections**: `books/{schoolId}`, `users/{uid}`, `transactions/{id}`, `reviews/{id}`
- **Role-based routing**: Auth flow branches to `homeFragment` (student) or `teacherDashboardFragment`

## Gotchas

- Safe Args generates `HomeFragmentDirections`, `CatalogFragmentDirections`, `BookDetailFragmentArgs` at build time — do NOT create manual stubs
- `Book.kt` is both `@Entity` (Room) and Firestore-compatible (`@DocumentId`) — Firestore ignores Room annotations
- `OnboardingPage` implements `java.io.Serializable` for Bundle passing via ViewPager2 fragments
- FCM Service class: `com.nammapustaka.service.NammaFcmService` (not `utils.NammaFcmService`)
- Lottie animation resources (`anim_*.json`) must be downloaded from LottieFiles.com manually
- Font files (`poppins_*.ttf`) must be downloaded from Google Fonts manually
