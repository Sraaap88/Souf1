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
            description = "Faire pousser 1 fleur dans la zone verte",
            briefText = "Défi 1: 1 fleur en zone verte"
        ),
        Challenge(
            id = 2,
            title = "Défi 2: Bourgeons", 
            description = "Faire pousser 2 bourgeons avec souffle doux",
            briefText = "Défi 2: 2 bourgeons",
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
    private var budsCreated = mutableListOf<String>()  // Liste des bourgeons créés
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun getMargueriteChallenges(): List<Challenge> = margueriteChallenges
    
    fun startChallenge(challengeId: Int) {
        currentChallenge = margueriteChallenges.find { it.id == challengeId }
        challengeStartTime = System.currentTimeMillis()
        challengeData.clear()
        flowersInZone.clear()  // Reset liste des fleurs
        budsCreated.clear()    // Reset liste des bourgeons
        println("Défi démarré: ${currentChallenge?.title}")
    }
    
    fun getCurrentChallenge(): Challenge? = currentChallenge
    
    fun getCurrentChallengeBrief(): String? = currentChallenge?.briefText
    
    fun updateChallengeProgress(force: Float, plantState: String) {
        val challenge = currentChallenge ?: return
        
        when (challenge.id) {
            1 -> updateChallenge1_FlowersInZone(force, plantState)
            2 -> updateChallenge2_Buds(force, plantState)
            3 -> updateChallenge3(force, plantState)
        }
    }
    
    // Fonction pour signaler qu'une fleur a été créée
    fun notifyFlowerCreated(flowerX: Float, flowerY: Float, flowerId: String) {
        val challenge = currentChallenge ?: return
        
        if (challenge.id == 1) {
            // Vérifier si la fleur est dans la zone verte (1/3 de l'écran, hauteur étendue vers le bas)
            val screenHeight = challengeData["screenHeight"] as? Float ?: 2000f
            val zoneTop = screenHeight / 3f - 60f
            val zoneBottom = screenHeight / 3f + 360f  // 2 fois plus large que la version précédente (420px total)
            
            if (flowerY >= zoneTop && flowerY <= zoneBottom) {
                if (!flowersInZone.contains(flowerId)) {
                    flowersInZone.add(flowerId)
                    challengeData["flowersInZoneCount"] = flowersInZone.size
                    println("Fleur dans la zone! Total: ${flowersInZone.size}/1")
                }
            } else {
                println("Fleur HORS zone: Y=${flowerY}, Zone=${zoneTop}-${zoneBottom}")
            }
        }
    }
    
    // Fonction pour signaler qu'un bourgeon a été créé
    fun notifyBudCreated(budX: Float, budY: Float, budId: String) {
        val challenge = currentChallenge ?: return
        
        if (challenge.id == 2) {
            if (!budsCreated.contains(budId)) {
                budsCreated.add(budId)
                challengeData["budsCreatedCount"] = budsCreated.size
                println("Bourgeon créé! Total: ${budsCreated.size}/2 (ID: $budId)")
            }
        }
    }
    
    // Mettre à jour les dimensions d'écran pour le calcul de zone
    fun updateScreenDimensions(width: Int, height: Int) {
        challengeData["screenWidth"] = width.toFloat()
        challengeData["screenHeight"] = height.toFloat()
    }
    
    fun checkChallengeCompletion(): ChallengeResult? {
        val challenge = currentChallenge ?: return null
        
        val isSuccessful = when (challenge.id) {
            1 -> checkChallenge1_FlowersInZone()
            2 -> checkChallenge2_Buds()
            3 -> checkChallenge3Completion()
            else -> false
        }
        
        if (isSuccessful) {
            challenge.isCompleted = true
            unlockNextChallenge(challenge.id)
            
            val successMessage = when (challenge.id) {
                1 -> "Défi réussi! ${flowersInZone.size} fleur dans la zone!"
                2 -> "Défi réussi! ${budsCreated.size} bourgeons créés!"
                else -> "Défi réussi!"
            }
            
            return ChallengeResult(challenge, true, successMessage)
        }
        
        return null  // Défi encore en cours
    }
    
    fun finalizeChallengeResult(): ChallengeResult? {
        val challenge = currentChallenge ?: return null
        
        val result = checkChallengeCompletion() ?: run {
            val failMessage = when (challenge.id) {
                1 -> "Défi échoué - Aucune fleur en zone verte!"
                2 -> "Défi échoué - Seulement ${budsCreated.size}/2 bourgeons créés!"
                else -> "Défi échoué!"
            }
            ChallengeResult(challenge, false, failMessage)
        }
        
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
        // MODIFIÉ: Succès si au moins 1 fleur dans la zone verte
        return flowersInZone.size >= 1
    }
    
    // ==================== LOGIQUE DU DÉFI 2: BOURGEONS ====================
    
    private fun updateChallenge2_Buds(force: Float, plantState: String) {
        // Le suivi se fait principalement via notifyBudCreated() quand un bourgeon est créé
        challengeData["currentPhase"] = plantState
        challengeData["totalBuds"] = budsCreated.size
        
        // Optionnel: Suivre la qualité du souffle pour des stats
        if (force > 0) {
            val currentAvgForce = challengeData["avgForce"] as? Float ?: 0f
            val forceCount = challengeData["forceCount"] as? Int ?: 0
            val newAvgForce = (currentAvgForce * forceCount + force) / (forceCount + 1)
            challengeData["avgForce"] = newAvgForce
            challengeData["forceCount"] = forceCount + 1
            
            // Suivre si le joueur respecte la technique "souffle doux"
            val gentleBreathCount = challengeData["gentleBreathCount"] as? Int ?: 0
            if (force < 0.3f) { // Seuil pour "souffle doux"
                challengeData["gentleBreathCount"] = gentleBreathCount + 1
            }
        }
    }
    
    private fun checkChallenge2_Buds(): Boolean {
        // Succès si au moins 2 bourgeons ont été créés
        return budsCreated.size >= 2
    }
    
    // ==================== LOGIQUE TEMPORAIRE DES AUTRES DÉFIS ====================
    
    private fun updateChallenge3(force: Float, plantState: String) {
        // TEMPORAIRE - Exemple simple
        if (plantState == "FLOWER") {
            challengeData["flowerTime"] = (System.currentTimeMillis() - challengeStartTime).toFloat()
        }
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
