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
        val stemIndex: Int,          // -1 pour tige principale, 0+ pour branches
        val pointIndex: Int,         // Index du point sur la tige
        var currentSize: Float = 0f,
        val maxSize: Float,
        val side: Boolean,           // true = droite, false = gauche
        var oscillation: Float = 0f,
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
    private val baseLeafSize = 35f
    private val maxLeafSize = 75f
    private val growthRate = 600f
    private val oscillationDecay = 0.96f
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun processLeavesGrowth(force: Float) {
        val currentTime = System.currentTimeMillis()
        
        // Créer des feuilles sur toutes les tiges (plus de rosette basale artificielle)
        if (force > forceThreshold && currentTime - leafCreationTimer > leafCreationInterval) {
            createRealisticLeaves()
            leafCreationTimer = currentTime
        }
        
        // Faire grandir les feuilles existantes
        growExistingLeaves(force)
        
        // Mettre à jour les oscillations et effet poids
        updateLeafPhysics()
        
        lastForce = force
    }
    
    fun resetLeaves() {
        leaves.clear()
        lastForce = 0f
        leafCreationTimer = 0L
    }
    
    fun createLeafPath(leaf: Leaf): Path {
        val path = Path()
        val currentSize = leaf.currentSize
        if (currentSize <= 0) return path
        
        val personality = leaf.personality
        val adjustedX = leaf.x + leaf.oscillation
        
        // Calculer l'angle réaliste avec effet poids
        val realAngle = calculateRealisticLeafAngle(leaf)
        
        // Toutes les feuilles utilisent la même forme (plus de distinction basal/cauline)
        createRealisticLeafPath(path, adjustedX, leaf.y, currentSize, personality, realAngle)
        
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
    
    private fun createRealisticLeaves() {
        // Créer feuilles sur la tige principale
        createLeavesOnMainStem()
        
        // Créer feuilles sur chaque branche
        for (branchIndex in plantStem.branches.indices) {
            createLeavesOnBranch(branchIndex)
        }
    }
    
    private fun calculateRealisticLeafAngle(leaf: Leaf): Float {
        val maturity = leaf.currentSize / leaf.maxSize
        val heightRatio = (plantStem.getStemBaseY() - leaf.y) / plantStem.getMaxPossibleHeight()
        
        // Commencer vertical (0°), puis s'écarter avec effet poids
        val baseAngle = 0f // Toutes commencent verticales
        
        // Écartement progressif selon la maturité
        val spreadAngle = if (leaf.side) 45f else -45f
        val maturitySpread = spreadAngle * maturity * maturity // Progression quadratique
        
        // Effet poids : les feuilles retombent
        val weightEffect = maturity * maturity * 30f // Plus matures = plus lourdes
        val finalWeight = if (heightRatio < 0.3f) weightEffect * 1.5f else weightEffect // Feuilles basses plus lourdes
        
        return baseAngle + maturitySpread + finalWeight
    }
    
    private fun createRealisticLeafPath(path: Path, x: Float, y: Float, size: Float, personality: LeafPersonality, angle: Float): Path {
        // Forme unique spatulée pour toutes les feuilles
        val length = size * 1.4f
        val maxWidth = size * 0.4f
        
        val points = mutableListOf<Pair<Float, Float>>()
        val lobeCount = 6 + (personality.teethCount % 4)
        
        // Créer la forme spatulée réaliste
        for (i in 0..lobeCount) {
            val t = i.toFloat() / lobeCount
            
            // Forme spatulée : étroite à la base, large au sommet
            val widthAtT = maxWidth * (0.15f + 0.85f * t * t)
            val yPos = -length * t
            
            // Lobes arrondis et naturels
            val baseX = -widthAtT * 0.5f
            val lobePhase = t * PI * 3.5f
            val lobeDepth = personality.teethDepth * widthAtT * 0.5f * sin(lobePhase).toFloat()
            val leftX = baseX + lobeDepth
            
            points.add(Pair(leftX, yPos))
        }
        
        // Côté droit avec asymétrie naturelle
        for (i in lobeCount downTo 0) {
            val t = i.toFloat() / lobeCount
            val widthAtT = maxWidth * (0.15f + 0.85f * t * t)
            val yPos = -length * t
            
            val baseX = widthAtT * 0.5f
            val lobePhase = t * PI * 3.5f
            val lobeDepth = personality.teethDepth * widthAtT * 0.5f * sin(lobePhase).toFloat()
            val asymmetryOffset = personality.asymmetry * widthAtT * t
            val rightX = baseX - lobeDepth + asymmetryOffset
            
            points.add(Pair(rightX, yPos))
        }
        
        // Rotation et construction du path
        val rad = Math.toRadians(angle.toDouble())
        val cos = cos(rad).toFloat()
        val sin = sin(rad).toFloat()
        
        if (points.isNotEmpty()) {
            val firstPoint = rotatePoint(points[0].first, points[0].second, cos, sin)
            path.moveTo(x + firstPoint.first, y + firstPoint.second)
            
            for (i in 1 until points.size) {
                val rotatedPoint = rotatePoint(points[i].first, points[i].second, cos, sin)
                path.lineTo(x + rotatedPoint.first, y + rotatedPoint.second)
            }
            path.close()
        }
        
        return path
    }
    
    private fun createLeavesOnMainStem() {
        val mainStem = plantStem.mainStem
        if (mainStem.size < 4) return
        
        // Chercher un point approprié pour une nouvelle feuille
        val availablePoints = mutableListOf<Int>()
        for (i in 2 until mainStem.size - 2) {
            val hasLeaf = leaves.any { it.stemIndex == -1 && it.pointIndex == i }
            if (!hasLeaf && i % 2 == 0) { // Plus de feuilles
                availablePoints.add(i)
            }
        }
        
        if (availablePoints.isNotEmpty()) {
            val pointIndex = availablePoints.random()
            val point = mainStem[pointIndex]
            val side = (pointIndex % 2 == 0)
            
            // Taille selon la hauteur : feuilles basses plus grandes
            val heightRatio = (plantStem.getStemBaseY() - point.y) / plantStem.getMaxPossibleHeight()
            val heightMultiplier = 0.7f + (1f - heightRatio) * 0.8f // 0.7x à 1.5x selon hauteur
            val size = (baseLeafSize + (Math.random() * (maxLeafSize - baseLeafSize)).toFloat()) * heightMultiplier
            
            val personality = generateLeafPersonality()
            
            val newLeaf = Leaf(
                x = point.x,
                y = point.y,
                size = size,
                angle = 0f, // Sera calculé dynamiquement
                stemIndex = -1,
                pointIndex = pointIndex,
                maxSize = size,
                side = side,
                leafType = LeafType.CAULINE,
                personality = personality
            )
            
            leaves.add(newLeaf)
        }
    }
    
    private fun createLeavesOnBranch(branchIndex: Int) {
        val branch = plantStem.branches[branchIndex]
        if (branch.points.size < 4) return
        
        // Chercher un point approprié pour une nouvelle feuille
        val availablePoints = mutableListOf<Int>()
        for (i in 1 until branch.points.size - 1) {
            val hasLeaf = leaves.any { it.stemIndex == branchIndex && it.pointIndex == i }
            if (!hasLeaf && i % 3 == 0) { // Espacement sur branches
                availablePoints.add(i)
            }
        }
        
        if (availablePoints.isNotEmpty()) {
            val pointIndex = availablePoints.random()
            val point = branch.points[pointIndex]
            val side = (pointIndex % 2 == 0)
            
            // Taille selon la hauteur sur la branche
            val heightRatio = (plantStem.getStemBaseY() - point.y) / plantStem.getMaxPossibleHeight()
            val heightMultiplier = 0.6f + (1f - heightRatio) * 0.6f // Plus petites sur branches
            val size = (baseLeafSize * 0.8f + (Math.random() * (maxLeafSize * 0.8f - baseLeafSize * 0.8f)).toFloat()) * heightMultiplier
            
            val personality = generateLeafPersonality()
            
            val newLeaf = Leaf(
                x = point.x,
                y = point.y,
                size = size,
                angle = 0f, // Sera calculé dynamiquement
                stemIndex = branchIndex,
                pointIndex = pointIndex,
                maxSize = size,
                side = side,
                leafType = LeafType.CAULINE,
                personality = personality
            )
            
            leaves.add(newLeaf)
        }
    }
    
    private fun generateLeafPersonality(): LeafPersonality {
        return LeafPersonality(
            teethDepth = 0.2f + (Math.random() * 0.15f).toFloat(),
            teethCount = 5 + (Math.random() * 4).toInt(),
            widthRatio = 0.35f + (Math.random() * 0.15f).toFloat(),
            curvature = (Math.random() * 0.3f - 0.15f).toFloat(),
            asymmetry = (Math.random() * 0.12f).toFloat(),
            colorVariation = (Math.random() * 0.2f).toFloat()
        )
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
                val forceVariation = abs(force - lastForce) * 2f
                leaf.oscillation = sin(System.currentTimeMillis() * 0.003f) * forceVariation * 2f
            }
        }
    }
    
    private fun updateLeafPhysics() {
        for (leaf in leaves) {
            // Oscillation réduite
            leaf.oscillation *= oscillationDecay
            
            // Limiter l'oscillation
            leaf.oscillation = leaf.oscillation.coerceIn(-4f, 4f)
        }
    }
    
    private fun rotatePoint(x: Float, y: Float, cos: Float, sin: Float): Pair<Float, Float> {
        return Pair(
            x * cos - y * sin,
            x * sin + y * cos
        )
    }
}
