package com.example.souffleforcetest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class OrganicLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    // ==================== UI ELEMENTS ====================
    
    private val resetButtonPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val resetTextPaint = Paint().apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 80f
        isAntiAlias = true
        textAlign = Paint.Align.LEFT
    }
    
    private val stemPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.rgb(50, 120, 50) // Vert marguerite
    }
    
    // ==================== ÉTATS DU SYSTÈME ====================
    
    private var showResetButton = false
    private var resetButtonX = 0f
    private var resetButtonY = 0f
    private val resetButtonRadius = 175f
    
    private var lightState = LightState.YELLOW
    private var stateStartTime = 0L
    
    // ==================== DONNÉES DE LA TIGE ====================
    
    data class StemPoint(
        val x: Float,
        val y: Float,
        val thickness: Float,
        val oscillation: Float = 0f,
        val permanentWave: Float = 0f
    )
    
    data class Branch(
        val points: MutableList<StemPoint> = mutableListOf(),
        val angle: Float,
        val startHeight: Float,
        var isActive: Boolean = true
    )
    
    private val mainStem = mutableListOf<StemPoint>()
    private val branches = mutableListOf<Branch>()
    private var stemHeight = 0f
    private var maxPossibleHeight = 0f
    private var stemBaseX = 0f
    private var stemBaseY = 0f
    private var lastForce = 0f
    private var isEmerging = false
    private var emergenceStartTime = 0L
    private var branchSide = true // true = droite, false = gauche
    
    // ==================== PARAMÈTRES DE CROISSANCE ====================
    
    private val forceThreshold = 0.05f // Seuil anti-bruit
    private val maxStemHeight = 0.8f // 80% de la hauteur d'écran
    private val baseThickness = 25f // Plus épaisse
    private val tipThickness = 8f // Plus épaisse
    private val growthRate = 2400f // x20 plus rapide
    private val oscillationDecay = 0.98f // Garde oscillation plus longtemps
    private val branchThreshold = 0.15f // Plus sensible (0.3f → 0.15f)
    private val emergenceDuration = 1000L // 1 seconde
    
    enum class LightState {
        YELLOW, GREEN_GROW, GREEN_LEAVES, GREEN_FLOWER, RED
    }
    
    // ==================== GESTION DE L'ÉCRAN ====================
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        resetButtonX = w - resetButtonRadius - 50f
        resetButtonY = resetButtonRadius + 80f
        
        stemBaseX = w / 2f
        stemBaseY = h - 100f
        maxPossibleHeight = h * maxStemHeight
    }
    
    // ==================== CONTRÔLE DU CYCLE ====================
    
    fun startCycle() {
        lightState = LightState.YELLOW
        stateStartTime = System.currentTimeMillis()
        showResetButton = false
        resetStem()
        invalidate()
    }
    
    fun updateForce(force: Float) {
        updateLightState()
        
        if (lightState == LightState.GREEN_GROW) {
            processStemGrowth(force)
        }
        
        if (!showResetButton && stemHeight > 30f) {
            showResetButton = true
        }
        
        invalidate()
    }
    
    private fun updateLightState() {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - stateStartTime
        
        when (lightState) {
            LightState.YELLOW -> {
                if (elapsedTime >= 2000) {
                    lightState = LightState.GREEN_GROW
                    stateStartTime = currentTime
                }
            }
            LightState.GREEN_GROW -> {
                if (elapsedTime >= 4000) { // 4 secondes : 1s émergence + 3s croissance
                    lightState = LightState.GREEN_LEAVES
                    stateStartTime = currentTime
                }
            }
            LightState.GREEN_LEAVES -> {
                if (elapsedTime >= 3000) {
                    lightState = LightState.GREEN_FLOWER
                    stateStartTime = currentTime
                }
            }
            LightState.GREEN_FLOWER -> {
                if (elapsedTime >= 3000) {
                    lightState = LightState.RED
                    stateStartTime = currentTime
                }
            }
            LightState.RED -> {}
        }
    }
    
    // ==================== LOGIQUE DE CROISSANCE ====================
    
    private fun processStemGrowth(force: Float) {
        val currentTime = System.currentTimeMillis()
        val phaseTime = currentTime - stateStartTime
        
        // Phase d'émergence (1 seconde)
        if (phaseTime < emergenceDuration) {
            if (force > forceThreshold && !isEmerging) {
                isEmerging = true
                emergenceStartTime = currentTime
            }
            
            if (isEmerging) {
                val emergenceProgress = (currentTime - emergenceStartTime) / emergenceDuration.toFloat()
                if (emergenceProgress <= 1f) {
                    createEmergenceStem(emergenceProgress)
                }
            }
            return
        }
        
        // Phase de croissance normale
        if (force > forceThreshold && mainStem.isNotEmpty()) {
            // Calcul de la qualité du souffle
            val forceStability = 1f - abs(force - lastForce).coerceAtMost(0.5f) * 2f
            val qualityMultiplier = 0.5f + forceStability * 0.5f
            
            // Croissance avec courbe réaliste
            val growthProgress = stemHeight / maxPossibleHeight
            val progressCurve = 1f - growthProgress * growthProgress // Ralentit vers la fin
            val adjustedGrowth = force * qualityMultiplier * progressCurve * growthRate * 0.016f * 10f // x10 supplémentaire
            
            if (adjustedGrowth > 0 && stemHeight < maxPossibleHeight) {
                growStem(adjustedGrowth, force)
            }
            
            // Détection ramification (souffle saccadé) - PLUS SENSIBLE
            if (abs(force - lastForce) > branchThreshold && stemHeight > 30f) { // Seuil réduit (50f → 30f)
                createBranch()
            }
        }
        
        // Decay des oscillations
        decayOscillations()
        lastForce = force
    }
    
    private fun createEmergenceStem(progress: Float) {
        mainStem.clear()
        val emergenceHeight = 30f * progress
        
        for (i in 0..5) {
            val segmentProgress = i / 5f
            val y = stemBaseY - emergenceHeight * segmentProgress
            val thickness = lerp(baseThickness, tipThickness, segmentProgress * 0.3f)
            val wiggle = sin(progress * PI * 3 + i * 0.5) * 2f * progress
            
            mainStem.add(StemPoint(stemBaseX + wiggle.toFloat(), y, thickness))
        }
        
        if (progress >= 1f) {
            stemHeight = emergenceHeight
        }
    }
    
    private fun growStem(growth: Float, force: Float) {
        stemHeight += growth
        
        val lastPoint = mainStem.lastOrNull() ?: return
        val segmentHeight = 8f
        val segments = (growth / segmentHeight).toInt().coerceAtLeast(1)
        
        for (i in 1..segments) {
            val currentHeight = stemHeight - growth + (growth * i / segments)
            val progressFromBase = currentHeight / maxPossibleHeight
            
            // Épaisseur qui diminue vers le haut
            val thickness = lerp(baseThickness, tipThickness, progressFromBase)
            
            // Position X avec légère ondulation naturelle
            val naturalSway = sin(currentHeight * 0.02f) * 3f
            val currentX = stemBaseX + naturalSway
            
            // Oscillation temporaire selon fréquence du souffle (PLUS SENSIBLE)
            val forceVariation = abs(force - 0.5f) * 4f // Plus sensible (2f → 4f)
            val oscillation = sin(System.currentTimeMillis() * 0.01f * forceVariation) * force * 35f // Plus forte (15f → 35f)
            
            val newY = stemBaseY - currentHeight
            val newPoint = StemPoint(currentX, newY, thickness, oscillation)
            mainStem.add(newPoint)
        }
    }
    
    private fun createBranch() {
        if (branches.size >= 3) return // Max 3 branches
        
        val branchStartHeight = stemHeight * (0.3f + branches.size * 0.2f)
        val branchAngle = (30f + Math.random() * 30f).toFloat() * if (branchSide) 1f else -1f
        
        branches.add(Branch(angle = branchAngle, startHeight = branchStartHeight))
        branchSide = !branchSide
    }
    
    private fun decayOscillations() {
        for (i in mainStem.indices) {
            val point = mainStem[i]
            val decayedOscillation = point.oscillation * oscillationDecay
            val permanentWave = point.permanentWave + point.oscillation * 0.05f
            
            mainStem[i] = point.copy(
                oscillation = decayedOscillation,
                permanentWave = permanentWave * 0.5f
            )
        }
    }
    
    private fun resetStem() {
        mainStem.clear()
        branches.clear()
        stemHeight = 0f
        lastForce = 0f
        isEmerging = false
        branchSide = true
    }
    
    // ==================== AFFICHAGE ====================
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // CORRIGÉ : Dessiner la tige dans TOUTES les phases après croissance
        if (lightState == LightState.GREEN_GROW || 
            lightState == LightState.GREEN_LEAVES || 
            lightState == LightState.GREEN_FLOWER || 
            lightState == LightState.RED) {
            drawStem(canvas)
        }
        
        drawTrafficLight(canvas)
    }
    
    private fun drawStem(canvas: Canvas) {
        if (mainStem.size < 2) return
        
        val path = Path()
        var lastX = mainStem[0].x
        var lastY = mainStem[0].y
        path.moveTo(lastX, lastY)
        
        for (i in 1 until mainStem.size) {
            val point = mainStem[i]
            val adjustedX = point.x + point.oscillation + point.permanentWave
            
            // Courbe fluide entre les points
            val controlX = (lastX + adjustedX) / 2f
            val controlY = (lastY + point.y) / 2f
            path.quadTo(controlX, controlY, adjustedX, point.y)
            
            lastX = adjustedX
            lastY = point.y
        }
        
        // Dessiner avec épaisseur variable
        for (i in 1 until mainStem.size) {
            val point = mainStem[i]
            val prevPoint = mainStem[i - 1]
            
            stemPaint.strokeWidth = point.thickness
            val adjustedX = point.x + point.oscillation + point.permanentWave
            val prevAdjustedX = prevPoint.x + prevPoint.oscillation + prevPoint.permanentWave
            
            canvas.drawLine(prevAdjustedX, prevPoint.y, adjustedX, point.y, stemPaint)
        }
    }
    
    private fun drawTrafficLight(canvas: Canvas) {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - stateStartTime
        
        val lightRadius = if (lightState == LightState.YELLOW) width * 0.4f else resetButtonRadius
        val lightX = if (lightState == LightState.YELLOW) width / 2f else resetButtonX
        val lightY = if (lightState == LightState.YELLOW) height / 2f else resetButtonY
        
        // Ombre
        resetButtonPaint.color = 0x40000000.toInt()
        canvas.drawCircle(lightX + 8f, lightY + 8f, lightRadius, resetButtonPaint)
        
        // Couleur selon l'état
        resetButtonPaint.color = when (lightState) {
            LightState.YELLOW -> 0xFFFFD700.toInt()
            LightState.GREEN_GROW -> 0xFF2F4F2F.toInt()
            LightState.GREEN_LEAVES -> 0xFF00FF00.toInt()
            LightState.GREEN_FLOWER -> 0xFFFF69B4.toInt()
            LightState.RED -> 0xFFFF0000.toInt()
        }
        canvas.drawCircle(lightX, lightY, lightRadius, resetButtonPaint)
        
        // Bordure
        resetButtonPaint.color = 0xFF333333.toInt()
        resetButtonPaint.style = Paint.Style.STROKE
        resetButtonPaint.strokeWidth = 12f
        canvas.drawCircle(lightX, lightY, lightRadius, resetButtonPaint)
        resetButtonPaint.style = Paint.Style.FILL
        
        // Texte et timer
        val timeRemaining = when (lightState) {
            LightState.YELLOW -> max(0, 2 - (elapsedTime / 1000))
            LightState.GREEN_GROW -> max(0, 4 - (elapsedTime / 1000))
            LightState.GREEN_LEAVES -> max(0, 3 - (elapsedTime / 1000))
            LightState.GREEN_FLOWER -> max(0, 3 - (elapsedTime / 1000))
            LightState.RED -> 0
        }
        
        if (lightState == LightState.YELLOW) {
            resetTextPaint.textAlign = Paint.Align.CENTER
            resetTextPaint.textSize = 180f
            resetTextPaint.color = 0xFF000000.toInt()
            canvas.drawText("INSPIREZ", lightX, lightY, resetTextPaint)
            
            if (timeRemaining > 0) {
                resetTextPaint.textSize = 108f
                canvas.drawText(timeRemaining.toString(), lightX, lightY + 144f, resetTextPaint)
            }
        } else if (lightState == LightState.RED) {
            resetTextPaint.textAlign = Paint.Align.CENTER
            resetTextPaint.textSize = 120f
            resetTextPaint.color = 0xFF000000.toInt()
            canvas.drawText("↻", lightX, lightY, resetTextPaint)
        }
    }
    
    // ==================== GESTION DES ÉVÉNEMENTS ====================
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && lightState == LightState.RED) {
            val dx = event.x - resetButtonX
            val dy = event.y - resetButtonY
            val distance = sqrt(dx * dx + dy * dy)
            
            if (distance <= resetButtonRadius) {
                startCycle()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
    
    // ==================== UTILITAIRES ====================
    
    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + fraction * (end - start)
    }
}
