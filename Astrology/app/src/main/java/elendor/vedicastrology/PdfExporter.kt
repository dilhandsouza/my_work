package elendor.vedicastrology

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import java.io.OutputStream

/** Writes the report blocks to an A4 PDF: dark text on white, charts drawn as vectors, text wrapped across pages. */
object PdfExporter {
    private const val W = 595
    private const val H = 842
    private const val M = 42f

    fun write(blocks: List<Block>, out: OutputStream) {
        val doc = PdfDocument()
        val ctx = Ctx(doc)
        try {
            ctx.newPage()
            for ((i, b) in blocks.withIndex()) {
                if (b is Block.Title && blocks.getOrNull(i + 1) is Block.Chart) ctx.need(280f + 12f + 40f)
                ctx.draw(b)
            }
            ctx.finishPage()
            doc.writeTo(out)
        } finally {
            doc.close()
        }
    }

    private class Ctx(val doc: PdfDocument) {
        var page: PdfDocument.Page? = null
        var canvas: Canvas? = null
        var y = M
        var pageNo = 0
        val contentW = W - 2 * M
        val bottom = H - M - 14f

        fun newPage() {
            finishPage()
            pageNo++
            val p = doc.startPage(PdfDocument.PageInfo.Builder(W, H, pageNo).create())
            page = p
            canvas = p.canvas
            y = M
        }

        fun finishPage() {
            val p = page ?: return
            val c = canvas!!
            val foot = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#777777"); textSize = 8f; textAlign = Paint.Align.CENTER
            }
            c.drawText("${Lang.t("Kundli Astrology")}  ·  ${Lang.t("Page %d", pageNo)}", W / 2f, H - 24f, foot)
            doc.finishPage(p)
            page = null
        }

        fun paint(size: Float, color: String, bold: Boolean = false, italic: Boolean = false, mono: Boolean = false) =
            TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = Color.parseColor(color)
                textSize = size
                typeface = Typeface.create(
                    if (mono) Typeface.MONOSPACE else Typeface.SANS_SERIF,
                    when { bold && italic -> Typeface.BOLD_ITALIC; bold -> Typeface.BOLD; italic -> Typeface.ITALIC; else -> Typeface.NORMAL }
                )
            }

        @Suppress("DEPRECATION")
        fun layout(text: String, p: TextPaint, width: Float, spacing: Float = 1.2f) =
            StaticLayout(text, p, width.toInt(), Layout.Alignment.ALIGN_NORMAL, spacing, 0f, false)

        fun need(h: Float) {
            if (y + h > bottom) newPage()
        }

        /** Draws wrapped text line by line so long passages continue onto the next page. */
        fun text(t: String, p: TextPaint, x: Float = M, width: Float = contentW, spacing: Float = 1.2f, bg: String? = null) {
            val l = layout(t, p, width, spacing)
            for (i in 0 until l.lineCount) {
                val top = l.getLineTop(i)
                val h = (l.getLineBottom(i) - top).toFloat()
                need(h)
                val c = canvas!!
                if (bg != null) c.drawRect(x - 4f, y, x + width + 4f, y + h, Paint().apply { color = Color.parseColor(bg) })
                var end = l.getLineEnd(i)
                while (end > l.getLineStart(i) && t[end - 1] == '\n') end--
                c.drawText(t, l.getLineStart(i), end, x, y + (l.getLineBaseline(i) - top), p)
                y += h
            }
        }

        fun draw(b: Block) {
            when (b) {
                is Block.Header -> {
                    need(70f)
                    text(b.text, paint(13f, Palette.INK, bold = true), M + 6f, contentW - 12f, 1.25f, Palette.HEADER_BG)
                    y += 8f
                }
                is Block.Title -> {
                    if (b.sub) {
                        need(50f)
                        y += 8f
                        text(b.text, paint(13f, Palette.SUB, bold = true))
                    } else {
                        need(80f)
                        y += 14f
                        text(b.text, paint(16f, Palette.PURPLE, bold = true))
                        canvas!!.drawRect(M, y + 1f, M + contentW, y + 2.5f, Paint().apply { color = Color.parseColor(Palette.GOLD) })
                        y += 7f
                    }
                }
                is Block.Para -> {
                    text(b.text, paint(10.5f, Palette.INK))
                    y += 5f
                }
                is Block.Table -> table(b.text)
                is Block.Note -> {
                    text(b.text, paint(9f, Palette.MUTED, italic = true))
                    y += 4f
                }
                is Block.Chart -> {
                    val size = 280f
                    need(size + 12f)
                    val c = canvas!!
                    c.save()
                    c.translate((W - size) / 2f, y)
                    KundliDraw.draw(c, size, b.lagnaSign, b.placements, "#FFF8E7")
                    c.restore()
                    y += size + 12f
                }
            }
        }

        fun table(t: String) {
            val mono = !Lang.isKn
            val p = paint(9f, Palette.INK, mono = mono)
            if (mono) {
                // Shrink the face so the widest row fits the page instead of wrapping and breaking the columns.
                val widest = t.lines().maxOfOrNull { it.length } ?: 1
                val unit = paint(10f, Palette.INK, mono = true).measureText("0")
                p.textSize = (10f * contentW / (widest.coerceAtLeast(1) * unit)).coerceIn(5.5f, 9.5f)
            }
            text(t, p, M + 4f, contentW - 8f, 1.15f, Palette.TABLE_BG)
            y += 6f
        }
    }
}
