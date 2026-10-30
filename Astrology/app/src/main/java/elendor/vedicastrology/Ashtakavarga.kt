package elendor.vedicastrology

/**
 * Parashari Ashtakavarga. For each of the 7 planets, eight contributors (Sun..Saturn and Lagna)
 * give a bindu to the signs lying in the listed houses counted from the contributor's own sign.
 * The sum over the seven planets (Sarvashtakavarga) totals the classical 337.
 */
object Ashtakavarga {

    data class Result(
        val bav: Map<String, Map<Int, Int>>,
        val sav: Map<Int, Int>,
        val total: Int
    )

    private val CONTRIBUTORS = listOf("Sun", "Moon", "Mars", "Mercury", "Jupiter", "Venus", "Saturn", "Lagna")

    private fun h(s: String) = s.split(",").map { it.trim().toInt() }

    // Rows follow CONTRIBUTORS order.
    private val TABLE: Map<String, List<List<Int>>> = mapOf(
        "Sun" to listOf("1,2,4,7,8,9,10,11", "3,6,10,11", "1,2,4,7,8,9,10,11", "3,5,6,9,10,11,12",
            "5,6,9,11", "6,7,12", "1,2,4,7,8,9,10,11", "3,4,6,10,11,12").map(::h),
        "Moon" to listOf("3,6,7,8,10,11", "1,3,6,7,10,11", "2,3,5,6,9,10,11", "1,3,4,5,7,8,10,11",
            "1,4,7,8,10,11,12", "3,4,5,7,9,10,11", "3,5,6,11", "3,6,10,11").map(::h),
        "Mars" to listOf("3,5,6,10,11", "3,6,11", "1,2,4,7,8,10,11", "3,5,6,11",
            "6,10,11,12", "6,8,11,12", "1,4,7,8,9,10,11", "1,3,6,10,11").map(::h),
        "Mercury" to listOf("5,6,9,11,12", "2,4,6,8,10,11", "1,2,4,7,8,9,10,11", "1,3,5,6,9,10,11,12",
            "6,8,11,12", "1,2,3,4,5,8,9,11", "1,2,4,7,8,9,10,11", "1,2,4,6,8,10,11").map(::h),
        "Jupiter" to listOf("1,2,3,4,7,8,9,10,11", "2,5,7,9,11", "1,2,4,7,8,10,11", "1,2,4,5,6,9,10,11",
            "1,2,3,4,7,8,10,11", "2,5,6,9,10,11", "3,5,6,12", "1,2,4,5,6,7,9,10,11").map(::h),
        "Venus" to listOf("8,11,12", "1,2,3,4,5,8,9,11,12", "3,5,6,9,11,12", "3,5,6,9,11",
            "5,8,9,10,11", "1,2,3,4,5,8,9,10,11", "3,4,5,8,9,10,11", "1,2,3,4,5,8,9,11").map(::h),
        "Saturn" to listOf("1,2,4,7,8,10,11", "3,6,11", "3,5,6,10,11,12", "6,8,9,10,11,12",
            "5,6,11,12", "6,11,12", "3,5,6,11", "1,3,4,6,10,11").map(::h)
    )

    fun compute(k: Kundli): Result {
        val contributorSign = CONTRIBUTORS.map { c ->
            if (c == "Lagna") k.lagnaSign else k.planet(c).sign
        }
        val bav = mutableMapOf<String, Map<Int, Int>>()
        val sav = IntArray(12)
        for ((planet, rows) in TABLE) {
            val per = IntArray(12)
            rows.forEachIndexed { i, houses ->
                for (house in houses) per[(contributorSign[i] + house - 1) % 12]++
            }
            bav[planet] = per.indices.associateWith { per[it] }
            for (s in 0..11) sav[s] += per[s]
        }
        return Result(bav, sav.indices.associateWith { sav[it] }, sav.sum())
    }
}
