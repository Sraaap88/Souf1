package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Path
import android.graphics.PointF
import kotlin.math.*

class IrisRenderer {
    
    // Délégué pour le dessin des fleurs
    private val flowerDrawer = IrisFlowerDrawer()
    
    fun drawIris(
        canvas: Canvas,
        stemPaint: Paint,
        leafPaint: Paint,
        flowerPaint: Paint,
        stems: List<IrisStem>,
        flowers: List<IrisFlower>,
        dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null
    ) {
        drawStems(canvas, stemPaint, stems, dissolveInfo)
        drawLeaves(canvas, leafPaint, stems, dissolveInfo)
        drawFlowers(canvas, flowerPaint, flowers, dissolveInfo)
    }
    
    private fun drawStems(canvas: Canvas, paint: Paint, stems: List<IrisStem>, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        paint.color = Color.rgb(40, 120, 40)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 8f
        
        // NOUVEAU: Appliquer les effets de dissolution
        if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            // Réduire l'opacité en fonction de la dissolution
            val alpha = ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
            paint.alpha = alpha
            
            // Si les tiges s'effondrent, réduire la largeur
            if (dissolveInfo.stemsCollapsing) {
                paint.strokeWidth = 8f * (1f - dissolveInfo.progress * 0.5f)
            }
        } else {
            paint.alpha = 255
        }
        
        for (stem in stems) {
            if (stem.segments.size >= 2) {
                val path = Path()
                path.moveTo(stem.segments[0].x, stem.segments[0].y)
                
                for (i in 1 until stem.segments.size) {
                    val current = stem.segments[i]
                    val previous = stem.segments[i - 1]
                    
                    // Courbe légère pour tiges élancées
                    var controlX = (previous.x + current.x) / 2f + sin(i * 0.3f) * 2f
                    var controlY = (previous.y + current.y) / 2f
                    
                    // NOUVEAU: Effet de dissolution - les tiges penchent
                    if (dissolveInfo?.stemsCollapsing == true) {
                        val bendFactor = dissolveInfo.progress * 20f
                        controlX += bendFactor * (i.toFloat() / stem.segments.size)
                        controlY += bendFactor * 0.5f
                    }
                    
                    path.quadTo(controlX, controlY, current.x, current.y)
                }
                
                canvas.drawPath(path, paint)
            }
        }
    }
    
    private fun drawLeaves(canvas: Canvas, paint: Paint, stems: List<IrisStem>, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        paint.color = Color.rgb(60, 140, 60)
        paint.style = Paint.Style.FILL
        
        // NOUVEAU: Appliquer les effets de dissolution aux feuilles
        if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            val alpha = ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
            paint.alpha = alpha
            
            // Si les feuilles se ratatinent, changer la couleur vers le brun
            if (dissolveInfo.leavesShriveling) {
                val shrivelingFactor = dissolveInfo.progress
                val red = (60 + (139 - 60) * shrivelingFactor).toInt() // Vers brun
                val green = (140 * (1f - shrivelingFactor * 0.7f)).toInt()
                val blue = (60 * (1f - shrivelingFactor * 0.8f)).toInt()
                paint.color = Color.rgb(red, green, blue)
            }
        } else {
            paint.alpha = 255
        }
        
        for (stem in stems) {
            for (leaf in stem.leaves) {
                if (leaf.growthProgress > 0f) {
                    drawSwordLeaf(canvas, paint, leaf, dissolveInfo)
                }
            }
        }
    }
    
    private fun drawSwordLeaf(canvas: Canvas, paint: Paint, leaf: IrisLeaf, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        canvas.save()
        canvas.translate(leaf.attachmentPoint.x, leaf.attachmentPoint.y)
        canvas.rotate(leaf.angle)
        
        var currentLength = leaf.length * leaf.growthProgress
        var currentWidth = leaf.width * leaf.growthProgress
        
        // NOUVEAU: Réduire la taille si les feuilles se ratatinent
        if (dissolveInfo?.leavesShriveling == true) {
            val shrinkFactor = 1f - dissolveInfo.progress * 0.6f
            currentLength *= shrinkFactor
            currentWidth *= shrinkFactor
        }
        
        // Feuille en forme d'épée - longue et étroite
        val path = Path()
        
        // Base de la feuille
        path.moveTo(-currentWidth / 2f, 0f)
        path.lineTo(currentWidth / 2f, 0f)
        
        // Côtés de la feuille - convergeant vers la pointe
        path.lineTo(currentWidth * 0.3f, -currentLength * 0.7f)
        path.lineTo(0f, -currentLength) // Pointe effilée
        path.lineTo(-currentWidth * 0.3f, -currentLength * 0.7f)
        
        path.close()
        
        // Dessiner la feuille
        canvas.drawPath(path, paint)
        
        // Nervure centrale (seulement si pas trop dissoute)
        if (dissolveInfo == null || dissolveInfo.progress < 0.7f) {
            val originalColor = paint.color
            paint.color = Color.rgb(40, 100, 40)
            paint.strokeWidth = 2f
            paint.style = Paint.Style.STROKE
            
            // NOUVEAU: Nervure qui s'affaiblit
            if (dissolveInfo != null && dissolveInfo.progress > 0f) {
                val alpha = ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
                paint.alpha = alpha
            }
            
            canvas.drawLine(0f, 0f, 0f, -currentLength, paint)
            
            paint.style = Paint.Style.FILL
            paint.color = originalColor
        }
        
        canvas.restore()
    }
    
    private fun drawFlowers(canvas: Canvas, paint: Paint, flowers: List<IrisFlower>, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        // Trier par couches : arrière-plan d'abord, puis premier plan
        val backgroundFlowers = flowers.filter { it.renderLayer < 30 && it.bloomProgress > 0f }
        val foregroundFlowers = flowers.filter { it.renderLayer >= 30 && it.bloomProgress > 0f }
        
        // NOUVEAU: Appliquer les effets de dissolution
        val baseAlpha = if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
        } else 255
        
        // Dessiner arrière-plan avec transparence
        for (flower in backgroundFlowers) {
            paint.alpha = (120 * baseAlpha / 255) // Semi-transparent + dissolution
            flowerDrawer.drawIrisFlower(canvas, paint, flower, dissolveInfo)
        }
        
        // Dessiner premier plan normalement
        paint.alpha = baseAlpha
        for (flower in foregroundFlowers) {
            flowerDrawer.drawIrisFlower(canvas, paint, flower, dissolveInfo)
        }
    }
}
