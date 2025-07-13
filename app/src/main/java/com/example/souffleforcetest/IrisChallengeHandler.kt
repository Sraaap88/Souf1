package com.example.souffleforcetest

class IrisChallengeHandler {
    
    // ==================== LOGIQUE DES DÉFIS IRIS ====================
    
    fun updateChallenge(
        challengeId: Int, 
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        when (challengeId) {
            1 -> updateIrisChallenge1_CentralZone(force, plantState, challengeData)
            2 -> updateIrisChallenge2_MixedObjective(force, plantState, challengeData)
            3 -> updateIrisChallenge3_AdvancedMastery(force, plantState, challengeData)
        }
    }
    
    private fun updateIrisChallenge1_CentralZone(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
    }
    
    private fun updateIrisChallenge2_MixedObjective(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
    }
    
    private fun updateIrisChallenge3_AdvancedMastery(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
    }
    
    fun checkChallenge(
        challengeId: Int,
        irisFlowersInZone: List<String>,
        irisRamifications: List<String>, // Plus utilisé mais gardé pour compatibilité
        irisTotalFlowers: List<String>
    ): Boolean {
        return when (challengeId) {
            1 -> irisFlowersInZone.size >= 4 // Inchangé
            2 -> irisTotalFlowers.size >= 10 && irisFlowersInZone.size >= 6 // 10 total + 6 en zone
            3 -> irisTotalFlowers.size >= 16 && irisFlowersInZone.size >= 8 // 16 total + 8 en zone
            else -> false
        }
    }
    
    fun getSuccessMessage(
        challengeId: Int,
        irisFlowersInZone: List<String>,
        irisRamifications: List<String>, // Plus utilisé mais gardé pour compatibilité
        irisTotalFlowers: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Défi réussi! ${irisFlowersInZone.size} iris élégants en zone centrale!"
            2 -> "Défi réussi! ${irisTotalFlowers.size} iris au total dont ${irisFlowersInZone.size} en zone spécifique!"
            3 -> "Défi réussi! ${irisTotalFlowers.size} iris magnifiques dont ${irisFlowersInZone.size} en zone centrale!\n🌸 ORCHIDÉE DÉBLOQUÉE!"
            else -> "Défi réussi!"
        }
    }
    
    fun getFailMessage(
        challengeId: Int,
        irisFlowersInZone: List<String>,
        irisRamifications: List<String>, // Plus utilisé mais gardé pour compatibilité
        irisTotalFlowers: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Défi échoué - Seulement ${irisFlowersInZone.size}/4 iris en zone centrale!"
            2 -> "Défi échoué - ${irisTotalFlowers.size}/10 iris total et ${irisFlowersInZone.size}/6 en zone spécifique!"
            3 -> "Défi échoué - ${irisTotalFlowers.size}/16 iris total et ${irisFlowersInZone.size}/8 en zone centrale!"
            else -> "Défi échoué!"
        }
    }
}
