package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Bitmap
import android.graphics.Path
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
    
    // NOUVELLE version naturelle de la tige
    fun drawRealisticStem(
        canvas: Canvas, 
        tracedPath: List<OrganicLineView.TracePoint>,
        time: Float,
        baseStrokeWidth: Float,
        maxStrokeWidth: Float
    ) {
        if (tracedPath.size > 1) {
            // Dessiner la tige principale avec courbes fluides
            val path = Path()
            
            for (i in tracedPath.indices) {
                val point = tracedPath[i]
                val thickness = lerp(maxStrokeWidth, baseStrokeWidth, i.toFloat() / tracedPath.size.toFloat())
                
                // Variation naturelle d'épaisseur
                val naturalVariation = kotlin.math.sin(point.y * 0.02f) * 0.5f
                val finalThickness = thickness + naturalVariation
                
                // Oscillation douce
                val oscillation = kotlin.math.sin(time + point.y * 0.01f) * 1.5f
                val adjustedX = point.x + oscillation
                
                // Couleur naturelle avec dégradé subtil
                val heightRatio = i.toFloat() / tracedPath.size.toFloat()
                val greenBase = 47 + (heightRatio * 20).toInt()
                val greenVariation = kotlin.math.sin(point.y * 0.05f) * 8
                val finalGreen = (greenBase + greenVariation).toInt().coerceIn(35, 75)
                
                stemPaint.color = android.graphics.Color.rgb(finalGreen, 85 + (heightRatio * 25).toInt(), finalGreen)
                stemPaint.strokeWidth = finalThickness
                
                if (i == 0) {
                    path.moveTo(adjustedX, point.y)
                } else {
                    val prevPoint = tracedPath[i-1]
                    val prevOscillation = kotlin.math.sin(time + prevPoint.y * 0.01f) * 1.5f
                    val prevX = prevPoint.x + prevOscillation
                    
                    // Courbe douce au lieu de ligne droite
                    val controlX = (prevX + adjustedX) / 2f
                    val controlY = (prevPoint.y + point.y) / 2f + kotlin.math.sin(i * 0.5f) * 3f
                    
                    // Ligne simple mais douce
                    canvas.drawLine(prevX, prevPoint.y, adjustedX, point.y, stemPaint)
                }
            }
        }
    }
    
    // NOUVELLE fonction pour les petits pics aléatoires
    fun drawStemSpikes(
        canvas: Canvas,
        tracedPath: List<OrganicLineView.TracePoint>,
        time: Float
    ) {
        val spikePaint = Paint().apply {
            color = 0xFF1F3F1F.toInt()
            strokeWidth = 2.5f
            isAntiAlias = true
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        }
        
        if (tracedPath.size > 1) {
            for (i in 2 until tracedPath.size) {
                val point = tracedPath[i]
                
                // Génération aléatoire déterministe
                val seed = (point.x * 137 + point.y * 73).toInt()
                val random1 = kotlin.math.abs(kotlin.math.sin(seed * 0.01f))
                val random2 = kotlin.math.abs(kotlin.math.cos(seed * 0.013f))
                
                // Pics occasionnels (environ 15% de chance)
                if (random1 > 0.85f) {
                    val oscillation = kotlin.math.sin(time + point.y * 0.01f) * 1.5f
                    val adjustedX = point.x + oscillation
                    
                    // Calculer l'angle de la tige
                    val prevPoint = tracedPath[i-1]
                    val dx = point.x - prevPoint.x
                    val dy = point.y - prevPoint.y
                    val stemAngle = kotlin.math.atan2(dy, dx)
                    
                    // Angle perpendiculaire pour le pic
                    val sideAngle = stemAngle + (if (random2 > 0.5f) kotlin.math.PI / 2 else -kotlin.math.PI / 2)
                    
                    // Longueur variable du pic
                    val spikeLength = 5f + random2.toFloat() * 8f
                    
                    val spikeEndX = adjustedX + kotlin.math.cos(sideAngle).toFloat() * spikeLength
                    val spikeEndY = point.y + kotlin.math.sin(sideAngle).toFloat() * spikeLength
                    
                    // Dessiner le pic
                    canvas.drawLine(adjustedX, point.y, spikeEndX, spikeEndY, spikePaint)
                    
                    // Petite base plus épaisse
                    spikePaint.strokeWidth = 4f
                    val baseLength = spikeLength * 0.25f
                    val baseEndX = adjustedX + kotlin.math.cos(sideAngle).toFloat() * baseLength
                    val baseEndY = point.y + kotlin.math.sin(sideAngle).toFloat() * baseLength
                    canvas.drawLine(adjustedX, point.y, baseEndX, baseEndY, spikePaint)
                    spikePaint.strokeWidth = 2.5f
                }
                
                // Micro-pics plus petits (20% de chance)
                if (random2 > 0.8f && random1 <= 0.85f) {
                    val oscillation = kotlin.math.sin(time + point.y * 0.01f) * 1.5f
                    val adjustedX = point.x + oscillation
                    
                    spikePaint.strokeWidth = 1.5f
                    val microLength = 3f + random1.toFloat() * 3f
                    val microAngle = random2.toFloat() * 2f * kotlin.math.PI.toFloat()
                    
                    val microEndX = adjustedX + kotlin.math.cos(microAngle) * microLength
                    val microEndY = point.y + kotlin.math.sin(microAngle) * microLength
                    
                    canvas.drawLine(adjustedX, point.y, microEndX, microEndY, spikePaint)
                    spikePaint.strokeWidth = 2.5f
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
    
    // AMÉLIORÉ : Feuilles encore plus longues
    fun drawLeaves(
        canvas: Canvas, 
        feuilles: List<OrganicLineView.Feuille>, 
        leafBitmap: Bitmap, 
        time: Float
    ) {
        for (feuille in feuilles) {
            if (feuille.longueur > 5) {
                val leafOscillation = kotlin.math.sin(time * 1.5f + feuille.bourgeon.y * 0.01f) * 8f
                
                // Feuilles encore plus longues
                val baseScale = 0.055f
                val lengthScale = kotlin.math.min(feuille.longueur / 250f, baseScale * 3.2f) // Encore plus long
                val widthScale = kotlin.math.min(feuille.largeur / 200f, baseScale * 1.2f)
                
                val leafW = leafBitmap.width.toFloat() * widthScale
                val leafH = leafBitmap.height.toFloat() * lengthScale
                
                val angleRad = feuille.angle * kotlin.math.PI / 180.0
                val leafCenterX = feuille.bourgeon.x + leafOscillation + kotlin.math.cos(angleRad).toFloat() * (leafW * 0.5f)
                val leafCenterY = feuille.bourgeon.y + kotlin.math.sin(angleRad).toFloat() * (leafW * 0.5f)
                
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
    
    // AMÉLIORÉ : Fleurs encore 20% plus grosses
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
                val scale = progressRatio * 2.24f // Encore +20% (1.87f * 1.2)
                val w = flowerBitmap.width.toFloat() * scale
                val h = flowerBitmap.height.toFloat() * scale
                
                val maxSize = 780f // Encore +20% (650f * 1.2)
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
