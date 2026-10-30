# Shadbala verification against reference software

The app's Shadbala ([Shadbala.kt](app/src/main/java/com/example/astrology/Shadbala.kt)) was compared with
**PyJHora 4.8.7**, a Python port of Jagannatha Hora, which uses Swiss Ephemeris (`pyswisseph 2.10.3.2`) and the
Lahiri ayanamsa. Three charts were compared, all values in rupas:

| Chart | Date / time (IST) | Place |
|---|---|---|
| Mumbai | 1990-05-15 04:30 (before sunrise, night) | 19.0760 N, 72.8777 E |
| Delhi | 1985-11-02 14:15 (day) | 28.6139 N, 77.2090 E |
| Chennai | 2000-01-01 22:10 (night) | 13.0827 N, 80.2707 E |

## Result

| Component | Planets within tolerance | Tolerance (rupa) | Worst difference |
|---|---|---|---|
| Naisargika | 21 / 21 | 0.01 | 0.00 |
| Dig | 21 / 21 | 0.05 | 0.00 |
| Cheshta (Mars to Saturn) | 15 / 15 | 0.10 | 0.08 |
| Sthana | 21 / 21 | 0.25 | 0.20 |
| Drik | 20 / 21 | 0.30 | 0.47 (Moon, Delhi) |
| Kala | not tolerance-checked, see below | | |

Sthana differs by up to 0.2 rupa because the two programs place the exaltation points, divisional-chart
boundaries and ephemeris slightly differently. The one Drik outlier is the Moon, which depends on whether a waning
Moon counts as benefic or malefic when it aspects.

## Where the reference was not used as truth

These differences were traced to the reference's source code, not guessed.

| Area | PyJHora behaviour | App behaviour (BPHS) |
|---|---|---|
| Dig, Cheshta | `abs(a - b) / 3` with no fold above 180 degrees, so results above 1 rupa appear (for example Mars Dig 1.33). Folded (`2 - v`) before comparing. | Angle folded to 0-180, maximum 60 virupas. |
| Paksha | Moon-Sun elongation not folded for a waning Moon, giving negative malefic values and benefic values above 1 rupa (Mumbai). In Delhi the waning Moon is treated as malefic. | Elongation folded; Moon always counted as benefic and doubled. |
| Hora | Hora lord comes out one position different from the standard count of hours from sunrise (Delhi, Saturday 14:15: Jupiter instead of Saturn). | Hours counted from sunrise, lords in the Chaldean order. |
| Abda / Masa | Awards 15 and 30 virupas to the weekday lord rather than the year and month lords. | Not implemented. |
| Sun / Moon Cheshta | Zero. | Ayana bala (Sun) and Paksha bala (Moon), as in BPHS. |

After the fold fix, Dig and Cheshta agree with the reference to within 0.08 rupa. For Kala, every sub-part matches
the reference (Natonnata, Tribhaga, Vara, Ayana) except the items listed above, so Kala totals differ by up to
1.75 rupas, entirely from those items.

## What is not verified

- Totals (Shadbala Pinda) differ by up to 2.8 rupas against the reference because of Kala and the Sun and Moon
  Cheshta convention. They are not claimed to equal Jagannatha Hora's.
- Cheshta uses Standish mean longitudes instead of the Surya Siddhanta mean planets that Jagannatha Hora uses.
- Abda, Masa and Yuddha bala are not implemented.
- The reference is a port, not the original Windows program, and three charts is a small sample. Compare against
  Jagannatha Hora itself before relying on exact numbers.

## Reproducing

PyJHora needs `pyswisseph`, which has no prebuilt wheel for Python 3.13 on Windows, so use Python 3.11 or 3.10:

```powershell
python -m venv jhvenv
jhvenv\Scripts\python.exe -m pip install PyJHora pyswisseph numpy pytz geopy python-dateutil requests scipy pandas
cd Astrology
..\jhvenv\Scripts\python.exe verify\shadbala_reference.py   # writes verify\reference_shadbala.json
gradle testDebugUnitTest                                    # writes verify\kotlin_shadbala.json
..\jhvenv\Scripts\python.exe verify\compare_shadbala.py
```

`shadbala_reference.py` stubs out `timezonefinder`, because its native `h3` dependency is blocked on some systems;
the timezone is passed explicitly, so the stub does not affect results.

## Ashtakavarga

Checked by unit test: the planet totals are the classical 48, 49, 39, 54, 56, 52 and 39, summing to 337.
