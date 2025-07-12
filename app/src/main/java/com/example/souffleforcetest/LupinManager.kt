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
        val maxLength: Float = 150f, // Épis plus longs pour les grandes tiges
        var hasStartedBlooming: Boolean = false
    )
    
    data class LupinFlower(
        val x: Float,
        val y: Float,
        val positionOnSpike: Float,
        var currentSize: Float = 0f,
        val maxSize: Float,
        val color: FlowerColor,
        val id: String = generateFlowerId()
    )
    
    data class LupinLeaf(
        val stemIndex: Int,
        val heightRatio: Float,
        var currentSize: Float = 0f,
        val maxSize: Float,
        val angle: Float,
        val folioleCount: Int = 5 + (Math.random() * 4).toInt(),
        val folioleAngles: List<Float> = generateFolioleAngles()
    ) {
        companion object {
            private fun generateFolioleAngles(): List<Float> {
                return (0..8).map { Math.random().toFloat() * 30f - 15f }
            }
        }
    }
    
    enum class FlowerColor(val rgb: IntArray) {
        PURPLE(intArrayOf(138, 43, 226)),
        BLUE(intArrayOf(65, 105, 225)),
        PINK(intArrayOf(255, 20, 147)),
        WHITE(intArrayOf(248, 248, 255)),
        YELLOW(intArrayOf(255, 215, 0))
    }
    
    // ==================== VARIABLES PRINCIPALES ====================
    
    private val stems = mutableListOf<LupinStem>()
    private val leaves = mutableListOf<LupinLeaf>()
    
    private var baseX = 0f
    private var baseY = 0f
    private var lastForce = 0f
    private var lastSpikeTime = 0L
    
    private var challengeManager: ChallengeManager? = null
    
    // ==================== PARAMÈTRES ÉQUILIBRÉS COMME PLANTSTEM ====================
    
    // Croissance calquée sur PlantStem
    private val stemGrowthRate = 2400f      // Même que PlantStem
    private val leafGrowthRate = 800f       // Proportionnel
    private val flowerGrowthRate = 600f     // Proportionnel
    
    // Tailles proportionnelles
    private val baseStemThickness = 12f
    private val segmentLength = 25f
    private val baseLeafSize = 55f
    private val baseFlowerSize = 8f
    
    // Paramètres équilibrés
    private val maxStems = 7                // Augmenté à 7 tiges max
    private val baseStemSpacing = 35f       // Espacement de base
    private val flowerDensity = 12
    
    // Seuils IDENTIQUES à PlantStem
    private val forceThreshold = 0.15f      // MÊME que PlantStem !
    private val spikeThreshold = 0.08f
    private val spikeMinInterval = 300L
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun setChallengeManager(manager: ChallengeManager) {
        challengeManager = manager
    }
    
    fun initialize(centerX: Float, bottomY: Float) {
        baseX = centerX
        baseY = bottomY
        createNewStem(baseX, baseY)
    }
    
    fun processStemGrowth(force: Float) {
        detectSpikeAndCreateStem(force)
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
    
    // ==================== CROISSANCE CORRIGÉE ====================
    
    private fun detectSpikeAndCreateStem(force: Float) {
        val currentTime = System.currentTimeMillis()
        val forceIncrease = force - lastForce
        val isSpike = forceIncrease > spikeThreshold && force > forceThreshold  // Utilise forceThreshold comme PlantStem
        val canCreateStem = currentTime - lastSpikeTime > spikeMinInterval
        
        if (isSpike && canCreateStem && stems.size < maxStems) {
            // Position réaliste pour bouquet naturel
            val newStemX = calculateRealisticStemPosition(stems.size)
            createNewStem(newStemX, baseY)
            lastSpikeTime = currentTime
        }
    }
    
    private fun createNewStem(stemX: Float, stemY: Float) {
        val stem = LupinStem(
            maxHeight = screenHeight * 0.6f + Math.random().toFloat() * screenHeight * 0.2f, // 60-80% de l'écran !
            baseX = stemX,
            baseY = stemY
        )
        
        // Points de départ pour grande tige
        stem.points.add(StemPoint(stemX, stemY, baseStemThickness))
        val secondY = stemY - 20f // Segment initial plus substantiel
        stem.points.add(StemPoint(stemX, secondY, baseStemThickness * 0.98f))
        stem.currentHeight = 20f
        
        stems.add(stem)
        challengeManager?.notifyLupinSpikeCreated("NEW_STEM", stem.id)
    }
    
    private fun growLatestStem(force: Float) {
        val latestStem = stems.lastOrNull() ?: return
        
        // SEUIL IDENTIQUE À PLANTSTEM
        if (latestStem.isActive && force > forceThreshold && latestStem.currentHeight < latestStem.maxHeight) {
            
            // CROISSANCE CALQUÉE SUR PLANTSTEM
            val baseGrowth = force * stemGrowthRate * 0.008f  // Même multiplicateur que PlantStem
            val individualGrowth = baseGrowth * latestStem.growthSpeedMultiplier
            latestStem.currentHeight = (latestStem.currentHeight + individualGrowth).coerceAtMost(latestStem.maxHeight)
            
            // Ajouter des segments régulièrement
            if (latestStem.points.size >= 2 && latestStem.currentHeight >= latestStem.points.size * segmentLength) {
                val lastPoint = latestStem.points.last()
                
                val randomOffset = (Math.random().toFloat() - 0.5f) * 2f
                val newX = latestStem.baseX + randomOffset
                val newY = lastPoint.y - segmentLength
                val newThickness = (lastPoint.thickness * 0.96f).coerceAtLeast(3f)
                
                if (newX >= 0 && newX <= screenWidth && newY >= 0) {
                    latestStem.points.add(StemPoint(newX, newY, newThickness))
                } else {
                    latestStem.isActive = false
                }
            }
            
            // Arrêter plus tard pour avoir de vraies grandes tiges
            if (latestStem.currentHeight >= latestStem.maxHeight * 0.95f) {
                latestStem.isActive = false
            }
        }
    }
    
    // ==================== POSITIONNEMENT RÉALISTE DES TIGES ====================
    
    private fun calculateRealisticStemPosition(stemIndex: Int): Float {
        // Position selon un bouquet naturel asymétrique
        return when (stemIndex) {
            0 -> baseX                                          // Centre
            1 -> baseX + baseStemSpacing * 0.8f                // Droite proche
            2 -> baseX - baseStemSpacing * 1.1f                // Gauche moyen
            3 -> baseX + baseStemSpacing * 1.6f                // Droite moyen  
            4 -> baseX - baseStemSpacing * 0.5f                // Gauche proche
            5 -> baseX + baseStemSpacing * 2.3f                // Droite loin
            6 -> baseX - baseStemSpacing * 1.9f                // Gauche loin
            else -> baseX + (Math.random().toFloat() - 0.5f) * baseStemSpacing * 3f
        }.let { x ->
            // Ajouter variation naturelle ±8px
            x + (Math.random().toFloat() - 0.5f) * 16f
        }
    }
    
    // ==================== FEUILLES CORRIGÉES ====================
    
    private fun createLeavesOnStems() {
        for ((index, stem) in stems.withIndex()) {
            if (stem.points.size < 2) continue
            
            val existingLeaves = leaves.filter { it.stemIndex == index }
            if (existingLeaves.isNotEmpty()) continue
            
            // Créer des feuilles quand la tige commence à bien pousser
            if (stem.currentHeight < 40f) continue
            
            val leafCount = 3 // Plus de feuilles sur les grandes tiges
            
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
            if (leaf.currentSize < leaf.maxSize && force > forceThreshold) {
                val growth = force * leafGrowthRate * 0.008f  // Même ratio que PlantStem
                leaf.currentSize = (leaf.currentSize + growth).coerceAtMost(leaf.maxSize)
            }
        }
    }
    
    // ==================== FLEURS CORRIGÉES ====================
    
    private fun createFlowerSpikes() {
        for (stem in stems) {
            if (stem.points.size < 2) continue
            if (stem.flowerSpike.hasStartedBlooming) continue
            
            // Floraison quand la tige arrête de pousser
            if (!stem.isActive && stem.currentHeight > stem.maxHeight * 0.15f) {
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
                if (flower.currentSize < flower.maxSize && force > forceThreshold) {
                    val growth = force * flowerGrowthRate * 0.008f  // Même ratio que PlantStem
                    flower.currentSize = (flower.currentSize + growth).coerceAtMost(flower.maxSize)
                    
                    // Notification dès que la fleur commence à être visible
                    if (flower.currentSize >= flower.maxSize * 0.2f && flower.currentSize < flower.maxSize) {
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
        val angleSpread = 70f
        
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
                    
                    // Pétale principal
                    canvas.drawCircle(flower.x, flower.y, size * 1.0f, paint)
                    
                    // Pétales latéraux
                    paint.color = Color.rgb(
                        (colorRgb[0] * 0.8f).toInt(),
                        (colorRgb[1] * 0.8f).toInt(),
                        (colorRgb[2] * 0.8f).toInt()
                    )
                    canvas.drawCircle(flower.x - size * 0.6f, flower.y + size * 0.3f, size * 0.6f, paint)
                    canvas.drawCircle(flower.x + size * 0.6f, flower.y + size * 0.3f, size * 0.6f, paint)
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
