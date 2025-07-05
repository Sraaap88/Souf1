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
    
    // Instance du gestionnaire de croissance
    private val growthManager = PlantGrowthManager(this)
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.15f // Augmenté de 0.05f à 0.15f (3x plus strict)
    private val maxStemHeight = 0.8f // Remis à la hauteur originale
    private val baseThickness = 25f
    private val tipThickness = 8f
    private val growthRate = 2400f
    private val oscillationDecay = 0.98f
    private val branchThreshold = 0.12f // Augmenté de 0.05f à 0.12f pour éviter branches accidentelles
    private val emergenceDuration = 1000L
    private val maxBranches = 2 // DEUX tiges secondaires (droite + gauche)
    
    init {
        maxPossibleHeight = screenHeight * maxStemHeight
    }
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun processStemGrowth(force: Float, phaseTime: Long) {
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
            // Vérification supplémentaire : force doit être stable et suffisante
            if (force > forceThreshold * 1.2f) { // 20% au-dessus du seuil minimum
                growthManager.growMainStem(force)
                
                // Faire pousser TOUTES les branches actives
                growthManager.growAllBranches(force)
                
                // Détection ramification (souffle saccadé) - max 2 branches
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
    fun hasVisibleStem(): Boolean = mainStem.size > 1
    
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
        
        // ALTERNANCE SIMPLE ET CLAIRE : 1 = GAUCHE, 2 = DROITE
        val isLeftSide = (branchCount == 1)  // Première branche = GAUCHE
        val sideMultiplier = if (isLeftSide) -1f else 1f  // GAUCHE = -, DROITE = +
        
        // Distance différenciée pour chaque côté
        val baseDistance = if (isLeftSide) 20f else 18f
        val cumulativeOffset = baseDistance * sideMultiplier  // GAUCHE = -20, DROITE = +18
        
        // ANGLES DIFFÉRENTS selon le côté
        val minAngle = if (isLeftSide) 6f else 8f
        val maxAngle = if (isLeftSide) 13f else 15f
        val angleValue = minAngle + Math.random() * (maxAngle - minAngle)
        val branchAngle = angleValue.toFloat() * sideMultiplier // GAUCHE = négatif, DROITE = positif
        
        // HAUTEURS ET CARACTÉRISTIQUES selon le côté
        val baseHeightRatio = if (isLeftSide) 0.73f else 0.78f
        val heightVariation = (Math.random() * 0.06f - 0.03f).toFloat()
        val branchMaxHeight = maxPossibleHeight * (baseHeightRatio + heightVariation)
        
        // PERSONNALITÉS DIFFÉRENTES pour chaque côté
        val personalityFactor = if (isLeftSide) 
            (1.0f + Math.random() * 0.2f).toFloat() else 
            (0.9f + Math.random() * 0.15f).toFloat()
        val trembleFreq = (0.95f + Math.random() * 0.1f).toFloat()
        val curvatureDir = sideMultiplier // Courbe dans la direction du côté
        val thicknessVar = if (isLeftSide) 
            (0.83f + Math.random() * 0.1f).toFloat() else  
            (0.88f + Math.random() * 0.1f).toFloat()
        
        val newBranch = Branch(
            angle = branchAngle,
            startHeight = 0f,
            baseOffset = cumulativeOffset,
            isMainStem = false,
            currentHeight = 0f,
            maxHeight = branchMaxHeight,
            personalityFactor = personalityFactor,
            trembleFrequency = trembleFreq,
            curvatureDirection = curvatureDir,
            thicknessVariation = thicknessVar
        )
        
        // Point de départ avec séparation claire
        val startX = stemBaseX + cumulativeOffset + (Math.random() * 1f - 0.5f).toFloat()
        val startThickness = baseThickness * thicknessVar
        newBranch.points.add(StemPoint(startX, stemBaseY, startThickness))
        
        // Premier segment avec direction claire selon le côté
        val initialHeight = if (isLeftSide) 12f else 14f
        val initialCurve = cos(Math.toRadians(abs(branchAngle).toDouble())).toFloat() * initialHeight * 0.2f
        val initialX = startX + initialCurve * sideMultiplier // Direction selon le côté
        val initialY = stemBaseY - initialHeight
        val initialThickness = startThickness * 0.95f
        
        newBranch.points.add(StemPoint(initialX, initialY, initialThickness))
        newBranch.currentHeight = initialHeight
        
        branches.add(newBranch)
        
        // Debug TRÈS CLAIR
        println("=== BRANCHE ${branchCount} ===")
        println("Côté: ${if (isLeftSide) "GAUCHE" else "DROITE"}")
        println("Position X: ${cumulativeOffset} (${if (sideMultiplier < 0) "négatif = gauche" else "positif = droite"})")
        println("Angle: ${branchAngle}° (${if (branchAngle < 0) "négatif = gauche" else "positif = droite"})")
        println("========================")
    }
    
    // ==================== UTILITAIRES ====================
    
    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + fraction * (end - start)
    }
}
