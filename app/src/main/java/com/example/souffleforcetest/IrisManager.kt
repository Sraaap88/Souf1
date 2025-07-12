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
    var isFullyBloomed: Boolean = false
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
    
    // Paramètres de croissance
    private val forceThreshold = 0.15f
    private val baseGrowthSpeed = 6f
    private val maxStemHeight = 0.45f
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
        // Iris pousse au centre avec 2-3 tiges groupées
        if (stems.isEmpty()) {
            createInitialStemGroup(centerX, bottomY)
        }
    }
    
    fun processStemGrowth(force: Float) {
        // CORRECTION: Créer les tiges SEULEMENT quand on souffle fort
        if (stems.isEmpty() && force > forceThreshold) {
            createInitialStemGroup(baseX, baseY)
            saccadeCount = 1
            currentActiveStemGroup = 0
            lastSaccadeTime = System.currentTimeMillis()
            isCurrentlyBreathing = true
            println("Iris: Tiges initiales créées !")
        }
        
        detectSaccadesAndActivateGroups(force, System.currentTimeMillis())
        
        if (force > forceThreshold && currentActiveStemGroup >= 0) {
            growActiveGroup(force)
        }
        
        lastForce = force
    }
    
    fun processLeavesGrowth(force: Float) {
        if (force > forceThreshold) {
            for (stem in stems) {
                createLeavesOnStem(stem)
                growExistingLeaves(stem, force)
            }
        }
    }
    
    fun processFlowerGrowth(force: Float) {
        if (force > forceThreshold) {
            for (stem in stems) {
                if (stem.currentHeight > stem.maxHeight * 0.7f && !stem.hasFlower) {
                    createFlowerOnStem(stem)
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
            
            if (groupToActivate == 0) {
                println("Saccade $saccadeCount: Groupe PRINCIPAL Iris activé")
            } else {
                println("Saccade $saccadeCount: Nouveau groupe Iris $groupToActivate créé")
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
            val maxHeight = screenHeight * maxStemHeight * heightVariation
            
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
        val baseRadius = 120f + groupNumber * 80f
        val groupAngle = Random.nextFloat() * 2 * PI
        val groupDistance = Random.nextFloat() * baseRadius + 60f
        
        val groupBaseX = (centerX + cos(groupAngle).toFloat() * groupDistance)
            .coerceIn(marginFromEdges, screenWidth - marginFromEdges)
        val groupBaseY = bottomY + (Random.nextFloat() - 0.5f) * 60f
        
        val stemCount = 2 + Random.nextInt(2) // 2 ou 3 tiges par groupe
        
        for (i in 0 until stemCount) {
            val angle = Random.nextFloat() * 15f - 7.5f
            val spacing = 30f * i - 15f
            val stemX = (groupBaseX + spacing).coerceIn(marginFromEdges, screenWidth - marginFromEdges)
            val heightVariation = 0.7f + Random.nextFloat() * 0.5f
            val maxHeight = screenHeight * maxStemHeight * heightVariation
            
            val stem = IrisStem(
                id = nextStemId++,
                startPoint = PointF(stemX, groupBaseY),
                currentPoint = PointF(stemX, groupBaseY),
                baseAngle = angle,
                growthSpeed = baseGrowthSpeed + Random.nextFloat() * 3f,
                maxHeight = maxHeight,
                curvature = Random.nextFloat() * 0.3f + 0.1f
            )
            
            stem.segments.add(PointF(stemX, groupBaseY))
            stems.add(stem)
        }
    }
    
    private fun growActiveGroup(force: Float) {
        val activeStemsInGroup = if (currentActiveStemGroup == 0) {
            stems.take(3) // Premier groupe
        } else {
            val startIndex = 3 + (currentActiveStemGroup - 1) * 3
            stems.drop(startIndex).take(3)
        }
        
        for (activeStem in activeStemsInGroup) {
            if (activeStem.currentHeight >= activeStem.maxHeight) continue
            
            val forceStability = 1f - abs(force - lastForce).coerceAtMost(0.5f) * 2f
            val qualityMultiplier = 0.5f + forceStability * 0.5f
            
            val growthProgress = activeStem.currentHeight / activeStem.maxHeight
            val progressCurve = 1f - growthProgress * growthProgress
            val adjustedGrowth = force * qualityMultiplier * progressCurve * activeStem.growthSpeed * 0.008f
            
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
    
    private fun createLeavesOnStem(stem: IrisStem) {
        if (stem.leaves.isNotEmpty()) return
        if (stem.currentHeight < 50f) return
        
        // 4-6 feuilles longues et étroites en forme d'épée
        val leafCount = 4 + Random.nextInt(3)
        
        for (i in 0 until leafCount) {
            val heightRatio = 0.1f + (i.toFloat() / leafCount) * 0.8f
            val attachmentY = stem.startPoint.y - stem.currentHeight * heightRatio
            val leafAngle = Random.nextFloat() * 60f - 30f // -30° à +30°
            val leafLength = 80f + Random.nextFloat() * 40f
            val leafWidth = 8f + Random.nextFloat() * 4f
            
            val leaf = IrisLeaf(
                attachmentPoint = PointF(stem.currentPoint.x, attachmentY),
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
                val growth = force * 0.01f
                leaf.growthProgress = (leaf.growthProgress + growth).coerceAtMost(1f)
            }
        }
    }
    
    private fun createFlowerOnStem(stem: IrisStem) {
        stem.hasFlower = true
        val flowerId = "iris_${stem.id}_${System.currentTimeMillis()}"
        
        val flower = IrisFlower(
            stemId = stem.id,
            position = PointF(stem.currentPoint.x, stem.currentPoint.y - 20f),
            id = flowerId
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
