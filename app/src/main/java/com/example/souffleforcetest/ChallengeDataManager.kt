package com.example.souffleforcetest

import android.content.Context
import android.content.SharedPreferences

class ChallengeDataManager(private val context: Context?, private val definitions: ChallengeDefinitions) {
    
    // ==================== DATA CLASSES ====================
    
    data class UnlockedFlower(
        val flowerType: String,  // "MARGUERITE", "ROSE", "LUPIN", "IRIS", "ORCHIDEE"
        val unlockedBy: String,  // "Défi 3 Marguerite", etc.
        val dateUnlocked: Long = System.currentTimeMillis()
    )
    
    // ==================== CLASSES DE DONNÉES INTERNES ====================
    
    class MargueriteData {
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
    
    class RoseData {
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
    
    class LupinData {
        val lupinFlowers = mutableListOf<String>()
        val lupinSpikeColors = mutableSetOf<String>()
        val lupinCompleteStems = mutableListOf<String>()
        
        fun clear() {
            lupinFlowers.clear()
            lupinSpikeColors.clear()
            lupinCompleteStems.clear()
        }
    }
    
    class IrisData {
        val irisFlowersInZone = mutableListOf<String>()
        val irisRamifications = mutableListOf<String>()
        val irisTotalFlowers = mutableListOf<String>()
        
        fun clear() {
            irisFlowersInZone.clear()
            irisRamifications.clear()
            irisTotalFlowers.clear()
        }
    }
    
    // ✅ NOUVEAU: Classe de données pour orchidées
    class OrchideeData {
        val orchideeFlowersInZone = mutableListOf<String>()
        val orchideeSpeciesCreated = mutableSetOf<String>() // Espèces d'orchidées créées
        val orchideeCompleteStems = mutableListOf<String>() // Tiges complètes avec fleurs
        val orchideeSaccadesCompleted = mutableListOf<String>() // Saccades réussies
        val orchideeSpeciesCollected = mutableMapOf<String, Int>() // Compteur par espèce
        val orchideeTotalFlowers = mutableListOf<String>() // Toutes les fleurs d'orchidées
        
        fun clear() {
            orchideeFlowersInZone.clear()
            orchideeSpeciesCreated.clear()
            orchideeCompleteStems.clear()
            orchideeSaccadesCompleted.clear()
            orchideeSpeciesCollected.clear()
            orchideeTotalFlowers.clear()
        }
    }
    
    // ==================== VARIABLES D'ÉTAT ====================
    
    // Variables de suivi par type de fleur
    private val margueriteData = MargueriteData()
    private val roseData = RoseData()
    private val lupinData = LupinData()
    private val irisData = IrisData()
    private val orchideeData = OrchideeData() // ✅ NOUVEAU: Données orchidées
    
    // Gestion des fleurs débloquées
    private val unlockedFlowers = mutableListOf<UnlockedFlower>()
    
    // ==================== SAUVEGARDE ====================
    
    private val sharedPrefs: SharedPreferences? by lazy {
        context?.getSharedPreferences("challenges_save", Context.MODE_PRIVATE)
    }
    
    init {
        // FORCE LES 5 FLEURS POUR TESTER (including orchidées)
        unlockedFlowers.add(UnlockedFlower("MARGUERITE", "Disponible par défaut"))
        unlockedFlowers.add(UnlockedFlower("ROSE", "Test forcé"))
        unlockedFlowers.add(UnlockedFlower("LUPIN", "Test forcé"))
        unlockedFlowers.add(UnlockedFlower("IRIS", "Test forcé"))
        unlockedFlowers.add(UnlockedFlower("ORCHIDEE", "Test forcé")) // ✅ NOUVEAU: Force orchidées
    }
    
    // ==================== ACCESSEURS DES DONNÉES ====================
    
    fun getMargueriteData(): MargueriteData = margueriteData
    fun getRoseData(): RoseData = roseData
    fun getLupinData(): LupinData = lupinData
    fun getIrisData(): IrisData = irisData
    fun getOrchideeData(): OrchideeData = orchideeData // ✅ NOUVEAU: Accesseur orchidées
    
    fun clearFlowerData(flowerType: String) {
        when (flowerType) {
            "MARGUERITE" -> margueriteData.clear()
            "ROSE" -> roseData.clear()
            "LUPIN" -> lupinData.clear()
            "IRIS" -> irisData.clear()
            "ORCHIDEE" -> orchideeData.clear() // ✅ NOUVEAU: Clear orchidées
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
    
    // ==================== NOTIFICATIONS D'ÉVÉNEMENTS ====================
    
    fun notifyFlowerCreated(
        challenge: ChallengeDefinitions.Challenge?, 
        flowerType: String, 
        flowerY: Float, 
        challengeData: Map<String, Any>, 
        flowerId: String,
        definitions: ChallengeDefinitions
    ) {
        challenge ?: return
        val screenHeight = challengeData["screenHeight"] as? Float ?: 2000f
        
        when (flowerType) {
            "MARGUERITE" -> handleMargueriteFlower(challenge, flowerY, screenHeight, flowerId, definitions)
            "ROSE" -> handleRoseFlower(challenge, flowerY, screenHeight, flowerId, definitions)
            "LUPIN" -> handleLupinFlower(flowerId)
            "IRIS" -> handleIrisFlower(challenge, flowerY, screenHeight, flowerId, definitions)
            "ORCHIDEE" -> handleOrchideeFlower(challenge, flowerY, screenHeight, flowerId, definitions) // ✅ NOUVEAU
        }
    }
    
    private fun handleMargueriteFlower(
        challenge: ChallengeDefinitions.Challenge, 
        flowerY: Float, 
        screenHeight: Float, 
        flowerId: String,
        definitions: ChallengeDefinitions
    ) {
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
    
    private fun handleRoseFlower(
        challenge: ChallengeDefinitions.Challenge, 
        flowerY: Float, 
        screenHeight: Float, 
        flowerId: String,
        definitions: ChallengeDefinitions
    ) {
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
    
    private fun handleIrisFlower(
        challenge: ChallengeDefinitions.Challenge, 
        flowerY: Float, 
        screenHeight: Float, 
        flowerId: String,
        definitions: ChallengeDefinitions
    ) {
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
                
                // Pour les défis 2 et 3, vérifier aussi la zone
                if (definitions.isInIrisZone(flowerY, screenHeight)) {
                    if (!irisData.irisFlowersInZone.contains(flowerId)) {
                        irisData.irisFlowersInZone.add(flowerId)
                        println("Iris - Fleur en zone! Total: ${irisData.irisFlowersInZone.size}")
                    }
                }
            }
        }
    }
    
    // ✅ NOUVEAU: Gestion des fleurs d'orchidées
    private fun handleOrchideeFlower(
        challenge: ChallengeDefinitions.Challenge, 
        flowerY: Float, 
        screenHeight: Float, 
        flowerId: String,
        definitions: ChallengeDefinitions
    ) {
        // Ajouter à la liste totale
        if (!orchideeData.orchideeTotalFlowers.contains(flowerId)) {
            orchideeData.orchideeTotalFlowers.add(flowerId)
            println("Orchidée - Fleur créée! Total: ${orchideeData.orchideeTotalFlowers.size}")
        }
        
        when (challenge.id) {
            1 -> {
                // Défi 1: Saccades régulières - vérifier zone
                if (definitions.isInOrchideeZone(flowerY, screenHeight, 1)) {
                    if (!orchideeData.orchideeFlowersInZone.contains(flowerId)) {
                        orchideeData.orchideeFlowersInZone.add(flowerId)
                        println("Orchidée - Fleur dans la zone! Total: ${orchideeData.orchideeFlowersInZone.size}/8")
                    }
                }
            }
            2 -> {
                // Défi 2: Souffle délicat - zone très précise
                if (definitions.isInOrchideeZone(flowerY, screenHeight, 2)) {
                    if (!orchideeData.orchideeFlowersInZone.contains(flowerId)) {
                        orchideeData.orchideeFlowersInZone.add(flowerId)
                        println("Orchidée - Fleur délicate en zone! Total: ${orchideeData.orchideeFlowersInZone.size}/5")
                    }
                }
            }
            3 -> {
                // Défi 3: 6 espèces différentes - pas de zone spécifique
                println("Orchidée - Fleur pour défi patience! Total: ${orchideeData.orchideeTotalFlowers.size}/20")
            }
        }
    }
    
    fun notifyLupinSpikeCreated(
        challenge: ChallengeDefinitions.Challenge?, 
        flowerType: String, 
        spikeColor: String, 
        stemId: String
    ) {
        challenge ?: return
        
        if (flowerType == "LUPIN") {
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
    
    // ✅ NOUVEAU: Notification spécifique aux orchidées
    fun notifyOrchideeCreated(
        challenge: ChallengeDefinitions.Challenge?,
        species: String,
        stemId: String
    ) {
        challenge ?: return
        
        // Ajouter l'espèce créée
        orchideeData.orchideeSpeciesCreated.add(species)
        
        // Compter par espèce
        val currentCount = orchideeData.orchideeSpeciesCollected[species] ?: 0
        orchideeData.orchideeSpeciesCollected[species] = currentCount + 1
        
        when (challenge.id) {
            1 -> {
                // Défi 1: Saccades - vérifier les espèces créées
                println("Orchidée - Espèce $species créée! Espèces uniques: ${orchideeData.orchideeSpeciesCreated.size}/6")
            }
            2 -> {
                // Défi 2: Souffle délicat - tige complète
                if (!orchideeData.orchideeCompleteStems.contains(stemId)) {
                    orchideeData.orchideeCompleteStems.add(stemId)
                    println("Orchidée - Tige délicate complète! Total: ${orchideeData.orchideeCompleteStems.size}/3")
                }
            }
            3 -> {
                // Défi 3: Patience - toutes espèces
                val totalSpecies = orchideeData.orchideeSpeciesCreated.size
                println("Orchidée - Patience: $totalSpecies/6 espèces, ${orchideeData.orchideeTotalFlowers.size}/20 fleurs")
            }
        }
    }
    
    fun notifyOrchideeSaccadeCompleted(
        challenge: ChallengeDefinitions.Challenge?,
        saccadeId: String
    ) {
        challenge ?: return
        
        if (challenge.id == 1) {
            if (!orchideeData.orchideeSaccadesCompleted.contains(saccadeId)) {
                orchideeData.orchideeSaccadesCompleted.add(saccadeId)
                println("Orchidée - Saccade réussie! Total: ${orchideeData.orchideeSaccadesCompleted.size}/10")
            }
        }
    }
    
    fun notifyDivisionCreated(
        challenge: ChallengeDefinitions.Challenge?, 
        flowerType: String, 
        divisionId: String
    ) {
        challenge ?: return
        
        when (flowerType) {
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
        }
    }
    
    fun notifyBudCreated(
        challenge: ChallengeDefinitions.Challenge?, 
        flowerType: String, 
        budId: String
    ) {
        challenge ?: return
        
        if (flowerType == "MARGUERITE") {
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
    
    // ==================== GESTION DES DÉBLOCAGES ====================
    
    fun unlockNextChallenge(
        definitions: ChallengeDefinitions, 
        currentFlowerType: String, 
        completedId: Int
    ) {
        val nextChallenge = definitions.getNextUnlockedChallenge(currentFlowerType, completedId)
        if (nextChallenge != null) {
            val (flowerType, challengeId) = nextChallenge
            val challenge = definitions.findChallengeById(flowerType, challengeId)
            challenge?.isUnlocked = true
            saveProgress()
        }
    }
    
    fun unlockNextFlower(
        definitions: ChallengeDefinitions, 
        currentFlowerType: String, 
        completedId: Int
    ) {
        val nextFlowerType = definitions.getUnlockedFlowerType(currentFlowerType, completedId)
        if (nextFlowerType != null) {
            when (nextFlowerType) {
                "ROSE" -> unlockRoseFlower()
                "LUPIN" -> unlockLupinFlower()
                "IRIS" -> unlockIrisFlower()
                "ORCHIDEE" -> unlockOrchideeFlower() // ✅ NOUVEAU
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
    
    private fun unlockIrisFlower() {
        if (unlockedFlowers.none { it.flowerType == "IRIS" }) {
            unlockedFlowers.add(UnlockedFlower("IRIS", "Défi 3 Lupin complété"))
            println("🌺 IRIS DÉBLOQUÉ!")
        }
    }
    
    // ✅ NOUVEAU: Débloquage des orchidées
    private fun unlockOrchideeFlower() {
        if (unlockedFlowers.none { it.flowerType == "ORCHIDEE" }) {
            unlockedFlowers.add(UnlockedFlower("ORCHIDEE", "Défi 3 Iris complété"))
            println("🌸 ORCHIDÉE DÉBLOQUÉE!")
        }
    }
    
    // ==================== SAUVEGARDE ET CHARGEMENT ====================
    
    fun saveProgress() {
        val editor = sharedPrefs?.edit() ?: return
        
        // Sauvegarder tous les types de défis
        saveFlowerChallenges(editor, "marguerite", definitions.margueriteChallenges)
        saveFlowerChallenges(editor, "rose", definitions.roseChallenges)
        saveFlowerChallenges(editor, "lupin", definitions.lupinChallenges)
        saveFlowerChallenges(editor, "iris", definitions.irisChallenges)
        saveFlowerChallenges(editor, "orchidee", definitions.orchideeChallenges) // ✅ NOUVEAU
        
        // Sauvegarder les fleurs débloquées
        editor.putInt("unlocked_flowers_count", unlockedFlowers.size)
        for (i in unlockedFlowers.indices) {
            val flower = unlockedFlowers[i]
            editor.putString("unlocked_flower_${i}_type", flower.flowerType)
            editor.putString("unlocked_flower_${i}_unlocked_by", flower.unlockedBy)
            editor.putLong("unlocked_flower_${i}_date", flower.dateUnlocked)
        }
        
        // ✅ NOUVEAU: Sauvegarder données spécifiques orchidées
        saveOrchideeData(editor)
        
        editor.putLong("last_save_time", System.currentTimeMillis())
        editor.apply()
        println("Progression sauvegardée!")
    }
    
    // ✅ NOUVEAU: Sauvegarder données orchidées
    private fun saveOrchideeData(editor: SharedPreferences.Editor) {
        // Sauvegarder espèces créées
        editor.putInt("orchidee_species_count", orchideeData.orchideeSpeciesCreated.size)
        orchideeData.orchideeSpeciesCreated.forEachIndexed { index, species ->
            editor.putString("orchidee_species_$index", species)
        }
        
        // Sauvegarder compteurs par espèce
        editor.putInt("orchidee_collected_count", orchideeData.orchideeSpeciesCollected.size)
        orchideeData.orchideeSpeciesCollected.entries.forEachIndexed { index, (species, count) ->
            editor.putString("orchidee_collected_species_$index", species)
            editor.putInt("orchidee_collected_count_$index", count)
        }
        
        // Sauvegarder listes
        editor.putInt("orchidee_flowers_zone_count", orchideeData.orchideeFlowersInZone.size)
        editor.putInt("orchidee_complete_stems_count", orchideeData.orchideeCompleteStems.size)
        editor.putInt("orchidee_saccades_count", orchideeData.orchideeSaccadesCompleted.size)
        editor.putInt("orchidee_total_flowers_count", orchideeData.orchideeTotalFlowers.size)
    }
    
    private fun saveFlowerChallenges(
        editor: SharedPreferences.Editor, 
        flowerType: String, 
        challenges: List<ChallengeDefinitions.Challenge>
    ) {
        for (challenge in challenges) {
            editor.putBoolean("${flowerType}_challenge_${challenge.id}_completed", challenge.isCompleted)
            editor.putBoolean("${flowerType}_challenge_${challenge.id}_unlocked", challenge.isUnlocked)
        }
    }
    
    fun loadProgress() {
        val prefs = sharedPrefs ?: return
        
        // Charger tous les types de défis
        loadFlowerChallenges(prefs, "marguerite", definitions.margueriteChallenges)
        loadFlowerChallenges(prefs, "rose", definitions.roseChallenges)
        loadFlowerChallenges(prefs, "lupin", definitions.lupinChallenges)
        loadFlowerChallenges(prefs, "iris", definitions.irisChallenges)
        loadFlowerChallenges(prefs, "orchidee", definitions.orchideeChallenges) // ✅ NOUVEAU
        
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
        
        // ✅ NOUVEAU: Charger données orchidées
        loadOrchideeData(prefs)
        
        val lastSaveTime = prefs.getLong("last_save_time", 0L)
        if (lastSaveTime > 0) {
            println("Progression chargée depuis: ${java.util.Date(lastSaveTime)}")
        }
    }
    
    // ✅ NOUVEAU: Charger données orchidées
    private fun loadOrchideeData(prefs: SharedPreferences) {
        // Charger espèces créées
        val speciesCount = prefs.getInt("orchidee_species_count", 0)
        for (i in 0 until speciesCount) {
            val species = prefs.getString("orchidee_species_$i", null)
            if (species != null) {
                orchideeData.orchideeSpeciesCreated.add(species)
            }
        }
        
        // Charger compteurs par espèce
        val collectedCount = prefs.getInt("orchidee_collected_count", 0)
        for (i in 0 until collectedCount) {
            val species = prefs.getString("orchidee_collected_species_$i", null)
            val count = prefs.getInt("orchidee_collected_count_$i", 0)
            if (species != null) {
                orchideeData.orchideeSpeciesCollected[species] = count
            }
        }
        
        // Note: Les listes de flowersInZone, etc. ne sont pas sauvegardées car elles 
        // sont reconstruites à chaque session de jeu
    }
    
    private fun loadFlowerChallenges(
        prefs: SharedPreferences, 
        flowerType: String, 
        challenges: List<ChallengeDefinitions.Challenge>
    ) {
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
        for (challenges in listOf(
            definitions.margueriteChallenges, 
            definitions.roseChallenges, 
            definitions.lupinChallenges, 
            definitions.irisChallenges,
            definitions.orchideeChallenges // ✅ NOUVEAU
        )) {
            challenges.forEach { 
                it.isCompleted = false
                it.isUnlocked = (it.id == 1)
            }
        }
        
        unlockedFlowers.clear()
        unlockedFlowers.add(UnlockedFlower("MARGUERITE", "Disponible par défaut"))
        
        // ✅ NOUVEAU: Reset données orchidées
        orchideeData.clear()
        
        sharedPrefs?.edit()?.clear()?.apply()
        println("Progression réinitialisée!")
    }
    
    fun activateCheatMode() {
        println("🎮 MODE CHEAT ACTIVÉ!")
        
        // Débloquer tous les défis via ChallengeDefinitions
        for (challenges in listOf(
            definitions.margueriteChallenges, 
            definitions.roseChallenges, 
            definitions.lupinChallenges, 
            definitions.irisChallenges,
            definitions.orchideeChallenges // ✅ NOUVEAU
        )) {
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
        unlockedFlowers.add(UnlockedFlower("IRIS", "Débloqué par cheat code"))
        unlockedFlowers.add(UnlockedFlower("ORCHIDEE", "Débloquée par cheat code")) // ✅ NOUVEAU
        
        saveProgress()
        
        println("✅ Tous les défis complétés!")
        println("✅ Toutes les fleurs débloquées!")
    }
    
    // ==================== NOUVELLES FONCTIONS ORCHIDÉES ====================
    
    // ✅ NOUVEAU: Fonctions utilitaires spécifiques aux orchidées
    fun getOrchideeSpeciesCount(): Int = orchideeData.orchideeSpeciesCreated.size
    
    fun getOrchideeTotalFlowers(): Int = orchideeData.orchideeTotalFlowers.size
    
    fun getOrchideeSpeciesCollected(): Map<String, Int> = orchideeData.orchideeSpeciesCollected.toMap()
    
    fun hasCollectedAllSpecies(): Boolean = orchideeData.orchideeSpeciesCreated.size >= 6
    
    fun getOrchideeProgress(challengeId: Int): String {
        return when (challengeId) {
            1 -> {
                val saccades = orchideeData.orchideeSaccadesCompleted.size
                val species = orchideeData.orchideeSpeciesCreated.size
                val flowersInZone = orchideeData.orchideeFlowersInZone.size
                "Saccades: $saccades/10, Espèces: $species/6, Fleurs en zone: $flowersInZone/8"
            }
            2 -> {
                val stemsComplete = orchideeData.orchideeCompleteStems.size
                val flowersInZone = orchideeData.orchideeFlowersInZone.size
                "Tiges délicates: $stemsComplete/3, Fleurs en zone: $flowersInZone/5"
            }
            3 -> {
                val species = orchideeData.orchideeSpeciesCreated.size
                val totalFlowers = orchideeData.orchideeTotalFlowers.size
                "Patience: $species/6 espèces, $totalFlowers/20 fleurs"
            }
            else -> "Progression orchidées inconnue"
        }
    }
    
    fun checkOrchideeCompletion(challengeId: Int): Boolean {
        return when (challengeId) {
            1 -> {
                // Défi 1: 10 saccades + 6 espèces + 8 fleurs en zone
                orchideeData.orchideeSaccadesCompleted.size >= 10 &&
                orchideeData.orchideeSpeciesCreated.size >= 6 &&
                orchideeData.orchideeFlowersInZone.size >= 8
            }
            2 -> {
                // Défi 2: 3 tiges délicates + 5 fleurs en zone précise
                orchideeData.orchideeCompleteStems.size >= 3 &&
                orchideeData.orchideeFlowersInZone.size >= 5
            }
            3 -> {
                // Défi 3: 6 espèces + 20 fleurs totales
                orchideeData.orchideeSpeciesCreated.size >= 6 &&
                orchideeData.orchideeTotalFlowers.size >= 20
            }
            else -> false
        }
    }
}
