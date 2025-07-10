package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Path
import kotlin.math.*

class RoseBushManager(private val screenWidth: Int, private val screenHeight: Int) {
    
    // ==================== DATA CLASSES ====================
    
    data class RoseBranch(
        val points: MutableList<BranchPoint> = mutableListOf(),
        val parentBranchIndex: Int = -1,  // -1 pour branche principale
        val branchOffsetRatio: Float = 0f,  // Position sur la branche parent (0-1)
        var currentLength: Float = 0f,
        val maxLength: Float,
        val angle: Float,  // Angle de croissance de cette branche
        var isActive: Boolean = true,
        val id: String = generateBranchId()
    )
    
    data class BranchPoint(
        val x: Float,
        val y: Float,
        val thickness: Float
    )
    
    data class RoseLeaf(
        val branchIndex: Int,
        val positionRatio: Float,  // Position sur la branche (0-1)
        var currentSize: Float = 0f,
        val maxSize: Float,
        val angle: Float,
        val side: Int  // -1 gauche, 1 droite
    )
    
    data class RoseFlower(
        val branchIndex: Int,
        val x: Float,
        val y: Float,
        var currentSize: Float = 0f,
        val maxSize: Float,
        val id: String = generateFlowerId()
    )
    
    // ==================== VARIABLES PRINCIPALES ====================
    
    private val branches = mutableListOf<RoseBranch>()
    private val leaves = mutableListOf<RoseLeaf>()
    private val flowers = mutableListOf<RoseFlower>()
    
    private var baseX = 0f
    private var baseY = 0f
    private var lastForce = 0f
    private var lastSpikeTime = 0L
    
    // Référence au gestionnaire de défis
    private var challengeManager: ChallengeManager? = null
    
    // ==================== PARAMÈTRES ====================
    
    private val spikeThreshold = 0.6f  // Seuil pour détecter un saccade
    private val spikeMinInterval = 300L  // Minimum 300ms entre saccades
    private val maxBranches = 8  // Maximum de branches
    private val branchGrowthRate = 150f
    private val leafGrowthRate = 80f
    private val flowerGrowthRate = 60f
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun setChallengeManager(manager: ChallengeManager) {
        challengeManager = manager
    }
    
    fun initialize(centerX: Float, bottomY: Float) {
        baseX = centerX
        baseY = bottomY
        
        // Créer la branche principale
        val mainBranch = RoseBranch(
            parentBranchIndex = -1,
            maxLength = 200f,
            angle = -90f  // Pousse vers le haut
        )
        
        // Point de base
        mainBranch.points.add(BranchPoint(baseX, baseY, 8f))
        branches.add(mainBranch)
    }
    
    fun processStemGrowth(force: Float) {
        // Détecter les saccades pour créer de nouvelles branches
        detectSpikeAndCreateBranch(force)
        
        // Faire pousser toutes les branches actives
        growActiveBranches(force)
        
        lastForce = force
    }
    
    fun processLeavesGrowth(force: Float) {
        // Créer des feuilles sur les branches existantes
        if (leaves.isEmpty()) {
            createLeavesOnBranches()
        }
        
        // Faire grandir les feuilles existantes
        growExistingLeaves(force)
    }
    
    fun processFlowerGrowth(force: Float) {
        // Créer des fleurs au bout des branches
        if (flowers.isEmpty()) {
            createFlowersOnBranches()
        }
        
        // Faire grandir les fleurs existantes
        growExistingFlowers(force)
    }
    
    fun reset() {
        branches.clear()
        leaves.clear()
        flowers.clear()
        lastForce = 0f
        lastSpikeTime = 0L
    }
    
    fun drawRoseBush(canvas: Canvas, branchPaint: Paint, leafPaint: Paint, flowerPaint: Paint) {
        // Dessiner les branches
        drawBranches(canvas, branchPaint)
        
        // Dessiner les feuilles
        drawLeaves(canvas, leafPaint)
        
        // Dessiner les fleurs
        drawFlowers(canvas, flowerPaint)
    }
    
    // ==================== DÉTECTION DES SACCADES ====================
    
    private fun detectSpikeAndCreateBranch(force: Float) {
        val currentTime = System.currentTimeMillis()
        
        // Détecter un saccade : augmentation rapide de force
        val forceIncrease = force - lastForce
        val isSpike = forceIncrease > spikeThreshold && force > 0.4f
        val canCreateBranch = currentTime - lastSpikeTime > spikeMinInterval && branches.size < maxBranches
        
        if (isSpike && canCreateBranch) {
            createNewBranch()
            lastSpikeTime = currentTime
            println("Saccade détecté! Nouvelle branche créée (${branches.size}/${maxBranches})")
        }
    }
    
    private fun createNewBranch() {
        if (branches.isEmpty()) return
        
        // Choisir une branche parent existante (pas forcément la principale)
        val eligibleBranches = branches.filter { it.points.size >= 3 && it.currentLength > 30f }
        if (eligibleBranches.isEmpty()) return
        
        val parentBranch = eligibleBranches.random()
        val parentIndex = branches.indexOf(parentBranch)
        
        // Position aléatoire sur la branche parent (entre 0.3 et 0.8)
        val positionRatio = 0.3f + Math.random().toFloat() * 0.5f
        val branchPoint = getBranchPointAtRatio(parentBranch, positionRatio)
        
        // Angle de ramification (45° à 135° par rapport à la branche parent)
        val parentAngle = parentBranch.angle
        val angleOffset = -60f + Math.random().toFloat() * 120f  // ±60°
        val newAngle = parentAngle + angleOffset
        
        // Longueur de la nouvelle branche (plus courte que le parent)
        val newLength = parentBranch.maxLength * (0.6f + Math.random().toFloat() * 0.3f)
        
        val newBranch = RoseBranch(
            parentBranchIndex = parentIndex,
            branchOffsetRatio = positionRatio,
            maxLength = newLength,
            angle = newAngle
        )
        
        // Point de départ = point sur la branche parent
        branchPoint?.let {
            newBranch.points.add(BranchPoint(it.x, it.y, 4f))
        }
        
        branches.add(newBranch)
    }
    
    private fun getBranchPointAtRatio(branch: RoseBranch, ratio: Float): BranchPoint? {
        if (branch.points.size < 2) return null
        
        val targetLength = branch.currentLength * ratio
        var currentLength = 0f
        
        for (i in 1 until branch.points.size) {
            val segmentLength = distance(branch.points[i-1], branch.points[i])
            if (currentLength + segmentLength >= targetLength) {
                // Interpoler dans ce segment
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
    
    // ==================== CROISSANCE DES BRANCHES ====================
    
    private fun growActiveBranches(force: Float) {
        for (branch in branches.filter { it.isActive && force > 0.2f }) {
            if (branch.currentLength < branch.maxLength) {
                val growth = force * branchGrowthRate * 0.016f  // 60fps
                branch.currentLength = (branch.currentLength + growth).coerceAtMost(branch.maxLength)
                
                // Ajouter un nouveau point si nécessaire
                if (branch.points.isNotEmpty()) {
                    val lastPoint = branch.points.last()
                    val segmentLength = 15f
                    
                    if (branch.currentLength >= branch.points.size * segmentLength) {
                        val angleRad = Math.toRadians(branch.angle.toDouble())
                        val newX = lastPoint.x + cos(angleRad).toFloat() * segmentLength
                        val newY = lastPoint.y + sin(angleRad).toFloat() * segmentLength
                        val newThickness = (lastPoint.thickness * 0.95f).coerceAtLeast(2f)
                        
                        branch.points.add(BranchPoint(newX, newY, newThickness))
                    }
                }
                
                // Arrêter la croissance si la branche atteint sa taille max
                if (branch.currentLength >= branch.maxLength * 0.95f) {
                    branch.isActive = false
                }
            }
        }
    }
    
    // ==================== CROISSANCE DES FEUILLES ====================
    
    private fun createLeavesOnBranches() {
        for ((index, branch) in branches.withIndex()) {
            if (branch.points.size < 2) continue
            
            // Créer 3-5 feuilles par branche
            val leafCount = 3 + (Math.random() * 3).toInt()
            
            for (i in 0 until leafCount) {
                val positionRatio = 0.2f + (i.toFloat() / leafCount) * 0.6f
                val side = if (i % 2 == 0) -1 else 1
                val size = 15f + Math.random().toFloat() * 10f
                val angle = Math.random().toFloat() * 60f - 30f  // ±30°
                
                leaves.add(RoseLeaf(
                    branchIndex = index,
                    positionRatio = positionRatio,
                    maxSize = size,
                    angle = angle,
                    side = side
                ))
            }
        }
    }
    
    private fun growExistingLeaves(force: Float) {
        for (leaf in leaves) {
            if (leaf.currentSize < leaf.maxSize && force > 0.15f) {
                val growth = force * leafGrowthRate * 0.016f
                leaf.currentSize = (leaf.currentSize + growth).coerceAtMost(leaf.maxSize)
            }
        }
    }
    
    // ==================== CROISSANCE DES FLEURS ====================
    
    private fun createFlowersOnBranches() {
        for ((index, branch) in branches.withIndex()) {
            if (branch.points.size < 2) continue
            
            // Une fleur au bout de chaque branche
            val lastPoint = branch.points.last()
            val flowerSize = 25f + Math.random().toFloat() * 15f
            
            val flower = RoseFlower(
                branchIndex = index,
                x = lastPoint.x,
                y = lastPoint.y,
                maxSize = flowerSize
            )
            
            flowers.add(flower)
            
            // Notifier le challenge manager
            challengeManager?.notifyFlowerCreated(flower.x, flower.y, flower.id)
        }
    }
    
    private fun growExistingFlowers(force: Float) {
        for (flower in flowers) {
            if (flower.currentSize < flower.maxSize && force > 0.2f) {
                val growth = force * flowerGrowthRate * 0.016f
                flower.currentSize = (flower.currentSize + growth).coerceAtMost(flower.maxSize)
            }
        }
    }
    
    // ==================== FONCTIONS DE RENDU ====================
    
    private fun drawBranches(canvas: Canvas, paint: Paint) {
        paint.color = Color.rgb(101, 67, 33)  // Brun pour les branches
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
        paint.color = Color.rgb(34, 139, 34)  // Vert pour les feuilles
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
        val path = Path()
        val size = leaf.currentSize
        val angle = leaf.angle
        val side = leaf.side
        
        // Feuille simple ovale
        val leafWidth = size * 0.6f
        val leafHeight = size
        
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(angle + side * 30f)
        
        path.addOval(-leafWidth/2 * side, -leafHeight/2, leafWidth/2 * side, leafHeight/2, Path.Direction.CW)
        canvas.drawPath(path, paint)
        
        canvas.restore()
    }
    
    private fun drawFlowers(canvas: Canvas, paint: Paint) {
        paint.color = Color.rgb(255, 182, 193)  // Rose clair
        paint.style = Paint.Style.FILL
        
        for (flower in flowers) {
            if (flower.currentSize > 0) {
                // Fleur simple - 5 pétales en cercle
                val petalCount = 5
                val petalSize = flower.currentSize * 0.8f
                
                for (i in 0 until petalCount) {
                    val angle = (i * 72f) * Math.PI / 180.0  // 72° entre chaque pétale
                    val petalX = flower.x + cos(angle).toFloat() * flower.currentSize * 0.3f
                    val petalY = flower.y + sin(angle).toFloat() * flower.currentSize * 0.3f
                    
                    canvas.drawCircle(petalX, petalY, petalSize * 0.5f, paint)
                }
                
                // Centre de la fleur
                paint.color = Color.rgb(255, 215, 0)  // Jaune
                canvas.drawCircle(flower.x, flower.y, flower.currentSize * 0.2f, paint)
                paint.color = Color.rgb(255, 182, 193)  // Retour au rose
            }
        }
    }
    
    // ==================== UTILITAIRES ====================
    
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
