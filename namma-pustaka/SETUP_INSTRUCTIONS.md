# Namma Pustaka — Setup Instructions

## 1. Firebase Setup
1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create project named **Namma Pustaka**
3. Add Android app → Package: `com.nammapustaka`
4. Download `google-services.json` → replace placeholder at `app/google-services.json`
5. Enable in Firebase Console:
   - **Authentication** → Email/Password provider
   - **Cloud Firestore** → Start in test mode, then apply security rules
   - **Cloud Storage** → Start in test mode
   - **Cloud Messaging** → No setup needed

## 2. Gemini API Key
1. Go to [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Create an API key
3. Open `app/build.gradle`, replace `YOUR_GEMINI_API_KEY_HERE` with your key in both `debug` and `release` buildTypes

## 3. Poppins Font Files
Download from [Google Fonts](https://fonts.google.com/specimen/Poppins):
- `Poppins-Regular.ttf` → rename → `poppins_regular.ttf` → place in `app/src/main/res/font/`
- `Poppins-Medium.ttf` → rename → `poppins_medium.ttf` → place in `app/src/main/res/font/`
- `Poppins-SemiBold.ttf` → rename → `poppins_semibold.ttf` → place in `app/src/main/res/font/`
- `Poppins-Bold.ttf` → rename → `poppins_bold.ttf` → place in `app/src/main/res/font/`

## 4. Lottie Animation Files
Download from [LottieFiles](https://lottiefiles.com) (free animations):
Place all files in `app/src/main/res/raw/`:

| File name | Search term | Purpose |
|-----------|-------------|---------|
| `anim_loading_dots.json` | "loading dots" | General loading |
| `anim_ai_sparkle.json` | "sparkle magic" | AI feature highlight |
| `anim_ai_avatar.json` | "robot chat assistant" | AI chat avatar |
| `anim_typing_dots.json` | "typing indicator" | AI typing |
| `anim_qr_scan.json` | "qr scan" | QR scanner overlay |
| `anim_success_check.json` | "success checkmark" | Success confirmation |
| `anim_crown.json` | "crown winner" | Leaderboard #1 |
| `anim_onboarding_books.json` | "books reading" | Onboarding page 1 |
| `anim_onboarding_ai.json` | "artificial intelligence" | Onboarding page 2 |
| `anim_onboarding_trophy.json` | "trophy award" | Onboarding page 3 |

## 5. Firestore Security Rules
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
      allow read: if request.auth != null;
    }
    match /books/{bookId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null &&
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'TEACHER';
    }
    match /transactions/{txId} {
      allow read, write: if request.auth != null;
    }
    match /reviews/{reviewId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null;
    }
  }
}
```

## 6. Android Studio Setup
1. Open Android Studio → Open project → select `namma-pustaka/` folder
2. Let Gradle sync complete
3. Connect Android device (API 24+) or start emulator
4. Run → Select `app` configuration

## 7. First Run
- Register as **TEACHER** to add books and manage the library
- Register as **STUDENT** to browse, borrow, and chat with AI
- School ID can be any string (e.g., `SCHOOL_001`) — teachers and students must use same ID

---
*Built with Kotlin + Material Design 3 + Firebase + Gemini AI*
