package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Path
import kotlin.math.*

class OrchideeFlowerDrawer {
    
    fun drawOrchideeFlower(canvas: Canvas, paint: Paint, flower: OrchideeFlower, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        canvas.save()
        canvas.translate(flower.position.x, flower.position.y)
        
        val baseSize = 92f * flower.bloomProgress
        var size = baseSize * flower.sizeMultiplier
        
        // Réduire la taille si dissolution
        if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            size *= (1f - dissolveInfo.progress * 0.4f)
        }
        
        // Dessiner selon l'espèce
        when (flower.genetics.species) {
            OrchideeSpecies.PHALAENOPSIS -> drawPhalaenopsis(canvas, paint, flower, size, dissolveInfo)
            OrchideeSpecies.CATTLEYA -> drawCattleya(canvas, paint, flower, size, dissolveInfo)
            OrchideeSpecies.DENDROBIUM -> drawDendrobium(canvas, paint, flower, size, dissolveInfo)
            OrchideeSpecies.VANDA -> drawVanda(canvas, paint, flower, size, dissolveInfo)
            OrchideeSpecies.ONCIDIUM -> drawOncidium(canvas, paint, flower, size, dissolveInfo)
            OrchideeSpecies.CYMBIDIUM -> drawCymbidium(canvas, paint, flower, size, dissolveInfo)
        }
        
        canvas.restore()
    }
    
    // ==================== PHALAENOPSIS - ORCHIDÉE PAPILLON ====================
    
    private fun drawPhalaenopsis(canvas: Canvas, paint: Paint, flower: OrchideeFlower, size: Float, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val genetics = flower.genetics
        val shapeVar = genetics.shapeVariation
        
        // Couleurs selon la génétique
        val primaryColor = genetics.colorPalette.primary
        val secondaryColor = genetics.colorPalette.secondary
        val accentColor = genetics.colorPalette.accent
        val throatColor = genetics.colorPalette.throat
        val veiningColor = genetics.colorPalette.veining
        
        // 5 pétales : 2 sépales latéraux + 2 pétales + 1 labelle complexe
        
        // Sépales latéraux (arrière-plan)
        for (i in 0..1) {
            val angle = if (i == 0) -45f else 45f
            canvas.save()
            canvas.rotate(angle)
            
            if (dissolveInfo?.flowersPetalsWilting != true || dissolveInfo.progress < 0.4f) {
                paint.style = Paint.Style.FILL
                paint.color = secondaryColor
                drawPhalaenopsisSepal(canvas, paint, size, shapeVar, dissolveInfo)
            }
            
            canvas.restore()
        }
        
        // Pétales principaux (forme papillon)
        for (i in 0..1) {
            val angle = if (i == 0) -25f else 25f
            canvas.save()
            canvas.rotate(angle)
            
            if (dissolveInfo?.flowersPetalsWilting != true || dissolveInfo.progress < 0.6f) {
                paint.style = Paint.Style.FILL
                paint.color = primaryColor
                drawPhalaenopsisPetal(canvas, paint, size, shapeVar, dissolveInfo)
                
                // Nervures
                paint.color = veiningColor
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1.5f
                drawPhalaenopsisPetalVeins(canvas, paint, size, shapeVar)
            }
            
            canvas.restore()
        }
        
        // Labelle (partie centrale complexe) - disparaît en dernier
        if (dissolveInfo?.flowersPetalsWilting != true || dissolveInfo.progress < 0.8f) {
            paint.style = Paint.Style.FILL
            paint.color = throatColor
            drawPhalaenopsisLabelle(canvas, paint, size, shapeVar, dissolveInfo)
            
            // Motifs sur le labelle
            paint.color = accentColor
            drawPhalaenopsisLabellePattern(canvas, paint, size, genetics.patternType)
        }
        
        // Centre
        if (dissolveInfo == null || dissolveInfo.progress < 0.9f) {
            drawOrchideeCenter(canvas, paint, size * 0.8f, accentColor, dissolveInfo)
        }
    }
    
    private fun drawPhalaenopsisSepal(canvas: Canvas, paint: Paint, size: Float, shapeVar: OrchideeShapeVariation, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val path = Path()
        val sepalLength = size * 0.6f * shapeVar.sepalLength
        val sepalWidth = size * 0.3f * shapeVar.sepalWidth
        
        var currentSize = sepalLength
        if (dissolveInfo?.flowersPetalsWilting == true) {
            currentSize *= (1f - dissolveInfo.progress * 0.5f)
        }
        
        path.moveTo(0f, 0f)
        path.quadTo(-sepalWidth * 0.7f, -currentSize * 0.3f, -sepalWidth, -currentSize)
        path.quadTo(0f, -currentSize * 1.1f, sepalWidth, -currentSize)
        path.quadTo(sepalWidth * 0.7f, -currentSize * 0.3f, 0f, 0f)
        
        canvas.drawPath(path, paint)
    }
    
    private fun drawPhalaenopsisPetal(canvas: Canvas, paint: Paint, size: Float, shapeVar: OrchideeShapeVariation, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val path = Path()
        val petalLength = size * 0.8f * shapeVar.petalLength
        val petalWidth = size * 0.4f * shapeVar.petalWidth
        val curvature = shapeVar.petalCurvature
        
        var currentLength = petalLength
        var currentWidth = petalWidth
        
        if (dissolveInfo?.flowersPetalsWilting == true) {
            val shrinkFactor = 1f - dissolveInfo.progress * 0.6f
            currentLength *= shrinkFactor
            currentWidth *= shrinkFactor
        }
        
        // Forme de papillon élargie
        path.moveTo(0f, 0f)
        path.quadTo(-currentWidth * (0.5f + curvature * 0.3f), -currentLength * 0.2f, -currentWidth, -currentLength * 0.7f)
        path.quadTo(-currentWidth * 0.8f, -currentLength, 0f, -currentLength * 1.1f)
        path.quadTo(currentWidth * 0.8f, -currentLength, currentWidth, -currentLength * 0.7f)
        path.quadTo(currentWidth * (0.5f + curvature * 0.3f), -currentLength * 0.2f, 0f, 0f)
        
        canvas.drawPath(path, paint)
    }
    
    private fun drawPhalaenopsisPetalVeins(canvas: Canvas, paint: Paint, size: Float, shapeVar: OrchideeShapeVariation) {
        val petalLength = size * 0.8f * shapeVar.petalLength
        val petalWidth = size * 0.4f * shapeVar.petalWidth
        
        // Nervures principales
        for (i in -1..1) {
            val startX = petalWidth * 0.2f * i
            val endX = petalWidth * 0.4f * i
            val endY = -petalLength * 0.8f
            canvas.drawLine(startX, 0f, endX, endY, paint)
        }
    }
    
    private fun drawPhalaenopsisLabelle(canvas: Canvas, paint: Paint, size: Float, shapeVar: OrchideeShapeVariation, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val labelleSize = size * 0.5f * shapeVar.labelleSize
        var currentSize = labelleSize
        
        if (dissolveInfo?.flowersPetalsWilting == true) {
            currentSize *= (1f - dissolveInfo.progress * 0.7f)
        }
        
        // Labelle en forme de coeur (caractéristique Phalaenopsis)
        val path = Path()
        path.moveTo(0f, currentSize * 0.2f)
        path.quadTo(-currentSize * 0.4f, currentSize * 0.1f, -currentSize * 0.5f, -currentSize * 0.2f)
        path.quadTo(-currentSize * 0.3f, -currentSize * 0.5f, 0f, -currentSize * 0.3f)
        path.quadTo(currentSize * 0.3f, -currentSize * 0.5f, currentSize * 0.5f, -currentSize * 0.2f)
        path.quadTo(currentSize * 0.4f, currentSize * 0.1f, 0f, currentSize * 0.2f)
        
        canvas.drawPath(path, paint)
    }
    
    private fun drawPhalaenopsisLabellePattern(canvas: Canvas, paint: Paint, size: Float, patternType: OrchideePatternType) {
        when (patternType.primary) {
            PatternStyle.SPOTTED -> {
                for (i in 0..2) {
                    val x = (Random.nextFloat() - 0.5f) * size * 0.3f
                    val y = (Random.nextFloat() - 0.5f) * size * 0.2f
                    canvas.drawCircle(x, y, size * 0.02f, paint)
                }
            }
            PatternStyle.VEINED -> {
                paint.strokeWidth = 1f
                for (i in -1..1) {
                    val startX = size * 0.1f * i
                    val endX = size * 0.05f * i
                    canvas.drawLine(startX, size * 0.1f, endX, -size * 0.2f, paint)
                }
            }
            else -> {}
        }
    }
    
    // ==================== CATTLEYA - GRANDES FLEURS RUFFLED ====================
    
    private fun drawCattleya(canvas: Canvas, paint: Paint, flower: OrchideeFlower, size: Float, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val genetics = flower.genetics
        val shapeVar = genetics.shapeVariation
        
        val primaryColor = genetics.colorPalette.primary
        val secondaryColor = genetics.colorPalette.secondary
        val throatColor = genetics.colorPalette.throat
        val veiningColor = genetics.colorPalette.veining
        
        // 5 pétales avec bords ruffled caractéristiques
        
        // Sépales arrière
        for (i in 0..2) {
            val angle = i * 120f
            canvas.save()
            canvas.rotate(angle)
            
            if (dissolveInfo?.flowersPetalsWilting != true || dissolveInfo.progress < 0.5f) {
                paint.style = Paint.Style.FILL
                paint.color = secondaryColor
                drawCattleyaSepal(canvas, paint, size, shapeVar, dissolveInfo)
            }
            
            canvas.restore()
        }
        
        // Pétales principaux avec ruffles
        for (i in 0..1) {
            val angle = if (i == 0) -30f else 30f
            canvas.save()
            canvas.rotate(angle)
            
            if (dissolveInfo?.flowersPetalsWilting != true || dissolveInfo.progress < 0.7f) {
                paint.style = Paint.Style.FILL
                paint.color = primaryColor
                drawCattleyaPetal(canvas, paint, size, shapeVar, dissolveInfo)
                
                // Nervures prononcées
                paint.color = veiningColor
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2f
                drawCattleyaPetalVeins(canvas, paint, size, shapeVar)
            }
            
            canvas.restore()
        }
        
        // Labelle trompette (très caractéristique)
        if (dissolveInfo?.flowersPetalsWilting != true || dissolveInfo.progress < 0.8f) {
            paint.style = Paint.Style.FILL
            paint.color = throatColor
            drawCattleyaLabelle(canvas, paint, size, shapeVar, dissolveInfo)
        }
        
        // Centre avec column
        if (dissolveInfo == null || dissolveInfo.progress < 0.9f) {
            drawOrchideeCenter(canvas, paint, size * 0.6f, genetics.colorPalette.accent, dissolveInfo)
        }
    }
    
    private fun drawCattleyaSepal(canvas: Canvas, paint: Paint, size: Float, shapeVar: OrchideeShapeVariation, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val path = Path()
        val sepalLength = size * 0.7f * shapeVar.sepalLength
        val sepalWidth = size * 0.25f * shapeVar.sepalWidth
        
        var currentSize = sepalLength
        if (dissolveInfo?.flowersPetalsWilting == true) {
            currentSize *= (1f - dissolveInfo.progress * 0.4f)
        }
        
        path.moveTo(0f, 0f)
        path.quadTo(-sepalWidth, -currentSize * 0.4f, -sepalWidth * 0.8f, -currentSize)
        path.quadTo(0f, -currentSize * 1.05f, sepalWidth * 0.8f, -currentSize)
        path.quadTo(sepalWidth, -currentSize * 0.4f, 0f, 0f)
        
        canvas.drawPath(path, paint)
    }
    
    private fun drawCattleyaPetal(canvas: Canvas, paint: Paint, size: Float, shapeVar: OrchideeShapeVariation, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val path = Path()
        val petalLength = size * 0.9f * shapeVar.petalLength
        val petalWidth = size * 0.5f * shapeVar.petalWidth
        val ruffleIntensity = shapeVar.ruffledEdges
        
        var currentLength = petalLength
        var currentWidth = petalWidth
        
        if (dissolveInfo?.flowersPetalsWilting == true) {
            val shrinkFactor = 1f - dissolveInfo.progress * 0.6f
            currentLength *= shrinkFactor
            currentWidth *= shrinkFactor
        }
        
        // Pétale avec bords ondulés
        path.moveTo(0f, 0f)
        
        // Côté gauche avec ruffles
        var x = -currentWidth * 0.3f
        var y = -currentLength * 0.2f
        path.quadTo(x, y, x - currentWidth * 0.3f, y - currentLength * 0.3f)
        
        // Ajouter des ondulations si ruffles activées
        if (ruffleIntensity > 0.3f) {
            for (i in 1..3) {
                val ruffleX = x - currentWidth * 0.2f + sin(i * PI / 2) * currentWidth * 0.1f * ruffleIntensity
                val ruffleY = y - currentLength * (0.3f + i * 0.2f)
                path.lineTo(ruffleX.toFloat(), ruffleY)
            }
        }
        
        path.quadTo(-currentWidth * 0.4f, -currentLength, 0f, -currentLength * 1.1f)
        
        // Côté droit avec ruffles
        x = currentWidth * 0.3f
        y = -currentLength * 0.2f
        
        if (ruffleIntensity > 0.3f) {
            for (i in 3 downTo 1) {
                val ruffleX = x + currentWidth * 0.2f + sin(i * PI / 2) * currentWidth * 0.1f * ruffleIntensity
                val ruffleY = y - currentLength * (0.3f + i * 0.2f)
                path.lineTo(ruffleX.toFloat(), ruffleY)
            }
        }
        
        path.quadTo(x + currentWidth * 0.3f, y - currentLength * 0.3f, x, y)
        path.quadTo(currentWidth * 0.3f, -currentLength * 0.2f, 0f, 0f)
        
        canvas.drawPath(path, paint)
    }
    
    private fun drawCattleyaPetalVeins(canvas: Canvas, paint: Paint, size: Float, shapeVar: OrchideeShapeVariation) {
        val petalLength = size * 0.9f * shapeVar.petalLength
        val petalWidth = size * 0.5f * shapeVar.petalWidth
        
        // Nervures prononcées (caractéristique Cattleya)
        for (i in -2..2) {
            val startX = petalWidth * 0.1f * i
            val endX = petalWidth * 0.2f * i
            val endY = -petalLength * 0.8f
            canvas.drawLine(startX, 0f, endX, endY, paint)
        }
    }
    
    private fun drawCattleyaLabelle(canvas: Canvas, paint: Paint, size: Float, shapeVar: OrchideeShapeVariation, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val labelleSize = size * 0.6f * shapeVar.labelleSize
        var currentSize = labelleSize
        
        if (dissolveInfo?.flowersPetalsWilting == true) {
            currentSize *= (1f - dissolveInfo.progress * 0.8f)
        }
        
        // Labelle en forme de trompette évasée
        val path = Path()
        path.moveTo(0f, currentSize * 0.3f)
        path.quadTo(-currentSize * 0.6f, currentSize * 0.2f, -currentSize * 0.8f, -currentSize * 0.1f)
        path.quadTo(-currentSize * 0.7f, -currentSize * 0.4f, -currentSize * 0.4f, -currentSize * 0.5f)
        path.quadTo(0f, -currentSize * 0.6f, currentSize * 0.4f, -currentSize * 0.5f)
        path.quadTo(currentSize * 0.7f, -currentSize * 0.4f, currentSize * 0.8f, -currentSize * 0.1f)
        path.quadTo(currentSize * 0.6f, currentSize * 0.2f, 0f, currentSize * 0.3f)
        
        canvas.drawPath(path, paint)
    }
    
    // ==================== DENDROBIUM - GRAPPES COMPACTES ====================
    
    private fun drawDendrobium(canvas: Canvas, paint: Paint, flower: OrchideeFlower, size: Float, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val genetics = flower.genetics
        val shapeVar = genetics.shapeVariation
        
        val primaryColor = genetics.colorPalette.primary
        val secondaryColor = genetics.colorPalette.secondary
        val throatColor = genetics.colorPalette.throat
        
        // Structure plus compacte avec pétales étroits
        
        // Sépales étroits
        for (i in 0..2) {
            val angle = i * 120f
            canvas.save()
            canvas.rotate(angle)
            
            if (dissolveInfo?.flowersPetalsWilting != true || dissolveInfo.progress < 0.4f) {
                paint.style = Paint.Style.FILL
                paint.color = secondaryColor
                drawDendrobiumSepal(canvas, paint, size, shapeVar, dissolveInfo)
            }
            
            canvas.restore()
        }
        
        // Pétales principaux plus étroits
        for (i in 0..1) {
            val angle = if (i == 0) -35f else 35f
            canvas.save()
            canvas.rotate(angle)
            
            if (dissolveInfo?.flowersPetalsWilting != true || dissolveInfo.progress < 0.6f) {
                paint.style = Paint.Style.FILL
                paint.color = primaryColor
                drawDendrobiumPetal(canvas, paint, size, shapeVar, dissolveInfo)
            }
            
            canvas.restore()
        }
        
        // Labelle tubulaire petit
        if (dissolveInfo?.flowersPetalsWilting != true || dissolveInfo.progress < 0.8f) {
            paint.style = Paint.Style.FILL
            paint.color = throatColor
            drawDendrobiumLabelle(canvas, paint, size, shapeVar, dissolveInfo)
        }
        
        // Centre compact
        if (dissolveInfo == null || dissolveInfo.progress < 0.9f) {
            drawOrchideeCenter(canvas, paint, size * 0.4f, genetics.colorPalette.accent, dissolveInfo)
        }
    }
    
    private fun drawDendrobiumSepal(canvas: Canvas, paint: Paint, size: Float, shapeVar: OrchideeShapeVariation, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val path = Path()
        val sepalLength = size * 0.5f * shapeVar.sepalLength
        val sepalWidth = size * 0.15f * shapeVar.sepalWidth
        
        var currentSize = sepalLength
        if (dissolveInfo?.flowersPetalsWilting == true) {
            currentSize *= (1f - dissolveInfo.progress * 0.5f)
        }
        
        // Sépales très étroits
        path.moveTo(0f, 0f)
        path.quadTo(-sepalWidth, -currentSize * 0.3f, -sepalWidth * 0.7f, -currentSize)
        path.quadTo(0f, -currentSize * 1.02f, sepalWidth * 0.7f, -currentSize)
        path.quadTo(sepalWidth, -currentSize * 0.3f, 0f, 0f)
        
        canvas.drawPath(path, paint)
    }
    
    private fun drawDendrobiumPetal(canvas: Canvas, paint: Paint, size: Float, shapeVar: OrchideeShapeVariation, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val path = Path()
        val petalLength = size * 0.6f * shapeVar.petalLength
        val petalWidth = size * 0.2f * shapeVar.petalWidth
        
        var currentLength = petalLength
        var currentWidth = petalWidth
        
        if (dissolveInfo?.flowersPetalsWilting == true) {
            val shrinkFactor = 1f - dissolveInfo.progress * 0.5f
            currentLength *= shrinkFactor
            currentWidth *= shrinkFactor
        }
        
        // Pétales allongés et étroits
        path.moveTo(0f, 0f)
        path.quadTo(-currentWidth, -currentLength * 0.3f, -currentWidth * 0.8f, -currentLength)
        path.quadTo(0f, -currentLength * 1.05f, currentWidth * 0.8f, -currentLength)
        path.quadTo(currentWidth, -currentLength * 0.3f, 0f, 0f)
        
        canvas.drawPath(path, paint)
    }
    
    private fun drawDendrobiumLabelle(canvas: Canvas, paint: Paint, size: Float, shapeVar: OrchideeShapeVariation, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val labelleSize = size * 0.3f * shapeVar.labelleSize
        var currentSize = labelleSize
        
        if (dissolveInfo?.flowersPetalsWilting == true) {
            currentSize *= (1f - dissolveInfo.progress * 0.7f)
        }
        
        // Petit labelle tubulaire
        val path = Path()
        path.moveTo(0f, currentSize * 0.2f)
        path.quadTo(-currentSize * 0.3f, currentSize * 0.1f, -currentSize * 0.3f, -currentSize * 0.2f)
        path.quadTo(0f, -currentSize * 0.4f, currentSize * 0.3f, -currentSize * 0.2f)
        path.quadTo(currentSize * 0.3f, currentSize * 0.1f, 0f, currentSize * 0.2f)
        
        canvas.drawPath(path, paint)
    }
    
    // ==================== VANDA - FLEURS PLATES TESSELLÉES ====================
    
    private fun drawVanda(canvas: Canvas, paint: Paint, flower: OrchideeFlower, size: Float, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val genetics = flower.genetics
        val shapeVar = genetics.shapeVariation
        
        val primaryColor = genetics.colorPalette.primary
        val secondaryColor = genetics.colorPalette.secondary
        val spotColor = genetics.colorPalette.spotColor
        val throatColor = genetics.colorPalette.throat
        
        // Fleur très plate avec motifs damier
        
        // 5 pétales très étalés et plats
        for (i in 0..4) {
            val angle = i * 72f
            canvas.save()
            canvas.rotate(angle)
            
            if (dissolveInfo?.flowersPetalsWilting != true || dissolveInfo.progress < 0.5f) {
                paint.style = Paint.Style.FILL
                paint.color = if (i < 3) secondaryColor else primaryColor
                drawVandaPetal(canvas, paint, size, shapeVar, dissolveInfo)
                
                // Motifs tessellés caractéristiques
                paint.color = spotColor
                drawVandaTessellation(canvas, paint, size, genetics.patternType)
            }
            
            canvas.restore()
        }
        
        // Labelle petit et pointu
        if (dissolveInfo?.flowersPetalsWilting != true || dissolveInfo.progress < 0.8f) {
            paint.style = Paint.Style.FILL
            paint.color = throatColor
            drawVandaLabelle(canvas, paint, size, shapeVar, dissolveInfo)
        }
        
        // Centre compact
        if (dissolveInfo == null || dissolveInfo.progress < 0.9f) {
            drawOrchideeCenter(canvas, paint, size * 0.3f, genetics.colorPalette.accent, dissolveInfo)
        }
    }
    
    private fun drawVandaPetal(canvas: Canvas, paint: Paint, size: Float, shapeVar: OrchideeShapeVariation, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val path = Path()
        val petalLength = size * 0.7f * shapeVar.petalLength
        val petalWidth = size * 0.4f * shapeVar.petalWidth
        val flatness = shapeVar.flatness
        
        var currentLength = petalLength
        var currentWidth = petalWidth
        
        if (dissolveInfo?.flowersPetalsWilting == true) {
            val shrinkFactor = 1f - dissolveInfo.progress * 0.4f
            currentLength *= shrinkFactor
            currentWidth *= shrinkFactor
        }
        
        // Pétales très plats et étalés
        path.moveTo(0f, 0f)
        path.quadTo(-currentWidth * flatness, -currentLength * 0.2f, -currentWidth, -currentLength)
        path.quadTo(0f, -currentLength * 1.02f, currentWidth, -currentLength)
        path.quadTo(currentWidth * flatness, -currentLength * 0.2f, 0f, 0f)
        
        canvas.drawPath(path, paint)
    }
    
    private fun drawVandaTessellation(canvas: Canvas, paint: Paint, size: Float, patternType: OrchideePatternType) {
        if (patternType.primary == PatternStyle.SPOTTED) {
            // Motif damier/tessellé caractéristique des Vanda
            val cellSize = size * 0.08f
            for (x in -2..2) {
                for (y in -3..-1) {
                    if ((x + y) % 2 == 0) {
                        val cellX = x * cellSize
                        val cellY = y * cellSize * 0.5f
                        canvas.drawCircle(cellX, cellY, cellSize * 0.3f, paint)
                    }
                }
            }
        }
    }
    
    private fun drawVandaLabelle(canvas: Canvas, paint: Paint, size: Float, shapeVar: OrchideeShapeVariation, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val labelleSize = size * 0.25f * shapeVar.labelleSize
        var currentSize = labelleSize
        
        if (dissolveInfo?.flowersPetalsWilting == true) {
            currentSize *= (1f - dissolveInfo.progress * 0.6f)
        }
        
        // Labelle petit et pointu
        val path = Path()
        path.moveTo(0f, currentSize * 0.1f)
        path.quadTo(-currentSize * 0.2f, 0f, -currentSize * 0.15f, -currentSize * 0.3f)
        path.quadTo(0f, -currentSize * 0.4f, currentSize * 0.15f, -currentSize * 0.3f)
        path.quadTo(currentSize * 0.2f, 0f, 0f, currentSize * 0.1f)
        
        canvas.drawPath(path, paint)
    }
    
    // ==================== ONCIDIUM - DANCING LADY ====================
    
    private fun drawOncidium(canvas: Canvas, paint: Paint, flower: OrchideeFlower, size: Float, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val genetics = flower.genetics
        val shapeVar = genetics.shapeVariation
        
        val primaryColor = genetics.colorPalette.primary
        val secondaryColor = genetics.colorPalette.secondary
        val throatColor = genetics.colorPalette.throat
        val accentColor = genetics.colorPalette.accent
        
        // Petite fleur avec labelle proéminent en "jupe"
        
        // Sépales et pétales petits (arrière-plan)
        for (i in 0..3) {
            val angle = i * 90f
            canvas.save()
            canvas.rotate(angle)
            
            if (dissolveInfo?.flowersPetalsWilting != true || dissolveInfo.progress < 0.4f) {
                paint.style = Paint.Style.FILL
                paint.color = secondaryColor
                drawOncidiumSmallPetal(canvas, paint, size, shapeVar, dissolveInfo)
            }
            
            canvas.restore()
        }
        
        // LABELLE PROÉMINENT en forme de jupe (caractéristique principale)
        if (dissolveInfo?.flowersPetalsWilting != true || dissolveInfo.progress < 0.7f) {
            paint.style = Paint.Style.FILL
            paint.color = primaryColor
            drawOncidiumSkirtLabelle(canvas, paint, size, shapeVar, dissolveInfo)
            
            // Motifs sur la jupe
            paint.color = accentColor
            drawOncidiumSkirtPattern(canvas, paint, size, genetics.patternType)
        }
        
        // Centre avec colonne proéminente
        if (dissolveInfo == null || dissolveInfo.progress < 0.9f) {
            paint.style = Paint.Style.FILL
            paint.color = throatColor
            drawOncidiumColumn(canvas, paint, size, dissolveInfo)
        }
    }
    
    private fun drawOncidiumSmallPetal(canvas: Canvas, paint: Paint, size: Float, shapeVar: OrchideeShapeVariation, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val path = Path()
        val petalLength = size * 0.3f * shapeVar.petalLength
        val petalWidth = size * 0.15f * shapeVar.petalWidth
        
        var currentLength = petalLength
        var currentWidth = petalWidth
        
        if (dissolveInfo?.flowersPetalsWilting == true) {
            val shrinkFactor = 1f - dissolveInfo.progress * 0.4f
            currentLength *= shrinkFactor
            currentWidth *= shrinkFactor
        }
        
        // Petits pétales étroits
        path.moveTo(0f, 0f)
        path.quadTo(-currentWidth, -currentLength * 0.3f, -currentWidth * 0.8f, -currentLength)
        path.quadTo(0f, -currentLength * 1.05f, currentWidth * 0.8f, -currentLength)
        path.quadTo(currentWidth, -currentLength * 0.3f, 0f, 0f)
        
        canvas.drawPath(path, paint)
    }
    
    private fun drawOncidiumSkirtLabelle(canvas: Canvas, paint: Paint, size: Float, shapeVar: OrchideeShapeVariation, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val labelleSize = size * 0.8f * shapeVar.labelleSize
        var currentSize = labelleSize
        
        if (dissolveInfo?.flowersPetalsWilting == true) {
            currentSize *= (1f - dissolveInfo.progress * 0.8f)
        }
        
        // Labelle en forme de jupe évasée (dancing lady)
        val path = Path()
        path.moveTo(0f, currentSize * 0.1f)
        
        // Partie évasée de la jupe
        path.quadTo(-currentSize * 0.6f, currentSize * 0.05f, -currentSize * 0.9f, -currentSize * 0.2f)
        path.quadTo(-currentSize * 0.8f, -currentSize * 0.5f, -currentSize * 0.5f, -currentSize * 0.6f)
        path.quadTo(-currentSize * 0.2f, -currentSize * 0.7f, 0f, -currentSize * 0.65f)
        path.quadTo(currentSize * 0.2f, -currentSize * 0.7f, currentSize * 0.5f, -currentSize * 0.6f)
        path.quadTo(currentSize * 0.8f, -currentSize * 0.5f, currentSize * 0.9f, -currentSize * 0.2f)
        path.quadTo(currentSize * 0.6f, currentSize * 0.05f, 0f, currentSize * 0.1f)
        
        canvas.drawPath(path, paint)
    }
    
    private fun drawOncidiumSkirtPattern(canvas: Canvas, paint: Paint, size: Float, patternType: OrchideePatternType) {
        when (patternType.primary) {
            PatternStyle.SPOTTED -> {
                // Petites taches sur la jupe
                for (i in 0..4) {
                    val angle = i * 72f
                    val radius = size * 0.4f
                    val x = cos(angle * PI / 180) * radius
                    val y = sin(angle * PI / 180) * radius - size * 0.3f
                    canvas.drawCircle(x.toFloat(), y.toFloat(), size * 0.03f, paint)
                }
            }
            PatternStyle.STRIPED -> {
                // Rayures radiales
                paint.strokeWidth = 1.5f
                for (i in -2..2) {
                    val angle = i * 15f
                    val startX = sin(angle * PI / 180) * size * 0.2f
                    val startY = -cos(angle * PI / 180) * size * 0.2f
                    val endX = sin(angle * PI / 180) * size * 0.5f
                    val endY = -cos(angle * PI / 180) * size * 0.5f
                    canvas.drawLine(startX.toFloat(), startY.toFloat(), endX.toFloat(), endY.toFloat(), paint)
                }
            }
            else -> {}
        }
    }
    
    private fun drawOncidiumColumn(canvas: Canvas, paint: Paint, size: Float, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        var columnSize = size * 0.15f
        
        if (dissolveInfo != null && dissolveInfo.progress > 0.8f) {
            columnSize *= (1f - (dissolveInfo.progress - 0.8f) * 3f).coerceAtLeast(0.2f)
        }
        
        // Colonne proéminente au centre
        val path = Path()
        path.moveTo(0f, columnSize * 0.3f)
        path.quadTo(-columnSize * 0.3f, columnSize * 0.2f, -columnSize * 0.2f, -columnSize * 0.2f)
        path.quadTo(0f, -columnSize * 0.4f, columnSize * 0.2f, -columnSize * 0.2f)
        path.quadTo(columnSize * 0.3f, columnSize * 0.2f, 0f, columnSize * 0.3f)
        
        canvas.drawPath(path, paint)
    }
    
    // ==================== CYMBIDIUM - GRANDES FLEURS EN ÉPI ====================
    
    private fun drawCymbidium(canvas: Canvas, paint: Paint, flower: OrchideeFlower, size: Float, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val genetics = flower.genetics
        val shapeVar = genetics.shapeVariation
        
        val primaryColor = genetics.colorPalette.primary
        val secondaryColor = genetics.colorPalette.secondary
        val throatColor = genetics.colorPalette.throat
        val veiningColor = genetics.colorPalette.veining
        
        // Grande fleur avec aspect cireux
        
        // Sépales et pétales similaires (6 au total)
        for (i in 0..5) {
            val angle = i * 60f
            canvas.save()
            canvas.rotate(angle)
            
            if (dissolveInfo?.flowersPetalsWilting != true || dissolveInfo.progress < 0.5f) {
                paint.style = Paint.Style.FILL
                paint.color = if (i % 2 == 0) primaryColor else secondaryColor
                drawCymbidiumPetal(canvas, paint, size, shapeVar, dissolveInfo)
                
                // Nervures marquées
                paint.color = veiningColor
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1.5f
                drawCymbidiumPetalVeins(canvas, paint, size, shapeVar)
            }
            
            canvas.restore()
        }
        
        // Labelle en forme de bateau
        if (dissolveInfo?.flowersPetalsWilting != true || dissolveInfo.progress < 0.8f) {
            paint.style = Paint.Style.FILL
            paint.color = throatColor
            drawCymbidiumBoatLabelle(canvas, paint, size, shapeVar, dissolveInfo)
        }
        
        // Centre avec finition cireuse
        if (dissolveInfo == null || dissolveInfo.progress < 0.9f) {
            drawOrchideeCenter(canvas, paint, size * 0.5f, genetics.colorPalette.accent, dissolveInfo)
        }
    }
    
    private fun drawCymbidiumPetal(canvas: Canvas, paint: Paint, size: Float, shapeVar: OrchideeShapeVariation, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val path = Path()
        val petalLength = size * 0.8f * shapeVar.petalLength
        val petalWidth = size * 0.35f * shapeVar.petalWidth
        
        var currentLength = petalLength
        var currentWidth = petalWidth
        
        if (dissolveInfo?.flowersPetalsWilting == true) {
            val shrinkFactor = 1f - dissolveInfo.progress * 0.5f
            currentLength *= shrinkFactor
            currentWidth *= shrinkFactor
        }
        
        // Pétales élégants et allongés
        path.moveTo(0f, 0f)
        path.quadTo(-currentWidth * 0.8f, -currentLength * 0.3f, -currentWidth, -currentLength * 0.8f)
        path.quadTo(-currentWidth * 0.6f, -currentLength * 1.1f, 0f, -currentLength * 1.05f)
        path.quadTo(currentWidth * 0.6f, -currentLength * 1.1f, currentWidth, -currentLength * 0.8f)
        path.quadTo(currentWidth * 0.8f, -currentLength * 0.3f, 0f, 0f)
        
        canvas.drawPath(path, paint)
    }
    
    private fun drawCymbidiumPetalVeins(canvas: Canvas, paint: Paint, size: Float, shapeVar: OrchideeShapeVariation) {
        val petalLength = size * 0.8f * shapeVar.petalLength
        val petalWidth = size * 0.35f * shapeVar.petalWidth
        
        // Nervures parallèles marquées
        for (i in -2..2) {
            val startX = petalWidth * 0.15f * i
            val endX = petalWidth * 0.2f * i
            val endY = -petalLength * 0.9f
            canvas.drawLine(startX, 0f, endX, endY, paint)
        }
    }
    
    private fun drawCymbidiumBoatLabelle(canvas: Canvas, paint: Paint, size: Float, shapeVar: OrchideeShapeVariation, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val labelleSize = size * 0.5f * shapeVar.labelleSize
        var currentSize = labelleSize
        
        if (dissolveInfo?.flowersPetalsWilting == true) {
            currentSize *= (1f - dissolveInfo.progress * 0.7f)
        }
        
        // Labelle en forme de bateau (caractéristique Cymbidium)
        val path = Path()
        path.moveTo(0f, currentSize * 0.2f)
        path.quadTo(-currentSize * 0.4f, currentSize * 0.15f, -currentSize * 0.6f, 0f)
        path.quadTo(-currentSize * 0.5f, -currentSize * 0.3f, -currentSize * 0.2f, -currentSize * 0.4f)
        path.quadTo(0f, -currentSize * 0.45f, currentSize * 0.2f, -currentSize * 0.4f)
        path.quadTo(currentSize * 0.5f, -currentSize * 0.3f, currentSize * 0.6f, 0f)
        path.quadTo(currentSize * 0.4f, currentSize * 0.15f, 0f, currentSize * 0.2f)
        
        canvas.drawPath(path, paint)
    }
    
    // ==================== CENTRE COMMUN ====================
    
    private fun drawOrchideeCenter(canvas: Canvas, paint: Paint, size: Float, accentColor: Int, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        var centerSize = size
        
        // Réduire si dissolution avancée
        if (dissolveInfo != null && dissolveInfo.progress > 0.7f) {
            centerSize *= (1f - (dissolveInfo.progress - 0.7f) * 2f).coerceAtLeast(0.3f)
        }
        
        // Colonne centrale (column)
        paint.style = Paint.Style.FILL
        paint.color = accentColor
        canvas.drawCircle(0f, 0f, centerSize * 0.08f, paint)
        
        // Pollinie (petites masses de pollen)
        paint.color = OrchideeColorHelper.blendColors(accentColor, Color.YELLOW, 0.3f)
        for (i in 0..1) {
            val angle = i * 180f
            val x = cos(angle * PI / 180) * centerSize * 0.05f
            val y = sin(angle * PI / 180) * centerSize * 0.05f
            canvas.drawCircle(x.toFloat(), y.toFloat(), centerSize * 0.02f, paint)
        }
        
        // Point central
        if (dissolveInfo == null || dissolveInfo.progress < 0.95f) {
            paint.color = OrchideeColorHelper.blendColors(accentColor, Color.BLACK, 0.4f)
            canvas.drawCircle(0f, 0f, centerSize * 0.03f, paint)
        }
    }
}
