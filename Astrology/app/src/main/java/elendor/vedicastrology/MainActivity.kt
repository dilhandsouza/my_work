package elendor.vedicastrology

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Filter
import android.widget.Filterable
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
    private var selectedPlace: Place? = null
    private var searchId = 0
    private var searchRunnable: Runnable? = null
    private val handler = Handler(Looper.getMainLooper())
    private val blocks = mutableListOf<Block>()
    private var reportName = ""
    private var infoKey = "Type at least 2 letters and pick your birth place from the suggestions."
    private var infoArgs: Array<Any?> = emptyArray()
    private var lastShown: (() -> Unit)? = null

    private fun setInfo(key: String, vararg args: Any?) {
        infoKey = key; infoArgs = arrayOf(*args)
        findViewById<TextView>(R.id.tvPlaceInfo).text = Lang.t(key, *args)
    }

    private fun applyLanguage() {
        title = Lang.t("Kundli Astrology")
        findViewById<EditText>(R.id.etName).hint = Lang.t("Name (optional)")
        btnDate.text = if (dateSet) Lang.t("Date: %s", "%04d-%02d-%02d".format(year, month + 1, day)) else Lang.t("Select birth date")
        btnTime.text = if (timeSet) Lang.t("Time: %s", "%02d:%02d".format(hour, minute)) else Lang.t("Select birth time")
        findViewById<AutoCompleteTextView>(R.id.etPlace).hint = Lang.t("Birth place (start typing a city)")
        findViewById<TextView>(R.id.tvStyleLabel).text = Lang.t("Chart style")
        findViewById<RadioButton>(R.id.rbNorth).text = Lang.t("North Indian")
        findViewById<RadioButton>(R.id.rbSouth).text = Lang.t("South Indian")
        findViewById<Button>(R.id.btnGenerate).text = Lang.t("Generate Kundli")
        findViewById<Button>(R.id.btnSavePdf).text = Lang.t("Save PDF")
        findViewById<Button>(R.id.btnSharePdf).text = Lang.t("Share PDF")
        findViewById<TextView>(R.id.tvPlaceInfo).text = Lang.t(infoKey, *infoArgs)
    }

    private fun setupLanguageSelector() {
        val prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        Lang.current = AppLang.entries.firstOrNull { it.code == prefs.getString("lang", "en") } ?: AppLang.EN
        val rg = findViewById<RadioGroup>(R.id.rgLang)
        rg.check(if (Lang.isKn) R.id.rbKannada else R.id.rbEnglish)
        applyLanguage()
        rg.setOnCheckedChangeListener { _, id ->
            val sel = if (id == R.id.rbKannada) AppLang.KN else AppLang.EN
            if (sel == Lang.current) return@setOnCheckedChangeListener
            Lang.current = sel
            prefs.edit().putString("lang", sel.code).apply()
            applyLanguage()
            // The report is language dependent, so redraw it in the new language.
            lastShown?.invoke()
        }

        val rgStyle = findViewById<RadioGroup>(R.id.rgChartStyle)
        KundliDraw.south = prefs.getBoolean("south", false)
        rgStyle.check(if (KundliDraw.south) R.id.rbSouth else R.id.rbNorth)
        rgStyle.setOnCheckedChangeListener { _, id ->
            val south = id == R.id.rbSouth
            if (south == KundliDraw.south) return@setOnCheckedChangeListener
            KundliDraw.south = south
            prefs.edit().putBoolean("south", south).apply()
            lastShown?.invoke()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Target SDK 35+ draws edge-to-edge, so keep content clear of the system bars.
        val content = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(content) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        btnDate = findViewById(R.id.btnDate)
        btnTime = findViewById(R.id.btnTime)
        progress = findViewById(R.id.progress)
        container = findViewById(R.id.resultContainer)
        setupLanguageSelector()

        btnDate.setOnClickListener {
            DatePickerDialog(this, { _, y, m, d ->
                year = y; month = m; day = d; dateSet = true
                btnDate.text = Lang.t("Date: %s", "%04d-%02d-%02d".format(y, m + 1, d))
            }, year, month, day).show()
        }
        btnTime.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                hour = h; minute = m; timeSet = true
                btnTime.text = Lang.t("Time: %s", "%02d:%02d".format(h, m))
            }, hour, minute, true).show()
        }
        findViewById<Button>(R.id.btnGenerate).setOnClickListener { generate() }
        findViewById<Button>(R.id.btnSavePdf).setOnClickListener { createPdf.launch(pdfFileName()) }
        findViewById<Button>(R.id.btnSharePdf).setOnClickListener { sharePdf() }
        setupPlaceSearch()
    }

    private class PlaceAdapter(ctx: Context) : BaseAdapter(), Filterable {
        var items: List<Place> = emptyList()
        private val inflater = LayoutInflater.from(ctx)
        override fun getCount() = items.size
        override fun getItem(position: Int) = items[position]
        override fun getItemId(position: Int) = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val v = convertView ?: inflater.inflate(android.R.layout.simple_dropdown_item_1line, parent, false)
            (v as TextView).text = items[position].label
            return v
        }
        // Results come from the network, so the built-in text filtering must not touch them.
        override fun getFilter() = object : Filter() {
            override fun performFiltering(constraint: CharSequence?) =
                FilterResults().apply { values = items; count = items.size }
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) = notifyDataSetChanged()
        }
    }

    private fun setupPlaceSearch() {
        val etPlace = findViewById<AutoCompleteTextView>(R.id.etPlace)
        val info = findViewById<TextView>(R.id.tvPlaceInfo)
        val adapter = PlaceAdapter(this)
        etPlace.setAdapter(adapter)
        var suppress = false

        etPlace.setOnItemClickListener { _, _, position, _ ->
            val p = adapter.getItem(position)
            selectedPlace = p
            suppress = true
            etPlace.setText(p.label)
            etPlace.setSelection(p.label.length)
            suppress = false
            setInfo("Selected: %s, %s  |  time zone %s", "%.4f".format(p.latitude), "%.4f".format(p.longitude), p.timeZone)
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(etPlace.windowToken, 0)
        }
        etPlace.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (suppress) return
                selectedPlace = null
                searchRunnable?.let { handler.removeCallbacks(it) }
                val q = s?.toString()?.trim().orEmpty()
                if (q.length < 2) {
                    adapter.items = emptyList(); adapter.notifyDataSetChanged()
                    setInfo("Type at least 2 letters and pick your birth place from the suggestions.")
                    return
                }
                setInfo("Searching…")
                val r = Runnable {
                    val id = ++searchId
                    thread {
                        val res = try { PlaceSearch.search(q) } catch (e: Exception) { null }
                        runOnUiThread {
                            if (id != searchId || selectedPlace != null) return@runOnUiThread
                            when {
                                res == null -> setInfo("Could not reach the place search. Check your internet connection.")
                                res.isEmpty() -> { adapter.items = res; adapter.notifyDataSetChanged(); setInfo("No matching places found.") }
                                else -> {
                                    adapter.items = res; adapter.notifyDataSetChanged()
                                    setInfo("Pick your birth place from the suggestions.")
                                    if (etPlace.hasFocus()) etPlace.showDropDown()
                                }
                            }
                        }
                    }
                }
                searchRunnable = r
                handler.postDelayed(r, 400)
            }
        })
    }

    private fun generate() {
        if (!dateSet || !timeSet) {
            toast(Lang.t("Please select birth date and time")); return
        }
        val name = findViewById<EditText>(R.id.etName).text.toString().trim()
        val typed = findViewById<EditText>(R.id.etPlace).text.toString().trim()
        val chosen = selectedPlace
        if (chosen != null) {
            show(name, chosen.label, chosen.latitude, chosen.longitude, TimeZone.getTimeZone(chosen.timeZone))
            return
        }
        if (typed.length < 2) {
            toast(Lang.t("Enter your birth place and pick it from the suggestions")); return
        }
        // Nothing picked from the list: fall back to the best match for what was typed.
        progress.visibility = View.VISIBLE
        thread {
            val best = try { PlaceSearch.search(typed, 1).firstOrNull() } catch (e: Exception) { null }
            runOnUiThread {
                progress.visibility = View.GONE
                if (best == null) {
                    toast(Lang.t("Could not find that place. Check the spelling and your internet connection."))
                } else {
                    selectedPlace = best
                    setInfo("Using: %s (%s, %s, %s)", best.label, "%.4f".format(best.latitude), "%.4f".format(best.longitude), best.timeZone)
                    show(name, best.label, best.latitude, best.longitude, TimeZone.getTimeZone(best.timeZone))
                }
            }
        }
    }

    private fun show(name: String, place: String, lat: Double, lon: Double, tz: TimeZone) {
        lastShown = { show(name, place, lat, lon, tz) }
        val cal = Calendar.getInstance(tz).apply {
            clear(); set(year, month, day, hour, minute, 0)
        }
        val birth = cal.timeInMillis
        val header = (if (name.isNotEmpty()) "$name\n" else "") +
            "%04d-%02d-%02d %02d:%02d (%s)\n%s  (%.3f, %.3f)".format(year, month + 1, day, hour, minute, tz.id, place, lat, lon)
        progress.visibility = View.VISIBLE
        container.removeAllViews()
        setExportVisible(false)

        thread {
            val now = System.currentTimeMillis()
            val result = try {
                val k = AstroEngine.compute(birth, lat, lon, tz)
                val (rise, set) = AstroEngine.sunriseSunset(birth, lat, lon, tz)
                val fmt = SimpleDateFormat("dd MMM yyyy", Lang.locale())
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
                    toast(Lang.t("Could not compute the chart")); return@runOnUiThread
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

                blocks.clear()
                reportName = name
                blocks += Block.Header(header)
                addTitle("Your Near Future at a Glance")
                addText(summary, 14f, false)
                addTitle("Next 12 Months – Month by Month")
                addText(monthly, 12f, true)
                addTitle("Life Areas Right Now (Jupiter & Saturn)")
                addText(areas, 13f, false)
                addTitle("Dasha Forecast (Vimshottari)")
                addText(dashaF, 13f, false)
                addTitle("Planetary Transits (Gochar) & Sade Sati")
                addText(transitText, 11f, true)
                addTitle("Upcoming Major Sign Changes (3 years)")
                addText(ingress, 13f, false)

                addTitle("Janma Kundli – Lagna Chart (D1)")
                blocks += Block.Chart(k.lagnaSign, k.planets.map { it.name to it.sign })
                addTitle("Chandra Kundli – Moon Chart")
                blocks += Block.Chart(moonSign, Vedic.moonPlacements(k))
                addTitle("Navamsa Chart (D9)")
                blocks += Block.Chart(Vedic.navamsaSign(k.ascendant), Vedic.navamsaPlacements(k))
                blocks += Block.Note(Lang.t(
                    if (KundliDraw.south) "Numbers are zodiac signs (1 = Aries; 12 = Pisces); signs are fixed, Aries is the second box in the top row; Lg = Lagna (marked with a corner slash)."
                    else "Numbers are zodiac signs (1 = Aries; 12 = Pisces); Lg = Lagna (house 1, top diamond)."))
                addTitle("Current Transit Chart (Lagna as reference)")
                blocks += Block.Chart(k.lagnaSign, transit)

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
                    addTitle(Lang.t(area), true)
                    addText(Readings.lifeAreaReading(area, k, shadbala, System.currentTimeMillis()), 11f, false)
                }

                addTitle("Dasha Period Readings")
                val now = System.currentTimeMillis()
                val maha = k.dashas.firstOrNull { now >= it.startMillis && now < it.endMillis }
                if (maha != null) {
                    addTitle(Lang.t("Current Mahadasha: %s", Lang.planet(maha.lord)), true)
                    addText(Readings.dashaReading(maha.lord, k, maha.startMillis, maha.endMillis), 11f, false)
                    val antars = AstroEngine.subPeriods(maha)
                    val antar = antars.firstOrNull { now >= it.startMillis && now < it.endMillis }
                    if (antar != null) {
                        addTitle(Lang.t("Current Antardasha: %s", Lang.planet(antar.lord)), true)
                        addText(Readings.dashaReading("${maha.lord}–${antar.lord}", k, antar.startMillis, antar.endMillis), 11f, false)
                    }
                }

                blocks += Block.Note(Insights.disclaimer())
                ReportRenderer.render(this, container, blocks)
                setExportVisible(true)
            }
        }
    }
    private fun addTitle(t: String, localized: Boolean = false) {
        blocks += Block.Title(if (localized) t else Lang.t(t), sub = localized)
    }

    private fun addText(t: String, size: Float, mono: Boolean) {
        if (t.isBlank()) return
        blocks += if (mono) Block.Table(t.trimEnd()) else Block.Para(t.trim())
    }

    private fun setExportVisible(v: Boolean) {
        findViewById<View>(R.id.exportRow).visibility = if (v) View.VISIBLE else View.GONE
    }

    private fun pdfFileName(): String {
        val base = reportName.replace(Regex("[^\\p{L}\\p{N}_-]+"), "_").trim('_')
        return if (base.isEmpty()) "Kundli_%04d-%02d-%02d.pdf".format(year, month + 1, day)
        else "Kundli_%s_%04d-%02d-%02d.pdf".format(base, year, month + 1, day)
    }

    private fun savePdfTo(uri: android.net.Uri) {
        val snapshot = blocks.toList()
        thread {
            val ok = try {
                contentResolver.openOutputStream(uri)?.use { PdfExporter.write(snapshot, it) } != null
            } catch (e: Exception) { false }
            runOnUiThread { toast(Lang.t(if (ok) "PDF saved" else "Could not create the PDF")) }
        }
    }

    private fun sharePdf() {
        val snapshot = blocks.toList()
        val name = pdfFileName()
        progress.visibility = View.VISIBLE
        thread {
            val uri = try {
                val dir = java.io.File(cacheDir, "reports").apply { mkdirs() }
                dir.listFiles()?.forEach { it.delete() }
                val f = java.io.File(dir, name)
                f.outputStream().use { PdfExporter.write(snapshot, it) }
                androidx.core.content.FileProvider.getUriForFile(this, "$packageName.fileprovider", f)
            } catch (e: Exception) { null }
            runOnUiThread {
                progress.visibility = View.GONE
                if (uri == null) { toast(Lang.t("Could not create the PDF")); return@runOnUiThread }
                val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(android.content.Intent.EXTRA_STREAM, uri)
                    putExtra(android.content.Intent.EXTRA_SUBJECT, Lang.t("Kundli Astrology"))
                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(android.content.Intent.createChooser(send, Lang.t("Share Kundli report")))
            }
        }
    }

    private val createPdf = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri -> if (uri != null) savePdfTo(uri) }

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_LONG).show()
}
