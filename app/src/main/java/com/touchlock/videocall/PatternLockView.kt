package com.touchlock.videocall

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class PatternLockView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface OnPatternListener {
        fun onPatternCompleted(pattern: List<Int>)
    }

    private var listener: OnPatternListener? = null
    private val selectedDots = mutableListOf<Int>()
    private val dotCenters = mutableListOf<PointF>()
    private val paintDot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.DKGRAY }
    private val paintSelected = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#2563eb") }
    private val paintLine = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2563eb")
        strokeWidth = 12f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private var currentX = 0f
    private var currentY = 0f
    private var isDrawing = false

    fun setOnPatternListener(listener: OnPatternListener) {
        this.listener = listener
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        dotCenters.clear()
        val stepX = w / 4f
        val stepY = h / 4f
        for (row in 1..3) {
            for (col in 1..3) {
                dotCenters.add(PointF(col * stepX, row * stepY))
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw connecting lines
        if (selectedDots.size > 1) {
            val path = Path()
            val start = dotCenters[selectedDots[0]]
            path.moveTo(start.x, start.y)
            for (i in 1 until selectedDots.size) {
                val next = dotCenters[selectedDots[i]]
                path.lineTo(next.x, next.y)
            }
            if (isDrawing) {
                path.lineTo(currentX, currentY)
            }
            canvas.drawPath(path, paintLine)
        } else if (isDrawing && selectedDots.size == 1) {
            val start = dotCenters[selectedDots[0]]
            canvas.drawLine(start.x, start.y, currentX, currentY, paintLine)
        }

        // Draw 9 dots
        for (i in dotCenters.indices) {
            val pt = dotCenters[i]
            val isSelected = selectedDots.contains(i)
            canvas.drawCircle(pt.x, pt.y, if (isSelected) 24f else 18f, if (isSelected) paintSelected else paintDot)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        currentX = event.x
        currentY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                selectedDots.clear()
                isDrawing = true
                checkDotHit(currentX, currentY)
                invalidate()
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                checkDotHit(currentX, currentY)
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isDrawing = false
                invalidate()
                if (selectedDots.isNotEmpty()) {
                    listener?.onPatternCompleted(selectedDots.toList())
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun checkDotHit(x: Float, y: Float) {
        for (i in dotCenters.indices) {
            val pt = dotCenters[i]
            val dist = Math.hypot((x - pt.x).toDouble(), (y - pt.y).toDouble())
            if (dist < 60 && !selectedDots.contains(i)) {
                selectedDots.add(i)
            }
        }
    }

    fun showError() {
        paintSelected.color = Color.RED
        paintLine.color = Color.RED
        invalidate()
        postDelayed({
            paintSelected.color = Color.parseColor("#2563eb")
            paintLine.color = Color.parseColor("#2563eb")
            selectedDots.clear()
            invalidate()
        }, 1000)
    }
}
