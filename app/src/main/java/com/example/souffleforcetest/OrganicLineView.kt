package com.example.souffleforcetest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class OrganicLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private val paint = Paint().apply {
        color = 0xFFFFFFFF.toInt() // Blanc
        strokeWidth = 1f // Épaisseur de base
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }
    
    private var currentForce = 0.0f
    private var previousForce = 0.0f
    private var currentHeight = 0f // Hauteur cumulative
    private var currentStrokeWidth = 1f // Épaisseur dynamique
    private var maxHeight = 0f
    private var baseX = 0f
    private var baseY = 0f
    
    // Configuration de croissance
    private val forceThreshold = 0.08f // Seuil anti-parasite à 8%
    private val growthRate = 12.4f // Pixels par frame (2x plus rapide)
    
    // Configuration du rythme
    private val baseStrokeWidth = 1f
    private val maxStrokeWidth = 12f
    private val strokeDecayRate = 0.2f // Retour graduel épaisseur
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // Position de base : en bas au centre
        baseX = w / 2.0f
        baseY = h - 50f // 50px du bas
        maxHeight = h - 100f // Hauteur maximale disponible
    }
    
    fun updateForce(force: Float) {
        // Sauvegarder la force précédente pour détecter les variations
        previousForce = currentForce
        currentForce = force
        
        // Croissance cumulative seulement si au-dessus du seuil
        if (force > forceThreshold) {
            val adjustedForce = force - forceThreshold
            currentHeight += adjustedForce * growthRate
            
            // Limiter à la hauteur maximale
            currentHeight = kotlin.math.min(currentHeight, maxHeight)
        }
        
        // Détecter les variations pour l'épaisseur
        val rhythmIntensity = kotlin.math.abs(currentForce - previousForce)
        
        // Augmenter l'épaisseur selon l'intensité du rythme
        if (rhythmIntensity > 0.02f) { // Seuil minimal pour variation
            val thicknessIncrease = rhythmIntensity * 40f // Facteur de multiplication
            currentStrokeWidth = kotlin.math.min(maxStrokeWidth, baseStrokeWidth + thicknessIncrease)
        }
        
        // Retour graduel à l'épaisseur de base
        if (currentStrokeWidth > baseStrokeWidth) {
            currentStrokeWidth = kotlin.math.max(baseStrokeWidth, currentStrokeWidth - strokeDecayRate)
        }
        
        invalidate() // Redessiner
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Mettre à jour l'épaisseur du pinceau
        paint.strokeWidth = currentStrokeWidth
        
        // Dessiner la ligne verticale cumulative
        if (currentHeight > 0) {
            canvas.drawLine(baseX, baseY, baseX, baseY - currentHeight, paint)
        }
        
        // Dessiner le point de base
        canvas.drawCircle(baseX, baseY, 6f, paint)
    }
}
