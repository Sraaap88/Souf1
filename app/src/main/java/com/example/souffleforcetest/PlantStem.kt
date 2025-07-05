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
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.05f
    private val maxStemHeight = 0.8f
    private val baseThickness = 25f
    private val tipThickness = 8f
    private val growthRate = 2400f
    private val oscillationDecay = 0.98f
    private val branchThreshold = 0.05f
    private val emergenceDuration = 1000L
    
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
            
            // Détection ramification (souffle saccadé)
            if (abs(force - lastForce) > branchThreshold && stemHeight > 20f) {
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
            val wiggle = sin(progress * PI * 3 + i * 0.5) * 2f * progress
            
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
                val naturalSway = sin(currentHeight * 0.02f) * 2f
                val currentX = lastPointX + naturalSway
                
                // Oscillation temporaire selon variation du souffle
                val forceVariation = abs(force - lastForce) * 10f
                val heightMultiplier = 1f + progressFromBase * 1.5f
                val oscillation = sin(System.currentTimeMillis() * 0.005f) * forceVariation * 25f * heightMultiplier
                
                val newY = stemBaseY - currentHeight
                val newPoint = StemPoint(currentX, newY, thickness, oscillation)
                mainStem.add(newPoint)
            }
        }
    }
    
    private fun createBranch() {
        if (branches.size >= 3) return
        
        val branchStartHeight = stemHeight
        val branchAngle = (30f + Math.random() * 30f).toFloat() * if (branchSide) 1f else -1f
        
        val newBranch = Branch(angle = branchAngle, startHeight = branchStartHeight)
        
        // Point de départ depuis la tige principale
        val mainPoint = mainStem.lastOrNull()
        if (mainPoint != null) {
            newBranch.points.add(StemPoint(mainPoint.x, mainPoint.y, mainPoint.thickness * 0.7f))
            
            // Ajouter quelques segments pour visibilité
            for (i in 1..3) {
                val branchLength = i * 15f
                val branchX = mainPoint.x + cos(Math.toRadians(branchAngle.toDouble())).toFloat() * branchLength
                val branchY = mainPoint.y - sin(Math.toRadians(branchAngle.toDouble())).toFloat() * branchLength * 0.5f
                val thickness = mainPoint.thickness * 0.7f * (1f - i * 0.15f)
                
                newBranch.points.add(StemPoint(branchX, branchY, thickness))
            }
        }
        
        branches.add(newBranch)
        branchSide = !branchSide
    }
    
    private fun updateOscillations() {
        for (i in mainStem.indices) {
            val point = mainStem[i]
            
            // Calcul de la hauteur relative
            val heightFromBase = stemBaseY - point.y
            val heightRatio = heightFromBase / maxPossibleHeight
            
            // Décroissance de l'oscillation temporaire avec lissage
            var smoothedOscillation = point.oscillation * oscillationDecay
            
            // Lissage avec les points voisins
            if (i > 0 && i < mainStem.size - 1) {
                val prevOsc = mainStem[i - 1].oscillation
                val nextOsc = mainStem[i + 1].oscillation
                smoothedOscillation = (smoothedOscillation * 0.6f + prevOsc * 0.2f + nextOsc * 0.2f) * oscillationDecay
            }
            
            // GARDE LA POSITION ACQUISE : pas de retour au centre
            var newPermanentWave = point.permanentWave
            
            // Transfert progressif de l'oscillation vers la forme permanente
            if (abs(smoothedOscillation) > 1f) {
                val transferRate = 0.02f
                newPermanentWave += smoothedOscillation * transferRate
                smoothedOscillation *= (1f - transferRate)
            }
            
            // Effet de poids qui s'ajoute sans revenir au centre
            val accumulatedWeight = heightRatio * heightRatio * 6f
            val weightDirection = if (point.x + newPermanentWave > stemBaseX) 1f else -1f
            val weightInfluence = accumulatedWeight * weightDirection * 0.05f
            newPermanentWave += weightInfluence
            
            // Limites généreuses pour formes organiques
            smoothedOscillation = smoothedOscillation.coerceIn(-30f, 30f)
            newPermanentWave = newPermanentWave.coerceIn(-60f, 60f)
            
            mainStem[i] = point.copy(
                oscillation = smoothedOscillation,
                permanentWave = newPermanentWave
            )
        }
    }
    
    // ==================== UTILITAIRES ====================
    
    private fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + fraction * (end - start)
    }
}
