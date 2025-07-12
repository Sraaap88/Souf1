package com.example.souffleforcetest

class RoseChallengeHandler {
    
    // ==================== LOGIQUE DES DÉFIS ROSIER ====================
    
    fun updateChallenge(
        challengeId: Int, 
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        when (challengeId) {
            1 -> updateRoseChallenge1_FlowersInZone(force, plantState, challengeData)
            2 -> updateRoseChallenge2_Divisions(force, plantState, challengeData)
            3 -> updateRoseChallenge3_FlowersAndZone(force, plantState, challengeData)
        }
    }
    
    private fun updateRoseChallenge1_FlowersInZone(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
    }
    
    private fun updateRoseChallenge2_Divisions(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
    }
    
    private fun updateRoseChallenge3_FlowersAndZone(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
    }
    
    fun checkChallenge(
        challengeId: Int,
        roseFlowersInZone: List<String>,
        roseDivisions: List<String>,
        roseTotalFlowers: List<String>,
        roseFlowersInZoneDefi3: List<String>
    ): Boolean {
        return when (challengeId) {
            1 -> roseFlowersInZone.size >= 6
            2 -> roseDivisions.size >= 10
            3 -> roseTotalFlowers.size >= 15 && roseFlowersInZoneDefi3.size >= 5 && roseDivisions.size >= 8
            else -> false
        }
    }
    
    fun getSuccessMessage(
        challengeId: Int,
        roseFlowersInZone: List<String>,
        roseDivisions: List<String>,
        roseTotalFlowers: List<String>,
        roseFlowersInZoneDefi3: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Défi réussi! ${roseFlowersInZone.size} fleurs parfaitement alignées en zone verte!"
            2 -> "Défi réussi! ${roseDivisions.size} divisions créées avec maîtrise!"
            3 -> "Défi réussi! ${roseTotalFlowers.size} fleurs (${roseFlowersInZoneDefi3.size} en zone) + ${roseDivisions.size} divisions!\n🌼 LUPIN DÉBLOQUÉ!"
            else -> "Défi réussi!"
        }
    }
    
    fun getFailMessage(
        challengeId: Int,
        roseFlowersInZone: List<String>,
        roseDivisions: List<String>,
        roseTotalFlowers: List<String>,
        roseFlowersInZoneDefi3: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Défi échoué - Seulement ${roseFlowersInZone.size}/6 fleurs en zone verte!"
            2 -> "Défi échoué - Seulement ${roseDivisions.size}/10 divisions créées!"
            3 -> "Défi échoué - ${roseTotalFlowers.size}/15 fleurs (${roseFlowersInZoneDefi3.size}/5 en zone) + ${roseDivisions.size}/8 divisions!"
            else -> "Défi échoué!"
        }
    }
}
