package com.example.souffleforcetest

class PlantGrowthFeatures(private val engine: PlantGrowthEngine) {
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.055f
    private val growthRate = 174.6f
    private val maxLeafWidth = 75f
    private val maxLeafLength = 200f
    
    // ==================== CROISSANCE DES FEUILLES ====================
    
    fun growLeaves(force: Float) {
        if (force > forceThreshold) {
            val adjustedForce = force - forceThreshold
            val growthIncrement = adjustedForce * growthRate * 0.08f
            
            for (bourgeon in engine.bourgeons) {
                if (bourgeon.taille > 2f) {
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
        if (force > forceThreshold) {
            val adjustedForce = force - forceThreshold
            val growthIncrement = adjustedForce * growthRate * 0.08f
            
            for (branch in engine.getBranches().filter { it.tracedPath.isNotEmpty() }) {
                val topPoint = branch.tracedPath.last()
                
                if (branch.fleur == null) {
                    val sizeVariation = 0.7f + (0..6).random() * 0.1f
                    branch.fleur = Fleur(topPoint.x, topPoint.y, 10f, 14, sizeVariation)
                }
                
                branch.fleur?.let { flower ->
                    val branchGrowthIncrement = growthIncrement * branch.growthMultiplier
                    flower.taille += branchGrowthIncrement * 0.15f + 1f
                    flower.taille = kotlin.math.min(flower.taille, 175f * flower.sizeMultiplier)
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
