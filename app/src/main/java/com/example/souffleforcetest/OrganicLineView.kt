package com.example.souffleforcetest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class OrganicLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    // Renderer pour le dessin
    private val plantRenderer = PlantRenderer(context)
    
    // Chargement des bitmaps (gardés pour compatibilité mais pas utilisés)
    private lateinit var stemBitmap: Bitmap
    private lateinit var leafBitmap: Bitmap
    private lateinit var flowerBitmap: Bitmap
    
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
    
    // Variables de croissance - MODIFIABLES
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
        YELLOW, GREEN_GROW, GREEN_LEAVES, GREEN_FLOWER, RED
    }
    
    data class TracePoint(
        val x: Float, val y: Float, val strokeWidth: Float,
        val waveFrequency: Float, val waveAmplitude: Float, val curvature: Float
    )
    
    data class Bourgeon(val x: Float, val y: Float, var taille: Float)
    data class Feuille(val bourgeon: Bourgeon, var longueur: Float, var largeur: Float, val angle: Float, var maxLargeurAtteinte: Boolean = false)
    data class Fleur(var x: Float, var y: Float, var taille: Float, var petalCount: Int)
    
    private val tracedPath = mutableListOf<TracePoint>()
    private val bourgeons = mutableListOf<Bourgeon>()
    private val feuilles = mutableListOf<Feuille>()
    private var fleur: Fleur? = null
    
    // PARAMÈTRES DE CROISSANCE - MODIFIABLES
    private val forceThreshold = 0.08f
    private val growthRate = 174.6f
    private val baseStrokeWidth = 9.6f
    private val maxStrokeWidth = 25.6f
    private val strokeDecayRate = 0.2f
    private val abruptThreshold = 0.15f
    private val centeringRate = 0.99f
    private val waveThreshold = 0.03f
    private val maxWaveAmplitude = 15f
    
    // NOUVEAUX paramètres pour feuilles réalistes 10x plus grandes
    private val maxLeafWidth = 450f  // Largeur max 10x plus grande
    private val maxLeafLength = 1200f // Longueur max 10x plus grande
    
    // Compteur pour alternance des feuilles
    private var leafSideCounter = 0
    
    init {
        try {
            stemBitmap = BitmapFactory.decodeResource(resources, R.drawable.stem_segment)
            leafBitmap = BitmapFactory.decodeResource(resources, R.drawable.leaf)
            flowerBitmap = BitmapFactory.decodeResource(resources, R.drawable.flower)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
        
        // Dessiner le cercle
        resetButtonPaint.color = 0x40000000.toInt()
        canvas.drawCircle(lightX + 8f, lightY + 8f, lightRadius, resetButtonPaint)
        
        when (lightState) {
            LightState.YELLOW -> resetButtonPaint.color = 0xFFFFD700.toInt()
            LightState.GREEN_GROW -> resetButtonPaint.color = 0xFF2F4F2F.toInt()
            LightState.GREEN_LEAVES -> resetButtonPaint.color = 0xFF00FF00.toInt()
            LightState.GREEN_FLOWER -> resetButtonPaint.color = 0xFFFF69B4.toInt()
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
        
        // Texte sur le cercle (centré)
        if (lightState == LightState.YELLOW) {
            resetTextPaint.textAlign = Paint.Align.CENTER
            resetTextPaint.textSize = 180f
            resetTextPaint.color = 0xFF000000.toInt()
            canvas.drawText(mainText, lightX, lightY, resetTextPaint)
            
            if (timeRemaining > 0) {
                resetTextPaint.textSize = 108f
                canvas.drawText(timeRemaining.toString(), lightX, lightY + 144f, resetTextPaint)
            }
        } else if (lightState == LightState.RED) {
            resetTextPaint.textAlign = Paint.Align.CENTER
            resetTextPaint.textSize = 120f
            resetTextPaint.color = 0xFF000000.toInt()
            canvas.drawText(mainText, lightX, lightY, resetTextPaint)
        }
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        baseX = w / 2.0f
        baseY = h - 100f
        maxHeight = h - 150f
        
        resetButtonX = w - resetButtonRadius - 50f
        resetButtonY = resetButtonRadius + 80f
        
        if (tracedPath.isEmpty()) {
            tracedPath.add(TracePoint(baseX, baseY, baseStrokeWidth, 0f, 0f, 0f))
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
    
    // LOGIQUE DE CROISSANCE avec comportement naturel amélioré
    private fun growStem(force: Float) {
        previousForce = currentForce
        currentForce = force
        
        if (force > forceThreshold) {
            val adjustedForce = force - forceThreshold
            val previousHeight = currentHeight
            currentHeight += adjustedForce * growthRate
            currentHeight = kotlin.math.min(currentHeight, maxHeight)
            
            if (currentHeight > previousHeight && currentHeight > 0) {
                val currentY = baseY - currentHeight
                val currentX = baseX + offsetX
                
                val rhythmIntensity = kotlin.math.abs(currentForce - previousForce)
                
                var waveFreq = 0f
                var waveAmp = 0f
                var curvature = 0f
                
                // Réponse plus naturelle au souffle
                if (rhythmIntensity > 0.01f && rhythmIntensity < abruptThreshold) {
                    waveFreq = (rhythmIntensity * 12f) + 1f
                    waveAmp = (rhythmIntensity * 150f).coerceAtMost(maxWaveAmplitude)
                }
                
                if (rhythmIntensity > 0.04f) {
                    curvature = (rhythmIntensity * 30f).coerceAtMost(15f)
                    if ((0..1).random() == 0) curvature = -curvature
                }
                
                tracedPath.add(TracePoint(currentX, currentY, currentStrokeWidth, waveFreq, waveAmp, curvature))
            }
        }
        
        val rhythmIntensity = kotlin.math.abs(currentForce - previousForce)
        
        if (rhythmIntensity > abruptThreshold) {
            // Déplacements plus naturels et progressifs
            val displacement = if ((0..1).random() == 0) {
                75f + rhythmIntensity * 100f
            } else {
                -(75f + rhythmIntensity * 100f)
            }
            offsetX += displacement
            
            if (currentHeight > 60f && tracedPath.size > 3) {
                val recentPoint = tracedPath[tracedPath.size - (2..5).random()]
                val attachY = recentPoint.y + ((-20..20).random()).toFloat()
                
                // AMÉLIORATION : Alternance systématique droite/gauche pour répartition naturelle
                leafSideCounter++
                val isRightSide = leafSideCounter % 2 == 0
                
                // Espacement plus naturel selon le côté
                val lateralOffset = if (isRightSide) {
                    (15..30).random().toFloat()
                } else {
                    -(15..30).random().toFloat()
                }
                
                val bourgeonnX = recentPoint.x + lateralOffset
                bourgeons.add(Bourgeon(bourgeonnX, attachY, 3f))
            }
        } else if (rhythmIntensity > 0.02f) {
            val thicknessIncrease = rhythmIntensity * 40f
            currentStrokeWidth = kotlin.math.min(maxStrokeWidth, baseStrokeWidth + thicknessIncrease)
        }
        
        if (currentStrokeWidth > baseStrokeWidth) {
            currentStrokeWidth = kotlin.math.max(baseStrokeWidth, currentStrokeWidth - strokeDecayRate)
        }
        
        // Retour au centre plus progressif et naturel
        offsetX *= centeringRate
        
        if (!showResetButton && currentHeight > 30f) {
            showResetButton = true
        }
    }
    
    // NOUVELLE logique de croissance des feuilles en 2 phases avec meilleure répartition
    private fun growLeaves(force: Float) {
        if (force > forceThreshold) {
            val adjustedForce = force - forceThreshold
            val growthIncrement = adjustedForce * growthRate * 0.08f
            
            for (bourgeon in bourgeons) {
                if (bourgeon.taille > 2f) {
                    var feuille = feuilles.find { it.bourgeon == bourgeon }
                    if (feuille == null) {
                        val stemX = tracedPath.minByOrNull { 
                            val dx = it.x - bourgeon.x
                            val dy = it.y - bourgeon.y
                            dx * dx + dy * dy
                        }?.x ?: bourgeon.x
                        
                        val isRightSide = bourgeon.x > stemX
                        
                        // AMÉLIORATION : Angles plus naturels et variés
                        val baseAngle = if (isRightSide) {
                            -25f  // Angle de base pour droite
                        } else {
                            205f  // Angle de base pour gauche
                        }
                        
                        // Variation naturelle selon la hauteur
                        val heightFactor = bourgeon.y / height.toFloat()
                        val heightVariation = (heightFactor - 0.5f) * 30f  // -15° à +15° selon hauteur
                        val randomVariation = ((-15..15).random()).toFloat()
                        
                        val finalAngle = baseAngle + heightVariation + randomVariation
                        
                        feuille = Feuille(bourgeon, 0f, 0f, finalAngle, false)
                        feuilles.add(feuille)
                    }
                    
                    // PHASE 1 : Croissance largeur ET longueur jusqu'à largeur max
                    if (!feuille.maxLargeurAtteinte) {
                        val lengthGrowth = growthIncrement * 0.3f
                        val widthGrowth = growthIncrement * 0.35f  // Croît un peu plus vite en largeur
                        
                        feuille.longueur += lengthGrowth
                        feuille.largeur += widthGrowth
                        
                        // Vérifier si largeur max atteinte
                        if (feuille.largeur >= maxLeafWidth) {
                            feuille.largeur = maxLeafWidth
                            feuille.maxLargeurAtteinte = true
                        }
                        
                        // Limites pour phase 1 (10x plus grandes)
                        feuille.longueur = kotlin.math.min(feuille.longueur, 600f)
                    } 
                    // PHASE 2 : Seulement croissance en longueur
                    else {
                        val lengthGrowth = growthIncrement * 0.5f  // Plus rapide maintenant
                        feuille.longueur += lengthGrowth
                        feuille.longueur = kotlin.math.min(feuille.longueur, maxLeafLength)
                        // Largeur reste fixe à maxLeafWidth
                    }
                }
            }
        }
    }
    
    private fun growFlowerOnly(force: Float) {
        if (force > forceThreshold) {
            val adjustedForce = force - forceThreshold
            val growthIncrement = adjustedForce * growthRate * 0.08f
            
            if (tracedPath.isNotEmpty()) {
                val topPoint = tracedPath.last()
                if (fleur == null) {
                    fleur = Fleur(topPoint.x, topPoint.y, 0f, 6)
                }
                fleur?.let {
                    it.taille += growthIncrement * 0.15f
                    it.taille = kotlin.math.min(it.taille, 175f)
                    it.petalCount = kotlin.math.max(5, (it.taille * 0.05f).toInt())
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
        
        // Tige réaliste
        plantRenderer.drawRealisticStem(canvas, tracedPath, time, baseStrokeWidth, maxStrokeWidth)
        
        if (tracedPath.isNotEmpty()) {
            plantRenderer.drawGrowthPoint(canvas, tracedPath.last(), time)
        }
        
        plantRenderer.drawAttachmentPoints(canvas, bourgeons, time)
        
        // Feuilles 3D réalistes (géométriques)
        plantRenderer.drawRealistic3DLeaves(canvas, feuilles, time)
        
        // Fleur géométrique réaliste
        plantRenderer.drawRealisticFlower(canvas, fleur, time)
        
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
        leafSideCounter = 0  // Reset du compteur d'alternance
        
        lightState = LightState.YELLOW
        stateStartTime = System.currentTimeMillis()
        canGrow = false
        
        tracedPath.add(TracePoint(baseX, baseY, baseStrokeWidth, 0f, 0f, 0f))
        invalidate()
    }
}
