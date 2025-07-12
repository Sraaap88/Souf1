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
    
    // ==================== DÉLÉGATION AUX HANDLERS ====================
    
    private val margueriteHandler = MargueriteChallengeHandler()
    private val roseHandler = RoseChallengeHandler()
    private val lupinHandler = LupinChallengeHandler()
    private val irisHandler = IrisChallengeHandler()
    private val orchideeHandler = OrchideeChallengeHandler()
    private val zoneHelper = ChallengeZoneHelper()
    
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
            isUnlocked = false
        ),
        Challenge(
            id = 3,
            title = "Défi 3: Précision",
            description = "Faire 2 fleurs en zone verte ET 1 bourgeon", 
            briefText = "Défi 3: 2 fleurs + 1 bourgeon",
            isUnlocked = false
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
            isUnlocked = false
        ),
        Challenge(
            id = 3,
            title = "Défi 3: Expertise Totale",
            description = "15 fleurs dont 5 en zone verte + 8 divisions", 
            briefText = "Défi 3: 15 fleurs (5 en zone) + 8 divisions",
            isUnlocked = false
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
            isUnlocked = false
        ),
        Challenge(
            id = 3,
            title = "Défi 3: Maître Lupins",
            description = "12 fleurs individuelles dans les épis", 
            briefText = "Défi 3: 12 fleurs totales",
            isUnlocked = false
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
            isUnlocked = false
        ),
        Challenge(
            id = 3,
            title = "Défi 3: Maître Iris",
            description = "12 iris parfaits + 6 ramifications précises", 
            briefText = "Défi 3: 12 iris + 6 ramifications",
            isUnlocked = false
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
            isUnlocked = false
        ),
        Challenge(
            id = 3,
            title = "Défi 3: Jardinier Expert",
            description = "8 orchidées + ramifications + zone parfaite", 
            briefText = "Défi 3: Maîtrise totale",
            isUnlocked = false
        )
    )
    
    // ==================== DÉLÉGATION DES FONCTIONS ====================
    
    fun updateMargueriteChallenge(challengeId: Int, force: Float, plantState: String, challengeData: MutableMap<String, Any>) {
        margueriteHandler.updateChallenge(challengeId, force, plantState, challengeData)
    }
    
    fun checkMargueriteChallenge(challengeId: Int, flowersInZone: List<String>, budsCreated: List<String>, flowersInZoneDefi3: List<String>, budsCreatedDefi3: List<String>): Boolean {
        return margueriteHandler.checkChallenge(challengeId, flowersInZone, budsCreated, flowersInZoneDefi3, budsCreatedDefi3)
    }
    
    fun getMargueriteSuccessMessage(challengeId: Int, flowersInZone: List<String>, budsCreated: List<String>, flowersInZoneDefi3: List<String>, budsCreatedDefi3: List<String>): String {
        return margueriteHandler.getSuccessMessage(challengeId, flowersInZone, budsCreated, flowersInZoneDefi3, budsCreatedDefi3)
    }
    
    fun getMargueriteFailMessage(challengeId: Int, flowersInZone: List<String>, budsCreated: List<String>, flowersInZoneDefi3: List<String>, budsCreatedDefi3: List<String>): String {
        return margueriteHandler.getFailMessage(challengeId, flowersInZone, budsCreated, flowersInZoneDefi3, budsCreatedDefi3)
    }
    
    fun updateRoseChallenge(challengeId: Int, force: Float, plantState: String, challengeData: MutableMap<String, Any>) {
        roseHandler.updateChallenge(challengeId, force, plantState, challengeData)
    }
    
    fun checkRoseChallenge(challengeId: Int, roseFlowersInZone: List<String>, roseDivisions: List<String>, roseTotalFlowers: List<String>, roseFlowersInZoneDefi3: List<String>): Boolean {
        return roseHandler.checkChallenge(challengeId, roseFlowersInZone, roseDivisions, roseTotalFlowers, roseFlowersInZoneDefi3)
    }
    
    fun getRoseSuccessMessage(challengeId: Int, roseFlowersInZone: List<String>, roseDivisions: List<String>, roseTotalFlowers: List<String>, roseFlowersInZoneDefi3: List<String>): String {
        return roseHandler.getSuccessMessage(challengeId, roseFlowersInZone, roseDivisions, roseTotalFlowers, roseFlowersInZoneDefi3)
    }
    
    fun getRoseFailMessage(challengeId: Int, roseFlowersInZone: List<String>, roseDivisions: List<String>, roseTotalFlowers: List<String>, roseFlowersInZoneDefi3: List<String>): String {
        return roseHandler.getFailMessage(challengeId, roseFlowersInZone, roseDivisions, roseTotalFlowers, roseFlowersInZoneDefi3)
    }
    
    // ==================== LOGIQUE LUPIN MODIFIÉE ====================
    
    fun updateLupinChallenge(challengeId: Int, force: Float, plantState: String, challengeData: MutableMap<String, Any>) {
        lupinHandler.updateChallenge(challengeId, force, plantState, challengeData)
    }
    
    fun checkLupinChallenge(challengeId: Int, lupinSpikeColors: Set<String>, lupinCompleteStems: List<String>, lupinFlowers: List<String>): Boolean {
        return lupinHandler.checkChallenge(challengeId, lupinSpikeColors, lupinCompleteStems, lupinFlowers)
    }
    
    fun getLupinSuccessMessage(challengeId: Int, lupinSpikeColors: Set<String>, lupinCompleteStems: List<String>, lupinFlowers: List<String>): String {
        return lupinHandler.getSuccessMessage(challengeId, lupinSpikeColors, lupinCompleteStems, lupinFlowers)
    }
    
    fun getLupinFailMessage(challengeId: Int, lupinSpikeColors: Set<String>, lupinCompleteStems: List<String>, lupinFlowers: List<String>): String {
        return lupinHandler.getFailMessage(challengeId, lupinSpikeColors, lupinCompleteStems, lupinFlowers)
    }
    
    fun updateIrisChallenge(challengeId: Int, force: Float, plantState: String, challengeData: MutableMap<String, Any>) {
        irisHandler.updateChallenge(challengeId, force, plantState, challengeData)
    }
    
    fun checkIrisChallenge(challengeId: Int, irisFlowersInZone: List<String>, irisRamifications: List<String>, irisTotalFlowers: List<String>): Boolean {
        return irisHandler.checkChallenge(challengeId, irisFlowersInZone, irisRamifications, irisTotalFlowers)
    }
    
    fun getIrisSuccessMessage(challengeId: Int, irisFlowersInZone: List<String>, irisRamifications: List<String>, irisTotalFlowers: List<String>): String {
        return irisHandler.getSuccessMessage(challengeId, irisFlowersInZone, irisRamifications, irisTotalFlowers)
    }
    
    fun getIrisFailMessage(challengeId: Int, irisFlowersInZone: List<String>, irisRamifications: List<String>, irisTotalFlowers: List<String>): String {
        return irisHandler.getFailMessage(challengeId, irisFlowersInZone, irisRamifications, irisTotalFlowers)
    }
    
    fun updateOrchideeChallenge(challengeId: Int, force: Float, plantState: String, challengeData: MutableMap<String, Any>) {
        orchideeHandler.updateChallenge(challengeId, force, plantState, challengeData)
    }
    
    fun checkOrchideeChallenge(challengeId: Int, orchideeFlowers: List<String>, orchideeRamifications: List<String>, orchideeInZone: List<String>): Boolean {
        return orchideeHandler.checkChallenge(challengeId, orchideeFlowers, orchideeRamifications, orchideeInZone)
    }
    
    fun getOrchideeSuccessMessage(challengeId: Int, orchideeFlowers: List<String>, orchideeRamifications: List<String>, orchideeInZone: List<String>): String {
        return orchideeHandler.getSuccessMessage(challengeId, orchideeFlowers, orchideeRamifications, orchideeInZone)
    }
    
    fun getOrchideeFailMessage(challengeId: Int, orchideeFlowers: List<String>, orchideeRamifications: List<String>, orchideeInZone: List<String>): String {
        return orchideeHandler.getFailMessage(challengeId, orchideeFlowers, orchideeRamifications, orchideeInZone)
    }
    
    // ==================== GESTION DES ZONES MODIFIÉE ====================
    
    fun isInMargueriteZone(flowerY: Float, screenHeight: Float, challengeId: Int): Boolean {
        return zoneHelper.isInMargueriteZone(flowerY, screenHeight, challengeId)
    }
    
    fun isInCentralZone(flowerY: Float, screenHeight: Float): Boolean {
        return zoneHelper.isInCentralZone(flowerY, screenHeight)
    }

    fun isInRoseZone(flowerY: Float, screenHeight: Float): Boolean {
        return zoneHelper.isInRoseZone(flowerY, screenHeight)
    }
    
    fun isInLupinZone(flowerY: Float, screenHeight: Float, challengeId: Int): Boolean {
        return zoneHelper.isInLupinZone(flowerY, screenHeight, challengeId)
    }
    
    // ==================== DÉBLOCAGE DES DÉFIS ====================
    
    fun getNextUnlockedChallenge(flowerType: String, completedId: Int): Pair<String, Int>? {
        return when (flowerType) {
            "MARGUERITE" -> when (completedId) {
                1 -> "MARGUERITE" to 2
                2 -> "MARGUERITE" to 3
                else -> null
            }
            "ROSE" -> when (completedId) {
                1 -> "ROSE" to 2
                2 -> "ROSE" to 3
                else -> null
            }
            "LUPIN" -> when (completedId) {
                1 -> "LUPIN" to 2
                2 -> "LUPIN" to 3
                else -> null
            }
            "IRIS" -> when (completedId) {
                1 -> "IRIS" to 2
                2 -> "IRIS" to 3
                else -> null
            }
            "ORCHIDEE" -> when (completedId) {
                1 -> "ORCHIDEE" to 2
                2 -> "ORCHIDEE" to 3
                else -> null
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

    fun getAllIrisChallenges(): List<Challenge> = irisChallenges
    fun getAllOrchideeChallenges(): List<Challenge> = orchideeChallenges
}
