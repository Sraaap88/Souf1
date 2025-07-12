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
        
        // CORRIGÉ: Sous-titre adaptatif selon la fleur ACTUELLE
        textPaint.textSize = 60f
        textPaint.isFakeBoldText = false
        
        // NOUVEAU: Utiliser getCurrentFlowerType() du ChallengeManager
        val flowerType = challengeManager.getCurrentFlowerType()
        
        // Afficher le nom de la fleur actuelle
        val displayName = when (flowerType) {
            "MARGUERITE" -> "MARGUERITE"
            "ROSE" -> "ROSIER"
            "LUPIN" -> "LUPIN"
            "IRIS" -> "IRIS"  // NOUVEAU: Ajouter l'iris
            else -> "MARGUERITE" // Fallback
        }
        
        canvas.drawText(displayName, screenWidth / 2f, screenHeight * 0.32f, textPaint)
        
        // CORRIGÉ: Charger les défis selon le type de fleur ACTUEL
        val challenges = when (flowerType) {
            "MARGUERITE" -> challengeManager.getMargueriteChallenges()
            "ROSE" -> challengeManager.getRoseChallenges()
            "LUPIN" -> challengeManager.getLupinChallenges()
            "IRIS" -> challengeManager.getIrisChallenges()  // NOUVEAU: Ajouter l'iris
            else -> challengeManager.getMargueriteChallenges() // Fallback
        }
        
        val buttonWidth = screenWidth * 0.25f
        val buttonHeight = screenHeight * 0.12f
        val startY = screenHeight * 0.45f
        
        // Afficher les 3 défis de la fleur actuelle
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
        
        // NOUVEAU: Affichage d'informations de debug (optionnel)
        if (challenges.isEmpty()) {
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = 40f
            textPaint.color = 0xFFFF6666.toInt()
            canvas.drawText("Debug: Aucun défi trouvé pour $flowerType", screenWidth / 2f, screenHeight * 0.85f, textPaint)
        }
    }
    
    fun drawChallengeBrief(canvas: Canvas, challengeManager: ChallengeManager, timeRemaining: Long, textPaint: Paint) {
        val challenge = challengeManager.getCurrentChallenge()
        val flowerType = challengeManager.getCurrentFlowerType()
        
        // Titre avec le nom de la fleur
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 120f
        textPaint.color = 0xFFFFFFFF.toInt()
        textPaint.isFakeBoldText = true
        canvas.drawText(challenge?.title ?: "DÉFI", screenWidth / 2f, screenHeight * 0.25f, textPaint)
        
        // NOUVEAU: Sous-titre avec le type de fleur
        textPaint.textSize = 60f
        textPaint.color = 0xFFFFD700.toInt()
        textPaint.isFakeBoldText = false
        val displayName = when (flowerType) {
            "MARGUERITE" -> "Marguerite"
            "ROSE" -> "Rosier"
            "LUPIN" -> "Lupin"
            "IRIS" -> "Iris"  // NOUVEAU: Ajouter l'iris
            else -> "Plante"
        }
        canvas.drawText(displayName, screenWidth / 2f, screenHeight * 0.32f, textPaint)
        
        // Description avec retour à la ligne automatique
        val description = challenge?.description ?: "Description à venir"
        drawMultilineText(canvas, description, screenWidth / 2f, screenHeight * 0.45f, 70f, screenWidth * 0.8f, textPaint)
        
        // Conseil spécial selon le type de défi et de fleur
        when {
            challenge?.id == 2 && flowerType == "MARGUERITE" -> {
                textPaint.textAlign = Paint.Align.CENTER
                textPaint.textSize = 55f
                textPaint.color = 0xFFFFD700.toInt()  // Jaune
                textPaint.isFakeBoldText = false
                canvas.drawText("Technique: souffle très doux", screenWidth / 2f, screenHeight * 0.62f, textPaint)
            }
            challenge?.id == 1 && flowerType == "LUPIN" -> {
                textPaint.textAlign = Paint.Align.CENTER
                textPaint.textSize = 55f
                textPaint.color = 0xFFFFD700.toInt()
                textPaint.isFakeBoldText = false
                canvas.drawText("Astuce: saccades pour différentes couleurs", screenWidth / 2f, screenHeight * 0.62f, textPaint)
            }
            challenge?.id == 2 && flowerType == "LUPIN" -> {
                textPaint.textAlign = Paint.Align.CENTER
                textPaint.textSize = 55f
                textPaint.color = 0xFFFFD700.toInt()
                textPaint.isFakeBoldText = false
                canvas.drawText("Astuce: saccades pour créer plusieurs tiges", screenWidth / 2f, screenHeight * 0.62f, textPaint)
            }
            challenge?.id == 2 && flowerType == "ROSE" -> {
                textPaint.textAlign = Paint.Align.CENTER
                textPaint.textSize = 55f
                textPaint.color = 0xFFFFD700.toInt()
                textPaint.isFakeBoldText = false
                canvas.drawText("Astuce: saccades pour créer des divisions", screenWidth / 2f, screenHeight * 0.62f, textPaint)
            }
            // NOUVEAU: Conseils pour l'iris
            challenge?.id == 1 && flowerType == "IRIS" -> {
                textPaint.textAlign = Paint.Align.CENTER
                textPaint.textSize = 55f
                textPaint.color = 0xFFFFD700.toInt()
                textPaint.isFakeBoldText = false
                canvas.drawText("Astuce: tiges droites élancées au centre", screenWidth / 2f, screenHeight * 0.62f, textPaint)
            }
            challenge?.id == 2 && flowerType == "IRIS" -> {
                textPaint.textAlign = Paint.Align.CENTER
                textPaint.textSize = 55f
                textPaint.color = 0xFFFFD700.toInt()
                textPaint.isFakeBoldText = false
                canvas.drawText("Astuce: saccades pour ramifications", screenWidth / 2f, screenHeight * 0.62f, textPaint)
            }
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
        val flowerType = challengeManager.getCurrentFlowerType()
        
        if (result != null) {
            // Couleur selon le succès
            val resultColor = if (result.success) 0xFF00FF00.toInt() else 0xFFFF0000.toInt()
            val resultText = if (result.success) "DÉFI RÉUSSI!" else "DÉFI ÉCHOUÉ!"
            
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = 150f
            textPaint.color = resultColor
            textPaint.isFakeBoldText = true
            canvas.drawText(resultText, screenWidth / 2f, screenHeight * 0.3f, textPaint)
            
            // NOUVEAU: Sous-titre avec le type de fleur
            textPaint.textSize = 70f
            textPaint.color = 0xFFFFFFFF.toInt()
            textPaint.isFakeBoldText = false
            val displayName = when (flowerType) {
                "MARGUERITE" -> "Marguerite"
                "ROSE" -> "Rosier"
                "LUPIN" -> "Lupin"
                "IRIS" -> "Iris"  // NOUVEAU: Ajouter l'iris
                else -> "Plante"
            }
            canvas.drawText("$displayName - ${result.challenge.title}", screenWidth / 2f, screenHeight * 0.42f, textPaint)
            
            // Message détaillé avec retour à la ligne
            drawMultilineText(canvas, result.message, screenWidth / 2f, screenHeight * 0.55f, 60f, screenWidth * 0.9f, textPaint)
            
            // Message spécial selon le type de défi et succès
            when {
                result.challenge.id == 2 && result.success && flowerType == "MARGUERITE" -> {
                    textPaint.textAlign = Paint.Align.CENTER
                    textPaint.textSize = 50f
                    textPaint.color = 0xFFFFD700.toInt()
                    canvas.drawText("Excellente maîtrise du souffle!", screenWidth / 2f, screenHeight * 0.68f, textPaint)
                }
                result.challenge.id == 1 && result.success && flowerType == "LUPIN" -> {
                    textPaint.textAlign = Paint.Align.CENTER
                    textPaint.textSize = 50f
                    textPaint.color = 0xFFFFD700.toInt()
                    canvas.drawText("Magnifique palette de couleurs!", screenWidth / 2f, screenHeight * 0.68f, textPaint)
                }
                result.challenge.id == 2 && result.success && flowerType == "LUPIN" -> {
                    textPaint.textAlign = Paint.Align.CENTER
                    textPaint.textSize = 50f
                    textPaint.color = 0xFFFFD700.toInt()
                    canvas.drawText("Maître jardinier de lupins!", screenWidth / 2f, screenHeight * 0.68f, textPaint)
                }
                result.challenge.id == 2 && result.success && flowerType == "ROSE" -> {
                    textPaint.textAlign = Paint.Align.CENTER
                    textPaint.textSize = 50f
                    textPaint.color = 0xFFFFD700.toInt()
                    canvas.drawText("Expert en multiplication des rosiers!", screenWidth / 2f, screenHeight * 0.68f, textPaint)
                }
                // NOUVEAU: Messages pour l'iris
                result.challenge.id == 1 && result.success && flowerType == "IRIS" -> {
                    textPaint.textAlign = Paint.Align.CENTER
                    textPaint.textSize = 50f
                    textPaint.color = 0xFFFFD700.toInt()
                    canvas.drawText("Élégance parfaite des iris!", screenWidth / 2f, screenHeight * 0.68f, textPaint)
                }
                result.challenge.id == 2 && result.success && flowerType == "IRIS" -> {
                    textPaint.textAlign = Paint.Align.CENTER
                    textPaint.textSize = 50f
                    textPaint.color = 0xFFFFD700.toInt()
                    canvas.drawText("Maître des ramifications d'iris!", screenWidth / 2f, screenHeight * 0.68f, textPaint)
                }
                result.challenge.id == 3 && result.success && flowerType == "IRIS" -> {
                    textPaint.textAlign = Paint.Align.CENTER
                    textPaint.textSize = 50f
                    textPaint.color = 0xFFFFD700.toInt()
                    canvas.drawText("Jardinier expert en iris!", screenWidth / 2f, screenHeight * 0.68f, textPaint)
                }
            }
            
            // Message de déblocage si applicable
            if (result.success) {
                when {
                    flowerType == "MARGUERITE" && result.challenge.id == 3 -> {
                        textPaint.textAlign = Paint.Align.CENTER
                        textPaint.textSize = 55f
                        textPaint.color = 0xFFFF69B4.toInt() // Rose
                        canvas.drawText("🌹 ROSIER DÉBLOQUÉ! 🌹", screenWidth / 2f, screenHeight * 0.75f, textPaint)
                    }
                    flowerType == "ROSE" && result.challenge.id == 3 -> {
                        textPaint.textAlign = Paint.Align.CENTER
                        textPaint.textSize = 55f
                        textPaint.color = 0xFF9370DB.toInt() // Violet
                        canvas.drawText("🌼 LUPIN DÉBLOQUÉ! 🌼", screenWidth / 2f, screenHeight * 0.75f, textPaint)
                    }
                    flowerType == "LUPIN" && result.challenge.id == 3 -> {
                        textPaint.textAlign = Paint.Align.CENTER
                        textPaint.textSize = 55f
                        textPaint.color = 0xFF4169E1.toInt() // Bleu
                        canvas.drawText("🌺 IRIS DÉBLOQUÉ! 🌺", screenWidth / 2f, screenHeight * 0.75f, textPaint)
                    }
                    // NOUVEAU: Message de déblocage pour l'iris
                    flowerType == "IRIS" && result.challenge.id == 3 -> {
                        textPaint.textAlign = Paint.Align.CENTER
                        textPaint.textSize = 55f
                        textPaint.color = 0xFFFF1493.toInt() // Rose profond
                        canvas.drawText("🌸 ORCHIDÉE DÉBLOQUÉE! 🌸", screenWidth / 2f, screenHeight * 0.75f, textPaint)
                    }
                }
            }
        } else {
            // Fallback si pas de résultat
            textPaint.textAlign = Paint.Align.CENTER
            textPaint.textSize = 150f
            textPaint.color = 0xFF00FF00.toInt()
            textPaint.isFakeBoldText = true
            canvas.drawText("DÉFI TERMINÉ!", screenWidth / 2f, screenHeight * 0.4f, textPaint)
        }
        
        // Statut de sauvegarde
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.textSize = 60f
        textPaint.color = 0xFFFFFFFF.toInt()
        textPaint.isFakeBoldText = false
        canvas.drawText("Progression sauvegardée", screenWidth / 2f, screenHeight * 0.85f, textPaint)
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
