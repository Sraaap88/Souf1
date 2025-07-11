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
        val side: Int,  // -1 gauche, 1 droite
        val folioleCount: Int = 5 + (Math.random() * 3).toInt(),  // FIXE: nombre de folioles
        val folioleVariations: List<Float> = generateFolioleVariations()  // FIXE: variations
    ) {
        companion object {
            private fun generateFolioleVariations(): List<Float> {
                return (0..7).map { Math.random().toFloat() }  // 8 variations fixes
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
    
    // ==================== VARIABLES PRINCIPALES ====================
    
    private val branches = mutableListOf<RoseBranch>()
    private val leaves = mutableListOf<RoseLeaf>()
    private val flowers = mutableListOf<RoseFlower>()
    
    private var baseX = 0f
    private var baseY = 0f
    private var lastForce = 0f
    private var lastSpikeTime = 0L
    private var lastAutoRamificationTime = 0L
    
    // Référence au gestionnaire de défis
    private var challengeManager: ChallengeManager? = null
    
    // ==================== PARAMÈTRES AUGMENTÉS ====================
    
    private val spikeThreshold = 0.4f
    private val spikeMinInterval = 200L
    private val autoRamificationInterval = 150L  // ULTRA RAPIDE: toutes les 150ms
    private val maxBranches = 80  // BEAUCOUP PLUS pour densité maximale
    private val branchGrowthRate = 25000f  // ÉNORME vitesse
    private val leafGrowthRate = 800f
    private val flowerGrowthRate = 500f
    
    // Tailles pour arbuste ultra-dense et ultra-haut
    private val baseBranchThickness = 18f  
    private val segmentLength = 25f  // PLUS PETITS segments pour fluidité
    private val baseLeafSize = 100f  
    private val baseFlowerSize = 35f
    
    // Paramètres pour tige tortueuse
    private val tortuosityFactor = 15f
    private val tortuosityFrequency = 0.3f
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun setChallengeManager(manager: ChallengeManager) {
        challengeManager = manager
    }
    
    fun initialize(centerX: Float, bottomY: Float) {
        baseX = centerX
        baseY = bottomY
        lastAutoRamificationTime = System.currentTimeMillis()
        
        // Créer la branche principale ULTRA MASSIVE
        val mainBranch = RoseBranch(
            parentBranchIndex = -1,
            maxLength = screenHeight * 5.0f,  // GIGANTESQUE: 500% de la hauteur d'écran!
            angle = -90f  
        )
        
        // Point de base + 2ème point pour démarrer
        mainBranch.points.add(BranchPoint(baseX, baseY, baseBranchThickness))
        val secondX = baseX
        val secondY = baseY - segmentLength * 0.2f
        mainBranch.points.add(BranchPoint(secondX, secondY, baseBranchThickness * 0.98f))
        mainBranch.currentLength = segmentLength * 0.2f
        
        branches.add(mainBranch)
    }
    
    fun processStemGrowth(force: Float) {
        // Croissance continue de TOUTES les branches pendant qu'on souffle
        growActiveBranches(force)
        
        // Détecter les saccades pour DIVISER la tige principale en Y
        detectSpikeAndSplitMainBranch(force)
        
        // Créer des branches secondaires en continu
        createSecondaryBranches(force)
        
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
        lastAutoRamificationTime = 0L
    }
    
    fun drawRoseBush(canvas: Canvas, branchPaint: Paint, leafPaint: Paint, flowerPaint: Paint) {
        // Dessiner les branches
        drawBranches(canvas, branchPaint)
        
        // Dessiner les feuilles
        drawLeaves(canvas, leafPaint)
        
        // Dessiner les fleurs
        drawFlowers(canvas, flowerPaint)
    }
    
    // ==================== DIVISION EN Y DE LA TIGE PRINCIPALE ====================
    
    private fun detectSpikeAndSplitMainBranch(force: Float) {
        val currentTime = System.currentTimeMillis()
        
        // Détecter un saccade : augmentation rapide de force
        val forceIncrease = force - lastForce
        val isSpike = forceIncrease > spikeThreshold && force > 0.4f
        val canSplit = currentTime - lastSpikeTime > spikeMinInterval
        
        if (isSpike && canSplit) {
            // Trouver les branches principales (tige principale et ses divisions)
            val mainBranches = branches.filter { it.parentBranchIndex == -1 || isMainBranchDivision(it) }
            
            for (mainBranch in mainBranches) {
                if (mainBranch.isActive && mainBranch.currentLength > 50f) {
                    splitBranchIntoY(mainBranch)
                }
            }
            
            lastSpikeTime = currentTime
        }
    }
    
    private fun isMainBranchDivision(branch: RoseBranch): Boolean {
        // Une division de tige principale a un parent qui est lui-même la tige principale
        if (branch.parentBranchIndex == -1) return true
        if (branch.parentBranchIndex >= branches.size) return false
        
        val parent = branches[branch.parentBranchIndex]
        return parent.parentBranchIndex == -1
    }
    
    private fun splitBranchIntoY(mainBranch: RoseBranch) {
        if (mainBranch.points.size < 3) return
        
        val splitPoint = mainBranch.points.last()
        val baseAngle = mainBranch.angle
        
        // Créer deux branches en Y (gauche et droite)
        val leftAngle = baseAngle - 30f  // 30° vers la gauche
        val rightAngle = baseAngle + 30f // 30° vers la droite
        
        val branchLength = screenHeight * 4.0f  // ULTRA LONGUES divisions
        
        // Branche gauche
        val leftBranch = RoseBranch(
            parentBranchIndex = branches.indexOf(mainBranch),
            branchOffsetRatio = 1.0f, // Au bout de la branche
            maxLength = branchLength,
            angle = leftAngle
        )
        
        // Branche droite  
        val rightBranch = RoseBranch(
            parentBranchIndex = branches.indexOf(mainBranch),
            branchOffsetRatio = 1.0f, // Au bout de la branche
            maxLength = branchLength,
            angle = rightAngle
        )
        
        // Initialiser les deux branches avec 2 points chacune
        for (branch in listOf(leftBranch, rightBranch)) {
            branch.points.add(BranchPoint(splitPoint.x, splitPoint.y, splitPoint.thickness * 0.9f))
            
            val angleRad = Math.toRadians(branch.angle.toDouble())
            val secondX = splitPoint.x + cos(angleRad).toFloat() * (segmentLength * 0.2f)
            val secondY = splitPoint.y + sin(angleRad).toFloat() * (segmentLength * 0.2f)
            branch.points.add(BranchPoint(secondX, secondY, splitPoint.thickness * 0.88f))
            branch.currentLength = segmentLength * 0.2f
        }
        
        branches.add(leftBranch)
        branches.add(rightBranch)
        
        // Arrêter la croissance de la branche principale
        mainBranch.isActive = false
    }
    
    // ==================== BRANCHES SECONDAIRES CONTINUES ====================
    
    private fun createSecondaryBranches(force: Float) {
        val currentTime = System.currentTimeMillis()
        
        // CRÉATION MASSIVE de branches sur la tige principale
        if (currentTime - lastAutoRamificationTime > autoRamificationInterval && 
            branches.size < maxBranches && force > 0.08f) {
            
            // PRIORITÉ ABSOLUE à la tige principale et ses divisions
            val mainBranches = branches.filter { 
                (it.parentBranchIndex == -1 || isMainBranchDivision(it)) &&
                it.points.size >= 2 && 
                it.currentLength > 20f
            }
            
            if (mainBranches.isNotEmpty()) {
                // CRÉER 2-4 branches à la fois pour densité maximale
                val branchesToCreate = if (force > 0.5f) 4 else if (force > 0.3f) 3 else 2
                
                for (i in 0 until minOf(branchesToCreate, maxBranches - branches.size)) {
                    val parentBranch = mainBranches.random()
                    createSecondaryBranchFrom(parentBranch)
                }
                
                lastAutoRamificationTime = currentTime
            }
        }
    }
    
    private fun createSecondaryBranchFrom(parentBranch: RoseBranch) {
        val parentIndex = branches.indexOf(parentBranch)
        
        // Position aléatoire sur toute la longueur de la branche principale
        val positionRatio = 0.2f + Math.random().toFloat() * 0.6f
        val branchPoint = getBranchPointAtRatio(parentBranch, positionRatio)
        
        // Angle perpendiculaire à la branche parent (branches qui sortent sur les côtés)
        val parentAngle = parentBranch.angle
        val angleOffset = if (Math.random() < 0.5) 90f else -90f  // 90° à gauche ou à droite
        val newAngle = parentAngle + angleOffset + (Math.random().toFloat() * 30f - 15f) // ±15° de variation
        
        // Longueur des branches secondaires GIGANTESQUES
        val newLength = (screenHeight * 1.2f) + (Math.random().toFloat() * screenHeight * 1.8f) // 120% à 300%
        
        val newBranch = RoseBranch(
            parentBranchIndex = parentIndex,
            branchOffsetRatio = positionRatio,
            maxLength = newLength,
            angle = newAngle
        )
        
        // Initialiser avec 2 points pour pouvoir pousser immédiatement
        branchPoint?.let {
            newBranch.points.add(BranchPoint(it.x, it.y, baseBranchThickness * 0.7f))
            
            val angleRad = Math.toRadians(newBranch.angle.toDouble())
            val secondX = it.x + cos(angleRad).toFloat() * (segmentLength * 0.1f)
            val secondY = it.y + sin(angleRad).toFloat() * (segmentLength * 0.1f)
            newBranch.points.add(BranchPoint(secondX, secondY, baseBranchThickness * 0.68f))
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
    
    // ==================== CROISSANCE DES BRANCHES ====================
    
    private fun growActiveBranches(force: Float) {
        val activeBranches = branches.filter { it.isActive }
        
        for (branch in activeBranches) {
            if (force > 0.05f && branch.currentLength < branch.maxLength) {
                // CROISSANCE MASSIVE basée sur la force ET la taille d'écran
                val baseGrowth = force * branchGrowthRate * 0.20f  // ULTRA RAPIDE de 0.15f à 0.20f
                val screenMultiplier = screenHeight / 1080f  
                val growth = baseGrowth * screenMultiplier
                
                branch.currentLength = (branch.currentLength + growth).coerceAtMost(branch.maxLength)
                
                // Ajouter un nouveau point si nécessaire
                if (branch.points.size >= 2 && branch.currentLength >= branch.points.size * segmentLength) {
                    val lastPoint = branch.points.last()
                    
                    // NOUVEAU: Toutes les branches sont maintenant tortueuses
                    val angleRad = if (branch.parentBranchIndex == -1) {
                        // Branche principale: tortuosité forte
                        val baseAngle = branch.angle
                        val tortuosity = sin(branch.points.size * tortuosityFrequency) * tortuosityFactor
                        Math.toRadians((baseAngle + tortuosity).toDouble())
                    } else {
                        // Branches secondaires: tortuosité plus légère
                        val baseAngle = branch.angle
                        val tortuosity = sin(branch.points.size * tortuosityFrequency * 0.7f) * (tortuosityFactor * 0.6f)
                        Math.toRadians((baseAngle + tortuosity).toDouble())
                    }
                    
                    val newX = lastPoint.x + cos(angleRad).toFloat() * segmentLength
                    val newY = lastPoint.y + sin(angleRad).toFloat() * segmentLength
                    val newThickness = (lastPoint.thickness * 0.96f).coerceAtLeast(2f)  
                    
                    branch.points.add(BranchPoint(newX, newY, newThickness))
                }
                
                // Arrêter la croissance TRÈS TARD pour pousser au maximum
                if (branch.currentLength >= branch.maxLength * 0.99f) {  // QUASI JAMAIS S'ARRÊTE
                    branch.isActive = false
                }
            }
        }
    }
    
    // ==================== CROISSANCE DES FEUILLES ====================
    
    private fun createLeavesOnBranches() {
        for ((index, branch) in branches.withIndex()) {
            if (branch.points.size < 3) continue
            
            val existingLeaves = leaves.filter { it.branchIndex == index }
            if (existingLeaves.isNotEmpty()) continue
            
            val leafCount = 3 + (Math.random() * 4).toInt()
            
            for (i in 0 until leafCount) {
                val positionRatio = 0.2f + (i.toFloat() / leafCount) * 0.6f
                val side = if (i % 2 == 0) -1 else 1
                val size = baseLeafSize + Math.random().toFloat() * 50f
                val angle = Math.random().toFloat() * 60f - 30f
                
                val leaf = RoseLeaf(
                    branchIndex = index,
                    positionRatio = positionRatio,
                    maxSize = size,
                    angle = angle,
                    side = side
                )
                
                leaf.currentSize = 0f
                leaves.add(leaf)
            }
        }
    }
    
    private fun growExistingLeaves(force: Float) {
        for (leaf in leaves) {
            if (leaf.currentSize < leaf.maxSize && force > 0.1f) {  
                val growth = force * leafGrowthRate * 0.035f  
                leaf.currentSize = (leaf.currentSize + growth).coerceAtMost(leaf.maxSize)
            }
        }
    }
    
    // ==================== CROISSANCE DES FLEURS ====================
    
    private fun createFlowersOnBranches() {
        for ((index, branch) in branches.withIndex()) {
            if (branch.points.size < 2) continue
            
            val existingFlowers = flowers.filter { it.branchIndex == index }
            if (existingFlowers.isNotEmpty()) continue
            
            if (!branch.isActive && branch.currentLength > 25f) {
                val lastPoint = branch.points.last()
                val flowerSize = baseFlowerSize + Math.random().toFloat() * 15f  // RÉDUIT: 35f + 15f max = jusqu'à 50f
                
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
            if (flower.currentSize < flower.maxSize && force > 0.1f) {
                val growth = force * flowerGrowthRate * 0.030f  
                flower.currentSize = (flower.currentSize + growth).coerceAtMost(flower.maxSize)
            }
        }
    }
    
    // ==================== FONCTIONS DE RENDU ====================
    
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
        
        // Utiliser les valeurs FIXES stockées dans la feuille
        val folioleCount = leaf.folioleCount
        val folioleSize = size / folioleCount * 1.5f
        
        paint.color = Color.rgb(34, 139, 34)
        paint.style = Paint.Style.FILL
        
        // Dessiner les folioles le long d'une tige - POSITIONS FIXES
        for (i in 0 until folioleCount) {
            val folioleY = -size/2 + (i * size / (folioleCount - 1))
            val folioleX = if (i % 2 == 0) -folioleSize/2 * side else folioleSize/2 * side
            
            // Foliole ovale dentelée - TAILLE FIXE basée sur les variations stockées
            val baseWidth = folioleSize * 0.6f
            val baseHeight = folioleSize * 0.8f
            val widthVariation = if (i < leaf.folioleVariations.size) leaf.folioleVariations[i] * 0.2f else 0f
            
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
        
        // Tige centrale de la feuille composée - ÉPAISSEUR FIXE
        paint.color = Color.rgb(20, 80, 20)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = size * 0.08f  
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
                
                // Centre de la fleur
                paint.color = Color.rgb(255, 215, 0)
                canvas.drawCircle(flower.x, flower.y, flower.currentSize * 0.25f, paint)
                paint.color = Color.rgb(255, 182, 193)
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
