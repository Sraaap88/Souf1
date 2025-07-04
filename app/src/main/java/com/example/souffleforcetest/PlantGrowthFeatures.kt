package com.example.souffleforcetest

class PlantGrowthFeatures(private val engine: PlantGrowthEngine) {
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.055f // Plus sensible (même que PlantGrowthEngine)
    private val growthRate = 174.6f
    private val maxLeafWidth = 75f
    private val maxLeafLength = 200f
    private val baseY = 2300f // Approximation de la base (screenHeight - 100f)
    
    // ==================== CROISSANCE DES FEUILLES ====================
    
    fun growLeaves(force: Float) {
        if (force > forceThreshold) {
            val adjustedForce = force - forceThreshold
            val growthIncrement = adjustedForce * growthRate * 0.08f
            
            for (bourgeon in engine.bourgeons) {
                if (bourgeon.taille > 2f) {
                    var feuille = engine.feuilles.find { it.bourgeon == bourgeon }
                    if (feuille == null) {
                        val closestBranch = findClosestBranch(bourgeon)
                        val closestBranchX = closestBranch?.startPoint?.x ?: 540f
                        
                        val isRightSide = bourgeon.x > closestBranchX
                        val baseAngle = if (isRightSide) 95f else 265f // 95° et 265° (plus vers le bas)
                        val heightFactor = bourgeon.y / 2400f // screenHeight approximé
                        val heightVariation = (heightFactor - 0.5f) * 5f // Très peu de variation
                        val randomVariation = ((-2..2).random()).toFloat() // Très peu de variation
                        val finalAngle = baseAngle + heightVariation + randomVariation
                        
                        feuille = Feuille(bourgeon, 0f, 0f, finalAngle, false)
                        engine.feuilles.add(feuille)
                    }
                    
                    if (!feuille.maxLargeurAtteinte) {
                        // Feuilles graduées : PROPORTIONNELLES à la hauteur de CETTE tige
                        val closestBranch = findClosestBranch(bourgeon)
                        val branchHeight = closestBranch?.currentHeight ?: 100f
                        val branchBaseY = closestBranch?.startPoint?.y ?: baseY
                        
                        // Position relative sur CETTE branche (0 = base, 1 = sommet)
                        val relativePosition = (bourgeon.y - branchBaseY) / branchHeight
                        val clampedPosition = relativePosition.coerceIn(0f, 1f)
                        
                        // Gradient proportionnel à cette tige : grosse à la base, fine en haut
                        val sizeMultiplier = 2.5f - (clampedPosition * 1.8f) // 2.5x à la base, 0.7x en haut
                        
                        val lengthGrowth = growthIncrement * 1.0f * 1.3f * sizeMultiplier
                        val widthGrowth = growthIncrement * 0.35f * sizeMultiplier
                        
                        feuille.longueur += lengthGrowth
                        feuille.largeur += widthGrowth
                        
                        if (feuille.largeur >= maxLeafWidth) {
                            feuille.largeur = maxLeafWidth
                            feuille.maxLargeurAtteinte = true
                        }
                        
                        feuille.longueur = kotlin.math.min(feuille.longueur, 100f)
                    } else {
                        // Même logique proportionnelle pour la phase finale
                        val closestBranch = findClosestBranch(bourgeon)
                        val branchHeight = closestBranch?.currentHeight ?: 100f
                        val branchBaseY = closestBranch?.startPoint?.y ?: baseY
                        
                        val relativePosition = (bourgeon.y - branchBaseY) / branchHeight
                        val clampedPosition = relativePosition.coerceIn(0f, 1f)
                        val sizeMultiplier = 2.5f - (clampedPosition * 1.8f)
                        
                        val lengthGrowth = growthIncrement * 1.4f * 1.3f * sizeMultiplier
                        feuille.longueur += lengthGrowth
                        feuille.longueur = kotlin.math.min(feuille.longueur, maxLeafLength)
                    }
                }
            }
        }
    }
    
    private fun findClosestBranch(bourgeon: Bourgeon): Branch? {
        var closestBranch: Branch? = null
        var minDistance = Float.MAX_VALUE
        
        for (branch in engine.getBranches()) {
            for (point in branch.tracedPath) {
                val distance = kotlin.math.sqrt(
                    (point.x - bourgeon.x) * (point.x - bourgeon.x) + 
                    (point.y - bourgeon.y) * (point.y - bourgeon.y)
                )
                if (distance < minDistance) {
                    minDistance = distance
                    closestBranch = branch
                }
            }
        }
        return closestBranch
    }
    
    // ==================== CROISSANCE DES FLEURS ====================
    
    fun growFlowers(force: Float) {
        if (force > forceThreshold) {
            val adjustedForce = force - forceThreshold
            val growthIncrement = adjustedForce * growthRate * 0.08f
            
            for (branch in engine.getBranches().filter { it.tracedPath.isNotEmpty() }) {
                val topPoint = branch.tracedPath.last()
                
                if (branch.fleur == null) {
                    val sizeVariation = 0.7f + (0..6).random() * 0.1f
                    branch.fleur = Fleur(topPoint.x, topPoint.y, 15f, 6, sizeVariation)
                }
                
                branch.fleur?.let { flower ->
                    val branchGrowthIncrement = growthIncrement * branch.growthMultiplier
                    flower.taille += branchGrowthIncrement * 0.4f
                    flower.taille = kotlin.math.min(flower.taille, 175f * flower.sizeMultiplier)
                    flower.petalCount = kotlin.math.max(5, (flower.taille * 0.05f).toInt())
                    flower.x = topPoint.x
                    flower.y = topPoint.y
                }
            }
            
            // Mettre à jour la fleur principale pour compatibilité
            val mainBranch = engine.getBranches().firstOrNull()
            mainBranch?.fleur?.let { engine.fleur = it }
        }
    }
}
