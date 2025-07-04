package com.example.souffleforcetest

import android.graphics.Canvas

/**
 * Coordinateur principal qui orchestre tous les composants de croissance
 * Remplace l'ancien PlantGrowthLogic monolithique
 */
class PlantGrowthLogic(
    private var screenWidth: Int = 1080,
    private var screenHeight: Int = 2400,
    private var currentPlantType: PlantType = PlantType.MARGUERITE
) {
    
    // ==================== COMPOSANTS ====================
    
    private val growthEngine = PlantGrowthEngine(screenWidth, screenHeight)
    private val growthFeatures = PlantGrowthFeatures(growthEngine, screenWidth, screenHeight)
    
    // ==================== PROPRIÉTÉS PUBLIQUES (pour compatibilité) ====================
    
    val tracedPath: MutableList<TracePoint>
        get() = growthEngine.tracedPath
    
    val bourgeons: MutableList<Bourgeon>
        get() = growthEngine.bourgeons
    
    val feuilles: MutableList<Feuille>
        get() = growthEngine.feuilles
    
    var fleur: Fleur?
        get() = growthEngine.fleur
        set(value) { growthEngine.fleur = value }
    
    // ==================== GESTION DU TYPE DE PLANTE ====================
    
    fun setPlantType(newType: PlantType) {
        if (currentPlantType != newType) {
            currentPlantType = newType
            // Recréer les composants avec le nouveau type
            resetPlant() // Nettoie l'ancien
            // Les composants utiliseront automatiquement le nouveau type
        }
    }
    
    fun getCurrentPlantType(): PlantType = currentPlantType
    
    fun updateScreenSize(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
        growthEngine.updateScreenSize(width, height)
        growthFeatures.updateScreenSize(width, height)
    }
    
    fun updateForce(force: Float, lightState: String) {
        println("*** LIGHT STATE: $lightState ***")
        when (lightState) {
            "GREEN_GROW" -> {
                println("*** GROWING STEMS ***")
                growthEngine.updateForce(force, lightState)
                // Créer des bourgeons pendant la croissance
                createBudsForActiveBranches(force)
            }
            "GREEN_LEAVES" -> {
                println("*** DEBUT GREEN_LEAVES ***")
                growthFeatures.growLeaves(force)
                println("*** Bourgeons: ${bourgeons.size}, Feuilles: ${feuilles.size} ***")
            }
            "GREEN_FLOWER" -> {
                println("*** DEBUT GREEN_FLOWER ***")
                growthFeatures.growFlowers(force)
                println("*** Fleurs: ${fleur?.taille} ***")
            }
        }
    }
    
    fun drawPlant(canvas: Canvas, renderer: PlantRenderer, time: Float) {
        println("*** DRAWING PLANT - Branches: ${growthEngine.getBranches().size} ***")
        
        // Dessiner toutes les branches actives
        for (branch in growthEngine.getBranches()) {
            println("*** Branch ${branch.id} - tracedPath: ${branch.tracedPath.size}, fleur: ${branch.fleur?.taille} ***")
            
            renderer.drawRealisticStem(canvas, branch.tracedPath, time, 9.6f, 25.6f)
            
            if (branch.tracedPath.isNotEmpty()) {
                renderer.drawGrowthPoint(canvas, branch.tracedPath.last(), time)
            }
            
            // Dessiner la fleur de cette branche
            if (branch.fleur != null) {
                println("*** TENTATIVE DESSIN FLEUR taille: ${branch.fleur!!.taille} ***")
                renderer.drawRealisticFlower(canvas, branch.fleur, time, currentPlantType.flowerStyle)
            }
        }
        
        // Dessiner les bourgeons et feuilles
        println("*** Bourgeons: ${bourgeons.size}, Feuilles: ${feuilles.size} ***")
        renderer.drawAttachmentPoints(canvas, bourgeons, time)
        renderer.drawRealistic3DLeaves(canvas, feuilles, time, currentPlantType.leafStyle)
    }
    
    fun resetPlant() {
        growthEngine.resetPlant()
    }
    
    fun getMainGrowthPoint(): TracePoint? {
        return growthEngine.getMainGrowthPoint()
    }
    
    fun hasVisibleGrowth(): Boolean {
        return growthEngine.hasVisibleGrowth()
    }
    
    // ==================== FONCTIONS PRIVÉES ====================
    
    private fun createBudsForActiveBranches(force: Float) {
        // FORCÉ : Créer des bourgeons à chaque appel si force > seuil
        if (force > 0.08f) {
            for (branch in growthEngine.getBranches()) {
                if (branch.currentHeight > 20f && branch.tracedPath.size > 3) {
                    // Créer un bourgeon à chaque fois
                    val segmentIndex = kotlin.math.max(0, branch.tracedPath.size - (2..5).random())
                    val budPoint = branch.tracedPath[segmentIndex]
                    
                    val budX = budPoint.x + ((-10..10).random()).toFloat()
                    val budY = budPoint.y + ((-5..5).random()).toFloat()
                    
                    growthEngine.bourgeons.add(Bourgeon(budX, budY, 5f))
                    println("*** BOURGEON CRÉÉ à ($budX, $budY) ***")
                }
            }
        }
    }
}
