package com.example.souffleforcetest

import kotlin.math.*

class RoseBushManager(private val screenWidth: Int, private val screenHeight: Int) {
    
    // ==================== DATA CLASSES ====================
    
    data class RoseBranch(
        val points: MutableList<BranchPoint> = mutableListOf(),
        var currentLength: Float = 0f,
        val maxLength: Float,
        val angle: Float,  // Angle de base de croissance
        var isActive: Boolean = true,
        val id: String = generateBranchId(),
        val growthSpeedMultiplier: Float = generateRandomGrowthSpeed()
    )
    
    data class BranchPoint(
        val x: Float,
        val y: Float,
        val thickness: Float
    )
    
    data class RoseLeaf(
        val branchIndex: Int,
        val positionRatio: Float,
        var currentSize: Float = 0f,
        val maxSize: Float,
        val angle: Float,
        val side: Int,
        val folioleCount: Int = 5 + (Math.random() * 3).toInt(),
        val folioleVariations: List<Float> = generateFolioleVariations()
    ) {
        companion object {
            private fun generateFolioleVariations(): List<Float> {
                return (0..7).map { Math.random().toFloat() }
            }
        }
    }
    
    data class RoseFlower(
        val branchIndex: Int,
        val x: Float,
        val y: Float,
        var currentSize: Float = 0f,
        val maxSize: Float,
        val id: String = generateFlowerId()
    )
    
    data class ScheduledSplit(
        val branchId: String,
        val scheduledTime: Long
    )
    
    // ==================== VARIABLES PRINCIPALES ====================
    
    private val branches = mutableListOf<RoseBranch>()
    private val leaves = mutableListOf<RoseLeaf>()
    private val flowers = mutableListOf<RoseFlower>()
    private val scheduledSplits = mutableListOf<ScheduledSplit>()
    
    private var baseX = 0f
    private var baseY = 0f
    private var lastForce = 0f
    private var lastSpikeTime = 0L
    
    // Référence au gestionnaire de défis
    private var challengeManager: ChallengeManager? = null
    
    // Renderer délégué
    private val renderer = RoseBushRenderer()
    
    // ==================== PARAMÈTRES ====================
    
    private val spikeThreshold = 0.4f
    private val spikeMinInterval = 300L
    private val secondSplitDelay = 500L
    private val branchGrowthRate = 1920f
    private val leafGrowthRate = 512f
    private val flowerGrowthRate = 320f
    
    // Tailles
    private val baseBranchThickness = 18.75f
    private val segmentLength = 30f
    private val baseLeafSize = 80f
    private val baseFlowerSize = 35f
    
    // Paramètres pour tige tortueuse naturelle
    private val tortuosityFactor = 8f
    private val tortuosityFrequency = 0.3f
    private val randomNoiseFactor = 5f
    
    // Paramètres pour séparations multiples
    private val threeWaySplitChance = 0.4f
    private val fourWaySplitChance = 0.0f
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun setChallengeManager(manager: ChallengeManager) {
        challengeManager = manager
    }
    
    fun initialize(centerX: Float, bottomY: Float) {
        baseX = centerX
        baseY = bottomY
        createInitialSplit(baseX, baseY, baseBranchThickness)
    }
    
    fun processStemGrowth(force: Float) {
        detectSpikeAndSplit(force)
        processScheduledSplits()
        growActiveBranches(force)
        lastForce = force
    }
    
    fun processLeavesGrowth(force: Float) {
        createLeavesOnBranches()
        growExistingLeaves(force)
    }
    
    fun processFlowerGrowth(force: Float) {
        createFlowersOnBranches()
        growExistingFlowers(force)
    }
    
    fun reset() {
        branches.clear()
        leaves.clear()
        flowers.clear()
        scheduledSplits.clear()
        lastForce = 0f
        lastSpikeTime = 0L
    }
    
    // NOUVEAU: Fonction de rendu déléguée
    fun drawRoseBush(
        canvas: android.graphics.Canvas, 
        branchPaint: android.graphics.Paint, 
        leafPaint: android.graphics.Paint, 
        flowerPaint: android.graphics.Paint, 
        dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null
    ) {
        renderer.drawRoseBush(canvas, branchPaint, leafPaint, flowerPaint, branches, leaves, flowers, dissolveInfo)
    }
    
    // ==================== GETTERS POUR LE RENDERER ====================
    
    fun getBranches(): List<RoseBranch> = branches
    fun getLeaves(): List<RoseLeaf> = leaves
    fun getFlowers(): List<RoseFlower> = flowers
    
    // ==================== DIVISION DES TIGES ====================
    
    private fun createInitialSplit(x: Float, y: Float, thickness: Float) {
        val baseAngle = -90f
        val branchCount = if (Math.random() < 0.6f) 3 else 4
        
        for (i in 0 until branchCount) {
            val branchAngle = calculateInitialBranchAngle(baseAngle, i, branchCount)
            
            val newBranch = RoseBranch(
                maxLength = screenHeight * 0.8f,
                angle = branchAngle
            )
            
            newBranch.points.add(BranchPoint(x, y, thickness))
            
            val angleRad = Math.toRadians(branchAngle.toDouble())
            val secondX = x + cos(angleRad).toFloat() * (segmentLength * 0.3f)
            val secondY = y + sin(angleRad).toFloat() * (segmentLength * 0.3f)
            newBranch.points.add(BranchPoint(secondX, secondY, thickness * 0.95f))
            newBranch.currentLength = segmentLength * 0.3f
            
            branches.add(newBranch)
        }
        
        challengeManager?.notifyDivisionCreated("initial_split_$branchCount")
    }
    
    private fun calculateInitialBranchAngle(baseAngle: Float, branchIndex: Int, totalBranches: Int): Float {
        return when (totalBranches) {
            3 -> {
                when (branchIndex) {
                    0 -> baseAngle - 20f
                    1 -> baseAngle + 2f
                    2 -> baseAngle + 25f
                    else -> baseAngle
                }
            }
            4 -> {
                when (branchIndex) {
                    0 -> baseAngle - 25f
                    1 -> baseAngle - 8f
                    2 -> baseAngle + 5f
                    3 -> baseAngle + 20f
                    else -> baseAngle
                }
            }
            else -> baseAngle
        }
    }
    
    private fun detectSpikeAndSplit(force: Float) {
        val currentTime = System.currentTimeMillis()
        
        val forceIncrease = force - lastForce
        val isSpike = forceIncrease > spikeThreshold && force > 0.4f
        val canSplit = currentTime - lastSpikeTime > spikeMinInterval
        
        if (isSpike && canSplit) {
            val eligibleBranches = branches.filter { it.isActive && it.currentLength > 80f }
            
            for (branch in eligibleBranches) {
                val newBranches = splitBranchMultiway(branch)
                
                if (newBranches.isNotEmpty()) {
                    scheduleSecondSplit(newBranches, currentTime)
                }
            }
            
            lastSpikeTime = currentTime
        }
    }
    
    private fun scheduleSecondSplit(newBranches: List<RoseBranch>, currentTime: Long) {
        val randomBranch = newBranches.random()
        
        val scheduledSplit = ScheduledSplit(
            branchId = randomBranch.id,
            scheduledTime = currentTime + secondSplitDelay
        )
        
        scheduledSplits.add(scheduledSplit)
    }
    
    private fun processScheduledSplits() {
        val currentTime = System.currentTimeMillis()
        val splitsToProcess = scheduledSplits.filter { it.scheduledTime <= currentTime }
        
        for (scheduledSplit in splitsToProcess) {
            val branch = branches.find { it.id == scheduledSplit.branchId && it.isActive }
            
            if (branch != null && branch.currentLength > 40f) {
                splitBranchMultiway(branch)
            }
        }
        
        scheduledSplits.removeAll(splitsToProcess)
    }
    
    private fun splitBranchMultiway(branch: RoseBranch): List<RoseBranch> {
        if (branch.points.size < 3) return emptyList()
        
        val splitPoint = branch.points.last()
        val baseAngle = getCurrentGrowthAngle(branch)
        
        val branchCount = when {
            Math.random() < fourWaySplitChance -> 4
            Math.random() < threeWaySplitChance -> 3
            else -> 2
        }
        
        val newBranches = mutableListOf<RoseBranch>()
        
        for (i in 0 until branchCount) {
            val branchAngle = calculateBranchAngle(baseAngle, i, branchCount)
            
            val newBranch = RoseBranch(
                maxLength = branch.maxLength,
                angle = branchAngle
            )
            
            newBranch.points.add(BranchPoint(splitPoint.x, splitPoint.y, splitPoint.thickness * 0.9f))
            
            val angleRad = Math.toRadians(branchAngle.toDouble())
            val secondX = splitPoint.x + cos(angleRad).toFloat() * (segmentLength * 0.3f)
            val secondY = splitPoint.y + sin(angleRad).toFloat() * (segmentLength * 0.3f)
            newBranch.points.add(BranchPoint(secondX, secondY, splitPoint.thickness * 0.88f))
            newBranch.currentLength = segmentLength * 0.3f
            
            newBranches.add(newBranch)
            branches.add(newBranch)
        }
        
        val divisionId = newBranches.joinToString("_") { "branch_${it.id}" }
        challengeManager?.notifyDivisionCreated("division_$divisionId")
        
        branch.isActive = false
        
        return newBranches
    }
    
    private fun calculateBranchAngle(baseAngle: Float, branchIndex: Int, totalBranches: Int): Float {
        return when (totalBranches) {
            2 -> {
                val spread = 15f
                baseAngle + if (branchIndex == 0) -spread else spread
            }
            3 -> {
                val spread = 12f
                val offset = 3f
                when (branchIndex) {
                    0 -> baseAngle - spread - offset
                    1 -> baseAngle + offset
                    2 -> baseAngle + spread + offset
                    else -> baseAngle
                }
            }
            4 -> {
                val spread = 10f
                val offset = 5f
                when (branchIndex) {
                    0 -> baseAngle - spread * 2 - offset
                    1 -> baseAngle - spread + offset
                    2 -> baseAngle + spread - offset
                    3 -> baseAngle + spread * 2 + offset
                    else -> baseAngle
                }
            }
            else -> baseAngle
        }
    }
    
    private fun getCurrentGrowthAngle(branch: RoseBranch): Float {
        val baseAngle = branch.angle
        val tortuosity = sin(branch.points.size * tortuosityFrequency) * tortuosityFactor
        val randomNoise = (Math.random().toFloat() - 0.5f) * randomNoiseFactor * 2f
        val currentAngle = baseAngle + tortuosity + randomNoise
        
        return currentAngle.coerceIn(-150f, -30f)
    }
    
    // ==================== CROISSANCE DES TIGES ====================
    
    private fun growActiveBranches(force: Float) {
        for (branch in branches.filter { it.isActive }) {
            if (force > 0.15f && branch.currentLength < branch.maxLength) {
                val baseGrowth = force * branchGrowthRate * 0.020f
                val individualGrowth = baseGrowth * branch.growthSpeedMultiplier
                branch.currentLength = (branch.currentLength + individualGrowth).coerceAtMost(branch.maxLength)
                
                if (branch.points.size >= 2 && branch.currentLength >= branch.points.size * segmentLength) {
                    val lastPoint = branch.points.last()
                    
                    val currentAngle = getCurrentGrowthAngle(branch)
                    val angleRad = Math.toRadians(currentAngle.toDouble())
                    
                    val newX = lastPoint.x + cos(angleRad).toFloat() * segmentLength
                    val newY = lastPoint.y + sin(angleRad).toFloat() * segmentLength
                    val newThickness = (lastPoint.thickness * 0.96f).coerceAtLeast(3f)
                    
                    if (newX >= 0 && newX <= screenWidth && newY >= 0 && newY <= screenHeight) {
                        branch.points.add(BranchPoint(newX, newY, newThickness))
                    } else {
                        branch.isActive = false
                    }
                }
                
                if (branch.currentLength >= branch.maxLength * 0.95f) {
                    branch.isActive = false
                }
            }
        }
    }
    
    // ==================== FEUILLES ====================
    
    private fun createLeavesOnBranches() {
        for ((index, branch) in branches.withIndex()) {
            if (branch.points.size < 3) continue
            
            val existingLeaves = leaves.filter { it.branchIndex == index }
            if (existingLeaves.isNotEmpty()) continue
            
            if (branch.currentLength < 90f) continue
            
            val leafCount = 3 + (Math.random() * 3).toInt()
            
            for (i in 0 until leafCount) {
                val positionRatio = 0.5f + (i.toFloat() / leafCount) * 0.4f
                val side = if (i % 2 == 0) -1 else 1
                val size = baseLeafSize + Math.random().toFloat() * 30f
                val angle = Math.random().toFloat() * 50f - 25f
                
                val leaf = RoseLeaf(
                    branchIndex = index,
                    positionRatio = positionRatio,
                    maxSize = size,
                    angle = angle,
                    side = side
                )
                
                leaves.add(leaf)
            }
        }
    }
    
    private fun growExistingLeaves(force: Float) {
        for (leaf in leaves) {
            if (leaf.currentSize < leaf.maxSize && force > 0.15f) {
                val growth = force * leafGrowthRate * 0.025f
                leaf.currentSize = (leaf.currentSize + growth).coerceAtMost(leaf.maxSize)
            }
        }
    }
    
    // ==================== FLEURS ====================
    
    private fun createFlowersOnBranches() {
        for ((index, branch) in branches.withIndex()) {
            if (branch.points.size < 2) continue
            
            val existingFlowers = flowers.filter { it.branchIndex == index }
            if (existingFlowers.isNotEmpty()) continue
            
            if (!branch.isActive && branch.currentLength > 40f) {
                val lastPoint = branch.points.last()
                val flowerSize = baseFlowerSize + Math.random().toFloat() * 10f
                
                val flower = RoseFlower(
                    branchIndex = index,
                    x = lastPoint.x,
                    y = lastPoint.y,
                    maxSize = flowerSize
                )
                
                flowers.add(flower)
                challengeManager?.notifyFlowerCreated(flower.x, flower.y, flower.id)
            }
        }
    }
    
    private fun growExistingFlowers(force: Float) {
        for (flower in flowers) {
            if (flower.currentSize < flower.maxSize && force > 0.15f) {
                val growth = force * flowerGrowthRate * 0.025f
                flower.currentSize = (flower.currentSize + growth).coerceAtMost(flower.maxSize)
            }
        }
    }
    
    // ==================== UTILITAIRES ====================
    
    fun getBranchPointAtRatio(branch: RoseBranch, ratio: Float): BranchPoint? {
        if (branch.points.size < 2) return null
        
        val targetLength = branch.currentLength * ratio
        var currentLength = 0f
        
        for (i in 1 until branch.points.size) {
            val segmentLength = distance(branch.points[i-1], branch.points[i])
            if (currentLength + segmentLength >= targetLength) {
                val segmentRatio = (targetLength - currentLength) / segmentLength
                val p1 = branch.points[i-1]
                val p2 = branch.points[i]
                
                val x = p1.x + (p2.x - p1.x) * segmentRatio
                val y = p1.y + (p2.y - p1.y) * segmentRatio
                val thickness = p1.thickness + (p2.thickness - p1.thickness) * segmentRatio
                
                return BranchPoint(x, y, thickness)
            }
            currentLength += segmentLength
        }
        
        return branch.points.lastOrNull()
    }
    
    private fun distance(p1: BranchPoint, p2: BranchPoint): Float {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        return sqrt(dx * dx + dy * dy)
    }
    
    // ==================== COMPANION OBJECT ====================
    
    companion object {
        private var branchIdCounter = 0
        private var flowerIdCounter = 0
        
        private fun generateBranchId(): String {
            branchIdCounter++
            return "rosebranch_$branchIdCounter"
        }
        
        private fun generateFlowerId(): String {
            flowerIdCounter++
            return "roseflower_$flowerIdCounter"
        }
        
        private fun generateRandomGrowthSpeed(): Float {
            val variation = 0.5f
            return 1.0f + (Math.random().toFloat() - 0.5f) * 2 * variation
        }
    }
}
