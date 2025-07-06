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
    private val maxBranches = 6 // 6 tiges secondaires + 1 principale = 7 total
    
    init {
        maxPossibleHeight = screenHeight * maxStemHeight
        growthManager = PlantGrowthManager(this) // Initialisation après la création de l'objet
    }
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun processStemGrowth(force: Float, phaseTime: Long) {
        // INITIALISATION FORCÉE : créer le point de base dès le premier appel
        if (mainStem.isEmpty() && !isEmerging) {
            println("INITIALISATION FORCÉE - Création point de base")
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
            if (force > forceThreshold * 1.5f) { // Augmenté de 1.2f à 1.5f - encore plus strict
                growthManager.growMainStem(force)
                
                // Faire pousser TOUTES les branches actives
                growthManager.growAllBranches(force)
                
                // Détection ramification (souffle saccadé) - max 6 branches
                if (abs(force - lastForce) > branchThreshold && stemHeight > 20f && branchCount < maxBranches) {
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
        
        println("=== CRÉATION BRANCHE ${branchCount} PAR SACCADÉ ===")
        
        // PROGRESSION LOGIQUE : proche droite, proche gauche, loin droite, loin gauche, très loin droite, très loin gauche
        val (isLeft, distance) = when (branchCount) {
            1 -> Pair(false, "proche")     // Tige 1: droite proche
            2 -> Pair(true, "proche")      // Tige 2: gauche proche  
            3 -> Pair(false, "loin")       // Tige 3: droite loin
            4 -> Pair(true, "loin")        // Tige 4: gauche loin
            5 -> Pair(false, "tres_loin")  // Tige 5: droite très loin
            6 -> Pair(true, "tres_loin")   // Tige 6: gauche très loin
            else -> Pair(false, "proche")  // Fallback
        }
        
        // POSITIONS ALÉATOIRES avec espacement variable
        val basePosition = when (distance) {
            "proche" -> if (isLeft) (-40f to -60f) else (40f to 60f)           // Gauche: -40 à -60, Droite: +40 à +60
            "loin" -> if (isLeft) (-70f to -90f) else (70f to 90f)             // Gauche: -70 à -90, Droite: +70 à +90
            "tres_loin" -> if (isLeft) (-100f to -120f) else (100f to 120f)    // Gauche: -100 à -120, Droite: +100 à +120
            else -> Pair(0f, 0f)
        }
        
        val forcedOffset = (basePosition.first + Math.random() * (basePosition.second - basePosition.first)).toFloat()
        
        // ANGLES PROGRESSIVEMENT PLUS PRONONCÉS
        val forcedAngle = when (distance) {
            "proche" -> if (isLeft) -12f else +12f     // Augmenté de ±10f à ±12f
            "loin" -> if (isLeft) -17f else +17f       // Augmenté de ±15f à ±17f
            "tres_loin" -> if (isLeft) -22f else +22f  // Nouveau: ±22f
            else -> if (isLeft) -10f else +10f
        }
        
        // HAUTEURS ALÉATOIRES selon l'ordre avec variation
        val baseHeightRange = when (branchCount) {
            1 -> (0.75f to 0.85f)  // Tige 1: 75-85%
            2 -> (0.70f to 0.80f)  // Tige 2: 70-80%
            3 -> (0.65f to 0.75f)  // Tige 3: 65-75%
            4 -> (0.60f to 0.70f)  // Tige 4: 60-70%
            5 -> (0.55f to 0.65f)  // Tige 5: 55-65%
            6 -> (0.50f to 0.60f)  // Tige 6: 50-60%
            else -> (0.60f to 0.70f)
        }
        
        val baseHeightRatio = (baseHeightRange.first + Math.random() * (baseHeightRange.second - baseHeightRange.first)).toFloat()
        val branchMaxHeight = maxPossibleHeight * baseHeightRatio
        
        // ÉPAISSEURS DÉCROISSANTES selon l'ordre
        val thicknessVar = when (branchCount) {
            1 -> 0.90f  // Tige 1: 90%
            2 -> 0.85f  // Tige 2: 85%
            3 -> 0.80f  // Tige 3: 80%
            4 -> 0.75f  // Tige 4: 75%
            5 -> 0.70f  // Tige 5: 70% (nouveau)
            6 -> 0.65f  // Tige 6: 65% (nouveau)
            else -> 0.70f
        }
        
        // PERSONNALITÉS selon la position
        val personalityFactor = when (distance) {
            "proche" -> 0.95f      // Proches plus stables
            "loin" -> 1.05f        // Loin plus dynamiques
            "tres_loin" -> 1.15f   // Très loin très dynamiques
            else -> 1.0f
        }
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
        
        // Premier segment : DIVERGENCE ALÉATOIRE selon distance
        val initialHeight = 12f
        val divergenceRange = when (distance) {
            "proche" -> if (isLeft) (-50f to -70f) else (50f to 70f)           // ±50 à ±70
            "loin" -> if (isLeft) (-80f to -100f) else (80f to 100f)           // ±80 à ±100
            "tres_loin" -> if (isLeft) (-110f to -130f) else (110f to 130f)    // ±110 à ±130
            else -> if (isLeft) (-40f to -60f) else (40f to 60f)
        }
        
        val divergenceForce = (divergenceRange.first + Math.random() * (divergenceRange.second - divergenceRange.first)).toFloat()
        
        val initialX = startX + divergenceForce
        val initialY = stemBaseY - initialHeight
        val initialThickness = startThickness * 0.95f
        
        newBranch.points.add(StemPoint(initialX, initialY, initialThickness))
        newBranch.currentHeight = initialHeight
        
        branches.add(newBranch)
        
        println("Tige ${branchCount}: ${if (isLeft) "GAUCHE" else "DROITE"} ${distance.uppercase()}")
        println("Position: ${forcedOffset.toInt()}px, Divergence: ${divergenceForce.toInt()}px")
        println("Hauteur: ${(baseHeightRatio*100).toInt()}%, Épaisseur: ${(thicknessVar*100).toInt()}%, Angle: ${forcedAngle}°")
        println("================================")
    }
    
    // ==================== UTILITAIRES ====================
    
    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + fraction * (end - start)
    }
}
