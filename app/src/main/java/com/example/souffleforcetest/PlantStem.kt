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
    private var branchCreationOrder = mutableListOf<Int>()
    
    // ==================== NOUVEAU SYSTÈME DE CONTRÔLE PROGRESSIF ====================
    
    // Historique des forces pour analyser la stabilité
    private val forceHistory = mutableListOf<Float>()
    private val maxHistorySize = 30 // 1 seconde d'historique à 30 FPS
    
    // Seuils progressifs pour chaque niveau de tiges - PLUS FACILES
    private val breathControlLevels = mapOf(
        1 to BreathControlLevel(
            minStabilityTime = 300L,        // 0.3 seconde stable (était 0.5s)
            maxForceVariation = 0.5f,       // Variation permise très large (était 0.4f)
            minForce = 0.15f,               // Force minimum très faible (était 0.2f)
            description = "Souffle doux et régulier"
        ),
        3 to BreathControlLevel(
            minStabilityTime = 600L,        // 0.6 seconde stable (était 1s)
            maxForceVariation = 0.4f,       // Variation plus permissive (était 0.25f)
            minForce = 0.2f,                // Force minimum plus faible (était 0.3f)
            description = "Souffle modéré et contrôlé"
        ),
        5 to BreathControlLevel(
            minStabilityTime = 900L,        // 0.9 seconde stable (était 1.5s)
            maxForceVariation = 0.3f,       // Variation plus permissive (était 0.15f)
            minForce = 0.25f,               // Force minimum plus faible (était 0.4f)
            description = "Souffle fort et précis"
        ),
        7 to BreathControlLevel(
            minStabilityTime = 1200L,       // 1.2 secondes stable (était 2s)
            maxForceVariation = 0.2f,       // Variation plus permissive (était 0.1f)
            minForce = 0.3f,                // Force minimum plus faible (était 0.5f)
            description = "Maîtrise parfaite du souffle"
        )
    )
    
    data class BreathControlLevel(
        val minStabilityTime: Long,     // Temps minimum de stabilité requis
        val maxForceVariation: Float,   // Variation maximale permise
        val minForce: Float,            // Force minimum requise
        val description: String
    )
    
    // État du contrôle du souffle
    private var currentStabilityStart = 0L
    private var lastStableForce = 0f
    private var isBreathStable = false
    
    // Instance du gestionnaire de croissance - initialisation tardive
    private lateinit var growthManager: PlantGrowthManager
    
    // Instance du gestionnaire de feuilles
    private lateinit var leavesManager: PlantLeavesManager
    
    // Instance du gestionnaire de fleurs
    private lateinit var flowerManager: FlowerManager
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.15f // Réduit pour détecter plus facilement
    private val maxStemHeight = 0.75f // Réduit pour que les branches soient plus hautes
    private val baseThickness = 25f
    private val tipThickness = 8f
    private val growthRate = 2400f
    private val oscillationDecay = 0.98f
    private val emergenceDuration = 1000L
    private val maxBranches = 6 // 6 tiges secondaires + 1 principale = 7 total
    
    init {
        maxPossibleHeight = screenHeight * maxStemHeight
        growthManager = PlantGrowthManager(this)
        leavesManager = PlantLeavesManager(this)
        flowerManager = FlowerManager(this)
        initializeBranchOrder()
    }
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun processStemGrowth(force: Float, phaseTime: Long) {
        // Mettre à jour l'historique des forces
        updateForceHistory(force)
        
        // Analyser la stabilité du souffle
        analyzeBreathStability(force)
        
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
        
        // Phase de croissance normale
        if (force > forceThreshold && mainStem.isNotEmpty()) {
            if (force > forceThreshold * 1.5f) {
                growthManager.growMainStem(force)
                growthManager.growAllBranches(force)
                
                // NOUVEAU SYSTÈME : Création de branches basée sur le contrôle du souffle
                checkForNewBranchCreation(force)
            }
        }
        
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
        stemHeight = 0f
        lastForce = 0f
        isEmerging = false
        branchSide = true
        branchCount = 0
        branchCreationOrder.clear()
        initializeBranchOrder()
        leavesManager.resetLeaves()
        flowerManager.resetFlowers()
        
        // Reset du système de contrôle du souffle
        forceHistory.clear()
        currentStabilityStart = 0L
        lastStableForce = 0f
        isBreathStable = false
    }
    
    fun getStemHeight(): Float = stemHeight
    fun hasVisibleStem(): Boolean = mainStem.size >= 1
    
    fun getLeaves(): List<PlantLeavesManager.Leaf> = leavesManager.leaves
    fun getLeavesManager(): PlantLeavesManager = leavesManager
    fun getFlowers(): List<FlowerManager.Flower> = flowerManager.flowers
    fun getFlowerManager(): FlowerManager = flowerManager
    
    // ==================== GETTERS POUR GROWTHMANAGER ====================
    
    fun getMaxPossibleHeight(): Float = maxPossibleHeight
    fun getStemBaseX(): Float = stemBaseX
    fun getStemBaseY(): Float = stemBaseY
    fun getLastForce(): Float = lastForce
    fun getBaseThickness(): Float = baseThickness
    fun getTipThickness(): Float = tipThickness
    fun getGrowthRate(): Float = growthRate
    fun getOscillationDecay(): Float = oscillationDecay
    
    // ==================== SETTERS POUR GROWTHMANAGER ====================
    
    fun setStemHeight(height: Float) {
        stemHeight = height
    }
    
    // ==================== NOUVEAU SYSTÈME DE CONTRÔLE PROGRESSIF ====================
    
    private fun updateForceHistory(force: Float) {
        forceHistory.add(force)
        if (forceHistory.size > maxHistorySize) {
            forceHistory.removeAt(0)
        }
    }
    
    private fun analyzeBreathStability(force: Float) {
        if (forceHistory.size < 10) return // Attendre un minimum de données
        
        // Calculer la variation récente
        val recentForces = forceHistory.takeLast(10)
        val avgForce = recentForces.average().toFloat()
        val maxVariation = recentForces.maxOf { abs(it - avgForce) }
        
        val currentTime = System.currentTimeMillis()
        
        // Vérifier si le souffle est stable selon les critères actuels
        val targetBranchCount = getTargetBranchCount()
        val level = breathControlLevels[targetBranchCount]
        
        if (level != null) {
            val isCurrentlyStable = force >= level.minForce && 
                                  maxVariation <= level.maxForceVariation
            
            if (isCurrentlyStable) {
                if (!isBreathStable) {
                    // Début d'une période stable
                    currentStabilityStart = currentTime
                    isBreathStable = true
                    lastStableForce = force
                }
            } else {
                // Perte de stabilité
                isBreathStable = false
                currentStabilityStart = 0L
            }
        }
    }
    
    private fun getTargetBranchCount(): Int {
        // Déterminer combien de tiges le joueur essaie d'obtenir
        return when (branchCount + 1) { // +1 pour inclure la tige principale
            0, 1 -> 1
            2, 3 -> 3
            4, 5 -> 5
            else -> 7
        }
    }
    
    private fun checkForNewBranchCreation(force: Float) {
        if (branchCount >= maxBranches || stemHeight < 30f) return
        
        val targetBranchCount = getTargetBranchCount()
        val level = breathControlLevels[targetBranchCount] ?: return
        
        val currentTime = System.currentTimeMillis()
        val stabilityDuration = if (isBreathStable) currentTime - currentStabilityStart else 0L
        
        // Vérifier si les conditions sont remplies pour créer une nouvelle tige
        if (isBreathStable && 
            stabilityDuration >= level.minStabilityTime &&
            force >= level.minForce) {
            
            // Conditions supplémentaires selon le niveau
            val             shouldCreateBranch = when (targetBranchCount) {
                1 -> true // Toujours facile pour la première
                3 -> {
                    // Pour 3 tiges : stabilité TRÈS permissive
                    val avgForce = forceHistory.takeLast(10).average().toFloat()
                    abs(force - avgForce) < 0.35f // Était 0.2f
                }
                5 -> {
                    // Pour 5 tiges : stabilité permissive + force moins précise
                    val avgForce = forceHistory.takeLast(15).average().toFloat()
                    abs(force - avgForce) < 0.25f && force in 0.25f..0.8f // Était 0.15f et 0.4f-0.7f
                }
                7 -> {
                    // Pour 7 tiges : contrôle amélioré mais accessible
                    val avgForce = forceHistory.takeLast(20).average().toFloat()
                    abs(force - avgForce) < 0.18f && 
                    force in 0.35f..0.75f &&  // Était 0.5f-0.65f
                    stabilityDuration >= 1500L // Était 2500L
                }
                else -> false
            }
            
            if (shouldCreateBranch) {
                createBranchInOrder()
                // Reset de la stabilité pour éviter de créer plusieurs tiges d'un coup
                isBreathStable = false
                currentStabilityStart = 0L
                
                println("Nouvelle tige créée ! Total: ${branchCount + 1} tiges (niveau: ${level.description})")
            }
        }
    }
    
    // ==================== FONCTIONS PRIVÉES EXISTANTES ====================
    
    private fun initializeBranchOrder() {
        branchCreationOrder = (1..maxBranches).toMutableList()
        branchCreationOrder.shuffle()
    }
    
    private fun createBranchInOrder() {
        if (branchCount >= maxBranches || branchCount >= branchCreationOrder.size) return
        
        val branchNumber = branchCreationOrder[branchCount]
        createBranch(branchNumber)
    }
    
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
    
    private fun createBranch(branchNumber: Int) {
        branchCount++
        
        // ESPACEMENT AUGMENTÉ : Plus d'espace entre les tiges
        val baseSpacing = 85f // Augmenté de 50f à 85f (+70% d'espace)
        
        val isLeft: Boolean
        val position: Float
        val heightRange: Pair<Float, Float>
        val thickness: Float
        
        when (branchNumber) {
            1 -> {
                isLeft = false
                position = baseSpacing
                heightRange = Pair(0.85f, 0.95f) // TOUJOURS plus haute que principale (0.75f)
                thickness = 0.90f
            }
            2 -> {
                isLeft = true  
                position = -baseSpacing
                heightRange = Pair(0.80f, 0.90f) // Plus haute que principale
                thickness = 0.85f
            }
            3 -> {
                isLeft = false
                position = baseSpacing * 2
                heightRange = Pair(0.78f, 0.88f) // Plus haute que principale
                thickness = 0.80f
            }
            4 -> {
                isLeft = true
                position = -baseSpacing * 2
                heightRange = Pair(0.78f, 0.88f) // Plus haute que principale
                thickness = 0.80f
            }
            5 -> {
                isLeft = false
                position = baseSpacing * 3
                heightRange = Pair(0.76f, 0.86f) // Plus haute que principale
                thickness = 0.75f
            }
            6 -> {
                isLeft = true
                position = -baseSpacing * 3
                heightRange = Pair(0.76f, 0.86f) // Plus haute que principale
                thickness = 0.75f
            }
            else -> {
                isLeft = false
                position = baseSpacing
                heightRange = Pair(0.80f, 0.90f)
                thickness = 0.80f
            }
        }
        
        // GARANTIE : Au moins une tige sera plus haute que la principale
        val adjustedHeightRange = if (branchCount == 1) {
            // La PREMIÈRE branche est TOUJOURS plus haute que la principale
            Pair(0.85f, 0.95f) // Garantie d'être plus haute que 0.75f
        } else {
            heightRange
        }
        
        val forcedOffset = position + (Math.random().toFloat() * 15f - 7.5f) // Variation augmentée
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
        
        val divergenceForce = position + (Math.random().toFloat() * 30f - 15f) // Divergence augmentée
        
        val initialHeight = 12f
        val initialX = startX + divergenceForce
        val initialY = stemBaseY - initialHeight
        val initialThickness = startThickness * 0.95f
        
        newBranch.points.add(StemPoint(initialX, initialY, initialThickness))
        newBranch.currentHeight = initialHeight
        
        branches.add(newBranch)
        
        println("Tige ${branchNumber} (${branchCount}ème créée): ${if (isLeft) "GAUCHE" else "DROITE"} - Hauteur max: ${(baseHeightRatio * 100).toInt()}% (Principal: 75%)")
    }
    
    // ==================== UTILITAIRES ====================
    
    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + fraction * (end - start)
    }
}
