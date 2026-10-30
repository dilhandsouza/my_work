package elendor.vedicastrology

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
/** Classical Vedic (Jyotish) details: Panchang, Navamsa, Bhava table, Drishti, combustion, extra yogas. */
object Vedic {

    fun houseOf(sign: Int, base: Int): Int = ((sign - base + 12) % 12) + 1

    private val TITHIS = listOf(
        "Pratipada", "Dwitiya", "Tritiya", "Chaturthi", "Panchami", "Shashthi", "Saptami", "Ashtami",
        "Navami", "Dashami", "Ekadashi", "Dwadashi", "Trayodashi", "Chaturdashi", "Purnima"
    )
    private val YOGAS = listOf(
        "Vishkambha", "Priti", "Ayushman", "Saubhagya", "Shobhana", "Atiganda", "Sukarma", "Dhriti", "Shula",
        "Ganda", "Vriddhi", "Dhruva", "Vyaghata", "Harshana", "Vajra", "Siddhi", "Vyatipata", "Variyan",
        "Parigha", "Shiva", "Siddha", "Sadhya", "Shubha", "Shukla", "Brahma", "Indra", "Vaidhriti"
    )
    private val KARANAS = listOf("Bava", "Balava", "Kaulava", "Taitila", "Gara", "Vanija", "Vishti")
    private val VARAS = listOf(
        "Ravivara (Sunday, Sun)", "Somavara (Monday, Moon)", "Mangalavara (Tuesday, Mars)",
        "Budhavara (Wednesday, Mercury)", "Guruvara (Thursday, Jupiter)", "Shukravara (Friday, Venus)",
        "Shanivara (Saturday, Saturn)"
    )

    fun navamsaSign(lon: Double): Int = (lon * 9 / 30).toInt() % 12

    fun lordOfHouse(k: Kundli, house: Int): String = SIGN_LORDS[(k.lagnaSign + house - 1) % 12]

    fun housesOwned(k: Kundli, planet: String): List<Int> = (1..12).filter { lordOfHouse(k, it) == planet }

    private fun time(ms: Long?, tz: TimeZone): String {
        if (ms == null) return Lang.t("n/a")
        val f = SimpleDateFormat("HH:mm", Locale.getDefault())
        f.timeZone = tz
        return f.format(Date(ms))
    }

    fun panchang(k: Kundli, birthMillis: Long, sunrise: Long?, sunset: Long?, tz: TimeZone): String {
        val sun = k.planet("Sun").lon
        val moon = k.planet("Moon")
        val diff = ((moon.lon - sun) % 360 + 360) % 360
        val t = (diff / 12).toInt().coerceIn(0, 29)
        val paksha = if (t < 15) "Shukla (waxing)" else "Krishna (waning)"
        val tithiName = if (t == 29) "Amavasya" else TITHIS[t % 15]
        val yoga = YOGAS[((((sun + moon.lon) % 360) / (360.0 / 27)).toInt()) % 27]
        val kar = (diff / 6).toInt().coerceIn(0, 59)
        val karana = when {
            kar == 0 -> "Kimstughna"
            kar == 57 -> "Shakuni"
            kar == 58 -> "Chatushpada"
            kar == 59 -> "Naga"
            else -> KARANAS[(kar - 1) % 7]
        }
        val cal = Calendar.getInstance(tz).apply { timeInMillis = birthMillis }
        var dow = cal.get(Calendar.DAY_OF_WEEK)
        if (sunrise != null && birthMillis < sunrise) dow = if (dow == 1) 7 else dow - 1
        val nakLord = DASHA_ORDER[moon.nakshatra % 9]
        return buildString {
            append(Lang.t("Vara (weekday): %s\n", Lang.t(VARAS[dow - 1])))
            append(Lang.t("Tithi: %s %s\n", Lang.t(paksha), Lang.t(tithiName)))
            append(Lang.t("Nakshatra: %s, pada %d (lord %s)\n", Lang.nak(moon.nakshatra), moon.pada, Lang.planet(nakLord)))
            append(Lang.t("Yoga: %s\n", Lang.t(yoga)))
            append(Lang.t("Karana: %s\n", Lang.t(karana)))
            append(Lang.t("Moon sign (Rashi): %s (lord %s)\n", Lang.sign(moon.sign), Lang.planet(SIGN_LORDS[moon.sign])))
            append(Lang.t("Sunrise %s, sunset %s (local)\n", time(sunrise, tz), time(sunset, tz)))
            append(Lang.t("The Vedic day starts at sunrise, so births before sunrise belong to the previous weekday."))
        }
    }

    fun navamsaPlacements(k: Kundli): List<Pair<String, Int>> = k.planets.map { it.name to navamsaSign(it.lon) }

    fun navamsaText(k: Kundli): String = buildString {
        append(Lang.t("Navamsa (D9) Lagna: %s\n", Lang.sign(navamsaSign(k.ascendant))))
        for (p in k.planets) {
            val n = navamsaSign(p.lon)
            append(Lang.t(
                "%sD9 %s%s\n",
                Lang.pad(Lang.planet(p.name), 9), Lang.pad(Lang.sign(n), 12),
                if (n == p.sign) Lang.t("  Vargottama") else ""
            ))
        }
        append("\n")
        append(Lang.t("The Navamsa shows the inner strength of planets and the quality of marriage and dharma."))
        append(" ")
        append(Lang.t("Vargottama planets (same sign in D1 and D9) give strong, stable results."))
    }

    fun moonPlacements(k: Kundli): List<Pair<String, Int>> = k.planets.map { it.name to it.sign }

    fun bhavaTable(k: Kundli): String = buildString {
        for (h in 1..12) {
            val sign = (k.lagnaSign + h - 1) % 12
            val lord = SIGN_LORDS[sign]
            val lh = k.houseOf(k.planet(lord))
            val occ = k.planets.filter { k.houseOf(it) == h }.joinToString(", ") { it.name }
            append(Lang.t(
                "H%-2d %slord %sin H%-2d  %s\n",
                h, Lang.pad(Lang.sign(sign), 13), Lang.pad(Lang.planet(lord), 9), lh,
                if (occ.isEmpty()) "-" else occ.split(", ").joinToString(", ") { Lang.planet(it) }
            ))
            append(Lang.t("     %s\n", Lang.t(Insights.houseMeaning[h - 1])))
        }
    }

    private fun aspectOffsets(name: String): List<Int> = when (name) {
        "Mars" -> listOf(4, 7, 8)
        "Jupiter" -> listOf(5, 7, 9)
        "Saturn" -> listOf(3, 7, 10)
        "Rahu", "Ketu" -> listOf(5, 9)
        else -> listOf(7)
    }

    fun aspects(k: Kundli): String = buildString {
        for (p in k.planets) {
            val h = k.houseOf(p)
            val targets = aspectOffsets(p.name).map { (h - 1 + it - 1) % 12 + 1 }
            val desc = targets.joinToString("; ") { th ->
                val occ = k.planets.filter { it.name != p.name && k.houseOf(it) == th }.map { Lang.planet(it.name) }
                Lang.t("H%d", th) + if (occ.isEmpty()) "" else Lang.t(" (%s)", occ.joinToString(","))
            }
            append(Lang.t("%s (H%d) aspects %s\n", Lang.planet(p.name), h, desc))
        }
    }

    private val combustOrb = mapOf("Moon" to 12.0, "Mars" to 17.0, "Mercury" to 14.0, "Jupiter" to 11.0, "Venus" to 10.0, "Saturn" to 15.0)

    fun combustion(k: Kundli): String {
        val sun = k.planet("Sun").lon
        val lines = mutableListOf<String>()
        for ((n, orb) in combustOrb) {
            val p = k.planet(n)
            var d = abs(p.lon - sun)
            if (d > 180) d = 360 - d
            val lim = when {
                n == "Mercury" && p.retrograde -> 12.0
                n == "Venus" && p.retrograde -> 8.0
                else -> orb
            }
            if (d < lim) lines += Lang.t(
                "%s is combust (%.1f° from the Sun): its significations are weakened or overshadowed by ego/authority themes.",
                Lang.planet(n), d
            )
        }
        val retro = k.planets.filter { it.retrograde && it.name != "Rahu" && it.name != "Ketu" }.map { it.name }
        if (retro.isNotEmpty()) lines += Lang.t(
            "Retrograde (Vakri) at birth: %s – intensified, internalised or delayed results of these planets.",
            retro.joinToString(", ") { Lang.planet(it) }
        )
        return if (lines.isEmpty()) Lang.t("No combust or retrograde planets.") else lines.joinToString("\n\n")
    }

    fun extraYogas(k: Kundli): String {
        val out = mutableListOf<String>()
        val kendra = listOf(1, 4, 7, 10)
        val trikona = listOf(1, 5, 9)
        val seen = mutableSetOf<String>()
        for (kh in kendra) for (th in trikona) {
            val a = lordOfHouse(k, kh)
            val b = lordOfHouse(k, th)
            if (a == b || !seen.add(listOf(a, b).sorted().joinToString())) continue
            if (k.planet(a).sign == k.planet(b).sign) {
                out += Lang.t(
                    "Raja Yoga: lords of house %d (%s) and house %d (%s) are together in %s – rise in status, authority and recognition.",
                    kh, Lang.planet(a), th, Lang.planet(b), Lang.sign(k.planet(a).sign)
                )
            }
        }
        val l2 = lordOfHouse(k, 2)
        val l11 = lordOfHouse(k, 11)
        if (l2 != l11 && k.planet(l2).sign == k.planet(l11).sign) {
            out += Lang.t(
                "Dhana Yoga: lords of the 2nd (%s) and 11th (%s) are together – good potential for wealth and gains.",
                Lang.planet(l2), Lang.planet(l11)
            )
        }
        for (dh in listOf(6, 8, 12)) {
            val l = lordOfHouse(k, dh)
            if (k.houseOf(k.planet(l)) in listOf(6, 8, 12)) {
                out += Lang.t(
                    "Viparita Raja Yoga: lord of house %d (%s) sits in a dusthana – success after initial difficulties and reversals.",
                    dh, Lang.planet(l)
                )
            }
        }
        val moon = k.planet("Moon")
        val mh = k.houseOf(moon)
        val around = k.planets.filter {
            it.name !in listOf("Sun", "Moon", "Rahu", "Ketu") &&
                (k.houseOf(it) == mh || (k.houseOf(it) - mh + 12) % 12 in listOf(1, 11))
        }
        if (around.isEmpty()) out += Lang.t("Kemadruma Yoga: no planet near the Moon – periods of emotional solitude; strong support systems and routine help.")
        return if (out.isEmpty()) "" else out.joinToString("\n\n")
    }
}
