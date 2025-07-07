package com.example.souffleforcetest

import kotlin.math.*

class PlantStem(private val screenWidth: Int, private val screenHeight: Int) {
    
    // ==================== DATA CLASSES ====================
    
    data class StemPoint(
        val x: Float,
        val y: Float,
        val thickness: Float,
        var oscillation: Float = 0f,
        var permanentWave: Float = 0f
    )
    
    data class Branch(
        val points: MutableList<StemPoint> = mutableListOf(),
        val angle: Float,
        val startHeight: Float,
        val baseOffset: Float = 0f,
        val isMainStem: Boolean = false,
        var isActive: Boolean = true,
        var currentHeight: Float = 0f,
        val maxHeight: Float = 0f,
        val personalityFactor: Float = 1f,
        val trembleFrequency: Float = 1f,
        val curvatureDirection: Float = 1f,
        val thicknessVariation: Float = 1f
    )
    
    // ==================== VARIABLES PRINCIPALES ====================
    
    val mainStem = mutableListOf<StemPoint>()
    val branches = mutableListOf<Branch>()
    
    private var stemHeight = 0f
    private var maxPossibleHeight = 0f
    private val stemBaseX = screenWidth / 2f
    private val stemBaseY = screenHeight - 100f
    private var lastForce = 0f
    private var isEmerging = false
    private var emergenceStartTime = 0L
    private var branchSide = true
    private var branchCount = 0
    private var branchCreationOrder = mutableListOf<Int>()
    
    // ==================== NOUVEAU SYSTÈME SACCADES SIMPLE ====================
    
    private var saccadeCount = 0
    private var isCurrentlyBreathing = false
    private var lastSaccadeTime = 0L
    private var lastForceState = 0f
    private val saccadeCooldown = 300L // 300ms minimum entre saccades
    private val breathStartThreshold = 0.3f
    private val breathEndThreshold = 0.2f
    
    // Mapping saccades → nombre total de tiges
    private val saccadeToStemMapping = mapOf(
        0 to 1,  // Pas de saccade détectée = 1 tige
        1 to 1,  // 1 saccade = 1 tige
        2 to 3,  // 2 saccades = 3 tiges
        3 to 5,  // 3 saccades = 5 tiges
        4 to 7   // 4+ saccades = 7 tiges (maximum)
    )
    
    // Instance du gestionnaire de croissance - initialisation tardive
    private lateinit var growthManager: PlantGrowthManager
    
    // Instance du gestionnaire de feuilles
    private lateinit var leavesManager: PlantLeavesManager
    
    // Instance du gestionnaire de fleurs
    private lateinit var flowerManager: FlowerManager
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.15f
    private val maxStemHeight = 0.75f // Réduit pour que les branches soient plus hautes
    private val baseThickness = 25f
    private val tipThickness = 8f
    private val growthRate = 2400f
    private val oscillationDecay = 0.98f
    private val emergenceDuration = 1000L
    private val maxBranches = 6 // 6 tiges secondaires + 1 principale = 7 total
    
    init {
        maxPossibleHeight = screenHeight * maxStemHeight
        growthManager = PlantGrowthManager(this)
        leavesManager = PlantLeavesManager(this)
        flowerManager = FlowerManager(this)
        initializeBranchOrder()
    }
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun processStemGrowth(force: Float, phaseTime: Long) {
        // INITIALISATION FORCÉE : créer le point de base dès le premier appel
        if (mainStem.isEmpty() && !isEmerging) {
            isEmerging = true
            emergenceStartTime = System.currentTimeMillis()
            mainStem.add(StemPoint(stemBaseX, stemBaseY, baseThickness))
        }
        
        // Phase d'émergence (1 seconde)
        if (phaseTime < emergenceDuration) {
            if (force > forceThreshold && !isEmerging) {
                isEmerging = true
                emergenceStartTime = System.currentTimeMillis()
            }
            
            if (isEmerging) {
                val emergenceProgress = (System.currentTimeMillis() - emergenceStartTime) / emergenceDuration.toFloat()
                if (emergenceProgress <= 1f) {
                    createEmergenceStem(emergenceProgress)
                }
            }
            return
        }
        
        // NOUVEAU : Détection des saccades pendant toute la phase de croissance
        detectSaccades(force, System.currentTimeMillis())
        
        // Phase de croissance normale avec saccades
        if (force > forceThreshold && mainStem.isNotEmpty()) {
            if (force > forceThreshold * 1.5f) {
                growthManager.growMainStem(force)
                growthManager.growAllBranches(force)
            }
        }
        
        growthManager.updateOscillations()
        lastForce = force
    }
    
    fun processLeavesGrowth(force: Float) {
        leavesManager.processLeavesGrowth(force)
    }
    
    fun processFlowerGrowth(force: Float) {
        flowerManager.processFlowerGrowth(force)
    }
    
    fun resetStem() {
        mainStem.clear()
        branches.clear()
        stemHeight = 0f
        lastForce = 0f
        isEmerging = false
        branchSide = true
        branchCount = 0
        branchCreationOrder.clear()
        initializeBranchOrder()
        leavesManager.resetLeaves()
        flowerManager.resetFlowers()
        
        // NOUVEAU : Reset du système de saccades
        saccadeCount = 0
        isCurrentlyBreathing = false
        lastSaccadeTime = 0L
        lastForceState = 0f
    }
    
    fun getStemHeight(): Float = stemHeight
    fun hasVisibleStem(): Boolean = mainStem.size >= 1
    
    fun getLeaves(): List<PlantLeavesManager.Leaf> = leavesManager.leaves
    fun getLeavesManager(): PlantLeavesManager = leavesManager
    fun getFlowers(): List<FlowerManager.Flower> = flowerManager.flowers
    fun getFlowerManager(): FlowerManager = flowerManager
    
    // EXPOSER GROWTHMANAGER POUR STEMCONTROLLER
    fun getGrowthManager(): PlantGrowthManager = growthManager
    
    // ==================== NOUVEAU SYSTÈME SACCADES ====================
    
    private fun detectSaccades(force: Float, currentTime: Long) {
        val wasBreathing = isCurrentlyBreathing
        val isNowBreathing = force > breathStartThreshold
        
        // Détection début de souffle (saccade)
        if (!wasBreathing && isNowBreathing) {
            // Vérifier le cooldown pour éviter les faux positifs
            if (currentTime - lastSaccadeTime > saccadeCooldown) {
                saccadeCount++
                lastSaccadeTime = currentTime
                isCurrentlyBreathing = true
                
                println("Saccade $saccadeCount détectée ! Force: %.2f".format(force))
                
                // Créer les tiges selon le mapping
                createStemsFromSaccades()
            }
        }
        
        // Détection fin de souffle
        if (wasBreathing && force < breathEndThreshold) {
            isCurrentlyBreathing = false
        }
        
        lastForceState = force
    }
    
    private fun createStemsFromSaccades() {
        val targetStemCount = saccadeToStemMapping[saccadeCount.coerceAtMost(4)] ?: 1
        val currentStemCount = 1 + branchCount // 1 principale + branches
        
        // Créer les tiges manquantes
        if (targetStemCount > currentStemCount) {
            val stemsToCreate = targetStemCount - currentStemCount
            
            for (i in 1..stemsToCreate) {
                if (branchCount < maxBranches) {
                    createBranchInOrder()
                    println("Tige créée ! Total: ${branchCount + 1}/${targetStemCount}")
                }
            }
        }
    }
    
    fun getSaccadeInfo(): String {
        val targetStems = saccadeToStemMapping[saccadeCount.coerceAtMost(4)] ?: 1
        val currentStems = 1 + branchCount
        return "Saccades: $saccadeCount → Cible: $targetStems tiges, Actuel: $currentStems tiges"
    }
    
    // ==================== GETTERS POUR GROWTHMANAGER ====================
    
    fun getMaxPossibleHeight(): Float = maxPossibleHeight
    fun getStemBaseX(): Float = stemBaseX
    fun getStemBaseY(): Float = stemBaseY
    fun getLastForce(): Float = lastForce
    fun getBaseThickness(): Float = baseThickness
    fun getTipThickness(): Float = tipThickness
    fun getGrowthRate(): Float = growthRate
    fun getOscillationDecay(): Float = oscillationDecay
    
    // ==================== SETTERS POUR GROWTHMANAGER ====================
    
    fun setStemHeight(height: Float) {
        stemHeight = height
    }
    
    // ==================== FONCTIONS PRIVÉES EXISTANTES ====================
    
    private fun initializeBranchOrder() {
        branchCreationOrder = (1..maxBranches).toMutableList()
        branchCreationOrder.shuffle()
    }
    
    private fun createBranchInOrder() {
        if (branchCount >= maxBranches || branchCount >= branchCreationOrder.size) return
        
        val branchNumber = branchCreationOrder[branchCount]
        createBranch(branchNumber)
    }
    
    private fun createEmergenceStem(progress: Float) {
        mainStem.clear()
        val emergenceHeight = 30f * progress
        
        for (i in 0..5) {
            val segmentProgress = i / 5f
            val y = stemBaseY - emergenceHeight * segmentProgress
            val thickness = lerp(baseThickness, tipThickness, segmentProgress * 0.3f)
            val wiggle = sin(progress * PI * 3 + i * 0.5) * 0.5f * progress
            
            mainStem.add(StemPoint(stemBaseX + wiggle.toFloat(), y, thickness))
        }
        
        if (progress >= 1f) {
            stemHeight = emergenceHeight
        }
    }
    
    private fun createBranch(branchNumber: Int) {
        branchCount++
        
        // ESPACEMENT AUGMENTÉ : Plus d'espace entre les tiges
        val baseSpacing = 85f // Augmenté de 50f à 85f (+70% d'espace)
        
        val isLeft: Boolean
        val position: Float
        val heightRange: Pair<Float, Float>
        val thickness: Float
        
        when (branchNumber) {
            1 -> {
                isLeft = false
                position = baseSpacing
                heightRange = Pair(0.85f, 0.95f) // TOUJOURS plus haute que principale (0.75f)
                thickness = 0.90f
            }
            2 -> {
                isLeft = true  
                position = -baseSpacing
                heightRange = Pair(0.80f, 0.90f) // Plus haute que principale
                thickness = 0.85f
            }
            3 -> {
                isLeft = false
                position = baseSpacing * 2
                heightRange = Pair(0.78f, 0.88f) // Plus haute que principale
                thickness = 0.80f
            }
            4 -> {
                isLeft = true
                position = -baseSpacing * 2
                heightRange = Pair(0.78f, 0.88f) // Plus haute que principale
                thickness = 0.80f
            }
            5 -> {
                isLeft = false
                position = baseSpacing * 3
                heightRange = Pair(0.76f, 0.86f) // Plus haute que principale
                thickness = 0.75f
            }
            6 -> {
                isLeft = true
                position = -baseSpacing * 3
                heightRange = Pair(0.76f, 0.86f) // Plus haute que principale
                thickness = 0.75f
            }
            else -> {
                isLeft = false
                position = baseSpacing
                heightRange = Pair(0.80f, 0.90f)
                thickness = 0.80f
            }
        }
        
        // GARANTIE : Au moins une tige sera plus haute que la principale
        val adjustedHeightRange = if (branchCount == 1) {
            // La PREMIÈRE branche est TOUJOURS plus haute que la principale
            Pair(0.85f, 0.95f) // Garantie d'être plus haute que 0.75f
        } else {
            heightRange
        }
        
        val forcedOffset = position + (Math.random().toFloat() * 15f - 7.5f) // Variation augmentée
        val forcedAngle = if (isLeft) -12f else +12f
        val baseHeightRatio = (adjustedHeightRange.first + Math.random().toFloat() * (adjustedHeightRange.second - adjustedHeightRange.first))
        val branchMaxHeight = maxPossibleHeight * baseHeightRatio
        val thicknessVar = thickness
        
        val personalityFactor = 0.95f
        val trembleFreq = 1.0f
        val curvatureDir = if (isLeft) -1f else 1f
        
        val newBranch = Branch(
            angle = forcedAngle,
            startHeight = 0f,
            baseOffset = forcedOffset,
            isMainStem = false,
            currentHeight = 0f,
            maxHeight = branchMaxHeight,
            personalityFactor = personalityFactor,
            trembleFrequency = trembleFreq,
            curvatureDirection = curvatureDir,
            thicknessVariation = thicknessVar
        )
        
        val startX = stemBaseX
        val startThickness = baseThickness * thicknessVar
        newBranch.points.add(StemPoint(startX, stemBaseY, startThickness))
        
        val divergenceForce = position + (Math.random().toFloat() * 30f - 15f) // Divergence augmentée
        
        val initialHeight = 12f
        val initialX = startX + divergenceForce
        val initialY = stemBaseY - initialHeight
        val initialThickness = startThickness * 0.95f
        
        newBranch.points.add(StemPoint(initialX, initialY, initialThickness))
        newBranch.currentHeight = initialHeight
        
        branches.add(newBranch)
        
        println("Tige ${branchNumber} (${branchCount}ème créée): ${if (isLeft) "GAUCHE" else "DROITE"} - Hauteur max: ${(baseHeightRatio * 100).toInt()}% (Principal: 75%)")
    }
    
    // ==================== UTILITAIRES ====================
    
    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + fraction * (end - start)
    }
}
