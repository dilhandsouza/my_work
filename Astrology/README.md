# Vedic Astrology (Kundli) Android app

Takes birth date, time and place and produces a sidereal (Lahiri) Vedic chart with forecasts.

## Features

- **Ephemeris**: Astronomy Engine (VSOP87 planets, ELP/MPP02 Moon, IAU 2006 precession and nutation), Lahiri
  ayanamsa, mean or true lunar node, ascendant and midheaven, sunrise and sunset.
- **Panchang**: tithi, nakshatra, yoga, karana, vara.
- **Charts**: Lagna (D1), Chandra, Navamsa (D9), current transits, in North Indian (diamond, houses fixed) or
  South Indian (4x4 grid, signs fixed, Lagna marked with a corner slash) style. Pick the style with the
  "Chart style" radio buttons under the language selector; it is saved, applies to the screen and the PDF, and
  switching redraws the current report.
- **Vimshottari dasha** to the pratyantardasha level, Sade Sati, Ashtama Shani, monthly and per-area outlook.
- **Ashtakavarga**: Parashari bhinnashtakavarga tables for the seven planets and Sarvashtakavarga (total 337).
- **Shadbala**: BPHS six-fold strength in virupas and rupas, compared with each planet's required minimum.
- **Gochar with Vedha**: transits from the natal Moon with Phaladeepika obstruction houses.
- **Readings**: planet, house, life-area and dasha narratives.
- **Languages**: English and Kannada (ಕನ್ನಡ), chosen with the radio buttons at the top of the screen. The choice is
  saved, and switching re-renders the current report, chart labels and place suggestions. Implementation: `Lang.kt`
  looks up the English template in the Kannada maps (`KnVedic.kt`, `KnForecast.kt`, `KnShadbala.kt`, `KnUi.kt`).
  A missing entry falls back to English. When you change an English string, change its Kannada key too.
  `KannadaCoverageTest` renders 60 charts in Kannada and fails on missing keys or mismatched `%s`/`%d` placeholders.
  **The Kannada text is machine-written and has not been reviewed by a native speaker; have it reviewed before
  publishing.** Numbers stay in Latin digits, and the date/time pickers follow the phone's language.

## Build

Requirements: JDK 17, Android SDK (platform 33, build-tools 33.0.2) and Gradle 7.6.x. There is no Gradle wrapper.

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
cd Astrology
gradle testDebugUnitTest assembleDebug
```

`local.properties` must point at the SDK (`sdk.dir=...`). The APK is written to
`app\build\outputs\apk\debug\app-debug.apk`.

## Tests and verification

- [ClassicalChecksTest.kt](app/src/test/java/com/example/astrology/ClassicalChecksTest.kt): Ashtakavarga classical
  totals, Shadbala plausibility ranges, Vedha smoke test, and a dump used for the reference comparison.
- [SHADBALA_VERIFICATION.md](SHADBALA_VERIFICATION.md): Shadbala compared with PyJHora on three charts.
- `verify/verify.py`, `verify/verify_extra.py`: planetary positions against JPL Horizons, Lahiri ayanamsa against
  Swiss Ephemeris, and Mesha Sankranti 2024 against the published Panchang.

## Known limitations

- Shadbala omits Abda, Masa and Yuddha bala, and Cheshta uses mean-element approximations; totals can differ from
  Jagannatha Hora by a couple of rupas.
- Vedha follows the Phaladeepika table only; other traditions differ.
- The true node uses a short series of correction terms, not the full lunar theory.
- Readings are rule-based text, not predictions; treat them as entertainment and guidance.
- Not yet run on a device or emulator.

Build requirements are now AGP 8.10.1, Gradle 8.11.1, compileSdk/targetSdk 36. See QUICKSTART.md for the AAB and Play checklist.

Birth place: type a city and pick it from the live suggestions (Open-Meteo geocoding). Latitude, longitude and the IANA time zone are filled in automatically; needs internet.
