package com.example.souffleforcetest

class LupinChallengeHandler {
    
    // ==================== LOGIQUE DES DÉFIS LUPIN MODIFIÉE ====================
    
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
    
    fun checkChallenge(
        challengeId: Int,
        lupinSpikeColors: Set<String>,
        lupinCompleteStems: List<String>,
        lupinFlowers: List<String>
    ): Boolean {
        return when (challengeId) {
            1 -> lupinSpikeColors.size >= 3
            2 -> lupinCompleteStems.size >= 5
            3 -> lupinFlowers.size >= 12  // MODIFIÉ: 15 → 12 fleurs
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
            3 -> "Défi réussi! ${lupinFlowers.size} fleurs dans les épis!\n🌺 IRIS DÉBLOQUÉ!"  // MODIFIÉ: texte mis à jour
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
            1 -> "Défi échoué - Seulement ${lupinSpikeColors.size}/3 couleurs d'épis!"
            2 -> "Défi échoué - Seulement ${lupinCompleteStems.size}/5 tiges complètes!"
            3 -> "Défi échoué - Seulement ${lupinFlowers.size}/12 fleurs dans les épis!"  // MODIFIÉ: 15 → 12
            else -> "Défi échoué!"
        }
    }
}
