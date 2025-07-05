package com.example.souffleforcetest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

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
    
    // NOUVEAU : Paint pour le cercle de test
    private val testCirclePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.BLUE
    }
    
    // ==================== ÉTATS DU SYSTÈME ====================
    
    private var showResetButton = false
    private var resetButtonX = 0f
    private var resetButtonY = 0f
    private val resetButtonRadius = 175f
    
    private var lightState = LightState.YELLOW
    private var stateStartTime = 0L
    
    // NOUVEAU : Variables pour le test de détection
    private var currentForce = 0f
    private var centerX = 0f
    private var centerY = 0f
    
    enum class LightState {
        YELLOW, GREEN_GROW, GREEN_LEAVES, GREEN_FLOWER, RED
    }
    
    // ==================== GESTION DE L'ÉCRAN ====================
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        resetButtonX = w - resetButtonRadius - 50f
        resetButtonY = resetButtonRadius + 80f
        
        // Position du cercle de test au centre
        centerX = w / 2f
        centerY = h / 2f
    }
    
    // ==================== CONTRÔLE DU CYCLE ====================
    
    fun startCycle() {
        lightState = LightState.YELLOW
        stateStartTime = System.currentTimeMillis()
        showResetButton = false
        invalidate()
    }
    
    fun updateForce(force: Float) {
        updateLightState()
        
        // Stocker la force pour le cercle de test
        currentForce = force
        
        // Afficher le bouton reset après les premières détections
        if (!showResetButton && force > 0.01f && 
            (lightState == LightState.GREEN_GROW || lightState == LightState.GREEN_LEAVES || lightState == LightState.GREEN_FLOWER)) {
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
                if (elapsedTime >= 3000) {
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
            LightState.RED -> {
                // Reste en rouge jusqu'au reset
            }
        }
    }
    
    // ==================== AFFICHAGE ====================
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Dessiner le cercle de test SEULEMENT pendant les phases de souffle
        if (lightState == LightState.GREEN_GROW || 
            lightState == LightState.GREEN_LEAVES || 
            lightState == LightState.GREEN_FLOWER) {
            drawTestCircle(canvas)
        }
        
        drawTrafficLight(canvas)
    }
    
    private fun drawTestCircle(canvas: Canvas) {
        // Rayon basé sur la force détectée (minimum 20, maximum 200)
        val baseRadius = 20f
        val maxRadius = 200f
        val currentRadius = baseRadius + (currentForce * (maxRadius - baseRadius))
        
        // Couleur qui varie avec l'intensité
        val intensity = (currentForce * 255).toInt().coerceIn(0, 255)
        testCirclePaint.color = Color.rgb(intensity, 100, 255 - intensity)
        
        // Dessiner le cercle
        canvas.drawCircle(centerX, centerY, currentRadius, testCirclePaint)
        
        // Texte pour afficher la valeur numérique
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 40f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val forceText = String.format("%.3f", currentForce)
        canvas.drawText(forceText, centerX, centerY + 15f, textPaint)
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
        
        // Dessiner l'ombre
        resetButtonPaint.color = 0x40000000.toInt()
        canvas.drawCircle(lightX + 8f, lightY + 8f, lightRadius, resetButtonPaint)
        
        // Couleur selon l'état
        when (lightState) {
            LightState.YELLOW -> resetButtonPaint.color = 0xFFFFD700.toInt()
            LightState.GREEN_GROW -> resetButtonPaint.color = 0xFF2F4F2F.toInt()
            LightState.GREEN_LEAVES -> resetButtonPaint.color = 0xFF00FF00.toInt()
            LightState.GREEN_FLOWER -> resetButtonPaint.color = 0xFFFF69B4.toInt()
            LightState.RED -> resetButtonPaint.color = 0xFFFF0000.toInt()
        }
        canvas.drawCircle(lightX, lightY, lightRadius, resetButtonPaint)
        
        // Bordure
        resetButtonPaint.color = 0xFF333333.toInt()
        resetButtonPaint.style = Paint.Style.STROKE
        resetButtonPaint.strokeWidth = 12f
        canvas.drawCircle(lightX, lightY, lightRadius, resetButtonPaint)
        resetButtonPaint.style = Paint.Style.FILL
        
        // Calcul du temps restant
        val timeRemaining = when (lightState) {
            LightState.YELLOW -> kotlin.math.max(0, 2 - (elapsedTime / 1000))
            LightState.GREEN_GROW -> kotlin.math.max(0, 3 - (elapsedTime / 1000))
            LightState.GREEN_LEAVES -> kotlin.math.max(0, 3 - (elapsedTime / 1000))
            LightState.GREEN_FLOWER -> kotlin.math.max(0, 3 - (elapsedTime / 1000))
            LightState.RED -> 0
        }
        
        val mainText = when (lightState) {
            LightState.YELLOW -> "INSPIREZ"
            LightState.GREEN_GROW -> "SOUFFLEZ (TIGE)"
            LightState.GREEN_LEAVES -> "SOUFFLEZ (FEUILLES)"
            LightState.GREEN_FLOWER -> "SOUFFLEZ (FLEUR)"
            LightState.RED -> "↻"
        }
        
        // Texte sur le cercle
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
        } else {
            // Pour les phases vertes, afficher le texte de façon plus compacte
            resetTextPaint.textAlign = Paint.Align.CENTER
            resetTextPaint.textSize = 60f
            resetTextPaint.color = 0xFF000000.toInt()
            
            when (lightState) {
                LightState.GREEN_GROW -> canvas.drawText("TIGE", lightX, lightY, resetTextPaint)
                LightState.GREEN_LEAVES -> canvas.drawText("FEUILLES", lightX, lightY, resetTextPaint)
                LightState.GREEN_FLOWER -> canvas.drawText("FLEUR", lightX, lightY, resetTextPaint)
                else -> {}
            }
            
            if (timeRemaining > 0) {
                resetTextPaint.textSize = 40f
                canvas.drawText(timeRemaining.toString(), lightX, lightY + 50f, resetTextPaint)
            }
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
        showResetButton = false
        lightState = LightState.YELLOW
        stateStartTime = System.currentTimeMillis()
        currentForce = 0f
        
        invalidate()
    }
}
