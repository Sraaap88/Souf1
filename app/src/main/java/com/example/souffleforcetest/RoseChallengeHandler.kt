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
    
    // ==================== CONDITIONS DE DÉFIS CORRIGÉES ====================
    
    fun checkChallenge(
        challengeId: Int,
        roseFlowersInZone: List<String>,
        roseDivisions: List<String>,
        roseTotalFlowers: List<String>,
        roseFlowersInZoneDefi3: List<String>
    ): Boolean {
        return when (challengeId) {
            1 -> {
                // DÉFI 1 CORRIGÉ: 6 fleurs dans la zone centrale (2 pouces de haut)
                roseFlowersInZone.size >= 6
            }
            2 -> {
                // DÉFI 2 INCHANGÉ: 10 divisions avec saccades précises
                roseDivisions.size >= 10
            }
            3 -> {
                // DÉFI 3 CORRIGÉ: 15 fleurs dont 5 en zone centrale (2 pouces) + 8 divisions
                roseTotalFlowers.size >= 15 && roseFlowersInZoneDefi3.size >= 5 && roseDivisions.size >= 8
            }
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
            1 -> "Défi réussi! ${roseFlowersInZone.size} fleurs parfaitement alignées en zone centrale (2 pouces)!"
            2 -> "Défi réussi! ${roseDivisions.size} divisions créées avec maîtrise des saccades!"
            3 -> "Défi réussi! ${roseTotalFlowers.size} fleurs (${roseFlowersInZoneDefi3.size} en zone centrale) + ${roseDivisions.size} divisions!\n🌼 LUPIN DÉBLOQUÉ!"
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
            1 -> "Défi échoué - Seulement ${roseFlowersInZone.size}/6 fleurs en zone centrale (2 pouces)!"
            2 -> "Défi échoué - Seulement ${roseDivisions.size}/10 divisions créées avec saccades!"
            3 -> "Défi échoué - ${roseTotalFlowers.size}/15 fleurs (${roseFlowersInZoneDefi3.size}/5 en zone centrale) + ${roseDivisions.size}/8 divisions!"
            else -> "Défi échoué!"
        }
    }
    
    // ==================== SUPPORT DISSOLUTION ====================
    
    /**
     * Fonction appelée pour déclencher la dissolution des roses lors d'un échec
     * @param dissolveProgress Progression de la dissolution (0.0 = intact, 1.0 = complètement dissous)
     */
    fun updateDissolveProgress(dissolveProgress: Float, challengeData: MutableMap<String, Any>) {
        challengeData["dissolveProgress"] = dissolveProgress.coerceIn(0f, 1f)
        
        // Effets spécifiques aux roses lors de la dissolution
        if (dissolveProgress > 0.2f) {
            challengeData["petalsDrooping"] = true // Les pétales s'affaissent
        }
        if (dissolveProgress > 0.4f) {
            challengeData["thornsWeakening"] = true // Les épines s'affaiblissent
        }
        if (dissolveProgress > 0.7f) {
            challengeData["branchesDissolving"] = true // Les branches se dissolvent
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
     * Indique si les pétales doivent s'affaisser
     */
    fun shouldPetalsDroop(challengeData: MutableMap<String, Any>): Boolean {
        return challengeData["petalsDrooping"] as? Boolean ?: false
    }
    
    /**
     * Indique si les épines doivent s'affaiblir
     */
    fun shouldThornsWeaken(challengeData: MutableMap<String, Any>): Boolean {
        return challengeData["thornsWeakening"] as? Boolean ?: false
    }
    
    /**
     * Indique si les branches doivent se dissoudre
     */
    fun shouldBranchesDissolve(challengeData: MutableMap<String, Any>): Boolean {
        return challengeData["branchesDissolving"] as? Boolean ?: false
    }
    
    /**
     * Reset de la dissolution pour un nouveau défi
     */
    fun resetDissolveEffects(challengeData: MutableMap<String, Any>) {
        challengeData.remove("dissolveProgress")
        challengeData.remove("petalsDrooping")
        challengeData.remove("thornsWeakening")
        challengeData.remove("branchesDissolving")
        challengeData.remove("fullyDissolved")
    }
}
