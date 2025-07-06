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
    
    // ==================== NOUVEAUX: ANALYSE DU SOUFFLE ====================
    
    private val forceHistory = mutableListOf<Float>()
    private val forceTimestamps = mutableListOf<Long>()
    private var breathQuality = 0f
    private var breathStyle = BreathStyle.UNKNOWN
    private var maxBranchesAllowed = 6
    private var plantPersonality = PlantPersonality.BALANCED
    private val maxHistorySize = 50
    
    enum class BreathStyle {
        UNKNOWN, STABLE_LONG, SHORT_STRONG, IRREGULAR
    }
    
    enum class PlantPersonality {
        TALL_STRAIGHT,    // Souffle stable → plante haute et droite
        COMPACT_WIDE,     // Souffle fort court → plante trapue écartée  
        CURVED_WILD       // Souffle irrégulier → courbures et asymétries
    }
    
    // Instance du gestionnaire de croissance - initialisation tardive
    private lateinit var growthManager: PlantGrowthManager
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.25f
    private val maxStemHeight = 0.8f
    private val baseThickness = 25f
    private val tipThickness = 8f
    private val growthRate = 2400f
    private val oscillationDecay = 0.98f
    private val emergenceDuration = 1000L
    
    // ==================== PARAMÈTRES VARIABLES SELON STYLE ====================
    
    private var dynamicBranchThreshold = 0.18f
    private var dynamicMaxBranches = 6
    private var dynamicGrowthStyle = 1f
    
    init {
        maxPossibleHeight = screenHeight * maxStemHeight
        growthManager = PlantGrowthManager(this)
    }
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun processStemGrowth(force: Float, phaseTime: Long) {
        // Enregistrer l'historique du souffle pour analyse
        recordBreathData(force)
        
        // Analyser le style de respiration toutes les 500ms
        if (forceHistory.size % 10 == 0) {
            analyzeBreathPattern()
        }
        
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
                
                // Détection ramification INTELLIGENTE basée sur le style de respiration
                val shouldCreateBranch = shouldCreateNewBranch(force)
                if (shouldCreateBranch && stemHeight > 20f && branchCount < dynamicMaxBranches) {
                    createBranch()
                }
            }
        }
        
        // Mise à jour des oscillations même sans souffle
        growthManager.updateOscillations()
        lastForce = force
    }
    
    fun resetStem() {
        mainStem.clear()
        branches.clear()
        stemHeight = 0f
        lastForce = 0f
        isEmerging = false
        branchSide = true
        branchCount = 0
        
        // Reset de l'analyse du souffle
        forceHistory.clear()
        forceTimestamps.clear()
        breathQuality = 0f
        breathStyle = BreathStyle.UNKNOWN
        plantPersonality = PlantPersonality.BALANCED
        resetDynamicParameters()
    }
    
    fun getStemHeight(): Float = stemHeight
    fun hasVisibleStem(): Boolean = mainStem.size >= 1
    fun getBreathStyle(): BreathStyle = breathStyle
    fun getPlantPersonality(): PlantPersonality = plantPersonality
    
    // ==================== GETTERS POUR GROWTHMANAGER ====================
    
    fun getMaxPossibleHeight(): Float = maxPossibleHeight
    fun getStemBaseX(): Float = stemBaseX
    fun getStemBaseY(): Float = stemBaseY
    fun getLastForce(): Float = lastForce
    fun getBaseThickness(): Float = baseThickness
    fun getTipThickness(): Float = tipThickness
    fun getGrowthRate(): Float = growthRate
    fun getOscillationDecay(): Float = oscillationDecay
    fun getDynamicGrowthStyle(): Float = dynamicGrowthStyle
    
    fun setStemHeight(height: Float) {
        stemHeight = height
    }
    
    // ==================== ANALYSE DU SOUFFLE ====================
    
    private fun recordBreathData(force: Float) {
        val currentTime = System.currentTimeMillis()
        forceHistory.add(force)
        forceTimestamps.add(currentTime)
        
        // Garder seulement les dernières données
        if (forceHistory.size > maxHistorySize) {
            forceHistory.removeAt(0)
            forceTimestamps.removeAt(0)
        }
    }
    
    private fun analyzeBreathPattern() {
        if (forceHistory.size < 20) return
        
        val recentForces = forceHistory.takeLast(20)
        val avgForce = recentForces.average().toFloat()
        val maxForce = recentForces.maxOrNull() ?: 0f
        val minForce = recentForces.minOrNull() ?: 0f
        val forceRange = maxForce - minForce
        
        // Calculer la stabilité (variation)
        val variance = recentForces.map { (it - avgForce) * (it - avgForce) }.average()
        val stability = 1f - (variance / (avgForce * avgForce)).coerceAtMost(1f)
        
        // Analyser le style de respiration
        breathStyle = when {
            stability > 0.8f && avgForce > 0.4f -> BreathStyle.STABLE_LONG
            avgForce > 0.6f && forceRange > 0.3f -> BreathStyle.SHORT_STRONG  
            stability < 0.6f -> BreathStyle.IRREGULAR
            else -> breathStyle // Garder l'analyse précédente
        }
        
        // Déterminer la personnalité de la plante
        plantPersonality = when (breathStyle) {
            BreathStyle.STABLE_LONG -> PlantPersonality.TALL_STRAIGHT
            BreathStyle.SHORT_STRONG -> PlantPersonality.COMPACT_WIDE
            BreathStyle.IRREGULAR -> PlantPersonality.CURVED_WILD
            else -> PlantPersonality.BALANCED
        }
        
        // Ajuster les paramètres dynamiques
        updateDynamicParameters()
        
        breathQuality = stability * avgForce
    }
    
    private fun updateDynamicParameters() {
        when (plantPersonality) {
            PlantPersonality.TALL_STRAIGHT -> {
                dynamicMaxBranches = (2..4).random() // Peu de branches
                dynamicBranchThreshold = 0.25f // Plus difficile de créer des branches
                dynamicGrowthStyle = 1.2f // Croissance plus verticale
            }
            PlantPersonality.COMPACT_WIDE -> {
                dynamicMaxBranches = (4..6).random() // Plus de branches
                dynamicBranchThreshold = 0.15f // Plus facile de créer des branches
                dynamicGrowthStyle = 0.8f // Croissance plus compacte
            }
            PlantPersonality.CURVED_WILD -> {
                dynamicMaxBranches = (1..5).random() // Variable
                dynamicBranchThreshold = 0.12f // Très sensible aux variations
                dynamicGrowthStyle = 0.9f // Croissance irrégulière
            }
            else -> {
                resetDynamicParameters()
            }
        }
    }
    
    private fun resetDynamicParameters() {
        dynamicMaxBranches = 6
        dynamicBranchThreshold = 0.18f
        dynamicGrowthStyle = 1f
    }
    
    private fun shouldCreateNewBranch(force: Float): Boolean {
        val forceVariation = abs(force - lastForce)
        
        return when (breathStyle) {
            BreathStyle.STABLE_LONG -> {
                // Branches rares, seulement sur variations intentionnelles
                forceVariation > dynamicBranchThreshold * 1.5f
            }
            BreathStyle.SHORT_STRONG -> {
                // Branches sur pics de force
                force > 0.7f && forceVariation > dynamicBranchThreshold
            }
            BreathStyle.IRREGULAR -> {
                // Branches fréquentes sur variations
                forceVariation > dynamicBranchThreshold * 0.8f
            }
            else -> {
                // Comportement par défaut
                forceVariation > dynamicBranchThreshold
            }
        }
    }
    
    // ==================== FONCTIONS PRIVÉES ====================
    
    private fun createEmergenceStem(progress: Float) {
        mainStem.clear()
        val emergenceHeight = 30f * progress
        
        for (i in 0..5) {
            val segmentProgress = i / 5f
            val y = stemBaseY - emergenceHeight * segmentProgress
            val thickness = lerp(baseThickness, tipThickness, segmentProgress * 0.3f)
            
            // Oscillation selon la personnalité
            val personalityWiggle = when (plantPersonality) {
                PlantPersonality.TALL_STRAIGHT -> 0.2f
                PlantPersonality.COMPACT_WIDE -> 0.8f
                PlantPersonality.CURVED_WILD -> 1.5f
                else -> 0.5f
            }
            
            val wiggle = sin(progress * PI * 3 + i * 0.5) * personalityWiggle * progress
            
            mainStem.add(StemPoint(stemBaseX + wiggle.toFloat(), y, thickness))
        }
        
        if (progress >= 1f) {
            stemHeight = emergenceHeight
        }
    }
    
    private fun createBranch() {
        branchCount++
        
        // Espacement et positionnement adaptatif selon la personnalité
        val baseSpacing = when (plantPersonality) {
            PlantPersonality.TALL_STRAIGHT -> 60f // Plus écartées
            PlantPersonality.COMPACT_WIDE -> 35f  // Plus serrées
            PlantPersonality.CURVED_WILD -> (30f..70f).random() // Aléatoire
            else -> 50f
        }
        
        val isLeft: Boolean
        val position: Float
        val heightRange: Pair<Float, Float>
        val thickness: Float
        
        when (branchCount) {
            1 -> {
                isLeft = false
                position = baseSpacing
                heightRange = when (plantPersonality) {
                    PlantPersonality.TALL_STRAIGHT -> Pair(0.8f, 0.9f)
                    PlantPersonality.COMPACT_WIDE -> Pair(0.6f, 0.7f)
                    else -> Pair(0.75f, 0.85f)
                }
                thickness = 0.90f
            }
            2 -> {
                isLeft = true  
                position = -baseSpacing
                heightRange = when (plantPersonality) {
                    PlantPersonality.TALL_STRAIGHT -> Pair(0.75f, 0.85f)
                    PlantPersonality.COMPACT_WIDE -> Pair(0.55f, 0.65f)
                    else -> Pair(0.70f, 0.80f)
                }
                thickness = 0.85f
            }
            3 -> {
                isLeft = false
                position = baseSpacing * 2
                heightRange = when (plantPersonality) {
                    PlantPersonality.COMPACT_WIDE -> Pair(0.50f, 0.60f)
                    else -> Pair(0.70f, 0.80f)
                }
                thickness = 0.80f
            }
            4 -> {
                isLeft = true
                position = -baseSpacing * 2
                heightRange = when (plantPersonality) {
                    PlantPersonality.COMPACT_WIDE -> Pair(0.45f, 0.55f)
                    else -> Pair(0.70f, 0.80f)
                }
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
        
        // Variation selon la personnalité
        val personalityVariation = when (plantPersonality) {
            PlantPersonality.TALL_STRAIGHT -> 5f
            PlantPersonality.COMPACT_WIDE -> 15f
            PlantPersonality.CURVED_WILD -> 25f
            else -> 10f
        }
        
        val forcedOffset = position + (Math.random().toFloat() * personalityVariation - personalityVariation/2)
        
        val forcedAngle = when (plantPersonality) {
            PlantPersonality.TALL_STRAIGHT -> if (isLeft) -8f else +8f
            PlantPersonality.COMPACT_WIDE -> if (isLeft) -20f else +20f
            PlantPersonality.CURVED_WILD -> if (isLeft) (-25f..-5f).random() else (5f..25f).random()
            else -> if (isLeft) -12f else +12f
        }
        
        val baseHeightRatio = (heightRange.first + Math.random().toFloat() * (heightRange.second - heightRange.first))
        val branchMaxHeight = maxPossibleHeight * baseHeightRatio * dynamicGrowthStyle
        val thicknessVar = thickness
        
        val personalityFactor = when (plantPersonality) {
            PlantPersonality.TALL_STRAIGHT -> 0.98f
            PlantPersonality.COMPACT_WIDE -> 0.90f  
            PlantPersonality.CURVED_WILD -> (0.85f..1.05f).random()
            else -> 0.95f
        }
        
        val trembleFreq = when (plantPersonality) {
            PlantPersonality.TALL_STRAIGHT -> 0.8f
            PlantPersonality.COMPACT_WIDE -> 1.2f
            PlantPersonality.CURVED_WILD -> (0.6f..1.5f).random()
            else -> 1.0f
        }
        
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
    
    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + fraction * (end - start)
    }
}
