package elendor.vedicastrology

import java.text.SimpleDateFormat
import java.util.TimeZone
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KannadaCoverageTest {
    private val tz = TimeZone.getTimeZone("Asia/Kolkata")
    private val now = java.util.GregorianCalendar(tz).apply { clear(); set(2025, 5, 1, 12, 0) }.timeInMillis

    @After fun reset() { Lang.current = AppLang.EN }

    private fun reports(birth: Long = java.util.GregorianCalendar(tz).apply { clear(); set(1990, 4, 15, 4, 30) }.timeInMillis): List<String> {
        val k = AstroEngine.compute(birth, 19.076, 72.8777, tz)
        val sb = Shadbala.compute(k)
        val fmt = SimpleDateFormat("dd MMM yyyy", Lang.locale())
        val out = mutableListOf(
            Forecast.summary(k, sb, now), Forecast.monthlyOutlook(k, sb, now), Forecast.areaOutlook(k, sb, now),
            Forecast.dashaForecast(k, sb, now), Forecast.transitReport(k, sb, now), Forecast.ingressReport(k, now),
            Forecast.ashtakavargaReport(k), Forecast.shadbalaReport(k, sb), Forecast.vedhaReport(k, now),
            Vedic.panchang(k, now, null, null, tz), Vedic.navamsaText(k), Vedic.bhavaTable(k), Vedic.aspects(k),
            Vedic.combustion(k), Vedic.extraYogas(k), Insights.planetTable(k), Insights.overview(k),
            Insights.planetInsights(k), Insights.yogas(k), Insights.dashaInsights(k, now, fmt), Insights.disclaimer()
        )
        for (p in k.planets) out += Readings.planetReading(p, k, sb)
        for (h in 1..12) out += Readings.houseReading(h, k, now)
        for (a in listOf("Career", "Marriage", "Wealth", "Health", "Home", "Children", "Travel")) out += Readings.lifeAreaReading(a, k, sb, now)
        for (d in k.dashas.take(3)) out += Readings.dashaReading(d.lord, k, d.startMillis, d.endMillis)
        return out
    }

    @Test
    fun kannadaTemplatesMatchEnglishPlaceholders() {
        fun types(s: String): Map<Int, String>? {
            val out = mutableMapOf<Int, String>()
            var seq = 0
            var positional = 0
            for (m in Regex("%(?:(\\d+)\\$)?[-#+ 0,(]*\\d*(?:\\.\\d+)?([a-zA-Z%])").findAll(s)) {
                if (m.groupValues[2] == "%" || m.groupValues[2] == "n") continue
                if (m.groupValues[1].isNotEmpty()) { positional++; out[m.groupValues[1].toInt()] = m.groupValues[2] }
                else out[++seq] = m.groupValues[2]
            }
            return if (positional > 0 && seq > 0) null else out
        }
        val bad = mutableListOf<String>()
        for ((en, kn) in KN_VEDIC + KN_FORECAST + KN_SHADBALA + KN_UI) {
            val e = types(en)
            val k = types(kn)
            if (k == null || (e != null && e.values.sorted() != k.values.sorted()) || (e != null && k.keys.any { it !in 1..e.size }))
                bad += en
        }
        bad.forEach { println("BAD TEMPLATE: $it") }
        assertTrue("bad templates: ${bad.size}", bad.isEmpty())
    }

    @Test
    fun kannadaReportsHaveNoUntranslatedText() {
        Lang.current = AppLang.EN
        val births = (0 until 60).map { i ->
            java.util.GregorianCalendar(tz).apply { clear(); set(1950 + i, (i * 5) % 12, 1 + (i * 7) % 28, (i * 3) % 24, 15) }.timeInMillis
        }
        val en = births.flatMap { reports(it) }
        Lang.current = AppLang.KN
        Lang.missing.clear()
        val kn = births.flatMap { reports(it) }
        assertEquals(en.size, kn.size)
        println("MISSING KEYS: ${Lang.missing.size}")
        Lang.missing.take(40).forEach { println("  MISSING: $it") }
        val words = Regex("[A-Za-z]{4,}")
        val left = kn.flatMap { words.findAll(it).map { m -> m.value }.toList() }.groupingBy { it }.eachCount()
        println("LATIN WORDS LEFT: $left")
        assertTrue("untranslated: ${Lang.missing.size}", Lang.missing.isEmpty())
    }
}
