package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color

class LupinRenderer {
    
    // ==================== FONCTION PRINCIPALE DE RENDU ====================
    
    fun drawLupin(
        canvas: Canvas, 
        stemPaint: Paint, 
        leafPaint: Paint, 
        flowerPaint: Paint,
        stems: List<LupinStem>,
        leaves: List<LupinLeaf>
    ) {
        drawStems(canvas, stemPaint, stems)
        drawBasalShoots(canvas, stemPaint, stems)
        drawLeaves(canvas, leafPaint, stems, leaves)
        drawFlowerSpikes(canvas, flowerPaint, stems)
    }
    
    // ==================== RENDU DES TIGES ====================
    
    private fun drawStems(canvas: Canvas, paint: Paint, stems: List<LupinStem>) {
        paint.color = Color.rgb(34, 139, 34)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        
        for (stem in stems) {
            if (stem.points.size >= 2) {
                for (i in 1 until stem.points.size) {
                    val p1 = stem.points[i-1]
                    val p2 = stem.points[i]
                    
                    paint.strokeWidth = p1.thickness
                    canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint)
                }
            }
        }
    }
    
    private fun drawBasalShoots(canvas: Canvas, paint: Paint, stems: List<LupinStem>) {
        paint.color = Color.rgb(34, 139, 34)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        
        for (stem in stems) {
            for (basalShoot in stem.basalShoots) {
                if (basalShoot.points.size >= 2) {
                    for (i in 1 until basalShoot.points.size) {
                        val p1 = basalShoot.points[i-1]
                        val p2 = basalShoot.points[i]
                        
                        paint.strokeWidth = p1.thickness
                        canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint)
                    }
                }
            }
        }
    }
    
    // ==================== RENDU DES FEUILLES ====================
    
    private fun drawLeaves(canvas: Canvas, paint: Paint, stems: List<LupinStem>, leaves: List<LupinLeaf>) {
        paint.color = Color.rgb(34, 139, 34)
        paint.style = Paint.Style.FILL
        
        for (leaf in leaves) {
            if (leaf.currentSize > 0 && leaf.stemIndex < stems.size) {
                if (leaf.isBasalShoot) {
                    // Feuille sur une petite tige basale
                    val stem = stems[leaf.stemIndex]
                    if (leaf.basalShootIndex < stem.basalShoots.size) {
                        val basalShoot = stem.basalShoots[leaf.basalShootIndex]
                        val leafPoint = getBasalShootPointAtHeight(basalShoot, leaf.heightRatio)
                        
                        leafPoint?.let { point ->
                            drawPalmateLeaf(canvas, paint, point.x, point.y, leaf)
                        }
                    }
                } else {
                    // Feuille sur une tige principale
                    val stem = stems[leaf.stemIndex]
                    val leafPoint = getStemPointAtHeight(stem, leaf.heightRatio)
                    
                    leafPoint?.let { point ->
                        drawPalmateLeaf(canvas, paint, point.x, point.y, leaf)
                    }
                }
            }
        }
    }
    
    private fun drawPalmateLeaf(canvas: Canvas, paint: Paint, x: Float, y: Float, leaf: LupinLeaf) {
        val size = leaf.currentSize
        if (size <= 0) return
        
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(leaf.angle)
        
        val folioleCount = leaf.folioleCount.coerceAtMost(leaf.folioleAngles.size)
        val angleSpread = 80f  // Plus étalé comme sur la photo
        
        for (i in 0 until folioleCount) {
            val folioleAngle = (i - folioleCount / 2f) * (angleSpread / folioleCount) + leaf.folioleAngles[i]
            val folioleLength = size * (0.9f + (i % 3) * 0.15f)
            val folioleWidth = folioleLength * 0.25f  // Plus étroit comme sur la photo
            
            canvas.save()
            canvas.rotate(folioleAngle)
            
            // Foliole en forme de lancette (plus pointue)
            paint.color = Color.rgb(50, 150, 50)  // Vert plus vif
            paint.style = Paint.Style.FILL
            
            // Forme lancéolée réaliste
            canvas.drawOval(
                -folioleWidth/2, folioleLength
