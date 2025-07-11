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
    
    private val spikeThreshold = 0.4f  // RÉDUIT de 0.6f à 0.4f (plus sensible)
    private val spikeMinInterval = 200L  // RÉDUIT de 300ms à 200ms
    private val autoRamificationInterval = 300L  // ENCORE PLUS RAPIDE: toutes les 300ms
    private val maxBranches = 25  // Réduit pour éviter le bordel
    private val branchGrowthRate = 12000f  // ENCORE PLUS RAPIDE
    private val leafGrowthRate = 800f  // RETOUR: animation de croissance des feuilles
    private val flowerGrowthRate = 500f  // DOUBLÉ de 250f à 500f
    
    // Tailles pour arbuste fourni et haut
    private val baseBranchThickness = 18f  // Légèrement plus épais
    private val segmentLength = 45f  // Segments moyens pour tige tortueuse
    private val baseLeafSize = 100f  // Feuilles énormes fixes (pas d'animation)
    private val baseFlowerSize = 50f  // RÉDUIT de 75f à 50f (30% plus petites)
    
    // NOUVEAU: Paramètres pour tige tortueuse
    private val tortuosityFactor = 15f  // Amplitude de la tortuosité
    private val tortuosityFrequency = 0.3f  // Fréquence des ondulations       
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun setChallengeManager(manager: ChallengeManager) {
        challengeManager = manager
    }
    
    fun initialize(centerX: Float, bottomY: Float) {
        baseX = centerX
        baseY = bottomY
        lastAutoRamificationTime = System.currentTimeMillis()  // NOUVEAU: initialiser le timer
        
        // Créer la branche principale TORTUEUSE et TRÈS HAUTE
        val mainBranch = RoseBranch(
            parentBranchIndex = -1,
            maxLength = screenHeight * 2.0f,  // ÉNORME: 200% de la hauteur d'écran!
            angle = -90f  // Commence vers le haut mais va devenir tortueux
        )
        
        // Point de base de taille normale AVEC 2ème POINT POUR DÉMARRER
        mainBranch.points.add(BranchPoint(baseX, baseY, baseBranchThickness))
        
        // CORRECTION CRITIQUE: 2ème point initial pour que la tige principale puisse pousser
        val secondX = baseX
        val secondY = baseY - segmentLength * 0.2f  // Un peu vers le haut
        mainBranch.points.add(BranchPoint(secondX, secondY, baseBranchThickness * 0.98f))
        mainBranch.currentLength = segmentLength * 0.2f  // Longueur initiale
        
        branches.add(mainBranch)
    }
    
    fun processStemGrowth(force: Float) {
        // NOUVEAU: Ramification automatique CONTINUE pendant la croissance
        continuousRamification(force)
        
        // Détecter les saccades pour DOUBLER la ramification d'un coup
        detectSpikeAndDoubleBranching(force)
        
        // Faire pousser toutes les branches actives EN MÊME TEMPS
        growActiveBranches(force)
        
        lastForce = force
    }
    
    fun processLeavesGrowth(force: Float) {
        // Créer des feuilles sur les branches existantes
        createLeavesOnBranches()
        
        // Faire grandir les feuilles existantes (animation de croissance seulement)
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
    
    // ==================== RAMIFICATION BASÉE SUR LE SOUFFLE ====================
    
    private fun continuousRamification(force: Float) {
        val currentTime = System.currentTimeMillis()
        
        // RAMIFICATION plus structurée, surtout sur la tige principale
        if (currentTime - lastAutoRamificationTime > autoRamificationInterval && 
            branches.size < maxBranches && force > 0.08f) {  
            
            // Privilégier la tige principale (index 0) pour 70% des ramifications
            val eligibleBranches = branches.filter { 
                it.points.size >= 2 && 
                it.currentLength > 20f
            }
            
            if (eligibleBranches.isNotEmpty()) {
                val parentBranch = if (Math.random() < 0.7 && branches.isNotEmpty()) {
                    branches[0]  // Tige principale dans 70% des cas
                } else {
                    eligibleBranches.random()
                }
                
                createNewBranchFrom(parentBranch)
                lastAutoRamificationTime = currentTime
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
            // SACCADE = quelques grosses branches bien placées
            val branchesToCreate = minOf(3, maxBranches - branches.size)  // 3 BRANCHES bien placées
            
            for (i in 0 until branchesToCreate) {
                createNewBranch()
            }
            
            lastSpikeTime = currentTime
        }
    }
    
    private fun createNewBranch() {
        if (branches.isEmpty()) return
        
        // Choisir preferentiellement la tige principale pour plus de ramifications
        val eligibleBranches = branches.filter { it.points.size >= 2 && it.currentLength > 10f }  // RÉDUIT de 15f à 10f
        if (eligibleBranches.isEmpty()) return
        
        // 60% de chance de choisir la branche principale ou une branche récente
        val parentBranch = if (Math.random() < 0.6 && branches.isNotEmpty()) {
            branches.take(3).random()  // Parmi les 3 premières branches
        } else {
            eligibleBranches.random()
        }
        
        createNewBranchFrom(parentBranch)
    }
    
    private fun createNewBranchFrom(parentBranch: RoseBranch) {
        val parentIndex = branches.indexOf(parentBranch)
        
        // POSITION plus logique selon le type de branche parent
        val positionRatio = if (parentIndex == 0) {
            // Tige principale: branches surtout dans le haut (50% à 90%)
            0.5f + Math.random().toFloat() * 0.4f
        } else {
            // Branches secondaires: ramification plus proche de la base (20% à 70%)
            0.2f + Math.random().toFloat() * 0.5f
        }
        
        val branchPoint = getBranchPointAtRatio(parentBranch, positionRatio)
        
        // ANGLES plus réalistes selon le parent
        val parentAngle = parentBranch.angle
        val angleOffset = if (parentIndex == 0) {
            // Tige principale: branches à 45-135° (vers les côtés et le haut)
            45f + Math.random().toFloat() * 90f * (if (Math.random() < 0.5) -1 else 1)
        } else {
            // Branches secondaires: angles plus modérés (30-60°)
            30f + Math.random().toFloat() * 30f * (if (Math.random() < 0.5) -1 else 1)
        }
        val newAngle = parentAngle + angleOffset
        
        // LONGUEUR selon la hiérarchie
        val newLength = if (parentIndex == 0) {
            // Branches principales: très longues (60% à 120% de l'écran)
            (screenHeight * 0.6f) + (Math.random().toFloat() * screenHeight * 0.6f)
        } else {
            // Branches secondaires: plus courtes (30% à 80% de l'écran)
            (screenHeight * 0.3f) + (Math.random().toFloat() * screenHeight * 0.5f)
        }
        
        val newBranch = RoseBranch(
            parentBranchIndex = parentIndex,
            branchOffsetRatio = positionRatio,
            maxLength = newLength,
            angle = newAngle
        )
        
        // Point de départ + 2ème point pour démarrer immédiatement
        branchPoint?.let {
            newBranch.points.add(BranchPoint(it.x, it.y, baseBranchThickness * 0.9f))
            
            val angleRad = Math.toRadians(newBranch.angle.toDouble())
            val secondX = it.x + cos(angleRad).toFloat() * (segmentLength * 0.1f)
            val secondY = it.y + sin(angleRad).toFloat() * (segmentLength * 0.1f)
            newBranch.points.add(BranchPoint(secondX, secondY, baseBranchThickness * 0.88f))
            newBranch.currentLength = segmentLength * 0.1f
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
        val activeBranches = branches.filter { it.isActive }
        
        for (branch in activeBranches) {
            if (force > 0.05f && branch.currentLength < branch.maxLength) {
                // CROISSANCE MASSIVE
                val baseGrowth = force * branchGrowthRate * 0.12f  // ÉNORME AUGMENTATION de 0.080f à 0.12f
                val screenMultiplier = screenHeight / 1080f  
                val growth = baseGrowth * screenMultiplier
                
                branch.currentLength = (branch.currentLength + growth).coerceAtMost(branch.maxLength)
                
                // Ajouter un nouveau point si nécessaire
                if (branch.points.size >= 2 && branch.currentLength >= branch.points.size * segmentLength) {
                    val lastPoint = branch.points.last()
                    
                    // Tige tortueuse pour la branche principale
                    val angleRad = if (branch.parentBranchIndex == -1) {
                        val baseAngle = branch.angle
                        val tortuosity = sin(branch.points.size * tortuosityFrequency) * tortuosityFactor
                        Math.toRadians((baseAngle + tortuosity).toDouble())
                    } else {
                        Math.toRadians(branch.angle.toDouble())
                    }
                    
                    val newX = lastPoint.x + cos(angleRad).toFloat() * segmentLength
                    val newY = lastPoint.y + sin(angleRad).toFloat() * segmentLength
                    val newThickness = (lastPoint.thickness * 0.96f).coerceAtLeast(2f)  
                    
                    branch.points.add(BranchPoint(newX, newY, newThickness))
                }
                
                // Arrêter la croissance BEAUCOUP PLUS TARD
                if (branch.currentLength >= branch.maxLength * 0.98f) {  // QUASI JAMAIS S'ARRÊTE
                    branch.isActive = false
                }
            }
        }
    }
    
    // ==================== CROISSANCE DES FEUILLES (ANIMATION DE CROISSANCE UNIQUEMENT) ====================
    
    private fun createLeavesOnBranches() {
        // Créer des feuilles sur toutes les branches qui n'en ont pas encore
        for ((index, branch) in branches.withIndex()) {
            if (branch.points.size < 3) continue  // Au moins 3 points pour avoir des feuilles
            
            // Vérifier si cette branche a déjà des feuilles
            val existingLeaves = leaves.filter { it.branchIndex == index }
            if (existingLeaves.isNotEmpty()) continue  // Déjà des feuilles sur cette branche
            
            // Créer 3-6 feuilles composées par branche qui vont grandir
            val leafCount = 3 + (Math.random() * 4).toInt()
            
            for (i in 0 until leafCount) {
                val positionRatio = 0.2f + (i.toFloat() / leafCount) * 0.6f  // Réparties sur la branche
                val side = if (i % 2 == 0) -1 else 1
                val size = baseLeafSize + Math.random().toFloat() * 50f  // ÉNORMES FEUILLES (100f + 50f max)
                val angle = Math.random().toFloat() * 60f - 30f  // ±30° pour plus de variété
                
                val leaf = RoseLeaf(
                    branchIndex = index,
                    positionRatio = positionRatio,
                    maxSize = size,
                    angle = angle,
                    side = side
                )
                
                // Feuille commence à 0 et va grandir (animation de croissance)
                leaf.currentSize = 0f
                
                leaves.add(leaf)
            }
        }
    }
    
    private fun growExistingLeaves(force: Float) {
        for (leaf in leaves) {
            // PHASE 1: Croissance des feuilles (seulement si on souffle)
            if (leaf.currentSize < leaf.maxSize && force > 0.1f) {  
                val growth = force * leafGrowthRate * 0.035f  
                leaf.currentSize = (leaf.currentSize + growth).coerceAtMost(leaf.maxSize)
            }
            // PHASE 2: Une fois adultes, les feuilles restent parfaitement STATIQUES
            // (pas d'animation de mouvement même terminées)
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
            if (!branch.isActive && branch.currentLength > 25f) {  // ENCORE RÉDUIT de 30f à 25f
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
            // CORRIGÉ: Fleurs poussent seulement si on souffle
            if (flower.currentSize < flower.maxSize && force > 0.1f) {  // CHANGÉ de 0.05f à 0.1f
                val growth = force * flowerGrowthRate * 0.030f  
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
        canvas.rotate(angle + side * 25f)  // ANGLE FIXE - pas d'animation de mouvement
        
        // Feuille composée de rosier ÉNORME (5-7 folioles pour les grandes feuilles)
        val folioleCount = 5 + (Math.random() * 3).toInt()  // AUGMENTÉ de 3-5 à 5-7
        val folioleSize = size / folioleCount * 1.5f  // AUGMENTÉ de 1.2f à 1.5f
        
        paint.color = Color.rgb(34, 139, 34)
        paint.style = Paint.Style.FILL
        
        // Dessiner les folioles le long d'une tige - POSITIONS FIXES
        for (i in 0 until folioleCount) {
            val folioleY = -size/2 + (i * size / (folioleCount - 1))
            val folioleX = if (i % 2 == 0) -folioleSize/2 * side else folioleSize/2 * side  // POSITION FIXE
            
            // Foliole ovale dentelée PLUS GROSSE - FORME FIXE
            val folioleWidth = folioleSize * 0.6f  
            val folioleHeight = folioleSize * 0.8f  
            
            canvas.drawOval(
                folioleX - folioleWidth/2, 
                folioleY - folioleHeight/2,
                folioleX + folioleWidth/2, 
                folioleY + folioleHeight/2, 
                paint
            )
        }
        
        // Tige centrale de la feuille composée PLUS ÉPAISSE - POSITION FIXE
        paint.color = Color.rgb(20, 80, 20)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = size * 0.08f  
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
