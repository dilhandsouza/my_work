package elendor.vedicastrology

import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.floor
import kotlin.math.min
import kotlin.math.sin

/**
 * Shadbala per Brihat Parashara Hora Shastra for the seven classical planets.
 * Components are computed in virupas and reported in rupas (60 virupas = 1 rupa) and compared with the
 * classical minimum required strength of each planet.
 *
 * Implemented: Sthana (Uchcha, Saptavargaja, Ojayugma, Kendradi, Drekkana), Dig, Kala (Natonnata, Paksha,
 * Tribhaga, Vara, Hora, Ayana), Cheshta, Naisargika and Drik.
 * Not implemented: Abda/Masa lord and Yuddha (planetary war) in Kala.
 */
object Shadbala {
    private const val HOUR = 3600000L
    private const val DAY = 24 * HOUR

    val PLANETS = listOf("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn")

    data class Result(
        val sthana: Double,
        val dig: Double,
        val kala: Double,
        val cheshta: Double,
        val naisargika: Double,
        val drik: Double,
        val total: Double,
        val required: Double
    ) {
        val ratio: Double get() = total / required
        fun rating(): String = when {
            ratio >= 1.5 -> Lang.t("Excellent")
            ratio >= 1.15 -> Lang.t("Strong")
            ratio >= 1.0 -> Lang.t("Adequate")
            ratio >= 0.8 -> Lang.t("Weak")
            else -> Lang.t("Very Weak")
        }
    }

    private val REQUIRED_VIRUPAS = mapOf(
        "Sun" to 390.0, "Moon" to 360.0, "Mars" to 300.0, "Mercury" to 420.0,
        "Jupiter" to 390.0, "Venus" to 330.0, "Saturn" to 300.0
    )

    private val NAISARGIKA = mapOf(
        "Sun" to 60.0, "Moon" to 51.43, "Venus" to 42.86, "Jupiter" to 34.29,
        "Mercury" to 25.71, "Mars" to 17.14, "Saturn" to 8.57
    )

    // Sidereal longitude of the exaltation point of each planet.
    private val EXALT_POINT = mapOf(
        "Sun" to 10.0, "Moon" to 33.0, "Mars" to 298.0, "Mercury" to 165.0,
        "Jupiter" to 95.0, "Venus" to 357.0, "Saturn" to 200.0
    )

    private val FRIENDS = mapOf(
        "Sun" to setOf("Moon", "Mars", "Jupiter"),
        "Moon" to setOf("Sun", "Mercury"),
        "Mars" to setOf("Sun", "Moon", "Jupiter"),
        "Mercury" to setOf("Sun", "Venus"),
        "Jupiter" to setOf("Sun", "Moon", "Mars"),
        "Venus" to setOf("Mercury", "Saturn"),
        "Saturn" to setOf("Mercury", "Venus")
    )
    private val ENEMIES = mapOf(
        "Sun" to setOf("Venus", "Saturn"),
        "Moon" to emptySet(),
        "Mars" to setOf("Mercury"),
        "Mercury" to setOf("Moon"),
        "Jupiter" to setOf("Mercury", "Venus"),
        "Venus" to setOf("Sun", "Moon"),
        "Saturn" to setOf("Sun", "Moon", "Mars")
    )

    // Signs of the moolatrikona: planet to (sign index, start degree, end degree).
    private val MOOLATRIKONA = mapOf(
        "Sun" to Triple(4, 0.0, 20.0), "Moon" to Triple(1, 3.0, 30.0), "Mars" to Triple(0, 0.0, 12.0),
        "Mercury" to Triple(5, 15.0, 20.0), "Jupiter" to Triple(8, 0.0, 10.0),
        "Venus" to Triple(6, 0.0, 15.0), "Saturn" to Triple(10, 0.0, 20.0)
    )

    private val WEEKDAY_LORDS = listOf("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn")
    private val HORA_ORDER = listOf("Sun", "Venus", "Mercury", "Moon", "Saturn", "Jupiter", "Mars")

    private fun norm(x: Double) = ((x % 360.0) + 360.0) % 360.0
    private fun angDist(a: Double, b: Double): Double {
        val d = abs(norm(a) - norm(b))
        return if (d > 180) 360 - d else d
    }
    private fun round2(x: Double) = Math.round(x * 100.0) / 100.0

    private fun houseFrom(sign: Int, base: Int) = (sign - base + 12) % 12 + 1

    private fun isBenefic(name: String, k: Kundli): Boolean = when (name) {
        "Jupiter", "Venus", "Mercury" -> true
        "Moon" -> norm(k.planet("Moon").lon - k.planet("Sun").lon) < 180
        else -> false
    }

    /** Compound (natural + temporary) relation points used by Saptavargaja. */
    private fun relationPoints(planet: String, lord: String, k: Kundli): Double {
        if (lord == planet) return 30.0
        val natural = when (lord) {
            in FRIENDS.getValue(planet) -> 1
            in ENEMIES.getValue(planet) -> -1
            else -> 0
        }
        val h = houseFrom(k.planet(lord).sign, k.planet(planet).sign)
        val temporary = if (h in listOf(2, 3, 4, 10, 11, 12)) 1 else -1
        return when (natural + temporary) {
            2 -> 20.0
            1 -> 15.0
            0 -> 10.0
            -1 -> 4.0
            else -> 2.0
        }
    }

    private fun vargaLords(sign: Int, deg: Double): List<String?> {
        val odd = sign % 2 == 0
        val d2 = if ((deg < 15) == odd) "Sun" else "Moon"
        val d3 = SIGN_LORDS[(sign + (deg / 10).toInt() * 4) % 12]
        val d7start = if (odd) sign else (sign + 6) % 12
        val d7 = SIGN_LORDS[(d7start + (deg / (30.0 / 7)).toInt()) % 12]
        val d9start = when (sign % 4) { 0 -> 0; 1 -> 9; 2 -> 6; else -> 3 }
        val d9 = SIGN_LORDS[(d9start + (deg / (30.0 / 9)).toInt()) % 12]
        val d12 = SIGN_LORDS[(sign + (deg / 2.5).toInt()) % 12]
        val d30 = if (odd) when {
            deg < 5 -> "Mars"; deg < 10 -> "Saturn"; deg < 18 -> "Jupiter"; deg < 25 -> "Mercury"; else -> "Venus"
        } else when {
            deg < 5 -> "Venus"; deg < 12 -> "Mercury"; deg < 20 -> "Jupiter"; deg < 25 -> "Saturn"; else -> "Mars"
        }
        return listOf(d2, d3, d7, d9, d12, d30)
    }

    private fun navamsaSign(p: PlanetPos): Int {
        val start = when (p.sign % 4) { 0 -> 0; 1 -> 9; 2 -> 6; else -> 3 }
        return (start + (p.degInSign / (30.0 / 9)).toInt()) % 12
    }

    private fun sthana(p: PlanetPos, k: Kundli): Double {
        val name = p.name
        val uchcha = angDist(p.lon, EXALT_POINT.getValue(name) + 180) / 3

        val mt = MOOLATRIKONA.getValue(name)
        val d1 = if (p.sign == mt.first && p.degInSign >= mt.second && p.degInSign < mt.third) 45.0
        else relationPoints(name, SIGN_LORDS[p.sign], k)
        val sapta = d1 + vargaLords(p.sign, p.degInSign).sumOf { relationPoints(name, it!!, k) }

        val evenLover = name == "Moon" || name == "Venus"
        fun oddEvenPoints(sign: Int) = if ((sign % 2 == 1) == evenLover) 15.0 else 0.0
        val ojayugma = oddEvenPoints(p.sign) + oddEvenPoints(navamsaSign(p))

        val kendradi = when (houseFrom(p.sign, k.lagnaSign)) {
            1, 4, 7, 10 -> 60.0
            2, 5, 8, 11 -> 30.0
            else -> 15.0
        }
        val part = (p.degInSign / 10).toInt()
        val drekkana = when (name) {
            "Sun", "Mars", "Jupiter" -> if (part == 0) 15.0 else 0.0
            "Mercury", "Saturn" -> if (part == 1) 15.0 else 0.0
            else -> if (part == 2) 15.0 else 0.0
        }
        return uchcha + sapta + ojayugma + kendradi + drekkana
    }

    private fun dig(p: PlanetPos, k: Kundli): Double {
        val strongPoint = when (p.name) {
            "Sun", "Mars" -> k.midheaven
            "Moon", "Venus" -> k.midheaven + 180
            "Mercury", "Jupiter" -> k.ascendant
            else -> k.ascendant + 180
        }
        return (180 - angDist(p.lon, strongPoint)) / 3
    }

    private class DayFrame(val sunrise: Long, val sunset: Long, val birth: Long, val tz: Long) {
        val isDay get() = birth in sunrise until sunset
        val weekdayLord: String get() =
            WEEKDAY_LORDS[Math.floorMod(Math.floorDiv(sunrise + tz, DAY) + 4, 7L).toInt()]
    }

    private fun dayFrame(k: Kundli): DayFrame {
        val tz = k.tzOffsetMs.toLong()
        val localMidnight = Math.floorDiv(k.birthMillis + tz, DAY) * DAY - tz
        var sr = k.sunrise ?: (localMidnight + 6 * HOUR)
        var ss = k.sunset ?: (localMidnight + 18 * HOUR)
        if (k.birthMillis < sr) { sr -= DAY; ss -= DAY }
        return DayFrame(sr, ss, k.birthMillis, tz)
    }

    private fun declination(p: PlanetPos, k: Kundli): Double {
        val trop = Math.toRadians(norm(p.lon + k.ayanamsa))
        return Math.toDegrees(asin(sin(Math.toRadians(23.4393)) * sin(trop)))
    }

    private fun ayana(p: PlanetPos, k: Kundli): Double {
        val dec = declination(p, k)
        val v = when (p.name) {
            "Moon", "Saturn" -> (23.45 - dec) / 46.9 * 60
            "Mercury" -> (23.45 + abs(dec)) / 46.9 * 60
            else -> (23.45 + dec) / 46.9 * 60
        }
        return if (p.name == "Sun") 2 * v else v
    }

    private fun paksha(p: PlanetPos, k: Kundli): Double {
        var e = norm(k.planet("Moon").lon - k.planet("Sun").lon)
        if (e > 180) e = 360 - e
        val benefic = e / 3
        val v = if (p.name in listOf("Jupiter", "Venus", "Mercury", "Moon")) benefic else 60 - benefic
        return if (p.name == "Moon") 2 * v else v
    }

    /** Kala bala components in virupas, in BPHS order (Abda/Masa/Yuddha not implemented). */
    internal fun kalaParts(p: PlanetPos, k: Kundli): Map<String, Double> {
        val f = dayFrame(k)
        val noon = (f.sunrise + f.sunset) / 2
        var away = Math.floorMod(abs(f.birth - noon), DAY).toDouble() / HOUR
        if (away > 12) away = 24 - away
        val natonnata = when (p.name) {
            "Mercury" -> 60.0
            "Sun", "Jupiter", "Venus" -> 60.0 * (12 - away) / 12
            else -> 60.0 * away / 12
        }

        val tribhagaLord = if (f.isDay) {
            val part = min(2, ((f.birth - f.sunrise).toDouble() / (f.sunset - f.sunrise) * 3).toInt())
            listOf("Mercury", "Sun", "Saturn")[part]
        } else {
            val nightLen = f.sunrise + DAY - f.sunset
            val part = min(2, ((f.birth - f.sunset).toDouble() / nightLen * 3).toInt().coerceAtLeast(0))
            listOf("Moon", "Venus", "Mars")[part]
        }
        val tribhaga = if (p.name == "Jupiter" || p.name == tribhagaLord) 60.0 else 0.0

        val vara = if (p.name == f.weekdayLord) 45.0 else 0.0
        val horaIdx = Math.floorMod(HORA_ORDER.indexOf(f.weekdayLord) + ((f.birth - f.sunrise) / HOUR).toInt(), 7)
        val hora = if (p.name == HORA_ORDER[horaIdx]) 60.0 else 0.0

        return linkedMapOf(
            "natonnata" to natonnata, "paksha" to paksha(p, k), "tribhaga" to tribhaga,
            "vara" to vara, "hora" to hora, "ayana" to ayana(p, k)
        )
    }

    private fun kala(p: PlanetPos, k: Kundli): Double = kalaParts(p, k).values.sum()

    // Standish (JPL) mean longitudes at J2000 and rate per Julian century, tropical degrees.
    private val MEAN_LON = mapOf(
        "Mercury" to (252.25032350 to 149472.67411175), "Venus" to (181.97909950 to 58517.81538729),
        "Earth" to (100.46457166 to 35999.37244981), "Mars" to (-4.55343205 to 19140.30268499),
        "Jupiter" to (34.39644051 to 3034.74612775), "Saturn" to (49.95424423 to 1222.49362201)
    )

    private fun meanLon(body: String, t: Double): Double {
        val (l0, rate) = MEAN_LON.getValue(body)
        return norm(l0 + rate * t)
    }

    private fun midAngle(a: Double, b: Double): Double {
        var d = norm(b - a)
        if (d > 180) d -= 360
        return norm(a + d / 2)
    }

    /**
     * Cheshta bala. Sun uses Ayana bala and Moon uses Paksha bala (BPHS). For the other planets it is the
     * Cheshta kendra divided by 3, where kendra = |Sheeghrochcha - average(true, mean)| folded to 0..180.
     * Mars, Jupiter and Saturn use the mean Sun as Sheeghrochcha; Mercury and Venus use their own mean
     * heliocentric longitude, with the mean Sun taking the place of the mean planet.
     */
    private fun cheshta(p: PlanetPos, k: Kundli): Double = min(60.0, when (p.name) {
        "Sun" -> ayana(p, k)
        "Moon" -> paksha(p, k)
        else -> {
            val t = (k.birthMillis / 86400000.0 + 2440587.5 - 2451545.0) / 36525.0
            val meanSun = norm(meanLon("Earth", t) + 180)
            val own = meanLon(p.name, t)
            val trueTropical = norm(p.lon + k.ayanamsa)
            val inner = p.name == "Mercury" || p.name == "Venus"
            val sheeghra = if (inner) own else meanSun
            val meanPlanet = if (inner) meanSun else own
            angDist(sheeghra, midAngle(trueTropical, meanPlanet)) / 3
        }
    })

    private fun aspectVirupa(d: Double, aspecting: String): Double {
        val base = when {
            d < 30 -> 0.0
            d < 60 -> (d - 30) / 2
            d < 90 -> d - 45
            d < 120 -> (120 - d) / 2 + 30
            d < 150 -> 150 - d
            d < 180 -> (d - 150) * 2
            d < 300 -> (300 - d) / 2
            else -> 0.0
        }
        val special = when (aspecting) {
            "Mars" -> d in 90.0..120.0 || d in 210.0..240.0
            "Jupiter" -> d in 120.0..150.0 || d in 240.0..270.0
            "Saturn" -> d in 60.0..90.0 || d in 270.0..300.0
            else -> false
        }
        return if (special) 60.0 else base
    }

    private fun drik(p: PlanetPos, k: Kundli): Double {
        var sum = 0.0
        for (other in PLANETS) {
            if (other == p.name) continue
            val v = aspectVirupa(norm(p.lon - k.planet(other).lon), other)
            sum += if (isBenefic(other, k)) v else -v
        }
        return sum / 4
    }

    fun compute(k: Kundli): Map<String, Result> {
        val out = linkedMapOf<String, Result>()
        for (name in PLANETS) {
            val p = k.planet(name)
            val parts = listOf(sthana(p, k), dig(p, k), kala(p, k), cheshta(p, k), NAISARGIKA.getValue(name), drik(p, k))
            val total = parts.sum()
            out[name] = Result(
                round2(parts[0] / 60), round2(parts[1] / 60), round2(parts[2] / 60),
                round2(parts[3] / 60), round2(parts[4] / 60), round2(parts[5] / 60),
                round2(total / 60), REQUIRED_VIRUPAS.getValue(name) / 60
            )
        }
        return out
    }
}
