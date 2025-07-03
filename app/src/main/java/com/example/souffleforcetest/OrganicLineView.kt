package com.example.souffleforcetest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class OrganicLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    // Chargement des bitmaps dans init
    private lateinit var stemBitmap: Bitmap
    private lateinit var leafBitmap: Bitmap
    private lateinit var flowerBitmap: Bitmap
    
    private val basePaint = Paint().apply {
        color = 0xFFFFFFFF.toInt()
        strokeWidth = 4f
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
    
    // États du système en 4 étapes
    private var lightState = LightState.YELLOW
    private var stateStartTime = 0L
    private var canGrow = false
    
    enum class LightState {
        YELLOW,      // 2s - Inspirez + timer
        GREEN_GROW,  // 3s - Croissance tige + timer
        GREEN_LEAVES, // 3s - Croissance feuilles + timer
        GREEN_FLOWER, // 3s - Création fleur + timer
        RED          // Infini - Admirez et recommencez
    }
    
    private data class TracePoint(
        val x: Float,
        val y: Float,
        val strokeWidth: Float,
        val waveFrequency: Float,
        val waveAmplitude: Float,
        val curvature: Float
    )
    
    // Éléments visuels
    data class Bourgeon(val x: Float, val y: Float, var taille: Float)
    data class Feuille(val bourgeon: Bourgeon, var longueur: Float, var largeur: Float, val angle: Float)
    data class Fleur(val x: Float, val y: Float, var taille: Float, var petalCount: Int)
    
    private val tracedPath = mutableListOf<TracePoint>()
    private val bourgeons = mutableListOf<Bourgeon>()
    private val feuilles = mutableListOf<Feuille>()
    private var fleur: Fleur? = null
    
    private val forceThreshold = 0.08f
    private val growthRate = 87.3f // Divisé par 2 pour ralentir
    private val baseStrokeWidth = 4f
    private val maxStrokeWidth = 48f // Divisé par 2 pour réduire épaisseur max
    private val strokeDecayRate = 0.2f
    private val abruptThreshold = 0.15f
    private val centeringRate = 0.92f
    private val waveThreshold = 0.03f
    private val maxWaveAmplitude = 15f
    
    init {
        // Charger les bitmaps ici
        try {
            stemBitmap = BitmapFactory.decodeResource(resources, R.drawable.stem_segment)
            leafBitmap = BitmapFactory.decodeResource(resources, R.drawable.leaf)
            flowerBitmap = BitmapFactory.decodeResource(resources, R.drawable.flower)
        } catch (e: Exception) {
            // Fallback si les images ne se chargent pas
            e.printStackTrace()
        }
    }
    
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
            LightState.GREEN_LEAVES -> resetButtonPaint.color = 0xFF228B22.toInt() // Vert foncé
            LightState.GREEN_FLOWER -> resetButtonPaint.color = 0xFF32CD32.toInt()  // Vert moyen
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
            LightState.GREEN_LEAVES -> kotlin.math.max(0, 3 - (elapsedTime / 1000))
            LightState.GREEN_FLOWER -> kotlin.math.max(0, 3 - (elapsedTime / 1000))
            LightState.RED -> 0
        }
        
        val mainText = when (lightState) {
            LightState.YELLOW -> "INSPIREZ"
            LightState.GREEN_GROW -> "SOUFFLEZ"
            LightState.GREEN_LEAVES -> "SOUFFLEZ"
            LightState.GREEN_FLOWER -> "SOUFFLEZ"
            LightState.RED -> "↻"
        }
        
        // Ajuster taille du texte selon la lumière
        val textSize = if (lightState == LightState.YELLOW) 180f else 120f
        resetTextPaint.textSize = textSize
        resetTextPaint.color = 0xFF000000.toInt()
        
        // Dessiner texte principal
        canvas.drawText(mainText, lightX, lightY, resetTextPaint)
        
        // Dessiner timer si applicable
        if (lightState != LightState.RED && timeRemaining > 0) {
            resetTextPaint.textSize = textSize * 0.6f
            canvas.drawText(timeRemaining.toString(), lightX, lightY + textSize * 0.8f, resetTextPaint)
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
            // NE PAS démarrer le cycle automatiquement - attendre les permissions
        }
    }
    
    // Fonction pour démarrer le cycle après permissions
    fun startCycle() {
        lightState = LightState.YELLOW
        stateStartTime = System.currentTimeMillis()
        invalidate()
    }
    
    fun updateForce(force: Float) {
        // Gérer le système en 4 étapes
        updateLightState()
        
        // Appliquer le souffle selon l'étape
        when (lightState) {
            LightState.YELLOW -> {
                // Phase d'inspiration - pas de croissance
                return
            }
            LightState.GREEN_GROW -> {
                // Phase souffle - croissance tige (votre logique originale)
                growStem(force)
            }
            LightState.GREEN_LEAVES -> {
                // Phase souffle - croissance feuilles directement
                growLeaves(force)
            }
            LightState.GREEN_FLOWER -> {
                // Phase souffle - croissance fleur seulement
                growFlowerOnly(force)
            }
            LightState.RED -> {
                // Phase finale - admirer
                return
            }
        }
        
        invalidate()
    }
    
    // Votre logique originale pour la tige
    private fun growStem(force: Float) {
        previousForce = currentForce
        currentForce = force
        
        if (force > forceThreshold) {
            val adjustedForce = force - forceThreshold
            currentHeight += adjustedForce * growthRate
            currentHeight = kotlin.math.min(currentHeight, maxHeight)
        }
        
        val rhythmIntensity = kotlin.math.abs(currentForce - previousForce)
        
        // Calculer position actuelle de la tige AVANT de l'utiliser
        val currentY = baseY - currentHeight
        val currentX = baseX + offsetX
        
        if (rhythmIntensity > abruptThreshold) {
            val displacement = if ((0..1).random() == 0) 120f else -120f
            offsetX += displacement
            
            // Créer des bourgeons lors des coups de vent - COLLÉS sur la tige
            if (currentHeight > 100f) {
                val budX = currentX // Position exacte de la tige
                val budY = currentY + ((-20..20).random()) // Légère variation verticale
                bourgeons.add(Bourgeon(budX, budY, 0f)) // Taille 0 au début
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
        
        // currentX et currentY déjà calculés plus haut
        
        if (tracedPath.isNotEmpty()) {
            val lastPoint = tracedPath.last()
            if (currentY < lastPoint.y) {
                tracedPath.add(TracePoint(currentX, currentY, currentStrokeWidth, waveFreq, waveAmp, curvature))
            }
        }
        
        if (!showResetButton && currentHeight > 50f) {
            showResetButton = true
        }
    }
    
    // Croissance des feuilles depuis les bourgeons (comme avant)
    private fun growLeaves(force: Float) {
        if (force > forceThreshold) {
            val adjustedForce = force - forceThreshold
            val growthIncrement = adjustedForce * growthRate * 0.3f
            
            // Faire grandir les bourgeons d'abord
            for (bourgeon in bourgeons) {
                bourgeon.taille += growthIncrement
                bourgeon.taille = kotlin.math.min(bourgeon.taille, 12f) // Réduit de 25px à 12px
            }
            
            // Créer feuilles depuis gros bourgeons
            for (bourgeon in bourgeons) {
                if (bourgeon.taille > 8f) { // Réduit de 15px à 8px
                    var feuille = feuilles.find { it.bourgeon == bourgeon }
                    if (feuille == null) {
                        val angle = (0..360).random().toFloat()
                        feuille = Feuille(bourgeon, 0f, 0f, angle)
                        feuilles.add(feuille)
                    }
                    
                    // Faire grandir la feuille - ENCORE PLUS LENT
                    feuille.longueur += growthIncrement * 0.2f // Encore réduit
                    feuille.largeur += growthIncrement * 0.1f  // Encore réduit
                    feuille.longueur = kotlin.math.min(feuille.longueur, 40f) // Max réduit de 120px à 40px
                    feuille.largeur = kotlin.math.min(feuille.largeur, 20f)   // Max réduit de 60px à 20px
                }
            }
        }
    }
    
    // Croissance fleur seulement (pas de feuilles)
    private fun growFlowerOnly(force: Float) {
        if (force > forceThreshold) {
            val adjustedForce = force - forceThreshold
            val growthIncrement = adjustedForce * growthRate * 0.4f
            
            // Créer/faire grandir la fleur au sommet
            if (tracedPath.isNotEmpty()) {
                val topPoint = tracedPath.minByOrNull { it.y }
                if (topPoint != null) {
                    if (fleur == null) {
                        fleur = Fleur(topPoint.x, topPoint.y, 0f, 5)
                    }
                    fleur?.let {
                        it.taille += growthIncrement * 0.2f // Encore réduit (était 0.5f)
                        it.taille = kotlin.math.min(it.taille, 60f) // Max réduit de 175px à 60px
                        it.petalCount = kotlin.math.max(5, (it.taille * 0.1f).toInt()) // Ajusté
                    }
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
                    lightState = LightState.GREEN_LEAVES
                    stateStartTime = currentTime
                    canGrow = false
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

        // TIGE avec votre image - suivant intelligemment les courbes
        if (::stemBitmap.isInitialized && tracedPath.size > 1) {
            for (i in 1 until tracedPath.size) {
                val prevPoint = tracedPath[i - 1]
                val currentPoint = tracedPath[i]
                
                // Oscillations comme votre tige originale
                val oscillation = kotlin.math.sin(time + currentPoint.y * 0.005f) * 35f
                val adjustedX = currentPoint.x + oscillation
                
                // Calculer l'angle de rotation pour suivre la courbe
                val dx = currentPoint.x - prevPoint.x
                val dy = currentPoint.y - prevPoint.y
                val angle = kotlin.math.atan2(dy.toDouble(), dx.toDouble()) * 180.0 / kotlin.math.PI
                
                // Scaling selon l'épaisseur de la tige (réduit)
                val scale = currentPoint.strokeWidth / 40f // Réduit de 20f à 40f pour images plus petites
                val stemW = stemBitmap.width * scale
                val stemH = stemBitmap.height * scale
                
                canvas.save()
                canvas.translate(adjustedX, currentPoint.y)
                canvas.rotate(angle.toFloat() + 90f) // +90 pour orienter correctement
                canvas.drawBitmap(
                    stemBitmap, 
                    -stemW / 2f, 
                    -stemH / 2f, 
                    null
                )
                canvas.restore()
            }
        }

        val currentY = baseY - currentHeight
        val pointOscillation = kotlin.math.sin(time * 2f) * 15f
        val currentX = baseX + offsetX + pointOscillation

        // Dessiner les bourgeons (petits points bruns sur la tige)
        basePaint.color = 0xFF654321.toInt() // Brun foncé
        basePaint.style = Paint.Style.FILL
        for (bourgeon in bourgeons) {
            if (bourgeon.taille > 0) {
                canvas.drawCircle(bourgeon.x, bourgeon.y, bourgeon.taille, basePaint)
            }
        }

        // FEUILLES avec votre image leaf
        for (feuille in feuilles) {
            if (feuille.longueur > 2 && feuille.largeur > 1 && ::leafBitmap.isInitialized) {
                canvas.save()
                canvas.translate(feuille.bourgeon.x, feuille.bourgeon.y)
                canvas.rotate(feuille.angle)
                
                // Scaling réduit pour feuilles
                val scale = feuille.longueur / 200f // Réduit de 100f à 200f
                val leafW = leafBitmap.width * scale
                val leafH = leafBitmap.height * scale
                
                val dstRect = RectF(-leafW/2, -leafH/2, leafW/2, leafH/2)
                canvas.drawBitmap(leafBitmap, null, dstRect, null)
                canvas.restore()
            }
        }

        // FLEUR avec votre image flower
        fleur?.let { flower ->
            if (flower.taille > 2 && ::flowerBitmap.isInitialized) {
                val scale = flower.taille / 300f // Réduit de 150f à 300f
                val w = flowerBitmap.width * scale
                val h = flowerBitmap.height * scale
                
                val rect = RectF(
                    flower.x - w / 2,
                    flower.y - h / 2,
                    flower.x + w / 2,
                    flower.y + h / 2
                )
                canvas.drawBitmap(flowerBitmap, null, rect, null)
            }
        }

        // Remettre les paramètres par défaut
        basePaint.color = 0xFFFFFFFF.toInt()
        basePaint.style = Paint.Style.STROKE

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
        
        // Redémarrer le cycle complet
        lightState = LightState.YELLOW
        stateStartTime = System.currentTimeMillis()
        canGrow = false
        
        tracedPath.add(TracePoint(baseX, baseY, ba
    }
