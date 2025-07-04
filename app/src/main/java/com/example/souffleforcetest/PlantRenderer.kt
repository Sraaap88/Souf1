package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Bitmap
import android.content.Context

// Fichier de rendu - NE PAS MODIFIER
class PlantRenderer(private val context: Context) {
    
    private val stemPaint = Paint().apply {
        color = 0xFF2F4F2F.toInt()
        strokeWidth = 8f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }
    
    private val basePaint = Paint().apply {
        color = 0xFFFFFFFF.toInt()
        strokeWidth = 4f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        style = Paint.Style.STROKE
    }
    
    fun lerp(start: Float, end: Float, fraction: Float): Float {
        return start + fraction * (end - start)
    }
    
    fun drawRealisticStem(
        canvas: Canvas, 
        tracedPath: List<OrganicLineView.TracePoint>,
        time: Float,
        baseStrokeWidth: Float,
        maxStrokeWidth: Float
    ) {
        if (tracedPath.size > 1) {
            // 1. SEGMENTS/NŒUDS - Calculer les positions des nœuds
            val nodeSpacing = 60f
            val nodes = mutableListOf<Pair<Float, Float>>()
            var currentDistance = 0f
            
            for (i in 1 until tracedPath.size) {
                val point = tracedPath[i]
                val prevPoint = tracedPath[i-1]
                val segmentLength = kotlin.math.sqrt(
                    (point.x - prevPoint.x) * (point.x - prevPoint.x) + 
                    (point.y - prevPoint.y) * (point.y - prevPoint.y)
                )
                currentDistance += segmentLength
                
                if (currentDistance >= nodeSpacing) {
                    nodes.add(Pair(point.x, point.y))
                    currentDistance = 0f
                }
            }
            
            // 2. TEXTURE FIBREUSE - Dessiner les fibres principales
            for (i in tracedPath.indices) {
                val point = tracedPath[i]
                val thickness = lerp(maxStrokeWidth, baseStrokeWidth, i.toFloat() / tracedPath.size.toFloat())
                val oscillation = kotlin.math.sin(time + point.y * 0.01f) * 2f
                val adjustedX = point.x + oscillation
                
                // 3. RELIEF 3D - Couleurs pour l'effet 3D
                val heightRatio = i.toFloat() / tracedPath.size.toFloat()
                val baseGreen = (47 + heightRatio * 30).toInt().coerceIn(47, 77)
                
                // Ligne principale (côté éclairé)
                stemPaint.color = android.graphics.Color.rgb(baseGreen + 20, 79 + (heightRatio * 40).toInt(), baseGreen + 20)
                stemPaint.strokeWidth = thickness
                
                if (i < tracedPath.size - 1) {
                    val nextPoint = tracedPath[i + 1]
                    val nextOscillation = kotlin.math.sin(time + nextPoint.y * 0.01f) * 2f
                    canvas.drawLine(adjustedX, point.y, nextPoint.x + nextOscillation, nextPoint.y, stemPaint)
                }
                
                // Fibres parallèles (côtés)
                val fiberOffset = thickness * 0.3f
                stemPaint.strokeWidth = thickness * 0.4f
                stemPaint.color = android.graphics.Color.rgb(baseGreen, 79 + (heightRatio * 20).toInt(), baseGreen)
                
                if (i < tracedPath.size - 1) {
                    val nextPoint = tracedPath[i + 1]
                    val nextOscillation = kotlin.math.sin(time + nextPoint.y * 0.01f) * 2f
                    
                    // Fibre gauche
                    canvas.drawLine(
                        adjustedX - fiberOffset, point.y,
                        nextPoint.x + nextOscillation - fiberOffset, nextPoint.y,
                        stemPaint
                    )
                    // Fibre droite
                    canvas.drawLine(
                        adjustedX + fiberOffset, point.y,
                        nextPoint.x + nextOscillation + fiberOffset, nextPoint.y,
                        stemPaint
                    )
                }
                
                // Ombre (côté sombre)
                stemPaint.strokeWidth = thickness * 0.6f
                stemPaint.color = android.graphics.Color.rgb(baseGreen - 15, 79, baseGreen - 15)
                
                if (i < tracedPath.size - 1) {
                    val nextPoint = tracedPath[i + 1]
                    val nextOscillation = kotlin.math.sin(time + nextPoint.y * 0.01f) * 2f
                    val shadowOffset = thickness * 0.2f
                    
                    canvas.drawLine(
                        adjustedX + shadowOffset, point.y,
                        nextPoint.x + nextOscillation + shadowOffset, nextPoint.y,
                        stemPaint
                    )
                }
                
                // Texture transversale (tous les 20 pixels)
                if (i % 3 == 0 && i > 0) {
                    stemPaint.strokeWidth = 2f
                    stemPaint.color = android.graphics.Color.rgb(baseGreen - 10, 69, baseGreen - 10)
                    val fiberLength = thickness * 0.8f
                    
                    canvas.drawLine(
                        adjustedX - fiberLength/2, point.y,
                        adjustedX + fiberLength/2, point.y,
                        stemPaint
                    )
                }
            }
            
            // Dessiner les nœuds (renflements)
            stemPaint.style = Paint.Style.FILL
            for (node in nodes) {
                val oscillation = kotlin.math.sin(time + node.second * 0.01f) * 2f
                val nodeX = node.first + oscillation
                
                // Nœud principal
                stemPaint.color = android.graphics.Color.rgb(67, 99, 67)
                canvas.drawCircle(nodeX, node.second, 8f, stemPaint)
                
                // Highlight du nœud
                stemPaint.color = android.graphics.Color.rgb(87, 119, 87)
                canvas.drawCircle(nodeX - 2f, node.second - 2f, 5f, stemPaint)
            }
            stemPaint.style = Paint.Style.STROKE
        }
    }
    
    // NOUVEAU : Fonction pour dessiner les poils de la tige
    fun drawStemHairs(
        canvas: Canvas,
        tracedPath: List<OrganicLineView.TracePoint>,
        time: Float
    ) {
        val hairPaint = Paint().apply {
            color = 0xFF1F3F1F.toInt() // Vert très sombre
            strokeWidth = 1f
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        }
        
        if (tracedPath.size > 1) {
            for (i in 1 until tracedPath.size) {
                val point = tracedPath[i]
                val prevPoint = tracedPath[i-1]
                
                // Densité des poils - un poil tous les 15 pixels environ
                val segmentLength = kotlin.math.sqrt(
                    (point.x - prevPoint.x) * (point.x - prevPoint.x) + 
                    (point.y - prevPoint.y) * (point.y - prevPoint.y)
                )
                
                if (segmentLength > 15f && i % 2 == 0) {
                    val oscillation = kotlin.math.sin(time + point.y * 0.01f) * 2f
                    val adjustedX = point.x + oscillation
                    
                    // Calcul de l'angle perpendiculaire au segment
                    val dx = point.x - prevPoint.x
                    val dy = point.y - prevPoint.y
                    val angle = kotlin.math.atan2(dy, dx)
                    val perpAngle1 = angle + kotlin.math.PI / 2
                    val perpAngle2 = angle - kotlin.math.PI / 2
                    
                    // Longueur des poils variable selon la hauteur
                    val heightRatio = i.toFloat() / tracedPath.size.toFloat()
                    val hairLength = 8f + heightRatio * 12f
                    
                    // Poils de chaque côté avec légère variation
                    val variation1 = (kotlin.math.sin(time * 2f + i * 0.5f) * 0.3f).toFloat()
                    val variation2 = (kotlin.math.cos(time * 1.8f + i * 0.7f) * 0.3f).toFloat()
                    
                    // Poil côté gauche
                    val hairX1 = adjustedX + kotlin.math.cos(perpAngle1).toFloat() * (hairLength + variation1)
                    val hairY1 = point.y + kotlin.math.sin(perpAngle1).toFloat() * (hairLength + variation1)
                    canvas.drawLine(adjustedX, point.y, hairX1, hairY1, hairPaint)
                    
                    // Poil côté droit
                    val hairX2 = adjustedX + kotlin.math.cos(perpAngle2).toFloat() * (hairLength + variation2)
                    val hairY2 = point.y + kotlin.math.sin(perpAngle2).toFloat() * (hairLength + variation2)
                    canvas.drawLine(adjustedX, point.y, hairX2, hairY2, hairPaint)
                }
            }
        }
    }
    
    fun drawGrowthPoint(canvas: Canvas, topPoint: OrganicLineView.TracePoint, time: Float) {
        val pointOscillation = kotlin.math.sin(time * 2f) * 3f
        basePaint.color = 0xFF90EE90.toInt()
        basePaint.style = Paint.Style.FILL
        canvas.drawCircle(topPoint.x + pointOscillation, topPoint.y, 4f, basePaint)
    }
    
    fun drawAttachmentPoints(canvas: Canvas, bourgeons: List<OrganicLineView.Bourgeon>, time: Float) {
        basePaint.color = 0xFF8B4513.toInt()
        basePaint.style = Paint.Style.FILL
        for (bourgeon in bourgeons) {
            if (bourgeon.taille > 1f) {
                val oscillation = kotlin.math.sin(time + bourgeon.y * 0.01f) * 1f
                canvas.drawCircle(bourgeon.x + oscillation, bourgeon.y, 2f, basePaint)
            }
        }
    }
    
    fun drawLeaves(
        canvas: Canvas, 
        feuilles: List<OrganicLineView.Feuille>, 
        leafBitmap: Bitmap, 
        time: Float
    ) {
        for (feuille in feuilles) {
            if (feuille.longueur > 5) {
                val leafOscillation = kotlin.math.sin(time * 1.5f + feuille.bourgeon.y * 0.01f) * 8f
                
                // AMÉLIORATION : Calcul séparé largeur/longueur au lieu d'un scale uniforme
                val baseScale = 0.055f
                val lengthScale = kotlin.math.min(feuille.longueur / 400f, baseScale * 2f) // Plus d'extension en longueur
                val widthScale = kotlin.math.min(feuille.largeur / 200f, baseScale * 1.2f)  // Moins d'extension en largeur
                
                val leafW = leafBitmap.width.toFloat() * widthScale
                val leafH = leafBitmap.height.toFloat() * lengthScale
                
                // AMÉLIORATION : Ajustement du pétiole - décalage pour compenser sa position dans l'image
                val petioleOffsetInImage = 0.15f // Le pétiole est à 15% du bord gauche dans l'image
                val actualPetioleOffset = leafW * petioleOffsetInImage
                
                // Position de la feuille avec correction du pétiole
                val angleRad = feuille.angle * kotlin.math.PI / 180.0
                val leafX = feuille.bourgeon.x + leafOscillation + kotlin.math.cos(angleRad).toFloat() * actualPetioleOffset
                val leafY = feuille.bourgeon.y + kotlin.math.sin(angleRad).toFloat() * actualPetioleOffset
                
                canvas.save()
                canvas.translate(leafX, leafY)
                canvas.rotate(feuille.angle + leafOscillation * 0.5f)
                
                val paint = Paint().apply {
                    isAntiAlias = true
                    isFilterBitmap = true
                    alpha = 220
                }
                
                // AMÉLIORATION : Rectangle avec largeur et hauteur séparées
                val dstRect = RectF(-leafW/2, -leafH/2, leafW/2, leafH/2)
                canvas.drawBitmap(leafBitmap, null, dstRect, paint)
                canvas.restore()
            }
        }
    }
    
    fun drawFlower(
        canvas: Canvas, 
        fleur: OrganicLineView.Fleur?, 
        flowerBitmap: Bitmap, 
        time: Float
    ) {
        fleur?.let { flower ->
            if (flower.taille > 5f) {
                val flowerOscillation = kotlin.math.sin(time * 0.8f) * 5f
                
                val progressRatio = flower.taille / 175f
                val scale = progressRatio * 1.44f
                val w = flowerBitmap.width.toFloat() * scale
                val h = flowerBitmap.height.toFloat() * scale
                
                val maxSize = 500f
                val finalW = kotlin.math.min(w, maxSize)
                val finalH = kotlin.math.min(h, maxSize)
                
                val paint = Paint().apply {
                    isAntiAlias = true
                    isFilterBitmap = true
                    alpha = 255
                }
                
                val rect = RectF(
                    flower.x - finalW / 2 + flowerOscillation,
                    flower.y - finalH / 2,
                    flower.x + finalW / 2 + flowerOscillation,
                    flower.y + finalH / 2
                )
                canvas.drawBitmap(flowerBitmap, null, rect, paint)
            }
        }
    }
}
