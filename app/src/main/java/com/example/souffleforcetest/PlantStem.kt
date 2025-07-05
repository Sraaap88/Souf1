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
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.05f
    private val maxStemHeight = 0.8f // Remis à la hauteur originale
    private val baseThickness = 25f
    private val tipThickness = 8f
    private val growthRate = 2400f
    private val oscillationDecay = 0.98f
    private val branchThreshold = 0.05f
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
        
        // Phase de croissance normale - SEULEMENT avec souffle actif
        if (force > forceThreshold && mainStem.isNotEmpty()) {
            growMainStem(force)
            
            // Faire pousser TOUTES les branches actives
            growAllBranches(force)
            
            // Détection ramification (souffle saccadé) - max 4 branches
            if (abs(force - lastForce) > branchThreshold && stemHeight > 20f && branchCount < maxBranches) {
                createBranch()
            }
        }
        
        // Mise à jour des oscillations même sans souffle
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
        val adjustedGrowth = force * qualityMultiplier * progressCurve * growthRate * 0.008f * 10f // Divisé par 2 (0.016f → 0.008f)
        
        if (adjustedGrowth > 0 && stemHeight < maxPossibleHeight) {
            stemHeight += adjustedGrowth
            
            val lastPoint = mainStem.lastOrNull() ?: return
            val segmentHeight = 7f + (Math.random() * 3f).toFloat() // Segments variables 7-10px
            val segments = (adjustedGrowth / segmentHeight).toInt().coerceAtLeast(1)
            
            for (i in 1..segments) {
                val currentHeight = stemHeight - adjustedGrowth + (adjustedGrowth * i / segments)
                val progressFromBase = currentHeight / maxPossibleHeight
                
                // Épaisseur avec micro-variations pour tige principale
                val microVariation = (Math.random() * 0.08f - 0.04f).toFloat() // ±4%
                val thickness = lerp(baseThickness, tipThickness, progressFromBase) * (1f + microVariation)
                
                // Position basée sur le DERNIER point (continuité)
                val lastPointX = lastPoint.x + lastPoint.oscillation + lastPoint.permanentWave
                
                // TIGE PRINCIPALE : oscillation naturelle très réduite + micro-tremblements
                val naturalSway = sin(currentHeight * 0.018f) * 0.6f // Légère ondulation
                val microTremble = sin(System.currentTimeMillis() * 0.008f) * 0.3f // Tremblement subtil
                val currentX = lastPointX + naturalSway + microTremble
                
                // Oscillation temporaire réduite pour tige principale
                val forceVariation = abs(force - lastForce) * 4f // Encore plus réduit
                val heightMultiplier = 1f + progressFromBase * 0.6f
                val oscillation = sin(System.currentTimeMillis() * 0.004f) * forceVariation * 8f * heightMultiplier
                
                val newY = stemBaseY - currentHeight
                val newPoint = StemPoint(currentX, newY, thickness, oscillation)
                mainStem.add(newPoint)
            }
        }
    }
    
    private fun createBranch() {
        branchCount++
        
        // ALTERNANCE GARANTIE : première droite, deuxième gauche
        val sideMultiplier = if (branchCount == 1) 1f else -1f // Première = droite, deuxième = gauche
        
        // Distance et hauteur différenciées pour chaque tige
        val baseDistance = if (branchCount == 1) 18f else 15f // Première plus loin
        val cumulativeOffset = baseDistance * sideMultiplier
        
        // ANGLES LÉGÈREMENT DIFFÉRENTS pour éviter la symétrie parfaite
        val minAngle = if (branchCount == 1) 8f else 5f // Première plus inclinée
        val maxAngle = if (branchCount == 1) 15f else 12f
        val branchAngle = (minAngle + Math.random() * (maxAngle - minAngle)).toFloat() * sideMultiplier
        
        // HAUTEURS DIFFÉRENTES pour effet naturel
        val baseHeightRatio = if (branchCount == 1) 0.80f else 0.75f // Première plus haute
        val heightVariation = (Math.random() * 0.08f - 0.04f).toFloat() // ±4%
        val branchMaxHeight = maxPossibleHeight * (baseHeightRatio + heightVariation)
        
        // PERSONNALITÉS DIFFÉRENTES pour chaque tige
        val personalityFactor = if (branchCount == 1) 
            (0.9f + Math.random() * 0.15f).toFloat() else // Première: 0.9-1.05
            (1.0f + Math.random() * 0.2f).toFloat()       // Deuxième: 1.0-1.2
        val trembleFreq = (0.95f + Math.random() * 0.1f).toFloat()
        val curvatureDir = if (branchCount == 1) 1f else -1f // Première droite, deuxième gauche
        val thicknessVar = if (branchCount == 1) 
            (0.90f + Math.random() * 0.1f).toFloat() else  // Première plus épaisse
            (0.85f + Math.random() * 0.1f).toFloat()       // Deuxième plus fine
        
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
        
        // Point de départ avec position spécifique pour chaque tige
        val startX = stemBaseX + cumulativeOffset + (Math.random() * 1f - 0.5f).toFloat()
        val startThickness = baseThickness * thicknessVar
        newBranch.points.add(StemPoint(startX, stemBaseY, startThickness))
        
        // Premier segment avec caractéristiques uniques
        val initialHeight = if (branchCount == 1) 14f else 12f // Première plus haute
        val initialCurve = cos(Math.toRadians(branchAngle.toDouble())).toFloat() * initialHeight * 0.15f
        val initialX = startX + initialCurve
        val initialY = stemBaseY - initialHeight
        val initialThickness = startThickness * 0.95f
        
        newBranch.points.add(StemPoint(initialX, initialY, initialThickness))
        newBranch.currentHeight = initialHeight
        
        branches.add(newBranch)
        // Pas d'alternance automatique, contrôle manuel pour 2 tiges
    }
    
    private fun growAllBranches(force: Float) {
        for (branch in branches.filter { it.isActive }) {
            growBranch(branch, force)
        }
    }
    
    private fun growBranch(branch: Branch, force: Float) {
        if (branch.currentHeight >= branch.maxHeight) return
        
        // Vitesse de croissance TRÈS PROCHE de la principale
        val branchGrowthMultiplier = 0.95f * branch.personalityFactor // 95% au lieu de 75%
        val forceStability = 1f - abs(force - lastForce).coerceAtMost(0.5f) * 2f
        val qualityMultiplier = 0.5f + forceStability * 0.5f
        
        val growthProgress = branch.currentHeight / branch.maxHeight
        val progressCurve = 1f - growthProgress * growthProgress
        val adjustedGrowth = force * qualityMultiplier * progressCurve * growthRate * 0.008f * 9f * branchGrowthMultiplier // Divisé par 2
        
        if (adjustedGrowth > 0) {
            branch.currentHeight += adjustedGrowth
            
            val lastPoint = branch.points.lastOrNull() ?: return
            val segmentHeight = 7f + (Math.random() * 2f).toFloat() // 7-9px (plus régulier)
            val segments = (adjustedGrowth / segmentHeight).toInt().coerceAtLeast(1)
            
            for (i in 1..segments) {
                val currentBranchHeight = branch.currentHeight - adjustedGrowth + (adjustedGrowth * i / segments)
                val progressFromBase = currentBranchHeight / branch.maxHeight
                
                // Épaisseur TRÈS SIMILAIRE à la principale
                val baseThicknessBranch = baseThickness * 0.9f * branch.thicknessVariation // 90% au lieu de 70%
                val thicknessProgress = progressFromBase * 0.4f // Diminution PLUS graduelle
                val microVariation = (Math.random() * 0.03f - 0.015f).toFloat() // ±1.5% très subtil
                val thickness = baseThicknessBranch * (1f - thicknessProgress + microVariation)
                
                // Position avec courbure adaptée à chaque tige
                val lastPointX = lastPoint.x + lastPoint.oscillation + lastPoint.permanentWave
                
                // Courbe de base selon l'angle
                val branchCurve = cos(Math.toRadians(branch.angle.toDouble())).toFloat() * currentBranchHeight * 0.3f
                
                // EFFET DE POIDS adapté à la personnalité
                val branchHeightRatio = currentBranchHeight / branch.maxHeight
                val branchWeightEffect = branchHeightRatio * branchHeightRatio * (1.5f + branch.personalityFactor * 0.5f)
                val branchWeightDirection = if (branch.angle > 0) 1f else -1f
                val branchWeightBend = branchWeightEffect * branchWeightDirection * 0.7f
                
                // Courbure naturelle selon la personnalité
                val naturalCurve = sin(currentBranchHeight * 0.006f * branch.personalityFactor) * 
                                 branch.curvatureDirection * 
                                 (0.2f + progressFromBase * 0.4f) * branch.personalityFactor
                
                // Tremblements uniques par tige
                val timeOffset = branch.angle * 8f + (if (branch.angle > 0) 0f else 1000f) // Déphasage
                val tremble = sin((System.currentTimeMillis() + timeOffset) * 0.0015f * branch.trembleFrequency) * 
                             (0.08f + progressFromBase * 0.15f) * branch.personalityFactor
                
                val currentX = (stemBaseX + branch.baseOffset) + branchCurve + branchWeightBend + naturalCurve + tremble
                val currentY = stemBaseY - currentBranchHeight
                
                // Oscillation adaptée à chaque tige
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
            
            // Transfert QUASI-NUL pour tige principale droite
            if (abs(smoothedOscillation) > 0.3f) {
                val transferRate = 0.005f // Ultra réduit
                newPermanentWave += smoothedOscillation * transferRate
                smoothedOscillation *= (1f - transferRate)
            }
            
            // Effet de poids PLUS PRONONCÉ pour courbure réaliste
            val accumulatedWeight = heightRatio * heightRatio * 2f // Augmenté de 0.5f à 2f
            val weightDirection = if (point.x + newPermanentWave > stemBaseX) 1f else -1f
            val weightInfluence = accumulatedWeight * weightDirection * 0.015f // Augmenté de 0.005f à 0.015f
            newPermanentWave += weightInfluence
            
            // Limites plus généreuses pour permettre courbure naturelle
            smoothedOscillation = smoothedOscillation.coerceIn(-5f, 5f) // Augmenté de 3f à 5f
            newPermanentWave = newPermanentWave.coerceIn(-15f, 15f) // Augmenté de 6f à 15f
            
            mainStem[i] = point.copy(
                oscillation = smoothedOscillation,
                permanentWave = newPermanentWave
            )
        }
        
        // Branches : oscillations TRÈS RÉDUITES pour marguerite réaliste
        for (branch in branches) {
            for (i in branch.points.indices) {
                val point = branch.points[i]
                val heightFromBase = stemBaseY - point.y
                val heightRatio = heightFromBase / maxPossibleHeight
                
                // Oscillations TRÈS SUBTILES
                var smoothedOscillation = point.oscillation * (0.97f + branch.personalityFactor * 0.01f) // Plus stable
                
                var newPermanentWave = point.permanentWave
                
                if (abs(smoothedOscillation) > 0.5f) { // Seuil plus élevé
                    val transferRate = 0.01f * branch.personalityFactor // Très réduit
                    newPermanentWave += smoothedOscillation * transferRate
                    smoothedOscillation *= (1f - transferRate)
                }
                
                // Effet de poids TRÈS LÉGER pour rester vertical
                val accumulatedWeight = heightRatio * heightRatio * (1f + branch.personalityFactor * 0.5f) // Très réduit
                val weightDirection = if (branch.angle > 0) 1f else -1f
                val weightInfluence = accumulatedWeight * weightDirection * (0.008f + branch.personalityFactor * 0.002f) // Très réduit
                newPermanentWave += weightInfluence
                
                // Courbure QUASI-NULLE pour marguerite droite
                val naturalBend = sin(heightFromBase * 0.008f) * branch.curvatureDirection * branch.personalityFactor * 0.2f // Très réduit
                newPermanentWave += naturalBend * 0.005f // Très réduit
                
                // Limites TRÈS STRICTES pour rester proche de la verticale
                val flexibilityFactor = branch.personalityFactor * 0.5f // Réduit la flexibilité
                val oscLimit = 8f + (flexibilityFactor * 4f) // 8-10px max
                val waveLimit = 15f + (flexibilityFactor * 5f) // 15-17.5px max
                
                smoothedOscillation = smoothedOscillation.coerceIn(-oscLimit, oscLimit)
                newPermanentWave = newPermanentWave.coerceIn(-waveLimit, waveLimit)
                
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
