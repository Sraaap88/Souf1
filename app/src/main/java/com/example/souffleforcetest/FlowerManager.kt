package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import kotlin.math.*

class FlowerManager(private val plantStem: PlantStem) {
    
    // ==================== DATA CLASSES ====================
    
    data class Flower(
        val x: Float,
        val y: Float,
        val stemIndex: Int,          // -1 pour tige principale, 0+ pour branches
        var currentSize: Float = 0f,
        val maxSize: Float,
        val perspective: FlowerPerspective,
        var centerSize: Float = 0f,
        val petals: MutableList<Petal> = mutableListOf(),
        var isFullyGrown: Boolean = false,
        var isBud: Boolean = false,              // NOUVEAU : Est-ce un bouton ?
        var budStage: BudStage = BudStage.TIGHT, // NOUVEAU : Stade du bouton
        var budSize: Float = 0f                  // NOUVEAU : Taille du bouton
    )
    
    data class Petal(
        val angle: Float,            // Angle autour du centre (0-360°)
        val length: Float,           // Longueur du pétale
        val width: Float,            // Largeur du pétale
        var currentLength: Float = 0f,
        val perspective: PetalPerspective
    )
    
    data class FlowerPerspective(
        val viewAngle: Float,        // Angle de vue (0° = face, 45° = angle, 90° = profil)
        val tiltAngle: Float,        // Inclinaison (haut/bas)
        val rotationAngle: Float,    // Rotation autour de l'axe
        val viewType: FlowerViewType // NOUVEAU : Type de vue
    )
    
    // NOUVEAU : Types de vue pour les marguerites
    enum class FlowerViewType {
        TOP_VIEW,    // Vue de dessus (comme actuellement)
        SIDE_VIEW,   // Vue de côté (profil complet)
        ANGLE_VIEW   // Vue en angle (3/4)
    }
    
    data class PetalPerspective(
        val depthFactor: Float,      // Facteur de profondeur (0-1)
        val visibilityFactor: Float, // Visibilité selon l'angle (0-1)
        val widthFactor: Float       // Facteur de largeur selon perspective
    )
    
    // NOUVEAU : Stades de développement des boutons
    enum class BudStage {
        TIGHT,      // Bouton très fermé (début)
        SWELLING,   // Bouton qui gonfle
        OPENING,    // Bouton qui commence à s'ouvrir (pétales visibles)
        STUCK       // Bouton bloqué (ne s'ouvrira plus)
    }
    
    // ==================== VARIABLES PRINCIPALES ====================
    
    val flowers = mutableListOf<Flower>()
    private var lastForce = 0f
    private val forceHistory = mutableListOf<Float>() // NOUVEAU : Historique pour stabilité
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.25f
    private val baseFlowerSize = 234f  // +30% (180f × 1.3)
    private val maxFlowerSize = 468f   // +30% (360f × 1.3)
    private val growthRate = 400f
    private val petalCount = 18 + (Math.random() * 8).toInt() // 18-26 pétales
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun processFlowerGrowth(force: Float) {
        // Mettre à jour l'historique des forces pour analyser la stabilité
        updateForceHistory(force)
        
        // Créer des fleurs sur chaque tige qui n'en a pas encore (au début de la phase)
        if (flowers.isEmpty()) {
            createFlowersOnStems(force)
        }
        
        // Faire grandir les fleurs et boutons existants
        growExistingFlowersAndBuds(force)
        
        lastForce = force
    }
    
    fun resetFlowers() {
        flowers.clear()
        lastForce = 0f
        forceHistory.clear() // NOUVEAU : Reset de l'historique
    }
    
    fun drawFlowers(canvas: Canvas, flowerPaint: Paint, centerPaint: Paint) {
        // Dessiner d'abord les boutons, puis les fleurs
        for (flower in flowers) {
            if (flower.isBud && flower.budSize > 0) {
                drawFlowerBud(canvas, flower, flowerPaint, centerPaint)
            } else if (!flower.isBud && flower.currentSize > 0) {
                drawSingleFlower(canvas, flower, flowerPaint, centerPaint)
            }
        }
    }
    
    fun drawSpecificFlowers(canvas: Canvas, specificFlowers: List<Flower>, flowerPaint: Paint, centerPaint: Paint) {
        for (flower in specificFlowers) {
            if (flower.isBud && flower.budSize > 0) {
                drawFlowerBud(canvas, flower, flowerPaint, centerPaint)
            } else if (!flower.isBud && flower.currentSize > 0) {
                drawSingleFlower(canvas, flower, flowerPaint, centerPaint)
            }
        }
    }
    
    // ==================== NOUVEAU SYSTÈME DE BOUTONS ====================
    
    private fun updateForceHistory(force: Float) {
        forceHistory.add(force)
        if (forceHistory.size > 20) { // Garder 20 valeurs pour analyse
            forceHistory.removeAt(0)
        }
    }
    
    private fun calculateForceStability(): Float {
        if (forceHistory.size < 10) return 1f
        
        val recent = forceHistory.takeLast(10)
        val avgForce = recent.average().toFloat()
        val maxVariation = recent.maxOf { abs(it - avgForce) }
        
        return (1f - (maxVariation / 0.5f)).coerceIn(0f, 1f)
    }
    
    private fun shouldCreateBud(stemIndex: Int, force: Float, forceStability: Float): Boolean {
        // 1. Probabilité de base (20% pour le réalisme)
        val baseBudChance = 0.2f
        
        // 2. Force insuffisante augmente les chances
        val forceEffect = if (force < 0.4f) {
            (0.4f - force) * 1.5f // +60% de chance si force = 0
        } else 0f
        
        // 3. Instabilité augmente les chances
        val stabilityEffect = if (forceStability < 0.7f) {
            (0.7f - forceStability) * 0.8f // +24% si très instable
        } else 0f
        
        // 4. Tiges secondaires tardives ont plus de boutons
        val stemEffect = when (stemIndex) {
            -1 -> 0f        // Tige principale : pas d'effet
            0, 1 -> 0.1f    // Premières branches : +10%
            2, 3 -> 0.2f    // Branches moyennes : +20%
            else -> 0.3f    // Dernières branches : +30%
        }
        
        val totalBudChance = (baseBudChance + forceEffect + stabilityEffect + stemEffect).coerceIn(0f, 0.8f)
        return Math.random() < totalBudChance
    }
    
    // ==================== FONCTIONS PRIVÉES ====================
    
    private fun createFlowersOnStems(force: Float) {
        val forceStability = calculateForceStability()
        
        // Créer fleur/bouton sur tige principale si elle a une taille suffisante
        if (!flowers.any { it.stemIndex == -1 } && plantStem.mainStem.size > 5) {
            createFlowerOrBudOnMainStem(force, forceStability)
        }
        
        // Créer fleurs/boutons sur chaque branche qui a une taille suffisante
        for (branchIndex in plantStem.branches.indices) {
            val branch = plantStem.branches[branchIndex]
            if (!flowers.any { it.stemIndex == branchIndex } && branch.points.size > 3) {
                createFlowerOrBudOnBranch(branchIndex, force, forceStability)
            }
        }
    }
    
    private fun createFlowerOrBudOnMainStem(force: Float, forceStability: Float) {
        val mainStem = plantStem.mainStem
        if (mainStem.size < 5) return
        
        val topPoint = mainStem.last()
        val size = baseFlowerSize + (Math.random() * (maxFlowerSize - baseFlowerSize)).toFloat()
        
        // GARANTIR au moins une vue de dessus sur la tige principale
        val viewType = FlowerViewType.TOP_VIEW
        
        val perspective = FlowerPerspective(
            viewAngle = 0f + (Math.random() * 10f - 5f).toFloat(),
            tiltAngle = (Math.random() * 15f - 7.5f).toFloat(),
            rotationAngle = (Math.random() * 360f).toFloat(),
            viewType = viewType
        )
        
        val flower = Flower(
            x = topPoint.x,
            y = topPoint.y - 20f,
            stemIndex = -1,
            maxSize = size,
            perspective = perspective,
            isBud = shouldCreateBud(-1, force, forceStability)
        )
        
        if (!flower.isBud) {
            createPetalsForFlower(flower)
        }
        
        flowers.add(flower)
    }
    
    private fun createFlowerOrBudOnBranch(branchIndex: Int, force: Float, forceStability: Float) {
        val branch = plantStem.branches[branchIndex]
        if (branch.points.size < 3) return
        
        val topPoint = branch.points.last()
        val size = (baseFlowerSize * 0.8f) + (Math.random() * (maxFlowerSize * 0.8f - baseFlowerSize * 0.8f)).toFloat()
        
        // NOUVEAU : Répartition des vues sur les branches
        val viewType = when (Math.random()) {
            in 0.0..0.4 -> FlowerViewType.SIDE_VIEW    // 40% vue de côté
            in 0.4..0.7 -> FlowerViewType.ANGLE_VIEW   // 30% vue en angle  
            else -> FlowerViewType.TOP_VIEW            // 30% vue de dessus
        }
        
        val branchAngle = branch.angle
        val perspective = when (viewType) {
            FlowerViewType.TOP_VIEW -> FlowerPerspective(
                viewAngle = 0f + (Math.random() * 15f - 7.5f).toFloat(),
                tiltAngle = (Math.random() * 20f - 10f).toFloat(),
                rotationAngle = (Math.random() * 360f).toFloat(),
                viewType = viewType
            )
            FlowerViewType.ANGLE_VIEW -> FlowerPerspective(
                viewAngle = 45f + (Math.random() * 20f - 10f).toFloat(),
                tiltAngle = branchAngle * 0.5f + (Math.random() * 25f - 12.5f).toFloat(),
                rotationAngle = (Math.random() * 360f).toFloat(),
                viewType = viewType
            )
            FlowerViewType.SIDE_VIEW -> FlowerPerspective(
                viewAngle = 85f + (Math.random() * 10f - 5f).toFloat(),
                tiltAngle = branchAngle * 0.8f + (Math.random() * 30f - 15f).toFloat(),
                rotationAngle = (Math.random() * 360f).toFloat(),
                viewType = viewType
            )
        }
        
        val flower = Flower(
            x = topPoint.x,
            y = topPoint.y - 15f,
            stemIndex = branchIndex,
            maxSize = size,
            perspective = perspective,
            isBud = shouldCreateBud(branchIndex, force, forceStability)
        )
        
        if (!flower.isBud) {
            createPetalsForFlower(flower)
        }
        
        flowers.add(flower)
    }
    
    private fun createPetalsForFlower(flower: Flower) {
        val basePetalCount = 18 + (Math.random() * 8).toInt()
        val petalCount = (basePetalCount * 1.2f).toInt()
        
        for (i in 0 until petalCount) {
            val angle = (i * 360f / petalCount) + (Math.random() * 10f - 5f).toFloat()
            val baseLength = flower.maxSize * 0.4f
            val length = baseLength + (Math.random() * baseLength * 0.3f).toFloat()
            val baseWidth = length * 0.25f + (Math.random() * length * 0.1f).toFloat()
            val width = baseWidth * 1.2f
            
            val petalPerspective = calculatePetalPerspective(angle, flower.perspective)
            
            val petal = Petal(
                angle = angle,
                length = length,
                width = width,
                perspective = petalPerspective
            )
            
            flower.petals.add(petal)
        }
    }
    
    private fun calculatePetalPerspective(petalAngle: Float, flowerPerspective: FlowerPerspective): PetalPerspective {
        val adjustedAngle = petalAngle + flowerPerspective.rotationAngle
        val radians = Math.toRadians(adjustedAngle.toDouble())
        
        val depthFactor = cos(radians).toFloat() * sin(Math.toRadians(flowerPerspective.viewAngle.toDouble())).toFloat()
        
        val visibilityFactor = if (depthFactor < 0) {
            0.3f + (depthFactor + 1f) * 0.7f
        } else {
            1f
        }
        
        val widthFactor = 1f - abs(sin(radians).toFloat()) * sin(Math.toRadians(flowerPerspective.viewAngle.toDouble())).toFloat() * 0.6f
        
        return PetalPerspective(
            depthFactor = depthFactor,
            visibilityFactor = visibilityFactor.coerceIn(0.2f, 1f),
            widthFactor = widthFactor.coerceIn(0.3f, 1f)
        )
    }
    
    private fun growExistingFlowersAndBuds(force: Float) {
        val forceStability = calculateForceStability()
        
        for (flower in flowers) {
            if (flower.isBud) {
                growBud(flower, force, forceStability)
            } else {
                growNormalFlower(flower, force, forceStability)
            }
        }
    }
    
    private fun growBud(flower: Flower, force: Float, forceStability: Float) {
        val targetBudSize = flower.maxSize * 0.3f // Les boutons font 30% de la taille d'une fleur
        
        if (flower.budSize < targetBudSize) {
            val budGrowthRate = 200f // Plus lent que les fleurs normales
            val qualityMultiplier = 0.5f + forceStability * 0.5f
            val adjustedGrowth = force * qualityMultiplier * budGrowthRate * 0.008f
            flower.budSize = (flower.budSize + adjustedGrowth).coerceAtMost(targetBudSize)
            
            // Évolution des stades selon la taille
            flower.budStage = when {
                flower.budSize < targetBudSize * 0.3f -> BudStage.TIGHT
                flower.budSize < targetBudSize * 0.7f -> BudStage.SWELLING  
                flower.budSize < targetBudSize * 0.9f -> BudStage.OPENING
                else -> BudStage.STUCK
            }
            
            // RARE : Un bouton peut parfois éclore en vraie fleur avec un souffle parfait
            if (flower.budStage == BudStage.OPENING && 
                force > 0.6f && 
                forceStability > 0.9f && 
                Math.random() < 0.15f) { // 15% de chance
                
                flower.isBud = false
                flower.currentSize = flower.budSize * 2f // Explosion de croissance
                createPetalsForFlower(flower) // Créer les pétales
            }
        }
    }
    
    private fun growNormalFlower(flower: Flower, force: Float, forceStability: Float) {
        if (flower.currentSize < flower.maxSize && force > forceThreshold) {
            val qualityMultiplier = 0.5f + forceStability * 0.5f
            
            val growthProgress = flower.currentSize / flower.maxSize
            val progressCurve = 1f - growthProgress * growthProgress
            val adjustedGrowth = force * qualityMultiplier * progressCurve * growthRate * 0.008f
            
            flower.currentSize = (flower.currentSize + adjustedGrowth).coerceAtMost(flower.maxSize)
            
            // CENTRE GRANDIT PROPORTIONNELLEMENT : 67.5% de la taille actuelle
            val targetCenterSize = flower.currentSize * 0.675f
            flower.centerSize = targetCenterSize
            
            // Faire grandir les pétales
            for (petal in flower.petals) {
                if (petal.currentLength < petal.length) {
                    petal.currentLength = (petal.currentLength + adjustedGrowth * 0.8f).coerceAtMost(petal.length)
                }
            }
            
            // Marquer comme complètement développée
            if (flower.currentSize >= flower.maxSize * 0.95f) {
                flower.isFullyGrown = true
            }
        }
    }
    
    // ==================== FONCTIONS DE RENDU ====================
    
    private fun drawSingleFlower(canvas: Canvas, flower: Flower, flowerPaint: Paint, centerPaint: Paint) {
        when (flower.perspective.viewType) {
            FlowerViewType.TOP_VIEW -> drawTopViewFlower(canvas, flower, flowerPaint, centerPaint)
            FlowerViewType.ANGLE_VIEW -> drawAngleViewFlower(canvas, flower, flowerPaint, centerPaint)
            FlowerViewType.SIDE_VIEW -> drawSideViewFlower(canvas, flower, flowerPaint, centerPaint)
        }
    }
    
    // Vue de dessus (fleur actuelle - gardée intacte)
    private fun drawTopViewFlower(canvas: Canvas, flower: Flower, flowerPaint: Paint, centerPaint: Paint) {
        val currentX = flower.x
        val currentY = flower.y
        
        // Dessiner les pétales (d'abord les arrière, puis les avant)
        val sortedPetals = flower.petals.sortedBy { it.perspective.depthFactor }
        
        for (petal in sortedPetals) {
            if (petal.currentLength > 0) {
                drawPetal(canvas, currentX, currentY, petal, flower, flowerPaint)
            }
        }
        
        // Dessiner le centre par-dessus
        if (flower.centerSize > 0) {
            drawFlowerCenter(canvas, currentX, currentY, flower, centerPaint)
        }
    }
    
    // NOUVEAU : Vue en angle (3/4)
    private fun drawAngleViewFlower(canvas: Canvas, flower: Flower, flowerPaint: Paint, centerPaint: Paint) {
        val currentX = flower.x
        val currentY = flower.y
        val size = flower.currentSize
        
        if (size <= 0) return
        
        // Centre visible mais écrasé en perspective
        val centerRadius = flower.centerSize * 0.3f
        centerPaint.color = Color.rgb(255, 200, 50)
        
        canvas.save()
        canvas.translate(currentX, currentY)
        canvas.scale(1f, 0.6f) // Perspective écrasée
        canvas.drawCircle(0f, -centerRadius * 0.3f, centerRadius, centerPaint)
        canvas.restore()
        
        // Pétales visibles (environ 60% des pétales)
        val visiblePetalCount = (flower.petals.size * 0.6f).toInt()
        flowerPaint.color = Color.WHITE
        flowerPaint.style = Paint.Style.STROKE
        flowerPaint.strokeCap = Paint.Cap.ROUND
        
        for (i in 0 until visiblePetalCount) {
            val petal = flower.petals[i]
            val angle = petal.angle + flower.perspective.rotationAngle
            val length = petal.currentLength * 0.8f // Légèrement raccourcis
            val width = petal.width * 0.7f
            
            val rad = Math.toRadians(angle.toDouble())
            val startX = currentX + cos(rad).toFloat() * centerRadius
            val startY = currentY + sin(rad).toFloat() * centerRadius * 0.6f - centerRadius * 0.3f
            val endX = currentX + cos(rad).toFloat() * length
            val endY = currentY + sin(rad).toFloat() * length * 0.6f - centerRadius * 0.3f
            
            flowerPaint.strokeWidth = width
            canvas.drawLine(startX, startY, endX, endY, flowerPaint)
        }
    }
    
    // NOUVEAU : Vue de côté (profil)
    private fun drawSideViewFlower(canvas: Canvas, flower: Flower, flowerPaint: Paint, centerPaint: Paint) {
        val currentX = flower.x
        val currentY = flower.y
        val size = flower.currentSize
        
        if (size <= 0) return
        
        // Centre de profil (petit trait vertical jaune)
        val centerHeight = flower.centerSize * 0.6f
        centerPaint.color = Color.rgb(255, 200, 50)
        centerPaint.style = Paint.Style.STROKE
        centerPaint.strokeWidth = centerHeight * 0.8f
        centerPaint.strokeCap = Paint.Cap.ROUND
        canvas.drawLine(currentX, currentY - centerHeight * 0.3f, currentX, currentY + centerHeight * 0.3f, centerPaint)
        
        // Pétales de profil (forme d'ellipse aplatie)
        flowerPaint.color = Color.WHITE
        flowerPaint.style = Paint.Style.STROKE
        flowerPaint.strokeCap = Paint.Cap.ROUND
        
        // Quelques pétales visibles en profil
        val visiblePetals = listOf(-45f, -25f, -5f, 5f, 25f, 45f) // Angles de pétales visibles
        
        for (angle in visiblePetals) {
            val length = size * 0.35f + (Math.random() * size * 0.1f).toFloat()
            val width = size * 0.04f + (Math.random() * size * 0.02f).toFloat()
            
            val rad = Math.toRadians(angle.toDouble())
            val startX = currentX
            val startY = currentY
            val endX = currentX + cos(rad).toFloat() * length * 0.3f // Très aplati
            val endY = currentY + sin(rad).toFloat() * length
            
            flowerPaint.strokeWidth = width
            canvas.drawLine(startX, startY, endX, endY, flowerPaint)
        }
        
        // Contour externe de profil (forme d'ellipse)
        flowerPaint.style = Paint.Style.STROKE
        flowerPaint.strokeWidth = 2f
        flowerPaint.color = Color.rgb(240, 240, 240)
        
        val ellipseWidth = size * 0.3f
        val ellipseHeight = size * 0.7f
        
        canvas.save()
        canvas.translate(currentX, currentY)
        canvas.scale(1f, 1f)
        
        // Dessiner ellipse comme contour
        val path = android.graphics.Path()
        path.addOval(-ellipseWidth/2, -ellipseHeight/2, ellipseWidth/2, ellipseHeight/2, android.graphics.Path.Direction.CW)
        canvas.drawPath(path, flowerPaint)
        
        canvas.restore()
    }
    
    private fun drawPetal(canvas: Canvas, centerX: Float, centerY: Float, petal: Petal, flower: Flower, paint: Paint) {
        val perspective = petal.perspective
        val flowerPerspective = flower.perspective
        
        val adjustedAngle = petal.angle + flowerPerspective.rotationAngle
        val radians = Math.toRadians(adjustedAngle.toDouble())
        
        val baseDistance = petal.currentLength * 0.3f
        val tipDistance = petal.currentLength
        
        val cos = cos(radians).toFloat()
        val sin = sin(radians).toFloat()
        
        val perspectiveFactor = cos(Math.toRadians(flowerPerspective.viewAngle.toDouble())).toFloat()
        val tiltFactor = sin(Math.toRadians(flowerPerspective.tiltAngle.toDouble())).toFloat()
        
        val baseX = centerX + cos * baseDistance * perspectiveFactor
        val baseY = centerY + sin * baseDistance + cos * baseDistance * tiltFactor * 0.3f
        
        val tipX = centerX + cos * tipDistance * perspectiveFactor
        val tipY = centerY + sin * tipDistance + cos * tipDistance * tiltFactor * 0.3f
        
        val width = petal.width * perspective.widthFactor * (flower.currentSize / flower.maxSize)
        
        val alpha = (255 * perspective.visibilityFactor).toInt()
        paint.color = Color.argb(alpha, 255, 255, 255)
        paint.strokeWidth = width
        paint.strokeCap = Paint.Cap.ROUND
        
        canvas.drawLine(baseX, baseY, tipX, tipY, paint)
    }
    
    private fun drawFlowerCenter(canvas: Canvas, centerX: Float, centerY: Float, flower: Flower, paint: Paint) {
        val centerRadius = flower.centerSize * 0.4f
        val perspective = flower.perspective
        
        val perspectiveFactor = cos(Math.toRadians(perspective.viewAngle.toDouble())).toFloat()
        val radiusX = centerRadius * perspectiveFactor
        val radiusY = centerRadius
        
        paint.color = Color.rgb(255, 200, 50)
        
        canvas.save()
        canvas.translate(centerX, centerY)
        canvas.scale(1f, perspectiveFactor)
        canvas.drawCircle(0f, 0f, radiusY, paint)
        canvas.restore()
        
        paint.color = Color.rgb(200, 150, 30)
        val pointCount = (centerRadius * 0.5f).toInt()
        
        for (i in 0 until pointCount) {
            val angle = Math.random() * 2 * PI
            val distance = Math.random() * radiusY * 0.8f
            val pointX = centerX + cos(angle).toFloat() * distance.toFloat() * perspectiveFactor
            val pointY = centerY + sin(angle).toFloat() * distance.toFloat()
            
            canvas.drawCircle(pointX, pointY, 1.5f, paint)
        }
    }
    
    // ==================== RENDU DES BOUTONS ====================
    
    private fun drawFlowerBud(canvas: Canvas, flower: Flower, budPaint: Paint, sepalsP: Paint) {
        val x = flower.x
        val y = flower.y
        val size = flower.budSize
        
        if (size <= 0) return
        
        when (flower.budStage) {
            BudStage.TIGHT -> drawTightBud(canvas, x, y, size, budPaint, sepalsP)
            BudStage.SWELLING -> drawSwellingBud(canvas, x, y, size, budPaint, sepalsP)
            BudStage.OPENING -> drawOpeningBud(canvas, x, y, size, budPaint, sepalsP, flower)
            BudStage.STUCK -> drawStuckBud(canvas, x, y, size, budPaint, sepalsP)
        }
    }
    
    private fun drawTightBud(canvas: Canvas, x: Float, y: Float, size: Float, budPaint: Paint, sepalsP: Paint) {
        // Corps du bouton - ovale vert
        budPaint.color = Color.rgb(60, 120, 60)
        budPaint.style = Paint.Style.FILL
        
        val width = size * 0.4f
        val height = size * 0.8f
        
        canvas.save()
        canvas.translate(x, y)
        canvas.scale(1f, 1.2f)
        canvas.drawCircle(0f, 0f, width, budPaint)
        canvas.restore()
        
        // Sépales (petites feuilles vertes à la base)
        sepalsP.color = Color.rgb(40, 100, 40)
        sepalsP.style = Paint.Style.STROKE
        sepalsP.strokeCap = Paint.Cap.ROUND
        
        for (i in 0..4) {
            val angle = i * 72f + Math.random() * 15f - 7.5f
            val sepaleLength = size * 0.25f
            val sepaleWidth = size * 0.08f
            
            val rad = Math.toRadians(angle.toDouble())
            val endX = x + cos(rad).toFloat() * sepaleLength
            val endY = y + sin(rad).toFloat() * sepaleLength
            
            sepalsP.strokeWidth = sepaleWidth
            canvas.drawLine(x, y + height * 0.3f, endX, endY, sepalsP)
        }
    }
    
    private fun drawSwellingBud(canvas: Canvas, x: Float, y: Float, size: Float, budPaint: Paint, sepalsP: Paint) {
        // Corps plus rond et plus gros
        budPaint.color = Color.rgb(70, 130, 70)
        budPaint.style = Paint.Style.FILL
        
        val radius = size * 0.5f
        canvas.drawCircle(x, y, radius, budPaint)
        
        // Sépales qui s'écartent
        sepalsP.color = Color.rgb(50, 110, 50)
        sepalsP.style = Paint.Style.STROKE
        sepalsP.strokeCap = Paint.Cap.ROUND
        
        for (i in 0..4) {
            val angle = i * 72f + 20f
            val sepaleLength = size * 0.35f
            val sepaleWidth = size * 0.1f
            
            val rad = Math.toRadians(angle.toDouble())
            val startDistance = radius * 0.7f
            val startX = x + cos(rad).toFloat() * startDistance
            val startY = y + sin(rad).toFloat() * startDistance
            val endX = x + cos(rad).toFloat() * sepaleLength
            val endY = y + sin(rad).toFloat() * sepaleLength
            
            sepalsP.strokeWidth = sepaleWidth
            canvas.drawLine(startX, startY, endX, endY, sepalsP)
        }
    }
    
    private fun drawOpeningBud(canvas: Canvas, x: Float, y: Float, size: Float, budPaint: Paint, sepalsP: Paint, flower: Flower) {
        // Base verte plus petite
        budPaint.color = Color.rgb(60, 120, 60)
        budPaint.style = Paint.Style.FILL
        val baseRadius = size * 0.3f
        canvas.drawCircle(x, y, baseRadius, budPaint)
        
        // Pétales blancs qui pointent
        budPaint.color = Color.rgb(245, 245, 245)
        budPaint.style = Paint.Style.STROKE
        budPaint.strokeWidth = size * 0.05f
        budPaint.strokeCap = Paint.Cap.ROUND
        
        val petalCount = 8 + (Math.random() * 4).toInt()
        for (i in 0 until petalCount) {
            val angle = i * 360f / petalCount + Math.random() * 10f - 5f
            val petalLength = size * (0.15f + Math.random() * 0.1f).toFloat()
            
            val rad = Math.toRadians(angle.toDouble())
            val startX = x + cos(rad).toFloat() * baseRadius
            val startY = y + sin(rad).toFloat() * baseRadius
            val endX = x + cos(rad).toFloat() * (baseRadius + petalLength)
            val endY = y + sin(rad).toFloat() * (baseRadius + petalLength)
            
            canvas.drawLine(startX, startY, endX, endY, budPaint)
        }
        
        // Sépales recourbés vers l'arrière
        sepalsP.color = Color.rgb(45, 95, 45)
        sepalsP.style = Paint.Style.STROKE
        sepalsP.strokeCap = Paint.Cap.ROUND
        
        for (i in 0..4) {
            val angle = i * 72f + 45f
            val rad = Math.toRadians(angle.toDouble())
            val sepaleLength = size * 0.4f
            val sepaleWidth = size * 0.08f
            
            val controlX = x + cos(rad).toFloat() * sepaleLength * 0.5f
            val controlY = y + sin(rad).toFloat() * sepaleLength * 0.5f + size * 0.2f
            val endX = x + cos(rad).toFloat() * sepaleLength
            val endY = y + sin(rad).toFloat() * sepaleLength + size * 0.3f
            
            sepalsP.strokeWidth = sepaleWidth
            canvas.drawLine(x, y, controlX, controlY, sepalsP)
            canvas.drawLine(controlX, controlY, endX, endY, sepalsP)
        }
    }
    
    private fun drawStuckBud(canvas: Canvas, x: Float, y: Float, size: Float, budPaint: Paint, sepalsP: Paint) {
        // Même forme que OPENING mais couleurs plus ternes
        budPaint.color = Color.rgb(50, 100, 50)
        budPaint.style = Paint.Style.FILL
        val baseRadius = size * 0.35f
        canvas.drawCircle(x, y, baseRadius, budPaint)
        
        // Pétales qui ne s'ouvrent plus - aspect fané
        budPaint.color = Color.rgb(220, 220, 180)
        budPaint.style = Paint.Style.STROKE
        budPaint.strokeWidth = size * 0.04f
        budPaint.strokeCap = Paint.Cap.ROUND
        
        val petalCount = 6
        for (i in 0 until petalCount) {
            val angle = i * 60f + Math.random() * 20f - 10f
            val petalLength = size * (0.1f + Math.random() * 0.08f).toFloat()
            
            val rad = Math.toRadians(angle)
            val startX = x + cos(rad).toFloat() * baseRadius
            val startY = y + sin(rad).toFloat() * baseRadius
            val endX = x + cos(rad).toFloat() * (baseRadius + petalLength)
            val endY = y + sin(rad).toFloat() * (baseRadius + petalLength)
            
            canvas.drawLine(startX, startY, endX, endY, budPaint)
        }
        
        // Sépales fanés
        sepalsP.color = Color.rgb(40, 80, 40)
        sepalsP.style = Paint.Style.STROKE
        sepalsP.strokeWidth = size * 0.06f
        sepalsP.strokeCap = Paint.Cap.ROUND
        
        for (i in 0..4) {
            val angle = i * 72f + 30f
            val rad = Math.toRadians(angle.toDouble())
            val sepaleLength = size * 0.3f
            
            val endX = x + cos(rad).toFloat() * sepaleLength
            val endY = y + sin(rad).toFloat() * sepaleLength + size * 0.15f
            
            canvas.drawLine(x, y, endX, endY, sepalsP)
        }
    }
}
