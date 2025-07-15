package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.view.MotionEvent
import kotlin.math.*

class PlantInteractionHandler(
    private val screenWidth: Int, 
    private val screenHeight: Int,
    private val challengeManager: ChallengeManager
) {
    
    // ==================== GESTION DE LA CROISSANCE DES PLANTES ====================
    
    fun handlePlantGrowth(
        selectedFlowerType: String,
        lightState: OrganicLineView.LightState,
        force: Float,
        plantStem: PlantStem?,
        roseBushManager: RoseBushManager?,
        lupinManager: LupinManager?,
        irisManager: IrisManager?,
        orchideeManager: OrchideeManager? // ✅ NOUVEAU: Ajout OrchideeManager
    ) {
        when (selectedFlowerType) {
            "MARGUERITE" -> {
                handleMargueriteGrowth(lightState, force, plantStem)
            }
            "ROSE" -> {
                handleRoseGrowth(lightState, force, roseBushManager)
            }
            "LUPIN" -> {
                handleLupinGrowth(lightState, force, lupinManager)
            }
            "IRIS" -> {
                handleIrisGrowth(lightState, force, irisManager)
            }
            "ORCHIDEE" -> { // ✅ NOUVEAU: Gestion des orchidées
                handleOrchideeGrowth(lightState, force, orchideeManager)
            }
        }
    }
    
    private fun handleMargueriteGrowth(
        lightState: OrganicLineView.LightState,
        force: Float,
        plantStem: PlantStem?
    ) {
        when (lightState) {
            OrganicLineView.LightState.GREEN_GROW -> {
                val phaseTime = System.currentTimeMillis()
                plantStem?.processStemGrowth(force, phaseTime)
            }
            OrganicLineView.LightState.GREEN_LEAVES -> {
                plantStem?.processLeavesGrowth(force)
            }
            OrganicLineView.LightState.GREEN_FLOWER -> {
                plantStem?.processFlowerGrowth(force)
            }
            else -> {}
        }
    }
    
    private fun handleRoseGrowth(
        lightState: OrganicLineView.LightState,
        force: Float,
        roseBushManager: RoseBushManager?
    ) {
        when (lightState) {
            OrganicLineView.LightState.GREEN_GROW -> {
                roseBushManager?.processStemGrowth(force)
            }
            OrganicLineView.LightState.GREEN_LEAVES -> {
                roseBushManager?.processLeavesGrowth(force)
            }
            OrganicLineView.LightState.GREEN_FLOWER -> {
                roseBushManager?.processFlowerGrowth(force)
            }
            else -> {}
        }
    }
    
    private fun handleLupinGrowth(
        lightState: OrganicLineView.LightState,
        force: Float,
        lupinManager: LupinManager?
    ) {
        when (lightState) {
            OrganicLineView.LightState.GREEN_GROW -> {
                lupinManager?.processStemGrowth(force)
            }
            OrganicLineView.LightState.GREEN_LEAVES -> {
                lupinManager?.processLeavesGrowth(force)
            }
            OrganicLineView.LightState.GREEN_FLOWER -> {
                lupinManager?.processFlowerGrowth(force)
            }
            else -> {}
        }
    }
    
    private fun handleIrisGrowth(
        lightState: OrganicLineView.LightState,
        force: Float,
        irisManager: IrisManager?
    ) {
        when (lightState) {
            OrganicLineView.LightState.GREEN_GROW -> {
                irisManager?.processStemGrowth(force)
            }
            OrganicLineView.LightState.GREEN_LEAVES -> {
                irisManager?.processLeavesGrowth(force)
            }
            OrganicLineView.LightState.GREEN_FLOWER -> {
                irisManager?.processFlowerGrowth(force)
            }
            else -> {}
        }
    }
    
    // ✅ NOUVEAU: Gestion de la croissance des orchidées
    private fun handleOrchideeGrowth(
        lightState: OrganicLineView.LightState,
        force: Float,
        orchideeManager: OrchideeManager?
    ) {
        when (lightState) {
            OrganicLineView.LightState.GREEN_GROW -> {
                orchideeManager?.processStemGrowth(force)
            }
            OrganicLineView.LightState.GREEN_LEAVES -> {
                orchideeManager?.processLeafGrowth(force)
            }
            OrganicLineView.LightState.GREEN_FLOWER -> {
                orchideeManager?.processFlowerGrowth(force)
            }
            else -> {}
        }
    }
    
    // ==================== GESTION DE L'AFFICHAGE AVEC DISSOLUTION ====================
    
    fun drawPlants(
        canvas: Canvas,
        selectedFlowerType: String,
        lightState: OrganicLineView.LightState,
        plantStem: PlantStem?,
        roseBushManager: RoseBushManager?,
        lupinManager: LupinManager?,
        irisManager: IrisManager?,
        orchideeManager: OrchideeManager?, // ✅ NOUVEAU: Paramètre OrchideeManager
        uiDrawing: UIDrawingManager,
        dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null
    ) {
        when (selectedFlowerType) {
            "MARGUERITE" -> drawPlantStem(canvas, lightState, plantStem, uiDrawing, dissolveInfo)
            "ROSE" -> drawRoseBush(canvas, roseBushManager, dissolveInfo)
            "LUPIN" -> drawLupin(canvas, lupinManager, dissolveInfo)
            "IRIS" -> drawIris(canvas, irisManager, dissolveInfo)
            "ORCHIDEE" -> drawOrchidee(canvas, orchideeManager, dissolveInfo) // ✅ NOUVEAU: Rendu des orchidées
        }
    }
    
    private fun drawPlantStem(
        canvas: Canvas,
        lightState: OrganicLineView.LightState,
        stem: PlantStem?,
        uiDrawing: UIDrawingManager,
        dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null
    ) {
        stem ?: return
        
        // Dessiner les fleurs de profil/arrière DERRIÈRE les tiges
        if (lightState == OrganicLineView.LightState.GREEN_FLOWER || 
            lightState == OrganicLineView.LightState.RED) {
            uiDrawing.drawBackgroundFlowers(canvas, stem.getFlowers(), stem, dissolveInfo)
        }
        
        // Dessiner la tige principale
        uiDrawing.drawMainStem(canvas, stem.mainStem, dissolveInfo)
        
        // Dessiner les branches
        uiDrawing.drawBranches(canvas, stem.branches, dissolveInfo)
        
        // Dessiner les feuilles pendant GREEN_LEAVES et après
        if (lightState == OrganicLineView.LightState.GREEN_LEAVES || 
            lightState == OrganicLineView.LightState.GREEN_FLOWER || 
            lightState == OrganicLineView.LightState.RED) {
            uiDrawing.drawLeaves(canvas, stem.getLeaves(), stem, dissolveInfo)
        }
        
        // Dessiner les fleurs de face/3-4 PAR-DESSUS les tiges
        if (lightState == OrganicLineView.LightState.GREEN_FLOWER || 
            lightState == OrganicLineView.LightState.RED) {
            uiDrawing.drawForegroundFlowers(canvas, stem.getFlowers(), stem, dissolveInfo)
        }
    }
    
    private fun drawRoseBush(canvas: Canvas, roseBushManager: RoseBushManager?, dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null) {
        roseBushManager?.let { manager ->
            val branchPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
            }
            
            val leafPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            
            val flowerPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            
            manager.drawRoseBush(canvas, branchPaint, leafPaint, flowerPaint, dissolveInfo)
        }
    }
    
    private fun drawLupin(canvas: Canvas, lupinManager: LupinManager?, dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null) {
        lupinManager?.let { manager ->
            val stemPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
            }
            
            val leafPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            
            val flowerPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            
            manager.drawLupin(canvas, stemPaint, leafPaint, flowerPaint, dissolveInfo)
        }
    }
    
    private fun drawIris(canvas: Canvas, irisManager: IrisManager?, dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null) {
        irisManager?.let { manager ->
            val stemPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
            }
            
            val leafPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            
            val flowerPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            
            val renderer = IrisRenderer()
            renderer.drawIris(canvas, stemPaint, leafPaint, flowerPaint, manager.getStems(), manager.getFlowers(), dissolveInfo)
        }
    }
    
    // ✅ NOUVEAU: Fonction de rendu des orchidées
    private fun drawOrchidee(canvas: Canvas, orchideeManager: OrchideeManager?, dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null) {
        orchideeManager?.let { manager ->
            val stemPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeWidth = 5f
                color = android.graphics.Color.rgb(40, 120, 40) // Vert foncé pour tiges d'orchidées
            }
            
            val leafPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
                color = android.graphics.Color.rgb(60, 140, 60) // Vert moyen pour feuilles
            }
            
            val flowerPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
                // Couleur sera définie par la génétique de chaque fleur
            }
            
            manager.drawOrchidees(canvas, stemPaint, leafPaint, flowerPaint, dissolveInfo)
        }
    }
    
    // ==================== DÉTECTION D'INTERACTIONS ====================
    
    fun detectFlowerSelection(
        event: MotionEvent,
        width: Int,
        height: Int,
        challengeManager: ChallengeManager
    ): String? {
        val unlockedFlowers = getUnlockedFlowers(challengeManager)
        val flowerButtonRadius = width * 0.18f
        val centerX = width / 2f
        val buttonY = height / 2f
        
        when (unlockedFlowers.size) {
            1 -> {
                val distance = sqrt((event.x - centerX).pow(2) + (event.y - buttonY).pow(2))
                if (distance <= flowerButtonRadius * 1.5f) return "MARGUERITE"
            }
            2 -> {
                val spacing = flowerButtonRadius * 2.8f
                val margueriteX = centerX - spacing / 2f
                val roseX = centerX + spacing / 2f
                
                val margueriteDistance = sqrt((event.x - margueriteX).pow(2) + (event.y - buttonY).pow(2))
                val roseDistance = sqrt((event.x - roseX).pow(2) + (event.y - buttonY).pow(2))
                
                if (margueriteDistance <= flowerButtonRadius * 1.5f) return "MARGUERITE"
                if (roseDistance <= flowerButtonRadius * 1.5f) return "ROSE"
            }
            3 -> {
                val spacing = flowerButtonRadius * 2.0f
                val topY = buttonY - spacing * 0.3f
                val bottomY = buttonY + spacing * 0.3f
                
                val positions = listOf(
                    Triple("MARGUERITE", centerX, topY),
                    Triple("ROSE", centerX - spacing / 2f, bottomY),
                    Triple("LUPIN", centerX + spacing / 2f, bottomY)
                )
                
                for ((flower, x, y) in positions) {
                    val distance = sqrt((event.x - x).pow(2) + (event.y - y).pow(2))
                    if (distance <= flowerButtonRadius * 1.5f) return flower
                }
            }
            4 -> { // ✅ NOUVEAU: Configuration à 4 fleurs avec IRIS
                val spacing = flowerButtonRadius * 2.5f
                val positions = listOf(
                    Triple("MARGUERITE", centerX - spacing / 2f, buttonY - spacing / 2f),
                    Triple("ROSE", centerX + spacing / 2f, buttonY - spacing / 2f),
                    Triple("LUPIN", centerX - spacing / 2f, buttonY + spacing / 2f),
                    Triple("IRIS", centerX + spacing / 2f, buttonY + spacing / 2f)
                )
                
                for ((flower, x, y) in positions) {
                    val distance = sqrt((event.x - x).pow(2) + (event.y - y).pow(2))
                    if (distance <= flowerButtonRadius * 1.5f) return flower
                }
            }
            else -> { // ✅ MODIFIÉ: Configuration à 5 fleurs avec ORCHIDEE
                val spacing = flowerButtonRadius * 2.2f
                val positions = listOf(
                    Triple("MARGUERITE", centerX, buttonY - spacing * 0.8f), // Haut centre
                    Triple("ROSE", centerX - spacing * 0.7f, buttonY - spacing * 0.2f), // Gauche haut
                    Triple("LUPIN", centerX + spacing * 0.7f, buttonY - spacing * 0.2f), // Droite haut
                    Triple("IRIS", centerX - spacing * 0.7f, buttonY + spacing * 0.4f), // Gauche bas
                    Triple("ORCHIDEE", centerX + spacing * 0.7f, buttonY + spacing * 0.4f) // Droite bas
                )
                
                for ((flower, x, y) in positions) {
                    val distance = sqrt((event.x - x).pow(2) + (event.y - y).pow(2))
                    if (distance <= flowerButtonRadius * 1.5f) return flower
                }
            }
        }
        return null
    }
    
    fun detectModeSelection(event: MotionEvent, width: Int, height: Int): String? {
        val buttonRadius = width * 0.15f
        val spacing = buttonRadius * 2.5f
        val centerX = width / 2f
        val zenButtonX = centerX - spacing / 2f
        val defiButtonX = centerX + spacing / 2f
        val buttonY = height / 2f
        
        val zenDistance = sqrt((event.x - zenButtonX).pow(2) + (event.y - buttonY).pow(2))
        val defiDistance = sqrt((event.x - defiButtonX).pow(2) + (event.y - buttonY).pow(2))
        
        return when {
            zenDistance <= buttonRadius -> "ZEN"
            defiDistance <= buttonRadius -> "DÉFI"
            else -> null
        }
    }
    
    fun detectChallengeSelection(
        event: MotionEvent,
        width: Int,
        height: Int,
        selectedFlowerType: String,
        challengeManager: ChallengeManager
    ): Int {
        val buttonWidth = width * 0.25f
        val buttonHeight = height * 0.12f
        val startY = height * 0.45f
        val centerX = width / 2f
        
        for (i in 1..3) {
            val buttonY = startY + (i - 1) * (buttonHeight + 30f)
            val buttonLeft = centerX - buttonWidth / 2f
            val buttonRight = centerX + buttonWidth / 2f
            val buttonTop = buttonY - buttonHeight / 2f
            val buttonBottom = buttonY + buttonHeight / 2f
            
            if (event.x >= buttonLeft && event.x <= buttonRight && 
                event.y >= buttonTop && event.y <= buttonBottom) {
                
                val challenges = when (selectedFlowerType) {
                    "MARGUERITE" -> challengeManager.getMargueriteChallenges()
                    "ROSE" -> challengeManager.getRoseChallenges()
                    "LUPIN" -> challengeManager.getLupinChallenges()
                    "IRIS" -> challengeManager.getIrisChallenges()
                    "ORCHIDEE" -> challengeManager.getOrchideeChallenges() // ✅ NOUVEAU: Défis orchidées
                    else -> challengeManager.getMargueriteChallenges()
                }
                
                val challenge = challenges.find { it.id == i }
                if (challenge?.isUnlocked == true) {
                    return i
                }
            }
        }
        return -1
    }
    
    // ==================== FONCTIONS UTILITAIRES ====================
    
    fun shouldShowResetButton(
        selectedFlowerType: String,
        plantStem: PlantStem?,
        roseBushManager: RoseBushManager?,
        lupinManager: LupinManager?,
        irisManager: IrisManager?,
        orchideeManager: OrchideeManager? // ✅ NOUVEAU: Paramètre OrchideeManager
    ): Boolean {
        return when (selectedFlowerType) {
            "MARGUERITE" -> (plantStem?.getStemHeight() ?: 0f) > 30f
            "ROSE" -> true // Les roses ont toujours un bouton reset
            "LUPIN" -> true // Les lupins ont toujours un bouton reset
            "IRIS" -> true // Les iris ont toujours un bouton reset
            "ORCHIDEE" -> true // ✅ NOUVEAU: Les orchidées ont toujours un bouton reset
            else -> true
        }
    }
    
    private fun getUnlockedFlowers(challengeManager: ChallengeManager): List<String> {
        val flowers = mutableListOf("MARGUERITE")
        
        if (challengeManager.isFlowerUnlocked("ROSE")) flowers.add("ROSE")
        if (challengeManager.isFlowerUnlocked("LUPIN")) flowers.add("LUPIN")
        if (challengeManager.isFlowerUnlocked("IRIS")) flowers.add("IRIS")
        if (challengeManager.isFlowerUnlocked("ORCHIDEE")) flowers.add("ORCHIDEE") // ✅ NOUVEAU: Déverrouillage orchidées
        
        return flowers
    }
    
    // ✅ NOUVEAU: Fonctions utilitaires spécifiques aux orchidées
    fun getOrchideeStats(orchideeManager: OrchideeManager?): String {
        return orchideeManager?.getOrchideeStats() ?: "Aucune orchidée"
    }
    
    fun getCurrentOrchideeSpecies(orchideeManager: OrchideeManager?): List<String> {
        return orchideeManager?.getCurrentSpecies() ?: emptyList()
    }
    
    // ✅ NOUVEAU: Réinitialisation spécifique aux orchidées
    fun resetOrchidees(orchideeManager: OrchideeManager?) {
        orchideeManager?.reset()
    }
    
    // ✅ NOUVEAU: Initialisation des orchidées
    fun initializeOrchidees(orchideeManager: OrchideeManager?, centerX: Float, bottomY: Float) {
        orchideeManager?.initialize(centerX, bottomY)
    }
}
