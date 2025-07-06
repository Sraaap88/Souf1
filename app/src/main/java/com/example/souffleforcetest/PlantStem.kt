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
    
    // Instance du gestionnaire de croissance - initialisation tardive
    private lateinit var growthManager: PlantGrowthManager
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.25f // Augmenté de 0.15f à 0.25f (moins sensible)
    private val maxStemHeight = 0.8f // Remis à la hauteur originale
    private val baseThickness = 25f
    private val tipThickness = 8f
    private val growthRate = 2400f
    private val oscillationDecay = 0.98f
    private val branchThreshold = 0.18f // Augmenté de 0.12f à 0.18f pour éviter branches accidentelles
    private val emergenceDuration = 1000L
    private val maxBranches = 5 // 5 tiges secondaires + 1 principale = 6 total
    
    init {
        maxPossibleHeight = screenHeight * maxStemHeight
        growthManager = PlantGrowthManager(this) // Initialisation après la création de l'objet
    }
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun processStemGrowth(force: Float, phaseTime: Long) {
        // INITIALISATION FORCÉE : créer le point de base dès le premier appel
        if (mainStem.isEmpty() && !isEmerging) {
            isEmerging = true
            emergenceStartTime = System.currentTimeMillis()
            // Créer immédiatement un point de base visible
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
            // Vérification RENFORCÉE : force doit être stable et forte
            if (force > forceThreshold * 1.5f) {
                growthManager.growMainStem(force)
                
                // Faire pousser TOUTES les branches actives - SIMPLE
                growthManager.growAllBranches(force)
                
                // Détection ramification (souffle saccadé) - SEUIL RÉDUIT pour créer plus facilement
                if (abs(force - lastForce) > 0.12f && stemHeight > 20f && branchCount < maxBranches) {
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
    }
    
    fun getStemHeight(): Float = stemHeight
    fun hasVisibleStem(): Boolean = mainStem.size >= 1 // Au moins 1 point pour être visible
    
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
    
    // ==================== FONCTIONS PRIVÉES ====================
    
    private fun createEmergenceStem(progress: Float) {
        mainStem.clear()
        val emergenceHeight = 30f * progress
        
        for (i in 0..5) {
            val segmentProgress = i / 5f
            val y = stemBaseY - emergenceHeight * segmentProgress
            val thickness = lerp(baseThickness, tipThickness, segmentProgress * 0.3f)
            // Tige principale PRESQUE DROITE - oscillation très réduite
            val wiggle = sin(progress * PI * 3 + i * 0.5) * 0.5f * progress
            
            mainStem.add(StemPoint(stemBaseX + wiggle.toFloat(), y, thickness))
        }
        
        if (progress >= 1f) {
            stemHeight = emergenceHeight
        }
    }
    
    private fun createBranch() {
        branchCount++
        
        // NOUVELLE APPROCHE : Les 3 premières comme avant, les 2 nouvelles IDENTIQUES aux tiges 2-3
        val (isLeft, basePos, heightRange, thickness) = when (branchCount) {
            1 -> Pair(false, 40f to 60f, 0.75f to 0.85f, 0.90f)      // Tige 1: droite proche ✅
            2 -> Pair(true, -60f to -40f, 0.70f to 0.80f, 0.85f)     // Tige 2: gauche proche ✅  
            3 -> Pair(false, 70f to 90f, 0.70f to 0.80f, 0.80f)      // Tige 3: droite loin ✅
            4 -> Pair(true, -80f to -60f, 0.70f to 0.80f, 0.85f)     // Tige 4: COPIE de tige 2 (gauche)
            5 -> Pair(false, 90f to 110f, 0.70f to 0.80f, 0.80f)     // Tige 5: COPIE de tige 3 (droite)
            else -> Pair(false, 40f to 60f, 0.70f to 0.80f, 0.80f)
        }
        
        val forcedOffset = (basePos.first + Math.random() * (basePos.second - basePos.first)).toFloat()
        val forcedAngle = if (isLeft) -12f else +12f
        val baseHeightRatio = (heightRange.first + Math.random() * (heightRange.second - heightRange.first)).toFloat()
        val branchMaxHeight = maxPossibleHeight * baseHeightRatio
        val thicknessVar = thickness
        
        // PERSONNALITÉ : Identique pour tiges 4-5 comme tiges 2-3
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
        
        // Point de départ IDENTIQUE pour toutes les tiges (même base)
        val startX = stemBaseX // MÊME POINT pour toutes
        val startThickness = baseThickness * thicknessVar
        newBranch.points.add(StemPoint(startX, stemBaseY, startThickness))
        
        // DIVERGENCES : Nouvelles tiges COPIENT les tiges 2-3
        val divergenceForce = when (branchCount) {
            1 -> (50f + Math.random() * 20f).toFloat()         // Droite: 50-70
            2 -> (-70f + Math.random() * 20f).toFloat()        // Gauche: -70 à -50
            3 -> (80f + Math.random() * 20f).toFloat()         // Droite: 80-100
            4 -> (-80f + Math.random() * 20f).toFloat()        // Gauche: -80 à -60 (comme tige 2)
            5 -> (90f + Math.random() * 20f).toFloat()         // Droite: 90-110 (comme tige 3)
            else -> (60f).toFloat()
        }
        
        
        val initialHeight = 12f
        val initialX = startX + divergenceForce
        val initialY = stemBaseY - initialHeight
        val initialThickness = startThickness * 0.95f
        
        newBranch.points.add(StemPoint(initialX, initialY, initialThickness))
        newBranch.currentHeight = initialHeight
        
        branches.add(newBranch)
        
        println("Tige ${branchCount}: ${if (isLeft) "GAUCHE" else "DROITE"} créée")
    }
    
    // ==================== UTILITAIRES ====================
    
    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + fraction * (end - start)
    }
}
