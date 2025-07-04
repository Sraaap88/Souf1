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
    
    // CORRIGÉ : Fonction pour dessiner des petits poils partout sur la tige
    fun drawStemHairs(
        canvas: Canvas,
        tracedPath: List<OrganicLineView.TracePoint>,
        time: Float
    ) {
        val hairPaint = Paint().apply {
            color = 0xFF0F2F0F.toInt() // Vert encore plus sombre pour être visible
            strokeWidth = 2f // Plus épais pour être visible
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        }
        
        if (tracedPath.size > 1) {
            for (i in tracedPath.indices) {
                val point = tracedPath[i]
                val oscillation = kotlin.math.sin(time + point.y * 0.01f) * 2f
                val adjustedX = point.x + oscillation
                
                // Densité augmentée - poils partout
                if (i % 1 == 0) { // Chaque point a des poils
                    
                    // Plusieurs poils autour de chaque point de la tige
                    for (hairIndex in 0..2) {
                        val angleOffset = (hairIndex * 120f) * kotlin.math.PI / 180f // 3 poils à 120° d'écart
                        val variation = kotlin.math.sin(time * 1.5f + i * 0.3f + hairIndex) * 2f
                        
                        // Longueur des poils plus courte mais plus visible
                        val hairLength = 6f + variation
                        
                        // Position du poil
                        val hairX = adjustedX + kotlin.math.cos(angleOffset).toFloat() * hairLength
                        val hairY = point.y + kotlin.math.sin(angleOffset).toFloat() * hairLength
                        
                        // Dessiner le poil
                        canvas.drawLine(adjustedX, point.y, hairX, hairY, hairPaint)
                    }
                    
                    // Poils supplémentaires plus courts
                    for (shortHair in 0..1) {
                        val randomAngle = ((i * 137 + shortHair * 73) % 360) * kotlin.math.PI / 180f
                        val shortLength = 3f + kotlin.math.sin(time + i * 0.1f) * 1f
                        
                        val shortHairX = adjustedX + kotlin.math.cos(randomAngle).toFloat() * shortLength
                        val shortHairY = point.y + kotlin.math.sin(randomAngle).toFloat() * shortLength
                        
                        hairPaint.strokeWidth = 1f
                        canvas.drawLine(adjustedX, point.y, shortHairX, shortHairY, hairPaint)
                        hairPaint.strokeWidth = 2f
                    }
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
    
    // CORRIGÉ : drawLeaves avec meilleur positionnement du pétiole
    fun drawLeaves(
        canvas: Canvas, 
        feuilles: List<OrganicLineView.Feuille>, 
        leafBitmap: Bitmap, 
        time: Float
    ) {
        for (feuille in feuilles) {
            if (feuille.longueur > 5) {
                val leafOscillation = kotlin.math.sin(time * 1.5f + feuille.bourgeon.y * 0.01f) * 8f
                
                // Calcul séparé largeur/longueur
                val baseScale = 0.055f
                val lengthScale = kotlin.math.min(feuille.longueur / 400f, baseScale * 2f)
                val widthScale = kotlin.math.min(feuille.largeur / 200f, baseScale * 1.2f)
                
                val leafW = leafBitmap.width.toFloat() * widthScale
                val leafH = leafBitmap.height.toFloat() * lengthScale
                
                // CORRECTION MAJEURE : Le pétiole part vraiment du point d'attache
                // On place la feuille de façon à ce que le début de l'image (le pétiole) touche le bourgeon
                val angleRad = feuille.angle * kotlin.math.PI / 180.0
                
                // Distance du pétiole depuis le centre de l'image vers le bord
                val petioleDistance = leafW * 0.4f // Le pétiole est à 40% du bord dans l'image
                
                // Position de la feuille décalée pour que le pétiole touche le bourgeon
                val leafCenterX = feuille.bourgeon.x + leafOscillation + kotlin.math.cos(angleRad).toFloat() * petioleDistance
                val leafCenterY = feuille.bourgeon.y + kotlin.math.sin(angleRad).toFloat() * petioleDistance
                
                canvas.save()
                canvas.translate(leafCenterX, leafCenterY)
                canvas.rotate(feuille.angle + leafOscillation * 0.5f)
                
                val paint = Paint().apply {
                    isAntiAlias = true
                    isFilterBitmap = true
                    alpha = 220
                }
                
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
