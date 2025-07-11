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
        val id: String = generateBranchId(),
        var hasAutoRamified: Boolean = false  // NOUVEAU: pour éviter la ramification multiple
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
    private var lastAutoRamificationTime = 0L  // NOUVEAU: pour la ramification automatique
    
    // Référence au gestionnaire de défis
    private var challengeManager: ChallengeManager? = null
    
    // ==================== PARAMÈTRES AUGMENTÉS ====================
    
    private val spikeThreshold = 0.6f  // Seuil pour détecter un saccade
    private val spikeMinInterval = 200L  // RÉDUIT de 300ms à 200ms
    private val autoRamificationInterval = 800L  // NOUVEAU: ramification auto toutes les 800ms
    private val maxBranches = 25  // AUGMENTÉ de 12 à 25 branches
    private val branchGrowthRate = 1200f  // DOUBLÉ de 600f à 1200f
    private val leafGrowthRate = 800f  // PLUS QUE DOUBLÉ de 300f à 800f
    private val flowerGrowthRate = 500f  // DOUBLÉ de 250f à 500f
    
    // Tailles augmentées
    private val baseBranchThickness = 15f  
    private val segmentLength = 35f  // AUGMENTÉ de 25f à 35f pour croissance plus visible       
    private val baseLeafSize = 100f  // MULTIPLIÉ PAR 5 (de 20f à 100f)
    private val baseFlowerSize = 50f       
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun setChallengeManager(manager: ChallengeManager) {
        challengeManager = manager
    }
    
    fun initialize(centerX: Float, bottomY: Float) {
        baseX = centerX
        baseY = bottomY
        lastAutoRamificationTime = System.currentTimeMillis()  // NOUVEAU: initialiser le timer
        
        // Créer la branche principale
        val mainBranch = RoseBranch(
            parentBranchIndex = -1,
            maxLength = 400f,  // AUGMENTÉ de 350f à 400f
            angle = -90f  // Pousse vers le haut
        )
        
        // Point de base de taille normale
        mainBranch.points.add(BranchPoint(baseX, baseY, baseBranchThickness))
        branches.add(mainBranch)
    }
    
    fun processStemGrowth(force: Float) {
        // NOUVEAU: Ramification automatique périodique
        autoRamifyBranches()
        
        // Détecter les saccades pour DOUBLER la ramification d'un coup
        detectSpikeAndDoubleBranching(force)
        
        // Faire pousser toutes les branches actives PLUS VITE
        growActiveBranches(force)
        
        lastForce = force
    }
    
    fun processLeavesGrowth(force: Float) {
        // Créer des feuilles sur les branches existantes (plus souvent)
        createLeavesOnBranches()
        
        // Faire grandir les feuilles existantes PLUS VITE
        growExistingLeaves(force)
    }
    
    fun processFlowerGrowth(force: Float) {
        // Créer des fleurs au bout des branches
        createFlowersOnBranches()
        
        // Faire grandir les fleurs existantes
        growExistingFlowers(force)
    }
    
    fun reset() {
        branches.clear()
        leaves.clear()
        flowers.clear()
        lastForce = 0f
        lastSpikeTime = 0L
        lastAutoRamificationTime = 0L  // NOUVEAU: reset du timer auto
    }
    
    fun drawRoseBush(canvas: Canvas, branchPaint: Paint, leafPaint: Paint, flowerPaint: Paint) {
        // Dessiner les branches
        drawBranches(canvas, branchPaint)
        
        // Dessiner les feuilles
        drawLeaves(canvas, leafPaint)
        
        // Dessiner les fleurs
        drawFlowers(canvas, flowerPaint)
    }
    
    // ==================== RAMIFICATION AUTOMATIQUE ====================
    
    private fun autoRamifyBranches() {
        val currentTime = System.currentTimeMillis()
        
        // NOUVEAU: Ramification automatique périodique
        if (currentTime - lastAutoRamificationTime > autoRamificationInterval && branches.size < maxBranches) {
            // Trouver une branche éligible qui n'a pas encore fait de ramification auto
            val eligibleBranches = branches.filter { 
                it.points.size >= 2 && 
                it.currentLength > 20f && 
                !it.hasAutoRamified &&
                it.isActive
            }
            
            if (eligibleBranches.isNotEmpty()) {
                val branch = eligibleBranches.random()
                branch.hasAutoRamified = true  // Marquer comme ayant ramifié
                createNewBranchFrom(branch)
                lastAutoRamificationTime = currentTime
                println("Ramification automatique! Branche créée (${branches.size}/${maxBranches})")
            }
        }
    }
    
    // ==================== DÉTECTION DES SACCADES (DOUBLÉE) ====================
    
    private fun detectSpikeAndDoubleBranching(force: Float) {
        val currentTime = System.currentTimeMillis()
        
        // Détecter un saccade : augmentation rapide de force
        val forceIncrease = force - lastForce
        val isSpike = forceIncrease > spikeThreshold && force > 0.4f
        val canCreateBranches = currentTime - lastSpikeTime > spikeMinInterval && branches.size < maxBranches
        
        if (isSpike && canCreateBranches) {
            // NOUVEAU: DOUBLER la création de branches lors d'une saccade
            val branchesToCreate = minOf(2, maxBranches - branches.size)  // Créer 2 branches ou moins si limite
            
            for (i in 0 until branchesToCreate) {
                createNewBranch()
            }
            
            lastSpikeTime = currentTime
            println("SACCADE! ${branchesToCreate} nouvelles branches créées (${branches.size}/${maxBranches})")
        }
    }
    
    private fun createNewBranch() {
        if (branches.isEmpty()) return
        
        // Choisir une branche parent existante
        val eligibleBranches = branches.filter { it.points.size >= 2 && it.currentLength > 15f }  // RÉDUIT de 30f à 15f
        if (eligibleBranches.isEmpty()) return
        
        val parentBranch = eligibleBranches.random()
        createNewBranchFrom(parentBranch)
    }
    
    private fun createNewBranchFrom(parentBranch: RoseBranch) {
        val parentIndex = branches.indexOf(parentBranch)
        
        // Position aléatoire sur la branche parent (entre 0.2 et 0.9 pour plus de variété)
        val positionRatio = 0.2f + Math.random().toFloat() * 0.7f
        val branchPoint = getBranchPointAtRatio(parentBranch, positionRatio)
        
        // Angle de ramification (30° à 150° par rapport à la branche parent pour plus de variété)
        val parentAngle = parentBranch.angle
        val angleOffset = -75f + Math.random().toFloat() * 150f  // ±75°
        val newAngle = parentAngle + angleOffset
        
        // Longueur de la nouvelle branche (plus variée)
        val newLength = parentBranch.maxLength * (0.4f + Math.random().toFloat() * 0.5f)  // 40% à 90%
        
        val newBranch = RoseBranch(
            parentBranchIndex = parentIndex,
            branchOffsetRatio = positionRatio,
            maxLength = newLength,
            angle = newAngle
        )
        
        // Point de départ = point sur la branche parent
        branchPoint?.let {
            newBranch.points.add(BranchPoint(it.x, it.y, baseBranchThickness * 0.7f))  // Légèrement plus gros
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
    
    // ==================== CROISSANCE DES BRANCHES (ACCÉLÉRÉE) ====================
    
    private fun growActiveBranches(force: Float) {
        for (branch in branches.filter { it.isActive && force > 0.05f }) {  // ENCORE PLUS SENSIBLE (0.1f -> 0.05f)
            if (branch.currentLength < branch.maxLength) {
                val growth = force * branchGrowthRate * 0.035f  // AUGMENTÉ de 0.020f à 0.035f
                branch.currentLength = (branch.currentLength + growth).coerceAtMost(branch.maxLength)
                
                // Ajouter un nouveau point si nécessaire
                if (branch.points.isNotEmpty()) {
                    val lastPoint = branch.points.last()
                    
                    if (branch.currentLength >= branch.points.size * segmentLength) {
                        val angleRad = Math.toRadians(branch.angle.toDouble())
                        val newX = lastPoint.x + cos(angleRad).toFloat() * segmentLength
                        val newY = lastPoint.y + sin(angleRad).toFloat() * segmentLength
                        val newThickness = (lastPoint.thickness * 0.92f).coerceAtLeast(2f)  // Légèrement moins de réduction
                        
                        branch.points.add(BranchPoint(newX, newY, newThickness))
                    }
                }
                
                // Arrêter la croissance si la branche atteint sa taille max
                if (branch.currentLength >= branch.maxLength * 0.90f) {  // RÉDUIT de 0.95f à 0.90f pour arrêter plus tôt
                    branch.isActive = false
                }
            }
        }
    }
    
    // ==================== CROISSANCE DES FEUILLES (ACCÉLÉRÉE ET PLUS GROSSES) ====================
    
    private fun createLeavesOnBranches() {
        // Créer des feuilles sur toutes les branches qui n'en ont pas encore
        for ((index, branch) in branches.withIndex()) {
            if (branch.points.size < 2) continue
            
            // Vérifier si cette branche a déjà des feuilles
            val existingLeaves = leaves.filter { it.branchIndex == index }
            if (existingLeaves.isNotEmpty()) continue  // Déjà des feuilles sur cette branche
            
            // Créer 3-5 feuilles composées par branche (AUGMENTÉ)
            val leafCount = 3 + (Math.random() * 3).toInt()
            
            for (i in 0 until leafCount) {
                val positionRatio = 0.2f + (i.toFloat() / leafCount) * 0.6f  // Plus réparties
                val side = if (i % 2 == 0) -1 else 1
                val size = baseLeafSize + Math.random().toFloat() * 50f  // ÉNORMES FEUILLES (100f + 50f max)
                val angle = Math.random().toFloat() * 60f - 30f  // ±30° pour plus de variété
                
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
            if (leaf.currentSize < leaf.maxSize && force > 0.05f) {  // PLUS SENSIBLE (0.1f -> 0.05f)
                val growth = force * leafGrowthRate * 0.035f  // AUGMENTÉ de 0.020f à 0.035f
                leaf.currentSize = (leaf.currentSize + growth).coerceAtMost(leaf.maxSize)
            }
        }
    }
    
    // ==================== CROISSANCE DES FLEURS ====================
    
    private fun createFlowersOnBranches() {
        for ((index, branch) in branches.withIndex()) {
            if (branch.points.size < 2) continue
            
            // Vérifier si cette branche a déjà une fleur
            val existingFlowers = flowers.filter { it.branchIndex == index }
            if (existingFlowers.isNotEmpty()) continue
            
            // Une fleur au bout de chaque branche inactive (terminée)
            if (!branch.isActive && branch.currentLength > 50f) {  // RÉDUIT le seuil de 100f à 50f
                val lastPoint = branch.points.last()
                val flowerSize = baseFlowerSize + Math.random().toFloat() * 25f
                
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
    }
    
    private fun growExistingFlowers(force: Float) {
        for (flower in flowers) {
            if (flower.currentSize < flower.maxSize && force > 0.05f) {  // PLUS SENSIBLE (0.1f -> 0.05f)
                val growth = force * flowerGrowthRate * 0.030f  // AUGMENTÉ de 0.020f à 0.030f
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
        val size = leaf.currentSize
        val angle = leaf.angle
        val side = leaf.side
        
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(angle + side * 25f)
        
        // Feuille composée de rosier ÉNORME (5-7 folioles pour les grandes feuilles)
        val folioleCount = 5 + (Math.random() * 3).toInt()  // AUGMENTÉ de 3-5 à 5-7
        val folioleSize = size / folioleCount * 1.5f  // AUGMENTÉ de 1.2f à 1.5f
        
        paint.color = Color.rgb(34, 139, 34)
        paint.style = Paint.Style.FILL
        
        // Dessiner les folioles le long d'une tige
        for (i in 0 until folioleCount) {
            val folioleY = -size/2 + (i * size / (folioleCount - 1))
            val folioleX = if (i % 2 == 0) -folioleSize/2 * side else folioleSize/2 * side  // PLUS ÉCARTÉES
            
            // Foliole ovale dentelée PLUS GROSSE
            val folioleWidth = folioleSize * 0.6f  // AUGMENTÉ de 0.4f à 0.6f
            val folioleHeight = folioleSize * 0.8f  // AUGMENTÉ de 0.6f à 0.8f
            
            canvas.drawOval(
                folioleX - folioleWidth/2, 
                folioleY - folioleHeight/2,
                folioleX + folioleWidth/2, 
                folioleY + folioleHeight/2, 
                paint
            )
        }
        
        // Tige centrale de la feuille composée PLUS ÉPAISSE
        paint.color = Color.rgb(20, 80, 20)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = size * 0.08f  // AUGMENTÉ de 0.05f à 0.08f
        canvas.drawLine(0f, -size/2, 0f, size/2, paint)
        
        canvas.restore()
    }
    
    private fun drawFlowers(canvas: Canvas, paint: Paint) {
        paint.color = Color.rgb(255, 182, 193)  // Rose clair
        paint.style = Paint.Style.FILL
        
        for (flower in flowers) {
            if (flower.currentSize > 0) {
                // Fleur simple mais plus grosse - 5 pétales en cercle
                val petalCount = 5
                val petalSize = flower.currentSize * 0.6f
                
                for (i in 0 until petalCount) {
                    val angle = (i * 72f) * Math.PI / 180.0  // 72° entre chaque pétale
                    val petalX = flower.x + cos(angle).toFloat() * flower.currentSize * 0.35f
                    val petalY = flower.y + sin(angle).toFloat() * flower.currentSize * 0.35f
                    
                    canvas.drawCircle(petalX, petalY, petalSize * 0.6f, paint)
                }
                
                // Centre de la fleur
                paint.color = Color.rgb(255, 215, 0)  // Jaune
                canvas.drawCircle(flower.x, flower.y, flower.currentSize * 0.25f, paint)
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
