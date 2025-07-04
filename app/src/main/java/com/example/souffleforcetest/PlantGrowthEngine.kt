package com.example.souffleforcetest

class PlantGrowthEngine(
    private var screenWidth: Int = 1080,
    private var screenHeight: Int = 2400
) {
    
    // ==================== VARIABLES DE CROISSANCE ====================
    
    private val currentPlantType = PlantType.MARGUERITE
    
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
    
    private val forceThreshold = 0.08f
    private val growthRate = 174.6f
    private val baseStrokeWidth = 9.6f
    private val maxStrokeWidth = 25.6f
    private val strokeDecayRate = 0.2f
    private val abruptThreshold = 0.15f
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
    
    fun updateForce(force: Float, lightState: String) {
        when (lightState) {
            "GREEN_GROW" -> growAllBranches(force)
            // Les feuilles et fleurs sont gérées par PlantGrowthFeatures
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
    
    fun addBourgeon(bourgeon: Bourgeon) {
        bourgeons.add(bourgeon)
    }
    
    fun getBourgeons(): MutableList<Bourgeon> = bourgeons
    fun getFeuilles(): MutableList<Feuille> = feuilles
    
    // ==================== CROISSANCE DES BRANCHES ====================
    
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
        
        if (rhythmIntensity > abruptThreshold && branches.isNotEmpty()) {
            createNewBranchFromBase()
        }
        
        for (branch in branches.filter { it.isActive }) {
            growBranch(branch, force, rhythmIntensity)
        }
        
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
        if (!currentPlantType.branchingFromBase) return
        
        val baseBranches = branches.filter { it.isFromBase }
        if (baseBranches.size >= currentPlantType.maxBranches) return
        
        val branchAngle = when (baseBranches.size) {
            1 -> if ((0..1).random() == 0) 25f else -25f
            2 -> if (baseBranches.any { it.startPoint.x > baseX }) -35f else 35f
            else -> ((-40..40).random()).toFloat()
        }
        
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
            
            if (branch.currentHeight > 30f && branch.tracedPath.size > 6) {
                // La création de bourgeons sera gérée par PlantGrowthFeatures
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
    
    // Suite dans le prochain fichier...
}
