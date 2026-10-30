package elendor.vedicastrology

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView

/** The report as a list of layout-independent blocks; the screen and the PDF both render from it. */
sealed class Block {
    class Header(val text: String) : Block()
    class Title(val text: String, val sub: Boolean = false) : Block()
    class Para(val text: String) : Block()
    /** Column-aligned text; it is shown without wrapping in a monospace face. */
    class Table(val text: String) : Block()
    class Note(val text: String) : Block()
    class Chart(val lagnaSign: Int, val placements: List<Pair<String, Int>>) : Block()
}

object Palette {
    const val PAGE = "#FFFDF8"
    const val INK = "#1B1B1B"
    const val PURPLE = "#5B2A86"
    const val GOLD = "#E0A526"
    const val SUB = "#7A4A00"
    const val TABLE_BG = "#F6F0E4"
    const val HEADER_BG = "#EDE4F6"
    const val MUTED = "#444444"
}

object ReportRenderer {
    fun render(ctx: Context, container: LinearLayout, blocks: List<Block>) {
        container.removeAllViews()
        container.setBackgroundColor(Color.parseColor(Palette.PAGE))
        val d = ctx.resources.displayMetrics.density
        fun dp(v: Int) = (v * d).toInt()
        container.setPadding(dp(16), dp(8), dp(16), dp(24))

        fun text(t: String, sp: Float, color: String): TextView = TextView(ctx).apply {
            text = t
            textSize = sp
            setTextColor(Color.parseColor(color))
            setLineSpacing(0f, if (Lang.isKn) 1.1f else 1.25f)
            setTextIsSelectable(true)
        }

        for (b in blocks) {
            when (b) {
                is Block.Header -> container.addView(text(b.text, 18f, Palette.INK).apply {
                    setTypeface(null, Typeface.BOLD)
                    setBackgroundColor(Color.parseColor(Palette.HEADER_BG))
                    setPadding(dp(14), dp(12), dp(14), dp(12))
                })
                is Block.Title -> {
                    if (b.sub) {
                        container.addView(text(b.text, 19f, Palette.SUB).apply {
                            setTypeface(null, Typeface.BOLD)
                            setPadding(0, dp(16), 0, dp(2))
                        })
                    } else {
                        container.addView(text(b.text, 23f, Palette.PURPLE).apply {
                            setTypeface(null, Typeface.BOLD)
                            setPadding(0, dp(28), 0, dp(4))
                        })
                        container.addView(View(ctx).apply {
                            setBackgroundColor(Color.parseColor(Palette.GOLD))
                            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(2))
                                .apply { bottomMargin = dp(8) }
                        })
                    }
                }
                is Block.Para -> container.addView(text(b.text, 17f, Palette.INK).apply {
                    setPadding(0, dp(4), 0, dp(6))
                })
                is Block.Table -> {
                    // Kannada glyphs are not fixed width, so the columns only line up in English.
                    val tv = text(b.text, 14f, Palette.INK).apply {
                        if (!Lang.isKn) typeface = Typeface.MONOSPACE
                        setLineSpacing(0f, 1.1f)
                        setPadding(dp(10), dp(8), dp(10), dp(8))
                    }
                    if (Lang.isKn) {
                        tv.setBackgroundColor(Color.parseColor(Palette.TABLE_BG))
                        container.addView(tv)
                    } else {
                        container.addView(HorizontalScrollView(ctx).apply {
                            setBackgroundColor(Color.parseColor(Palette.TABLE_BG))
                            isHorizontalScrollBarEnabled = true
                            addView(tv)
                        })
                    }
                    container.addView(View(ctx), ViewGroup.LayoutParams(1, dp(6)))
                }
                is Block.Note -> container.addView(text(b.text, 14f, Palette.MUTED).apply {
                    setTypeface(null, Typeface.ITALIC)
                    setPadding(0, dp(6), 0, dp(6))
                })
                is Block.Chart -> container.addView(
                    KundliView(ctx, b.lagnaSign, b.placements),
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        .apply { gravity = Gravity.CENTER; topMargin = dp(6); bottomMargin = dp(6) }
                )
            }
        }
    }
}
