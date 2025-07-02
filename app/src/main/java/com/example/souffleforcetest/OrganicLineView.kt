package com.example.souffleforcetest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.collections.mutableListOf

class OrganicLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private val basePaint = Paint().apply {
        color = 0xFFFFFFFF.toInt() // Blanc
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }
    
    private var currentForce = 0.0f
    private var previousForce = 0.0f
    private var currentHeight = 0f // Position actuelle du point
    private var currentStrokeWidth = 1f // Épaisseur actuelle
    private var maxHeight = 0f
    private var baseX = 0f
    private var baseY = 0f
    
    // Stockage du tracé
    private data class TracePoint(
        val x: Float,
        val y: Float,
        val strokeWidth: Float
    )
    
    private val tracedPath = mutableListOf<TracePoint>()
    
    // Configuration
    private val forceThreshold = 0.08f
    private val growthRate = 14.9f // Pixels par frame (20% plus rapide)
    private val baseStrokeWidth = 1f
    private val maxStrokeWidth = 12f
    private val strokeDecayRate = 0.2f
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        baseX = w / 2.0f
        baseY = h - 50f
        maxHeight = h - 100f
        
        // Point de départ
        if (tracedPath.isEmpty()) {
            tracedPath.add(TracePoint(baseX, baseY, baseStrokeWidth))
        }
    }
    
    fun updateForce(force: Float) {
        previousForce = currentForce
        currentForce = force
        
        // Croissance de la position du point
        if (force > forceThreshold) {
            val adjustedForce = force - forceThreshold
            currentHeight += adjustedForce * growthRate
            currentHeight = kotlin.math.min(currentHeight, maxHeight)
        }
        
        // Calcul de l'épaisseur selon le rythme
        val rhythmIntensity = kotlin.math.abs(currentForce - previousForce)
        
        if (rhythmIntensity > 0.02f) {
            val thicknessIncrease = rhythmIntensity * 40f
            currentStrokeWidth = kotlin.math.min(maxStrokeWidth, baseStrokeWidth + thicknessIncrease)
        }
        
        // Retour graduel à l'épaisseur de base
        if (currentStrokeWidth > baseStrokeWidth) {
            currentStrokeWidth = kotlin.math.max(baseStrokeWidth, currentStrokeWidth - strokeDecayRate)
        }
        
        // Ajouter le nouveau point au tracé
        val currentY = baseY - currentHeight
        if (tracedPath.isNotEmpty()) {
            val lastPoint = tracedPath.last()
            if (currentY < lastPoint.y) { // Seulement si on monte
                tracedPath.add(TracePoint(baseX, currentY, currentStrokeWidth))
            }
        }
        
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Dessiner le tracé avec les épaisseurs variables
        for (i in 1 until tracedPath.size) {
            val prevPoint = tracedPath[i - 1]
            val currentPoint = tracedPath[i]
            
            basePaint.strokeWidth = currentPoint.strokeWidth
            canvas.drawLine(
                prevPoint.x, prevPoint.y,
                currentPoint.x, currentPoint.y,
                basePaint
            )
        }
        
        // Dessiner le point actuel (qui trace)
        val currentY = baseY - currentHeight
        basePaint.strokeWidth = 1f
        canvas.drawCircle(baseX, currentY, 6f, basePaint)
    }
}
