package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint

class PlantRenderManager(
    private val plantStem: PlantStem?,
    private val roseBushManager: RoseBushManager?,
    private val lupinManager: LupinManager?,
    private val irisManager: IrisManager?,
    private val uiDrawing: UIDrawingManager
) {
    
    // ==================== RENDU PRINCIPAL ====================
    
    fun drawPlant(canvas: Canvas, selectedFlowerType: String, lightState: OrganicLineView.LightState) {
        when (selectedFlowerType) {
            "MARGUERITE" -> drawPlantStem(canvas, lightState)
            "ROSE" -> drawRoseBush(canvas)
            "LUPIN" -> drawLupin(canvas)
            "IRIS" -> drawIris(canvas)
        }
    }
    
    // ==================== RENDU DE LA MARGUERITE ====================
    
    private fun drawPlantStem(canvas: Canvas, lightState: OrganicLineView.LightState) {
        val stem = plantStem ?: return
        
        // Dessiner les fleurs de profil/arrière DERRIÈRE les tiges
        if (lightState == OrganicLineView.LightState.GREEN_FLOWER || 
            lightState == OrganicLineView.LightState.RED) {
            drawBackgroundFlowers(canvas, stem.getFlowers())
        }
        
        // Dessiner la tige principale
        drawMainStem(canvas, stem.mainStem)
        
        // Dessiner les branches
        drawBranches(canvas, stem.branches)
        
        // Dessiner les feuilles pendant GREEN_LEAVES et après
        if (lightState == OrganicLineView.LightState.GREEN_LEAVES || 
            lightState == OrganicLineView.LightState.GREEN_FLOWER || 
            lightState == OrganicLineView.LightState.RED) {
            drawLeaves(canvas, stem.getLeaves())
        }
        
        // Dessiner les fleurs de face/3-4 PAR-DESSUS les tiges
        if (lightState == OrganicLineView.LightState.GREEN_FLOWER || 
            lightState == OrganicLineView.LightState.RED) {
            drawForegroundFlowers(canvas, stem.getFlowers())
        }
    }
    
    private fun drawMainStem(canvas: Canvas, mainStem: List<PlantStem.StemPoint>) {
        if (mainStem.size < 2) return
        uiDrawing.drawMainStem(canvas, mainStem)
    }
    
    private fun drawBranches(canvas: Canvas, branches: List<PlantStem.Branch>) {
        uiDrawing.drawBranches(canvas, branches)
    }
    
    private fun drawLeaves(canvas: Canvas, leaves: List<PlantLeavesManager.Leaf>) {
        val stem = plantStem ?: return
        uiDrawing.drawLeaves(canvas, leaves, stem)
    }
    
    private fun drawBackgroundFlowers(canvas: Canvas, flowers: List<FlowerManager.Flower>) {
        val stem = plantStem ?: return
        uiDrawing.drawBackgroundFlowers(canvas, flowers, stem)
    }
    
    private fun drawForegroundFlowers(canvas: Canvas, flowers: List<FlowerManager.Flower>) {
        val stem = plantStem ?: return
        uiDrawing.drawForegroundFlowers(canvas, flowers, stem)
    }
    
    // ==================== RENDU DU ROSIER ====================
    
    private fun drawRoseBush(canvas: Canvas) {
        roseBushManager?.let { manager ->
            val branchPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
            }
            
            val leafPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            
            val flowerPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            
            manager.drawRoseBush(canvas, branchPaint, leafPaint, flowerPaint)
        }
    }
    
    // ==================== RENDU DU LUPIN ====================
    
    private fun drawLupin(canvas: Canvas) {
        lupinManager?.let { manager ->
            val stemPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
            }
            
            val leafPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            
            val flowerPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            
            manager.drawLupin(canvas, stemPaint, leafPaint, flowerPaint)
        }
    }
    
    // ==================== RENDU DE L'IRIS ====================
    
    private fun drawIris(canvas: Canvas) {
        irisManager?.let { manager ->
            val stemPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
            }
            
            val leafPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            
            val flowerPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            
            manager.drawIris(canvas, stemPaint, leafPaint, flowerPaint)
        }
    }
}
