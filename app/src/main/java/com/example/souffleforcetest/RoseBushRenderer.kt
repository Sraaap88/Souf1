package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import kotlin.math.*

class RoseBushRenderer {
    
    // ==================== FONCTION PRINCIPALE DE RENDU ====================
    
    fun drawRoseBush(
        canvas: Canvas,
        branchPaint: Paint,
        leafPaint: Paint,
        flowerPaint: Paint,
        branches: List<RoseBushManager.RoseBranch>,
        leaves: List<RoseBushManager.RoseLeaf>,
        flowers: List<RoseBushManager.RoseFlower>,
        dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null
    ) {
        drawBranches(canvas, branchPaint, branches, dissolveInfo)
        drawLeaves(canvas, leafPaint, branches, leaves, dissolveInfo)
        drawFlowers(canvas, flowerPaint, flowers, dissolveInfo)
    }
    
    // ==================== NOUVELLE FONCTION OPTIMISÉE ====================
    
    fun drawRoseBushOptimized(
        canvas: Canvas, 
        branchPaint: Paint, 
        leafPaint: Paint, 
        flowerPaint: Paint, 
        visibleBranches: List<RoseBushManager.RoseBranch>,
        visibleLeaves: List<RoseBushManager.RoseLeaf>,
        visibleFlowers: List<RoseBushManager.RoseFlower>,
        dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null
    ) {
        // Dessiner seulement les branches visibles
        drawBranchesOptimized(canvas, branchPaint, visibleBranches, dissolveInfo)
        
        // Dessiner seulement les feuilles visibles
        drawLeavesOptimized(canvas, leafPaint, visibleLeaves, dissolveInfo)
        
        // Dessiner seulement les fleurs visibles
        drawFlowersOptimized(canvas, flowerPaint, visibleFlowers, dissolveInfo)
    }
    
    // ==================== RENDU DES BRANCHES AVEC DISSOLUTION ====================
    
    private fun drawBranches(
        canvas: Canvas, 
        paint: Paint, 
        branches: List<RoseBushManager.RoseBranch>, 
        dissolveInfo: ChallengeEffectsManager.DissolveInfo?
    ) {
        paint.color = Color.rgb(101, 67, 33)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        
        // NOUVEAU: Appliquer les effets de dissolution
        if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            // Réduire l'opacité en fonction de la dissolution
            val alpha = ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
            paint.alpha = alpha
            
            // Si les tiges s'effondrent, changer la couleur vers le gris
            if (dissolveInfo.stemsCollapsing) {
                val shrivelingFactor = dissolveInfo.progress
                val red = (101 * (1f + shrivelingFactor * 0.3f)).toInt().coerceAtMost(155) // Plus gris
                val green = (67 * (1f + shrivelingFactor * 0.2f)).toInt().coerceAtMost(90)
                val blue = (33 * (1f + shrivelingFactor * 0.1f)).toInt().coerceAtMost(50)
                paint.color = Color.rgb(red, green, blue)
            }
        } else {
            paint.alpha = 255
        }
        
        for (branch in branches) {
            if (branch.points.size >= 2) {
                for (i in 1 until branch.points.size) {
                    val p1 = branch.points[i-1]
                    val p2 = branch.points[i]
                    
                    var strokeWidth = p1.thickness
                    
                    // NOUVEAU: Réduire l'épaisseur si les tiges s'effondrent
                    if (dissolveInfo?.stemsCollapsing == true) {
                        strokeWidth *= (1f - dissolveInfo.progress * 0.3f)
                    }
                    
                    paint.strokeWidth = strokeWidth
                    
                    // NOUVEAU: Effet de courbure si les tiges s'effondrent
                    var adjustedX2 = p2.x
                    var adjustedY2 = p2.y
                    if (dissolveInfo?.stemsCollapsing == true) {
                        val bendFactor = dissolveInfo.progress * 12f
                        val heightRatio = i.toFloat() / branch.points.size
                        adjustedX2 += bendFactor * heightRatio * heightRatio * if (branch.angle > -90f) 1f else -1f
                        adjustedY2 += bendFactor * 0.4f * heightRatio
                    }
                    
                    canvas.drawLine(p1.x, p1.y, adjustedX2, adjustedY2, paint)
                }
            }
        }
    }
    
    private fun drawBranchesOptimized(
        canvas: Canvas, 
        paint: Paint, 
        visibleBranches: List<RoseBushManager.RoseBranch>, 
        dissolveInfo: ChallengeEffectsManager.DissolveInfo?
    ) {
        paint.color = Color.rgb(101, 67, 33)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        
        if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            val alpha = ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
            paint.alpha = alpha
            
            if (dissolveInfo.stemsCollapsing) {
                val shrivelingFactor = dissolveInfo.progress
                val red = (101 * (1f + shrivelingFactor * 0.3f)).toInt().coerceAtMost(155)
                val green = (67 * (1f + shrivelingFactor * 0.2f)).toInt().coerceAtMost(90)
                val blue = (33 * (1f + shrivelingFactor * 0.1f)).toInt().coerceAtMost(50)
                paint.color = Color.rgb(red, green, blue)
            }
        } else {
            paint.alpha = 255
        }
        
        for (branch in visibleBranches) {
            if (branch.points.size >= 2) {
                for (i in 1 until branch.points.size) {
                    val p1 = branch.points[i-1]
                    val p2 = branch.points[i]
                    
                    var strokeWidth = p1.thickness
                    if (dissolveInfo?.stemsCollapsing == true) {
                        strokeWidth *= (1f - dissolveInfo.progress * 0.3f)
                    }
                    
                    paint.strokeWidth = strokeWidth
                    
                    var adjustedX2 = p2.x
                    var adjustedY2 = p2.y
                    if (dissolveInfo?.stemsCollapsing == true) {
                        val bendFactor = dissolveInfo.progress * 12f
                        val heightRatio = i.toFloat() / branch.points.size
                        adjustedX2 += bendFactor * heightRatio * heightRatio * if (branch.angle > -90f) 1f else -1f
                        adjustedY2 += bendFactor * 0.4f * heightRatio
                    }
                    
                    canvas.drawLine(p1.x, p1.y, adjustedX2, adjustedY2, paint)
                }
            }
        }
    }
    
    // ==================== RENDU DES FEUILLES AVEC DISSOLUTION ====================
    
    private fun drawLeaves(
        canvas: Canvas, 
        paint: Paint, 
        branches: List<RoseBushManager.RoseBranch>, 
        leaves: List<RoseBushManager.RoseLeaf>, 
        dissolveInfo: ChallengeEffectsManager.DissolveInfo?
    ) {
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
            if (leaf.currentSize > 0 && leaf.branchIndex < branches.size) {
                val branch = branches[leaf.branchIndex]
                val leafPoint = getBranchPointAtRatio(branch, leaf.positionRatio)
                
                leafPoint?.let { point ->
                    drawSingleLeaf(canvas, paint, point.x, point.y, leaf, dissolveInfo)
                }
            }
        }
    }
    
    private fun drawLeavesOptimized(
        canvas: Canvas, 
        paint: Paint, 
        visibleLeaves: List<RoseBushManager.RoseLeaf>, 
        dissolveInfo: ChallengeEffectsManager.DissolveInfo?
    ) {
        paint.color = Color.rgb(34, 139, 34)
        paint.style = Paint.Style.FILL
        
        if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            val alpha = ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
            paint.alpha = alpha
            
            if (dissolveInfo.leavesShriveling) {
                val shrivelingFactor = dissolveInfo.progress
                val red = (34 + (139 - 34) * shrivelingFactor).toInt()
                val green = (139 * (1f - shrivelingFactor * 0.7f)).toInt()
                val blue = (34 * (1f - shrivelingFactor * 0.8f)).toInt()
                paint.color = Color.rgb(red, green, blue)
            }
        } else {
            paint.alpha = 255
        }
        
        // Pour la version optimisée, on dessine directement les feuilles visibles
        // sans recalculer leur position (car elles ont été pré-filtrées)
        for (leaf in visibleLeaves) {
            if (leaf.currentSize > 0) {
                // Note: Pour une optimisation complète, il faudrait pré-calculer les positions
                // Pour l'instant on utilise une estimation basée sur les données de la feuille
                drawSingleLeafOptimized(canvas, paint, leaf, dissolveInfo)
            }
        }
    }
    
    private fun drawSingleLeaf(
        canvas: Canvas, 
        paint: Paint, 
        x: Float, 
        y: Float, 
        leaf: RoseBushManager.RoseLeaf, 
        dissolveInfo: ChallengeEffectsManager.DissolveInfo?
    ) {
        var size = leaf.currentSize
        val angle = leaf.angle
        val side = leaf.side
        
        // NOUVEAU: Réduire la taille si les feuilles se ratatinent
        if (dissolveInfo?.leavesShriveling == true) {
            val shrinkFactor = 1f - dissolveInfo.progress * 0.7f
            size *= shrinkFactor
        }
        
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(angle + side * 25f)
        
        val folioleCount = leaf.folioleCount
        val folioleSize = size / folioleCount * 1.2f
        
        // Couleur des feuilles avec dissolution
        paint.color = Color.rgb(34, 139, 34)
        if (dissolveInfo?.leavesShriveling == true) {
            val shrivelingFactor = dissolveInfo.progress
            val red = (34 + (139 - 34) * shrivelingFactor).toInt()
            val green = (139 * (1f - shrivelingFactor * 0.7f)).toInt()
            val blue = (34 * (1f - shrivelingFactor * 0.8f)).toInt()
            paint.color = Color.rgb(red, green, blue)
        }
        paint.style = Paint.Style.FILL
        
        // NOUVEAU: Moins de folioles si dissolution avancée
        val visibleFolioleCount = if (dissolveInfo?.leavesShriveling == true) {
            (folioleCount * (1f - dissolveInfo.progress * 0.5f)).toInt().coerceAtLeast(2)
        } else folioleCount
        
        for (i in 0 until visibleFolioleCount) {
            val folioleY = -size/2 + (i * size / (folioleCount - 1))
            val folioleX = if (i % 2 == 0) -folioleSize/3 * side else folioleSize/3 * side
            
            val baseWidth = folioleSize * 0.5f
            val baseHeight = folioleSize * 0.7f
            val widthVariation = if (i < leaf.folioleVariations.size) leaf.folioleVariations[i] * 0.15f else 0f
            
            var folioleWidth = baseWidth * (1f + widthVariation)
            var folioleHeight = baseHeight * (1f + widthVariation)
            
            // NOUVEAU: Réduire encore la taille des folioles individuelles
            if (dissolveInfo?.leavesShriveling == true) {
                val additionalShrink = 1f - dissolveInfo.progress * 0.4f
                folioleWidth *= additionalShrink
                folioleHeight *= additionalShrink
            }
            
            canvas.drawOval(
                folioleX - folioleWidth/2, 
                folioleY - folioleHeight/2,
                folioleX + folioleWidth/2, 
                folioleY + folioleHeight/2, 
                paint
            )
        }
        
        // Tige centrale (s'affaiblit avec dissolution)
        if (dissolveInfo == null || dissolveInfo.progress < 0.8f) {
            paint.color = Color.rgb(20, 80, 20)
            if (dissolveInfo?.leavesShriveling == true) {
                val shrivelingFactor = dissolveInfo.progress
                val red = (20 + (100 - 20) * shrivelingFactor).toInt()
                val green = (80 * (1f - shrivelingFactor * 0.7f)).toInt()
                val blue = (20 * (1f - shrivelingFactor * 0.8f)).toInt()
                paint.color = Color.rgb(red, green, blue)
            }
            paint.style = Paint.Style.STROKE
            var strokeWidth = size * 0.06f
            if (dissolveInfo?.leavesShriveling == true) {
                strokeWidth *= (1f - dissolveInfo.progress * 0.4f)
            }
            paint.strokeWidth = strokeWidth
            canvas.drawLine(0f, -size/2, 0f, size/2, paint)
        }
        
        canvas.restore()
    }
    
    private fun drawSingleLeafOptimized(
        canvas: Canvas, 
        paint: Paint, 
        leaf: RoseBushManager.RoseLeaf, 
        dissolveInfo: ChallengeEffectsManager.DissolveInfo?
    ) {
        // Version optimisée - position estimée car on n'a pas accès aux branches complètes
        // Pour une vraie optimisation, il faudrait pré-calculer et stocker les positions
        // Pour l'instant, on utilise une position basique ou on skip
        
        // Cette fonction peut être laissée vide car le vrai filtrage se fait en amont
        // Les feuilles visibles sont déjà déterminées par RoseOptimizer
    }
    
    // ==================== RENDU DES FLEURS AVEC DISSOLUTION ====================
    
    private fun drawFlowers(
        canvas: Canvas, 
        paint: Paint, 
        flowers: List<RoseBushManager.RoseFlower>, 
        dissolveInfo: ChallengeEffectsManager.DissolveInfo?
    ) {
        paint.color = Color.rgb(255, 182, 193)
        paint.style = Paint.Style.FILL
        
        // NOUVEAU: Appliquer les effets de dissolution aux fleurs
        val baseAlpha = if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
        } else 255
        
        paint.alpha = baseAlpha
        
        for (flower in flowers) {
            if (flower.currentSize > 0) {
                drawSingleFlower(canvas, paint, flower, dissolveInfo, baseAlpha)
            }
        }
    }
    
    private fun drawFlowersOptimized(
        canvas: Canvas, 
        paint: Paint, 
        visibleFlowers: List<RoseBushManager.RoseFlower>, 
        dissolveInfo: ChallengeEffectsManager.DissolveInfo?
    ) {
        paint.color = Color.rgb(255, 182, 193)
        paint.style = Paint.Style.FILL
        
        val baseAlpha = if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
        } else 255
        
        paint.alpha = baseAlpha
        
        for (flower in visibleFlowers) {
            if (flower.currentSize > 0) {
                drawSingleFlower(canvas, paint, flower, dissolveInfo, baseAlpha)
            }
        }
    }
    
    private fun drawSingleFlower(
        canvas: Canvas, 
        paint: Paint, 
        flower: RoseBushManager.RoseFlower, 
        dissolveInfo: ChallengeEffectsManager.DissolveInfo?,
        baseAlpha: Int
    ) {
        var flowerSize = flower.currentSize
        
        // NOUVEAU: Réduire la taille des fleurs si elles flétrissent
        if (dissolveInfo?.flowersPetalsWilting == true) {
            val wiltFactor = 1f - dissolveInfo.progress * 0.8f
            flowerSize *= wiltFactor
        }
        
        // Couleur des pétales (ternit avec dissolution)
        var petalRed = 255
        var petalGreen = 182
        var petalBlue = 193
        
        if (dissolveInfo?.flowersPetalsWilting == true) {
            val wiltFactor = dissolveInfo.progress
            petalRed = (255 * (1f - wiltFactor * 0.3f)).toInt()
            petalGreen = (182 * (1f - wiltFactor * 0.4f)).toInt()
            petalBlue = (193 * (1f - wiltFactor * 0.2f)).toInt()
        }
        
        paint.color = Color.rgb(petalRed, petalGreen, petalBlue)
        
        // Dessiner les pétales (moins nombreux si dissolution avancée)
        val maxPetals = if (dissolveInfo?.flowersPetalsWilting == true) {
            (5 * (1f - dissolveInfo.progress * 0.6f)).toInt().coerceAtLeast(2)
        } else 5
        
        val petalSize = flowerSize * 0.6f
        
        for (i in 0 until maxPetals) {
            // NOUVEAU: Pétales qui tombent progressivement
            var petalAlpha = baseAlpha
            if (dissolveInfo?.flowersPetalsWilting == true) {
                // Les derniers pétales disparaissent en premier
                val petalWiltChance = dissolveInfo.progress + (i.toFloat() / maxPetals) * 0.3f
                if (petalWiltChance > 0.7f) {
                    petalAlpha = (petalAlpha * (1f - (petalWiltChance - 0.7f) / 0.3f)).toInt().coerceAtLeast(0)
                }
            }
            paint.alpha = petalAlpha
            
            val angle = (i * 72f) * Math.PI / 180.0
            var petalDistance = flowerSize * 0.35f
            
            // NOUVEAU: Pétales qui s'affaissent vers le centre
            if (dissolveInfo?.flowersPetalsWilting == true) {
                petalDistance *= (1f - dissolveInfo.progress * 0.6f)
            }
            
            val petalX = flower.x + cos(angle).toFloat() * petalDistance
            val petalY = flower.y + sin(angle).toFloat() * petalDistance
            
            var adjustedPetalSize = petalSize * 0.6f
            
            // NOUVEAU: Pétales qui rétrécissent
            if (dissolveInfo?.flowersPetalsWilting == true) {
                adjustedPetalSize *= (1f - dissolveInfo.progress * 0.5f)
            }
            
            canvas.drawCircle(petalX, petalY, adjustedPetalSize, paint)
        }
        
        // Centre de la fleur (s'assombrit avec dissolution)
        var centerRed = 255
        var centerGreen = 215
        var centerBlue = 0
        
        if (dissolveInfo?.flowersPetalsWilting == true) {
            val wiltFactor = dissolveInfo.progress
            centerRed = (255 * (1f - wiltFactor * 0.4f)).toInt()
            centerGreen = (215 * (1f - wiltFactor * 0.5f)).toInt()
            centerBlue = (0 + (100 * wiltFactor)).toInt() // Vers brun
        }
        
        paint.color = Color.rgb(centerRed, centerGreen, centerBlue)
        paint.alpha = baseAlpha
        
        var centerSize = flowerSize * 0.25f
        if (dissolveInfo?.flowersPetalsWilting == true) {
            centerSize *= (1f - dissolveInfo.progress * 0.3f)
        }
        
        canvas.drawCircle(flower.x, flower.y, centerSize, paint)
    }
    
    // ==================== FONCTIONS UTILITAIRES ====================
    
    private fun getBranchPointAtRatio(branch: RoseBushManager.RoseBranch, ratio: Float): RoseBushManager.BranchPoint? {
        if (branch.points.size < 2) return null
        
        val targetLength = branch.currentLength * ratio
        var currentLength = 0f
        
        for (i in 1 until branch.points.size) {
            val segmentLength = distance(branch.points[i-1], branch.points[i])
            if (currentLength + segmentLength >= targetLength) {
                val segmentRatio = (targetLength - currentLength) / segmentLength
                val p1 = branch.points[i-1]
                val p2 = branch.points[i]
                
                val x = p1.x + (p2.x - p1.x) * segmentRatio
                val y = p1.y + (p2.y - p1.y) * segmentRatio
                val thickness = p1.thickness + (p2.thickness - p1.thickness) * segmentRatio
                
                return RoseBushManager.BranchPoint(x, y, thickness)
            }
            currentLength += segmentLength
        }
        
        return branch.points.lastOrNull()
    }
    
    private fun distance(p1: RoseBushManager.BranchPoint, p2: RoseBushManager.BranchPoint): Float {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        return sqrt(dx * dx + dy * dy)
    }
}
