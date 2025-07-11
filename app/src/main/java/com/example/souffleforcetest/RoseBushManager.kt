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
        val growthSpeedMultiplier: Float = generateRandomGrowthSpeed() // NOUVEAU: Vitesse individuelle
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
    
    data class ScheduledSplit(
        val branchId: String,
        val scheduledTime: Long
    )
    
    // ==================== VARIABLES PRINCIPALES ====================
    
    private val branches = mutableListOf<RoseBranch>()
    private val leaves = mutableListOf<RoseLeaf>()
    private val flowers = mutableListOf<RoseFlower>()
    private val scheduledSplits = mutableListOf<ScheduledSplit>()
    
    private var baseX = 0f
    private var baseY = 0f
    private var lastForce = 0f
    private var lastSpikeTime = 0L
    
    // Référence au gestionnaire de défis
    private var challengeManager: ChallengeManager? = null
    
    // ==================== PARAMÈTRES SIMPLES ====================
    
    private val spikeThreshold = 0.4f  // Seuil pour détecter une saccade
    private val spikeMinInterval = 300L  // Minimum entre saccades
    private val secondSplitDelay = 500L  // Délai pour la 2ème séparation automatique
    private val branchGrowthRate = 2400f  // RÉDUIT de 20% (était 3000f)
    private val leafGrowthRate = 640f     // RÉDUIT de 20% (était 800f)
    private val flowerGrowthRate = 400f   // RÉDUIT de 20% (était 500f)
    
    // Tailles
    private val baseBranchThickness = 15f  
    private val segmentLength = 30f  // Segments pour croissance fluide
    private val baseLeafSize = 80f  
    private val baseFlowerSize = 35f
    
    // Paramètres pour tige tortueuse naturelle - AMÉLIORÉS POUR RÉALISME
    private val tortuosityFactor = 8f      // RÉDUIT (était 12f) - moins parfait
    private val tortuosityFrequency = 0.3f // RÉDUIT (était 0.4f) - moins régulier
    private val randomNoiseFactor = 5f     // NOUVEAU - bruit aléatoire
    
    // NOUVEAUX PARAMÈTRES pour séparations multiples
    private val threeWaySplitChance = 0.4f  // 40% de chance de séparation en 3 (était 0.3f)
    private val fourWaySplitChance = 0.0f   // 0% de chance de séparation en 4 pour éviter éventail
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun setChallengeManager(manager: ChallengeManager) {
        challengeManager = manager
    }
    
    fun initialize(centerX: Float, bottomY: Float) {
        baseX = centerX
        baseY = bottomY
        
        // NOUVEAU: Créer directement la séparation depuis le point de base
        createInitialSplit(baseX, baseY, baseBranchThickness)
    }
    
    // NOUVELLE FONCTION: Créer la séparation initiale
    private fun createInitialSplit(x: Float, y: Float, thickness: Float) {
        val baseAngle = -90f  // Angle vers le haut (négative Y dans Android)
        
        // FORCER minimum 3 branches pour éviter séparation en 2 au début
        val branchCount = if (Math.random() < 0.6f) 3 else 4
        
        // Créer les branches initiales
        for (i in 0 until branchCount) {
            val branchAngle = calculateInitialBranchAngle(baseAngle, i, branchCount)
            
            val newBranch = RoseBranch(
                maxLength = screenHeight * 2.0f,  // Longueur normale
                angle = branchAngle
            )
            
            // Initialiser chaque branche avec 2 points
            newBranch.points.add(BranchPoint(x, y, thickness))
            
            val angleRad = Math.toRadians(branchAngle.toDouble())
            val secondX = x + cos(angleRad).toFloat() * (segmentLength * 0.3f)
            val secondY = y + sin(angleRad).toFloat() * (segmentLength * 0.3f)
            newBranch.points.add(BranchPoint(secondX, secondY, thickness * 0.95f))
            newBranch.currentLength = segmentLength * 0.3f
            
            branches.add(newBranch)
        }
        
        // Notifier le challengeManager de la création initiale
        challengeManager?.notifyDivisionCreated("initial_split_$branchCount")
    }
    
    // NOUVELLE FONCTION: Calculer l'angle pour les branches initiales (naturel, pas éventail)
    private fun calculateInitialBranchAngle(baseAngle: Float, branchIndex: Int, totalBranches: Int): Float {
        return when (totalBranches) {
            3 -> {
                // AMÉLIORATION: Séparation naturelle avec contrainte vers le haut
                when (branchIndex) {
                    0 -> baseAngle - 35f  // vers gauche mais pas trop
                    1 -> baseAngle + 5f   // légèrement vers droite
                    2 -> baseAngle + 40f  // vers droite mais pas trop
                    else -> baseAngle
                }
            }
            4 -> {
                // AMÉLIORATION: Séparation naturelle, toutes vers le haut
                when (branchIndex) {
                    0 -> baseAngle - 40f  // vers gauche
                    1 -> baseAngle - 15f  // légèrement gauche
                    2 -> baseAngle + 10f  // légèrement droite
                    3 -> baseAngle + 35f  // vers droite
                    else -> baseAngle
                }
            }
            else -> baseAngle
        }
    }
    
    fun processStemGrowth(force: Float) {
        // Détecter les saccades pour diviser les tiges
        detectSpikeAndSplit(force)
        
        // Vérifier les séparations programmées
        processScheduledSplits()
        
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
        scheduledSplits.clear()
        lastForce = 0f
        lastSpikeTime = 0L
    }
    
    fun drawRoseBush(canvas: Canvas, branchPaint: Paint, leafPaint: Paint, flowerPaint: Paint) {
        drawBranches(canvas, branchPaint)
        drawLeaves(canvas, leafPaint)
        drawFlowers(canvas, flowerPaint)
    }
    
    // ==================== DIVISION DES TIGES ====================
    
    private fun detectSpikeAndSplit(force: Float) {
        val currentTime = System.currentTimeMillis()
        
        // Détecter une saccade
        val forceIncrease = force - lastForce
        val isSpike = forceIncrease > spikeThreshold && force > 0.4f
        val canSplit = currentTime - lastSpikeTime > spikeMinInterval
        
        if (isSpike && canSplit) {
            // Diviser toutes les tiges actives qui sont assez longues
            val eligibleBranches = branches.filter { it.isActive && it.currentLength > 80f }
            
            for (branch in eligibleBranches) {
                val newBranches = splitBranchMultiway(branch)
                
                // Programmer une 2ème séparation sur une des nouvelles branches
                if (newBranches.isNotEmpty()) {
                    scheduleSecondSplit(newBranches, currentTime)
                }
            }
            
            lastSpikeTime = currentTime
        }
    }
    
    private fun scheduleSecondSplit(newBranches: List<RoseBranch>, currentTime: Long) {
        // Choisir aléatoirement une des nouvelles branches
        val randomBranch = newBranches.random()
        
        // Programmer la séparation dans 500ms
        val scheduledSplit = ScheduledSplit(
            branchId = randomBranch.id,
            scheduledTime = currentTime + secondSplitDelay
        )
        
        scheduledSplits.add(scheduledSplit)
    }
    
    private fun processScheduledSplits() {
        val currentTime = System.currentTimeMillis()
        val splitsToProcess = scheduledSplits.filter { it.scheduledTime <= currentTime }
        
        for (scheduledSplit in splitsToProcess) {
            // Trouver la branche correspondante
            val branch = branches.find { it.id == scheduledSplit.branchId && it.isActive }
            
            if (branch != null && branch.currentLength > 40f) {
                splitBranchMultiway(branch)
            }
        }
        
        // Nettoyer les séparations traitées
        scheduledSplits.removeAll(splitsToProcess)
    }
    
    // NOUVELLE FONCTION: Séparation multiple (2, 3 ou 4 branches)
    private fun splitBranchMultiway(branch: RoseBranch): List<RoseBranch> {
        if (branch.points.size < 3) return emptyList()
        
        val splitPoint = branch.points.last()
        val baseAngle = getCurrentGrowthAngle(branch)
        
        // Déterminer le nombre de branches à créer
        val branchCount = when {
            Math.random() < fourWaySplitChance -> 4
            Math.random() < threeWaySplitChance -> 3
            else -> 2
        }
        
        val newBranches = mutableListOf<RoseBranch>()
        
        // Créer les nouvelles branches
        for (i in 0 until branchCount) {
            val branchAngle = calculateBranchAngle(baseAngle, i, branchCount)
            
            val newBranch = RoseBranch(
                maxLength = branch.maxLength,
                angle = branchAngle
            )
            
            // Initialiser la nouvelle branche avec 2 points
            newBranch.points.add(BranchPoint(splitPoint.x, splitPoint.y, splitPoint.thickness * 0.9f))
            
            val angleRad = Math.toRadians(branchAngle.toDouble())
            val secondX = splitPoint.x + cos(angleRad).toFloat() * (segmentLength * 0.3f)
            val secondY = splitPoint.y + sin(angleRad).toFloat() * (segmentLength * 0.3f)
            newBranch.points.add(BranchPoint(secondX, secondY, splitPoint.thickness * 0.88f))
            newBranch.currentLength = segmentLength * 0.3f
            
            newBranches.add(newBranch)
            branches.add(newBranch)
        }
        
        // Notifier le challengeManager
        val divisionId = newBranches.joinToString("_") { "branch_${it.id}" }
        challengeManager?.notifyDivisionCreated("division_$divisionId")
        
        // Arrêter la croissance de la tige mère
        branch.isActive = false
        
        return newBranches
    }
    
    // NOUVELLE FONCTION: Calculer l'angle pour chaque branche dans une séparation multiple
    private fun calculateBranchAngle(baseAngle: Float, branchIndex: Int, totalBranches: Int): Float {
        return when (totalBranches) {
            2 -> {
                // Séparation classique en Y
                val spread = 25f
                baseAngle + if (branchIndex == 0) -spread else spread
            }
            3 -> {
                // Séparation en trident, légèrement décalée
                val spread = 20f
                val offset = 5f // Petit décalage pour l'asymétrie
                when (branchIndex) {
                    0 -> baseAngle - spread - offset
                    1 -> baseAngle + offset
                    2 -> baseAngle + spread + offset
                    else -> baseAngle
                }
            }
            4 -> {
                // Séparation en éventail, décalée pour être naturelle
                val spread = 15f
                val offset = 8f
                when (branchIndex) {
                    0 -> baseAngle - spread * 2 - offset
                    1 -> baseAngle - spread + offset
                    2 -> baseAngle + spread - offset
                    3 -> baseAngle + spread * 2 + offset
                    else -> baseAngle
                }
            }
            else -> baseAngle
        }
    }
    
    private fun getCurrentGrowthAngle(branch: RoseBranch): Float {
        // AMÉLIORATION: Calculer l'angle avec tortuosité naturelle ET bruit aléatoire
        val baseAngle = branch.angle
        
        // Tortuosité sinusoïdale (moins parfaite)
        val tortuosity = sin(branch.points.size * tortuosityFrequency) * tortuosityFactor
        
        // NOUVEAU: Bruit aléatoire pour briser la perfection sinusoïdale
        val randomNoise = (Math.random().toFloat() - 0.5f) * randomNoiseFactor * 2f
        
        // NOUVEAU: Contrainte pour empêcher les tiges de pointer vers le bas
        val currentAngle = baseAngle + tortuosity + randomNoise
        
        // Contraindre l'angle entre -150° et -30° (toujours vers le haut)
        return currentAngle.coerceIn(-150f, -30f)
    }
    
    // ==================== CROISSANCE DES TIGES ====================
    
    private fun growActiveBranches(force: Float) {
        for (branch in branches.filter { it.isActive }) {
            // Pousse SEULEMENT si on souffle (force > 0.15f)
            if (force > 0.15f && branch.currentLength < branch.maxLength) {
                // Croissance proportionnelle à la force ET à la vitesse individuelle de la branche
                val baseGrowth = force * branchGrowthRate * 0.020f
                val individualGrowth = baseGrowth * branch.growthSpeedMultiplier
                branch.currentLength = (branch.currentLength + individualGrowth).coerceAtMost(branch.maxLength)
                
                // Ajouter un nouveau point si nécessaire
                if (branch.points.size >= 2 && branch.currentLength >= branch.points.size * segmentLength) {
                    val lastPoint = branch.points.last()
                    
                    // Calculer l'angle avec tortuosité naturelle
                    val currentAngle = getCurrentGrowthAngle(branch)
                    val angleRad = Math.toRadians(currentAngle.toDouble())
                    
                    val newX = lastPoint.x + cos(angleRad).toFloat() * segmentLength
                    val newY = lastPoint.y + sin(angleRad).toFloat() * segmentLength
                    val newThickness = (lastPoint.thickness * 0.96f).coerceAtLeast(3f)
                    
                    branch.points.add(BranchPoint(newX, newY, newThickness))
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
        
        // NOUVELLE FONCTION: Générer une vitesse de croissance aléatoire mais similaire
        private fun generateRandomGrowthSpeed(): Float {
            // AUGMENTATION DRASTIQUE: Variation de ±50% autour de la vitesse normale
            val variation = 0.5f
            return 1.0f + (Math.random().toFloat() - 0.5f) * 2 * variation
        }
    }
}
