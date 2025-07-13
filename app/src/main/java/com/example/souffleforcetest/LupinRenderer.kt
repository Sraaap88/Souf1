package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import kotlin.math.sqrt

class LupinRenderer {
    
    // ==================== FONCTION PRINCIPALE DE RENDU ====================
    
    fun drawLupin(
        canvas: Canvas, 
        stemPaint: Paint, 
        leafPaint: Paint, 
        flowerPaint: Paint,
        stems: List<LupinStem>,
        leaves: List<LupinLeaf>,
        dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null
    ) {
        drawStems(canvas, stemPaint, stems, dissolveInfo)
        drawBasalShoots(canvas, stemPaint, stems, dissolveInfo)
        drawLeaves(canvas, leafPaint, stems, leaves, dissolveInfo)
        drawFlowerSpikes(canvas, flowerPaint, stems, dissolveInfo)
    }
    
    // ==================== RENDU DES TIGES ====================
    
    private fun drawStems(canvas: Canvas, paint: Paint, stems: List<LupinStem>, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        paint.color = Color.rgb(34, 139, 34)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        
        // NOUVEAU: Appliquer les effets de dissolution
        if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            // Réduire l'opacité en fonction de la dissolution
            val alpha = ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
            paint.alpha = alpha
            
            // Si les tiges s'effondrent, changer la couleur vers le brun
            if (dissolveInfo.stemsCollapsing) {
                val shrivelingFactor = dissolveInfo.progress
                val red = (34 + (139 - 34) * shrivelingFactor).toInt() // Vers brun
                val green = (139 * (1f - shrivelingFactor * 0.6f)).toInt()
                val blue = (34 * (1f - shrivelingFactor * 0.8f)).toInt()
                paint.color = Color.rgb(red, green, blue)
            }
        } else {
            paint.alpha = 255
        }
        
        for (stem in stems) {
            if (stem.points.size >= 2) {
                for (i in 1 until stem.points.size) {
                    val p1 = stem.points[i-1]
                    val p2 = stem.points[i]
                    
                    var strokeWidth = p1.thickness
                    
                    // NOUVEAU: Réduire l'épaisseur si les tiges s'effondrent
                    if (dissolveInfo?.stemsCollapsing == true) {
                        strokeWidth *= (1f - dissolveInfo.progress * 0.4f)
                    }
                    
                    paint.strokeWidth = strokeWidth
                    canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint)
                }
            }
        }
    }
    
    private fun drawBasalShoots(canvas: Canvas, paint: Paint, stems: List<LupinStem>, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        paint.color = Color.rgb(34, 139, 34)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        
        // NOUVEAU: Appliquer les effets de dissolution aux tiges basales
        if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            val alpha = ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
            paint.alpha = alpha
            
            // Changement de couleur vers le brun
            if (dissolveInfo.stemsCollapsing) {
                val shrivelingFactor = dissolveInfo.progress
                val red = (34 + (139 - 34) * shrivelingFactor).toInt()
                val green = (139 * (1f - shrivelingFactor * 0.6f)).toInt()
                val blue = (34 * (1f - shrivelingFactor * 0.8f)).toInt()
                paint.color = Color.rgb(red, green, blue)
            }
        } else {
            paint.alpha = 255
        }
        
        for (stem in stems) {
            for (basalShoot in stem.basalShoots) {
                if (basalShoot.points.size >= 2) {
                    for (i in 1 until basalShoot.points.size) {
                        val p1 = basalShoot.points[i-1]
                        val p2 = basalShoot.points[i]
                        
                        var strokeWidth = p1.thickness
                        
                        // NOUVEAU: Réduire l'épaisseur si dissolution
                        if (dissolveInfo?.stemsCollapsing == true) {
                            strokeWidth *= (1f - dissolveInfo.progress * 0.4f)
                        }
                        
                        paint.strokeWidth = strokeWidth
                        canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint)
                    }
                }
            }
        }
    }
    
    // ==================== RENDU DES FEUILLES ====================
    
    private fun drawLeaves(canvas: Canvas, paint: Paint, stems: List<LupinStem>, leaves: List<LupinLeaf>, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        paint.color = Color.rgb(34, 139, 34)
        paint.style = Paint.Style.FILL
        
        // NOUVEAU: Appliquer les effets de dissolution aux feuilles
        if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            val alpha = ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
            paint.alpha = alpha
            
            // Si les feuilles se ratatinent, changer la couleur vers le brun
            if (dissolveInfo.leavesShriveling) {
                val shrivelingFactor = dissolveInfo.progress
                val red = (34 + (139 - 34) * shrivelingFactor).toInt() // Vers brun
                val green = (139 * (1f - shrivelingFactor * 0.7f)).toInt()
                val blue = (34 * (1f - shrivelingFactor * 0.8f)).toInt()
                paint.color = Color.rgb(red, green, blue)
            }
        } else {
            paint.alpha = 255
        }
        
        for (leaf in leaves) {
            if (leaf.currentSize > 0 && leaf.stemIndex < stems.size) {
                if (leaf.isBasalShoot) {
                    // Feuille sur une petite tige basale
                    val stem = stems[leaf.stemIndex]
                    if (leaf.basalShootIndex < stem.basalShoots.size) {
                        val basalShoot = stem.basalShoots[leaf.basalShootIndex]
                        val leafPoint = getBasalShootPointAtHeight(basalShoot, leaf.heightRatio)
                        
                        leafPoint?.let { point ->
                            drawPalmateLeaf(canvas, paint, point.x, point.y, leaf, dissolveInfo)
                        }
                    }
                } else {
                    // Feuille sur une tige principale
                    val stem = stems[leaf.stemIndex]
                    val leafPoint = getStemPointAtHeight(stem, leaf.heightRatio)
                    
                    leafPoint?.let { point ->
                        drawPalmateLeaf(canvas, paint, point.x, point.y, leaf, dissolveInfo)
                    }
                }
            }
        }
    }
    
    private fun drawPalmateLeaf(canvas: Canvas, paint: Paint, x: Float, y: Float, leaf: LupinLeaf, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        var size = leaf.currentSize
        if (size <= 0) return
        
        // NOUVEAU: Réduire la taille si les feuilles se ratatinent
        if (dissolveInfo?.leavesShriveling == true) {
            val shrinkFactor = 1f - dissolveInfo.progress * 0.7f
            size *= shrinkFactor
        }
        
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(leaf.angle)
        
        val folioleCount = leaf.folioleCount.coerceAtMost(leaf.folioleAngles.size)
        val angleSpread = 80f  // Plus étalé comme sur la photo
        
        for (i in 0 until folioleCount) {
            val folioleAngle = (i - folioleCount / 2f) * (angleSpread / folioleCount) + leaf.folioleAngles[i]
            var folioleLength = size * (0.9f + (i % 3) * 0.15f)
            var folioleWidth = folioleLength * 0.25f  // Plus étroit comme sur la photo
            
            // NOUVEAU: Réduire encore plus la taille des folioles individuelles
            if (dissolveInfo?.leavesShriveling == true) {
                val additionalShrink = 1f - dissolveInfo.progress * 0.3f
                folioleLength *= additionalShrink
                folioleWidth *= additionalShrink
            }
            
            canvas.save()
            canvas.rotate(folioleAngle)
            
            // Couleur des folioles (affectée par la dissolution)
            var folioleColor = Color.rgb(50, 150, 50)
            if (dissolveInfo?.leavesShriveling == true) {
                val shrivelingFactor = dissolveInfo.progress
                val red = (50 + (120 - 50) * shrivelingFactor).toInt()
                val green = (150 * (1f - shrivelingFactor * 0.6f)).toInt()
                val blue = (50 * (1f - shrivelingFactor * 0.8f)).toInt()
                folioleColor = Color.rgb(red, green, blue)
            }
            
            // Foliole en forme de lancette (plus pointue)
            paint.color = folioleColor
            paint.style = Paint.Style.FILL
            
            // Forme lancéolée réaliste
            canvas.drawOval(
                -folioleWidth/2, folioleLength * 0.1f,
                folioleWidth/2, folioleLength * 0.95f,
                paint
            )
            
            // Pointe effilée en haut (seulement si pas trop dissoute)
            if (dissolveInfo == null || dissolveInfo.progress < 0.6f) {
                paint.color = Color.rgb(45, 140, 45)
                if (dissolveInfo?.leavesShriveling == true) {
                    val shrivelingFactor = dissolveInfo.progress
                    val red = (45 + (115 - 45) * shrivelingFactor).toInt()
                    val green = (140 * (1f - shrivelingFactor * 0.6f)).toInt()
                    val blue = (45 * (1f - shrivelingFactor * 0.8f)).toInt()
                    paint.color = Color.rgb(red, green, blue)
                }
                canvas.drawOval(
                    -folioleWidth/4, 0f,
                    folioleWidth/4, folioleLength * 0.2f,
                    paint
                )
            }
            
            // Contour sombre pour bien définir chaque foliole (s'affaiblit avec dissolution)
            if (dissolveInfo == null || dissolveInfo.progress < 0.8f) {
                paint.color = Color.rgb(25, 100, 25)
                if (dissolveInfo?.leavesShriveling == true) {
                    val shrivelingFactor = dissolveInfo.progress
                    val red = (25 + (90 - 25) * shrivelingFactor).toInt()
                    val green = (100 * (1f - shrivelingFactor * 0.7f)).toInt()
                    val blue = (25 * (1f - shrivelingFactor * 0.8f)).toInt()
                    paint.color = Color.rgb(red, green, blue)
                }
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 2.5f * (if (dissolveInfo?.leavesShriveling == true) 1f - dissolveInfo.progress * 0.5f else 1f)
                canvas.drawOval(
                    -folioleWidth/2, folioleLength * 0.1f,
                    folioleWidth/2, folioleLength * 0.95f,
                    paint
                )
            }
            
            // Nervure centrale très marquée (s'affaiblit avec dissolution)
            if (dissolveInfo == null || dissolveInfo.progress < 0.7f) {
                paint.color = Color.rgb(20, 90, 20)
                if (dissolveInfo?.leavesShriveling == true) {
                    val shrivelingFactor = dissolveInfo.progress
                    val red = (20 + (80 - 20) * shrivelingFactor).toInt()
                    val green = (90 * (1f - shrivelingFactor * 0.7f)).toInt()
                    val blue = (20 * (1f - shrivelingFactor * 0.8f)).toInt()
                    paint.color = Color.rgb(red, green, blue)
                }
                paint.strokeWidth = 3f * (if (dissolveInfo?.leavesShriveling == true) 1f - dissolveInfo.progress * 0.4f else 1f)
                canvas.drawLine(0f, folioleLength * 0.15f, 0f, folioleLength * 0.85f, paint)
            }
            
            // Nervures secondaires bien définies (disparaissent plus vite)
            if (dissolveInfo == null || dissolveInfo.progress < 0.5f) {
                paint.strokeWidth = 1.5f * (if (dissolveInfo?.leavesShriveling == true) 1f - dissolveInfo.progress * 0.6f else 1f)
                paint.color = Color.rgb(30, 110, 30)
                if (dissolveInfo?.leavesShriveling == true) {
                    val shrivelingFactor = dissolveInfo.progress
                    val red = (30 + (100 - 30) * shrivelingFactor).toInt()
                    val green = (110 * (1f - shrivelingFactor * 0.6f)).toInt()
                    val blue = (30 * (1f - shrivelingFactor * 0.8f)).toInt()
                    paint.color = Color.rgb(red, green, blue)
                }
                for (j in 1..4) {
                    val nervureY = folioleLength * (0.25f + j * 0.15f)
                    val nervureWidth = folioleWidth * (0.35f - j * 0.06f)
                    canvas.drawLine(-nervureWidth/2, nervureY, 0f, nervureY * 0.95f, paint)
                    canvas.drawLine(nervureWidth/2, nervureY, 0f, nervureY * 0.95f, paint)
                }
            }
            
            paint.style = Paint.Style.FILL
            canvas.restore()
        }
        
        // Pétiole bien marqué (s'affaiblit avec dissolution)
        if (dissolveInfo == null || dissolveInfo.progress < 0.8f) {
            paint.color = Color.rgb(35, 110, 35)
            if (dissolveInfo?.leavesShriveling == true) {
                val shrivelingFactor = dissolveInfo.progress
                val red = (35 + (105 - 35) * shrivelingFactor).toInt()
                val green = (110 * (1f - shrivelingFactor * 0.7f)).toInt()
                val blue = (35 * (1f - shrivelingFactor * 0.8f)).toInt()
                paint.color = Color.rgb(red, green, blue)
            }
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f * (if (dissolveInfo?.leavesShriveling == true) 1f - dissolveInfo.progress * 0.3f else 1f)
            canvas.drawLine(0f, 0f, 0f, -size * 0.25f, paint)
        }
        
        paint.style = Paint.Style.FILL
        canvas.restore()
    }
    
    // ==================== RENDU DES FLEURS ====================
    
    private fun drawFlowerSpikes(canvas: Canvas, paint: Paint, stems: List<LupinStem>, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        paint.style = Paint.Style.FILL
        
        // NOUVEAU: Appliquer les effets de dissolution aux fleurs
        val baseAlpha = if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
        } else 255
        
        for (stem in stems) {
            if (!stem.flowerSpike.hasStartedBlooming) continue
            
            for (flower in stem.flowerSpike.flowers) {
                if (flower.currentSize > 0) {
                    val colorRgb = flower.color.rgb
                    var size = flower.currentSize
                    
                    // NOUVEAU: Réduire la taille des fleurs si elles flétrissent
                    if (dissolveInfo?.flowersPetalsWilting == true) {
                        val wiltFactor = 1f - dissolveInfo.progress * 0.8f
                        size *= wiltFactor
                    }
                    
                    // Couleur des fleurs (ternit avec dissolution)
                    var flowerRed = colorRgb[0]
                    var flowerGreen = colorRgb[1]
                    var flowerBlue = colorRgb[2]
                    
                    if (dissolveInfo?.flowersPetalsWilting == true) {
                        val wiltFactor = dissolveInfo.progress
                        flowerRed = (colorRgb[0] * (1f - wiltFactor * 0.4f)).toInt()
                        flowerGreen = (colorRgb[1] * (1f - wiltFactor * 0.5f)).toInt()
                        flowerBlue = (colorRgb[2] * (1f - wiltFactor * 0.3f)).toInt()
                    }
                    
                    paint.alpha = baseAlpha
                    
                    // Fleur conique TRÈS définie - chaque pétale visible
                    
                    // Pétale central (étendard) - forme conique
                    paint.color = Color.rgb(flowerRed, flowerGreen, flowerBlue)
                    canvas.drawOval(
                        flower.x - size * 0.4f, flower.y - size * 0.6f,
                        flower.x + size * 0.4f, flower.y + size * 0.1f, 
                        paint
                    )
                    
                    // Contour sombre pour définir le pétale central (plus faible si dissolution)
                    if (dissolveInfo == null || dissolveInfo.progress < 0.7f) {
                        paint.color = Color.rgb(
                            (flowerRed * 0.6f).toInt(),
                            (flowerGreen * 0.6f).toInt(),
                            (flowerBlue * 0.6f).toInt()
                        )
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = 2f * (if (dissolveInfo?.flowersPetalsWilting == true) 1f - dissolveInfo.progress * 0.5f else 1f)
                        canvas.drawOval(
                            flower.x - size * 0.4f, flower.y - size * 0.6f,
                            flower.x + size * 0.4f, flower.y + size * 0.1f, 
                            paint
                        )
                        paint.style = Paint.Style.FILL
                    }
                    
                    // Pétales latéraux (ailes) - bien définis
                    paint.color = Color.rgb(
                        (flowerRed * 0.85f).toInt(),
                        (flowerGreen * 0.85f).toInt(),
                        (flowerBlue * 0.85f).toInt()
                    )
                    
                    // Aile gauche
                    canvas.drawOval(
                        flower.x - size * 0.7f, flower.y - size * 0.2f,
                        flower.x - size * 0.1f, flower.y + size * 0.3f,
                        paint
                    )
                    
                    // Contour aile gauche (plus faible si dissolution)
                    if (dissolveInfo == null || dissolveInfo.progress < 0.8f) {
                        paint.color = Color.rgb(
                            (flowerRed * 0.5f).toInt(),
                            (flowerGreen * 0.5f).toInt(),
                            (flowerBlue * 0.5f).toInt()
                        )
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = 1.5f * (if (dissolveInfo?.flowersPetalsWilting == true) 1f - dissolveInfo.progress * 0.4f else 1f)
                        canvas.drawOval(
                            flower.x - size * 0.7f, flower.y - size * 0.2f,
                            flower.x - size * 0.1f, flower.y + size * 0.3f,
                            paint
                        )
                        paint.style = Paint.Style.FILL
                    }
                    
                    // Aile droite
                    paint.color = Color.rgb(
                        (flowerRed * 0.85f).toInt(),
                        (flowerGreen * 0.85f).toInt(),
                        (flowerBlue * 0.85f).toInt()
                    )
                    canvas.drawOval(
                        flower.x + size * 0.1f, flower.y - size * 0.2f,
                        flower.x + size * 0.7f, flower.y + size * 0.3f,
                        paint
                    )
                    
                    // Contour aile droite (plus faible si dissolution)
                    if (dissolveInfo == null || dissolveInfo.progress < 0.8f) {
                        paint.color = Color.rgb(
                            (flowerRed * 0.5f).toInt(),
                            (flowerGreen * 0.5f).toInt(),
                            (flowerBlue * 0.5f).toInt()
                        )
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = 1.5f * (if (dissolveInfo?.flowersPetalsWilting == true) 1f - dissolveInfo.progress * 0.4f else 1f)
                        canvas.drawOval(
                            flower.x + size * 0.1f, flower.y - size * 0.2f,
                            flower.x + size * 0.7f, flower.y + size * 0.3f,
                            paint
                        )
                        paint.style = Paint.Style.FILL
                    }
                    
                    // Carène (pétale inférieur) - forme conique pointue
                    paint.color = Color.rgb(
                        (flowerRed * 0.75f).toInt(),
                        (flowerGreen * 0.75f).toInt(),
                        (flowerBlue * 0.75f).toInt()
                    )
                    canvas.drawOval(
                        flower.x - size * 0.25f, flower.y + size * 0.1f,
                        flower.x + size * 0.25f, flower.y + size * 0.5f,
                        paint
                    )
                    
                    // Contour carène (plus faible si dissolution)
                    if (dissolveInfo == null || dissolveInfo.progress < 0.8f) {
                        paint.color = Color.rgb(
                            (flowerRed * 0.4f).toInt(),
                            (flowerGreen * 0.4f).toInt(),
                            (flowerBlue * 0.4f).toInt()
                        )
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = 1.5f * (if (dissolveInfo?.flowersPetalsWilting == true) 1f - dissolveInfo.progress * 0.4f else 1f)
                        canvas.drawOval(
                            flower.x - size * 0.25f, flower.y + size * 0.1f,
                            flower.x + size * 0.25f, flower.y + size * 0.5f,
                            paint
                        )
                        paint.style = Paint.Style.FILL
                    }
                }
            }
        }
    }
    
    // ==================== FONCTIONS UTILITAIRES ====================
    
    private fun getStemPointAtHeight(stem: LupinStem, heightRatio: Float): StemPoint? {
        if (stem.points.size < 2) return null
        
        val targetHeight = stem.currentHeight * heightRatio
        var currentHeight = 0f
        
        for (i in 1 until stem.points.size) {
            val segmentHeight = stem.points[i-1].y - stem.points[i].y
            if (currentHeight + segmentHeight >= targetHeight) {
                val segmentRatio = (targetHeight - currentHeight) / segmentHeight
                val p1 = stem.points[i-1]
                val p2 = stem.points[i]
                
                val x = p1.x + (p2.x - p1.x) * segmentRatio
                val y = p1.y + (p2.y - p1.y) * segmentRatio
                val thickness = p1.thickness + (p2.thickness - p1.thickness) * segmentRatio
                
                return StemPoint(x, y, thickness)
            }
            currentHeight += segmentHeight
        }
        
        return stem.points.lastOrNull()
    }
    
    private fun getBasalShootPointAtHeight(basalShoot: BasalShoot, heightRatio: Float): StemPoint? {
        if (basalShoot.points.size < 2) return null
        
        val targetHeight = basalShoot.currentHeight * heightRatio
        var currentHeight = 0f
        
        for (i in 1 until basalShoot.points.size) {
            val p1 = basalShoot.points[i-1]
            val p2 = basalShoot.points[i]
            val segmentHeight = sqrt(
                (p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y)
            )
            
            if (currentHeight + segmentHeight >= targetHeight) {
                val segmentRatio = (targetHeight - currentHeight) / segmentHeight
                
                val x = p1.x + (p2.x - p1.x) * segmentRatio
                val y = p1.y + (p2.y - p1.y) * segmentRatio
                val thickness = p1.thickness + (p2.thickness - p1.thickness) * segmentRatio
                
                return StemPoint(x, y, thickness)
            }
            currentHeight += segmentHeight
        }
        
        return basalShoot.points.lastOrNull()
    }
}
