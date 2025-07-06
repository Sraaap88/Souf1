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
    
    // Gestionnaires des composants
    private lateinit var growthManager: PlantGrowthManager
    private lateinit var leavesManager: PlantLeavesManager
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.25f
    private val maxStemHeight = 0.8f
    private val baseThickness = 25f
    private val tipThickness = 8f
    private val growthRate = 2400f
    private val oscillationDecay = 0.98f
    private val branchThreshold = 0.18f
    private val emergenceDuration = 1000L
    private val maxBranches = 6
    
    init {
        maxPossibleHeight = screenHeight * maxStemHeight
        growthManager = PlantGrowthManager(this)
        leavesManager = PlantLeavesManager(this)
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
        
        // Phase de croissance normale - STRICTEMENT avec souffle actif
        if (force > forceThreshold && mainStem.isNotEmpty()) {
            if (force > forceThreshold * 1.5f) {
                growthManager.growMainStem(force)
                growthManager.growAllBranches(force)
                
                if (abs(force - lastForce) > 0.12f && stemHeight > 20f && branchCount < maxBranches) {
                    createBranch()
                }
            }
        }
        
        growthManager.updateOscillations()
        lastForce = force
    }
    
    fun processLeafGrowth(force: Float) {
        leavesManager.updateLeaves(force)
    }
    
    fun resetStem() {
        mainStem.clear()
        branches.clear()
        stemHeight = 0f
        lastForce = 0f
        isEmerging = false
        branchSide = true
        branchCount = 0
        leavesManager.resetLeaves()
    }
    
    fun getStemHeight(): Float = stemHeight
    fun hasVisibleStem(): Boolean = mainStem.size >= 1
    fun getLeaves() = leavesManager.getLeaves()
    
    // ==================== GETTERS POUR GROWTHMANAGER ====================
    
    fun getMaxPossibleHeight(): Float = maxPossibleHeight
    fun getStemBaseX(): Float = stemBaseX
    fun getStemBaseY(): Float = stemBaseY
    fun getLastForce(): Float = lastForce
    fun getBaseThickness(): Float = baseThickness
    fun getTipThickness(): Float = tipThickness
    fun getGrowthRate(): Float = growthRate
    fun getOscillationDecay(): Float = oscillationDecay
    
    fun setStemHeight(height: Float) {
        stemHeight = height
    }
    
    // ==================== FONCTIONS PRIVÉES ====================
    
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
    
    private fun createBranch() {
        branchCount++
        val baseSpacing = 50f
        
        val isLeft: Boolean
        val position: Float
        val heightRange: Pair<Float, Float>
        val thickness: Float
        
        when (branchCount) {
            1 -> {
                isLeft = false
                position = baseSpacing
                heightRange = Pair(0.75f, 0.85f)
                thickness = 0.90f
            }
            2 -> {
                isLeft = true  
                position = -baseSpacing
                heightRange = Pair(0.70f, 0.80f)
                thickness = 0.85f
            }
            3 -> {
                isLeft = false
                position = baseSpacing * 2
                heightRange = Pair(0.70f, 0.80f)
                thickness = 0.80f
            }
            4 -> {
                isLeft = true
                position = -baseSpacing * 2
                heightRange = Pair(0.70f, 0.80f)
                thickness = 0.80f
            }
            5 -> {
                isLeft = false
                position = baseSpacing * 3
                heightRange = Pair(0.65f, 0.75f)
                thickness = 0.75f
            }
            6 -> {
                isLeft = true
                position = -baseSpacing * 3
                heightRange = Pair(0.65f, 0.75f)
                thickness = 0.75f
            }
            else -> {
                isLeft = false
                position = baseSpacing
                heightRange = Pair(0.70f, 0.80f)
                thickness = 0.80f
            }
        }
        
        val forcedOffset = position + (Math.random().toFloat() * 10f - 5f)
        val forcedAngle = if (isLeft) -12f else +12f
        val baseHeightRatio = (heightRange.first + Math.random().toFloat() * (heightRange.second - heightRange.first))
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
        
        val divergenceForce = position + (Math.random().toFloat() * 20f - 10f)
        val initialHeight = 12f
        val initialX = startX + divergenceForce
        val initialY = stemBaseY - initialHeight
        val initialThickness = startThickness * 0.95f
        
        newBranch.points.add(StemPoint(initialX, initialY, initialThickness))
        newBranch.currentHeight = initialHeight
        
        branches.add(newBranch)
    }
    
    // ==================== UTILITAIRES ====================
    
    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + fraction * (end - start)
    }
}
