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
    private var maxHeight = 0f
    private var baseX = 0f
    private var baseY = 0f
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // Position de base : en bas au centre
        baseX = w / 2.0f
        baseY = h - 50f // 50px du bas
        maxHeight = h - 100f // Hauteur maximale disponible
    }
    
    fun updateForce(force: Float) {
        this.currentForce = force
        invalidate() // Redessiner
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Calculer la hauteur de la ligne selon la force
        val lineHeight = currentForce * maxHeight
        
        // Dessiner la ligne verticale depuis le bas vers le haut
        if (lineHeight > 0) {
            canvas.drawLine(baseX, baseY, baseX, baseY - lineHeight, paint)
        }
        
        // Dessiner le point de base
        canvas.drawCircle(baseX, baseY, 6f, paint)
    }
}