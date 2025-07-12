package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import kotlin.math.*

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
    
    // ==================== FONCTIONS DE RENDU POUR MARGUERITES ====================
    
    private fun drawLeaves(canvas: Canvas, leaves: List<PlantLeavesManager.Leaf>) {
        val stem = plantStem ?: return
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.rgb(34, 139, 34)
            style = Paint.Style.FILL
        }
        
        for (leaf in leaves) {
            if (leaf.currentSize > 0) {
                // Obtenir la position sur la tige
                val leafPoint = stem.getStemPointAtRatio(leaf.heightRatio)
                leafPoint?.let { point ->
                    canvas.save()
                    canvas.translate(point.x, point.y)
                    canvas.rotate(leaf.angle)
                    
                    val size = leaf.currentSize
                    canvas.drawOval(
                        -size/2, -size/4,
                        size/2, size/4,
                        paint
                    )
                    
                    canvas.restore()
                }
            }
        }
    }
    
    private fun drawBackgroundFlowers(canvas: Canvas, flowers: List<FlowerManager.Flower>) {
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        
        for (flower in flowers) {
            if (flower.currentSize > 0 && (flower.orientation == 1 || flower.orientation == 2)) {
                paint.color = Color.rgb(255, 255, 255)
                canvas.drawCircle(flower.x, flower.y, flower.currentSize * 0.6f, paint)
                
                paint.color = Color.rgb(255, 215, 0)
                canvas.drawCircle(flower.x, flower.y, flower.currentSize * 0.3f, paint)
            }
        }
    }
    
    private fun drawForegroundFlowers(canvas: Canvas, flowers: List<FlowerManager.Flower>) {
        val paint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        
        for (flower in flowers) {
            if (flower.currentSize > 0 && (flower.orientation == 3 || flower.orientation == 4)) {
                for (i in 0..11) {
                    val angle = i * 30f * PI / 180f
                    val petalX = flower.x + cos(angle).toFloat() * flower.currentSize * 0.7f
                    val petalY = flower.y + sin(angle).toFloat() * flower.currentSize * 0.7f
                    
                    paint.color = Color.rgb(255, 255, 255)
                    canvas.drawCircle(petalX, petalY, flower.currentSize * 0.2f, paint)
                }
                
                paint.color = Color.rgb(255, 215, 0)
                canvas.drawCircle(flower.x, flower.y, flower.currentSize * 0.3f, paint)
            }
        }
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
