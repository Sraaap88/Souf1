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
        val maxLength: Float = 200f, // Longueur de l'épi floral
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
    
    // Référence au gestionnaire de défis
    private var challengeManager: ChallengeManager? = null
    
    // ==================== PARAMÈTRES DE CROISSANCE ====================
    
    private val stemGrowthRate = 1800f     // Plus lent que les autres plantes
    private val leafGrowthRate = 600f
    private val flowerGrowthRate = 400f
    
    // Tailles
    private val baseStemThickness = 12f
    private val segmentLength = 25f        // Segments pour croissance fluide
    private val baseLeafSize = 60f
    private val baseFlowerSize = 8f        // Petites fleurs individuelles
    
    // Paramètres spécifiques au lupin
    private val stemCount = 2 + (Math.random() * 2).toInt() // 2-3 tiges principales
    private val stemSpacing = 80f          // Espacement entre tiges
    private val flowerDensity = 12         // Nombre de fleurs par épi
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun setChallengeManager(manager: ChallengeManager) {
        challengeManager = manager
    }
    
    fun initialize(centerX: Float, bottomY: Float) {
        baseX = centerX
        baseY = bottomY
        
        // Créer 2-3 tiges principales du lupin
        for (i in 0 until stemCount) {
            val offsetX = (i - stemCount / 2f) * stemSpacing
            val stemX = baseX + offsetX + (Math.random().toFloat() - 0.5f) * 30f // Légère variation
            
            val stem = LupinStem(
                maxHeight = screenHeight * 0.6f + Math.random().toFloat() * screenHeight * 0.2f,
                baseX = stemX,
                baseY = bottomY
            )
            
            // Point de base
            stem.points.add(StemPoint(stemX, bottomY, baseStemThickness))
            // Deuxième point pour commencer la croissance
            val secondY = bottomY - segmentLength * 0.3f
            stem.points.add(StemPoint(stemX, secondY, baseStemThickness * 0.98f))
            stem.currentHeight = segmentLength * 0.3f
            
            stems.add(stem)
        }
    }
    
    fun processStemGrowth(force: Float) {
        growStems(force)
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
    }
    
    fun drawLupin(canvas: Canvas, stemPaint: Paint, leafPaint: Paint, flowerPaint: Paint) {
        drawStems(canvas, stemPaint)
        drawLeaves(canvas, leafPaint)
        drawFlowerSpikes(canvas, flowerPaint)
    }
    
    // ==================== CROISSANCE DES TIGES ====================
    
    private fun growStems(force: Float) {
        for (stem in stems.filter { it.isActive }) {
            if (force > 0.15f && stem.currentHeight < stem.maxHeight) {
                // Croissance proportionnelle à la force ET à la vitesse individuelle
                val baseGrowth = force * stemGrowthRate * 0.020f
                val individualGrowth = baseGrowth * stem.growthSpeedMultiplier
                stem.currentHeight = (stem.currentHeight + individualGrowth).coerceAtMost(stem.maxHeight)
                
                // Ajouter un nouveau point si nécessaire
                if (stem.points.size >= 2 && stem.currentHeight >= stem.points.size * segmentLength) {
                    val lastPoint = stem.points.last()
                    
                    // Les lupins poussent très droit avec une légère variation naturelle
                    val randomOffset = (Math.random().toFloat() - 0.5f) * 4f
                    val newX = stem.baseX + randomOffset
                    val newY = lastPoint.y - segmentLength
                    val newThickness = (lastPoint.thickness * 0.98f).coerceAtLeast(4f)
                    
                    // Vérifier que ça reste dans l'écran
                    if (newX >= 0 && newX <= screenWidth && newY >= 0) {
                        stem.points.add(StemPoint(newX, newY, newThickness))
                    } else {
                        stem.isActive = false
                    }
                }
                
                // Arrêter quand on atteint la hauteur max
                if (stem.currentHeight >= stem.maxHeight * 0.95f) {
                    stem.isActive = false
                }
            }
        }
    }
    
    // ==================== FEUILLES PALMÉES ====================
    
    private fun createLeavesOnStems() {
        for ((index, stem) in stems.withIndex()) {
            if (stem.points.size < 3) continue
            
            val existingLeaves = leaves.filter { it.stemIndex == index }
            if (existingLeaves.isNotEmpty()) continue
            
            // Ne pas créer de feuilles dans les premiers 3cm
            if (stem.currentHeight < 80f) continue
            
            val leafCount = 3 + (Math.random() * 2).toInt() // 3-4 feuilles par tige
            
            for (i in 0 until leafCount) {
                val heightRatio = 0.2f + (i.toFloat() / leafCount) * 0.6f // Entre 20% et 80% de la hauteur
                val size = baseLeafSize + Math.random().toFloat() * 20f
                val angle = Math.random().toFloat() * 60f - 30f // Variation ±30°
                
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
            if (leaf.currentSize < leaf.maxSize && force > 0.15f) {
                val growth = force * leafGrowthRate * 0.025f
                leaf.currentSize = (leaf.currentSize + growth).coerceAtMost(leaf.maxSize)
            }
        }
    }
    
    // ==================== ÉPIS FLORAUX ====================
    
    private fun createFlowerSpikes() {
        for (stem in stems) {
            if (stem.points.size < 2) continue
            if (stem.flowerSpike.hasStartedBlooming) continue
            
            // Commencer la floraison quand la tige atteint 80% de sa hauteur
            if (!stem.isActive && stem.currentHeight > stem.maxHeight * 0.8f) {
                createFlowersOnSpike(stem)
                stem.flowerSpike.hasStartedBlooming = true
            }
        }
    }
    
    private fun createFlowersOnSpike(stem: LupinStem) {
        val topPoint = stem.points.lastOrNull() ?: return
        val spikeColor = FlowerColor.values().random() // Couleur aléatoire pour l'épi
        
        for (i in 0 until flowerDensity) {
            val positionOnSpike = i.toFloat() / (flowerDensity - 1) // 0.0 à 1.0
            val yOffset = positionOnSpike * stem.flowerSpike.maxLength
            
            val flowerX = topPoint.x + (Math.random().toFloat() - 0.5f) * 8f // Légère variation
            val flowerY = topPoint.y - yOffset
            val flowerSize = baseFlowerSize + Math.random().toFloat() * 4f
            
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
                if (flower.currentSize < flower.maxSize && force > 0.15f) {
                    val growth = force * flowerGrowthRate * 0.025f
                    flower.currentSize = (flower.currentSize + growth).coerceAtMost(flower.maxSize)
                    
                    // Notifier le challenge manager quand une fleur atteint sa taille max
                    if (flower.currentSize >= flower.maxSize * 0.9f) {
                        challengeManager?.notifyFlowerCreated(flower.x, flower.y, flower.id)
                    }
                }
            }
        }
    }
    
    // ==================== RENDU ====================
    
    private fun drawStems(canvas: Canvas, paint: Paint) {
        paint.color = Color.rgb(34, 139, 34) // Vert tige
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
        paint.color = Color.rgb(34, 139, 34) // Vert feuille
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
        
        // Dessiner les folioles en éventail (caractéristique du lupin)
        val folioleCount = leaf.folioleCount.coerceAtMost(leaf.folioleAngles.size)
        val angleSpread = 60f // Éventail de 60°
        
        for (i in 0 until folioleCount) {
            val folioleAngle = (i - folioleCount / 2f) * (angleSpread / folioleCount) + leaf.folioleAngles[i]
            val folioleLength = size * (0.8f + Math.random().toFloat() * 0.4f) // Variation de longueur
            val folioleWidth = folioleLength * 0.3f
            
            canvas.save()
            canvas.rotate(folioleAngle)
            
            // Dessiner une foliole elliptique
            paint.color = Color.rgb(34, 139, 34)
            canvas.drawOval(
                -folioleWidth/2, 0f,
                folioleWidth/2, folioleLength,
                paint
            )
            
            // Nervure centrale
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
                    // Couleur de la fleur
                    val colorRgb = flower.color.rgb
                    paint.color = Color.rgb(colorRgb[0], colorRgb[1], colorRgb[2])
                    
                    // Dessiner la fleur papillonacée (simplifiée)
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
            // Variation de ±30% pour les lupins (plus variable que les autres)
            val variation = 0.3f
            return 1.0f + (Math.random().toFloat() - 0.5f) * 2 * variation
        }
    }
}
