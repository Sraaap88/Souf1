package com.example.souffleforcetest

import android.graphics.*
import android.content.Context
import java.lang.Math

class PlantRenderer(private val context: Context) {
    
    private val stemPaint = Paint().apply {
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }
    
    private val basePaint = Paint().apply {
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.FILL
    }
    
    fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + fraction * (end - start)
    }
    
    // ==================== TIGES SELON LE STYLE ====================
    
    fun drawRealisticStem(
        canvas: Canvas, 
        tracedPath: List<TracePoint>,
        time: Float,
        baseStrokeWidth: Float,
        maxStrokeWidth: Float,
        stemStyle: StemStyle = StemStyle.STRAIGHT
    ) {
        if (tracedPath.size > 1) {
            when (stemStyle) {
                StemStyle.STRAIGHT -> drawStraightStem(canvas, tracedPath, time, baseStrokeWidth, maxStrokeWidth)
                StemStyle.THORNY -> drawThornyStem(canvas, tracedPath, time, baseStrokeWidth, maxStrokeWidth)
                StemStyle.THICK -> drawThickStem(canvas, tracedPath, time, baseStrokeWidth, maxStrokeWidth)
                StemStyle.CURVED -> drawCurvedStem(canvas, tracedPath, time, baseStrokeWidth, maxStrokeWidth)
                StemStyle.BAMBOO -> drawBambooStem(canvas, tracedPath, time, baseStrokeWidth, maxStrokeWidth)
            }
        }
    }
    
    private fun drawStraightStem(canvas: Canvas, tracedPath: List<TracePoint>, time: Float, baseStrokeWidth: Float, maxStrokeWidth: Float) {
        // Code actuel pour marguerite
        for (i in 1 until tracedPath.size) {
            val point = tracedPath[i]
            val prevPoint = tracedPath[i-1]
            
            val thickness = lerp(maxStrokeWidth, baseStrokeWidth, i.toFloat() / tracedPath.size.toFloat())
            
            val heightFactor = i.toFloat() / tracedPath.size.toFloat()
            val windStrength = 1f + heightFactor * 2f
            val baseOscillation = kotlin.math.sin(time * 0.8f + point.y * 0.005f) * windStrength
            val microOscillation = kotlin.math.sin(time * 3f + i * 0.2f) * 0.5f * heightFactor
            val totalOscillation = baseOscillation + microOscillation
            
            val heightRatio = i.toFloat() / tracedPath.size.toFloat()
            val greenBase = (40 + heightRatio * 25).toInt().coerceIn(35, 70)
            val greenVariation = kotlin.math.sin(point.y * 0.03f + time * 0.5f) * 5
            val finalGreen = (greenBase + greenVariation).toInt().coerceIn(30, 80)
            
            stemPaint.color = Color.rgb(finalGreen, 85 + (heightRatio * 20).toInt(), finalGreen)
            
            val thicknessVariation = kotlin.math.sin(point.y * 0.05f + time * 0.3f) * 1.5f
            stemPaint.strokeWidth = thickness + thicknessVariation
            
            val adjustedX1 = prevPoint.x + kotlin.math.sin(time * 0.8f + prevPoint.y * 0.005f) * windStrength * 0.8f
            val adjustedX2 = point.x + totalOscillation
            
            canvas.drawLine(adjustedX1, prevPoint.y, adjustedX2, point.y, stemPaint)
            
            if (i % 4 == 0) {
                stemPaint.style = Paint.Style.FILL
                stemPaint.color = Color.rgb(finalGreen - 8, 75, finalGreen - 8)
                val nodePulse = 1f + kotlin.math.sin(time * 2f + i * 0.3f) * 0.2f
                canvas.drawCircle(adjustedX2, point.y, thickness * 0.4f * nodePulse, stemPaint)
                stemPaint.style = Paint.Style.STROKE
            }
        }
    }
    
    private fun drawThornyStem(canvas: Canvas, tracedPath: List<TracePoint>, time: Float, baseStrokeWidth: Float, maxStrokeWidth: Float) {
        // Rose - tige avec épines
        drawStraightStem(canvas, tracedPath, time, baseStrokeWidth, maxStrokeWidth) // Base
        
        // Ajouter des épines
        for (i in 2 until tracedPath.size step 3) {
            val point = tracedPath[i]
            val thornLength = 8f + kotlin.math.sin(time + i * 0.5f) * 2f
            val thornAngle = if (i % 2 == 0) 45f else -45f
            
            stemPaint.color = Color.rgb(100, 60, 40) // Brun pour épines
            stemPaint.strokeWidth = 3f
            
            val thornEndX = point.x + kotlin.math.cos(Math.toRadians(thornAngle.toDouble())).toFloat() * thornLength
            val thornEndY = point.y + kotlin.math.sin(Math.toRadians(thornAngle.toDouble())).toFloat() * thornLength
            
            canvas.drawLine(point.x, point.y, thornEndX, thornEndY, stemPaint)
        }
    }
    
    private fun drawThickStem(canvas: Canvas, tracedPath: List<TracePoint>, time: Float, baseStrokeWidth: Float, maxStrokeWidth: Float) {
        // Tournesol - tige très épaisse
        val thickMultiplier = 2.5f
        drawStraightStem(canvas, tracedPath, time, baseStrokeWidth * thickMultiplier, maxStrokeWidth * thickMultiplier)
    }
    
    private fun drawCurvedStem(canvas: Canvas, tracedPath: List<TracePoint>, time: Float, baseStrokeWidth: Float, maxStrokeWidth: Float) {
        // Lys - naturellement courbé avec plus de fluidité
        // Similar to straight but with more pronounced curves
        drawStraightStem(canvas, tracedPath, time, baseStrokeWidth, maxStrokeWidth)
    }
    
    private fun drawBambooStem(canvas: Canvas, tracedPath: List<TracePoint>, time: Float, baseStrokeWidth: Float, maxStrokeWidth: Float) {
        // Bambou - segments distincts
        drawStraightStem(canvas, tracedPath, time, baseStrokeWidth, maxStrokeWidth)
        
        // Ajouter des nœuds de bambou
        for (i in 5 until tracedPath.size step 8) {
            val point = tracedPath[i]
            stemPaint.style = Paint.Style.FILL
            stemPaint.color = Color.rgb(120, 140, 80)
            canvas.drawCircle(point.x, point.y, baseStrokeWidth * 1.5f, stemPaint)
            stemPaint.style = Paint.Style.STROKE
        }
    }
    
    // ==================== FEUILLES SELON LE STYLE ====================
    
    fun drawRealistic3DLeaves(
        canvas: Canvas, 
        feuilles: List<Feuille>, 
        time: Float,
        leafStyle: LeafStyle = LeafStyle.SERRATED
    ) {
        for (feuille in feuilles) {
            if (feuille.longueur > 5) {
                when (leafStyle) {
                    LeafStyle.SERRATED -> drawSerratedLeaf(canvas, feuille, time)
                    LeafStyle.COMPOUND -> drawCompoundLeaf(canvas, feuille, time)
                    LeafStyle.HEART_SHAPED -> drawHeartLeaf(canvas, feuille, time)
                    LeafStyle.LONG_THIN -> drawLongThinLeaf(canvas, feuille, time)
                    LeafStyle.OVAL -> drawOvalLeaf(canvas, feuille, time)
                }
            }
        }
    }
    
    private fun drawSerratedLeaf(canvas: Canvas, feuille: Feuille, time: Float) {
        // Code actuel des feuilles dentelées de marguerite
        val leafOscillation = kotlin.math.sin(time * 1.2f + feuille.bourgeon.y * 0.008f) * 12f
        val microWave = kotlin.math.sin(time * 4f + feuille.hashCode() * 0.01f) * 3f
        val totalMovement = leafOscillation + microWave
        
        val leafIndex = feuille.hashCode() % 1000
        val tiltAngle = (leafIndex % 50 - 25).toFloat()
        val perspectiveFactor = Math.cos(Math.toRadians(tiltAngle.toDouble())).toFloat()
        
        val sizeMultiplier = 1.34f
        val displayWidth = feuille.largeur * sizeMultiplier * kotlin.math.abs(perspectiveFactor).coerceAtLeast(0.2f)
        val displayLength = feuille.longueur * sizeMultiplier
        
        canvas.save()
        canvas.translate(feuille.bourgeon.x + totalMovement, feuille.bourgeon.y)
        
        val rotationWave = kotlin.math.sin(time * 0.8f + feuille.bourgeon.y * 0.01f) * 8f
        canvas.rotate(feuille.angle + rotationWave)
        
        val lightingFactor = kotlin.math.abs(perspectiveFactor)
        val colorPulse = kotlin.math.sin(time * 0.5f + feuille.hashCode() * 0.01f) * 0.1f
        val brightness = (0.5f + lightingFactor * 0.3f + colorPulse).coerceIn(0.3f, 0.8f)
        val greenValue = (25 + brightness * 30).toInt()
        
        val leafPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.rgb(greenValue, (70 + brightness * 20).toInt(), greenValue)
        }
        
        // Dessiner la feuille dentelée comme actuellement...
        // [Code de la feuille dentelée existant]
        
        canvas.restore()
    }
    
    private fun drawCompoundLeaf(canvas: Canvas, feuille: Feuille, time: Float) {
        // Rose - feuilles composées (plusieurs folioles)
        canvas.save()
        canvas.translate(feuille.bourgeon.x, feuille.bourgeon.y)
        canvas.rotate(feuille.angle)
        
        val leafPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.rgb(60, 120, 60)
        }
        
        // Dessiner 3-5 folioles
        for (i in 0..4) {
            val folioleY = feuille.longueur * i / 4f
            val folioleWidth = feuille.largeur * (0.3f + 0.4f * (1f - i / 4f))
            
            canvas.drawOval(-folioleWidth/2, folioleY - folioleWidth/3, 
                           folioleWidth/2, folioleY + folioleWidth/3, leafPaint)
        }
        
        canvas.restore()
    }
    
    private fun drawHeartLeaf(canvas: Canvas, feuille: Feuille, time: Float) {
        // Tournesol - feuilles en forme de cœur
        canvas.save()
        canvas.translate(feuille.bourgeon.x, feuille.bourgeon.y)
        canvas.rotate(feuille.angle)
        
        val leafPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.rgb(70, 140, 70)
        }
        
        val heartPath = Path()
        val width = feuille.largeur
        val height = feuille.longueur
        
        // Forme de cœur
        heartPath.moveTo(0f, height * 0.3f)
        heartPath.cubicTo(-width * 0.3f, 0f, -width * 0.5f, height * 0.2f, 0f, height)
        heartPath.cubicTo(width * 0.5f, height * 0.2f, width * 0.3f, 0f, 0f, height * 0.3f)
        
        canvas.drawPath(heartPath, leafPaint)
        canvas.restore()
    }
    
    private fun drawLongThinLeaf(canvas: Canvas, feuille: Feuille, time: Float) {
        // Lys - feuilles longues et fines
        canvas.save()
        canvas.translate(feuille.bourgeon.x, feuille.bourgeon.y)
        canvas.rotate(feuille.angle)
        
        val leafPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.rgb(50, 100, 50)
        }
        
        // Feuille très allongée
        canvas.drawOval(-feuille.largeur * 0.2f, 0f, 
                       feuille.largeur * 0.2f, feuille.longueur * 1.5f, leafPaint)
        
        canvas.restore()
    }
    
    private fun drawOvalLeaf(canvas: Canvas, feuille: Feuille, time: Float) {
        // Feuille standard ovale
        canvas.save()
        canvas.translate(feuille.bourgeon.x, feuille.bourgeon.y)
        canvas.rotate(feuille.angle)
        
        val leafPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = Color.rgb(80, 130, 80)
        }
        
        canvas.drawOval(-feuille.largeur/2, 0f, feuille.largeur/2, feuille.longueur, leafPaint)
        canvas.restore()
    }
    
    // ==================== FLEURS SELON LE STYLE ====================
    
    fun drawRealisticFlower(
        canvas: Canvas, 
        fleur: Fleur?, 
        time: Float,
        flowerStyle: FlowerStyle = FlowerStyle.DAISY
    ) {
        fleur?.let { flower ->
            if (flower.taille > 5f) {
                when (flowerStyle) {
                    FlowerStyle.DAISY -> drawDaisyFlower(canvas, flower, time)
                    FlowerStyle.LAYERED_PETALS -> drawRoseFlower(canvas, flower, time)
                    FlowerStyle.LARGE_CENTER -> drawSunflowerFlower(canvas, flower, time)
                    FlowerStyle.TRUMPET -> drawTrumpetFlower(canvas, flower, time)
                    FlowerStyle.BELL -> drawBellFlower(canvas, flower, time)
                }
            }
        }
    }
    
    private fun drawDaisyFlower(canvas: Canvas, flower: Fleur, time: Float) {
        // Code actuel de la marguerite
        // [Votre code existant pour la marguerite]
    }
    
    private fun drawRoseFlower(canvas: Canvas, flower: Fleur, time: Float) {
        // Rose - pétales en couches multiples
        canvas.save()
        canvas.translate(flower.x, flower.y)
        
        val progressRatio = (flower.taille / 175f).coerceAtMost(1f)
        
        // Plusieurs couches de pétales
        for (layer in 0..2) {
            val petalCount = 8 - layer * 2
            val petalSize = progressRatio * (30f - layer * 8f)
            
            for (i in 0 until petalCount) {
                val angle = i * 360f / petalCount + layer * 15f + time * 5f
                canvas.save()
                canvas.rotate(angle)
                
                val petalPaint = Paint().apply {
                    color = when (layer) {
                        0 -> Color.rgb(220, 100, 120) // Externe
                        1 -> Color.rgb(240, 120, 140) // Milieu  
                        else -> Color.rgb(255, 140, 160) // Interne
                    }
                    isAntiAlias = true
                    style = Paint.Style.FILL
                }
                
                canvas.drawOval(-petalSize/3, 0f, petalSize/3, petalSize, petalPaint)
                canvas.restore()
            }
        }
        
        canvas.restore()
    }
    
    private fun drawSunflowerFlower(canvas: Canvas, flower: Fleur, time: Float) {
        // Tournesol - gros centre + pétales jaunes
        canvas.save()
        canvas.translate(flower.x, flower.y)
        
        val progressRatio = (flower.taille / 175f).coerceAtMost(1f)
        val centerSize = progressRatio * 60f // Beaucoup plus gros
        val petalLength = progressRatio * 40f
        
        // Pétales jaunes
        for (i in 0..15) {
            val angle = i * 22.5f + time * 10f
            canvas.save()
            canvas.rotate(angle)
            
            val petalPaint = Paint().apply {
                color = Color.rgb(255, 215, 0) // Jaune vif
                isAntiAlias = true
                style = Paint.Style.FILL
            }
            
            canvas.drawOval(-petalLength/5, centerSize/2, petalLength/5, centerSize/2 + petalLength, petalPaint)
            canvas.restore()
        }
        
        // Gros centre brun
        val centerPaint = Paint().apply {
            color = Color.rgb(101, 67, 33)
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        canvas.drawCircle(0f, 0f, centerSize, centerPaint)
        
        canvas.restore()
    }
    
    private fun drawTrumpetFlower(canvas: Canvas, flower: Fleur, time: Float) {
        // Lys - forme de trompette
        canvas.save()
        canvas.translate(flower.x, flower.y)
        
        val progressRatio = (flower.taille / 175f).coerceAtMost(1f)
        val trumpetLength = progressRatio * 50f
        
        val trumpetPaint = Paint().apply {
            color = Color.rgb(255, 255, 255) // Blanc
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        
        // Forme de trompette
        val trumpetPath = Path()
        trumpetPath.moveTo(0f, 0f)
        trumpetPath.cubicTo(-trumpetLength/4, -trumpetLength/3, 
                          -trumpetLength/2, -trumpetLength/2, 
                          -trumpetLength, -trumpetLength)
        trumpetPath.cubicTo(-trumpetLength * 0.8f, -trumpetLength * 1.2f,
                          trumpetLength * 0.8f, -trumpetLength * 1.2f,
                          trumpetLength, -trumpetLength)
        trumpetPath.cubicTo(trumpetLength/2, -trumpetLength/2,
                          trumpetLength/4, -trumpetLength/3,
                          0f, 0f)
        
        canvas.drawPath(trumpetPath, trumpetPaint)
        canvas.restore()
    }
    
    private fun drawBellFlower(canvas: Canvas, flower: Fleur, time: Float) {
        // Campanule - forme de cloche
        canvas.save()
        canvas.translate(flower.x, flower.y)
        
        val progressRatio = (flower.taille / 175f).coerceAtMost(1f)
        val bellSize = progressRatio * 30f
        
        val bellPaint = Paint().apply {
            color = Color.rgb(138, 43, 226) // Violet
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        
        // Forme de cloche
        canvas.drawOval(-bellSize, -bellSize/2, bellSize, bellSize, bellPaint)
        
        canvas.restore()
    }
    
    // ==================== MÉTHODES EXISTANTES ====================
    
    fun drawGrowthPoint(canvas: Canvas, topPoint: TracePoint, time: Float) {
        val pointOscillation = kotlin.math.sin(time * 2f) * 3f
        basePaint.color = 0xFF90EE90.toInt()
        basePaint.style = Paint.Style.FILL
        canvas.drawCircle(topPoint.x + pointOscillation, topPoint.y, 4f, basePaint)
    }
    
    fun drawAttachmentPoints(canvas: Canvas, bourgeons: List<Bourgeon>, time: Float) {
        basePaint.color = 0xFF6B4423.toInt()
        basePaint.style = Paint.Style.FILL
        for (bourgeon in bourgeons) {
            if (bourgeon.taille > 1f) {
                val oscillation = kotlin.math.sin(time + bourgeon.y * 0.01f) * 1f
                canvas.drawCircle(bourgeon.x + oscillation, bourgeon.y, 3f, basePaint)
                basePaint.color = 0xFF8B5A2B.toInt()
                canvas.drawCircle(bourgeon.x + oscillation - 1f, bourgeon.y - 1f, 1.5f, basePaint)
                basePaint.color = 0xFF6B4423.toInt()
            }
        }
    }
}
