package com.example.souffleforcetest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import kotlin.math.*

class UIDrawingManager(
    private val context: Context,
    private val screenWidth: Int,
    private val screenHeight: Int,
    private val challengeManager: ChallengeManager
) {
    
    private val challengeUIHelper = ChallengeUIHelper(screenWidth, screenHeight)
    
    // ==================== FONCTION PRINCIPALE DE RENDU ====================
    
    fun drawCurrentState(
        canvas: Canvas,
        lightState: OrganicLineView.LightState,
        timeRemaining: Long,
        resetButtonX: Float,
        resetButtonY: Float,
        resetButtonRadius: Float,
        challengeManager: ChallengeManager
    ) {
        when (lightState) {
            OrganicLineView.LightState.START -> drawFlowerSelectionScreen(canvas)
            OrganicLineView.LightState.MODE_CHOICE -> drawModeChoiceScreen(canvas)
            OrganicLineView.LightState.CHALLENGE_SELECTION -> drawChallengeSelectionScreen(canvas)
            OrganicLineView.LightState.CHALLENGE_BRIEF -> drawChallengeBriefScreen(canvas, timeRemaining)
            OrganicLineView.LightState.YELLOW -> drawYellowLight(canvas, timeRemaining)
            OrganicLineView.LightState.GREEN_GROW -> drawGreenLight(canvas, "CROISSANCE", timeRemaining)
            OrganicLineView.LightState.GREEN_LEAVES -> drawGreenLight(canvas, "FEUILLES", timeRemaining)
            OrganicLineView.LightState.GREEN_FLOWER -> drawGreenLight(canvas, "FLEURS", timeRemaining)
            OrganicLineView.LightState.CHALLENGE_RESULT -> drawChallengeResultScreen(canvas)
            OrganicLineView.LightState.RED -> drawRedLight(canvas, resetButtonX, resetButtonY, resetButtonRadius)
        }
    }
    
    // ==================== ÉCRAN DE SÉLECTION DES FLEURS ====================
    
    private fun drawFlowerSelectionScreen(canvas: Canvas) {
        val paint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        // Fond noir
        canvas.drawColor(Color.BLACK)
        
        // Titre principal
        paint.textSize = 120f
        paint.color = 0xFFFFFFFF.toInt()
        paint.isFakeBoldText = true
        canvas.drawText("CHOISIR", screenWidth / 2f, screenHeight * 0.2f, paint)
        canvas.drawText("FLEUR", screenWidth / 2f, screenHeight * 0.28f, paint)
        
        // Obtenir les fleurs débloquées
        val unlockedFlowers = getUnlockedFlowers()
        
        // Dessiner les fleurs selon le nombre débloqué
        when (unlockedFlowers.size) {
            1 -> drawSingleFlower(canvas, paint)
            2 -> drawTwoFlowers(canvas, paint, unlockedFlowers)
            3 -> drawThreeFlowers(canvas, paint, unlockedFlowers)
            else -> drawFourFlowers(canvas, paint, unlockedFlowers)
        }
        
        // Instructions
        paint.textSize = 50f
        paint.color = 0xFFCCCCCC.toInt()
        paint.isFakeBoldText = false
        canvas.drawText("Touchez une fleur pour la sélectionner", screenWidth / 2f, screenHeight * 0.85f, paint)
    }
    
    private fun drawSingleFlower(canvas: Canvas, paint: Paint) {
        val flowerRadius = screenWidth * 0.18f
        val centerX = screenWidth / 2f
        val centerY = screenHeight / 2f
        
        drawMargueriteIcon(canvas, paint, centerX, centerY, flowerRadius)
        
        paint.textSize = 60f
        paint.color = 0xFFFFD700.toInt()
        canvas.drawText("MARGUERITE", centerX, centerY + flowerRadius + 80f, paint)
    }
    
    private fun drawTwoFlowers(canvas: Canvas, paint: Paint, unlockedFlowers: List<String>) {
        val flowerRadius = screenWidth * 0.18f
        val spacing = flowerRadius * 2.8f
        val centerX = screenWidth / 2f
        val centerY = screenHeight / 2f
        
        // Marguerite (gauche)
        val margueriteX = centerX - spacing / 2f
        drawMargueriteIcon(canvas, paint, margueriteX, centerY, flowerRadius)
        paint.textSize = 50f
        paint.color = 0xFFFFD700.toInt()
        canvas.drawText("MARGUERITE", margueriteX, centerY + flowerRadius + 60f, paint)
        
        // Rose (droite)
        val roseX = centerX + spacing / 2f
        drawRoseIcon(canvas, paint, roseX, centerY, flowerRadius)
        paint.color = 0xFFFF69B4.toInt()
        canvas.drawText("ROSIER", roseX, centerY + flowerRadius + 60f, paint)
    }
    
    private fun drawThreeFlowers(canvas: Canvas, paint: Paint, unlockedFlowers: List<String>) {
        val flowerRadius = screenWidth * 0.18f
        val spacing = flowerRadius * 2.0f
        val centerX = screenWidth / 2f
        val centerY = screenHeight / 2f
        val topY = centerY - spacing * 0.3f
        val bottomY = centerY + spacing * 0.3f
        
        // Marguerite (haut centre)
        drawMargueriteIcon(canvas, paint, centerX, topY, flowerRadius)
        paint.textSize = 45f
        paint.color = 0xFFFFD700.toInt()
        canvas.drawText("MARGUERITE", centerX, topY + flowerRadius + 50f, paint)
        
        // Rose (bas gauche)
        val roseX = centerX - spacing / 2f
        drawRoseIcon(canvas, paint, roseX, bottomY, flowerRadius)
        paint.color = 0xFFFF69B4.toInt()
        canvas.drawText("ROSIER", roseX, bottomY + flowerRadius + 50f, paint)
        
        // Lupin (bas droite)
        val lupinX = centerX + spacing / 2f
        drawLupinIcon(canvas, paint, lupinX, bottomY, flowerRadius)
        paint.color = 0xFF9370DB.toInt()
        canvas.drawText("LUPIN", lupinX, bottomY + flowerRadius + 50f, paint)
    }
    
    private fun drawFourFlowers(canvas: Canvas, paint: Paint, unlockedFlowers: List<String>) {
        val flowerRadius = screenWidth * 0.15f
        val spacing = flowerRadius * 2.2f
        val centerX = screenWidth / 2f
        val centerY = screenHeight / 2f
        val topY = centerY - spacing * 0.4f
        val bottomY = centerY + spacing * 0.4f
        
        // Marguerite (haut gauche)
        val margueriteX = centerX - spacing * 0.5f
        drawMargueriteIcon(canvas, paint, margueriteX, topY, flowerRadius)
        paint.textSize = 40f
        paint.color = 0xFFFFD700.toInt()
        canvas.drawText("MARGUERITE", margueriteX, topY + flowerRadius + 45f, paint)
        
        // Rose (haut droite)
        val roseX = centerX + spacing * 0.5f
        drawRoseIcon(canvas, paint, roseX, topY, flowerRadius)
        paint.color = 0xFFFF69B4.toInt()
        canvas.drawText("ROSIER", roseX, topY + flowerRadius + 45f, paint)
        
        // Lupin (bas gauche)
        val lupinX = centerX - spacing * 0.5f
        drawLupinIcon(canvas, paint, lupinX, bottomY, flowerRadius)
        paint.color = 0xFF9370DB.toInt()
        canvas.drawText("LUPIN", lupinX, bottomY + flowerRadius + 45f, paint)
        
        // Iris (bas droite)
        val irisX = centerX + spacing * 0.5f
        drawIrisIcon(canvas, paint, irisX, bottomY, flowerRadius)
        paint.color = 0xFF4169E1.toInt()
        canvas.drawText("IRIS", irisX, bottomY + flowerRadius + 45f, paint)
    }
    
    // ==================== ICÔNES DES FLEURS ====================
    
    private fun drawMargueriteIcon(canvas: Canvas, paint: Paint, x: Float, y: Float, radius: Float) {
        // Centre jaune
        paint.color = 0xFFFFD700.toInt()
        paint.style = Paint.Style.FILL
        canvas.drawCircle(x, y, radius * 0.3f, paint)
        
        // Pétales blancs
        paint.color = 0xFFFFFFFF.toInt()
        for (i in 0..11) {
            val angle = i * 30f * PI / 180f
            val petalX = x + cos(angle).toFloat() * radius * 0.7f
            val petalY = y + sin(angle).toFloat() * radius * 0.7f
            canvas.drawCircle(petalX, petalY, radius * 0.2f, paint)
        }
        
        // Bordure
        paint.color = 0xFF333333.toInt()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawCircle(x, y, radius, paint)
        paint.style = Paint.Style.FILL
    }
    
    private fun drawRoseIcon(canvas: Canvas, paint: Paint, x: Float, y: Float, radius: Float) {
        // Pétales en spirale pour représenter la rose
        paint.style = Paint.Style.FILL
        
        // Pétales extérieurs (rouge foncé)
        paint.color = 0xFFDC143C.toInt()
        for (i in 0..7) {
            val angle = i * 45f * PI / 180f
            val petalX = x + cos(angle).toFloat() * radius * 0.8f
            val petalY = y + sin(angle).toFloat() * radius * 0.8f
            canvas.drawCircle(petalX, petalY, radius * 0.25f, paint)
        }
        
        // Pétales moyens (rouge moyen)
        paint.color = 0xFFFF1493.toInt()
        for (i in 0..5) {
            val angle = (i * 60f + 30f) * PI / 180f
            val petalX = x + cos(angle).toFloat() * radius * 0.5f
            val petalY = y + sin(angle).toFloat() * radius * 0.5f
            canvas.drawCircle(petalX, petalY, radius * 0.2f, paint)
        }
        
        // Centre (rouge clair)
        paint.color = 0xFFFF69B4.toInt()
        canvas.drawCircle(x, y, radius * 0.25f, paint)
        
        // Bordure
        paint.color = 0xFF333333.toInt()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawCircle(x, y, radius, paint)
        paint.style = Paint.Style.FILL
    }
    
    private fun drawLupinIcon(canvas: Canvas, paint: Paint, x: Float, y: Float, radius: Float) {
        // Tige verte
        paint.color = 0xFF228B22.toInt()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8f
        canvas.drawLine(x, y + radius * 0.8f, x, y - radius * 0.8f, paint)
        
        // Épi de fleurs (petites fleurs violettes le long de la tige)
        paint.style = Paint.Style.FILL
        paint.color = 0xFF9370DB.toInt()
        
        for (i in 0..6) {
            val flowerY = y - radius * 0.6f + (i * radius * 0.2f)
            val offset = (i % 2) * radius * 0.1f - radius * 0.05f
            canvas.drawCircle(x + offset, flowerY, radius * 0.08f, paint)
        }
        
        // Feuilles palmées (caractéristique du lupin)
        paint.color = 0xFF228B22.toInt()
        for (i in 0..2) {
            val angle = (i * 120f - 60f) * PI / 180f
            val leafX = x + cos(angle).toFloat() * radius * 0.6f
            val leafY = y + sin(angle).toFloat() * radius * 0.3f + radius * 0.4f
            canvas.drawCircle(leafX, leafY, radius * 0.12f, paint)
        }
        
        // Bordure
        paint.color = 0xFF333333.toInt()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawCircle(x, y, radius, paint)
        paint.style = Paint.Style.FILL
    }
    
    private fun drawIrisIcon(canvas: Canvas, paint: Paint, x: Float, y: Float, radius: Float) {
        // Tige droite et fine
        paint.color = 0xFF228B22.toInt()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        canvas.drawLine(x, y + radius * 0.8f, x, y - radius * 0.6f, paint)
        
        // Feuilles longues et effilées (caractéristique de l'iris)
        paint.style = Paint.Style.FILL
        paint.color = 0xFF228B22.toInt()
        
        // Feuille gauche
        val leafPath1 = android.graphics.Path()
        leafPath1.moveTo(x - radius * 0.1f, y + radius * 0.8f)
        leafPath1.lineTo(x - radius * 0.4f, y)
        leafPath1.lineTo(x - radius * 0.35f, y - radius * 0.7f)
        leafPath1.lineTo(x - radius * 0.05f, y + radius * 0.6f)
        leafPath1.close()
        canvas.drawPath(leafPath1, paint)
        
        // Feuille droite
        val leafPath2 = android.graphics.Path()
        leafPath2.moveTo(x + radius * 0.1f, y + radius * 0.8f)
        leafPath2.lineTo(x + radius * 0.4f, y)
        leafPath2.lineTo(x + radius * 0.35f, y - radius * 0.7f)
        leafPath2.lineTo(x + radius * 0.05f, y + radius * 0.6f)
        leafPath2.close()
        canvas.drawPath(leafPath2, paint)
        
        // Fleur d'iris (3 pétales dressés + 3 sépales retombants)
        paint.color = 0xFF4169E1.toInt()
        
        // Pétales dressés (plus petits)
        for (i in 0..2) {
            val angle = (i * 120f) * PI / 180f
            val petalX = x + cos(angle).toFloat() * radius * 0.3f
            val petalY = y - radius * 0.6f + sin(angle).toFloat() * radius * 0.3f
            canvas.drawCircle(petalX, petalY, radius * 0.12f, paint)
        }
        
        // Sépales retombants (plus grands)
        paint.color = 0xFF191970.toInt()
        for (i in 0..2) {
            val angle = (i * 120f + 60f) * PI / 180f
            val sepalX = x + cos(angle).toFloat() * radius * 0.45f
            val sepalY = y - radius * 0.4f + sin(angle).toFloat() * radius * 0.45f
            canvas.drawCircle(sepalX, sepalY, radius * 0.15f, paint)
        }
        
        // Centre de la fleur
        paint.color = 0xFFFFD700.toInt()
        canvas.drawCircle(x, y - radius * 0.6f, radius * 0.08f, paint)
        
        // Bordure
        paint.color = 0xFF333333.toInt()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawCircle(x, y, radius, paint)
        paint.style = Paint.Style.FILL
    }
    
    private fun getUnlockedFlowers(): List<String> {
        val flowers = mutableListOf("MARGUERITE")
        
        if (challengeManager.isFlowerUnlocked("ROSE")) {
            flowers.add("ROSE")
        }
        
        if (challengeManager.isFlowerUnlocked("LUPIN")) {
            flowers.add("LUPIN")
        }
        
        if (challengeManager.isFlowerUnlocked("IRIS")) {
            flowers.add("IRIS")
        }
        
        return flowers
    }
    
    // ==================== AUTRES ÉCRANS ====================
    
    private fun drawModeChoiceScreen(canvas: Canvas) {
        val paint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        canvas.drawColor(Color.BLACK)
        
        // Titre
        paint.textSize = 120f
        paint.color = 0xFFFFFFFF.toInt()
        paint.isFakeBoldText = true
        canvas.drawText("CHOISIR", screenWidth / 2f, screenHeight * 0.25f, paint)
        canvas.drawText("MODE", screenWidth / 2f, screenHeight * 0.33f, paint)
        
        // Boutons ZEN et DÉFI
        val buttonRadius = screenWidth * 0.15f
        val spacing = buttonRadius * 2.5f
        val centerX = screenWidth / 2f
        val zenButtonX = centerX - spacing / 2f
        val defiButtonX = centerX + spacing / 2f
        val buttonY = screenHeight / 2f
        
        // Bouton ZEN
        paint.color = 0xFF00AA00.toInt()
        paint.style = Paint.Style.FILL
        canvas.drawCircle(zenButtonX, buttonY, buttonRadius, paint)
        
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 60f
        paint.isFakeBoldText = true
        canvas.drawText("ZEN", zenButtonX, buttonY + 20f, paint)
        
        // Bouton DÉFI
        paint.color = 0xFFFF4500.toInt()
        paint.style = Paint.Style.FILL
        canvas.drawCircle(defiButtonX, buttonY, buttonRadius, paint)
        
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawText("DÉFI", defiButtonX, buttonY + 20f, paint)
        
        // Instructions
        paint.textSize = 50f
        paint.color = 0xFFCCCCCC.toInt()
        paint.isFakeBoldText = false
        canvas.drawText("Choisissez votre mode de jeu", centerX, screenHeight * 0.75f, paint)
    }
    
    private fun drawChallengeSelectionScreen(canvas: Canvas) {
        val paint = Paint().apply { isAntiAlias = true }
        val buttonPaint = Paint().apply { isAntiAlias = true }
        
        canvas.drawColor(Color.BLACK)
        challengeUIHelper.drawChallengeSelection(canvas, challengeManager, paint, buttonPaint)
    }
    
    private fun drawChallengeBriefScreen(canvas: Canvas, timeRemaining: Long) {
        val paint = Paint().apply { isAntiAlias = true }
        
        canvas.drawColor(Color.BLACK)
        challengeUIHelper.drawChallengeBrief(canvas, challengeManager, timeRemaining, paint)
    }
    
    private fun drawChallengeResultScreen(canvas: Canvas) {
        val paint = Paint().apply { isAntiAlias = true }
        
        canvas.drawColor(Color.BLACK)
        challengeUIHelper.drawChallengeResult(canvas, challengeManager, paint)
    }
    
    private fun drawYellowLight(canvas: Canvas, timeRemaining: Long) {
        canvas.drawColor(0xFFFFD700.toInt())
        
        val paint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = 200f
            color = 0xFF000000.toInt()
            isFakeBoldText = true
        }
        
        canvas.drawText("PRÊT", screenWidth / 2f, screenHeight / 2f - 50f, paint)
        
        paint.textSize = 100f
        canvas.drawText("$timeRemaining", screenWidth / 2f, screenHeight / 2f + 100f, paint)
    }
    
    private fun drawGreenLight(canvas: Canvas, phase: String, timeRemaining: Long) {
        canvas.drawColor(0xFF00FF00.toInt())
        
        val paint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
            textSize = 150f
            color = 0xFF000000.toInt()
            isFakeBoldText = true
        }
        
        canvas.drawText(phase, screenWidth / 2f, screenHeight / 2f - 50f, paint)
        
        paint.textSize = 80f
        canvas.drawText("$timeRemaining", screenWidth / 2f, screenHeight / 2f + 80f, paint)
    }
    
    private fun drawRedLight(canvas: Canvas, resetButtonX: Float, resetButtonY: Float, resetButtonRadius: Float) {
        canvas.drawColor(0xFFFF0000.toInt())
        
        val paint = Paint().apply {
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        // Bouton RESET
        paint.color = 0xFF000000.toInt()
        paint.style = Paint.Style.FILL
        canvas.drawCircle(resetButtonX, resetButtonY, resetButtonRadius, paint)
        
        paint.color = 0xFFFFFFFF.toInt()
        paint.textSize = 60f
        paint.isFakeBoldText = true
        canvas.drawText("RESET", resetButtonX, resetButtonY + 20f, paint)
        
        // Icône de reset
        paint.color = 0xFFFFFFFF.toInt()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 8f
        val iconRadius = 30f
        canvas.drawCircle(resetButtonX, resetButtonY - 40f, iconRadius, paint)
        
        // Flèche de reset
        val arrowSize = 15f
        canvas.drawLine(
            resetButtonX + iconRadius - arrowSize,
            resetButtonY - 40f - iconRadius + arrowSize,
            resetButtonX + iconRadius,
            resetButtonY - 40f - iconRadius,
            paint
        )
        canvas.drawLine(
            resetButtonX + iconRadius,
            resetButtonY - 40f - iconRadius,
            resetButtonX + iconRadius - arrowSize,
            resetButtonY - 40f - iconRadius - arrowSize,
            paint
        )
    }
    
    // ==================== FONCTIONS DE RENDU POUR MARGUERITES ====================
    
    fun drawMainStem(canvas: Canvas, mainStem: List<PlantStem.StemPoint>) {
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(34, 139, 34)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        
        for (i in 1 until mainStem.size) {
            val p1 = mainStem[i-1]
            val p2 = mainStem[i]
            paint.strokeWidth = p1.thickness
            canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint)
        }
    }
    
    fun drawBranches(canvas: Canvas, branches: List<PlantStem.Branch>) {
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(34, 139, 34)
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        
        for (branch in branches) {
            if (branch.points.size >= 2) {
                for (i in 1 until branch.points.size) {
                    val p1 = branch.points[i-1]
                    val p2 = branch.points[i]
                    paint.strokeWidth = p1.thickness
                    canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint)
                }
            }
        }
    }
    
    fun drawLeaves(canvas: Canvas, leaves: List<PlantLeavesManager.Leaf>, stem: PlantStem) {
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(34, 139, 34)
            style = Paint.Style.FILL
        }
        
        for (leaf in leaves) {
            if (leaf.currentSize > 0) {
                val leafPoint = stem.getStemPointAtRatio(leaf.heightRatio)
                leafPoint?.let { point ->
                    canvas.save()
                    canvas.translate(point.x, point.y)
                    canvas.rotate(leaf.angle)
                    
                    val size = leaf.currentSize
                    canvas.drawOval(
                        -size/2, -size/4,
                        size/2, size/4,
                        paint
                    )
                    
                    canvas.restore()
                }
            }
        }
    }
    
    fun drawBackgroundFlowers(canvas: Canvas, flowers: List<FlowerManager.Flower>, stem: PlantStem) {
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        
        for (flower in flowers) {
            if (flower.currentSize > 0 && (flower.orientation == 1 || flower.orientation == 2)) {
                paint.color = Color.rgb(255, 255, 255)
                canvas.drawCircle(flower.x, flower.y, flower.currentSize * 0.6f, paint)
                
                paint.color = Color.rgb(255, 215, 0)
                canvas.drawCircle(flower.x, flower.y, flower.currentSize * 0.3f, paint)
            }
        }
    }
    
    fun drawForegroundFlowers(canvas: Canvas, flowers: List<FlowerManager.Flower>, stem: PlantStem) {
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        
        for (flower in flowers) {
            if (flower.currentSize > 0 && (flower.orientation == 3 || flower.orientation == 4)) {
                for (i in 0..11) {
                    val angle = i * 30f * PI / 180f
                    val petalX = flower.x + cos(angle).toFloat() * flower.currentSize * 0.7f
                    val petalY = flower.y + sin(angle).toFloat() * flower.currentSize * 0.7f
                    
                    paint.color = Color.rgb(255, 255, 255)
                    canvas.drawCircle(petalX, petalY, flower.currentSize * 0.2f, paint)
                }
                
                paint.color = Color.rgb(255, 215, 0)
                canvas.drawCircle(flower.x, flower.y, flower.currentSize * 0.3f, paint)
            }
        }
    }
}
