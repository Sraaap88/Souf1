package com.example.souffleforcetest

import kotlin.math.*

class PlantGrowthManager(private val plantStem: PlantStem) {
    
    // ==================== FONCTIONS DE CROISSANCE ====================
    
    fun growMainStem(force: Float) {
        // Calcul de la qualité du souffle
        val forceStability = 1f - abs(force - plantStem.getLastForce()).coerceAtMost(0.5f) * 2f
        val qualityMultiplier = 0.5f + forceStability * 0.5f
        
        // Croissance avec courbe réaliste
        val growthProgress = plantStem.getStemHeight() / plantStem.getMaxPossibleHeight()
        val progressCurve = 1f - growthProgress * growthProgress
        val adjustedGrowth = force * qualityMultiplier * progressCurve * plantStem.getGrowthRate() * 0.008f * 10f // Divisé par 2 (0.016f → 0.008f)
        
        if (adjustedGrowth > 0 && plantStem.getStemHeight() < plantStem.getMaxPossibleHeight()) {
            val newHeight = plantStem.getStemHeight() + adjustedGrowth
            plantStem.setStemHeight(newHeight)
            
            val lastPoint = plantStem.mainStem.lastOrNull() ?: return
            val segmentHeight = 7f + (Math.random() * 3f).toFloat() // Segments variables 7-10px
            val segments = (adjustedGrowth / segmentHeight).toInt().coerceAtLeast(1)
            
            for (i in 1..segments) {
                val currentHeight = newHeight - adjustedGrowth + (adjustedGrowth * i / segments)
                val progressFromBase = currentHeight / plantStem.getMaxPossibleHeight()
                
                // Épaisseur avec micro-variations pour tige principale
                val microVariation = (Math.random() * 0.08f - 0.04f).toFloat() // ±4%
                val thickness = lerp(plantStem.getBaseThickness(), plantStem.getTipThickness(), progressFromBase) * (1f + microVariation)
                
                // Position basée sur le DERNIER point (continuité)
                val lastPointX = lastPoint.x + lastPoint.oscillation + lastPoint.permanentWave
                
                // TIGE PRINCIPALE : courbure réaliste comme vraie marguerite
                val baseDeviation = lastPointX - plantStem.getStemBaseX()
                val heightRatio = currentHeight / plantStem.getMaxPossibleHeight()
                
                // Force de rappel TRÈS DOUCE qui permet la courbure naturelle
                val centeringStrength = (1f - heightRatio * 0.95f) * 0.005f // Ultra doux
                val centeringForce = -baseDeviation * centeringStrength
                
                // Mouvement naturel TRÈS fluide
                val naturalSway = sin(currentHeight * 0.002f) * 0.08f * (1f - heightRatio * 0.2f)
                val microTremble = sin(System.currentTimeMillis() * 0.001f) * 0.02f
                
                // EFFET DE POIDS RÉALISTE - commence dès 20% de hauteur
                val weightStart = 0.2f
                val weightStrength = if (heightRatio > weightStart) {
                    val adjustedRatio = (heightRatio - weightStart) / (1f - weightStart)
                    adjustedRatio * adjustedRatio * adjustedRatio * 3.5f // Courbure cubique très douce
                } else 0f
                
                // Direction de poids : légère tendance vers la droite (naturel)
                val weightDirection = if (baseDeviation > -1f) 0.6f else -0.3f
                val weightBend = weightStrength * weightDirection
                
                // Lissage ULTRA étendu sur 5 points pour fluidité maximale
                val smoothingRange = 5
                var smoothedMovement = 0f
                var totalWeight = 0f
                
                for (j in 1..smoothingRange) {
                    val prevIndex = plantStem.mainStem.size - j
                    if (prevIndex >= 0) {
                        val weight = 1f / (j * j) // Poids décroissant quadratique
                        val prevPoint = plantStem.mainStem[prevIndex]
                        val prevMovement = prevPoint.x - plantStem.getStemBaseX()
                        smoothedMovement += prevMovement * weight
                        totalWeight += weight
                    }
                }
                
                if (totalWeight > 0) smoothedMovement /= totalWeight
                
                // Mouvement total avec lissage ultra-fluide
                val rawMovement = naturalSway + microTremble + centeringForce + weightBend
                val smoothingFactor = 0.8f + heightRatio * 0.15f // Lissage intense
                val finalMovement = rawMovement * (1f - smoothingFactor) + smoothedMovement * smoothingFactor * 0.25f
                
                // Limitation très permissive pour courbure naturelle
                val maxMove = 3f * (1f - heightRatio * 0.2f)
                val limitedMovement = finalMovement.coerceIn(-maxMove, maxMove)
                
                val currentX = lastPointX + limitedMovement
                
                // Oscillation temporaire avec lissage vers le sommet
                val forceVariation = abs(force - plantStem.getLastForce()) * 1f
                val heightMultiplier = 1f + progressFromBase * 0.1f * (1f - progressFromBase) // Réduit vers le sommet
                val oscillation = sin(System.currentTimeMillis() * 0.002f) * forceVariation * 2f * heightMultiplier
                
                val newY = plantStem.getStemBaseY() - currentHeight
                val newPoint = PlantStem.StemPoint(currentX, newY, thickness, oscillation)
                plantStem.mainStem.add(newPoint)
            }
        }
    }
    
    fun growAllBranches(force: Float) {
        for (branch in plantStem.branches.filter { it.isActive }) {
            growBranch(branch, force)
        }
    }
    
    private fun growBranch(branch: PlantStem.Branch, force: Float) {
        if (branch.currentHeight >= branch.maxHeight) return
        
        // DEBUG: Vérifier les valeurs de la branche
        if (branch.points.size <= 2) { // Seulement au début
            println("GROW BRANCH - baseOffset: ${branch.baseOffset}, angle: ${branch.angle}")
        }
        
        // Vitesse de croissance TRÈS PROCHE de la principale
        val branchGrowthMultiplier = 0.95f * branch.personalityFactor // 95% au lieu de 75%
        val forceStability = 1f - abs(force - plantStem.getLastForce()).coerceAtMost(0.5f) * 2f
        val qualityMultiplier = 0.5f + forceStability * 0.5f
        
        val growthProgress = branch.currentHeight / branch.maxHeight
        val progressCurve = 1f - growthProgress * growthProgress
        val adjustedGrowth = force * qualityMultiplier * progressCurve * plantStem.getGrowthRate() * 0.008f * 9f * branchGrowthMultiplier // Divisé par 2
        
        if (adjustedGrowth > 0) {
            branch.currentHeight += adjustedGrowth
            
            val lastPoint = branch.points.lastOrNull() ?: return
            val segmentHeight = 7f + (Math.random() * 2f).toFloat() // 7-9px (plus régulier)
            val segments = (adjustedGrowth / segmentHeight).toInt().coerceAtLeast(1)
            
            for (i in 1..segments) {
                val currentBranchHeight = branch.currentHeight - adjustedGrowth + (adjustedGrowth * i / segments)
                val progressFromBase = currentBranchHeight / branch.maxHeight
                
                // Épaisseur TRÈS SIMILAIRE à la principale
                val baseThicknessBranch = plantStem.getBaseThickness() * 0.9f * branch.thicknessVariation // 90% au lieu de 70%
                val thicknessProgress = progressFromBase * 0.4f // Diminution PLUS graduelle
                val microVariation = (Math.random() * 0.03f - 0.015f).toFloat() // ±1.5% très subtil
                val thickness = baseThicknessBranch * (1f - thicknessProgress + microVariation)
                
                // Position avec courbure adaptée à chaque tige
                val lastPointX = lastPoint.x + lastPoint.oscillation + lastPoint.permanentWave
                
                // POSITION ultra-réaliste comme vraies marguerites
                val baseX = plantStem.getStemBaseX() + branch.baseOffset
                
                // COURBURE ÉLÉGANTE ET FLUIDE selon le côté
                val branchDirection = if (branch.angle < 0) -1f else 1f
                val heightRatio = currentBranchHeight / branch.maxHeight
                
                // PHASE 1: Départ légèrement incliné (0-30%)
                val earlyIncline = if (heightRatio < 0.3f) {
                    val earlyRatio = heightRatio / 0.3f
                    earlyRatio * earlyRatio * 8f * branchDirection
                } else 8f * branchDirection
                
                // PHASE 2: Courbure gracieuse (30-70%)
                val midCurve = if (heightRatio > 0.3f && heightRatio < 0.7f) {
                    val midRatio = (heightRatio - 0.3f) / 0.4f
                    val curveIntensity = sin(midRatio * PI.toFloat()) * 15f // Courbe en cloche
                    curveIntensity * branchDirection
                } else 0f
                
                // PHASE 3: Courbure finale élégante (70-100%)
                val finalCurve = if (heightRatio > 0.7f) {
                    val finalRatio = (heightRatio - 0.7f) / 0.3f
                    val elegantCurve = finalRatio * finalRatio * finalRatio * 20f // Courbure cubique
                    val downwardBend = finalRatio * finalRatio * 8f // Vers le bas
                    elegantCurve * branchDirection + downwardBend * abs(branchDirection) * 0.3f
                } else 0f
                
                // EFFET DE POIDS RÉALISTE progressif
                val naturalWeight = heightRatio * heightRatio * heightRatio * 12f * abs(branchDirection)
                val weightSway = sin(heightRatio * PI.toFloat() * 0.5f) * naturalWeight * 0.4f
                
                // LISSAGE FLUIDE pour transitions douces
                val totalCurve = earlyIncline + midCurve + finalCurve + weightSway
                
                // Position finale ultra-fluide
                val currentX = baseX + totalCurve
                val currentY = plantStem.getStemBaseY() - currentBranchHeight
                
                // DEBUG: Courbure réaliste par phases
                if (branch.points.size <= 5) {
                    val phase = when {
                        heightRatio < 0.3f -> "DÉPART"
                        heightRatio < 0.7f -> "MILIEU" 
                        else -> "SOMMET"
                    }
                    println("Branche ${if (branch.angle < 0) "GAUCHE" else "DROITE"} - ${phase}: X=${currentX.toInt()} (courbe=${totalCurve.toInt()}px)")
                }
                
                // Oscillation adaptée à chaque tige
                val forceVariation = abs(force - plantStem.getLastForce()) * 2.5f * branch.personalityFactor
                val heightMultiplier = 1f + progressFromBase * 0.4f
                val phaseOffset = branch.angle * 0.08f + (if (branch.angle > 0) 0f else PI.toFloat())
                val oscillation = sin(System.currentTimeMillis() * 0.0025f * branch.trembleFrequency + phaseOffset) * 
                                forceVariation * 5f * heightMultiplier
                
                val newPoint = PlantStem.StemPoint(currentX, currentY, thickness, oscillation)
                branch.points.add(newPoint)
            }
        }
    }
    
    fun updateOscillations() {
        // Tige principale : oscillations très réduites
        for (i in plantStem.mainStem.indices) {
            val point = plantStem.mainStem[i]
            val heightFromBase = plantStem.getStemBaseY() - point.y
            val heightRatio = heightFromBase / plantStem.getMaxPossibleHeight()
            
            // Décroissance plus rapide pour tige principale
            var smoothedOscillation = point.oscillation * 0.95f // Plus rapide que 0.98f
            
            // Lissage avec les points voisins
            if (i > 0 && i < plantStem.mainStem.size - 1) {
                val prevOsc = plantStem.mainStem[i - 1].oscillation
                val nextOsc = plantStem.mainStem[i + 1].oscillation
                smoothedOscillation = (smoothedOscillation * 0.7f + prevOsc * 0.15f + nextOsc * 0.15f) * 0.95f
            }
            
            var newPermanentWave = point.permanentWave
            
            // Transfert avec lissage progressif
            if (abs(smoothedOscillation) > 0.2f) {
                val transferRate = 0.008f * (1f + heightRatio) // Plus de transfert vers le haut
                newPermanentWave += smoothedOscillation * transferRate
                smoothedOscillation *= (1f - transferRate)
            }
            
            // Effet de poids avec lissage progressif
            val weightMultiplier = heightRatio * (2f - heightRatio) // Courbe qui diminue vers le sommet
            val accumulatedWeight = weightMultiplier * 1.5f
            val weightDirection = if (point.x + newPermanentWave > plantStem.getStemBaseX()) 1f else -1f
            val weightInfluence = accumulatedWeight * weightDirection * 0.012f
            newPermanentWave += weightInfluence
            
            // Limites progressives - plus strictes vers le sommet
            val flexLimit = 8f * (1.5f - heightRatio * 0.5f) // Plus strict vers le haut
            val waveLimit = 20f * (1.5f - heightRatio * 0.5f)
            
            smoothedOscillation = smoothedOscillation.coerceIn(-flexLimit, flexLimit)
            newPermanentWave = newPermanentWave.coerceIn(-waveLimit, waveLimit)
            
            plantStem.mainStem[i] = point.copy(
                oscillation = smoothedOscillation,
                permanentWave = newPermanentWave
            )
        }
        
        // Branches : oscillations TRÈS RÉDUITES pour marguerite réaliste
        for (branch in plantStem.branches) {
            for (i in branch.points.indices) {
                val point = branch.points[i]
                val heightFromBase = plantStem.getStemBaseY() - point.y
                val heightRatio = heightFromBase / plantStem.getMaxPossibleHeight()
                
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
