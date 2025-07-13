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
        val id: String = generateFlowerId()  // ID unique pour chaque fleur
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
        val rotationAngle: Float     // Rotation autour de l'axe
    )
    
    data class PetalPerspective(
        val depthFactor: Float,      // Facteur de profondeur (0-1)
        val visibilityFactor: Float, // Visibilité selon l'angle (0-1)
        val widthFactor: Float       // Facteur de largeur selon perspective
    )
    
    // ==================== VARIABLES PRINCIPALES ====================
    
    val flowers = mutableListOf<Flower>()
    private var lastForce = 0f
    
    // Gestionnaire de boutons
    private val budManager = BudManager(plantStem)
    
    // Référence au gestionnaire de défis (sera injectée)
    private var challengeManager: ChallengeManager? = null
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.25f
    private val baseFlowerSize = 234f  // +30% (180f × 1.3)
    private val maxFlowerSize = 468f   // +30% (360f × 1.3)
    private val growthRate = 400f
    private val petalCount = 18 + (Math.random() * 8).toInt() // 18-26 pétales
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    // Injection du ChallengeManager
    fun setChallengeManager(manager: ChallengeManager) {
        challengeManager = manager
        // NOUVEAU: Injecter aussi dans le BudManager
        budManager.setChallengeManager(manager)
    }
    
    fun processFlowerGrowth(force: Float) {
        // Créer des boutons sur tiges éligibles (5-30% croissance)
        budManager.processBudGrowth(force)
        
        // Créer des fleurs sur tiges suffisamment développées (30%+)
        if (flowers.isEmpty()) {
            createFlowersOnStems()
        }
        
        // Faire grandir les fleurs existantes
        growExistingFlowers(force)
        
        lastForce = force
    }
    
    fun resetFlowers() {
        flowers.clear()
        budManager.resetBuds()
        lastForce = 0f
    }
    
    // NOUVEAU: Version avec dissolution
    fun drawFlowers(canvas: Canvas, flowerPaint: Paint, centerPaint: Paint, dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null) {
        // Dessiner d'abord les boutons avec leurs propres Paint
        val budPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        val budPetalPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        budManager.drawBuds(canvas, budPaint, budPetalPaint)
        
        // Puis dessiner les fleurs avec dissolution
        for (flower in flowers) {
            if (flower.currentSize > 0) {
                drawSingleFlower(canvas, flower, flowerPaint, centerPaint, dissolveInfo)
            }
        }
    }
    
    // NOUVEAU: Version avec dissolution pour fleurs spécifiques
    fun drawSpecificFlowersWithDissolution(canvas: Canvas, specificFlowers: List<Flower>, flowerPaint: Paint, centerPaint: Paint, dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null) {
        // Dessiner les boutons avec leurs propres Paint
        val budPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        val budPetalPaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
        budManager.drawBuds(canvas, budPaint, budPetalPaint)
        
        // Puis dessiner les fleurs spécifiques avec dissolution
        for (flower in specificFlowers) {
            if (flower.currentSize > 0) {
                drawSingleFlower(canvas, flower, flowerPaint, centerPaint, dissolveInfo)
            }
        }
    }
    
    // ANCIEN: Version sans dissolution (rétrocompatibilité)
    fun drawSpecificFlowers(canvas: Canvas, specificFlowers: List<Flower>, flowerPaint: Paint, centerPaint: Paint) {
        drawSpecificFlowersWithDissolution(canvas, specificFlowers, flowerPaint, centerPaint, null)
    }
    
    // ==================== FONCTIONS PRIVÉES ====================
    
    private fun createFlowersOnStems() {
        // Créer fleur sur tige principale SEULEMENT si 30%+ ET assez haute
        if (!flowers.any { it.stemIndex == -1 } && plantStem.mainStem.size > 5) {
            val mainStemHeight = if (plantStem.mainStem.isNotEmpty()) {
                plantStem.getStemBaseY() - plantStem.mainStem.last().y
            } else 0f
            
            if (mainStemHeight >= 80f) { // 30%+ de croissance
                createFlowerOnMainStem()
            }
        }
        
        // Créer fleurs sur branches avec critère 30%+
        for (branchIndex in plantStem.branches.indices) {
            val branch = plantStem.branches[branchIndex]
            
            // Calculer le pourcentage de croissance
            val growthPercentage = if (branch.maxHeight > 0) {
                branch.currentHeight / branch.maxHeight
            } else 0f
            
            val absoluteHeight = branch.currentHeight
            val topPointY = if (branch.points.isNotEmpty()) branch.points.last().y else plantStem.getStemBaseY()
            val distanceFromGround = plantStem.getStemBaseY() - topPointY
            
            println("Branche $branchIndex: ${(growthPercentage * 100).toInt()}%, hauteur: ${absoluteHeight}px, distance sol: ${distanceFromGround}px")
            
            if (!flowers.any { it.stemIndex == branchIndex } && 
                growthPercentage >= 0.30f &&           // MODIFIÉ: 30% minimum au lieu de 15%
                absoluteHeight >= 60f &&               // ET au moins 60px absolus
                distanceFromGround >= 60f) {            // ET au moins 60px du sol
                createFlowerOnBranch(branchIndex)
            }
        }
    }
    
    private fun createFlowerOnMainStem() {
        val mainStem = plantStem.mainStem
        if (mainStem.size < 5) return
        
        val topPoint = mainStem.last()
        val size = baseFlowerSize + (Math.random() * (maxFlowerSize - baseFlowerSize)).toFloat()
        
        val perspective = FlowerPerspective(
            viewAngle = 0f + (Math.random() * 10f - 5f).toFloat(),
            tiltAngle = (Math.random() * 15f - 7.5f).toFloat(),
            rotationAngle = (Math.random() * 360f).toFloat()
        )
        
        val flower = Flower(
            x = topPoint.x,
            y = topPoint.y - 20f,
            stemIndex = -1,
            maxSize = size,
            perspective = perspective
        )
        
        createPetalsForFlower(flower)
        flowers.add(flower)
        
        // Notifier le ChallengeManager qu'une fleur a été créée
        challengeManager?.notifyFlowerCreated(flower.x, flower.y, flower.id)
        println("Fleur créée sur tige principale à (${flower.x}, ${flower.y})")
    }
    
    private fun createFlowerOnBranch(branchIndex: Int) {
        val branch = plantStem.branches[branchIndex]
        if (branch.points.size < 3) return
        
        val topPoint = branch.points.last()
        val size = (baseFlowerSize * 0.8f) + (Math.random() * (maxFlowerSize * 0.8f - baseFlowerSize * 0.8f)).toFloat()
        
        val perspective = FlowerPerspective(
            viewAngle = 0f + (Math.random() * 15f - 7.5f).toFloat(),
            tiltAngle = (Math.random() * 20f - 10f).toFloat(),
            rotationAngle = (Math.random() * 360f).toFloat()
        )
        
        val flower = Flower(
            x = topPoint.x,
            y = topPoint.y - 15f,
            stemIndex = branchIndex,
            maxSize = size,
            perspective = perspective
        )
        
        createPetalsForFlower(flower)
        flowers.add(flower)
        
        // Notifier le ChallengeManager qu'une fleur a été créée
        challengeManager?.notifyFlowerCreated(flower.x, flower.y, flower.id)
        println("Fleur créée sur branche $branchIndex à (${flower.x}, ${flower.y})")
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
    
    private fun growExistingFlowers(force: Float) {
        for (flower in flowers) {
            if (flower.currentSize < flower.maxSize && force > forceThreshold) {
                val forceStability = 1f - abs(force - lastForce).coerceAtMost(0.5f) * 2f
                val qualityMultiplier = 0.5f + forceStability * 0.5f
                
                val growthProgress = flower.currentSize / flower.maxSize
                val progressCurve = 1f - growthProgress * growthProgress
                
                // Ralentissement entre 30-50% de croissance
                val slowdownMultiplier = if (growthProgress >= 0.3f && growthProgress <= 0.5f) {
                    0.4f // 60% plus lent dans cette tranche
                } else {
                    1f // Vitesse normale
                }
                
                val adjustedGrowth = force * qualityMultiplier * progressCurve * growthRate * 0.008f * slowdownMultiplier
                
                flower.currentSize = (flower.currentSize + adjustedGrowth).coerceAtMost(flower.maxSize)
                
                // CENTRE GRANDIT PROPORTIONNELLEMENT : 67.5% de la taille actuelle
                val targetCenterSize = flower.currentSize * 0.675f
                flower.centerSize = targetCenterSize
                
                // Faire grandir les pétales avec le même ralentissement
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
    }
    
    // ==================== FONCTIONS DE RENDU AVEC DISSOLUTION ====================
    
    private fun drawSingleFlower(canvas: Canvas, flower: Flower, flowerPaint: Paint, centerPaint: Paint, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val currentX = flower.x
        val currentY = flower.y
        
        // NOUVEAU: Appliquer les effets de dissolution globaux
        val baseAlpha = if (dissolveInfo != null && dissolveInfo.progress > 0f) {
            ((1f - dissolveInfo.progress) * 255f).toInt().coerceIn(0, 255)
        } else 255
        
        // Dessiner les pétales avec dissolution
        val sortedPetals = flower.petals.sortedBy { it.perspective.depthFactor }
        
        // NOUVEAU: Moins de pétales si dissolution avancée
        val visiblePetals = if (dissolveInfo?.flowersPetalsWilting == true) {
            val keepRatio = 1f - dissolveInfo.progress * 0.7f
            val petalsToKeep = (sortedPetals.size * keepRatio).toInt().coerceAtLeast(5)
            sortedPetals.take(petalsToKeep)
        } else sortedPetals
        
        for ((index, petal) in visiblePetals.withIndex()) {
            if (petal.currentLength > 0) {
                // NOUVEAU: Pétales qui tombent progressivement
                var petalAlpha = baseAlpha
                if (dissolveInfo?.flowersPetalsWilting == true) {
                    // Les derniers pétales (par index) disparaissent en premier
                    val petalWiltChance = dissolveInfo.progress + (index.toFloat() / visiblePetals.size) * 0.4f
                    if (petalWiltChance > 0.6f) {
                        petalAlpha = (petalAlpha * (1f - (petalWiltChance - 0.6f) / 0.4f)).toInt().coerceAtLeast(0)
                    }
                }
                
                drawPetal(canvas, currentX, currentY, petal, flower, flowerPaint, petalAlpha, dissolveInfo)
            }
        }
        
        // Dessiner le centre par-dessus avec dissolution
        if (flower.centerSize > 0) {
            drawFlowerCenter(canvas, currentX, currentY, flower, centerPaint, baseAlpha, dissolveInfo)
        }
    }
    
    private fun drawPetal(canvas: Canvas, centerX: Float, centerY: Float, petal: Petal, flower: Flower, paint: Paint, alpha: Int, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        val perspective = petal.perspective
        val flowerPerspective = flower.perspective
        
        val adjustedAngle = petal.angle + flowerPerspective.rotationAngle
        val radians = Math.toRadians(adjustedAngle.toDouble())
        
        var baseDistance = petal.currentLength * 0.3f
        var tipDistance = petal.currentLength
        
        // NOUVEAU: Pétales qui se rétractent
        if (dissolveInfo?.flowersPetalsWilting == true) {
            val shrinkFactor = 1f - dissolveInfo.progress * 0.6f
            baseDistance *= shrinkFactor
            tipDistance *= shrinkFactor
        }
        
        val cos = cos(radians).toFloat()
        val sin = sin(radians).toFloat()
        
        val perspectiveFactor = cos(Math.toRadians(flowerPerspective.viewAngle.toDouble())).toFloat()
        val tiltFactor = sin(Math.toRadians(flowerPerspective.tiltAngle.toDouble())).toFloat()
        
        val baseX = centerX + cos * baseDistance * perspectiveFactor
        val baseY = centerY + sin * baseDistance + cos * baseDistance * tiltFactor * 0.3f
        
        var tipX = centerX + cos * tipDistance * perspectiveFactor
        var tipY = centerY + sin * tipDistance + cos * tipDistance * tiltFactor * 0.3f
        
        // NOUVEAU: Pétales qui s'affaissent
        if (dissolveInfo?.flowersPetalsWilting == true) {
            val droopFactor = dissolveInfo.progress * 0.8f
            tipY += droopFactor * 20f // Affaissement vers le bas
            tipX += (tipX - centerX) * droopFactor * 0.3f // Léger écartement
        }
        
        var width = petal.width * perspective.widthFactor * (flower.currentSize / flower.maxSize)
        
        // NOUVEAU: Pétales qui s'amincissent
        if (dissolveInfo?.flowersPetalsWilting == true) {
            width *= (1f - dissolveInfo.progress * 0.4f)
        }
        
        // Couleur des pétales (ternit avec dissolution)
        var petalRed = 255
        var petalGreen = 255
        var petalBlue = 255
        
        if (dissolveInfo?.flowersPetalsWilting == true) {
            val wiltFactor = dissolveInfo.progress
            petalRed = (255 * (1f - wiltFactor * 0.2f)).toInt()
            petalGreen = (255 * (1f - wiltFactor * 0.3f)).toInt()
            petalBlue = (255 * (1f - wiltFactor * 0.1f)).toInt()
        }
        
        val finalAlpha = (alpha * perspective.visibilityFactor).toInt()
        paint.color = Color.argb(finalAlpha, petalRed, petalGreen, petalBlue)
        paint.strokeWidth = width
        paint.strokeCap = Paint.Cap.ROUND
        
        canvas.drawLine(baseX, baseY, tipX, tipY, paint)
    }
    
    private fun drawFlowerCenter(canvas: Canvas, centerX: Float, centerY: Float, flower: Flower, paint: Paint, alpha: Int, dissolveInfo: ChallengeEffectsManager.DissolveInfo?) {
        var centerRadius = flower.centerSize * 0.4f
        val perspective = flower.perspective
        
        // NOUVEAU: Centre qui rétrécit
        if (dissolveInfo?.flowersPetalsWilting == true) {
            centerRadius *= (1f - dissolveInfo.progress * 0.5f)
        }
        
        // Appliquer la perspective au centre
        val perspectiveFactor = cos(Math.toRadians(perspective.viewAngle.toDouble())).toFloat()
        val radiusX = centerRadius * perspectiveFactor
        val radiusY = centerRadius
        
        // Couleur du centre (s'assombrit avec dissolution)
        var centerRed = 255
        var centerGreen = 200
        var centerBlue = 50
        
        if (dissolveInfo?.flowersPetalsWilting == true) {
            val wiltFactor = dissolveInfo.progress
            centerRed = (255 * (1f - wiltFactor * 0.3f)).toInt()
            centerGreen = (200 * (1f - wiltFactor * 0.4f)).toInt()
            centerBlue = (50 + (100 * wiltFactor)).toInt() // Vers brun
        }
        
        paint.color = Color.argb(alpha, centerRed, centerGreen, centerBlue)
        
        // Dessiner le centre comme une ellipse selon la perspective
        canvas.save()
        canvas.translate(centerX, centerY)
        canvas.scale(1f, perspectiveFactor)
        canvas.drawCircle(0f, 0f, radiusY, paint)
        canvas.restore()
        
        // Texture granuleuse (s'affaiblit avec dissolution)
        if (dissolveInfo == null || dissolveInfo.progress < 0.8f) {
            var textureRed = 200
            var textureGreen = 150
            var textureBlue = 30
            
            if (dissolveInfo?.flowersPetalsWilting == true) {
                val wiltFactor = dissolveInfo.progress
                textureRed = (200 * (1f - wiltFactor * 0.4f)).toInt()
                textureGreen = (150 * (1f - wiltFactor * 0.5f)).toInt()
                textureBlue = (30 + (80 * wiltFactor)).toInt()
            }
            
            paint.color = Color.argb(alpha, textureRed, textureGreen, textureBlue)
            val pointCount = (centerRadius * 0.5f).toInt()
            
            // Points fixes basés sur la position mais avec perspective
            val seed = (centerX + centerY).toInt()
            for (i in 0 until pointCount) {
                val angleSeed = (seed + i * 137) % 360
                val distanceSeed = ((seed + i * 97) % 100) / 100f
                
                val angle = angleSeed * PI / 180.0
                val distance = distanceSeed * radiusY * 0.8f
                val pointX = centerX + cos(angle).toFloat() * distance * perspectiveFactor
                val pointY = centerY + sin(angle).toFloat() * distance
                
                var pointSize = 1.5f
                if (dissolveInfo?.flowersPetalsWilting == true) {
                    pointSize *= (1f - dissolveInfo.progress * 0.3f)
                }
                
                canvas.drawCircle(pointX, pointY, pointSize, paint)
            }
        }
    }
    
    // ==================== FONCTION UTILITAIRE ====================
    
    companion object {
        private var flowerIdCounter = 0
        
        // Générateur d'ID unique pour les fleurs
        private fun generateFlowerId(): String {
            flowerIdCounter++
            return "flower_$flowerIdCounter"
        }
    }
}
