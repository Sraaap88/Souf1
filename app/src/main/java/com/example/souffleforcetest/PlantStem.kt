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
        val isMainStem: Boolean = false,
        var isActive: Boolean = true
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
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.05f
    private val maxStemHeight = 0.8f
    private val baseThickness = 25f
    private val tipThickness = 8f
    private val growthRate = 2400f
    private val oscillationDecay = 0.98f
    private val branchThreshold = 0.05f
    private val emergenceDuration = 1000L
    private val maxBranches = 4 // Max 5 tiges total (1 principale + 4 branches)
    
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
        
        // Phase de croissance normale
        if (force > forceThreshold && mainStem.isNotEmpty()) {
            growMainStem(force)
            
            // Détection ramification (souffle saccadé) - max 4 branches
            if (abs(force - lastForce) > branchThreshold && stemHeight > 20f && branchCount < maxBranches) {
                createBranch()
            }
        }
        
        // Mise à jour des oscillations
        updateOscillations()
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
    
    private fun growMainStem(force: Float) {
        // Calcul de la qualité du souffle
        val forceStability = 1f - abs(force - lastForce).coerceAtMost(0.5f) * 2f
        val qualityMultiplier = 0.5f + forceStability * 0.5f
        
        // Croissance avec courbe réaliste
        val growthProgress = stemHeight / maxPossibleHeight
        val progressCurve = 1f - growthProgress * growthProgress
        val adjustedGrowth = force * qualityMultiplier * progressCurve * growthRate * 0.016f * 10f
        
        if (adjustedGrowth > 0 && stemHeight < maxPossibleHeight) {
            stemHeight += adjustedGrowth
            
            val lastPoint = mainStem.lastOrNull() ?: return
            val segmentHeight = 8f
            val segments = (adjustedGrowth / segmentHeight).toInt().coerceAtLeast(1)
            
            for (i in 1..segments) {
                val currentHeight = stemHeight - adjustedGrowth + (adjustedGrowth * i / segments)
                val progressFromBase = currentHeight / maxPossibleHeight
                
                // Épaisseur qui diminue vers le haut
                val thickness = lerp(baseThickness, tipThickness, progressFromBase)
                
                // Position basée sur le DERNIER point (continuité)
                val lastPointX = lastPoint.x + lastPoint.oscillation + lastPoint.permanentWave
                
                // TIGE PRINCIPALE : oscillation naturelle très réduite pour rester droite
                val naturalSway = sin(currentHeight * 0.02f) * 0.8f // Réduit de 2f à 0.8f
                val currentX = lastPointX + naturalSway
                
                // Oscillation temporaire réduite pour tige principale
                val forceVariation = abs(force - lastForce) * 5f // Réduit de 10f à 5f
                val heightMultiplier = 1f + progressFromBase * 0.8f // Réduit de 1.5f à 0.8f
                val oscillation = sin(System.currentTimeMillis() * 0.005f) * forceVariation * 12f * heightMultiplier // Réduit de 25f à 12f
                
                val newY = stemBaseY - currentHeight
                val newPoint = StemPoint(currentX, newY, thickness, oscillation)
                mainStem.add(newPoint)
            }
        }
    }
    
    private fun createBranch() {
        branchCount++
        
        // Alternance des côtés pour les branches
        val branchAngle = (25f + Math.random() * 20f).toFloat() * if (branchSide) 1f else -1f
        val branchMaxHeight = maxPossibleHeight * 0.7f // Branches plus courtes que la principale
        
        val newBranch = Branch(
            angle = branchAngle, 
            startHeight = 0f, // Toutes les branches partent de la base
            isMainStem = false
        )
        
        // Point de départ à la base
        newBranch.points.add(StemPoint(stemBaseX, stemBaseY, baseThickness * 0.8f))
        
        // Créer segments initiaux pour la branche
        val branchSegments = 8
        for (i in 1..branchSegments) {
            val segmentProgress = i / branchSegments.toFloat()
            val branchHeight = branchMaxHeight * segmentProgress * 0.3f // Croissance initiale modérée
            
            val branchX = stemBaseX + cos(Math.toRadians(branchAngle.toDouble())).toFloat() * branchHeight * 0.8f
            val branchY = stemBaseY - branchHeight
            val thickness = (baseThickness * 0.8f) * (1f - segmentProgress * 0.4f)
            
            // Branches plus courbées que la principale
            val branchSway = sin(branchHeight * 0.03f) * 2f * if (branchSide) 1f else -1f
            
            newBranch.points.add(StemPoint(branchX + branchSway, branchY, thickness))
        }
        
        branches.add(newBranch)
        branchSide = !branchSide // Alternance pour la prochaine branche
    }
    
    private fun updateOscillations() {
        // Tige principale : oscillations très réduites
        for (i in mainStem.indices) {
            val point = mainStem[i]
            val heightFromBase = stemBaseY - point.y
            val heightRatio = heightFromBase / maxPossibleHeight
            
            // Décroissance plus rapide pour tige principale
            var smoothedOscillation = point.oscillation * 0.95f // Plus rapide que 0.98f
            
            // Lissage avec les points voisins
            if (i > 0 && i < mainStem.size - 1) {
                val prevOsc = mainStem[i - 1].oscillation
                val nextOsc = mainStem[i + 1].oscillation
                smoothedOscillation = (smoothedOscillation * 0.7f + prevOsc * 0.15f + nextOsc * 0.15f) * 0.95f
            }
            
            var newPermanentWave = point.permanentWave
            
            // Transfert très réduit pour tige principale
            if (abs(smoothedOscillation) > 0.5f) {
                val transferRate = 0.01f // Réduit de 0.02f à 0.01f
                newPermanentWave += smoothedOscillation * transferRate
                smoothedOscillation *= (1f - transferRate)
            }
            
            // Effet de poids très réduit pour garder la tige droite
            val accumulatedWeight = heightRatio * heightRatio * 2f // Réduit de 6f à 2f
            val weightDirection = if (point.x + newPermanentWave > stemBaseX) 1f else -1f
            val weightInfluence = accumulatedWeight * weightDirection * 0.02f // Réduit de 0.05f à 0.02f
            newPermanentWave += weightInfluence
            
            // Limites plus strictes pour tige principale
            smoothedOscillation = smoothedOscillation.coerceIn(-15f, 15f) // Réduit de 30f à 15f
            newPermanentWave = newPermanentWave.coerceIn(-25f, 25f) // Réduit de 60f à 25f
            
            mainStem[i] = point.copy(
                oscillation = smoothedOscillation,
                permanentWave = newPermanentWave
            )
        }
        
        // Branches : oscillations plus marquées
        for (branch in branches) {
            for (i in branch.points.indices) {
                val point = branch.points[i]
                val heightFromBase = stemBaseY - point.y
                val heightRatio = heightFromBase / maxPossibleHeight
                
                // Oscillations plus marquées pour les branches
                var smoothedOscillation = point.oscillation * 0.96f
                
                var newPermanentWave = point.permanentWave
                
                if (abs(smoothedOscillation) > 1f) {
                    val transferRate = 0.03f // Plus élevé que la tige principale
                    newPermanentWave += smoothedOscillation * transferRate
                    smoothedOscillation *= (1f - transferRate)
                }
                
                // Effet de poids plus marqué pour les branches
                val accumulatedWeight = heightRatio * heightRatio * 4f
                val weightDirection = if (branch.angle > 0) 1f else -1f
                val weightInfluence = accumulatedWeight * weightDirection * 0.04f
                newPermanentWave += weightInfluence
                
                // Limites plus généreuses pour les branches
                smoothedOscillation = smoothedOscillation.coerceIn(-25f, 25f)
                newPermanentWave = newPermanentWave.coerceIn(-40f, 40f)
                
                branch.points[i] = point.copy(
                    oscillation = smoothedOscillation,
                    permanentWave = newPermanentWave
                )
            }
        }
    }
    
    // ==================== UTILITAIRES ====================
    
    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + fraction * (end - start)
    }
}
