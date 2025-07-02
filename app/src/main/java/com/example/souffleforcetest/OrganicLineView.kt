package com.example.souffleforcetest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class OrganicLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    private val basePaint = Paint().apply {
        color = 0xFFFFFFFF.toInt() // Blanc
        strokeWidth = 4f // Encore plus épais de base
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
        textSize = 24f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    
    private var currentForce = 0.0f
    private var previousForce = 0.0f
    private var currentHeight = 0f // Position actuelle du point
    private var currentStrokeWidth = 4f // Épaisseur actuelle plus épaisse
    private var offsetX = 0f // Déplacement horizontal
    private var maxHeight = 0f
    private var baseX = 0f
    private var baseY = 0f
    private var showResetButton = false
    private var resetButtonX = 0f
    private var resetButtonY = 0f
    private val resetButtonRadius = 35f
    
    // Stockage du tracé
    private data class TracePoint(
        val x: Float,
        val y: Float,
        val strokeWidth: Float,
        val waveFrequency: Float, // Fréquence d'ondulation figée
        val waveAmplitude: Float  // Amplitude d'ondulation figée
    )
    
    private val tracedPath = mutableListOf<TracePoint>()
    
    // Configuration
    private val forceThreshold = 0.08f
    private val growthRate = 58.2f // Pixels par frame (50% plus rapide encore)
    private val baseStrokeWidth = 4f // Encore plus épais
    private val maxStrokeWidth = 48f // Encore plus épais aussi
    private val strokeDecayRate = 0.2f
    private val abruptThreshold = 0.15f // Seuil pour détecter coup de vent
    private val centeringRate = 0.92f // Vitesse de retour au centre
    private val waveThreshold = 0.03f // Seuil pour déclencher ondulations
    private val maxWaveAmplitude = 15f // Amplitude max des ondulations (plus visible)
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        baseX = w / 2.0f
        baseY = h - 50f
        maxHeight = h - 100f
        
        // Position du bouton reset (en haut à droite)
        resetButtonX = w - resetButtonRadius - 30f
        resetButtonY = resetButtonRadius + 50f
        
        // Point de départ
        if (tracedPath.isEmpty()) {
            tracedPath.add(TracePoint(baseX + offsetX, baseY, baseStrokeWidth, 0f, 0f))
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
            // Déplacement horizontal instantané ±40px (doublé)
            val displacement = if ((0..1).random() == 0) 40f else -40f
            offsetX += displacement
        } else if (rhythmIntensity > 0.02f) {
            // Variation rythmée normale - épaisseur plus prononcée
            val thicknessIncrease = rhythmIntensity * 80f // Doublé pour plus de variation
            currentStrokeWidth = kotlin.math.min(maxStrokeWidth, baseStrokeWidth + thicknessIncrease)
        }
        
        // Retours graduels
        if (currentStrokeWidth > baseStrokeWidth) {
            currentStrokeWidth = kotlin.math.max(baseStrokeWidth, currentStrokeWidth - strokeDecayRate)
        }
        offsetX *= centeringRate // Retour graduel au centre
        
        // Calcul des ondulations selon micro-variations
        var waveFreq = 0f
        var waveAmp = 0f
        
        if (rhythmIntensity > waveThreshold && rhythmIntensity < abruptThreshold) {
            // Micro-variations = ondulations
            waveFreq = (rhythmIntensity * 20f) + 2f // Fréquence 2-6
            waveAmp = (rhythmIntensity * 100f).coerceAtMost(maxWaveAmplitude)
        }
        
        // Ajouter le nouveau point au tracé (avec déplacement et ondulations)
        val currentY = baseY - currentHeight
        val currentX = baseX + offsetX
        
        if (tracedPath.isNotEmpty()) {
            val lastPoint = tracedPath.last()
            if (currentY < lastPoint.y) { // Seulement si on monte
                tracedPath.add(TracePoint(currentX, currentY, currentStrokeWidth, waveFreq, waveAmp))
            }
        }
        
        // Montrer le bouton reset après le premier souffle
        if (!showResetButton && currentHeight > 50f) {
            showResetButton = true
        }
        
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val time = System.currentTimeMillis() * 0.003f // Temps pour animation
        
        // Vent de base qui affecte toute la plante
        val baseWind = kotlin.math.sin(time * 0.4f) * 3f
        
        // Dessiner le tracé avec balancement global + danse locale
        for (i in 1 until tracedPath.size) {
            val prevPoint = tracedPath[i - 1]
            val currentPoint = tracedPath[i]
            
            // Mouvement global (balancement de toute la plante)
            val globalSway = baseWind * (currentPoint.y / maxHeight) // Plus fort vers le haut
            
            // Mouvement local (danse selon la mémoire du segment)
            val localDance = if (currentPoint.waveAmplitude > 0) {
                kotlin.math.sin(currentPoint.y * currentPoint.waveFrequency * 0.01f + time * 1.5f) * 
                (currentPoint.waveAmplitude * 0.7f) // 70% de l'amplitude originale
            } else 0f
            
            // Mouvement total = global + local
            val prevTotalOffset = globalSway + 
                if (prevPoint.waveAmplitude > 0) {
                    kotlin.math.sin(prevPoint.y * prevPoint.waveFrequency * 0.01f + time * 1.5f) * 
                    (prevPoint.waveAmplitude * 0.7f)
                } else 0f
                
            val currentTotalOffset = globalSway + localDance
            
            basePaint.strokeWidth = currentPoint.strokeWidth
            canvas.drawLine(
                prevPoint.x + prevTotalOffset, prevPoint.y,
                currentPoint.x + currentTotalOffset, currentPoint.y,
                basePaint
            )
        }
        
        // Dessiner le point actuel (qui trace) avec déplacement
        val currentY = baseY - currentHeight
        val currentX = baseX + offsetX + baseWind * (currentHeight / maxHeight)
        basePaint.style = Paint.Style.FILL // Point plein
        canvas.drawCircle(currentX, currentY, 8f, basePaint) // Point plus gros aussi
        basePaint.style = Paint.Style.STROKE // Remettre pour les lignes
        
        // Dessiner le bouton reset si disponible
        if (showResetButton) {
            // Ombre douce
            resetButtonPaint.color = 0x40000000.toInt() // Noir transparent
            canvas.drawCircle(resetButtonX + 3f, resetButtonY + 3f, resetButtonRadius, resetButtonPaint)
            
            // Bouton principal rouge
            resetButtonPaint.color = 0xFFE53E3E.toInt() // Rouge joli
            canvas.drawCircle(resetButtonX, resetButtonY, resetButtonRadius, resetButtonPaint)
            
            // Bordure plus foncée
            resetButtonPaint.color = 0xFFC53030.toInt() // Rouge plus foncé
            resetButtonPaint.style = Paint.Style.STROKE
            resetButtonPaint.strokeWidth = 3f
            canvas.drawCircle(resetButtonX, resetButtonY, resetButtonRadius, resetButtonPaint)
            resetButtonPaint.style = Paint.Style.FILL
            
            // Texte "↻"
            canvas.drawText("↻", resetButtonX, resetButtonY + 8f, resetTextPaint)
        }
    }
}
