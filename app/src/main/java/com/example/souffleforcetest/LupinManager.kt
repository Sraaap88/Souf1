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
        val maxLength: Float = 150f,
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
    
    private var challengeManager: ChallengeManager? = null
    
    // ==================== SYSTÈME ORDRE ALÉATOIRE COPIÉ DE PLANTSTEM ====================
    
    private var saccadeCount = 0
    private var isCurrentlyBreathing = false
    private var lastSaccadeTime = 0L
    private val saccadeCooldown = 300L
    private val breathStartThreshold = 0.3f
    private val breathEndThreshold = 0.2f
    
    // Pool de toutes les tiges possibles en ordre aléatoire
    private var stemOrderPool = mutableListOf<Int>() // 0=principale, 1-6=branches
    private var currentActiveStemIndex = -1 // Index dans le pool (-1 = aucune active)
    
    // ==================== PARAMÈTRES COPIÉS DE PLANTSTEM ====================
    
    private val forceThreshold = 0.15f      // EXACTEMENT comme PlantStem
    private val maxStemHeight = 0.75f       // EXACTEMENT comme PlantStem
    private val baseThickness = 17.5f       // 30% plus fin (25 * 0.7)
    private val tipThickness = 5.6f         // 30% plus fin (8 * 0.7)
    private val growthRate = 7200f          // 3X plus rapide (2400 * 3)
    private val maxBranches = 14            // 14 tiges max (7 paires)
    
    private val baseLeafSize = 83f
    private val baseFlowerSize = 20f        // 2X plus gros (10 * 2)
    private val flowerDensity = 12
    
    init {
        setupRandomStemOrder()
    }
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun setChallengeManager(manager: ChallengeManager) {
        challengeManager = manager
    }
    
    fun initialize(centerX: Float, bottomY: Float) {
        baseX = centerX
        baseY = bottomY
        
        // Créer le point de base comme PlantStem
        if (stems.isEmpty()) {
            createMainStem()
        }
    }
    
    fun processStemGrowth(force: Float) {
        // COPIE EXACTE de PlantStem.processStemGrowth
        if (stems.isEmpty()) {
            createMainStem()
            
            // Détecter immédiatement une première saccade si on souffle fort
            if (force > forceThreshold) {
                saccadeCount = 1
                currentActiveStemIndex = 0
                lastSaccadeTime = System.currentTimeMillis()
                isCurrentlyBreathing = true
            }
        }
        
        // Détection des saccades et activation des tiges
        detectSaccadesAndActivateStems(force, System.currentTimeMillis())
        
        // Faire pousser SEULEMENT la tige actuellement active
        if (force > forceThreshold && currentActiveStemIndex >= 0) {
            growOnlyActiveStem(force)
        }
        
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
        
        // Reset du système aléatoire
        saccadeCount = 0
        isCurrentlyBreathing = false
        lastSaccadeTime = 0L
        currentActiveStemIndex = -1
        setupRandomStemOrder()
    }
    
    fun drawLupin(canvas: Canvas, stemPaint: Paint, leafPaint: Paint, flowerPaint: Paint) {
        drawStems(canvas, stemPaint)
        drawLeaves(canvas, leafPaint)
        drawFlowerSpikes(canvas, flowerPaint)
    }
    
    // ==================== SYSTÈME COPIÉ DE PLANTSTEM ====================
    
    private fun setupRandomStemOrder() {
        // Créer un pool des 7 paires de tiges possibles (0=principale, 1-6=paires)
        stemOrderPool = mutableListOf(0, 1, 2, 3, 4, 5, 6)
        stemOrderPool.shuffle() // Mélanger l'ordre de façon aléatoire
    }
    
    private fun detectSaccadesAndActivateStems(force: Float, currentTime: Long) {
        val wasBreathing = isCurrentlyBreathing
        val isNowBreathing = force > breathStartThreshold
        
        // Détection début de souffle = nouvelle saccade
        if (!wasBreathing && isNowBreathing) {
            if (currentTime - lastSaccadeTime > saccadeCooldown) {
                saccadeCount++
                lastSaccadeTime = currentTime
                isCurrentlyBreathing = true
                
                // Activer la prochaine tige selon l'ordre aléatoire
                activateNextStemInOrder()
            }
        }
        
        // Détection fin de souffle
        if (wasBreathing && force < breathEndThreshold) {
            isCurrentlyBreathing = false
        }
    }
    
    private fun activateNextStemInOrder() {
        if (saccadeCount <= stemOrderPool.size) {
            // Prendre la paire suivante dans l'ordre aléatoire
            val pairTypeToActivate = stemOrderPool[saccadeCount - 1]
            currentActiveStemIndex = saccadeCount - 1
            
            if (pairTypeToActivate == 0) {
                // Paire principale (déjà créée)
                println("Saccade $saccadeCount: Paire PRINCIPALE activée")
            } else {
                // Créer une nouvelle paire de tiges à côté
                println("Saccade $saccadeCount: Nouvelle paire $pairTypeToActivate créée")
                createNewStemPair(pairTypeToActivate)
            }
        }
    }
    
    private fun createMainStem() {
        // Créer la première paire de tiges au centre avec plus d'espacement
        val baseSpacing = 30f  // Espacement entre les 2 tiges de la paire
        
        // Première tige de la paire
        val stem1 = LupinStem(
            maxHeight = screenHeight * maxStemHeight * (0.9f + Math.random().toFloat() * 0.2f),
            baseX = baseX - baseSpacing/2,
            baseY = baseY,
            growthSpeedMultiplier = 0.9f + Math.random().toFloat() * 0.2f
        )
        stem1.points.add(StemPoint(baseX - baseSpacing/2, baseY, baseThickness))
        stems.add(stem1)
        
        // Deuxième tige de la paire
        val stem2 = LupinStem(
            maxHeight = screenHeight * maxStemHeight * (0.9f + Math.random().toFloat() * 0.2f),
            baseX = baseX + baseSpacing/2,
            baseY = baseY,
            growthSpeedMultiplier = 0.9f + Math.random().toFloat() * 0.2f
        )
        stem2.points.add(StemPoint(baseX + baseSpacing/2, baseY, baseThickness))
        stems.add(stem2)
        
        challengeManager?.notifyLupinSpikeCreated("NEW_STEM", stem1.id)
        challengeManager?.notifyLupinSpikeCreated("NEW_STEM", stem2.id)
    }
    
    private fun createNewStemPair(pairNumber: Int) {
        val pairSpacing = 45f  // Plus d'espacement entre les paires
        val stemSpacing = 25f  // Espacement entre les 2 tiges d'une paire
        val side = if (pairNumber % 2 == 0) 1f else -1f
        val distance = (pairNumber / 2) * pairSpacing
        
        // Position de base pour cette paire
        val pairBaseX = baseX + (side * distance)
        
        val newX1 = pairBaseX - stemSpacing/2
        val newX2 = pairBaseX + stemSpacing/2
        
        // Première tige de la paire
        val stem1 = LupinStem(
            maxHeight = screenHeight * maxStemHeight * (0.85f + Math.random().toFloat() * 0.3f),
            baseX = newX1,
            baseY = baseY,
            growthSpeedMultiplier = 0.8f + Math.random().toFloat() * 0.4f
        )
        stem1.points.add(StemPoint(newX1, baseY, baseThickness))
        stems.add(stem1)
        
        // Deuxième tige de la paire
        val stem2 = LupinStem(
            maxHeight = screenHeight * maxStemHeight * (0.85f + Math.random().toFloat() * 0.3f),
            baseX = newX2,
            baseY = baseY,
            growthSpeedMultiplier = 0.8f + Math.random().toFloat() * 0.4f
        )
        stem2.points.add(StemPoint(newX2, baseY, baseThickness))
        stems.add(stem2)
        
        challengeManager?.notifyLupinSpikeCreated("NEW_STEM", stem1.id)
        challengeManager?.notifyLupinSpikeCreated("NEW_STEM", stem2.id)
    }
    
    private fun growOnlyActiveStem(force: Float) {
        if (currentActiveStemIndex < 0) return
        
        // Faire pousser toutes les tiges de la paire active
        val stemsInCurrentPair = if (currentActiveStemIndex == 0) {
            // Paire principale : les 2 premières tiges
            stems.take(2)
        } else {
            // Autres paires : 2 tiges par paire, en commençant après la paire principale
            val startIndex = 2 + (currentActiveStemIndex - 1) * 2
            stems.drop(startIndex).take(2)
        }
        
        for (activeStem in stemsInCurrentPair) {
            if (activeStem.currentHeight >= activeStem.maxHeight) continue
            
            // COPIE EXACTE de la logique de croissance de PlantStem avec vitesse individuelle
            val forceStability = 1f - abs(force - lastForce).coerceAtMost(0.5f) * 2f
            val qualityMultiplier = 0.5f + forceStability * 0.5f
            
            val growthProgress = activeStem.currentHeight / activeStem.maxHeight
            val progressCurve = 1f - growthProgress * growthProgress
            val adjustedGrowth = force * qualityMultiplier * progressCurve * growthRate * 0.008f * activeStem.growthSpeedMultiplier
            
            if (adjustedGrowth > 0) {
                activeStem.currentHeight += adjustedGrowth
                
                val lastPoint = activeStem.points.lastOrNull() ?: continue
                val segmentHeight = 7f + (Math.random() * 2f).toFloat()
                val segments = (adjustedGrowth / segmentHeight).toInt().coerceAtLeast(1)
                
                for (i in 1..segments) {
                    val currentHeight = activeStem.currentHeight - adjustedGrowth + (adjustedGrowth * i / segments)
                    val progressFromBase = currentHeight / activeStem.maxHeight
                    
                    val thicknessProgress = progressFromBase * 0.4f
                    val thickness = baseThickness * (1f - thicknessProgress)
                    
                    val currentX = activeStem.baseX + (Math.random().toFloat() - 0.5f) * 3f
                    val currentY = baseY - currentHeight
                    
                    val newPoint = StemPoint(currentX, currentY, thickness)
                    activeStem.points.add(newPoint)
                }
            }
        }
    }
    
    // ==================== FEUILLES ====================
    
    private fun createLeavesOnStems() {
        for ((index, stem) in stems.withIndex()) {
            if (stem.points.size < 2) continue
            
            val existingLeaves = leaves.filter { it.stemIndex == index }
            if (existingLeaves.isNotEmpty()) continue
            
            if (stem.currentHeight < 40f) continue
            
            val leafCount = 3
            
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
                val growth = force * 800f * 0.008f
                leaf.currentSize = (leaf.currentSize + growth).coerceAtMost(leaf.maxSize)
            }
        }
    }
    
    // ==================== FLEURS ====================
    
    private fun createFlowerSpikes() {
        for (stem in stems) {
            if (stem.points.size < 2) continue
            if (stem.flowerSpike.hasStartedBlooming) continue
            
            // Fleurs BEAUCOUP plus faciles - dès 40% de hauteur
            if (stem.currentHeight > stem.maxHeight * 0.4f) {
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
                    val growth = force * 600f * 0.008f
                    flower.currentSize = (flower.currentSize + growth).coerceAtMost(flower.maxSize)
                    
                    if (flower.currentSize >= flower.maxSize * 0.1f && flower.currentSize < flower.maxSize) {
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
            val folioleWidth = folioleLength * 0.35f
            
            canvas.save()
            canvas.rotate(folioleAngle)
            
            // Foliole plus réaliste avec dégradé
            paint.color = Color.rgb(34, 139, 34)
            paint.style = Paint.Style.FILL
            
            // Forme de foliole plus naturelle (ovale allongé)
            canvas.drawOval(
                -folioleWidth/2, 0f,
                folioleWidth/2, folioleLength,
                paint
            )
            
            // Nervure centrale plus prononcée
            paint.color = Color.rgb(20, 100, 20)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawLine(0f, folioleLength * 0.1f, 0f, folioleLength * 0.9f, paint)
            
            // Nervures secondaires
            paint.strokeWidth = 1f
            paint.color = Color.rgb(25, 110, 25)
            for (j in 1..3) {
                val nervureY = folioleLength * (0.2f + j * 0.2f)
                val nervureWidth = folioleWidth * (0.3f - j * 0.05f)
                canvas.drawLine(-nervureWidth/2, nervureY, 0f, nervureY * 0.9f, paint)
                canvas.drawLine(nervureWidth/2, nervureY, 0f, nervureY * 0.9f, paint)
            }
            
            paint.style = Paint.Style.FILL
            canvas.restore()
        }
        
        // Pétiole (tige de la feuille)
        paint.color = Color.rgb(40, 120, 40)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        canvas.drawLine(0f, 0f, 0f, -size * 0.2f, paint)
        paint.style = Paint.Style.FILL
        
        canvas.restore()
    }
    
    private fun drawFlowerSpikes(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.FILL
        
        for (stem in stems) {
            if (!stem.flowerSpike.hasStartedBlooming) continue
            
            for (flower in stem.flowerSpike.flowers) {
                if (flower.currentSize > 0) {
                    val colorRgb = flower.color.rgb
                    val size = flower.currentSize
                    
                    // Fleur en forme de pois (papillonacée) comme vrais lupins
                    paint.color = Color.rgb(colorRgb[0], colorRgb[1], colorRgb[2])
                    
                    // Étendard (pétale principal en haut)
                    canvas.drawOval(
                        flower.x - size * 0.6f, flower.y - size * 0.8f,
                        flower.x + size * 0.6f, flower.y - size * 0.2f, 
                        paint
                    )
                    
                    // Ailes (pétales latéraux)
                    paint.color = Color.rgb(
                        (colorRgb[0] * 0.85f).toInt(),
                        (colorRgb[1] * 0.85f).toInt(),
                        (colorRgb[2] * 0.85f).toInt()
                    )
                    canvas.drawOval(
                        flower.x - size * 0.8f, flower.y - size * 0.3f,
                        flower.x - size * 0.1f, flower.y + size * 0.2f,
                        paint
                    )
                    canvas.drawOval(
                        flower.x + size * 0.1f, flower.y - size * 0.3f,
                        flower.x + size * 0.8f, flower.y + size * 0.2f,
                        paint
                    )
                    
                    // Carène (pétale inférieur pointu)
                    paint.color = Color.rgb(
                        (colorRgb[0] * 0.7f).toInt(),
                        (colorRgb[1] * 0.7f).toInt(),
                        (colorRgb[2] * 0.7f).toInt()
                    )
                    canvas.drawOval(
                        flower.x - size * 0.3f, flower.y,
                        flower.x + size * 0.3f, flower.y + size * 0.6f,
                        paint
                    )
                    
                    // Centre plus sombre
                    paint.color = Color.rgb(
                        (colorRgb[0] * 0.5f).toInt(),
                        (colorRgb[1] * 0.5f).toInt(),
                        (colorRgb[2] * 0.5f).toInt()
                    )
                    canvas.drawCircle(flower.x, flower.y, size * 0.15f, paint)
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
