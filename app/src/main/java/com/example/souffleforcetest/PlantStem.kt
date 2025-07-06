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
    
    data class Leaf(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val angle: Float,
        val stemIndex: Int, // -1 pour tige principale, 0-5 pour branches
        val attachmentHeight: Float,
        val leafType: LeafType,
        var growthProgress: Float = 0f,
        val side: LeafSide,
        var oscillation: Float = 0f
    )
    
    enum class LeafType { BASAL_LARGE, STEM_MEDIUM, STEM_SMALL }
    enum class LeafSide { LEFT, RIGHT, CENTER }
    
    // ==================== VARIABLES PRINCIPALES ====================
    
    val mainStem = mutableListOf<StemPoint>()
    val branches = mutableListOf<Branch>()
    val leaves = mutableListOf<Leaf>()
    
    private var stemHeight = 0f
    private var maxPossibleHeight = 0f
    private val stemBaseX = screenWidth / 2f
    private val stemBaseY = screenHeight - 100f
    private var lastForce = 0f
    private var isEmerging = false
    private var emergenceStartTime = 0L
    private var branchSide = true
    private var branchCount = 0
    private var leavesCreated = false
    
    // Instance du gestionnaire de croissance - initialisation tardive
    private lateinit var growthManager: PlantGrowthManager
    
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
        if (!leavesCreated && stemHeight > 50f) {
            createAllLeaves()
            leavesCreated = true
        }
        
        growExistingLeaves(force)
        updateLeafOscillations()
    }
    
    fun resetStem() {
        mainStem.clear()
        branches.clear()
        leaves.clear()
        stemHeight = 0f
        lastForce = 0f
        isEmerging = false
        branchSide = true
        branchCount = 0
        leavesCreated = false
    }
    
    fun getStemHeight(): Float = stemHeight
    fun hasVisibleStem(): Boolean = mainStem.size >= 1
    
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
    
    // ==================== FONCTIONS PRIVÉES TIGES ====================
    
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
    
    // ==================== FONCTIONS FEUILLES ====================
    
    private fun createAllLeaves() {
        createBasalLeaves()
        createMainStemLeaves()
        createBranchLeaves()
    }
    
    private fun createBasalLeaves() {
        val baseX = stemBaseX
        val baseY = stemBaseY
        val basalCount = (4..6).random()
        
        for (i in 0 until basalCount) {
            val angle = (i * 360f / basalCount) + (-15..15).random()
            val distance = (35..55).random().toFloat()
            
            val radians = Math.toRadians(angle.toDouble())
            val leafX = baseX + (cos(radians) * distance).toFloat()
            val leafY = baseY - (5..15).random().toFloat()
            
            val leaf = Leaf(
                x = leafX,
                y = leafY,
                width = (45..65).random().toFloat(),
                height = (25..35).random().toFloat(),
                angle = angle + (-10..10).random(),
                stemIndex = -1,
                attachmentHeight = 0f,
                leafType = LeafType.BASAL_LARGE,
                side = when {
                    leafX < baseX - 10 -> LeafSide.LEFT
                    leafX > baseX + 10 -> LeafSide.RIGHT
                    else -> LeafSide.CENTER
                }
            )
            
            leaves.add(leaf)
        }
    }
    
    private fun createMainStemLeaves() {
        if (mainStem.size < 10) return
        
        val leafCount = (2..3).random()
        
        for (i in 0 until leafCount) {
            val heightRatio = (0.2f + i * 0.3f)
            val targetHeight = stemHeight * heightRatio
            
            val stemPoint = findStemPointAtHeight(mainStem, targetHeight) ?: continue
            
            val side = if (i % 2 == 0) LeafSide.LEFT else LeafSide.RIGHT
            val sideMultiplier = if (side == LeafSide.LEFT) -1f else 1f
            
            val leafX = stemPoint.x + (sideMultiplier * (15..25).random())
            val leafY = stemPoint.y - (2..8).random()
            
            val leaf = Leaf(
                x = leafX,
                y = leafY,
                width = (25..35).random().toFloat(),
                height = (15..22).random().toFloat(),
                angle = (sideMultiplier * (20..40).random()).toFloat(),
                stemIndex = -1,
                attachmentHeight = targetHeight,
                leafType = LeafType.STEM_MEDIUM,
                side = side
            )
            
            leaves.add(leaf)
        }
    }
    
    private fun createBranchLeaves() {
        for ((branchIndex, branch) in branches.withIndex()) {
            if (branch.points.size < 5) continue
            
            val leafCount = (1..2).random()
            val branchHeight = branch.currentHeight
            
            for (i in 0 until leafCount) {
                val heightRatio = (0.3f + i * 0.4f)
                val targetHeight = branchHeight * heightRatio
                
                val branchPoint = findStemPointAtHeight(branch.points, targetHeight) ?: continue
                
                val side = if (i % 2 == 0) LeafSide.LEFT else LeafSide.RIGHT
                val sideMultiplier = if (side == LeafSide.LEFT) -1f else 1f
                
                val branchDirection = if (branch.angle < 0) -1f else 1f
                val adjustedMultiplier = sideMultiplier * branchDirection * 0.7f
                
                val leafX = branchPoint.x + (adjustedMultiplier * (10..18).random())
                val leafY = branchPoint.y - (1..5).random()
                
                val leaf = Leaf(
                    x = leafX,
                    y = leafY,
                    width = (18..28).random().toFloat(),
                    height = (10..16).random().toFloat(),
                    angle = (adjustedMultiplier * (15..30).random()).toFloat(),
                    stemIndex = branchIndex,
                    attachmentHeight = targetHeight,
                    leafType = LeafType.STEM_SMALL,
                    side = side
                )
                
                leaves.add(leaf)
            }
        }
    }
    
    private fun growExistingLeaves(force: Float) {
        for (i in leaves.indices) {
            val leaf = leaves[i]
            
            if (leaf.growthProgress < 1f) {
                val growthSpeed = when (leaf.leafType) {
                    LeafType.BASAL_LARGE -> 0.015f
                    LeafType.STEM_MEDIUM -> 0.022f
                    LeafType.STEM_SMALL -> 0.030f
                }
                
                val forceMultiplier = 0.5f + (force * 0.5f)
                val growth = growthSpeed * forceMultiplier
                
                leaves[i] = leaf.copy(growthProgress = (leaf.growthProgress + growth).coerceAtMost(1f))
            }
        }
    }
    
    private fun updateLeafOscillations() {
        for (i in leaves.indices) {
            val leaf = leaves[i]
            
            val windStrength = 0.3f
            val timeOffset = i * 0.5f
            val sizeMultiplier = when (leaf.leafType) {
                LeafType.BASAL_LARGE -> 0.5f
                LeafType.STEM_MEDIUM -> 0.8f
                LeafType.STEM_SMALL -> 1.2f
            }
            
            val oscillation = sin((System.currentTimeMillis() * 0.001f) + timeOffset) * 
                             windStrength * sizeMultiplier * leaf.growthProgress
            
            leaves[i] = leaf.copy(oscillation = oscillation)
        }
    }
    
    private fun findStemPointAtHeight(points: List<StemPoint>, targetHeight: Float): StemPoint? {
        if (points.isEmpty()) return null
        
        val baseY = stemBaseY
        val targetY = baseY - targetHeight
        
        return points.minByOrNull { abs(it.y - targetY) }
    }
    
    // ==================== UTILITAIRES ====================
    
    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + fraction * (end - start)
    }
}
