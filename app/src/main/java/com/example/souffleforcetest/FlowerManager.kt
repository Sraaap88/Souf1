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
        var isFullyGrown: Boolean = false
    )
    
    data class Petal(
        val angle: Float,            // Angle autour du centre (0-360°)
        val length: Float,           // Longueur du pétale
        val width: Float,            // Largeur du pétale
        var currentLength: Float = 0f,
        val perspective: PetalPerspective
    )
    
    data class FlowerPerspective(
        val viewAngle: Float,        // Angle de vue (0° = face, 90° = profil)
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
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.25f
    private val baseFlowerSize = 234f  // +30% (180f × 1.3)
    private val maxFlowerSize = 468f   // +30% (360f × 1.3)
    private val growthRate = 400f
    private val petalCount = 18 + (Math.random() * 8).toInt() // 18-26 pétales
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun processFlowerGrowth(force: Float) {
        // Créer des fleurs sur chaque tige qui n'en a pas encore (au début de la phase)
        if (flowers.isEmpty()) {
            createFlowersOnStems()
        }
        
        // Faire grandir les fleurs existantes
        growExistingFlowers(force)
        
        lastForce = force
    }
    
    fun resetFlowers() {
        flowers.clear()
        lastForce = 0f
    }
    
    fun drawFlowers(canvas: Canvas, flowerPaint: Paint, centerPaint: Paint) {
        for (flower in flowers) {
            if (flower.currentSize > 0) {
                drawSingleFlower(canvas, flower, flowerPaint, centerPaint)
            }
        }
    }
    
    fun drawSpecificFlowers(canvas: Canvas, specificFlowers: List<Flower>, flowerPaint: Paint, centerPaint: Paint) {
        for (flower in specificFlowers) {
            if (flower.currentSize > 0) {
                drawSingleFlower(canvas, flower, flowerPaint, centerPaint)
            }
        }
    }
    
    // ==================== FONCTIONS PRIVÉES ====================
    
    private fun createFlowersOnStems() {
        // Créer fleur sur tige principale si elle a une taille suffisante
        if (!flowers.any { it.stemIndex == -1 } && plantStem.mainStem.size > 5) {
            createFlowerOnMainStem()
        }
        
        // Créer fleurs sur chaque branche qui a une taille suffisante
        for (branchIndex in plantStem.branches.indices) {
            val branch = plantStem.branches[branchIndex]
            if (!flowers.any { it.stemIndex == branchIndex } && branch.points.size > 3) {
                createFlowerOnBranch(branchIndex)
            }
        }
    }
    
    private fun createFlowerOnMainStem() {
        val mainStem = plantStem.mainStem
        if (mainStem.size < 5) return
        
        // Prendre le DERNIER point (sommet de la tige)
        val topPoint = mainStem.last()
        val size = baseFlowerSize + (Math.random() * (maxFlowerSize - baseFlowerSize)).toFloat()
        
        // Tige principale = vue de face (perspective frontale simple)
        val perspective = FlowerPerspective(
            viewAngle = 0f + (Math.random() * 10f - 5f).toFloat(), // Presque face avec légère variation
            tiltAngle = (Math.random() * 15f - 7.5f).toFloat(),    // Légère inclinaison
            rotationAngle = (Math.random() * 360f).toFloat()        // Rotation aléatoire
        )
        
        val flower = Flower(
            x = topPoint.x,
            y = topPoint.y - 20f,
            stemIndex = -1,
            maxSize = size,
            perspective = perspective
        )
        
        // Créer les pétales
        createPetalsForFlower(flower)
        flowers.add(flower)
    }
    
    private fun createFlowerOnBranch(branchIndex: Int) {
        val branch = plantStem.branches[branchIndex]
        if (branch.points.size < 3) return
        
        // Prendre le DERNIER point (bout de la branche)
        val topPoint = branch.points.last()
        val size = (baseFlowerSize * 0.8f) + (Math.random() * (maxFlowerSize * 0.8f - baseFlowerSize * 0.8f)).toFloat()
        
        // Branches = perspectives variées selon leur angle
        val branchAngle = branch.angle
        val perspective = FlowerPerspective(
            viewAngle = 30f + abs(branchAngle) * 0.5f + (Math.random() * 20f - 10f).toFloat(), // Plus inclinées
            tiltAngle = branchAngle * 0.3f + (Math.random() * 20f - 10f).toFloat(),
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
    }
    
    private fun createPetalsForFlower(flower: Flower) {
        val basePetalCount = 18 + (Math.random() * 8).toInt() // 18-26 pétales de base
        val petalCount = (basePetalCount * 1.2f).toInt() // +20% de pétales (22-31)
        
        for (i in 0 until petalCount) {
            val angle = (i * 360f / petalCount) + (Math.random() * 10f - 5f).toFloat()
            val baseLength = flower.maxSize * 0.4f
            val length = baseLength + (Math.random() * baseLength * 0.3f).toFloat()
            val baseWidth = length * 0.25f + (Math.random() * length * 0.1f).toFloat()
            val width = baseWidth * 1.2f // +20% plus larges
            
            // Calculer la perspective du pétale (version simple)
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
        // Convertir l'angle du pétale en coordonnées 3D virtuelles
        val adjustedAngle = petalAngle + flowerPerspective.rotationAngle
        val radians = Math.toRadians(adjustedAngle.toDouble())
        
        // Facteur de profondeur selon l'angle de vue
        val depthFactor = cos(radians).toFloat() * sin(Math.toRadians(flowerPerspective.viewAngle.toDouble())).toFloat()
        
        // Visibilité (les pétales arrière sont moins visibles en perspective) - VERSION SIMPLE
        val visibilityFactor = if (depthFactor < 0) {
            0.3f + (depthFactor + 1f) * 0.7f // Pétales arrière plus transparents
        } else {
            1f // Pétales avant complètement visibles
        }
        
        // Facteur de largeur (compression perspective)
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
                val adjustedGrowth = force * qualityMultiplier * progressCurve * growthRate * 0.008f
                
                flower.currentSize = (flower.currentSize + adjustedGrowth).coerceAtMost(flower.maxSize)
                
                // CENTRE GRANDIT PROPORTIONNELLEMENT : 67.5% de la taille actuelle (75% - 10%)
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
    }
    
    private fun drawSingleFlower(canvas: Canvas, flower: Flower, flowerPaint: Paint, centerPaint: Paint) {
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
    
    private fun drawPetal(canvas: Canvas, centerX: Float, centerY: Float, petal: Petal, flower: Flower, paint: Paint) {
        val perspective = petal.perspective
        val flowerPerspective = flower.perspective
        
        // Calculer la position du pétale avec perspective
        val adjustedAngle = petal.angle + flowerPerspective.rotationAngle
        val radians = Math.toRadians(adjustedAngle.toDouble())
        
        val baseDistance = petal.currentLength * 0.3f
        val tipDistance = petal.currentLength
        
        // Positions avec perspective
        val cos = cos(radians).toFloat()
        val sin = sin(radians).toFloat()
        
        // Appliquer la perspective 3D
        val perspectiveFactor = cos(Math.toRadians(flowerPerspective.viewAngle.toDouble())).toFloat()
        val tiltFactor = sin(Math.toRadians(flowerPerspective.tiltAngle.toDouble())).toFloat()
        
        val baseX = centerX + cos * baseDistance * perspectiveFactor
        val baseY = centerY + sin * baseDistance + cos * baseDistance * tiltFactor * 0.3f
        
        val tipX = centerX + cos * tipDistance * perspectiveFactor
        val tipY = centerY + sin * tipDistance + cos * tipDistance * tiltFactor * 0.3f
        
        val width = petal.width * perspective.widthFactor * (flower.currentSize / flower.maxSize)
        
        // Couleur avec transparence selon la visibilité
        val alpha = (255 * perspective.visibilityFactor).toInt()
        paint.color = Color.argb(alpha, 255, 255, 255) // Blanc avec transparence
        paint.strokeWidth = width
        paint.strokeCap = Paint.Cap.ROUND
        
        // Dessiner le pétale comme une ligne épaisse
        canvas.drawLine(baseX, baseY, tipX, tipY, paint)
    }
    
    private fun drawFlowerCenter(canvas: Canvas, centerX: Float, centerY: Float, flower: Flower, paint: Paint) {
        val centerRadius = flower.centerSize * 0.4f
        val perspective = flower.perspective
        
        // Appliquer la perspective au centre
        val perspectiveFactor = cos(Math.toRadians(perspective.viewAngle.toDouble())).toFloat()
        val radiusX = centerRadius * perspectiveFactor
        val radiusY = centerRadius
        
        // Dégradé jaune-orange pour le centre
        paint.color = Color.rgb(255, 200, 50)
        
        // Dessiner le centre comme une ellipse selon la perspective
        canvas.save()
        canvas.translate(centerX, centerY)
        canvas.scale(1f, perspectiveFactor)
        canvas.drawCircle(0f, 0f, radiusY, paint)
        canvas.restore()
        
        // Ajouter texture granuleuse avec petits points
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
}
