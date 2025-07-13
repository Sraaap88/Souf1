package com.example.souffleforcetest

import android.content.Context
import android.graphics.PointF
import kotlin.math.*
import kotlin.random.Random

data class IrisStem(
    val id: Int,
    val startPoint: PointF,
    var currentPoint: PointF,
    val baseAngle: Float,
    val growthSpeed: Float,
    val maxHeight: Float,
    val curvature: Float,
    var currentHeight: Float = 0f,
    var segments: MutableList<PointF> = mutableListOf(),
    var isGrowing: Boolean = true,
    var hasFlower: Boolean = false,
    var leaves: MutableList<IrisLeaf> = mutableListOf(),
    var lastGrowthTime: Long = 0L,
    var growthPhase: Int = 0,
    var branches: MutableList<IrisBranch> = mutableListOf()
)

data class IrisLeaf(
    val attachmentPoint: PointF,
    val angle: Float,
    val length: Float,
    val width: Float,
    var growthProgress: Float = 0f
)

data class IrisBranch(
    val parentStemId: Int,
    val branchPoint: PointF,
    val angle: Float,
    val length: Float,
    var currentLength: Float = 0f,
    var hasFlower: Boolean = false,
    var isGrowing: Boolean = true
)

data class IrisFlower(
    val stemId: Int,
    val position: PointF,
    var bloomProgress: Float = 0f,
    val id: String,
    var isFullyBloomed: Boolean = false,
    val sizeMultiplier: Float = 1f,
    val renderLayer: Int = Random.nextInt(100) // 0-29 = arrière, 30-99 = avant
)

class IrisManager(private val screenWidth: Int, private val screenHeight: Int) {
    
    private val stems = mutableListOf<IrisStem>()
    private val flowers = mutableListOf<IrisFlower>()
    private var nextStemId = 0
    private var challengeManager: ChallengeManager? = null
    
    // Système de saccades comme Lupin
    private var saccadeCount = 0
    private var isCurrentlyBreathing = false
    private var lastSaccadeTime = 0L
    private val saccadeCooldown = 300L
    private val breathStartThreshold = 0.3f
    private val breathEndThreshold = 0.2f
    
    private var stemGroupOrder = mutableListOf<Int>()
    private var currentActiveStemGroup = -1
    private var lastForce = 0f
    
    // Paramètres de croissance - AJUSTÉS
    private val forceThreshold = 0.15f
    private val baseGrowthSpeed = 6f
    private val maxStemHeight = 0.36f // 20% plus bas (0.45f → 0.36f)
    private val stemsPerGroup = 3
    private val maxGroups = 4
    
    // Marges pour éviter les bords
    private val marginFromEdges = screenWidth * 0.12f
    
    init {
        setupRandomStemOrder()
    }
    
    fun setChallengeManager(manager: ChallengeManager) {
        challengeManager = manager
    }
    
    fun initialize(centerX: Float, bottomY: Float) {
        // Ne rien faire ici - laisser processStemGrowth gérer
    }
    
    fun processStemGrowth(force: Float) {
        // EXACTEMENT comme le Lupin - créer le groupe principal seulement si vide
        if (stems.isEmpty()) {
            createMainStemGroup()
            
            // Activer le premier groupe seulement si on souffle
            if (force > forceThreshold) {
                saccadeCount = 1
                currentActiveStemGroup = 0
                lastSaccadeTime = System.currentTimeMillis()
                isCurrentlyBreathing = true
            }
        }
        
        // Détecter les saccades pour créer de nouveaux groupes
        detectSaccadesAndActivateGroups(force, System.currentTimeMillis())
        
        // Faire grandir le groupe actif seulement si force + groupe actif
        if (force > forceThreshold && currentActiveStemGroup >= 0) {
            growActiveGroup(force)
        }
        
        lastForce = force
    }
    
    private fun createMainStemGroup() {
        // DOUBLE de tiges (14-28 au lieu de 7-14)
        val stemCount = 14 + Random.nextInt(15) // 14 à 28 tiges
        val radius = 240f
        val centerX = screenWidth / 2f
        val centerY = screenHeight * 0.85f
        
        for (i in 0 until stemCount) {
            val angle = Random.nextFloat() * 2 * PI
            val distance = Random.nextFloat() * radius + 120f
            var stemX = centerX + (cos(angle) * distance).toFloat()
            var stemY = centerY + (Random.nextFloat() - 0.5f) * 60f
            
            stemX = stemX.coerceIn(marginFromEdges, screenWidth - marginFromEdges)
            
            val heightVariation = 0.8f + Random.nextFloat() * 0.6f
            val maxHeight = screenHeight * maxStemHeight * heightVariation // Hauteur restaurée
            
            val stem = IrisStem(
                id = nextStemId++,
                startPoint = PointF(stemX, stemY),
                currentPoint = PointF(stemX, stemY),
                baseAngle = Random.nextFloat() * 5f - 2.5f,
                growthSpeed = baseGrowthSpeed * 3.3f + Random.nextFloat() * 4.4f, // 10% plus vite (3f → 3.3f, 4f → 4.4f)
                maxHeight = maxHeight,
                curvature = Random.nextFloat() * 0.1f + 0.05f
            )
            stem.segments.add(PointF(stemX, stemY))
            stems.add(stem)
        }
    }
    
    fun processLeavesGrowth(force: Float) {
        if (force > forceThreshold) {
            // Créer feuilles du sol une seule fois par groupe de tiges
            createGroundLeavesForGroups()
            
            // Faire grandir toutes les feuilles existantes
            for (stem in stems) {
                growExistingLeaves(stem, force)
            }
        }
    }
    
    fun processFlowerGrowth(force: Float) {
        if (force > forceThreshold) {
            for (stem in stems) {
                // MOINS de fleurs malgré plus de tiges : 40% des tiges ont des fleurs (68% → 40%)
                // Seuil plus élevé aussi (25% → 35%)
                if (stem.currentHeight > stem.maxHeight * 0.35f && !stem.hasFlower) {
                    // 40% de chance au lieu de 68%
                    if (Random.nextFloat() < 0.40f) {
                        createFlowerOnStem(stem)
                    }
                }
            }
            growExistingFlowers(force)
        }
    }
    
    fun reset() {
        stems.clear()
        flowers.clear()
        nextStemId = 0
        saccadeCount = 0
        isCurrentlyBreathing = false
        lastSaccadeTime = 0L
        currentActiveStemGroup = -1
        setupRandomStemOrder()
    }
    
    private fun setupRandomStemOrder() {
        stemGroupOrder = mutableListOf(0, 1, 2, 3)
        stemGroupOrder.shuffle()
    }
    
    private fun detectSaccadesAndActivateGroups(force: Float, currentTime: Long) {
        val wasBreathing = isCurrentlyBreathing
        val isNowBreathing = force > breathStartThreshold
        
        if (!wasBreathing && isNowBreathing) {
            if (currentTime - lastSaccadeTime > saccadeCooldown) {
                saccadeCount++
                lastSaccadeTime = currentTime
                isCurrentlyBreathing = true
                activateNextGroup()
            }
        }
        
        if (wasBreathing && force < breathEndThreshold) {
            isCurrentlyBreathing = false
        }
    }
    
    private fun activateNextGroup() {
        if (saccadeCount <= stemGroupOrder.size) {
            val groupToActivate = stemGroupOrder[saccadeCount - 1]
            currentActiveStemGroup = saccadeCount - 1
            
            if (groupToActivate > 0) {
                createNewStemGroup(groupToActivate)
            }
        }
    }
    
    private fun createInitialStemGroup(centerX: Float, bottomY: Float) {
        // Créer 2-3 tiges droites élancées près du centre
        val stemCount = 2 + Random.nextInt(2) // 2 ou 3 tiges
        
        for (i in 0 until stemCount) {
            val angle = Random.nextFloat() * 10f - 5f // Léger angle
            val spacing = 40f * i - 20f // Espacement serré
            val stemX = (centerX + spacing).coerceIn(marginFromEdges, screenWidth - marginFromEdges)
            val heightVariation = 0.8f + Random.nextFloat() * 0.4f
            val maxHeight = screenHeight * maxStemHeight * heightVariation // Hauteur restaurée
            
            val stem = IrisStem(
                id = nextStemId++,
                startPoint = PointF(stemX, bottomY),
                currentPoint = PointF(stemX, bottomY),
                baseAngle = angle,
                growthSpeed = baseGrowthSpeed + Random.nextFloat() * 2f,
                maxHeight = maxHeight,
                curvature = Random.nextFloat() * 0.2f + 0.1f
            )
            
            stem.segments.add(PointF(stemX, bottomY))
            stems.add(stem)
        }
    }
    
    private fun createNewStemGroup(groupNumber: Int) {
        val centerX = screenWidth / 2f
        val bottomY = screenHeight * 0.85f
        val baseRadius = 180f + groupNumber * 120f
        val groupAngle = Random.nextFloat() * 2 * PI
        val groupDistance = Random.nextFloat() * baseRadius + 100f
        
        val groupBaseX = (centerX + cos(groupAngle).toFloat() * groupDistance)
            .coerceIn(marginFromEdges, screenWidth - marginFromEdges)
        val groupBaseY = bottomY + (Random.nextFloat() - 0.5f) * 80f
        
        // DOUBLE de tiges (8-14 au lieu de 4-7)
        val stemCount = 8 + Random.nextInt(7) // 8 à 14 tiges par groupe
        
        for (i in 0 until stemCount) {
            val angle = Random.nextFloat() * 8f - 4f
            val spacing = 50f * i - 25f
            val stemX = (groupBaseX + spacing).coerceIn(marginFromEdges, screenWidth - marginFromEdges)
            val heightVariation = 0.8f + Random.nextFloat() * 0.4f
            val maxHeight = screenHeight * maxStemHeight * heightVariation // Hauteur restaurée
            
            val stem = IrisStem(
                id = nextStemId++,
                startPoint = PointF(stemX, groupBaseY),
                currentPoint = PointF(stemX, groupBaseY),
                baseAngle = angle,
                growthSpeed = baseGrowthSpeed * 3.3f + Random.nextFloat() * 4.4f, // 10% plus vite
                maxHeight = maxHeight,
                curvature = Random.nextFloat() * 0.15f + 0.05f
            )
            
            stem.segments.add(PointF(stemX, groupBaseY))
            stems.add(stem)
        }
    }
    
    private fun growActiveGroup(force: Float) {
        // Groupes adaptés aux nouvelles quantités (DOUBLE)
        for (activeStemsInGroup in listOf(
            stems.take(28), // Premier groupe DOUBLE (jusqu'à 28 tiges)
            if (stems.size > 28) stems.drop(28).take(14) else emptyList(),
            if (stems.size > 42) stems.drop(42).take(14) else emptyList(),
            if (stems.size > 56) stems.drop(56) else emptyList()
        )) {
            for (activeStem in activeStemsInGroup) {
                if (activeStem.currentHeight >= activeStem.maxHeight) continue
                
                val forceStability = 1f - abs(force - lastForce).coerceAtMost(0.5f) * 2f
                val qualityMultiplier = 0.5f + forceStability * 0.5f
                
                val growthProgress = activeStem.currentHeight / activeStem.maxHeight
                val progressCurve = 1f - growthProgress * growthProgress
                
                val adjustedGrowth = force * qualityMultiplier * progressCurve * activeStem.growthSpeed * 5.28f // 10% plus vite (4.8f → 5.28f)
                
                if (adjustedGrowth > 0) {
                    activeStem.currentHeight += adjustedGrowth
                    
                    val lastPoint = activeStem.segments.lastOrNull() ?: continue
                    val angleRad = Math.toRadians(activeStem.baseAngle.toDouble())
                    val curvatureEffect = sin(growthProgress * PI).toFloat() * activeStem.curvature * 20f
                    
                    val newX = lastPoint.x + sin(angleRad).toFloat() * 2f + curvatureEffect
                    val newY = lastPoint.y - adjustedGrowth
                    
                    activeStem.currentPoint = PointF(newX, newY)
                    activeStem.segments.add(PointF(newX, newY))
                }
            }
        }
    }
    
    // NOUVEAU: Créer des feuilles du sol par groupe
    private fun createGroundLeavesForGroups() {
        if (stems.isEmpty()) return
        
        // Créer des feuilles pour chaque groupe de tiges
        val processedGroups = mutableSetOf<Int>()
        
        for ((index, stem) in stems.withIndex()) {
            val groupId = when {
                index < 16 -> 0  // Premier groupe (adapté au DOUBLE)
                else -> 1 + (index - 16) / 8  // Groupes suivants
            }
            
            if (!processedGroups.contains(groupId) && stem.currentHeight > 30f) {
                createGroundLeavesNearStem(stem)
                processedGroups.add(groupId)
            }
        }
    }
    
    private fun createGroundLeavesNearStem(stem: IrisStem) {
        if (stem.leaves.isNotEmpty()) return
        
        // 3-4 grandes feuilles partant du sol près de la tige
        val leafCount = 3 + Random.nextInt(2)
        
        for (i in 0 until leafCount) {
            // Position près du sol, légèrement décalée de la tige
            val offsetX = (Random.nextFloat() - 0.5f) * 80f
            val offsetY = Random.nextFloat() * 30f
            val leafBase = PointF(
                stem.startPoint.x + offsetX,
                stem.startPoint.y + offsetY
            )
            
            // Angles variés mais tous pointant vers le haut
            val leafAngle = Random.nextFloat() * 40f - 20f // -20° à +20°
            
            // Feuilles 30% PLUS hautes (augmentation demandée)
            val heightVariation = 0.6f + (i * 0.2f) + Random.nextFloat() * 0.3f
            val leafLength = 780f + Random.nextFloat() * 520f * heightVariation // 30% plus haut (600f → 780f, 400f → 520f)
            val leafWidth = 15f + Random.nextFloat() * 8f // Légèrement plus larges aussi
            
            val leaf = IrisLeaf(
                attachmentPoint = leafBase,
                angle = leafAngle,
                length = leafLength,
                width = leafWidth
            )
            
            stem.leaves.add(leaf)
        }
    }
    
    private fun growExistingLeaves(stem: IrisStem, force: Float) {
        for (leaf in stem.leaves) {
            if (leaf.growthProgress < 1f) {
                val growth = force * 0.025f // 50% moins vite (0.05f → 0.025f)
                leaf.growthProgress = (leaf.growthProgress + growth).coerceAtMost(1f)
            }
        }
    }
    
    private fun createFlowerOnStem(stem: IrisStem) {
        stem.hasFlower = true
        val flowerId = "iris_${stem.id}_${System.currentTimeMillis()}"
        
        // Taille variable des fleurs (15% plus grande : 0.7x à 1.3x → 0.805x à 1.495x)
        val sizeVariation = 0.805f + Random.nextFloat() * 0.69f // 15% plus grande
        
        val flower = IrisFlower(
            stemId = stem.id,
            position = PointF(stem.currentPoint.x, stem.currentPoint.y - 20f),
            id = flowerId,
            sizeMultiplier = sizeVariation
        )
        
        flowers.add(flower)
        challengeManager?.notifyFlowerCreated(flower.position.x, flower.position.y, flowerId)
    }
    
    private fun growExistingFlowers(force: Float) {
        for (flower in flowers) {
            if (flower.bloomProgress < 1f && !flower.isFullyBloomed) {
                val growth = force * 0.015f
                flower.bloomProgress = (flower.bloomProgress + growth).coerceAtMost(1f)
                
                if (flower.bloomProgress >= 1f) {
                    flower.isFullyBloomed = true
                }
            }
        }
    }
    
    fun notifyIrisBranchCreated(branchId: String) {
        challengeManager?.notifyDivisionCreated(branchId)
    }
    
    // Getters pour le renderer
    fun getStems(): List<IrisStem> = stems
    fun getFlowers(): List<IrisFlower> = flowers
}
