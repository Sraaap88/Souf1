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
    
    // ==================== RENDU DES PLANTES ====================
    
    fun drawMainStem(canvas: Canvas, mainStem: List<PlantStem.StemPoint>) {
        if (mainStem.size < 2) return
        
        stemPaint.color = Color.rgb(50, 120, 50)
        
        for (i in 1 until mainStem.size) {
            val point = mainStem[i]
            val prevPoint = mainStem[i - 1]
            
            stemPaint.strokeWidth = point.thickness
            
            val adjustedX = point.x + point.oscillation + point.permanentWave
            val prevAdjustedX = prevPoint.x + prevPoint.oscillation + prevPoint.permanentWave
            
            if (i == 1) {
                canvas.drawLine(prevAdjustedX, prevPoint.y, adjustedX, point.y, stemPaint)
            } else {
                val controlX = (prevAdjustedX + adjustedX) / 2f
                val controlY = (prevPoint.y + point.y) / 2f
                val curvatureOffset = (adjustedX - prevAdjustedX) * 0.3f
                val finalControlX = controlX + curvatureOffset
                
                canvas.drawLine(prevAdjustedX, prevPoint.y, finalControlX, controlY, stemPaint)
                canvas.drawLine(finalControlX, controlY, adjustedX, point.y, stemPaint)
            }
        }
    }
    
    fun drawBranches(canvas: Canvas, branches: List<PlantStem.Branch>) {
        branchPaint.color = Color.rgb(40, 100, 40)
        
        for (branch in branches.filter { it.isActive }) {
            if (branch.points.size >= 2) {
                for (i in 1 until branch.points.size) {
                    val point = branch.points[i]
                    val prevPoint = branch.points[i - 1]
                    
                    branchPaint.strokeWidth = point.thickness
                    
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
    
    fun drawLeaves(canvas: Canvas, leaves: List<PlantLeavesManager.Leaf>, stem: PlantStem) {
        for (leaf in leaves) {
            if (leaf.currentSize > 0) {
                leafPaint.color = stem.getLeavesManager().getLeafColor(leaf)
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
    
    fun drawBackgroundFlowers(canvas: Canvas, flowers: List<FlowerManager.Flower>, stem: PlantStem) {
        val backgroundFlowers = flowers.filter { it.perspective.viewAngle > 60f }
        if (backgroundFlowers.isNotEmpty()) {
            stem.getFlowerManager().drawSpecificFlowers(canvas, backgroundFlowers, flowerPaint, flowerCenterPaint)
        }
    }
    
    fun drawForegroundFlowers(canvas: Canvas, flowers: List<FlowerManager.Flower>, stem: PlantStem) {
        val foregroundFlowers = flowers.filter { it.perspective.viewAngle <= 60f }
        if (foregroundFlowers.isNotEmpty()) {
            stem.getFlowerManager().drawSpecificFlowers(canvas, foregroundFlowers, flowerPaint, flowerCenterPaint)
        }
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
