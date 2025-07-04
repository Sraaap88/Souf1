package com.example.souffleforcetest

// Extension de PlantGrowthEngine pour les feuilles et fleurs
class PlantGrowthFeatures(
    private val engine: PlantGrowthEngine,
    private var screenWidth: Int = 1080,
    private var screenHeight: Int = 2400
) {
    
    private val currentPlantType = PlantType.MARGUERITE
    private var leafSideCounter = 0
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.08f
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
                        val closestBranchX = findClosestBranchX(bourgeon)
                        val finalAngle = calculateLeafAngle(bourgeon, closestBranchX)
                        
                        feuille = Feuille(bourgeon, 0f, 0f, finalAngle, false)
                        engine.feuilles.add(feuille)
                    }
                    
                    if (!feuille.maxLargeurAtteinte) {
                        val lengthGrowth = growthIncrement * 0.4f * currentPlantType.leafLengthMultiplier
                        val widthGrowth = growthIncrement * 0.45f
                        
                        feuille.longueur += lengthGrowth
                        feuille.largeur += widthGrowth
                        
                        if (feuille.largeur >= maxLeafWidth) {
                            feuille.largeur = maxLeafWidth
                            feuille.maxLargeurAtteinte = true
                        }
                        
                        feuille.longueur = kotlin.math.min(feuille.longueur, 100f)
                    } else {
                        val lengthGrowth = growthIncrement * 0.6f * currentPlantType.leafLengthMultiplier
                        feuille.longueur += lengthGrowth
                        feuille.longueur = kotlin.math.min(feuille.longueur, maxLeafLength)
                    }
                }
            }
        }
    }
    
    private fun findClosestBranchX(bourgeon: Bourgeon): Float {
        var closestBranchX = screenWidth / 2f
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
    
    private fun calculateLeafAngle(bourgeon: Bourgeon, closestBranchX: Float): Float {
        val isRightSide = bourgeon.x > closestBranchX
        val baseAngle = if (isRightSide) -25f else 205f
        val heightFactor = bourgeon.y / screenHeight.toFloat()
        val heightVariation = (heightFactor - 0.5f) * 30f
        val randomVariation = ((-15..15).random()).toFloat()
        return baseAngle + heightVariation + randomVariation
    }
    
    // ==================== CROISSANCE DES FLEURS ====================
    
    fun growFlowers(force: Float) {
        println("*** growFlowers appelé avec force: $force ***")
        if (force > forceThreshold) {
            val adjustedForce = force - forceThreshold
            val growthIncrement = adjustedForce * growthRate * 0.08f
            println("*** growthIncrement: $growthIncrement ***")
            
            for (branch in engine.getBranches().filter { it.tracedPath.isNotEmpty() }) {
                val topPoint = branch.tracedPath.last()
                println("*** Branch ${branch.id} - topPoint: (${topPoint.x}, ${topPoint.y}) ***")
                
                if (branch.fleur == null) {
                    val sizeVariation = 0.7f + (0..6).random() * 0.1f
                    branch.fleur = Fleur(topPoint.x, topPoint.y, 5f, 6, sizeVariation) // FORCÉ à 5f au lieu de 0f
                    println("*** FLEUR CRÉÉE pour branch ${branch.id} ***")
                }
                
                branch.fleur?.let { flower ->
                    val branchGrowthIncrement = growthIncrement * branch.growthMultiplier
                    flower.taille += branchGrowthIncrement * 0.15f + 1f // FORCÉ +1f
                    flower.taille = kotlin.math.min(flower.taille, 175f * flower.sizeMultiplier)
                    flower.petalCount = kotlin.math.max(5, (flower.taille * 0.05f).toInt())
                    flower.x = topPoint.x
                    flower.y = topPoint.y
                    println("*** FLEUR grandit - taille: ${flower.taille} ***")
                }
            }
            
            // Mettre à jour la fleur principale pour compatibilité
            val mainBranch = engine.getBranches().firstOrNull()
            mainBranch?.fleur?.let { 
                engine.fleur = it 
                println("*** FLEUR PRINCIPALE mise à jour ***")
            }
        }
    }
    
    // ==================== CRÉATION DE BOURGEONS ====================
    
    fun createRealisticBud(branch: Branch) {
        val existingBudsOnBranch = engine.bourgeons.count { bourgeon ->
            branch.tracedPath.any { point ->
                val distance = kotlin.math.sqrt(
                    (point.x - bourgeon.x) * (point.x - bourgeon.x) + 
                    (point.y - bourgeon.y) * (point.y - bourgeon.y)
                )
                distance < 50f
            }
        }
        
        val maxBudsForBranch = kotlin.math.min(6, (branch.currentHeight / 80f * currentPlantType.leafDensity).toInt() + 2)
        if (existingBudsOnBranch >= maxBudsForBranch) return
        
        val minSegmentFromTop = 3
        val maxSegmentFromTop = kotlin.math.min(branch.tracedPath.size - 2, 12)
        
        if (maxSegmentFromTop <= minSegmentFromTop) return
        
        val segmentIndex = branch.tracedPath.size - (minSegmentFromTop..maxSegmentFromTop).random()
        val budPoint = branch.tracedPath[segmentIndex]
        
        leafSideCounter++
        val preferredSide = leafSideCounter % 2 == 0
        
        val sameHeightBuds = engine.bourgeons.filter { kotlin.math.abs(it.y - budPoint.y) < 25f }
        val hasRightBud = sameHeightBuds.any { it.x > budPoint.x }
        val hasLeftBud = sameHeightBuds.any { it.x < budPoint.x }
        
        val isRightSide = when {
            preferredSide && !hasRightBud -> true
            !preferredSide && !hasLeftBud -> false
            !hasRightBud -> true
            !hasLeftBud -> false
            else -> (0..1).random() == 0
        }
        
        val naturalOffset = if (isRightSide) {
            (2..6).random().toFloat()
        } else {
            -(2..6).random().toFloat()
        }
        
        val verticalJitter = ((-3..3).random()).toFloat()
        
        val budX = budPoint.x + naturalOffset
        val budY = budPoint.y + verticalJitter
        
        val clampedBudX = budX.coerceIn(100f, screenWidth - 100f)
        
        engine.bourgeons.add(Bourgeon(clampedBudX, budY, 3f))
    }
    
    fun updateScreenSize(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
    }
}
