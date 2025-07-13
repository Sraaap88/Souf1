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
        challengeManager?.notifyBudCreated(
