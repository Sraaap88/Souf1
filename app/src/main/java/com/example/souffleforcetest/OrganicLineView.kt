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
    
    // ==================== COMPOSANTS ====================
    
    // Renderer pour le dessin
    private val plantRenderer = PlantRenderer(context)
    
    // Logique de croissance séparée
    private val growthLogic = PlantGrowthLogic()
    
    // Chargement des bitmaps (gardés pour compatibilité)
    private lateinit var stemBitmap: Bitmap
    private lateinit var leafBitmap: Bitmap
    private lateinit var flowerBitmap: Bitmap
    
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
    
    // ==================== ÉTATS DU SYSTÈME ====================
    
    private var showResetButton = false
    private var resetButtonX = 0f
    private var resetButtonY = 0f
    private val resetButtonRadius = 175f
    
    private var lightState = LightState.YELLOW
    private var stateStartTime = 0L
    private var canGrow = false
    
    enum class LightState {
        YELLOW, GREEN_GROW, GREEN_LEAVES, GREEN_FLOWER, RED
    }
    
    // ==================== INITIALISATION ====================
    
    init {
        try {
            stemBitmap = BitmapFactory.decodeResource(resources, R.drawable.stem_segment)
            leafBitmap = BitmapFactory.decodeResource(resources, R.drawable.leaf)
            flowerBitmap = BitmapFactory.decodeResource(resources, R.drawable.flower)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // ==================== GESTION DE L'ÉCRAN ====================
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        resetButtonX = w - resetButtonRadius - 50f
        resetButtonY = resetButtonRadius + 80f
        
        // Informer la logique de croissance des nouvelles dimensions
        growthLogic.updateScreenSize(w, h)
    }
    
    // ==================== CONTRÔLE DU CYCLE ====================
    
    fun startCycle() {
        lightState = LightState.YELLOW
        stateStartTime = System.currentTimeMillis()
        invalidate()
    }
    
    fun updateForce(force: Float) {
        updateLightState()
        
        // CORRIGÉ : Déléguer TOUJOURS la mise à jour de force
        when (lightState) {
            LightState.YELLOW -> return
            LightState.GREEN_GROW -> growthLogic.updateForce(force, "GREEN_GROW")
            LightState.GREEN_LEAVES -> growthLogic.updateForce(force, "GREEN_LEAVES")
            LightState.GREEN_FLOWER -> growthLogic.updateForce(force, "GREEN_FLOWER")
            LightState.RED -> return
        }
        
        // Mettre à jour l'état du bouton reset
        if (!showResetButton && growthLogic.hasVisibleGrowth()) {
            showResetButton = true
        }
        
        invalidate()
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
                    // CORRIGÉ : On garde canGrow = true pour les feuilles
                    canGrow = true
                }
            }
            LightState.GREEN_LEAVES -> {
                // CORRIGÉ : canGrow reste true pour faire pousser les feuilles
                canGrow = true
                if (elapsedTime >= 3000) {
                    lightState = LightState.GREEN_FLOWER
                    stateStartTime = currentTime
                }
            }
            LightState.GREEN_FLOWER -> {
                // CORRIGÉ : canGrow reste true pour faire pousser les fleurs
                canGrow = true
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
    
    // ==================== AFFICHAGE ====================
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val time = System.currentTimeMillis() * 0.002f
        
        // Déléguer le dessin de la plante à la logique de croissance
        growthLogic.drawPlant(canvas, plantRenderer, time)
        
        drawTrafficLight(canvas)
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
    
    // ==================== GESTION DES ÉVÉNEMENTS ====================
    
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
        // Déléguer le reset à la logique de croissance
        growthLogic.resetPlant()
        
        showResetButton = false
        lightState = LightState.YELLOW
        stateStartTime = System.currentTimeMillis()
        canGrow = false
        
        invalidate()
    }
}
