"""Diff the app's Shadbala (rupas) against PyJHora (Jagannatha Hora port).

Run:
  python shadbala_reference.py      (Python 3.11 venv with PyJHora + pyswisseph; writes reference_shadbala.json)
  gradlew testDebugUnitTest         (writes kotlin_shadbala.json)
  python compare_shadbala.py

PyJHora quirks handled or excluded (verified by reading its source, see ../../SHADBALA_VERIFICATION.md):
  * Dig and Cheshta use abs(angle difference)/3 without folding angles above 180 deg, so values above 1 rupa
    appear. They are folded here (v -> 2 - v for v > 1) before comparing.
  * Sun and Moon Cheshta are 0 in PyJHora; BPHS gives them Ayana/Paksha bala, which the app follows. Excluded.
  * Kala is shown but not tolerance-checked: PyJHora differs on Paksha (unfolded elongation, waning Moon treated
    as malefic), Hora lord and Abda/Masa lords.
"""
import json
import pathlib

here = pathlib.Path(__file__).parent
ref = json.loads((here / "reference_shadbala.json").read_text())
app = json.loads((here / "kotlin_shadbala.json").read_text())

COMPONENTS = ["sthana", "kala", "dig", "cheshta", "naisargika", "drik"]
TOLERANCE = {"sthana": 0.25, "dig": 0.05, "cheshta": 0.1, "naisargika": 0.01, "drik": 0.3}


def fold(v):
    return 2 - v if v > 1 else v


def ref_value(comp, value):
    return fold(value) if comp in ("dig", "cheshta") else value


stats = {c: [0, 0, 0.0] for c in TOLERANCE}  # within, compared, worst
for case in ref:
    print(f"\n{case}   (app / reference, diff in rupas)")
    print(f"{'planet':8}" + "".join(f"{c:>22}" for c in COMPONENTS))
    for planet, r in ref[case].items():
        a = app[case][planet]
        cells = []
        for c in COMPONENTS:
            rv = ref_value(c, r[c])
            diff = a[c] - rv
            cells.append(f"{a[c]:6.2f}/{rv:6.2f}({diff:+.2f})")
            if c in TOLERANCE and not (c == "cheshta" and planet in ("Sun", "Moon")):
                s = stats[c]
                s[1] += 1
                s[0] += abs(diff) <= TOLERANCE[c]
                s[2] = max(s[2], abs(diff))
        print(f"{planet:8}" + "".join(f"{x:>22}" for x in cells))

print("\nAgreement per component (within tolerance, 3 births x 7 planets):")
for c, (ok, n, worst) in stats.items():
    print(f"  {c:11} {ok}/{n} within {TOLERANCE[c]} rupa   worst {worst:.2f}")
