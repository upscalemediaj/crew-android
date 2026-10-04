# Crew Android — GitHub APK build

This project is arranged so the **repository root** contains:

- `settings.gradle.kts`
- `build.gradle.kts`
- `gradle.properties`
- `app/`
- `.github/workflows/build-apk.yml`

The workflow intentionally **does not use `./gradlew`**, so it cannot fail with `gradlew: No such file or directory`. GitHub Actions installs Gradle 8.9 directly.

## Upload to GitHub

1. Create or open your GitHub repository.
2. Delete the old project files/workflow, or upload these files so they are at the **repository root**.
3. Confirm GitHub shows `app`, `.github`, `settings.gradle.kts`, and `build.gradle.kts` on the first page of the repository.
4. Open **Actions → Build Crew APK → Run workflow**.
5. When the job finishes, download **Crew-debug-apk** from the Artifacts section.
6. Extract the artifact ZIP; inside is `app-debug.apk`.

## Important

Do not keep an old workflow that contains:

```bash
chmod +x gradlew
./gradlew assembleDebug
```

unless you deliberately add a Gradle wrapper. This project uses the system Gradle installed by GitHub Actions instead.
