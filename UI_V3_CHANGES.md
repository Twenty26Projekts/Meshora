# BitChat UI v3

This project is based on the BitChat Android source only. No SharkLink source files or communication code were merged.

## UI changes
- Primary brand color changed to `#0957D0`.
- Redesigned first-run welcome/onboarding entry screen.
- Added a persisted first-run welcome completion flag.
- Existing BitChat permission/onboarding flow remains the functional source of truth after the welcome screen.
- Existing Dark/Light theme preference remains in use.
- Native Android splash screen added using AndroidX SplashScreen.
- Splash uses the BitChat logo recolored to `#0957D0` and follows light/dark system appearance.
- Existing Nearby / Chats / People / Profile UI remains in place.
- Bottom navigation remains background-neutral with no pink tonal surface.
- Communication/mesh/BLE code was not replaced.

## Build verification
A local Gradle compile was attempted, but this environment cannot download Gradle 9.4.1 from services.gradle.org. Therefore no successful build claim is made here.
