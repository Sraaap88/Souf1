package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import kotlin.math.*

class BudManager(private val plantStem: PlantStem) {
    
    data class Bud(
        val x: Float,
        val y: Float,
        val stemIndex: Int,
        var currentSize: Float = 0f,
        val maxSize: Float,
        val creationTime: Long,
        var isFullyGrown: Boolean = false,
        val petalCount: Int,
        val petalVariations: List<Float>,
        val id: String = generateBudId()
    )
    
    val buds = mutableListOf<Bud>()
    private var lastForce = 0f
    private var challengeManager: ChallengeManager? = null
    
    private val forceThreshold = 0.25f
    private val baseBudSize = 140f
    private val maxBudSize = 245f
    private val growthRate = 300f
    private val minPetalCount = 4
    private val maxPetalCount = 7
    private val budChallengeMinForce = 0.05f
    private val budChallengeMaxForce = 0.25f
    
    fun setChallengeManager(manager: ChallengeManager) {
        challengeManager = manager
    }
    
    fun processBudGrowth(force: Float) {
        if (challengeManager?.getCurrentChallenge()?.id == 2 || challengeManager?.getCurrentChallenge()?.id == 3) {
            createBudsForChallenge(force)
        } else {
            createBudsOnEligibleStems()
        }
        growExistingBuds(force)
        lastForce = force
    }
    
    fun resetBuds() {
        buds.clear()
        lastForce = 0f
    }
    
    // MODIFIÉ: Ajout du paramètre dissolveInfo
    fun drawBuds(
        canvas: Canvas, 
        budPaint: Paint, 
        petalPaint: Paint, 
        dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null
    ) {
        for (bud in buds) {
            if (bud.currentSize > 0) {
                drawSingleBud(canvas, bud, budPaint, petalPaint, dissolveInfo)
            }
        }
    }
    
    private fun createBudsForChallenge(force: Float) {
        if (force < budChallengeMinForce || force > budChallengeMaxForce) return
        checkMainStemForBudChallenge()
        for (branchIndex in plantStem.branches.indices) {
            checkBranchForBudChallenge(branchIndex)
        }
    }
    
    private fun checkMainStemForBudChallenge() {
        if (buds.any { it.stemIndex == -1 } || plantStem.mainStem.size <= 3) return
        
        val mainStemHeight = if (plantStem.mainStem.isNotEmpty()) {
            plantStem.getStemBaseY() - plantStem.mainStem.last().y
        } else 0f
        
        if (mainStemHeight in 20f..80f) {
            createBudOnMainStemForChallenge()
        }
    }
    
    private fun checkBranchForBudChallenge(branchIndex: Int) {
        if (buds.any { it.stemIndex == branchIndex }) return
        
        val branch = plantStem.branches[branchIndex]
        val growthPercentage = if (branch.maxHeight > 0) {
            branch.currentHeight / branch.maxHeight
        } else 0f
        
        if (growthPercentage >= 0.05f && growthPercentage <= 0.30f && 
            branch.currentHeight >= 15f && branch.points.isNotEmpty()) {
            createBudOnBranchForChallenge(branchIndex)
        }
    }
    
    private fun createBudOnMainStemForChallenge() {
        val topPoint = plantStem.mainStem.last()
        
        val sizeVariation = Math.random().toFloat()
        val size = baseBudSize + (sizeVariation * (maxBudSize - baseBudSize))
        val petalCount = minPetalCount + (Math.random() * (maxPetalCount - minPetalCount + 1)).toInt()
        val petalVariations = (0 until petalCount).map { 
            0.8f + (Math.random().toFloat() * 0.4f)
        }
        
        val bud = Bud(
            x = topPoint.x,
            y = topPoint.y - 10f,
            stemIndex = -1,
            maxSize = size,
            creationTime = System.currentTimeMillis(),
            petalCount = petalCount,
            petalVariations = petalVariations
        )
        
        buds.add(bud)
        challengeManager?.notifyBudCreated(bud.x, bud.y, bud.id)
        println("Bourgeon créé pour défi sur tige principale: ${petalCount} pointes, ID: ${bud.id}")
    }
    
    private fun createBudOnBranchForChallenge(branchIndex: Int) {
        val branch = plantStem.branches[branchIndex]
        val topPoint = branch.points.last()
        
        val baseSizeForBranch = baseBudSize * 0.8f
        val maxSizeForBranch = maxBudSize * 0.8f
        val sizeVariation = Math.random().toFloat()
        val size = baseSizeForBranch + (sizeVariation * (maxSizeForBranch - baseSizeForBranch))
        val petalCount = (minPetalCount - 1) + (Math.random() * (maxPetalCount - minPetalCount + 2)).toInt()
        val petalVariations = (0 until petalCount).map { 
            0.7f + (Math.random().toFloat() * 0.5f)
        }
        
        val bud = Bud(
            x = topPoint.x,
            y = topPoint.y - 8f,
            stemIndex = branchIndex,
            maxSize = size,
            creationTime = System.currentTimeMillis(),
            petalCount = petalCount,
            petalVariations = petalVariations
        )
        
        buds.add(bud)
        challengeManager?.notifyBudCreated(bud.x, bud.y, bud.id)
        println("Bourgeon créé pour défi sur branche $branchIndex: ${petalCount} pointes, ID: ${bud.id}")
    }
    
    private fun createBudsOnEligibleStems() {
        checkMainStemForBud()
        for (branchIndex in plantStem.branches.indices) {
            checkBranchForBud(branchIndex)
        }
    }
    
    private fun checkMainStemForBud() {
        if (buds.any { it.stemIndex == -1 } || plantStem.mainStem.size <= 5) return
        
        val mainStemHeight = if (plantStem.mainStem.isNotEmpty()) {
            plantStem.getStemBaseY() - plantStem.mainStem.last().y
        } else 0f
        
        if (mainStemHeight in 30f..79f) {
            createBudOnMainStem()
        }
    }
    
    private fun checkBranchForBud(branchIndex: Int) {
        if (buds.any { it.stemIndex == branchIndex }) return
        
        val branch = plantStem.branches[branchIndex]
        val growthPercentage = if (branch.maxHeight > 0) {
            branch.currentHeight / branch.maxHeight
        } else 0f
        
        if (growthPercentage >= 0.05f && growthPercentage < 0.30f && 
            branch.currentHeight >= 20f && branch.points.isNotEmpty()) {
            createBudOnBranch(branchIndex)
        }
    }
    
    private fun createBudOnMainStem() {
        val topPoint = plantStem.mainStem.last()
        
        val sizeVariation = Math.random().toFloat()
        val size = baseBudSize + (sizeVariation * (maxBudSize - baseBudSize))
        val petalCount = minPetalCount + (Math.random() * (maxPetalCount - minPetalCount + 1)).toInt()
        val petalVariations = (0 until petalCount).map { 
            0.8f + (Math.random().toFloat() * 0.4f)
        }
        
        val bud = Bud(
            x = topPoint.x,
            y = topPoint.y - 10f,
            stemIndex = -1,
            maxSize = size,
            creationTime = System.currentTimeMillis(),
            petalCount = petalCount,
            petalVariations = petalVariations
        )
        
        buds.add(bud)
        println("Bouton créé sur tige principale: ${petalCount} pointes, taille max: ${size.toInt()}px")
    }
    
    private fun createBudOnBranch(branchIndex: Int) {
        val branch = plantStem.branches[branchIndex]
        val topPoint = branch.points.last()
        
        val baseSizeForBranch = baseBudSize * 0.8f
        val maxSizeForBranch = maxBudSize * 0.8f
        val sizeVariation = Math.random().toFloat()
        val size = baseSizeForBranch + (sizeVariation * (maxSizeForBranch - baseSizeForBranch))
        val petalCount = (minPetalCount - 1) + (Math.random() * (maxPetalCount - minPetalCount + 2)).toInt()
        val petalVariations = (0 until petalCount).map { 
            0.7f + (Math.random().toFloat() * 0.5f)
        }
        
        val bud = Bud(
            x = topPoint.x,
            y = topPoint.y - 8f,
            stemIndex = branchIndex,
            maxSize = size,
            creationTime = System.currentTimeMillis(),
            petalCount = petalCount,
            petalVariations = petalVariations
        )
        
        buds.add(bud)
        
        val growthPercentage = (branch.currentHeight / branch.maxHeight * 100).toInt()
        println("Bouton créé sur branche $branchIndex: ${petalCount} pointes, taille: ${size.toInt()}px (croissance: ${growthPercentage}%)")
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
                
                if (bud.currentSize >= bud.maxSize * 0.95f) {
                    bud.isFullyGrown = true
                }
            }
        }
    }
    
    // MODIFIÉ: Ajout des effets de dissolution
    private fun drawSingleBud(
        canvas: Canvas, 
        bud: Bud, 
        budPaint: Paint, 
        petalPaint: Paint, 
        dissolveInfo: ChallengeEffectsManager.DissolveInfo?
    ) {
        val centerX = bud.x
        val centerY = bud.y
        var size = bud.currentSize
        
        if (size <= 0) return
        
        // Appliquer les effets de dissolution
        if (dissolveInfo != null && dissolveInfo.progress > 0f && dissolveInfo.flowersPetalsWilting) {
            size *= (1f - dissolveInfo.progress * 0.8f)
        }
        
        val baseWidth = size * 0.6f
        val baseHeight = size * 0.4f
        
        // Couleur de base avec dissolution
        var baseRed = 60
        var baseGreen = 120
        var baseBlue = 60
        var baseAlpha = 255
        
        if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            baseAlpha = ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
            
            if (dissolveInfo.flowersPetalsWilting) {
                val wiltFactor = dissolveInfo.progress
                baseRed = (60 + (139 - 60) * wiltFactor).toInt()
                baseGreen = (120 * (1f - wiltFactor * 0.6f)).toInt()
                baseBlue = (60 * (1f - wiltFactor * 0.8f)).toInt()
            }
        }
        
        // Base ovale
        budPaint.color = Color.argb(baseAlpha, baseRed, baseGreen, baseBlue)
        budPaint.style = Paint.Style.FILL
        canvas.drawOval(
            centerX - baseWidth/2, 
            centerY - baseHeight/2, 
            centerX + baseWidth/2, 
            centerY + baseHeight/2, 
            budPaint
        )
        
        // Pointes (si pas trop dissoutes)
        if (dissolveInfo == null || dissolveInfo.progress < 0.9f) {
            var petalRed = 240
            var petalGreen = 240
            var petalBlue = 240
            
            if (dissolveInfo?.flowersPetalsWilting == true) {
                val wiltFactor = dissolveInfo.progress
                petalRed = (240 * (1f - wiltFactor * 0.5f)).toInt()
                petalGreen = (240 * (1f - wiltFactor * 0.4f)).toInt()
                petalBlue = (240 * (1f - wiltFactor * 0.3f)).toInt()
            }
            
            petalPaint.color = Color.argb(baseAlpha, petalRed, petalGreen, petalBlue)
            petalPaint.style = Paint.Style.STROKE
            
            var strokeWidth = size * 0.04f
            if (dissolveInfo?.flowersPetalsWilting == true) {
                strokeWidth *= (1f - dissolveInfo.progress * 0.6f)
            }
            petalPaint.strokeWidth = strokeWidth
            petalPaint.strokeCap = Paint.Cap.ROUND
            
            val petalCount = bud.petalCount
            val petalBaseY = centerY - baseHeight/2
            var petalHeight = size * 0.5f
            
            if (dissolveInfo?.flowersPetalsWilting == true) {
                petalHeight *= (1f - dissolveInfo.progress * 0.7f)
            }
            
            for (i in 0 until petalCount) {
                val progress = i.toFloat() / (petalCount - 1).coerceAtLeast(1)
                val petalStartX = centerX - baseWidth/3 + (progress * baseWidth * 2/3)
                
                var petalVariation = bud.petalVariations.getOrElse(i) { 1f }
                if (dissolveInfo?.flowersPetalsWilting == true) {
                    petalVariation *= (1f - dissolveInfo.progress * 0.5f)
                }
                
                val finalPetalHeight = petalHeight * petalVariation
                val curveOffset = (progress - 0.5f) * size * 0.1f
                val petalEndX = petalStartX + curveOffset
                val petalEndY = petalBaseY - finalPetalHeight
                
                canvas.drawLine(petalStartX, petalBaseY, petalEndX, petalEndY, petalPaint)
            }
        }
        
        // Contour (si pas trop dissout)
        if (dissolveInfo == null || dissolveInfo.progress < 0.8f) {
            var contourRed = 40
            var contourGreen = 90
            var contourBlue = 40
            
            if (dissolveInfo?.flowersPetalsWilting == true) {
                val wiltFactor = dissolveInfo.progress
                contourRed = (40 + (99 - 40) * wiltFactor).toInt()
                contourGreen = (90 * (1f - wiltFactor * 0.7f)).toInt()
                contourBlue = (40 * (1f - wiltFactor * 0.8f)).toInt()
            }
            
            budPaint.color = Color.argb(baseAlpha, contourRed, contourGreen, contourBlue)
            budPaint.style = Paint.Style.STROKE
            
            var strokeWidth = 2f
            if (dissolveInfo?.flowersPetalsWilting == true) {
                strokeWidth *= (1f - dissolveInfo.progress * 0.5f)
            }
            budPaint.strokeWidth = strokeWidth
            
            canvas.drawOval(
                centerX - baseWidth/2, 
                centerY - baseHeight/2, 
                centerX + baseWidth/2, 
                centerY + baseHeight/2, 
                budPaint
            )
        }
    }
    
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
    
    companion object {
        private var budIdCounter = 0
        
        private fun generateBudId(): String {
            budIdCounter++
            return "bud_$budIdCounter"
        }
    }
}
