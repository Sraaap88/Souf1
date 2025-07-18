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
    
    // ==================== CONDITIONS DE DÉFIS CORRIGÉES ====================
    
    fun checkChallenge(
        challengeId: Int, 
        flowersInZone: List<String>,
        budsCreated: List<String>,
        flowersInZoneDefi3: List<String>,
        budsCreatedDefi3: List<String>
    ): Boolean {
        return when (challengeId) {
            1 -> {
                // DÉFI 1 CORRIGÉ: 1 fleur dans la zone verte (1 pouce de haut)
                flowersInZone.size >= 1
            }
            2 -> {
                // DÉFI 2 INCHANGÉ: 2 bourgeons avec souffle doux
                budsCreated.size >= 2
            }
            3 -> {
                // DÉFI 3 CORRIGÉ: 2 fleurs en zone verte (1 pouce) ET 1 bourgeon
                flowersInZoneDefi3.size >= 2 && budsCreatedDefi3.size >= 1
            }
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
            1 -> "Défi réussi! ${flowersInZone.size} fleur dans la zone verte (1 pouce)!"
            2 -> "Défi réussi! ${budsCreated.size} bourgeons créés avec souffle doux!"
            3 -> "Défi réussi! ${flowersInZoneDefi3.size} fleurs en zone verte + ${budsCreatedDefi3.size} bourgeon!\n🌹 ROSIER DÉBLOQUÉ!"
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
            1 -> "Défi échoué - Aucune fleur dans la zone verte (1 pouce de haut)!"
            2 -> "Défi échoué - Seulement ${budsCreated.size}/2 bourgeons créés avec souffle doux!"
            3 -> "Défi échoué - ${flowersInZoneDefi3.size}/2 fleurs en zone verte, ${budsCreatedDefi3.size}/1 bourgeon!"
            else -> "Défi échoué!"
        }
    }
    
    // ==================== SUPPORT DISSOLUTION ====================
    
    /**
     * Fonction appelée pour déclencher la dissolution des marguerites lors d'un échec
     * @param dissolveProgress Progression de la dissolution (0.0 = intact, 1.0 = complètement dissous)
     */
    fun updateDissolveProgress(dissolveProgress: Float, challengeData: MutableMap<String, Any>) {
        challengeData["dissolveProgress"] = dissolveProgress.coerceIn(0f, 1f)
        
        // Effets spécifiques aux marguerites lors de la dissolution
        if (dissolveProgress > 0.3f) {
            challengeData["petalsFalling"] = true // Les pétales commencent à tomber
        }
        if (dissolveProgress > 0.6f) {
            challengeData["stemsWilting"] = true // Les tiges flétrissent
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
     * Indique si les pétales doivent tomber
     */
    fun shouldPetalsFall(challengeData: MutableMap<String, Any>): Boolean {
        return challengeData["petalsFalling"] as? Boolean ?: false
    }
    
    /**
     * Indique si les tiges doivent flétrir
     */
    fun shouldStemsWilt(challengeData: MutableMap<String, Any>): Boolean {
        return challengeData["stemsWilting"] as? Boolean ?: false
    }
    
    /**
     * Reset de la dissolution pour un nouveau défi
     */
    fun resetDissolveEffects(challengeData: MutableMap<String, Any>) {
        challengeData.remove("dissolveProgress")
        challengeData.remove("petalsFalling")
        challengeData.remove("stemsWilting")
        challengeData.remove("fullyDissolved")
    }
}
