package com.example.souffleforcetest

class PlantGrowthFeatures(private val engine: PlantGrowthEngine) {
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.01f // TRÈS bas pour micro faible
    private val growthRate = 174.6f
    private val maxLeafWidth = 75f
    private val maxLeafLength = 200f
    
    // ==================== CROISSANCE DES FEUILLES ====================
    
    fun growLeaves(force: Float) {
        // FORCÉ : Feuilles poussent même avec peu de force
        val forcedForce = kotlin.math.max(force * 10f, 0.1f) // Amplifier le signal
        
        if (forcedForce > forceThreshold) {
            val adjustedForce = forcedForce - forceThreshold
            val growthIncrement = adjustedForce * growthRate * 0.15f // Plus rapide
            
            for (bourgeon in engine.bourgeons) {
                if (bourgeon.taille > 1f) { // Seuil réduit
                    var feuille = engine.feuilles.find { it.bourgeon == bourgeon }
                    if (feuille == null) {
                        var closestBranchX = findClosestBranchX(bourgeon)
                        
                        val isRightSide = bourgeon.x > closestBranchX
                        val baseAngle = if (isRightSide) -25f else 205f
                        val heightFactor = bourgeon.y / 2400f
                        val heightVariation = (heightFactor - 0.5f) * 30f
                        val randomVariation = ((-15..15).random()).toFloat()
                        val finalAngle = baseAngle + heightVariation + randomVariation
                        
                        feuille = Feuille(bourgeon, 0f, 0f, finalAngle, false)
                        engine.feuilles.add(feuille)
                    }
                    
                    if (!feuille.maxLargeurAtteinte) {
                        val lengthGrowth = growthIncrement * 0.4f * 1.3f
                        val widthGrowth = growthIncrement * 0.45f
                        
                        feuille.longueur += lengthGrowth
                        feuille.largeur += widthGrowth
                        
                        if (feuille.largeur >= maxLeafWidth) {
                            feuille.largeur = maxLeafWidth
                            feuille.maxLargeurAtteinte = true
                        }
                        
                        feuille.longueur = kotlin.math.min(feuille.longueur, 100f)
                    } else {
                        val lengthGrowth = growthIncrement * 0.6f * 1.3f
                        feuille.longueur += lengthGrowth
                        feuille.longueur = kotlin.math.min(feuille.longueur, maxLeafLength)
                    }
                }
            }
        }
    }
    
    private fun findClosestBranchX(bourgeon: Bourgeon): Float {
        var closestBranchX = 540f
        var minDistance = Float.MAX_VALUE
        
        for (branch in engine.getBranches()) {
            for (point in branch.tracedPath) {
                val distance = kotlin.math.sqrt(
                    (point.x - bourgeon.x) * (point.x - bourgeon.x) + 
                    (point.y - bourgeon.y) * (point.y - bourgeon.y)
                )
                if (distance < minDistance) {
                    minDistance = distance
                    closestBranchX = point.x
                }
            }
        }
        return closestBranchX
    }
    
    // ==================== CROISSANCE DES FLEURS ====================
    
    fun growFlowers(force: Float) {
        // FORCÉ : Fleurs poussent même avec peu de force
        val forcedForce = kotlin.math.max(force * 10f, 0.1f) // Amplifier le signal
        
        if (forcedForce > forceThreshold) {
            val adjustedForce = forcedForce - forceThreshold
            val growthIncrement = adjustedForce * growthRate * 0.15f // Plus rapide
            
            for (branch in engine.getBranches().filter { it.tracedPath.isNotEmpty() }) {
                val topPoint = branch.tracedPath.last()
                
                if (branch.fleur == null) {
                    val sizeVariation = 0.7f + (0..6).random() * 0.1f
                    branch.fleur = Fleur(topPoint.x, topPoint.y, 15f, 14, sizeVariation) // Plus grosse au départ
                }
                
                branch.fleur?.let { flower ->
                    val branchGrowthIncrement = growthIncrement * branch.growthMultiplier
                    flower.taille += branchGrowthIncrement * 0.3f + 2f // Croissance forcée
                    flower.taille = kotlin.math.min(flower.taille, 200f * flower.sizeMultiplier) // Plus grosse
                    flower.petalCount = 14
                    flower.x = topPoint.x
                    flower.y = topPoint.y
                }
            }
            
            val mainBranch = engine.getBranches().firstOrNull()
            mainBranch?.fleur?.let { engine.fleur = it }
        }
    }
}
