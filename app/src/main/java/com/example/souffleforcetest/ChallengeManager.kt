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
    
    // ==================== DÉFIS MARGUERITE ====================
    
    private val margueriteChallenges = listOf(
        Challenge(
            id = 1,
            title = "Défi 1: Zone Verte",
            description = "Faire pousser au moins 2 fleurs dans la zone verte",
            briefText = "Défi 1: 2 fleurs en zone verte"
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
    private var challengeData = mutableMapOf<String, Any>()  // Pour stocker données du défi
    private var flowersInZone = mutableListOf<String>()  // Liste des fleurs dans la zone verte
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun getMargueriteChallenges(): List<Challenge> = margueriteChallenges
    
    fun startChallenge(challengeId: Int) {
        currentChallenge = margueriteChallenges.find { it.id == challengeId }
        challengeStartTime = System.currentTimeMillis()
        challengeData.clear()
        flowersInZone.clear()  // Reset liste des fleurs
        println("Défi démarré: ${currentChallenge?.title}")
    }
    
    fun getCurrentChallenge(): Challenge? = currentChallenge
    
    fun getCurrentChallengeBrief(): String? = currentChallenge?.briefText
    
    fun updateChallengeProgress(force: Float, plantState: String) {
        val challenge = currentChallenge ?: return
        
        when (challenge.id) {
            1 -> updateChallenge1_FlowersInZone(force, plantState)
            2 -> updateChallenge2(force, plantState) 
            3 -> updateChallenge3(force, plantState)
        }
    }
    
    // NOUVEAU: Fonction pour signaler qu'une fleur a été créée
    fun notifyFlowerCreated(flowerX: Float, flowerY: Float, flowerId: String) {
        val challenge = currentChallenge ?: return
        
        if (challenge.id == 1) {
            // Vérifier si la fleur est dans la zone verte (utiliser UIDrawingManager)
            // Pour l'instant, on simule la vérification - il faudra intégrer avec UIDrawingManager
            val screenHeight = 2000f  // À remplacer par la vraie valeur
            val zoneTop = screenHeight / 3f - 60f
            val zoneBottom = screenHeight / 3f + 60f
            
            if (flowerY >= zoneTop && flowerY <= zoneBottom) {
                if (!flowersInZone.contains(flowerId)) {
                    flowersInZone.add(flowerId)
                    challengeData["flowersInZoneCount"] = flowersInZone.size
                    println("Fleur dans la zone! Total: ${flowersInZone.size}/2")
                }
            }
        }
    }
    
    fun checkChallengeCompletion(): ChallengeResult? {
        val challenge = currentChallenge ?: return null
        
        val isSuccessful = when (challenge.id) {
            1 -> checkChallenge1_FlowersInZone()
            2 -> checkChallenge2Completion()
            3 -> checkChallenge3Completion()
            else -> false
        }
        
        if (isSuccessful) {
            challenge.isCompleted = true
            unlockNextChallenge(challenge.id)
            return ChallengeResult(challenge, true, "Défi réussi! ${flowersInZone.size} fleurs dans la zone!")
        }
        
        return null  // Défi encore en cours
    }
    
    fun finalizeChallengeResult(): ChallengeResult? {
        val challenge = currentChallenge ?: return null
        
        val result = checkChallengeCompletion() ?: ChallengeResult(
            challenge, 
            false, 
            "Défi échoué - Seulement ${flowersInZone.size}/2 fleurs en zone verte!"
        )
        
        currentChallenge = null
        return result
    }
    
    // ==================== LOGIQUE DU DÉFI 1: FLEURS EN ZONE VERTE ====================
    
    private fun updateChallenge1_FlowersInZone(force: Float, plantState: String) {
        // Le suivi se fait via notifyFlowerCreated() quand une fleur est créée
        challengeData["currentPhase"] = plantState
        challengeData["totalFlowers"] = flowersInZone.size
    }
    
    private fun checkChallenge1_FlowersInZone(): Boolean {
        // Succès si au moins 2 fleurs dans la zone verte
        return flowersInZone.size >= 2
    }
    
    // ==================== LOGIQUE TEMPORAIRE DES AUTRES DÉFIS ====================
    
    private fun updateChallenge1(force: Float, plantState: String) {
        // TEMPORAIRE - Exemple simple (ancien défi 1)
        challengeData["maxForce"] = maxOf(challengeData["maxForce"] as? Float ?: 0f, force)
    }
    
    private fun updateChallenge2(force: Float, plantState: String) {
        // TEMPORAIRE - Exemple simple
        challengeData["totalForce"] = (challengeData["totalForce"] as? Float ?: 0f) + force
    }
    
    private fun updateChallenge3(force: Float, plantState: String) {
        // TEMPORAIRE - Exemple simple
        if (plantState == "FLOWER") {
            challengeData["flowerTime"] = (System.currentTimeMillis() - challengeStartTime).toFloat()
        }
    }
    
    private fun checkChallenge1Completion(): Boolean {
        // TEMPORAIRE - Ancien défi 1 (condition bidon)
        return (challengeData["maxForce"] as? Float ?: 0f) > 0.5f
    }
    
    private fun checkChallenge2Completion(): Boolean {
        // TEMPORAIRE - Condition bidon  
        return (challengeData["totalForce"] as? Float ?: 0f) > 10f
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
