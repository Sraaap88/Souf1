package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import kotlin.math.*

class LupinManager(private val screenWidth: Int, private val screenHeight: Int) {
    
    // ==================== DATA CLASSES ====================
    
    data class LupinStem(
        val points: MutableList<StemPoint> = mutableListOf(),
        var currentHeight: Float = 0f,
        val maxHeight: Float,
        val baseX: Float,
        val baseY: Float,
        var isActive: Boolean = true,
        val id: String = generateStemId(),
        val growthSpeedMultiplier: Float = generateRandomGrowthSpeed(),
        val flowerSpike: FlowerSpike = FlowerSpike()
    )
    
    data class StemPoint(
        val x: Float,
        val y: Float,
        val thickness: Float
    )
    
    data class FlowerSpike(
        val flowers: MutableList<LupinFlower> = mutableListOf(),
        var currentLength: Float = 0f,
        val maxLength: Float = 150f,
        var hasStartedBlooming: Boolean = false
    )
    
    data class LupinFlower(
        val x: Float,
        val y: Float,
        val positionOnSpike: Float,
        var currentSize: Float = 0f,
        val maxSize: Float,
        val color: FlowerColor,
        val id: String = generateFlowerId()
    )
    
    data class LupinLeaf(
        val stemIndex: Int,
        val heightRatio: Float,
        var currentSize: Float = 0f,
        val maxSize: Float,
        val angle: Float,
        val folioleCount: Int = 5 + (Math.random() * 4).toInt(),
        val folioleAngles: List<Float> = generateFolioleAngles()
    ) {
        companion object {
            private fun generateFolioleAngles(): List<Float> {
                return (0..8).map { Math.random().toFloat() * 30f - 15f }
            }
        }
    }
    
    enum class FlowerColor(val rgb: IntArray) {
        PURPLE(intArrayOf(138, 43, 226)),
        BLUE(intArrayOf(65, 105, 225)),
        PINK(intArrayOf(255, 20, 147)),
        WHITE(intArrayOf(248, 248, 255)),
        YELLOW(intArrayOf(255, 215, 0))
    }
    
    // ==================== VARIABLES PRINCIPALES ====================
    
    private val stems = mutableListOf<LupinStem>()
    private val leaves = mutableListOf<LupinLeaf>()
    
    private var baseX = 0f
    private var baseY = 0f
    private var lastForce = 0f
    
    private var challengeManager: ChallengeManager? = null
    
    // ==================== SYSTÈME ORDRE ALÉATOIRE ====================
    
    private var saccadeCount = 0
    private var isCurrentlyBreathing = false
    private var lastSaccadeTime = 0L
    private val saccadeCooldown = 300L
    private val breathStartThreshold = 0.3f
    private val breathEndThreshold = 0.2f
    
    private var stemOrderPool = mutableListOf<Int>()
    private var currentActiveStemIndex = -1
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.15f
    private val maxStemHeight = 0.6f
    private val baseThickness = 13.1f
    private val tipThickness = 4.2f
    private val growthRate = 7200f
    private val maxBranches = 21
    
    private val baseLeafSize = 125f
    private val baseFlowerSize = 40f
    private val flowerDensity = 12
    
    // Instance du renderer
    private val renderer = LupinRenderer()
    
    init {
        setupRandomStemOrder()
    }
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun setChallengeManager(manager: ChallengeManager) {
        challengeManager = manager
    }
    
    fun initialize(centerX: Float, bottomY: Float) {
        baseX = centerX
        baseY = bottomY
        
        if (stems.isEmpty()) {
            createMainStem()
        }
    }
    
    fun processStemGrowth(force: Float) {
        if (stems.isEmpty()) {
            createMainStem()
            
            if (force > forceThreshold) {
                saccadeCount = 1
                currentActiveStemIndex = 0
                lastSaccadeTime = System.currentTimeMillis()
                isCurrentlyBreathing = true
            }
        }
        
        detectSaccadesAndActivateStems(force, System.currentTimeMillis())
        
        if (force > forceThreshold && currentActiveStemIndex >= 0) {
            growOnlyActiveStem(force)
        }
        
        lastForce = force
    }
    
    fun processLeavesGrowth(force: Float) {
        createLeavesOnStems()
        growExistingLeaves(force)
    }
    
    fun processFlowerGrowth(force: Float) {
        createFlowerSpikes()
        growExistingFlowers(force)
    }
    
    fun reset() {
        stems.clear()
        leaves.clear()
        lastForce = 0f
        
        saccadeCount = 0
        isCurrentlyBreathing = false
        lastSaccadeTime = 0L
        currentActiveStemIndex = -1
        setupRandomStemOrder()
    }
    
    fun drawLupin(canvas: Canvas, stemPaint: Paint, leafPaint: Paint, flowerPaint: Paint) {
        renderer.drawStems(canvas, stemPaint, stems)
        renderer.drawLeaves(canvas, leafPaint, leaves, stems)
        renderer.drawFlowerSpikes(canvas, flowerPaint, stems)
    }
    
    // ==================== GESTION DES TIGES ====================
    
    private fun setupRandomStemOrder() {
        stemOrderPool = mutableListOf(0, 1, 2, 3, 4, 5, 6)
        stemOrderPool.shuffle()
    }
    
    private fun detectSaccadesAndActivateStems(force: Float, currentTime: Long) {
        val wasBreathing = isCurrentlyBreathing
        val isNowBreathing = force > breathStartThreshold
        
        if (!wasBreathing && isNowBreathing) {
            if (currentTime - lastSaccadeTime > saccadeCooldown) {
                saccadeCount++
                lastSaccadeTime = currentTime
                isCurrentlyBreathing = true
                
                activateNextStemInOrder()
            }
        }
        
        if (wasBreathing && force < breathEndThreshold) {
            isCurrentlyBreathing = false
        }
    }
    
    private fun activateNextStemInOrder() {
        if (saccadeCount <= stemOrderPool.size) {
            val groupTypeToActivate = stemOrderPool[saccadeCount - 1]
            currentActiveStemIndex = saccadeCount - 1
            
            if (groupTypeToActivate == 0) {
                println("Saccade $saccadeCount: Groupe PRINCIPAL activé")
            } else {
                println("Saccade $saccadeCount: Nouveau groupe $groupTypeToActivate créé")
                createNewStemGroup(groupTypeToActivate)
            }
        }
    }
    
    private fun createMainStem() {
        val stemCount = 2 + (Math.random() * 4).toInt()
        val safeMargin = screenWidth * 0.15f
        
        for (i in 0 until stemCount) {
            val stemX = safeMargin + Math.random().toFloat() * (screenWidth - 2 * safeMargin)
            val stemY = baseY + (Math.random().toFloat() - 0.5f) * 30f
            
            val stem = LupinStem(
                maxHeight = screenHeight * maxStemHeight * (0.8f + Math.random().toFloat() * 0.4f),
                baseX = stemX,
                baseY = stemY,
                growthSpeedMultiplier = 0.7f + Math.random().toFloat() * 0.6f
            )
            stem.points.add(StemPoint(stemX, stemY, baseThickness))
            stems.add(stem)
            challengeManager?.notifyLupinSpikeCreated("NEW_STEM", stem.id)
            
            createBasalStems(stemX, stemY, stem.id)
        }
    }
    
    private fun createNewStemGroup(groupNumber: Int) {
        val stemCount = 2 + (Math.random() * 4).toInt()
        val safeMargin = screenWidth * 0.15f
        
        for (i in 0 until stemCount) {
            val stemX = safeMargin + Math.random().toFloat() * (screenWidth - 2 * safeMargin)
            val stemY = baseY + (Math.random().toFloat() - 0.5f) * 40f
            
            val stem = LupinStem(
                maxHeight = screenHeight * maxStemHeight * (0.8f + Math.random().toFloat() * 0.4f),
                baseX = stemX,
                baseY = stemY,
                growthSpeedMultiplier = 0.7f + Math.random().toFloat() * 0.6f
            )
            stem.points.add(StemPoint(stemX, stemY, baseThickness))
            stems.add(stem)
            challengeManager?.notifyLupinSpikeCreated("NEW_STEM", stem.id)
            
            createBasalStems(stemX, stemY, stem.id)
        }
    }
    
    private fun createBasalStems(mainStemX: Float, mainStemY: Float, mainStemId: String) {
        for (i in 0..2) {
            val angle = (Math.random() - 0.5) * 60f
            val distance = 15f + Math.random().toFloat() * 10f
            
            val basalX = mainStemX + cos(Math.toRadians(angle.toDouble())).toFloat() * distance
            val basalY = mainStemY + (Math.random().toFloat() - 0.5f) * 8f
            
            val basalStem = LupinStem(
                maxHeight = 40f + Math.random().toFloat() * 20f,
                baseX = basalX,
                baseY = basalY,
                growthSpeedMultiplier = 1.2f + Math.random().toFloat() * 0.3f
            )
            basalStem.points.add(StemPoint(basalX, basalY, baseThickness * 0.6f))
            stems.add(basalStem)
        }
    }
    
    private fun growOnlyActiveStem(force: Float) {
        if (currentActiveStemIndex < 0) return
        
        val stemsInCurrentGroup = if (currentActiveStemIndex == 0) {
            val firstGroupSize = stems.indexOfFirst { it.maxHeight < 80f }
            if (firstGroupSize == -1) stems else stems.take(firstGroupSize)
        } else {
            val previousGroupsSize = calculatePreviousGroupsSize(currentActiveStemIndex - 1)
            val currentGroupStart = previousGroupsSize
            val currentGroupEnd = stems.indexOfFirst { stem ->
                stems.indexOf(stem) > currentGroupStart && stem.maxHeight < 80f
            }
            if (currentGroupEnd == -1) stems.drop(currentGroupStart) 
            else stems.subList(currentGroupStart, currentGroupEnd)
        }
        
        for (activeStem in stemsInCurrentGroup) {
            if (activeStem.currentHeight >= activeStem.maxHeight) continue
            
            val forceStability = 1f - abs(force - lastForce).coerceAtMost(0.5f) * 2f
            val qualityMultiplier = 0.5f + forceStability * 0.5f
            
            val growthProgress = activeStem.currentHeight / activeStem.maxHeight
            val progressCurve = 1f - growthProgress * growthProgress
            val adjustedGrowth = force * qualityMultiplier * progressCurve * growthRate * 0.008f * activeStem.growthSpeedMultiplier
            
            if (adjustedGrowth > 0) {
                activeStem.currentHeight += adjustedGrowth
                
                val lastPoint = activeStem.points.lastOrNull() ?: continue
                val segmentHeight = 7f + (Math.random() * 2f).toFloat()
                val segments = (adjustedGrowth / segmentHeight).toInt().coerceAtLeast(1)
                
                for (i in 1..segments) {
                    val currentHeight = activeStem.currentHeight - adjustedGrowth + (adjustedGrowth * i / segments)
                    val progressFromBase = currentHeight / activeStem.maxHeight
                    
                    val thicknessProgress = progressFromBase * 0.4f
                    val thickness = baseThickness * (1f - thicknessProgress)
                    
                    val currentX = activeStem.baseX + (Math.random().toFloat() - 0.5f) * 3f
                    val currentY = baseY - currentHeight
                    
                    val newPoint = StemPoint(currentX, currentY, thickness)
                    activeStem.points.add(newPoint)
                }
            }
        }
    }
    
    private fun calculatePreviousGroupsSize(groupIndex: Int): Int {
        return stems.count { it.maxHeight >= 80f }
    }
    
    // ==================== GESTION DES FEUILLES ====================
    
    private fun createLeavesOnStems() {
        for ((index, stem) in stems.withIndex()) {
            if (stem.points.size < 2) continue
            
            val existingLeaves = leaves.filter { it.stemIndex == index }
            if (existingLeaves.isNotEmpty()) continue
            
            if (stem.currentHeight < 30f) continue
            
            val leafCount = if (stem.maxHeight < 80f) {
                1 + (Math.random() * 2).toInt()
            } else {
                4 + (Math.random() * 2).toInt()
            }
            
            for (i in 0 until leafCount) {
                val heightRatio = if (stem.maxHeight < 80f) {
                    0.6f + (i.toFloat() / leafCount) * 0.4f
                } else {
                    if (i >= leafCount - 2) {
                        0.85f + (i - leafCount + 2) * 0.05f
                    } else {
                        0.3f + (i.toFloat() / (leafCount - 2)) * 0.5f
                    }
                }
                
                val size = baseLeafSize * (if (stem.maxHeight < 80f) 0.6f else 1f) + Math.random().toFloat() * 10f
                val angle = Math.random().toFloat() * 40f - 20f
                
                val leaf = LupinLeaf(
                    stemIndex = index,
                    heightRatio = heightRatio,
                    maxSize = size,
                    angle = angle
                )
                
                leaves.add(leaf)
            }
        }
    }
    
    private fun growExistingLeaves(force: Float) {
        for (leaf in leaves) {
            if (leaf.currentSize < leaf.maxSize && force > forceThreshold) {
                val growth = force * 800f * 0.008f
                leaf.currentSize = (leaf.currentSize + growth).coerceAtMost(leaf.maxSize)
            }
        }
    }
    
    // ==================== GESTION DES FLEURS ====================
    
    private fun createFlowerSpikes() {
        for (stem in stems) {
            if (stem.points.size < 2) continue
            if (stem.flowerSpike.hasStartedBlooming) continue
            
            if (stem.currentHeight > stem.maxHeight * 0.4f) {
                createFlowersOnSpike(stem)
                stem.flowerSpike.hasStartedBlooming = true
            }
        }
    }
    
    private fun createFlowersOnSpike(stem: LupinStem) {
        val topPoint = stem.points.lastOrNull() ?: return
        val spikeColor = FlowerColor.values().random()
        
        challengeManager?.notifyLupinSpikeCreated(spikeColor.name, stem.id)
        
        for (i in 0 until flowerDensity) {
            val positionOnSpike = i.toFloat() / (flowerDensity - 1)
            val yOffset = positionOnSpike * stem.flowerSpike.maxLength
            
            val flowerX = topPoint.x + (Math.random().toFloat() - 0.5f) * 4f
            val flowerY = topPoint.y - yOffset
            val flowerSize = baseFlowerSize + Math.random().toFloat() * 2f
            
            val flower = LupinFlower(
                x = flowerX,
                y = flowerY,
                positionOnSpike = positionOnSpike,
                maxSize = flowerSize,
                color = spikeColor
            )
            
            stem.flowerSpike.flowers.add(flower)
        }
    }
    
    private fun growExistingFlowers(force: Float) {
        for (stem in stems) {
            if (!stem.flowerSpike.hasStartedBlooming) continue
            
            for (flower in stem.flowerSpike.flowers) {
                if (flower.currentSize < flower.maxSize && force > forceThreshold) {
                    val growth = force * 600f * 0.008f
                    flower.currentSize = (flower.currentSize + growth).coerceAtMost(flower.maxSize)
                    
                    if (flower.currentSize >= flower.maxSize * 0.1f && flower.currentSize < flower.maxSize) {
                        challengeManager?.notifyFlowerCreated(flower.x, flower.y, flower.id)
                    }
                }
            }
        }
    }
    
    // ==================== UTILITAIRES ====================
    
    fun getStemPointAtHeight(stem: LupinStem, heightRatio: Float): StemPoint? {
        if (stem.points.size < 2) return null
        
        val targetHeight = stem.currentHeight * heightRatio
        var currentHeight = 0f
        
        for (i in 1 until stem.points.size) {
            val segmentHeight = stem.points[i-1].y - stem.points[i].y
            if (currentHeight + segmentHeight >= targetHeight) {
                val segmentRatio = (targetHeight - currentHeight) / segmentHeight
                val p1 = stem.points[i-1]
                val p2 = stem.points[i]
                
                val x = p1.x + (p2.x - p1.x) * segmentRatio
                val y = p1.y + (p2.y - p1.y) * segmentRatio
                val thickness = p1.thickness + (p2.thickness - p1.thickness) * segmentRatio
                
                return StemPoint(x, y, thickness)
            }
            currentHeight += segmentHeight
        }
        
        return stem.points.lastOrNull()
    }
    
    companion object {
        private var stemIdCounter = 0
        private var flowerIdCounter = 0
        
        private fun generateStemId(): String {
            stemIdCounter++
            return "lupinstem_$stemIdCounter"
        }
        
        private fun generateFlowerId(): String {
            flowerIdCounter++
            return "lupinflower_$flowerIdCounter"
        }
        
        private fun generateRandomGrowthSpeed(): Float {
            val variation = 0.1f
            return 1.0f + (Math.random().toFloat() - 0.5f) * 2 * variation
        }
    }
}
