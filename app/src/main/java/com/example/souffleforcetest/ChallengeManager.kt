package com.example.souffleforcetest

import android.content.Context
import android.content.SharedPreferences

class ChallengeManager(private val context: Context? = null) {
    
    // ==================== DATA CLASSES ====================
    
    data class Challenge(
        val id: Int,
        val title: String,
        val description: String,
        val briefText: String,  // Texte affiché pendant le jeu
        var isCompleted: Boolean = false,
        var isUnlocked: Boolean = true  // Pour l'instant tous débloqués
    )
    
    data class UnlockedFlower(
        val flowerType: String,  // "MARGUERITE", "ROSE", etc.
        val unlockedBy: String,  // "Défi 3 Marguerite", etc.
        val dateUnlocked: Long = System.currentTimeMillis()
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
    
    // ==================== DÉFIS ROSIER ====================
    
    private val roseChallenges = listOf(
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
    
    // ==================== VARIABLES D'ÉTAT ====================
    
    private var currentChallenge: Challenge? = null
    private var currentFlowerType: String = "MARGUERITE"  // NOUVEAU: Type de fleur actuel
    private var challengeStartTime = 0L
    private var challengeData = mutableMapOf<String, Any>()  // Pour stocker données du défi
    
    // Variables pour marguerite
    private var flowersInZone = mutableListOf<String>()  // Liste des fleurs dans la zone verte
    private var budsCreated = mutableListOf<String>()  // Liste des bourgeons créés
    private var flowersInZoneDefi3 = mutableListOf<String>()  // Liste des fleurs zone verte défi 3
    private var budsCreatedDefi3 = mutableListOf<String>()    // Liste des bourgeons défi 3
    
    // NOUVEAU: Variables pour rosier
    private var roseFlowersInZone = mutableListOf<String>()  // Fleurs rosier en zone verte
    private var roseDivisions = mutableListOf<String>()      // Divisions créées par saccades
    private var roseTotalFlowers = mutableListOf<String>()   // Total fleurs rosier
    private var roseFlowersInZoneDefi3 = mutableListOf<String>()  // Fleurs zone verte défi 3 rosier
    
    // Gestion des fleurs débloquées
    private val unlockedFlowers = mutableListOf<UnlockedFlower>()
    
    // ==================== SAUVEGARDE ====================
    
    private val sharedPrefs: SharedPreferences? by lazy {
        context?.getSharedPreferences("challenges_save", Context.MODE_PRIVATE)
    }
    
    init {
        // Marguerite toujours débloquée par défaut
        unlockedFlowers.add(UnlockedFlower("MARGUERITE", "Disponible par défaut"))
        
        // Charger la sauvegarde au démarrage
        loadChallengeProgress()
    }
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun getMargueriteChallenges(): List<Challenge> = margueriteChallenges
    
    // NOUVEAU: Fonction pour les défis du rosier
    fun getRoseChallenges(): List<Challenge> = roseChallenges
    
    fun startChallenge(challengeId: Int) {
        // Déterminer quel type de défi selon la fleur sélectionnée
        currentChallenge = when (currentFlowerType) {
            "MARGUERITE" -> margueriteChallenges.find { it.id == challengeId }
            "ROSE" -> roseChallenges.find { it.id == challengeId }
            else -> margueriteChallenges.find { it.id == challengeId }
        }
        
        challengeStartTime = System.currentTimeMillis()
        challengeData.clear()
        
        // Reset variables selon le type de fleur
        if (currentFlowerType == "MARGUERITE") {
            flowersInZone.clear()
            budsCreated.clear()
            flowersInZoneDefi3.clear()
            budsCreatedDefi3.clear()
        } else if (currentFlowerType == "ROSE") {
            roseFlowersInZone.clear()
            roseDivisions.clear()
            roseTotalFlowers.clear()
            roseFlowersInZoneDefi3.clear()
        }
        
        println("Défi démarré: ${currentChallenge?.title} (${currentFlowerType})")
    }
    
    // NOUVEAU: Définir le type de fleur pour les défis
    fun setCurrentFlowerType(flowerType: String) {
        currentFlowerType = flowerType
    }
    
    fun getCurrentChallenge(): Challenge? = currentChallenge
    
    fun getCurrentChallengeBrief(): String? = currentChallenge?.briefText
    
    fun updateChallengeProgress(force: Float, plantState: String) {
        val challenge = currentChallenge ?: return
        
        if (currentFlowerType == "MARGUERITE") {
            when (challenge.id) {
                1 -> updateChallenge1_FlowersInZone(force, plantState)
                2 -> updateChallenge2_Buds(force, plantState)
                3 -> updateChallenge3_FlowersAndBuds(force, plantState)
            }
        } else if (currentFlowerType == "ROSE") {
            when (challenge.id) {
                1 -> updateRoseChallenge1_FlowersInZone(force, plantState)
                2 -> updateRoseChallenge2_Divisions(force, plantState)
                3 -> updateRoseChallenge3_FlowersAndZone(force, plantState)
            }
        }
    }
    
    // Fonction pour signaler qu'une fleur a été créée
    fun notifyFlowerCreated(flowerX: Float, flowerY: Float, flowerId: String) {
        val challenge = currentChallenge ?: return
        
        if (currentFlowerType == "MARGUERITE") {
            // Logique existante pour marguerite
            if (challenge.id == 1) {
                val screenHeight = challengeData["screenHeight"] as? Float ?: 2000f
                val zoneTop = screenHeight / 3f - 60f
                val zoneBottom = screenHeight / 3f + 360f
                
                if (flowerY >= zoneTop && flowerY <= zoneBottom) {
                    if (!flowersInZone.contains(flowerId)) {
                        flowersInZone.add(flowerId)
                        challengeData["flowersInZoneCount"] = flowersInZone.size
                        println("Fleur dans la zone! Total: ${flowersInZone.size}/1")
                    }
                }
            } else if (challenge.id == 3) {
                val screenHeight = challengeData["screenHeight"] as? Float ?: 2000f
                val zoneTop = screenHeight / 3f - 120f
                val zoneBottom = screenHeight / 3f + 120f
                
                if (flowerY >= zoneTop && flowerY <= zoneBottom) {
                    if (!flowersInZoneDefi3.contains(flowerId)) {
                        flowersInZoneDefi3.add(flowerId)
                        challengeData["flowersInZoneDefi3Count"] = flowersInZoneDefi3.size
                        println("Défi 3 - Fleur dans la zone! Total: ${flowersInZoneDefi3.size}/2")
                    }
                }
            }
        } else if (currentFlowerType == "ROSE") {
            // NOUVEAU: Logique pour les défis du rosier
            if (challenge.id == 1) {
                // Zone verte : 2 pouces (~192px) au centre
                val screenHeight = challengeData["screenHeight"] as? Float ?: 2000f
                val zoneHeight = 192f  // 2 pouces
                val zoneTop = (screenHeight - zoneHeight) / 2f
                val zoneBottom = zoneTop + zoneHeight
                
                if (flowerY >= zoneTop && flowerY <= zoneBottom) {
                    if (!roseFlowersInZone.contains(flowerId)) {
                        roseFlowersInZone.add(flowerId)
                        challengeData["roseFlowersInZoneCount"] = roseFlowersInZone.size
                        println("Rosier - Fleur dans la zone! Total: ${roseFlowersInZone.size}/4")
                    }
                }
            } else if (challenge.id == 3) {
                // Défi 3 - Compter toutes les fleurs ET celles en zone
                if (!roseTotalFlowers.contains(flowerId)) {
                    roseTotalFlowers.add(flowerId)
                    challengeData["roseTotalFlowersCount"] = roseTotalFlowers.size
                    println("Rosier - Fleur totale! Total: ${roseTotalFlowers.size}/8")
                }
                
                // Vérifier si elle est aussi en zone verte
                val screenHeight = challengeData["screenHeight"] as? Float ?: 2000f
                val zoneHeight = 192f
                val zoneTop = (screenHeight - zoneHeight) / 2f
                val zoneBottom = zoneTop + zoneHeight
                
                if (flowerY >= zoneTop && flowerY <= zoneBottom) {
                    if (!roseFlowersInZoneDefi3.contains(flowerId)) {
                        roseFlowersInZoneDefi3.add(flowerId)
                        challengeData["roseFlowersInZoneDefi3Count"] = roseFlowersInZoneDefi3.size
                        println("Rosier - Fleur en zone défi 3! Total: ${roseFlowersInZoneDefi3.size}/3")
                    }
                }
            }
        }
    }
    
    // NOUVEAU: Fonction pour signaler qu'une division a été créée (rosier)
    fun notifyDivisionCreated(divisionId: String) {
        val challenge = currentChallenge ?: return
        
        if (currentFlowerType == "ROSE" && challenge.id == 2) {
            if (!roseDivisions.contains(divisionId)) {
                roseDivisions.add(divisionId)
                challengeData["roseDivisionsCount"] = roseDivisions.size
                println("Rosier - Division créée! Total: ${roseDivisions.size}/6")
            }
        }
    }
    
    // Fonction pour signaler qu'un bourgeon a été créé
    fun notifyBudCreated(budX: Float, budY: Float, budId: String) {
        val challenge = currentChallenge ?: return
        
        if (currentFlowerType == "MARGUERITE") {
            if (challenge.id == 2) {
                if (!budsCreated.contains(budId)) {
                    budsCreated.add(budId)
                    challengeData["budsCreatedCount"] = budsCreated.size
                    println("Bourgeon créé! Total: ${budsCreated.size}/2 (ID: $budId)")
                }
            } else if (challenge.id == 3) {
                if (!budsCreatedDefi3.contains(budId)) {
                    budsCreatedDefi3.add(budId)
                    challengeData["budsCreatedDefi3Count"] = budsCreatedDefi3.size
                    println("Défi 3 - Bourgeon créé! Total: ${budsCreatedDefi3.size}/1 (ID: $budId)")
                }
            }
        }
        // Le rosier n'a pas de défis avec bourgeons pour l'instant
    }
    
    // Mettre à jour les dimensions d'écran pour le calcul de zone
    fun updateScreenDimensions(width: Int, height: Int) {
        challengeData["screenWidth"] = width.toFloat()
        challengeData["screenHeight"] = height.toFloat()
    }
    
    fun checkChallengeCompletion(): ChallengeResult? {
        val challenge = currentChallenge ?: return null
        
        val isSuccessful = if (currentFlowerType == "MARGUERITE") {
            when (challenge.id) {
                1 -> checkChallenge1_FlowersInZone()
                2 -> checkChallenge2_Buds()
                3 -> checkChallenge3_FlowersAndBuds()
                else -> false
            }
        } else if (currentFlowerType == "ROSE") {
            when (challenge.id) {
                1 -> checkRoseChallenge1_FlowersInZone()
                2 -> checkRoseChallenge2_Divisions()
                3 -> checkRoseChallenge3_FlowersAndZone()
                else -> false
            }
        } else {
            false
        }
        
        if (isSuccessful) {
            challenge.isCompleted = true
            unlockNextChallenge(challenge.id)
            
            // Débloquer la prochaine fleur selon le défi complété
            if (currentFlowerType == "MARGUERITE" && challenge.id == 3) {
                unlockRoseFlower()
            } else if (currentFlowerType == "ROSE" && challenge.id == 3) {
                unlockLupinFlower()  // NOUVEAU: Débloquer le Lupin
            }
            
            saveChallengeProgress()
            
            val successMessage = if (currentFlowerType == "MARGUERITE") {
                when (challenge.id) {
                    1 -> "Défi réussi! ${flowersInZone.size} fleur dans la zone!"
                    2 -> "Défi réussi! ${budsCreated.size} bourgeons créés!"
                    3 -> "Défi réussi! ${flowersInZoneDefi3.size} fleurs + ${budsCreatedDefi3.size} bourgeon!\n🌹 ROSE DÉBLOQUÉE!"
                    else -> "Défi réussi!"
                }
            } else {
                when (challenge.id) {
                    1 -> "Défi réussi! ${roseFlowersInZone.size} fleurs en zone verte!"
                    2 -> "Défi réussi! ${roseDivisions.size} divisions créées!"
                    3 -> "Défi réussi! ${roseTotalFlowers.size} fleurs (${roseFlowersInZoneDefi3.size} en zone)!\n🌼 LUPIN DÉBLOQUÉ!"
                    else -> "Défi réussi!"
                }
            }
            
            return ChallengeResult(challenge, true, successMessage)
        }
        
        return null
    }
    
    fun finalizeChallengeResult(): ChallengeResult? {
        val challenge = currentChallenge ?: return null
        
        val result = checkChallengeCompletion() ?: run {
            val failMessage = if (currentFlowerType == "MARGUERITE") {
                when (challenge.id) {
                    1 -> "Défi échoué - Aucune fleur en zone verte!"
                    2 -> "Défi échoué - Seulement ${budsCreated.size}/2 bourgeons créés!"
                    3 -> "Défi échoué - ${flowersInZoneDefi3.size}/2 fleurs, ${budsCreatedDefi3.size}/1 bourgeon!"
                    else -> "Défi échoué!"
                }
            } else {
                when (challenge.id) {
                    1 -> "Défi échoué - Seulement ${roseFlowersInZone.size}/4 fleurs en zone!"
                    2 -> "Défi échoué - Seulement ${roseDivisions.size}/6 divisions créées!"
                    3 -> "Défi échoué - ${roseTotalFlowers.size}/8 fleurs (${roseFlowersInZoneDefi3.size}/3 en zone)!"
                    else -> "Défi échoué!"
                }
            }
            ChallengeResult(challenge, false, failMessage)
        }
        
        currentChallenge = null
        return result
    }
    
    // ==================== GESTION DES FLEURS DÉBLOQUÉES ====================
    
    private fun unlockRoseFlower() {
        if (unlockedFlowers.none { it.flowerType == "ROSE" }) {
            unlockedFlowers.add(UnlockedFlower("ROSE", "Défi 3 Marguerite complété"))
            println("🌹 ROSE DÉBLOQUÉE!")
        }
    }
    
    // NOUVEAU: Débloquer le Lupin
    private fun unlockLupinFlower() {
        if (unlockedFlowers.none { it.flowerType == "LUPIN" }) {
            unlockedFlowers.add(UnlockedFlower("LUPIN", "Défi 3 Rosier complété"))
            println("🌼 LUPIN DÉBLOQUÉ!")
        }
    }
    
    fun getUnlockedFlowers(): List<UnlockedFlower> = unlockedFlowers.toList()
    
    fun isFlowerUnlocked(flowerType: String): Boolean {
        return unlockedFlowers.any { it.flowerType == flowerType }
    }
    
    fun getFlowerUnlockMessage(flowerType: String): String? {
        return unlockedFlowers.find { it.flowerType == flowerType }?.unlockedBy
    }
    
    // ==================== LOGIQUE DES DÉFIS MARGUERITE (INCHANGÉE) ====================
    
    private fun updateChallenge1_FlowersInZone(force: Float, plantState: String) {
        challengeData["currentPhase"] = plantState
        challengeData["totalFlowers"] = flowersInZone.size
    }
    
    private fun checkChallenge1_FlowersInZone(): Boolean {
        return flowersInZone.size >= 1
    }
    
    private fun updateChallenge2_Buds(force: Float, plantState: String) {
        challengeData["currentPhase"] = plantState
        challengeData["totalBuds"] = budsCreated.size
        
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
    
    private fun checkChallenge2_Buds(): Boolean {
        return budsCreated.size >= 2
    }
    
    private fun updateChallenge3_FlowersAndBuds(force: Float, plantState: String) {
        challengeData["currentPhase"] = plantState
        challengeData["totalFlowersDefi3"] = flowersInZoneDefi3.size
        challengeData["totalBudsDefi3"] = budsCreatedDefi3.size
        
        if (force > 0) {
            val currentAvgForce = challengeData["avgForceDefi3"] as? Float ?: 0f
            val forceCount = challengeData["forceCountDefi3"] as? Int ?: 0
            val newAvgForce = (currentAvgForce * forceCount + force) / (forceCount + 1)
            challengeData["avgForceDefi3"] = newAvgForce
            challengeData["forceCountDefi3"] = forceCount + 1
        }
    }
    
    private fun checkChallenge3_FlowersAndBuds(): Boolean {
        return flowersInZoneDefi3.size >= 2 && budsCreatedDefi3.size >= 1
    }
    
    // ==================== NOUVEAU: LOGIQUE DES DÉFIS ROSIER ====================
    
    private fun updateRoseChallenge1_FlowersInZone(force: Float, plantState: String) {
        challengeData["currentPhase"] = plantState
        challengeData["totalRoseFlowers"] = roseFlowersInZone.size
    }
    
    private fun checkRoseChallenge1_FlowersInZone(): Boolean {
        return roseFlowersInZone.size >= 4
    }
    
    private fun updateRoseChallenge2_Divisions(force: Float, plantState: String) {
        challengeData["currentPhase"] = plantState
        challengeData["totalDivisions"] = roseDivisions.size
    }
    
    private fun checkRoseChallenge2_Divisions(): Boolean {
        return roseDivisions.size >= 6
    }
    
    private fun updateRoseChallenge3_FlowersAndZone(force: Float, plantState: String) {
        challengeData["currentPhase"] = plantState
        challengeData["totalRoseFlowersDefi3"] = roseTotalFlowers.size
        challengeData["roseFlowersInZoneDefi3"] = roseFlowersInZoneDefi3.size
    }
    
    private fun checkRoseChallenge3_FlowersAndZone(): Boolean {
        return roseTotalFlowers.size >= 8 && roseFlowersInZoneDefi3.size >= 3
    }
    
    // ==================== GESTION DU DÉBLOCAGE ====================
    
    private fun unlockNextChallenge(completedId: Int) {
        if (currentFlowerType == "MARGUERITE") {
            when (completedId) {
                1 -> margueriteChallenges.find { it.id == 2 }?.isUnlocked = true
                2 -> margueriteChallenges.find { it.id == 3 }?.isUnlocked = true
            }
        } else if (currentFlowerType == "ROSE") {
            when (completedId) {
                1 -> roseChallenges.find { it.id == 2 }?.isUnlocked = true
                2 -> roseChallenges.find { it.id == 3 }?.isUnlocked = true
            }
        }
        
        saveChallengeProgress()
    }
    
    // ==================== SAUVEGARDE ÉTENDUE ====================
    
    private fun saveChallengeProgress() {
        val editor = sharedPrefs?.edit() ?: return
        
        // Sauvegarder les défis marguerite
        for (challenge in margueriteChallenges) {
            editor.putBoolean("marguerite_challenge_${challenge.id}_completed", challenge.isCompleted)
            editor.putBoolean("marguerite_challenge_${challenge.id}_unlocked", challenge.isUnlocked)
        }
        
        // NOUVEAU: Sauvegarder les défis rosier
        for (challenge in roseChallenges) {
            editor.putBoolean("rose_challenge_${challenge.id}_completed", challenge.isCompleted)
            editor.putBoolean("rose_challenge_${challenge.id}_unlocked", challenge.isUnlocked)
        }
        
        // Sauvegarder les fleurs débloquées
        editor.putInt("unlocked_flowers_count", unlockedFlowers.size)
        for (i in unlockedFlowers.indices) {
            val flower = unlockedFlowers[i]
            editor.putString("unlocked_flower_${i}_type", flower.flowerType)
            editor.putString("unlocked_flower_${i}_unlocked_by", flower.unlockedBy)
            editor.putLong("unlocked_flower_${i}_date", flower.dateUnlocked)
        }
        
        editor.putLong("last_save_time", System.currentTimeMillis())
        editor.apply()
        println("Progression sauvegardée!")
    }
    
    private fun loadChallengeProgress() {
        val prefs = sharedPrefs ?: return
        
        // Charger les défis marguerite
        for (challenge in margueriteChallenges) {
            challenge.isCompleted = prefs.getBoolean("marguerite_challenge_${challenge.id}_completed", false)
            if (challenge.id == 1) {
                challenge.isUnlocked = true
            } else {
                challenge.isUnlocked = prefs.getBoolean("marguerite_challenge_${challenge.id}_unlocked", false)
            }
        }
        
        // NOUVEAU: Charger les défis rosier
        for (challenge in roseChallenges) {
            challenge.isCompleted = prefs.getBoolean("rose_challenge_${challenge.id}_completed", false)
            if (challenge.id == 1) {
                challenge.isUnlocked = true
            } else {
                challenge.isUnlocked = prefs.getBoolean("rose_challenge_${challenge.id}_unlocked", false)
            }
        }
        
        // Charger les fleurs débloquées
        unlockedFlowers.clear()
        unlockedFlowers.add(UnlockedFlower("MARGUERITE", "Disponible par défaut"))
        
        val flowerCount = prefs.getInt("unlocked_flowers_count", 1)
        for (i in 0 until flowerCount) {
            val flowerType = prefs.getString("unlocked_flower_${i}_type", null)
            val unlockedBy = prefs.getString("unlocked_flower_${i}_unlocked_by", null)
            val dateUnlocked = prefs.getLong("unlocked_flower_${i}_date", System.currentTimeMillis())
            
            if (flowerType != null && unlockedBy != null && flowerType != "MARGUERITE") {
                if (unlockedFlowers.none { it.flowerType == flowerType }) {
                    unlockedFlowers.add(UnlockedFlower(flowerType, unlockedBy, dateUnlocked))
                }
            }
        }
        
        val lastSaveTime = prefs.getLong("last_save_time", 0L)
        if (lastSaveTime > 0) {
            println("Progression chargée depuis: ${java.util.Date(lastSaveTime)}")
        }
    }
    
    fun resetAllChallenges() {
        margueriteChallenges.forEach { 
            it.isCompleted = false
            it.isUnlocked = (it.id == 1)
        }
        
        // NOUVEAU: Reset défis rosier
        roseChallenges.forEach { 
            it.isCompleted = false
            it.isUnlocked = (it.id == 1)
        }
        
        unlockedFlowers.clear()
        unlockedFlowers.add(UnlockedFlower("MARGUERITE", "Disponible par défaut"))
        
        sharedPrefs?.edit()?.clear()?.apply()
        println("Progression réinitialisée!")
    }
    
    fun activateCheatMode() {
        println("🎮 MODE CHEAT ACTIVÉ!")
        
        // Débloquer tous les défis
        for (challenge in margueriteChallenges) {
            challenge.isCompleted = true
            challenge.isUnlocked = true
        }
        
        // NOUVEAU: Débloquer défis rosier
        for (challenge in roseChallenges) {
            challenge.isCompleted = true
            challenge.isUnlocked = true
        }
        
        // Débloquer toutes les fleurs
        unlockedFlowers.clear()
        unlockedFlowers.add(UnlockedFlower("MARGUERITE", "Disponible par défaut"))
        unlockedFlowers.add(UnlockedFlower("ROSE", "Débloquée par cheat code"))
        unlockedFlowers.add(UnlockedFlower("LUPIN", "Débloqué par cheat code"))
        
        saveChallengeProgress()
        
        println("✅ Tous les défis complétés!")
        println("✅ Toutes les fleurs débloquées!")
    }
    
    // ==================== RÉSULTAT ====================
    
    data class ChallengeResult(
        val challenge: Challenge,
        val success: Boolean,
        val message: String
    )
}
