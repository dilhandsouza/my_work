import json
import sys
import types

# timezonefinder needs a native DLL that may be blocked by policy; the timezone is passed explicitly here.
stub = types.ModuleType("timezonefinder")
stub.TimezoneFinder = type("TimezoneFinder", (), {})
sys.modules["timezonefinder"] = stub

import swisseph as swe
from jhora import utils
from jhora.panchanga import drik
from jhora.horoscope.chart import strength

NAMES = ["Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn"]
COMPONENTS = ["sthana", "kala", "dig", "cheshta", "naisargika", "drik"]

CASES = [
    ("Mumbai 1990-05-15 04:30", (1990, 5, 15), (4, 30, 0), 19.076, 72.8777, 5.5),
    ("Delhi 1985-11-02 14:15", (1985, 11, 2), (14, 15, 0), 28.6139, 77.2090, 5.5),
    ("Chennai 2000-01-01 22:10", (2000, 1, 1), (22, 10, 0), 13.0827, 80.2707, 5.5),
]

out = {}
for label, d, t, lat, lon, tz in CASES:
    drik.set_ayanamsa_mode("LAHIRI")
    place = drik.Place(label, lat, lon, tz)
    jd = utils.julian_day_number(drik.Date(*d), t)
    res = strength.shad_bala(jd, place)
    rows = {}
    for i, n in enumerate(NAMES):
        row = {c: round(res[j][i] / 60.0, 2) for j, c in enumerate(COMPONENTS)}
        row["total"] = res[7][i]
        rows[n] = row
    out[label] = rows
import pathlib
with open(pathlib.Path(__file__).with_name("reference_shadbala.json"), "w") as f:
    json.dump(out, f, indent=1)
