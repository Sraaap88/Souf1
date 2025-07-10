package com.example.souffleforcetest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import kotlin.math.*

class UIDrawingManager(private val context: Context, private val screenWidth: Int, private val screenHeight: Int) {
    
    // ==================== UI PAINTS ====================
    
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
        color = Color.rgb(50, 120, 50)
    }
    
    private val branchPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.rgb(40, 100, 40)
    }
    
    private val leafPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.rgb(34, 139, 34)
    }
    
    private val flowerPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.WHITE
    }
    
    private val flowerCenterPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.rgb(255, 200, 50)
    }
    
    // ==================== IMAGE RESOURCES ====================
    
    private var daisyBitmap: Bitmap? = null
    
    init {
        // Charger l'image de marguerite
        try {
            daisyBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.marguerite)
        } catch (e: Exception) {
            // Si l'image n'est pas trouvée, on garde daisyBitmap = null
        }
    }
    
    // ==================== FONCTION PRINCIPALE D'AFFICHAGE ====================
    
    fun drawCurrentState(canvas: Canvas, lightState: OrganicLineView.LightState, timeRemaining: Long, 
                        resetButtonX: Float, resetButtonY: Float, resetButtonRadius: Float) {
        when (lightState) {
            OrganicLineView.LightState.START -> {
                drawStartButtons(canvas)
            }
            OrganicLineView.LightState.FLOWER_CHOICE -> {
                drawFlowerChoice(canvas)
            }
            OrganicLineView.LightState.YELLOW -> {
                drawInspirePhase(canvas, timeRemaining)
            }
            OrganicLineView.LightState.RED -> {
                drawResetButton(canvas, resetButtonX, resetButtonY, resetButtonRadius)
            }
            else -> {
                drawGreenPhases(canvas, lightState, timeRemaining, resetButtonX, resetButtonY, resetButtonRadius)
            }
        }
    }
    
    // ==================== FONCTIONS D'AFFICHAGE SPÉCIFIQUES ====================
    
    private fun drawStartButtons(canvas: Canvas) {
        // Calculer positions des deux boutons - VRAIMENT CENTRER L'ENSEMBLE
        val buttonRadius = screenWidth * 0.15f
        val spacing = buttonRadius * 2.5f
        val centerX = screenWidth / 2f
        val zenButtonX = centerX - spacing / 2f
        val defiButtonX = centerX + spacing / 2f
        val buttonY = screenHeight / 2f
        
        // Dessiner bouton ZEN (bleu marine pour meilleur contraste)
        drawSingleButton(canvas, zenButtonX, buttonY, buttonRadius, 0xFF1E3A8A.toInt(), "ZEN")
        
        // Dessiner bouton DÉFI (orange feu)
        drawSingleButton(canvas, defiButtonX, buttonY, buttonRadius, 0xFFFF4500.toInt(), "DÉFI")
    }
    
    private fun drawFlowerChoice(canvas: Canvas) {
        // Titre
        resetTextPaint.textAlign = Paint.Align.CENTER
        resetTextPaint.textSize = 150f
        resetTextPaint.color = 0xFFFFFFFF.toInt()
        resetTextPaint.isFakeBoldText = true
        canvas.drawText("CHOISIR FLEUR", screenWidth / 2f, screenHeight * 0.25f, resetTextPaint)
        
        // Position du bouton marguerite (juste l'image, pas de cercle)
        val flowerButtonX = screenWidth / 2f
        val flowerButtonY = screenHeight / 2f
        val flowerButtonRadius = screenWidth * 0.2f
        
        // Dessiner SEULEMENT l'image de marguerite (pas de cercle)
        drawMiniDaisy(canvas, flowerButtonX, flowerButtonY, flowerButtonRadius * 1.5f)
        
        // Nom de la fleur en dessous
        resetTextPaint.textSize = 60f
        resetTextPaint.isFakeBoldText = false
        canvas.drawText("MARGUERITE", flowerButtonX, flowerButtonY + flowerButtonRadius + 80f, resetTextPaint)
    }
    
    private fun drawInspirePhase(canvas: Canvas, timeRemaining: Long) {
        // Pas de cercle, juste le texte au centre en blanc
        resetTextPaint.textAlign = Paint.Align.CENTER
        resetTextPaint.textSize = 180f
        resetTextPaint.color = 0xFFFFFFFF.toInt()
        resetTextPaint.isFakeBoldText = false
        canvas.drawText("INSPIREZ", screenWidth / 2f, screenHeight / 2f, resetTextPaint)
        
        if (timeRemaining > 0) {
            resetTextPaint.textSize = 108f
            canvas.drawText(timeRemaining.toString(), screenWidth / 2f, screenHeight / 2f + 144f, resetTextPaint)
        }
    }
    
    private fun drawResetButton(canvas: Canvas, lightX: Float, lightY: Float, lightRadius: Float) {
        // Ombre
        resetButtonPaint.color = 0x40000000.toInt()
        canvas.drawCircle(lightX + 8f, lightY + 8f, lightRadius, resetButtonPaint)
        
        // Bouton rouge
        resetButtonPaint.color = 0xFFFF0000.toInt()
        canvas.drawCircle(lightX, lightY, lightRadius, resetButtonPaint)
        
        // Bordure
        resetButtonPaint.color = 0xFF333333.toInt()
        resetButtonPaint.style = Paint.Style.STROKE
        resetButtonPaint.strokeWidth = 12f
        canvas.drawCircle(lightX, lightY, lightRadius, resetButtonPaint)
        resetButtonPaint.style = Paint.Style.FILL
        
        // Texte reset - 1/4 de hauteur plus bas
        resetTextPaint.textAlign = Paint.Align.CENTER
        resetTextPaint.textSize = 120f
        resetTextPaint.color = 0xFF000000.toInt()
        resetTextPaint.isFakeBoldText = false
        canvas.drawText("↻", lightX, lightY + 30f, resetTextPaint)
    }
    
    private fun drawGreenPhases(canvas: Canvas, lightState: OrganicLineView.LightState, timeRemaining: Long,
                               lightX: Float, lightY: Float, lightRadius: Float) {
        // Ombre
        resetButtonPaint.color = 0x40000000.toInt()
        canvas.drawCircle(lightX + 8f, lightY + 8f, lightRadius, resetButtonPaint)
        
        // Couleur selon l'état
        resetButtonPaint.color = when (lightState) {
            OrganicLineView.LightState.GREEN_GROW -> 0xFF2F4F2F.toInt()
            OrganicLineView.LightState.GREEN_LEAVES -> 0xFF00FF00.toInt()
            OrganicLineView.LightState.GREEN_FLOWER -> 0xFFFF69B4.toInt()
            else -> 0xFF00AA00.toInt()
        }
        canvas.drawCircle(lightX, lightY, lightRadius, resetButtonPaint)
        
        // Bordure
        resetButtonPaint.color = 0xFF333333.toInt()
        resetButtonPaint.style = Paint.Style.STROKE
        resetButtonPaint.strokeWidth = 12f
        canvas.drawCircle(lightX, lightY, lightRadius, resetButtonPaint)
        resetButtonPaint.style = Paint.Style.FILL
        
        // Texte pour les phases vertes - plus grand et en gras
        resetTextPaint.textAlign = Paint.Align.CENTER
        resetTextPaint.textSize = 80f
        resetTextPaint.color = 0xFF000000.toInt()
        resetTextPaint.isFakeBoldText = true
        
        val phaseText = when (lightState) {
            OrganicLineView.LightState.GREEN_GROW -> "TIGE"
            OrganicLineView.LightState.GREEN_LEAVES -> "FEUILLES"
            OrganicLineView.LightState.GREEN_FLOWER -> "FLEUR"
            else -> ""
        }
        
        canvas.drawText(phaseText, lightX, lightY, resetTextPaint)
        
        if (timeRemaining > 0) {
            resetTextPaint.textSize = 50f
            resetTextPaint.isFakeBoldText = false
            canvas.drawText(timeRemaining.toString(), lightX, lightY + 60f, resetTextPaint)
        }
    }
    
    private fun drawSingleButton(canvas: Canvas, x: Float, y: Float, radius: Float, color: Int, text: String) {
        // Ombre
        resetButtonPaint.color = 0x40000000.toInt()
        canvas.drawCircle(x + 8f, y + 8f, radius, resetButtonPaint)
        
        // Bouton
        resetButtonPaint.color = color
        canvas.drawCircle(x, y, radius, resetButtonPaint)
        
        // Bordure
        resetButtonPaint.color = 0xFF333333.toInt()
        resetButtonPaint.style = Paint.Style.STROKE
        resetButtonPaint.strokeWidth = 8f
        canvas.drawCircle(x, y, radius, resetButtonPaint)
        resetButtonPaint.style = Paint.Style.FILL
        
        // Texte - 1/4 de hauteur plus bas
        resetTextPaint.textAlign = Paint.Align.CENTER
        resetTextPaint.textSize = 80f
        resetTextPaint.color = 0xFFFFFFFF.toInt()
        resetTextPaint.isFakeBoldText = false
        canvas.drawText(text, x, y + 30f, resetTextPaint)
    }
    
    private fun drawMiniDaisy(canvas: Canvas, centerX: Float, centerY: Float, size: Float) {
        if (daisyBitmap != null) {
            // Utiliser ton image de marguerite
            val matrix = Matrix()
            val scale = size / maxOf(daisyBitmap!!.width, daisyBitmap!!.height)
            matrix.setScale(scale, scale)
            matrix.postTranslate(
                centerX - (daisyBitmap!!.width * scale) / 2f,
                centerY - (daisyBitmap!!.height * scale) / 2f
            )
            
            val paint = Paint().apply {
                isAntiAlias = true
                isFilterBitmap = true
            }
            canvas.drawBitmap(daisyBitmap!!, matrix, paint)
        } else {
            // Fallback si l'image n'est pas trouvée
            val centerPaint = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(255, 200, 50)
                style = Paint.Style.FILL
            }
            canvas.drawCircle(centerX, centerY, size * 0.3f, centerPaint)
            
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = size * 0.2f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("IMG", centerX, centerY, textPaint)
        }
    }
    
    // ==================== FONCTIONS POUR DESSINER LA PLANTE ====================
    
    fun drawMainStem(canvas: Canvas, mainStem: List<PlantStem.StemPoint>) {
        if (mainStem.size < 2) return
        
        stemPaint.color = Color.rgb(50, 120, 50)
        
        for (i in 1 until mainStem.size) {
            val point = mainStem[i]
            val prevPoint = mainStem[i - 1]
            
            stemPaint.strokeWidth = point.thickness
            
            val adjustedX = point.x + point.oscillation + point.permanentWave
            val prevAdjustedX = prevPoint.x + prevPoint.oscillation + prevPoint.permanentWave
            
            if (i == 1) {
                canvas.drawLine(prevAdjustedX, prevPoint.y, adjustedX, point.y, stemPaint)
            } else {
                val controlX = (prevAdjustedX + adjustedX) / 2f
                val controlY = (prevPoint.y + point.y) / 2f
                val curvatureOffset = (adjustedX - prevAdjustedX) * 0.3f
                val finalControlX = controlX + curvatureOffset
                
                canvas.drawLine(prevAdjustedX, prevPoint.y, finalControlX, controlY, stemPaint)
                canvas.drawLine(finalControlX, controlY, adjustedX, point.y, stemPaint)
            }
        }
    }
    
    fun drawBranches(canvas: Canvas, branches: List<PlantStem.Branch>) {
        branchPaint.color = Color.rgb(40, 100, 40)
        
        for (branch in branches.filter { it.isActive }) {
            if (branch.points.size >= 2) {
                for (i in 1 until branch.points.size) {
                    val point = branch.points[i]
                    val prevPoint = branch.points[i - 1]
                    
                    branchPaint.strokeWidth = point.thickness
                    
                    if (i == 1 || branch.points.size <= 2) {
                        canvas.drawLine(prevPoint.x, prevPoint.y, point.x, point.y, branchPaint)
                    } else {
                        val controlX = (prevPoint.x + point.x) / 2f
                        val controlY = (prevPoint.y + point.y) / 2f
                        canvas.drawLine(prevPoint.x, prevPoint.y, controlX, controlY, branchPaint)
                        canvas.drawLine(controlX, controlY, point.x, point.y, branchPaint)
                    }
                }
            }
        }
    }
    
    fun drawLeaves(canvas: Canvas, leaves: List<PlantLeavesManager.Leaf>, stem: PlantStem) {
        for (leaf in leaves) {
            if (leaf.currentSize > 0) {
                leafPaint.color = stem.getLeavesManager().getLeafColor(leaf)
                val leafPath = stem.getLeavesManager().createLeafPath(leaf)
                canvas.drawPath(leafPath, leafPaint)
                
                if (leaf.currentSize > leaf.maxSize * 0.7f) {
                    leafPaint.style = Paint.Style.STROKE
                    leafPaint.strokeWidth = 1.5f
                    leafPaint.color = Color.rgb(20, 80, 20)
                    canvas.drawPath(leafPath, leafPaint)
                    leafPaint.style = Paint.Style.FILL
                }
            }
        }
    }
    
    fun drawBackgroundFlowers(canvas: Canvas, flowers: List<FlowerManager.Flower>, stem: PlantStem) {
        val backgroundFlowers = flowers.filter { it.perspective.viewAngle > 60f }
        if (backgroundFlowers.isNotEmpty()) {
            stem.getFlowerManager().drawSpecificFlowers(canvas, backgroundFlowers, flowerPaint, flowerCenterPaint)
        }
    }
    
    fun drawForegroundFlowers(canvas: Canvas, flowers: List<FlowerManager.Flower>, stem: PlantStem) {
        val foregroundFlowers = flowers.filter { it.perspective.viewAngle <= 60f }
        if (foregroundFlowers.isNotEmpty()) {
            stem.getFlowerManager().drawSpecificFlowers(canvas, foregroundFlowers, flowerPaint, flowerCenterPaint)
        }
    }
}
