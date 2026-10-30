# Quick start

## 1. Toolchain (Windows)

| Tool | Version used | Install |
|---|---|---|
| JDK | Temurin 17 | `winget install EclipseAdoptium.Temurin.17.JDK` |
| Android SDK | platform 33, build-tools 33.0.2, platform-tools | `sdkmanager "platform-tools" "platforms;android-33" "build-tools;33.0.2"` |
| Gradle | 7.6.4 (no wrapper in this project) | download from services.gradle.org |

Create `local.properties` next to `build.gradle`:

```
sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk
```

## 2. Build and test

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
cd Astrology
gradle testDebugUnitTest assembleDebug
```

Output: `app\build\outputs\apk\debug\app-debug.apk`. Install it with
`adb install -r app\build\outputs\apk\debug\app-debug.apk` or open the folder in Android Studio.

## 3. Sample chart to try

1990-05-15, 04:30, Mumbai (19.0760 N, 72.8777 E), time zone Asia/Kolkata.

The unit tests use this chart. Expected: Ashtakavarga planet totals 48/49/39/54/56/52/39 (sum 337) and Shadbala
totals of roughly 6 to 9 rupas per planet.

## 4. Main API

```kotlin
AstroEngine.compute(utcMillis, lat, lon, tz): Kundli
Ashtakavarga.compute(k): Result           // bav[planet][sign], sav[sign], total
Shadbala.compute(k): Map<String, Result>  // rupas per component, total, required, ratio, rating()
Gochar.transitStrength(moonSign, now): Map<String, Strength>   // rating plus Vedha blocker text
Readings.planetReading / houseReading / lifeAreaReading / dashaReading
Forecast.summary / dashaForecast / monthlyOutlook / areaOutlook / transitReport
```

`Shadbala.Result` values are in rupas (60 virupas = 1 rupa). `rating()` compares the total with the planet's
classical minimum (Sun 6.5, Moon 6, Mars 5, Mercury 7, Jupiter 6.5, Venus 5.5, Saturn 5).

## 5. Verification

- Shadbala vs PyJHora: see [SHADBALA_VERIFICATION.md](SHADBALA_VERIFICATION.md).
- Positions vs JPL Horizons and Panchang: `verify/verify.py`, `verify/verify_extra.py`.
- Limitations: see [README.md](README.md).

## Google Play (AAB) build

Requires JDK 17, Android SDK platform 36 + build-tools 36.0.0, and Gradle 8.11.1 (AGP 8.10.1).

    gradle bundleRelease      # app\build\outputs\bundle\release\app-release.aab
    gradle assembleRelease    # sideload APK

Play checklist (you must do these yourself):
1. Create a developer account at https://play.google.com/console ($25 one-time, identity verification).
2. The package name is `elendor.vedicastrology`. It cannot be changed after the first upload.
3. Enrol in Play App Signing. `release.keystore` is your upload key; back it up with `keystore.properties`.
4. Prepare a 512x512 icon, 1024x500 feature graphic, screenshots, a privacy policy URL, the Data safety form (the app sends the text typed in the birth-place field to the Open-Meteo geocoding API, geocoding-api.open-meteo.com; no account or key is used) and the content rating.
5. New personal accounts must run a closed test with 12+ testers for 14 days before production. Confirm this in the console.
6. Raise `versionCode` for every upload. New apps and updates must target API 36 from 31 Aug 2026 (already done).
