package com.example.souffleforcetest

import android.graphics.*
import android.content.Context

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
    
    // NOUVELLE tige organique et réaliste
    fun drawRealisticStem(
        canvas: Canvas, 
        tracedPath: List<OrganicLineView.TracePoint>,
        time: Float,
        baseStrokeWidth: Float,
        maxStrokeWidth: Float
    ) {
        if (tracedPath.size > 1) {
            for (i in 1 until tracedPath.size) {
                val point = tracedPath[i]
                val prevPoint = tracedPath[i-1]
                
                val thickness = lerp(maxStrokeWidth, baseStrokeWidth, i.toFloat() / tracedPath.size.toFloat())
                val oscillation = kotlin.math.sin(time + point.y * 0.01f) * 1.5f
                
                // Couleur naturelle avec variation selon la hauteur
                val heightRatio = i.toFloat() / tracedPath.size.toFloat()
                val greenBase = (40 + heightRatio * 25).toInt().coerceIn(35, 70)
                val greenVariation = kotlin.math.sin(point.y * 0.03f) * 5
                val finalGreen = (greenBase + greenVariation).toInt().coerceIn(30, 80)
                
                stemPaint.color = Color.rgb(finalGreen, 85 + (heightRatio * 20).toInt(), finalGreen)
                stemPaint.strokeWidth = thickness + kotlin.math.sin(point.y * 0.05f) * 1.5f
                
                val adjustedX1 = prevPoint.x + kotlin.math.sin(time + prevPoint.y * 0.01f) * 1.5f
                val adjustedX2 = point.x + oscillation
                
                // Ligne principale de la tige
                canvas.drawLine(adjustedX1, prevPoint.y, adjustedX2, point.y, stemPaint)
                
                // Petites bosses organiques tous les segments
                if (i % 4 == 0) {
                    stemPaint.style = Paint.Style.FILL
                    stemPaint.color = Color.rgb(finalGreen - 8, 75, finalGreen - 8)
                    canvas.drawCircle(adjustedX2, point.y, thickness * 0.4f, stemPaint)
                    stemPaint.style = Paint.Style.STROKE
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
        basePaint.color = 0xFF6B4423.toInt()
        basePaint.style = Paint.Style.FILL
        for (bourgeon in bourgeons) {
            if (bourgeon.taille > 1f) {
                val oscillation = kotlin.math.sin(time + bourgeon.y * 0.01f) * 1f
                canvas.drawCircle(bourgeon.x + oscillation, bourgeon.y, 3f, basePaint)
                // Petit reflet
                basePaint.color = 0xFF8B5A2B.toInt()
                canvas.drawCircle(bourgeon.x + oscillation - 1f, bourgeon.y - 1f, 1.5f, basePaint)
                basePaint.color = 0xFF6B4423.toInt()
            }
        }
    }
    
    // NOUVELLES feuilles 3D réalistes avec phases de croissance
    fun drawRealistic3DLeaves(
        canvas: Canvas, 
        feuilles: List<OrganicLineView.Feuille>, 
        time: Float
    ) {
        for (feuille in feuilles) {
            if (feuille.longueur > 5) {
                val leafOscillation = kotlin.math.sin(time * 1.5f + feuille.bourgeon.y * 0.01f) * 6f
                
                // Orientation 3D basée sur la position
                val leafIndex = feuille.hashCode() % 1000
                val tiltAngle = (leafIndex % 50 - 25).toFloat()
                val perspectiveFactor = kotlin.math.cos(Math.toRadians(tiltAngle.toDouble())).toFloat()
                
                // Calcul de la taille avec perspective
                val displayWidth = feuille.largeur * kotlin.math.abs(perspectiveFactor).coerceAtLeast(0.2f)
                val displayLength = feuille.longueur
                
                canvas.save()
                canvas.translate(feuille.bourgeon.x + leafOscillation, feuille.bourgeon.y)
                canvas.rotate(feuille.angle + leafOscillation * 0.4f)
                
                // Couleur selon éclairage et perspective
                val lightingFactor = kotlin.math.abs(perspectiveFactor)
                val brightness = (0.6f + lightingFactor * 0.4f).coerceIn(0.4f, 1f)
                val greenValue = (40 + brightness * 35).toInt()
                
                val leafPaint = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.FILL
                    color = Color.rgb(greenValue, (85 + brightness * 25).toInt(), greenValue)
                }
                
                if (perspectiveFactor > 0.3f) {
                    // Feuille vue de face - forme organique
                    val leafPath = Path()
                    leafPath.moveTo(0f, 0f)
                    
                    // Côté gauche avec courbe naturelle
                    leafPath.cubicTo(
                        -displayWidth * 0.3f, displayLength * 0.2f,
                        -displayWidth * 0.5f, displayLength * 0.6f,
                        -displayWidth * 0.2f, displayLength * 0.9f
                    )
                    leafPath.cubicTo(
                        -displayWidth * 0.1f, displayLength * 0.95f,
                        0f, displayLength,
                        0f, displayLength
                    )
                    
                    // Côté droit symétrique
                    leafPath.cubicTo(
                        0f, displayLength,
                        displayWidth * 0.1f, displayLength * 0.95f,
                        displayWidth * 0.2f, displayLength * 0.9f
                    )
                    leafPath.cubicTo(
                        displayWidth * 0.5f, displayLength * 0.6f,
                        displayWidth * 0.3f, displayLength * 0.2f,
                        0f, 0f
                    )
                    
                    canvas.drawPath(leafPath, leafPaint)
                    
                    // Nervure centrale
                    val nervurePaint = Paint().apply {
                        color = Color.rgb(greenValue - 15, 70, greenValue - 15)
                        strokeWidth = 2f * lightingFactor
                        style = Paint.Style.STROKE
                        isAntiAlias = true
                    }
                    canvas.drawLine(0f, 0f, 0f, displayLength, nervurePaint)
                    
                    // Nervures secondaires
                    nervurePaint.strokeWidth = 1f * lightingFactor
                    for (i in 1..4) {
                        val progress = i.toFloat() / 5f
                        val nervureY = displayLength * progress
                        val nervureLength = displayWidth * 0.3f * (1f - progress * 0.5f)
                        
                        canvas.drawLine(0f, nervureY, -nervureLength, nervureY + displayLength * 0.1f, nervurePaint)
                        canvas.drawLine(0f, nervureY, nervureLength, nervureY + displayLength * 0.1f, nervurePaint)
                    }
                    
                } else {
                    // Feuille vue de profil - forme compressée
                    leafPaint.color = Color.rgb(greenValue - 20, 60, greenValue - 20)
                    val profileWidth = 3f
                    
                    val profilePath = Path()
                    profilePath.moveTo(0f, 0f)
                    profilePath.lineTo(-profileWidth, displayLength * 0.3f)
                    profilePath.lineTo(-profileWidth * 0.5f, displayLength)
                    profilePath.lineTo(profileWidth * 0.5f, displayLength)
                    profilePath.lineTo(profileWidth, displayLength * 0.3f)
                    profilePath.close()
                    
                    canvas.drawPath(profilePath, leafPaint)
                }
                
                canvas.restore()
            }
        }
    }
    
    // NOUVELLE fleur géométrique réaliste
    fun drawRealisticFlower(
        canvas: Canvas, 
        fleur: OrganicLineView.Fleur?, 
        time: Float
    ) {
        fleur?.let { flower ->
            if (flower.taille > 5f) {
                val flowerOscillation = kotlin.math.sin(time * 0.8f) * 4f
                
                canvas.save()
                canvas.translate(flower.x + flowerOscillation, flower.y)
                
                val progressRatio = (flower.taille / 175f).coerceAtMost(1f)
                val petalCount = 6
                val petalLength = 25f + progressRatio * 35f
                val petalWidth = 12f + progressRatio * 18f
                
                // Dessiner chaque pétale
                for (i in 0 until petalCount) {
                    val angle = (i * 360f / petalCount) + kotlin.math.sin(time + i * 0.5f) * 3f
                    
                    canvas.save()
                    canvas.rotate(angle)
                    
                    // Pétale en forme de goutte
                    val petalPaint = Paint().apply {
                        isAntiAlias = true
                        style = Paint.Style.FILL
                    }
                    
                    // Dégradé du rose au blanc
                    val gradient = LinearGradient(
                        0f, 0f, 0f, petalLength,
                        intArrayOf(0xFFFFB6C1.toInt(), 0xFFF8BBD9.toInt(), 0xFFFFFFFF.toInt()),
                        floatArrayOf(0f, 0.5f, 1f),
                        Shader.TileMode.CLAMP
                    )
                    petalPaint.shader = gradient
                    
                    val petalPath = Path()
                    petalPath.moveTo(0f, 0f)
                    petalPath.cubicTo(-petalWidth/2, petalLength * 0.3f, 
                                    -petalWidth/3, petalLength * 0.8f, 
                                    0f, petalLength)
                    petalPath.cubicTo(petalWidth/3, petalLength * 0.8f, 
                                    petalWidth/2, petalLength * 0.3f, 
                                    0f, 0f)
                    
                    canvas.drawPath(petalPath, petalPaint)
                    
                    // Nervure centrale du pétale
                    val nervurePaint = Paint().apply {
                        color = 0xFFE8A0B8.toInt()
                        strokeWidth = 1f
                        style = Paint.Style.STROKE
                        isAntiAlias = true
                    }
                    canvas.drawLine(0f, 0f, 0f, petalLength, nervurePaint)
                    
                    canvas.restore()
                }
                
                // Centre de la fleur (pistil et étamines)
                val centerPaint = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.FILL
                }
                
                // Base du centre (jaune)
                centerPaint.color = 0xFFFFD700.toInt()
                canvas.drawCircle(0f, 0f, 8f + progressRatio * 4f, centerPaint)
                
                // Petites étamines autour
                centerPaint.color = 0xFFFFA500.toInt()
                val stamenCount = 8
                for (i in 0 until stamenCount) {
                    val stamenAngle = i * 360f / stamenCount + time * 10f
                    val stamenRadius = 6f + progressRatio * 2f
                    val stamenX = kotlin.math.cos(Math.toRadians(stamenAngle.toDouble())).toFloat() * stamenRadius
                    val stamenY = kotlin.math.sin(Math.toRadians(stamenAngle.toDouble())).toFloat() * stamenRadius
                    canvas.drawCircle(stamenX, stamenY, 1.5f, centerPaint)
                }
                
                // Point central (pistil)
                centerPaint.color = 0xFFFF6347.toInt()
                canvas.drawCircle(0f, 0f, 2f + progressRatio * 1f, centerPaint)
                
                canvas.restore()
            }
        }
    }
}
