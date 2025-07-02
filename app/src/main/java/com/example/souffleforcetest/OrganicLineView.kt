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
        strokeWidth = 4f // Même épaisseur que l'ancienne barre
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
    }
    
    private var currentForce = 0.0f
    private var currentHeight = 0f // Hauteur cumulative
    private var maxHeight = 0f
    private var baseX = 0f
    private var baseY = 0f
    
    // Configuration de croissance
    private val forceThreshold = 0.08f // Seuil anti-parasite à 8%
    private val growthRate = 6.2f // Pixels par frame (15% plus rapide)
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // Position de base : en bas au centre
        baseX = w / 2.0f
        baseY = h - 50f // 50px du bas
        maxHeight = h - 100f // Hauteur maximale disponible
    }
    
    fun updateForce(force: Float) {
        this.currentForce = force
        
        // Croissance cumulative seulement si au-dessus du seuil
        if (force > forceThreshold) {
            val adjustedForce = force - forceThreshold
            currentHeight += adjustedForce * growthRate
            
            // Limiter à la hauteur maximale
            currentHeight = kotlin.math.min(currentHeight, maxHeight)
        }
        
        invalidate() // Redessiner
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Dessiner la ligne verticale cumulative
        if (currentHeight > 0) {
            canvas.drawLine(baseX, baseY, baseX, baseY - currentHeight, paint)
        }
        
        // Dessiner le point de base
        canvas.drawCircle(baseX, baseY, 6f, paint)
    }
}
