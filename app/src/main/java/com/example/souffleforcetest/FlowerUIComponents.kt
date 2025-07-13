package com.example.souffleforcetest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix

class FlowerUIComponents(private val context: Context, private val screenWidth: Int, private val screenHeight: Int) {
    
    // ==================== PAINTS SPÉCIALISÉS ====================
    
    private val flowerTextPaint = Paint().apply {
        color = 0xFFFFFFFF.toInt()
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    
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
    
    // ==================== IMAGE RESOURCES ====================
    
    private var daisyBitmap: Bitmap? = null
    
    init {
        // Charger l'image de marguerite
        try {
            daisyBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.marguerite)
        } catch (e: Exception) {
            // Si l'image n'est pas trouvée, on garde daisyBitmap = null
        }
    }
    
    // ==================== SÉLECTION DES FLEURS ====================
    
    fun drawFlowerChoice(canvas: Canvas, challengeManager: ChallengeManager) {
        // Titre
        flowerTextPaint.textSize = 150f
        flowerTextPaint.color = 0xFFFFFFFF.toInt()
        flowerTextPaint.isFakeBoldText = true
        canvas.drawText("CHOISIR FLEUR", screenWidth / 2f, screenHeight * 0.25f, flowerTextPaint)
        
        // Obtenir les fleurs débloquées
        val unlockedFlowers = getUnlockedFlowersList(challengeManager)
        val flowerButtonRadius = screenWidth * 0.18f  // AUGMENTÉ de 0.12f à 0.18f pour plus grandes images
        val centerX = screenWidth / 2f
        val buttonY = screenHeight / 2f
        
        when (unlockedFlowers.size) {
            1 -> {
                // Seulement marguerite - centrée
                drawFlowerButton(canvas, centerX, buttonY, flowerButtonRadius, "MARGUERITE", challengeManager)
            }
            2 -> {
                // Marguerite + Rose - côte à côte
                val spacing = flowerButtonRadius * 3.5f
                val margueriteX = centerX - spacing / 2f
                val roseX = centerX + spacing / 2f
                
                drawFlowerButton(canvas, margueriteX, buttonY, flowerButtonRadius, "MARGUERITE", challengeManager)
                drawFlowerButton(canvas, roseX, buttonY, flowerButtonRadius, "ROSE", challengeManager)
            }
            3 -> {
                // Marguerite + Rose + Lupin - en triangle, ajusté pour images plus grandes
                val spacing = flowerButtonRadius * 3.0f
                val topY = buttonY - spacing * 0.4f
                val bottomY = buttonY + spacing * 0.4f
                
                // Marguerite en haut au centre
                drawFlowerButton(canvas, centerX, topY, flowerButtonRadius, "MARGUERITE", challengeManager)
                
                // Rose en bas à gauche
                val roseX = centerX - spacing / 2f
                drawFlowerButton(canvas, roseX, bottomY, flowerButtonRadius, "ROSE", challengeManager)
                
                // Lupin en bas à droite
                val lupinX = centerX + spacing / 2f
                drawFlowerButton(canvas, lupinX, bottomY, flowerButtonRadius, "LUPIN", challengeManager)
            }
            else -> {
                // 4+ fleurs - en carré ou plus (pour futures fleurs)
                val spacing = flowerButtonRadius * 3f
                val positions = listOf(
                    Pair(centerX - spacing / 2f, buttonY - spacing / 2f), // Haut gauche
                    Pair(centerX + spacing / 2f, buttonY - spacing / 2f), // Haut droite  
                    Pair(centerX - spacing / 2f, buttonY + spacing / 2f), // Bas gauche
                    Pair(centerX + spacing / 2f, buttonY + spacing / 2f)  // Bas droite
                )
                
                val flowerTypes = listOf("MARGUERITE", "ROSE", "LUPIN", "IRIS")
                for (i in unlockedFlowers.indices.take(4)) {
                    val (x, y) = positions[i]
                    drawFlowerButton(canvas, x, y, flowerButtonRadius * 0.9f, flowerTypes[i], challengeManager)
                }
            }
        }
    }
    
    private fun drawFlowerButton(canvas: Canvas, x: Float, y: Float, radius: Float, flowerType: String, challengeManager: ChallengeManager) {
        val isUnlocked = challengeManager.isFlowerUnlocked(flowerType)
        
        when (flowerType) {
            "MARGUERITE" -> {
                // Toujours débloquée
                drawMiniDaisy(canvas, x, y, radius * 1.5f)
            }
            "ROSE" -> {
                if (isUnlocked) {
                    // Rose débloquée
                    flowerTextPaint.textSize = radius * 1.6f
                    flowerTextPaint.color = 0xFFFF69B4.toInt()  // Rose
                    canvas.drawText("🌹", x, y + 15f, flowerTextPaint)
                } else {
                    // Rose verrouillée
                    drawLockedFlower(canvas, x, y, radius, "VERROUILLÉ")
                }
            }
            "LUPIN" -> {
                if (isUnlocked) {
                    // Lupin débloqué - MEILLEURE REPRÉSENTATION
                    flowerTextPaint.textSize = radius * 1.4f
                    flowerTextPaint.color = 0xFF9370DB.toInt()  // Violet (couleur typique du lupin)
                    
                    // Dessiner plusieurs petits points pour simuler l'épi
                    val spikeHeight = radius * 1.2f
                    val pointCount = 8
                    for (i in 0 until pointCount) {
                        val pointY = y - spikeHeight/2f + (i * spikeHeight / pointCount)
                        val pointSize = radius * 0.15f * (1f - (i.toFloat() / pointCount) * 0.3f) // Plus petit vers le haut
                        
                        flowerTextPaint.style = Paint.Style.FILL
                        canvas.drawCircle(x, pointY, pointSize, flowerTextPaint)
                    }
                    
                    // Tige
                    flowerTextPaint.style = Paint.Style.STROKE
                    flowerTextPaint.strokeWidth = radius * 0.05f
                    flowerTextPaint.color = 0xFF228B22.toInt()  // Vert
                    canvas.drawLine(x, y + spikeHeight/2f, x, y + radius, flowerTextPaint)
                    
                    // Reset du style
                    flowerTextPaint.style = Paint.Style.FILL
                } else {
                    // Lupin verrouillé
                    drawLockedFlower(canvas, x, y, radius, "VERROUILLÉ")
                }
            }
            "IRIS" -> {
                if (isUnlocked) {
                    // Iris débloqué - MEILLEUR RENDU
                    flowerTextPaint.style = Paint.Style.FILL
                    flowerTextPaint.color = 0xFF4B0082.toInt()  // Indigo
                    
                    // Dessiner 3 pétales stylisés pour simuler un iris
                    for (i in 0..2) {
                        val angle = (i * 120f) * Math.PI / 180f
                        val petalX = x + kotlin.math.cos(angle).toFloat() * radius * 0.4f
                        val petalY = y + kotlin.math.sin(angle).toFloat() * radius * 0.4f
                        canvas.drawCircle(petalX, petalY, radius * 0.25f, flowerTextPaint)
                    }
                    
                    // Centre doré
                    flowerTextPaint.color = 0xFFFFD700.toInt()  // Or
                    canvas.drawCircle(x, y, radius * 0.15f, flowerTextPaint)
                } else {
                    // Iris verrouillé
                    drawLockedFlower(canvas, x, y, radius, "VERROUILLÉ")
                }
            }
        }
    }
    
    private fun drawLockedFlower(canvas: Canvas, x: Float, y: Float, radius: Float, text: String) {
        // Cadenas
        flowerTextPaint.textSize = radius * 1.4f
        flowerTextPaint.color = 0xAA888888.toInt()  // Gris
        canvas.drawText("🔒", x, y + 15f, flowerTextPaint)
    }
    
    private fun getUnlockedFlowersList(challengeManager: ChallengeManager): List<String> {
        val flowers = mutableListOf("MARGUERITE")  // Toujours débloquée
        
        if (challengeManager.isFlowerUnlocked("ROSE")) {
            flowers.add("ROSE")
        }
        
        if (challengeManager.isFlowerUnlocked("LUPIN")) {
            flowers.add("LUPIN")
        }
        
        // IRIS maintenant activé !
        if (challengeManager.isFlowerUnlocked("IRIS")) {
            flowers.add("IRIS")
        }
        
        return flowers
    }
    
    private fun drawMiniDaisy(canvas: Canvas, centerX: Float, centerY: Float, size: Float) {
        if (daisyBitmap != null) {
            // Utiliser l'image de marguerite
            val matrix = Matrix()
            val scale = size / maxOf(daisyBitmap!!.width, daisyBitmap!!.height)
            matrix.setScale(scale, scale)
            matrix.postTranslate(
                centerX - (daisyBitmap!!.width * scale) / 2f,
                centerY - (daisyBitmap!!.height * scale) / 2f
            )
            
            val paint = Paint().apply {
                isAntiAlias = true
                isFilterBitmap = true
            }
            canvas.drawBitmap(daisyBitmap!!, matrix, paint)
        } else {
            // Fallback si l'image n'est pas trouvée
            val centerPaint = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(255, 200, 50)
                style = Paint.Style.FILL
            }
            canvas.drawCircle(centerX, centerY, size * 0.3f, centerPaint)
            
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = size * 0.2f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("IMG", centerX, centerY, textPaint)
        }
    }
    
    // ==================== RENDU DES PLANTES - VERSIONS ORIGINALES (RÉTROCOMPATIBILITÉ) ====================
    
    fun drawMainStem(canvas: Canvas, mainStem: List<PlantStem.StemPoint>) {
        drawMainStemWithDissolution(canvas, mainStem, null)
    }
    
    fun drawBranches(canvas: Canvas, branches: List<PlantStem.Branch>) {
        drawBranchesWithDissolution(canvas, branches, null)
    }
    
    fun drawLeaves(canvas: Canvas, leaves: List<PlantLeavesManager.Leaf>, stem: PlantStem) {
        drawLeavesWithDissolution(canvas, leaves, stem, null)
    }
    
    fun drawBackgroundFlowers(canvas: Canvas, flowers: List<FlowerManager.Flower>, stem: PlantStem) {
        drawBackgroundFlowersWithDissolution(canvas, flowers, stem, null)
    }
    
    fun drawForegroundFlowers(canvas: Canvas, flowers: List<FlowerManager.Flower>, stem: PlantStem) {
        drawForegroundFlowersWithDissolution(canvas, flowers, stem, null)
    }
    
    // ==================== RENDU DES PLANTES - NOUVELLES VERSIONS AVEC DISSOLUTION ====================
    
    fun drawMainStemWithDissolution(canvas: Canvas, mainStem: List<PlantStem.StemPoint>, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        if (mainStem.size < 2) return
        
        stemPaint.color = Color.rgb(50, 120, 50)
        
        // Appliquer dissolution si présente
        if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            val alpha = ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
            stemPaint.alpha = alpha
            
            if (dissolveInfo.stemsCollapsing) {
                val shrivelingFactor = dissolveInfo.progress
                val red = (50 + (139 - 50) * shrivelingFactor).toInt()
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
            if (dissolveInfo?.stemsCollapsing == true) {
                strokeWidth *= (1f - dissolveInfo.progress * 0.4f)
            }
            stemPaint.strokeWidth = strokeWidth
            
            val adjustedX = point.x + point.oscillation + point.permanentWave
            val prevAdjustedX = prevPoint.x + prevPoint.oscillation + prevPoint.permanentWave
            
            // Effet d'affaissement
            var finalX = adjustedX
            var finalY = point.y
            if (dissolveInfo?.stemsCollapsing == true) {
                val bendFactor = dissolveInfo.progress * 15f
                val heightRatio = i.toFloat() / mainStem.size
                finalX += bendFactor * heightRatio * heightRatio
                finalY += bendFactor * 0.3f * heightRatio
            }
            
            if (i == 1) {
                canvas.drawLine(prevAdjustedX, prevPoint.y, finalX, finalY, stemPaint)
            } else {
                val controlX = (prevAdjustedX + finalX) / 2f
                val controlY = (prevPoint.y + finalY) / 2f
                val curvatureOffset = (finalX - prevAdjustedX) * 0.3f
                val finalControlX = controlX + curvatureOffset
                
                canvas.drawLine(prevAdjustedX, prevPoint.y, finalControlX, controlY, stemPaint)
                canvas.drawLine(finalControlX, controlY, finalX, finalY, stemPaint)
            }
        }
    }
    
    fun drawBranchesWithDissolution(canvas: Canvas, branches: List<PlantStem.Branch>, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        branchPaint.color = Color.rgb(40, 100, 40)
        
        if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            val alpha = ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
            branchPaint.alpha = alpha
        } else {
            branchPaint.alpha = 255
        }
        
        for (branch in branches.filter { it.isActive }) {
            if (branch.points.size >= 2) {
                for (i in 1 until branch.points.size) {
                    val point = branch.points[i]
                    val prevPoint = branch.points[i - 1]
                    
                    var strokeWidth = point.thickness
                    if (dissolveInfo?.stemsCollapsing == true) {
                        strokeWidth *= (1f - dissolveInfo.progress * 0.5f)
                    }
                    branchPaint.strokeWidth = strokeWidth
                    
                    if (i == 1 || branch.points.size <= 2) {
                        canvas.drawLine(prevPoint.x, prevPoint.y, point.x, point.y, branchPaint)
                    } else {
                        val controlX = (prevPoint.x + point.x) / 2f
                        val controlY = (prevPoint.y + point.y) / 2f
                        canvas.drawLine(prevPoint.x, prevPoint.y, controlX, controlY, branchPaint)
                        canvas.drawLine(controlX, controlY, point.x, point.y, branchPaint)
                    }
                }
            }
        }
    }
    
    fun drawLeavesWithDissolution(canvas: Canvas, leaves: List<PlantLeavesManager.Leaf>, stem: PlantStem, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            val alpha = ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
            leafPaint.alpha = alpha
        } else {
            leafPaint.alpha = 255
        }
        
        for (leaf in leaves) {
            if (leaf.currentSize > 0) {
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
                val leafPath = stem.getLeavesManager().createLeafPath(leaf)
                canvas.drawPath(leafPath, leafPaint)
                
                if (leaf.currentSize > leaf.maxSize * 0.7f) {
                    leafPaint.style = Paint.Style.STROKE
                    leafPaint.strokeWidth = 1.5f
                    leafPaint.color = Color.rgb(20, 80, 20)
                    canvas.drawPath(leafPath, leafPaint)
                    leafPaint.style = Paint.Style.FILL
                }
            }
        }
    }
    
    fun drawBackgroundFlowersWithDissolution(canvas: Canvas, flowers: List<FlowerManager.Flower>, stem: PlantStem, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val backgroundFlowers = flowers.filter { it.perspective.viewAngle > 60f }
        if (backgroundFlowers.isNotEmpty()) {
            drawFlowersWithDissolution(canvas, backgroundFlowers, stem, dissolveInfo)
        }
    }
    
    fun drawForegroundFlowersWithDissolution(canvas: Canvas, flowers: List<FlowerManager.Flower>, stem: PlantStem, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val foregroundFlowers = flowers.filter { it.perspective.viewAngle <= 60f }
        if (foregroundFlowers.isNotEmpty()) {
            drawFlowersWithDissolution(canvas, foregroundFlowers, stem, dissolveInfo)
        }
    }
    
    private fun drawFlowersWithDissolution(canvas: Canvas, flowers: List<FlowerManager.Flower>, stem: PlantStem, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        for (flower in flowers) {
            if (flower.currentSize > 0) {
                drawMargueriteWithDissolution(canvas, flower, dissolveInfo)
            }
        }
    }
    
    private fun drawMargueriteWithDissolution(canvas: Canvas, flower: FlowerManager.Flower, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        var flowerSize = flower.currentSize
        
        // Réduire la taille si dissolution
        if (dissolveInfo?.flowersPetalsWilting == true) {
            val wiltFactor = 1f - dissolveInfo.progress * 0.9f
            flowerSize *= wiltFactor
        }
        
        val baseAlpha = if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
        } else 255
        
        canvas.save()
        canvas.translate(flower.x, flower.y)
        
        // Couleur des pétales
        var petalRed = 255
        var petalGreen = 255
        var petalBlue = 255
        
        if (dissolveInfo?.flowersPetalsWilting == true) {
            val wiltFactor = dissolveInfo.progress
            petalRed = (255 * (1f - wiltFactor * 0.3f)).toInt()
            petalGreen = (255 * (1f - wiltFactor * 0.4f)).toInt()
            petalBlue = (255 * (1f - wiltFactor * 0.2f)).toInt()
        }
        
        // Pétales (moins nombreux si dissolution)
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
            
            // Pétales qui tombent
            var petalAlpha = baseAlpha
            if (dissolveInfo?.flowersPetalsWilting == true) {
                val petalWiltChance = dissolveInfo.progress + (i.toFloat() / maxPetals) * 0.3f
                if (petalWiltChance > 0.7f) {
                    petalAlpha = (petalAlpha * (1f - (petalWiltChance - 0.7f) / 0.3f)).toInt().coerceAtLeast(0)
                }
            }
            flowerPaint.alpha = petalAlpha
            
            canvas.save()
            canvas.rotate(angle * 180f / Math.PI.toFloat())
            
            // Pétales qui s'affaissent
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
        
        // Centre
        var centerRed = 255
        var centerGreen = 255
        var centerBlue = 0
        
        if (dissolveInfo?.flowersPetalsWilting == true) {
            val wiltFactor = dissolveInfo.progress
            centerRed = (255 * (1f - wiltFactor * 0.5f)).toInt()
            centerGreen = (255 * (1f - wiltFactor * 0.6f)).toInt()
            centerBlue = (0 + (100 * wiltFactor)).toInt()
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
            // Zones pour rosier et lupin - bande de 2 pouces (~192px) au centre
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
                else -> "MARGUERITE"
            }
        } else {
            // Fallback: détecter selon les fleurs débloquées
            when {
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
