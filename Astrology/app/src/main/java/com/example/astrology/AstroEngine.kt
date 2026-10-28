package com.example.astrology

import io.github.cosinekitty.astronomy.Aberration
import io.github.cosinekitty.astronomy.Body
import io.github.cosinekitty.astronomy.Direction
import io.github.cosinekitty.astronomy.Observer
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.eclipticGeoMoon
import io.github.cosinekitty.astronomy.geoVector
import io.github.cosinekitty.astronomy.rotationEqdEct
import io.github.cosinekitty.astronomy.rotationEqjEct
import io.github.cosinekitty.astronomy.searchRiseSet
import io.github.cosinekitty.astronomy.siderealTime
import io.github.cosinekitty.astronomy.sunPosition
import java.util.TimeZone
import kotlin.math.*

val SIGNS = listOf(
    "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
    "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
)
val SIGN_LORDS = listOf("Mars", "Venus", "Mercury", "Moon", "Sun", "Mercury", "Venus", "Mars", "Jupiter", "Saturn", "Saturn", "Jupiter")

val NAKSHATRAS = listOf(
    "Ashwini", "Bharani", "Krittika", "Rohini", "Mrigashira", "Ardra", "Punarvasu", "Pushya", "Ashlesha",
    "Magha", "Purva Phalguni", "Uttara Phalguni", "Hasta", "Chitra", "Swati", "Vishakha", "Anuradha", "Jyeshtha",
    "Mula", "Purva Ashadha", "Uttara Ashadha", "Shravana", "Dhanishta", "Shatabhisha", "Purva Bhadrapada",
    "Uttara Bhadrapada", "Revati"
)

val DASHA_ORDER = listOf("Ketu", "Venus", "Sun", "Moon", "Mars", "Rahu", "Jupiter", "Saturn", "Mercury")
val DASHA_YEARS = mapOf(
    "Ketu" to 7.0, "Venus" to 20.0, "Sun" to 6.0, "Moon" to 10.0, "Mars" to 7.0,
    "Rahu" to 18.0, "Jupiter" to 16.0, "Saturn" to 19.0, "Mercury" to 17.0
)

val PLANET_ABBR = mapOf(
    "Sun" to "Su", "Moon" to "Mo", "Mars" to "Ma", "Mercury" to "Me", "Jupiter" to "Ju",
    "Venus" to "Ve", "Saturn" to "Sa", "Rahu" to "Ra", "Ketu" to "Ke"
)

data class PlanetPos(val name: String, val lon: Double, val retrograde: Boolean = false) {
    val sign: Int get() = (lon / 30).toInt() % 12
    val degInSign: Double get() = lon % 30
    val nakshatra: Int get() = (lon / (360.0 / 27)).toInt() % 27
    val pada: Int get() = ((lon % (360.0 / 27)) / (360.0 / 108)).toInt() + 1
}

/** [fullStartMillis] differs from [startMillis] only for the first (partial, balance-at-birth) Mahadasha. */
data class DashaPeriod(
    val lord: String,
    val startMillis: Long,
    val endMillis: Long,
    val fullStartMillis: Long = startMillis
)

data class Ingress(val planet: String, val millis: Long, val sign: Int)

data class Kundli(
    val ascendant: Double,
    val planets: List<PlanetPos>,
    val ayanamsa: Double,
    val dashas: List<DashaPeriod>,
    val midheaven: Double = 0.0,
    val birthMillis: Long = 0L,
    val sunrise: Long? = null,
    val sunset: Long? = null,
    val tzOffsetMs: Int = 0,
    val trueNode: Boolean = false
) {
    val ashtakavarga: Ashtakavarga.Result by lazy { Ashtakavarga.compute(this) }
    val chandra: PlanetPos get() = planet("Moon")
    val lagnaSign: Int get() = (ascendant / 30).toInt() % 12
    fun planet(name: String) = planets.first { it.name == name }
    fun houseOf(lon: Double): Int = ((((lon / 30).toInt() % 12) - lagnaSign + 12) % 12) + 1
    fun houseOf(p: PlanetPos) = houseOf(p.lon)
}

/**
 * Positions come from Astronomy Engine (VSOP87 planets, ELP/MPP02-based Moon, IAU 2006
 * precession/nutation), accurate to about 1 arcminute or better. Longitudes are tropical,
 * true equinox of date, then converted to sidereal using the Lahiri (Chitra Paksha) ayanamsa.
 */
object AstroEngine {
    private const val RAD = PI / 180.0
    private fun norm(x: Double): Double = ((x % 360.0) + 360.0) % 360.0

    /** When true, Rahu/Ketu use the true (oscillating) lunar node instead of the mean node. */
    @Volatile
    var useTrueNode = false

    private val BODIES = listOf(
        "Mars" to Body.Mars, "Mercury" to Body.Mercury, "Jupiter" to Body.Jupiter,
        "Venus" to Body.Venus, "Saturn" to Body.Saturn
    )

    /** Lahiri ayanamsa as defined in Swiss Ephemeris: 23.245524743 deg at JD 2435553.5 plus IAU 2006 precession. */
    fun lahiri(jdTt: Double): Double {
        fun p(t: Double) = 5028.796195 * t + 1.1054348 * t * t
        val t = (jdTt - 2451545.0) / 36525.0
        val t0 = (2435553.5 - 2451545.0) / 36525.0
        return 23.245524743 + (p(t) - p(t0)) / 3600.0
    }

    private fun tropicalEcliptic(body: Body, time: Time): Double {
        val v = geoVector(body, time, Aberration.Corrected)
        return rotationEqjEct(time).rotate(v).toSpherical().lon
    }

    private fun speedSign(body: Body, time: Time): Boolean {
        var d = tropicalEcliptic(body, time.addDays(0.5)) - tropicalEcliptic(body, time.addDays(-0.5))
        if (d > 180) d -= 360
        if (d < -180) d += 360
        return d < 0
    }

    private fun nodeLongitude(time: Time): Double {
        val t = time.tt / 36525.0
        val mean = norm(125.0445479 - 1934.1362891 * t + 0.0020754 * t * t)
        if (!useTrueNode) return mean
        val d = norm(297.8501921 + 445267.1114034 * t) * RAD
        val m = norm(357.5291092 + 35999.0502909 * t) * RAD
        val mp = norm(134.9633964 + 477198.8675055 * t) * RAD
        val f = norm(93.2720950 + 483202.0175233 * t) * RAD
        return norm(
            mean - 1.4979 * sin(2 * (d - f)) - 0.1500 * sin(m) - 0.1226 * sin(2 * d) +
                0.1176 * sin(2 * f) - 0.0801 * sin(2 * (mp - f))
        )
    }

    fun tropicalPositions(utcMillis: Long): Map<String, Double> {
        val time = Time.fromMillisecondsSince1970(utcMillis)
        val out = linkedMapOf<String, Double>()
        out["Sun"] = sunPosition(time).elon
        out["Moon"] = eclipticGeoMoon(time).lon
        for ((n, b) in BODIES) out[n] = tropicalEcliptic(b, time)
        val t = time.tt / 36525.0
        val rahu = nodeLongitude(time)
        out["Rahu"] = rahu
        out["Ketu"] = norm(rahu + 180.0)
        return out
    }

    fun ascendantTropical(utcMillis: Long, lat: Double, lon: Double): Double {
        val time = Time.fromMillisecondsSince1970(utcMillis)
        val lst = norm(siderealTime(time) * 15.0 + lon)
        // cos(true obliquity) is the [1][1] element of the equatorial-to-ecliptic rotation
        val eps = acos(rotationEqdEct(time).rot[1][1])
        val l = lst * RAD
        val e = eps
        val asc = atan2(cos(l), -(sin(l) * cos(e) + tan(lat * RAD) * sin(e))) / RAD
        return norm(asc)
    }

    fun midheavenTropical(utcMillis: Long, lon: Double): Double {
        val time = Time.fromMillisecondsSince1970(utcMillis)
        val l = norm(siderealTime(time) * 15.0 + lon) * RAD
        val e = acos(rotationEqdEct(time).rot[1][1])
        return norm(atan2(sin(l), cos(l) * cos(e)) / RAD)
    }

    fun compute(utcMillis: Long, lat: Double, lon: Double, tz: TimeZone): Kundli {
        val time = Time.fromMillisecondsSince1970(utcMillis)
        val ayan = lahiri(time.tt + 2451545.0)
        val trop = tropicalPositions(utcMillis)
        val retro = BODIES.filter { speedSign(it.second, time) }.map { it.first }.toSet() + setOf("Rahu", "Ketu")
        val planets = trop.map { (n, l) -> PlanetPos(n, norm(l - ayan), n in retro) }
        val asc = norm(ascendantTropical(utcMillis, lat, lon) - ayan)
        val moonLon = planets.first { it.name == "Moon" }.lon
        val mc = norm(midheavenTropical(utcMillis, lon) - ayan)
        val (rise, set) = sunriseSunset(utcMillis, lat, lon, tz)
        return Kundli(
            asc, planets, ayan, vimshottari(moonLon, utcMillis), mc, utcMillis, rise, set,
            tz.getOffset(utcMillis), useTrueNode
        )
    }

    private val MS_PER_YEAR = 365.25 * 86400000.0

    /** Sidereal (Lahiri) longitudes of all nine grahas at the given instant. */
    fun siderealAt(utcMillis: Long): List<PlanetPos> {
        val time = Time.fromMillisecondsSince1970(utcMillis)
        val ayan = lahiri(time.tt + 2451545.0)
        return tropicalPositions(utcMillis).map { (n, l) -> PlanetPos(n, norm(l - ayan)) }
    }

    private fun siderealLon(name: String, utcMillis: Long): Double {
        val time = Time.fromMillisecondsSince1970(utcMillis)
        val ayan = lahiri(time.tt + 2451545.0)
        val trop = when (name) {
            "Rahu", "Ketu" -> {
                val t = time.tt / 36525.0
                val r = norm(125.0445479 - 1934.1362891 * t + 0.0020754 * t * t)
                if (name == "Rahu") r else norm(r + 180.0)
            }
            else -> tropicalEcliptic(BODIES.first { it.first == name }.second, time)
        }
        return norm(trop - ayan)
    }

    /** Sign changes of the slow planets between [fromMillis] and [fromMillis] + [days]; times accurate to ~1 hour. */
    fun ingresses(fromMillis: Long, days: Int, names: List<String>): List<Ingress> {
        val day = 86400000L
        val out = mutableListOf<Ingress>()
        for (n in names) {
            var prevT = fromMillis
            var prevSign = (siderealLon(n, prevT) / 30).toInt()
            for (i in 1..days) {
                val t = fromMillis + i * day
                val s = (siderealLon(n, t) / 30).toInt()
                if (s != prevSign) {
                    var lo = prevT
                    var hi = t
                    repeat(12) {
                        val mid = (lo + hi) / 2
                        if ((siderealLon(n, mid) / 30).toInt() == prevSign) lo = mid else hi = mid
                    }
                    out += Ingress(n, hi, s)
                    prevSign = s
                }
                prevT = t
            }
        }
        return out.sortedBy { it.millis }
    }

    /** Sunrise and sunset (UTC millis) for the local civil day containing [utcMillis]; null at polar extremes. */
    fun sunriseSunset(utcMillis: Long, lat: Double, lon: Double, tz: TimeZone): Pair<Long?, Long?> {
        val offset = tz.getOffset(utcMillis)
        val localMidnight = Math.floorDiv(utcMillis + offset, 86400000L) * 86400000L - offset
        val start = Time.fromMillisecondsSince1970(localMidnight)
        val obs = Observer(lat, lon, 0.0)
        val rise = searchRiseSet(Body.Sun, obs, Direction.Rise, start, 1.0)?.toMillisecondsSince1970()
        val set = searchRiseSet(Body.Sun, obs, Direction.Set, start, 1.0)?.toMillisecondsSince1970()
        return rise to set
    }

    /** Antardasha / Pratyantardasha split of the full parent period (proportional to Vimshottari years). */
    fun subPeriods(parent: DashaPeriod): List<DashaPeriod> {
        val full = parent.endMillis - parent.fullStartMillis
        var idx = DASHA_ORDER.indexOf(parent.lord)
        var start = parent.fullStartMillis
        val out = mutableListOf<DashaPeriod>()
        repeat(9) {
            val lord = DASHA_ORDER[idx % 9]
            val len = (full * DASHA_YEARS.getValue(lord) / 120.0).toLong()
            out += DashaPeriod(lord, start, start + len)
            start += len
            idx++
        }
        return out
    }

    private fun vimshottari(moonLon: Double, birth: Long): List<DashaPeriod> {
        val span = 360.0 / 27
        val nak = (moonLon / span).toInt()
        val frac = (moonLon % span) / span
        val msPerYear = 365.25 * 86400000.0
        var idx = nak % 9
        val out = mutableListOf<DashaPeriod>()
        var start = birth
        var first = true
        repeat(9) {
            val lord = DASHA_ORDER[idx % 9]
            val years = DASHA_YEARS.getValue(lord) * (if (first) 1 - frac else 1.0)
            val end = start + (years * msPerYear).toLong()
            val fullStart = if (first) end - (DASHA_YEARS.getValue(lord) * msPerYear).toLong() else start
            out += DashaPeriod(lord, start, end, fullStart)
            start = end
            idx++
            first = false
        }
        return out
    }
}
