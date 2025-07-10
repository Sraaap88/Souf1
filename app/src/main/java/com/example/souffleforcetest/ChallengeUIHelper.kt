package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color

class ChallengeUIHelper(private val screenWidth: Int, private val screenHeight: Int) {
    
    // ==================== FONCTIONS POUR LES DÉFIS ====================
    
    fun drawChallengeSelection(canvas: Canvas, challengeManager: ChallengeManager, textPaint: Paint, buttonPaint: Paint) {
        // Titre
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 120f
        textPaint.color = 0xFFFFFFFF.toInt()
        textPaint.isFakeBoldText = true
        canvas.drawText("SÉLECTIONNER DÉFI", screenWidth / 2f, screenHeight * 0.2f, textPaint)
        
        // Sous-titre
        textPaint.textSize = 60f
        textPaint.isFakeBoldText = false
        canvas.drawText("MARGUERITE", screenWidth / 2f, screenHeight * 0.28f, textPaint)
        
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
            buttonPaint.color = color
            buttonPaint.style = Paint.Style.FILL
            canvas.drawRoundRect(
                screenWidth / 2f - buttonWidth / 2f,
                buttonY - buttonHeight / 2f,
                screenWidth / 2f + buttonWidth / 2f,
                buttonY + buttonHeight / 2f,
                20f, 20f, buttonPaint
            )
            
            // Bordure
            buttonPaint.color = 0xFF333333.toInt()
            buttonPaint.style = Paint.Style.STROKE
            buttonPaint.strokeWidth = 4f
            canvas.drawRoundRect(
                screenWidth / 2f - buttonWidth / 2f,
                buttonY - buttonHeight / 2f,
                screenWidth / 2f + buttonWidth / 2f,
                buttonY + buttonHeight / 2f,
                20f, 20f, buttonPaint
            )
            buttonPaint.style = Paint.Style.FILL
            
            // Texte du défi
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = 50f
            textPaint.color = 0xFFFFFFFF.toInt()
            textPaint.isFakeBoldText = false
            
            val buttonText = if (challenge?.isUnlocked == true) {
                challenge.title + if (challenge.isCompleted) " ✓" else ""
            } else {
                "VERROUILLÉ"
            }
            
            canvas.drawText(buttonText, screenWidth / 2f, buttonY + 15f, textPaint)
        }
    }
    
    fun drawChallengeBrief(canvas: Canvas, challengeManager: ChallengeManager, timeRemaining: Long, textPaint: Paint) {
        val challenge = challengeManager.getCurrentChallenge()
        
        // Titre
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 120f
        textPaint.color = 0xFFFFFFFF.toInt()
        textPaint.isFakeBoldText = true
        canvas.drawText(challenge?.title ?: "DÉFI", screenWidth / 2f, screenHeight * 0.3f, textPaint)
        
        // Description avec retour à la ligne automatique
        val description = challenge?.description ?: "Description à venir"
        drawMultilineText(canvas, description, screenWidth / 2f, screenHeight * 0.5f, 70f, screenWidth * 0.8f, textPaint)
        
        // Conseil spécial pour le défi bourgeons
        if (challenge?.id == 2) {
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = 55f
            textPaint.color = 0xFFFFD700.toInt()  // Jaune
            textPaint.isFakeBoldText = false
            canvas.drawText("Technique: souffle très doux", screenWidth / 2f, screenHeight * 0.65f, textPaint)
        }
        
        // Compte à rebours
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 90f
        textPaint.color = 0xFFFFD700.toInt()
        textPaint.isFakeBoldText = false
        canvas.drawText("Début dans: $timeRemaining", screenWidth / 2f, screenHeight * 0.8f, textPaint)
    }
    
    fun drawChallengeResult(canvas: Canvas, challengeManager: ChallengeManager, textPaint: Paint) {
        // Récupérer le vrai résultat du défi
        val result = challengeManager.finalizeChallengeResult()
        
        if (result != null) {
            // Couleur selon le succès
            val resultColor = if (result.success) 0xFF00FF00.toInt() else 0xFFFF0000.toInt()
            val resultText = if (result.success) "DÉFI RÉUSSI!" else "DÉFI ÉCHOUÉ!"
            
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = 150f
            textPaint.color = resultColor
            textPaint.isFakeBoldText = true
            canvas.drawText(resultText, screenWidth / 2f, screenHeight * 0.4f, textPaint)
            
            // Message détaillé
            textPaint.textSize = 60f
            textPaint.color = 0xFFFFFFFF.toInt()
            textPaint.isFakeBoldText = false
            canvas.drawText(result.message, screenWidth / 2f, screenHeight * 0.55f, textPaint)
            
            // Message spécial selon le type de défi
            if (result.challenge.id == 2 && result.success) {
                textPaint.textSize = 50f
                textPaint.color = 0xFFFFD700.toInt()
                canvas.drawText("Excellente maîtrise du souffle!", screenWidth / 2f, screenHeight * 0.65f, textPaint)
            }
        } else {
            // Fallback si pas de résultat
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = 150f
            textPaint.color = 0xFF00FF00.toInt()
            textPaint.isFakeBoldText = true
            canvas.drawText("DÉFI TERMINÉ!", screenWidth / 2f, screenHeight * 0.4f, textPaint)
        }
        
        // Statut de progression
        textPaint.
