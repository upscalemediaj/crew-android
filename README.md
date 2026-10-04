# Crew Android

Installable Android shell for the Crew website.

## What it does
- Loads your existing Crew PHP website inside an Android WebView.
- Remembers the Crew login/session in the app.
- First launch asks for your HTTPS Crew website URL.
- Allows camera and microphone access for Crew voice/video calls.
- Provides back, reload, home and site-settings controls.
- Protected streaming providers (Netflix, Prime Video, Disney+, Max, Hulu, Apple TV+, Peacock, Paramount+) open in the device's supported browser/app because their DRM playback is not guaranteed inside Android WebView.

## Build an APK in Android Studio
1. Install current Android Studio.
2. Open this folder as a project.
3. Wait for Gradle sync.
4. Build > Build APK(s).
5. Android Studio shows the path to `app-debug.apk`.

For a release build, use Build > Generate Signed App Bundle / APK and create your own signing key.

## Important
This app does not collect Netflix/Prime/etc. credentials. Those services open in their official supported browser/app. Crew credentials remain inside the Crew website session.

## GitHub Actions build fix

For GitHub builds, use the included `.github/workflows/build-apk.yml`. It installs JDK 17, Android SDK 35, and Gradle 8.9 directly and does not require a `gradlew` wrapper. See `README-GITHUB-BUILD.md`.
