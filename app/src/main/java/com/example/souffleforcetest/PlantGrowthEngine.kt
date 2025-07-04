package com.example.souffleforcetest

class PlantGrowthEngine(
    private var screenWidth: Int = 1080,
    private var screenHeight: Int = 2400
) {
    
    // ==================== VARIABLES DE CROISSANCE ====================
    
    private var currentForce = 0.0f
    private var previousForce = 0.0f
    private var currentHeight = 0f
    private var currentStrokeWidth = 4f
    private var offsetX = 0f
    private var maxHeight = 0f
    private var baseX = 0f
    private var baseY = 0f
    
    // Collections de la plante
    val tracedPath = mutableListOf<TracePoint>()
    val bourgeons = mutableListOf<Bourgeon>()
    val feuilles = mutableListOf<Feuille>()
    var fleur: Fleur? = null
    
    // Système de branches
    private val branches = mutableListOf<Branch>()
    private var branchIdCounter = 0
    private var mainBranch: Branch? = null
    private var leafSideCounter = 0
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.035f // Plus sensible (0.055f → 0.035f)
    private val growthRate = 220f // Plus rapide (174.6f → 220f)
    private val baseStrokeWidth = 9.6f
    private val maxStrokeWidth = 25.6f
    private val strokeDecayRate = 0.2f
    private val abruptThreshold = 0.22f // Plus difficile pour 2ème branche (0.18f → 0.22f)
    private val maxWaveAmplitude = 15f
    private val maxLeafWidth = 75f
    private val maxLeafLength = 200f
    
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
    
    fun growAllBranches(force: Float) {
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
    
    fun getBranches(): List<Branch> {
        return branches.filter { it.isActive }
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
    
    private fun createNewBranchFromBase() {
        // MARGUERITE : Ramification PROGRESSIVE avec difficulté croissante
        val baseBranches = branches.filter { it.isFromBase }
        if (baseBranches.size >= 3) return // Maximum 3 branches
        
        // NOUVEAU : Difficulté progressive selon le nombre de branches
        val mainBranchHeight = mainBranch?.currentHeight ?: 0f
        when (baseBranches.size) {
            1 -> {
                // 2ème branche : DIFFICILE
                if (mainBranchHeight < 180f) return // Plus élevé (120f → 180f)
            }
            2 -> {
                // 3ème branche : UN PEU PLUS DIFFICILE (pas impossible)
                if (mainBranchHeight < 220f) return // Juste un peu plus (280f → 220f)
                
                // Condition supplémentaire réduite
                val otherBranchesHeight = baseBranches.filter { it != mainBranch }.minOfOrNull { it.currentHeight } ?: 0f
                if (otherBranchesHeight < 60f) return // Réduit (80f → 60f)
            }
        }
        
        // Créer une nouvelle branche depuis la base avec angle varié
        val branchAngle = when (baseBranches.size) {
            1 -> if ((0..1).random() == 0) 25f else -25f
            2 -> if (baseBranches.any { it.startPoint.x > baseX }) -35f else 35f
            else -> ((-40..40).random()).toFloat()
        }
        
        // Position de départ légèrement décalée pour éviter superposition
        val baseOffset = (baseBranches.size * 15f) * if (branchAngle > 0) 1f else -1f
        val startX = baseX + baseOffset.coerceIn(-30f, 30f)
        
        val newBranchPoint = TracePoint(startX, baseY, baseStrokeWidth * 0.7f, 0f, 0f, 0f)
        val growthVariation = 0.6f + (0..4).random() * 0.15f
        
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
                    val pullForce = 0.15f
                    currentX = currentX + (centerX - currentX) * pullForce
                }
                
                var waveFreq = 0f
                var waveAmp = 0f
                var curvature = 0f
                
                if (rhythmIntensity > 0.01f && rhythmIntensity < abruptThreshold) {
                    waveFreq = (rhythmIntensity * 8f * branch.growthMultiplier) + 1f
                    waveAmp = (rhythmIntensity * 60f * branch.growthMultiplier).coerceAtMost(maxWaveAmplitude)
                }
                
                if (rhythmIntensity > 0.04f) {
                    curvature = (rhythmIntensity * 15f * branch.growthMultiplier).coerceAtMost(8f)
                    if ((0..1).random() == 0) curvature = -curvature
                }
                
                val newPoint = TracePoint(currentX, currentY, branch.currentStrokeWidth, waveFreq, waveAmp, curvature)
                branch.tracedPath.add(newPoint)
            }
        }
        
        if (rhythmIntensity > abruptThreshold) {
            val displacement = if ((0..1).random() == 0) {
                (25f + rhythmIntensity * 35f) * branch.growthMultiplier
            } else {
                -(25f + rhythmIntensity * 35f) * branch.growthMultiplier
            }
            branch.offsetX += displacement
            branch.offsetX = branch.offsetX.coerceIn(-100f, 100f)
            
            // Créer des bourgeons plus nombreux et PLUS HAUT (jusqu'à 20% du haut)
            if (branch.currentHeight > 20f && branch.tracedPath.size > 4) { // Seuil encore réduit (24f → 20f)
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
        
        branch.offsetX *= 0.96f
        
        if (branch.currentHeight < 40f && branches.size > 4) {
            branch.isActive = false
        }
    }
    
    // Méthode améliorée pour créer plus de bourgeons
    private fun createRealisticBud(branch: Branch) {
        val existingBudsOnBranch = bourgeons.count { bourgeon ->
            branch.tracedPath.any { point ->
                val distance = kotlin.math.sqrt(
                    (point.x - bourgeon.x) * (point.x - bourgeon.x) + 
                    (point.y - bourgeon.y) * (point.y - bourgeon.y)
                )
                distance < 50f
            }
        }
        
        // Plus de bourgeons pour 25% plus de feuilles (version qui marchait)
        val maxBudsForBranch = kotlin.math.min(8, (branch.currentHeight / 64f * 1.875f).toInt() + 3)
        if (existingBudsOnBranch >= maxBudsForBranch) return
        
        val minSegmentFromTop = 2 // Version qui marchait
        val maxSegmentFromTop = kotlin.math.min(branch.tracedPath.size - 1, 15)
        
        if (maxSegmentFromTop <= minSegmentFromTop) return
        
        val segmentIndex = branch.tracedPath.size - (minSegmentFromTop..maxSegmentFromTop).random()
        val budPoint = branch.tracedPath[segmentIndex]
        
        leafSideCounter++
        val preferredSide = leafSideCounter % 2 == 0
        
        val sameHeightBuds = bourgeons.filter { kotlin.math.abs(it.y - budPoint.y) < 25f }
        val hasRightBud = sameHeightBuds.any { it.x > budPoint.x }
        val hasLeftBud = sameHeightBuds.any { it.x < budPoint.x }
        
        val isRightSide = when {
            preferredSide && !hasRightBud -> true
            !preferredSide && !hasLeftBud -> false
            !hasRightBud -> true
            !hasLeftBud -> false
            else -> (0..1).random() == 0
        }
        
        val naturalOffset = if (isRightSide) {
            (2..6).random().toFloat()
        } else {
            -(2..6).random().toFloat()
        }
        
        val verticalJitter = ((-3..3).random()).toFloat()
        
        val budX = budPoint.x + naturalOffset
        val budY = budPoint.y + verticalJitter
        
        val clampedBudX = budX.coerceIn(100f, screenWidth - 100f)
        
        bourgeons.add(Bourgeon(clampedBudX, budY, 3f))
    }
}
