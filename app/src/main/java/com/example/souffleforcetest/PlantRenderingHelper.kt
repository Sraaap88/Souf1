package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color

class PlantRenderingHelper(private val screenWidth: Int, private val screenHeight: Int) {
    
    // ==================== PAINTS SPÉCIALISÉS ====================
    
    private val stemPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.rgb(50, 120, 50)
    }
    
    private val branchPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.rgb(40, 100, 40)
    }
    
    private val leafPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.rgb(34, 139, 34)
    }
    
    private val flowerPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.WHITE
    }
    
    private val flowerCenterPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.rgb(255, 200, 50)
    }
    
    // Paint pour la zone cible des défis
    private val targetZonePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = 0x4000FF00.toInt()  // Vert lime transparent (40% opacité)
    }
    
    // ==================== RENDU DES PLANTES AVEC DISSOLUTION ====================
    
    fun drawMainStem(canvas: Canvas, mainStem: List<PlantStem.StemPoint>, dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null) {
        if (mainStem.size < 2) return
        
        stemPaint.color = Color.rgb(50, 120, 50)
        
        // NOUVEAU: Appliquer les effets de dissolution
        if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            // Réduire l'opacité en fonction de la dissolution
            val alpha = ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
            stemPaint.alpha = alpha
            
            // Si les tiges s'effondrent, changer la couleur vers le brun
            if (dissolveInfo.stemsCollapsing) {
                val shrivelingFactor = dissolveInfo.progress
                val red = (50 + (139 - 50) * shrivelingFactor).toInt() // Vers brun
                val green = (120 * (1f - shrivelingFactor * 0.6f)).toInt()
                val blue = (50 * (1f - shrivelingFactor * 0.8f)).toInt()
                stemPaint.color = Color.rgb(red, green, blue)
            }
        } else {
            stemPaint.alpha = 255
        }
        
        for (i in 1 until mainStem.size) {
            val point = mainStem[i]
            val prevPoint = mainStem[i - 1]
            
            var strokeWidth = point.thickness
            
            // NOUVEAU: Réduire l'épaisseur si les tiges s'effondrent
            if (dissolveInfo?.stemsCollapsing == true) {
                strokeWidth *= (1f - dissolveInfo.progress * 0.4f)
            }
            
            stemPaint.strokeWidth = strokeWidth
            
            val adjustedX = point.x + point.oscillation + point.permanentWave
            val prevAdjustedX = prevPoint.x + prevPoint.oscillation + prevPoint.permanentWave
            
            // NOUVEAU: Effet de courbure si les tiges s'effondrent
            var finalAdjustedX = adjustedX
            var finalPointY = point.y
            if (dissolveInfo?.stemsCollapsing == true) {
                val bendFactor = dissolveInfo.progress * 15f
                val heightRatio = i.toFloat() / mainStem.size
                finalAdjustedX += bendFactor * heightRatio * heightRatio
                finalPointY += bendFactor * 0.3f * heightRatio
            }
            
            if (i == 1) {
                canvas.drawLine(prevAdjustedX, prevPoint.y, finalAdjustedX, finalPointY, stemPaint)
            } else {
                val controlX = (prevAdjustedX + finalAdjustedX) / 2f
                val controlY = (prevPoint.y + finalPointY) / 2f
                val curvatureOffset = (finalAdjustedX - prevAdjustedX) * 0.3f
                val finalControlX = controlX + curvatureOffset
                
                canvas.drawLine(prevAdjustedX, prevPoint.y, finalControlX, controlY, stemPaint)
                canvas.drawLine(finalControlX, controlY, finalAdjustedX, finalPointY, stemPaint)
            }
        }
    }
    
    fun drawBranches(canvas: Canvas, branches: List<PlantStem.Branch>, dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null) {
        branchPaint.color = Color.rgb(40, 100, 40)
        
        // NOUVEAU: Appliquer les effets de dissolution aux branches
        if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            val alpha = ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
            branchPaint.alpha = alpha
            
            // Changement de couleur vers le brun
            if (dissolveInfo.stemsCollapsing) {
                val shrivelingFactor = dissolveInfo.progress
                val red = (40 + (139 - 40) * shrivelingFactor).toInt()
                val green = (100 * (1f - shrivelingFactor * 0.6f)).toInt()
                val blue = (40 * (1f - shrivelingFactor * 0.8f)).toInt()
                branchPaint.color = Color.rgb(red, green, blue)
            }
        } else {
            branchPaint.alpha = 255
        }
        
        for (branch in branches.filter { it.isActive }) {
            if (branch.points.size >= 2) {
                for (i in 1 until branch.points.size) {
                    val point = branch.points[i]
                    val prevPoint = branch.points[i - 1]
                    
                    var strokeWidth = point.thickness
                    
                    // NOUVEAU: Réduire l'épaisseur si dissolution
                    if (dissolveInfo?.stemsCollapsing == true) {
                        strokeWidth *= (1f - dissolveInfo.progress * 0.5f) // Branches plus fragiles
                    }
                    
                    branchPaint.strokeWidth = strokeWidth
                    
                    // NOUVEAU: Effet de courbure plus prononcé sur les branches
                    var finalX = point.x
                    var finalY = point.y
                    if (dissolveInfo?.stemsCollapsing == true) {
                        val bendFactor = dissolveInfo.progress * 20f // Plus fort que tige principale
                        val heightRatio = i.toFloat() / branch.points.size
                        val branchDirection = if (branch.angle < 0) -1f else 1f
                        finalX += bendFactor * heightRatio * branchDirection
                        finalY += bendFactor * 0.4f * heightRatio
                    }
                    
                    if (i == 1 || branch.points.size <= 2) {
                        canvas.drawLine(prevPoint.x, prevPoint.y, finalX, finalY, branchPaint)
                    } else {
                        val controlX = (prevPoint.x + finalX) / 2f
                        val controlY = (prevPoint.y + finalY) / 2f
                        canvas.drawLine(prevPoint.x, prevPoint.y, controlX, controlY, branchPaint)
                        canvas.drawLine(controlX, controlY, finalX, finalY, branchPaint)
                    }
                }
            }
        }
    }
    
    fun drawLeaves(canvas: Canvas, leaves: List<PlantLeavesManager.Leaf>, stem: PlantStem, dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null) {
        // NOUVEAU: Appliquer les effets de dissolution aux feuilles
        if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            val alpha = ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
            leafPaint.alpha = alpha
        } else {
            leafPaint.alpha = 255
        }
        
        for (leaf in leaves) {
            if (leaf.currentSize > 0) {
                // Couleur des feuilles (affectée par la dissolution)
                var leafColor = stem.getLeavesManager().getLeafColor(leaf)
                
                if (dissolveInfo?.leavesShriveling == true) {
                    val shrivelingFactor = dissolveInfo.progress
                    val originalRed = Color.red(leafColor)
                    val originalGreen = Color.green(leafColor)
                    val originalBlue = Color.blue(leafColor)
                    
                    val red = (originalRed + (139 - originalRed) * shrivelingFactor).toInt()
                    val green = (originalGreen * (1f - shrivelingFactor * 0.7f)).toInt()
                    val blue = (originalBlue * (1f - shrivelingFactor * 0.8f)).toInt()
                    leafColor = Color.rgb(red, green, blue)
                }
                
                leafPaint.color = leafColor
                
                // Créer le chemin de la feuille avec effets de dissolution
                val leafPath = createLeafPathWithDissolution(leaf, stem, dissolveInfo)
                canvas.drawPath(leafPath, leafPaint)
                
                // Nervure centrale (s'affaiblit avec dissolution)
                if ((dissolveInfo == null || dissolveInfo.progress < 0.7f) && leaf.currentSize > leaf.maxSize * 0.7f) {
                    leafPaint.style = Paint.Style.STROKE
                    
                    var strokeWidth = 1.5f
                    if (dissolveInfo?.leavesShriveling == true) {
                        strokeWidth *= (1f - dissolveInfo.progress * 0.5f)
                        val alpha = ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
                        leafPaint.alpha = alpha
                    }
                    leafPaint.strokeWidth = strokeWidth
                    leafPaint.color = Color.rgb(20, 80, 20)
                    
                    canvas.drawPath(leafPath, leafPaint)
                    leafPaint.style = Paint.Style.FILL
                }
            }
        }
    }
    
    private fun createLeafPathWithDissolution(
        leaf: PlantLeavesManager.Leaf, 
        stem: PlantStem, 
        dissolveInfo: ChallengeEffectsManager.DissolveInfo?
    ): android.graphics.Path {
        // Utiliser la méthode existante mais avec une taille ajustée
        var adjustedLeaf = leaf
        
        if (dissolveInfo?.leavesShriveling == true) {
            val shrinkFactor = 1f - dissolveInfo.progress * 0.8f
            adjustedLeaf = leaf.copy(currentSize = leaf.currentSize * shrinkFactor)
        }
        
        return stem.getLeavesManager().createLeafPath(adjustedLeaf)
    }
    
    fun drawBackgroundFlowers(canvas: Canvas, flowers: List<FlowerManager.Flower>, stem: PlantStem, dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null) {
        val backgroundFlowers = flowers.filter { it.perspective.viewAngle > 60f }
        if (backgroundFlowers.isNotEmpty()) {
            // NOUVEAU: Passer dissolveInfo au FlowerManager
            stem.getFlowerManager().drawSpecificFlowersWithDissolution(canvas, backgroundFlowers, flowerPaint, flowerCenterPaint, dissolveInfo)
        }
    }
    
    fun drawForegroundFlowers(canvas: Canvas, flowers: List<FlowerManager.Flower>, stem: PlantStem, dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null) {
        val foregroundFlowers = flowers.filter { it.perspective.viewAngle <= 60f }
        if (foregroundFlowers.isNotEmpty()) {
            // NOUVEAU: Passer dissolveInfo au FlowerManager
            stem.getFlowerManager().drawSpecificFlowersWithDissolution(canvas, foregroundFlowers, flowerPaint, flowerCenterPaint, dissolveInfo)
        }
    }
    
    // ==================== NOUVELLES MÉTHODES DE RENDU AVEC DISSOLUTION ====================
    
    fun drawMargueriteWithDissolution(
        canvas: Canvas, 
        flower: FlowerManager.Flower, 
        dissolveInfo: ChallengeEffectsManager.DissolveInfo?
    ) {
        var flowerSize = flower.currentSize
        
        // NOUVEAU: Réduire la taille des fleurs si elles flétrissent
        if (dissolveInfo?.flowersPetalsWilting == true) {
            val wiltFactor = 1f - dissolveInfo.progress * 0.9f
            flowerSize *= wiltFactor
        }
        
        // NOUVEAU: Appliquer les effets de dissolution
        val baseAlpha = if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
        } else 255
        
        canvas.save()
        canvas.translate(flower.x, flower.y)
        
        // Couleur des pétales (ternit avec dissolution)
        var petalRed = 255
        var petalGreen = 255
        var petalBlue = 255
        
        if (dissolveInfo?.flowersPetalsWilting == true) {
            val wiltFactor = dissolveInfo.progress
            petalRed = (255 * (1f - wiltFactor * 0.3f)).toInt()
            petalGreen = (255 * (1f - wiltFactor * 0.4f)).toInt()
            petalBlue = (255 * (1f - wiltFactor * 0.2f)).toInt()
        }
        
        // Dessiner les pétales (moins nombreux si dissolution avancée)
        val maxPetals = if (dissolveInfo?.flowersPetalsWilting == true) {
            (8 * (1f - dissolveInfo.progress * 0.6f)).toInt().coerceAtLeast(3)
        } else 8
        
        flowerPaint.color = Color.rgb(petalRed, petalGreen, petalBlue)
        flowerPaint.style = Paint.Style.FILL
        flowerPaint.alpha = baseAlpha
        
        for (i in 0 until maxPetals) {
            val angle = (i * 360f / maxPetals) * Math.PI.toFloat() / 180f
            val petalLength = flowerSize * 0.6f
            val petalWidth = flowerSize * 0.15f
            
            // NOUVEAU: Pétales qui tombent progressivement
            var petalAlpha = baseAlpha
            if (dissolveInfo?.flowersPetalsWilting == true) {
                // Les derniers pétales disparaissent en premier
                val petalWiltChance = dissolveInfo.progress + (i.toFloat() / maxPetals) * 0.3f
                if (petalWiltChance > 0.7f) {
                    petalAlpha = (petalAlpha * (1f - (petalWiltChance - 0.7f) / 0.3f)).toInt().coerceAtLeast(0)
                }
            }
            flowerPaint.alpha = petalAlpha
            
            canvas.save()
            canvas.rotate(angle * 180f / Math.PI.toFloat())
            
            // NOUVEAU: Pétales qui s'affaissent
            var adjustedLength = petalLength
            if (dissolveInfo?.flowersPetalsWilting == true) {
                adjustedLength *= (1f - dissolveInfo.progress * 0.5f)
            }
            
            canvas.drawOval(
                -petalWidth / 2f, flowerSize * 0.1f,
                petalWidth / 2f, flowerSize * 0.1f + adjustedLength,
                flowerPaint
            )
            
            canvas.restore()
        }
        
        // Centre de la fleur (s'assombrit avec dissolution)
        var centerRed = 255
        var centerGreen = 255
        var centerBlue = 0
        
        if (dissolveInfo?.flowersPetalsWilting == true) {
            val wiltFactor = dissolveInfo.progress
            centerRed = (255 * (1f - wiltFactor * 0.5f)).toInt()
            centerGreen = (255 * (1f - wiltFactor * 0.6f)).toInt()
            centerBlue = (0 + (100 * wiltFactor)).toInt() // Vers brun
        }
        
        flowerCenterPaint.color = Color.rgb(centerRed, centerGreen, centerBlue)
        flowerCenterPaint.alpha = baseAlpha
        
        var centerSize = flowerSize * 0.2f
        if (dissolveInfo?.flowersPetalsWilting == true) {
            centerSize *= (1f - dissolveInfo.progress * 0.3f)
        }
        
        canvas.drawCircle(0f, 0f, centerSize, flowerCenterPaint)
        
        canvas.restore()
    }
    
    // ==================== ZONES CIBLES POUR DÉFIS ====================
    
    fun drawTargetZone(canvas: Canvas, challengeManager: ChallengeManager, challengeId: Int = 1) {
        val currentFlowerType = detectCurrentFlowerType(challengeManager)
        
        val zoneTop: Float
        val zoneBottom: Float
        
        if (currentFlowerType == "MARGUERITE") {
            // Zones existantes pour marguerite
            when (challengeId) {
                1 -> {
                    zoneTop = screenHeight / 3f - 60f
                    zoneBottom = screenHeight / 3f + 360f
                }
                3 -> {
                    zoneTop = screenHeight / 3f - 120f
                    zoneBottom = screenHeight / 3f + 120f
                }
                else -> {
                    zoneTop = screenHeight / 3f - 60f
                    zoneBottom = screenHeight / 3f + 360f
                }
            }
        } else {
            // Zones pour rosier, lupin et iris - bande de 2 pouces (~192px) au centre
            val zoneHeight = 192f  // 2 pouces
            zoneTop = (screenHeight - zoneHeight) / 2f
            zoneBottom = zoneTop + zoneHeight
        }
        
        val zoneLeft = 0f
        val zoneRight = screenWidth.toFloat()
        
        // Dessiner le rectangle transparent vert lime
        canvas.drawRect(zoneLeft, zoneTop, zoneRight, zoneBottom, targetZonePaint)
        
        // Bordures pour mieux voir la zone
        val borderPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeWidth = 4f
            color = 0x8000FF00.toInt()  // Vert lime plus opaque pour les bordures
        }
        canvas.drawRect(zoneLeft, zoneTop, zoneRight, zoneBottom, borderPaint)
    }
    
    fun shouldShowTargetZone(lightState: OrganicLineView.LightState): Boolean {
        // Afficher la zone pendant les phases de croissance seulement
        return when (lightState) {
            OrganicLineView.LightState.GREEN_GROW,
            OrganicLineView.LightState.GREEN_LEAVES,
            OrganicLineView.LightState.GREEN_FLOWER -> true
            else -> false
        }
    }
    
    // Fonction utilitaire pour vérifier si un point est dans la zone cible (marguerite)
    fun isPointInMargueriteTargetZone(x: Float, y: Float): Boolean {
        val zoneTop = screenHeight / 3f - 60f
        val zoneBottom = screenHeight / 3f + 360f  // Zone élargie à 360px vers le bas
        val zoneLeft = 0f
        val zoneRight = screenWidth.toFloat()
        
        return x >= zoneLeft && x <= zoneRight && y >= zoneTop && y <= zoneBottom
    }
    
    // ==================== FONCTIONS UTILITAIRES ====================
    
    private fun detectCurrentFlowerType(challengeManager: ChallengeManager): String {
        val currentChallenge = challengeManager.getCurrentChallenge()
        
        return if (currentChallenge != null) {
            when {
                challengeManager.getMargueriteChallenges().any { it == currentChallenge } -> "MARGUERITE"
                challengeManager.getRoseChallenges().any { it == currentChallenge } -> "ROSIER"
                challengeManager.getLupinChallenges().any { it == currentChallenge } -> "LUPIN"
                challengeManager.getIrisChallenges().any { it == currentChallenge } -> "IRIS"
                else -> "MARGUERITE"
            }
        } else {
            // Fallback: détecter selon les fleurs débloquées
            when {
                challengeManager.isFlowerUnlocked("IRIS") -> "IRIS"
                challengeManager.isFlowerUnlocked("LUPIN") -> "LUPIN"
                challengeManager.isFlowerUnlocked("ROSE") -> "ROSIER"  
                else -> "MARGUERITE"
            }
        }
    }
    
    // ==================== FONCTIONS D'ACCÈS AUX PAINTS ====================
    
    fun getStemPaint(): Paint = stemPaint
    fun getBranchPaint(): Paint = branchPaint
    fun getLeafPaint(): Paint = leafPaint
    fun getFlowerPaint(): Paint = flowerPaint
    fun getFlowerCenterPaint(): Paint = flowerCenterPaint
}
