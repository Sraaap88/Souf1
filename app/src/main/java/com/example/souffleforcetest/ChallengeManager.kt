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
            description = "Faire 2 fleurs en zone verte ET 1 bourgeon", 
            briefText = "Défi 3: 2 fleurs + 1 bourgeon",
            isUnlocked = false  // Débloqué après défi 2
        )
    )
    
    // ==================== VARIABLES D'ÉTAT ====================
    
    private var currentChallenge: Challenge? = null
    private var challengeStartTime = 0L
    private var challengeData = mutableMapOf<String, Any>()  // Pour stocker données du défi
    private var flowersInZone = mutableListOf<String>()  // Liste des fleurs dans la zone verte
    private var budsCreated = mutableListOf<String>()  // Liste des bourgeons créés
    private var flowersInZoneDefi3 = mutableListOf<String>()  // NOUVEAU: Liste des fleurs zone verte défi 3
    private var budsCreatedDefi3 = mutableListOf<String>()    // NOUVEAU: Liste des bourgeons défi 3
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun getMargueriteChallenges(): List<Challenge> = margueriteChallenges
    
    fun startChallenge(challengeId: Int) {
        currentChallenge = margueriteChallenges.find { it.id == challengeId }
        challengeStartTime = System.currentTimeMillis()
        challengeData.clear()
        flowersInZone.clear()  // Reset liste des fleurs
        budsCreated.clear()    // Reset liste des bourgeons
        flowersInZoneDefi3.clear()  // NOUVEAU: Reset défi 3
        budsCreatedDefi3.clear()    // NOUVEAU: Reset défi 3
        println("Défi démarré: ${currentChallenge?.title}")
    }
    
    fun getCurrentChallenge(): Challenge? = currentChallenge
    
    fun getCurrentChallengeBrief(): String? = currentChallenge?.briefText
    
    fun updateChallengeProgress(force: Float, plantState: String) {
        val challenge = currentChallenge ?: return
        
        when (challenge.id) {
            1 -> updateChallenge1_FlowersInZone(force, plantState)
            2 -> updateChallenge2_Buds(force, plantState)
            3 -> updateChallenge3_FlowersAndBuds(force, plantState)  // NOUVEAU
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
        } else if (challenge.id == 3) {
            // NOUVEAU: Défi 3 - Zone verte de 240px total (120px haut + 120px bas)
            val screenHeight = challengeData["screenHeight"] as? Float ?: 2000f
            val zoneTop = screenHeight / 3f - 120f
            val zoneBottom = screenHeight / 3f + 120f  // 240px total
            
            if (flowerY >= zoneTop && flowerY <= zoneBottom) {
                if (!flowersInZoneDefi3.contains(flowerId)) {
                    flowersInZoneDefi3.add(flowerId)
                    challengeData["flowersInZoneDefi3Count"] = flowersInZoneDefi3.size
                    println("Défi 3 - Fleur dans la zone! Total: ${flowersInZoneDefi3.size}/2")
                }
            } else {
                println("Défi 3 - Fleur HORS zone: Y=${flowerY}, Zone=${zoneTop}-${zoneBottom}")
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
        } else if (challenge.id == 3) {
            // NOUVEAU: Défi 3 - Comptage des bourgeons
            if (!budsCreatedDefi3.contains(budId)) {
                budsCreatedDefi3.add(budId)
                challengeData["budsCreatedDefi3Count"] = budsCreatedDefi3.size
                println("Défi 3 - Bourgeon créé! Total: ${budsCreatedDefi3.size}/1 (ID: $budId)")
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
            3 -> checkChallenge3_FlowersAndBuds()  // NOUVEAU
            else -> false
        }
        
        if (isSuccessful) {
            challenge.isCompleted = true
            unlockNextChallenge(challenge.id)
            
            val successMessage = when (challenge.id) {
                1 -> "Défi réussi! ${flowersInZone.size} fleur dans la zone!"
                2 -> "Défi réussi! ${budsCreated.size} bourgeons créés!"
                3 -> "Défi réussi! ${flowersInZoneDefi3.size} fleurs + ${budsCreatedDefi3.size} bourgeon!"  // NOUVEAU
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
                3 -> "Défi échoué - ${flowersInZoneDefi3.size}/2 fleurs, ${budsCreatedDefi3.size}/1 bourgeon!"  // NOUVEAU
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
    
    // ==================== LOGIQUE DU DÉFI 3: FLEURS EN ZONE + BOURGEONS ====================
    
    private fun updateChallenge3_FlowersAndBuds(force: Float, plantState: String) {
        // Le suivi se fait via notifyFlowerCreated() et notifyBudCreated()
        challengeData["currentPhase"] = plantState
        challengeData["totalFlowersDefi3"] = flowersInZoneDefi3.size
        challengeData["totalBudsDefi3"] = budsCreatedDefi3.size
        
        // Optionnel: Suivre la qualité du souffle
        if (force > 0) {
            val currentAvgForce = challengeData["avgForceDefi3"] as? Float ?: 0f
            val forceCount = challengeData["forceCountDefi3"] as? Int ?: 0
            val newAvgForce = (currentAvgForce * forceCount + force) / (forceCount + 1)
            challengeData["avgForceDefi3"] = newAvgForce
            challengeData["forceCountDefi3"] = forceCount + 1
        }
    }
    
    private fun checkChallenge3_FlowersAndBuds(): Boolean {
        // Succès si au moins 2 fleurs dans la zone verte ET au moins 1 bourgeon
        return flowersInZoneDefi3.size >= 2 && budsCreatedDefi3.size >= 1
    }
    
    // ==================== LOGIQUE TEMPORAIRE SUPPRIMÉE ====================
    
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
