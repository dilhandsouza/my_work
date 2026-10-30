"""Cross-check the app's algorithm (Python port using Astronomy Engine) against JPL Horizons.

The Kotlin engine in AstroEngine.kt uses the same Astronomy Engine calls, so this validates
the approach and formulas (ecliptic of date, ayanamsa, ascendant).
Run: python verify.py
"""
import json
import math
import urllib.parse
import urllib.request
from datetime import datetime, timedelta

import astronomy as ast

HORIZONS = "https://ssd.jpl.nasa.gov/api/horizons.api"
IDS = {"Sun": "10", "Moon": "301", "Mercury": "199", "Venus": "299", "Mars": "499",
       "Jupiter": "599", "Saturn": "699"}
CASES = [
    ("1990-05-15 04:30", 19.0760, 72.8777),   # Mumbai
    ("1985-11-02 18:45", 28.6139, 77.2090),   # Delhi
    ("2000-01-01 12:00", 51.5074, -0.1278),   # London
    ("2010-07-23 02:10", 13.0827, 80.2707),   # Chennai
    ("2024-03-10 09:20", 40.7128, -74.0060),  # New York
]


def norm(x):
    return x % 360.0


def lahiri(jd_tt):
    p = lambda t: 5028.796195 * t + 1.1054348 * t * t
    t = (jd_tt - 2451545.0) / 36525.0
    t0 = (2435553.5 - 2451545.0) / 36525.0
    return 23.245524743 + (p(t) - p(t0)) / 3600.0


def ours(dt):
    t = ast.Time.Make(dt.year, dt.month, dt.day, dt.hour, dt.minute, 0)
    out = {"Sun": ast.SunPosition(t).elon, "Moon": ast.EclipticGeoMoon(t).lon}
    rot = ast.Rotation_EQJ_ECT(t)
    for n, b in [("Mercury", ast.Body.Mercury), ("Venus", ast.Body.Venus), ("Mars", ast.Body.Mars),
                 ("Jupiter", ast.Body.Jupiter), ("Saturn", ast.Body.Saturn)]:
        v = ast.GeoVector(b, t, True)
        out[n] = ast.SphereFromVector(ast.RotateVector(rot, v)).lon
    return t, out


def ascendant(t, lat, lon):
    lst = norm(ast.SiderealTime(t) * 15.0 + lon)
    eps = math.acos(ast.Rotation_EQD_ECT(t).rot[1][1])
    l = math.radians(lst)
    return norm(math.degrees(math.atan2(math.cos(l), -(math.sin(l) * math.cos(eps)
                + math.tan(math.radians(lat)) * math.sin(eps)))))


def horizons(body, dt):
    stop = dt + timedelta(minutes=1)
    q = {"format": "json", "COMMAND": f"'{body}'", "OBJ_DATA": "NO", "MAKE_EPHEM": "YES",
         "EPHEM_TYPE": "OBSERVER", "CENTER": "'500@399'", "QUANTITIES": "'31'",
         "START_TIME": f"'{dt:%Y-%m-%d %H:%M}'", "STOP_TIME": f"'{stop:%Y-%m-%d %H:%M}'",
         "STEP_SIZE": "'1m'", "TIME_TYPE": "'UT'", "ANG_FORMAT": "DEG"}
    res = json.load(urllib.request.urlopen(HORIZONS + "?" + urllib.parse.urlencode(q), timeout=60))["result"]
    row = res.split("$$SOE")[1].split("$$EOE")[0].strip().splitlines()[0].split()
    return float(row[-2])  # ObsEcLon: true ecliptic of date, apparent


def main():
    worst = 0.0
    print("Lahiri at J2000 =", f"{lahiri(2451545.0):.5f}", "(Swiss Ephemeris reference ~23.8571)")
    for s, lat, lon in CASES:
        dt = datetime.strptime(s, "%Y-%m-%d %H:%M")
        t, mine = ours(dt)
        print(f"\n{s} UT")
        for n, v in mine.items():
            ref = horizons(IDS[n], dt)
            d = ((v - ref + 180) % 360 - 180) * 3600
            worst = max(worst, abs(d))
            print(f"  {n:8s} ours {v:10.5f}  horizons {ref:10.5f}  diff {d:+7.2f}\"")
        asc = ascendant(t, lat, lon)
        print(f"  Ascendant tropical {asc:.5f}  sidereal {norm(asc - lahiri(t.tt + 2451545.0)):.5f}")
    print(f"\nWorst planetary difference vs JPL Horizons: {worst:.2f} arcsec ({worst / 60:.2f} arcmin)")


if __name__ == "__main__":
    main()
