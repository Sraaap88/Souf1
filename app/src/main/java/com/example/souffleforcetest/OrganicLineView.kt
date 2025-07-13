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
    private var selectedMode = ""
    private var selectedFlowerType = "MARGUERITE"
    
    // Système de cheat code
    private var cheatTapCount = 0
    private var lastCheatTapTime = 0L
    private val cheatTimeWindow = 3000L
    private val cheatRequiredTaps = 3
    
    // NOUVEAU: Variable pour stocker le résultat du défi
    private var lastChallengeResult: ChallengeDefinitions.ChallengeResult? = null
    
    // ==================== GESTIONNAIRES ====================
    
    private var plantStem: PlantStem? = null
    private var roseBushManager: RoseBushManager? = null
    private var lupinManager: LupinManager? = null
    private var irisManager: IrisManager? = null
    private var uiDrawing: UIDrawingManager? = null
    private val challengeManager = ChallengeManager(context)
    private var selectedChallengeId = -1
    
    // NOUVEAUX: Système d'effets complet
    private var fireworkManager: FireworkManager? = null
    private var rainManager: RainManager? = null
    private var challengeEffectsManager: ChallengeEffectsManager? = null
    private var interactionHandler: PlantInteractionHandler? = null
    private var isInitialized = false
    
    enum class LightState {
        START, MODE_CHOICE, CHALLENGE_SELECTION, CHALLENGE_BRIEF, 
        YELLOW, GREEN_GROW, GREEN_LEAVES, GREEN_FLOWER, CHALLENGE_RESULT, RED
    }
    
    // ==================== INITIALISATION SÉCURISÉE ====================
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        // PROTECTION: Éviter les initialisations multiples
        if (w <= 0 || h <= 0) return
        
        try {
            resetButtonX = w - resetButtonRadius - 50f
            resetButtonY = resetButtonRadius + 80f
            
            // Initialiser tous les gestionnaires de manière sécurisée
            plantStem = PlantStem(w, h)
            roseBushManager = RoseBushManager(w, h)
            lupinManager = LupinManager(w, h)
            irisManager = IrisManager(w, h)
            uiDrawing = UIDrawingManager(context, w, h, challengeManager)
            
            // NOUVEAU: Initialisation complète du système d'effets
            fireworkManager = FireworkManager(w, h)
            rainManager = RainManager(w, h)
            challengeEffectsManager = ChallengeEffectsManager()
            interactionHandler = PlantInteractionHandler(w, h, challengeManager)
            
            // NOUVEAU: Configuration du système d'effets
            challengeEffectsManager?.setFireworkManager(fireworkManager!!)
            challengeEffectsManager?.setRainManager(rainManager!!)
            challengeEffectsManager?.setOnFireworkStartedCallback {
                post { invalidate() } // Post sur le thread UI
            }
            challengeEffectsManager?.setOnRainStartedCallback {
                post { invalidate() } // Post sur le thread UI pour la pluie
            }
            
            // NOUVEAU: Configuration des gestionnaires dans ChallengeManager
            challengeManager.setEffectsManager(challengeEffectsManager!!)
            challengeManager.setFireworkManager(fireworkManager!!)
            challengeManager.setRainManager(rainManager!!)
            challengeManager.setOnFireworkStartedCallback {
                post { invalidate() }
            }
            challengeManager.setOnRainStartedCallback {
                post { invalidate() }
            }
            
            // Injecter le ChallengeManager
            challengeManager.updateScreenDimensions(w, h)
            plantStem?.getFlowerManager()?.setChallengeManager(challengeManager)
            roseBushManager?.setChallengeManager(challengeManager)
            lupinManager?.setChallengeManager(challengeManager)
            irisManager?.setChallengeManager(challengeManager)
            
            isInitialized = true
            
        } catch (e: Exception) {
            // LOG: En cas d'erreur, ne pas planter l'app
            e.printStackTrace()
            isInitialized = false
        }
    }
    
    // ==================== CONTRÔLE DU CYCLE ====================
    
    fun startCycle() {
        lightState = LightState.START
        stateStartTime = System.currentTimeMillis()
        showResetButton = false
        selectedMode = ""
        selectedFlowerType = "MARGUERITE"
        lastChallengeResult = null  // NOUVEAU: Reset du résultat
        
        // PROTECTION: Reset seulement si initialisé
        try {
            plantStem?.resetStem()
            roseBushManager?.reset()
            lupinManager?.reset()
            irisManager?.reset()
            
            // NOUVEAU: Arrêter tous les effets
            challengeEffectsManager?.stopAllEffects()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        cheatTapCount = 0
        lastCheatTapTime = 0L
        invalidate()
    }
    
    fun updateForce(force: Float) {
        // PROTECTION: Ne rien faire si pas initialisé
        if (!isInitialized || interactionHandler == null) return
        
        try {
            updateLightState()
            
            // Déléguer la logique de croissance au gestionnaire d'interaction
            interactionHandler?.handlePlantGrowth(
                selectedFlowerType, lightState, force, 
                plantStem, roseBushManager, lupinManager, irisManager
            )
            
            // Gérer le bouton reset
            if (!showResetButton) {
                showResetButton = interactionHandler?.shouldShowResetButton(
                    selectedFlowerType, plantStem, roseBushManager, lupinManager, irisManager
                ) ?: false
            }
            
            // Mettre à jour le défi si en mode DÉFI
            if (selectedMode == "DÉFI" && challengeManager.getCurrentChallenge() != null) {
                val plantState = when (lightState) {
                    LightState.GREEN_GROW -> "STEM"
                    LightState.GREEN_LEAVES -> "LEAVES" 
                    LightState.GREEN_FLOWER -> "FLOWER"
                    else -> "OTHER"
                }
                challengeManager.updateChallengeProgress(force, plantState)
                challengeManager.checkChallengeCompletion()
            }
            
            // NOUVEAU: Mettre à jour tous les effets (feu d'artifice + pluie)
            challengeEffectsManager?.updateEffects(0.016f)
            
            invalidate()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun updateLightState() {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - stateStartTime
        
        when (lightState) {
            LightState.START, LightState.MODE_CHOICE, LightState.CHALLENGE_SELECTION -> {
                // Pas de transitions automatiques
            }
            LightState.CHALLENGE_BRIEF -> {
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
                if (elapsedTime >= 4000) {
                    lightState = LightState.GREEN_LEAVES
                    stateStartTime = currentTime
                }
            }
            LightState.GREEN_LEAVES -> {
                if (elapsedTime >= 3000) {
                    lightState = LightState.GREEN_FLOWER
                    stateStartTime = currentTime
                }
            }
            LightState.GREEN_FLOWER -> {
                if (elapsedTime >= 4000) {
                    if (selectedMode == "DÉFI") {
                        // NOUVEAU: Stocker le résultat avant de passer à CHALLENGE_RESULT
                        lastChallengeResult = challengeManager.finalizeChallengeResult()
                        lightState = LightState.CHALLENGE_RESULT
                    } else {
                        lightState = LightState.RED
                    }
                    stateStartTime = currentTime
                }
            }
            LightState.CHALLENGE_RESULT -> {
                if (elapsedTime >= 3000) { // CORRIGÉ: 3 secondes au lieu de 4 - pluie démarre 1 seconde plus tôt
                    lightState = LightState.RED
                    stateStartTime = currentTime
                    
                    // NOUVEAU: Déclencher la pluie SEULEMENT si le défi a échoué
                    if (lastChallengeResult != null && !lastChallengeResult!!.success) {
                        challengeEffectsManager?.startRain(selectedFlowerType)
                    }
                }
            }
            LightState.RED -> {}
        }
    }
    
    // ==================== AFFICHAGE SÉCURISÉ ====================
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // PROTECTION: Ne rien dessiner si pas initialisé
        if (!isInitialized || uiDrawing == null || interactionHandler == null) {
            return
        }
        
        try {
            // Calculer le temps restant
            val currentTime = System.currentTimeMillis()
            val elapsedTime = currentTime - stateStartTime
            val timeRemaining = when (lightState) {
                LightState.START, LightState.MODE_CHOICE, LightState.CHALLENGE_SELECTION -> 0
                LightState.CHALLENGE_BRIEF -> max(0, 3 - (elapsedTime / 1000))
                LightState.YELLOW -> max(0, 2 - (elapsedTime / 1000))
                LightState.GREEN_GROW -> max(0, 4 - (elapsedTime / 1000))
                LightState.GREEN_LEAVES -> max(0, 3 - (elapsedTime / 1000))
                LightState.GREEN_FLOWER -> max(0, 4 - (elapsedTime / 1000))
                LightState.CHALLENGE_RESULT -> max(0, 4 - (elapsedTime / 1000))
                LightState.RED -> 0
            }
            
            // Dessiner les plantes
            if (lightState == LightState.GREEN_GROW || 
                lightState == LightState.GREEN_LEAVES || 
                lightState == LightState.GREEN_FLOWER || 
                lightState == LightState.RED) {
                
                // NOUVEAU: Récupérer dissolveInfo et le passer à drawPlants
                val dissolveInfo = challengeEffectsManager?.getDissolveInfo(selectedFlowerType)
                interactionHandler?.drawPlants(
                    canvas, selectedFlowerType, lightState,
                    plantStem, roseBushManager, lupinManager, irisManager, uiDrawing!!, dissolveInfo
                )
            }
            
            // Dessiner l'UI
            uiDrawing?.drawCurrentState(canvas, lightState, timeRemaining, resetButtonX, resetButtonY, resetButtonRadius, challengeManager)
            
            // NOUVEAU: Dessiner tous les effets par-dessus tout
            val paint = Paint()
            fireworkManager?.draw(canvas, paint)
            rainManager?.draw(canvas, paint)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // ==================== GESTION DES ÉVÉNEMENTS ====================
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // PROTECTION: Ne pas traiter les événements si pas initialisé
        if (!isInitialized || interactionHandler == null) {
            return super.onTouchEvent(event)
        }
        
        if (event.action == MotionEvent.ACTION_DOWN) {
            return try {
                when (lightState) {
                    LightState.START -> handleFlowerChoiceClick(event)
                    LightState.MODE_CHOICE -> handleModeChoiceClick(event)
                    LightState.CHALLENGE_SELECTION -> handleChallengeSelectionClick(event)
                    LightState.RED -> handleResetButtonClick(event)
                    else -> super.onTouchEvent(event)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                super.onTouchEvent(event)
            }
        }
        return super.onTouchEvent(event)
    }
    
    private fun handleFlowerChoiceClick(event: MotionEvent): Boolean {
        // Vérifier cheat code
        if (checkCheatCode(event)) return true
        
        val selectedFlower = interactionHandler?.detectFlowerSelection(
            event, width, height, challengeManager
        )
        
        if (selectedFlower != null) {
            selectedFlowerType = selectedFlower
            goToModeChoice()
            return true
        }
        return false
    }
    
    private fun handleModeChoiceClick(event: MotionEvent): Boolean {
        val mode = interactionHandler?.detectModeSelection(event, width, height)
        
        when (mode) {
            "ZEN" -> {
                selectedMode = "ZEN"
                initializePlant()
                lightState = LightState.YELLOW
                stateStartTime = System.currentTimeMillis()
                return true
            }
            "DÉFI" -> {
                selectedMode = "DÉFI"
                challengeManager.setCurrentFlowerType(selectedFlowerType)
                lightState = LightState.CHALLENGE_SELECTION
                stateStartTime = System.currentTimeMillis()
                return true
            }
        }
        return false
    }
    
    private fun handleChallengeSelectionClick(event: MotionEvent): Boolean {
        val challengeId = interactionHandler?.detectChallengeSelection(
            event, width, height, selectedFlowerType, challengeManager
        )
        
        if (challengeId != null && challengeId > 0) {
            selectedChallengeId = challengeId
            challengeManager.startChallenge(challengeId)
            initializePlant()
            lightState = LightState.CHALLENGE_BRIEF
            stateStartTime = System.currentTimeMillis()
            return true
        }
        return false
    }
    
    private fun handleResetButtonClick(event: MotionEvent): Boolean {
        val dx = event.x - resetButtonX
        val dy = event.y - resetButtonY
        val distance = sqrt(dx * dx + dy * dy)
        
        if (distance <= resetButtonRadius) {
            startCycle()
            return true
        }
        return false
    }
    
    // ==================== FONCTIONS UTILITAIRES ====================
    
    private fun goToModeChoice() {
        lightState = LightState.MODE_CHOICE
        stateStartTime = System.currentTimeMillis()
        invalidate()
    }
    
    private fun initializePlant() {
        try {
            when (selectedFlowerType) {
                "ROSE" -> roseBushManager?.initialize(width / 2f, height * 0.85f)
                "LUPIN" -> lupinManager?.initialize(width / 2f, height * 0.85f)
                "IRIS" -> irisManager?.initialize(width / 2f, height * 0.85f)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun checkCheatCode(event: MotionEvent): Boolean {
        val currentTime = System.currentTimeMillis()
        
        if (currentTime - lastCheatTapTime > cheatTimeWindow) {
            cheatTapCount = 0
        }
        
        val cornerSize = 150f
        val isTopRightCorner = event.x >= width - cornerSize && event.y <= cornerSize
        
        if (isTopRightCorner) {
            cheatTapCount++
            lastCheatTapTime = currentTime
            
            if (cheatTapCount >= cheatRequiredTaps) {
                challengeManager.activateCheatMode()
                invalidate()
                return true
            }
        } else {
            cheatTapCount = 0
        }
        return false
    }
    
    // ==================== CYCLE DE VIE ANDROID ====================
    
    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        try {
            challengeEffectsManager?.stopAllEffects()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Rien à faire ici, onSizeChanged se chargera de l'initialisation
    }
}
