package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color

class ChallengeUIHelper(private val screenWidth: Int, private val screenHeight: Int) {
    
    // ==================== FONCTIONS POUR LES DÉFIS ====================
    
    fun drawChallengeSelection(canvas: Canvas, challengeManager: ChallengeManager, textPaint: Paint, buttonPaint: Paint) {
        // Titre avec retour à la ligne
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 100f  // Réduit de 120f à 100f
        textPaint.color = 0xFFFFFFFF.toInt()
        textPaint.isFakeBoldText = true
        
        // Première ligne : "SÉLECTIONNER"
        canvas.drawText("SÉLECTIONNER", screenWidth / 2f, screenHeight * 0.15f, textPaint)
        
        // Deuxième ligne : "DÉFI"
        canvas.drawText("DÉFI", screenWidth / 2f, screenHeight * 0.22f, textPaint)
        
        // NOUVEAU: Sous-titre adaptatif selon la fleur
        textPaint.textSize = 60f
        textPaint.isFakeBoldText = false
        
        // Déterminer le type de fleur pour afficher le bon sous-titre
        val currentChallenge = challengeManager.getCurrentChallenge()
        val flowerType = if (currentChallenge != null) {
            when {
                challengeManager.getMargueriteChallenges().any { it == currentChallenge } -> "MARGUERITE"
                challengeManager.getRoseChallenges().any { it == currentChallenge } -> "ROSIER"
                else -> "MARGUERITE"
            }
        } else {
            // Fallback: essayer de détecter selon les défis disponibles
            if (challengeManager.isFlowerUnlocked("ROSE")) "ROSIER" else "MARGUERITE"
        }
        
        canvas.drawText(flowerType, screenWidth / 2f, screenHeight * 0.32f, textPaint)
        
        // NOUVEAU: 3 boutons adaptatifs selon la fleur
        val challenges = when (flowerType) {
            "ROSIER" -> challengeManager.getRoseChallenges()
            else -> challengeManager.getMargueriteChallenges()
        }
        
        val buttonWidth = screenWidth * 0.25f
        val buttonHeight = screenHeight * 0.12f
        val startY = screenHeight * 0.45f
        
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
        
        // CORRIGÉ: Statut simple au lieu de getCompletionStatus()
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 60f
        textPaint.color = 0xFFFFFFFF.toInt()
        textPaint.isFakeBoldText = false
        canvas.drawText("Progression sauvegardée", screenWidth / 2f, screenHeight * 0.75f, textPaint)
    }
    
    // ==================== FONCTION UTILITAIRE POUR TEXTE MULTILIGNE ====================
    
    private fun drawMultilineText(canvas: Canvas, text: String, centerX: Float, startY: Float, textSize: Float, maxWidth: Float, textPaint: Paint) {
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = textSize
        textPaint.color = 0xFFFFFFFF.toInt()
        textPaint.isFakeBoldText = false
        
        val words = text.split(" ")
        var currentLine = ""
        var currentY = startY
        val lineHeight = textSize * 1.2f
        
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val testWidth = textPaint.measureText(testLine)
            
            if (testWidth <= maxWidth) {
                currentLine = testLine
            } else {
                // Dessiner la ligne actuelle et commencer une nouvelle ligne
                if (currentLine.isNotEmpty()) {
                    canvas.drawText(currentLine, centerX, currentY, textPaint)
                    currentY += lineHeight
                }
                currentLine = word
            }
        }
        
        // Dessiner la dernière ligne
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine, centerX, currentY, textPaint)
        }
    }
}
