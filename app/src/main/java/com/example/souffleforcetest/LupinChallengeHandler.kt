package com.example.souffleforcetest

class LupinChallengeHandler {
     
    // ==================== LOGIQUE DES DÉFIS LUPIN AUGMENTÉE ====================
    
    fun updateChallenge(
        challengeId: Int, 
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        when (challengeId) {
            1 -> updateLupinChallenge1_ColoredSpikes(force, plantState, challengeData)
            2 -> updateLupinChallenge2_CompleteStems(force, plantState, challengeData)
            3 -> updateLupinChallenge3_TotalFlowers(force, plantState, challengeData)
        }
    }
    
    private fun updateLupinChallenge1_ColoredSpikes(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
    }
    
    private fun updateLupinChallenge2_CompleteStems(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
    }
    
    private fun updateLupinChallenge3_TotalFlowers(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
    }
    
    // ==================== CONDITIONS DE DÉFIS AUGMENTÉES ====================
    
    fun checkChallenge(
        challengeId: Int,
        lupinSpikeColors: Set<String>,
        lupinCompleteStems: List<String>,
        lupinFlowers: List<String>
    ): Boolean {
        return when (challengeId) {
            1 -> {
                // DÉFI 1 AUGMENTÉ: 3 → 4 couleurs d'épis
                lupinSpikeColors.size >= 4
            }
            2 -> {
                // DÉFI 2 AUGMENTÉ: 5 → 8 tiges complètes
                lupinCompleteStems.size >= 8
            }
            3 -> {
                // DÉFI 3 AUGMENTÉ: 12 → 20 fleurs individuelles
                lupinFlowers.size >= 20
            }
            else -> false
        }
    }
    
    fun getSuccessMessage(
        challengeId: Int,
        lupinSpikeColors: Set<String>,
        lupinCompleteStems: List<String>,
        lupinFlowers: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Défi réussi! ${lupinSpikeColors.size} épis de couleurs différentes!"
            2 -> "Défi réussi! ${lupinCompleteStems.size} tiges complètes!"
            3 -> "Défi réussi! ${lupinFlowers.size} fleurs dans les épis!\n🌺 IRIS DÉBLOQUÉ!"
            else -> "Défi réussi!"
        }
    }
    
    fun getFailMessage(
        challengeId: Int,
        lupinSpikeColors: Set<String>,
        lupinCompleteStems: List<String>,
        lupinFlowers: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Défi échoué - Seulement ${lupinSpikeColors.size}/4 couleurs d'épis!"
            2 -> "Défi échoué - Seulement ${lupinCompleteStems.size}/8 tiges complètes!"
            3 -> "Défi échoué - Seulement ${lupinFlowers.size}/20 fleurs dans les épis!"
            else -> "Défi échoué!"
        }
    }
    
    // ==================== SUPPORT DISSOLUTION ====================
    
    /**
     * Fonction appelée pour déclencher la dissolution des lupins lors d'un échec
     * @param dissolveProgress Progression de la dissolution (0.0 = intact, 1.0 = complètement dissous)
     */
    fun updateDissolveProgress(dissolveProgress: Float, challengeData: MutableMap<String, Any>) {
        challengeData["dissolveProgress"] = dissolveProgress.coerceIn(0f, 1f)
        
        // Effets spécifiques aux lupins lors de la dissolution
        if (dissolveProgress > 0.25f) {
            challengeData["spikesWilting"] = true // Les épis commencent à flétrir
        }
        if (dissolveProgress > 0.5f) {
            challengeData["colorsBlending"] = true // Les couleurs se mélangent/ternissent
        }
        if (dissolveProgress > 0.75f) {
            challengeData["stemsCollapsing"] = true // Les tiges s'effondrent
        }
        if (dissolveProgress >= 1f) {
            challengeData["fullyDissolved"] = true // Complètement dissous
        }
    }
    
    /**
     * Retourne le niveau de dissolution actuel
     */
    fun getDissolveProgress(challengeData: MutableMap<String, Any>): Float {
        return challengeData["dissolveProgress"] as? Float ?: 0f
    }
    
    /**
     * Indique si les épis doivent flétrir
     */
    fun shouldSpikesWilt(challengeData: MutableMap<String, Any>): Boolean {
        return challengeData["spikesWilting"] as? Boolean ?: false
    }
    
    /**
     * Indique si les couleurs doivent se ternir/se mélanger
     */
    fun shouldColorsBlend(challengeData: MutableMap<String, Any>): Boolean {
        return challengeData["colorsBlending"] as? Boolean ?: false
    }
    
    /**
     * Indique si les tiges doivent s'effondrer
     */
    fun shouldStemsCollapse(challengeData: MutableMap<String, Any>): Boolean {
        return challengeData["stemsCollapsing"] as? Boolean ?: false
    }
    
    /**
     * Reset de la dissolution pour un nouveau défi
     */
    fun resetDissolveEffects(challengeData: MutableMap<String, Any>) {
        challengeData.remove("dissolveProgress")
        challengeData.remove("spikesWilting")
        challengeData.remove("colorsBlending")
        challengeData.remove("stemsCollapsing")
        challengeData.remove("fullyDissolved")
    }
}
