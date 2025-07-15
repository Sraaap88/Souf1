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
                // ✅ MODIFIÉ: FlowerUIComponents gère maintenant aussi les orchidées
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
        
        // ✅ MODIFIÉ: Texte adapté selon le type de plante
        resetTextPaint.textAlign = Paint.Align.CENTER
        resetTextPaint.textSize = 80f
        resetTextPaint.color = 0xFF000000.toInt()
        resetTextPaint.isFakeBoldText = true
        
        val phaseText = when (lightState) {
            OrganicLineView.LightState.GREEN_GROW -> getGrowPhaseText(challengeManager)
            OrganicLineView.LightState.GREEN_LEAVES -> getLeavesPhaseText(challengeManager)
            OrganicLineView.LightState.GREEN_FLOWER -> getFlowerPhaseText(challengeManager)
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
        
        // ✅ NOUVEAU: Affichage spécial pour les orchidées avec conseils d'espèces
        displayOrchideeSpecialInfo(canvas, challengeManager)
    }
    
    // ✅ NOUVEAU: Textes adaptés selon le type de plante
    private fun getGrowPhaseText(challengeManager: ChallengeManager): String {
        return when (challengeManager.getCurrentFlowerType()) {
            "ORCHIDEE" -> "PSEUDO-\nBULBE"
            "IRIS" -> "RHIZOME"
            "LUPIN" -> "TIGE"
            "ROSE" -> "BRANCHES"
            else -> "TIGE"
        }
    }
    
    private fun getLeavesPhaseText(challengeManager: ChallengeManager): String {
        return when (challengeManager.getCurrentFlowerType()) {
            "ORCHIDEE" -> "FEUILLES\nBASALES"
            "IRIS" -> "FEUILLES\nEN ÉVENTAIL"
            "LUPIN" -> "FEUILLES\nPALMÉES"
            "ROSE" -> "FEUILLES\nCOMPOSÉES"
            else -> "FEUILLES"
        }
    }
    
    private fun getFlowerPhaseText(challengeManager: ChallengeManager): String {
        return when (challengeManager.getCurrentFlowerType()) {
            "ORCHIDEE" -> "FLEURS\nPROCÉDURALES"
            "IRIS" -> "FLEURS\nTRIANGULAIRES"
            "LUPIN" -> "ÉPIS\nFLORAUX"
            "ROSE" -> "BOUTONS\n& ROSES"
            else -> "FLEUR"
        }
    }
    
    // ✅ NOUVEAU: Affichage d'informations spéciales pour les orchidées
    private fun displayOrchideeSpecialInfo(canvas: Canvas, challengeManager: ChallengeManager) {
        if (challengeManager.getCurrentFlowerType() != "ORCHIDEE") return
        
        // Afficher des conseils spécifiques aux orchidées en bas de l'écran
        resetTextPaint.textAlign = Paint.Align.CENTER
        resetTextPaint.textSize = 28f
        resetTextPaint.color = 0x99FF69B4.toInt()  // Rose semi-transparent
        resetTextPaint.isFakeBoldText = false
        
        val orchideeAdvice = when (challengeManager.getCurrentChallenge()?.id) {
            1 -> "Saccades régulières pour activer les espèces"
            2 -> "Souffle délicat pour orchidées fragiles"
            3 -> "Patience : 6 espèces différentes"
            else -> "Respirez calmement pour 6 espèces d'orchidées"
        }
        
        canvas.drawText(orchideeAdvice, screenWidth / 2f, screenHeight - 80f, resetTextPaint)
        
        // Indicateur d'espèce active (si disponible)
        resetTextPaint.textSize = 24f
        resetTextPaint.color = 0x77FFFFFF.toInt()
        canvas.drawText("Espèces procédurales en croissance...", screenWidth / 2f, screenHeight - 50f, resetTextPaint)
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
    
    // ==================== DÉLÉGATION POUR LE RENDU DES PLANTES ====================
    
    fun drawMainStem(canvas: Canvas, mainStem: List<PlantStem.StemPoint>, dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null) {
        flowerComponents.drawMainStemWithDissolution(canvas, mainStem, dissolveInfo)
    }
    
    fun drawBranches(canvas: Canvas, branches: List<PlantStem.Branch>, dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null) {
        flowerComponents.drawBranchesWithDissolution(canvas, branches, dissolveInfo)
    }
    
    fun drawLeaves(canvas: Canvas, leaves: List<PlantLeavesManager.Leaf>, stem: PlantStem, dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null) {
        flowerComponents.drawLeavesWithDissolution(canvas, leaves, stem, dissolveInfo)
    }
    
    fun drawBackgroundFlowers(canvas: Canvas, flowers: List<FlowerManager.Flower>, stem: PlantStem, dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null) {
        flowerComponents.drawBackgroundFlowersWithDissolution(canvas, flowers, stem, dissolveInfo)
    }
    
    fun drawForegroundFlowers(canvas: Canvas, flowers: List<FlowerManager.Flower>, stem: PlantStem, dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null) {
        flowerComponents.drawForegroundFlowersWithDissolution(canvas, flowers, stem, dissolveInfo)
    }
    
    // ==================== NOUVELLES FONCTIONS ORCHIDÉES ====================
    
    // ✅ NOUVEAU: Fonctions de délégation spécifiques aux orchidées
    fun drawOrchideeStem(canvas: Canvas, stems: List<OrchideeStem>, dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null) {
        // Déléguer vers FlowerUIComponents si il y a une fonction spécialisée
        // Sinon, utilisation basique avec Paint
        val stemPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            strokeWidth = 5f
            color = android.graphics.Color.rgb(40, 120, 40)
            
            // Appliquer dissolution si présente
            dissolveInfo?.let { info ->
                if (info.progress > 0f) {
                    alpha = ((1f - info.progress) * 255f).toInt().coerceIn(0, 255)
                    if (info.stemsCollapsing) {
                        strokeWidth *= (1f - info.progress * 0.5f)
                    }
                }
            }
        }
        
        for (stem in stems) {
            if (stem.segments.size >= 2) {
                for (i in 1 until stem.segments.size) {
                    val start = stem.segments[i - 1]
                    val end = stem.segments[i]
                    canvas.drawLine(start.x, start.y, end.x, end.y, stemPaint)
                }
            }
        }
    }
    
    fun drawOrchideeLeaves(canvas: Canvas, stems: List<OrchideeStem>, dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null) {
        val leafPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = android.graphics.Color.rgb(60, 140, 60)
            
            // Appliquer dissolution si présente
            dissolveInfo?.let { info ->
                if (info.progress > 0f) {
                    alpha = ((1f - info.progress) * 255f).toInt().coerceIn(0, 255)
                    if (info.leavesShriveling) {
                        val shrivelingFactor = info.progress
                        val red = (60 + (180 - 60) * shrivelingFactor).toInt()
                        val green = (140 + (150 - 140) * shrivelingFactor * 0.5f).toInt()
                        val blue = (60 * (1f - shrivelingFactor * 0.9f)).toInt()
                        color = android.graphics.Color.rgb(red, green, blue)
                    }
                }
            }
        }
        
        for (stem in stems) {
            // Dessiner feuilles basales
            for (leaf in stem.leaves) {
                if (leaf.growthProgress > 0f) {
                    drawSingleOrchideeLeaf(canvas, leaf, leafPaint, dissolveInfo)
                }
            }
            
            // Dessiner feuilles caulinaires
            for (leaf in stem.caulineLeaves) {
                if (leaf.growthProgress > 0f) {
                    drawSingleOrchideeLeaf(canvas, leaf, leafPaint, dissolveInfo)
                }
            }
        }
    }
    
    private fun drawSingleOrchideeLeaf(canvas: Canvas, leaf: OrchideeLeaf, paint: Paint, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        canvas.save()
        canvas.translate(leaf.attachmentPoint.x, leaf.attachmentPoint.y)
        canvas.rotate(leaf.angle)
        
        var currentLength = leaf.length * leaf.growthProgress
        var currentWidth = leaf.width * leaf.growthProgress
        
        // Réduire la taille si dissolution
        dissolveInfo?.let { info ->
            if (info.leavesShriveling && info.progress > 0f) {
                val shrinkFactor = 1f - info.progress * 0.7f
                currentLength *= shrinkFactor
                currentWidth *= shrinkFactor
            }
        }
        
        // Dessiner selon le type de feuille
        when (leaf.leafType) {
            OrchideeLeafType.STRAP_SHAPED -> {
                // Feuille en lanière
                canvas.drawRoundRect(
                    -currentWidth / 2f, 0f, 
                    currentWidth / 2f, -currentLength,
                    currentWidth * 0.2f, currentWidth * 0.2f,
                    paint
                )
            }
            OrchideeLeafType.OVAL_THICK -> {
                // Feuille ovale épaisse
                canvas.drawOval(
                    -currentWidth / 2f, 0f,
                    currentWidth / 2f, -currentLength,
                    paint
                )
            }
            OrchideeLeafType.NEEDLE_THIN -> {
                // Feuille aiguille
                canvas.drawRoundRect(
                    -currentWidth * 0.2f, 0f,
                    currentWidth * 0.2f, -currentLength,
                    currentWidth * 0.1f, currentWidth * 0.1f,
                    paint
                )
            }
            OrchideeLeafType.BROAD_FLAT -> {
                // Feuille large et plate
                canvas.drawRoundRect(
                    -currentWidth / 2f, 0f,
                    currentWidth / 2f, -currentLength * 0.8f,
                    currentWidth * 0.1f, currentWidth * 0.1f,
                    paint
                )
            }
        }
        
        canvas.restore()
    }
    
    fun drawOrchideeFlowers(canvas: Canvas, flowers: List<OrchideeFlower>, dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null) {
        // Les fleurs d'orchidées sont rendues via OrchideeRenderer
        // Cette fonction est pour compatibilité UIDrawingManager
        
        val flowerPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            
            // Appliquer dissolution si présente
            dissolveInfo?.let { info ->
                if (info.progress > 0f) {
                    alpha = ((1f - info.progress) * 255f).toInt().coerceIn(0, 255)
                }
            }
        }
        
        // Rendu basique des fleurs (le vrai rendu est fait par OrchideeFlowerDrawer)
        for (flower in flowers) {
            if (flower.bloomProgress > 0f) {
                flowerPaint.color = flower.genetics.colorPalette.primary
                val size = 60f * flower.sizeMultiplier * flower.bloomProgress
                
                // Réduire si dissolution
                var currentSize = size
                dissolveInfo?.let { info ->
                    if (info.flowersPetalsWilting && info.progress > 0f) {
                        currentSize *= (1f - info.progress * 0.6f)
                    }
                }
                
                canvas.drawCircle(flower.position.x, flower.position.y, currentSize * 0.5f, flowerPaint)
            }
        }
    }
    
    // ==================== FONCTION UTILITAIRE ====================
    
    fun isPointInTargetZone(x: Float, y: Float): Boolean {
        return flowerComponents.isPointInMargueriteTargetZone(x, y)
    }
    
    // ✅ NOUVEAU: Fonctions utilitaires pour orchidées
    fun getOrchideeDisplayName(species: OrchideeSpecies): String {
        return when (species) {
            OrchideeSpecies.PHALAENOPSIS -> "Phalaenopsis\n(Papillon)"
            OrchideeSpecies.CATTLEYA -> "Cattleya\n(Royale)"
            OrchideeSpecies.DENDROBIUM -> "Dendrobium\n(Grappes)"
            OrchideeSpecies.VANDA -> "Vanda\n(Bleue)"
            OrchideeSpecies.ONCIDIUM -> "Oncidium\n(Danseuse)"
            OrchideeSpecies.CYMBIDIUM -> "Cymbidium\n(Bateau)"
        }
    }
    
    fun shouldShowOrchideeSpeciesInfo(challengeManager: ChallengeManager): Boolean {
        return challengeManager.getCurrentFlowerType() == "ORCHIDEE" && 
               challengeManager.getCurrentChallenge()?.id == 3 // Défi spécial orchidées
    }
}
