package com.example.souffleforcetest

class MargueriteChallengeHandler {
    
    // ==================== LOGIQUE DES DÉFIS MARGUERITE ====================
    
    fun updateChallenge(
        challengeId: Int, 
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        when (challengeId) {
            1 -> updateMargueriteChallenge1_FlowersInZone(force, plantState, challengeData)
            2 -> updateMargueriteChallenge2_Buds(force, plantState, challengeData)
            3 -> updateMargueriteChallenge3_FlowersAndBuds(force, plantState, challengeData)
        }
    }
    
    private fun updateMargueriteChallenge1_FlowersInZone(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
    }
    
    private fun updateMargueriteChallenge2_Buds(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
        
        if (force > 0) {
            val currentAvgForce = challengeData["avgForce"] as? Float ?: 0f
            val forceCount = challengeData["forceCount"] as? Int ?: 0
            val newAvgForce = (currentAvgForce * forceCount + force) / (forceCount + 1)
            challengeData["avgForce"] = newAvgForce
            challengeData["forceCount"] = forceCount + 1
            
            val gentleBreathCount = challengeData["gentleBreathCount"] as? Int ?: 0
            if (force < 0.3f) {
                challengeData["gentleBreathCount"] = gentleBreathCount + 1
            }
        }
    }
    
    private fun updateMargueriteChallenge3_FlowersAndBuds(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
        
        if (force > 0) {
            val currentAvgForce = challengeData["avgForceDefi3"] as? Float ?: 0f
            val forceCount = challengeData["forceCountDefi3"] as? Int ?: 0
            val newAvgForce = (currentAvgForce * forceCount + force) / (forceCount + 1)
            challengeData["avgForceDefi3"] = newAvgForce
            challengeData["forceCountDefi3"] = forceCount + 1
        }
    }
    
    fun checkChallenge(
        challengeId: Int, 
        flowersInZone: List<String>,
        budsCreated: List<String>,
        flowersInZoneDefi3: List<String>,
        budsCreatedDefi3: List<String>
    ): Boolean {
        return when (challengeId) {
            1 -> flowersInZone.size >= 1
            2 -> budsCreated.size >= 2
            3 -> flowersInZoneDefi3.size >= 2 && budsCreatedDefi3.size >= 1
            else -> false
        }
    }
    
    fun getSuccessMessage(
        challengeId: Int,
        flowersInZone: List<String>,
        budsCreated: List<String>,
        flowersInZoneDefi3: List<String>,
        budsCreatedDefi3: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Défi réussi! ${flowersInZone.size} fleur dans la zone!"
            2 -> "Défi réussi! ${budsCreated.size} bourgeons créés!"
            3 -> "Défi réussi! ${flowersInZoneDefi3.size} fleurs + ${budsCreatedDefi3.size} bourgeon!\n🌹 ROSE DÉBLOQUÉE!"
            else -> "Défi réussi!"
        }
    }
    
    fun getFailMessage(
        challengeId: Int,
        flowersInZone: List<String>,
        budsCreated: List<String>,
        flowersInZoneDefi3: List<String>,
        budsCreatedDefi3: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Défi échoué - Aucune fleur en zone verte!"
            2 -> "Défi échoué - Seulement ${budsCreated.size}/2 bourgeons créés!"
            3 -> "Défi échoué - ${flowersInZoneDefi3.size}/2 fleurs, ${budsCreatedDefi3.size}/1 bourgeon!"
            else -> "Défi échoué!"
        }
    }
}
