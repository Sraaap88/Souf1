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
    private var currentStrokeWidth = 2f // Épaisseur actuelle
    private var offsetX = 0f // Déplacement horizontal
    private var maxHeight = 0f
    private var baseX = 0f
    private var baseY = 0f
    
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
    private val baseStrokeWidth = 2f // 2x plus épais
    private val maxStrokeWidth = 24f // 2x plus épais aussi
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
        
        invalidate()
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val time = System.currentTimeMillis() * 0.003f // Temps plus rapide pour respiration visible
        
        // Dessiner le tracé avec les épaisseurs et ondulations variables
        for (i in 1 until tracedPath.size) {
            val prevPoint = tracedPath[i - 1]
            val currentPoint = tracedPath[i]
            
            // Calculer ondulation pour chaque point selon sa fréquence propre
            val prevWaveOffset = if (prevPoint.waveAmplitude > 0) {
                kotlin.math.sin(prevPoint.y * prevPoint.waveFrequency * 0.01f + time) * prevPoint.waveAmplitude
            } else 0f
            
            val currentWaveOffset = if (currentPoint.waveAmplitude > 0) {
                kotlin.math.sin(currentPoint.y * currentPoint.waveFrequency * 0.01f + time) * currentPoint.waveAmplitude
            } else 0f
            
            basePaint.strokeWidth = currentPoint.strokeWidth
            canvas.drawLine(
                prevPoint.x + prevWaveOffset, prevPoint.y,
                currentPoint.x + currentWaveOffset, currentPoint.y,
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
