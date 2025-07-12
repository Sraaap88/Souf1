package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import kotlin.math.*

class BudManager(private val plantStem: PlantStem) {
    
    // ==================== DATA CLASSES ====================
     
    data class Bud(
        val x: Float,
        val y: Float,
        val stemIndex: Int,              // -1 pour tige principale, 0+ pour branches
        var currentSize: Float = 0f,
        val maxSize: Float,
        val creationTime: Long,
        var isFullyGrown: Boolean = false,
        val petalCount: Int,             // Nombre de pointes unique pour ce bouton
        val petalVariations: List<Float>, // Variations de longueur pour chaque pointe
        val id: String = generateBudId() // NOUVEAU: ID unique pour le défi
    )
    
    // ==================== VARIABLES ====================
    
    val buds = mutableListOf<Bud>()
    private var lastForce = 0f
    
    // NOUVEAU: Référence au gestionnaire de défis
    private var challengeManager: ChallengeManager? = null
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.25f
    private val baseBudSize = 140f          // 7x plus gros (20f × 7)
    private val maxBudSize = 245f           // 7x plus gros (35f × 7)
    private val growthRate = 300f           // Vitesse de croissance
    private val minPetalCount = 4           // Minimum 4 pointes
    private val maxPetalCount = 7           // Maximum 7 pointes
    
    // NOUVEAU: Paramètres pour le défi bourgeons
    private val budChallengeMinForce = 0.05f  // Force minimum pour défi
    private val budChallengeMaxForce = 0.25f  // Force maximum pour défi
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    // NOUVEAU: Injection du ChallengeManager
    fun setChallengeManager(manager: ChallengeManager) {
        challengeManager = manager
    }
    
    fun processBudGrowth(force: Float) {
        // MODIFIÉ: Créer des boutons selon le mode (normal ou défi)
        if (challengeManager?.getCurrentChallenge()?.id == 2 || challengeManager?.getCurrentChallenge()?.id == 3) {
            // Mode défi 2 ou 3: conditions strictes
            createBudsForChallenge(force)
        } else {
            // Mode normal: comportement existant
            createBudsOnEligibleStems()
        }
        
        // Faire grandir les boutons existants
        growExistingBuds(force)
        
        lastForce = force
    }
    
    fun resetBuds() {
        buds.clear()
        lastForce = 0f
    }
    
    fun drawBuds(canvas: Canvas, budPaint: Paint, petalPaint: Paint) {
        for (bud in buds) {
            if (bud.currentSize > 0) {
                drawSingleBud(canvas, bud, budPaint, petalPaint)
            }
        }
    }
    
    // ==================== FONCTIONS PRIVÉES ====================
    
    // NOUVEAU: Création de bourgeons pour le défi (conditions strictes)
    private fun createBudsForChallenge(force: Float) {
        // Conditions strictes: force très faible et constante
        if (force < budChallengeMinForce || force > budChallengeMaxForce) return
        
        // Vérifier tige principale (5-30% de croissance)
        checkMainStemForBudChallenge()
        
        // Vérifier branches (5-30% de croissance)
        for (branchIndex in plantStem.branches.indices) {
            checkBranchForBudChallenge(branchIndex)
        }
    }
    
    private fun checkMainStemForBudChallenge() {
        if (buds.any { it.stemIndex == -1 } || plantStem.mainStem.size <= 3) return
        
        val mainStemHeight = if (plantStem.mainStem.isNotEmpty()) {
            plantStem.getStemBaseY() - plantStem.mainStem.last().y
        } else 0f
        
        // Conditions pour défi: entre 20px et 80px (5-30% environ)
        if (mainStemHeight in 20f..80f) {
            createBudOnMainStemForChallenge()
        }
    }
    
    private fun checkBranchForBudChallenge(branchIndex: Int) {
        if (buds.any { it.stemIndex == branchIndex }) return
        
        val branch = plantStem.branches[branchIndex]
        val growthPercentage = if (branch.maxHeight > 0) {
            branch.currentHeight / branch.maxHeight
        } else 0f
        
        // Conditions pour défi: 5-30% de croissance
        if (growthPercentage >= 0.05f && growthPercentage <= 0.30f && 
            branch.currentHeight >= 15f && branch.points.isNotEmpty()) {
            createBudOnBranchForChallenge(branchIndex)
        }
    }
    
    private fun createBudOnMainStemForChallenge() {
        val topPoint = plantStem.mainStem.last()
        
        val sizeVariation = Math.random().toFloat()
        val size = baseBudSize + (sizeVariation * (maxBudSize - baseBudSize))
        val petalCount = minPetalCount + (Math.random() * (maxPetalCount - minPetalCount + 1)).toInt()
        val petalVariations = (0 until petalCount).map { 
            0.8f + (Math.random().toFloat() * 0.4f)
        }
        
        val bud = Bud(
            x = topPoint.x,
            y = topPoint.y - 10f,
            stemIndex = -1,
            maxSize = size,
            creationTime = System.currentTimeMillis(),
            petalCount = petalCount,
            petalVariations = petalVariations
        )
        
        buds.add(bud)
        
        // NOUVEAU: Notifier le ChallengeManager
        challengeManager?.notifyBudCreated(bud.x, bud.y, bud.id)
        
        println("Bourgeon créé pour défi sur tige principale: ${petalCount} pointes, ID: ${bud.id}")
    }
    
    private fun createBudOnBranchForChallenge(branchIndex: Int) {
        val branch = plantStem.branches[branchIndex]
        val topPoint = branch.points.last()
        
        val baseSizeForBranch = baseBudSize * 0.8f
        val maxSizeForBranch = maxBudSize * 0.8f
        val sizeVariation = Math.random().toFloat()
        val size = baseSizeForBranch + (sizeVariation * (maxSizeForBranch - baseSizeForBranch))
        val petalCount = (minPetalCount - 1) + (Math.random() * (maxPetalCount - minPetalCount + 2)).toInt()
        val petalVariations = (0 until petalCount).map { 
            0.7f + (Math.random().toFloat() * 0.5f)
        }
        
        val bud = Bud(
            x = topPoint.x,
            y = topPoint.y - 8f,
            stemIndex = branchIndex,
            maxSize = size,
            creationTime = System.currentTimeMillis(),
            petalCount = petalCount,
            petalVariations = petalVariations
        )
        
        buds.add(bud)
        
        // NOUVEAU: Notifier le ChallengeManager
        challengeManager?.notifyBudCreated(bud.x, bud.y, bud.id)
        
        println("Bourgeon créé pour défi sur branche $branchIndex: ${petalCount} pointes, ID: ${bud.id}")
    }
    
    private fun createBudsOnEligibleStems() {
        // Vérifier la tige principale
        checkMainStemForBud()
        
        // Vérifier chaque branche
        for (branchIndex in plantStem.branches.indices) {
            checkBranchForBud(branchIndex)
        }
    }
    
    private fun checkMainStemForBud() {
        if (buds.any { it.stemIndex == -1 } || plantStem.mainStem.size <= 5) return
        
        val mainStemHeight = if (plantStem.mainStem.isNotEmpty()) {
            plantStem.getStemBaseY() - plantStem.mainStem.last().y
        } else 0f
        
        // Bouton sur tige principale si entre 30px et 80px de hauteur
        if (mainStemHeight in 30f..79f) {
            createBudOnMainStem()
        }
    }
    
    private fun checkBranchForBud(branchIndex: Int) {
        if (buds.any { it.stemIndex == branchIndex }) return
        
        val branch = plantStem.branches[branchIndex]
        
        // Calculer le pourcentage de croissance
        val growthPercentage = if (branch.maxHeight > 0) {
            branch.currentHeight / branch.maxHeight
        } else 0f
        
        // Créer bouton si entre 5% et 30% de croissance
        if (growthPercentage >= 0.05f && growthPercentage < 0.30f && 
            branch.currentHeight >= 20f && branch.points.isNotEmpty()) {
            createBudOnBranch(branchIndex)
        }
    }
    
    private fun createBudOnMainStem() {
        val topPoint = plantStem.mainStem.last()
        
        // Taille aléatoire dans la plage
        val sizeVariation = Math.random().toFloat()
        val size = baseBudSize + (sizeVariation * (maxBudSize - baseBudSize))
        
        // Nombre de pointes aléatoire
        val petalCount = minPetalCount + (Math.random() * (maxPetalCount - minPetalCount + 1)).toInt()
        
        // Variations de longueur pour chaque pointe (fixes, pas d'animation)
        val petalVariations = (0 until petalCount).map { 
            0.8f + (Math.random().toFloat() * 0.4f) // Variations entre 0.8 et 1.2
        }
        
        val bud = Bud(
            x = topPoint.x,
            y = topPoint.y - 10f,
            stemIndex = -1,
            maxSize = size,
            creationTime = System.currentTimeMillis(),
            petalCount = petalCount,
            petalVariations = petalVariations
        )
        
        buds.add(bud)
        println("Bouton créé sur tige principale: ${petalCount} pointes, taille max: ${size.toInt()}px")
    }
    
    private fun createBudOnBranch(branchIndex: Int) {
        val branch = plantStem.branches[branchIndex]
        val topPoint = branch.points.last()
        
        // Taille aléatoire légèrement plus petite pour les branches
        val baseSizeForBranch = baseBudSize * 0.8f
        val maxSizeForBranch = maxBudSize * 0.8f
        val sizeVariation = Math.random().toFloat()
        val size = baseSizeForBranch + (sizeVariation * (maxSizeForBranch - baseSizeForBranch))
        
        // Nombre de pointes aléatoire (parfois moins sur les branches)
        val petalCount = (minPetalCount - 1) + (Math.random() * (maxPetalCount - minPetalCount + 2)).toInt()
        
        // Variations de longueur pour chaque pointe
        val petalVariations = (0 until petalCount).map { 
            0.7f + (Math.random().toFloat() * 0.5f) // Variations entre 0.7 et 1.2
        }
        
        val bud = Bud(
            x = topPoint.x,
            y = topPoint.y - 8f,
            stemIndex = branchIndex,
            maxSize = size,
            creationTime = System.currentTimeMillis(),
            petalCount = petalCount,
            petalVariations = petalVariations
        )
        
        buds.add(bud)
        
        val growthPercentage = (branch.currentHeight / branch.maxHeight * 100).toInt()
        println("Bouton créé sur branche $branchIndex: ${petalCount} pointes, taille: ${size.toInt()}px (croissance: ${growthPercentage}%)")
    }
    
    private fun growExistingBuds(force: Float) {
        for (bud in buds) {
            if (bud.currentSize < bud.maxSize && force > forceThreshold) {
                val forceStability = 1f - abs(force - lastForce).coerceAtMost(0.5f) * 2f
                val qualityMultiplier = 0.5f + forceStability * 0.5f
                
                val growthProgress = bud.currentSize / bud.maxSize
                val progressCurve = 1f - growthProgress * growthProgress
                val adjustedGrowth = force * qualityMultiplier * progressCurve * growthRate * 0.008f
                
                bud.currentSize = (bud.currentSize + adjustedGrowth).coerceAtMost(bud.maxSize)
                
                // Marquer comme complètement développé
                if (bud.currentSize >= bud.maxSize * 0.95f) {
                    bud.isFullyGrown = true
                }
            }
        }
    }
    
    private fun drawSingleBud(canvas: Canvas, bud: Bud, budPaint: Paint, petalPaint: Paint) {
        val centerX = bud.x
        val centerY = bud.y
        val size = bud.currentSize
        
        if (size <= 0) return
        
        // VUE DE PROFIL : Dessiner un ovale comme base (tige attachée en bas)
        val baseWidth = size * 0.6f
        val baseHeight = size * 0.4f
        
        // Base ovale verte (partie attachée à la tige)
        budPaint.color = Color.rgb(60, 120, 60)
        budPaint.style = Paint.Style.FILL
        canvas.drawOval(
            centerX - baseWidth/2, 
            centerY - baseHeight/2, 
            centerX + baseWidth/2, 
            centerY + baseHeight/2, 
            budPaint
        )
        
        // VUE DE PROFIL : Les pointes/sépales partent du haut de l'ovale vers le haut
        petalPaint.color = Color.rgb(240, 240, 240)
        petalPaint.style = Paint.Style.STROKE
        petalPaint.strokeWidth = size * 0.04f
        petalPaint.strokeCap = Paint.Cap.ROUND
        
        val petalCount = bud.petalCount
        val petalBaseY = centerY - baseHeight/2  // Haut de l'ovale
        val petalHeight = size * 0.5f            // Hauteur des pointes
        
        // Répartir les pointes sur la largeur du haut de l'ovale
        for (i in 0 until petalCount) {
            // Position X le long du haut de l'ovale
            val progress = i.toFloat() / (petalCount - 1).coerceAtLeast(1)
            val petalStartX = centerX - baseWidth/3 + (progress * baseWidth * 2/3)
            
            // Variation de hauteur pour chaque pointe
            val petalVariation = bud.petalVariations.getOrElse(i) { 1f }
            val finalPetalHeight = petalHeight * petalVariation
            
            // Légère courbure vers l'extérieur pour les pointes latérales
            val curveOffset = (progress - 0.5f) * size * 0.1f
            val petalEndX = petalStartX + curveOffset
            val petalEndY = petalBaseY - finalPetalHeight
            
            // Dessiner la pointe/sépale
            canvas.drawLine(petalStartX, petalBaseY, petalEndX, petalEndY, petalPaint)
        }
        
        // Contour de l'ovale vert
        budPaint.color = Color.rgb(40, 90, 40)
        budPaint.style = Paint.Style.STROKE
        budPaint.strokeWidth = 2f
        canvas.drawOval(
            centerX - baseWidth/2, 
            centerY - baseHeight/2, 
            centerX + baseWidth/2, 
            centerY + baseHeight/2, 
            budPaint
        )
    }
    
    // ==================== FONCTIONS UTILITAIRES ====================
    
    fun getBudCount(): Int = buds.size
    
    fun getFullyGrownBudCount(): Int = buds.count { it.isFullyGrown }
    
    fun getBudInfo(): String {
        val total = buds.size
        val grown = getFullyGrownBudCount()
        return "Boutons: $total total, $grown matures"
    }
    
    fun removeBudsForStem(stemIndex: Int) {
        buds.removeAll { it.stemIndex == stemIndex }
    }
    
    fun hasBudOnStem(stemIndex: Int): Boolean {
        return buds.any { it.stemIndex == stemIndex }
    }
    
    // ==================== COMPANION OBJECT ====================
    
    companion object {
        private var budIdCounter = 0
        
        // NOUVEAU: Générateur d'ID unique pour les bourgeons
        private fun generateBudId(): String {
            budIdCounter++
            return "bud_$budIdCounter"
        }
    }
}
