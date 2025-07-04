package com.example.souffleforcetest

class PlantGrowthFeatures(private val engine: PlantGrowthEngine) {
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.055f // Plus sensible (même que PlantGrowthEngine)
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
                        val baseAngle = if (isRightSide) 95f else 265f // 95° et 265° (plus vers le bas)
                        val heightFactor = bourgeon.y / 2400f // screenHeight approximé
                        val heightVariation = (heightFactor - 0.5f) * 5f // Très peu de variation
                        val randomVariation = ((-2..2).random()).toFloat() // Très peu de variation
                        val finalAngle = baseAngle + heightVariation + randomVariation
                        
                        feuille = Feuille(bourgeon, 0f, 0f, finalAngle, false)
                        engine.feuilles.add(feuille)
                    }
                    
                    if (!feuille.maxLargeurAtteinte) {
                        // Feuilles graduées : GROSSES à la base, FINES en haut
                        val heightRatio = bourgeon.y / 2400f // Position relative sur l'écran
                        val sizeMultiplier = 2.5f - (heightRatio * 1.8f) // 2.5x en bas, 0.7x en haut (plus de contraste)
                        
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
                        // Même logique de taille graduée ACCENTUÉE pour la phase finale
                        val heightRatio = bourgeon.y / 2400f
                        val sizeMultiplier = 2.5f - (heightRatio * 1.8f) // Même gradient : grosses → fines
                        
                        val lengthGrowth = growthIncrement * 1.4f * 1.3f * sizeMultiplier
                        feuille.longueur += lengthGrowth
                        feuille.longueur = kotlin.math.min(feuille.longueur, maxLeafLength)
                    }
                }
            }
        }
    }
    
    private fun findClosestBranchX(bourgeon: Bourgeon): Float {
        var closestBranchX = 540f // screenWidth / 2 approximé
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
                    branch.fleur = Fleur(topPoint.x, topPoint.y, 0f, 6, sizeVariation)
                }
                
                branch.fleur?.let { flower ->
                    val branchGrowthIncrement = growthIncrement * branch.growthMultiplier
                    flower.taille += branchGrowthIncrement * 0.15f
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
