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
        
        val size = 160f * flower.bloomProgress  // 4x plus gros (40f -> 160f)
        
        // Couleurs typiques de l'iris - violet/bleu
        val petalColors = listOf(
            Color.rgb(138, 43, 226),  // Violet
            Color.rgb(75, 0, 130),    // Indigo
            Color.rgb(100, 149, 237)  // Bleu clair
        )
        
        // Dessiner les 3 pétales caractéristiques de l'iris
        for (i in 0..2) {
            val angle = i * 120f // 3 pétales à 120° chacun
            canvas.save()
            canvas.rotate(angle)
            
            // Pétale supérieur (étendard)
            paint.color = petalColors[0]
            paint.style = Paint.Style.FILL
            drawIrisPetal(canvas, paint, size, true)
            
            // Pétale inférieur (chute)
            canvas.translate(0f, size * 0.3f)
            paint.color = petalColors[1]
            drawIrisPetal(canvas, paint, size * 0.8f, false)
            
            canvas.restore()
        }
        
        // Centre de la fleur plus gros aussi
        paint.color = Color.rgb(255, 215, 0) // Jaune doré
        canvas.drawCircle(0f, 0f, size * 0.15f, paint)
        
        // Contours pour définir les pétales
        paint.color = Color.rgb(50, 0, 80)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f  // Trait plus épais pour fleur plus grosse
        
        for (i in 0..2) {
            val angle = i * 120f
            canvas.save()
            canvas.rotate(angle)
            
            drawIrisPetalOutline(canvas, paint, size, true)
            canvas.translate(0f, size * 0.3f)
            drawIrisPetalOutline(canvas, paint, size * 0.8f, false)
            
            canvas.restore()
        }
        
        paint.style = Paint.Style.FILL
        canvas.restore()
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
