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
    
    // Paint pour le texte en haut à gauche
    private val instructionTextPaint = Paint().apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 80f
        isAntiAlias = true
        textAlign = Paint.Align.LEFT
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
    
    // Variables pour les voyelles
    private var uIntensity = 0f
    private var aIntensity = 0f
    private var iIntensity = 0f
    private var oIntensity = 0f
    
    // États du système en 6 étapes maintenant
    private var lightState = LightState.YELLOW
    private var stateStartTime = 0L
    private var canGrow = false
    
    enum class LightState {
        YELLOW,       // 2s - Inspirez + timer
        GREEN_GROW,   // 3s - Croissance tige + timer
        GREEN_BUDS,   // 3s - Bourgeons avec U + timer
        GREEN_LEAVES, // 3s - Feuilles avec A/I + timer
        GREEN_FLOWER, // 3s - Fleur avec O + timer
        RED           // Infini - Admirez et recommencez
    }
    
    private data class TracePoint(
        val x: Float,
        val y: Float,
        val strokeWidth: Float,
        val waveFrequency: Float,
        val waveAmplitude: Float,
        val curvature: Float
    )
    
    // Nouveaux éléments visuels
    data class Bourgeon(val x: Float, val y: Float, var taille: Float)
    data class Feuille(val bourgeon: Bourgeon, var longueur: Float, var largeur: Float, val angle: Float)
    data class Fleur(val x: Float, val y: Float, var taille: Float, var petalCount: Int)
    
    private val tracedPath = mutableListOf<TracePoint>()
    private val bourgeons = mutableListOf<Bourgeon>()
    private val feuilles = mutableListOf<Feuille>()
    private var fleur: Fleur? = null
    
    private val forceThreshold = 0.08f
    private val growthRate = 174.6f
    private val baseStrokeWidth = 4f
    private val maxStrokeWidth = 96f
    private val strokeDecayRate = 0.2f
    private val abruptThreshold = 0.15f
    private val centeringRate = 0.92f
    private val waveThreshold = 0.03f
    private val maxWaveAmplitude = 15f
    
    private fun drawTrafficLight(canvas: Canvas) {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - stateStartTime
        
        // Calculer la taille selon l'étape
        val lightRadius = when (lightState) {
            LightState.YELLOW -> width * 0.4f // Presque plein écran
            else -> resetButtonRadius // Taille normale
        }
        
        // Position ajustée pour grande lumière jaune
        val lightX = if (lightState == LightState.YELLOW) width / 2f else resetButtonX
        val lightY = if (lightState == LightState.YELLOW) height / 2f else resetButtonY
        
        // Ombre
        resetButtonPaint.color = 0x40000000.toInt()
        canvas.drawCircle(lightX + 8f, lightY + 8f, lightRadius, resetButtonPaint)
        
        // Couleur selon l'étape
        when (lightState) {
            LightState.YELLOW -> resetButtonPaint.color = 0xFFFFD700.toInt() // Jaune
            LightState.GREEN_GROW -> resetButtonPaint.color = 0xFF00FF00.toInt()  // Vert clair
            LightState.GREEN_BUDS -> resetButtonPaint.color = 0xFF228B22.toInt() // Vert foncé
            LightState.GREEN_LEAVES -> resetButtonPaint.color = 0xFF32CD32.toInt() // Vert moyen
            LightState.GREEN_FLOWER -> resetButtonPaint.color = 0xFF90EE90.toInt()  // Vert plus clair
            LightState.RED -> resetButtonPaint.color = 0xFFFF0000.toInt()    // Rouge
        }
        canvas.drawCircle(lightX, lightY, lightRadius, resetButtonPaint)
        
        // Bordure
        resetButtonPaint.color = 0xFF333333.toInt()
        resetButtonPaint.style = Paint.Style.STROKE
        resetButtonPaint.strokeWidth = 12f
        canvas.drawCircle(lightX, lightY, lightRadius, resetButtonPaint)
        resetButtonPaint.style = Paint.Style.FILL
        
        // Calculer timer uniquement
        val timeRemaining = when (lightState) {
            LightState.YELLOW -> kotlin.math.max(0, 2 - (elapsedTime / 1000))
            LightState.GREEN_GROW -> kotlin.math.max(0, 3 - (elapsedTime / 1000))
            LightState.GREEN_BUDS -> kotlin.math.max(0, 3 - (elapsedTime / 1000))
            LightState.GREEN_LEAVES -> kotlin.math.max(0, 3 - (elapsedTime / 1000))
            LightState.GREEN_FLOWER -> kotlin.math.max(0, 3 - (elapsedTime / 1000))
            LightState.RED -> 0
        }
        
        // Ajuster taille du texte selon la lumière
        val textSize = if (lightState == LightState.YELLOW) 180f else 120f
        resetTextPaint.textSize = textSize
        resetTextPaint.color = 0xFF000000.toInt()
        
        // Pour l'étape jaune : texte "INSPIREZ" au centre
        if (lightState == LightState.YELLOW) {
            canvas.drawText("INSPIREZ", lightX, lightY - 60f, resetTextPaint)
        }
        
        // Dessiner timer dans la lumière (sauf rouge)
        if (lightState != LightState.RED && timeRemaining > 0) {
            canvas.drawText(timeRemaining.toString(), lightX, lightY, resetTextPaint)
        } else if (lightState == LightState.RED) {
            canvas.drawText("↻", lightX, lightY, resetTextPaint)
        }
        
        // Dessiner le texte d'instruction en haut à gauche (sauf jaune)
        if (lightState != LightState.YELLOW) {
            val instructionText = when (lightState) {
                LightState.GREEN_GROW -> "SOUFFLEZ"
                LightState.GREEN_BUDS -> "DITES U"
                LightState.GREEN_LEAVES -> "DITES A et I"
                LightState.GREEN_FLOWER -> "DITES O"
                LightState.RED -> ""
                else -> ""
            }
            canvas.drawText(instructionText, 50f, 120f, instructionTextPaint)
        }
        
        // Remettre taille normale
        resetTextPaint.textSize = 120f
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        baseX = w / 2.0f
        baseY = h - 50f
        maxHeight = h - 100f
        
        resetButtonX = w - resetButtonRadius - 50f
        resetButtonY = resetButtonRadius + 80f
        
        if (tracedPath.isEmpty()) {
            tracedPath.add(TracePoint(baseX + offsetX, baseY, baseStrokeWidth, 0f, 0f, 0f))
            // Démarrer le cycle du feu
            stateStartTime = System.currentTimeMillis()
            lightState = LightState.YELLOW
        }
    }
    
    fun updateForce(force: Float) {
        // Gérer le système en 6 étapes
        updateLightState()
        
        // Ne grandir que pendant l'étape de croissance
        if (lightState != LightState.GREEN_GROW) {
            if (lightState == LightState.GREEN_BUDS) {
                // Faire grandir les bourgeons selon U
                updateBuds()
            } else if (lightState == LightState.GREEN_LEAVES) {
                // Créer/grandir les feuilles selon A/I
                updateLeaves()
            } else if (lightState == LightState.GREEN_FLOWER) {
                // Créer/grandir la fleur selon O
                updateFlower()
            }
            return
        }
        
        previousForce = currentForce
        currentForce = force
        
        if (force > forceThreshold) {
            val adjustedForce = force - forceThreshold
            currentHeight += adjustedForce * growthRate
            currentHeight = kotlin.math.min(currentHeight, maxHeight)
        }
        
        val rhythmIntensity = kotlin.math.abs(currentForce - previousForce)
        
        if (rhythmIntensity > abruptThreshold) {
            val displacement = if ((0..1).random() == 0) 120f else -120f
            offsetX += displacement
            
            // Créer des bourgeons lors des coups de vent
            if (currentHeight > 100f) {
                val budX = baseX + offsetX + (if ((0..1).random() == 0) 60f else -60f)
                val budY = baseY - currentHeight + (0..100).random()
                bourgeons.add(Bourgeon(budX, budY, 0f))
            }
        } else if (rhythmIntensity > 0.02f) {
            val thicknessIncrease = rhythmIntensity * 160f
            currentStrokeWidth = kotlin.math.min(maxStrokeWidth, baseStrokeWidth + thicknessIncrease)
        }
        
        if (currentStrokeWidth > baseStrokeWidth) {
            currentStrokeWidth = kotlin.math.max(baseStrokeWidth, currentStrokeWidth - strokeDecayRate)
        }
        offsetX *= centeringRate
        
        var curvature = 0f
        if (rhythmIntensity > 0.05f) {
            curvature = (rhythmIntensity * 40f).coerceAtMost(20f)
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
    
    // Fonctions publiques pour recevoir les données audio
    fun updateVowelU(intensity: Float) {
        uIntensity = intensity.coerceIn(0f, 1f)
    }
    
    fun updateVowelA(intensity: Float) {
        aIntensity = intensity.coerceIn(0f, 1f)
    }
    
    fun updateVowelI(intensity: Float) {
        iIntensity = intensity.coerceIn(0f, 1f)
    }
    
    fun updateVowelO(intensity: Float) {
        oIntensity = intensity.coerceIn(0f, 1f)
    }
    
    // Fonction pour redémarrer le cycle proprement après permissions
    fun restartCycle() {
        lightState = LightState.YELLOW
        stateStartTime = System.currentTimeMillis()
        invalidate()
    }
    
    // Fonction pour mettre à jour les bourgeons (étape GREEN_BUDS)
    private fun updateBuds() {
        for (bourgeon in bourgeons) {
            bourgeon.taille = uIntensity * 30f
        }
    }
    
    // Fonction pour mettre à jour les feuilles (étape GREEN_LEAVES)
    private fun updateLeaves() {
        for (bourgeon in bourgeons) {
            if (bourgeon.taille > 5f) {
                var feuille = feuilles.find { it.bourgeon == bourgeon }
                if (feuille == null) {
                    val angle = (0..360).random().toFloat()
                    feuille = Feuille(bourgeon, 0f, 0f, angle)
                    feuilles.add(feuille)
                }
                
                feuille.longueur = aIntensity * 50f
                feuille.largeur = iIntensity * 20f
            }
        }
    }
    
    // Fonction pour mettre à jour la fleur (étape GREEN_FLOWER)
    private fun updateFlower() {
        if (tracedPath.isNotEmpty()) {
            val topPoint = tracedPath.minByOrNull { it.y }
            if (topPoint != null) {
                if (fleur == null) {
                    fleur = Fleur(topPoint.x, topPoint.y, 0f, 5)
                }
                fleur?.let {
                    it.taille = oIntensity * 40f
                    it.petalCount = kotlin.math.max(3, (oIntensity * 8).toInt())
                }
            }
        }
    }
    
    private fun updateLightState() {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - stateStartTime
        
        when (lightState) {
            LightState.YELLOW -> {
                canGrow = false
                if (elapsedTime >= 2000) {
                    lightState = LightState.GREEN_GROW
                    stateStartTime = currentTime
                }
            }
            LightState.GREEN_GROW -> {
                canGrow = true
                if (elapsedTime >= 3000) {
                    lightState = LightState.GREEN_BUDS
                    stateStartTime = currentTime
                    canGrow = false
                }
            }
            LightState.GREEN_BUDS -> {
                canGrow = false
                if (elapsedTime >= 3000) {
                    lightState = LightState.GREEN_LEAVES
                    stateStartTime = currentTime
                }
            }
            LightState.GREEN_LEAVES -> {
                canGrow = false
                if (elapsedTime >= 3000) {
                    lightState = LightState.GREEN_FLOWER
                    stateStartTime = currentTime
                }
            }
            LightState.GREEN_FLOWER -> {
                canGrow = false
                if (elapsedTime >= 3000) {
                    lightState = LightState.RED
                    stateStartTime = currentTime
                }
            }
            LightState.RED -> {
                canGrow = false
            }
        }
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val time = System.currentTimeMillis() * 0.002f
        
        for (i in 1 until tracedPath.size) {
            val prevPoint = tracedPath[i - 1]
            val currentPoint = tracedPath[i]
            
            val oscillation = kotlin.math.sin(time + currentPoint.y * 0.005f) * 35f
            
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
            segmentPath.moveTo(prevPoint.x + oscillation * 0.8f, prevPoint.y)
            segmentPath.quadTo(controlX, controlY, currentPoint.x + oscillation, currentPoint.y)
            
            basePaint.strokeWidth = currentPoint.strokeWidth
            canvas.drawPath(segmentPath, basePaint)
        }
        
        val currentY = baseY - currentHeight
        val pointOscillation = kotlin.math.sin(time * 2f) * 15f
        val currentX = baseX + offsetX + pointOscillation
        basePaint.style = Paint.Style.FILL
        canvas.drawCircle(currentX, currentY, 8f, basePaint)
        basePaint.style = Paint.Style.STROKE
        
        // Dessiner les bourgeons
        basePaint.color = 0xFF32CD32.toInt()
        basePaint.style = Paint.Style.FILL
        for (bourgeon in bourgeons) {
            if (bourgeon.taille > 0) {
                canvas.drawCircle(bourgeon.x, bourgeon.y, bourgeon.taille, basePaint)
            }
        }
        
        // Dessiner les feuilles
        basePaint.color = 0xFF228B22.toInt()
        for (feuille in feuilles) {
            if (feuille.longueur > 0 && feuille.largeur > 0) {
                canvas.save()
                canvas.translate(feuille.bourgeon.x, feuille.bourgeon.y)
                canvas.rotate(feuille.angle)
                canvas.drawOval(0f, 0f, feuille.longueur, feuille.largeur, basePaint)
                canvas.restore()
            }
        }
        
        // Dessiner la fleur
        fleur?.let { flower ->
            if (flower.taille > 0) {
                basePaint.color = 0xFFFFB6C1.toInt()
                val angleStep = 360f / flower.petalCount
                for (i in 0 until flower.petalCount) {
                    val angle = i * angleStep
                    val petalX = flower.x + kotlin.math.cos(Math.toRadians(angle.toDouble())).toFloat() * flower.taille * 0.5f
                    val petalY = flower.y + kotlin.math.sin(Math.toRadians(angle.toDouble())).toFloat() * flower.taille * 0.5f
                    canvas.drawCircle(petalX, petalY, flower.taille * 0.3f, basePaint)
                }
                basePaint.color = 0xFFFFD700.toInt()
                canvas.drawCircle(flower.x, flower.y, flower.taille * 0.2f, basePaint)
            }
        }
        
        // Remettre la couleur blanche pour la tige
        basePaint.color = 0xFFFFFFFF.toInt()
        basePaint.style = Paint.Style.STROKE
        
        // Dessiner le feu de circulation
        drawTrafficLight(canvas)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && lightState == LightState.RED) {
            val lightX = resetButtonX
            val lightY = resetButtonY
            val lightRadius = resetButtonRadius
            
            val dx = event.x - lightX
            val dy = event.y - lightY
            val distance = kotlin.math.sqrt(dx * dx + dy * dy)
            
            if (distance <= lightRadius) {
                resetPlant()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
    
    private fun resetPlant() {
        tracedPath.clear()
        bourgeons.clear()
        feuilles.clear()
        fleur = null
        
        currentHeight = 0f
        currentStrokeWidth = baseStrokeWidth
        offsetX = 0f
        showResetButton = false
        
        uIntensity = 0f
        aIntensity = 0f
        iIntensity = 0f
        oIntensity = 0f
        
        lightState = LightState.YELLOW
        stateStartTime = System.currentTimeMillis()
        canGrow = false
        
        tracedPath.add(TracePoint(baseX, baseY, baseStrokeWidth, 0f, 0f, 0f))
        invalidate()
    }
}
