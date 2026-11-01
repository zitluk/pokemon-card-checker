# Pokemon Card Checker

Offline-first Pokémon TCG collection manager for Android (Kotlin, Jetpack Compose, Material 3).

## Requirements

- Android Studio (latest stable)
- JDK 17
- Android SDK **API 35** platform + build-tools
- Emulator or device (minSdk 26)

## Build

```bash
cd ~/Projects/pokemon-card-checker
export JAVA_HOME="$(/usr/libexec/java_home -v 17 2>/dev/null || echo /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home)"
./gradlew assembleDebug
```

## Run

Open this folder in Android Studio, sync Gradle, select an **API 35** emulator, press **Run**.

## API attribution

- Card catalog and indicative prices: [TCGdex API v2](https://tcgdex.dev/) (no API key required for basic REST).
- Prices are **estimates** from third-party marketplaces (Cardmarket EUR shown by default).

## Disclaimers

Fan-made app; not affiliated with The Pokémon Company, Nintendo, or Creatures.
