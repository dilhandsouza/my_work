package elendor.vedicastrology

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class Place(
    val label: String,
    val latitude: Double,
    val longitude: Double,
    val timeZone: String
) {
    override fun toString() = label
}

/** City search backed by the Open-Meteo geocoding API (no key needed). */
object PlaceSearch {

    fun search(query: String, limit: Int = 8): List<Place> {
        val parts = query.split(',').map { it.trim() }.filter { it.isNotEmpty() }
        if (parts.isEmpty() || parts[0].length < 2) return emptyList()
        // The API matches on the place name only, so extra words ("Maharashtra", "India") are used as filters.
        val name = URLEncoder.encode(parts[0], "UTF-8")
        val url = URL("https://geocoding-api.open-meteo.com/v1/search?name=$name&count=20&language=${Lang.current.code}&format=json")
        val conn = url.openConnection() as HttpURLConnection
        conn.connectTimeout = 8000
        conn.readTimeout = 8000
        try {
            if (conn.responseCode != 200) return emptyList()
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val arr = JSONObject(body).optJSONArray("results") ?: return emptyList()
            val all = ArrayList<Place>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                if (!o.has("timezone")) continue
                val label = listOf(
                    o.optString("name"), o.optString("admin2"), o.optString("admin1"), o.optString("country")
                ).filter { it.isNotEmpty() }.distinct().joinToString(", ")
                all.add(Place(label, o.getDouble("latitude"), o.getDouble("longitude"), o.getString("timezone")))
            }
            val filters = parts.drop(1).map { it.lowercase() }
            val filtered = if (filters.isEmpty()) all
            else all.filter { p -> filters.all { f -> p.label.lowercase().contains(f) } }
            return (if (filtered.isEmpty()) all else filtered).take(limit)
        } finally {
            conn.disconnect()
        }
    }
}
