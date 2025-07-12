package com.example.souffleforcetest

class ChallengeDefinitions {
    
    // ==================== DATA CLASSES ====================
    
    data class Challenge(
        val id: Int,
        val title: String,
        val description: String,
        val briefText: String,  // Texte affiché pendant le jeu
        var isCompleted: Boolean = false,
        var isUnlocked: Boolean = true  // Pour l'instant tous débloqués
    )
    
    data class ChallengeResult(
        val challenge: Challenge,
        val success: Boolean,
        val message: String
    )
    
    // ==================== DÉFINITIONS DES DÉFIS ====================
    
    val margueriteChallenges = listOf(
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
    
    val roseChallenges = listOf(
        Challenge(
            id = 1,
            title = "Défi 1: Zone Verte",
            description = "Faire pousser 4 fleurs dans la zone verte",
            briefText = "Défi 1: 4 fleurs en zone verte"
        ),
        Challenge(
            id = 2,
            title = "Défi 2: Ramification", 
            description = "Créer 6 divisions avec saccades",
            briefText = "Défi 2: 6 divisions",
            isUnlocked = false  // Débloqué après défi 1
        ),
        Challenge(
            id = 3,
            title = "Défi 3: Maîtrise",
            description = "8 fleurs dont 3 en zone verte", 
            briefText = "Défi 3: 8 fleurs (3 en zone)",
            isUnlocked = false  // Débloqué après défi 2
        )
    )
    
    val lupinChallenges = listOf(
        Challenge(
            id = 1,
            title = "Défi 1: Épis Colorés",
            description = "Faire pousser 3 épis de lupin de couleurs différentes",
            briefText = "Défi 1: 3 épis colorés"
        ),
        Challenge(
            id = 2,
            title = "Défi 2: Jardinage", 
            description = "Créer 5 tiges complètes avec leurs épis floraux",
            briefText = "Défi 2: 5 tiges complètes",
            isUnlocked = false  // Débloqué après défi 1
        ),
        Challenge(
            id = 3,
            title = "Défi 3: Maître Lupins",
            description = "15 fleurs individuelles dans les épis", 
            briefText = "Défi 3: 15 fleurs totales",
            isUnlocked = false  // Débloqué après défi 2
        )
    )
    
    // ==================== LOGIQUE DES DÉFIS MARGUERITE ====================
    
    fun updateMargueriteChallenge(
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
    
    fun checkMargueriteChallenge(
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
    
    fun getMargueriteSuccessMessage(
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
    
    fun getMargueriteFailMessage(
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
    
    // ==================== LOGIQUE DES DÉFIS ROSIER ====================
    
    fun updateRoseChallenge(
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
    
    fun checkRoseChallenge(
        challengeId: Int,
        roseFlowersInZone: List<String>,
        roseDivisions: List<String>,
        roseTotalFlowers: List<String>,
        roseFlowersInZoneDefi3: List<String>
    ): Boolean {
        return when (challengeId) {
            1 -> roseFlowersInZone.size >= 4
            2 -> roseDivisions.size >= 6
            3 -> roseTotalFlowers.size >= 8 && roseFlowersInZoneDefi3.size >= 3
            else -> false
        }
    }
    
    fun getRoseSuccessMessage(
        challengeId: Int,
        roseFlowersInZone: List<String>,
        roseDivisions: List<String>,
        roseTotalFlowers: List<String>,
        roseFlowersInZoneDefi3: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Défi réussi! ${roseFlowersInZone.size} fleurs en zone verte!"
            2 -> "Défi réussi! ${roseDivisions.size} divisions créées!"
            3 -> "Défi réussi! ${roseTotalFlowers.size} fleurs (${roseFlowersInZoneDefi3.size} en zone)!\n🌼 LUPIN DÉBLOQUÉ!"
            else -> "Défi réussi!"
        }
    }
    
    fun getRoseFailMessage(
        challengeId: Int,
        roseFlowersInZone: List<String>,
        roseDivisions: List<String>,
        roseTotalFlowers: List<String>,
        roseFlowersInZoneDefi3: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Défi échoué - Seulement ${roseFlowersInZone.size}/4 fleurs en zone!"
            2 -> "Défi échoué - Seulement ${roseDivisions.size}/6 divisions créées!"
            3 -> "Défi échoué - ${roseTotalFlowers.size}/8 fleurs (${roseFlowersInZoneDefi3.size}/3 en zone)!"
            else -> "Défi échoué!"
        }
    }
    
    // ==================== LOGIQUE DES DÉFIS LUPIN ====================
    
    fun updateLupinChallenge(
        challengeId: Int, 
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        when (challengeId) {
            1 -> updateLupinChallenge1_ColoredSpikes(force, plantState, challengeData)
            2 -> updateLupinChallenge2_CompleteStems(force, plantState, challengeData)
            3 -> updateLupinChallenge3_TotalFlowers(force, plantState, challengeData)
        }
    }
    
    private fun updateLupinChallenge1_ColoredSpikes(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
    }
    
    private fun updateLupinChallenge2_CompleteStems(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
    }
    
    private fun updateLupinChallenge3_TotalFlowers(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
    }
    
    fun checkLupinChallenge(
        challengeId: Int,
        lupinSpikeColors: Set<String>,
        lupinCompleteStems: List<String>,
        lupinFlowers: List<String>
    ): Boolean {
        return when (challengeId) {
            1 -> lupinSpikeColors.size >= 3
            2 -> lupinCompleteStems.size >= 5
            3 -> lupinFlowers.size >= 15
            else -> false
        }
    }
    
    fun getLupinSuccessMessage(
        challengeId: Int,
        lupinSpikeColors: Set<String>,
        lupinCompleteStems: List<String>,
        lupinFlowers: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Défi réussi! ${lupinSpikeColors.size} épis de couleurs différentes!"
            2 -> "Défi réussi! ${lupinCompleteStems.size} tiges complètes!"
            3 -> "Défi réussi! ${lupinFlowers.size} fleurs dans les épis!\n🌺 IRIS DÉBLOQUÉ!"
            else -> "Défi réussi!"
        }
    }
    
    fun getLupinFailMessage(
        challengeId: Int,
        lupinSpikeColors: Set<String>,
        lupinCompleteStems: List<String>,
        lupinFlowers: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Défi échoué - Seulement ${lupinSpikeColors.size}/3 couleurs d'épis!"
            2 -> "Défi échoué - Seulement ${lupinCompleteStems.size}/5 tiges complètes!"
            3 -> "Défi échoué - Seulement ${lupinFlowers.size}/15 fleurs dans les épis!"
            else -> "Défi échoué!"
        }
    }
    
    // ==================== GESTION DES ZONES ====================
    
    fun isInMargueriteZone(flowerY: Float, screenHeight: Float, challengeId: Int): Boolean {
        return when (challengeId) {
            1 -> {
                val zoneTop = screenHeight / 3f - 60f
                val zoneBottom = screenHeight / 3f + 360f
                flowerY >= zoneTop && flowerY <= zoneBottom
            }
            3 -> {
                val zoneTop = screenHeight / 3f - 120f
                val zoneBottom = screenHeight / 3f + 120f
                flowerY >= zoneTop && flowerY <= zoneBottom
            }
            else -> false
        }
    }
    
    fun isInRoseZone(flowerY: Float, screenHeight: Float): Boolean {
        val zoneHeight = 192f  // 2 pouces
        val zoneTop = (screenHeight - zoneHeight) / 2f
        val zoneBottom = zoneTop + zoneHeight
        return flowerY >= zoneTop && flowerY <= zoneBottom
    }
    
    // ==================== DÉBLOCAGE DES DÉFIS ====================
    
    fun getNextUnlockedChallenge(flowerType: String, completedId: Int): Pair<String, Int>? {
        return when (flowerType) {
            "MARGUERITE" -> {
                when (completedId) {
                    1 -> "MARGUERITE" to 2
                    2 -> "MARGUERITE" to 3
                    else -> null
                }
            }
            "ROSE" -> {
                when (completedId) {
                    1 -> "ROSE" to 2
                    2 -> "ROSE" to 3
                    else -> null
                }
            }
            "LUPIN" -> {
                when (completedId) {
                    1 -> "LUPIN" to 2
                    2 -> "LUPIN" to 3
                    else -> null
                }
            }
            else -> null
        }
    }
    
    fun getUnlockedFlowerType(currentFlowerType: String, completedChallengeId: Int): String? {
        return when {
            currentFlowerType == "MARGUERITE" && completedChallengeId == 3 -> "ROSE"
            currentFlowerType == "ROSE" && completedChallengeId == 3 -> "LUPIN"
            currentFlowerType == "LUPIN" && completedChallengeId == 3 -> "IRIS"
            else -> null
        }
    }
    
    // ==================== FONCTIONS UTILITAIRES ====================
    
    fun getChallengesByFlowerType(flowerType: String): List<Challenge> {
        return when (flowerType) {
            "MARGUERITE" -> margueriteChallenges
            "ROSE" -> roseChallenges
            "LUPIN" -> lupinChallenges
            else -> margueriteChallenges
        }
    }
    
    fun findChallengeById(flowerType: String, challengeId: Int): Challenge? {
        return getChallengesByFlowerType(flowerType).find { it.id == challengeId }
    }
}
