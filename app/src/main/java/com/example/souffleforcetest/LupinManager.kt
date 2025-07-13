package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import kotlin.math.*

// ==================== FONCTIONS UTILITAIRES GLOBALES ====================

private var stemIdCounter = 0
private var flowerIdCounter = 0

fun generateStemId(): String {
    stemIdCounter++
    return "lupinstem_$stemIdCounter"
}

fun generateFlowerId(): String {
    flowerIdCounter++
    return "lupinflower_$flowerIdCounter"
}

fun generateRandomGrowthSpeed(): Float {
    val variation = 0.1f
    return 1.0f + (Math.random().toFloat() - 0.5f) * 2 * variation
}

fun generateFolioleAngles(): List<Float> {
    return (0..8).map { Math.random().toFloat() * 30f - 15f }
}

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
    val flowerSpike: FlowerSpike = FlowerSpike(),
    val basalShoots: MutableList<BasalShoot> = mutableListOf()
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

data class BasalShoot(
    val points: MutableList<StemPoint> = mutableListOf(),
    var currentHeight: Float = 0f,
    val maxHeight: Float,
    val baseX: Float,
    val baseY: Float,
    val angle: Float,
    val id: String = generateStemId()
)

data class LupinLeaf(
    val stemIndex: Int,
    val heightRatio: Float,
    var currentSize: Float = 0f,
    val maxSize: Float,
    val angle: Float,
    val folioleCount: Int = 5 + (Math.random() * 4).toInt(),
    val folioleAngles: List<Float> = generateFolioleAngles(),
    val isBasalShoot: Boolean = false,
    val basalShootIndex: Int = -1,
    val isSubFloral: Boolean = false
)

enum class FlowerColor(val rgb: IntArray) {
    PURPLE(intArrayOf(138, 43, 226)),
    BLUE(intArrayOf(65, 105, 225)),
    PINK(intArrayOf(255, 20, 147)),
    WHITE(intArrayOf(248, 248, 255)),
    YELLOW(intArrayOf(255, 215, 0))
}

// ==================== CLASSE PRINCIPALE ====================

class LupinManager(private val screenWidth: Int, private val screenHeight: Int) {
    
    // Variables principales
    private val stems = mutableListOf<LupinStem>()
    private val leaves = mutableListOf<LupinLeaf>()
    private val renderer = LupinRenderer() // Référence au renderer
    
    private var baseX = 0f
    private var baseY = 0f
    private var lastForce = 0f
    private var challengeManager: ChallengeManager? = null
    
    // Système ordre aléatoire
    private var saccadeCount = 0
    private var isCurrentlyBreathing = false
    private var lastSaccadeTime = 0L
    private val saccadeCooldown = 300L
    private val breathStartThreshold = 0.3f
    private val breathEndThreshold = 0.2f
    
    private var stemOrderPool = mutableListOf<Int>()
    private var currentActiveStemIndex = -1
    
    // Paramètres de croissance
    private val forceThreshold = 0.15f
    private val maxStemHeight = 0.5f // Limité à 50% de l'écran
    private val baseThickness = 13.1f
    private val tipThickness = 4.2f
    private val growthRate = 7200f
    private val maxBranches = 21
    
    private val baseLeafSize = 125f
    private val baseFlowerSize = 40f
    private val flowerDensity = 12
    
    // Paramètres pour petites tiges basales
    private val basalShootCount = 3
    private val basalShootMaxHeight = 80f
    private val basalShootAngleSpread = 45f
    
    // Marges pour éviter les bords
    private val marginFromEdges = screenWidth * 0.15f
    
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
        
        growBasalShoots(force)
        lastForce = force
    }
    
    fun processLeavesGrowth(force: Float) {
        createLeavesOnStems()
        createLeavesOnBasalShoots()
        createSubFloralLeaves()
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
    
    fun drawLupin(canvas: Canvas, stemPaint: Paint, leafPaint: Paint, flowerPaint: Paint, dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null) {
        // CORRIGÉ: Passer dissolveInfo au LupinRenderer
        renderer.drawLupin(canvas, stemPaint, leafPaint, flowerPaint, stems, leaves, dissolveInfo)
    }
    
    // ==================== SYSTÈME ORDRE ALÉATOIRE ====================
    
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
    
    // ==================== CRÉATION DES TIGES ====================
    
    private fun createMainStem() {
        val stemCount = 3 + (Math.random() * 4).toInt() // 3 à 6 tiges
        val radius = 320f // 2X plus grand pour plus d'espacement
        
        for (i in 0 until stemCount) {
            val angle = Math.random() * 2 * PI
            val distance = Math.random() * radius + 160f // Distance minimale 2X plus grande
            var stemX = baseX + (cos(angle) * distance).toFloat()
            var stemY = baseY + (Math.random().toFloat() - 0.5f) * 80f // Plus de variation Y
            
            stemX = stemX.coerceIn(marginFromEdges, screenWidth - marginFromEdges)
            
            val heightVariation = 0.6f + Math.random().toFloat() * 0.8f
            val maxHeight = screenHeight * maxStemHeight * heightVariation
            
            val stem = LupinStem(
                maxHeight = maxHeight,
                baseX = stemX,
                baseY = stemY,
                growthSpeedMultiplier = 0.5f + Math.random().toFloat() * 1.0f
            )
            stem.points.add(StemPoint(stemX, stemY, baseThickness))
            stems.add(stem)
            
            createBasalShootsForStem(stem)
            challengeManager?.notifyLupinSpikeCreated("NEW_STEM", stem.id)
        }
    }
    
    private fun createNewStemGroup(groupNumber: Int) {
        val baseRadius = 480f + groupNumber * 160f  // 2X plus espacé
        val groupAngle = Math.random() * 2 * PI
        val groupDistance = Math.random() * baseRadius + 240f  // Distance minimale 2X plus grande
        
        var groupBaseX = baseX + (cos(groupAngle) * groupDistance).toFloat()
        var groupBaseY = baseY + (Math.random().toFloat() - 0.5f) * 120f  // Plus de variation Y
        
        groupBaseX = groupBaseX.coerceIn(marginFromEdges, screenWidth - marginFromEdges)
        
        val stemCount = 3 + (Math.random() * 4).toInt() // 3 à 6 tiges
        
        for (i in 0 until stemCount) {
            val localRadius = 320f + Math.random().toFloat() * 240f  // 2X plus espacé localement
            val localAngle = Math.random() * 2 * PI
            val localDistance = Math.random() * localRadius + 120f  // Distance minimale 2X plus grande
            
            var stemX = groupBaseX + (cos(localAngle) * localDistance).toFloat()
            var stemY = groupBaseY + (Math.random().toFloat() - 0.5f) * 100f
            
            stemX = stemX.coerceIn(marginFromEdges, screenWidth - marginFromEdges)
            
            val heightVariation = 0.6f + Math.random().toFloat() * 0.8f
            val maxHeight = screenHeight * maxStemHeight * heightVariation
            
            val stem = LupinStem(
                maxHeight = maxHeight,
                baseX = stemX,
                baseY = stemY,
                growthSpeedMultiplier = 0.4f + Math.random().toFloat() * 1.2f
            )
            stem.points.add(StemPoint(stemX, stemY, baseThickness))
            stems.add(stem)
            
            createBasalShootsForStem(stem)
            challengeManager?.notifyLupinSpikeCreated("NEW_STEM", stem.id)
        }
    }
    
    // ==================== PETITES TIGES BASALES ====================
    
    private fun createBasalShootsForStem(stem: LupinStem) {
        for (i in 0 until basalShootCount) {
            val angle = (i - 1) * (basalShootAngleSpread / (basalShootCount - 1)) + 
                       (Math.random().toFloat() - 0.5f) * 10f
            val shootHeight = basalShootMaxHeight * (0.7f + Math.random().toFloat() * 0.6f)
            
            val basalShoot = BasalShoot(
                maxHeight = shootHeight,
                baseX = stem.baseX,
                baseY = stem.baseY,
                angle = angle
            )
            basalShoot.points.add(StemPoint(stem.baseX, stem.baseY, baseThickness * 0.6f))
            stem.basalShoots.add(basalShoot)
        }
    }
    
    private fun growBasalShoots(force: Float) {
        if (force <= forceThreshold) return
        
        for (stem in stems) {
            for (basalShoot in stem.basalShoots) {
                if (basalShoot.currentHeight >= basalShoot.maxHeight) continue
                
                val growth = force * 300f * 0.008f
                basalShoot.currentHeight += growth
                
                if (growth > 0) {
                    val angleRad = Math.toRadians(basalShoot.angle.toDouble())
                    val newX = basalShoot.baseX + (sin(angleRad) * basalShoot.currentHeight).toFloat()
                    val newY = basalShoot.baseY - (cos(angleRad) * basalShoot.currentHeight * 0.3f).toFloat()
                    val thickness = baseThickness * 0.6f * (1f - basalShoot.currentHeight / basalShoot.maxHeight * 0.5f)
                    
                    basalShoot.points.add(StemPoint(newX, newY, thickness))
                }
            }
        }
    }
    
    // ==================== CROISSANCE DES TIGES ACTIVES ====================
    
    private fun growOnlyActiveStem(force: Float) {
        if (currentActiveStemIndex < 0) return
        
        val stemsInCurrentGroup = if (currentActiveStemIndex == 0) {
            val mainGroupSize = getMainGroupSize()
            stems.take(mainGroupSize)
        } else {
            val (startIndex, groupSize) = getGroupIndexAndSize(currentActiveStemIndex)
            stems.drop(startIndex).take(groupSize)
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
    
    private fun getMainGroupSize(): Int {
        var count = 0
        for (stem in stems) {
            if (stem.baseX >= marginFromEdges && stem.baseX <= screenWidth - marginFromEdges) {
                count++
            } else {
                break
            }
        }
        return count.coerceAtLeast(3).coerceAtMost(6)
    }
    
    private fun getGroupIndexAndSize(groupIndex: Int): Pair<Int, Int> {
        val mainGroupSize = getMainGroupSize()
        val startIndex = mainGroupSize + (groupIndex - 1) * 3
        return Pair(startIndex, (3 + (Math.random() * 4).toInt()).coerceAtMost(stems.size - startIndex))
    }
    
    // ==================== CRÉATION ET CROISSANCE DES FEUILLES ====================
    
    private fun createLeavesOnStems() {
        for ((index, stem) in stems.withIndex()) {
            if (stem.points.size < 2) continue
            
            val existingLeaves = leaves.filter { 
                it.stemIndex == index && !it.isBasalShoot && !it.isSubFloral 
            }
            if (existingLeaves.isNotEmpty()) continue
            
            if (stem.currentHeight < 40f) continue
            
            // Plus de feuilles pour les tiges hautes (5-7 feuilles)
            val leafCount = if (stem.currentHeight > stem.maxHeight * 0.6f) {
                5 + (Math.random() * 3).toInt() // 5 à 7 feuilles pour les hautes tiges
            } else {
                3 + (Math.random() * 2).toInt() // 3 à 4 feuilles pour les petites tiges
            }
            
            for (i in 0 until leafCount) {
                // Répartir les feuilles sur toute la hauteur de la tige
                val heightRatio = 0.2f + (i.toFloat() / leafCount) * 0.6f // De 20% à 80% de la hauteur
                val size = baseLeafSize + Math.random().toFloat() * 10f
                val angle = (Math.random().toFloat() - 0.5f) * 40f // Angle plus droit
                
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
    
    private fun createLeavesOnBasalShoots() {
        for ((stemIndex, stem) in stems.withIndex()) {
            for ((shootIndex, basalShoot) in stem.basalShoots.withIndex()) {
                if (basalShoot.points.size < 2) continue
                if (basalShoot.currentHeight < 20f) continue
                
                val existingLeaves = leaves.filter { 
                    it.isBasalShoot && it.stemIndex == stemIndex && it.basalShootIndex == shootIndex 
                }
                if (existingLeaves.isNotEmpty()) continue
                
                val leafCount = 1 + (Math.random() * 2).toInt()
                for (i in 0 until leafCount) {
                    val heightRatio = 0.5f + (i.toFloat() / leafCount) * 0.4f
                    val size = baseLeafSize * 2.0f + Math.random().toFloat() * 20f // 2X plus grandes
                    val angle = (Math.random().toFloat() - 0.5f) * 40f
                    
                    val leaf = LupinLeaf(
                        stemIndex = stemIndex,
                        heightRatio = heightRatio,
                        maxSize = size,
                        angle = angle,
                        isBasalShoot = true,
                        basalShootIndex = shootIndex
                    )
                    
                    leaves.add(leaf)
                }
            }
        }
    }
    
    private fun createSubFloralLeaves() {
        for ((stemIndex, stem) in stems.withIndex()) {
            if (!stem.flowerSpike.hasStartedBlooming) continue
            
            val existingSubFloralLeaves = leaves.filter { 
                it.stemIndex == stemIndex && it.isSubFloral 
            }
            if (existingSubFloralLeaves.isNotEmpty()) continue
            
            val leafCount = 2 + (Math.random() * 2).toInt()
            for (i in 0 until leafCount) {
                val heightRatio = 0.8f + (i.toFloat() / leafCount) * 0.15f
                val size = baseLeafSize * 0.8f + Math.random().toFloat() * 8f
                val angle = (Math.random().toFloat() - 0.5f) * 30f
                
                val leaf = LupinLeaf(
                    stemIndex = stemIndex,
                    heightRatio = heightRatio,
                    maxSize = size,
                    angle = angle,
                    isSubFloral = true
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
    
    // ==================== CRÉATION ET CROISSANCE DES FLEURS ====================
    
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
}
