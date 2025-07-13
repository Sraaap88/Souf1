package com.example.souffleforcetest

class IrisChallengeHandler {
    
    // ==================== LOGIQUE DES DÉFIS IRIS CORRIGÉE ====================
    
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
    
    // ==================== SUPPORT DISSOLUTION ====================
    
    /**
     * Fonction appelée pour déclencher la dissolution des iris lors d'un échec
     * @param dissolveProgress Progression de la dissolution (0.0 = intact, 1.0 = complètement dissous)
     */
    fun updateDissolveProgress(dissolveProgress: Float, challengeData: MutableMap<String, Any>) {
        challengeData["dissolveProgress"] = dissolveProgress.coerceIn(0f, 1f)
        
        // Effets spécifiques aux iris lors de la dissolution
        if (dissolveProgress > 0.2f) {
            challengeData["fallsPetalsDropping"] = true // Les pétales "falls" tombent d'abord
        }
        if (dissolveProgress > 0.4f) {
            challengeData["standardsWilting"] = true // Les pétales "standards" flétrissent
        }
        if (dissolveProgress > 0.6f) {
            challengeData["beardDissolving"] = true // La barbe se dissout
        }
        if (dissolveProgress > 0.8f) {
            challengeData["leavesShriveling"] = true // Les feuilles se ratatinent
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
     * Indique si les pétales "falls" doivent tomber
     */
    fun shouldFallsDropPetals(challengeData: MutableMap<String, Any>): Boolean {
        return challengeData["fallsPetalsDropping"] as? Boolean ?: false
    }
    
    /**
     * Indique si les pétales "standards" doivent flétrir
     */
    fun shouldStandardsWilt(challengeData: MutableMap<String, Any>): Boolean {
        return challengeData["standardsWilting"] as? Boolean ?: false
    }
    
    /**
     * Indique si la barbe doit se dissoudre
     */
    fun shouldBeardDissolve(challengeData: MutableMap<String, Any>): Boolean {
        return challengeData["beardDissolving"] as? Boolean ?: false
    }
    
    /**
     * Indique si les feuilles doivent se ratatiner
     */
    fun shouldLeavesShrive(challengeData: MutableMap<String, Any>): Boolean {
        return challengeData["leavesShriveling"] as? Boolean ?: false
    }
    
    /**
     * Reset de la dissolution pour un nouveau défi
     */
    fun resetDissolveEffects(challengeData: MutableMap<String, Any>) {
        challengeData.remove("dissolveProgress")
        challengeData.remove("fallsPetalsDropping")
        challengeData.remove("standardsWilting")
        challengeData.remove("beardDissolving")
        challengeData.remove("leavesShriveling")
        challengeData.remove("fullyDissolved")
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
    
    // ==================== CONDITIONS DE DÉFIS CORRIGÉES ====================
    
    fun checkChallenge(
        challengeId: Int,
        irisFlowersInZone: List<String>,
        irisRamifications: List<String>, // Plus utilisé mais gardé pour compatibilité
        irisTotalFlowers: List<String>
    ): Boolean {
        return when (challengeId) {
            1 -> {
                // DÉFI 1 CORRIGÉ: 4 iris en zone centrale (2 pouces de haut)
                irisFlowersInZone.size >= 4
            }
            2 -> {
                // DÉFI 2 CORRIGÉ: 10 iris total avec ramifications en zone centrale
                irisTotalFlowers.size >= 10 && irisFlowersInZone.size >= 6
            }
            3 -> {
                // DÉFI 3 CORRIGÉ: 16 iris total dont 8 en zone centrale (2 pouces)
                irisTotalFlowers.size >= 16 && irisFlowersInZone.size >= 8
            }
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
            1 -> "Défi réussi! ${irisFlowersInZone.size} iris élégants en zone centrale (2 pouces)!"
            2 -> "Défi réussi! ${irisTotalFlowers.size} iris au total dont ${irisFlowersInZone.size} en zone centrale!"
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
            1 -> "Défi échoué - Seulement ${irisFlowersInZone.size}/4 iris en zone centrale (2 pouces)!"
            2 -> "Défi échoué - ${irisTotalFlowers.size}/10 iris total et ${irisFlowersInZone.size}/6 en zone centrale!"
            3 -> "Défi échoué - ${irisTotalFlowers.size}/16 iris total et ${irisFlowersInZone.size}/8 en zone centrale!"
            else -> "Défi échoué!"
        }
    }
}
