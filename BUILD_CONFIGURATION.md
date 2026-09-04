# SharkLink-style BitChat UI — build configuration

This project keeps BitChat's communication/mesh implementation intact and changes the presentation layer only.

## Android build versions

- Android Gradle Plugin: 9.2.0
- Gradle: 9.4.1
- Kotlin: 2.3.10
- compileSdk: 37
- targetSdk: 37
- Build Tools: 36.0.0
- JDK: 21 is used by the project toolchain; AGP 9.2 requires JDK 17+.

## Important

The project previously contained generated dependency locks and Gradle dependency-verification metadata tied to AGP 9.3.1. Those generated files were removed so they cannot force resolution of the old AGP version after downgrading the plugin.

Open the `bitchat-android-main` directory itself in Android Studio.
