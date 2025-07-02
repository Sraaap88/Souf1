package com.example.souffleforcetest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
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
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && showResetButton) {
            val dx = event.x - resetButtonX
            val dy = event.y - resetButtonY
            val distance = kotlin.math.sqrt(dx * dx + dy * dy)
            
            if (distance <= resetButtonRadius) {
                // Reset la plante
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
        
        // Remettre le point de départ
        tracedPath.add(TracePoint(baseX, baseY, baseStrokeWidth, 0f, 0f, 0f))
        invalidate()
    }
    
    private val resetButtonPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val resetTextPaint = Paint().apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 120f // 5x plus gros aussi
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
    private val resetButtonRadius = 175f // 5x plus gros (35f * 5)
    
    // Stockage du tracé
    private data class TracePoint(
        val x: Float,
        val y: Float,
        val strokeWidth: Float,
        val waveFrequency: Float, // Fréquence d'ondulation figée
        val waveAmplitude: Float, // Amplitude d'ondulation figée
        val curvature: Float      // Courbure pour les courbes naturelles
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
        resetButtonX = w - resetButtonRadius - 50f // Plus d'espace pour le gros bouton
        resetButtonY = resetButtonRadius + 80f
        
        // Point de départ
        if (tracedPath.isEmpty()) {
            tracedPath.add(TracePoint(baseX + offsetX, baseY, baseStrokeWidth, 0f, 0f, 0f))
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
        
        // Calcul de la courbure selon les variations
        var curvature = 0f
        if (rhythmIntensity > 0.05f) {
            // Plus de variation = plus de courbure naturelle
            curvature = (rhythmIntensity * 60f).coerceAtMost(30f) // Max 30px de courbure
            if ((0..1).random() == 0) curvature = -curvature // Direction aléatoire
        }
        
        // Calcul des ondulations selon micro-variations (seuils ajustés)
        var waveFreq = 0f
        var waveAmp = 0f
        
        if (rhythmIntensity > 0.01f && rhythmIntensity < abruptThreshold) {
            // Micro-variations = ondulations (seuil plus bas)
            waveFreq = (rhythmIntensity * 15f) + 1f // Fréquence 1-3.25
            waveAmp = (rhythmIntensity * 200f).coerceAtMost(maxWaveAmplitude) // Plus sensible
        }
        
        // FORCER des ondulations pour test si aucune variation
        if (waveAmp == 0f && currentHeight > 0f) {
            waveFreq = 2f // Fréquence de base
            waveAmp = 3f // Amplitude de base pour voir le mouvement
        }
        
        // Ajouter le nouveau point au tracé (avec déplacement, ondulations et courbure)
        val currentY = baseY - currentHeight
        val currentX = baseX + offsetX
        
        if (tracedPath.isNotEmpty()) {
            val lastPoint = tracedPath.last()
            if (currentY < lastPoint.y) { // Seulement si on monte
                tracedPath.add(TracePoint(currentX, currentY, currentStrokeWidth, waveFreq, waveAmp, curvature))
            }
        }
        
        // Montrer le bouton reset après le premier souffle
        if (!showResetButton && currentHeight > 50f) {
            showResetButton = true
        }
        
        invalidate() // Redessiner pour nouveau point
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val time = System.currentTimeMillis() * 0.002f // Temps pour animation
        
        // Dessiner le tracé avec courbes naturelles, épaisseurs variables ET oscillation
        for (i in 1 until tracedPath.size) {
            val prevPoint = tracedPath[i - 1]
            val currentPoint = tracedPath[i]
            
            // Oscillation pour ce segment
            val oscillation = kotlin.math.sin(time + currentPoint.y * 0.01f) * 8f
            
            // Calculer le point de contrôle pour la courbe Bézier (avec oscillation)
            val midX = (prevPoint.x + currentPoint.x) / 2f + oscillation
            val midY = (prevPoint.y + currentPoint.y) / 2f
            
            // Ajouter la courbure perpendiculaire au segment
            val dx = currentPoint.x - prevPoint.x
            val dy = currentPoint.y - prevPoint.y
            val length = kotlin.math.sqrt(dx * dx + dy * dy)
            
            val controlX = if (length > 0) {
                midX + (-dy / length) * currentPoint.curvature
            } else midX
            
            val controlY = if (length > 0) {
                midY + (dx / length) * currentPoint.curvature
            } else midY
            
            // Créer Path pour ce segment avec SON épaisseur (et oscillation)
            val segmentPath = Path()
            segmentPath.moveTo(prevPoint.x + oscillation * 0.7f, prevPoint.y)
            segmentPath.quadTo(controlX, controlY, currentPoint.x + oscillation, currentPoint.y)
            
            // Appliquer l'épaisseur spécifique à ce segment
            basePaint.strokeWidth = currentPoint.strokeWidth
            canvas.drawPath(segmentPath, basePaint)
        }
        
        // Dessiner le point actuel (avec léger mouvement aussi)
        val currentY = baseY - currentHeight
        val pointOscillation = kotlin.math.sin(time * 1.5f) * 3f
        val currentX = baseX + offsetX + pointOscillation
        basePaint.style = Paint.Style.FILL
        canvas.drawCircle(currentX, currentY, 8f, basePaint)
        basePaint.style = Paint.Style.STROKE
        
        // Dessiner le bouton reset si disponible
        if (showResetButton) {
            // Ombre douce
            resetButtonPaint.color = 0x40000000.toInt()
            canvas.drawCircle(resetButtonX + 8f, resetButtonY + 8f, resetButtonRadius, resetButtonPaint)
            
            // Bouton principal rouge
            resetButtonPaint.color = 0xFFE53E3E.toInt()
            canvas.drawCircle(resetButtonX, resetButtonY, resetButtonRadius, resetButtonPaint)
            
            // Bordure plus foncée
            resetButtonPaint.color = 0xFFC53030.toInt()
            resetButtonPaint.style = Paint.Style.STROKE
            resetButtonPaint.strokeWidth = 8f
            canvas.drawCircle(resetButtonX, resetButtonY, resetButtonRadius, resetButtonPaint)
            resetButtonPaint.style = Paint.Style.FILL
            
            // Texte "↻" plus gros
            canvas.drawText("↻", resetButtonX, resetButtonY + 40f, resetTextPaint)
        }
    }
}
