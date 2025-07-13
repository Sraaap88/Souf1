package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import kotlin.math.*

// ==================== OPTIMISATION DE RENDU ====================

class LupinOptimizer(private val screenWidth: Int, private val screenHeight: Int) {
    
    // Marges pour éléments partiellement visibles
    private val marginTop = -200f
    private val marginBottom = screenHeight + 200f
    private val marginLeft = -200f
    private val marginRight = screenWidth + 200f
    
    fun isLeafVisible(leaf: LupinLeaf, stem: LupinStem): Boolean {
        val leafPosition = getLeafPosition(leaf, stem) ?: return false
        
        // Vérifier si la feuille (avec sa taille) est dans les limites étendues
        val leafRadius = leaf.currentSize * 0.5f
        return leafPosition.x + leafRadius >= marginLeft && 
               leafPosition.x - leafRadius <= marginRight &&
               leafPosition.y + leafRadius >= marginTop && 
               leafPosition.y - leafRadius <= marginBottom
    }
    
    fun isStemVisible(stem: LupinStem): Boolean {
        if (stem.points.isEmpty()) return false
        
        // Vérifier si au moins une partie de la tige est visible
        for (point in stem.points) {
            if (point.x >= marginLeft && point.x <= marginRight &&
                point.y >= marginTop && point.y <= marginBottom) {
                return true
            }
        }
        return false
    }
    
    fun isFlowerVisible(flower: LupinFlower): Boolean {
        val flowerRadius = flower.currentSize * 0.5f
        return flower.x + flowerRadius >= marginLeft && 
               flower.x - flowerRadius <= marginRight &&
               flower.y + flowerRadius >= marginTop && 
               flower.y - flowerRadius <= marginBottom
    }
    
    private fun getLeafPosition(leaf: LupinLeaf, stem: LupinStem): StemPoint? {
        return if (leaf.isBasalShoot) {
            getBasalShootLeafPosition(leaf, stem)
        } else {
            getMainStemLeafPosition(leaf, stem)
        }
    }
    
    private fun getMainStemLeafPosition(leaf: LupinLeaf, stem: LupinStem): StemPoint? {
        if (stem.points.size < 2) return null
        
        val targetHeight = stem.currentHeight * leaf.heightRatio
        var currentHeight = 0f
        
        for (i in 1 until stem.points.size) {
            val p1 = stem.points[i-1]
            val p2 = stem.points[i]
            val segmentHeight = abs(p2.y - p1.y)
            
            if (currentHeight + segmentHeight >= targetHeight) {
                val ratio = (targetHeight - currentHeight) / segmentHeight
                val stemX = p1.x + (p2.x - p1.x) * ratio
                val stemY = p1.y + (p2.y - p1.y) * ratio
                
                val angleRad = Math.toRadians(leaf.angle.toDouble())
                val leafX = stemX + cos(angleRad).toFloat() * 30f
                val leafY = stemY + sin(angleRad).toFloat() * 30f
                
                return StemPoint(leafX, leafY, 0f)
            }
            currentHeight += segmentHeight
        }
        return null
    }
    
    private fun getBasalShootLeafPosition(leaf: LupinLeaf, stem: LupinStem): StemPoint? {
        if (leaf.basalShootIndex >= stem.basalShoots.size) return null
        
        val basalShoot = stem.basalShoots[leaf.basalShootIndex]
        if (basalShoot.points.size < 2) return null
        
        val targetHeight = basalShoot.currentHeight * leaf.heightRatio
        val angleRad = Math.toRadians(basalShoot.angle.toDouble())
        
        val shootX = basalShoot.baseX + (sin(angleRad) * targetHeight).toFloat()
        val shootY = basalShoot.baseY - (cos(angleRad) * targetHeight * 0.3f).toFloat()
        
        val leafAngleRad = Math.toRadians(leaf.angle.toDouble())
        val leafX = shootX + cos(leafAngleRad).toFloat() * 25f
        val leafY = shootY + sin(leafAngleRad).toFloat() * 25f
        
        return StemPoint(leafX, leafY, 0f)
    }
    
    // Stats de performance
    private var totalLeaves = 0
    private var visibleLeaves = 0
    
    fun startFrame() {
        totalLeaves = 0
        visibleLeaves = 0
    }
    
    fun countLeaf(isVisible: Boolean) {
        totalLeaves++
        if (isVisible) visibleLeaves++
    }
    
    fun getOptimizationStats(): String {
        val culledPercent = if (totalLeaves > 0) ((totalLeaves - visibleLeaves) * 100 / totalLeaves) else 0
        return "Lupin: $visibleLeaves/$totalLeaves feuilles ($culledPercent% optimisées)"
    }
}

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

// ==================== CLASSE PRINCIPALE OPTIMISÉE ====================

class LupinManager(private val screenWidth: Int, private val screenHeight: Int) {
    
    // Variables principales
    private val stems = mutableListOf<LupinStem>()
    private val leaves = mutableListOf<LupinLeaf>()
    private val renderer = LupinRenderer() // Référence au renderer
    
    // NOUVEAU: Optimiseur de rendu
    private val optimizer = LupinOptimizer(screenWidth, screenHeight)
    
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
    
    // NOUVEAU: Fonction de rendu optimisée
    fun drawLupin(canvas: Canvas, stemPaint: Paint, leafPaint: Paint, flowerPaint: Paint, dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null) {
        optimizer.startFrame()
        
        // Filtrer les éléments visibles avant de les passer au renderer
        val visibleStems = stems.filter { optimizer.isStemVisible(it) }
        
        val visibleLeaves = leaves.filter { leaf ->
            val stem = stems.getOrNull(leaf.stemIndex)
            val isVisible = stem != null && optimizer.isLeafVisible(leaf, stem)
            optimizer.countLeaf(isVisible)
            isVisible
        }
        
        val visibleFlowers = stems.flatMap { stem ->
            stem.flowerSpike.flowers.filter { optimizer.isFlowerVisible(it) }
        }
        
        // Debug performance (optionnel - retirez en production)
        // println(optimizer.getOptimizationStats())
        
        // Passer seulement les éléments visibles au renderer
        renderer.drawLupinOptimized(canvas, stemPaint, leafPaint, flowerPaint, visibleStems, visibleLeaves, visibleFlowers, dissolveInfo)
    }
    
    // ==================== RESTE DU CODE INCHANGÉ ====================
    // [Tout le reste du code reste identique - système ordre aléatoire, création des tiges, etc.]
    
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
    
    // [Continuez avec tout le reste du code original - création des tiges, feuilles, fleurs, etc.]
    // Je n'ai mis que les parties modifiées pour l'optimisation
    
    private fun createMainStem() {
        val stemCount = 3 + (Math.random() * 4).toInt()
        val radius = 320f
        
        for (i in 0 until stemCount) {
            val angle = Math.random() * 2 * PI
            val distance = Math.random() * radius + 160f
            var stemX = baseX + (cos(angle) * distance).toFloat()
            var stemY = baseY + (Math.random().toFloat() - 0.5f) * 80f
            
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
    
    // [Inclure tout le reste des fonctions du LupinManager original]
    // Pour économiser l'espace, je n'ai inclus que les parties modifiées
}
