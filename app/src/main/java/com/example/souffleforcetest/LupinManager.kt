package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import kotlin.math.*

class LupinManager(private val screenWidth: Int, private val screenHeight: Int) {
    
    // ==================== DATA CLASSES ====================
    
    data class LupinStem(
        val points: MutableList<StemPoint> = mutableListOf(),
        var currentHeight: Float = 0f,
        val maxHeight: Float,
        val baseX: Float,
        val baseY: Float,
        var isActive: Boolean = true,
        val id: String = generateStemId(),
        val growthSpeedMultiplier: Float = generateRandomGrowthSpeed(),
        val flowerSpike: FlowerSpike = FlowerSpike()
    )
    
    data class StemPoint(
        val x: Float,
        val y: Float,
        val thickness: Float
    )
    
    data class FlowerSpike(
        val flowers: MutableList<LupinFlower> = mutableListOf(),
        var currentLength: Float = 0f,
        val maxLength: Float = 80f,
        var hasStartedBlooming: Boolean = false
    )
    
    data class LupinFlower(
        val x: Float,
        val y: Float,
        val positionOnSpike: Float, // 0.0 = bas, 1.0 = haut
        var currentSize: Float = 0f,
        val maxSize: Float,
        val color: FlowerColor,
        val id: String = generateFlowerId()
    )
    
    data class LupinLeaf(
        val stemIndex: Int,
        val heightRatio: Float, // Position sur la tige (0.0 = bas, 1.0 = haut)
        var currentSize: Float = 0f,
        val maxSize: Float,
        val angle: Float,
        val folioleCount: Int = 5 + (Math.random() * 4).toInt(), // 5-8 folioles
        val folioleAngles: List<Float> = generateFolioleAngles()
    ) {
        companion object {
            private fun generateFolioleAngles(): List<Float> {
                return (0..8).map { Math.random().toFloat() * 30f - 15f } // Variation ±15°
            }
        }
    }
    
    enum class FlowerColor(val rgb: IntArray) {
        PURPLE(intArrayOf(138, 43, 226)),    // Violet
        BLUE(intArrayOf(65, 105, 225)),      // Bleu royal
        PINK(intArrayOf(255, 20, 147)),      // Rose fuchsia
        WHITE(intArrayOf(248, 248, 255)),    // Blanc cassé
        YELLOW(intArrayOf(255, 215, 0))      // Jaune doré
    }
    
    // ==================== VARIABLES PRINCIPALES ====================
    
    private val stems = mutableListOf<LupinStem>()
    private val leaves = mutableListOf<LupinLeaf>()
    
    private var baseX = 0f
    private var baseY = 0f
    private var lastForce = 0f
    private var lastSpikeTime = 0L
    
    // Référence au gestionnaire de défis
    private var challengeManager: ChallengeManager? = null
    
    // ==================== PARAMÈTRES ULTRA FACILES ====================
    
    private val stemGrowthRate = 5000f     // ÉNORME pour croissance ultra-rapide
    private val leafGrowthRate = 800f      
    private val flowerGrowthRate = 500f    
    
    // Tailles optimisées
    private val baseStemThickness = 8f
    private val segmentLength = 10f        // TRÈS court pour progression rapide
    private val baseLeafSize = 35f         
    private val baseFlowerSize = 5f        
    
    // Paramètres très généreux
    private val maxStems = 5               
    private val stemSpacing = 30f          
    private val flowerDensity = 6          
    
    // Paramètres pour saccades ULTRA sensibles
    private val spikeThreshold = 0.05f     // EXTRÊMEMENT bas
    private val spikeMinInterval = 100L    // TRÈS court
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun setChallengeManager(manager: ChallengeManager) {
        challengeManager = manager
    }
    
    fun initialize(centerX: Float, bottomY: Float) {
        baseX = centerX
        baseY = bottomY
        
        // Créer UNE SEULE tige au début
        createNewStem(baseX, baseY)
    }
    
    fun processStemGrowth(force: Float) {
        // Détecter les saccades pour créer de nouvelles tiges
        detectSpikeAndCreateStem(force)
        
        // Faire pousser seulement la tige la plus récente (la dernière ajoutée)
        growLatestStem(force)
        
        lastForce = force
    }
    
    fun processLeavesGrowth(force: Float) {
        createLeavesOnStems()
        growExistingLeaves(force)
    }
    
    fun processFlowerGrowth(force: Float) {
        createFlowerSpikes()
        growExistingFlowers(force)
    }
    
    fun reset() {
        stems.clear()
        leaves.clear()
        lastForce = 0f
        lastSpikeTime = 0L
    }
    
    fun drawLupin(canvas: Canvas, stemPaint: Paint, leafPaint: Paint, flowerPaint: Paint) {
        drawStems(canvas, stemPaint)
        drawLeaves(canvas, leafPaint)
        drawFlowerSpikes(canvas, flowerPaint)
    }
    
    // ==================== CROISSANCE ULTRA FACILE DES TIGES ====================
    
    private fun detectSpikeAndCreateStem(force: Float) {
        val currentTime = System.currentTimeMillis()
        
        // Seuils EXTRÊMEMENT bas pour détecter les saccades
        val forceIncrease = force - lastForce
        val isSpike = forceIncrease > spikeThreshold && force > 0.02f  // TRÈS TRÈS bas
        val canCreateStem = currentTime - lastSpikeTime > spikeMinInterval
        
        if (isSpike && canCreateStem && stems.size < maxStems) {
            val newStemX = baseX + (stems.size - 2) * stemSpacing + (Math.random().toFloat() - 0.5f) * 15f
            createNewStem(newStemX, baseY)
            lastSpikeTime = currentTime
        }
    }
    
    private fun createNewStem(stemX: Float, stemY: Float) {
        val stem = LupinStem(
            maxHeight = screenHeight * 0.3f + Math.random().toFloat() * screenHeight * 0.1f, // TRÈS réduit
            baseX = stemX,
            baseY = stemY
        )
        
        // Point de base
        stem.points.add(StemPoint(stemX, stemY, baseStemThickness))
        // Deuxième point
        val secondY = stemY - 5f // Très petit segment initial
        stem.points.add(StemPoint(stemX, secondY, baseStemThickness * 0.98f))
        stem.currentHeight = 5f
        
        stems.add(stem)
        
        // Notifier le challenge manager qu'une tige complète a été créée
        challengeManager?.notifyLupinSpikeCreated("NEW_STEM", stem.id)
    }
    
    private fun growLatestStem(force: Float) {
        // Faire pousser seulement la dernière tige ajoutée
        val latestStem = stems.lastOrNull() ?: return
        
        // CHANGEMENT MAJEUR : Force minimum TRÈS TRÈS basse
        if (latestStem.isActive && force > 0.01f && latestStem.currentHeight < latestStem.maxHeight) { // ULTRA bas
            
            // ÉNORME multiplicateur de croissance
            val baseGrowth = force * stemGrowthRate * 0.05f  // DOUBLE du précédent!
            val individualGrowth = baseGrowth * latestStem.growthSpeedMultiplier
            latestStem.currentHeight = (latestStem.currentHeight + individualGrowth).coerceAtMost(latestStem.maxHeight)
            
            // Segments TRÈS courts pour progression rapide
            if (latestStem.points.size >= 2 && latestStem.currentHeight >= latestStem.points.size * segmentLength) {
                val lastPoint = latestStem.points.last()
                
                val randomOffset = (Math.random().toFloat() - 0.5f) * 1f // Très petit offset
                val newX = latestStem.baseX + randomOffset
                val newY = lastPoint.y - segmentLength
                val newThickness = (lastPoint.thickness * 0.98f).coerceAtLeast(2f)
                
                if (newX >= 0 && newX <= screenWidth && newY >= 0) {
                    latestStem.points.add(StemPoint(newX, newY, newThickness))
                } else {
                    latestStem.isActive = false
                }
            }
            
            // Arrêter TRÈS tard pour avoir de grandes tiges
            if (latestStem.currentHeight >= latestStem.maxHeight * 0.95f) {
                latestStem.isActive = false
            }
        }
    }
    
    // ==================== FEUILLES TRÈS FACILES ====================
    
    private fun createLeavesOnStems() {
        for ((index, stem) in stems.withIndex()) {
            if (stem.points.size < 2) continue
            
            val existingLeaves = leaves.filter { it.stemIndex == index }
            if (existingLeaves.isNotEmpty()) continue
            
            // Créer des feuilles très tôt
            if (stem.currentHeight < 20f) continue // TRÈS bas
            
            val leafCount = 2
            
            for (i in 0 until leafCount) {
                val heightRatio = 0.3f + (i.toFloat() / leafCount) * 0.5f
                val size = baseLeafSize + Math.random().toFloat() * 10f
                val angle = Math.random().toFloat() * 60f - 30f
                
                val leaf = LupinLeaf(
                    stemIndex = index,
                    heightRatio = heightRatio,
                    maxSize = size,
                    angle = angle
                )
                
                leaves.add(leaf)
            }
        }
    }
    
    private fun growExistingLeaves(force: Float) {
        for (leaf in leaves) {
            if (leaf.currentSize < leaf.maxSize && force > 0.01f) { // Seuil ULTRA bas
                val growth = force * leafGrowthRate * 0.03f
                leaf.currentSize = (leaf.currentSize + growth).coerceAtMost(leaf.maxSize)
            }
        }
    }
    
    // ==================== FLEURS TRÈS FACILES ====================
    
    private fun createFlowerSpikes() {
        for (stem in stems) {
            if (stem.points.size < 2) continue
            if (stem.flowerSpike.hasStartedBlooming) continue
            
            // Commencer la floraison TRÈS tôt
            if (!stem.isActive && stem.currentHeight > stem.maxHeight * 0.15f) { // ULTRA bas
                createFlowersOnSpike(stem)
                stem.flowerSpike.hasStartedBlooming = true
            }
        }
    }
    
    private fun createFlowersOnSpike(stem: LupinStem) {
        val topPoint = stem.points.lastOrNull() ?: return
        val spikeColor = FlowerColor.values().random()
        
        challengeManager?.notifyLupinSpikeCreated(spikeColor.name, stem.id)
        
        for (i in 0 until flowerDensity) {
            val positionOnSpike = i.toFloat() / (flowerDensity - 1)
            val yOffset = positionOnSpike * stem.flowerSpike.maxLength
            
            val flowerX = topPoint.x + (Math.random().toFloat() - 0.5f) * 4f
            val flowerY = topPoint.y - yOffset
            val flowerSize = baseFlowerSize + Math.random().toFloat() * 2f
            
            val flower = LupinFlower(
                x = flowerX,
                y = flowerY,
                positionOnSpike = positionOnSpike,
                maxSize = flowerSize,
                color = spikeColor
            )
            
            stem.flowerSpike.flowers.add(flower)
        }
    }
    
    private fun growExistingFlowers(force: Float) {
        for (stem in stems) {
            if (!stem.flowerSpike.hasStartedBlooming) continue
            
            for (flower in stem.flowerSpike.flowers) {
                if (flower.currentSize < flower.maxSize && force > 0.01f) { // Seuil ULTRA bas
                    val growth = force * flowerGrowthRate * 0.03f
                    flower.currentSize = (flower.currentSize + growth).coerceAtMost(flower.maxSize)
                    
                    if (flower.currentSize >= flower.maxSize * 0.3f && flower.currentSize < flower.maxSize) { // TRÈS tôt
                        challengeManager?.notifyFlowerCreated(flower.x, flower.y, flower.id)
                    }
                }
            }
        }
    }
    
    // ==================== RENDU ====================
    
    private fun drawStems(canvas: Canvas, paint: Paint) {
        paint.color = Color.rgb(34, 139, 34)
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        
        for (stem in stems) {
            if (stem.points.size >= 2) {
                for (i in 1 until stem.points.size) {
                    val p1 = stem.points[i-1]
                    val p2 = stem.points[i]
                    
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
            if (leaf.currentSize > 0 && leaf.stemIndex < stems.size) {
                val stem = stems[leaf.stemIndex]
                val leafPoint = getStemPointAtHeight(stem, leaf.heightRatio)
                
                leafPoint?.let { point ->
                    drawPalmateLeaf(canvas, paint, point.x, point.y, leaf)
                }
            }
        }
    }
    
    private fun drawPalmateLeaf(canvas: Canvas, paint: Paint, x: Float, y: Float, leaf: LupinLeaf) {
        val size = leaf.currentSize
        if (size <= 0) return
        
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(leaf.angle)
        
        val folioleCount = leaf.folioleCount.coerceAtMost(leaf.folioleAngles.size)
        val angleSpread = 60f
        
        for (i in 0 until folioleCount) {
            val folioleAngle = (i - folioleCount / 2f) * (angleSpread / folioleCount) + leaf.folioleAngles[i]
            val folioleLength = size * (0.8f + (i % 3) * 0.1f)
            val folioleWidth = folioleLength * 0.3f
            
            canvas.save()
            canvas.rotate(folioleAngle)
            
            paint.color = Color.rgb(34, 139, 34)
            canvas.drawOval(
                -folioleWidth/2, 0f,
                folioleWidth/2, folioleLength,
                paint
            )
            
            paint.color = Color.rgb(20, 100, 20)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 1.5f
            canvas.drawLine(0f, 0f, 0f, folioleLength, paint)
            paint.style = Paint.Style.FILL
            
            canvas.restore()
        }
        
        canvas.restore()
    }
    
    private fun drawFlowerSpikes(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.FILL
        
        for (stem in stems) {
            if (!stem.flowerSpike.hasStartedBlooming) continue
            
            for (flower in stem.flowerSpike.flowers) {
                if (flower.currentSize > 0) {
                    val colorRgb = flower.color.rgb
                    paint.color = Color.rgb(colorRgb[0], colorRgb[1], colorRgb[2])
                    
                    val size = flower.currentSize
                    
                    // Pétale principal (étendard)
                    canvas.drawCircle(flower.x, flower.y, size * 0.8f, paint)
                    
                    // Pétales latéraux (ailes)
                    paint.color = Color.rgb(
                        (colorRgb[0] * 0.8f).toInt(),
                        (colorRgb[1] * 0.8f).toInt(),
                        (colorRgb[2] * 0.8f).toInt()
                    )
                    canvas.drawCircle(flower.x - size * 0.5f, flower.y + size * 0.3f, size * 0.5f, paint)
                    canvas.drawCircle(flower.x + size * 0.5f, flower.y + size * 0.3f, size * 0.5f, paint)
                }
            }
        }
    }
    
    // ==================== UTILITAIRES ====================
    
    private fun getStemPointAtHeight(stem: LupinStem, heightRatio: Float): StemPoint? {
        if (stem.points.size < 2) return null
        
        val targetHeight = stem.currentHeight * heightRatio
        var currentHeight = 0f
        
        for (i in 1 until stem.points.size) {
            val segmentHeight = stem.points[i-1].y - stem.points[i].y
            if (currentHeight + segmentHeight >= targetHeight) {
                val segmentRatio = (targetHeight - currentHeight) / segmentHeight
                val p1 = stem.points[i-1]
                val p2 = stem.points[i]
                
                val x = p1.x + (p2.x - p1.x) * segmentRatio
                val y = p1.y + (p2.y - p1.y) * segmentRatio
                val thickness = p1.thickness + (p2.thickness - p1.thickness) * segmentRatio
                
                return StemPoint(x, y, thickness)
            }
            currentHeight += segmentHeight
        }
        
        return stem.points.lastOrNull()
    }
    
    companion object {
        private var stemIdCounter = 0
        private var flowerIdCounter = 0
        
        private fun generateStemId(): String {
            stemIdCounter++
            return "lupinstem_$stemIdCounter"
        }
        
        private fun generateFlowerId(): String {
            flowerIdCounter++
            return "lupinflower_$flowerIdCounter"
        }
        
        private fun generateRandomGrowthSpeed(): Float {
            val variation = 0.1f
            return 1.0f + (Math.random().toFloat() - 0.5f) * 2 * variation
        }
    }
}
