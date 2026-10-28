package com.example.astrology

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Near-future predictions from Vimshottari dasha periods and Gochar (transits from the natal Moon and Lagna). */
object Forecast {
    private const val DAY = 86400000L
    private val dayFmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val monthFmt = SimpleDateFormat("MMM yyyy", Locale.getDefault())

    private fun d(ms: Long): String = dayFmt.format(Date(ms))

    private val favourable = mapOf(
        "Sun" to listOf(3, 6, 10, 11), "Mars" to listOf(3, 6, 11), "Mercury" to listOf(2, 4, 6, 8, 10, 11),
        "Jupiter" to listOf(2, 5, 7, 9, 11), "Venus" to listOf(1, 2, 3, 4, 5, 8, 9, 11, 12),
        "Saturn" to listOf(3, 6, 11), "Rahu" to listOf(3, 6, 10, 11)
    )

    private val remedy = mapOf(
        "Sun" to "Sunday; Surya Arghya at sunrise, 'Om Suryaya Namah'; ruby (only after expert advice)",
        "Moon" to "Monday; 'Om Chandraya Namah'; offer water/milk to Shiva; pearl (with advice)",
        "Mars" to "Tuesday; 'Om Mangalaya Namah', Hanuman Chalisa; coral (with advice)",
        "Mercury" to "Wednesday; 'Om Budhaya Namah', Vishnu Sahasranama; emerald (with advice)",
        "Jupiter" to "Thursday; 'Om Gurave Namah', respect teachers, donate yellow items; yellow sapphire (with advice)",
        "Venus" to "Friday; 'Om Shukraya Namah', Lakshmi worship; diamond/white sapphire (with advice)",
        "Saturn" to "Saturday; 'Om Shanaye Namah', serve the elderly and workers, Hanuman worship; blue sapphire only after a trial and advice",
        "Rahu" to "'Om Rahave Namah', Durga worship, donate on Saturdays; hessonite (with advice)",
        "Ketu" to "'Om Ketave Namah', Ganesha worship; cat's eye (with advice)"
    )

    private fun isNode(n: String) = n == "Rahu" || n == "Ketu"

    /** Strength of a planet as dasha lord: Shadbala-based rating. */
    fun score(k: Kundli, name: String, shadbala: Map<String, Shadbala.Result>): Int {
        // Nodes have no Shadbala; they act through the lord of the sign they occupy.
        val key = if (isNode(name)) SIGN_LORDS[k.planet(name).sign] else name
        val sb = shadbala[key] ?: return 2
        return when {
            sb.ratio >= 1.5 -> 4
            sb.ratio >= 1.1 -> 3
            sb.ratio >= 0.85 -> 2
            sb.ratio >= 0.7 -> 1
            else -> 0
        }
    }

    fun rating(s: Int) = when {
        s >= 4 -> "Very favourable"
        s >= 3 -> "Favourable"
        s >= 2 -> "Mixed"
        else -> "Challenging"
    }

    private fun lordSummary(k: Kundli, name: String): String {
        val p = k.planet(name)
        val h = k.houseOf(p)
        val owned = Vedic.housesOwned(k, name)
        val own = if (owned.isEmpty()) "acts through the sign lord ${SIGN_LORDS[p.sign]}" else "rules house${if (owned.size > 1) "s" else ""} ${owned.joinToString(" & ")}"
        return "$name $own and sits in house $h (${Insights.houseMeaning[h - 1]}); " +
            "it signifies ${Insights.planetTheme[name]}."
    }

    private fun areas(k: Kundli, name: String): String {
        val p = k.planet(name)
        val owned = Vedic.housesOwned(k, if (isNode(name)) SIGN_LORDS[p.sign] else name)
        val hs = (owned + k.houseOf(p)).distinct().sorted()
        return hs.joinToString("; ") { Insights.houseMeaning[it - 1] }
    }

    private fun currentOf(list: List<DashaPeriod>, now: Long) = list.firstOrNull { now >= it.startMillis && now < it.endMillis }

    fun dashaForecast(k: Kundli, shadbala: Map<String, Shadbala.Result>, now: Long): String = buildString {
        val maha = currentOf(k.dashas, now)
        if (maha == null) {
            append("Current time is beyond the computed dasha span."); return@buildString
        }
        val antars = AstroEngine.subPeriods(maha)
        val antar = currentOf(antars, now)
        append("Mahadasha: ${maha.lord} until ${d(maha.endMillis)} – ${rating(score(k, maha.lord, shadbala))}\n")
        append(lordSummary(k, maha.lord)).append("\n")
        append("Themes of this decade-scale period: ${areas(k, maha.lord)}.\n\n")
        if (antar != null) {
            append("Antardasha: ${antar.lord} (${d(maxOf(antar.startMillis, k.dashas.first().startMillis))} – ${d(antar.endMillis)}) – ${rating(score(k, antar.lord, shadbala))}\n")
            append(lordSummary(k, antar.lord)).append("\n")
            val praty = currentOf(AstroEngine.subPeriods(antar), now)
            if (praty != null) {
                append("Pratyantardasha: ${praty.lord} until ${d(praty.endMillis)} – ${rating(score(k, praty.lord, shadbala))}\n")
            }
            append("\n")
        }
        append("Upcoming sub-periods (next ~3 years):\n")
        val horizon = now + 3 * 365 * DAY
        val all = antars + (k.dashas.firstOrNull { it.startMillis >= maha.endMillis }?.let { AstroEngine.subPeriods(it) } ?: emptyList())
        val parentOf = { a: DashaPeriod -> if (a.startMillis < maha.endMillis) maha.lord else k.dashas.first { it.startMillis >= maha.endMillis }.lord }
        for (a in all) {
            if (a.endMillis <= now || a.startMillis > horizon) continue
            val sc = (score(k, parentOf(a), shadbala) + score(k, a.lord, shadbala)) / 2
            append("• ${parentOf(a)}–${a.lord}: ${d(maxOf(a.startMillis, now))} to ${d(a.endMillis)} – ${rating(sc)}\n")
            append("   focus: ${areas(k, a.lord)}\n")
        }
        append("\nTraditional remedies for the current ${maha.lord} Mahadasha: ${remedy.getValue(maha.lord)}.")
    }

    private fun houseFrom(sign: Int, base: Int) = (sign - base + 12) % 12 + 1

    fun transitReport(k: Kundli, shadbala: Map<String, Shadbala.Result>, now: Long): String = buildString {
        val pos = AstroEngine.siderealAt(now)
        val moonSign = k.planet("Moon").sign
        append("Transit positions on ${d(now)} (sidereal):\n")
        append("%-8s %-12s %-6s %-6s\n".format("Planet", "Sign", "fromLg", "fromMo"))
        for (p in pos) {
            append("%-8s %-12s H%-5d H%-5d\n".format(p.name, SIGNS[p.sign], houseFrom(p.sign, k.lagnaSign), houseFrom(p.sign, moonSign)))
        }
        append("\n")
        val sat = pos.first { it.name == "Saturn" }
        val sMo = houseFrom(sat.sign, moonSign)
        append(
            when (sMo) {
                12 -> "Sade Sati – first phase: Saturn is in the 12th from your Moon. Expenses, sleep and mental pressure rise; start of a 7.5-year karmic cycle.\n"
                1 -> "Sade Sati – peak phase: Saturn is over your natal Moon sign. Emotional weight, responsibilities and lessons in patience; steady effort pays off later.\n"
                2 -> "Sade Sati – last phase: Saturn is in the 2nd from your Moon. Finances and family need care; relief gradually approaches.\n"
                8 -> "Ashtama Shani: Saturn is in the 8th from your Moon. A testing time for health, sudden changes and patience.\n"
                4 -> "Ardhashtama Shani: Saturn is in the 4th from your Moon. Pressure around home, comfort and peace of mind.\n"
                else -> "You are not in Sade Sati. Saturn in house $sMo from the Moon is ${if (sMo in favourable.getValue("Saturn")) "supportive (rewards steady work)" else "neutral to slow"}.\n"
            }
        )
        val jup = pos.first { it.name == "Jupiter" }
        val jMo = houseFrom(jup.sign, moonSign)
        val jupiter_strength = shadbala["Jupiter"]?.rating() ?: "Unknown"
        append(
            "Jupiter is in house $jMo from the Moon ($jupiter_strength strength): " +
                (if (jMo in favourable.getValue("Jupiter")) "a supportive Guru transit – growth, guidance and good fortune." else "a less supportive Guru transit – progress needs more effort and patience.") + "\n"
        )
    }

    fun ashtakavargaReport(k: Kundli): String = buildString {
        val av = k.ashtakavarga
        append("Ashtakavarga (Benefic Aspect Values)\n")
        append("SAV (Sum of Aspects) by sign:\n")
        val signs = listOf("Ar", "Ta", "Ge", "Ca", "Le", "Vi", "Li", "Sc", "Sa", "Ca", "Aq", "Pi")
        for (i in 0..11) {
            append("${signs[i]}: ${av.sav[i] ?: 0} ")
            if ((i + 1) % 4 == 0) append("\n")
        }
        append("\nTotal: ${av.total} (expected ~337 for standard horoscope)\n")
    }

    fun shadbalaReport(k: Kundli, shadbala: Map<String, Shadbala.Result>): String = buildString {
        append("Shadbala (rupas; total vs required minimum)\n")
        for ((name, result) in shadbala) {
            append("$name: ${result.rating()} (${result.total} / ${"%.2f".format(result.required)} rupas, ratio ${"%.2f".format(result.ratio)})\n")
            append("  Sthana=${result.sthana} Dig=${result.dig} Kala=${result.kala} Cheshta=${result.cheshta} Naisargika=${result.naisargika} Drik=${result.drik}\n")
        }
    }

    fun vedhaReport(k: Kundli, now: Long): String = buildString {
        val gochar = Gochar.transitStrength(k.chandra.sign, now)
        append("Vedha (Transit Obstructions)\n")
        for ((name, strength) in gochar) {
            append("$name: ${strength.rating}")
            if (strength.vedha != null) append(" (${strength.vedha})")
            append("\n")
        }
    }

    fun areaOutlook(k: Kundli, shadbala: Map<String, Shadbala.Result>, now: Long): String = buildString {
        val pos = AstroEngine.siderealAt(now)
        val jh = houseFrom(pos.first { it.name == "Jupiter" }.sign, k.lagnaSign)
        val sh = houseFrom(pos.first { it.name == "Saturn" }.sign, k.lagnaSign)
        fun hit(base: Int, nths: List<Int>) = nths.map { (base - 1 + it - 1) % 12 + 1 }
        val jSet = hit(jh, listOf(1, 5, 7, 9))
        val sSet = hit(sh, listOf(1, 3, 7, 10))
        val areas = listOf(
            "Career & status" to 10, "Marriage & partnerships" to 7, "Income & savings" to 2, "Gains & network" to 11,
            "Health & vitality" to 1, "Home & peace of mind" to 4, "Children, creativity & studies" to 5, "Luck & higher learning" to 9
        )
        append("Jupiter transits house $jh and Saturn house $sh from your Lagna.\n\n")
        for ((label, h) in areas) {
            val j = h in jSet
            val s = h in sSet
            val txt = when {
                j && s -> "growth with effort – both Jupiter and Saturn act here; build slowly and wisely."
                j -> "supported by Jupiter – good window for progress and opportunities."
                s -> "pressured by Saturn – slow results, responsibility and discipline needed."
                else -> "no major slow-planet trigger; results follow your dasha."
            }
            append("• $label (house $h): $txt\n")
        }
    }

    fun ingressReport(k: Kundli, now: Long): String = buildString {
        val moonSign = k.planet("Moon").sign
        val list = AstroEngine.ingresses(now, 3 * 365, listOf("Jupiter", "Saturn", "Rahu")) +
            AstroEngine.ingresses(now, 365, listOf("Mars"))
        if (list.isEmpty()) { append("No major sign changes found."); return@buildString }
        for (i in list.sortedBy { it.millis }) {
            val hl = houseFrom(i.sign, k.lagnaSign)
            val hm = houseFrom(i.sign, moonSign)
            val good = hm in (favourable[i.planet] ?: emptyList())
            val rahuNote = if (i.planet == "Rahu") " (Ketu moves into ${SIGNS[(i.sign + 6) % 12]})" else ""
            append("• ${d(i.millis)}: ${i.planet} enters ${SIGNS[i.sign]}$rahuNote\n")
            append("   house $hl from Lagna (${Insights.houseMeaning[hl - 1]}), house $hm from Moon – ${if (good) "supportive" else "testing"}.\n")
        }
    }

    fun monthlyOutlook(k: Kundli, shadbala: Map<String, Shadbala.Result>, now: Long): String = buildString {
        val moonSign = k.planet("Moon").sign
        val rows = mutableListOf<Triple<String, Double, String>>()
        for (i in 0 until 12) {
            val t = now + (i * 30.44 * DAY).toLong()
            val pos = AstroEngine.siderealAt(t)
            val fav = favourable.count { (n, hs) -> houseFrom(pos.first { it.name == n }.sign, moonSign) in hs }
            val maha = currentOf(k.dashas, t)
            val antar = maha?.let { currentOf(AstroEngine.subPeriods(it), t) }
            val ds = if (maha != null && antar != null) (score(k, maha.lord, shadbala) + score(k, antar.lord, shadbala)) / 2.0 else 0.0
            val comb = (fav - 3.5) + ds / 1.5
            val label = when {
                comb >= 2 -> "Good"
                comb >= 0 -> "Fairly good"
                comb >= -2 -> "Mixed – be careful"
                else -> "Challenging – go slow"
            }
            rows += Triple(
                monthFmt.format(Date(t)), comb,
                "$label  (transits $fav/7 favourable, dasha ${maha?.lord ?: "-"}/${antar?.lord ?: "-"})"
            )
        }
        for (r in rows) append("${r.first}: ${r.third}\n")
        val best = rows.maxByOrNull { it.second }
        val worst = rows.minByOrNull { it.second }
        if (best != null && worst != null) {
            append("\nMost supportive month: ${best.first}. Month needing most care: ${worst.first}.")
        }
    }

    fun summary(k: Kundli, shadbala: Map<String, Shadbala.Result>, now: Long): String {
        val maha = currentOf(k.dashas, now) ?: return ""
        val antar = currentOf(AstroEngine.subPeriods(maha), now)
        val sc = if (antar != null) (score(k, maha.lord, shadbala) + score(k, antar.lord, shadbala)) / 2 else score(k, maha.lord, shadbala)
        return "You are running ${maha.lord} Mahadasha" + (if (antar != null) " – ${antar.lord} Antardasha (until ${d(antar.endMillis)})" else "") +
            ". Overall period tone: ${rating(sc)}. Key focus areas: ${areas(k, antar?.lord ?: maha.lord)}."
    }
}
