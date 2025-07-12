package com.example.souffleforcetest

class ChallengeDefinitions {
    
    // ==================== DATA CLASSES ====================
    
    data class Challenge(
        val id: Int,
        val title: String,
        val description: String,
        val briefText: String,
        var isCompleted: Boolean = false,
        var isUnlocked: Boolean = false
    )
    
    data class ChallengeResult(
        val challenge: Challenge,
        val success: Boolean,
        val message: String
    )
    
    // ==================== LISTES DES DÉFIS ====================
    
    val margueriteChallenges = listOf(
        Challenge(
            id = 1,
            title = "Jardin Ordonné",
            description = "Faire pousser 1 fleur dans la zone supérieure de l'écran pour créer un jardin bien organisé.",
            briefText = "Concentrez votre souffle vers le haut pour faire grandir une belle marguerite dans la zone cible.",
            isUnlocked = true
        ),
        Challenge(
            id = 2,
            title = "Maître Ramification",
            description = "Créer 2 bourgeons sur votre marguerite en utilisant un souffle très doux et contrôlé.",
            briefText = "Utilisez un souffle léger et régulier pour encourager la formation de bourgeons floraux.",
            isUnlocked = false
        ),
        Challenge(
            id = 3,
            title = "Expertise Totale",
            description = "Combiner vos compétences : 2 fleurs dans la zone haute ET 1 bourgeon supplémentaire.",
            briefText = "Démontrez votre maîtrise complète en combinant placement précis et technique avancée.",
            isUnlocked = false
        )
    )
    
    val roseChallenges = listOf(
        Challenge(
            id = 1,
            title = "Jardin Ordonné",
            description = "Faire pousser 6 roses dans la zone supérieure pour créer un massif élégant.",
            briefText = "Concentrez votre énergie pour faire fleurir un bouquet de roses dans la partie haute.",
            isUnlocked = true
        ),
        Challenge(
            id = 2,
            title = "Maître Ramification",
            description = "Créer 10 divisions sur votre rosier pour multiplier les branches florales.",
            briefText = "Utilisez des saccades contrôlées pour stimuler la ramification du rosier.",
            isUnlocked = false
        ),
        Challenge(
            id = 3,
            title = "Expertise Totale",
            description = "Réussir un exploit complet : 15 roses au total, 5 dans la zone haute, et 8 divisions.",
            briefText = "Démontrez une maîtrise parfaite en combinant quantité, placement et technique.",
            isUnlocked = false
        )
    )
    
    val lupinChallenges = listOf(
        Challenge(
            id = 1,
            title = "Arc-en-Ciel",
            description = "Créer 3 épis de lupin de couleurs différentes pour former un arc-en-ciel floral.",
            briefText = "Variez votre technique pour obtenir des épis de couleurs diverses et éclatantes.",
            isUnlocked = true
        ),
        Challenge(
            id = 2,
            title = "Jardin Touffu",
            description = "Faire pousser 5 tiges complètes de lupin pour créer un massif dense et fourni.",
            briefText = "Utilisez des saccades répétées pour multiplier les tiges et créer un lupin majestueux.",
            isUnlocked = false
        ),
        Challenge(
            id = 3,
            title = "Maître Jardinier",
            description = "Réaliser l'exploit ultime : 25 fleurs de lupin réparties sur plusieurs tiges colorées.",
            briefText = "Démontrez votre expertise en créant un lupin magnifique et abondamment fleuri.",
            isUnlocked = false
        )
    )
    
    // NOUVEAU: Défis de l'Iris
    val irisChallenges = listOf(
        Challenge(
            id = 1,
            title = "Élégance Royale",
            description = "Faire pousser 3 iris de couleurs différentes pour créer un jardin royal et raffiné.",
            briefText = "Cultivez avec grâce trois iris aux teintes distinctes, symboles de noblesse et d'élégance.",
            isUnlocked = true
        ),
        Challenge(
            id = 2,
            title = "Symphonie Florale",
            description = "Créer 4 tiges d'iris parfaitement développées avec leurs feuilles caractéristiques.",
            briefText = "Orchestrez une croissance harmonieuse pour obtenir des iris complets et majestueux.",
            isUnlocked = false
        ),
        Challenge(
            id = 3,
            title = "Jardin Impérial",
            description = "Accomplir l'excellence : 8 iris total, 4 tiges complètes, et 3 couleurs différentes.",
            briefText = "Réalisez un chef-d'œuvre digne d'un jardin impérial avec une diversité et une abondance parfaites.",
            isUnlocked = false
        )
    )
    
    // ==================== FONCTIONS UTILITAIRES ====================
    
    fun findChallengeById(flowerType: String, challengeId: Int): Challenge? {
        val challenges = when (flowerType) {
            "MARGUERITE" -> margueriteChallenges
            "ROSE" -> roseChallenges
            "LUPIN" -> lupinChallenges
            "IRIS" -> irisChallenges
            else -> return null
        }
        return challenges.find { it.id == challengeId }
    }
    
    fun getNextUnlockedChallenge(flowerType: String, completedId: Int): Pair<String, Int>? {
        return when (flowerType) {
            "MARGUERITE" -> {
                when (completedId) {
                    1 -> Pair("MARGUERITE", 2)
                    2 -> Pair("MARGUERITE", 3)
                    else -> null
                }
            }
            "ROSE" -> {
                when (completedId) {
                    1 -> Pair("ROSE", 2)
                    2 -> Pair("ROSE", 3)
                    else -> null
                }
            }
            "LUPIN" -> {
                when (completedId) {
                    1 -> Pair("LUPIN", 2)
                    2 -> Pair("LUPIN", 3)
                    else -> null
                }
            }
            "IRIS" -> {
                when (completedId) {
                    1 -> Pair("IRIS", 2)
                    2 -> Pair("IRIS", 3)
                    else -> null
                }
            }
            else -> null
        }
    }
    
    fun getUnlockedFlowerType(flowerType: String, completedId: Int): String? {
        return when (flowerType) {
            "MARGUERITE" -> if (completedId == 3) "ROSE" else null
            "ROSE" -> if (completedId == 3) "LUPIN" else null
            "LUPIN" -> if (completedId == 3) "IRIS" else null
            "IRIS" -> null // Dernière fleur pour l'instant
            else -> null
        }
    }
    
    // ==================== ZONES DE VALIDATION ====================
    
    fun isInMargueriteZone(flowerY: Float, screenHeight: Float, challengeId: Int): Boolean {
        return when (challengeId) {
            1, 3 -> flowerY <= screenHeight * 0.4f // Zone haute
            else -> false
        }
    }
    
    fun isInRoseZone(flowerY: Float, screenHeight: Float): Boolean {
        return flowerY <= screenHeight * 0.45f // Zone haute pour roses
    }
    
    // NOUVEAU: Zone pour les iris (zone centrale élégante)
    fun isInIrisZone(flowerY: Float, screenHeight: Float): Boolean {
        return flowerY >= screenHeight * 0.25f && flowerY <= screenHeight * 0.65f // Zone centrale
    }
    
    // ==================== MISE À JOUR DES DÉFIS ====================
    
    fun updateMargueriteChallenge(challengeId: Int, force: Float, plantState: String, data: MutableMap<String, Any>) {
        // Logique existante pour marguerite (inchangée)
    }
    
    fun updateRoseChallenge(challengeId: Int, force: Float, plantState: String, data: MutableMap<String, Any>) {
        // Logique existante pour rose (inchangée)
    }
    
    fun updateLupinChallenge(challengeId: Int, force: Float, plantState: String, data: MutableMap<String, Any>) {
        // Logique existante pour lupin (inchangée)
    }
    
    // NOUVEAU: Mise à jour des défis d'iris
    fun updateIrisChallenge(challengeId: Int, force: Float, plantState: String, data: MutableMap<String, Any>) {
        when (challengeId) {
            1 -> {
                // Défi 1: 3 iris de couleurs différentes
                // La logique sera gérée par les notifications dans ChallengeManager
            }
            2 -> {
                // Défi 2: 4 tiges complètes
                // La logique sera gérée par les notifications dans ChallengeManager
            }
            3 -> {
                // Défi 3: 8 iris, 4 tiges, 3 couleurs
                // La logique sera gérée par les notifications dans ChallengeManager
            }
        }
    }
    
    // ==================== VÉRIFICATION DES DÉFIS ====================
    
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
    
    fun checkRoseChallenge(
        challengeId: Int,
        flowersInZone: List<String>,
        divisions: List<String>,
        totalFlowers: List<String>,
        flowersInZoneDefi3: List<String>
    ): Boolean {
        return when (challengeId) {
            1 -> flowersInZone.size >= 6
            2 -> divisions.size >= 10
            3 -> totalFlowers.size >= 15 && flowersInZoneDefi3.size >= 5 && divisions.size >= 8
            else -> false
        }
    }
    
    fun checkLupinChallenge(
        challengeId: Int,
        spikeColors: Set<String>,
        completeStems: List<String>,
        totalFlowers: List<String>
    ): Boolean {
        return when (challengeId) {
            1 -> spikeColors.size >= 3
            2 -> completeStems.size >= 5
            3 -> totalFlowers.size >= 25
            else -> false
        }
    }
    
    // NOUVEAU: Vérification des défis d'iris
    fun checkIrisChallenge(
        challengeId: Int,
        irisColors: Set<String>,
        completeStems: List<String>,
        totalFlowers: List<String>
    ): Boolean {
        return when (challengeId) {
            1 -> irisColors.size >= 3 // 3 couleurs différentes
            2 -> completeStems.size >= 4 // 4 tiges complètes
            3 -> totalFlowers.size >= 8 && completeStems.size >= 4 && irisColors.size >= 3 // Défi complet
            else -> false
        }
    }
    
    // ==================== MESSAGES DE SUCCÈS ====================
    
    fun getMargueriteSuccessMessage(
        challengeId: Int,
        flowersInZone: List<String>,
        budsCreated: List<String>,
        flowersInZoneDefi3: List<String>,
        budsCreatedDefi3: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Parfait! ${flowersInZone.size} fleur dans la zone cible. Votre jardin est bien ordonné!"
            2 -> "Excellent! ${budsCreated.size} bourgeons créés. Vous maîtrisez la ramification!"
            3 -> "Magistral! ${flowersInZoneDefi3.size} fleurs en zone + ${budsCreatedDefi3.size} bourgeon. Expertise totale!"
            else -> "Défi réussi!"
        }
    }
    
    fun getRoseSuccessMessage(
        challengeId: Int,
        flowersInZone: List<String>,
        divisions: List<String>,
        totalFlowers: List<String>,
        flowersInZoneDefi3: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Superbe! ${flowersInZone.size} roses dans la zone haute. Votre massif est magnifique!"
            2 -> "Excellent! ${divisions.size} divisions créées. Votre rosier se ramifie parfaitement!"
            3 -> "Parfait! ${totalFlowers.size} roses total, ${flowersInZoneDefi3.size} en zone haute, ${divisions.size} divisions!"
            else -> "Défi réussi!"
        }
    }
    
    fun getLupinSuccessMessage(
        challengeId: Int,
        spikeColors: Set<String>,
        completeStems: List<String>,
        totalFlowers: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Magnifique! ${spikeColors.size} couleurs d'épis. Votre arc-en-ciel floral est réussi!"
            2 -> "Parfait! ${completeStems.size} tiges complètes. Votre jardin touffu est splendide!"
            3 -> "Extraordinaire! ${totalFlowers.size} fleurs de lupin. Vous êtes un maître jardinier!"
            else -> "Défi réussi!"
        }
    }
    
    // NOUVEAU: Messages de succès pour l'iris
    fun getIrisSuccessMessage(
        challengeId: Int,
        irisColors: Set<String>,
        completeStems: List<String>,
        totalFlowers: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Élégant! ${irisColors.size} couleurs d'iris. Votre jardin royal rayonne de noblesse!"
            2 -> "Harmonieux! ${completeStems.size} tiges parfaites. Votre symphonie florale est magistrale!"
            3 -> "Impérial! ${totalFlowers.size} iris, ${completeStems.size} tiges, ${irisColors.size} couleurs. Chef-d'œuvre accompli!"
            else -> "Défi réussi!"
        }
    }
    
    // ==================== MESSAGES D'ÉCHEC ====================
    
    fun getMargueriteFailMessage(
        challengeId: Int,
        flowersInZone: List<String>,
        budsCreated: List<String>,
        flowersInZoneDefi3: List<String>,
        budsCreatedDefi3: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Dommage! ${flowersInZone.size}/1 fleur en zone. Concentrez votre souffle vers le haut."
            2 -> "Presque! ${budsCreated.size}/2 bourgeons. Utilisez un souffle plus doux et régulier."
            3 -> "Effort louable! ${flowersInZoneDefi3.size}/2 fleurs + ${budsCreatedDefi3.size}/1 bourgeon. Continuez!"
            else -> "Défi échoué!"
        }
    }
    
    fun getRoseFailMessage(
        challengeId: Int,
        flowersInZone: List<String>,
        divisions: List<String>,
        totalFlowers: List<String>,
        flowersInZoneDefi3: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Pas mal! ${flowersInZone.size}/6 roses en zone. Dirigez plus d'énergie vers le haut."
            2 -> "Bon début! ${divisions.size}/10 divisions. Utilisez plus de saccades contrôlées."
            3 -> "Progrès! ${totalFlowers.size}/15 roses, ${flowersInZoneDefi3.size}/5 en zone, ${divisions.size}/8 divisions."
            else -> "Défi échoué!"
        }
    }
    
    fun getLupinFailMessage(
        challengeId: Int,
        spikeColors: Set<String>,
        completeStems: List<String>,
        totalFlowers: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Bien tenté! ${spikeColors.size}/3 couleurs. Variez vos techniques pour plus de diversité."
            2 -> "Bon effort! ${completeStems.size}/5 tiges. Utilisez plus de saccades pour multiplier les tiges."
            3 -> "Progrès notable! ${totalFlowers.size}/25 fleurs. Persistez pour un lupin plus fourni."
            else -> "Défi échoué!"
        }
    }
    
    // NOUVEAU: Messages d'échec pour l'iris
    fun getIrisFailMessage(
        challengeId: Int,
        irisColors: Set<String>,
        completeStems: List<String>,
        totalFlowers: List<String>
    ): String {
        return when (challengeId) {
            1 -> "Noble effort! ${irisColors.size}/3 couleurs. Variez vos techniques pour plus d'élégance."
            2 -> "Bon développement! ${completeStems.size}/4 tiges. Continuez pour une symphonie plus complète."
            3 -> "Effort royal! ${totalFlowers.size}/8 iris, ${completeStems.size}/4 tiges, ${irisColors.size}/3 couleurs. Persévérez!"
            else -> "Défi échoué!"
        }
    }
}
