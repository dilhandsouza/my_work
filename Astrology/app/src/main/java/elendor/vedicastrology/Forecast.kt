package elendor.vedicastrology

import java.text.SimpleDateFormat
import java.util.Date

/** Near-future predictions from Vimshottari dasha periods and Gochar (transits from the natal Moon and Lagna). */
object Forecast {
    private const val DAY = 86400000L
    private fun d(ms: Long): String = SimpleDateFormat("dd MMM yyyy", Lang.locale()).format(Date(ms))

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
        s >= 4 -> Lang.t("Very favourable")
        s >= 3 -> Lang.t("Favourable")
        s >= 2 -> Lang.t("Mixed")
        else -> Lang.t("Challenging")
    }

    private fun lordSummary(k: Kundli, name: String): String {
        val p = k.planet(name)
        val h = k.houseOf(p)
        val owned = Vedic.housesOwned(k, name)
        val own = if (owned.isEmpty()) Lang.t("acts through the sign lord %s", Lang.planet(SIGN_LORDS[p.sign]))
            else Lang.t(if (owned.size > 1) "rules houses %s" else "rules house %s", owned.joinToString(" & "))
        return Lang.t("%1\$s %2\$s and sits in house %3\$d (%4\$s); it signifies %5\$s.",
            Lang.planet(name), own, h, Lang.t(Insights.houseMeaning[h - 1]), Lang.t(Insights.planetTheme[name].toString()))
    }

    private fun areas(k: Kundli, name: String): String {
        val p = k.planet(name)
        val owned = Vedic.housesOwned(k, if (isNode(name)) SIGN_LORDS[p.sign] else name)
        val hs = (owned + k.houseOf(p)).distinct().sorted()
        return hs.joinToString("; ") { Lang.t(Insights.houseMeaning[it - 1]) }
    }

    private fun currentOf(list: List<DashaPeriod>, now: Long) = list.firstOrNull { now >= it.startMillis && now < it.endMillis }

    fun dashaForecast(k: Kundli, shadbala: Map<String, Shadbala.Result>, now: Long): String = buildString {
        val maha = currentOf(k.dashas, now)
        if (maha == null) { append(Lang.t("Current time is beyond the computed dasha span.")); return@buildString }
        val antars = AstroEngine.subPeriods(maha)
        val antar = currentOf(antars, now)
        append(Lang.t("Mahadasha: %1\$s until %2\$s \u2013 %3\$s\n", Lang.planet(maha.lord), d(maha.endMillis), rating(score(k, maha.lord, shadbala))))
        append(lordSummary(k, maha.lord)).append("\n")
        append(Lang.t("Themes of this decade-scale period: %s.\n\n", areas(k, maha.lord)))
        if (antar != null) {
            append(Lang.t("Antardasha: %1\$s (%2\$s \u2013 %3\$s) \u2013 %4\$s\n", Lang.planet(antar.lord), d(maxOf(antar.startMillis, k.dashas.first().startMillis)), d(antar.endMillis), rating(score(k, antar.lord, shadbala))))
            append(lordSummary(k, antar.lord)).append("\n")
            val praty = currentOf(AstroEngine.subPeriods(antar), now)
            if (praty != null) append(Lang.t("Pratyantardasha: %1\$s until %2\$s \u2013 %3\$s\n", Lang.planet(praty.lord), d(praty.endMillis), rating(score(k, praty.lord, shadbala))))
            append("\n")
        }
        append(Lang.t("Upcoming sub-periods (next ~3 years):\n"))
        val horizon = now + 3 * 365 * DAY
        val all = antars + (k.dashas.firstOrNull { it.startMillis >= maha.endMillis }?.let { AstroEngine.subPeriods(it) } ?: emptyList())
        val parentOf = { a: DashaPeriod -> if (a.startMillis < maha.endMillis) maha.lord else k.dashas.first { it.startMillis >= maha.endMillis }.lord }
        for (a in all) {
            if (a.endMillis <= now || a.startMillis > horizon) continue
            val sc = (score(k, parentOf(a), shadbala) + score(k, a.lord, shadbala)) / 2
            append(Lang.t("\u2022 %1\$s\u2013%2\$s: %3\$s to %4\$s \u2013 %5\$s\n", Lang.planet(parentOf(a)), Lang.planet(a.lord), d(maxOf(a.startMillis, now)), d(a.endMillis), rating(sc)))
            append(Lang.t("   focus: %s\n", areas(k, a.lord)))
        }
        append(Lang.t("\nTraditional remedies for the current %1\$s Mahadasha: %2\$s.", Lang.planet(maha.lord), Lang.t(remedy.getValue(maha.lord))))
    }

    private fun houseFrom(sign: Int, base: Int) = (sign - base + 12) % 12 + 1

    fun transitReport(k: Kundli, shadbala: Map<String, Shadbala.Result>, now: Long): String = buildString {
        val pos = AstroEngine.siderealAt(now)
        val moonSign = k.planet("Moon").sign
        append(Lang.t("Transit positions on %1\$s (sidereal):\n", d(now)))
        append("%s %s %s %s\n".format(Lang.pad(Lang.t("Planet"),8), Lang.pad(Lang.t("Sign"),12), Lang.pad(Lang.t("fromLg"),6), Lang.pad(Lang.t("fromMo"),6)))
        for (p in pos) append("%s %s %s%-5d %s%-5d\n".format(Lang.pad(Lang.planet(p.name),8), Lang.pad(Lang.sign(p.sign),12), Lang.t("H"), houseFrom(p.sign,k.lagnaSign), Lang.t("H"), houseFrom(p.sign,moonSign)))
        append("\n")
        val sat = pos.first { it.name == "Saturn" }
        val sMo = houseFrom(sat.sign, moonSign)
        append(when (sMo) {
            12 -> Lang.t("Sade Sati \u2013 first phase: Saturn is in the 12th from your Moon. Expenses, sleep and mental pressure rise; start of a 7.5-year karmic cycle.\n")
            1 -> Lang.t("Sade Sati \u2013 peak phase: Saturn is over your natal Moon sign. Emotional weight, responsibilities and lessons in patience; steady effort pays off later.\n")
            2 -> Lang.t("Sade Sati \u2013 last phase: Saturn is in the 2nd from your Moon. Finances and family need care; relief gradually approaches.\n")
            8 -> Lang.t("Ashtama Shani: Saturn is in the 8th from your Moon. A testing time for health, sudden changes and patience.\n")
            4 -> Lang.t("Ardhashtama Shani: Saturn is in the 4th from your Moon. Pressure around home, comfort and peace of mind.\n")
            else -> Lang.t("You are not in Sade Sati. Saturn in house %1\$d from the Moon is %2\$s.\n", sMo, Lang.t(if(sMo in favourable.getValue("Saturn")) "supportive (rewards steady work)" else "neutral to slow"))
        })
        val jup = pos.first { it.name == "Jupiter" }
        val jMo = houseFrom(jup.sign, moonSign)
        val js = shadbala["Jupiter"]?.rating() ?: "Unknown"
        append(Lang.t("Jupiter is in house %1\$d from the Moon (%2\$s strength): %3\$s\n", jMo, Lang.t(js), Lang.t(if(jMo in favourable.getValue("Jupiter")) "a supportive Guru transit \u2013 growth, guidance and good fortune." else "a less supportive Guru transit \u2013 progress needs more effort and patience.")))
    }

    fun ashtakavargaReport(k: Kundli): String = buildString {
        val av = k.ashtakavarga
        append(Lang.t("Ashtakavarga (Benefic Aspect Values)\n"))
        append(Lang.t("SAV (Sum of Aspects) by sign:\n"))
        val signs = listOf("Ar", "Ta", "Ge", "Ca", "Le", "Vi", "Li", "Sc", "Sa", "Ca", "Aq", "Pi")
        for (i in 0..11) {
            append(Lang.t("%1\$s: %2\$d ", Lang.t(if (Lang.isKn && i == 9) "Cp" else signs[i]), av.sav[i] ?: 0))
            if ((i + 1) % 4 == 0) append("\n")
        }
        append(Lang.t("\nTotal: %1\$d (expected ~337 for standard horoscope)\n", av.total))
    }

    fun shadbalaReport(k: Kundli, shadbala: Map<String, Shadbala.Result>): String = buildString {
        append(Lang.t("Shadbala (rupas; total vs required minimum)\n"))
        for ((name, result) in shadbala) {
            append(Lang.t("%1\$s: %2\$s (%3\$s / %4\$s rupas, ratio %5\$s)\n", Lang.planet(name), Lang.t(result.rating()), result.total, "%.2f".format(result.required), "%.2f".format(result.ratio)))
            append(Lang.t("  Sthana=%1\$s Dig=%2\$s Kala=%3\$s Cheshta=%4\$s Naisargika=%5\$s Drik=%6\$s\n", result.sthana, result.dig, result.kala, result.cheshta, result.naisargika, result.drik))
        }
    }

    fun vedhaReport(k: Kundli, now: Long): String = buildString {
        val gochar = Gochar.transitStrength(k.chandra.sign, now)
        append(Lang.t("Vedha (Transit Obstructions)\n"))
        for ((name, strength) in gochar) {
            append(Lang.t("%1\$s: %2\$s", Lang.planet(name), Lang.t(strength.rating)))
            if (strength.vedha != null) append(Lang.t(" (%s)", Lang.planet(strength.vedha)))
            append("\n")
        }
    }

    fun areaOutlook(k: Kundli, shadbala: Map<String, Shadbala.Result>, now: Long): String = buildString {
        val pos = AstroEngine.siderealAt(now)
        val jh = houseFrom(pos.first { it.name == "Jupiter" }.sign, k.lagnaSign)
        val sh = houseFrom(pos.first { it.name == "Saturn" }.sign, k.lagnaSign)
        fun hit(base: Int, nths: List<Int>) = nths.map { (base - 1 + it - 1) % 12 + 1 }
        val jSet = hit(jh, listOf(1,5,7,9)); val sSet = hit(sh, listOf(1,3,7,10))
        val areas = listOf("Career & status" to 10,"Marriage & partnerships" to 7,"Income & savings" to 2,"Gains & network" to 11,"Health & vitality" to 1,"Home & peace of mind" to 4,"Children, creativity & studies" to 5,"Luck & higher learning" to 9)
        append(Lang.t("Jupiter transits house %1\$d and Saturn house %2\$d from your Lagna.\n\n", jh, sh))
        for ((label,h) in areas) {
            val j=h in jSet; val saturnHit=h in sSet
            val txt=when { j && saturnHit -> Lang.t("growth with effort \u2013 both Jupiter and Saturn act here; build slowly and wisely."); j -> Lang.t("supported by Jupiter \u2013 good window for progress and opportunities."); saturnHit -> Lang.t("pressured by Saturn \u2013 slow results, responsibility and discipline needed."); else -> Lang.t("no major slow-planet trigger; results follow your dasha.") }
            append(Lang.t("\u2022 %1\$s (house %2\$d): %3\$s\n", Lang.t(label),h,txt))
        }
    }

    fun ingressReport(k: Kundli, now: Long): String = buildString {
        val moonSign=k.planet("Moon").sign
        val list=AstroEngine.ingresses(now,3*365,listOf("Jupiter","Saturn","Rahu"))+AstroEngine.ingresses(now,365,listOf("Mars"))
        if(list.isEmpty()){append(Lang.t("No major sign changes found."));return@buildString}
        for(i in list.sortedBy{it.millis}) {
            val hl=houseFrom(i.sign,k.lagnaSign); val hm=houseFrom(i.sign,moonSign); val good=hm in (favourable[i.planet]?:emptyList())
            val note=if(i.planet=="Rahu") Lang.t(" (Ketu moves into %s)",Lang.sign((i.sign+6)%12)) else ""
            append(Lang.t("\u2022 %1\$s: %2\$s enters %3\$s%4\$s\n",d(i.millis),Lang.planet(i.planet),Lang.sign(i.sign),note))
            append(Lang.t("   house %1\$d from Lagna (%2\$s), house %3\$d from Moon \u2013 %4\$s.\n",hl,Lang.t(Insights.houseMeaning[hl-1]),hm,Lang.t(if(good) "supportive" else "testing")))
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
                comb >= 2 -> Lang.t("Good")
                comb >= 0 -> Lang.t("Fairly good")
                comb >= -2 -> Lang.t("Mixed – be careful")
                else -> Lang.t("Challenging – go slow")
            }
            rows += Triple(
                SimpleDateFormat("MMM yyyy", Lang.locale()).format(Date(t)), comb,
                Lang.t("%1\$s  (transits %2\$d/7 favourable, dasha %3\$s/%4\$s)", label, fav,
                    maha?.lord?.let { Lang.planet(it) } ?: "-", antar?.lord?.let { Lang.planet(it) } ?: "-")
            )
        }
        for (r in rows) append(Lang.t("%1\$s: %2\$s\n", r.first, r.third))
        val best = rows.maxByOrNull { it.second }
        val worst = rows.minByOrNull { it.second }
        if (best != null && worst != null) {
            append(Lang.t("\nMost supportive month: %1\$s. Month needing most care: %2\$s.", best.first, worst.first))
        }
    }

    fun summary(k: Kundli, shadbala: Map<String, Shadbala.Result>, now: Long): String {
        val maha=currentOf(k.dashas,now)?:return ""
        val antar=currentOf(AstroEngine.subPeriods(maha),now)
        val sc=if(antar!=null)(score(k,maha.lord,shadbala)+score(k,antar.lord,shadbala))/2 else score(k,maha.lord,shadbala)
        return Lang.t("You are running %1\$s Mahadasha",Lang.planet(maha.lord))+
            (if(antar!=null) Lang.t(" \u2013 %1\$s Antardasha (until %2\$s)",Lang.planet(antar.lord),d(antar.endMillis)) else "")+
            Lang.t(". Overall period tone: %1\$s. Key focus areas: %2\$s.",rating(sc),areas(k,antar?.lord?:maha.lord))
    }
}
