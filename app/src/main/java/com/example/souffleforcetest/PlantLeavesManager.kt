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
    private val baseLeafSize = 50f  // +20% encore (42f → 50f)
    private val maxLeafSize = 108f  // +20% encore (90f → 108f)
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
        
        // Créer 8-10 feuilles basales en rosette naturelle (moins dense)
        val leafCount = 8 + (Math.random() * 3).toInt()
        
        for (i in 0 until leafCount) {
            // Distribution moins géométrique avec clusters naturels
            val baseAngle = (i * 360f / leafCount)
            val clusterVariation = (Math.random() * 40f - 20f).toFloat() // ±20° pour clusters
            val angle = baseAngle + clusterVariation
            
            // Distance variable pour aspect naturel
            val distance = 20f + (Math.random() * 25f).toFloat() // 20-45px
            
            // Position avec variation en hauteur (rosette 3D)
            val leafX = baseX + cos(Math.toRadians(angle.toDouble())).toFloat() * distance
            val heightVariation = (Math.random() * 8f - 4f).toFloat() // ±4px en hauteur
            val leafY = baseY + sin(Math.toRadians(angle.toDouble())).toFloat() * distance * 0.2f + heightVariation
            
            // Tailles très variées pour naturel
            val sizeVariation = 0.6f + (Math.random() * 0.8f).toFloat() // 60% à 140% de variation
            val size = (basalLeafSize + (Math.random() * (maxBasalLeafSize - basalLeafSize)).toFloat()) * sizeVariation
            val personality = leafRenderer.generateLeafPersonality()
            
            val basalLeaf = Leaf(
                x = leafX,
                y = leafY,
                size = size,
                angle = angle + (Math.random() * 30f - 15f).toFloat(), // ±15° de variation d'orientation
                stemIndex = -2, // Code pour feuilles basales
                pointIndex = -1,
                maxSize = size,
                side = angle > 180f,
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
                    heightRatio < 0.3f -> 0.9f    // 90% de chance en bas
                    heightRatio < 0.6f -> 0.6f    // 60% de chance au milieu
                    else -> 0.3f                  // 30% de chance en haut
                }
                
                // Espacement naturel avec variation aléatoire
                val lastLeafDistance = leaves.filter { it.stemIndex == -1 }
                    .minByOrNull { abs(it.pointIndex - i) }?.let { abs(it.pointIndex - i) } ?: 10
                
                val minDistance = 2 + (Math.random() * 2).toInt() // 2-4 points minimum (plus dense)
                
                if (lastLeafDistance >= minDistance && Math.random() < naturalProbability) {
                    availablePoints.add(i)
                }
            }
        }
        
        if (availablePoints.isNotEmpty()) {
            val pointIndex = availablePoints.random()
            val point = mainStem[pointIndex]
            
            // Répartition 360° VRAIMENT autour de la tige (côtés inclus)
            val existingLeaves = leaves.filter { it.stemIndex == -1 }
            val spiralAngle = (existingLeaves.size * 137.5f) % 360f // Spirale d'or
            
            // Variation pour naturel + priorité aux côtés
            val sideBonus = when {
                spiralAngle in 45f..135f -> 20f      // Favoriser côté droit
                spiralAngle in 225f..315f -> -20f    // Favoriser côté gauche
                else -> 0f
            }
            val naturalAngle = spiralAngle + sideBonus + (Math.random() * 40f - 20f).toFloat()
            val normalizedAngle = ((naturalAngle % 360f) + 360f) % 360f
            
            // Position latérale autour de la tige
            val radius = 8f + (Math.random() * 4f).toFloat() // 8-12px du centre de la tige
            val lateralX = point.x + cos(Math.toRadians(normalizedAngle.toDouble())).toFloat() * radius
            val lateralY = point.y + sin(Math.toRadians(normalizedAngle.toDouble())).toFloat() * radius * 0.3f
            
            val naturalSide = normalizedAngle > 180f
            
            // Taille selon la hauteur : feuilles basses plus grandes
            val heightRatio = (plantStem.getStemBaseY() - point.y) / plantStem.getMaxPossibleHeight()
            val heightMultiplier = 0.7f + (1f - heightRatio) * 0.8f // 0.7x à 1.5x selon hauteur
            val size = (baseLeafSize + (Math.random() * (maxLeafSize - baseLeafSize)).toFloat()) * heightMultiplier
            
            val personality = leafRenderer.generateLeafPersonality()
            
            val newLeaf = Leaf(
                x = lateralX, // Position latérale autour de la tige
                y = lateralY,
                size = size,
                angle = normalizedAngle, // Angle de sortie latéral
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
                    branchHeightRatio < 0.4f -> 0.8f    // 80% de chance vers la base
                    branchHeightRatio < 0.7f -> 0.5f    // 50% de chance au milieu
                    else -> 0.2f                        // 20% de chance vers le bout
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
            
            // Répartition 360° autour des branches aussi
            val existingBranchLeaves = leaves.filter { it.stemIndex == branchIndex }
            val spiralAngle = (existingBranchLeaves.size * 137.5f) % 360f
            
            // Variation plus importante sur les branches
            val branchAngle = spiralAngle + (Math.random() * 60f - 30f).toFloat()
            val normalizedBranchAngle = ((branchAngle % 360f) + 360f) % 360f
            
            // Position latérale autour de la branche
            val radius = 6f + (Math.random() * 3f).toFloat() // 6-9px du centre (plus petit que tige principale)
            val lateralX = point.x + cos(Math.toRadians(normalizedBranchAngle.toDouble())).toFloat() * radius
            val lateralY = point.y + sin(Math.toRadians(normalizedBranchAngle.toDouble())).toFloat() * radius * 0.2f
            
            val naturalSide = normalizedBranchAngle > 180f
            
            // Taille selon la hauteur sur la branche
            val heightRatio = (plantStem.getStemBaseY() - point.y) / plantStem.getMaxPossibleHeight()
            val heightMultiplier = 0.6f + (1f - heightRatio) * 0.6f // Plus petites sur branches
            val size = (baseLeafSize * 0.8f + (Math.random() * (maxLeafSize * 0.8f - baseLeafSize * 0.8f)).toFloat()) * heightMultiplier
            
            val personality = leafRenderer.generateLeafPersonality()
            
            val newLeaf = Leaf(
                x = lateralX, // Position latérale autour de la branche
                y = lateralY,
                size = size,
                angle = normalizedBranchAngle, // Angle de sortie latéral
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
