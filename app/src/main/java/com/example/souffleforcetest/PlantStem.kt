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
    
    private var stemHeight = 30f  // MODIFIÉ : Commencer avec 30px
    private var maxPossibleHeight = 0f
    private val stemBaseX = screenWidth / 2f
    private val stemBaseY = screenHeight - 100f
    private var lastForce = 0f
    private var branchCount = 0
    
    // ==================== SYSTÈME ORDRE ALÉATOIRE DE TIGES ====================
    
    private var saccadeCount = 0
    private var isCurrentlyBreathing = false
    private var lastSaccadeTime = 0L
    private val saccadeCooldown = 300L
    private val breathStartThreshold = 0.3f
    private val breathEndThreshold = 0.2f
    
    // Pool de toutes les tiges possibles en ordre aléatoire
    private var stemOrderPool = mutableListOf<Int>() // 0=principale, 1-6=branches
    private var currentActiveStemIndex = -1 // Index dans le pool (-1 = aucune active)
    
    // Courbures aléatoires naturelles pour chaque tige
    private val stemRandomCurvatures = mutableMapOf<Int, Float>()
    
    // Instance du gestionnaire de croissance
    private lateinit var growthManager: PlantGrowthManager
    private lateinit var leavesManager: PlantLeavesManager
    private lateinit var flowerManager: FlowerManager
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.15f
    private val maxStemHeight = 0.75f
    private val baseThickness = 25f
    private val tipThickness = 8f
    private val growthRate = 2400f
    private val oscillationDecay = 0.98f
    private val maxBranches = 6
    
    init {
        maxPossibleHeight = screenHeight * maxStemHeight
        growthManager = PlantGrowthManager(this)
        leavesManager = PlantLeavesManager(this)
        flowerManager = FlowerManager(this)
        setupRandomStemOrder()
        createInitialStem()  // NOUVEAU : Créer la tige de départ
    }
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun processStemGrowth(force: Float, phaseTime: Long) {
        // MODIFIÉ : Plus d'émergence, directement la logique de croissance
        
        // Détection des saccades et activation des tiges
        detectSaccadesAndActivateStems(force, System.currentTimeMillis())
        
        // Faire pousser SEULEMENT la tige actuellement active
        if (force > forceThreshold && currentActiveStemIndex >= 0) {
            growOnlyActiveStem(force)
        }
        
        // Mise à jour des oscillations pour toutes les tiges
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
        stemHeight = 30f  // MODIFIÉ : Reset à 30px
        lastForce = 0f
        branchCount = 0
        leavesManager.resetLeaves()
        flowerManager.resetFlowers()
        
        // Reset du système aléatoire
        saccadeCount = 0
        isCurrentlyBreathing = false
        lastSaccadeTime = 0L
        currentActiveStemIndex = -1
        stemRandomCurvatures.clear()
        setupRandomStemOrder()
        
        // NOUVEAU : Recréer la tige de départ
        createInitialStem()
    }
    
    // NOUVEAU : Créer la tige de départ (identique à la fin d'émergence)
    private fun createInitialStem() {
        mainStem.clear()
        val emergenceHeight = 30f
        
        // Créer 6 segments identiques à l'émergence
        for (i in 0..5) {
            val segmentProgress = i / 5f
            val y = stemBaseY - emergenceHeight * segmentProgress
            val thickness = lerp(baseThickness, tipThickness, segmentProgress * 0.3f)
            val wiggle = sin(segmentProgress * PI * 3 + i * 0.5) * 0.5f
            
            mainStem.add(StemPoint(stemBaseX + wiggle.toFloat(), y, thickness))
        }
    }
    
    fun getStemHeight(): Float = stemHeight
    fun hasVisibleStem(): Boolean = mainStem.size >= 1
    
    fun getLeaves(): List<PlantLeavesManager.Leaf> = leavesManager.leaves
    fun getLeavesManager(): PlantLeavesManager = leavesManager
    fun getFlowers(): List<FlowerManager.Flower> = flowerManager.flowers
    fun getFlowerManager(): FlowerManager = flowerManager
    fun getGrowthManager(): PlantGrowthManager = growthManager
    
    // ==================== SYSTÈME ORDRE ALÉATOIRE ====================
    
    private fun setupRandomStemOrder() {
        // Créer un pool des 7 tiges possibles (0=principale, 1-6=branches)
        stemOrderPool = mutableListOf(0, 1, 2, 3, 4, 5, 6)
        stemOrderPool.shuffle() // Mélanger l'ordre de façon aléatoire
        
        // Générer des courbures aléatoires pour chaque tige
        for (i in 0..6) {
            val randomCurvature = (Math.random().toFloat() - 0.5f) * 0.8f // -0.4 à +0.4
            stemRandomCurvatures[i] = randomCurvature
        }
        
        println("Ordre aléatoire des tiges: ${stemOrderPool.map { if (it == 0) "P" else it.toString() }}")
    }
    
    private fun detectSaccadesAndActivateStems(force: Float, currentTime: Long) {
        val wasBreathing = isCurrentlyBreathing
        val isNowBreathing = force > breathStartThreshold
        
        // Détection début de souffle = nouvelle saccade
        if (!wasBreathing && isNowBreathing) {
            if (currentTime - lastSaccadeTime > saccadeCooldown) {
                saccadeCount++
                lastSaccadeTime = currentTime
                isCurrentlyBreathing = true
                
                // Activer la prochaine tige selon l'ordre aléatoire
                activateNextStemInOrder()
            }
        }
        
        // Détection fin de souffle
        if (wasBreathing && force < breathEndThreshold) {
            isCurrentlyBreathing = false
        }
    }
    
    private fun activateNextStemInOrder() {
        if (saccadeCount <= stemOrderPool.size) {
            // Prendre la tige suivante dans l'ordre aléatoire
            val stemTypeToActivate = stemOrderPool[saccadeCount - 1]
            currentActiveStemIndex = saccadeCount - 1
            
            if (stemTypeToActivate == 0) {
                // Tige principale (déjà créée)
                println("Saccade $saccadeCount: Tige PRINCIPALE activée")
            } else {
                // NE PAS créer la branche immédiatement, juste l'activer
                println("Saccade $saccadeCount: Branche $stemTypeToActivate sera créée quand elle poussera")
            }
        }
    }
    
    private fun ensureBranchExists(branchNumber: Int) {
        // S'assurer que cette branche existe SEULEMENT si on souffle assez fort
        while (branches.size < branchNumber) {
            createBranchWithRandomCurvature(branches.size + 1)
        }
    }
    
    private fun growOnlyActiveStem(force: Float) {
        if (currentActiveStemIndex < 0 || currentActiveStemIndex >= stemOrderPool.size) return
        
        val activeStemType = stemOrderPool[currentActiveStemIndex]
        
        if (activeStemType == 0) {
            // Faire pousser SEULEMENT la tige principale
            growthManager.growMainStem(force)
            applyRandomCurvatureToMainStem(force)
        } else {
            // Créer la branche SEULEMENT si force très forte ET soutenue
            if (force > forceThreshold * 3f) { // AUGMENTÉ: 3x au lieu de 1.5x
                ensureBranchExists(activeStemType)
                
                // Faire pousser SEULEMENT cette branche spécifique
                val branchIndex = activeStemType - 1
                if (branchIndex < branches.size) {
                    growSpecificBranch(branchIndex, force)
                    applyRandomCurvatureToBranch(branchIndex, force)
                }
            }
        }
    }
    
    private fun growSpecificBranch(branchIndex: Int, force: Float) {
        val branch = branches[branchIndex]
        if (branch.currentHeight >= branch.maxHeight) return
        
        // Copie de la logique de croissance d'une seule branche
        val branchHeightRatio = branch.currentHeight / branch.maxHeight
        val branchResistance = if (branchHeightRatio > 0.667f) 0.8f else 1f
        
        val branchGrowthMultiplier = 1.0f * branch.personalityFactor * branchResistance
        val forceStability = 1f - abs(force - lastForce).coerceAtMost(0.5f) * 2f
        val qualityMultiplier = 0.5f + forceStability * 0.5f
        
        val growthProgress = branch.currentHeight / branch.maxHeight
        val progressCurve = 1f - growthProgress * growthProgress
        val adjustedGrowth = force * qualityMultiplier * progressCurve * growthRate * 0.008f * 9f * branchGrowthMultiplier
        
        if (adjustedGrowth > 0) {
            branch.currentHeight += adjustedGrowth
            
            val lastPoint = branch.points.lastOrNull() ?: return
            val segmentHeight = 7f + (Math.random() * 2f).toFloat()
            val segments = (adjustedGrowth / segmentHeight).toInt().coerceAtLeast(1)
            
            for (i in 1..segments) {
                val currentBranchHeight = branch.currentHeight - adjustedGrowth + (adjustedGrowth * i / segments)
                val progressFromBase = currentBranchHeight / branch.maxHeight
                
                val baseThicknessBranch = baseThickness * 0.9f * branch.thicknessVariation
                val thicknessProgress = progressFromBase * 0.4f
                val microVariation = (Math.random() * 0.03f - 0.015f).toFloat()
                val thickness = baseThicknessBranch * (1f - thicknessProgress + microVariation)
                
                val lastPointX = lastPoint.x + lastPoint.oscillation + lastPoint.permanentWave
                val baseX = stemBaseX
                
                val branchDirection = if (branch.angle < 0) -1f else 1f
                val heightRatio = currentBranchHeight / branch.maxHeight
                
                val baseDistance = abs(branch.baseOffset)
                val divergenceMultiplier = baseDistance / 50f
                
                val earlyDivergence = if (heightRatio < 0.15f) {
                    val divergenceRatio = heightRatio / 0.15f
                    divergenceRatio * 60f * branchDirection * divergenceMultiplier
                } else 60f * branchDirection * divergenceMultiplier
                
                val midCurve = if (heightRatio > 0.15f && heightRatio < 0.7f) {
                    val midRatio = (heightRatio - 0.15f) / 0.55f
                    val additionalCurve = sin(midRatio * PI.toFloat()) * 20f * divergenceMultiplier
                    additionalCurve * branchDirection
                } else 0f
                
                val finalCurve = if (heightRatio > 0.7f) {
                    val finalRatio = (heightRatio - 0.7f) / 0.3f
                    val elegantCurve = finalRatio * finalRatio * finalRatio * 15f * divergenceMultiplier
                    val downwardBend = finalRatio * finalRatio * 6f * divergenceMultiplier
                    
                    val curveReduction = if (heightRatio > 0.667f) 0.8f else 1f
                    (elegantCurve * branchDirection + downwardBend * abs(branchDirection) * 0.3f) * curveReduction
                } else 0f
                
                val naturalWeight = heightRatio * heightRatio * 5f * abs(branchDirection) * divergenceMultiplier
                
                val totalCurve = earlyDivergence + midCurve + finalCurve + naturalWeight
                val currentX = baseX + totalCurve
                val currentY = stemBaseY - currentBranchHeight
                
                val forceVariation = abs(force - lastForce) * 2.5f * branch.personalityFactor
                val heightMultiplier = 1f + progressFromBase * 0.4f
                val phaseOffset = branch.angle * 0.08f + (if (branch.angle > 0) 0f else PI.toFloat())
                val oscillation = sin(System.currentTimeMillis() * 0.0025f * branch.trembleFrequency + phaseOffset) * 
                                forceVariation * 5f * heightMultiplier
                
                val newPoint = StemPoint(currentX, currentY, thickness, oscillation)
                branch.points.add(newPoint)
            }
        }
    }
    
    private fun applyRandomCurvatureToMainStem(force: Float) {
        val randomCurvature = stemRandomCurvatures[0] ?: 0f
        // Application simple de la courbure aléatoire à la tige principale
        // Pour l'instant, on garde la courbure naturelle existante
    }
    
    private fun applyRandomCurvatureToBranch(branchIndex: Int, force: Float) {
        val branchType = branchIndex + 1
        val randomCurvature = stemRandomCurvatures[branchType] ?: 0f
        // Application simple de la courbure aléatoire à cette branche
        // Pour l'instant, on garde la courbure naturelle existante
    }
    
    private fun createBranchWithRandomCurvature(branchNumber: Int) {
        branchCount++
        
        val baseSpacing = 85f
        
        val isLeft: Boolean
        val position: Float
        val heightRange: Pair<Float, Float>
        val thickness: Float
        
        when (branchNumber) {
            1 -> {
                isLeft = false
                position = baseSpacing
                heightRange = Pair(0.85f, 0.95f)
                thickness = 0.90f
            }
            2 -> {
                isLeft = true  
                position = -baseSpacing
                heightRange = Pair(0.80f, 0.90f)
                thickness = 0.85f
            }
            3 -> {
                isLeft = false
                position = baseSpacing * 2
                heightRange = Pair(0.78f, 0.88f)
                thickness = 0.80f
            }
            4 -> {
                isLeft = true
                position = -baseSpacing * 2
                heightRange = Pair(0.78f, 0.88f)
                thickness = 0.80f
            }
            5 -> {
                isLeft = false
                position = baseSpacing * 3
                heightRange = Pair(0.76f, 0.86f)
                thickness = 0.75f
            }
            6 -> {
                isLeft = true
                position = -baseSpacing * 3
                heightRange = Pair(0.76f, 0.86f)
                thickness = 0.75f
            }
            else -> {
                isLeft = false
                position = baseSpacing
                heightRange = Pair(0.80f, 0.90f)
                thickness = 0.80f
            }
        }
        
        val adjustedHeightRange = if (branchCount == 1) {
            Pair(0.85f, 0.95f)
        } else {
            heightRange
        }
        
        // Ajouter courbure aléatoire à la position
        val randomCurvature = stemRandomCurvatures[branchNumber] ?: 0f
        val forcedOffset = position + (Math.random().toFloat() * 15f - 7.5f) + (randomCurvature * 30f)
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
            currentHeight = 25f, // NOUVEAU: Commencer avec une hauteur minimum de 25px
            maxHeight = branchMaxHeight,
            personalityFactor = personalityFactor,
            trembleFrequency = trembleFreq,
            curvatureDirection = curvatureDir,
            thicknessVariation = thicknessVar
        )
        
        val startX = stemBaseX
        val startThickness = baseThickness * thicknessVar
        newBranch.points.add(StemPoint(startX, stemBaseY, startThickness))
        
        val divergenceForce = position + (Math.random().toFloat() * 30f - 15f)
        
        // NOUVEAU: Créer plusieurs points initiaux pour avoir une hauteur minimum
        for (i in 1..3) {
            val segmentHeight = 8f * i
            val segmentX = startX + (divergenceForce * i / 3f)
            val segmentY = stemBaseY - segmentHeight
            val segmentThickness = startThickness * (1f - i * 0.05f)
            
            newBranch.points.add(StemPoint(segmentX, segmentY, segmentThickness))
        }
        
        branches.add(newBranch)
        
        println("Branche $branchNumber créée avec hauteur initiale 25px et courbure ${randomCurvature}")
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
    
    fun setStemHeight(height: Float) {
        stemHeight = height
    }
    
    fun getSaccadeInfo(): String {
        val activeStem = if (currentActiveStemIndex >= 0 && currentActiveStemIndex < stemOrderPool.size) {
            val stemType = stemOrderPool[currentActiveStemIndex]
            if (stemType == 0) "Principale" else "Branche $stemType"
        } else "Aucune"
        
        return "Saccades: $saccadeCount, Active: $activeStem"
    }
    
    // ==================== UTILITAIRES ====================
    
    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + fraction * (end - start)
    }
}
