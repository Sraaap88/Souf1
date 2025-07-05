package com.example.souffleforcetest

import android.graphics.Canvas

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
        val baseStrokeWidth: Float = 9.6f
    )
    
    data class Bourgeon(val x: Float, val y: Float, var taille: Float)
    data class Feuille(val bourgeon: Bourgeon, var longueur: Float, var largeur: Float, val angle: Float, var maxLargeurAtteinte: Boolean = false)
    data class Fleur(var x: Float, var y: Float, var taille: Float, var petalCount: Int, val sizeMultiplier: Float = 1f)
    
    // ==================== VARIABLES DE CROISSANCE ====================
    
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
    
    // Paramètres des feuilles (réduits de 20% en plus)
    private val maxLeafWidth = 60f    // 75f * 0.8 = 60f
    private val maxLeafLength = 160f  // 200f * 0.8 = 160f
    
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
            baseStrokeWidth = baseStrokeWidth
        )
        branches.add(mainBranch!!)
    }
    
    private fun growAllBranches(force: Float) {
        val rhythmIntensity = kotlin.math.abs(force - previousForce)
        previousForce = force
        
        // Créer nouvelle branche si bruit saccadé
        if (rhythmIntensity > abruptThreshold && branches.isNotEmpty()) {
            createNewBranch()
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
    
    private fun createNewBranch() {
        val parentBranch = branches.filter { it.isActive && it.tracedPath.size > 5 }.randomOrNull()
        parentBranch?.let { parent ->
            val branchPointIndex = (parent.tracedPath.size * 0.6f).toInt().coerceAtMost(parent.tracedPath.size - 1)
            val branchPoint = parent.tracedPath[branchPointIndex]
            
            val growthVariation = 0.7f + (0..6).random() * 0.1f
            val branchAngle = (15..25).random().toFloat() * if ((0..1).random() == 0) 1f else -1f
            val branchOffset = Math.sin(Math.toRadians(branchAngle.toDouble())).toFloat() * 15f
            
            val newBaseStroke = parent.baseStrokeWidth * 0.5f
            val newMaxStroke = parent.maxStrokeWidth * 0.5f
            
            val newBranchPoint = TracePoint(
                branchPoint.x + branchOffset,
                branchPoint.y,
                newBaseStroke,
                0f, 0f, 0f
            )
            
            val newBranch = Branch(
                id = branchIdCounter++,
                startPoint = newBranchPoint,
                tracedPath = mutableListOf(newBranchPoint),
                growthMultiplier = growthVariation,
                currentStrokeWidth = newBaseStroke,
                maxStrokeWidth = newMaxStroke,
                baseStrokeWidth = newBaseStroke
            )
            
            branches.add(newBranch)
        }
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
                
                // Contraintes d'écran strictes
                currentX = currentX.coerceIn(100f, screenWidth - 100f)
                
                if (currentX < 150f || currentX > screenWidth - 150f) {
                    val centerX = screenWidth / 2f
                    val pullForce = 0.1f
                    currentX = currentX + (centerX - currentX) * pullForce
                }
                
                var waveFreq = 0f
                var waveAmp = 0f
                var curvature = 0f
                
                if (rhythmIntensity > 0.01f && rhythmIntensity < abruptThreshold) {
                    waveFreq = (rhythmIntensity * 8f * branch.growthMultiplier) + 1f
                    waveAmp = (rhythmIntensity * 80f * branch.growthMultiplier).coerceAtMost(maxWaveAmplitude)
                }
                
                if (rhythmIntensity > 0.04f) {
                    curvature = (rhythmIntensity * 20f * branch.growthMultiplier).coerceAtMost(10f)
                    if ((0..1).random() == 0) curvature = -curvature
                }
                
                val newPoint = TracePoint(currentX, currentY, branch.currentStrokeWidth, waveFreq, waveAmp, curvature)
                branch.tracedPath.add(newPoint)
            }
        }
        
        if (rhythmIntensity > abruptThreshold) {
            val displacement = if ((0..1).random() == 0) {
                (40f + rhythmIntensity * 50f) * branch.growthMultiplier
            } else {
                -(40f + rhythmIntensity * 50f) * branch.growthMultiplier
            }
            branch.offsetX += displacement
            branch.offsetX = branch.offsetX.coerceIn(-150f, 150f)
            
            // NOUVEAU : Créer des bourgeons de façon plus réaliste
            if (branch.currentHeight > 40f && branch.tracedPath.size > 8) {
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
        
        branch.offsetX *= 0.95f
        
        if (branch.currentHeight < 50f && branches.size > 3) {
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
                        val lengthGrowth = growthIncrement * 0.3f
                        val widthGrowth = growthIncrement * 0.35f
                        
                        feuille.longueur += lengthGrowth
                        feuille.largeur += widthGrowth
                        
                        if (feuille.largeur >= maxLeafWidth) {
                            feuille.largeur = maxLeafWidth
                            feuille.maxLargeurAtteinte = true
                        }
                        
                        feuille.longueur = kotlin.math.min(feuille.longueur, 80f) // 100f * 0.8 = 80f
                    } else {
                        val lengthGrowth = growthIncrement * 0.5f
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
    
    // NOUVELLE méthode réaliste pour créer des bourgeons
    private fun createRealisticBud(branch: Branch) {
        // Ne pas créer trop de bourgeons sur une même branche
        val existingBudsOnBranch = bourgeons.count { bourgeon ->
            branch.tracedPath.any { point ->
                val distance = kotlin.math.sqrt(
                    (point.x - bourgeon.x) * (point.x - bourgeon.x) + 
                    (point.y - bourgeon.y) * (point.y - bourgeon.y)
                )
                distance < 50f // Bourgeon appartient à cette branche si < 50px
            }
        }
        
        // Maximum 3-4 bourgeons par branche selon sa taille
        val maxBudsForBranch = kotlin.math.min(4, (branch.currentHeight / 100f).toInt() + 1)
        if (existingBudsOnBranch >= maxBudsForBranch) return
        
        // Placement le long de la branche avec espacement naturel
        val minSegmentFromTop = 5 // Pas trop près du sommet
        val maxSegmentFromTop = kotlin.math.min(branch.tracedPath.size - 3, 15) // Pas trop près de la base
        
        if (maxSegmentFromTop <= minSegmentFromTop) return
        
        val segmentIndex = branch.tracedPath.size - (minSegmentFromTop..maxSegmentFromTop).random()
        val budPoint = branch.tracedPath[segmentIndex]
        
        // Alternance naturelle mais pas systématique
        leafSideCounter++
        val preferredSide = leafSideCounter % 2 == 0
        
        // Vérifier s'il n'y a pas déjà un bourgeon proche de ce côté
        val sameHeightBuds = bourgeons.filter { kotlin.math.abs(it.y - budPoint.y) < 30f }
        val hasRightBud = sameHeightBuds.any { it.x > budPoint.x }
        val hasLeftBud = sameHeightBuds.any { it.x < budPoint.x }
        
        // Choisir le côté selon la préférence et la disponibilité
        val isRightSide = when {
            preferredSide && !hasRightBud -> true
            !preferredSide && !hasLeftBud -> false
            !hasRightBud -> true
            !hasLeftBud -> false
            else -> (0..1).random() == 0 // Aléatoire si les deux côtés occupés
        }
        
        // Position plus naturelle : directement sur la tige mais légèrement décalée
        val naturalOffset = if (isRightSide) {
            (3..8).random().toFloat()
        } else {
            -(3..8).random().toFloat()
        }
        
        // Petit décalage vertical pour plus de naturel
        val verticalJitter = ((-5..5).random()).toFloat()
        
        val budX = budPoint.x + naturalOffset
        val budY = budPoint.y + verticalJitter
        
        // S'assurer que le bourgeon reste dans l'écran
        val clampedBudX = budX.coerceIn(80f, screenWidth - 80f)
        
        bourgeons.add(Bourgeon(clampedBudX, budY, 3f))
    }
}
