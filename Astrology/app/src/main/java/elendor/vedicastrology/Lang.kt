package elendor.vedicastrology

import java.util.Collections

enum class AppLang(val code: String, val label: String) {
    EN("en", "English"),
    KN("kn", "ಕನ್ನಡ")
}

/**
 * Report language. English strings are the lookup keys: [t] returns the Kannada template for the
 * exact English template when the current language is Kannada (format args are applied afterwards).
 * Internal identifiers (planet names such as "Sun", sign names) stay English; translate only at display time.
 */
object Lang {
    @Volatile
    var current: AppLang = AppLang.EN

    val isKn: Boolean get() = current == AppLang.KN

    /** English templates that had no Kannada entry; used by tests to catch gaps. */
    val missing: MutableSet<String> = Collections.synchronizedSet(LinkedHashSet())

    private val kn: Map<String, String> by lazy { KN_VEDIC + KN_FORECAST + KN_SHADBALA + KN_UI }

    /** Translate an English template and apply [args] with String.format. Kannada templates may use %1\$s positions. */
    fun t(en: String, vararg args: Any?): String {
        val tpl = if (isKn) {
            kn[en] ?: run { if (en.any { it in 'A'..'Z' || it in 'a'..'z' }) missing.add(en); en }
        } else en
        return if (args.isEmpty()) tpl else String.format(tpl, *args)
    }

    /** Left-align [s] in a column of [width]; Kannada text is proportional so only a single space is used. */
    fun pad(s: String, width: Int): String = if (isKn) "$s " else s.padEnd(width)

    private val PLANETS_KN = mapOf(
        "Sun" to "ಸೂರ್ಯ", "Moon" to "ಚಂದ್ರ", "Mars" to "ಮಂಗಳ", "Mercury" to "ಬುಧ", "Jupiter" to "ಗುರು",
        "Venus" to "ಶುಕ್ರ", "Saturn" to "ಶನಿ", "Rahu" to "ರಾಹು", "Ketu" to "ಕೇತು"
    )
    private val ABBR_KN = mapOf(
        "Sun" to "ಸೂ", "Moon" to "ಚಂ", "Mars" to "ಮಂ", "Mercury" to "ಬು", "Jupiter" to "ಗು",
        "Venus" to "ಶು", "Saturn" to "ಶ", "Rahu" to "ರಾ", "Ketu" to "ಕೇ"
    )
    private val SIGNS_KN = listOf(
        "ಮೇಷ", "ವೃಷಭ", "ಮಿಥುನ", "ಕರ್ಕಾಟಕ", "ಸಿಂಹ", "ಕನ್ಯಾ",
        "ತುಲಾ", "ವೃಶ್ಚಿಕ", "ಧನು", "ಮಕರ", "ಕುಂಭ", "ಮೀನ"
    )
    private val NAKSHATRAS_KN = listOf(
        "ಅಶ್ವಿನಿ", "ಭರಣಿ", "ಕೃತ್ತಿಕಾ", "ರೋಹಿಣಿ", "ಮೃಗಶಿರ", "ಆರ್ದ್ರಾ", "ಪುನರ್ವಸು", "ಪುಷ್ಯ", "ಆಶ್ಲೇಷ",
        "ಮಘ", "ಪೂರ್ವ ಫಲ್ಗುಣಿ", "ಉತ್ತರ ಫಲ್ಗುಣಿ", "ಹಸ್ತ", "ಚಿತ್ರಾ", "ಸ್ವಾತಿ", "ವಿಶಾಖ", "ಅನುರಾಧ", "ಜ್ಯೇಷ್ಠ",
        "ಮೂಲ", "ಪೂರ್ವಾಷಾಢ", "ಉತ್ತರಾಷಾಢ", "ಶ್ರವಣ", "ಧನಿಷ್ಠ", "ಶತಭಿಷ", "ಪೂರ್ವಾಭಾದ್ರಪದ",
        "ಉತ್ತರಾಭಾದ್ರಪದ", "ರೇವತಿ"
    )

    /** Planet name from its English identifier ("Sun", "Rahu", ...). Unknown names pass through. */
    fun planet(en: String): String = if (isKn) PLANETS_KN[en] ?: en else en

    /** Short chart label for a planet (Su, Mo ... in English). */
    fun abbr(en: String): String = if (isKn) ABBR_KN[en] ?: en else PLANET_ABBR.getValue(en)

    val lagnaAbbr: String get() = if (isKn) "ಲ" else "Lg"

    /** Zodiac sign name from index 0..11. */
    fun sign(index: Int): String = if (isKn) SIGNS_KN[index] else SIGNS[index]

    /** Zodiac sign name from its English name. */
    fun signName(en: String): String {
        val i = SIGNS.indexOf(en)
        return if (i < 0) en else sign(i)
    }

    /** Nakshatra name from index 0..26. */
    fun nak(index: Int): String = if (isKn) NAKSHATRAS_KN[index] else NAKSHATRAS[index]

    /** Month abbreviation index 0..11 for the "MMM yyyy" style dates. */
    fun locale(): java.util.Locale = if (isKn) java.util.Locale("kn", "IN") else java.util.Locale.getDefault()
}
