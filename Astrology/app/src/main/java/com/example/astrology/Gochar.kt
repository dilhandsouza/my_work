package com.example.astrology

/**
 * Gochar (transit) results from the natal Moon with Vedha, per Phaladeepika / Brihat Samhita.
 * A planet transiting a favourable house is obstructed when another planet occupies that house's Vedha
 * house (both counted from the natal Moon). Exempt pairs: Sun-Saturn and Moon-Mercury do not obstruct
 * each other. Rahu and Ketu neither cause nor receive Vedha here.
 */
object Gochar {

    data class Strength(val rating: String, val vedha: String?)

    /** Favourable house to Vedha house, per planet. */
    private val VEDHA: Map<String, Map<Int, Int>> = mapOf(
        "Sun" to mapOf(3 to 9, 6 to 12, 10 to 4, 11 to 5),
        "Moon" to mapOf(1 to 5, 3 to 9, 6 to 12, 7 to 2, 10 to 4, 11 to 8),
        "Mars" to mapOf(3 to 12, 6 to 9, 11 to 5),
        "Mercury" to mapOf(2 to 5, 4 to 3, 6 to 9, 8 to 1, 10 to 8, 11 to 12),
        "Jupiter" to mapOf(2 to 12, 5 to 4, 7 to 3, 9 to 10, 11 to 8),
        "Venus" to mapOf(1 to 8, 2 to 7, 3 to 1, 4 to 10, 5 to 9, 8 to 5, 9 to 11, 11 to 6, 12 to 3),
        "Saturn" to mapOf(3 to 12, 6 to 9, 11 to 5)
    )

    private val NODE_FAVOURABLE = setOf(3, 6, 10, 11)

    private val EXEMPT = setOf(setOf("Sun", "Saturn"), setOf("Moon", "Mercury"))

    fun transitStrength(moonSign: Int, now: Long): Map<String, Strength> {
        val transit = AstroEngine.siderealAt(now)
        val houses = transit.associate { it.name to Vedic.houseOf(it.sign, moonSign) }
        val out = linkedMapOf<String, Strength>()

        for (p in transit) {
            val h = houses.getValue(p.name)
            val table = VEDHA[p.name]
            if (table == null) {
                out[p.name] = Strength(if (h in NODE_FAVOURABLE) "Strong" else "Neutral", null)
                continue
            }
            val vedhaHouse = table[h]
            if (vedhaHouse == null) {
                out[p.name] = Strength(if (h in listOf(6, 8, 12)) "Challenging" else "Unfavourable", null)
                continue
            }
            val blocker = transit.firstOrNull {
                it.name != p.name && it.name in VEDHA && houses.getValue(it.name) == vedhaHouse &&
                    setOf(it.name, p.name) !in EXEMPT
            }
            out[p.name] = if (blocker != null) {
                Strength("Obstructed", "house $h blocked by ${blocker.name} in house $vedhaHouse")
            } else Strength("Strong", null)
        }
        return out
    }

    fun bavInTransitSign(k: Kundli, planet: String, now: Long): Int {
        val p = AstroEngine.siderealAt(now).firstOrNull { it.name == planet } ?: return 0
        return k.ashtakavarga.bav[planet]?.get(p.sign) ?: 0
    }
}
