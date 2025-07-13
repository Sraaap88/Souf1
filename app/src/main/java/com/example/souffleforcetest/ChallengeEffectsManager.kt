package com.example.souffleforcetest

class ChallengeEffectsManager {

     // ==================== DATA CLASS POUR LA DISSOLUTION ====================
    
    data class DissolveInfo(
        val progress: Float = 0f,
        // Effets communs
        val stemsWilting: Boolean = false,
        val stemsCollapsing: Boolean = false,
        // Effets spécifiques marguerite
        val petalsFalling: Boolean = false,
        // Effets spécifiques rose
        val petalsDrooping: Boolean = false,
        val thornsWeakening: Boolean = false,
        val branchesDissolving: Boolean = false,
        // Effets spécifiques lupin
        val spikesWilting: Boolean = false,
        val colorsBlending: Boolean = false,
        // Effets spécifiques iris
        val fallsDropping: Boolean = false,
        val standardsWilting: Boolean = false,
        val beardDissolving: Boolean = false,
        val leavesShriveling: Boolean = false
    )
    
    // ==================== GESTIONNAIRES D'EFFETS ====================
    
    private var fireworkManager: FireworkManager? = null
    private var onFireworkStarted: (() -> Unit)? = null
    
    private var rainManager: RainManager? = null
    private var onRainStarted: (() -> Unit)? = null
    
    // Gestionnaires de défis pour la dissolution
    private val margueriteChallengeHandler = MargueriteChallengeHandler()
    private val roseChallengeHandler = RoseChallengeHandler()
    private val lupinChallengeHandler = LupinChallengeHandler()
    private val irisChallengeHandler = IrisChallengeHandler()
    
    // ==================== CONFIGURATION DES GESTIONNAIRES ====================
    
    fun setFireworkManager(manager: FireworkManager) {
        fireworkManager = manager
    }
    
    fun setOnFireworkStartedCallback(callback: () -> Unit) {
        onFireworkStarted = callback
    }
    
    fun setRainManager(manager: RainManager) {
        rainManager = manager
    }
    
    fun setOnRainStartedCallback(callback: () -> Unit) {
        onRainStarted = callback
    }
    
    // ==================== GESTION DU FEU D'ARTIFICE ====================
    
    fun triggerFirework() {
        fireworkManager?.startFirework()
        onFireworkStarted?.invoke()
        println("🎆 FEU D'ARTIFICE DÉCLENCHÉ ! 🎆")
    }
    
    fun isFireworkActive(): Boolean {
        return fireworkManager?.isPlaying() ?: false
    }
    
    // ==================== GESTION DE LA PLUIE ====================
    
    fun triggerRain(currentFlowerType: String, challengeData: MutableMap<String, Any>) {
        rainManager?.startRain()
        onRainStarted?.invoke()
        println("🌧️ LA PLUIE COMMENCE ! 🌧️")
        
        // Mettre à jour la progression de dissolution dans les gestionnaires de défis
        updateDissolveProgress(currentFlowerType, challengeData)
    }
    
    fun isRainActive(): Boolean {
        return rainManager?.isPlaying() ?: false
    }
    
    // ==================== GESTION DE LA DISSOLUTION ====================
    
    fun updateDissolveProgress(currentFlowerType: String, challengeData: MutableMap<String, Any>) {
        val dissolveProgress = rainManager?.getDissolveProgress() ?: 0f
        
        when (currentFlowerType) {
            "MARGUERITE" -> margueriteChallengeHandler.updateDissolveProgress(dissolveProgress, challengeData)
            "ROSE" -> roseChallengeHandler.updateDissolveProgress(dissolveProgress, challengeData)
            "LUPIN" -> lupinChallengeHandler.updateDissolveProgress(dissolveProgress, challengeData)
            "IRIS" -> irisChallengeHandler.updateDissolveProgress(dissolveProgress, challengeData)
        }
    }
    
    fun getDissolveProgress(currentFlowerType: String, challengeData: MutableMap<String, Any>): Float {
        return when (currentFlowerType) {
            "MARGUERITE" -> margueriteChallengeHandler.getDissolveProgress(challengeData)
            "ROSE" -> roseChallengeHandler.getDissolveProgress(challengeData)
            "LUPIN" -> lupinChallengeHandler.getDissolveProgress(challengeData)
            "IRIS" -> irisChallengeHandler.getDissolveProgress(challengeData)
            else -> 0f
        }
    }
    
    // ==================== EFFETS SPÉCIFIQUES PAR FLEUR ====================
    
    // Marguerite
    fun shouldMargueritePetalsFall(challengeData: MutableMap<String, Any>): Boolean {
        return margueriteChallengeHandler.shouldPetalsFall(challengeData)
    }
    
    fun shouldMargueriteStemsWilt(challengeData: MutableMap<String, Any>): Boolean {
        return margueriteChallengeHandler.shouldStemsWilt(challengeData)
    }
    
    // Rose
    fun shouldRosePetalsDroop(challengeData: MutableMap<String, Any>): Boolean {
        return roseChallengeHandler.shouldPetalsDroop(challengeData)
    }
    
    fun shouldRoseThornsWeaken(challengeData: MutableMap<String, Any>): Boolean {
        return roseChallengeHandler.shouldThornsWeaken(challengeData)
    }
    
    fun shouldRoseBranchesDissolve(challengeData: MutableMap<String, Any>): Boolean {
        return roseChallengeHandler.shouldBranchesDissolve(challengeData)
    }
    
    // Lupin
    fun shouldLupinSpikesWilt(challengeData: MutableMap<String, Any>): Boolean {
        return lupinChallengeHandler.shouldSpikesWilt(challengeData)
    }
    
    fun shouldLupinColorsBlend(challengeData: MutableMap<String, Any>): Boolean {
        return lupinChallengeHandler.shouldColorsBlend(challengeData)
    }
    
    fun shouldLupinStemsCollapse(challengeData: MutableMap<String, Any>): Boolean {
        return lupinChallengeHandler.shouldStemsCollapse(challengeData)
    }
    
    // Iris
    fun shouldIrisFallsDropPetals(challengeData: MutableMap<String, Any>): Boolean {
        return irisChallengeHandler.shouldFallsDropPetals(challengeData)
    }
    
    fun shouldIrisStandardsWilt(challengeData: MutableMap<String, Any>): Boolean {
        return irisChallengeHandler.shouldStandardsWilt(challengeData)
    }
    
    fun shouldIrisBeardDissolve(challengeData: MutableMap<String, Any>): Boolean {
        return irisChallengeHandler.shouldBeardDissolve(challengeData)
    }
    
    fun shouldIrisLeavesShrive(challengeData: MutableMap<String, Any>): Boolean {
        return irisChallengeHandler.shouldLeavesShrive(challengeData)
    }
    
    // ==================== CONTRÔLE GÉNÉRAL ====================
    
    fun stopAllEffects() {
        fireworkManager?.stop()
        rainManager?.stop()
    }
    
    fun updateEffects(deltaTime: Float) {
        fireworkManager?.update(deltaTime)
        rainManager?.update(deltaTime)
    }
    
    fun isAnyEffectActive(): Boolean {
        return isFireworkActive() || isRainActive()
    }
    
    // ==================== MÉTHODES POUR LES RENDERERS ====================
    
    /**
     * Retourne les informations de dissolution pour un type de fleur spécifique
     * Utilisé par les renderers pour adapter l'affichage
     */
    fun getDissolveInfo(flowerType: String, challengeData: MutableMap<String, Any>): DissolveInfo {
        val progress = getDissolveProgress(flowerType, challengeData)
        
        return when (flowerType) {
            "MARGUERITE" -> DissolveInfo(
                progress = progress,
                petalsFalling = shouldMargueritePetalsFall(challengeData),
                stemsWilting = shouldMargueriteStemsWilt(challengeData)
            )
            "ROSE" -> DissolveInfo(
                progress = progress,
                petalsDrooping = shouldRosePetalsDroop(challengeData),
                thornsWeakening = shouldRoseThornsWeaken(challengeData),
                branchesDissolving = shouldRoseBranchesDissolve(challengeData)
            )
            "LUPIN" -> DissolveInfo(
                progress = progress,
                spikesWilting = shouldLupinSpikesWilt(challengeData),
                colorsBlending = shouldLupinColorsBlend(challengeData),
                stemsCollapsing = shouldLupinStemsCollapse(challengeData)
            )
            "IRIS" -> DissolveInfo(
                progress = progress,
                fallsDropping = shouldIrisFallsDropPetals(challengeData),
                standardsWilting = shouldIrisStandardsWilt(challengeData),
                beardDissolving = shouldIrisBeardDissolve(challengeData),
                leavesShriveling = shouldIrisLeavesShrive(challengeData)
            )
            else -> DissolveInfo(progress = 0f)
        }
    }
}
