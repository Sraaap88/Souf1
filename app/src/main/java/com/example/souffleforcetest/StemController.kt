package com.example.souffleforcetest

import kotlin.math.*

class StemController(private val plantStem: PlantStem) {
    
    // ==================== DATA CLASSES ====================
    
    data class StemCreationPlan(
        val targetStemCount: Int,       // Nombre de tiges à créer
        val creationOrder: List<Int>,   // Ordre de création des tiges
        val isMainStemActive: Boolean,  // Tige principale activée
        val timingOffsets: List<Long>   // Décalages temporels pour chaque tige
    )
    
    data class StemGrowthState(
        val stemIndex: Int,             // -1 = principale, 0+ = branches
        val isActive: Boolean,          // Tige en croissance
        val startTime: Long,            // Moment de création
        val lastGrowthTime: Long,       // Dernière croissance
        val forceMultiplier: Float      // Multiplicateur de force pour cette tige
    )
    
    // ==================== VARIABLES ====================
    
    private val breathAnalyzer = BreathAnalyzer()
    private var currentPlan: StemCreationPlan? = null
    private val stemStates = mutableMapOf<Int, StemGrowthState>()
    
    private var phaseStartTime = 0L
    private var lastAnalysisTime = 0L
    private var planGenerated = false
    
    // ==================== PARAMÈTRES ====================
    
    private val maxStems = 7                    // Maximum 7 tiges (1 principale + 6 branches)
    private val planGenerationDelay = 1000L     // Attendre 1s avant de générer le plan
    private val stemCreationInterval = 200L     // 200ms entre chaque création de tige
    private val analysisInterval = 33L          // Analyse toutes les 33ms (30 FPS)
    
    // Mapping saccades → nombre de tiges
    private val stemCountMapping = mapOf(
        0 to 1,    // Aucune saccade détectée = 1 tige principale
        1 to 1,    // 1 saccade = 1 tige principale
        2 to 3,    // 2 saccades = 3 tiges
        3 to 3,    // 3 saccades = 3 tiges  
        4 to 5,    // 4 saccades = 5 tiges
        5 to 5,    // 5 saccades = 5 tiges
        6 to 7     // 6+ saccades = 7 tiges
    )
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun startGrowthPhase(currentTime: Long) {
        phaseStartTime = currentTime
        lastAnalysisTime = currentTime
        planGenerated = false
        currentPlan = null
        stemStates.clear()
        breathAnalyzer.reset()
        
        // Activer immédiatement la tige principale
        activateMainStem(currentTime)
    }
    
    fun processGrowth(force: Float, currentTime: Long): Boolean {
        // Analyser le souffle à intervalle régulier
        if (currentTime - lastAnalysisTime >= analysisInterval) {
            breathAnalyzer.analyzeBreath(force, currentTime)
            lastAnalysisTime = currentTime
        }
        
        // Générer le plan après le délai initial
        if (!planGenerated && currentTime - phaseStartTime >= planGenerationDelay) {
            generateStemCreationPlan(currentTime)
            planGenerated = true
        }
        
        // Exécuter le plan de création
        executeStemCreationPlan(currentTime)
        
        // Faire pousser les tiges actives
        return growActiveStems(force, currentTime)
    }
    
    fun getStemCount(): Int {
        return stemStates.size
    }
    
    fun getActiveStems(): List<Int> {
        return stemStates.filter { it.value.isActive }.keys.toList()
    }
    
    fun getBreathAnalysis(): BreathAnalyzer.BreathAnalysis {
        return breathAnalyzer.analyzeBreath(0f, System.currentTimeMillis())
    }
    
    fun debugInfo(): String {
        val plan = currentPlan
        val breathInfo = breathAnalyzer.debugInfo()
        val activeStems = getActiveStems().size
        
        return "Plan: ${plan?.targetStemCount ?: "none"} tiges, " +
               "Actives: $activeStems, $breathInfo"
    }
    
    // ==================== FONCTIONS PRIVÉES ====================
    
    private fun activateMainStem(currentTime: Long) {
        val mainStemState = StemGrowthState(
            stemIndex = -1,
            isActive = true,
            startTime = currentTime,
            lastGrowthTime = currentTime,
            forceMultiplier = 1.0f
        )
        stemStates[-1] = mainStemState
    }
    
    private fun generateStemCreationPlan(currentTime: Long) {
        val analysis = breathAnalyzer.analyzeBreath(0f, currentTime)
        val breathCount = analysis.breathCount
        
        // Déterminer le nombre de tiges selon les saccades
        val targetCount = stemCountMapping[breathCount.coerceAtMost(6)] ?: 1
        
        // Générer l'ordre de création (aléatoire mais équilibré)
        val creationOrder = generateCreationOrder(targetCount)
        
        // Calculer les décalages temporels
        val timingOffsets = generateTimingOffsets(targetCount, currentTime)
        
        currentPlan = StemCreationPlan(
            targetStemCount = targetCount,
            creationOrder = creationOrder,
            isMainStemActive = true,
            timingOffsets = timingOffsets
        )
        
        println("Plan généré: ${targetCount} tiges pour ${breathCount} saccades")
    }
    
    private fun generateCreationOrder(targetCount: Int): List<Int> {
        if (targetCount <= 1) return emptyList()
        
        // Créer l'ordre de création des branches (exclut la tige principale)
        val branchCount = targetCount - 1 // -1 pour la tige principale
        val order = mutableListOf<Int>()
        
        // Ordre équilibré : alterner gauche/droite
        when (branchCount) {
            1 -> order.add(0)                    // 1 branche à droite
            2 -> order.addAll(listOf(0, 1))      // droite, gauche
            3 -> order.addAll(listOf(0, 1, 2))   // droite, gauche, droite loin
            4 -> order.addAll(listOf(0, 1, 2, 3)) // équilibré
            5 -> order.addAll(listOf(0, 1, 2, 3, 4)) // droite, gauche, droite, gauche, droite
            6 -> order.addAll(listOf(0, 1, 2, 3, 4, 5)) // tous
        }
        
        return order
    }
    
    private fun generateTimingOffsets(targetCount: Int, baseTime: Long): List<Long> {
        val offsets = mutableListOf<Long>()
        
        // Première tige = immédiatement (déjà créée)
        offsets.add(0L)
        
        // Autres tiges = avec décalage progressif
        for (i in 1 until targetCount) {
            val offset = i * stemCreationInterval
            offsets.add(baseTime + offset)
        }
        
        return offsets
    }
    
    private fun executeStemCreationPlan(currentTime: Long) {
        val plan = currentPlan ?: return
        
        // Créer les branches selon le plan et le timing
        for (i in plan.creationOrder.indices) {
            val branchIndex = plan.creationOrder[i]
            val creationTime = plan.timingOffsets.getOrNull(i + 1) ?: continue
            
            // Vérifier si c'est le moment de créer cette branche
            if (currentTime >= creationTime && !stemStates.containsKey(branchIndex)) {
                createBranch(branchIndex, currentTime)
            }
        }
    }
    
    private fun createBranch(branchIndex: Int, currentTime: Long) {
        // Déléguer la création physique à PlantStem
        try {
            // On utilise la méthode existante en appelant directement la création
            val method = plantStem::class.java.getDeclaredMethod("createBranch", Int::class.java)
            method.isAccessible = true
            method.invoke(plantStem, branchIndex + 1) // +1 car PlantStem utilise 1-6
            
            // Enregistrer l'état de la nouvelle branche
            val branchState = StemGrowthState(
                stemIndex = branchIndex,
                isActive = true,
                startTime = currentTime,
                lastGrowthTime = currentTime,
                forceMultiplier = 0.95f // Légèrement plus lent que la principale
            )
            stemStates[branchIndex] = branchState
            
            println("Branche $branchIndex créée")
            
        } catch (e: Exception) {
            println("Erreur création branche $branchIndex: ${e.message}")
        }
    }
    
    private fun growActiveStems(force: Float, currentTime: Long): Boolean {
        if (force <= 0.15f) return false // Seuil minimum
        
        var hasGrowth = false
        
        for ((stemIndex, state) in stemStates) {
            if (!state.isActive) continue
            
            // Calculer la force ajustée pour cette tige
            val adjustedForce = force * state.forceMultiplier
            
            // Faire pousser selon le type de tige
            when (stemIndex) {
                -1 -> {
                    // Tige principale
                    plantStem.growthManager?.growMainStem(adjustedForce)
                    hasGrowth = true
                }
                else -> {
                    // Branches - faire pousser toutes les branches actives
                    plantStem.growthManager?.growAllBranches(adjustedForce)
                    hasGrowth = true
                }
            }
            
            // Mettre à jour le temps de dernière croissance
            stemStates[stemIndex] = state.copy(lastGrowthTime = currentTime)
        }
        
        return hasGrowth
    }
    
    // ==================== UTILITAIRES ====================
    
    fun getPlannedStemCount(): Int {
        return currentPlan?.targetStemCount ?: 1
    }
    
    fun getBreathCount(): Int {
        return breathAnalyzer.getBreathCount()
    }
    
    fun getForceStability(): Float {
        return breathAnalyzer.getForceStability()
    }
    
    fun isPlanGenerated(): Boolean {
        return planGenerated
    }
    
    fun getCreationProgress(): Float {
        val plan = currentPlan ?: return 1f
        val activeCount = stemStates.size
        return activeCount.toFloat() / plan.targetStemCount.toFloat()
    }
}
