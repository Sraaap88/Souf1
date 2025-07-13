package com.example.souffleforcetest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color

class UIDrawingManager(private val context: Context, private val screenWidth: Int, private val screenHeight: Int, private val challengeManager: ChallengeManager) {
    
    // ==================== DÉLÉGATION AUX COMPOSANTS ====================
    
    private val flowerComponents = FlowerUIComponents(context, screenWidth, screenHeight)
    private val challengeUIHelper = ChallengeUIHelper(screenWidth, screenHeight)
    
    // ==================== UI PAINTS PRINCIPAUX ====================
    
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
    
    // ==================== FONCTION PRINCIPALE D'AFFICHAGE ====================
    
    fun drawCurrentState(canvas: Canvas, lightState: OrganicLineView.LightState, timeRemaining: Long, 
                        resetButtonX: Float, resetButtonY: Float, resetButtonRadius: Float, challengeManager: ChallengeManager) {
        
        // Dessiner la zone cible AVANT tout le reste si défi actif
        if ((challengeManager.getCurrentChallenge()?.id == 1 || challengeManager.getCurrentChallenge()?.id == 3) && 
            flowerComponents.shouldShowTargetZone(lightState)) {
            flowerComponents.drawTargetZone(canvas, challengeManager, challengeManager.getCurrentChallenge()?.id ?: 1)
        }
        
        when (lightState) {
            OrganicLineView.LightState.START -> {
                // CORRECTION : Utiliser flowerComponents directement (pas de nouveau renderer)
                flowerComponents.drawFlowerChoice(canvas, challengeManager)
            }        
            OrganicLineView.LightState.MODE_CHOICE -> {
                drawStartButtons(canvas)
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
    
    // ==================== FONCTIONS D'AFFICHAGE PRINCIPALES ====================
    
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
    
    // ==================== DÉLÉGATION POUR LE RENDU DES PLANTES - CORRIGÉ ====================
    
    // CORRECTION: Supprimer dissolveInfo des signatures puisque FlowerUIComponents ne l'accepte pas
    fun drawMainStem(canvas: Canvas, mainStem: List<PlantStem.StemPoint>) {
        flowerComponents.drawMainStem(canvas, mainStem)
    }
    
    fun drawBranches(canvas: Canvas, branches: List<PlantStem.Branch>) {
        flowerComponents.drawBranches(canvas, branches)
    }
    
    fun drawLeaves(canvas: Canvas, leaves: List<PlantLeavesManager.Leaf>, stem: PlantStem) {
        flowerComponents.drawLeaves(canvas, leaves, stem)
    }
    
    fun drawBackgroundFlowers(canvas: Canvas, flowers: List<FlowerManager.Flower>, stem: PlantStem) {
        flowerComponents.drawBackgroundFlowers(canvas, flowers, stem)
    }
    
    fun drawForegroundFlowers(canvas: Canvas, flowers: List<FlowerManager.Flower>, stem: PlantStem) {
        flowerComponents.drawForegroundFlowers(canvas, flowers, stem)
    }
    
    // ==================== FONCTION UTILITAIRE ====================
    
    fun isPointInTargetZone(x: Float, y: Float): Boolean {
        return flowerComponents.isPointInMargueriteTargetZone(x, y)
    }
}
