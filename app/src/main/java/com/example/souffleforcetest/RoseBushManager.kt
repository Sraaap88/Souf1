package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import kotlin.math.*

class RoseBushManager(private val screenWidth: Int, private val screenHeight: Int) {
    
    // ==================== DATA CLASSES ====================
    
    data class RoseBranch(
        val points: MutableList<BranchPoint> = mutableListOf(),
        var currentLength: Float = 0f,
        val maxLength: Float,
        val angle: Float,  // Angle de base de croissance
        var isActive: Boolean = true,
        val id: String = generateBranchId(),
        var generationLevel: Int = 0,  // Niveau de génération (0=principale, 1=1ère division, etc.)
        var splitsCount: Int = 0       // Nombre de fois que cette tige a été divisée
    )
    
    data class BranchPoint(
        val x: Float,
        val y: Float,
        val thickness: Float
    )
    
    data class RoseLeaf(
        val branchIndex: Int,
        val positionRatio: Float,
        var currentSize: Float = 0f,
        val maxSize: Float,
        val angle: Float,
        val side: Int,
        val folioleCount: Int = 5 + (Math.random() * 3).toInt(),
        val folioleVariations: List<Float> = generateFolioleVariations()
    ) {
        companion object {
            private fun generateFolioleVariations(): List<Float> {
                return (0..7).map { Math.random().toFloat() }
            }
        }
    }
    
    data class RoseFlower(
        val branchIndex: Int,
        val x: Float,
        val y: Float,
        var currentSize: Float = 0f,
        val maxSize: Float,
        val id: String = generateFlowerId()
    )
    
    data class PendingSplit(
        val branchId: String,
        val scheduledTime: Long  // Quand effectuer la division
    )
    
    // ==================== VARIABLES PRINCIPALES ====================
    
    private val branches = mutableListOf<RoseBranch>()
    private val leaves = mutableListOf<RoseLeaf>()
    private val flowers = mutableListOf<RoseFlower>()
    
    private var baseX = 0f
    private var baseY = 0f
    private var lastForce = 0f
    private var lastSpikeTime = 0L
    
    // Variables pour la ramification en cascade
    private var pendingSplits = mutableListOf<PendingSplit>()
    
    // Référence au gestionnaire de défis
    private var challengeManager: ChallengeManager? = null
    
    // ==================== PARAMÈTRES ====================
    
    private val spikeThreshold = 0.4f
    private val spikeMinInterval = 300L
    private val branchGrowthRate = 3000f
    private val leafGrowthRate = 800f
    private val flowerGrowthRate = 500f
    
    // Tailles
    private val baseBranchThickness = 15f  
    private val segmentLength = 30f
    private val baseLeafSize = 80f  
    private val baseFlowerSize = 35f
    
    // Paramètres pour tiges tortueuses arbustives
    private val tortuosityFactor = 20f  // Amplitude des courbures
    private val tortuosityFrequency = 0.25f  // Fréquence des changements d'angle
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun setChallengeManager(manager: ChallengeManager) {
        challengeManager = manager
    }
    
    fun initialize(centerX: Float, bottomY: Float) {
        baseX = centerX
        baseY = bottomY
        
        // Créer 4 tiges principales pour un aspect arbustif
        val startingAngles = listOf(-45f, -15f, 15f, 45f)  // Angles de départ horizontaux-montants
        
        for (i in startingAngles.indices) {
            val branch = RoseBranch(
                maxLength = screenHeight * 1.8f,  // Tiges principales longues
                angle = startingAngles[i]  // Angle de départ
            )
            
            // Commencer avec 2 points pour pouvoir pousser immédiatement
            branch.points.add(BranchPoint(baseX, baseY, baseBranchThickness))
            
            // Deuxième point dans la direction initiale
            val angleRad = Math.toRadians(startingAngles[i].toDouble())
            val secondX = baseX + cos(angleRad).toFloat() * (segmentLength * 0.3f)
            val secondY = baseY + sin(angleRad).toFloat() * (segmentLength * 0.3f)
            branch.points.add(BranchPoint(secondX, secondY, baseBranchThickness * 0.98f))
            branch.currentLength = segmentLength * 0.3f
            
            branches.add(branch)
        }
    }
    
    fun processStemGrowth(force: Float) {
        // Traiter les divisions en cascade programmées
        processPendingSplits()
        
        // Détecter les saccades pour diviser les tiges
        detectSpikeAndSplit(force)
        
        // Faire pousser toutes les tiges actives
        growActiveBranches(force)
        
        lastForce = force
    }
    
    fun processLeavesGrowth(force: Float) {
        createLeavesOnBranches()
        growExistingLeaves(force)
    }
    
    fun processFlowerGrowth(force: Float) {
        createFlowersOnBranches()
        growExistingFlowers(force)
    }
    
    fun reset() {
        branches.clear()
        leaves.clear()
        flowers.clear()
        lastForce = 0f
        lastSpikeTime = 0L
        pendingSplits.clear()
    }
    
    fun drawRoseBush(canvas: Canvas, branchPaint: Paint, leafPaint: Paint, flowerPaint: Paint) {
        drawBranches(canvas, branchPaint)
        drawLeaves(canvas, leafPaint)
        drawFlowers(canvas, flowerPaint)
    }
    
    // ==================== DIVISION DES TIGES AMÉLIORÉE ====================
    
    private fun detectSpikeAndSplit(force: Float) {
        val currentTime = System.currentTimeMillis()
        
        // Détecter une saccade
        val forceIncrease = force - lastForce
        val isSpike = forceIncrease > spikeThreshold && force > 0.4f
        val canSplit = currentTime - lastSpikeTime > spikeMinInterval
        
        if (isSpike && canSplit) {
            // Diviser les tiges actives qui sont assez développées
            val eligibleBranches = branches.filter { 
                it.isActive && 
                it.currentLength > 60f &&
                it.points.size >= 4
            }
            
            for (branch in eligibleBranches) {
                splitBranchInTwo(branch)
                
                // Programmer une 2ème division aléatoire 500ms plus tard
                val delayedSplitTime = currentTime + 500L
                pendingSplits.add(PendingSplit(branch.id, delayedSplitTime))
            }
            
            lastSpikeTime = currentTime
        }
    }
    
    private fun processPendingSplits() {
        val currentTime = System.currentTimeMillis()
        val splitsToProcess = pendingSplits.filter { currentTime >= it.scheduledTime }
        
        for (pendingSplit in splitsToProcess) {
            // Trouver une des nouvelles branches créées pour la diviser à nouveau
            val recentBranches = branches.filter { 
                it.isActive && 
                it.currentLength > 40f &&
                it.generationLevel > 0
            }
            
            if (recentBranches.isNotEmpty()) {
                // Choisir aléatoirement une branche récente à diviser
                val branchToSplit = recentBranches.random()
                splitBranchInTwo(branchToSplit)
            }
        }
        
        // Supprimer les divisions traitées
        pendingSplits.removeAll(splitsToProcess)
    }
    
    private fun splitBranchInTwo(branch: RoseBranch) {
        if (branch.points.size < 3) return
        
        val splitPoint = branch.points.last()
        val baseAngle = getCurrentGrowthAngle(branch)
        
        // Créer deux nouvelles tiges en Y
        val leftAngle = baseAngle - 25f
        val rightAngle = baseAngle + 25f
        
        // Calculer la réduction de vitesse selon le nombre de divisions
        val speedReduction = 1f - (branch.splitsCount / 2 * 0.15f).coerceAtMost(0.6f)
        val newMaxLength = branch.maxLength * speedReduction
        
        val leftBranch = RoseBranch(
            maxLength = newMaxLength,
            angle = leftAngle,
            generationLevel = branch.generationLevel + 1,
            splitsCount = 0
        )
        
        val rightBranch = RoseBranch(
            maxLength = newMaxLength,
            angle = rightAngle,
            generationLevel = branch.generationLevel + 1,
            splitsCount = 0
        )
        
        // Initialiser chaque nouvelle tige avec 2 points
        for ((newBranch, angle) in listOf(leftBranch to leftAngle, rightBranch to rightAngle)) {
            newBranch.points.add(BranchPoint(splitPoint.x, splitPoint.y, splitPoint.thickness * 0.9f))
            
            val angleRad = Math.toRadians(angle.toDouble())
            val secondX = splitPoint.x + cos(angleRad).toFloat() * (segmentLength * 0.3f)
            val secondY = splitPoint.y + sin(angleRad).toFloat() * (segmentLength * 0.3f)
            newBranch.points.add(BranchPoint(secondX, secondY, splitPoint.thickness * 0.88f))
            newBranch.currentLength = segmentLength * 0.3f
        }
        
        branches.add(leftBranch)
        branches.add(rightBranch)
        
        // Incrémenter le compteur de divisions de la branche mère
        branch.splitsCount++
        
        // Notifier le challengeManager qu'une division a été créée
        challengeManager?.notifyDivisionCreated("division_${leftBranch.id}_${rightBranch.id}")
        
        // Arrêter la croissance de la tige mère
        branch.isActive = false
    }
    
    private fun getCurrentGrowthAngle(branch: RoseBranch): Float {
        // Redressement progressif + tortuosité pour aspect arbustif
        val baseAngle = branch.angle
        val progressRatio = (branch.currentLength / branch.maxLength).coerceAtMost(1f)
        
        // Redressement progressif vers le ciel (-90°)
        val targetAngle = -90f
        val redirectionFactor = (progressRatio * 0.7f).coerceAtMost(0.7f)
        val redirectedAngle = baseAngle + (targetAngle - baseAngle) * redirectionFactor
        
        // Tortuosité naturelle avec courbures plus marquées
        val tortuosityIntensity = tortuosityFactor * 1.5f
        val tortuosity = sin(branch.points.size * tortuosityFrequency) * tortuosityIntensity
        
        // Courbures supplémentaires pour aspect plus organique
        val secondaryWave = cos(branch.points.size * tortuosityFrequency * 0.3f) * (tortuosityFactor * 0.8f)
        
        return redirectedAngle + tortuosity + secondaryWave
    }
    
    // ==================== CROISSANCE DES TIGES ====================
    
    private fun growActiveBranches(force: Float) {
        for (branch in branches.filter { it.isActive }) {
            // Pousse SEULEMENT si on souffle
            if (force > 0.15f && branch.currentLength < branch.maxLength) {
                
                // Ralentir la croissance près du haut de l'écran (80%)
                val screenLimit = screenHeight * 0.8f
                val currentY = if (branch.points.isNotEmpty()) branch.points.last().y else screenHeight.toFloat()
                val distanceFromTop = screenLimit - currentY
                val slowdownFactor = when {
                    distanceFromTop > 200f -> 1f
                    distanceFromTop > 0f -> (distanceFromTop / 200f).coerceAtLeast(0.3f)
                    else -> 0.1f
                }
                
                // Réduction de vitesse selon le niveau de génération
                val generationSlowdown = 1f - (branch.generationLevel * 0.1f).coerceAtMost(0.5f)
                
                // Croissance avec tous les facteurs de ralentissement
                val baseGrowth = force * branchGrowthRate * 0.020f
                val finalGrowth = baseGrowth * slowdownFactor * generationSlowdown
                
                branch.currentLength = (branch.currentLength + finalGrowth).coerceAtMost(branch.maxLength)
                
                // Ajouter un nouveau point si nécessaire
                if (branch.points.size >= 2 && branch.currentLength >= branch.points.size * segmentLength) {
                    val lastPoint = branch.points.last()
                    
                    // Calculer l'angle avec tortuosité naturelle
                    val currentAngle = getCurrentGrowthAngle(branch)
                    val angleRad = Math.toRadians(currentAngle.toDouble())
                    
                    val newX = lastPoint.x + cos(angleRad).toFloat() * segmentLength
                    val newY = lastPoint.y + sin(angleRad).toFloat() * segmentLength
                    val newThickness = (lastPoint.thickness * 0.96f).coerceAtLeast(3f)
                    
                    // Arrêter complètement si on atteint le haut de l'écran
                    if (newY > 50f) {
                        branch.points.add(BranchPoint(newX, newY, newThickness))
                    } else {
                        branch.isActive = false
                    }
                }
                
                // Arrêter quand on atteint la longueur max
                if (branch.currentLength >= branch.maxLength * 0.95f) {
                    branch.isActive = false
                }
            }
        }
    }
    
    // ==================== FEUILLES ====================
    
    private fun createLeavesOnBranches() {
        for ((index, branch) in branches.withIndex()) {
            if (branch.points.size < 3) continue
            
            val existingLeaves = leaves.filter { it.branchIndex == index }
            if (existingLeaves.isNotEmpty()) continue
            
            val leafCount = 3 + (Math.random() * 3).toInt()
            
            for (i in 0 until leafCount) {
                val positionRatio = 0.3f + (i.toFloat() / leafCount) * 0.5f
                val side = if (i % 2 == 0) -1 else 1
                val size = baseLeafSize + Math.random().toFloat() * 30f
                val angle = Math.random().toFloat() * 50f - 25f
                
                val leaf = RoseLeaf(
                    branchIndex = index,
                    positionRatio = positionRatio,
                    maxSize = size,
                    angle = angle,
                    side = side
                )
                
                leaves.add(leaf)
            }
        }
    }
    
    private fun growExistingLeaves(force: Float) {
        for (leaf in leaves) {
            if (leaf.currentSize < leaf.maxSize && force > 0.15f) {
                val growth = force * leafGrowthRate * 0.025f
                leaf.currentSize = (leaf.currentSize + growth).coerceAtMost(leaf.maxSize)
            }
        }
    }
    
    // ==================== FLEURS ====================
    
    private fun createFlowersOnBranches() {
        for ((index, branch) in branches.withIndex()) {
            if (branch.points.size < 2) continue
            
            val existingFlowers = flowers.filter { it.branchIndex == index }
            if (existingFlowers.isNotEmpty()) continue
            
            if (!branch.isActive && branch.currentLength > 40f) {
                val lastPoint = branch.points.last()
                val flowerSize = baseFlowerSize + Math.random().toFloat() * 10f
                
                val flower = RoseFlower(
                    branchIndex = index,
                    x = lastPoint.x,
                    y = lastPoint.y,
                    maxSize = flowerSize
                )
                
                flowers.add(flower)
                challengeManager?.notifyFlowerCreated(flower.x, flower.y, flower.id)
            }
        }
    }
    
    private fun growExistingFlowers(force: Float) {
        for (flower in flowers) {
            if (flower.currentSize < flower.maxSize && force > 0.15f) {
                val growth = force * flowerGrowthRate * 0.025f
                flower.currentSize = (flower.currentSize + growth).coerceAtMost(flower.maxSize)
            }
        }
    }
    
    // ==================== RENDU ====================
    
    private fun drawBranches(canvas: Canvas, paint: Paint) {
        paint.color = Color.rgb(101, 67, 33)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        
        for (branch in branches) {
            if (branch.points.size >= 2) {
                for (i in 1 until branch.points.size) {
                    val p1 = branch.points[i-1]
                    val p2 = branch.points[i]
                    
                    paint.strokeWidth = p1.thickness
                    canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paint)
                }
            }
        }
    }
    
    private fun drawLeaves(canvas: Canvas, paint: Paint) {
        paint.color = Color.rgb(34, 139, 34)
        paint.style = Paint.Style.FILL
        
        for (leaf in leaves) {
            if (leaf.currentSize > 0 && leaf.branchIndex < branches.size) {
                val branch = branches[leaf.branchIndex]
                val leafPoint = getBranchPointAtRatio(branch, leaf.positionRatio)
                
                leafPoint?.let { point ->
                    drawSingleLeaf(canvas, paint, point.x, point.y, leaf)
                }
            }
        }
    }
    
    private fun drawSingleLeaf(canvas: Canvas, paint: Paint, x: Float, y: Float, leaf: RoseLeaf) {
        val size = leaf.currentSize
        val angle = leaf.angle
        val side = leaf.side
        
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(angle + side * 25f)
        
        val folioleCount = leaf.folioleCount
        val folioleSize = size / folioleCount * 1.2f
        
        paint.color = Color.rgb(34, 139, 34)
        paint.style = Paint.Style.FILL
        
        for (i in 0 until folioleCount) {
            val folioleY = -size/2 + (i * size / (folioleCount - 1))
            val folioleX = if (i % 2 == 0) -folioleSize/3 * side else folioleSize/3 * side
            
            val baseWidth = folioleSize * 0.5f
            val baseHeight = folioleSize * 0.7f
            val widthVariation = if (i < leaf.folioleVariations.size) leaf.folioleVariations[i] * 0.15f else 0f
            
            val folioleWidth = baseWidth * (1f + widthVariation)
            val folioleHeight = baseHeight * (1f + widthVariation)
            
            canvas.drawOval(
                folioleX - folioleWidth/2, 
                folioleY - folioleHeight/2,
                folioleX + folioleWidth/2, 
                folioleY + folioleHeight/2, 
                paint
            )
        }
        
        // Tige centrale
        paint.color = Color.rgb(20, 80, 20)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = size * 0.06f
        canvas.drawLine(0f, -size/2, 0f, size/2, paint)
        
        canvas.restore()
    }
    
    private fun drawFlowers(canvas: Canvas, paint: Paint) {
        paint.color = Color.rgb(255, 182, 193)
        paint.style = Paint.Style.FILL
        
        for (flower in flowers) {
            if (flower.currentSize > 0) {
                val petalCount = 5
                val petalSize = flower.currentSize * 0.6f
                
                for (i in 0 until petalCount) {
                    val angle = (i * 72f) * Math.PI / 180.0
                    val petalX = flower.x + cos(angle).toFloat() * flower.currentSize * 0.35f
                    val petalY = flower.y + sin(angle).toFloat() * flower.currentSize * 0.35f
                    
                    canvas.drawCircle(petalX, petalY, petalSize * 0.6f, paint)
                }
                
                paint.color = Color.rgb(255, 215, 0)
                canvas.drawCircle(flower.x, flower.y, flower.currentSize * 0.25f, paint)
                paint.color = Color.rgb(255, 182, 193)
            }
        }
    }
    
    // ==================== UTILITAIRES ====================
    
    private fun getBranchPointAtRatio(branch: RoseBranch, ratio: Float): BranchPoint? {
        if (branch.points.size < 2) return null
        
        val targetLength = branch.currentLength * ratio
        var currentLength = 0f
        
        for (i in 1 until branch.points.size) {
            val segmentLength = distance(branch.points[i-1], branch.points[i])
            if (currentLength + segmentLength >= targetLength) {
                val segmentRatio = (targetLength - currentLength) / segmentLength
                val p1 = branch.points[i-1]
                val p2 = branch.points[i]
                
                val x = p1.x + (p2.x - p1.x) * segmentRatio
                val y = p1.y + (p2.y - p1.y) * segmentRatio
                val thickness = p1.thickness + (p2.thickness - p1.thickness) * segmentRatio
                
                return BranchPoint(x, y, thickness)
            }
            currentLength += segmentLength
        }
        
        return branch.points.lastOrNull()
    }
    
    private fun distance(p1: BranchPoint, p2: BranchPoint): Float {
        val dx = p2.x - p1.x
        val dy = p2.y - p1.y
        return sqrt(dx * dx + dy * dy)
    }
    
    companion object {
        private var branchIdCounter = 0
        private var flowerIdCounter = 0
        
        private fun generateBranchId(): String {
            branchIdCounter++
            return "rosebranch_$branchIdCounter"
        }
        
        private fun generateFlowerId(): String {
            flowerIdCounter++
            return "roseflower_$flowerIdCounter"
        }
    }
}
