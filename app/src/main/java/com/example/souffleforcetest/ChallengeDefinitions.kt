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
            title = "Défi 1: Jardin Ordonné",
            description = "Faire pousser 6 fleurs dans la zone verte",
            briefText = "Défi 1: 6 fleurs en zone verte"
        ),
        Challenge(
            id = 2,
            title = "Défi 2: Maître Ramification", 
            description = "Créer 10 divisions avec saccades précises",
            briefText = "Défi 2: 10 divisions",
            isUnlocked = false  // Débloqué après défi 1
        ),
        Challenge(
            id = 3,
            title = "Défi 3: Expertise Totale",
            description = "15 fleurs dont 5 en zone verte + 8 divisions", 
            briefText = "Défi 3: 15 fleurs (5 en zone) + 8 divisions",
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

    val irisChallenges = listOf(
        Challenge(
            id = 1,
            title = "Défi 1: Élégance Bleue",
            description = "Faire pousser 4 iris dans la zone centrale",
            briefText = "Défi 1: 4 iris en zone centrale"
        ),
        Challenge(
            id = 2,
            title = "Défi 2: Jardin d'Iris", 
            description = "Créer 8 tiges d'iris avec ramifications",
            briefText = "Défi 2: 8 tiges ramifiées",
            isUnlocked = false  // Débloqué après défi 1
        ),
        Challenge(
            id = 3,
            title = "Défi 3: Maître Iris",
            description = "12 iris parfaits + 6 ramifications précises", 
            briefText = "Défi 3: 12 iris + 6 ramifications",
            isUnlocked = false  // Débloqué après défi 2
        )
    )

    val orchideeChallenges = listOf(
        Challenge(
            id = 1,
            title = "Défi 1: Orchidée Royale",
            description = "Faire éclore 2 orchidées parfaites",
            briefText = "Défi 1: 2 orchidées parfaites"
        ),
        Challenge(
            id = 2,
            title = "Défi 2: Collection Exotique", 
            description = "Cultiver 5 orchidées avec contrôle précis",
            briefText = "Défi 2: 5 orchidées précises",
            isUnlocked = false  // Débloqué après défi 1
        ),
        Challenge(
            id = 3,
            title = "Défi 3: Jardinier Expert",
            description = "8 orchidées + ramifications + zone parfaite", 
            briefText = "Défi 3: Maîtrise totale",
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
            1 -> roseFlowersInZone.size >= 6
            2 -> roseDivisions.size >= 10
            3 -> roseTotalFlowers.size >= 15 && roseFlowersInZoneDefi3.size >= 5 && roseDivisions.size >= 8
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
            1 -> "Défi réussi! ${roseFlowersInZone.size} fleurs parfaitement alignées en zone verte!"
            2 -> "Défi réussi! ${roseDivisions.size} divisions créées avec maîtrise!"
            3 -> "Défi réussi! ${roseTotalFlowers.size} fleurs (${roseFlowersInZoneDefi3.size} en zone) + ${roseDivisions.size} divisions!\n🌼 LUPIN DÉBLOQUÉ!"
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
            1 -> "Défi échoué - Seulement ${roseFlowersInZone.size}/6 fleurs en zone verte!"
            2 -> "Défi échoué - Seulement ${roseDivisions.size}/10 divisions créées!"
            3 -> "Défi échoué - ${roseTotalFlowers.size}/15 fleurs (${roseFlowersInZoneDefi3.size}/5 en zone) + ${roseDivisions.size}/8 divisions!"
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

    // ==================== LOGIQUE DES DÉFIS IRIS ====================
    
    fun updateIrisChallenge(
        challengeId: Int, 
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        when (challengeId) {
            1 -> updateIrisChallenge1_CentralZone(force, plantState, challengeData)
            2 -> updateIrisChallenge2_Ramifications(force, plantState, challengeData)
            3 -> updateIrisChallenge3_Mastery(force, plantState, challengeData)
        }
    }
    
    private fun updateIrisChallenge1_CentralZone(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
    }
    
    private fun updateIrisChallenge2_Ramifications(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
    }
    
    private fun updateIrisChallenge3_Mastery(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        challengeData["currentPhase"] = plantState
    }
    
    fun checkIrisChallenge(
        challengeId: Int,
        irisFlowersInZone: List<String>,
        irisRamifications: List<String>,
        irisTotalFlowers: List<String>
    ): Boolean {
        return when (challengeId) {
            1 -> irisFlowersInZone.size >= 4
            2 -> irisRamifications.size >= 8
            3 -> irisTotalFlowers.size >= 12 && irisRamifications.size >= 6
            else -> false
        }
    }
    
    fun getIrisSuccessMessage(
        challengeId: Int,
        irisFlowersInZone: List<String>,
        irisRamifications: List<String>,
        irisTotalFlowers: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Défi réussi! ${irisFlowersInZone.size} iris élégants en zone centrale!"
            2 -> "Défi réussi! ${irisRamifications.size} ramifications parfaites!"
            3 -> "Défi réussi! ${irisTotalFlowers.size} iris + ${irisRamifications.size} ramifications!\n🌺 ORCHIDÉE DÉBLOQUÉE!"
            else -> "Défi réussi!"
        }
    }
    
    fun getIrisFailMessage(
        challengeId: Int,
        irisFlowersInZone: List<String>,
        irisRamifications: List<String>,
        irisTotalFlowers: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Défi échoué - Seulement ${irisFlowersInZone.size}/4 iris en zone centrale!"
            2 -> "Défi échoué - Seulement ${irisRamifications.size}/8 ramifications créées!"
            3 -> "Défi échoué - ${irisTotalFlowers.size}/12 iris + ${irisRamifications.size}/6 ramifications!"
            else -> "Défi échoué!"
        }
    }

    // ==================== LOGIQUE DES DÉFIS ORCHIDÉE ====================
    
    fun updateOrchideeChallenge(
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
    
    fun checkOrchideeChallenge(
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
    
    fun getOrchideeSuccessMessage(
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
    
    fun getOrchideeFailMessage(
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
    
    fun isInCentralZone(flowerY: Float, screenHeight: Float): Boolean {
        val zoneHeight = 192f  // 2 pouces
        val zoneTop = (screenHeight - zoneHeight) / 2f
        val zoneBottom = zoneTop + zoneHeight
        return flowerY >= zoneTop && flowerY <= zoneBottom
    }

    fun isInRoseZone(flowerY: Float, screenHeight: Float): Boolean {
        return isInCentralZone(flowerY, screenHeight)
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
            "IRIS" -> {
                when (completedId) {
                    1 -> "IRIS" to 2
                    2 -> "IRIS" to 3
                    else -> null
                }
            }
            "ORCHIDEE" -> {
                when (completedId) {
                    1 -> "ORCHIDEE" to 2
                    2 -> "ORCHIDEE" to 3
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
            currentFlowerType == "IRIS" && completedChallengeId == 3 -> "ORCHIDEE"
            else -> null
        }
    }
    
    // ==================== FONCTIONS UTILITAIRES ====================
    
    fun getChallengesByFlowerType(flowerType: String): List<Challenge> {
        return when (flowerType) {
            "MARGUERITE" -> margueriteChallenges
            "ROSE" -> roseChallenges
            "LUPIN" -> lupinChallenges
            "IRIS" -> irisChallenges
            "ORCHIDEE" -> orchideeChallenges
            else -> margueriteChallenges
        }
    }
    
    fun findChallengeById(flowerType: String, challengeId: Int): Challenge? {
        return getChallengesByFlowerType(flowerType).find { it.id == challengeId }
    }

    // ==================== NOUVELLES FONCTIONS POUR IRIS ET ORCHIDÉE ====================
    
    fun getIrisChallenges(): List<Challenge> {
        return irisChallenges
    }
    
    fun getOrchideeChallenges(): List<Challenge> {
        return orchideeChallenges
    }
}
