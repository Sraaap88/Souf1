package com.example.souffleforcetest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class OrganicLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private val basePaint = Paint().apply {
        color = 0xFFFFFFFF.toInt()
        strokeWidth = 4f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }
    
    private val resetButtonPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val resetTextPaint = Paint().apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 120f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    
    private var currentForce = 0.0f
    private var previousForce = 0.0f
    private var currentHeight = 0f
    private var currentStrokeWidth = 4f
    private var offsetX = 0f
    private var maxHeight = 0f
    private var baseX = 0f
    private var baseY = 0f
    private var showResetButton = false
    private var resetButtonX = 0f
    private var resetButtonY = 0f
    private val resetButtonRadius = 175f
    
    private data class TracePoint(
        val x: Float,
        val y: Float,
        val strokeWidth: Float,
        val waveFrequency: Float,
        val waveAmplitude: Float,
        val curvature: Float
    )
    
    private val tracedPath = mutableListOf<TracePoint>()
    
    private val forceThreshold = 0.08f
    private val growthRate = 116.4f // 2x plus rapide
    private val baseStrokeWidth = 4f
    private val maxStrokeWidth = 96f // Différence d'épaisseur plus visible
    private val strokeDecayRate = 0.2f
    private val abruptThreshold = 0.15f
    private val centeringRate = 0.92f
    private val waveThreshold = 0.03f
    private val maxWaveAmplitude = 15f
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        baseX = w / 2.0f
        baseY = h - 50f
        maxHeight = h - 100f
        
        resetButtonX = w - resetButtonRadius - 50f
        resetButtonY = resetButtonRadius + 80f
        
        if (tracedPath.isEmpty()) {
            tracedPath.add(TracePoint(baseX + offsetX, baseY, baseStrokeWidth, 0f, 0f, 0f))
        }
    }
    
    fun updateForce(force: Float) {
        previousForce = currentForce
        currentForce = force
        
        if (force > forceThreshold) {
            val adjustedForce = force - forceThreshold
            currentHeight += adjustedForce * growthRate
            currentHeight = kotlin.math.min(currentHeight, maxHeight)
        }
        
        val rhythmIntensity = kotlin.math.abs(currentForce - previousForce)
        
        if (rhythmIntensity > abruptThreshold) {
            val displacement = if ((0..1).random() == 0) 80f else -80f // Changements plus gros
            offsetX += displacement
        } else if (rhythmIntensity > 0.02f) {
            val thicknessIncrease = rhythmIntensity * 160f // Plus de variation d'épaisseur
            currentStrokeWidth = kotlin.math.min(maxStrokeWidth, baseStrokeWidth + thicknessIncrease)
        }
        
        if (currentStrokeWidth > baseStrokeWidth) {
            currentStrokeWidth = kotlin.math.max(baseStrokeWidth, currentStrokeWidth - strokeDecayRate)
        }
        offsetX *= centeringRate
        
        var curvature = 0f
        if (rhythmIntensity > 0.05f) {
            curvature = (rhythmIntensity * 60f).coerceAtMost(30f)
            if ((0..1).random() == 0) curvature = -curvature
        }
        
        var waveFreq = 0f
        var waveAmp = 0f
        
        if (rhythmIntensity > 0.01f && rhythmIntensity < abruptThreshold) {
            waveFreq = (rhythmIntensity * 15f) + 1f
            waveAmp = (rhythmIntensity * 200f).coerceAtMost(maxWaveAmplitude)
        }
        
        if (waveAmp == 0f && currentHeight > 0f) {
            waveFreq = 2f
            waveAmp = 3f
        }
        
        val currentY = baseY - currentHeight
        val currentX = baseX + offsetX
        
        if (tracedPath.isNotEmpty()) {
            val lastPoint = tracedPath.last()
            if (currentY < lastPoint.y) {
                tracedPath.add(TracePoint(currentX, currentY, currentStrokeWidth, waveFreq, waveAmp, curvature))
            }
        }
        
        if (!showResetButton && currentHeight > 50f) {
            showResetButton = true
        }
        
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val time = System.currentTimeMillis() * 0.002f
        
        for (i in 1 until tracedPath.size) {
            val prevPoint = tracedPath[i - 1]
            val currentPoint = tracedPath[i]
            
            // Oscillation TRÈS VISIBLE
            val oscillation = kotlin.math.sin(time + currentPoint.y * 0.005f) * 35f // Beaucoup plus d'amplitude
            
            val midX = (prevPoint.x + currentPoint.x) / 2f + oscillation
            val midY = (prevPoint.y + currentPoint.y) / 2f
            
            val dx = currentPoint.x - prevPoint.x
            val dy = currentPoint.y - prevPoint.y
            val length = kotlin.math.sqrt(dx * dx + dy * dy)
            
            val controlX = if (length > 0) {
                midX + (-dy / length) * currentPoint.curvature
            } else midX
            
            val controlY = if (length > 0) {
                midY + (dx / length) * currentPoint.curvature
            } else midY
            
            val segmentPath = Path()
            segmentPath.moveTo(prevPoint.x + oscillation * 0.8f, prevPoint.y) // Plus d'oscillation
            segmentPath.quadTo(controlX, controlY, currentPoint.x + oscillation, currentPoint.y)
            
            basePaint.strokeWidth = currentPoint.strokeWidth
            canvas.drawPath(segmentPath, basePaint)
        }
        
        val currentY = baseY - currentHeight
        val pointOscillation = kotlin.math.sin(time * 2f) * 15f // Point oscille beaucoup plus
        val currentX = baseX + offsetX + pointOscillation
        basePaint.style = Paint.Style.FILL
        canvas.drawCircle(currentX, currentY, 8f, basePaint)
        basePaint.style = Paint.Style.STROKE
        
        if (showResetButton) {
            resetButtonPaint.color = 0x40000000.toInt()
            canvas.drawCircle(resetButtonX + 8f, resetButtonY + 8f, resetButtonRadius, resetButtonPaint)
            
            resetButtonPaint.color = 0xFFE53E3E.toInt()
            canvas.drawCircle(resetButtonX, resetButtonY, resetButtonRadius, resetButtonPaint)
            
            resetButtonPaint.color = 0xFFC53030.toInt()
            resetButtonPaint.style = Paint.Style.STROKE
            resetButtonPaint.strokeWidth = 8f
            canvas.drawCircle(resetButtonX, resetButtonY, resetButtonRadius, resetButtonPaint)
            resetButtonPaint.style = Paint.Style.FILL
            
            canvas.drawText("↻", resetButtonX, resetButtonY + 40f, resetTextPaint)
        }
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && showResetButton) {
            val dx = event.x - resetButtonX
            val dy = event.y - resetButtonY
            val distance = kotlin.math.sqrt(dx * dx + dy * dy)
            
            if (distance <= resetButtonRadius) {
                resetPlant()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
    
    private fun resetPlant() {
        tracedPath.clear()
        currentHeight = 0f
        currentStrokeWidth = baseStrokeWidth
        offsetX = 0f
        showResetButton = false
        
        tracedPath.add(TracePoint(baseX, baseY, baseStrokeWidth, 0f, 0f, 0f))
        invalidate()
    }
}
