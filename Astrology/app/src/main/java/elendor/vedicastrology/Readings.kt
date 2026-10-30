package elendor.vedicastrology

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

        sb.append(Lang.t("%1\$s in %2\$s (%3\$.1f\u00b0) / Nakshatra %4\$s\n", Lang.planet(p.name), Lang.sign(p.sign), p.degInSign, Lang.nak(p.nakshatra)))
        sb.append(Lang.t("Dignity: %1\$s | Shadbala: %2\$s | Retrograde: %3\$s\n", Lang.t(dignity), Lang.t(strength), Lang.t(p.retrograde.toString())))
        if (sb_result != null) {
            sb.append(Lang.t("Strength breakdown: Sthana=%1\$s Dig=%2\$s Kala=%3\$s ", sb_result.sthana, sb_result.dig, sb_result.kala))
            sb.append(Lang.t("Cheshta=%1\$s Naisargika=%2\$s Drik=%3\$s\n", sb_result.cheshta, sb_result.naisargika, sb_result.drik))
        }

        val h = k.houseOf(p)
        val lordName = SIGN_LORDS[p.sign]
        sb.append(Lang.t("House: %1\$d | Sign Lord: %2\$s\n", h, Lang.planet(lordName.orEmpty())))

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
            sb.append(Lang.t("House %1\$d: %2\$s | Lord %3\$s in %4\$s (%5\$s)\n", h, Lang.t(theme ?: ""), Lang.planet(lord.name), Lang.sign(lord.sign), Lang.t(lordDig)))
        } else {
            sb.append(Lang.t("House %1\$d: %2\$s\n", h, Lang.t(theme ?: "")))
        }

        val transit = AstroEngine.siderealAt(now)
        val transiting = transit.filter { k.houseOf(it) == h }
        if (transiting.isNotEmpty()) {
            sb.append(Lang.t("Current transits: %s\n", transiting.joinToString(", ") { Lang.planet(it.name) }))
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
        sb.append(Lang.t("%1\$s (%2\$s):\n", Lang.t(area), Lang.t(theme)))

        val lordSign = (k.lagnaSign + h - 1) % 12
        val lordName = SIGN_LORDS[lordSign]
        val houseDiv = k.planets.firstOrNull { it.name == lordName }
        if (houseDiv != null) {
            val strength = shadbala[houseDiv.name]?.rating() ?: "Unknown"
            sb.append(Lang.t("House %1\$d lord (%2\$s in %3\$s): %4\$s\n", h, Lang.planet(houseDiv.name), Lang.sign(houseDiv.sign), Lang.t(strength)))
        }

        val transit = AstroEngine.siderealAt(now)
        val benefics = listOf("Sun", "Moon", "Jupiter", "Venus", "Mercury")
        val malefics = listOf("Mars", "Saturn", "Rahu", "Ketu")
        val inHouse = transit.filter { k.houseOf(it) == h }
        val favorable = inHouse.filter { it.name in benefics }
        val unfavorable = inHouse.filter { it.name in malefics }

        if (favorable.isNotEmpty()) sb.append(Lang.t("Favorable transits: %s\n", favorable.joinToString(", ") { Lang.planet(it.name) }))
        if (unfavorable.isNotEmpty()) sb.append(Lang.t("Challenging transits: %s\n", unfavorable.joinToString(", ") { Lang.planet(it.name) }))

        if (inHouse.isEmpty()) sb.append(Lang.t("Overall: No major transits in this house currently.\n"))

        return sb.toString()
    }

    fun dashaReading(dasha: String, k: Kundli, start: Long, end: Long): String {
        val sb = StringBuilder()
        val starty = (start - System.currentTimeMillis()) / (365.25 * 24 * 3600 * 1000)
        val duration = String.format("%.1f", (end-start)/(365.25*24*3600*1000))
        val timing = if (starty < 0) Lang.t("ended %d years ago", -starty.toInt()) else Lang.t("starts in %d years", starty.toInt())

        val planet = dasha.split(" ")[0]
        var dashaLabel = dasha
        for (name in listOf("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "Rahu", "Ketu")) {
            dashaLabel = dashaLabel.replace(name, Lang.planet(name))
        }
        dashaLabel = dashaLabel.replace("Mahadasha", Lang.t("Mahadasha"))
            .replace("Antardasha", Lang.t("Antardasha"))
        sb.append(Lang.t("%1\$s: %2\$s (%3\$s year duration)\n", dashaLabel, timing, duration))
        val p = k.planets.firstOrNull { it.name == planet }
        if (p != null) {
            val dign = when {
                p.sign == Insights.exalt[p.name] -> "exalted \u2192 favorable growth"
                p.sign == Insights.debil[p.name] -> "debilitated \u2192 challenges to overcome"
                else -> "neutral \u2192 steady progress"
            }
            sb.append(Lang.t("%1\$s is %2\$s in your chart.\n", Lang.planet(planet), Lang.t(dign)))
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
        sb.append(Lang.t("Focus: %s\n", Lang.t(theme)))
        val mantraPlanet = if (planet in listOf("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "Rahu", "Ketu")) Lang.planet(planet) else dashaLabel.substringBefore(" ")
        sb.append(Lang.t("Remedies: chant %1\$s mantra, donate its metal, fast on its day\n", mantraPlanet.uppercase()))

        return sb.toString()
    }
}
