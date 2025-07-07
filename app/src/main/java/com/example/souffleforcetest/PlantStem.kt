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
    
    // Instance du gestionnaire de croissance - initialisation tardive
    private lateinit var growthManager: PlantGrowthManager
    
    // Instance du gestionnaire de feuilles
    private lateinit var leavesManager: PlantLeavesManager
    
    // Instance du gestionnaire de fleurs
    private lateinit var flowerManager: FlowerManager
    
    // NOUVEAU : Systèmes de contrôle avancé
    private lateinit var stemController: StemController
    private lateinit var curvatureModifier: CurvatureModifier
    
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
        
        // NOUVEAU : Initialiser les systèmes avancés
        stemController = StemController(this)
        curvatureModifier = CurvatureModifier(this)
        
        initializeBranchOrder()
    }
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun processStemGrowth(force: Float, phaseTime: Long) {
        // NOUVEAU SYSTÈME : Utiliser le StemController pour gérer la croissance
        
        // INITIALISATION FORCÉE : créer le point de base dès le premier appel
        if (mainStem.isEmpty() && !isEmerging) {
            isEmerging = true
            emergenceStartTime = System.currentTimeMillis()
            mainStem.add(StemPoint(stemBaseX, stemBaseY, baseThickness))
            
            // Initialiser le contrôleur de tiges
            stemController.startGrowthPhase(System.currentTimeMillis())
            curvatureModifier.initializeStem(-1) // Tige principale
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
        
        // NOUVEAU : Phase de croissance contrôlée par StemController
        if (force > forceThreshold && mainStem.isNotEmpty()) {
            // Laisser le StemController gérer la création et croissance des tiges
            stemController.processGrowth(force, System.currentTimeMillis())
            
            // Obtenir l'analyse du souffle pour les effets de courbure
            val analysis = stemController.getBreathAnalysis()
            
            // Appliquer les modifications de courbure basées sur les fréquences
            curvatureModifier.updateAllStems(
                frequency = analysis.avgFrequency,
                trend = analysis.frequencyTrend,
                force = force
            )
        }
        
        // Mise à jour des oscillations (système existant préservé)
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
        
        // NOUVEAU : Reset des systèmes avancés
        stemController = StemController(this)
        curvatureModifier.reset()
    }
    
    fun getStemHeight(): Float = stemHeight
    fun hasVisibleStem(): Boolean = mainStem.size >= 1
    
    fun getLeaves(): List<PlantLeavesManager.Leaf> = leavesManager.leaves
    fun getLeavesManager(): PlantLeavesManager = leavesManager
    fun getFlowers(): List<FlowerManager.Flower> = flowerManager.flowers
    fun getFlowerManager(): FlowerManager = flowerManager
    
    // NOUVEAU : Exposer le gestionnaire de croissance pour StemController
    val growthManager: PlantGrowthManager
        get() = if (::growthManager.isInitialized) growthManager else throw IllegalStateException("PlantGrowthManager not initialized")
    
    // NOUVEAU : Getters pour les nouveaux systèmes
    fun getStemController(): StemController = stemController
    fun getCurvatureModifier(): CurvatureModifier = curvatureModifier
    
    // NOUVEAU : Info de debug pour les nouveaux systèmes
    fun getAdvancedDebugInfo(): String {
        return if (::stemController.isInitialized && ::curvatureModifier.isInitialized) {
            "${stemController.debugInfo()} | ${curvatureModifier.debugInfo()}"
        } else {
            "Systèmes non initialisés"
        }
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
    
    // ==================== FONCTIONS POUR COMPATIBILITÉ STEMCONTROLLER ====================
    
    // Fonction accessible pour StemController
    fun createBranch(branchNumber: Int) {
        createBranchInternal(branchNumber)
    }
    
    private fun createBranchInternal(branchNumber: Int) {
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
        
        // NOUVEAU : Initialiser la courbure pour cette nouvelle branche
        curvatureModifier.initializeStem(branchCount - 1, forcedAngle / 12f)
        
        println("Tige ${branchNumber} (${branchCount}ème créée): ${if (isLeft) "GAUCHE" else "DROITE"} - Hauteur max: ${(baseHeightRatio * 100).toInt()}% (Principal: 75%)")
    }
    
    // ==================== FONCTIONS PRIVÉES EXISTANTES ====================
    
    private fun initializeBranchOrder() {
        branchCreationOrder = (1..maxBranches).toMutableList()
        branchCreationOrder.shuffle()
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
    
    // ==================== SUPPRESSION ANCIEN SYSTÈME ====================
    
    // L'ancien système de contrôle du souffle est remplacé par les nouveaux modules
    // Ces fonctions sont conservées pour compatibilité mais ne sont plus utilisées
    
    // ==================== UTILITAIRES ====================
    
    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + fraction * (end - start)
    }
}
