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
    
    // Chargement des bitmaps
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
    
    // États du système
    private var lightState = LightState.YELLOW
    private var stateStartTime = 0L
    private var canGrow = false
    
    enum class LightState {
        YELLOW,
        GREEN_GROW,
        GREEN_LEAVES,
        GREEN_FLOWER,
        RED
    }
    
    private data class TracePoint(
        val x: Float,
        val y: Float,
        val strokeWidth: Float,
        val waveFrequency: Float,
        val waveAmplitude: Float,
        val curvature: Float
    )
    
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
    
    init {
        try {
            stemBitmap = BitmapFactory.decodeResource(resources, R.drawable.stem_segment)
            leafBitmap = BitmapFactory.decodeResource(resources, R.drawable.leaf)
            flowerBitmap = BitmapFactory.decodeResource(resources, R.drawable.flower)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // Fonction utilitaire pour interpolation linéaire
    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + fraction * (end - start)
    }
    
    private fun drawTrafficLight(canvas: Canvas) {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - stateStartTime
        
        val lightRadius = when (lightState) {
            LightState.YELLOW -> width * 0.4f
            else -> resetButtonRadius
        }
        
        val lightX = if (lightState == LightState.YELLOW) width / 2f else resetButtonX
        val lightY = if (lightState == LightState.YELLOW) height / 2f else resetButtonY
        
        resetButtonPaint.color = 0x40000000.toInt()
        canvas.drawCircle(lightX + 8f, lightY + 8f, lightRadius, resetButtonPaint)
        
        when (lightState) {
            LightState.YELLOW -> resetButtonPaint.color = 0xFFFFD700.toInt()
            LightState.GREEN_GROW -> resetButtonPaint.color = 0xFF00FF00.toInt()
            LightState.GREEN_LEAVES -> resetButtonPaint.color = 0xFF228B22.toInt()
            LightState.GREEN_FLOWER -> resetButtonPaint.color = 0xFF32CD32.toInt()
            LightState.RED -> resetButtonPaint.color = 0xFFFF0000.toInt()
        }
        canvas.drawCircle(lightX, lightY, lightRadius, resetButtonPaint)
        
        resetButtonPaint.color = 0xFF333333.toInt()
        resetButtonPaint.style = Paint.Style.STROKE
        resetButtonPaint.strokeWidth = 12f
        canvas.drawCircle(lightX, lightY, lightRadius, resetButtonPaint)
        resetButtonPaint.style = Paint.Style.FILL
        
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
        
        val textSize = if (lightState == LightState.YELLOW) 180f else 120f
        resetTextPaint.textSize = textSize
        resetTextPaint.color = 0xFF000000.toInt()
        
        canvas.drawText(mainText, lightX, lightY, resetTextPaint)
        
        if (lightState != LightState.RED && timeRemaining > 0) {
            resetTextPaint.textSize = textSize * 0.6f
            canvas.drawText(timeRemaining.toString(), lightX, lightY + textSize * 0.8f, resetTextPaint)
        }
        
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
        }
    }
    
    fun startCycle() {
        lightState = LightState.YELLOW
        stateStartTime = System.currentTimeMillis()
        invalidate()
    }
    
    fun updateForce(force: Float) {
        updateLightState()
        
        when (lightState) {
            LightState.YELLOW -> return
            LightState.GREEN_GROW -> growStem(force)
            LightState.GREEN_LEAVES -> growLeaves(force)
            LightState.GREEN_FLOWER -> growFlowerOnly(force)
            LightState.RED -> return
        }
        
        invalidate()
    }
    
    private fun growStem(force: Float) {
        previousForce = currentForce
        currentForce = force
        
        if (force > forceThreshold) {
            val adjustedForce = force - forceThreshold
            val previousHeight = currentHeight
            currentHeight += adjustedForce * growthRate
            currentHeight = kotlin.math.min(currentHeight, maxHeight)
            
            // Ajouter un point de tracé chaque fois que la tige pousse
            if (currentHeight > previousHeight && currentHeight > 0) {
                val currentY = baseY - currentHeight
                val currentX = baseX + offsetX
                
                // Calculer les paramètres d'oscillation
                val rhythmIntensity = kotlin.math.abs(currentForce - previousForce)
                
                var waveFreq = 0f
                var waveAmp = 0f
                var curvature = 0f
                
                if (rhythmIntensity > 0.01f && rhythmIntensity < abruptThreshold) {
                    waveFreq = (rhythmIntensity * 15f) + 1f
                    waveAmp = (rhythmIntensity * 200f).coerceAtMost(maxWaveAmplitude)
                }
                
                if (rhythmIntensity > 0.05f) {
                    curvature = (rhythmIntensity * 40f).coerceAtMost(20f)
                    if ((0..1).random() == 0) curvature = -curvature
                }
                
                // Ajouter le point de tracé
                tracedPath.add(TracePoint(currentX, currentY, currentStrokeWidth, waveFreq, waveAmp, curvature))
            }
        }
        
        val rhythmIntensity = kotlin.math.abs(currentForce - previousForce)
        
        // Gestion des mouvements brusques
        if (rhythmIntensity > abruptThreshold) {
            val displacement = if ((0..1).random() == 0) 30f else -30f
            offsetX += displacement
            
            // Créer des bourgeons si la tige est assez haute
            if (currentHeight > 80f) {
                val currentY = baseY - currentHeight
                val currentX = baseX + offsetX
                val budX = currentX + ((-15..15).random()).toFloat()
                val budY = currentY + ((-20..20).random()).toFloat()
                bourgeons.add(Bourgeon(budX, budY, 0f))
            }
        } else if (rhythmIntensity > 0.02f) {
            // Augmenter l'épaisseur
            val thicknessIncrease = rhythmIntensity * 80f
            currentStrokeWidth = kotlin.math.min(maxStrokeWidth, baseStrokeWidth + thicknessIncrease)
        }
        
        // Réduction progressive de l'épaisseur
        if (currentStrokeWidth > baseStrokeWidth) {
            currentStrokeWidth = kotlin.math.max(baseStrokeWidth, currentStrokeWidth - strokeDecayRate)
        }
        
        // Recentrage progressif
        offsetX *= centeringRate
        
        // Afficher le bouton reset quand la tige commence à pousser
        if (!showResetButton && currentHeight > 30f) {
            showResetButton = true
        }
    }
    
    private fun growLeaves(force: Float) {
        if (force > forceThreshold) {
            val adjustedForce = force - forceThreshold
            val growthIncrement = adjustedForce * growthRate * 0.3f
            
            for (bourgeon in bourgeons) {
                bourgeon.taille += growthIncrement
                bourgeon.taille = kotlin.math.min(bourgeon.taille, 25f)
            }
            
            for (bourgeon in bourgeons) {
                if (bourgeon.taille > 15f) {
                    var feuille = feuilles.find { it.bourgeon == bourgeon }
                    if (feuille == null) {
                        val angle = (0..360).random().toFloat()
                        feuille = Feuille(bourgeon, 0f, 0f, angle)
                        feuilles.add(feuille)
                    }
                    
                    feuille.longueur += growthIncrement * 0.6f
                    feuille.largeur += growthIncrement * 0.3f
                    feuille.longueur = kotlin.math.min(feuille.longueur, 120f)
                    feuille.largeur = kotlin.math.min(feuille.largeur, 60f)
                }
            }
        }
    }
    
    private fun growFlowerOnly(force: Float) {
        if (force > forceThreshold) {
            val adjustedForce = force - forceThreshold
            val growthIncrement = adjustedForce * growthRate * 0.4f
            
            if (tracedPath.isNotEmpty()) {
                val topPoint = tracedPath.minByOrNull { it.y }
                if (topPoint != null) {
                    if (fleur == null) {
                        fleur = Fleur(topPoint.x, topPoint.y, 0f, 5)
                    }
                    fleur?.let {
                        it.taille += growthIncrement * 0.5f
                        it.taille = kotlin.math.min(it.taille, 175f)
                        it.petalCount = kotlin.math.max(5, (it.taille * 0.05f).toInt())
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

        // TIGE - plus visible et plus grosse
        if (currentHeight > 0 && ::stemBitmap.isInitialized) {
            val stemTop = baseY - currentHeight
            val currentX = baseX + offsetX
            
            // Segments plus rapprochés pour une tige continue
            val segmentHeight = 20f
            val totalSegments = (currentHeight / segmentHeight).toInt() + 2
            
            for (i in 0 until totalSegments) {
                val segmentY = baseY - (i.toFloat() * segmentHeight)
                
                // Dessiner tous les segments jusqu'au sommet
                if (segmentY >= stemTop - segmentHeight) {
                    val oscillation = kotlin.math.sin(time + segmentY * 0.005f) * 3f
                    val adjustedX = currentX + oscillation
                    
                    // Tige plus grosse et plus visible
                    val positionRatio = if (currentHeight > 0) (baseY - segmentY) / currentHeight else 0f
                    val thickness = baseStrokeWidth + (currentStrokeWidth - baseStrokeWidth) * (1f - positionRatio)
                    val scale = (thickness / 30f).coerceIn(0.15f, 0.4f) // Plus grosse !
                    
                    val stemW = stemBitmap.width.toFloat() * scale
                    val stemH = stemBitmap.height.toFloat() * scale
                    
                    canvas.save()
                    canvas.translate(adjustedX, segmentY)
                    
                    // Rotation plus subtile
                    val rotation = (offsetX / 100f).coerceIn(-10f, 10f)
                    canvas.rotate(rotation)
                    
                    val paint = Paint().apply {
                        isAntiAlias = true
                        alpha = 255 // Complètement opaque
                        isFilterBitmap = true
                    }
                    
                    canvas.drawBitmap(stemBitmap, -stemW / 2f, -stemH / 2f, paint)
                    canvas.restore()
                }
            }
        }

        // Point de croissance visible (sommet de la tige)
        if (currentHeight > 0) {
            val stemTop = baseY - currentHeight
            val currentX = baseX + offsetX
            val pointOscillation = kotlin.math.sin(time * 2f) * 3f
            
            // Dessiner un petit point vert au sommet
            basePaint.color = 0xFF90EE90.toInt()
            basePaint.style = Paint.Style.FILL
            canvas.drawCircle(currentX + pointOscillation, stemTop, 4f, basePaint)
        }

        // Bourgeons
        basePaint.color = 0xFF8B4513.toInt()
        basePaint.style = Paint.Style.FILL
        for (bourgeon in bourgeons) {
            if (bourgeon.taille > 2) {
                val oscillation = kotlin.math.sin(time + bourgeon.y * 0.01f) * 2f
                canvas.drawCircle(bourgeon.x + oscillation, bourgeon.y, bourgeon.taille * 0.5f, basePaint)
            }
        }

        // FEUILLES - 30% plus petites
        for (feuille in feuilles) {
            if (feuille.longueur > 10 && ::leafBitmap.isInitialized) {
                val leafOscillation = kotlin.math.sin(time * 1.5f + feuille.bourgeon.y * 0.01f) * 8f
                
                canvas.save()
                canvas.translate(feuille.bourgeon.x + leafOscillation, feuille.bourgeon.y)
                canvas.rotate(feuille.angle + leafOscillation * 0.5f)
                
                // 30% plus petites : 0.12f devient 0.08f
                val scale = kotlin.math.min(feuille.longueur / 400f, 0.08f)
                val leafW = leafBitmap.width.toFloat() * scale
                val leafH = leafBitmap.height.toFloat() * scale
                
                val paint = Paint().apply {
                    isAntiAlias = true
                    isFilterBitmap = true
                    alpha = 220
                }
                
                val dstRect = RectF(-leafW/2, -leafH/2, leafW/2, leafH/2)
                canvas.drawBitmap(leafBitmap, null, dstRect, paint)
                canvas.restore()
            }
        }

        // FLEUR - 2 fois plus grosse
        fleur?.let { flower ->
            if (flower.taille > 10 && ::flowerBitmap.isInitialized) {
                val flowerOscillation = kotlin.math.sin(time * 0.8f) * 5f
                
                // 2 fois plus grosse : 0.6f devient 1.2f
                val scale = kotlin.math.min(flower.taille / 100f, 1.2f)
                val w = flowerBitmap.width.toFloat() * scale
                val h = flowerBitmap.height.toFloat() * scale
                
                // Taille max plus grande aussi : 250f devient 500f
                val maxSize = 500f
                val finalW = kotlin.math.min(w, maxSize)
                val finalH = kotlin.math.min(h, maxSize)
                
                val paint = Paint().apply {
                    isAntiAlias = true
                    isFilterBitmap = true
                    alpha = 240
                }
                
                val rect = RectF(
                    flower.x - finalW / 2 + flowerOscillation,
                    flower.y - finalH / 2,
                    flower.x + finalW / 2 + flowerOscillation,
                    flower.y + finalH / 2
                )
                canvas.drawBitmap(flowerBitmap, null, rect, paint)
            }
        }

        // Reset du style pour le feu de circulation
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
        
        lightState = LightState.YELLOW
        stateStartTime = System.currentTimeMillis()
        canGrow = false
        
        tracedPath.add(TracePoint(baseX, baseY, baseStrokeWidth, 0f, 0f, 0f))
        invalidate()
    }
}
