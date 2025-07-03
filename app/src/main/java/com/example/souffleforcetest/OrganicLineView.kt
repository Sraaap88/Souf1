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
    
    private val stemPaint = Paint().apply {
        color = 0xFF8B7355.toInt() // Couleur tronc
        strokeWidth = 8f
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
    data class Fleur(var x: Float, var y: Float, var taille: Float, var petalCount: Int)
    
    private val tracedPath = mutableListOf<TracePoint>()
    private val bourgeons = mutableListOf<Bourgeon>()
    private val feuilles = mutableListOf<Feuille>()
    private var fleur: Fleur? = null
    
    private val forceThreshold = 0.08f
    private val growthRate = 174.6f
    private val baseStrokeWidth = 8f
    private val maxStrokeWidth = 24f
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
        baseY = h - 100f
        maxHeight = h - 150f
        
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
        
        // Gestion des mouvements brusques - créer des points d'attache pour futures feuilles
        if (rhythmIntensity > abruptThreshold) {
            val displacement = if ((0..1).random() == 0) 60f else -60f
            offsetX += displacement
            
            // Créer des points d'attache (petits bourgeons) pendant la croissance
            if (currentHeight > 80f && tracedPath.size > 3) {
                val recentPoint = tracedPath[tracedPath.size - (2..4).random()]
                val attachX = recentPoint.x + ((-25..25).random()).toFloat()
                val attachY = recentPoint.y + ((-15..15).random()).toFloat()
                bourgeons.add(Bourgeon(attachX, attachY, 3f)) // Petit point d'attache
            }
        } else if (rhythmIntensity > 0.02f) {
            // Augmenter l'épaisseur
            val thicknessIncrease = rhythmIntensity * 50f
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
            val growthIncrement = adjustedForce * growthRate * 0.05f
            
            // Créer des feuilles à partir des points d'attache
            for (bourgeon in bourgeons) {
                if (bourgeon.taille > 2f) { // Point d'attache mature
                    var feuille = feuilles.find { it.bourgeon == bourgeon }
                    if (feuille == null) {
                        // Calculer l'angle du pétiole vers la tige la plus proche
                        val stemX = tracedPath.minByOrNull { 
                            val dx = it.x - bourgeon.x
                            val dy = it.y - bourgeon.y
                            dx * dx + dy * dy
                        }?.x ?: bourgeon.x
                        
                        // Angle pour que le pétiole pointe vers la tige
                        val angleToStem = kotlin.math.atan2(
                            (stemX - bourgeon.x).toDouble(),
                            (0f - 0f).toDouble() // Vertical reference
                        ) * 180.0 / kotlin.math.PI
                        
                        feuille = Feuille(bourgeon, 0f, 0f, angleToStem.toFloat())
                        feuilles.add(feuille)
                    }
                    
                    // Faire grandir la feuille
                    feuille.longueur += growthIncrement * 0.15f
                    feuille.largeur += growthIncrement * 0.08f
                    feuille.longueur = kotlin.math.min(feuille.longueur, 80f)
                    feuille.largeur = kotlin.math.min(feuille.largeur, 40f)
                }
            }
        }
    }
    
    private fun growFlowerOnly(force: Float) {
        if (force > forceThreshold) {
            val adjustedForce = force - forceThreshold
            val growthIncrement = adjustedForce * growthRate * 0.08f // Plus lent : 0.15f -> 0.08f
            
            if (tracedPath.isNotEmpty()) {
                val topPoint = tracedPath.last()
                if (fleur == null) {
                    fleur = Fleur(topPoint.x, topPoint.y, 0f, 5)
                }
                fleur?.let {
                    it.taille += growthIncrement * 0.15f // Plus lent : 0.2f -> 0.15f
                    it.taille = kotlin.math.min(it.taille, 175f)
                    it.petalCount = kotlin.math.max(5, (it.taille * 0.05f).toInt())
                    // Mettre à jour la position de la fleur au sommet
                    it.x = topPoint.x
                    it.y = topPoint.y
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

        // TIGE LIGNE - comme un tronc qui suit le tracé
        if (tracedPath.size > 1) {
            val path = Path()
            
            // Commencer le chemin
            val firstPoint = tracedPath[0]
            path.moveTo(firstPoint.x, firstPoint.y)
            
            // Dessiner la ligne qui suit tous les points du tracé
            for (i in 1 until tracedPath.size) {
                val point = tracedPath[i]
                val prevPoint = tracedPath[i-1]
                
                // Ajouter de l'oscillation dynamique
                val oscillation = kotlin.math.sin(time + point.y * 0.01f) * 2f
                val adjustedX = point.x + oscillation
                
                // Ligne lisse vers le point suivant
                path.lineTo(adjustedX, point.y)
            }
            
            // Dessiner le chemin avec une apparence de tronc
            for (i in tracedPath.indices) {
                val point = tracedPath[i]
                val thickness = lerp(maxStrokeWidth, baseStrokeWidth, i.toFloat() / tracedPath.size.toFloat())
                stemPaint.strokeWidth = thickness
                
                if (i < tracedPath.size - 1) {
                    val nextPoint = tracedPath[i + 1]
                    val oscillation1 = kotlin.math.sin(time + point.y * 0.01f) * 2f
                    val oscillation2 = kotlin.math.sin(time + nextPoint.y * 0.01f) * 2f
                    
                    canvas.drawLine(
                        point.x + oscillation1, point.y,
                        nextPoint.x + oscillation2, nextPoint.y,
                        stemPaint
                    )
                }
            }
        }

        // Point de croissance visible (sommet de la tige)
        if (tracedPath.isNotEmpty()) {
            val topPoint = tracedPath.last()
            val pointOscillation = kotlin.math.sin(time * 2f) * 3f
            
            basePaint.color = 0xFF90EE90.toInt()
            basePaint.style = Paint.Style.FILL
            canvas.drawCircle(topPoint.x + pointOscillation, topPoint.y, 4f, basePaint)
        }

        // Points d'attache (petits bourgeons bruns)
        basePaint.color = 0xFF8B4513.toInt()
        basePaint.style = Paint.Style.FILL
        for (bourgeon in bourgeons) {
            if (bourgeon.taille > 1f) {
                val oscillation = kotlin.math.sin(time + bourgeon.y * 0.01f) * 1f
                canvas.drawCircle(bourgeon.x + oscillation, bourgeon.y, 2f, basePaint) // Petit point discret
            }
        }

        // FEUILLES - attachées par le pétiole
        for (feuille in feuilles) {
            if (feuille.longueur > 5 && ::leafBitmap.isInitialized) {
                val leafOscillation = kotlin.math.sin(time * 1.5f + feuille.bourgeon.y * 0.01f) * 8f
                
                // Calculer la position de la feuille : décalée pour que le pétiole touche le point d'attache
                val scale = kotlin.math.min(feuille.longueur / 400f, 0.055f)
                val leafW = leafBitmap.width.toFloat() * scale
                val petioleOffset = leafW * 0.3f // Distance du pétiole depuis le centre de l'image
                
                // Position de la feuille décalée
                val leafX = feuille.bourgeon.x + leafOscillation + kotlin.math.cos(kotlin.math.toRadians(feuille.angle.toDouble())).toFloat() * petioleOffset
                val leafY = feuille.bourgeon.y + kotlin.math.sin(kotlin.math.toRadians(feuille.angle.toDouble())).toFloat() * petioleOffset
                
                canvas.save()
                canvas.translate(leafX, leafY)
                canvas.rotate(feuille.angle + leafOscillation * 0.5f)
                
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

        // FLEUR - au sommet de la tige (opaque)
        fleur?.let { flower ->
            if (flower.taille > 5f && ::flowerBitmap.isInitialized) {
                val flowerOscillation = kotlin.math.sin(time * 0.8f) * 5f
                
                val progressRatio = flower.taille / 175f
                val scale = progressRatio * 1.2f
                val w = flowerBitmap.width.toFloat() * scale
                val h = flowerBitmap.height.toFloat() * scale
                
                val maxSize = 500f
                val finalW = kotlin.math.min(w, maxSize)
                val finalH = kotlin.math.min(h, maxSize)
                
                val paint = Paint().apply {
                    isAntiAlias = true
                    isFilterBitmap = true
                    alpha = 255 // Complètement opaque
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
