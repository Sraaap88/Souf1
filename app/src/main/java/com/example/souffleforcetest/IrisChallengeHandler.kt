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
            2 -> updateIrisChallenge2_Ramifications(force, plantState, challengeData)
            3 -> updateIrisChallenge3_Mastery(force, plantState, challengeData)
        }
    }
    
    private fun updateIrisChallenge1_CentralZone(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
    }
    
    private fun updateIrisChallenge2_Ramifications(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
    }
    
    private fun updateIrisChallenge3_Mastery(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
    }
    
    fun checkChallenge(
        challengeId: Int,
        irisFlowersInZone: List<String>,
        irisRamifications: List<String>,
        irisTotalFlowers: List<String>
    ): Boolean {
        return when (challengeId) {
            1 -> irisFlowersInZone.size >= 4
            2 -> irisRamifications.size >= 8
            3 -> irisTotalFlowers.size >= 12 && irisRamifications.size >= 6
            else -> false
        }
    }
    
    fun getSuccessMessage(
        challengeId: Int,
        irisFlowersInZone: List<String>,
        irisRamifications: List<String>,
        irisTotalFlowers: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Défi réussi! ${irisFlowersInZone.size} iris élégants en zone centrale!"
            2 -> "Défi réussi! ${irisRamifications.size} ramifications parfaites!"
            3 -> "Défi réussi! ${irisTotalFlowers.size} iris + ${irisRamifications.size} ramifications!\n🌺 ORCHIDÉE DÉBLOQUÉE!"
            else -> "Défi réussi!"
        }
    }
    
    fun getFailMessage(
        challengeId: Int,
        irisFlowersInZone: List<String>,
        irisRamifications: List<String>,
        irisTotalFlowers: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Défi échoué - Seulement ${irisFlowersInZone.size}/4 iris en zone centrale!"
            2 -> "Défi échoué - Seulement ${irisRamifications.size}/8 ramifications créées!"
            3 -> "Défi échoué - ${irisTotalFlowers.size}/12 iris + ${irisRamifications.size}/6 ramifications!"
            else -> "Défi échoué!"
        }
    }
}
