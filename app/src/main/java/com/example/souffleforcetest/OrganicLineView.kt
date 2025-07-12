package com.example.souffleforcetest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class OrganicLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    // ==================== ÉTATS DU SYSTÈME ====================
    
    private var showResetButton = false
    private var resetButtonX = 0f
    private var resetButtonY = 0f
    private val resetButtonRadius = 175f
    
    private var lightState = LightState.START
    private var stateStartTime = 0L
    private var selectedMode = ""  // "ZEN" ou "DÉFI"
    private var selectedFlowerType = "MARGUERITE"  // "MARGUERITE", "ROSE", "LUPIN", "IRIS", etc.
    
    // NOUVEAU: Système de cheat code
    private var cheatTapCount = 0  // Nombre de taps en haut à droite
    private var lastCheatTapTime = 0L
    private val cheatTimeWindow = 3000L  // 3 secondes pour faire 3 taps
    private val cheatRequiredTaps = 3  // 3 taps requis
    
    // ==================== LOGIQUE DE PLANTE ====================
    
    private var plantStem: PlantStem? = null
    private var roseBushManager: RoseBushManager? = null
    private var lupinManager: LupinManager? = null  // NOUVEAU: Gestionnaire du lupin
    private lateinit var uiDrawing: UIDrawingManager
    private val challengeManager = ChallengeManager(context)
    private var selectedChallengeId = -1
    
    // NOUVEAU: États réorganisés
    enum class LightState {
        START,           // Choix de fleur maintenant
        MODE_CHOICE,     // Choix ZEN/DÉFI (ancien START)
        CHALLENGE_SELECTION, 
        CHALLENGE_BRIEF, 
        YELLOW, 
        GREEN_GROW, 
        GREEN_LEAVES, 
        GREEN_FLOWER, 
        CHALLENGE_RESULT, 
        RED
    }
    
    // ==================== GESTION DE L'ÉCRAN ====================
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        resetButtonX = w - resetButtonRadius - 50f
        resetButtonY = resetButtonRadius + 80f
        
        plantStem = PlantStem(w, h)
        roseBushManager = RoseBushManager(w, h)
        lupinManager = LupinManager(w, h)  // NOUVEAU: Initialiser le gestionnaire lupin
        uiDrawing = UIDrawingManager(context, w, h, challengeManager)
        
        // Injecter le ChallengeManager dans les gestionnaires
        challengeManager.updateScreenDimensions(w, h)
        plantStem?.getFlowerManager()?.setChallengeManager(challengeManager)
        roseBushManager?.setChallengeManager(challengeManager)
        lupinManager?.setChallengeManager(challengeManager)  // NOUVEAU
    }
    
    // ==================== CONTRÔLE DU CYCLE ====================
    
    fun startCycle() {
        lightState = LightState.START  // Maintenant = choix de fleur
        stateStartTime = System.currentTimeMillis()
        showResetButton = false
        selectedMode = ""
        selectedFlowerType = "MARGUERITE"
        plantStem?.resetStem()
        roseBushManager?.reset()
        lupinManager?.reset()  // NOUVEAU: Reset du lupin
        
        // Reset de la séquence de cheat
        cheatTapCount = 0
        lastCheatTapTime = 0L
        
        invalidate()
    }
    
    fun updateForce(force: Float) {
        updateLightState()
        
        if (selectedFlowerType == "MARGUERITE") {
            // Logique existante pour la marguerite
            if (lightState == LightState.GREEN_GROW) {
                val phaseTime = System.currentTimeMillis() - stateStartTime
                plantStem?.processStemGrowth(force, phaseTime)
            }
            
            if (lightState == LightState.GREEN_LEAVES) {
                plantStem?.processLeavesGrowth(force)
            }
            
            if (lightState == LightState.GREEN_FLOWER) {
                plantStem?.processFlowerGrowth(force)
            }
            
            if (!showResetButton && (plantStem?.getStemHeight() ?: 0f) > 30f) {
                showResetButton = true
            }
        } else if (selectedFlowerType == "ROSE") {
            // Logique pour le rosier
            if (lightState == LightState.GREEN_GROW) {
                roseBushManager?.processStemGrowth(force)
            }
            
            if (lightState == LightState.GREEN_LEAVES) {
                roseBushManager?.processLeavesGrowth(force)
            }
            
            if (lightState == LightState.GREEN_FLOWER) {
                roseBushManager?.processFlowerGrowth(force)
            }
            
            // Bouton reset pour le rosier
            if (!showResetButton) {
                showResetButton = true
            }
        } else if (selectedFlowerType == "LUPIN") {
            // NOUVEAU: Logique pour le lupin
            if (lightState == LightState.GREEN_GROW) {
                lupinManager?.processStemGrowth(force)
            }
            
            if (lightState == LightState.GREEN_LEAVES) {
                lupinManager?.processLeavesGrowth(force)
            }
            
            if (lightState == LightState.GREEN_FLOWER) {
                lupinManager?.processFlowerGrowth(force)
            }
            
            // Bouton reset pour le lupin
            if (!showResetButton) {
                showResetButton = true
            }
        }
        // TODO: Ajouter LUPIN, IRIS, etc. ici plus tard
        
        // Mettre à jour le défi si en mode DÉFI
        if (selectedMode == "DÉFI" && challengeManager.getCurrentChallenge() != null) {
            val plantState = when (lightState) {
                LightState.GREEN_GROW -> "STEM"
                LightState.GREEN_LEAVES -> "LEAVES" 
                LightState.GREEN_FLOWER -> "FLOWER"
                else -> "OTHER"
            }
            challengeManager.updateChallengeProgress(force, plantState)
        }
        
        invalidate()
    }
    
    private fun updateLightState() {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - stateStartTime
        
        when (lightState) {
            LightState.START -> {
                // Reste en START (choix de fleur) jusqu'à ce qu'on choisisse
            }
            LightState.MODE_CHOICE -> {
                // Reste en MODE_CHOICE jusqu'à ce qu'on choisisse ZEN/DÉFI
            }
            LightState.CHALLENGE_SELECTION -> {
                // Reste en CHALLENGE_SELECTION jusqu'à ce qu'on choisisse un défi
            }
            LightState.CHALLENGE_BRIEF -> {
                // Auto-transition après 3 secondes
                if (elapsedTime >= 3000) {
                    lightState = LightState.YELLOW
                    stateStartTime = currentTime
                }
            }
            LightState.YELLOW -> {
                if (elapsedTime >= 2000) { 
                    lightState = LightState.GREEN_GROW
                    stateStartTime = currentTime
                }
            }
            LightState.GREEN_GROW -> {
                if (elapsedTime >= 4000) { // 4 secondes
                    lightState = LightState.GREEN_LEAVES
                    stateStartTime = currentTime
                }
            }
            LightState.GREEN_LEAVES -> {
                if (elapsedTime >= 3000) { // 3 secondes
                    lightState = LightState.GREEN_FLOWER
                    stateStartTime = currentTime
                }
            }
            LightState.GREEN_FLOWER -> {
                if (elapsedTime >= 4000) { // 4 secondes
                    // Vérifier si on est en mode défi
                    if (selectedMode == "DÉFI") {
                        val result = challengeManager.finalizeChallengeResult()
                        lightState = LightState.CHALLENGE_RESULT
                    } else {
                        lightState = LightState.RED
                    }
                    stateStartTime = currentTime
                }
            }
            LightState.CHALLENGE_RESULT -> {
                // Auto-transition après 4 secondes
                if (elapsedTime >= 4000) {
                    lightState = LightState.RED
                    stateStartTime = currentTime
                }
            }
            LightState.RED -> {}
        }
    }
    
    // ==================== AFFICHAGE ====================
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Calculer le temps restant
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - stateStartTime
        val timeRemaining = when (lightState) {
            LightState.START -> 0
            LightState.MODE_CHOICE -> 0
            LightState.CHALLENGE_SELECTION -> 0
            LightState.CHALLENGE_BRIEF -> max(0, 3 - (elapsedTime / 1000))
            LightState.YELLOW -> max(0, 2 - (elapsedTime / 1000))
            LightState.GREEN_GROW -> max(0, 4 - (elapsedTime / 1000))
            LightState.GREEN_LEAVES -> max(0, 3 - (elapsedTime / 1000))
            LightState.GREEN_FLOWER -> max(0, 4 - (elapsedTime / 1000))
            LightState.CHALLENGE_RESULT -> max(0, 4 - (elapsedTime / 1000))
            LightState.RED -> 0
        }
        
        // Dessiner la plante selon le type sélectionné
        if (lightState == LightState.GREEN_GROW || 
            lightState == LightState.GREEN_LEAVES || 
            lightState == LightState.GREEN_FLOWER || 
            lightState == LightState.RED) {
            
            if (selectedFlowerType == "MARGUERITE") {
                drawPlantStem(canvas)
            } else if (selectedFlowerType == "ROSE") {
                drawRoseBush(canvas)
            } else if (selectedFlowerType == "LUPIN") {
                drawLupin(canvas)  // NOUVEAU: Dessiner le lupin
            }
            // TODO: Ajouter LUPIN, IRIS, etc. ici plus tard
        }
        
        // Déléguer tout l'affichage UI au UIDrawingManager
        uiDrawing.drawCurrentState(canvas, lightState, timeRemaining, resetButtonX, resetButtonY, resetButtonRadius, challengeManager)
    }
    
    private fun drawPlantStem(canvas: Canvas) {
        val stem = plantStem ?: return
        
        // Dessiner les fleurs de profil/arrière DERRIÈRE les tiges
        if (lightState == LightState.GREEN_FLOWER || 
            lightState == LightState.RED) {
            drawBackgroundFlowers(canvas, stem.getFlowers())
        }
        
        // Dessiner la tige principale
        drawMainStem(canvas, stem.mainStem)
        
        // Dessiner les branches
        drawBranches(canvas, stem.branches)
        
        // Dessiner les feuilles pendant GREEN_LEAVES et après
        if (lightState == LightState.GREEN_LEAVES || 
            lightState == LightState.GREEN_FLOWER || 
            lightState == LightState.RED) {
            drawLeaves(canvas, stem.getLeaves())
        }
        
        // Dessiner les fleurs de face/3-4 PAR-DESSUS les tiges
        if (lightState == LightState.GREEN_FLOWER || 
            lightState == LightState.RED) {
            drawForegroundFlowers(canvas, stem.getFlowers())
        }
    }
    
    private fun drawRoseBush(canvas: Canvas) {
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
            
            manager.drawRoseBush(canvas, branchPaint, leafPaint, flowerPaint)
        }
    }
    
    // NOUVEAU: Fonction de dessin pour le lupin
    private fun drawLupin(canvas: Canvas) {
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
            
            manager.drawLupin(canvas, stemPaint, leafPaint, flowerPaint)
        }
    }
    
    private fun drawMainStem(canvas: Canvas, mainStem: List<PlantStem.StemPoint>) {
        if (mainStem.size < 2) return
        
        uiDrawing.drawMainStem(canvas, mainStem)
    }
    
    private fun drawBranches(canvas: Canvas, branches: List<PlantStem.Branch>) {
        uiDrawing.drawBranches(canvas, branches)
    }
    
    private fun drawLeaves(canvas: Canvas, leaves: List<PlantLeavesManager.Leaf>) {
        val stem = plantStem ?: return
        uiDrawing.drawLeaves(canvas, leaves, stem)
    }
    
    private fun drawBackgroundFlowers(canvas: Canvas, flowers: List<FlowerManager.Flower>) {
        val stem = plantStem ?: return
        uiDrawing.drawBackgroundFlowers(canvas, flowers, stem)
    }
    
    private fun drawForegroundFlowers(canvas: Canvas, flowers: List<FlowerManager.Flower>) {
        val stem = plantStem ?: return
        uiDrawing.drawForegroundFlowers(canvas, flowers, stem)
    }
    
    // ==================== GESTION DES ÉVÉNEMENTS ====================
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (lightState == LightState.START) {
                return handleFlowerChoiceClick(event)  // NOUVEAU: Premier écran
            } else if (lightState == LightState.MODE_CHOICE) {
                return handleModeChoiceClick(event)   // NOUVEAU: Deuxième écran
            } else if (lightState == LightState.CHALLENGE_SELECTION) {
                return handleChallengeSelectionClick(event)
            } else if (lightState == LightState.RED) {
                return handleResetButtonClick(event)
            }
        }
        return super.onTouchEvent(event)
    }
    
    // ==================== NOUVEAU: CHOIX DE FLEUR (1er ÉCRAN) ====================
    
    private fun handleFlowerChoiceClick(event: MotionEvent): Boolean {
        // Vérifier d'abord le cheat code
        if (checkCheatCode(event)) {
            return true
        }
        
        // Obtenir les fleurs débloquées
        val unlockedFlowers = getUnlockedFlowers()
        
        if (unlockedFlowers.size == 1) {
            // Seulement la marguerite - centrée
            val flowerButtonRadius = width * 0.18f
            val centerX = width / 2f
            val buttonY = height / 2f
            
            val dx = event.x - centerX
            val dy = event.y - buttonY
            val distance = sqrt(dx * dx + dy * dy)
            
            if (distance <= flowerButtonRadius * 1.5f) {
                selectedFlowerType = "MARGUERITE"
                goToModeChoice()
                return true
            }
        } else if (unlockedFlowers.size == 2) {
            // 2 fleurs : côte à côte
            val flowerButtonRadius = width * 0.18f
            val spacing = flowerButtonRadius * 2.8f
            val centerX = width / 2f
            val buttonY = height / 2f
            
            // Marguerite (gauche)
            val margueriteX = centerX - spacing / 2f
            val margueriteDx = event.x - margueriteX
            val margueriteDy = event.y - buttonY
            val margueriteDistance = sqrt(margueriteDx * margueriteDx + margueriteDy * margueriteDy)
            
            // Rose (droite)
            val roseX = centerX + spacing / 2f
            val roseDx = event.x - roseX
            val roseDy = event.y - buttonY
            val roseDistance = sqrt(roseDx * roseDx + roseDy * roseDy)
            
            if (margueriteDistance <= flowerButtonRadius * 1.5f) {
                selectedFlowerType = "MARGUERITE"
                goToModeChoice()
                return true
            } else if (roseDistance <= flowerButtonRadius * 1.5f) {
                selectedFlowerType = "ROSE"
                goToModeChoice()
                return true
            }
        } else if (unlockedFlowers.size >= 3) {
            // 3+ fleurs : triangle
            val flowerButtonRadius = width * 0.18f
            val spacing = flowerButtonRadius * 2.0f
            val centerX = width / 2f
            val buttonY = height / 2f
            val topY = buttonY - spacing * 0.3f
            val bottomY = buttonY + spacing * 0.3f
            
            // Marguerite (haut centre)
            val margueriteX = centerX
            val margueriteDx = event.x - margueriteX
            val margueriteDy = event.y - topY
            val margueriteDistance = sqrt(margueriteDx * margueriteDx + margueriteDy * margueriteDy)
            
            // Rose (bas gauche)
            val roseX = centerX - spacing / 2f
            val roseDx = event.x - roseX
            val roseDy = event.y - bottomY
            val roseDistance = sqrt(roseDx * roseDx + roseDy * roseDy)
            
            // Lupin (bas droite)
            val lupinX = centerX + spacing / 2f
            val lupinDx = event.x - lupinX
            val lupinDy = event.y - bottomY
            val lupinDistance = sqrt(lupinDx * lupinDx + lupinDy * lupinDy)
            
            if (margueriteDistance <= flowerButtonRadius * 1.5f) {
                selectedFlowerType = "MARGUERITE"
                goToModeChoice()
                return true
            } else if (roseDistance <= flowerButtonRadius * 1.5f) {
                selectedFlowerType = "ROSE"
                goToModeChoice()
                return true
            } else if (lupinDistance <= flowerButtonRadius * 1.5f) {
                selectedFlowerType = "LUPIN"
                goToModeChoice()
                return true
            }
        }
        
        return false
    }
    
    private fun getUnlockedFlowers(): List<String> {
        val flowers = mutableListOf("MARGUERITE")  // Toujours débloquée
        
        if (challengeManager.isFlowerUnlocked("ROSE")) {
            flowers.add("ROSE")
        }
        
        if (challengeManager.isFlowerUnlocked("LUPIN")) {
            flowers.add("LUPIN")
        }
        
        // Retirer IRIS pour l'instant car pas encore implémenté
        // if (challengeManager.isFlowerUnlocked("IRIS")) {
        //     flowers.add("IRIS")
        // }
        
        return flowers
    }
    
    private fun goToModeChoice() {
        lightState = LightState.MODE_CHOICE
        stateStartTime = System.currentTimeMillis()
        invalidate()
    }
    
    // ==================== NOUVEAU: CHOIX DE MODE (2ème ÉCRAN) ====================
    
    private fun handleModeChoiceClick(event: MotionEvent): Boolean {
        // Calculer positions des boutons ZEN/DÉFI - VRAIMENT CENTRER L'ENSEMBLE
        val buttonRadius = width * 0.15f
        val spacing = buttonRadius * 2.5f
        val centerX = width / 2f
        val zenButtonX = centerX - spacing / 2f
        val defiButtonX = centerX + spacing / 2f
        val buttonY = height / 2f
        
        // Vérifier clic sur bouton ZEN
        val zenDx = event.x - zenButtonX
        val zenDy = event.y - buttonY
        val zenDistance = sqrt(zenDx * zenDx + zenDy * zenDy)
        
        // Vérifier clic sur bouton DÉFI
        val defiDx = event.x - defiButtonX
        val defiDy = event.y - buttonY
        val defiDistance = sqrt(defiDx * defiDx + defiDy * defiDy)
        
        if (zenDistance <= buttonRadius) {
            // Mode ZEN sélectionné - aller directement à la croissance
            selectedMode = "ZEN"
            initializePlant()
            lightState = LightState.YELLOW
            stateStartTime = System.currentTimeMillis()
            return true
        } else if (defiDistance <= buttonRadius) {
            // Mode DÉFI sélectionné - aller à la sélection de défi
            selectedMode = "DÉFI"
            lightState = LightState.CHALLENGE_SELECTION
            stateStartTime = System.currentTimeMillis()
            return true
        }
        return false
    }
    
    // ==================== CHEAT CODE SECRET ====================
    
    private fun checkCheatCode(event: MotionEvent): Boolean {
        val currentTime = System.currentTimeMillis()
        
        // Reset si trop de temps écoulé
        if (currentTime - lastCheatTapTime > cheatTimeWindow) {
            cheatTapCount = 0
        }
        
        // Vérifier si le tap est dans le coin haut-droite
        val cornerSize = 150f  // Taille de la zone de détection
        val isTopRightCorner = event.x >= width - cornerSize && event.y <= cornerSize
        
        if (isTopRightCorner) {
            cheatTapCount++
            lastCheatTapTime = currentTime
            
            // Vérifier si on a atteint 3 taps
            if (cheatTapCount >= cheatRequiredTaps) {
                activateCheatCode()
                return true
            }
        } else {
            // Reset si tap ailleurs
            cheatTapCount = 0
        }
        
        return false
    }
    
    private fun activateCheatCode() {
        // Débloquer toutes les fleurs instantanément
        challengeManager.activateCheatMode()
        
        // Rester sur l'écran de choix de fleur pour voir le résultat
        invalidate()
    }
    
    private fun handleChallengeSelectionClick(event: MotionEvent): Boolean {
        // Zone des 3 boutons de défi (calculée dans UIDrawingManager)
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
                
                challengeManager.setCurrentFlowerType(selectedFlowerType)
                
                // Choisir les défis selon la fleur sélectionnée
                val challenges = when (selectedFlowerType) {
                    "MARGUERITE" -> challengeManager.getMargueriteChallenges()
                    "ROSE" -> challengeManager.getRoseChallenges()
                    "LUPIN" -> challengeManager.getLupinChallenges()
                    else -> challengeManager.getMargueriteChallenges()
                }
                
                val challenge = challenges.find { it.id == i }
                
                if (challenge?.isUnlocked == true) {
                    selectedChallengeId = i
                    challengeManager.startChallenge(i)
                    initializePlant()
                    lightState = LightState.CHALLENGE_BRIEF
                    stateStartTime = System.currentTimeMillis()
                    return true
                }
            }
        }
        return false
    }
    
    private fun initializePlant() {
        when (selectedFlowerType) {
            "ROSE" -> {
                roseBushManager?.initialize(width / 2f, height * 0.85f)
            }
            "LUPIN" -> {
                lupinManager?.initialize(width / 2f, height * 0.85f)  // NOUVEAU
            }
            // La marguerite s'initialise automatiquement dans PlantStem
        }
    }
    
    private fun handleResetButtonClick(event: MotionEvent): Boolean {
        // Appui sur le bouton RESET
        val dx = event.x - resetButtonX
        val dy = event.y - resetButtonY
        val distance = sqrt(dx * dx + dy * dy)
        
        if (distance <= resetButtonRadius) {
            startCycle()
            return true
        }
        return false
    }
}
