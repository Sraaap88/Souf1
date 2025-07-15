package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Path
import android.graphics.PointF
import kotlin.math.*

class OrchideeRenderer {
    
    // Délégué pour le dessin des fleurs
    private val flowerDrawer = OrchideeFlowerDrawer()
    
    fun drawOrchidee(
        canvas: Canvas,
        stemPaint: Paint,
        leafPaint: Paint,
        flowerPaint: Paint,
        stems: List<OrchideeStem>,
        flowers: List<OrchideeFlower>,
        dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null
    ) {
        drawStems(canvas, stemPaint, stems, dissolveInfo)
        drawLeaves(canvas, leafPaint, stems, dissolveInfo)
        drawFlowers(canvas, flowerPaint, flowers, dissolveInfo)
    }
    
    private fun drawStems(canvas: Canvas, paint: Paint, stems: List<OrchideeStem>, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        paint.color = Color.rgb(40, 120, 40)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeWidth = 6f
        
        // Appliquer les effets de dissolution
        if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            val alpha = ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
            paint.alpha = alpha
            
            // Si les tiges s'effondrent, réduire la largeur et changer la couleur
            if (dissolveInfo.stemsCollapsing) {
                paint.strokeWidth = 6f * (1f - dissolveInfo.progress * 0.6f)
                
                // Couleur vers le brun
                val collapsingFactor = dissolveInfo.progress
                val red = (40 + (139 - 40) * collapsingFactor).toInt()
                val green = (120 * (1f - collapsingFactor * 0.7f)).toInt()
                val blue = (40 * (1f - collapsingFactor * 0.8f)).toInt()
                paint.color = Color.rgb(red, green, blue)
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
                    
                    // Courbe naturelle pour tiges d'orchidée
                    var controlX = (previous.x + current.x) / 2f + sin(i * 0.2f) * 1.5f
                    var controlY = (previous.y + current.y) / 2f
                    
                    // Effet de dissolution - les tiges penchent et se courbent
                    if (dissolveInfo?.stemsCollapsing == true) {
                        val bendFactor = dissolveInfo.progress * 15f
                        val segmentRatio = i.toFloat() / stem.segments.size
                        controlX += bendFactor * segmentRatio * cos(segmentRatio * PI).toFloat()
                        controlY += bendFactor * 0.3f * segmentRatio
                    }
                    
                    path.quadTo(controlX, controlY, current.x, current.y)
                }
                
                canvas.drawPath(path, paint)
                
                // Ajouter texture de tige d'orchidée (segments visibles)
                if (dissolveInfo == null || dissolveInfo.progress < 0.5f) {
                    val originalStrokeWidth = paint.strokeWidth
                    paint.strokeWidth = 1f
                    paint.color = OrchideeColorHelper.blendColors(paint.color, Color.BLACK, 0.3f)
                    
                    // Segments de tige caractéristiques des orchidées
                    for (j in 1 until stem.segments.size step 3) {
                        val segment = stem.segments[j]
                        canvas.drawCircle(segment.x, segment.y, originalStrokeWidth * 0.6f, paint)
                    }
                    
                    paint.strokeWidth = originalStrokeWidth
                }
            }
        }
    }
    
    private fun drawLeaves(canvas: Canvas, paint: Paint, stems: List<OrchideeStem>, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        paint.color = Color.rgb(60, 140, 60)
        paint.style = Paint.Style.FILL
        
        // Appliquer les effets de dissolution aux feuilles
        if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            val alpha = ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
            paint.alpha = alpha
            
            // Si les feuilles se ratatinent, changer la couleur vers le jaune-brun
            if (dissolveInfo.leavesShriveling) {
                val shrivelingFactor = dissolveInfo.progress
                val red = (60 + (180 - 60) * shrivelingFactor).toInt()
                val green = (140 + (150 - 140) * shrivelingFactor * 0.5f).toInt()
                val blue = (60 * (1f - shrivelingFactor * 0.9f)).toInt()
                paint.color = Color.rgb(red, green, blue)
            }
        } else {
            paint.alpha = 255
        }
        
        for (stem in stems) {
            for (leaf in stem.leaves) {
                if (leaf.growthProgress > 0f) {
                    drawOrchideeLeaf(canvas, paint, leaf, dissolveInfo)
                }
            }
        }
    }
    
    private fun drawOrchideeLeaf(canvas: Canvas, paint: Paint, leaf: OrchideeLeaf, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        canvas.save()
        canvas.translate(leaf.attachmentPoint.x, leaf.attachmentPoint.y)
        canvas.rotate(leaf.angle)
        
        var currentLength = leaf.length * leaf.growthProgress
        var currentWidth = leaf.width * leaf.growthProgress
        
        // Réduire la taille si les feuilles se ratatinent
        if (dissolveInfo?.leavesShriveling == true) {
            val shrinkFactor = 1f - dissolveInfo.progress * 0.7f
            currentLength *= shrinkFactor
            currentWidth *= shrinkFactor
            
            // Ajouter effet de courbure/flétrissement
            canvas.rotate(dissolveInfo.progress * 15f)
        }
        
        // Feuille d'orchidée - ovale allongée avec bout pointu
        val path = Path()
        
        // Forme caractéristique des feuilles d'orchidée
        when (leaf.leafType) {
            OrchideeLeafType.STRAP_SHAPED -> drawStrapLeaf(path, currentLength, currentWidth)
            OrchideeLeafType.OVAL_THICK -> drawOvalLeaf(path, currentLength, currentWidth)
            OrchideeLeafType.NEEDLE_THIN -> drawNeedleLeaf(path, currentLength, currentWidth)
            OrchideeLeafType.BROAD_FLAT -> drawBroadLeaf(path, currentLength, currentWidth)
        }
        
        // Dessiner la feuille
        canvas.drawPath(path, paint)
        
        // Nervures (seulement si pas trop dissoute)
        if (dissolveInfo == null || dissolveInfo.progress < 0.6f) {
            drawOrchideeLeafVeins(canvas, paint, currentLength, currentWidth, leaf.leafType, dissolveInfo)
        }
        
        canvas.restore()
    }
    
    private fun drawStrapLeaf(path: Path, length: Float, width: Float) {
        // Feuille en lanière (Vanda, Phalaenopsis)
        path.moveTo(-width / 2f, 0f)
        path.lineTo(width / 2f, 0f)
        path.quadTo(width * 0.3f, -length * 0.7f, 0f, -length)
        path.quadTo(-width * 0.3f, -length * 0.7f, -width / 2f, 0f)
        path.close()
    }
    
    private fun drawOvalLeaf(path: Path, length: Float, width: Float) {
        // Feuille ovale épaisse (Cattleya, Oncidium)
        path.moveTo(-width * 0.4f, 0f)
        path.quadTo(-width * 0.6f, -length * 0.3f, -width * 0.5f, -length * 0.7f)
        path.quadTo(-width * 0.2f, -length * 1.1f, 0f, -length)
        path.quadTo(width * 0.2f, -length * 1.1f, width * 0.5f, -length * 0.7f)
        path.quadTo(width * 0.6f, -length * 0.3f, width * 0.4f, 0f)
        path.quadTo(0f, length * 0.1f, -width * 0.4f, 0f)
        path.close()
    }
    
    private fun drawNeedleLeaf(path: Path, length: Float, width: Float) {
        // Feuille en aiguille (Dendrobium)
        path.moveTo(-width * 0.2f, 0f)
        path.lineTo(width * 0.2f, 0f)
        path.quadTo(width * 0.1f, -length * 0.8f, 0f, -length)
        path.quadTo(-width * 0.1f, -length * 0.8f, -width * 0.2f, 0f)
        path.close()
    }
    
    private fun drawBroadLeaf(path: Path, length: Float, width: Float) {
        // Feuille large et plate (Cymbidium)
        path.moveTo(-width * 0.5f, 0f)
        path.quadTo(-width * 0.7f, -length * 0.2f, -width * 0.6f, -length * 0.6f)
        path.quadTo(-width * 0.3f, -length * 0.9f, 0f, -length)
        path.quadTo(width * 0.3f, -length * 0.9f, width * 0.6f, -length * 0.6f)
        path.quadTo(width * 0.7f, -length * 0.2f, width * 0.5f, 0f)
        path.quadTo(0f, length * 0.05f, -width * 0.5f, 0f)
        path.close()
    }
    
    private fun drawOrchideeLeafVeins(canvas: Canvas, paint: Paint, length: Float, width: Float, leafType: OrchideeLeafType, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val originalColor = paint.color
        val originalStyle = paint.style
        
        paint.color = OrchideeColorHelper.blendColors(originalColor, Color.BLACK, 0.4f)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        
        // Appliquer transparence si dissolution
        if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            val alpha = ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
            paint.alpha = alpha
        }
        
        when (leafType) {
            OrchideeLeafType.STRAP_SHAPED -> {
                // Nervures parallèles
                for (i in -1..1) {
                    val x = width * 0.2f * i
                    canvas.drawLine(x, 0f, x * 0.5f, -length * 0.8f, paint)
                }
            }
            OrchideeLeafType.OVAL_THICK -> {
                // Nervures en éventail
                canvas.drawLine(0f, 0f, 0f, -length * 0.9f, paint) // Nervure centrale
                for (i in listOf(-1, 1)) {
                    val startX = width * 0.1f * i
                    val endX = width * 0.3f * i
                    canvas.drawLine(startX, -length * 0.1f, endX, -length * 0.7f, paint)
                }
            }
            OrchideeLeafType.NEEDLE_THIN -> {
                // Nervure centrale unique
                canvas.drawLine(0f, 0f, 0f, -length * 0.9f, paint)
            }
            OrchideeLeafType.BROAD_FLAT -> {
                // Nervures multiples
                for (i in -2..2) {
                    val startX = width * 0.15f * i
                    val endX = width * 0.2f * i
                    canvas.drawLine(startX, -length * 0.05f, endX, -length * 0.8f, paint)
                }
            }
        }
        
        paint.color = originalColor
        paint.style = originalStyle
    }
    
    private fun drawFlowers(canvas: Canvas, paint: Paint, flowers: List<OrchideeFlower>, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        // Trier par couches pour rendu correct
        val sortedFlowers = flowers.filter { it.bloomProgress > 0f }
            .sortedBy { it.renderLayer }
        
        // Appliquer les effets de dissolution
        val baseAlpha = if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
        } else 255
        
        for (flower in sortedFlowers) {
            // Ajuster l'opacité selon la couche et la dissolution
            val layerAlpha = when {
                flower.renderLayer < 20 -> (180 * baseAlpha / 255) // Arrière-plan
                flower.renderLayer < 40 -> (220 * baseAlpha / 255) // Moyen
                else -> baseAlpha // Premier plan
            }
            
            paint.alpha = layerAlpha
            
            // Dessiner la fleur selon son espèce
            flowerDrawer.drawOrchideeFlower(canvas, paint, flower, dissolveInfo)
        }
        
        // Restaurer l'opacité
        paint.alpha = 255
    }
    
    // ==================== EFFETS SPÉCIAUX ORCHIDÉE ====================
    
    fun drawOrchideeSpecialEffects(canvas: Canvas, paint: Paint, flowers: List<OrchideeFlower>, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        // Pollen et particules brillantes (seulement si pas de dissolution)
        if (dissolveInfo == null || dissolveInfo.progress < 0.3f) {
            drawPollenParticles(canvas, paint, flowers)
        }
        
        // Effet de flétrissement avancé
        if (dissolveInfo != null && dissolveInfo.progress > 0.5f) {
            drawWitheringEffect(canvas, paint, flowers, dissolveInfo)
        }
    }
    
    private fun drawPollenParticles(canvas: Canvas, paint: Paint, flowers: List<OrchideeFlower>) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(255, 255, 200) // Jaune pollen
        paint.alpha = 150
        
        for (flower in flowers) {
            if (flower.bloomProgress > 0.8f) {
                // Petites particules de pollen autour de la fleur
                for (i in 0..2) {
                    val angle = (System.currentTimeMillis() / 1000f + i * 2.1f) % (2 * PI)
                    val radius = flower.sizeMultiplier * 20f
                    val x = flower.position.x + cos(angle) * radius
                    val y = flower.position.y + sin(angle) * radius
                    canvas.drawCircle(x.toFloat(), y.toFloat(), 1.5f, paint)
                }
            }
        }
        
        paint.alpha = 255
    }
    
    private fun drawWitheringEffect(canvas: Canvas, paint: Paint, flowers: List<OrchideeFlower>, dissolveInfo: ChallengeEffectsManager.DissolveInfo) {
        if (dissolveInfo.progress < 0.5f) return
        
        val witherProgress = (dissolveInfo.progress - 0.5f) * 2f // 0 à 1
        
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(139, 69, 19) // Brun fané
        paint.alpha = (witherProgress * 100f).toInt().coerceIn(0, 255)
        
        for (flower in flowers) {
            if (flower.bloomProgress > 0f) {
                // Taches de flétrissement
                val spotCount = (witherProgress * 3f).toInt()
                for (i in 0 until spotCount) {
                    val angle = i * 120f
                    val radius = flower.sizeMultiplier * 15f * witherProgress
                    val x = flower.position.x + cos(angle * PI / 180) * radius
                    val y = flower.position.y + sin(angle * PI / 180) * radius
                    canvas.drawCircle(x.toFloat(), y.toFloat(), 3f * witherProgress, paint)
                }
            }
        }
        
        paint.alpha = 255
    }
    
    // ==================== RENDU SPÉCIALISÉ PAR ESPÈCE ====================
    
    fun drawSpeciesCluster(canvas: Canvas, paint: Paint, flowers: List<OrchideeFlower>, species: OrchideeSpecies, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        when (species) {
            OrchideeSpecies.DENDROBIUM -> drawDendrobiumCluster(canvas, paint, flowers, dissolveInfo)
            OrchideeSpecies.CYMBIDIUM -> drawCymbidiumSpike(canvas, paint, flowers, dissolveInfo)
            OrchideeSpecies.ONCIDIUM -> drawOncidiumBranch(canvas, paint, flowers, dissolveInfo)
            else -> drawFlowers(canvas, paint, flowers, dissolveInfo)
        }
    }
    
    private fun drawDendrobiumCluster(canvas: Canvas, paint: Paint, flowers: List<OrchideeFlower>, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        // Grappes caractéristiques des Dendrobium
        val clusters = flowers.groupBy { it.clusterId }
        
        for ((clusterId, clusterFlowers) in clusters) {
            if (clusterFlowers.size > 1) {
                // Dessiner la tige de grappe
                paint.style = Paint.Style.STROKE
                paint.color = Color.rgb(40, 120, 40)
                paint.strokeWidth = 3f
                
                if (dissolveInfo?.stemsCollapsing == true) {
                    paint.strokeWidth *= (1f - dissolveInfo.progress * 0.5f)
                }
                
                // Ligne reliant les fleurs de la grappe
                for (i in 0 until clusterFlowers.size - 1) {
                    val start = clusterFlowers[i].position
                    val end = clusterFlowers[i + 1].position
                    canvas.drawLine(start.x, start.y, end.x, end.y, paint)
                }
            }
        }
        
        // Dessiner les fleurs
        drawFlowers(canvas, paint, flowers, dissolveInfo)
    }
    
    private fun drawCymbidiumSpike(canvas: Canvas, paint: Paint, flowers: List<OrchideeFlower>, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        // Épi caractéristique des Cymbidium
        if (flowers.size > 2) {
            paint.style = Paint.Style.STROKE
            paint.color = Color.rgb(60, 140, 60)
            paint.strokeWidth = 5f
            
            if (dissolveInfo?.stemsCollapsing == true) {
                paint.strokeWidth *= (1f - dissolveInfo.progress * 0.4f)
            }
            
            // Tige principale de l'épi
            val sortedFlowers = flowers.sortedBy { it.position.y }
            val path = Path()
            
            if (sortedFlowers.isNotEmpty()) {
                path.moveTo(sortedFlowers[0].position.x, sortedFlowers[0].position.y)
                
                for (i in 1 until sortedFlowers.size) {
                    val flower = sortedFlowers[i]
                    path.lineTo(flower.position.x, flower.position.y)
                }
                
                canvas.drawPath(path, paint)
            }
        }
        
        // Dessiner les fleurs
        drawFlowers(canvas, paint, flowers, dissolveInfo)
    }
    
    private fun drawOncidiumBranch(canvas: Canvas, paint: Paint, flowers: List<OrchideeFlower>, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        // Branches ramifiées des Oncidium
        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(50, 130, 50)
        paint.strokeWidth = 2f
        
        if (dissolveInfo?.stemsCollapsing == true) {
            paint.strokeWidth *= (1f - dissolveInfo.progress * 0.6f)
        }
        
        // Petites branches vers chaque fleur
        for (flower in flowers) {
            if (flower.bloomProgress > 0f) {
                val branchLength = 15f * flower.sizeMultiplier
                val branchAngle = flower.angle + 90f
                
                val startX = flower.position.x - cos(branchAngle * PI / 180) * branchLength
                val startY = flower.position.y - sin(branchAngle * PI / 180) * branchLength
                
                canvas.drawLine(startX.toFloat(), startY.toFloat(), flower.position.x, flower.position.y, paint)
            }
        }
        
        // Dessiner les fleurs
        drawFlowers(canvas, paint, flowers, dissolveInfo)
    }
}
