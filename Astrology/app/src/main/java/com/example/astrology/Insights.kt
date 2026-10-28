package com.example.astrology

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
        append("Ascendant (Lagna): ${SIGNS[k.lagnaSign]} ${dms(k.ascendant % 30)}\n\n")
        for (p in k.planets) {
            val dig = dignity(p)
            append("%-8s %-12s %s  H%-2d  %s%s%s\n".format(
                p.name, SIGNS[p.sign], dms(p.degInSign), k.houseOf(p),
                NAKSHATRAS[p.nakshatra] + "-" + p.pada,
                if (p.retrograde && p.name != "Rahu" && p.name != "Ketu") " (R)" else "",
                if (dig.isEmpty()) "" else "  [$dig]"
            ))
        }
        append("\nAyanamsa (Lahiri): %.3f°".format(k.ayanamsa))
    }

    fun overview(k: Kundli): String {
        val moon = k.planet("Moon")
        val sun = k.planet("Sun")
        return buildString {
            append("Ascendant – ${SIGNS[k.lagnaSign]} (lord ${SIGN_LORDS[k.lagnaSign]}):\n")
            append(lagnaText[k.lagnaSign]).append("\n\n")
            append("Moon sign (Rashi) – ${SIGNS[moon.sign]}, nakshatra ${NAKSHATRAS[moon.nakshatra]} pada ${moon.pada}: ")
            append(moonText[moon.sign]).append(".\n\n")
            append("Sun sign (sidereal) – ${SIGNS[sun.sign]}: your core identity expresses through the qualities of ${SIGNS[sun.sign]}.\n\n")
            val lagnaLord = SIGN_LORDS[k.lagnaSign]
            val ll = k.planet(lagnaLord)
            append("Lagna lord $lagnaLord sits in ${SIGNS[ll.sign]} in house ${k.houseOf(ll)} – life energy is directed towards ${houseMeaning[k.houseOf(ll) - 1]}.")
        }
    }

    fun planetInsights(k: Kundli): String = buildString {
        for (p in k.planets) {
            val h = k.houseOf(p)
            append("• ${p.name} in ${SIGNS[p.sign]}, house $h")
            val dig = dignity(p)
            if (dig.isNotEmpty()) append(" ($dig)")
            append("\n  ${planetTheme[p.name]} are channelled into ${houseMeaning[h - 1]}.")
            when (dig) {
                "Exalted", "Own sign" -> append(" This placement is strong and supportive.")
                "Debilitated" -> append(" This placement needs conscious effort; results improve with maturity.")
            }
            append("\n")
        }
    }

    fun yogas(k: Kundli): String {
        val out = mutableListOf<String>()
        val mars = k.planet("Mars")
        if (k.houseOf(mars) in listOf(1, 2, 4, 7, 8, 12)) {
            out += "Mangal Dosha: Mars in house ${k.houseOf(mars)} from the Ascendant. Compatibility with a partner's chart is worth checking."
        } else out += "No Mangal Dosha from the Ascendant."

        val moon = k.planet("Moon")
        val jup = k.planet("Jupiter")
        if ((k.houseOf(jup) - k.houseOf(moon) + 12) % 12 in listOf(0, 3, 6, 9)) {
            out += "Gajakesari Yoga: Jupiter in a kendra from the Moon – brings wisdom, reputation and good judgment."
        }
        val sun = k.planet("Sun")
        val mer = k.planet("Mercury")
        if (sun.sign == mer.sign) out += "Budhaditya Yoga: Sun and Mercury together – sharp intellect and communication."
        for (n in listOf("Mars", "Jupiter", "Saturn", "Venus", "Mercury")) {
            val p = k.planet(n)
            if ((SIGN_LORDS[p.sign] == n || exalt[n] == p.sign) && k.houseOf(p) in listOf(1, 4, 7, 10)) {
                val name = when (n) {
                    "Mars" -> "Ruchaka"; "Mercury" -> "Bhadra"; "Jupiter" -> "Hamsa"
                    "Venus" -> "Malavya"; else -> "Shasha"
                }
                out += "$name Yoga (Pancha Mahapurusha): $n strong in a kendra – a mark of distinction."
            }
        }
        return out.joinToString("\n\n")
    }

    fun dashaInsights(k: Kundli, now: Long, fmt: SimpleDateFormat): String = buildString {
        val cur = k.dashas.firstOrNull { now in it.startMillis until it.endMillis }
        if (cur != null) {
            append("Current Mahadasha: ${cur.lord} (${fmt.format(Date(cur.startMillis))} – ${fmt.format(Date(cur.endMillis))})\n")
            append(dashaText.getValue(cur.lord)).append("\n")
            val lp = k.planet(cur.lord)
            append("${cur.lord} is in house ${k.houseOf(lp)}, so results centre on ${houseMeaning[k.houseOf(lp) - 1]}.\n\n")
        }
        append("Vimshottari Mahadasha sequence:\n")
        for (d in k.dashas) {
            append("• %-8s %s – %s\n".format(d.lord, fmt.format(Date(d.startMillis)), fmt.format(Date(d.endMillis))))
        }
    }

    fun disclaimer() = "Positions: Astronomy Engine (VSOP87/ELP), Lahiri ayanamsa, mean lunar node, whole-sign houses; " +
        "planet longitudes agree with JPL Horizons to a few arc-seconds. Interpretations are generic and for " +
        "self-reflection only; consult a professional astrologer for important decisions."
}
