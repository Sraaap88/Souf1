package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import kotlin.math.*

class BudManager(private val plantStem: PlantStem) {
    
    // ==================== DATA CLASSES ====================
    
    data class Bud(
        val x: Float,
        val y: Float,
        val stemIndex: Int,              // -1 pour tige principale, 0+ pour branches
        var currentSize: Float = 0f,
        val maxSize: Float,
        val creationTime: Long,
        var isFullyGrown: Boolean = false
    )
    
    // ==================== VARIABLES ====================
    
    val buds = mutableListOf<Bud>()
    private var lastForce = 0f
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.25f
    private val baseBudSize = 20f           // Taille de base du bouton
    private val maxBudSize = 35f            // Taille maximum du bouton
    private val growthRate = 300f           // Vitesse de croissance
    private val petalCount = 6              // Nombre de petites pétales
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun processBudGrowth(force: Float) {
        // Créer des boutons sur les tiges éligibles (5-30% de croissance)
        createBudsOnEligibleStems()
        
        // Faire grandir les boutons existants
        growExistingBuds(force)
        
        lastForce = force
    }
    
    fun resetBuds() {
        buds.clear()
        lastForce = 0f
    }
    
    fun drawBuds(canvas: Canvas, budPaint: Paint, petalPaint: Paint) {
        for (bud in buds) {
            if (bud.currentSize > 0) {
                drawSingleBud(canvas, bud, budPaint, petalPaint)
            }
        }
    }
    
    // ==================== FONCTIONS PRIVÉES ====================
    
    private fun createBudsOnEligibleStems() {
        // Vérifier la tige principale
        checkMainStemForBud()
        
        // Vérifier chaque branche
        for (branchIndex in plantStem.branches.indices) {
            checkBranchForBud(branchIndex)
        }
    }
    
    private fun checkMainStemForBud() {
        if (buds.any { it.stemIndex == -1 } || plantStem.mainStem.size <= 5) return
        
        val mainStemHeight = if (plantStem.mainStem.isNotEmpty()) {
            plantStem.getStemBaseY() - plantStem.mainStem.last().y
        } else 0f
        
        // Bouton sur tige principale si entre 30px et 80px de hauteur
        if (mainStemHeight in 30f..79f) {
            createBudOnMainStem()
        }
    }
    
    private fun checkBranchForBud(branchIndex: Int) {
        if (buds.any { it.stemIndex == branchIndex }) return
        
        val branch = plantStem.branches[branchIndex]
        
        // Calculer le pourcentage de croissance
        val growthPercentage = if (branch.maxHeight > 0) {
            branch.currentHeight / branch.maxHeight
        } else 0f
        
        // Créer bouton si entre 5% et 30% de croissance
        if (growthPercentage >= 0.05f && growthPercentage < 0.30f && 
            branch.currentHeight >= 20f && branch.points.isNotEmpty()) {
            createBudOnBranch(branchIndex)
        }
    }
    
    private fun createBudOnMainStem() {
        val topPoint = plantStem.mainStem.last()
        val size = baseBudSize + (Math.random() * (maxBudSize - baseBudSize)).toFloat()
        
        val bud = Bud(
            x = topPoint.x,
            y = topPoint.y - 10f,
            stemIndex = -1,
            maxSize = size,
            creationTime = System.currentTimeMillis()
        )
        
        buds.add(bud)
        println("Bouton créé sur tige principale")
    }
    
    private fun createBudOnBranch(branchIndex: Int) {
        val branch = plantStem.branches[branchIndex]
        val topPoint = branch.points.last()
        val size = (baseBudSize * 0.8f) + (Math.random() * (maxBudSize * 0.8f - baseBudSize * 0.8f)).toFloat()
        
        val bud = Bud(
            x = topPoint.x,
            y = topPoint.y - 8f,
            stemIndex = branchIndex,
            maxSize = size,
            creationTime = System.currentTimeMillis()
        )
        
        buds.add(bud)
        
        val growthPercentage = (branch.currentHeight / branch.maxHeight * 100).toInt()
        println("Bouton créé sur branche $branchIndex (croissance: ${growthPercentage}%)")
    }
    
    private fun growExistingBuds(force: Float) {
        for (bud in buds) {
            if (bud.currentSize < bud.maxSize && force > forceThreshold) {
                val forceStability = 1f - abs(force - lastForce).coerceAtMost(0.5f) * 2f
                val qualityMultiplier = 0.5f + forceStability * 0.5f
                
                val growthProgress = bud.currentSize / bud.maxSize
                val progressCurve = 1f - growthProgress * growthProgress
                val adjustedGrowth = force * qualityMultiplier * progressCurve * growthRate * 0.008f
                
                bud.currentSize = (bud.currentSize + adjustedGrowth).coerceAtMost(bud.maxSize)
                
                // Marquer comme complètement développé
                if (bud.currentSize >= bud.maxSize * 0.95f) {
                    bud.isFullyGrown = true
                }
            }
        }
    }
    
    private fun drawSingleBud(canvas: Canvas, bud: Bud, budPaint: Paint, petalPaint: Paint) {
        val centerX = bud.x
        val centerY = bud.y
        val size = bud.currentSize
        
        if (size <= 0) return
        
        // Dessiner le centre vert du bouton
        val centerRadius = size * 0.3f
        budPaint.color = Color.rgb(60, 120, 60)
        budPaint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, centerRadius, budPaint)
        
        // Dessiner les petites pétales qui pointent
        petalPaint.color = Color.rgb(240, 240, 240)
        petalPaint.style = Paint.Style.STROKE
        petalPaint.strokeWidth = size * 0.08f
        petalPaint.strokeCap = Paint.Cap.ROUND
        
        for (i in 0 until petalCount) {
            val angle = (i * 360f / petalCount) + (Math.random() * 8f - 4f).toFloat()
            val petalLength = size * (0.4f + Math.random() * 0.2f).toFloat()
            
            val rad = Math.toRadians(angle.toDouble())
            val startX = centerX + cos(rad).toFloat() * centerRadius
            val startY = centerY + sin(rad).toFloat() * centerRadius
            val endX = centerX + cos(rad).toFloat() * (centerRadius + petalLength)
            val endY = centerY + sin(rad).toFloat() * (centerRadius + petalLength)
            
            canvas.drawLine(startX, startY, endX, endY, petalPaint)
        }
        
        // Optionnel: petit contour pour le centre
        budPaint.color = Color.rgb(40, 90, 40)
        budPaint.style = Paint.Style.STROKE
        budPaint.strokeWidth = 1f
        canvas.drawCircle(centerX, centerY, centerRadius, budPaint)
    }
    
    // ==================== FONCTIONS UTILITAIRES ====================
    
    fun getBudCount(): Int = buds.size
    
    fun getFullyGrownBudCount(): Int = buds.count { it.isFullyGrown }
    
    fun getBudInfo(): String {
        val total = buds.size
        val grown = getFullyGrownBudCount()
        return "Boutons: $total total, $grown matures"
    }
    
    fun removeBudsForStem(stemIndex: Int) {
        buds.removeAll { it.stemIndex == stemIndex }
    }
    
    fun hasBudOnStem(stemIndex: Int): Boolean {
        return buds.any { it.stemIndex == stemIndex }
    }
}
