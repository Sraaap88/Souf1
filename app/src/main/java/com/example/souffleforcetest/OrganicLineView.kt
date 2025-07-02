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
        strokeWidth = 2f // 2x plus épais de base
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }
    
    private var currentForce = 0.0f
    private var previousForce = 0.0f
    private var currentHeight = 0f // Position actuelle du point
    private var currentStrokeWidth = 1f // Épaisseur actuelle
    private var offsetX = 0f // Déplacement horizontal
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
    private val growthRate = 19.4f // Pixels par frame (30% plus rapide)
    private val baseStrokeWidth = 2f // 2x plus épais
    private val maxStrokeWidth = 24f // 2x plus épais aussi
    private val strokeDecayRate = 0.2f
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        baseX = w / 2.0f
        baseY = h - 50f
        maxHeight = h - 100f
        
        // Point de départ
        if (tracedPath.isEmpty()) {
            tracedPath.add(TracePoint(baseX + offsetX, baseY, baseStrokeWidth))
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
        
        // Calcul des variations
        val rhythmIntensity = kotlin.math.abs(currentForce - previousForce)
        
        // Détecter coup de vent (variation brusque)
        if (rhythmIntensity > abruptThreshold) {
            // Déplacement horizontal instantané ±20px
            val displacement = if ((0..1).random() == 0) 20f else -20f
            offsetX += displacement
        } else if (rhythmIntensity > 0.02f) {
            // Variation rythmée normale - épaisseur
            val thicknessIncrease = rhythmIntensity * 40f
            currentStrokeWidth = kotlin.math.min(maxStrokeWidth, baseStrokeWidth + thicknessIncrease)
        }
        
        // Retours graduels
        if (currentStrokeWidth > baseStrokeWidth) {
            currentStrokeWidth = kotlin.math.max(baseStrokeWidth, currentStrokeWidth - strokeDecayRate)
        }
        offsetX *= centeringRate // Retour graduel au centre
        
        // Ajouter le nouveau point au tracé (avec déplacement)
        val currentY = baseY - currentHeight
        val currentX = baseX + offsetX
        
        if (tracedPath.isNotEmpty()) {
            val lastPoint = tracedPath.last()
            if (currentY < lastPoint.y) { // Seulement si on monte
                tracedPath.add(TracePoint(currentX, currentY, currentStrokeWidth))
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
        
        // Dessiner le point actuel (qui trace) avec déplacement
        val currentY = baseY - currentHeight
        val currentX = baseX + offsetX
        basePaint.style = Paint.Style.FILL // Point plein
        canvas.drawCircle(currentX, currentY, 6f, basePaint)
        basePaint.style = Paint.Style.STROKE // Remettre pour les lignes
    }
}
