package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import kotlin.math.sqrt

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
                -folioleWidth/2, folioleLength * 0.1f,
                folioleWidth/2, folioleLength * 0.95f,
                paint
            )
            
            // Pointe effilée en haut
            paint.color = Color.rgb(45, 140, 45)
            canvas.drawOval(
                -folioleWidth/4, 0f,
                folioleWidth/4, folioleLength * 0.2f,
                paint
            )
            
            // Contour sombre pour bien définir chaque foliole
            paint.color = Color.rgb(25, 100, 25)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2.5f
            canvas.drawOval(
                -folioleWidth/2, folioleLength * 0.1f,
                folioleWidth/2, folioleLength * 0.95f,
                paint
            )
            
            // Nervure centrale très marquée
            paint.color = Color.rgb(20, 90, 20)
            paint.strokeWidth = 3f
            canvas.drawLine(0f, folioleLength * 0.15f, 0f, folioleLength * 0.85f, paint)
            
            // Nervures secondaires bien définies
            paint.strokeWidth = 1.5f
            paint.color = Color.rgb(30, 110, 30)
            for (j in 1..4) {
                val nervureY = folioleLength * (0.25f + j * 0.15f)
                val nervureWidth = folioleWidth * (0.35f - j * 0.06f)
                canvas.drawLine(-nervureWidth/2, nervureY, 0f, nervureY * 0.95f, paint)
                canvas.drawLine(nervureWidth/2, nervureY, 0f, nervureY * 0.95f, paint)
            }
            
            paint.style = Paint.Style.FILL
            canvas.restore()
        }
        
        // Pétiole bien marqué
        paint.color = Color.rgb(35, 110, 35)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawLine(0f, 0f, 0f, -size * 0.25f, paint)
        paint.style = Paint.Style.FILL
        
        canvas.restore()
    }
    
    // ==================== RENDU DES FLEURS ====================
    
    private fun drawFlowerSpikes(canvas: Canvas, paint: Paint, stems: List<LupinStem>) {
        paint.style = Paint.Style.FILL
        
        for (stem in stems) {
            if (!stem.flowerSpike.hasStartedBlooming) continue
            
            for (flower in stem.flowerSpike.flowers) {
                if (flower.currentSize > 0) {
                    val colorRgb = flower.color.rgb
                    val size = flower.currentSize
                    
                    // Fleur conique TRÈS définie - chaque pétale visible
                    
                    // Pétale central (étendard) - forme conique
                    paint.color = Color.rgb(colorRgb[0], colorRgb[1], colorRgb[2])
                    canvas.drawOval(
                        flower.x - size * 0.4f, flower.y - size * 0.6f,
                        flower.x + size * 0.4f, flower.y + size * 0.1f, 
                        paint
                    )
                    
                    // Contour sombre pour définir le pétale central
                    paint.color = Color.rgb(
                        (colorRgb[0] * 0.6f).toInt(),
                        (colorRgb[1] * 0.6f).toInt(),
                        (colorRgb[2] * 0.6f).toInt()
                    )
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 2f
                    canvas.drawOval(
                        flower.x - size * 0.4f, flower.y - size * 0.6f,
                        flower.x + size * 0.4f, flower.y + size * 0.1f, 
                        paint
                    )
                    paint.style = Paint.Style.FILL
                    
                    // Pétales latéraux (ailes) - bien définis
                    paint.color = Color.rgb(
                        (colorRgb[0] * 0.85f).toInt(),
                        (colorRgb[1] * 0.85f).toInt(),
                        (colorRgb[2] * 0.85f).toInt()
                    )
                    
                    // Aile gauche
                    canvas.drawOval(
                        flower.x - size * 0.7f, flower.y - size * 0.2f,
                        flower.x - size * 0.1f, flower.y + size * 0.3f,
                        paint
                    )
                    // Contour aile gauche
                    paint.color = Color.rgb(
                        (colorRgb[0] * 0.5f).toInt(),
                        (colorRgb[1] * 0.5f).toInt(),
                        (colorRgb[2] * 0.5f).toInt()
                    )
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 1.5f
                    canvas.drawOval(
                        flower.x - size * 0.7f, flower.y - size * 0.2f,
                        flower.x - size * 0.1f, flower.y + size * 0.3f,
                        paint
                    )
                    paint.style = Paint.Style.FILL
                    
                    // Aile droite
                    paint.color = Color.rgb(
                        (colorRgb[0] * 0.85f).toInt(),
                        (colorRgb[1] * 0.85f).toInt(),
                        (colorRgb[2] * 0.85f).toInt()
                    )
                    canvas.drawOval(
                        flower.x + size * 0.1f, flower.y - size * 0.2f,
                        flower.x + size * 0.7f, flower.y + size * 0.3f,
                        paint
                    )
                    // Contour aile droite
                    paint.color = Color.rgb(
                        (colorRgb[0] * 0.5f).toInt(),
                        (colorRgb[1] * 0.5f).toInt(),
                        (colorRgb[2] * 0.5f).toInt()
                    )
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 1.5f
                    canvas.drawOval(
                        flower.x + size * 0.1f, flower.y - size * 0.2f,
                        flower.x + size * 0.7f, flower.y + size * 0.3f,
                        paint
                    )
                    paint.style = Paint.Style.FILL
                    
                    // Carène (pétale inférieur) - forme conique pointue
                    paint.color = Color.rgb(
                        (colorRgb[0] * 0.75f).toInt(),
                        (colorRgb[1] * 0.75f).toInt(),
                        (colorRgb[2] * 0.75f).toInt()
                    )
                    canvas.drawOval(
                        flower.x - size * 0.25f, flower.y + size * 0.1f,
                        flower.x + size * 0.25f, flower.y + size * 0.5f,
                        paint
                    )
                    // Contour carène
                    paint.color = Color.rgb(
                        (colorRgb[0] * 0.4f).toInt(),
                        (colorRgb[1] * 0.4f).toInt(),
                        (colorRgb[2] * 0.4f).toInt()
                    )
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 1.5f
                    canvas.drawOval(
                        flower.x - size * 0.25f, flower.y + size * 0.1f,
                        flower.x + size * 0.25f, flower.y + size * 0.5f,
                        paint
                    )
                    paint.style = Paint.Style.FILL
                }
            }
        }
    }
    
    // ==================== FONCTIONS UTILITAIRES ====================
    
    private fun getStemPointAtHeight(stem: LupinStem, heightRatio: Float): StemPoint? {
        if (stem.points.size < 2) return null
        
        val targetHeight = stem.currentHeight * heightRatio
        var currentHeight = 0f
        
        for (i in 1 until stem.points.size) {
            val segmentHeight = stem.points[i-1].y - stem.points[i].y
            if (currentHeight + segmentHeight >= targetHeight) {
                val segmentRatio = (targetHeight - currentHeight) / segmentHeight
                val p1 = stem.points[i-1]
                val p2 = stem.points[i]
                
                val x = p1.x + (p2.x - p1.x) * segmentRatio
                val y = p1.y + (p2.y - p1.y) * segmentRatio
                val thickness = p1.thickness + (p2.thickness - p1.thickness) * segmentRatio
                
                return StemPoint(x, y, thickness)
            }
            currentHeight += segmentHeight
        }
        
        return stem.points.lastOrNull()
    }
    
    private fun getBasalShootPointAtHeight(basalShoot: BasalShoot, heightRatio: Float): StemPoint? {
        if (basalShoot.points.size < 2) return null
        
        val targetHeight = basalShoot.currentHeight * heightRatio
        var currentHeight = 0f
        
        for (i in 1 until basalShoot.points.size) {
            val p1 = basalShoot.points[i-1]
            val p2 = basalShoot.points[i]
            val segmentHeight = sqrt(
                (p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y)
            )
            
            if (currentHeight + segmentHeight >= targetHeight) {
                val segmentRatio = (targetHeight - currentHeight) / segmentHeight
                
                val x = p1.x + (p2.x - p1.x) * segmentRatio
                val y = p1.y + (p2.y - p1.y) * segmentRatio
                val thickness = p1.thickness + (p2.thickness - p1.thickness) * segmentRatio
                
                return StemPoint(x, y, thickness)
            }
            currentHeight += segmentHeight
        }
        
        return basalShoot.points.lastOrNull()
    }
}
