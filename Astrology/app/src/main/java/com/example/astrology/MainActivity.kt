package com.example.astrology

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Typeface
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.concurrent.thread

private class Report(
    val k: Kundli,
    val fmt: SimpleDateFormat,
    val transit: List<Pair<String, Int>>,
    val texts: List<String>
)

class MainActivity : AppCompatActivity() {

    private var year = 1995
    private var month = 0
    private var day = 1
    private var hour = 12
    private var minute = 0
    private var dateSet = false
    private var timeSet = false

    private lateinit var btnDate: Button
    private lateinit var btnTime: Button
    private lateinit var progress: ProgressBar
    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Kundli Astrology"

        btnDate = findViewById(R.id.btnDate)
        btnTime = findViewById(R.id.btnTime)
        progress = findViewById(R.id.progress)
        container = findViewById(R.id.resultContainer)

        btnDate.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                year = y; month = m; day = d; dateSet = true
                btnDate.text = "Date: %04d-%02d-%02d".format(y, m + 1, d)
            }, year, month, day).show()
        }
        btnTime.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                hour = h; minute = m; timeSet = true
                btnTime.text = "Time: %02d:%02d".format(h, m)
            }, hour, minute, true).show()
        }
        findViewById<Button>(R.id.btnGenerate).setOnClickListener { generate() }
    }

    private fun generate() {
        if (!dateSet || !timeSet) {
            toast("Please select birth date and time"); return
        }
        val tzId = findViewById<EditText>(R.id.etTz).text.toString().trim()
        val tz = TimeZone.getTimeZone(tzId)
        if (tz.id == "GMT" && !tzId.equals("GMT", true) && !tzId.equals("UTC", true)) {
            toast("Unknown time zone ID: $tzId"); return
        }
        val latStr = findViewById<EditText>(R.id.etLat).text.toString().trim()
        val lonStr = findViewById<EditText>(R.id.etLon).text.toString().trim()
        val place = findViewById<EditText>(R.id.etPlace).text.toString().trim()
        val name = findViewById<EditText>(R.id.etName).text.toString().trim()

        if (latStr.isNotEmpty() && lonStr.isNotEmpty()) {
            val lat = latStr.toDoubleOrNull()
            val lon = lonStr.toDoubleOrNull()
            if (lat == null || lon == null || lat !in -89.0..89.0 || lon !in -180.0..180.0) {
                toast("Invalid latitude/longitude"); return
            }
            show(name, place.ifEmpty { "%.3f, %.3f".format(lat, lon) }, lat, lon, tz)
            return
        }
        if (place.isEmpty()) {
            toast("Enter a place or latitude/longitude"); return
        }
        progress.visibility = View.VISIBLE
        thread {
            val result = try {
                @Suppress("DEPRECATION")
                Geocoder(this, Locale.getDefault()).getFromLocationName(place, 1)?.firstOrNull()
            } catch (e: Exception) {
                null
            }
            runOnUiThread {
                progress.visibility = View.GONE
                if (result == null) {
                    toast("Could not find the place. Check internet or enter latitude/longitude.")
                } else {
                    findViewById<EditText>(R.id.etLat).setText("%.4f".format(result.latitude))
                    findViewById<EditText>(R.id.etLon).setText("%.4f".format(result.longitude))
                    show(name, place, result.latitude, result.longitude, tz)
                }
            }
        }
    }

    private fun show(name: String, place: String, lat: Double, lon: Double, tz: TimeZone) {
        val cal = Calendar.getInstance(tz).apply {
            clear(); set(year, month, day, hour, minute, 0)
        }
        val birth = cal.timeInMillis
        val header = (if (name.isNotEmpty()) "$name\n" else "") +
            "%04d-%02d-%02d %02d:%02d (%s)\n%s  (%.3f, %.3f)".format(year, month + 1, day, hour, minute, tz.id, place, lat, lon)
        progress.visibility = View.VISIBLE
        container.removeAllViews()

        thread {
            val now = System.currentTimeMillis()
            val result = try {
                val k = AstroEngine.compute(birth, lat, lon, tz)
                val (rise, set) = AstroEngine.sunriseSunset(birth, lat, lon, tz)
                val fmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val transit = AstroEngine.siderealAt(now).map { it.name to it.sign }

                val shadbala = Shadbala.compute(k)
                val summary = Forecast.summary(k, shadbala, now)
                val dashaF = Forecast.dashaForecast(k, shadbala, now)
                val monthly = Forecast.monthlyOutlook(k, shadbala, now)
                val areas = Forecast.areaOutlook(k, shadbala, now)
                val transitText = Forecast.transitReport(k, shadbala, now)
                val ingress = Forecast.ingressReport(k, now)
                val panchang = Vedic.panchang(k, birth, rise, set, tz)
                val ashtakavargaText = Forecast.ashtakavargaReport(k)
                val shadbalaText = Forecast.shadbalaReport(k, shadbala)
                val vedhaText = Forecast.vedhaReport(k, now)
                Report(k, fmt, transit, listOf(summary, dashaF, monthly, areas, transitText, ingress, panchang, ashtakavargaText, shadbalaText, vedhaText))
            } catch (e: Exception) {
                null
            }
            runOnUiThread {
                progress.visibility = View.GONE
                if (result == null) {
                    toast("Could not compute the chart"); return@runOnUiThread
                }
                val k = result.k
                val fmt = result.fmt
                val transit = result.transit
                val moonSign = k.planet("Moon").sign
                val summary = result.texts[0]
                val dashaF = result.texts[1]
                val monthly = result.texts[2]
                val areas = result.texts[3]
                val transitText = result.texts[4]
                val ingress = result.texts[5]
                val panchang = result.texts[6]
                val ashtakavargaText = result.texts[7]
                val shadbalaText = result.texts[8]
                val vedhaText = result.texts[9]
                val shadbala = Shadbala.compute(k)

                addText(header, 14f, false)
                addTitle("Your Near Future at a Glance")
                addText(summary, 14f, false)
                addTitle("Next 12 Months � Month by Month")
                addText(monthly, 12f, true)
                addTitle("Life Areas Right Now (Jupiter & Saturn)")
                addText(areas, 13f, false)
                addTitle("Dasha Forecast (Vimshottari)")
                addText(dashaF, 13f, false)
                addTitle("Planetary Transits (Gochar) & Sade Sati")
                addText(transitText, 11f, true)
                addTitle("Upcoming Major Sign Changes (3 years)")
                addText(ingress, 13f, false)

                addTitle("Janma Kundli � Lagna Chart (D1)")
                container.addView(KundliView(this, k.lagnaSign, k.planets.map { it.name to it.sign }))
                addTitle("Chandra Kundli � Moon Chart")
                container.addView(KundliView(this, moonSign, Vedic.moonPlacements(k)))
                addTitle("Navamsa Chart (D9)")
                container.addView(KundliView(this, Vedic.navamsaSign(k.ascendant), Vedic.navamsaPlacements(k)))
                addText("Numbers are zodiac signs (1 = Aries � 12 = Pisces); Lg = Lagna (house 1, top diamond).", 11f, false)
                addTitle("Current Transit Chart (Lagna as reference)")
                container.addView(KundliView(this, k.lagnaSign, transit))

                addTitle("Panchang at Birth")
                addText(panchang, 13f, false)
                addTitle("Planetary Positions (Nirayana, Lahiri)")
                addText(Insights.planetTable(k), 11f, true)
                addTitle("Bhava (House) Table")
                addText(Vedic.bhavaTable(k), 11f, true)
                addTitle("Navamsa Positions")
                addText(Vedic.navamsaText(k), 11f, true)
                addTitle("Graha Drishti (Aspects)")
                addText(Vedic.aspects(k), 12f, false)
                addTitle("Combustion & Retrograde")
                addText(Vedic.combustion(k), 13f, false)
                addTitle("Personality Overview")
                addText(Insights.overview(k), 14f, false)
                addTitle("Planet-by-Planet Insights")
                addText(Insights.planetInsights(k), 13f, false)
                addTitle("Yogas & Doshas")
                addText(listOf(Insights.yogas(k), Vedic.extraYogas(k)).filter { it.isNotEmpty() }.joinToString("\n\n"), 13f, false)
                addTitle("Ashtakavarga (Benefic Aspect Values)")
                addText(ashtakavargaText, 12f, true)
                addTitle("Shadbala (Six-Fold Strength Scores)")
                addText(shadbalaText, 12f, false)
                addTitle("Vedha (Transit Obstructions)")
                addText(vedhaText, 12f, false)

                addTitle("Detailed Planet Readings")
                for (p in k.planets) {
                    addText(Readings.planetReading(p, k, shadbala), 11f, false)
                }

                addTitle("House Readings")
                for (h in 1..12) {
                    addText(Readings.houseReading(h, k, System.currentTimeMillis()), 11f, false)
                }

                addTitle("Life Area Readings")
                for (area in listOf("Career", "Marriage", "Wealth", "Health", "Home", "Children", "Travel")) {
                    addTitle("$area")
                    addText(Readings.lifeAreaReading(area, k, shadbala, System.currentTimeMillis()), 11f, false)
                }

                addTitle("Dasha Period Readings")
                val now = System.currentTimeMillis()
                val maha = k.dashas.firstOrNull { now >= it.startMillis && now < it.endMillis }
                if (maha != null) {
                    addTitle("Current Mahadasha: ${maha.lord}")
                    addText(Readings.dashaReading(maha.lord, k, maha.startMillis, maha.endMillis), 11f, false)
                    val antars = AstroEngine.subPeriods(maha)
                    val antar = antars.firstOrNull { now >= it.startMillis && now < it.endMillis }
                    if (antar != null) {
                        addTitle("Current Antardasha: ${antar.lord}")
                        addText(Readings.dashaReading("${maha.lord}–${antar.lord}", k, antar.startMillis, antar.endMillis), 11f, false)
                    }
                }

                addText(Insights.disclaimer(), 11f, false)
            }
        }
    }
    private fun addTitle(t: String) {
        val tv = addText(t, 18f, false)
        tv.setTypeface(null, Typeface.BOLD)
        tv.setTextColor(0xFF5B2A86.toInt())
        tv.setPadding(0, 32, 0, 8)
    }

    private fun addText(t: String, size: Float, mono: Boolean): TextView {
        val tv = TextView(this)
        tv.text = t
        tv.textSize = size
        if (mono) tv.typeface = Typeface.MONOSPACE
        tv.setTextIsSelectable(true)
        container.addView(tv)
        return tv
    }

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_LONG).show()
}
