package com.example.souffleforcetest

import android.graphics.Canvas

/**
 * Coordinateur principal - EXACTEMENT la même interface qu'avant
 */
class PlantGrowthLogic(
    private var screenWidth: Int = 1080,
    private var screenHeight: Int = 2400
) {
    
    // ==================== COMPOSANTS ====================
    
    private val growthEngine = PlantGrowthEngine(screenWidth, screenHeight)
    private val growthFeatures = PlantGrowthFeatures(growthEngine)
    
    // ==================== PROPRIÉTÉS PUBLIQUES (compatibilité exacte) ====================
    
    val tracedPath: MutableList<TracePoint>
        get() = growthEngine.tracedPath
    
    val bourgeons: MutableList<Bourgeon>
        get() = growthEngine.bourgeons
    
    val feuilles: MutableList<Feuille>
        get() = growthEngine.feuilles
    
    var fleur: Fleur?
        get() = growthEngine.fleur
        set(value) { growthEngine.fleur = value }
    
    // ==================== FONCTIONS PUBLIQUES (interface exacte) ====================
    
    fun updateScreenSize(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
        growthEngine.updateScreenSize(width, height)
    }
    
    fun updateForce(force: Float, lightState: String) {
        when (lightState) {
            "GREEN_GROW" -> growthEngine.growAllBranches(force)
            "GREEN_LEAVES" -> growthFeatures.growLeaves(force)
            "GREEN_FLOWER" -> growthFeatures.growFlowers(force)
        }
    }
    
    fun drawPlant(canvas: Canvas, renderer: PlantRenderer, time: Float) {
        // Dessiner toutes les branches actives
        for (branch in growthEngine.getBranches()) {
            renderer.drawRealisticStem(canvas, branch.tracedPath, time, 9.6f, 25.6f)
            
            if (branch.tracedPath.isNotEmpty()) {
                renderer.drawGrowthPoint(canvas, branch.tracedPath.last(), time)
            }
            
            // Dessiner la fleur de cette branche
            renderer.drawRealisticFlower(canvas, branch.fleur, time)
        }
        
        renderer.drawAttachmentPoints(canvas, bourgeons, time)
        renderer.drawRealistic3DLeaves(canvas, feuilles, time)
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
}
