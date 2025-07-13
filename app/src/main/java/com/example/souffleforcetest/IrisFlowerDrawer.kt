package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Path
import kotlin.math.*

class IrisFlowerDrawer {
    
    fun drawIrisFlower(canvas: Canvas, paint: Paint, flower: IrisFlower, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        canvas.save()
        canvas.translate(flower.position.x, flower.position.y)
        
        val baseSize = 88.5f * flower.bloomProgress
        var size = baseSize * flower.sizeMultiplier
        
        // NOUVEAU: Réduire la taille si dissolution
        if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            size *= (1f - dissolveInfo.progress * 0.3f)
        }
        
        // Couleurs
        val upperPetalColor = Color.rgb(138, 43, 226)  // Violet
        val lowerPetalColor = Color.rgb(75, 0, 130)    // Indigo plus foncé
        val lowerPetalLight = Color.rgb(100, 149, 237) // Bleu clair
        val veiningColor = Color.rgb(40, 20, 80)       // Violet foncé
        val beardColor = Color.rgb(255, 215, 0)        // Jaune doré
        val centralPetalColor = Color.rgb(138, 43, 226) // Violet pour pétale central
        val yellowVeinColor = Color.rgb(255, 215, 0)   // Jaune pour nervures
        
        // NOUVEAU: Pétale central violet avec nervures jaunes (VERS LE HAUT)
        if (dissolveInfo?.shouldStandardsWilt != true || dissolveInfo.progress < 0.6f) {  // CORRIGÉ
            paint.style = Paint.Style.FILL
            paint.color = centralPetalColor
            drawCentralPetalUp(canvas, paint, size)
            
            // Nervures jaunes sur pétale central
            paint.color = yellowVeinColor
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1f
            drawCentralPetalVeinsUp(canvas, paint, size)
        }
        
        // STRUCTURE RÉALISTE D'IRIS : 3 pétales dressés + 3 pétales tombants
        for (i in 0..5) { // 6 pétales au total
            val angle = i * 60f // Répartis sur 360°
            canvas.save()
            canvas.rotate(angle)
            
            if (i % 2 == 0) {
                // STANDARDS : Pétales DRESSÉS (3 standards) - vers le haut
                if (dissolveInfo?.shouldStandardsWilt != true || dissolveInfo.progress < 0.5f) {  // CORRIGÉ
                    paint.style = Paint.Style.FILL
                    paint.color = upperPetalColor
                    drawStandardPetalUp(canvas, paint, size, dissolveInfo)
                    
                    // Veines des standards
                    paint.color = veiningColor
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 1.5f
                    drawStandardPetalVeins(canvas, paint, size)
                }
            } else {
                // FALLS : Pétales TOMBANTS (3 falls) - retombent vers le bas
                var shouldDrawFalls = true
                
                // NOUVEAU: Les falls tombent en premier lors de la dissolution
                if (dissolveInfo?.shouldFallsDropPetals == true && dissolveInfo.progress > 0.3f) {  // CORRIGÉ
                    shouldDrawFalls = false
                }
                
                if (shouldDrawFalls) {
                    paint.style = Paint.Style.FILL
                    paint.color = lowerPetalLight
                    drawFallPetalDown(canvas, paint, size, dissolveInfo)
                    
                    // Bordure des falls
                    paint.color = lowerPetalColor
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 3f
                    drawFallPetalOutline(canvas, paint, size)
                    
                    // BARBE sur les falls (se dissout en dernier)
                    if (dissolveInfo?.shouldBeardDissolve != true || dissolveInfo.progress < 0.8f) {  // CORRIGÉ
                        paint.style = Paint.Style.FILL
                        paint.color = beardColor
                        drawIrisBeard(canvas, paint, size * 0.8f, dissolveInfo)
                    }
                    
                    // Veines des falls
                    paint.color = veiningColor
                    paint.strokeWidth = 1.2f
                    drawFallPetalVeins(canvas, paint, size)
                }
            }
            
            canvas.restore()
        }
        
        // Centre (disparaît en dernier)
        if (dissolveInfo == null || dissolveInfo.progress < 0.9f) {
            drawIrisCenter(canvas, paint, size, dissolveInfo)
        }
        
        paint.style = Paint.Style.FILL
        canvas.restore()
    }
    
    // NOUVEAU: Pétale central violet vers le HAUT
    private fun drawCentralPetalUp(canvas: Canvas, paint: Paint, size: Float) {
        val path = Path()
        path.moveTo(0f, 0f)
        path.quadTo(-size * 0.15f, -size * 0.05f, -size * 0.1f, -size * 0.25f)
        path.quadTo(0f, -size * 0.3f, size * 0.1f, -size * 0.25f)
        path.quadTo(size * 0.15f, -size * 0.05f, 0f, 0f)
        canvas.drawPath(path, paint)
    }
    
    // NOUVEAU: Nervures jaunes sur pétale central vers le haut
    private fun drawCentralPetalVeinsUp(canvas: Canvas, paint: Paint, size: Float) {
        for (i in -1..1) {
            val startX = size * 0.03f * i
            val endX = size * 0.05f * i
            val endY = -size * 0.2f
            canvas.drawLine(startX, 0f, endX, endY, paint)
        }
    }
    
    // STANDARDS : Pétales dressés vers le haut (comme sur la photo)
    private fun drawStandardPetalUp(canvas: Canvas, paint: Paint, size: Float, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val path = Path()
        path.moveTo(0f, 0f)
        
        var petalSize = size
        // NOUVEAU: Les standards flétrissent
        if (dissolveInfo?.shouldStandardsWilt == true) {  // CORRIGÉ
            petalSize *= (1f - dissolveInfo.progress * 0.4f)
        }
        
        // Pétales élancés vers le haut
        path.quadTo(-petalSize * 0.2f, -petalSize * 0.1f, -petalSize * 0.15f, -petalSize * 0.4f)
        path.quadTo(0f, -petalSize * 0.5f, petalSize * 0.15f, -petalSize * 0.4f)
        path.quadTo(petalSize * 0.2f, -petalSize * 0.1f, 0f, 0f)
        canvas.drawPath(path, paint)
    }
    
    private fun drawStandardPetalVeins(canvas: Canvas, paint: Paint, size: Float) {
        for (i in -1..1) {
            val startX = size * 0.05f * i
            val endX = size * 0.08f * i
            val endY = -size * 0.35f
            canvas.drawLine(startX, 0f, endX, endY, paint)
        }
    }
    
    // FALLS : Pétales qui tombent vers le bas (comme sur la photo)
    private fun drawFallPetalDown(canvas: Canvas, paint: Paint, size: Float, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val path = Path()
        path.moveTo(0f, 0f)
        
        var petalSize = size
        var dropFactor = 0f
        
        // NOUVEAU: Les falls tombent et se ratatinent
        if (dissolveInfo?.shouldFallsDropPetals == true) {  // CORRIGÉ
            petalSize *= (1f - dissolveInfo.progress * 0.6f)
            dropFactor = dissolveInfo.progress * 30f // Effet de chute
        }
        
        // Forme qui TOMBE vers le bas avec courbure naturelle + effet de chute
        path.quadTo(-petalSize * 0.25f, petalSize * 0.05f + dropFactor, -petalSize * 0.3f, petalSize * 0.2f + dropFactor)
        path.quadTo(-petalSize * 0.35f, petalSize * 0.4f + dropFactor, -petalSize * 0.3f, petalSize * 0.6f + dropFactor)
        path.quadTo(-petalSize * 0.2f, petalSize * 0.8f + dropFactor, -petalSize * 0.1f, petalSize * 0.9f + dropFactor)
        
        // Pointe qui retombe + effet de chute
        path.quadTo(0f, petalSize * 1.0f + dropFactor, petalSize * 0.1f, petalSize * 0.9f + dropFactor)
        path.quadTo(petalSize * 0.2f, petalSize * 0.8f + dropFactor, petalSize * 0.3f, petalSize * 0.6f + dropFactor)
        path.quadTo(petalSize * 0.35f, petalSize * 0.4f + dropFactor, petalSize * 0.3f, petalSize * 0.2f + dropFactor)
        path.quadTo(petalSize * 0.25f, petalSize * 0.05f + dropFactor, 0f, 0f)
        
        canvas.drawPath(path, paint)
    }
    
    private fun drawFallPetalOutline(canvas: Canvas, paint: Paint, size: Float) {
        val path = Path()
        path.moveTo(0f, 0f)
        
        // Même forme pour le contour
        path.quadTo(-size * 0.25f, size * 0.05f, -size * 0.3f, size * 0.2f)
        path.quadTo(-size * 0.35f, size * 0.4f, -size * 0.3f, size * 0.6f)
        path.quadTo(-size * 0.2f, size * 0.8f, -size * 0.1f, size * 0.9f)
        path.quadTo(0f, size * 1.0f, size * 0.1f, size * 0.9f)
        path.quadTo(size * 0.2f, size * 0.8f, size * 0.3f, size * 0.6f)
        path.quadTo(size * 0.35f, size * 0.4f, size * 0.3f, size * 0.2f)
        path.quadTo(size * 0.25f, size * 0.05f, 0f, 0f)
        
        canvas.drawPath(path, paint)
    }
    
    private fun drawFallPetalVeins(canvas: Canvas, paint: Paint, size: Float) {
        // Veines qui suivent la courbure du fall
        for (i in -1..1) {
            val startX = size * 0.08f * i
            val midX = size * 0.15f * i
            val endX = size * 0.05f * i
            
            val path = Path()
            path.moveTo(startX, size * 0.1f)
            path.quadTo(midX, size * 0.5f, endX, size * 0.8f)
            canvas.drawPath(path, paint)
        }
    }
    
    private fun drawIrisBeard(canvas: Canvas, paint: Paint, size: Float, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        var beardSize = size
        
        // NOUVEAU: La barbe se dissout
        if (dissolveInfo?.shouldBeardDissolve == true) {  // CORRIGÉ
            beardSize *= (1f - dissolveInfo.progress * 0.8f)
            // Changer la couleur vers une teinte fanée
            val fadeFactor = dissolveInfo.progress
            val red = (255 * (1f - fadeFactor * 0.3f)).toInt()
            val green = (215 * (1f - fadeFactor * 0.5f)).toInt()
            val blue = (0 + (100 * fadeFactor)).toInt() // Légèrement bleuté
            paint.color = Color.rgb(red, green, blue)
        }
        
        // Barbe au centre du fall qui retombe
        val beardPath = Path()
        beardPath.moveTo(0f, beardSize * 0.2f)
        beardPath.lineTo(0f, beardSize * 0.5f)
        
        paint.strokeWidth = 6f
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawPath(beardPath, paint)
        
        // Petits poils duveteux (disparaissent progressivement)
        paint.strokeWidth = 2f
        val poilCount = if (dissolveInfo?.shouldBeardDissolve == true) {  // CORRIGÉ
            (6 * (1f - dissolveInfo.progress * 0.7f)).toInt().coerceAtLeast(1)
        } else 6
        
        for (i in 1..poilCount) {
            val y = beardSize * 0.2f + (i * beardSize * 0.05f)
            val offset = beardSize * 0.015f
            canvas.drawLine(-offset, y, offset, y, paint)
        }
    }
    
    private fun drawIrisCenter(canvas: Canvas, paint: Paint, size: Float, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        // Centre complexe avec style et stigmates
        
        var centerSize = size
        // NOUVEAU: Le centre rétrécit lors de la dissolution
        if (dissolveInfo != null && dissolveInfo.progress > 0.7f) {
            centerSize *= (1f - (dissolveInfo.progress - 0.7f) * 2f).coerceAtLeast(0.3f)
        }
        
        // Base du centre - jaune doré
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(255, 215, 0)
        canvas.drawCircle(0f, 0f, centerSize * 0.12f, paint)
        
        // Stigmates (3 parties reproductrices)
        paint.color = Color.rgb(200, 160, 0)
        for (i in 0..2) {
            canvas.save()
            canvas.rotate(i * 120f)
            
            // Forme de stigmate (plus petite si dissolution)
            val stigmaPath = Path()
            stigmaPath.moveTo(0f, -centerSize * 0.08f)
            stigmaPath.quadTo(centerSize * 0.04f, -centerSize * 0.06f, centerSize * 0.03f, 0f)
            stigmaPath.quadTo(centerSize * 0.04f, centerSize * 0.06f, 0f, centerSize * 0.08f)
            stigmaPath.quadTo(-centerSize * 0.04f, centerSize * 0.06f, -centerSize * 0.03f, 0f)
            stigmaPath.quadTo(-centerSize * 0.04f, -centerSize * 0.06f, 0f, -centerSize * 0.08f)
            
            canvas.drawPath(stigmaPath, paint)
            canvas.restore()
        }
        
        // Point central plus foncé (disparaît en dernier)
        if (dissolveInfo == null || dissolveInfo.progress < 0.95f) {
            paint.color = Color.rgb(150, 100, 0)
            canvas.drawCircle(0f, 0f, centerSize * 0.04f, paint)
        }
    }
}
