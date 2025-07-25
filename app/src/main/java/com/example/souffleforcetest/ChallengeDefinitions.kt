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
    
    // ==================== DÉFINITIONS AVEC DESCRIPTIONS CORRIGÉES ====================
    
    val margueriteChallenges = listOf(
        Challenge(
            id = 1,
            title = "Défi 1: Zone Précise",
            description = "Faire pousser 1 fleur dans la zone verte",
            briefText = "Défi 1: 1 fleur en zone verte"
        ),
        Challenge(
            id = 2,
            title = "Défi 2: Souffle Doux", 
            description = "Faire pousser 2 bourgeons avec un souffle très doux",
            briefText = "Défi 2: 2 bourgeons",
            isUnlocked = false
        ),
        Challenge(
            id = 3,
            title = "Défi 3: Zone + Bourgeons",
            description = "Faire 2 fleurs dans la zone verte ET créer 1 bourgeon", 
            briefText = "Défi 3: 2 fleurs zone + 1 bourgeon",
            isUnlocked = false
        )
    )
    
    val roseChallenges = listOf(
        Challenge(
            id = 1,
            title = "Défi 1: Jardin Ordonné",
            description = "Faire pousser 6 fleurs dans la zone centrale",
            briefText = "Défi 1: 6 fleurs en zone centrale"
        ),
        Challenge(
            id = 2,
            title = "Défi 2: Maître Ramification", 
            description = "Créer 10 divisions en utilisant des saccades de souffle",
            briefText = "Défi 2: 10 divisions",
            isUnlocked = false
        ),
        Challenge(
            id = 3,
            title = "Défi 3: Triple Objectif",
            description = "Créer 15 fleurs au total, dont 5 dans la zone centrale, et 8 divisions", 
            briefText = "Défi 3: 15 total + 5 zone + 8 divisions",
            isUnlocked = false
        )
    )
    
    val lupinChallenges = listOf(
        Challenge(
            id = 1,
            title = "Défi 1: Palette Colorée",
            description = "Faire pousser 3 épis de couleurs différentes",
            briefText = "Défi 1: 3 couleurs d'épis"
        ),
        Challenge(
            id = 2,
            title = "Défi 2: Jardinage Expert", 
            description = "Créer 5 tiges complètes avec leurs épis",
            briefText = "Défi 2: 5 tiges complètes",
            isUnlocked = false
        ),
        Challenge(
            id = 3,
            title = "Défi 3: Production Totale",
            description = "Créer 12 fleurs individuelles dans les épis", 
            briefText = "Défi 3: 12 fleurs au total",
            isUnlocked = false
        )
    )

    val irisChallenges = listOf(
        Challenge(
            id = 1,
            title = "Défi 1: Élégance Centrée",
            description = "Faire pousser 4 iris dans la zone centrale",
            briefText = "Défi 1: 4 iris en zone centrale"
        ),
        Challenge(
            id = 2,
            title = "Défi 2: Double Objectif", 
            description = "Créer 10 iris au total dont 6 dans la zone centrale",
            briefText = "Défi 2: 10 total + 6 en zone",
            isUnlocked = false
        ),
        Challenge(
            id = 3,
            title = "Défi 3: Maîtrise Complète",
            description = "Créer 16 iris au total dont 8 dans la zone centrale", 
            briefText = "Défi 3: 16 total + 8 en zone",
            isUnlocked = false
        )
    )

    // ✅ CORRIGÉ: Défis orchidées avec descriptions cohérentes
    val orchideeChallenges = listOf(
        Challenge(
            id = 1,
            title = "Défi 1: Saccades Régulières",
            description = "Réussir 10 saccades, créer 6 espèces différentes et placer 8 orchidées dans la zone élégante",
            briefText = "Défi 1: 10 saccades + 6 espèces + 8 en zone"
        ),
        Challenge(
            id = 2,
            title = "Défi 2: Souffle Délicat", 
            description = "Créer 3 tiges complètes délicates et placer 5 orchidées dans la zone de précision",
            briefText = "Défi 2: 3 tiges délicates + 5 en zone précise",
            isUnlocked = false
        ),
        Challenge(
            id = 3,
            title = "Défi 3: Patience du Maître",
            description = "Cultiver les 6 espèces d'orchidées et créer 20 fleurs au total avec patience", 
            briefText = "Défi 3: 6 espèces + 20 fleurs totales",
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
    
    // ==================== LOGIQUE LUPIN ====================
    
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
    
    // ==================== LOGIQUE IRIS ====================
    
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
    
    // ✅ CORRIGÉ: Logique orchidées avec signature cohérente
    fun updateOrchideeChallenge(challengeId: Int, force: Float, plantState: String, challengeData: MutableMap<String, Any>) {
        orchideeHandler.updateChallenge(challengeId, force, plantState, challengeData)
    }
    
    fun checkOrchideeChallenge(challengeId: Int, orchideeFlowersInZone: List<String>, orchideeSpeciesCount: Int, orchideeTotalFlowers: List<String>): Boolean {
        return orchideeHandler.checkChallenge(challengeId, orchideeFlowersInZone, orchideeSpeciesCount, orchideeTotalFlowers)
    }
    
    fun getOrchideeSuccessMessage(challengeId: Int, orchideeFlowersInZone: List<String>, orchideeSpeciesCount: Int, orchideeTotalFlowers: List<String>): String {
        return orchideeHandler.getSuccessMessage(challengeId, orchideeFlowersInZone, orchideeSpeciesCount, orchideeTotalFlowers)
    }
    
    fun getOrchideeFailMessage(challengeId: Int, orchideeFlowersInZone: List<String>, orchideeSpeciesCount: Int, orchideeTotalFlowers: List<String>): String {
        return orchideeHandler.getFailMessage(challengeId, orchideeFlowersInZone, orchideeSpeciesCount, orchideeTotalFlowers)
    }
    
    // ==================== GESTION DES ZONES CORRIGÉES ====================
    
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
    
    fun isInIrisZone(flowerY: Float, screenHeight: Float): Boolean {
        return zoneHelper.isInIrisZone(flowerY, screenHeight)
    }
    
    // ✅ AJOUTÉ: Zone orchidées
    fun isInOrchideeZone(flowerY: Float, screenHeight: Float, challengeId: Int): Boolean {
        return zoneHelper.isInOrchideeZone(flowerY, screenHeight, challengeId)
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
