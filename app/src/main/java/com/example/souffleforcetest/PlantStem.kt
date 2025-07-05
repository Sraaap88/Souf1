package com.example.souffleforcetest

import kotlin.math.*

class PlantStem(private val screenWidth: Int, private val screenHeight: Int) {
    
    // ==================== DATA CLASSES ====================
    
    data class StemPoint(
        val x: Float,
        val y: Float,
        val thickness: Float,
        var oscillation: Float = 0f,
        var permanentWave: Float = 0f,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    data class Branch(
        val points: MutableList<StemPoint> = mutableListOf(),
        val angle: Float,
        val startHeight: Float,
        var isActive: Boolean = true,
        var currentLength: Float = 0f,
        val maxLength: Float = 100f + Math.random().toFloat() * 50f // Longueur variable
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
    private val oscillationDecay = 0.97f // Plus lent pour courbes douces
    private val permanentWaveDecay = 0.985f // Retour progressif vers 50%
    private val branchGrowthRate = 800f // Vitesse de croissance des branches
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
            // Croissance tige principale
            growMainStem(force)
            
            // Croissance des branches
            growBranches(force)
            
            // Détection ramification (souffle saccadé)
            if (abs(force - lastForce) > 0.05f && stemHeight > 20f) {
                createBranch()
            }
        }
        
        // Mise à jour des oscillations (rendu plus fluide)
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
                
                // Position X avec légère ondulation naturelle
                val naturalSway = sin(currentHeight * 0.02f) * 3f
                val currentX = stemBaseX + naturalSway
                
                // Oscillation temporaire selon variation du souffle
                val forceVariation = abs(force - lastForce) * 15f // Plus sensible
                val oscillation = sin(System.currentTimeMillis() * 0.005f) * forceVariation * 25f
                
                val newY = stemBaseY - currentHeight
                val newPoint = StemPoint(currentX, newY, thickness, oscillation)
                mainStem.add(newPoint)
            }
        }
    }
    
    private fun growBranches(force: Float) {
        for (branch in branches.filter { it.isActive }) {
            if (branch.currentLength < branch.maxLength) {
                // Croissance de la branche
                val branchGrowth = force * branchGrowthRate * 0.016f * 5f
                branch.currentLength += branchGrowth
                
                // Ajouter des segments à la branche
                if (branch.points.isNotEmpty()) {
                    val lastBranchPoint = branch.points.last()
                    val growthSteps = (branchGrowth / 5f).toInt().coerceAtLeast(1)
                    
                    for (step in 1..growthSteps) {
                        val segmentLength = branchGrowth / growthSteps * step
                        val totalLength = branch.currentLength - branchGrowth + segmentLength
                        
                        // Position selon l'angle de la branche
                        val branchX = branch.points[0].x + cos(Math.toRadians(branch.angle.toDouble())).toFloat() * totalLength
                        val branchY = branch.points[0].y - sin(Math.toRadians(abs(branch.angle).toDouble())).toFloat() * totalLength * 0.3f
                        
                        // Épaisseur qui diminue
                        val branchProgress = totalLength / branch.maxLength
                        val thickness = baseThickness * 0.6f * (1f - branchProgress * 0.5f)
                        
                        // Oscillation de la branche (moins que la tige principale)
                        val branchOscillation = sin(System.currentTimeMillis() * 0.003f) * abs(force - lastForce) * 8f
                        
                        val newBranchPoint = StemPoint(branchX, branchY, thickness, branchOscillation)
                        branch.points.add(newBranchPoint)
                    }
                }
            }
        }
    }
    
    private fun createBranch() {
        if (branches.size >= 3) return
        
        val branchStartHeight = stemHeight
        val branchAngle = (25f + Math.random() * 35f).toFloat() * if (branchSide) 1f else -1f
        
        // Créer la branche avec point de départ
        val newBranch = Branch(angle = branchAngle, startHeight = branchStartHeight)
        
        // Point de départ depuis la tige principale
        val mainPoint = mainStem.lastOrNull()
        if (mainPoint != null) {
            newBranch.points.add(StemPoint(mainPoint.x, mainPoint.y, mainPoint.thickness * 0.7f))
        }
        
        branches.add(newBranch)
        branchSide = !branchSide
    }
    
    private fun updateOscillations() {
        val currentTime = System.currentTimeMillis()
        
        // Mise à jour tige principale avec courbes fluides
        for (i in mainStem.indices) {
            val point = mainStem[i]
            
            // Décroissance progressive de l'oscillation
            val timeDecay = min(1f, (currentTime - point.timestamp) / 2000f) // 2 secondes pour décroissance
            val decayedOscillation = point.oscillation * pow(oscillationDecay, timeDecay)
            
            // Conversion progressive vers onde permanente (50% final)
            val permanentContribution = point.oscillation * 0.005f // 0.5% par frame vers permanent
            val newPermanentWave = (point.permanentWave + permanentContribution) * permanentWaveDecay
            
            // Lissage pour éviter les zigzags
            val smoothedOscillation = if (i > 0 && i < mainStem.size - 1) {
                val prevOsc = mainStem[i - 1].oscillation * 0.3f
                val nextOsc = if (i + 1 < mainStem.size) mainStem[i + 1].oscillation * 0.3f else 0f
                decayedOscillation * 0.4f + prevOsc + nextOsc
            } else {
                decayedOscillation
            }
            
            mainStem[i] = point.copy(
                oscillation = smoothedOscillation,
                permanentWave = newPermanentWave
            )
        }
        
        // Mise à jour des branches
        for (branch in branches) {
            for (i in branch.points.indices) {
                val point = branch.points[i]
                val decayedOscillation = point.oscillation * oscillationDecay
                val newPermanentWave = point.permanentWave * permanentWaveDecay
                
                branch.points[i] = point.copy(
                    oscillation = decayedOscillation,
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
