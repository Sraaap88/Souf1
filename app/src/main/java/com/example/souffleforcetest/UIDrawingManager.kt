package com.example.souffleforcetest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import kotlin.math.*

class UIDrawingManager(private val context: Context, private val screenWidth: Int, private val screenHeight: Int, private val challengeManager: ChallengeManager) {
    
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
    
    // Paint pour la zone cible des défis
    private val targetZonePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = 0x4000FF00.toInt()  // Vert lime transparent (40% opacité)
    }
    
    // ==================== IMAGE RESOURCES ====================
    
    private var daisyBitmap: Bitmap? = null
    
    // Déléguer les fonctions de défi au ChallengeUIHelper
    private val challengeUIHelper = ChallengeUIHelper(screenWidth, screenHeight)
    
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
                        resetButtonX: Float, resetButtonY: Float, resetButtonRadius: Float, challengeManager: ChallengeManager) {
        
        // Dessiner la zone cible AVANT tout le reste si défi actif
        if ((challengeManager.getCurrentChallenge()?.id == 1 || challengeManager.getCurrentChallenge()?.id == 3) && shouldShowTargetZone(lightState)) {
            drawTargetZone(canvas, challengeManager.getCurrentChallenge()?.id ?: 1)
        }
        
        when (lightState) {
            OrganicLineView.LightState.START -> {
            drawFlowerChoice(canvas)  // START = choix de fleur maintenant
            }        
            OrganicLineView.LightState.MODE_CHOICE -> {
                drawStartButtons(canvas)  // MODE_CHOICE = choix ZEN/DÉFI
            }            
            OrganicLineView.LightState.CHALLENGE_SELECTION -> {
                challengeUIHelper.drawChallengeSelection(canvas, challengeManager, resetTextPaint, resetButtonPaint)
            }
            OrganicLineView.LightState.CHALLENGE_BRIEF -> {
                challengeUIHelper.drawChallengeBrief(canvas, challengeManager, timeRemaining, resetTextPaint)
            }
            OrganicLineView.LightState.YELLOW -> {
                drawInspirePhase(canvas, timeRemaining, challengeManager)
            }
            OrganicLineView.LightState.CHALLENGE_RESULT -> {
                challengeUIHelper.drawChallengeResult(canvas, challengeManager, resetTextPaint)
            }
            OrganicLineView.LightState.RED -> {
                drawResetButton(canvas, resetButtonX, resetButtonY, resetButtonRadius)
            }
            else -> {
                drawGreenPhases(canvas, lightState, timeRemaining, resetButtonX, resetButtonY, resetButtonRadius, challengeManager)
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
        
        // Position des fleurs
        val flowerButtonRadius = screenWidth * 0.15f
        val spacing = flowerButtonRadius * 3f
        val centerX = screenWidth / 2f
        val buttonY = screenHeight / 2f
        
        // Marguerite (toujours disponible) - à gauche
        val margueriteX = centerX - spacing / 2f
        drawMiniDaisy(canvas, margueriteX, buttonY, flowerButtonRadius * 1.5f)
        
        // Nom en dessous
        resetTextPaint.textSize = 50f
        resetTextPaint.color = 0xFFFFFFFF.toInt()
        resetTextPaint.isFakeBoldText = false
        canvas.drawText("MARGUERITE", margueriteX, buttonY + flowerButtonRadius + 80f, resetTextPaint)
        
        // Rose (si débloquée) - à droite
        val roseX = centerX + spacing / 2f
        val roseUnlocked = challengeManager.isFlowerUnlocked("ROSE")
        
        if (roseUnlocked) {
            // Rose débloquée - afficher l'émoji rose
            resetTextPaint.textSize = flowerButtonRadius * 1.8f
            resetTextPaint.color = 0xFFFF69B4.toInt()  // Rose
            canvas.drawText("🌹", roseX, buttonY + 20f, resetTextPaint)
            
            // Nom en dessous
            resetTextPaint.textSize = 50f
            resetTextPaint.color = 0xFFFFFFFF.toInt()
            resetTextPaint.isFakeBoldText = false
            canvas.drawText("ROSE", roseX, buttonY + flowerButtonRadius + 80f, resetTextPaint)
        } else {
            // Rose verrouillée - afficher cadenas
            resetTextPaint.textSize = flowerButtonRadius * 1.5f
            resetTextPaint.color = 0xAA888888.toInt()  // Gris
            canvas.drawText("🔒", roseX, buttonY + 20f, resetTextPaint)
            
            // Nom en dessous
            resetTextPaint.textSize = 50f
            resetTextPaint.color = 0xAA888888.toInt()  // Gris
            resetTextPaint.isFakeBoldText = false
            canvas.drawText("VERROUILLÉ", roseX, buttonY + flowerButtonRadius + 80f, resetTextPaint)
        }
    }
    
    private fun drawInspirePhase(canvas: Canvas, timeRemaining: Long, challengeManager: ChallengeManager) {
        // Texte principal au centre
        resetTextPaint.textAlign = Paint.Align.CENTER
        resetTextPaint.textSize = 180f
        resetTextPaint.color = 0xFFFFFFFF.toInt()
        resetTextPaint.isFakeBoldText = false
        canvas.drawText("INSPIREZ", screenWidth / 2f, screenHeight / 2f, resetTextPaint)
        
        if (timeRemaining > 0) {
            resetTextPaint.textSize = 108f
            canvas.drawText(timeRemaining.toString(), screenWidth / 2f, screenHeight / 2f + 144f, resetTextPaint)
        }
        
        // Afficher le défi actuel si en mode défi
        val challengeBrief = challengeManager.getCurrentChallengeBrief()
        if (challengeBrief != null) {
            resetTextPaint.textSize = 50f
            resetTextPaint.color = 0xAAFFFFFF.toInt()  // Semi-transparent
            canvas.drawText(challengeBrief, screenWidth / 2f, 150f, resetTextPaint)
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
                               lightX: Float, lightY: Float, lightRadius: Float, challengeManager: ChallengeManager) {
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
        
        // Texte pour les phases vertes
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
        
        // Afficher le défi actuel en haut si en mode défi avec indication spéciale pour défi bourgeons
        val challengeBrief = challengeManager.getCurrentChallengeBrief()
        if (challengeBrief != null) {
            resetTextPaint.textAlign = Paint.Align.CENTER
            resetTextPaint.textSize = 45f
            resetTextPaint.color = 0xAAFFFFFF.toInt()  // Semi-transparent
            resetTextPaint.isFakeBoldText = false
            canvas.drawText(challengeBrief, screenWidth / 2f, 120f, resetTextPaint)
            
            // Affichage spécial pour le défi bourgeons avec conseil
            if (challengeManager.getCurrentChallenge()?.id == 2) {
                resetTextPaint.textSize = 35f
                resetTextPaint.color = 0x88FFFF00.toInt()  // Jaune semi-transparent
                canvas.drawText("Souffle doux et constant requis", screenWidth / 2f, 170f, resetTextPaint)
            }
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
    
    // ==================== ZONE CIBLE POUR DÉFIS ====================
    
    private fun shouldShowTargetZone(lightState: OrganicLineView.LightState): Boolean {
        // Afficher la zone pendant les phases de croissance seulement
        return when (lightState) {
            OrganicLineView.LightState.GREEN_GROW,
            OrganicLineView.LightState.GREEN_LEAVES,
            OrganicLineView.LightState.GREEN_FLOWER -> true
            else -> false
        }
    }
    
    private fun drawTargetZone(canvas: Canvas, challengeId: Int = 1) {
        val zoneTop: Float
        val zoneBottom: Float
        
        when (challengeId) {
            1 -> {
                // Défi 1: Zone au 1/3 de l'écran, 2 fois plus large que la version actuelle
                zoneTop = screenHeight / 3f - 60f      // 1/3 de l'écran moins 60px
                zoneBottom = screenHeight / 3f + 360f  // 1/3 de l'écran plus 360px (420px total)
            }
            3 -> {
                // Défi 3: Zone verte de 240px total (120px haut + 120px bas)
                zoneTop = screenHeight / 3f - 120f     // 1/3 de l'écran moins 120px
                zoneBottom = screenHeight / 3f + 120f  // 1/3 de l'écran plus 120px (240px total)
            }
            else -> {
                // Par défaut défi 1
                zoneTop = screenHeight / 3f - 60f
                zoneBottom = screenHeight / 3f + 360f
            }
        }
        
        val zoneLeft = 0f
        val zoneRight = screenWidth.toFloat()
        
        // Dessiner le rectangle transparent vert lime
        canvas.drawRect(zoneLeft, zoneTop, zoneRight, zoneBottom, targetZonePaint)
        
        // Bordures pour mieux voir la zone
        val borderPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = 0x8000FF00.toInt()  // Vert lime plus opaque pour les bordures
        }
        canvas.drawRect(zoneLeft, zoneTop, zoneRight, zoneBottom, borderPaint)
    }
    
    // Fonction utilitaire pour vérifier si un point est dans la zone cible
    fun isPointInTargetZone(x: Float, y: Float): Boolean {
        val zoneTop = screenHeight / 3f - 60f
        val zoneBottom = screenHeight / 3f + 360f  // Zone élargie à 360px vers le bas
        val zoneLeft = 0f
        val zoneRight = screenWidth.toFloat()
        
        return x >= zoneLeft && x <= zoneRight && y >= zoneTop && y <= zoneBottom
    }
}
