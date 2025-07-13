package com.example.souffleforcetest

import kotlin.math.*

// ==================== OPTIMISATION DE RENDU POUR ROSIER ====================

class RoseOptimizer(private val screenWidth: Int, private val screenHeight: Int) {
    
    // Marges pour éléments partiellement visibles
    private val marginTop = -150f
    private val marginBottom = screenHeight + 150f
    private val marginLeft = -150f
    private val marginRight = screenWidth + 150f
    
    fun isBranchVisible(branch: RoseBushManager.RoseBranch): Boolean {
        if (branch.points.isEmpty()) return false
        
        // Vérifier si au moins une partie de la branche est visible
        for (point in branch.points) {
            if (point.x >= marginLeft && point.x <= marginRight &&
                point.y >= marginTop && point.y <= marginBottom) {
                return true
            }
        }
        return false
    }
    
    fun isLeafVisible(leaf: RoseBushManager.RoseLeaf, branches: List<RoseBushManager.RoseBranch>): Boolean {
        if (leaf.branchIndex >= branches.size) return false
        
        val branch = branches[leaf.branchIndex]
        val leafPosition = getLeafPosition(leaf, branch) ?: return false
        
        val leafRadius = leaf.currentSize * 0.5f
        return leafPosition.x + leafRadius >= marginLeft && 
               leafPosition.x - leafRadius <= marginRight &&
               leafPosition.y + leafRadius >= marginTop && 
               leafPosition.y - leafRadius <= marginBottom
    }
    
    fun isFlowerVisible(flower: RoseBushManager.RoseFlower): Boolean {
        val flowerRadius = flower.currentSize * 0.5f
        return flower.x + flowerRadius >= marginLeft && 
               flower.x - flowerRadius <= marginRight &&
               flower.y + flowerRadius >= marginTop && 
               flower.y - flowerRadius <= marginBottom
    }
    
    private fun getLeafPosition(leaf: RoseBushManager.RoseLeaf, branch: RoseBushManager.RoseBranch): RoseBushManager.BranchPoint? {
        if (branch.points.size < 2) return null
        
        val targetLength = branch.currentLength * leaf.positionRatio
        var currentLength = 0f
        
        for (i in 1 until branch.points.size) {
            val p1 = branch.points[i-1]
            val p2 = branch.points[i]
            val segmentLength = distance(p1, p2)
            
            if (currentLength + segmentLength >= targetLength) {
                val segmentRatio = (targetLength - currentLength) / segmentLength
                val branchX = p1.x + (p2.x - p1.x) * segmentRatio
                val branchY = p1.y + (p2.y - p1.y) * segmentRatio
                
                // Calculer la position de la feuille selon son côté et angle
                val leafOffset = 25f * leaf.side // Côté de la feuille
                val angleOffset = Math.toRadians(leaf.angle.toDouble())
                val leafX = branchX + cos(angleOffset).toFloat() * leafOffset
                val leafY = branchY + sin(angleOffset).toFloat() * leafOffset
                
                return RoseBushManager.BranchPoint(leafX, leafY, 0f)
            }
            currentLength += segmentLength
        }
        return null
    }
    
    private fun distance(p1: RoseBushManager.BranchPoint, p2: RoseBushManager.BranchPoint): Float {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        return sqrt(dx * dx + dy * dy)
    }
    
    // Stats de performance
    private var totalLeaves = 0
    private var visibleLeaves = 0
    private var totalBranches = 0
    private var visibleBranches = 0
    
    fun startFrame() {
        totalLeaves = 0
        visibleLeaves = 0
        totalBranches = 0
        visibleBranches = 0
    }
    
    fun countLeaf(isVisible: Boolean) {
        totalLeaves++
        if (isVisible) visibleLeaves++
    }
    
    fun countBranch(isVisible: Boolean) {
        totalBranches++
        if (isVisible) visibleBranches++
    }
    
    fun getOptimizationStats(): String {
        val leafCulledPercent = if (totalLeaves > 0) ((totalLeaves - visibleLeaves) * 100 / totalLeaves) else 0
        val branchCulledPercent = if (totalBranches > 0) ((totalBranches - visibleBranches) * 100 / totalBranches) else 0
        return "Rose: $visibleLeaves/$totalLeaves feuilles, $visibleBranches/$totalBranches branches (${leafCulledPercent}%/${branchCulledPercent}% optimisé)"
    }
}

class RoseBushManager(private val screenWidth: Int, private val screenHeight: Int) {
    
    // ==================== DATA CLASSES ====================
    
    data class RoseBranch(
        val points: MutableList<BranchPoint> = mutableListOf(),
        var currentLength: Float = 0f,
        val maxLength: Float,
        val angle: Float,
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
    
    // NOUVEAU: Optimiseur de rendu
    private val optimizer = RoseOptimizer(screenWidth, screenHeight)
    
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
    
    // NOUVEAU: Fonction de rendu optimisée
    fun drawRoseBush(
        canvas: android.graphics.Canvas, 
        branchPaint: android.graphics.Paint, 
        leafPaint: android.graphics.Paint, 
        flowerPaint: android.graphics.Paint, 
        dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null
    ) {
        optimizer.startFrame()
        
        // Filtrer les éléments visibles
        val visibleBranches = branches.filter { branch ->
            val isVisible = optimizer.isBranchVisible(branch)
            optimizer.countBranch(isVisible)
            isVisible
        }
        
        val visibleLeaves = leaves.filter { leaf ->
            val isVisible = optimizer.isLeafVisible(leaf, branches)
            optimizer.countLeaf(isVisible)
            isVisible
        }
        
        val visibleFlowers = flowers.filter { optimizer.isFlowerVisible(it) }
        
        // Debug performance (optionnel - retirez en production)
        // println(optimizer.getOptimizationStats())
        
        // Passer seulement les éléments visibles au renderer
        renderer.drawRoseBushOptimized(canvas, branchPaint, leafPaint, flowerPaint, visibleBranches, visibleLeaves, visibleFlowers, dissolveInfo)
    }
    
    // ==================== GETTERS POUR LE RENDERER ====================
    
    fun getBranches(): List<RoseBranch> = branches
    fun getLeaves(): List<RoseLeaf> = leaves
    fun getFlowers(): List<RoseFlower> = flowers
    
    // ==================== RESTE DU CODE INCHANGÉ ====================
    // [Tout le reste du code reste identique]
    
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
    
    // [Inclure toutes les autres fonctions du RoseBushManager original]
    // Pour économiser l'espace, je n'ai inclus que les parties modifiées
    
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
