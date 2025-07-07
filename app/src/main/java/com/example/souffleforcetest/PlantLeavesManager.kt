package com.example.souffleforcetest

import android.graphics.Path
import kotlin.math.*

class PlantLeavesManager(private val plantStem: PlantStem) {
    
    // ==================== DATA CLASSES ====================
    
    data class Leaf(
        val x: Float,
        val y: Float,
        val size: Float,
        val angle: Float,
        val stemIndex: Int,          // -1 pour tige principale, -2 pour basales, 0+ pour branches
        val pointIndex: Int,         // Index du point sur la tige
        var currentSize: Float = 0f,
        val maxSize: Float,
        val side: Boolean,           // true = droite, false = gauche
        var oscillation: Float = 0f,
        var permanentCurve: Float = 0f, // Courbure permanente comme les tiges
        val leafType: LeafType,      // Type de feuille
        val personality: LeafPersonality // Caractéristiques uniques
    )
    
    data class LeafPersonality(
        val teethDepth: Float,       // Profondeur des dents (0.1-0.4)
        val teethCount: Int,         // Nombre de dents (4-8)
        val widthRatio: Float,       // Ratio largeur/longueur (0.3-0.6)
        val curvature: Float,        // Courbure générale (-0.3 à 0.3)
        val asymmetry: Float,        // Asymétrie (0-0.2)
        val colorVariation: Float    // Variation de couleur (0-0.3)
    )
    
    enum class LeafType {
        BASAL,      // Feuilles de base (grandes, très dentées)
        CAULINE     // Feuilles de tige (petites, lobées)
    }
    
    // ==================== VARIABLES PRINCIPALES ====================
    
    val leaves = mutableListOf<Leaf>()
    private var lastForce = 0f
    private var leafCreationTimer = 0L
    private val leafCreationInterval = 150L // Une feuille toutes les 150ms
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.25f
    private val baseLeafSize = 50f  
    private val maxLeafSize = 108f  
    private val basalLeafSize = 65f // Feuilles basales plus longues
    private val maxBasalLeafSize = 125f
    private var basalLeavesCreated = false
    
    // ==================== RENDERER ====================
    
    private val leafRenderer = LeafRenderer(plantStem)
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun processLeavesGrowth(force: Float) {
        val currentTime = System.currentTimeMillis()
        
        // Créer les feuilles basales en premier (rosette dense)
        if (!basalLeavesCreated && force > forceThreshold) {
            createBasalRosette()
            basalLeavesCreated = true
        }
        
        // Créer des feuilles sur toutes les tiges
        if (force > forceThreshold && currentTime - leafCreationTimer > leafCreationInterval) {
            createRealisticLeaves()
            leafCreationTimer = currentTime
        }
        
        // Faire grandir les feuilles existantes
        leafRenderer.growExistingLeaves(leaves, force, lastForce)
        
        // Mettre à jour les oscillations et courbure par poids
        leafRenderer.updateLeafPhysics(leaves)
        
        lastForce = force
    }
    
    fun resetLeaves() {
        leaves.clear()
        lastForce = 0f
        leafCreationTimer = 0L
        basalLeavesCreated = false
    }
    
    fun createLeafPath(leaf: Leaf): Path {
        val path = Path()
        val currentSize = leaf.currentSize
        if (currentSize <= 0) return path
        
        val personality = leaf.personality
        val adjustedX = leaf.x + leaf.oscillation + leaf.permanentCurve
        
        // Calculer l'angle réaliste avec effet poids
        val realAngle = leafRenderer.calculateRealisticLeafAngle(leaf)
        
        // Toutes les feuilles utilisent la même forme
        leafRenderer.createRealisticLeafPath(path, adjustedX, leaf.y, currentSize, personality, realAngle)
        
        return path
    }
    
    fun getLeafColor(leaf: Leaf): Int {
        // Couleurs réalistes avec dégradés selon la position
        val maturity = leaf.currentSize / leaf.maxSize
        val maturityBonus = (maturity * 15).toInt()
        
        // Feuilles plus basses = plus foncées
        val heightRatio = (plantStem.getStemBaseY() - leaf.y) / plantStem.getMaxPossibleHeight()
        val heightDarkening = (heightRatio * 20).toInt()
        
        val r = (45 + (leaf.personality.colorVariation * 10).toInt() - maturityBonus - heightDarkening).coerceIn(25, 65)
        val g = (125 + (leaf.personality.colorVariation * 20).toInt() + maturityBonus).coerceIn(100, 160)
        val b = (40 + (leaf.personality.colorVariation * 8).toInt() - maturityBonus - heightDarkening).coerceIn(25, 55)
        
        return android.graphics.Color.rgb(r, g, b)
    }
    
    // ==================== FONCTIONS PRIVÉES ====================
    
    private fun createBasalRosette() {
        val baseX = plantStem.getStemBaseX()
        val baseY = plantStem.getStemBaseY()
        
        // Créer 12-15 feuilles basales en rosette dense
        val leafCount = 12 + (Math.random() * 4).toInt()
        
        for (i in 0 until leafCount) {
            val angle = (i * 360f / leafCount) + (Math.random() * 20f - 10f).toFloat()
            val distance = 25f + (Math.random() * 15f).toFloat() // Plus près de la base
            
            val leafX = baseX + cos(Math.toRadians(angle.toDouble())).toFloat() * distance
            val leafY = baseY + sin(Math.toRadians(angle.toDouble())).toFloat() * distance * 0.3f
            
            // Feuilles basales plus longues
            val size = basalLeafSize + (Math.random() * (maxBasalLeafSize - basalLeafSize)).toFloat()
            val personality = leafRenderer.generateLeafPersonality()
            
            val basalLeaf = Leaf(
                x = leafX,
                y = leafY,
                size = size,
                angle = (Math.random() * 360f).toFloat(), // Angles aléatoires dans toutes les directions
                stemIndex = -2, // Code pour feuilles basales
                pointIndex = -1,
                maxSize = size,
                side = angle > 180f, // Basé sur l'angle réel
                leafType = LeafType.BASAL,
                personality = personality
            )
            
            leaves.add(basalLeaf)
        }
    }
    
    private fun createRealisticLeaves() {
        // Créer feuilles sur la tige principale
        createLeavesOnMainStem()
        
        // Créer feuilles sur chaque branche
        for (branchIndex in plantStem.branches.indices) {
            createLeavesOnBranch(branchIndex)
        }
    }
    
    private fun createLeavesOnMainStem() {
        val mainStem = plantStem.mainStem
        if (mainStem.size < 4) return
        
        // Répartition naturelle : plus de feuilles vers le bas, moins vers le haut
        val totalHeight = plantStem.getMaxPossibleHeight()
        val availablePoints = mutableListOf<Int>()
        
        for (i in 2 until mainStem.size - 2) {
            val hasLeaf = leaves.any { it.stemIndex == -1 && it.pointIndex == i }
            if (!hasLeaf) {
                val point = mainStem[i]
                val heightRatio = (plantStem.getStemBaseY() - point.y) / totalHeight
                
                // Probabilité naturelle : plus de chances vers le bas
                val naturalProbability = when {
                    heightRatio < 0.3f -> 0.8f    // 80% de chance en bas
                    heightRatio < 0.6f -> 0.5f    // 50% de chance au milieu
                    else -> 0.2f                  // 20% de chance en haut
                }
                
                // Espacement naturel avec variation aléatoire
                val lastLeafDistance = leaves.filter { it.stemIndex == -1 }
                    .minByOrNull { abs(it.pointIndex - i) }?.let { abs(it.pointIndex - i) } ?: 10
                
                val minDistance = 3 + (Math.random() * 2).toInt() // 3-5 points minimum
                
                if (lastLeafDistance >= minDistance && Math.random() < naturalProbability) {
                    availablePoints.add(i)
                }
            }
        }
        
        if (availablePoints.isNotEmpty()) {
            val pointIndex = availablePoints.random()
            val point = mainStem[pointIndex]
            
            // Alternance naturelle avec variation 360°
            val existingLeaves = leaves.filter { it.stemIndex == -1 }
            val baseAngle = (existingLeaves.size * 137.5f) % 360f // Angle d'or pour répartition naturelle
            val naturalAngle = baseAngle + (Math.random() * 60f - 30f).toFloat() // ±30° de variation
            val naturalSide = naturalAngle > 180f
            
            // Taille selon la hauteur : feuilles basses plus grandes
            val heightRatio = (plantStem.getStemBaseY() - point.y) / plantStem.getMaxPossibleHeight()
            val heightMultiplier = 0.7f + (1f - heightRatio) * 0.8f // 0.7x à 1.5x selon hauteur
            val size = (baseLeafSize + (Math.random() * (maxLeafSize - baseLeafSize)).toFloat()) * heightMultiplier
            
            val personality = leafRenderer.generateLeafPersonality()
            
            val newLeaf = Leaf(
                x = point.x,
                y = point.y,
                size = size,
                angle = naturalAngle, // Angle calculé pour répartition 360°
                stemIndex = -1,
                pointIndex = pointIndex,
                maxSize = size,
                side = naturalSide,
                leafType = LeafType.CAULINE,
                personality = personality
            )
            
            leaves.add(newLeaf)
        }
    }
    
    private fun createLeavesOnBranch(branchIndex: Int) {
        val branch = plantStem.branches[branchIndex]
        if (branch.points.size < 4) return
        
        // Répartition naturelle sur les branches aussi
        val availablePoints = mutableListOf<Int>()
        
        for (i in 1 until branch.points.size - 1) {
            val hasLeaf = leaves.any { it.stemIndex == branchIndex && it.pointIndex == i }
            if (!hasLeaf) {
                val point = branch.points[i]
                val branchHeightRatio = i.toFloat() / branch.points.size
                
                // Plus de feuilles vers la base des branches
                val naturalProbability = when {
                    branchHeightRatio < 0.4f -> 0.6f    // 60% de chance vers la base
                    branchHeightRatio < 0.7f -> 0.3f    // 30% de chance au milieu
                    else -> 0.1f                        // 10% de chance vers le bout
                }
                
                val lastLeafDistance = leaves.filter { it.stemIndex == branchIndex }
                    .minByOrNull { abs(it.pointIndex - i) }?.let { abs(it.pointIndex - i) } ?: 5
                
                val minDistance = 2 + (Math.random() * 2).toInt() // 2-4 points minimum
                
                if (lastLeafDistance >= minDistance && Math.random() < naturalProbability) {
                    availablePoints.add(i)
                }
            }
        }
        
        if (availablePoints.isNotEmpty()) {
            val pointIndex = availablePoints.random()
            val point = branch.points[pointIndex]
            
            // Alternance naturelle sur la branche avec répartition 360°
            val existingBranchLeaves = leaves.filter { it.stemIndex == branchIndex }
            val baseAngle = (existingBranchLeaves.size * 137.5f) % 360f // Angle d'or
            val branchAngle = baseAngle + (Math.random() * 90f - 45f).toFloat() // Plus de variation sur branches
            val naturalSide = branchAngle > 180f
            
            // Taille selon la hauteur sur la branche
            val heightRatio = (plantStem.getStemBaseY() - point.y) / plantStem.getMaxPossibleHeight()
            val heightMultiplier = 0.6f + (1f - heightRatio) * 0.6f // Plus petites sur branches
            val size = (baseLeafSize * 0.8f + (Math.random() * (maxLeafSize * 0.8f - baseLeafSize * 0.8f)).toFloat()) * heightMultiplier
            
            val personality = leafRenderer.generateLeafPersonality()
            
            val newLeaf = Leaf(
                x = point.x,
                y = point.y,
                size = size,
                angle = branchAngle, // Angle calculé pour répartition
                stemIndex = branchIndex,
                pointIndex = pointIndex,
                maxSize = size,
                side = naturalSide,
                leafType = LeafType.CAULINE,
                personality = personality
            )
            
            leaves.add(newLeaf)
        }
    }
}
