package elendor.vedicastrology

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Insights {

    private val lagnaText = listOf(
        "Energetic, courageous and direct. You are a natural initiator who prefers action over waiting; watch impatience and temper.",
        "Steady, practical and sensual. You value security, comfort and loyalty; you are persistent but can be stubborn.",
        "Curious, witty and adaptable. Communication and learning drive you; guard against scattered focus and restlessness.",
        "Caring, intuitive and protective. Home, family and emotions matter deeply; moods can swing with your surroundings.",
        "Confident, generous and charismatic. You seek recognition and lead naturally; keep pride in check.",
        "Analytical, helpful and detail-oriented. You excel at service, health and craft; avoid over-criticism of yourself and others.",
        "Diplomatic, charming and fair-minded. You seek harmony and partnership; indecision is your challenge.",
        "Intense, determined and perceptive. You have strong willpower and depth; learn to release secrecy and grudges.",
        "Optimistic, philosophical and freedom-loving. You seek meaning, travel and wisdom; avoid overpromising.",
        "Disciplined, ambitious and responsible. Slow, steady progress brings lasting success; avoid being overly serious.",
        "Original, humanitarian and independent. You think ahead of the crowd; emotional detachment can be an issue.",
        "Compassionate, imaginative and spiritual. You are sensitive and artistic; set boundaries to avoid escapism."
    )

    private val moonText = listOf(
        "emotions are quick and passionate; you need independence",
        "emotions are steady and comfort-seeking; you are affectionate and loyal",
        "your mind is quick, curious and talkative",
        "your Moon is strong here: nurturing, empathic and attached to home",
        "you feel proud and need appreciation and warmth",
        "your mind is analytical and you worry over details",
        "you seek emotional balance and harmony in relationships",
        "emotions run deep and intense; you are private and resilient",
        "your mind is optimistic, adventurous and idealistic",
        "emotions are reserved; you are practical and dutiful",
        "your mind is unconventional and friendly yet detached",
        "your mind is intuitive, dreamy and compassionate"
    )

    val houseMeaning = listOf(
        "self, personality, health", "wealth, speech, family", "courage, siblings, effort",
        "home, mother, peace of mind", "children, creativity, intellect", "work, health, obstacles",
        "marriage, partnerships", "transformation, longevity, research", "fortune, dharma, higher learning",
        "career, status, public life", "gains, friends, aspirations", "expenses, solitude, spirituality"
    )

    val planetTheme = mapOf(
        "Sun" to "authority, vitality, ego", "Moon" to "mind, emotions, nurturing",
        "Mars" to "energy, drive, courage", "Mercury" to "intellect, speech, business",
        "Jupiter" to "wisdom, growth, luck", "Venus" to "love, beauty, comforts",
        "Saturn" to "discipline, karma, delays", "Rahu" to "ambition, unconventional desires",
        "Ketu" to "detachment, spirituality, past-life skills"
    )

    private val dashaText = mapOf(
        "Ketu" to "A period of detachment, introspection and spiritual inclination; unexpected changes are possible.",
        "Venus" to "A period favouring relationships, comforts, creativity, money and luxury.",
        "Sun" to "A period highlighting authority, career visibility, father figures and self-confidence.",
        "Moon" to "A period focused on emotions, home, mother, public dealings and mental well-being.",
        "Mars" to "A period of high energy, ambition, property matters and competition; manage anger.",
        "Rahu" to "A period of ambition, foreign links, rapid and unconventional change; avoid shortcuts.",
        "Jupiter" to "A period of growth, wisdom, education, children, fortune and expansion.",
        "Saturn" to "A period of hard work, responsibility and patience; rewards come through discipline.",
        "Mercury" to "A period favouring learning, communication, trade, analysis and networking."
    )

    val exalt = mapOf("Sun" to 0, "Moon" to 1, "Mars" to 9, "Mercury" to 5, "Jupiter" to 3, "Venus" to 11, "Saturn" to 6)
    val debil = mapOf("Sun" to 6, "Moon" to 7, "Mars" to 3, "Mercury" to 11, "Jupiter" to 9, "Venus" to 5, "Saturn" to 0)

    fun dignity(p: PlanetPos): String = when {
        exalt[p.name] == p.sign -> "Exalted"
        debil[p.name] == p.sign -> "Debilitated"
        SIGN_LORDS[p.sign] == p.name -> "Own sign"
        else -> ""
    }

    fun dms(deg: Double): String {
        val d = deg.toInt()
        val m = ((deg - d) * 60).toInt()
        return "%d°%02d'".format(d, m)
    }

    fun planetTable(k: Kundli): String = buildString {
        append(Lang.t("Ascendant (Lagna): %s %s\n\n", Lang.sign(k.lagnaSign), dms(k.ascendant % 30)))
        for (p in k.planets) {
            val dig = dignity(p)
            append(Lang.t(
                "%s%s%s  H%-2d  %s%s%s\n",
                Lang.pad(Lang.planet(p.name), 9), Lang.pad(Lang.sign(p.sign), 13), dms(p.degInSign), k.houseOf(p),
                Lang.t("%s-%d", Lang.nak(p.nakshatra), p.pada),
                if (p.retrograde && p.name != "Rahu" && p.name != "Ketu") Lang.t(" (R)") else "",
                if (dig.isEmpty()) "" else Lang.t("  [%s]", Lang.t(dig))
            ))
        }
        append("\n")
        append(Lang.t("Ayanamsa (Lahiri): %.3f°", k.ayanamsa))
    }

    fun overview(k: Kundli): String {
        val moon = k.planet("Moon")
        val sun = k.planet("Sun")
        return buildString {
            append(Lang.t("Ascendant – %s (lord %s):\n", Lang.sign(k.lagnaSign), Lang.planet(SIGN_LORDS[k.lagnaSign])))
            append(Lang.t(lagnaText[k.lagnaSign])).append("\n\n")
            append(Lang.t(
                "Moon sign (Rashi) – %s, nakshatra %s pada %d: %s.\n\n",
                Lang.sign(moon.sign), Lang.nak(moon.nakshatra), moon.pada, Lang.t(moonText[moon.sign])
            ))
            append(Lang.t(
                "Sun sign (sidereal) – %s: your core identity expresses through the qualities of %s.\n\n",
                Lang.sign(sun.sign), Lang.sign(sun.sign)
            ))
            val lagnaLord = SIGN_LORDS[k.lagnaSign]
            val ll = k.planet(lagnaLord)
            append(Lang.t(
                "Lagna lord %s sits in %s in house %d – life energy is directed towards %s.",
                Lang.planet(lagnaLord), Lang.sign(ll.sign), k.houseOf(ll), Lang.t(houseMeaning[k.houseOf(ll) - 1])
            ))
        }
    }

    fun planetInsights(k: Kundli): String = buildString {
        for (p in k.planets) {
            val h = k.houseOf(p)
            append(Lang.t("• %s in %s, house %d", Lang.planet(p.name), Lang.sign(p.sign), h))
            val dig = dignity(p)
            if (dig.isNotEmpty()) append(Lang.t(" (%s)", Lang.t(dig)))
            append(Lang.t(
                "\n  %s are channelled into %s.",
                Lang.t(planetTheme.getValue(p.name)), Lang.t(houseMeaning[h - 1])
            ))
            when (dig) {
                "Exalted", "Own sign" -> append(Lang.t(" This placement is strong and supportive."))
                "Debilitated" -> append(Lang.t(" This placement needs conscious effort; results improve with maturity."))
            }
            append("\n")
        }
    }

    fun yogas(k: Kundli): String {
        val out = mutableListOf<String>()
        val mars = k.planet("Mars")
        if (k.houseOf(mars) in listOf(1, 2, 4, 7, 8, 12)) {
            out += Lang.t(
                "Mangal Dosha: Mars in house %d from the Ascendant. Compatibility with a partner's chart is worth checking.",
                k.houseOf(mars)
            )
        } else out += Lang.t("No Mangal Dosha from the Ascendant.")

        val moon = k.planet("Moon")
        val jup = k.planet("Jupiter")
        if ((k.houseOf(jup) - k.houseOf(moon) + 12) % 12 in listOf(0, 3, 6, 9)) {
            out += Lang.t("Gajakesari Yoga: Jupiter in a kendra from the Moon – brings wisdom, reputation and good judgment.")
        }
        val sun = k.planet("Sun")
        val mer = k.planet("Mercury")
        if (sun.sign == mer.sign) out += Lang.t("Budhaditya Yoga: Sun and Mercury together – sharp intellect and communication.")
        for (n in listOf("Mars", "Jupiter", "Saturn", "Venus", "Mercury")) {
            val p = k.planet(n)
            if ((SIGN_LORDS[p.sign] == n || exalt[n] == p.sign) && k.houseOf(p) in listOf(1, 4, 7, 10)) {
                val name = when (n) {
                    "Mars" -> "Ruchaka"; "Mercury" -> "Bhadra"; "Jupiter" -> "Hamsa"
                    "Venus" -> "Malavya"; else -> "Shasha"
                }
                out += Lang.t("%s Yoga (Pancha Mahapurusha): %s strong in a kendra – a mark of distinction.", Lang.t(name), Lang.planet(n))
            }
        }
        return out.joinToString("\n\n")
    }

    fun dashaInsights(k: Kundli, now: Long, fmt: SimpleDateFormat): String = buildString {
        val cur = k.dashas.firstOrNull { now in it.startMillis until it.endMillis }
        if (cur != null) {
            append(Lang.t(
                "Current Mahadasha: %s (%s – %s)\n",
                Lang.planet(cur.lord), fmt.format(Date(cur.startMillis)), fmt.format(Date(cur.endMillis))
            ))
            append(Lang.t(dashaText.getValue(cur.lord))).append("\n")
            val lp = k.planet(cur.lord)
            append(Lang.t(
                "%s is in house %d, so results centre on %s.\n\n",
                Lang.planet(cur.lord), k.houseOf(lp), Lang.t(houseMeaning[k.houseOf(lp) - 1])
            ))
        }
        append(Lang.t("Vimshottari Mahadasha sequence:\n"))
        for (d in k.dashas) {
            append(Lang.t(
                "• %s%s – %s\n",
                Lang.pad(Lang.planet(d.lord), 9), fmt.format(Date(d.startMillis)), fmt.format(Date(d.endMillis))
            ))
        }
    }

    fun disclaimer() = Lang.t(
        "Positions: Astronomy Engine (VSOP87/ELP), Lahiri ayanamsa, mean lunar node, whole-sign houses; planet longitudes agree with JPL Horizons to a few arc-seconds. Interpretations are generic and for self-reflection only; consult a professional astrologer for important decisions."
    )}
