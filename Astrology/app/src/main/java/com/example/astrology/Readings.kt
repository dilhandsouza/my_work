package com.example.astrology

/**
 * Detailed readings: planet, house, nakshatra, life-area, and dasha narratives.
 */
object Readings {

    private val SIGN_NAMES = mapOf(
        0 to "Aries", 1 to "Taurus", 2 to "Gemini", 3 to "Cancer", 4 to "Leo", 5 to "Virgo",
        6 to "Libra", 7 to "Scorpio", 8 to "Sagittarius", 9 to "Capricorn", 10 to "Aquarius", 11 to "Pisces"
    )

    private val SIGN_LORDS = mapOf(
        0 to "Mars", 1 to "Venus", 2 to "Mercury", 3 to "Moon", 4 to "Sun", 5 to "Mercury",
        6 to "Venus", 7 to "Mars", 8 to "Jupiter", 9 to "Saturn", 10 to "Saturn", 11 to "Jupiter"
    )

    private val HOUSE_THEMES = mapOf(
        1 to "Self, body, appearance",
        2 to "Wealth, speech, family",
        3 to "Courage, siblings, communication",
        4 to "Mother, home, property, mind",
        5 to "Children, creativity, romance",
        6 to "Health, enemies, service",
        7 to "Partner, spouse, business",
        8 to "Death, inheritance, occult",
        9 to "Fortune, spirituality, travel",
        10 to "Career, status, public image",
        11 to "Gains, friends, community",
        12 to "Loss, isolation, moksha"
    )

    fun planetReading(p: PlanetPos, k: Kundli, shadbala: Map<String, Shadbala.Result>): String {
        val sb = StringBuilder()
        val sign = SIGN_NAMES[p.sign]
        val nk = NAKSHATRAS[p.nakshatra]
        val sb_result = shadbala[p.name]
        val strength = sb_result?.rating() ?: "Unknown"
        val dignity = when {
            p.sign == Insights.exalt[p.name] -> "Exalted"
            p.sign == Insights.debil[p.name] -> "Debilitated"
            SIGN_LORDS[p.sign] == p.name -> "Own sign"
            else -> "Neutral"
        }

        sb.append("${p.name} in $sign (${String.format("%.1f", p.degInSign)}°) / Nakshatra $nk\n")
        sb.append("Dignity: $dignity | Shadbala: $strength | Retrograde: ${p.retrograde}\n")
        if (sb_result != null) {
            sb.append("Strength breakdown: Sthana=${sb_result.sthana} Dig=${sb_result.dig} Kala=${sb_result.kala} ")
            sb.append("Cheshta=${sb_result.cheshta} Naisargika=${sb_result.naisargika} Drik=${sb_result.drik}\n")
        }

        val h = k.houseOf(p)
        val lordName = SIGN_LORDS[p.sign]
        sb.append("House: ${h} | Sign Lord: $lordName\n")

        return sb.toString()
    }

    fun houseReading(h: Int, k: Kundli, now: Long): String {
        val sb = StringBuilder()
        val theme = HOUSE_THEMES[h]
        val lordSign = (k.lagnaSign + h - 1) % 12
        val lordName = SIGN_LORDS[lordSign]
        val lord = k.planets.firstOrNull { it.name == lordName }

        if (lord != null) {
            val lordDig = when {
                lord.sign == Insights.exalt[lord.name] -> "exalted"
                lord.sign == Insights.debil[lord.name] -> "debilitated"
                else -> "neutral"
            }
            sb.append("House $h: $theme | Lord ${lord.name} in ${SIGN_NAMES[lord.sign]} ($lordDig)\n")
        } else {
            sb.append("House $h: $theme\n")
        }

        val transit = AstroEngine.siderealAt(now)
        val transiting = transit.filter { k.houseOf(it) == h }
        if (transiting.isNotEmpty()) {
            sb.append("Current transits: ${transiting.joinToString(", ") { it.name }}\n")
        }

        return sb.toString()
    }

    fun lifeAreaReading(area: String, k: Kundli, shadbala: Map<String, Shadbala.Result>, now: Long): String {
        val (h, theme) = when (area) {
            "Career" -> Pair(10, "profession, status, public image")
            "Marriage" -> Pair(7, "partner, commitment, relations")
            "Wealth" -> Pair(2, "finance, speech, family")
            "Health" -> Pair(1, "body, vitality, constitution")
            "Home" -> Pair(4, "property, mother, foundation")
            "Children" -> Pair(5, "offspring, creativity, romance")
            "Travel" -> Pair(9, "journey, fortune, learning")
            else -> Pair(1, "general life events")
        }

        val sb = StringBuilder()
        sb.append("$area ($theme):\n")

        val lordSign = (k.lagnaSign + h - 1) % 12
        val lordName = SIGN_LORDS[lordSign]
        val houseDiv = k.planets.firstOrNull { it.name == lordName }
        if (houseDiv != null) {
            val strength = shadbala[houseDiv.name]?.rating() ?: "Unknown"
            sb.append("House $h lord (${houseDiv.name} in ${SIGN_NAMES[houseDiv.sign]}): $strength\n")
        }

        val transit = AstroEngine.siderealAt(now)
        val benefics = listOf("Sun", "Moon", "Jupiter", "Venus", "Mercury")
        val malefics = listOf("Mars", "Saturn", "Rahu", "Ketu")
        val inHouse = transit.filter { k.houseOf(it) == h }
        val favorable = inHouse.filter { it.name in benefics }
        val unfavorable = inHouse.filter { it.name in malefics }

        if (favorable.isNotEmpty()) sb.append("Favorable transits: ${favorable.joinToString(", ") { it.name }}\n")
        if (unfavorable.isNotEmpty()) sb.append("Challenging transits: ${unfavorable.joinToString(", ") { it.name }}\n")

        if (inHouse.isEmpty()) sb.append("Overall: No major transits in this house currently.\n")

        return sb.toString()
    }

    fun dashaReading(dasha: String, k: Kundli, start: Long, end: Long): String {
        val sb = StringBuilder()
        val starty = (start - System.currentTimeMillis()) / (365.25 * 24 * 3600 * 1000)

        sb.append("$dasha: ${if (starty < 0) "ended ${-starty.toInt()} years ago" else "starts in ${starty.toInt()} years"}")
        sb.append(" (${String.format("%.1f", (end-start)/(365.25*24*3600*1000))} year duration)\n")

        val planet = dasha.split(" ")[0]
        val p = k.planets.firstOrNull { it.name == planet }
        if (p != null) {
            val dign = when {
                p.sign == Insights.exalt[p.name] -> "exalted → favorable growth"
                p.sign == Insights.debil[p.name] -> "debilitated → challenges to overcome"
                else -> "neutral → steady progress"
            }
            sb.append("$planet is $dign in your chart.\n")
        }

        val theme = when (planet) {
            "Sun" -> "authority, ego, leadership"
            "Moon" -> "emotions, family, security"
            "Mars" -> "energy, courage, conflict"
            "Mercury" -> "communication, learning, trade"
            "Jupiter" -> "wisdom, expansion, prosperity"
            "Venus" -> "creativity, pleasure, marriage"
            "Saturn" -> "discipline, limitation, karma"
            "Rahu" -> "obsession, foreign, innovation"
            "Ketu" -> "liberation, spirituality, loss"
            else -> "unknown"
        }
        sb.append("Focus: $theme\n")
        sb.append("Remedies: chant ${planet.uppercase()} mantra, donate its metal, fast on its day\n")

        return sb.toString()
    }
}
