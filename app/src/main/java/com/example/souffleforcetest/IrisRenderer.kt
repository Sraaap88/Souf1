package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Path
import android.graphics.PointF
import kotlin.math.*

class IrisRenderer {
    
    fun drawIris(
        canvas: Canvas,
        stemPaint: Paint,
        leafPaint: Paint,
        flowerPaint: Paint,
        stems: List<IrisStem>,
        flowers: List<IrisFlower>
    ) {
        drawStems(canvas, stemPaint, stems)
        drawLeaves(canvas, leafPaint, stems)
        drawFlowers(canvas, flowerPaint, flowers)
    }
    
    private fun drawStems(canvas: Canvas, paint: Paint, stems: List<IrisStem>) {
        paint.color = Color.rgb(40, 120, 40)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 8f
        
        for (stem in stems) {
            if (stem.segments.size >= 2) {
                val path = Path()
                path.moveTo(stem.segments[0].x, stem.segments[0].y)
                
                for (i in 1 until stem.segments.size) {
                    val current = stem.segments[i]
                    val previous = stem.segments[i - 1]
                    
                    // Courbe légère pour tiges élancées
                    val controlX = (previous.x + current.x) / 2f + sin(i * 0.3f) * 2f
                    val controlY = (previous.y + current.y) / 2f
                    
                    path.quadTo(controlX, controlY, current.x, current.y)
                }
                
                canvas.drawPath(path, paint)
            }
        }
    }
    
    private fun drawLeaves(canvas: Canvas, paint: Paint, stems: List<IrisStem>) {
        paint.color = Color.rgb(60, 140, 60)
        paint.style = Paint.Style.FILL
        
        for (stem in stems) {
            for (leaf in stem.leaves) {
                if (leaf.growthProgress > 0f) {
                    drawSwordLeaf(canvas, paint, leaf)
                }
            }
        }
    }
    
    private fun drawSwordLeaf(canvas: Canvas, paint: Paint, leaf: IrisLeaf) {
        canvas.save()
        canvas.translate(leaf.attachmentPoint.x, leaf.attachmentPoint.y)
        canvas.rotate(leaf.angle)
        
        val currentLength = leaf.length * leaf.growthProgress
        val currentWidth = leaf.width * leaf.growthProgress
        
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
        paint.color = Color.rgb(60, 140, 60)
        canvas.drawPath(path, paint)
        
        // Nervure centrale
        paint.color = Color.rgb(40, 100, 40)
        paint.strokeWidth = 2f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(0f, 0f, 0f, -currentLength, paint)
        
        paint.style = Paint.Style.FILL
        canvas.restore()
    }
    
    private fun drawFlowers(canvas: Canvas, paint: Paint, flowers: List<IrisFlower>) {
        // Trier par couches : arrière-plan d'abord, puis premier plan
        val backgroundFlowers = flowers.filter { it.renderLayer < 30 && it.bloomProgress > 0f }
        val foregroundFlowers = flowers.filter { it.renderLayer >= 30 && it.bloomProgress > 0f }
        
        // Dessiner arrière-plan avec transparence
        for (flower in backgroundFlowers) {
            paint.alpha = 120 // Semi-transparent
            drawIrisFlower(canvas, paint, flower)
        }
        
        // Dessiner premier plan normalement
        paint.alpha = 255
        for (flower in foregroundFlowers) {
            drawIrisFlower(canvas, paint, flower)
        }
    }
    
    private fun drawIrisFlower(canvas: Canvas, paint: Paint, flower: IrisFlower) {
        canvas.save()
        canvas.translate(flower.position.x, flower.position.y)
        
        val baseSize = 88.5f * flower.bloomProgress
        val size = baseSize * flower.sizeMultiplier
        
        // Couleurs
        val upperPetalColor = Color.rgb(138, 43, 226)  // Violet
        val lowerPetalColor = Color.rgb(75, 0, 130)    // Indigo plus foncé
        val lowerPetalLight = Color.rgb(100, 149, 237) // Bleu clair
        val veiningColor = Color.rgb(40, 20, 80)       // Violet foncé
        val beardColor = Color.rgb(255, 215, 0)        // Jaune doré
        val centralPetalColor = Color.rgb(138, 43, 226) // Violet pour pétale central
        val yellowVeinColor = Color.rgb(255, 215, 0)   // Jaune pour nervures
        
        // NOUVEAU: Pétale central violet avec nervures jaunes (VERS LE HAUT)
        paint.style = Paint.Style.FILL
        paint.color = centralPetalColor
        drawCentralPetalUp(canvas, paint, size)
        
        // Nervures jaunes sur pétale central
        paint.color = yellowVeinColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        drawCentralPetalVeinsUp(canvas, paint, size)
        
        // CORRIGÉ: 1 pétale court vers le HAUT, 2 pétales longs vers le BAS
        for (i in 0..2) {
            val angle = i * 120f
            canvas.save()
            canvas.rotate(angle)
            
            if (i == 0) {
                // Pétale supérieur - court vers le haut (PLUS GROS)
                paint.style = Paint.Style.FILL
                paint.color = upperPetalColor
                drawShortPetalUp(canvas, paint, size)
                
                // Veines courtes
                paint.color = veiningColor
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1.5f
                drawShortPetalVeinsUp(canvas, paint, size)
            } else {
                // Pétales inférieurs - longs vers le bas (CORRIGÉ)
                canvas.translate(0f, size * 0.1f)
                paint.style = Paint.Style.FILL
                paint.color = lowerPetalLight
                drawLongPetalDown(canvas, paint, size)
                
                // Bordure
                paint.color = lowerPetalColor
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3f
                drawLongPetalDownOutline(canvas, paint, size)
                
                // BARBE
                paint.style = Paint.Style.FILL
                paint.color = beardColor
                drawIrisBeard(canvas, paint, size * 0.8f)
                
                // Veines longues
                paint.color = veiningColor
                paint.strokeWidth = 1.2f
                drawLongPetalVeinsDown(canvas, paint, size)
            }
            
            canvas.restore()
        }
        
        // Centre
        drawIrisCenter(canvas, paint, size)
        
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
    
    // Pétale court vers le HAUT (PLUS GROS - 30% plus large)
    private fun drawShortPetalUp(canvas: Canvas, paint: Paint, size: Float) {
        val path = Path()
        path.moveTo(0f, 0f)
        // 30% plus large (0.25f → 0.325f, 0.2f → 0.26f)
        path.quadTo(-size * 0.325f, -size * 0.05f, -size * 0.26f, -size * 0.2f)
        path.quadTo(0f, -size * 0.25f, size * 0.26f, -size * 0.2f)
        path.quadTo(size * 0.325f, -size * 0.05f, 0f, 0f)
        canvas.drawPath(path, paint)
    }
    
    private fun drawShortPetalVeinsUp(canvas: Canvas, paint: Paint, size: Float) {
        // Plus de veines car pétale plus gros
        for (i in -1..1) {
            val startX = size * 0.12f * i // Élargi pour correspondre
            val endX = size * 0.1f * i
            val endY = -size * 0.15f
            canvas.drawLine(startX, 0f, endX, endY, paint)
        }
    }
    
    // CORRIGÉ: Pétales longs vers le BAS (au lieu du haut)
    private fun drawLongPetalDown(canvas: Canvas, paint: Paint, size: Float) {
        val path = Path()
        path.moveTo(0f, 0f)
        
        // INVERSER TOUTES les coordonnées Y : + devient -, - devient +
        path.quadTo(-size * 0.28f, size * 0.1f, -size * 0.245f, size * 0.4f)
        path.quadTo(-size * 0.175f, size * 0.84f, -size * 0.105f, size * 1.08f)
        
        // Pointe plus fine et chétive vers le bas
        path.quadTo(-size * 0.02f, size * 1.32f, 0f, size * 1.38f) // Pointe très fine
        path.quadTo(size * 0.02f, size * 1.32f, size * 0.105f, size * 1.08f)
        
        path.quadTo(size * 0.175f, size * 0.84f, size * 0.245f, size * 0.4f)
        path.quadTo(size * 0.28f, size * 0.1f, 0f, 0f)
        canvas.drawPath(path, paint)
    }
    
    private fun drawLongPetalDownOutline(canvas: Canvas, paint: Paint, size: Float) {
        val path = Path()
        path.moveTo(0f, 0f)
        
        // Même forme mais pour le contour
        path.quadTo(-size * 0.28f, size * 0.1f, -size * 0.245f, size * 0.4f)
        path.quadTo(-size * 0.175f, size * 0.84f, -size * 0.105f, size * 1.08f)
        path.quadTo(-size * 0.02f, size * 1.32f, 0f, size * 1.38f)
        path.quadTo(size * 0.02f, size * 1.32f, size * 0.105f, size * 1.08f)
        path.quadTo(size * 0.175f, size * 0.84f, size * 0.245f, size * 0.4f)
        path.quadTo(size * 0.28f, size * 0.1f, 0f, 0f)
        canvas.drawPath(path, paint)
    }
    
    private fun drawLongPetalVeinsDown(canvas: Canvas, paint: Paint, size: Float) {
        // Veines adaptées à la nouvelle forme plus longue
        for (i in -1..1) { // Moins de veines car plus mince
            val startX = size * 0.03f * i // Plus près du centre
            val endX = size * 0.06f * i
            val endY = size * 1.0f // Plus longues
            canvas.drawLine(startX, 0f, endX, endY, paint)
        }
    }
    
    private fun drawIrisBeard(canvas: Canvas, paint: Paint, size: Float) {
        // La barbe caractéristique de l'iris - ligne duveteuse au centre
        val beardPath = Path()
        beardPath.moveTo(0f, size * 0.1f)
        beardPath.lineTo(0f, size * 0.4f)
        
        // Effet duveteux avec petits traits perpendiculaires
        paint.strokeWidth = 6f
        paint.strokeCap = Paint.Cap.ROUND
        canvas.drawPath(beardPath, paint)
        
        // Petits poils de barbe
        paint.strokeWidth = 2f
        for (i in 1..8) {
            val y = size * 0.1f + (i * size * 0.04f)
            val offset = size * 0.02f
            canvas.drawLine(-offset, y, offset, y, paint)
        }
    }
    
    private fun drawIrisCenter(canvas: Canvas, paint: Paint, size: Float) {
        // Centre complexe avec style et stigmates
        
        // Base du centre - jaune doré
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(255, 215, 0)
        canvas.drawCircle(0f, 0f, size * 0.12f, paint)
        
        // Stigmates (3 parties reproductrices)
        paint.color = Color.rgb(200, 160, 0)
        for (i in 0..2) {
            canvas.save()
            canvas.rotate(i * 120f)
            
            // Forme de stigmate
            val stigmaPath = Path()
            stigmaPath.moveTo(0f, -size * 0.08f)
            stigmaPath.quadTo(size * 0.04f, -size * 0.06f, size * 0.03f, 0f)
            stigmaPath.quadTo(size * 0.04f, size * 0.06f, 0f, size * 0.08f)
            stigmaPath.quadTo(-size * 0.04f, size * 0.06f, -size * 0.03f, 0f)
            stigmaPath.quadTo(-size * 0.04f, -size * 0.06f, 0f, -size * 0.08f)
            
            canvas.drawPath(stigmaPath, paint)
            canvas.restore()
        }
        
        // Point central plus foncé
        paint.color = Color.rgb(150, 100, 0)
        canvas.drawCircle(0f, 0f, size * 0.04f, paint)
    }
}
