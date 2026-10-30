package elendor.vedicastrology

import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClassicalChecksTest {
    private val tz = TimeZone.getTimeZone("Asia/Kolkata")

    // 1990-05-15 04:30 IST, Mumbai
    private fun chart() = AstroEngine.compute(
        java.util.GregorianCalendar(tz).apply { clear(); set(1990, 4, 15, 4, 30) }.timeInMillis,
        19.076, 72.8777, tz
    )

    @Test
    fun ashtakavargaMatchesClassicalTotals() {
        val av = chart().ashtakavarga
        val expected = mapOf(
            "Sun" to 48, "Moon" to 49, "Mars" to 39, "Mercury" to 54,
            "Jupiter" to 56, "Venus" to 52, "Saturn" to 39
        )
        for ((planet, total) in expected) assertEquals(planet, total, av.bav.getValue(planet).values.sum())
        assertEquals(337, av.total)
    }

    @Test
    fun shadbalaIsInPlausibleRange() {
        val sb = Shadbala.compute(chart())
        assertEquals(7, sb.size)
        for ((name, r) in sb) {
            println("$name sthana=${r.sthana} dig=${r.dig} kala=${r.kala} cheshta=${r.cheshta} drik=${r.drik} total=${r.total}/${r.required} ${r.rating()}")
            assertTrue("$name total ${r.total}", r.total in 2.0..15.0)
            assertTrue("$name dig ${r.dig}", r.dig in 0.0..1.0)
            assertTrue("$name kala ${r.kala}", r.kala >= 0.0)
        }
    }

    @Test
    fun vedhaRunsForAllPlanets() {
        assertEquals(9, Gochar.transitStrength(0, System.currentTimeMillis()).size)
    }

    /** Writes component values (rupas) for fixed births so verify/compare_shadbala.py can diff them against PyJHora. */
    @Test
    fun dumpShadbalaForReferenceComparison() {
        val cases = listOf(
            Triple("Mumbai 1990-05-15 04:30", intArrayOf(1990, 4, 15, 4, 30), doubleArrayOf(19.076, 72.8777)),
            Triple("Delhi 1985-11-02 14:15", intArrayOf(1985, 10, 2, 14, 15), doubleArrayOf(28.6139, 77.2090)),
            Triple("Chennai 2000-01-01 22:10", intArrayOf(2000, 0, 1, 22, 10), doubleArrayOf(13.0827, 80.2707))
        )
        val json = StringBuilder("{")
        cases.forEachIndexed { i, (label, t, loc) ->
            val ms = java.util.GregorianCalendar(tz).apply { clear(); set(t[0], t[1], t[2], t[3], t[4]) }.timeInMillis
            val chart = AstroEngine.compute(ms, loc[0], loc[1], tz)
            val sb = Shadbala.compute(chart)
            if (i > 0) json.append(",")
            json.append("\"$label\":{")
            json.append(sb.entries.joinToString(",") { (n, r) ->
                "\"$n\":{\"sthana\":${r.sthana},\"kala\":${r.kala},\"dig\":${r.dig},\"cheshta\":${r.cheshta}," +
                    "\"naisargika\":${r.naisargika},\"drik\":${r.drik},\"total\":${r.total}}"
            })
            json.append("}")
        }
        json.append("}")
        java.io.File("../verify/kotlin_shadbala.json").writeText(json.toString())
    }
}
