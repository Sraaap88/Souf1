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
    
    // Paint pour la zone cible des défis
    private val targetZonePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = 0x4000FF00.toInt()  // Vert lime transparent (40% opacité)
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
                        resetButtonX: Float, resetButtonY: Float, resetButtonRadius: Float, challengeManager: ChallengeManager) {
        
        // Dessiner la zone cible AVANT tout le reste si défi actif (seulement pour défi 1 - zone verte)
        if (challengeManager.getCurrentChallenge()?.id == 1 && shouldShowTargetZone(lightState)) {
            drawTargetZone(canvas)
        }
        
        when (lightState) {
            OrganicLineView.LightState.START -> {
                drawStartButtons(canvas)
            }
            OrganicLineView.LightState.FLOWER_CHOICE -> {
                drawFlowerChoice(canvas)
            }
            OrganicLineView.LightState.CHALLENGE_SELECTION -> {
                drawChallengeSelection(canvas, challengeManager)
            }
            OrganicLineView.LightState.CHALLENGE_BRIEF -> {
                drawChallengeBrief(canvas, challengeManager, timeRemaining)
            }
            OrganicLineView.LightState.YELLOW -> {
                drawInspirePhase(canvas, timeRemaining, challengeManager)
            }
            OrganicLineView.LightState.CHALLENGE_RESULT -> {
                drawChallengeResult(canvas, challengeManager)
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
            
            // NOUVEAU: Affichage spécial pour le défi bourgeons avec conseil
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
    
    // ==================== NOUVELLES FONCTIONS POUR LES DÉFIS ====================
    
    private fun drawChallengeSelection(canvas: Canvas, challengeManager: ChallengeManager) {
        // Titre
        resetTextPaint.textAlign = Paint.Align.CENTER
        resetTextPaint.textSize = 120f
        resetTextPaint.color = 0xFFFFFFFF.toInt()
        resetTextPaint.isFakeBoldText = true
        canvas.drawText("SÉLECTIONNER DÉFI", screenWidth / 2f, screenHeight * 0.2f, resetTextPaint)
        
        // Sous-titre
        resetTextPaint.textSize = 60f
        resetTextPaint.isFakeBoldText = false
        canvas.drawText("MARGUERITE", screenWidth / 2f, screenHeight * 0.28f, resetTextPaint)
        
        // 3 boutons de défi
        val challenges = challengeManager.getMargueriteChallenges()
        val buttonWidth = screenWidth * 0.25f
        val buttonHeight = screenHeight * 0.12f
        val startY = screenHeight * 0.4f
        
        for (i in 1..3) {
            val challenge = challenges.find { it.id == i }
            val buttonY = startY + (i - 1) * (buttonHeight + 30f)
            
            // Couleur selon l'état
            val color = when {
                challenge?.isCompleted == true -> 0xFF00AA00.toInt()  // Vert si complété
                challenge?.isUnlocked == true -> 0xFF4169E1.toInt()   // Bleu si débloqué
                else -> 0xFF666666.toInt()  // Gris si verrouillé
            }
            
            // Dessiner le bouton rectangulaire
            resetButtonPaint.color = color
            canvas.drawRoundRect(
                screenWidth / 2f - buttonWidth / 2f,
                buttonY - buttonHeight / 2f,
                screenWidth / 2f + buttonWidth / 2f,
                buttonY + buttonHeight / 2f,
                20f, 20f, resetButtonPaint
            )
            
            // Bordure
            resetButtonPaint.color = 0xFF333333.toInt()
            resetButtonPaint.style = Paint.Style.STROKE
            resetButtonPaint.strokeWidth = 4f
            canvas.drawRoundRect(
                screenWidth / 2f - buttonWidth / 2f,
                buttonY - buttonHeight / 2f,
                screenWidth / 2f + buttonWidth / 2f,
                buttonY + buttonHeight / 2f,
                20f, 20f, resetButtonPaint
            )
            resetButtonPaint.style = Paint.Style.FILL
            
            // Texte du défi
            resetTextPaint.textAlign = Paint.Align.CENTER
            resetTextPaint.textSize = 50f
            resetTextPaint.color = 0xFFFFFFFF.toInt()
            resetTextPaint.isFakeBoldText = false
            
            val buttonText = if (challenge?.isUnlocked == true) {
                challenge.title + if (challenge.isCompleted) " ✓" else ""
            } else {
                "VERROUILLÉ"
            }
            
            canvas.drawText(buttonText, screenWidth / 2f, buttonY + 15f, resetTextPaint)
        }
    }
    
    private fun drawChallengeBrief(canvas: Canvas, challengeManager: ChallengeManager, timeRemaining: Long) {
        val challenge = challengeManager.getCurrentChallenge()
        
        // Titre
        resetTextPaint.textAlign = Paint.Align.CENTER
        resetTextPaint.textSize = 120f
        resetTextPaint.color = 0xFFFFFFFF.toInt()
        resetTextPaint.isFakeBoldText = true
        canvas.drawText(challenge?.title ?: "DÉFI", screenWidth / 2f, screenHeight * 0.3f, resetTextPaint)
        
        // Description
        resetTextPaint.textSize = 70f
        resetTextPaint.isFakeBoldText = false
        canvas.drawText(challenge?.description ?: "Description à venir", screenWidth / 2f, screenHeight * 0.5f, resetTextPaint)
        
        // NOUVEAU: Conseil spécial pour le défi bourgeons
        if (challenge?.id == 2) {
            resetTextPaint.textSize = 55f
            resetTextPaint.color = 0xFFFFD700.toInt()  // Jaune
            canvas.drawText("Technique: souffle très doux et constant", screenWidth / 2f, screenHeight * 0.6f, resetTextPaint)
        }
        
        // Compte à rebours
        resetTextPaint.textSize = 90f
        resetTextPaint.color = 0xFFFFD700.toInt()
        canvas.drawText("Début dans: $timeRemaining", screenWidth / 2f, screenHeight * 0.7f, resetTextPaint)
    }
    
    private fun drawChallengeResult(canvas: Canvas, challengeManager: ChallengeManager) {
        // NOUVEAU: Récupérer le vrai résultat du défi
        val result = challengeManager.finalizeChallengeResult()
        
        if (result != null) {
            // Couleur selon le succès
            val resultColor = if (result.success) 0xFF00FF00.toInt() else 0xFFFF0000.toInt()
            val resultText = if (result.success) "DÉFI RÉUSSI!" else "DÉFI ÉCHOUÉ!"
            
            resetTextPaint.textAlign = Paint.Align.CENTER
            resetTextPaint.textSize = 150f
            resetTextPaint.color = resultColor
            resetTextPaint.isFakeBoldText = true
            canvas.drawText(resultText, screenWidth / 2f, screenHeight * 0.4f, resetTextPaint)
            
            // Message détaillé
            resetTextPaint.textSize = 60f
            resetTextPaint.color = 0xFFFFFFFF.toInt()
            resetTextPaint.isFakeBoldText = false
            canvas.drawText(result.message, screenWidth / 2f, screenHeight * 0.55f, resetTextPaint)
            
            // NOUVEAU: Message spécial selon le type de défi
            if (result.challenge.id == 2 && result.success) {
                resetTextPaint.textSize = 50f
                resetTextPaint.color = 0xFFFFD700.toInt()
                canvas.drawText("Excellente maîtrise du souffle!", screenWidth / 2f, screenHeight * 0.65f, resetTextPaint)
            }
        } else {
            // Fallback si pas de résultat
            resetTextPaint.textAlign = Paint.Align.CENTER
            resetTextPaint.textSize = 150f
            resetTextPaint.color = 0xFF00FF00.toInt()
            resetTextPaint.isFakeBoldText = true
            canvas.drawText("DÉFI TERMINÉ!", screenWidth / 2f, screenHeight * 0.4f, resetTextPaint)
        }
        
        // Statut de progression
        resetTextPaint.textSize = 60f
        resetTextPaint.color = 0xFFFFFFFF.toInt()
        resetTextPaint.isFakeBoldText = false
        canvas.drawText(challengeManager.getCompletionStatus(), screenWidth / 2f, screenHeight * 0.75f, resetTextPaint)
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
    
    private fun drawTargetZone(canvas: Canvas) {
        // Zone au 1/3 de l'écran (en haut), hauteur ~1 pouce (120px)
        val zoneTop = screenHeight / 3f - 60f      // 1/3 de l'écran moins la moitié de la hauteur
        val zoneBottom = screenHeight / 3f + 60f   // 1/3 de l'écran plus la moitié de la hauteur
        val zoneLeft = 0f
        val zoneRight = screenWidth.toFloat()
        
        // Dessiner le rectangle transparent vert lime
        canvas.drawRect(zoneLeft, zoneTop, zoneRight, zoneBottom, targetZonePaint)
        
        // Optionnel: bordures pour mieux voir la zone
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
        val zoneBottom = screenHeight / 3f + 60f
        val zoneLeft = 0f
        val zoneRight = screenWidth.toFloat()
        
        return x >= zoneLeft && x <= zoneRight && y >= zoneTop && y <= zoneBottom
    }
}
