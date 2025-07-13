package com.example.souffleforcetest

class ChallengeEffectsManager {
    
    // ==================== DATA CLASSES ====================
    
    data class DissolveInfo(
        val progress: Float,                    // 0.0 à 1.0
        val stemsCollapsing: Boolean,          // Tiges qui s'effondrent
        val leavesShriveling: Boolean,         // Feuilles qui se ratatinent
        val flowersPetalsWilting: Boolean,     // Pétales qui flétrissent
        
        // Spécifiques à l'Iris - NOMS CORRECTS
        val shouldFallsDropPetals: Boolean = false,    // Pétales tombantes qui tombent
        val shouldStandardsWilt: Boolean = false,      // Pétales dressés qui flétrissent
        val shouldBeardDissolve: Boolean = false,      // Barbe qui se dissout
        val shouldLeavesShrive: Boolean = leavesShriveling  // Alias pour cohérence
    )
    
    // ==================== VARIABLES PRINCIPALES ====================
    
    private var fireworkManager: FireworkManager? = null
    private var rainManager: RainManager? = null
    
    // Callbacks pour notifier l'UI
    private var onFireworkStartedCallback: (() -> Unit)? = null
    private var onRainStartedCallback: (() -> Unit)? = null
    
    // État de dissolution par type de fleur
    private val dissolveStates = mutableMapOf<String, MutableMap<String, Any>>()
    
    // ==================== CONFIGURATION ====================
    
    fun setFireworkManager(manager: FireworkManager) {
        fireworkManager = manager
    }
    
    fun setRainManager(manager: RainManager) {
        rainManager = manager
    }
    
    fun setOnFireworkStartedCallback(callback: () -> Unit) {
        onFireworkStartedCallback = callback
    }
    
    fun setOnRainStartedCallback(callback: () -> Unit) {
        onRainStartedCallback = callback
    }
    
    // ==================== GESTION DES EFFETS ====================
    
    fun startFireworks() {
        fireworkManager?.startFirework()  // CORRIGÉ: startFirework() - méthode réelle
        onFireworkStartedCallback?.invoke()
    }
    
    fun startRain(flowerType: String) {
        rainManager?.startRain()
        onRainStartedCallback?.invoke()
        
        // Initialiser la dissolution pour ce type de fleur
        initializeDissolve(flowerType)
    }
    
    fun stopAllEffects() {
        fireworkManager?.stop()  // CORRIGÉ: stop() - méthode réelle
        rainManager?.stop()      // CORRIGÉ: stop() - méthode réelle
        
        // Reset de toutes les dissolutions
        dissolveStates.clear()
    }
    
    fun updateEffects(deltaTime: Float) {
        fireworkManager?.update(deltaTime)
        rainManager?.update(deltaTime)
        
        // Mettre à jour la dissolution pour toutes les fleurs actives
        for ((flowerType, challengeData) in dissolveStates) {
            if (rainManager?.isPlaying() == true) {  // CORRIGÉ: isPlaying() - méthode réelle
                updateDissolveProgress(deltaTime * 0.5f, challengeData) // Vitesse de dissolution
            }
        }
    }
    
    // ==================== GESTION DE LA DISSOLUTION ====================
    
    private fun initializeDissolve(flowerType: String) {
        val challengeData = mutableMapOf<String, Any>(
            "dissolveProgress" to 0f,
            "stemsCollapsing" to true,
            "leavesShriveling" to true,
            "flowersPetalsWilting" to true
        )
        
        // Effets spécifiques selon le type de fleur
        when (flowerType) {
            "IRIS" -> {
                challengeData["shouldFallsDropPetals"] = true
                challengeData["shouldStandardsWilt"] = true
                challengeData["shouldBeardDissolve"] = true
            }
            "LUPIN" -> {
                challengeData["flowersPetalsWilting"] = true
            }
            "ROSE" -> {
                challengeData["stemsCollapsing"] = true
                challengeData["flowersPetalsWilting"] = true
            }
            "MARGUERITE" -> {
                challengeData["flowersPetalsWilting"] = true
            }
        }
        
        dissolveStates[flowerType] = challengeData
    }
    
    fun updateDissolveProgress(deltaProgress: Float, challengeData: MutableMap<String, Any>) {
        val currentProgress = challengeData["dissolveProgress"] as? Float ?: 0f
        val newProgress = (currentProgress + deltaProgress).coerceIn(0f, 1f)
        challengeData["dissolveProgress"] = newProgress
    }
    
    fun getDissolveProgress(flowerType: String, challengeData: MutableMap<String, Any>): Float {
        return dissolveStates[flowerType]?.get("dissolveProgress") as? Float ?: 0f
    }
    
    fun getDissolveInfo(flowerType: String): DissolveInfo? {
        val challengeData = dissolveStates[flowerType] ?: return null
        
        val progress = challengeData["dissolveProgress"] as? Float ?: 0f
        if (progress <= 0f) return null
        
        return DissolveInfo(
            progress = progress,
            stemsCollapsing = challengeData["stemsCollapsing"] as? Boolean ?: false,
            leavesShriveling = challengeData["leavesShriveling"] as? Boolean ?: false,
            flowersPetalsWilting = challengeData["flowersPetalsWilting"] as? Boolean ?: false,
            shouldFallsDropPetals = challengeData["shouldFallsDropPetals"] as? Boolean ?: false,
            shouldStandardsWilt = challengeData["shouldStandardsWilt"] as? Boolean ?: false,
            shouldBeardDissolve = challengeData["shouldBeardDissolve"] as? Boolean ?: false
        )
    }
    
    fun resetDissolveEffects() {
        dissolveStates.clear()
    }
    
    // ==================== MÉTHODES SPÉCIFIQUES POUR IRIS ====================
    
    fun shouldFallsDropPetals(challengeData: MutableMap<String, Any>): Boolean {
        return challengeData["shouldFallsDropPetals"] as? Boolean ?: false
    }
    
    fun shouldStandardsWilt(challengeData: MutableMap<String, Any>): Boolean {
        return challengeData["shouldStandardsWilt"] as? Boolean ?: false
    }
    
    fun shouldBeardDissolve(challengeData: MutableMap<String, Any>): Boolean {
        return challengeData["shouldBeardDissolve"] as? Boolean ?: false
    }
    
    fun shouldLeavesShrive(challengeData: MutableMap<String, Any>): Boolean {
        return challengeData["leavesShriveling"] as? Boolean ?: false
    }
    
    // ==================== GETTERS D'ÉTAT ====================
    
    fun isFireworksActive(): Boolean = fireworkManager?.isPlaying() ?: false  // CORRIGÉ: isPlaying() - méthode réelle
    fun isRainActive(): Boolean = rainManager?.isPlaying() ?: false           // CORRIGÉ: isPlaying() - méthode réelle
    fun isAnyEffectActive(): Boolean = isFireworksActive() || isRainActive()
}
