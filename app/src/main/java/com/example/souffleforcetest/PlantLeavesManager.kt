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
    private val leafCreationInterval = 300L // Une feuille toutes les 300ms
    private var basalLeavesCreated = false
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.25f
    private val baseLeafSize = 18f
    private val maxLeafSize = 45f
    private val basalLeafSize = 35f
    private val maxBasalLeafSize = 65f
    private val growthRate = 600f
    private val oscillationDecay = 0.96f
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun processLeavesGrowth(force: Float) {
        val currentTime = System.currentTimeMillis()
        
        // Créer les feuilles basales en premier (rosette)
        if (!basalLeavesCreated && force > forceThreshold) {
            createBasalLeaves()
            basalLeavesCreated = true
        }
        
        // Créer les feuilles caulinaires sur les tiges
        if (force > forceThreshold && currentTime - leafCreationTimer > leafCreationInterval) {
            createCaulineLeaves()
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
        basalLeavesCreated = false
    }
    
    fun createLeafPath(leaf: Leaf): Path {
        val path = Path()
        val currentSize = leaf.currentSize
        if (currentSize <= 0) return path
        
        val personality = leaf.personality
        val adjustedX = leaf.x + leaf.oscillation
        
        when (leaf.leafType) {
            LeafType.BASAL -> createBasalLeafPath(path, adjustedX, leaf.y, currentSize, personality, leaf.angle)
            LeafType.CAULINE -> createCaulineLeafPath(path, adjustedX, leaf.y, currentSize, personality, leaf.angle)
        }
        
        return path
    }
    
    fun getLeafColor(leaf: Leaf): Int {
        // Variation de couleur selon la personnalité
        val baseGreen = 34 + (leaf.personality.colorVariation * 40).toInt()
        val variance = (15 * leaf.personality.colorVariation).toInt()
        
        val r = (baseGreen - variance).coerceIn(20, 60)
        val g = (139 + variance).coerceIn(120, 180)
        val b = (baseGreen).coerceIn(20, 60)
        
        return android.graphics.Color.rgb(r, g, b)
    }
    
    // ==================== FONCTIONS PRIVÉES ====================
    
    private fun createBasalLeaves() {
        val baseX = plantStem.getStemBaseX()
        val baseY = plantStem.getStemBaseY()
        
        // Créer 6-8 feuilles basales en rosette
        val leafCount = 6 + (Math.random() * 3).toInt()
        
        for (i in 0 until leafCount) {
            val angle = (i * 360f / leafCount) + (Math.random() * 30f - 15f).toFloat()
            val distance = 40f + (Math.random() * 25f).toFloat()
            
            val leafX = baseX + cos(Math.toRadians(angle.toDouble())).toFloat() * distance
            val leafY = baseY + sin(Math.toRadians(angle.toDouble())).toFloat() * distance * 0.5f
            
            val size = basalLeafSize + (Math.random() * (maxBasalLeafSize - basalLeafSize)).toFloat()
            val personality = generateLeafPersonality(LeafType.BASAL)
            
            val basalLeaf = Leaf(
                x = leafX,
                y = leafY,
                size = size,
                angle = angle,
                stemIndex = -2, // Code spécial pour feuilles basales
                pointIndex = -1,
                maxSize = size,
                side = angle > 0,
                leafType = LeafType.BASAL,
                personality = personality
            )
            
            leaves.add(basalLeaf)
        }
    }
    
    private fun createCaulineLeaves() {
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
        
        // Chercher un point approprié pour une nouvelle feuille
        val availablePoints = mutableListOf<Int>()
        for (i in 3 until mainStem.size - 2) { // Éviter base et sommet
            val hasLeaf = leaves.any { it.stemIndex == -1 && it.pointIndex == i }
            if (!hasLeaf && i % 3 == 0) { // Espacement naturel
                availablePoints.add(i)
            }
        }
        
        if (availablePoints.isNotEmpty()) {
            val pointIndex = availablePoints.random()
            val point = mainStem[pointIndex]
            val side = (pointIndex % 2 == 0)
            
            val size = baseLeafSize + (Math.random() * (maxLeafSize - baseLeafSize)).toFloat()
            val leafAngle = if (side) 65f else -65f + (Math.random() * 40f - 20f).toFloat()
            val personality = generateLeafPersonality(LeafType.CAULINE)
            
            val newLeaf = Leaf(
                x = point.x,
                y = point.y,
                size = size,
                angle = leafAngle,
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
        for (i in 2 until branch.points.size - 1) {
            val hasLeaf = leaves.any { it.stemIndex == branchIndex && it.pointIndex == i }
            if (!hasLeaf && i % 4 == 0) { // Espacement plus large sur branches
                availablePoints.add(i)
            }
        }
        
        if (availablePoints.isNotEmpty()) {
            val pointIndex = availablePoints.random()
            val point = branch.points[pointIndex]
            val side = (pointIndex % 2 == 0)
            
            val size = baseLeafSize * 0.7f + (Math.random() * (maxLeafSize * 0.7f - baseLeafSize * 0.7f)).toFloat()
            val branchDirection = if (branch.angle > 0) 1f else -1f
            val leafAngle = branchDirection * 75f + (Math.random() * 50f - 25f).toFloat()
            val personality = generateLeafPersonality(LeafType.CAULINE)
            
            val newLeaf = Leaf(
                x = point.x,
                y = point.y,
                size = size,
                angle = leafAngle,
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
    
    private fun generateLeafPersonality(type: LeafType): LeafPersonality {
        return when (type) {
            LeafType.BASAL -> LeafPersonality(
                teethDepth = 0.25f + (Math.random() * 0.15f).toFloat(),
                teethCount = 6 + (Math.random() * 3).toInt(),
                widthRatio = 0.4f + (Math.random() * 0.2f).toFloat(),
                curvature = (Math.random() * 0.4f - 0.2f).toFloat(),
                asymmetry = (Math.random() * 0.15f).toFloat(),
                colorVariation = (Math.random() * 0.25f).toFloat()
            )
            LeafType.CAULINE -> LeafPersonality(
                teethDepth = 0.15f + (Math.random() * 0.1f).toFloat(),
                teethCount = 3 + (Math.random() * 3).toInt(),
                widthRatio = 0.3f + (Math.random() * 0.2f).toFloat(),
                curvature = (Math.random() * 0.3f - 0.15f).toFloat(),
                asymmetry = (Math.random() * 0.1f).toFloat(),
                colorVariation = (Math.random() * 0.2f).toFloat()
            )
        }
    }
    
    private fun createBasalLeafPath(path: Path, x: Float, y: Float, size: Float, personality: LeafPersonality, angle: Float): Path {
        // Feuille basale : grande, très dentée, allongée
        val length = size
        val width = size * personality.widthRatio
        
        // Points de la feuille avec dents
        val points = mutableListOf<Pair<Float, Float>>()
        
        // Côté gauche avec dents
        for (i in 0..personality.teethCount) {
            val t = i.toFloat() / personality.teethCount
            val baseY = -length * t
            val baseX = -width * 0.5f * sin(PI * t).toFloat() * (1f + personality.curvature * t)
            
            // Ajouter la dent
            val toothDepth = personality.teethDepth * width * sin(PI * t * 2).toFloat()
            val toothX = baseX + toothDepth
            
            points.add(Pair(toothX, baseY))
        }
        
        // Côté droit avec dents (symétrique mais avec asymétrie)
        for (i in personality.teethCount downTo 0) {
            val t = i.toFloat() / personality.teethCount
            val baseY = -length * t
            val baseX = width * 0.5f * sin(PI * t).toFloat() * (1f + personality.curvature * t)
            
            // Asymétrie
            val asymmetryOffset = personality.asymmetry * width * t
            
            // Ajouter la dent
            val toothDepth = personality.teethDepth * width * sin(PI * t * 2).toFloat()
            val toothX = baseX - toothDepth + asymmetryOffset
            
            points.add(Pair(toothX, baseY))
        }
        
        // Construire le path
        if (points.isNotEmpty()) {
            path.moveTo(x + points[0].first, y + points[0].second)
            for (i in 1 until points.size) {
                path.lineTo(x + points[i].first, y + points[i].second)
            }
            path.close()
        }
        
        return path
    }
    
    private fun createCaulineLeafPath(path: Path, x: Float, y: Float, size: Float, personality: LeafPersonality, angle: Float): Path {
        // Feuille caulinaire : plus petite, lobée
        val length = size * 0.8f
        val width = size * personality.widthRatio
        
        val points = mutableListOf<Pair<Float, Float>>()
        
        // Forme plus simple, lobée
        for (i in 0..personality.teethCount) {
            val t = i.toFloat() / personality.teethCount
            val baseY = -length * t
            val baseX = -width * 0.5f * sin(PI * t).toFloat()
            
            // Lobes plus doux
            val lobeDepth = personality.teethDepth * width * 0.7f
            val lobeX = baseX + lobeDepth * sin(PI * t * 3).toFloat()
            
            points.add(Pair(lobeX, baseY))
        }
        
        // Côté droit
        for (i in personality.teethCount downTo 0) {
            val t = i.toFloat() / personality.teethCount
            val baseY = -length * t
            val baseX = width * 0.5f * sin(PI * t).toFloat()
            
            val lobeDepth = personality.teethDepth * width * 0.7f
            val lobeX = baseX - lobeDepth * sin(PI * t * 3).toFloat()
            
            points.add(Pair(lobeX, baseY))
        }
        
        // Construire le path avec rotation
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
    
    private fun rotatePoint(x: Float, y: Float, cos: Float, sin: Float): Pair<Float, Float> {
        return Pair(
            x * cos - y * sin,
            x * sin + y * cos
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
                val oscillationStrength = if (leaf.leafType == LeafType.BASAL) 1.5f else 3f
                leaf.oscillation = sin(System.currentTimeMillis() * 0.003f) * forceVariation * oscillationStrength
            }
        }
    }
    
    private fun updateLeafOscillations() {
        for (leaf in leaves) {
            leaf.oscillation *= oscillationDecay
            
            // Limiter l'oscillation selon le type
            val maxOscillation = if (leaf.leafType == LeafType.BASAL) 3f else 5f
            leaf.oscillation = leaf.oscillation.coerceIn(-maxOscillation, maxOscillation)
        }
    }
}
