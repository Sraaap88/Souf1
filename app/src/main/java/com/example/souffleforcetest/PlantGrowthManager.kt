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
                
                // TIGE PRINCIPALE : courbure étalée sur plus grande distance
                val baseDeviation = lastPointX - plantStem.getStemBaseX()
                val heightRatio = currentHeight / plantStem.getMaxPossibleHeight()
                
                // Force de rappel TRÈS DOUCE qui diminue vers le sommet
                val centeringStrength = (1f - heightRatio * 0.9f) * 0.008f // Plus doux et étalé
                val centeringForce = -baseDeviation * centeringStrength
                
                // Mouvement naturel avec lissage progressif
                val naturalSway = sin(currentHeight * 0.003f) * 0.12f * (1f - heightRatio * 0.3f) // Plus étalé
                val microTremble = sin(System.currentTimeMillis() * 0.002f) * 0.03f
                
                // EFFET DE POIDS PROGRESSIF et ÉTALÉ - commence plus tôt
                val weightStart = 0.3f // Commence à 30% de hauteur
                val weightStrength = if (heightRatio > weightStart) {
                    val adjustedRatio = (heightRatio - weightStart) / (1f - weightStart)
                    adjustedRatio * adjustedRatio * (2.5f - adjustedRatio) // Courbure plus douce et étalée
                } else 0f
                
                val weightDirection = if (baseDeviation > 0) 1f else if (baseDeviation < -2f) -1f else 0.2f
                val weightBend = weightStrength * weightDirection * 0.6f
                
                // Lissage étendu avec plusieurs points précédents
                val smoothingRange = 3 // Lissage sur 3 points
                var smoothedMovement = 0f
                var totalWeight = 0f
                
                for (j in 1..smoothingRange) {
                    val prevIndex = plantStem.mainStem.size - j
                    if (prevIndex >= 0) {
                        val weight = 1f / j // Poids décroissant
                        val prevPoint = plantStem.mainStem[prevIndex]
                        val prevMovement = prevPoint.x - plantStem.getStemBaseX()
                        smoothedMovement += prevMovement * weight
                        totalWeight += weight
                    }
                }
                
                if (totalWeight > 0) smoothedMovement /= totalWeight
                
                // Mouvement total avec lissage étendu
                val rawMovement = naturalSway + microTremble + centeringForce + weightBend
                val smoothingFactor = 0.6f + heightRatio * 0.3f // Plus de lissage vers le sommet
                val finalMovement = rawMovement * (1f - smoothingFactor) + smoothedMovement * smoothingFactor * 0.15f
                
                // Limitation progressive - plus permissive pour courbure étalée
                val maxMove = 2f * (1f - heightRatio * 0.3f) // Plus permissif
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
                
                // POSITION avec inclinaison naturelle selon le côté
                val baseX = plantStem.getStemBaseX() + branch.baseOffset
                
                // INCLINAISON progressive selon le côté de la branche - AUGMENTÉE
                val branchDirection = if (branch.angle < 0) -1f else 1f // Gauche ou droite
                val heightRatio = currentBranchHeight / branch.maxHeight
                
                // Inclinaison qui s'accentue progressivement - PLUS FORTE
                val progressiveIncline = heightRatio * heightRatio * 25f * branchDirection // Augmenté de 15f à 25f
                
                // COURBURE VERS LE BAS au sommet des tiges secondaires - COMMENCE PLUS TÔT
                val topCurveStart = 0.5f // Commence à 50% de hauteur au lieu de 70%
                val topCurve = if (heightRatio > topCurveStart) {
                    val curveRatio = (heightRatio - topCurveStart) / (1f - topCurveStart)
                    val curveStrength = curveRatio * curveRatio * 8f // Courbure vers le bas
                    curveStrength * abs(branchDirection) * 0.5f // Vers l'extérieur
                } else 0f
                
                // Effet de poids adapté à la tige secondaire
                val secondaryWeightEffect = heightRatio * heightRatio * 1.2f
                val secondaryWeightBend = secondaryWeightEffect * branchDirection * 0.4f
                
                // Position finale avec inclinaison et courbure
                val currentX = baseX + progressiveIncline + topCurve + secondaryWeightBend
                val currentY = plantStem.getStemBaseY() - currentBranchHeight
                
                // DEBUG: Position avec inclinaison renforcée
                if (branch.points.size <= 5) {
                    println("Branche ${if (branch.angle < 0) "GAUCHE" else "DROITE"}: X=${currentX} (base=${baseX}, incline=${progressiveIncline.toInt()}px, courbe=${topCurve.toInt()}px)")
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
