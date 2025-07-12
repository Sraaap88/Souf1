package com.example.souffleforcetest

class OrchideeChallengeHandler {
    
    // ==================== LOGIQUE DES DÉFIS ORCHIDÉE ====================
    
    fun updateChallenge(
        challengeId: Int, 
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        when (challengeId) {
            1 -> updateOrchideeChallenge1_Perfect(force, plantState, challengeData)
            2 -> updateOrchideeChallenge2_Precision(force, plantState, challengeData)
            3 -> updateOrchideeChallenge3_Expertise(force, plantState, challengeData)
        }
    }
    
    private fun updateOrchideeChallenge1_Perfect(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
    }
    
    private fun updateOrchideeChallenge2_Precision(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
    }
    
    private fun updateOrchideeChallenge3_Expertise(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
    }
    
    fun checkChallenge(
        challengeId: Int,
        orchideeFlowers: List<String>,
        orchideeRamifications: List<String>,
        orchideeInZone: List<String>
    ): Boolean {
        return when (challengeId) {
            1 -> orchideeFlowers.size >= 2
            2 -> orchideeFlowers.size >= 5
            3 -> orchideeFlowers.size >= 8 && orchideeRamifications.size >= 4 && orchideeInZone.size >= 3
            else -> false
        }
    }
    
    fun getSuccessMessage(
        challengeId: Int,
        orchideeFlowers: List<String>,
        orchideeRamifications: List<String>,
        orchideeInZone: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Défi réussi! ${orchideeFlowers.size} orchidées royales parfaites!"
            2 -> "Défi réussi! ${orchideeFlowers.size} orchidées avec contrôle précis!"
            3 -> "Défi réussi! Maîtrise totale - ${orchideeFlowers.size} orchidées + ${orchideeRamifications.size} ramifications!\n👑 EXPERT JARDINIER!"
            else -> "Défi réussi!"
        }
    }
    
    fun getFailMessage(
        challengeId: Int,
        orchideeFlowers: List<String>,
        orchideeRamifications: List<String>,
        orchideeInZone: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Défi échoué - Seulement ${orchideeFlowers.size}/2 orchidées parfaites!"
            2 -> "Défi échoué - Seulement ${orchideeFlowers.size}/5 orchidées précises!"
            3 -> "Défi échoué - ${orchideeFlowers.size}/8 orchidées + ${orchideeRamifications.size}/4 ramifications + ${orchideeInZone.size}/3 en zone!"
            else -> "Défi échoué!"
        }
    }
}
