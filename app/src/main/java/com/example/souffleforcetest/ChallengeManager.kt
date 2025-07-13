package com.example.souffleforcetest

import android.content.Context
import android.content.SharedPreferences

class ChallengeManager(private val context: Context? = null) {
    
    // ==================== DÉLÉGATION AUX DÉFINITIONS ====================
    
    private val definitions = ChallengeDefinitions()
    
    // ==================== DATA CLASSES ====================
    
    data class UnlockedFlower(
        val flowerType: String,  // "MARGUERITE", "ROSE", "LUPIN", "IRIS", etc.
        val unlockedBy: String,  // "Défi 3 Marguerite", etc.
        val dateUnlocked: Long = System.currentTimeMillis()
    )
    
    // ==================== VARIABLES D'ÉTAT ====================
    
    private var currentChallenge: ChallengeDefinitions.Challenge? = null
    private var currentFlowerType: String = "MARGUERITE"
    private var challengeStartTime = 0L
    private var challengeData = mutableMapOf<String, Any>()
    
    // Variables de suivi par type de fleur
    private val margueriteData = MargueriteData()
    private val roseData = RoseData()
    private val lupinData = LupinData()
    private val irisData = IrisData()  // NOUVEAU: Données pour l'iris
    
    // Gestion des fleurs débloquées
    private val unlockedFlowers = mutableListOf<UnlockedFlower>()
    
    // ==================== CLASSES DE DONNÉES INTERNES ====================
    
    private class MargueriteData {
        val flowersInZone = mutableListOf<String>()
        val budsCreated = mutableListOf<String>()
        val flowersInZoneDefi3 = mutableListOf<String>()
        val budsCreatedDefi3 = mutableListOf<String>()
        
        fun clear() {
            flowersInZone.clear()
            budsCreated.clear()
            flowersInZoneDefi3.clear()
            budsCreatedDefi3.clear()
        }
    }
    
    private class RoseData {
        val roseFlowersInZone = mutableListOf<String>()
        val roseDivisions = mutableListOf<String>()
        val roseTotalFlowers = mutableListOf<String>()
        val roseFlowersInZoneDefi3 = mutableListOf<String>()
        
        fun clear() {
            roseFlowersInZone.clear()
            roseDivisions.clear()
            roseTotalFlowers.clear()
            roseFlowersInZoneDefi3.clear()
        }
    }
    
    private class LupinData {
        val lupinFlowers = mutableListOf<String>()
        val lupinSpikeColors = mutableSetOf<String>()
        val lupinCompleteStems = mutableListOf<String>()
        
        fun clear() {
            lupinFlowers.clear()
            lupinSpikeColors.clear()
            lupinCompleteStems.clear()
        }
    }
    
    // NOUVEAU: Classe de données pour l'iris
    private class IrisData {
        val irisFlowersInZone = mutableListOf<String>()
        val irisRamifications = mutableListOf<String>()
        val irisTotalFlowers = mutableListOf<String>()
        
        fun clear() {
            irisFlowersInZone.clear()
            irisRamifications.clear()
            irisTotalFlowers.clear()
        }
    }
    
    // ==================== SAUVEGARDE ====================
    
    private val sharedPrefs: SharedPreferences? by lazy {
        context?.getSharedPreferences("challenges_save", Context.MODE_PRIVATE)
    }
    
    init {
        // FORCE LES 4 FLEURS POUR TESTER
        unlockedFlowers.add(UnlockedFlower("MARGUERITE", "Disponible par défaut"))
        unlockedFlowers.add(UnlockedFlower("ROSE", "Test forcé"))
        unlockedFlowers.add(UnlockedFlower("LUPIN", "Test forcé"))
        unlockedFlowers.add(UnlockedFlower("IRIS", "Test forcé"))
        
        loadChallengeProgress()
    }
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    // Délégation vers ChallengeDefinitions
    fun getMargueriteChallenges(): List<ChallengeDefinitions.Challenge> = definitions.margueriteChallenges
    fun getRoseChallenges(): List<ChallengeDefinitions.Challenge> = definitions.roseChallenges
    fun getLupinChallenges(): List<ChallengeDefinitions.Challenge> = definitions.lupinChallenges
    fun getIrisChallenges(): List<ChallengeDefinitions.Challenge> = definitions.irisChallenges  // NOUVEAU
    
    fun setCurrentFlowerType(flowerType: String) {
        currentFlowerType = flowerType
    }
    
    fun getCurrentFlowerType(): String = currentFlowerType
    
    fun getCurrentChallenge(): ChallengeDefinitions.Challenge? = currentChallenge
    fun getCurrentChallengeBrief(): String? = currentChallenge?.briefText
    
    fun startChallenge(challengeId: Int) {
        currentChallenge = definitions.findChallengeById(currentFlowerType, challengeId)
        challengeStartTime = System.currentTimeMillis()
        challengeData.clear()
        
        // Reset des données selon le type de fleur
        when (currentFlowerType) {
            "MARGUERITE" -> margueriteData.clear()
            "ROSE" -> roseData.clear()
            "LUPIN" -> lupinData.clear()
            "IRIS" -> irisData.clear()  // NOUVEAU
        }
        
        println("Défi démarré: ${currentChallenge?.title} (${currentFlowerType})")
    }
    
    fun updateChallengeProgress(force: Float, plantState: String) {
        val challenge = currentChallenge ?: return
        
        // Déléguer vers ChallengeDefinitions
        when (currentFlowerType) {
            "MARGUERITE" -> definitions.updateMargueriteChallenge(challenge.id, force, plantState, challengeData)
            "ROSE" -> definitions.updateRoseChallenge(challenge.id, force, plantState, challengeData)
            "LUPIN" -> definitions.updateLupinChallenge(challenge.id, force, plantState, challengeData)
            "IRIS" -> definitions.updateIrisChallenge(challenge.id, force, plantState, challengeData)  // NOUVEAU
        }
    }
    
    fun updateScreenDimensions(width: Int, height: Int) {
        challengeData["screenWidth"] = width.toFloat()
        challengeData["screenHeight"] = height.toFloat()
    }
    
    // ==================== NOTIFICATIONS D'ÉVÉNEMENTS ====================
    
    fun notifyFlowerCreated(flowerX: Float, flowerY: Float, flowerId: String) {
        val challenge = currentChallenge ?: return
        val screenHeight = challengeData["screenHeight"] as? Float ?: 2000f
        
        when (currentFlowerType) {
            "MARGUERITE" -> handleMargueriteFlower(challenge, flowerY, screenHeight, flowerId)
            "ROSE" -> handleRoseFlower(challenge, flowerY, screenHeight, flowerId)
            "LUPIN" -> handleLupinFlower(flowerId)
            "IRIS" -> handleIrisFlower(challenge, flowerY, screenHeight, flowerId)  // NOUVEAU
        }
    }
    
    private fun handleMargueriteFlower(challenge: ChallengeDefinitions.Challenge, flowerY: Float, screenHeight: Float, flowerId: String) {
        when (challenge.id) {
            1 -> {
                if (definitions.isInMargueriteZone(flowerY, screenHeight, 1)) {
                    if (!margueriteData.flowersInZone.contains(flowerId)) {
                        margueriteData.flowersInZone.add(flowerId)
                        println("Fleur dans la zone! Total: ${margueriteData.flowersInZone.size}/1")
                    }
                }
            }
            3 -> {
                if (definitions.isInMargueriteZone(flowerY, screenHeight, 3)) {
                    if (!margueriteData.flowersInZoneDefi3.contains(flowerId)) {
                        margueriteData.flowersInZoneDefi3.add(flowerId)
                        println("Défi 3 - Fleur dans la zone! Total: ${margueriteData.flowersInZoneDefi3.size}/2")
                    }
                }
            }
        }
    }
    
    private fun handleRoseFlower(challenge: ChallengeDefinitions.Challenge, flowerY: Float, screenHeight: Float, flowerId: String) {
        when (challenge.id) {
            1 -> {
                if (definitions.isInRoseZone(flowerY, screenHeight)) {
                    if (!roseData.roseFlowersInZone.contains(flowerId)) {
                        roseData.roseFlowersInZone.add(flowerId)
                        println("Rosier - Fleur dans la zone! Total: ${roseData.roseFlowersInZone.size}/6")
                    }
                }
            }
            3 -> {
                if (!roseData.roseTotalFlowers.contains(flowerId)) {
                    roseData.roseTotalFlowers.add(flowerId)
                    println("Rosier - Fleur totale! Total: ${roseData.roseTotalFlowers.size}/15")
                }
                
                if (definitions.isInRoseZone(flowerY, screenHeight)) {
                    if (!roseData.roseFlowersInZoneDefi3.contains(flowerId)) {
                        roseData.roseFlowersInZoneDefi3.add(flowerId)
                        println("Rosier - Fleur en zone défi 3! Total: ${roseData.roseFlowersInZoneDefi3.size}/5")
                    }
                }
            }
        }
    }
    
    private fun handleLupinFlower(flowerId: String) {
        if (!lupinData.lupinFlowers.contains(flowerId)) {
            lupinData.lupinFlowers.add(flowerId)
            println("Lupin - Fleur créée! Total: ${lupinData.lupinFlowers.size}")
        }
    }
    
    // NOUVEAU: Gestion des fleurs d'iris
    private fun handleIrisFlower(challenge: ChallengeDefinitions.Challenge, flowerY: Float, screenHeight: Float, flowerId: String) {
        when (challenge.id) {
            1 -> {
                if (definitions.isInIrisZone(flowerY, screenHeight)) {
                    if (!irisData.irisFlowersInZone.contains(flowerId)) {
                        irisData.irisFlowersInZone.add(flowerId)
                        println("Iris - Fleur dans la zone centrale! Total: ${irisData.irisFlowersInZone.size}/4")
                    }
                }
            }
            2, 3 -> {
                if (!irisData.irisTotalFlowers.contains(flowerId)) {
                    irisData.irisTotalFlowers.add(flowerId)
                    println("Iris - Fleur totale! Total: ${irisData.irisTotalFlowers.size}")
                }
            }
        }
    }
    
    fun notifyLupinSpikeCreated(spikeColor: String, stemId: String) {
        val challenge = currentChallenge ?: return
        
        if (currentFlowerType == "LUPIN") {
            if (challenge.id == 1) {
                lupinData.lupinSpikeColors.add(spikeColor)
                println("Lupin - Épi de couleur $spikeColor! Total couleurs: ${lupinData.lupinSpikeColors.size}/3")
            }
            
            if (challenge.id == 2) {
                if (!lupinData.lupinCompleteStems.contains(stemId)) {
                    lupinData.lupinCompleteStems.add(stemId)
                    println("Lupin - Tige complète! Total: ${lupinData.lupinCompleteStems.size}/5")
                }
            }
        }
    }
    
    fun notifyDivisionCreated(divisionId: String) {
        val challenge = currentChallenge ?: return
        
        when (currentFlowerType) {
            "ROSE" -> {
                when (challenge.id) {
                    2 -> {
                        if (!roseData.roseDivisions.contains(divisionId)) {
                            roseData.roseDivisions.add(divisionId)
                            println("Rosier - Division créée! Total: ${roseData.roseDivisions.size}/10")
                        }
                    }
                    3 -> {
                        if (!roseData.roseDivisions.contains(divisionId)) {
                            roseData.roseDivisions.add(divisionId)
                            println("Rosier - Division défi 3! Total: ${roseData.roseDivisions.size}/8")
                        }
                    }
                }
            }
            "IRIS" -> {
                // NOUVEAU: Gestion des ramifications d'iris
                when (challenge.id) {
                    2 -> {
                        if (!irisData.irisRamifications.contains(divisionId)) {
                            irisData.irisRamifications.add(divisionId)
                            println("Iris - Ramification créée! Total: ${irisData.irisRamifications.size}/8")
                        }
                    }
                    3 -> {
                        if (!irisData.irisRamifications.contains(divisionId)) {
                            irisData.irisRamifications.add(divisionId)
                            println("Iris - Ramification défi 3! Total: ${irisData.irisRamifications.size}/6")
                        }
                    }
                }
            }
        }
    }
    
    fun notifyBudCreated(budX: Float, budY: Float, budId: String) {
        val challenge = currentChallenge ?: return
        
        if (currentFlowerType == "MARGUERITE") {
            when (challenge.id) {
                2 -> {
                    if (!margueriteData.budsCreated.contains(budId)) {
                        margueriteData.budsCreated.add(budId)
                        println("Bourgeon créé! Total: ${margueriteData.budsCreated.size}/2 (ID: $budId)")
                    }
                }
                3 -> {
                    if (!margueriteData.budsCreatedDefi3.contains(budId)) {
                        margueriteData.budsCreatedDefi3.add(budId)
                        println("Défi 3 - Bourgeon créé! Total: ${margueriteData.budsCreatedDefi3.size}/1 (ID: $budId)")
                    }
                }
            }
        }
    }
    
    // ==================== VÉRIFICATION ET FINALISATION DES DÉFIS ====================
    
    fun checkChallengeCompletion(): ChallengeDefinitions.ChallengeResult? {
        val challenge = currentChallenge ?: return null
        
        val isSuccessful = when (currentFlowerType) {
            "MARGUERITE" -> definitions.checkMargueriteChallenge(
                challenge.id,
                margueriteData.flowersInZone,
                margueriteData.budsCreated,
                margueriteData.flowersInZoneDefi3,
                margueriteData.budsCreatedDefi3
            )
            "ROSE" -> definitions.checkRoseChallenge(
                challenge.id,
                roseData.roseFlowersInZone,
                roseData.roseDivisions,
                roseData.roseTotalFlowers,
                roseData.roseFlowersInZoneDefi3
            )
            "LUPIN" -> definitions.checkLupinChallenge(
                challenge.id,
                lupinData.lupinSpikeColors,
                lupinData.lupinCompleteStems,
                lupinData.lupinFlowers
            )
            "IRIS" -> definitions.checkIrisChallenge(  // NOUVEAU
                challenge.id,
                irisData.irisFlowersInZone,
                irisData.irisRamifications,
                irisData.irisTotalFlowers
            )
            else -> false
        }
        
        if (isSuccessful) {
            challenge.isCompleted = true
            unlockNextChallenge(challenge.id)
            unlockNextFlower(challenge.id)
            saveChallengeProgress()
            
            val successMessage = getSuccessMessage(challenge.id)
            return ChallengeDefinitions.ChallengeResult(challenge, true, successMessage)
        }
        
        return null
    }
    
    fun finalizeChallengeResult(): ChallengeDefinitions.ChallengeResult? {
        val challenge = currentChallenge ?: return null
        
        val result = checkChallengeCompletion() ?: run {
            val failMessage = getFailMessage(challenge.id)
            ChallengeDefinitions.ChallengeResult(challenge, false, failMessage)
        }
        
        currentChallenge = null
        return result
    }
    
    private fun getSuccessMessage(challengeId: Int): String {
        return when (currentFlowerType) {
            "MARGUERITE" -> definitions.getMargueriteSuccessMessage(
                challengeId,
                margueriteData.flowersInZone,
                margueriteData.budsCreated,
                margueriteData.flowersInZoneDefi3,
                margueriteData.budsCreatedDefi3
            )
            "ROSE" -> definitions.getRoseSuccessMessage(
                challengeId,
                roseData.roseFlowersInZone,
                roseData.roseDivisions,
                roseData.roseTotalFlowers,
                roseData.roseFlowersInZoneDefi3
            )
            "LUPIN" -> definitions.getLupinSuccessMessage(
                challengeId,
                lupinData.lupinSpikeColors,
                lupinData.lupinCompleteStems,
                lupinData.lupinFlowers
            )
            "IRIS" -> definitions.getIrisSuccessMessage(  // NOUVEAU
                challengeId,
                irisData.irisFlowersInZone,
                irisData.irisRamifications,
                irisData.irisTotalFlowers
            )
            else -> "Défi réussi!"
        }
    }
    
    private fun getFailMessage(challengeId: Int): String {
        return when (currentFlowerType) {
            "MARGUERITE" -> definitions.getMargueriteFailMessage(
                challengeId,
                margueriteData.flowersInZone,
                margueriteData.budsCreated,
                margueriteData.flowersInZoneDefi3,
                margueriteData.budsCreatedDefi3
            )
            "ROSE" -> definitions.getRoseFailMessage(
                challengeId,
                roseData.roseFlowersInZone,
                roseData.roseDivisions,
                roseData.roseTotalFlowers,
                roseData.roseFlowersInZoneDefi3
            )
            "LUPIN" -> definitions.getLupinFailMessage(
                challengeId,
                lupinData.lupinSpikeColors,
                lupinData.lupinCompleteStems,
                lupinData.lupinFlowers
            )
            "IRIS" -> definitions.getIrisFailMessage(  // NOUVEAU
                challengeId,
                irisData.irisFlowersInZone,
                irisData.irisRamifications,
                irisData.irisTotalFlowers
            )
            else -> "Défi échoué!"
        }
    }
    
    // ==================== GESTION DES DÉBLOCAGES ====================
    
    private fun unlockNextChallenge(completedId: Int) {
        val nextChallenge = definitions.getNextUnlockedChallenge(currentFlowerType, completedId)
        if (nextChallenge != null) {
            val (flowerType, challengeId) = nextChallenge
            val challenge = definitions.findChallengeById(flowerType, challengeId)
            challenge?.isUnlocked = true
            saveChallengeProgress()
        }
    }
    
    private fun unlockNextFlower(completedId: Int) {
        val nextFlowerType = definitions.getUnlockedFlowerType(currentFlowerType, completedId)
        if (nextFlowerType != null) {
            when (nextFlowerType) {
                "ROSE" -> unlockRoseFlower()
                "LUPIN" -> unlockLupinFlower()
                "IRIS" -> unlockIrisFlower()
                "ORCHIDEE" -> unlockOrchideeFlower()  // Pour le futur
            }
        }
    }
    
    private fun unlockRoseFlower() {
        if (unlockedFlowers.none { it.flowerType == "ROSE" }) {
            unlockedFlowers.add(UnlockedFlower("ROSE", "Défi 3 Marguerite complété"))
            println("🌹 ROSE DÉBLOQUÉE!")
        }
    }
    
    private fun unlockLupinFlower() {
        if (unlockedFlowers.none { it.flowerType == "LUPIN" }) {
            unlockedFlowers.add(UnlockedFlower("LUPIN", "Défi 3 Rosier complété"))
            println("🌼 LUPIN DÉBLOQUÉ!")
        }
    }
    
    // NOUVEAU: Déblocage de l'iris
    private fun unlockIrisFlower() {
        if (unlockedFlowers.none { it.flowerType == "IRIS" }) {
            unlockedFlowers.add(UnlockedFlower("IRIS", "Défi 3 Lupin complété"))
            println("🌺 IRIS DÉBLOQUÉ!")
        }
    }
    
    private fun unlockOrchideeFlower() {
        if (unlockedFlowers.none { it.flowerType == "ORCHIDEE" }) {
            unlockedFlowers.add(UnlockedFlower("ORCHIDEE", "Défi 3 Iris complété"))
            println("🌸 ORCHIDÉE DÉBLOQUÉE!")
        }
    }
    
    // ==================== GESTION DES FLEURS DÉBLOQUÉES ====================
    
    fun getUnlockedFlowers(): List<UnlockedFlower> = unlockedFlowers.toList()
    
    fun isFlowerUnlocked(flowerType: String): Boolean {
        return unlockedFlowers.any { it.flowerType == flowerType }
    }
    
    fun getFlowerUnlockMessage(flowerType: String): String? {
        return unlockedFlowers.find { it.flowerType == flowerType }?.unlockedBy
    }
    
    // ==================== SAUVEGARDE ET CHARGEMENT ====================
    
    private fun saveChallengeProgress() {
        val editor = sharedPrefs?.edit() ?: return
        
        // Sauvegarder tous les types de défis
        saveFlowerChallenges(editor, "marguerite", definitions.margueriteChallenges)
        saveFlowerChallenges(editor, "rose", definitions.roseChallenges)
        saveFlowerChallenges(editor, "lupin", definitions.lupinChallenges)
        saveFlowerChallenges(editor, "iris", definitions.irisChallenges)  // NOUVEAU
        
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
    
    private fun saveFlowerChallenges(editor: android.content.SharedPreferences.Editor, flowerType: String, challenges: List<ChallengeDefinitions.Challenge>) {
        for (challenge in challenges) {
            editor.putBoolean("${flowerType}_challenge_${challenge.id}_completed", challenge.isCompleted)
            editor.putBoolean("${flowerType}_challenge_${challenge.id}_unlocked", challenge.isUnlocked)
        }
    }
    
    private fun loadChallengeProgress() {
        val prefs = sharedPrefs ?: return
        
        // Charger tous les types de défis
        loadFlowerChallenges(prefs, "marguerite", definitions.margueriteChallenges)
        loadFlowerChallenges(prefs, "rose", definitions.roseChallenges)
        loadFlowerChallenges(prefs, "lupin", definitions.lupinChallenges)
        loadFlowerChallenges(prefs, "iris", definitions.irisChallenges)
        
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
    
    private fun loadFlowerChallenges(prefs: SharedPreferences, flowerType: String, challenges: List<ChallengeDefinitions.Challenge>) {
        for (challenge in challenges) {
            challenge.isCompleted = prefs.getBoolean("${flowerType}_challenge_${challenge.id}_completed", false)
            if (challenge.id == 1) {
                challenge.isUnlocked = true
            } else {
                challenge.isUnlocked = prefs.getBoolean("${flowerType}_challenge_${challenge.id}_unlocked", false)
            }
        }
    }
    
    // ==================== FONCTIONS UTILITAIRES ====================
    
    fun resetAllChallenges() {
        // Reset tous les défis via ChallengeDefinitions
        for (challenges in listOf(definitions.margueriteChallenges, definitions.roseChallenges, definitions.lupinChallenges, definitions.irisChallenges)) {
            challenges.forEach { 
                it.isCompleted = false
                it.isUnlocked = (it.id == 1)
            }
        }
        
        unlockedFlowers.clear()
        unlockedFlowers.add(UnlockedFlower("MARGUERITE", "Disponible par défaut"))
        
        sharedPrefs?.edit()?.clear()?.apply()
        println("Progression réinitialisée!")
    }
    
    fun activateCheatMode() {
        println("🎮 MODE CHEAT ACTIVÉ!")
        
        // Débloquer tous les défis via ChallengeDefinitions
        for (challenges in listOf(definitions.margueriteChallenges, definitions.roseChallenges, definitions.lupinChallenges, definitions.irisChallenges)) {
            challenges.forEach {
                it.isCompleted = true
                it.isUnlocked = true
            }
        }
        
        // Débloquer toutes les fleurs
        unlockedFlowers.clear()
        unlockedFlowers.add(UnlockedFlower("MARGUERITE", "Disponible par défaut"))
        unlockedFlowers.add(UnlockedFlower("ROSE", "Débloquée par cheat code"))
        unlockedFlowers.add(UnlockedFlower("LUPIN", "Débloqué par cheat code"))
        unlockedFlowers.add(UnlockedFlower("IRIS", "Débloqué par cheat code"))  // AJOUTÉ !
        
        saveChallengeProgress()
        
        println("✅ Tous les défis complétés!")
        println("✅ Toutes les fleurs débloquées!")
    }
}
