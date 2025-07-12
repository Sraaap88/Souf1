package com.example.souffleforcetest

class IrisChallengeHandler {
    
    fun updateChallenge(
        challengeId: Int, 
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        when (challengeId) {
            1 -> updateIrisChallenge1_CentralZone(force, plantState, challengeData)
            2 -> updateIrisChallenge2_RamifiedStems(force, plantState, challengeData)
            3 -> updateIrisChallenge3_MasterIris(force, plantState, challengeData)
        }
    }
    
    private fun updateIrisChallenge1_CentralZone(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
    }
    
    private fun updateIrisChallenge2_RamifiedStems(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
    }
    
    private fun updateIrisChallenge3_MasterIris(
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
            1 -> "Défi réussi! ${irisFlowersInZone.size} iris élégants dans la zone centrale!"
            2 -> "Défi réussi! ${irisRamifications.size} tiges ramifiées créées!"
            3 -> "Défi réussi! ${irisTotalFlowers.size} iris parfaits + ${irisRamifications.size} ramifications!\n🌺 Maître des Iris!"
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
            2 -> "Défi échoué - Seulement ${irisRamifications.size}/8 tiges ramifiées!"
            3 -> "Défi échoué - Iris: ${irisTotalFlowers.size}/12, Ramifications: ${irisRamifications.size}/6!"
            else -> "Défi échoué!"
        }
    }
}
