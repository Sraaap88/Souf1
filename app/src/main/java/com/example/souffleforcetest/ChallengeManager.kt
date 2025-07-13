package com.example.souffleforcetest

import android.content.Context

class ChallengeManager(private val context: Context? = null) {
    
    // ==================== GESTIONNAIRES DÉLÉGUÉS ====================
    
    private val definitions = ChallengeDefinitions()
    private lateinit var dataManager: ChallengeDataManager
    private lateinit var effectsManager: ChallengeEffectsManager
    
    // Gestionnaires de défis spécialisés
    private val margueriteChallengeHandler = MargueriteChallengeHandler()
    private val roseChallengeHandler = RoseChallengeHandler()
    private val lupinChallengeHandler = LupinChallengeHandler()
    private val irisChallengeHandler = IrisChallengeHandler()
    
    // ==================== VARIABLES D'ÉTAT ====================
    
    private var currentChallenge: ChallengeDefinitions.Challenge? = null
    private var currentFlowerType: String = "MARGUERITE"
    private var challengeStartTime = 0L
    private var challengeData = mutableMapOf<String, Any>()
    
    // ==================== INITIALISATION ====================
    
    init {
        dataManager = ChallengeDataManager(context, definitions)
        dataManager.loadProgress()
    }
    
    fun setEffectsManager(manager: ChallengeEffectsManager) {
        effectsManager = manager
    }
    
    // ==================== CONFIGURATION DES GESTIONNAIRES ====================
    
    fun setFireworkManager(manager: FireworkManager) {
        if (::effectsManager.isInitialized) {
            effectsManager.setFireworkManager(manager)
        }
    }
    
    fun setRainManager(manager: RainManager) {
        if (::effectsManager.isInitialized) {
            effectsManager.setRainManager(manager)
        }
    }
    
    fun setOnFireworkStartedCallback(callback: () -> Unit) {
        if (::effectsManager.isInitialized) {
            effectsManager.setOnFireworkStartedCallback(callback)
        }
    }
    
    fun setOnRainStartedCallback(callback: () -> Unit) {
        if (::effectsManager.isInitialized) {
            effectsManager.setOnRainStartedCallback(callback)
        }
    }
    
    // ==================== DÉLÉGATION AUX GESTIONNAIRES ====================
    
    fun getDissolveProgress(): Float {
        return if (::effectsManager.isInitialized) {
            effectsManager.getDissolveProgress(currentFlowerType, challengeData)
        } else 0f
    }
    
    fun getDissolveInfo(flowerType: String): ChallengeEffectsManager.DissolveInfo? {
        return if (::effectsManager.isInitialized) {
            effectsManager.getDissolveInfo(flowerType)
        } else null
    }
    
    fun isRainActive(): Boolean {
        return if (::effectsManager.isInitialized) {
            effectsManager.isRainActive()
        } else false
    }
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    // Délégation vers ChallengeDefinitions
    fun getMargueriteChallenges(): List<ChallengeDefinitions.Challenge> = definitions.margueriteChallenges
    fun getRoseChallenges(): List<ChallengeDefinitions.Challenge> = definitions.roseChallenges
    fun getLupinChallenges(): List<ChallengeDefinitions.Challenge> = definitions.lupinChallenges
    fun getIrisChallenges(): List<ChallengeDefinitions.Challenge> = definitions.irisChallenges
    
    fun setCurrentFlowerType(flowerType: String) {
        currentFlowerType = flowerType
    }
    
    fun getCurrentFlowerType(): String = currentFlowerType
    fun getCurrentChallenge(): ChallengeDefinitions.Challenge? = currentChallenge
    fun getCurrentChallengeBrief(): String? = currentChallenge?.briefText
    
    // Délégation vers DataManager
    fun getUnlockedFlowers() = dataManager.getUnlockedFlowers()
    fun isFlowerUnlocked(flowerType: String) = dataManager.isFlowerUnlocked(flowerType)
    fun getFlowerUnlockMessage(flowerType: String) = dataManager.getFlowerUnlockMessage(flowerType)
    fun resetAllChallenges() = dataManager.resetAllChallenges()
    fun activateCheatMode() = dataManager.activateCheatMode()
    
    // ==================== GESTION DES DÉFIS ====================
    
    fun startChallenge(challengeId: Int) {
        currentChallenge = definitions.findChallengeById(currentFlowerType, challengeId)
        challengeStartTime = System.currentTimeMillis()
        challengeData.clear()
        
        // Reset des données selon le type de fleur
        dataManager.clearFlowerData(currentFlowerType)
        resetDissolveEffects()
        
        // Arrêter les effets en cours
        if (::effectsManager.isInitialized) {
            effectsManager.stopAllEffects()
        }
        
        println("Défi démarré: ${currentChallenge?.title} (${currentFlowerType})")
    }
    
    private fun resetDissolveEffects() {
        if (::effectsManager.isInitialized) {
            effectsManager.resetDissolveEffects()
        }
        
        when (currentFlowerType) {
            "MARGUERITE" -> margueriteChallengeHandler.resetDissolveEffects(challengeData)
            "ROSE" -> roseChallengeHandler.resetDissolveEffects(challengeData)
            "LUPIN" -> lupinChallengeHandler.resetDissolveEffects(challengeData)
            "IRIS" -> irisChallengeHandler.resetDissolveEffects(challengeData)
        }
    }
    
    fun updateChallengeProgress(force: Float, plantState: String) {
        val challenge = currentChallenge ?: return
        
        // Déléguer vers les gestionnaires spécialisés
        when (currentFlowerType) {
            "MARGUERITE" -> margueriteChallengeHandler.updateChallenge(challenge.id, force, plantState, challengeData)
            "ROSE" -> roseChallengeHandler.updateChallenge(challenge.id, force, plantState, challengeData)
            "LUPIN" -> lupinChallengeHandler.updateChallenge(challenge.id, force, plantState, challengeData)
            "IRIS" -> irisChallengeHandler.updateChallenge(challenge.id, force, plantState, challengeData)
        }
        
        // Mettre à jour la dissolution si la pluie est active
        if (::effectsManager.isInitialized && effectsManager.isRainActive()) {
            effectsManager.updateDissolveProgress(0.016f, challengeData)
        }
    }
    
    fun updateScreenDimensions(width: Int, height: Int) {
        challengeData["screenWidth"] = width.toFloat()
        challengeData["screenHeight"] = height.toFloat()
    }
    
    // ==================== NOTIFICATIONS D'ÉVÉNEMENTS ====================
    
    fun notifyFlowerCreated(flowerX: Float, flowerY: Float, flowerId: String) {
        dataManager.notifyFlowerCreated(currentChallenge, currentFlowerType, flowerY, challengeData, flowerId, definitions)
    }
    
    fun notifyLupinSpikeCreated(spikeColor: String, stemId: String) {
        dataManager.notifyLupinSpikeCreated(currentChallenge, currentFlowerType, spikeColor, stemId)
    }
    
    fun notifyDivisionCreated(divisionId: String) {
        dataManager.notifyDivisionCreated(currentChallenge, currentFlowerType, divisionId)
    }
    
    fun notifyBudCreated(budX: Float, budY: Float, budId: String) {
        dataManager.notifyBudCreated(currentChallenge, currentFlowerType, budId)
    }
    
    // ==================== VÉRIFICATION ET FINALISATION DES DÉFIS ====================
    
    fun checkChallengeCompletion(): ChallengeDefinitions.ChallengeResult? {
        val challenge = currentChallenge ?: return null
        
        val isSuccessful = when (currentFlowerType) {
            "MARGUERITE" -> {
                val data = dataManager.getMargueriteData()
                margueriteChallengeHandler.checkChallenge(
                    challenge.id, data.flowersInZone, data.budsCreated, 
                    data.flowersInZoneDefi3, data.budsCreatedDefi3
                )
            }
            "ROSE" -> {
                val data = dataManager.getRoseData()
                roseChallengeHandler.checkChallenge(
                    challenge.id, data.roseFlowersInZone, data.roseDivisions,
                    data.roseTotalFlowers, data.roseFlowersInZoneDefi3
                )
            }
            "LUPIN" -> {
                val data = dataManager.getLupinData()
                lupinChallengeHandler.checkChallenge(
                    challenge.id, data.lupinSpikeColors, data.lupinCompleteStems, data.lupinFlowers
                )
            }
            "IRIS" -> {
                val data = dataManager.getIrisData()
                irisChallengeHandler.checkChallenge(
                    challenge.id, data.irisFlowersInZone, data.irisRamifications, data.irisTotalFlowers
                )
            }
            else -> false
        }
        
        if (isSuccessful) {
            challenge.isCompleted = true
            dataManager.unlockNextChallenge(definitions, currentFlowerType, challenge.id)
            dataManager.unlockNextFlower(definitions, currentFlowerType, challenge.id)
            dataManager.saveProgress()
            
            if (::effectsManager.isInitialized) {
                effectsManager.startFireworks()
            }
            
            val successMessage = getSuccessMessage(challenge.id)
            return ChallengeDefinitions.ChallengeResult(challenge, true, successMessage)
        }
        
        return null
    }
    
    fun finalizeChallengeResult(): ChallengeDefinitions.ChallengeResult? {
        val challenge = currentChallenge ?: return null
        
        val result = checkChallengeCompletion() ?: run {
            // SUPPRIMÉ: startRain() - maintenant géré dans OrganicLineView
            val failMessage = getFailMessage(challenge.id)
            ChallengeDefinitions.ChallengeResult(challenge, false, failMessage)
        }
        
        currentChallenge = null
        return result
    }
    
    private fun getSuccessMessage(challengeId: Int): String {
        return when (currentFlowerType) {
            "MARGUERITE" -> {
                val data = dataManager.getMargueriteData()
                margueriteChallengeHandler.getSuccessMessage(
                    challengeId, data.flowersInZone, data.budsCreated,
                    data.flowersInZoneDefi3, data.budsCreatedDefi3
                )
            }
            "ROSE" -> {
                val data = dataManager.getRoseData()
                roseChallengeHandler.getSuccessMessage(
                    challengeId, data.roseFlowersInZone, data.roseDivisions,
                    data.roseTotalFlowers, data.roseFlowersInZoneDefi3
                )
            }
            "LUPIN" -> {
                val data = dataManager.getLupinData()
                lupinChallengeHandler.getSuccessMessage(
                    challengeId, data.lupinSpikeColors, data.lupinCompleteStems, data.lupinFlowers
                )
            }
            "IRIS" -> {
                val data = dataManager.getIrisData()
                irisChallengeHandler.getSuccessMessage(
                    challengeId, data.irisFlowersInZone, data.irisRamifications, data.irisTotalFlowers
                )
            }
            else -> "Défi réussi!"
        }
    }
    
    private fun getFailMessage(challengeId: Int): String {
        return when (currentFlowerType) {
            "MARGUERITE" -> {
                val data = dataManager.getMargueriteData()
                margueriteChallengeHandler.getFailMessage(
                    challengeId, data.flowersInZone, data.budsCreated,
                    data.flowersInZoneDefi3, data.budsCreatedDefi3
                )
            }
            "ROSE" -> {
                val data = dataManager.getRoseData()
                roseChallengeHandler.getFailMessage(
                    challengeId, data.roseFlowersInZone, data.roseDivisions,
                    data.roseTotalFlowers, data.roseFlowersInZoneDefi3
                )
            }
            "LUPIN" -> {
                val data = dataManager.getLupinData()
                lupinChallengeHandler.getFailMessage(
                    challengeId, data.lupinSpikeColors, data.lupinCompleteStems, data.lupinFlowers
                )
            }
            "IRIS" -> {
                val data = dataManager.getIrisData()
                irisChallengeHandler.getFailMessage(
                    challengeId, data.irisFlowersInZone, data.irisRamifications, data.irisTotalFlowers
                )
            }
            else -> "Défi échoué!"
        }
    }
}
