# Vedic Astrology (Kundli) Android app

Takes birth date, time and place and produces a sidereal (Lahiri) Vedic chart with forecasts.

## Features

- **Ephemeris**: Astronomy Engine (VSOP87 planets, ELP/MPP02 Moon, IAU 2006 precession and nutation), Lahiri
  ayanamsa, mean or true lunar node, ascendant and midheaven, sunrise and sunset.
- **Panchang**: tithi, nakshatra, yoga, karana, vara.
- **Charts**: Lagna (D1), Chandra, Navamsa (D9), current transits.
- **Vimshottari dasha** to the pratyantardasha level, Sade Sati, Ashtama Shani, monthly and per-area outlook.
- **Ashtakavarga**: Parashari bhinnashtakavarga tables for the seven planets and Sarvashtakavarga (total 337).
- **Shadbala**: BPHS six-fold strength in virupas and rupas, compared with each planet's required minimum.
- **Gochar with Vedha**: transits from the natal Moon with Phaladeepika obstruction houses.
- **Readings**: planet, house, life-area and dasha narratives.

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
