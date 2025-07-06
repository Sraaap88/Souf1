package com.example.souffleforcetest

import kotlin.math.*

class PlantLeavesManager(private val plantStem: PlantStem) {
    
    // ==================== DATA CLASSES ====================
    
    data class Leaf(
        val x: Float,
        val y: Float,
        val size: Float,
        val angle: Float,
        val stemIndex: Int,          // -1 pour tige principale, 0+ pour branches
        val pointIndex: Int,         // Index du point sur la tige
        var currentSize: Float = 0f,
        val maxSize: Float,
        val side: Boolean,           // true = droite, false = gauche
        var oscillation: Float = 0f
    )
    
    // ==================== VARIABLES PRINCIPALES ====================
    
    val leaves = mutableListOf<Leaf>()
    private var lastForce = 0f
    private var leafCreationTimer = 0L
    private val leafCreationInterval = 200L // Une feuille toutes les 200ms
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.25f
    private val baseLeafSize = 15f
    private val maxLeafSize = 35f
    private val growthRate = 800f
    private val oscillationDecay = 0.96f
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun processLeavesGrowth(force: Float) {
        val currentTime = System.currentTimeMillis()
        
        // Créer de nouvelles feuilles si force suffisante
        if (force > forceThreshold && currentTime - leafCreationTimer > leafCreationInterval) {
            createNewLeaves()
            leafCreationTimer = currentTime
        }
        
        // Faire grandir les feuilles existantes
        growExistingLeaves(force)
        
        // Mettre à jour les oscillations
        updateLeafOscillations()
        
        lastForce = force
    }
    
    fun resetLeaves() {
        leaves.clear()
        lastForce = 0f
        leafCreationTimer = 0L
    }
    
    // ==================== FONCTIONS PRIVÉES ====================
    
    private fun createNewLeaves() {
        // Créer feuilles sur la tige principale
        createLeavesOnMainStem()
        
        // Créer feuilles sur chaque branche
        for (branchIndex in plantStem.branches.indices) {
            createLeavesOnBranch(branchIndex)
        }
    }
    
    private fun createLeavesOnMainStem() {
        val mainStem = plantStem.mainStem
        if (mainStem.size < 3) return
        
        // Chercher un point approprié pour une nouvelle feuille
        val availablePoints = mutableListOf<Int>()
        for (i in 2 until mainStem.size - 1) { // Pas tout en bas ni tout en haut
            val hasLeaf = leaves.any { it.stemIndex == -1 && it.pointIndex == i }
            if (!hasLeaf) availablePoints.add(i)
        }
        
        if (availablePoints.isNotEmpty()) {
            val pointIndex = availablePoints.random()
            val point = mainStem[pointIndex]
            val side = (pointIndex % 2 == 0) // Alternance gauche/droite
            
            val leafSize = baseLeafSize + (Math.random() * (maxLeafSize - baseLeafSize)).toFloat()
            val leafAngle = if (side) 45f else -45f + (Math.random() * 30f - 15f).toFloat()
            
            val newLeaf = Leaf(
                x = point.x,
                y = point.y,
                size = leafSize,
                angle = leafAngle,
                stemIndex = -1,
                pointIndex = pointIndex,
                maxSize = leafSize,
                side = side
            )
            
            leaves.add(newLeaf)
        }
    }
    
    private fun createLeavesOnBranch(branchIndex: Int) {
        val branch = plantStem.branches[branchIndex]
        if (branch.points.size < 3) return
        
        // Chercher un point approprié pour une nouvelle feuille
        val availablePoints = mutableListOf<Int>()
        for (i in 1 until branch.points.size - 1) {
            val hasLeaf = leaves.any { it.stemIndex == branchIndex && it.pointIndex == i }
            if (!hasLeaf) availablePoints.add(i)
        }
        
        if (availablePoints.isNotEmpty()) {
            val pointIndex = availablePoints.random()
            val point = branch.points[pointIndex]
            val side = (pointIndex % 2 == 0)
            
            val leafSize = baseLeafSize * 0.8f + (Math.random() * (maxLeafSize * 0.8f - baseLeafSize * 0.8f)).toFloat()
            val branchDirection = if (branch.angle > 0) 1f else -1f
            val leafAngle = branchDirection * 60f + (Math.random() * 40f - 20f).toFloat()
            
            val newLeaf = Leaf(
                x = point.x,
                y = point.y,
                size = leafSize,
                angle = leafAngle,
                stemIndex = branchIndex,
                pointIndex = pointIndex,
                maxSize = leafSize,
                side = side
            )
            
            leaves.add(newLeaf)
        }
    }
    
    private fun growExistingLeaves(force: Float) {
        for (leaf in leaves) {
            if (leaf.currentSize < leaf.maxSize && force > forceThreshold) {
                val forceStability = 1f - abs(force - lastForce).coerceAtMost(0.5f) * 2f
                val qualityMultiplier = 0.5f + forceStability * 0.5f
                
                val growthProgress = leaf.currentSize / leaf.maxSize
                val progressCurve = 1f - growthProgress * growthProgress
                val adjustedGrowth = force * qualityMultiplier * progressCurve * growthRate * 0.008f
                
                leaf.currentSize = (leaf.currentSize + adjustedGrowth).coerceAtMost(leaf.maxSize)
                
                // Oscillation lors de la croissance
                val forceVariation = abs(force - lastForce) * 3f
                leaf.oscillation = sin(System.currentTimeMillis() * 0.003f) * forceVariation * 2f
            }
        }
    }
    
    private fun updateLeafOscillations() {
        for (leaf in leaves) {
            leaf.oscillation *= oscillationDecay
            
            // Limiter l'oscillation
            leaf.oscillation = leaf.oscillation.coerceIn(-5f, 5f)
        }
    }
}
