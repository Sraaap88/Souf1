package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import kotlin.math.*

class IrisManager(private val screenWidth: Int, private val screenHeight: Int) {
    
    // ==================== DATA CLASSES ====================
    
    data class IrisStem(
        val points: MutableList<StemPoint> = mutableListOf(),
        var currentHeight: Float = 0f,
        val maxHeight: Float,
        val baseX: Float,
        val baseY: Float,
        var isActive: Boolean = true,
        val id: String = generateStemId(),
        val growthSpeedMultiplier: Float = generateRandomGrowthSpeed(),
        var hasFlower: Boolean = false,
        var flower: IrisFlower? = null
    )
    
    data class StemPoint(
        val x: Float,
        val y: Float,
        val thickness: Float
    )
    
    data class IrisFlower(
        val x: Float,
        val y: Float,
        var currentSize: Float = 0f,
        val maxSize: Float,
        val color: FlowerColor,
        val id: String = generateFlowerId(),
        var petalPhase: Float = 0f,
        val petalCount: Int = 6
    )
    
    data class IrisLeaf(
        val stemIndex: Int,
        val side: LeafSide,
        var currentHeight: Float = 0f,
        val maxHeight: Float,
        val width: Float,
        val angle: Float,
        val id: String = generateLeafId()
    )
    
    enum class LeafSide {
        LEFT, RIGHT
    }
    
    enum class FlowerColor(val rgb: IntArray, val darkRgb: IntArray) {
        PURPLE(intArrayOf(138, 43, 226), intArrayOf(75, 0, 130)),
        BLUE(intArrayOf(65, 105, 225), intArrayOf(25, 25, 112)),
        WHITE(intArrayOf(248, 248, 255), intArrayOf(220, 220, 220)),
        YELLOW(intArrayOf(255, 215, 0), intArrayOf(255, 165, 0)),
        PINK(intArrayOf(255, 182, 193), intArrayOf(219, 112, 147))
    }
    
    // ==================== VARIABLES PRINCIPALES ====================
    
    private val stems = mutableListOf<IrisStem>()
    private val leaves = mutableListOf<IrisLeaf>()
    
    private var baseX = 0f
    private var baseY = 0f
    private var lastForce = 0f
    private var lastSpikeTime = 0L
    
    // Référence au gestionnaire de défis
    private var challengeManager: ChallengeManager? = null
    
    // ==================== PARAMÈTRES EXPLOSIFS ====================
    
    private val stemGrowthRate = 12000f    // GIGANTESQUE
    private val leafGrowthRate = 4000f     // GIGANTESQUE
    private val flowerGrowthRate = 3000f   // GIGANTESQUE
    
    // Tailles réduites pour croissance RAPIDE
    private val baseStemThickness = 6f
    private val segmentLength = 10f        // Court
    private val baseLeafHeight = 50f       // Réduit
    private val baseLeafWidth = 8f         // Réduit
    private val baseFlowerSize = 15f       // Réduit
    
    // Paramètres généreux
    private val maxStems = 5
    private val stemSpacing = 40f
    private val leavesPerStem = 2          // Moins de feuilles = plus rapide
    
    // Paramètres ULTRA sensibles
    private val spikeThreshold = 0.03f     // TRÈS bas
    private val spikeMinInterval = 80L     // TRÈS court
    
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
        
        // Faire pousser toutes les tiges actives
        growActiveStems(force)
        
        lastForce = force
    }
    
    fun processLeavesGrowth(force: Float) {
        createLeavesOnStems()
        growExistingLeaves(force)
    }
    
    fun processFlowerGrowth(force: Float) {
        createFlowersOnStems()
        growExistingFlowers(force)
    }
    
    fun reset() {
        stems.clear()
        leaves.clear()
        lastForce = 0f
        lastSpikeTime = 0L
    }
    
    fun drawIris(canvas: Canvas, stemPaint: Paint, leafPaint: Paint, flowerPaint: Paint) {
        drawLeaves(canvas, leafPaint)
        drawStems(canvas, stemPaint)
        drawFlowers(canvas, flowerPaint)
    }
    
    // ==================== CROISSANCE EXPLOSIVE ====================
    
    private fun detectSpikeAndCreateStem(force: Float) {
        val currentTime = System.currentTimeMillis()
        
        // Détection TRÈS facile
        val forceIncrease = force - lastForce
        val isSpike = forceIncrease > spikeThreshold && force > 0.02f  // TRÈS bas
        val canCreateStem = currentTime - lastSpikeTime > spikeMinInterval
        
        if (isSpike && canCreateStem && stems.size < maxStems) {
            val newStemX = baseX + (stems.size - 2) * stemSpacing + (Math.random().toFloat() - 0.5f) * 15f
            createNewStem(newStemX, baseY)
            lastSpikeTime = currentTime
        }
    }
    
    private fun createNewStem(stemX: Float, stemY: Float) {
        val stem = IrisStem(
            maxHeight = screenHeight * 0.3f + Math.random().toFloat() * screenHeight * 0.1f, // Réduit
            baseX = stemX,
            baseY = stemY
        )
        
        // Point de base
        stem.points.add(StemPoint(stemX, stemY, baseStemThickness))
        // Deuxième point pour commencer la croissance
        val secondY = stemY - segmentLength * 0.2f
        stem.points.add(StemPoint(stemX, secondY, baseStemThickness * 0.99f))
        stem.currentHeight = segmentLength * 0.2f
        
        stems.add(stem)
        
        // Notifier le challenge manager qu'une tige a été créée
        challengeManager?.notifyIrisStemCreated(stem.id)
    }
    
    private fun growActiveStems(force: Float) {
        for (stem in stems) {
            if (stem.isActive && force > 0.005f && stem.currentHeight < stem.maxHeight) { // TRÈS bas
                // Croissance ÉNORME
                val baseGrowth = force * stemGrowthRate * 0.08f  // GIGANTESQUE
                val individualGrowth = baseGrowth * stem.growthSpeedMultiplier
                stem.currentHeight = (stem.currentHeight + individualGrowth).coerceAtMost(stem.maxHeight)
                
                // Ajouter un nouveau point si nécessaire
                if (stem.points.size >= 2 && stem.currentHeight >= stem.points.size * segmentLength) {
                    val lastPoint = stem.points.last()
                    
                    // Les iris poussent droits avec une très légère variation
                    val randomOffset = (Math.random().toFloat() - 0.5f) * 1f  // Très petit
                    val newX = stem.baseX + randomOffset
                    val newY = lastPoint.y - segmentLength
                    val newThickness = (lastPoint.thickness * 0.98f).coerceAtLeast(2f)
                    
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
    
    // ==================== FEUILLES EXPLOSIVES ====================
    
    private fun createLeavesOnStems() {
        for ((index, stem) in stems.withIndex()) {
            if (stem.points.size < 2) continue
            
            val existingLeaves = leaves.filter { it.stemIndex == index }
            if (existingLeaves.isNotEmpty()) continue
            
            // Ne pas créer de feuilles dans les premiers centimètres
            if (stem.currentHeight < 15f) continue  // TRÈS bas
            
            // Créer 2 feuilles par tige seulement
            for (i in 0 until leavesPerStem) {
                val side = if (i % 2 == 0) LeafSide.LEFT else LeafSide.RIGHT
                val maxHeight = baseLeafHeight + Math.random().toFloat() * 20f
                val width = baseLeafWidth + Math.random().toFloat() * 3f
                val angle = if (side == LeafSide.LEFT) -10f - Math.random().toFloat() * 5f 
                           else 10f + Math.random().toFloat() * 5f
                
                val leaf = IrisLeaf(
                    stemIndex = index,
                    side = side,
                    maxHeight = maxHeight,
                    width = width,
                    angle = angle
                )
                
                leaves.add(leaf)
            }
        }
    }
    
    private fun growExistingLeaves(force: Float) {
        for (leaf in leaves) {
            if (leaf.currentHeight < leaf.maxHeight && force > 0.005f) { // TRÈS bas
                val growth = force * leafGrowthRate * 0.06f  // GIGANTESQUE
                leaf.currentHeight = (leaf.currentHeight + growth).coerceAtMost(leaf.maxHeight)
            }
        }
    }
    
    // ==================== FLEURS EXPLOSIVES ====================
    
    private fun createFlowersOnStems() {
        for (stem in stems) {
            if (stem.points.size < 2) continue
            if (stem.hasFlower) continue
            
            // Commencer la floraison quand la tige atteint 40% de sa hauteur
            if (!stem.isActive && stem.currentHeight > stem.maxHeight * 0.4f) {
                createFlowerOnStem(stem)
                stem.hasFlower = true
            }
        }
    }
    
    private fun createFlowerOnStem(stem: IrisStem) {
        val topPoint = stem.points.lastOrNull() ?: return
        val flowerColor = FlowerColor.values().random()
        
        // Notifier le challenge manager qu'une fleur a été créée
        challengeManager?.notifyIrisFlowerCreated(flowerColor.name, stem.id)
        
        val flowerSize = baseFlowerSize + Math.random().toFloat() * 5f
        
        val flower = IrisFlower(
            x = topPoint.x,
            y = topPoint.y - 5f, // Légèrement au-dessus de la tige
            maxSize = flowerSize,
            color = flowerColor
        )
        
        stem.flower = flower
    }
    
    private fun growExistingFlowers(force: Float) {
        for (stem in stems) {
            val flower = stem.flower ?: continue
            
            if (flower.currentSize < flower.maxSize && force > 0.005f) { // TRÈS bas
                val growth = force * flowerGrowthRate * 0.06f  // GIGANTESQUE
                flower.currentSize = (flower.currentSize + growth).coerceAtMost(flower.maxSize)
                
                // Animation des pétales
                flower.petalPhase += 0.05f
                if (flower.petalPhase > 2 * PI) flower.petalPhase = 0f
                
                // Notifier quand la fleur atteint sa taille finale
                if (flower.currentSize >= flower.maxSize * 0.5f) { // TRÈS tôt
                    challengeManager?.notifyFlowerCreated(flower.x, flower.y, flower.id)
                }
            }
        }
    }
    
    // ==================== RENDU ====================
    
    private fun drawStems(canvas: Canvas, paint: Paint) {
        paint.color = Color.rgb(50, 120, 50) // Vert un peu plus clair
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
        paint.color = Color.rgb(60, 130, 60) // Vert feuillage
        paint.style = Paint.Style.FILL
        
        for (leaf in leaves) {
            if (leaf.currentHeight > 0 && leaf.stemIndex < stems.size) {
                val stem = stems[leaf.stemIndex]
                val basePoint = stem.points.firstOrNull() ?: continue
                
                drawIrisLeaf(canvas, paint, basePoint.x, basePoint.y, leaf)
            }
        }
    }
    
    private fun drawIrisLeaf(canvas: Canvas, paint: Paint, baseX: Float, baseY: Float, leaf: IrisLeaf) {
        if (leaf.currentHeight <= 0) return
        
        canvas.save()
        canvas.translate(baseX, baseY)
        canvas.rotate(leaf.angle)
        
        // Dessiner une feuille longue et effilée (caractéristique des iris)
        val height = leaf.currentHeight
        val width = leaf.width
        
        // Forme de feuille d'iris : longue, effilée, avec une nervure centrale
        paint.color = Color.rgb(60, 130, 60)
        paint.style = Paint.Style.FILL
        
        // Créer la forme de la feuille avec des points
        val leafPath = android.graphics.Path()
        leafPath.moveTo(0f, 0f) // Base
        leafPath.lineTo(-width/2, -height * 0.3f) // Côté gauche bas
        leafPath.lineTo(-width/3, -height * 0.7f) // Côté gauche milieu
        leafPath.lineTo(0f, -height) // Pointe
        leafPath.lineTo(width/3, -height * 0.7f) // Côté droit milieu
        leafPath.lineTo(width/2, -height * 0.3f) // Côté droit bas
        leafPath.close()
        
        canvas.drawPath(leafPath, paint)
        
        // Nervure centrale
        paint.color = Color.rgb(40, 100, 40)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2f
        canvas.drawLine(0f, 0f, 0f, -height, paint)
        
        canvas.restore()
    }
    
    private fun drawFlowers(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.FILL
        
        for (stem in stems) {
            val flower = stem.flower ?: continue
            if (flower.currentSize <= 0) continue
            
            drawIrisFlower(canvas, paint, flower)
        }
    }
    
    private fun drawIrisFlower(canvas: Canvas, paint: Paint, flower: IrisFlower) {
        val size = flower.currentSize
        val colorRgb = flower.color.rgb
        val darkColorRgb = flower.color.darkRgb
        
        canvas.save()
        canvas.translate(flower.x, flower.y)
        
        // Dessiner les 6 pétales de l'iris (3 pétales dressés + 3 sépales retombants)
        for (i in 0 until flower.petalCount) {
            val angle = (i * 60f) + (sin(flower.petalPhase) * 5f) // Légère animation
            val isSepal = i % 2 == 0 // Alterner entre pétales et sépales
            
            canvas.save()
            canvas.rotate(angle)
            
            if (isSepal) {
                // Sépales (retombants) - plus foncés
                paint.color = Color.rgb(darkColorRgb[0], darkColorRgb[1], darkColorRgb[2])
                drawIrisPetal(canvas, paint, size * 0.8f, size * 0.4f, true) // Plus larges, retombants
            } else {
                // Pétales (dressés) - plus clairs
                paint.color = Color.rgb(colorRgb[0], colorRgb[1], colorRgb[2])
                drawIrisPetal(canvas, paint, size * 0.6f, size * 0.3f, false) // Plus étroits, dressés
            }
            
            canvas.restore()
        }
        
        // Centre de la fleur
        paint.color = Color.rgb(255, 215, 0) // Jaune doré
        canvas.drawCircle(0f, 0f, size * 0.1f, paint)
        
        canvas.restore()
    }
    
    private fun drawIrisPetal(canvas: Canvas, paint: Paint, length: Float, width: Float, isSepal: Boolean) {
        val path = android.graphics.Path()
        
        if (isSepal) {
            // Forme de sépale retombant
            path.moveTo(0f, 0f)
            path.lineTo(-width/2, -length * 0.3f)
            path.lineTo(-width/3, -length * 0.7f)
            path.lineTo(0f, -length)
            path.lineTo(width/3, -length * 0.7f)
            path.lineTo(width/2, -length * 0.3f)
            path.close()
        } else {
            // Forme de pétale dressé
            path.moveTo(0f, 0f)
            path.lineTo(-width/3, -length * 0.4f)
            path.lineTo(-width/4, -length * 0.8f)
            path.lineTo(0f, -length)
            path.lineTo(width/4, -length * 0.8f)
            path.lineTo(width/3, -length * 0.4f)
            path.close()
        }
        
        canvas.drawPath(path, paint)
    }
    
    // ==================== UTILITAIRES ====================
    
    companion object {
        private var stemIdCounter = 0
        private var flowerIdCounter = 0
        private var leafIdCounter = 0
        
        private fun generateStemId(): String {
            stemIdCounter++
            return "irisstem_$stemIdCounter"
        }
        
        private fun generateFlowerId(): String {
            flowerIdCounter++
            return "irisflower_$flowerIdCounter"
        }
        
        private fun generateLeafId(): String {
            leafIdCounter++
            return "irisleaf_$leafIdCounter"
        }
        
        private fun generateRandomGrowthSpeed(): Float {
            // Variation de ±5% pour les iris
            val variation = 0.05f
            return 1.0f + (Math.random().toFloat() - 0.5f) * 2 * variation
        }
    }
}
