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
        for (flower in flowers) {
            if (flower.bloomProgress > 0f) {
                drawIrisFlower(canvas, paint, flower)
            }
        }
    }
    
    private fun drawIrisFlower(canvas: Canvas, paint: Paint, flower: IrisFlower) {
        canvas.save()
        canvas.translate(flower.position.x, flower.position.y)
        
        val size = 88.5f * flower.bloomProgress  // 15% plus petit (208f → 177f) donc 50% plus petit 88.5f
        
        // Couleurs dégradées pour plus de réalisme
        val upperPetalColor = Color.rgb(138, 43, 226)  // Violet
        val lowerPetalColor = Color.rgb(75, 0, 130)    // Indigo plus foncé
        val lowerPetalLight = Color.rgb(100, 149, 237) // Bleu clair pour centre
        val veiningColor = Color.rgb(40, 20, 80)       // Violet très foncé pour veines
        val beardColor = Color.rgb(255, 215, 0)        // Jaune doré pour barbes
        
        // Dessiner les 3 pétales caractéristiques de l'iris
        for (i in 0..2) {
            val angle = i * 120f // 3 pétales à 120° chacun
            canvas.save()
            canvas.rotate(angle)
            
            // Pétale supérieur (étendard) avec dégradé
            paint.style = Paint.Style.FILL
            paint.color = upperPetalColor
            drawDetailedIrisPetal(canvas, paint, size, true)
            
            // Veines sur pétale supérieur
            paint.color = veiningColor
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.5f
            drawPetalVeins(canvas, paint, size, true)
            
            // Pétale inférieur (chute) - base plus claire
            canvas.translate(0f, size * 0.3f)
            paint.style = Paint.Style.FILL
            paint.color = lowerPetalLight
            drawDetailedIrisPetal(canvas, paint, size * 0.8f, false)
            
            // Bordure plus foncée sur pétale inférieur
            paint.color = lowerPetalColor
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f
            drawDetailedIrisPetalOutline(canvas, paint, size * 0.8f, false)
            
            // BARBE caractéristique de l'iris sur pétale inférieur
            paint.style = Paint.Style.FILL
            paint.color = beardColor
            drawIrisBeard(canvas, paint, size * 0.8f)
            
            // Veines sur pétale inférieur
            paint.color = veiningColor
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.2f
            drawPetalVeins(canvas, paint, size * 0.8f, false)
            
            canvas.restore()
        }
        
        // Centre détaillé de la fleur
        drawIrisCenter(canvas, paint, size)
        
        // Contours finaux pour définir la forme
        paint.color = Color.rgb(50, 0, 80)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        
        for (i in 0..2) {
            val angle = i * 120f
            canvas.save()
            canvas.rotate(angle)
            
            drawDetailedIrisPetalOutline(canvas, paint, size, true)
            canvas.translate(0f, size * 0.3f)
            drawDetailedIrisPetalOutline(canvas, paint, size * 0.8f, false)
            
            canvas.restore()
        }
        
        paint.style = Paint.Style.FILL
        canvas.restore()
    }
    
    private fun drawDetailedIrisPetal(canvas: Canvas, paint: Paint, size: Float, isUpper: Boolean) {
        val path = Path()
        
        if (isUpper) {
            // Pétale supérieur - forme plus élégante
            path.moveTo(0f, 0f)
            path.quadTo(-size * 0.35f, -size * 0.15f, -size * 0.25f, -size * 0.5f)
            path.quadTo(-size * 0.15f, -size * 0.75f, 0f, -size * 0.85f)
            path.quadTo(size * 0.15f, -size * 0.75f, size * 0.25f, -size * 0.5f)
            path.quadTo(size * 0.35f, -size * 0.15f, 0f, 0f)
        } else {
            // Pétale inférieur - plus arrondi et charnu
            path.moveTo(0f, 0f)
            path.quadTo(-size * 0.45f, size * 0.1f, -size * 0.35f, size * 0.35f)
            path.quadTo(-size * 0.2f, size * 0.55f, 0f, size * 0.6f)
            path.quadTo(size * 0.2f, size * 0.55f, size * 0.35f, size * 0.35f)
            path.quadTo(size * 0.45f, size * 0.1f, 0f, 0f)
        }
        
        canvas.drawPath(path, paint)
    }
    
    private fun drawDetailedIrisPetalOutline(canvas: Canvas, paint: Paint, size: Float, isUpper: Boolean) {
        val path = Path()
        
        if (isUpper) {
            path.moveTo(0f, 0f)
            path.quadTo(-size * 0.35f, -size * 0.15f, -size * 0.25f, -size * 0.5f)
            path.quadTo(-size * 0.15f, -size * 0.75f, 0f, -size * 0.85f)
            path.quadTo(size * 0.15f, -size * 0.75f, size * 0.25f, -size * 0.5f)
            path.quadTo(size * 0.35f, -size * 0.15f, 0f, 0f)
        } else {
            path.moveTo(0f, 0f)
            path.quadTo(-size * 0.45f, size * 0.1f, -size * 0.35f, size * 0.35f)
            path.quadTo(-size * 0.2f, size * 0.55f, 0f, size * 0.6f)
            path.quadTo(size * 0.2f, size * 0.55f, size * 0.35f, size * 0.35f)
            path.quadTo(size * 0.45f, size * 0.1f, 0f, 0f)
        }
        
        canvas.drawPath(path, paint)
    }
    
    private fun drawPetalVeins(canvas: Canvas, paint: Paint, size: Float, isUpper: Boolean) {
        if (isUpper) {
            // Veines sur pétale supérieur - en éventail
            for (i in -2..2) {
                val startX = size * 0.1f * i
                val endX = size * 0.15f * i
                val endY = -size * 0.7f
                canvas.drawLine(startX, 0f, endX, endY, paint)
            }
        } else {
            // Veines sur pétale inférieur - rayonnantes
            for (i in -2..2) {
                val startX = size * 0.05f * i
                val endX = size * 0.2f * i
                val endY = size * 0.4f
                canvas.drawLine(startX, 0f, endX, endY, paint)
            }
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
    
    private fun drawIrisPetal(canvas: Canvas, paint: Paint, size: Float, isUpper: Boolean) {
        val path = Path()
        
        if (isUpper) {
            // Pétale supérieur - plus droit et élancé
            path.moveTo(0f, 0f)
            path.quadTo(-size * 0.3f, -size * 0.2f, -size * 0.2f, -size * 0.6f)
            path.quadTo(0f, -size * 0.8f, size * 0.2f, -size * 0.6f)
            path.quadTo(size * 0.3f, -size * 0.2f, 0f, 0f)
        } else {
            // Pétale inférieur - plus arrondi et tombant
            path.moveTo(0f, 0f)
            path.quadTo(-size * 0.4f, size * 0.1f, -size * 0.3f, size * 0.4f)
            path.quadTo(0f, size * 0.6f, size * 0.3f, size * 0.4f)
            path.quadTo(size * 0.4f, size * 0.1f, 0f, 0f)
        }
        
        canvas.drawPath(path, paint)
    }
    
    private fun drawIrisPetalOutline(canvas: Canvas, paint: Paint, size: Float, isUpper: Boolean) {
        val path = Path()
        
        if (isUpper) {
            path.moveTo(0f, 0f)
            path.quadTo(-size * 0.3f, -size * 0.2f, -size * 0.2f, -size * 0.6f)
            path.quadTo(0f, -size * 0.8f, size * 0.2f, -size * 0.6f)
            path.quadTo(size * 0.3f, -size * 0.2f, 0f, 0f)
        } else {
            path.moveTo(0f, 0f)
            path.quadTo(-size * 0.4f, size * 0.1f, -size * 0.3f, size * 0.4f)
            path.quadTo(0f, size * 0.6f, size * 0.3f, size * 0.4f)
            path.quadTo(size * 0.4f, size * 0.1f, 0f, 0f)
        }
        
        canvas.drawPath(path, paint)
    }
}
