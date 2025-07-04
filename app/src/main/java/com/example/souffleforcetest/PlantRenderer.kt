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
    
    // NOUVELLE tige organique avec animation naturelle
    fun drawRealisticStem(
        canvas: Canvas, 
        tracedPath: List<TracePoint>,
        time: Float,
        baseStrokeWidth: Float,
        maxStrokeWidth: Float
    ) {
        if (tracedPath.size > 1) {
            for (i in 1 until tracedPath.size) {
                val point = tracedPath[i]
                val prevPoint = tracedPath[i-1]
                
                val thickness = lerp(maxStrokeWidth, baseStrokeWidth, i.toFloat() / tracedPath.size.toFloat())
                
                // Animation naturelle plus prononcée
                val heightFactor = i.toFloat() / tracedPath.size.toFloat()
                val windStrength = 1f + heightFactor * 2f // Plus de mouvement en haut
                val baseOscillation = kotlin.math.sin(time * 0.8f + point.y * 0.005f) * windStrength
                val microOscillation = kotlin.math.sin(time * 3f + i * 0.2f) * 0.5f * heightFactor
                val totalOscillation = baseOscillation + microOscillation
                
                // Couleur naturelle avec variation selon la hauteur
                val heightRatio = i.toFloat() / tracedPath.size.toFloat()
                val greenBase = (40 + heightRatio * 25).toInt().coerceIn(35, 70)
                val greenVariation = kotlin.math.sin(point.y * 0.03f + time * 0.5f) * 5
                val finalGreen = (greenBase + greenVariation).toInt().coerceIn(30, 80)
                
                stemPaint.color = Color.rgb(finalGreen, 85 + (heightRatio * 20).toInt(), finalGreen)
                
                // Variation d'épaisseur plus organique
                val thicknessVariation = kotlin.math.sin(point.y * 0.05f + time * 0.3f) * 1.5f
                stemPaint.strokeWidth = thickness + thicknessVariation
                
                val adjustedX1 = prevPoint.x + kotlin.math.sin(time * 0.8f + prevPoint.y * 0.005f) * windStrength * 0.8f
                val adjustedX2 = point.x + totalOscillation
                
                // Ligne principale de la tige avec courbe douce
                canvas.drawLine(adjustedX1, prevPoint.y, adjustedX2, point.y, stemPaint)
                
                // Petites bosses organiques animées
                if (i % 4 == 0) {
                    stemPaint.style = Paint.Style.FILL
                    stemPaint.color = Color.rgb(finalGreen - 8, 75, finalGreen - 8)
                    val nodePulse = 1f + kotlin.math.sin(time * 2f + i * 0.3f) * 0.2f
                    canvas.drawCircle(adjustedX2, point.y, thickness * 0.4f * nodePulse, stemPaint)
                    stemPaint.style = Paint.Style.STROKE
                }
                
                // Animation de croissance (pulse lors de la croissance)
                if (i > tracedPath.size - 5) {
                    val growthPulse = 1f + kotlin.math.sin(time * 8f) * 0.3f
                    stemPaint.strokeWidth = (thickness + thicknessVariation) * growthPulse
                    canvas.drawLine(adjustedX1, prevPoint.y, adjustedX2, point.y, stemPaint)
                }
            }
        }
    }
    
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
                // Petit reflet
                basePaint.color = 0xFF8B5A2B.toInt()
                canvas.drawCircle(bourgeon.x + oscillation - 1f, bourgeon.y - 1f, 1.5f, basePaint)
                basePaint.color = 0xFF6B4423.toInt()
            }
        }
    }
    
    // NOUVELLES feuilles 3D réalistes 10x plus grandes avec animations
    fun drawRealistic3DLeaves(
        canvas: Canvas, 
        feuilles: List<Feuille>, 
        time: Float
    ) {
        for (feuille in feuilles) {
            if (feuille.longueur > 5) {
                // Animation plus prononcée
                val leafOscillation = kotlin.math.sin(time * 1.2f + feuille.bourgeon.y * 0.008f) * 12f
                val microWave = kotlin.math.sin(time * 4f + feuille.hashCode() * 0.01f) * 3f
                val totalMovement = leafOscillation + microWave
                
                // Orientation 3D basée sur la position
                val leafIndex = feuille.hashCode() % 1000
                val tiltAngle = (leafIndex % 50 - 25).toFloat()
                val perspectiveFactor = Math.cos(Math.toRadians(tiltAngle.toDouble())).toFloat()
                
                // FEUILLES 30% plus longues + ajustements
                val sizeMultiplier = 1.84f // Encore plus longues (1.74f → 1.84f)
                val displayWidth = feuille.largeur * sizeMultiplier * 0.8f * kotlin.math.abs(perspectiveFactor).coerceAtLeast(0.2f) // Moins larges (0.8f)
                val displayLength = feuille.longueur * sizeMultiplier
                
                canvas.save()
                canvas.translate(feuille.bourgeon.x + totalMovement, feuille.bourgeon.y)
                
                // Animation de rotation plus naturelle
                val rotationWave = kotlin.math.sin(time * 0.8f + feuille.bourgeon.y * 0.01f) * 8f
                canvas.rotate(feuille.angle + rotationWave)
                
                // Couleur selon éclairage et perspective avec animation (vert foncé marguerite)
                val lightingFactor = kotlin.math.abs(perspectiveFactor)
                val colorPulse = kotlin.math.sin(time * 0.5f + feuille.hashCode() * 0.01f) * 0.1f
                val brightness = (0.5f + lightingFactor * 0.3f + colorPulse).coerceIn(0.3f, 0.8f)
                val greenValue = (25 + brightness * 30).toInt() // Plus sombre pour marguerite
                
                val leafPaint = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.FILL
                    color = Color.rgb(greenValue, (70 + brightness * 20).toInt(), greenValue)
                }
                
                if (perspectiveFactor > 0.3f) {
                    // MARGUERITE : Feuille dentelée caractéristique
                    val leafPath = Path()
                    leafPath.moveTo(0f, 0f)
                    
                    // Ondulation des bords pour effet vivant
                    val edgeWave = kotlin.math.sin(time * 2f) * 0.03f
                    
                    // Côté gauche avec bords fortement dentelés
                    val leftPoints = arrayOf(
                        Pair(-displayWidth * (0.15f + edgeWave), displayLength * 0.2f),
                        Pair(-displayWidth * (0.4f - edgeWave), displayLength * 0.3f),
                        Pair(-displayWidth * (0.25f + edgeWave), displayLength * 0.45f), // Dent
                        Pair(-displayWidth * (0.45f - edgeWave), displayLength * 0.6f),
                        Pair(-displayWidth * (0.3f + edgeWave), displayLength * 0.75f), // Dent
                        Pair(-displayWidth * (0.35f - edgeWave), displayLength * 0.9f),
                        Pair(-displayWidth * 0.1f, displayLength * 0.95f)
                    )
                    
                    for ((x, y) in leftPoints) {
                        leafPath.lineTo(x, y)
                    }
                    leafPath.lineTo(0f, displayLength)
                    
                    // Côté droit symétrique avec dentelures
                    val rightPoints = arrayOf(
                        Pair(displayWidth * 0.1f, displayLength * 0.95f),
                        Pair(displayWidth * (0.35f - edgeWave), displayLength * 0.9f),
                        Pair(displayWidth * (0.3f + edgeWave), displayLength * 0.75f), // Dent
                        Pair(displayWidth * (0.45f - edgeWave), displayLength * 0.6f),
                        Pair(displayWidth * (0.25f + edgeWave), displayLength * 0.45f), // Dent
                        Pair(displayWidth * (0.4f - edgeWave), displayLength * 0.3f),
                        Pair(displayWidth * (0.15f + edgeWave), displayLength * 0.2f)
                    )
                    
                    for ((x, y) in rightPoints) {
                        leafPath.lineTo(x, y)
                    }
                    leafPath.close()
                    
                    canvas.drawPath(leafPath, leafPaint)
                    
                    // Nervure centrale plus marquée
                    val nervurePaint = Paint().apply {
                        color = Color.rgb(greenValue - 10, 50, greenValue - 10)
                        strokeWidth = (3f + sizeMultiplier * 0.2f) * lightingFactor
                        style = Paint.Style.STROKE
                        isAntiAlias = true
                    }
                    canvas.drawLine(0f, 0f, 0f, displayLength, nervurePaint)
                    
                    // Nervures secondaires plus visibles
                    nervurePaint.strokeWidth = (1.5f + sizeMultiplier * 0.1f) * lightingFactor
                    for (i in 1..5) {
                        val progress = i.toFloat() / 6f
                        val nervureY = displayLength * progress
                        val nervureLength = displayWidth * 0.3f * (1f - progress * 0.3f)
                        val nervureWave = kotlin.math.sin(time + i * 0.5f) * 1f
                        
                        canvas.drawLine(0f, nervureY, -nervureLength + nervureWave, nervureY + displayLength * 0.05f, nervurePaint)
                        canvas.drawLine(0f, nervureY, nervureLength + nervureWave, nervureY + displayLength * 0.05f, nervurePaint)
                    }
                    
                } else {
                    // Feuille vue de profil - forme compressée animée
                    leafPaint.color = Color.rgb(greenValue - 20, 60, greenValue - 20)
                    val profileWidth = 6f + sizeMultiplier * 0.2f
                    val profileWave = kotlin.math.sin(time * 3f) * 2f
                    
                    val profilePath = Path()
                    profilePath.moveTo(0f, 0f)
                    profilePath.lineTo(-profileWidth + profileWave, displayLength * 0.3f)
                    profilePath.lineTo(-profileWidth * 0.5f, displayLength)
                    profilePath.lineTo(profileWidth * 0.5f, displayLength)
                    profilePath.lineTo(profileWidth + profileWave, displayLength * 0.3f)
                    profilePath.close()
                    
                    canvas.drawPath(profilePath, leafPaint)
                }
                
                canvas.restore()
            }
        }
    }
    
    // NOUVELLE fleur géométrique réaliste 10x plus grande avec animations
    fun drawRealisticFlower(
        canvas: Canvas, 
        fleur: Fleur?, 
        time: Float
    ) {
        fleur?.let { flower ->
            if (flower.taille > 5f) {
                // Animation plus complexe
                val flowerOscillation = kotlin.math.sin(time * 0.6f) * 8f
                val flowerPulse = 1f + kotlin.math.sin(time * 1.5f) * 0.1f
                
                canvas.save()
                canvas.translate(flower.x + flowerOscillation, flower.y)
                
                val progressRatio = (flower.taille / 175f).coerceAtMost(1f)
                val petalCount = 14 // Marguerite: 12-16 pétales
                
                // CORRIGÉ : Tailles augmentées de 30% pour les fleurs
                val sizeMultiplier = 5.2f // 4f * 1.3 = 5.2f (30% plus grande)
                val petalLength = (20f + progressRatio * 50f) * sizeMultiplier * flowerPulse
                val petalWidth = (6f + progressRatio * 12f) * sizeMultiplier * flowerPulse
                
                // Dessiner chaque pétale avec animation individuelle + perspective
                val mainFlowerIndex = petalCount / 2 // Pétale principal (face)
                
                for (i in 0 until petalCount) {
                    val baseAngle = i * 360f / petalCount
                    val petalWave = kotlin.math.sin(time * 2f + i * 0.8f) * 6f
                    val angle = baseAngle + petalWave
                    
                    canvas.save()
                    canvas.rotate(angle)
                    
                    // NOUVEAU : Perspective pour certains pétales
                    val isMainPetal = (i == mainFlowerIndex || i == mainFlowerIndex + 1)
                    val perspectiveAngle = if (isMainPetal) 0f else (i - mainFlowerIndex) * 15f
                    val perspectiveFactor = kotlin.math.abs(kotlin.math.cos(Math.toRadians(perspectiveAngle.toDouble()))).toFloat()
                    val finalPerspectiveFactor = perspectiveFactor.coerceAtLeast(0.3f) // Pas trop écrasé
                    
                    // Animation d'ouverture plus rapide des pétales
                    val openingFactor = kotlin.math.min(1f, progressRatio * 2.5f)
                    val currentPetalLength = petalLength * openingFactor
                    val currentPetalWidth = petalWidth * openingFactor * finalPerspectiveFactor * 1.3f // Pétales plus larges
                    
                    // Pétale en forme de goutte animé
                    val petalPaint = Paint().apply {
                        isAntiAlias = true
                        style = Paint.Style.FILL
                    }
                    
                    // MARGUERITE : BLANC FIXE seulement
                    val brightness = 0.9f + finalPerspectiveFactor * 0.1f // Légère variation selon perspective
                    val whiteValue = (255 * brightness).toInt().coerceIn(240, 255)
                    petalPaint.color = Color.rgb(whiteValue, whiteValue, whiteValue)
                    
                    // MARGUERITE : Forme de pétale plus large et moins pointue
                    val organicVariation = kotlin.math.sin(time + i * 0.7f) * 0.03f
                    val petalPath = Path()
                    petalPath.moveTo(0f, 0f)
                    petalPath.cubicTo(
                        -currentPetalWidth * (0.5f + organicVariation), currentPetalLength * 0.3f, // Plus large (0.4f → 0.5f)
                        -currentPetalWidth * (0.35f - organicVariation), currentPetalLength * 0.8f, // Moins pointu (0.25f → 0.35f, 0.7f → 0.8f)
                        -currentPetalWidth * 0.15f, currentPetalLength * 0.98f // Bout moins pointu (0.1f → 0.15f, 0.95f → 0.98f)
                    )
                    petalPath.cubicTo(
                        0f, currentPetalLength,
                        0f, currentPetalLength,
                        0f, currentPetalLength
                    )
                    petalPath.cubicTo(
                        currentPetalWidth * 0.15f, currentPetalLength * 0.98f,
                        currentPetalWidth * (0.35f - organicVariation), currentPetalLength * 0.8f, 
                        currentPetalWidth * (0.5f + organicVariation), currentPetalLength * 0.3f
                    )
                    petalPath.cubicTo(
                        currentPetalWidth * 0.3f, currentPetalLength * 0.15f, // Moins pointu (0.2f → 0.3f)
                        0f, 0f,
                        0f, 0f
                    )
                    
                    canvas.drawPath(petalPath, petalPaint)
                    
                    // MARGUERITE : Nervure centrale très fine (seulement sur pétales de face)
                    if (isMainPetal) {
                        val nervurePaint = Paint().apply {
                            color = Color.rgb(245, 245, 240) // Blanc cassé très discret
                            strokeWidth = 1f + sizeMultiplier * 0.05f
                            style = Paint.Style.STROKE
                            isAntiAlias = true
                        }
                        canvas.drawLine(0f, 0f, 0f, currentPetalLength, nervurePaint)
                    }
                    
                    canvas.restore()
                }
                
                // CORRIGÉ : Centre proportionnel aux pétales
                val centerPaint = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.FILL
                }
                
                // Centre plus petit et proportionnel
                val centerSize = petalLength * 0.12f * flowerPulse // Beaucoup plus petit
                
                // MARGUERITE : Centre jaune vif caractéristique
                val centerColorShift = kotlin.math.sin(time * 1.2f) * 0.05f
                centerPaint.color = Color.rgb(
                    (255 * (0.95f + centerColorShift)).toInt(),
                    (230 * (0.95f + centerColorShift)).toInt(),
                    (0 * (0.1f + centerColorShift)).toInt()
                )
                canvas.drawCircle(0f, 0f, centerSize, centerPaint)
                
                // Petites étamines plus proportionnelles
                centerPaint.color = 0xFFFFA500.toInt()
                val stamenCount = 8
                val stamenRadius = centerSize * 0.6f // Proportionnel au centre
                for (i in 0 until stamenCount) {
                    val stamenAngle = i * 360f / stamenCount + time * 15f
                    val stamenBob = kotlin.math.sin(time * 3f + i * 0.4f) * 1f
                    val finalStamenRadius = stamenRadius + stamenBob
                    
                    val stamenX = Math.cos(Math.toRadians(stamenAngle.toDouble())).toFloat() * finalStamenRadius
                    val stamenY = Math.sin(Math.toRadians(stamenAngle.toDouble())).toFloat() * finalStamenRadius
                    canvas.drawCircle(stamenX, stamenY, centerSize * 0.15f, centerPaint) // Proportionnel
                }
                
                // Point central plus petit
                val pistilPulse = 1f + kotlin.math.sin(time * 2.5f) * 0.2f
                centerPaint.color = 0xFFFF6347.toInt()
                canvas.drawCircle(0f, 0f, centerSize * 0.3f * pistilPulse, centerPaint) // Beaucoup plus petit
                
                canvas.restore()
            }
        }
    }
}
