package com.example.souffleforcetest

import android.graphics.Canvas

// Enum pour les types de plantes
enum class PlantType(
    val maxBranches: Int,
    val branchingFromBase: Boolean,
    val leafDensity: Float,
    val leafLengthMultiplier: Float
) {
    MARGUERITE(
        maxBranches = 3, // Maximum 3 branches depuis la base
        branchingFromBase = true, // Ramification uniquement depuis la base
        leafDensity = 1.5f, // Plus de feuilles
        leafLengthMultiplier = 1.3f // Feuilles plus longues
    )
}

class PlantGrowthLogic(
    private var screenWidth: Int = 1080,
    private var screenHeight: Int = 2400
) {
    
    // ==================== DATA CLASSES ====================
    
    data class TracePoint(
        val x: Float, val y: Float, val strokeWidth: Float,
        val waveFrequency: Float, val waveAmplitude: Float, val curvature: Float
    )
    
    data class Branch(
        val id: Int,
        val startPoint: TracePoint,
        val tracedPath: MutableList<TracePoint>,
        var isActive: Boolean = true,
        val growthMultiplier: Float = 1f,
        var currentHeight: Float = 0f,
        var offsetX: Float = 0f,
        var currentStrokeWidth: Float = 0f,
        var fleur: Fleur? = null,
        val maxStrokeWidth: Float = 25.6f,
        val baseStrokeWidth: Float = 9.6f,
        val isFromBase: Boolean = true // Nouvelle propriété
    )
    
    data class Bourgeon(val x: Float, val y: Float, var taille: Float)
    data class Feuille(val bourgeon: Bourgeon, var longueur: Float, var largeur: Float, val angle: Float, var maxLargeurAtteinte: Boolean = false)
    data class Fleur(var x: Float, var y: Float, var taille: Float, var petalCount: Int, val sizeMultiplier: Float = 1f)
    
    // ==================== VARIABLES DE CROISSANCE ====================
    
    // Type de plante actuel
    private val currentPlantType = PlantType.MARGUERITE
    
    // Variables de force et croissance
    private var currentForce = 0.0f
    private var previousForce = 0.0f
    private var currentHeight = 0f
    private var currentStrokeWidth = 4f
    private var offsetX = 0f
    private var maxHeight = 0f
    private var baseX = 0f
    private var baseY = 0f
    
    // Collections de la plante
    val tracedPath = mutableListOf<TracePoint>() // Pour compatibilité
    val bourgeons = mutableListOf<Bourgeon>()
    val feuilles = mutableListOf<Feuille>()
    var fleur: Fleur? = null // Pour compatibilité
    
    // Système de branches
    private val branches = mutableListOf<Branch>()
    private var branchIdCounter = 0
    private var mainBranch: Branch? = null
    private var leafSideCounter = 0
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.08f
    private val growthRate = 174.6f
    private val baseStrokeWidth = 9.6f
    private val maxStrokeWidth = 25.6f
    private val strokeDecayRate = 0.2f
    private val abruptThreshold = 0.15f
    private val centeringRate = 0.99f
    private val maxWaveAmplitude = 15f
    
    // Paramètres des feuilles améliorés
    private val maxLeafWidth = 75f    // Augmenté de 60f à 75f
    private val maxLeafLength = 200f  // Augmenté de 160f à 200f (plus longues)
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun updateScreenSize(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
        baseX = width / 2.0f
        baseY = height - 100f
        maxHeight = height - 150f
        
        if (tracedPath.isEmpty()) {
            initializePlant()
        }
    }
    
    fun updateForce(force: Float, lightState: String) {
        when (lightState) {
            "GREEN_GROW" -> growAllBranches(force)
            "GREEN_LEAVES" -> growLeaves(force)
            "GREEN_FLOWER" -> growAllFlowers(force)
        }
    }
    
    fun drawPlant(canvas: Canvas, renderer: PlantRenderer, time: Float) {
        // Dessiner toutes les branches
        for (branch in branches.filter { it.isActive }) {
            renderer.drawRealisticStem(canvas, branch.tracedPath, time, baseStrokeWidth, maxStrokeWidth)
            
            if (branch.tracedPath.isNotEmpty()) {
                renderer.drawGrowthPoint(canvas, branch.tracedPath.last(), time)
            }
            
            // Dessiner la fleur de cette branche
            renderer.drawRealisticFlower(canvas, branch.fleur, time)
        }
        
        renderer.drawAttachmentPoints(canvas, bourgeons, time)
        renderer.drawRealistic3DLeaves(canvas, feuilles, time)
    }
    
    fun resetPlant() {
        tracedPath.clear()
        bourgeons.clear()
        feuilles.clear()
        fleur = null
        branches.clear()
        branchIdCounter = 0
        mainBranch = null
        
        currentHeight = 0f
        currentStrokeWidth = baseStrokeWidth
        offsetX = 0f
        leafSideCounter = 0
        
        initializePlant()
    }
    
    fun getMainGrowthPoint(): TracePoint? {
        return mainBranch?.tracedPath?.lastOrNull()
    }
    
    fun hasVisibleGrowth(): Boolean {
        return currentHeight > 30f
    }
    
    // ==================== FONCTIONS PRIVÉES ====================
    
    private fun initializePlant() {
        val initialPoint = TracePoint(baseX, baseY, baseStrokeWidth, 0f, 0f, 0f)
        tracedPath.add(initialPoint)
        
        mainBranch = Branch(
            id = branchIdCounter++,
            startPoint = initialPoint,
            tracedPath = mutableListOf(initialPoint),
            growthMultiplier = 1f,
            currentStrokeWidth = baseStrokeWidth,
            maxStrokeWidth = maxStrokeWidth,
            baseStrokeWidth = baseStrokeWidth,
            isFromBase = true
        )
        branches.add(mainBranch!!)
    }
    
    private fun growAllBranches(force: Float) {
        val rhythmIntensity = kotlin.math.abs(force - previousForce)
        previousForce = force
        
        // Créer nouvelle branche si bruit saccadé (MARGUERITE: seulement depuis la base)
        if (rhythmIntensity > abruptThreshold && branches.isNotEmpty()) {
            createNewBranchFromBase()
        }
        
        // Faire pousser toutes les branches actives
        for (branch in branches.filter { it.isActive }) {
            growBranch(branch, force, rhythmIntensity)
        }
        
        // Mettre à jour la compatibilité (branche principale)
        mainBranch?.let { main ->
            if (main.tracedPath.isNotEmpty()) {
                tracedPath.clear()
                tracedPath.addAll(main.tracedPath)
                currentHeight = main.currentHeight
                fleur = main.fleur
            }
        }
    }
    
    private fun createNewBranchFromBase() {
        // MARGUERITE : Ramification uniquement depuis la base
        if (!currentPlantType.branchingFromBase) return
        
        // Limiter le nombre de branches selon le type de plante
        val baseBranches = branches.filter { it.isFromBase }
        if (baseBranches.size >= currentPlantType.maxBranches) return
        
        // Créer une nouvelle branche depuis la base avec angle varié
        val branchAngle = when (baseBranches.size) {
            1 -> if ((0..1).random() == 0) 25f else -25f  // Première branche secondaire
            2 -> if (baseBranches.any { it.startPoint.x > baseX }) -35f else 35f  // Équilibrer
            else -> ((-40..40).random()).toFloat()  // Angle aléatoire pour les suivantes
        }
        
        // Position de départ légèrement décalée pour éviter superposition
        val baseOffset = (baseBranches.size * 15f) * if (branchAngle > 0) 1f else -1f
        val startX = baseX + baseOffset.coerceIn(-30f, 30f)
        
        val newBranchPoint = TracePoint(
            startX,
            baseY,
            baseStrokeWidth * 0.7f, // Branches secondaires plus fines
            0f, 0f, 0f
        )
        
        val growthVariation = 0.6f + (0..4).random() * 0.15f // Croissance variée
        
        val newBranch = Branch(
            id = branchIdCounter++,
            startPoint = newBranchPoint,
            tracedPath = mutableListOf(newBranchPoint),
            growthMultiplier = growthVariation,
            currentStrokeWidth = baseStrokeWidth * 0.7f,
            maxStrokeWidth = maxStrokeWidth * 0.7f,
            baseStrokeWidth = baseStrokeWidth * 0.7f,
            isFromBase = true
        )
        
        // Appliquer l'angle initial pour la direction de croissance
        newBranch.offsetX = kotlin.math.sin(Math.toRadians(branchAngle.toDouble())).toFloat() * 20f
        
        branches.add(newBranch)
    }
    
    private fun growBranch(branch: Branch, force: Float, rhythmIntensity: Float) {
        if (force > forceThreshold) {
            val adjustedForce = (force - forceThreshold) * branch.growthMultiplier
            val previousHeight = branch.currentHeight
            branch.currentHeight += adjustedForce * growthRate * 0.8f
            branch.currentHeight = kotlin.math.min(branch.currentHeight, maxHeight)
            
            if (branch.currentHeight > previousHeight && branch.currentHeight > 0) {
                val lastPoint = branch.tracedPath.last()
                val currentY = lastPoint.y - (branch.currentHeight - previousHeight)
                var currentX = lastPoint.x + branch.offsetX
                
                // Contraintes d'écran avec plus d'espace pour réduire superpositions
                currentX = currentX.coerceIn(120f, screenWidth - 120f)
                
                if (currentX < 180f || currentX > screenWidth - 180f) {
                    val centerX = screenWidth / 2f
                    val pullForce = 0.15f // Force de recentrage augmentée
                    currentX = currentX + (centerX - currentX) * pullForce
                }
                
                var waveFreq = 0f
                var waveAmp = 0f
                var curvature = 0f
                
                if (rhythmIntensity > 0.01f && rhythmIntensity < abruptThreshold) {
                    waveFreq = (rhythmIntensity * 8f * branch.growthMultiplier) + 1f
                    waveAmp = (rhythmIntensity * 60f * branch.growthMultiplier).coerceAtMost(maxWaveAmplitude) // Réduit
                }
                
                if (rhythmIntensity > 0.04f) {
                    curvature = (rhythmIntensity * 15f * branch.growthMultiplier).coerceAtMost(8f) // Réduit
                    if ((0..1).random() == 0) curvature = -curvature
                }
                
                val newPoint = TracePoint(currentX, currentY, branch.currentStrokeWidth, waveFreq, waveAmp, curvature)
                branch.tracedPath.add(newPoint)
            }
        }
        
        if (rhythmIntensity > abruptThreshold) {
            // Réduction des déplacements pour moins de superposition
            val displacement = if ((0..1).random() == 0) {
                (25f + rhythmIntensity * 35f) * branch.growthMultiplier // Réduit
            } else {
                -(25f + rhythmIntensity * 35f) * branch.growthMultiplier // Réduit
            }
            branch.offsetX += displacement
            branch.offsetX = branch.offsetX.coerceIn(-100f, 100f) // Plage réduite
            
            // Créer des bourgeons plus nombreux (pour plus de feuilles)
            if (branch.currentHeight > 30f && branch.tracedPath.size > 6) { // Seuil réduit
                createRealisticBud(branch)
            }
        }
        
        if (rhythmIntensity > 0.02f) {
            val thicknessIncrease = rhythmIntensity * 30f * branch.growthMultiplier
            branch.currentStrokeWidth = kotlin.math.min(branch.maxStrokeWidth, branch.baseStrokeWidth + thicknessIncrease)
        }
        
        if (branch.currentStrokeWidth > branch.baseStrokeWidth) {
            branch.currentStrokeWidth = kotlin.math.max(branch.baseStrokeWidth, branch.currentStrokeWidth - strokeDecayRate)
        }
        
        branch.offsetX *= 0.96f // Légèrement plus de rétention pour éviter oscillations
        
        if (branch.currentHeight < 40f && branches.size > 4) { // Seuil légèrement réduit
            branch.isActive = false
        }
    }
    
    private fun growLeaves(force: Float) {
        if (force > forceThreshold) {
            val adjustedForce = force - forceThreshold
            val growthIncrement = adjustedForce * growthRate * 0.08f
            
            for (bourgeon in bourgeons) {
                if (bourgeon.taille > 2f) {
                    var feuille = feuilles.find { it.bourgeon == bourgeon }
                    if (feuille == null) {
                        var closestBranchX = baseX
                        var minDistance = Float.MAX_VALUE
                        
                        for (branch in branches.filter { it.isActive }) {
                            for (point in branch.tracedPath) {
                                val distance = kotlin.math.sqrt(
                                    (point.x - bourgeon.x) * (point.x - bourgeon.x) + 
                                    (point.y - bourgeon.y) * (point.y - bourgeon.y)
                                )
                                if (distance < minDistance) {
                                    minDistance = distance
                                    closestBranchX = point.x
                                }
                            }
                        }
                        
                        val isRightSide = bourgeon.x > closestBranchX
                        val baseAngle = if (isRightSide) -25f else 205f
                        val heightFactor = bourgeon.y / screenHeight.toFloat()
                        val heightVariation = (heightFactor - 0.5f) * 30f
                        val randomVariation = ((-15..15).random()).toFloat()
                        val finalAngle = baseAngle + heightVariation + randomVariation
                        
                        feuille = Feuille(bourgeon, 0f, 0f, finalAngle, false)
                        feuilles.add(feuille)
                    }
                    
                    if (!feuille.maxLargeurAtteinte) {
                        val lengthGrowth = growthIncrement * 0.4f * currentPlantType.leafLengthMultiplier
                        val widthGrowth = growthIncrement * 0.45f
                        
                        feuille.longueur += lengthGrowth
                        feuille.largeur += widthGrowth
                        
                        if (feuille.largeur >= maxLeafWidth) {
                            feuille.largeur = maxLeafWidth
                            feuille.maxLargeurAtteinte = true
                        }
                        
                        feuille.longueur = kotlin.math.min(feuille.longueur, 100f)
                    } else {
                        val lengthGrowth = growthIncrement * 0.6f * currentPlantType.leafLengthMultiplier
                        feuille.longueur += lengthGrowth
                        feuille.longueur = kotlin.math.min(feuille.longueur, maxLeafLength)
                    }
                }
            }
        }
    }
    
    private fun growAllFlowers(force: Float) {
        if (force > forceThreshold) {
            val adjustedForce = force - forceThreshold
            val growthIncrement = adjustedForce * growthRate * 0.08f
            
            for (branch in branches.filter { it.isActive && it.tracedPath.isNotEmpty() }) {
                val topPoint = branch.tracedPath.last()
                
                if (branch.fleur == null) {
                    val sizeVariation = 0.7f + (0..6).random() * 0.1f
                    branch.fleur = Fleur(topPoint.x, topPoint.y, 0f, 6, sizeVariation)
                }
                
                branch.fleur?.let { flower ->
                    val branchGrowthIncrement = growthIncrement * branch.growthMultiplier
                    flower.taille += branchGrowthIncrement * 0.15f
                    flower.taille = kotlin.math.min(flower.taille, 175f * flower.sizeMultiplier)
                    flower.petalCount = kotlin.math.max(5, (flower.taille * 0.05f).toInt())
                    flower.x = topPoint.x
                    flower.y = topPoint.y
                }
            }
            
            mainBranch?.fleur?.let { fleur = it }
        }
    }
    
    // Méthode améliorée pour créer plus de bourgeons
    private fun createRealisticBud(branch: Branch) {
        // Plus de bourgeons selon la densité du type de plante
        val existingBudsOnBranch = bourgeons.count { bourgeon ->
            branch.tracedPath.any { point ->
                val distance = kotlin.math.sqrt(
                    (point.x - bourgeon.x) * (point.x - bourgeon.x) + 
                    (point.y - bourgeon.y) * (point.y - bourgeon.y)
                )
                distance < 50f
            }
        }
        
        // Plus de bourgeons pour plus de feuilles
        val maxBudsForBranch = kotlin.math.min(6, (branch.currentHeight / 80f * currentPlantType.leafDensity).toInt() + 2)
        if (existingBudsOnBranch >= maxBudsForBranch) return
        
        // Placement plus fréquent le long de la branche
        val minSegmentFromTop = 3 // Plus près du sommet
        val maxSegmentFromTop = kotlin.math.min(branch.tracedPath.size - 2, 12)
        
        if (maxSegmentFromTop <= minSegmentFromTop) return
        
        val segmentIndex = branch.tracedPath.size - (minSegmentFromTop..maxSegmentFromTop).random()
        val budPoint = branch.tracedPath[segmentIndex]
        
        // Alternance mais avec plus de variation
        leafSideCounter++
        val preferredSide = leafSideCounter % 2 == 0
        
        // Espacement vertical réduit pour plus de densité
        val sameHeightBuds = bourgeons.filter { kotlin.math.abs(it.y - budPoint.y) < 25f } // Réduit de 30f à 25f
        val hasRightBud = sameHeightBuds.any { it.x > budPoint.x }
        val hasLeftBud = sameHeightBuds.any { it.x < budPoint.x }
        
        val isRightSide = when {
            preferredSide && !hasRightBud -> true
            !preferredSide && !hasLeftBud -> false
            !hasRightBud -> true
            !hasLeftBud -> false
            else -> (0..1).random() == 0
        }
        
        // Position avec moins de décalage pour réduire superpositions
        val naturalOffset = if (isRightSide) {
            (2..6).random().toFloat() // Réduit
        } else {
            -(2..6).random().toFloat() // Réduit
        }
        
        val verticalJitter = ((-3..3).random()).toFloat() // Réduit
        
        val budX = budPoint.x + naturalOffset
        val budY = budPoint.y + verticalJitter
        
        val clampedBudX = budX.coerceIn(100f, screenWidth - 100f)
        
        bourgeons.add(Bourgeon(clampedBudX, budY, 3f))
    }
}
