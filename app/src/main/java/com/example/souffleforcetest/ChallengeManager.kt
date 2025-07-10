package com.example.souffleforcetest

class ChallengeManager {
    
    // ==================== DATA CLASSES ====================
    
    data class Challenge(
        val id: Int,
        val title: String,
        val description: String,
        val briefText: String,  // Texte affiché pendant le jeu
        var isCompleted: Boolean = false,
        var isUnlocked: Boolean = true  // Pour l'instant tous débloqués
    )
    
    // ==================== DÉFIS TEMPORAIRES (À REMPLACER) ====================
    
    private val margueriteChallenges = listOf(
        Challenge(
            id = 1,
            title = "Défi 1: Contrôle",
            description = "Défi temporaire - À définir ensemble",
            briefText = "Défi 1: À définir"
        ),
        Challenge(
            id = 2,
            title = "Défi 2: Endurance", 
            description = "Défi temporaire - À définir ensemble",
            briefText = "Défi 2: À définir",
            isUnlocked = false  // Débloqué après défi 1
        ),
        Challenge(
            id = 3,
            title = "Défi 3: Précision",
            description = "Défi temporaire - À définir ensemble", 
            briefText = "Défi 3: À définir",
            isUnlocked = false  // Débloqué après défi 2
        )
    )
    
    // ==================== VARIABLES D'ÉTAT ====================
    
    private var currentChallenge: Challenge? = null
    private var challengeStartTime = 0L
    private var challengeData = mutableMapOf<String, Float>()  // Pour stocker données du défi
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun getMargueriteChallenges(): List<Challenge> = margueriteChallenges
    
    fun startChallenge(challengeId: Int) {
        currentChallenge = margueriteChallenges.find { it.id == challengeId }
        challengeStartTime = System.currentTimeMillis()
        challengeData.clear()
        println("Défi démarré: ${currentChallenge?.title}")
    }
    
    fun getCurrentChallenge(): Challenge? = currentChallenge
    
    fun getCurrentChallengeBrief(): String? = currentChallenge?.briefText
    
    fun updateChallengeProgress(force: Float, plantState: String) {
        val challenge = currentChallenge ?: return
        
        // LOGIQUE TEMPORAIRE - À REMPLACER PAR LES VRAIS DÉFIS
        when (challenge.id) {
            1 -> updateChallenge1(force, plantState)
            2 -> updateChallenge2(force, plantState) 
            3 -> updateChallenge3(force, plantState)
        }
    }
    
    fun checkChallengeCompletion(): ChallengeResult? {
        val challenge = currentChallenge ?: return null
        
        // LOGIQUE TEMPORAIRE - À REMPLACER
        val isSuccessful = when (challenge.id) {
            1 -> checkChallenge1Completion()
            2 -> checkChallenge2Completion()
            3 -> checkChallenge3Completion()
            else -> false
        }
        
        if (isSuccessful) {
            challenge.isCompleted = true
            unlockNextChallenge(challenge.id)
            return ChallengeResult(challenge, true, "Défi réussi!")
        }
        
        return null  // Défi encore en cours
    }
    
    fun finalizeChallengeResult(): ChallengeResult? {
        val challenge = currentChallenge ?: return null
        
        val result = checkChallengeCompletion() ?: ChallengeResult(
            challenge, 
            false, 
            "Défi échoué - Réessayez!"
        )
        
        currentChallenge = null
        return result
    }
    
    // ==================== LOGIQUE TEMPORAIRE DES DÉFIS ====================
    
    private fun updateChallenge1(force: Float, plantState: String) {
        // TEMPORAIRE - Exemple simple
        challengeData["maxForce"] = maxOf(challengeData["maxForce"] ?: 0f, force)
    }
    
    private fun updateChallenge2(force: Float, plantState: String) {
        // TEMPORAIRE - Exemple simple
        challengeData["totalForce"] = (challengeData["totalForce"] ?: 0f) + force
    }
    
    private fun updateChallenge3(force: Float, plantState: String) {
        // TEMPORAIRE - Exemple simple
        if (plantState == "FLOWER") {
            challengeData["flowerTime"] = (System.currentTimeMillis() - challengeStartTime).toFloat()
        }
    }
    
    private fun checkChallenge1Completion(): Boolean {
        // TEMPORAIRE - Condition bidon
        return (challengeData["maxForce"] ?: 0f) > 0.5f
    }
    
    private fun checkChallenge2Completion(): Boolean {
        // TEMPORAIRE - Condition bidon  
        return (challengeData["totalForce"] ?: 0f) > 10f
    }
    
    private fun checkChallenge3Completion(): Boolean {
        // TEMPORAIRE - Condition bidon
        return challengeData.containsKey("flowerTime")
    }
    
    // ==================== GESTION DU DÉBLOCAGE ====================
    
    private fun unlockNextChallenge(completedId: Int) {
        when (completedId) {
            1 -> margueriteChallenges.find { it.id == 2 }?.isUnlocked = true
            2 -> margueriteChallenges.find { it.id == 3 }?.isUnlocked = true
        }
    }
    
    // ==================== SAUVEGARDE (SIMPLE POUR L'INSTANT) ====================
    
    fun getCompletionStatus(): String {
        val completed = margueriteChallenges.count { it.isCompleted }
        return "Marguerite: $completed/3 défis"
    }
    
    fun resetAllChallenges() {
        margueriteChallenges.forEach { 
            it.isCompleted = false
            it.isUnlocked = (it.id == 1)  // Seul le premier débloqué
        }
    }
    
    // ==================== RÉSULTAT ====================
    
    data class ChallengeResult(
        val challenge: Challenge,
        val success: Boolean,
        val message: String
    )
}
