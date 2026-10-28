"""Extra checks:
1. Ascendant via independent horizon geometry (the ecliptic point at the ascendant must lie on
   the horizon, in the east, rising).
2. Sidereal Sun entering Aries (Mesha Sankranti 2024) vs the published Panchang time.
Run: python verify_extra.py
"""
import math
from datetime import datetime, timedelta

import astronomy as ast
from verify import CASES, ascendant, lahiri


def horizon_check(t, lat, lon, asc):
    obs = ast.Observer(lat, lon, 0.0)
    rot = ast.CombineRotation(ast.Rotation_ECT_EQD(t), ast.Rotation_EQD_HOR(t, obs))
    r = math.radians(asc)
    v = ast.Vector(math.cos(r), math.sin(r), 0.0, t)
    sph = ast.SphereFromVector(ast.RotateVector(rot, v))
    return sph.lat, sph.lon  # altitude, azimuth (deg)


for s, lat, lon in CASES:
    dt = datetime.strptime(s, "%Y-%m-%d %H:%M")
    t = ast.Time.Make(dt.year, dt.month, dt.day, dt.hour, dt.minute, 0)
    a = ascendant(t, lat, lon)
    alt, az = horizon_check(t, lat, lon, a)
    print(f"{s}  asc {a:9.5f}  altitude {alt:+.5f} deg  azimuth {az:7.2f} deg")


def sid_sun(dt):
    t = ast.Time.Make(dt.year, dt.month, dt.day, dt.hour, dt.minute, 0)
    return (ast.SunPosition(t).elon - lahiri(t.tt + 2451545.0) + 180) % 360 - 180


lo, hi = datetime(2024, 4, 13, 0, 0), datetime(2024, 4, 14, 0, 0)
for _ in range(30):
    mid = lo + (hi - lo) / 2
    if sid_sun(mid) < 0:
        lo = mid
    else:
        hi = mid
ist = lo + timedelta(hours=5, minutes=30)
print(f"\nSun enters sidereal Aries: {ist:%Y-%m-%d %H:%M} IST (published Panchang: 13 Apr 2024 ~21:06 IST)")
