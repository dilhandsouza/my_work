package com.example.astrology

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View

/** North Indian style kundli: houses are fixed, signs rotate with the ascendant. */
class KundliView(
    context: Context,
    private val lagnaSign: Int,
    private val placements: List<Pair<String, Int>>
) : View(context) {

    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#5B2A86"); style = Paint.Style.STROKE; strokeWidth = 4f
    }
    private val signPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#B07A00"); textAlign = Paint.Align.CENTER
    }
    private val planetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#222222"); textAlign = Paint.Align.CENTER; isFakeBoldText = true
    }
    private val bg = Paint().apply { color = Color.parseColor("#FFF8E7") }

    override fun onMeasure(w: Int, h: Int) {
        val size = MeasureSpec.getSize(w)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(c: Canvas) {
        val s = width.toFloat() - 8f
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
        signPaint.textSize = s * 0.04f
        planetPaint.textSize = s * 0.04f

        for (h in 1..12) {
            val (cx, cy) = centers[h - 1]
            val x = cx * s
            val y = cy * s
            val signIdx = (lagnaSign + h - 1) % 12
            val sign = signIdx + 1
            val inHouse = placements.filter { it.second == signIdx }
                .map { PLANET_ABBR.getValue(it.first) }
            val lines = mutableListOf<String>()
            if (h == 1) lines += "Lg"
            inHouse.chunked(3).forEach { lines += it.joinToString(" ") }
            val lh = planetPaint.textSize * 1.2f
            val top = y - lh * lines.size / 2f
            c.drawText(sign.toString(), x, top - lh * 0.1f, signPaint)
            lines.forEachIndexed { i, t -> c.drawText(t, x, top + lh * (i + 1.1f), planetPaint) }
        }
        c.restore()
    }
}
