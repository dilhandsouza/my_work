package elendor.vedicastrology

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View

/** North Indian style kundli: houses are fixed, signs rotate with the ascendant. */
object KundliDraw {
    /** Selected chart style; North Indian by default. */
    var south = false

    private val southCells = listOf(
        11 to (0 to 0), 0 to (1 to 0), 1 to (2 to 0), 2 to (3 to 0),
        3 to (3 to 1), 4 to (3 to 2), 5 to (3 to 3), 6 to (2 to 3),
        7 to (1 to 3), 8 to (0 to 3), 9 to (0 to 2), 10 to (0 to 1)
    )

    /** South Indian style: signs are fixed in the grid, Pisces at top-left going clockwise from Aries. */
    private fun drawSouth(c: Canvas, size: Float, lagnaSign: Int, placements: List<Pair<String, Int>>, background: String) {
        val s = size - 8f
        val cell = s / 4f
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#5B2A86"); style = Paint.Style.STROKE; strokeWidth = (s * 0.005f).coerceAtLeast(1.2f)
        }
        val signPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#7A4A00"); textSize = s * 0.04f; isFakeBoldText = true
        }
        val planetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#111111"); textAlign = Paint.Align.CENTER; isFakeBoldText = true; textSize = s * 0.042f
        }
        c.save()
        c.translate(4f, 4f)
        c.drawRect(0f, 0f, s, s, Paint().apply { color = Color.parseColor(background) })
        for (i in 0..4) {
            val p = i * cell
            if (i == 0 || i == 4) {
                c.drawLine(p, 0f, p, s, line); c.drawLine(0f, p, s, p, line)
            } else {
                c.drawLine(p, 0f, p, cell, line); c.drawLine(p, s - cell, p, s, line)
                c.drawLine(0f, p, cell, p, line); c.drawLine(s - cell, p, s, p, line)
            }
        }
        c.drawLine(cell, cell, cell, 3 * cell, line); c.drawLine(3 * cell, cell, 3 * cell, 3 * cell, line)
        c.drawLine(cell, cell, 3 * cell, cell, line); c.drawLine(cell, 3 * cell, 3 * cell, 3 * cell, line)
        for ((sign, pos) in southCells) {
            val x0 = pos.first * cell
            val y0 = pos.second * cell
            if (sign == lagnaSign) c.drawLine(x0, y0, x0 + cell * 0.16f, y0 + cell * 0.16f, line)
            val numX = x0 + if (sign == lagnaSign) cell * 0.2f else cell * 0.06f
            c.drawText((sign + 1).toString(), numX, y0 + signPaint.textSize * 1.05f, signPaint)
            val lines = mutableListOf<String>()
            if (sign == lagnaSign) lines += Lang.lagnaAbbr
            placements.filter { it.second == sign }.map { Lang.abbr(it.first) }.chunked(2).forEach { lines += it.joinToString(" ") }
            val lh = planetPaint.textSize * 1.15f
            val top = y0 + cell * 0.3f
            lines.forEachIndexed { i, t -> c.drawText(t, x0 + cell / 2, top + lh * i + lh * 0.6f, planetPaint) }
        }
        c.restore()
    }

    fun draw(c: Canvas, size: Float, lagnaSign: Int, placements: List<Pair<String, Int>>, background: String) {
        if (south) return drawSouth(c, size, lagnaSign, placements, background)
        val s = size - 8f
        val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#5B2A86"); style = Paint.Style.STROKE; strokeWidth = (s * 0.005f).coerceAtLeast(1.2f)
        }
        val signPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#7A4A00"); textAlign = Paint.Align.CENTER; textSize = s * 0.045f; isFakeBoldText = true
        }
        val planetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#111111"); textAlign = Paint.Align.CENTER; isFakeBoldText = true; textSize = s * 0.052f
        }
        val bg = Paint().apply { color = Color.parseColor(background) }

        c.save()
        c.translate(4f, 4f)
        c.drawRect(0f, 0f, s, s, bg)
        c.drawRect(0f, 0f, s, s, line)
        c.drawLine(0f, 0f, s, s, line)
        c.drawLine(s, 0f, 0f, s, line)
        val d = Path().apply {
            moveTo(s / 2, 0f); lineTo(s, s / 2); lineTo(s / 2, s); lineTo(0f, s / 2); close()
        }
        c.drawPath(d, line)

        val centers = listOf(
            0.5f to 0.25f, 0.25f to 0.08f, 0.08f to 0.25f, 0.25f to 0.5f,
            0.08f to 0.75f, 0.25f to 0.92f, 0.5f to 0.75f, 0.75f to 0.92f,
            0.92f to 0.75f, 0.75f to 0.5f, 0.92f to 0.25f, 0.75f to 0.08f
        )

        for (h in 1..12) {
            val (cx, cy) = centers[h - 1]
            val x = cx * s
            val y = cy * s
            val signIdx = (lagnaSign + h - 1) % 12
            val inHouse = placements.filter { it.second == signIdx }.map { Lang.abbr(it.first) }
            val lines = mutableListOf<String>()
            if (h == 1) lines += Lang.lagnaAbbr
            inHouse.chunked(3).forEach { lines += it.joinToString(" ") }
            val lh = planetPaint.textSize * 1.2f
            val top = y - lh * lines.size / 2f
            c.drawText((signIdx + 1).toString(), x, top - lh * 0.1f, signPaint)
            lines.forEachIndexed { i, t -> c.drawText(t, x, top + lh * (i + 1.1f), planetPaint) }
        }
        c.restore()
    }
}

class KundliView(
    context: Context,
    private val lagnaSign: Int,
    private val placements: List<Pair<String, Int>>
) : View(context) {

    override fun onMeasure(w: Int, h: Int) {
        val size = MeasureSpec.getSize(w)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(c: Canvas) = KundliDraw.draw(c, width.toFloat(), lagnaSign, placements, "#FFF8E7")
}
