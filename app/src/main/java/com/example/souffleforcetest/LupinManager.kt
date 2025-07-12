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
    private val baseThickness = 13.1f       // 25% plus fin (17.5 * 0.75)
    private val tipThickness = 4.2f         // 25% plus fin (5.6 * 0.75)
    private val growthRate = 7200f          // 3X plus rapide (2400 * 3)
    private val maxBranches = 21            // 21 tiges max (7 groupes de 3)
    
    private val baseLeafSize = 125f         // 25% plus grand (100 * 1.25)
    private val baseFlowerSize = 40f        // 2X plus gros (20 * 2)
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
        // Créer un pool des 7 groupes de tiges possibles (0=principal, 1-6=groupes)
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
            // Prendre le groupe suivant dans l'ordre aléatoire
            val groupTypeToActivate = stemOrderPool[saccadeCount - 1]
            currentActiveStemIndex = saccadeCount - 1
            
            if (groupTypeToActivate == 0) {
                // Groupe principal (déjà créé)
                println("Saccade $saccadeCount: Groupe PRINCIPAL activé")
            } else {
                // Créer un nouveau groupe de 3 tiges n'importe où
                println("Saccade $saccadeCount: Nouveau groupe $groupTypeToActivate créé")
                createNewStemGroup(groupTypeToActivate)
            }
        }
    }
    
    private fun createMainStem() {
        // Créer le premier groupe de 3 tiges au centre, ÉNORMÉMENT espacées
        val radius = 160f  // 2X plus grand (80f * 2)
        
        for (i in 0..2) {
            // Position complètement aléatoire dans un cercle énorme, très espacées
            val angle = Math.random() * 2 * PI
            val distance = Math.random() * radius + 80f  // Distance minimale 2X plus grande (40f * 2)
            val stemX = baseX + (cos(angle) * distance).toFloat()
            val stemY = baseY + (Math.random().toFloat() - 0.5f) * 40f  // Plus de variation Y
            
            val stem = LupinStem(
                maxHeight = screenHeight * maxStemHeight * (0.7f + Math.random().toFloat() * 0.6f),
                baseX = stemX,
                baseY = stemY,
                growthSpeedMultiplier = 0.5f + Math.random().toFloat() * 1.0f
            )
            stem.points.add(StemPoint(stemX, stemY, baseThickness))
            stems.add(stem)
            challengeManager?.notifyLupinSpikeCreated("NEW_STEM", stem.id)
        }
    }
    
    private fun createNewStemGroup(groupNumber: Int) {
        // Position de base ÉNORMÉMENT éloignée pour ce groupe
        val baseRadius = 240f + groupNumber * 80f  // 2X plus espacé (120f * 2)
        val groupAngle = Math.random() * 2 * PI
        val groupDistance = Math.random() * baseRadius + 120f  // Distance minimale 2X plus grande
        
        val groupBaseX = baseX + (cos(groupAngle) * groupDistance).toFloat()
        val groupBaseY = baseY + (Math.random().toFloat() - 0.5f) * 60f  // Plus de variation Y
        
        // 3 tiges dans ce groupe, positions ÉNORMÉMENT espacées entre elles
        for (i in 0..2) {
            val localRadius = 160f + Math.random().toFloat() * 120f  // 2X plus espacé localement (80f * 2)
            val localAngle = Math.random() * 2 * PI
            val localDistance = Math.random() * localRadius + 60f  // Distance minimale 2X plus grande (30f * 2)
            
            val stemX = groupBaseX + (cos(localAngle) * localDistance).toFloat()
            val stemY = groupBaseY + (Math.random().toFloat() - 0.5f) * 50f
            
            val stem = LupinStem(
                maxHeight = screenHeight * maxStemHeight * (0.6f + Math.random().toFloat() * 0.8f),
                baseX = stemX,
                baseY = stemY,
                growthSpeedMultiplier = 0.4f + Math.random().toFloat() * 1.2f
            )
            stem.points.add(StemPoint(stemX, stemY, baseThickness))
            stems.add(stem)
            challengeManager?.notifyLupinSpikeCreated("NEW_STEM", stem.id)
        }
    }
    
    private fun growOnlyActiveStem(force: Float) {
        if (currentActiveStemIndex < 0) return
        
        // Faire pousser toutes les tiges du groupe actif
        val stemsInCurrentGroup = if (currentActiveStemIndex == 0) {
            // Groupe principal : les 3 premières tiges
            stems.take(3)
        } else {
            // Autres groupes : 3 tiges par groupe, en commençant après le groupe principal
            val startIndex = 3 + (currentActiveStemIndex - 1) * 3
            stems.drop(startIndex).take(3)
        }
        
        for (activeStem in stemsInCurrentGroup) {
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
        val angleSpread = 80f  // Plus étalé comme sur la photo
        
        for (i in 0 until folioleCount) {
            val folioleAngle = (i - folioleCount / 2f) * (angleSpread / folioleCount) + leaf.folioleAngles[i]
            val folioleLength = size * (0.9f + (i % 3) * 0.15f)
            val folioleWidth = folioleLength * 0.25f  // Plus étroit comme sur la photo
            
            canvas.save()
            canvas.rotate(folioleAngle)
            
            // Foliole en forme de lancette (plus pointue)
            paint.color = Color.rgb(50, 150, 50)  // Vert plus vif
            paint.style = Paint.Style.FILL
            
            // Forme lancéolée réaliste
            canvas.drawOval(
                -folioleWidth/2, folioleLength * 0.1f,
                folioleWidth/2, folioleLength * 0.95f,
                paint
            )
            
            // Pointe effilée en haut
            paint.color = Color.rgb(45, 140, 45)
            canvas.drawOval(
                -folioleWidth/4, 0f,
                folioleWidth/4, folioleLength * 0.2f,
                paint
            )
            
            // Contour sombre pour bien définir chaque foliole
            paint.color = Color.rgb(25, 100, 25)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2.5f
            canvas.drawOval(
                -folioleWidth/2, folioleLength * 0.1f,
                folioleWidth/2, folioleLength * 0.95f,
                paint
            )
            
            // Nervure centrale très marquée
            paint.color = Color.rgb(20, 90, 20)
            paint.strokeWidth = 3f
            canvas.drawLine(0f, folioleLength * 0.15f, 0f, folioleLength * 0.85f, paint)
            
            // Nervures secondaires bien définies
            paint.strokeWidth = 1.5f
            paint.color = Color.rgb(30, 110, 30)
            for (j in 1..4) {
                val nervureY = folioleLength * (0.25f + j * 0.15f)
                val nervureWidth = folioleWidth * (0.35f - j * 0.06f)
                canvas.drawLine(-nervureWidth/2, nervureY, 0f, nervureY * 0.95f, paint)
                canvas.drawLine(nervureWidth/2, nervureY, 0f, nervureY * 0.95f, paint)
            }
            
            paint.style = Paint.Style.FILL
            canvas.restore()
        }
        
        // Pétiole bien marqué
        paint.color = Color.rgb(35, 110, 35)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
        canvas.drawLine(0f, 0f, 0f, -size * 0.25f, paint)
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
                    
                    // Fleur conique TRÈS définie - chaque pétale visible
                    
                    // Pétale central (étendard) - forme conique
                    paint.color = Color.rgb(colorRgb[0], colorRgb[1], colorRgb[2])
                    canvas.drawOval(
                        flower.x - size * 0.4f, flower.y - size * 0.6f,
                        flower.x + size * 0.4f, flower.y + size * 0.1f, 
                        paint
                    )
                    
                    // Contour sombre pour définir le pétale central
                    paint.color = Color.rgb(
                        (colorRgb[0] * 0.6f).toInt(),
                        (colorRgb[1] * 0.6f).toInt(),
                        (colorRgb[2] * 0.6f).toInt()
                    )
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 2f
                    canvas.drawOval(
                        flower.x - size * 0.4f, flower.y - size * 0.6f,
                        flower.x + size * 0.4f, flower.y + size * 0.1f, 
                        paint
                    )
                    paint.style = Paint.Style.FILL
                    
                    // Pétales latéraux (ailes) - bien définis
                    paint.color = Color.rgb(
                        (colorRgb[0] * 0.85f).toInt(),
                        (colorRgb[1] * 0.85f).toInt(),
                        (colorRgb[2] * 0.85f).toInt()
                    )
                    
                    // Aile gauche
                    canvas.drawOval(
                        flower.x - size * 0.7f, flower.y - size * 0.2f,
                        flower.x - size * 0.1f, flower.y + size * 0.3f,
                        paint
                    )
                    // Contour aile gauche
                    paint.color = Color.rgb(
                        (colorRgb[0] * 0.5f).toInt(),
                        (colorRgb[1] * 0.5f).toInt(),
                        (colorRgb[2] * 0.5f).toInt()
                    )
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 1.5f
                    canvas.drawOval(
                        flower.x - size * 0.7f, flower.y - size * 0.2f,
                        flower.x - size * 0.1f, flower.y + size * 0.3f,
                        paint
                    )
                    paint.style = Paint.Style.FILL
                    
                    // Aile droite
                    paint.color = Color.rgb(
                        (colorRgb[0] * 0.85f).toInt(),
                        (colorRgb[1] * 0.85f).toInt(),
                        (colorRgb[2] * 0.85f).toInt()
                    )
                    canvas.drawOval(
                        flower.x + size * 0.1f, flower.y - size * 0.2f,
                        flower.x + size * 0.7f, flower.y + size * 0.3f,
                        paint
                    )
                    // Contour aile droite
                    paint.color = Color.rgb(
                        (colorRgb[0] * 0.5f).toInt(),
                        (colorRgb[1] * 0.5f).toInt(),
                        (colorRgb[2] * 0.5f).toInt()
                    )
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 1.5f
                    canvas.drawOval(
                        flower.x + size * 0.1f, flower.y - size * 0.2f,
                        flower.x + size * 0.7f, flower.y + size * 0.3f,
                        paint
                    )
                    paint.style = Paint.Style.FILL
                    
                    // Carène (pétale inférieur) - forme conique pointue
                    paint.color = Color.rgb(
                        (colorRgb[0] * 0.75f).toInt(),
                        (colorRgb[1] * 0.75f).toInt(),
                        (colorRgb[2] * 0.75f).toInt()
                    )
                    canvas.drawOval(
                        flower.x - size * 0.25f, flower.y + size * 0.1f,
                        flower.x + size * 0.25f, flower.y + size * 0.5f,
                        paint
                    )
                    // Contour carène
                    paint.color = Color.rgb(
                        (colorRgb[0] * 0.4f).toInt(),
                        (colorRgb[1] * 0.4f).toInt(),
                        (colorRgb[2] * 0.4f).toInt()
                    )
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 1.5f
                    canvas.drawOval(
                        flower.x - size * 0.25f, flower.y + size * 0.1f,
                        flower.x + size * 0.25f, flower.y + size * 0.5f,
                        paint
                    )
                    paint.style = Paint.Style.FILL
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
