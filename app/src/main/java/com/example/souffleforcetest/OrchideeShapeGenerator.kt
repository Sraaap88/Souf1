package com.example.souffleforcetest

import android.graphics.Path
import android.graphics.PointF
import kotlin.random.Random
import kotlin.math.*

/**
 * GÉNÉRATEUR SPÉCIALISÉ DE FORMES GÉOMÉTRIQUES POUR ORCHIDÉES
 * Crée des géométries procédurales optimisées pour Canvas Android
 */
class OrchideeShapeGenerator {
    
    companion object {
        
        // ==================== CONSTANTES DE FORME ====================
        
        private const val BASE_SIZE = 100f
        private const val GOLDEN_RATIO = 1.618f
        private const val PI_F = PI.toFloat()
        
        // ==================== GÉNÉRATEURS PRINCIPAUX PAR ESPÈCE ====================
        
        /**
         * Génère la forme caractéristique Phalaenopsis (papillon)
         */
        fun generatePhalaenopsisShape(
            variation: OrchideeShapeVariation,
            centerX: Float = 0f,
            centerY: Float = 0f,
            baseScale: Float = 1f
        ): OrchideeFlowerGeometry {
            
            val scale = baseScale * variation.labelleSize
            val asymmetry = variation.asymmetry
            
            // Sépales dorsaux et latéraux
            val dorsalSepal = createPhalaenopsisDorsalSepal(variation, centerX, centerY, scale)
            val lateralSepals = createPhalaenopsisLateralSepals(variation, centerX, centerY, scale, asymmetry)
            
            // Pétales en ailes de papillon
            val leftPetal = createPhalaenopsisWingPetal(variation, centerX, centerY, scale, -1f, asymmetry)
            val rightPetal = createPhalaenopsisWingPetal(variation, centerX, centerY, scale, 1f, asymmetry)
            
            // Labelle complexe en forme de papillon
            val labelle = createPhalaenopsisLabelle(variation, centerX, centerY, scale)
            
            // Colonne centrale
            val column = createPhalaenopsisColumn(variation, centerX, centerY, scale * 0.3f)
            
            return OrchideeFlowerGeometry(
                dorsalSepal = dorsalSepal,
                lateralSepals = lateralSepals,
                petals = listOf(leftPetal, rightPetal),
                labelle = labelle,
                column = column,
                centerPoint = PointF(centerX, centerY),
                boundingRadius = scale * variation.spreadAngle / 90f
            )
        }
        
        private fun createPhalaenopsisDorsalSepal(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float
        ): Path {
            val path = Path()
            val length = BASE_SIZE * variation.sepalLength * scale
            val width = BASE_SIZE * variation.sepalWidth * scale * 0.6f
            
            // Forme allongée pointue vers le haut
            val tipY = centerY - length
            val baseWidth = width * 0.8f
            
            path.moveTo(centerX, centerY)
            
            // Courbe de Bézier pour forme naturelle
            path.cubicTo(
                centerX - baseWidth/2, centerY - length * 0.3f,
                centerX - width/3, centerY - length * 0.7f,
                centerX, tipY
            )
            
            path.cubicTo(
                centerX + width/3, centerY - length * 0.7f,
                centerX + baseWidth/2, centerY - length * 0.3f,
                centerX, centerY
            )
            
            // Ajouter variations naturelles
            addNaturalVariations(path, variation.ruffledEdges * 0.3f, scale)
            
            return path
        }
        
        private fun createPhalaenopsisLateralSepals(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float,
            asymmetry: Float
        ): List<Path> {
            val sepals = mutableListOf<Path>()
            val length = BASE_SIZE * variation.sepalLength * scale * 0.9f
            val width = BASE_SIZE * variation.sepalWidth * scale * 0.5f
            
            // Sépale gauche
            val leftPath = Path()
            val leftAngle = -45f + asymmetry * 10f
            val leftTipX = centerX + cos(Math.toRadians(leftAngle.toDouble())).toFloat() * length
            val leftTipY = centerY + sin(Math.toRadians(leftAngle.toDouble())).toFloat() * length
            
            leftPath.moveTo(centerX, centerY)
            leftPath.cubicTo(
                centerX - width/2, centerY + length * 0.2f,
                leftTipX - width/4, leftTipY - length * 0.1f,
                leftTipX, leftTipY
            )
            leftPath.cubicTo(
                leftTipX + width/4, leftTipY + length * 0.1f,
                centerX + width/3, centerY + length * 0.3f,
                centerX, centerY
            )
            
            // Sépale droit (symétrique avec variations)
            val rightPath = Path()
            val rightAngle = -135f - asymmetry * 10f
            val rightTipX = centerX + cos(Math.toRadians(rightAngle.toDouble())).toFloat() * length
            val rightTipY = centerY + sin(Math.toRadians(rightAngle.toDouble())).toFloat() * length
            
            rightPath.moveTo(centerX, centerY)
            rightPath.cubicTo(
                centerX + width/2, centerY + length * 0.2f,
                rightTipX + width/4, rightTipY - length * 0.1f,
                rightTipX, rightTipY
            )
            rightPath.cubicTo(
                rightTipX - width/4, rightTipY + length * 0.1f,
                centerX - width/3, centerY + length * 0.3f,
                centerX, centerY
            )
            
            sepals.add(leftPath)
            sepals.add(rightPath)
            
            return sepals
        }
        
        private fun createPhalaenopsisWingPetal(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float,
            side: Float, // -1 pour gauche, 1 pour droite
            asymmetry: Float
        ): Path {
            val path = Path()
            val length = BASE_SIZE * variation.petalLength * scale
            val width = BASE_SIZE * variation.petalWidth * scale
            val curvature = variation.petalCurvature
            
            // Angle d'ouverture des ailes
            val baseAngle = variation.spreadAngle / 2f
            val wingAngle = (90f - baseAngle) * side + asymmetry * 15f * side
            
            val tipX = centerX + cos(Math.toRadians(wingAngle.toDouble())).toFloat() * length
            val tipY = centerY - sin(Math.toRadians(wingAngle.toDouble())).toFloat() * length * 0.3f
            
            // Point de contrôle pour la courbure de l'aile
            val controlOffset = width * curvature
            val control1X = centerX + side * controlOffset * 0.7f
            val control1Y = centerY - length * 0.2f
            val control2X = tipX - side * width * 0.3f
            val control2Y = tipY + length * 0.1f
            
            path.moveTo(centerX, centerY)
            
            // Bord extérieur de l'aile
            path.cubicTo(control1X, control1Y, control2X, control2Y, tipX, tipY)
            
            // Bord intérieur avec courbure plus douce
            val innerControlX = centerX + side * width * 0.4f
            val innerControlY = centerY - length * 0.4f
            path.cubicTo(
                tipX - side * width * 0.6f, tipY - length * 0.1f,
                innerControlX, innerControlY,
                centerX, centerY
            )
            
            // Ajouter ondulations sur les bords
            addWingRuffles(path, variation.ruffledEdges, scale, side)
            
            return path
        }
        
        private fun createPhalaenopsisLabelle(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float
        ): Path {
            val path = Path()
            val size = BASE_SIZE * variation.labelleSize * scale * 0.8f
            val depth = variation.throatDepth
            val ruffles = variation.ruffledEdges
            
            // Forme en papillon du labelle
            val wingSpan = size * 1.2f
            val bodyLength = size * 0.8f
            
            // Corps central allongé
            path.moveTo(centerX, centerY + bodyLength * 0.2f)
            
            // Aile gauche du labelle
            path.cubicTo(
                centerX - wingSpan * 0.3f, centerY + bodyLength * 0.1f,
                centerX - wingSpan * 0.5f, centerY + bodyLength * 0.4f,
                centerX - wingSpan * 0.4f, centerY + bodyLength * 0.7f
            )
            
            // Pointe du labelle
            path.cubicTo(
                centerX - size * 0.2f, centerY + bodyLength,
                centerX + size * 0.2f, centerY + bodyLength,
                centerX + wingSpan * 0.4f, centerY + bodyLength * 0.7f
            )
            
            // Aile droite du labelle
            path.cubicTo(
                centerX + wingSpan * 0.5f, centerY + bodyLength * 0.4f,
                centerX + wingSpan * 0.3f, centerY + bodyLength * 0.1f,
                centerX, centerY + bodyLength * 0.2f
            )
            
            // Gorge profonde au centre
            addThroatDetails(path, centerX, centerY + bodyLength * 0.3f, size * depth * 0.3f, ruffles)
            
            return path
        }
        
        /**
         * Génère la forme caractéristique Cattleya (grande fleur avec froufrous)
         */
        fun generateCattleyaShape(
            variation: OrchideeShapeVariation,
            centerX: Float = 0f,
            centerY: Float = 0f,
            baseScale: Float = 1f
        ): OrchideeFlowerGeometry {
            
            val scale = baseScale * variation.labelleSize
            
            // Sépales plus larges et arrondis
            val dorsalSepal = createCattleyaDorsalSepal(variation, centerX, centerY, scale)
            val lateralSepals = createCattleyaLateralSepals(variation, centerX, centerY, scale)
            
            // Pétales larges avec froufrous intenses
            val leftPetal = createCattleyaRuffledPetal(variation, centerX, centerY, scale, -1f)
            val rightPetal = createCattleyaRuffledPetal(variation, centerX, centerY, scale, 1f)
            
            // Labelle en trompette spectaculaire
            val labelle = createCattleyaTrumpetLabelle(variation, centerX, centerY, scale)
            
            // Colonne proéminente
            val column = createCattleyaColumn(variation, centerX, centerY, scale * 0.4f)
            
            return OrchideeFlowerGeometry(
                dorsalSepal = dorsalSepal,
                lateralSepals = lateralSepals,
                petals = listOf(leftPetal, rightPetal),
                labelle = labelle,
                column = column,
                centerPoint = PointF(centerX, centerY),
                boundingRadius = scale * 1.3f
            )
        }
        
        private fun createCattleyaDorsalSepal(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float
        ): Path {
            val path = Path()
            val length = BASE_SIZE * variation.sepalLength * scale * 1.2f
            val width = BASE_SIZE * variation.sepalWidth * scale
            
            // Forme ovale allongée
            path.addOval(
                centerX - width/2, 
                centerY - length, 
                centerX + width/2, 
                centerY - length * 0.2f, 
                Path.Direction.CW
            )
            
            // Ajouter des froufrous sur les bords
            addIntenseRuffles(path, variation.ruffledEdges, scale)
            
            return path
        }
        
        private fun createCattleyaLateralSepals(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float
        ): List<Path> {
            val sepals = mutableListOf<Path>()
            val length = BASE_SIZE * variation.sepalLength * scale
            val width = BASE_SIZE * variation.sepalWidth * scale * 0.8f
            
            // Angles plus ouverts pour Cattleya
            val angles = listOf(-60f, -120f)
            
            for (angle in angles) {
                val path = Path()
                val tipX = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * length
                val tipY = centerY + sin(Math.toRadians(angle.toDouble())).toFloat() * length
                
                // Forme ovale inclinée
                val matrix = android.graphics.Matrix()
                matrix.setRotate(angle, centerX, centerY)
                
                val ovalPath = Path()
                ovalPath.addOval(
                    centerX - width/2, 
                    centerY, 
                    centerX + width/2, 
                    centerY + length, 
                    Path.Direction.CW
                )
                ovalPath.transform(matrix)
                
                sepals.add(ovalPath)
            }
            
            return sepals
        }
        
        private fun createCattleyaRuffledPetal(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float,
            side: Float
        ): Path {
            val path = Path()
            val length = BASE_SIZE * variation.petalLength * scale * 1.3f
            val width = BASE_SIZE * variation.petalWidth * scale * 1.2f
            val ruffleIntensity = variation.ruffledEdges
            
            // Base du pétale
            val baseAngle = 75f * side
            val tipX = centerX + cos(Math.toRadians(baseAngle.toDouble())).toFloat() * length
            val tipY = centerY - sin(Math.toRadians(baseAngle.toDouble())).toFloat() * length * 0.5f
            
            path.moveTo(centerX, centerY)
            
            // Créer une forme large avec nombreux froufrous
            val ruffleCount = (5 + ruffleIntensity * 10).toInt()
            val angleStep = (baseAngle * 2) / ruffleCount
            
            for (i in 0..ruffleCount) {
                val currentAngle = baseAngle - angleStep * i
                val distance = length * (0.8f + 0.2f * sin(i * PI_F / 2))
                val ruffleSize = width * ruffleIntensity * (0.1f + 0.1f * sin(i * PI_F))
                
                val x = centerX + cos(Math.toRadians(currentAngle.toDouble())).toFloat() * distance
                val y = centerY - sin(Math.toRadians(currentAngle.toDouble())).toFloat() * distance * 0.5f
                
                // Ajouter ondulation
                val ruffleX = x + sin(i * PI_F / 3) * ruffleSize
                val ruffleY = y + cos(i * PI_F / 3) * ruffleSize
                
                if (i == 0) {
                    path.lineTo(ruffleX, ruffleY)
                } else {
                    path.quadTo(x, y, ruffleX, ruffleY)
                }
            }
            
            path.close()
            
            return path
        }
        
        private fun createCattleyaTrumpetLabelle(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float
        ): Path {
            val path = Path()
            val size = BASE_SIZE * variation.labelleSize * scale * 1.4f
            val depth = variation.throatDepth
            val ruffles = variation.ruffledEdges
            
            // Forme de trompette évasée
            val mouthWidth = size * 1.5f
            val throatWidth = size * 0.3f
            val length = size * 0.9f
            
            path.moveTo(centerX - throatWidth/2, centerY + length * 0.1f)
            
            // Évasement progressif de la trompette
            path.cubicTo(
                centerX - mouthWidth * 0.3f, centerY + length * 0.3f,
                centerX - mouthWidth * 0.5f, centerY + length * 0.6f,
                centerX - mouthWidth/2, centerY + length
            )
            
            // Bord inférieur avec froufrous intenses
            addTrumpetRuffles(path, centerX, centerY + length, mouthWidth, ruffles)
            
            // Côté droit
            path.cubicTo(
                centerX + mouthWidth * 0.5f, centerY + length * 0.6f,
                centerX + mouthWidth * 0.3f, centerY + length * 0.3f,
                centerX + throatWidth/2, centerY + length * 0.1f
            )
            
            // Gorge profonde
            path.cubicTo(
                centerX + throatWidth/4, centerY + length * 0.05f,
                centerX - throatWidth/4, centerY + length * 0.05f,
                centerX - throatWidth/2, centerY + length * 0.1f
            )
            
            return path
        }
        
        /**
         * Génère la forme caractéristique Vanda (plate et géométrique)
         */
        fun generateVandaShape(
            variation: OrchideeShapeVariation,
            centerX: Float = 0f,
            centerY: Float = 0f,
            baseScale: Float = 1f
        ): OrchideeFlowerGeometry {
            
            val scale = baseScale * variation.labelleSize
            val flatness = variation.flatness
            
            // Formes plus plates et géométriques
            val dorsalSepal = createVandaFlatSepal(variation, centerX, centerY, scale, 0f)
            val lateralSepals = createVandaFlatSepals(variation, centerX, centerY, scale)
            
            // Pétales symétriques et plats
            val leftPetal = createVandaFlatPetal(variation, centerX, centerY, scale, -1f)
            val rightPetal = createVandaFlatPetal(variation, centerX, centerY, scale, 1f)
            
            // Labelle petit et pointu
            val labelle = createVandaPointedLabelle(variation, centerX, centerY, scale)
            
            // Colonne discrète
            val column = createVandaColumn(variation, centerX, centerY, scale * 0.2f)
            
            return OrchideeFlowerGeometry(
                dorsalSepal = dorsalSepal,
                lateralSepals = lateralSepals,
                petals = listOf(leftPetal, rightPetal),
                labelle = labelle,
                column = column,
                centerPoint = PointF(centerX, centerY),
                boundingRadius = scale * flatness
            )
        }
        
        private fun createVandaFlatSepal(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float,
            angle: Float
        ): Path {
            val path = Path()
            val length = BASE_SIZE * variation.sepalLength * scale
            val width = BASE_SIZE * variation.sepalWidth * scale
            val flatness = variation.flatness
            
            // Forme très plate et géométrique
            val matrix = android.graphics.Matrix()
            matrix.setRotate(angle, centerX, centerY)
            
            val flatPath = Path()
            flatPath.addRect(
                centerX - width/2, 
                centerY - length * flatness, 
                centerX + width/2, 
                centerY + length * 0.2f * flatness, 
                Path.Direction.CW
            )
            
            // Arrondir légèrement les coins
            addSubtleRounding(flatPath, width * 0.1f)
            flatPath.transform(matrix)
            
            return flatPath
        }
        
        private fun createVandaFlatSepals(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float
        ): List<Path> {
            return listOf(
                createVandaFlatSepal(variation, centerX, centerY, scale, -45f),
                createVandaFlatSepal(variation, centerX, centerY, scale, -135f)
            )
        }
        
        private fun createVandaFlatPetal(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float,
            side: Float
        ): Path {
            val path = Path()
            val length = BASE_SIZE * variation.petalLength * scale
            val width = BASE_SIZE * variation.petalWidth * scale
            val flatness = variation.flatness
            
            // Angle pour Vanda très ouvert
            val angle = 85f * side
            val tipX = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * length
            val tipY = centerY - sin(Math.toRadians(angle.toDouble())).toFloat() * length * flatness * 0.3f
            
            // Forme rectangulaire arrondie
            path.moveTo(centerX, centerY)
            path.lineTo(centerX + side * width * 0.3f, centerY)
            path.lineTo(tipX + side * width * 0.2f, tipY)
            path.lineTo(tipX - side * width * 0.2f, tipY)
            path.lineTo(centerX - side * width * 0.1f, centerY)
            path.close()
            
            return path
        }
        
        private fun createVandaPointedLabelle(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float
        ): Path {
            val path = Path()
            val size = BASE_SIZE * variation.labelleSize * scale * 0.6f
            
            // Forme triangulaire pointue
            path.moveTo(centerX, centerY + size * 0.1f)
            path.lineTo(centerX - size * 0.4f, centerY + size * 0.5f)
            path.lineTo(centerX, centerY + size)
            path.lineTo(centerX + size * 0.4f, centerY + size * 0.5f)
            path.close()
            
            return path
        }
        
        /**
         * Génère la forme caractéristique Oncidium (dancing lady)
         */
        fun generateOncidiumShape(
            variation: OrchideeShapeVariation,
            centerX: Float = 0f,
            centerY: Float = 0f,
            baseScale: Float = 1f
        ): OrchideeFlowerGeometry {
            
            val scale = baseScale * variation.labelleSize * 0.7f // Plus petites
            
            // Sépales et pétales plus petits
            val dorsalSepal = createOncidiumSmallSepal(variation, centerX, centerY, scale)
            val lateralSepals = createOncidiumSmallSepals(variation, centerX, centerY, scale)
            
            val leftPetal = createOncidiumSmallPetal(variation, centerX, centerY, scale, -1f)
            val rightPetal = createOncidiumSmallPetal(variation, centerX, centerY, scale, 1f)
            
            // Labelle en forme de jupe de danseuse
            val labelle = createOncidiumDancingSkirt(variation, centerX, centerY, scale)
            
            // Colonne typique
            val column = createOncidiumColumn(variation, centerX, centerY, scale * 0.3f)
            
            return OrchideeFlowerGeometry(
                dorsalSepal = dorsalSepal,
                lateralSepals = lateralSepals,
                petals = listOf(leftPetal, rightPetal),
                labelle = labelle,
                column = column,
                centerPoint = PointF(centerX, centerY),
                boundingRadius = scale * 1.2f
            )
        }
        
        private fun createOncidiumSmallSepal(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float
        ): Path {
            val path = Path()
            val length = BASE_SIZE * variation.sepalLength * scale * 0.8f
            val width = BASE_SIZE * variation.sepalWidth * scale * 0.6f
            
            // Forme ovale étroite
            path.addOval(
                centerX - width/2, 
                centerY - length, 
                centerX + width/2, 
                centerY - length * 0.1f, 
                Path.Direction.CW
            )
            
            return path
        }
        
        private fun createOncidiumSmallSepals(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float
        ): List<Path> {
            return listOf(
                createOncidiumSmallSepal(variation, centerX, centerY, scale),
                createOncidiumSmallSepal(variation, centerX, centerY, scale)
            ).mapIndexed { index, path ->
                val matrix = android.graphics.Matrix()
                matrix.setRotate(if (index == 0) -30f else -150f, centerX, centerY)
                val transformedPath = Path()
                path.transform(matrix, transformedPath)
                transformedPath
            }
        }
        
        private fun createOncidiumSmallPetal(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float,
            side: Float
        ): Path {
            val path = Path()
            val length = BASE_SIZE * variation.petalLength * scale * 0.7f
            val width = BASE_SIZE * variation.petalWidth * scale * 0.5f
            
            val angle = 60f * side
            val tipX = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * length
            val tipY = centerY - sin(Math.toRadians(angle.toDouble())).toFloat() * length * 0.6f
            
            // Forme simple allongée
            path.moveTo(centerX, centerY)
            path.quadTo(centerX + side * width/2, centerY - length * 0.3f, tipX, tipY)
            path.quadTo(centerX + side * width/3, centerY - length * 0.2f, centerX, centerY)
            
            return path
        }
        
        private fun createOncidiumDancingSkirt(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float
        ): Path {
            val path = Path()
            val size = BASE_SIZE * variation.labelleSize * scale * 1.3f
            val fullness = 0.7f + variation.ruffledEdges * 0.3f // Ampleur de la jupe
            
            // Taille de la danseuse
            val waistY = centerY + size * 0.2f
            val waistWidth = size * 0.3f
            
            // Jupe évasée
            val skirtBottom = centerY + size
            val skirtWidth = size * fullness
            
            path.moveTo(centerX - waistWidth/2, waistY)
            
            // Côté gauche de la jupe avec ondulations
            val leftSkirtEdge = centerX - skirtWidth/2
            addSkirtRuffles(path, centerX - waistWidth/2, waistY, leftSkirtEdge, skirtBottom, fullness)
            
            // Bas de la jupe
            path.lineTo(centerX + skirtWidth/2, skirtBottom)
            
            // Côté droit de la jupe
            addSkirtRuffles(path, centerX + skirtWidth/2, skirtBottom, centerX + waistWidth/2, waistY, fullness)
            
            path.close()
            
            return path
        }
        
        /**
         * Génère la forme caractéristique Dendrobium (tube étroit)
         */
        fun generateDendrobiumShape(
            variation: OrchideeShapeVariation,
            centerX: Float = 0f,
            centerY: Float = 0f,
            baseScale: Float = 1f
        ): OrchideeFlowerGeometry {
            
            val scale = baseScale * variation.labelleSize * 0.8f
            
            // Formes plus étroites et allongées
            val dorsalSepal = createDendrobiumNarrowSepal(variation, centerX, centerY, scale)
            val lateralSepals = createDendrobiumNarrowSepals(variation, centerX, centerY, scale)
            
            val leftPetal = createDendrobiumNarrowPetal(variation, centerX, centerY, scale, -1f)
            val rightPetal = createDendrobiumNarrowPetal(variation, centerX, centerY, scale, 1f)
            
            // Labelle en tube étroit
            val labelle = createDendrobiumTubeLabelle(variation, centerX, centerY, scale)
            
            val column = createDendrobiumColumn(variation, centerX, centerY, scale * 0.4f)
            
            return OrchideeFlowerGeometry(
                dorsalSepal = dorsalSepal,
                lateralSepals = lateralSepals,
                petals = listOf(leftPetal, rightPetal),
                labelle = labelle,
                column = column,
                centerPoint = PointF(centerX, centerY),
                boundingRadius = scale * 0.9f
            )
        }
        
        private fun createDendrobiumNarrowSepal(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float
        ): Path {
            val path = Path()
            val length = BASE_SIZE * variation.sepalLength * scale
            val width = BASE_SIZE * variation.sepalWidth * scale * 0.7f
            
            // Forme lance étroite
            path.moveTo(centerX, centerY)
            path.quadTo(centerX - width/2, centerY - length * 0.7f, centerX, centerY - length)
            path.quadTo(centerX + width/2, centerY - length * 0.7f, centerX, centerY)
            
            return path
        }
        
        private fun createDendrobiumNarrowSepals(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float
        ): List<Path> {
            return listOf(-40f, -140f).map { angle ->
                val path = createDendrobiumNarrowSepal(variation, centerX, centerY, scale)
                val matrix = android.graphics.Matrix()
                matrix.setRotate(angle, centerX, centerY)
                val transformedPath = Path()
                path.transform(matrix, transformedPath)
                transformedPath
            }
        }
        
        private fun createDendrobiumNarrowPetal(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float,
            side: Float
        ): Path {
            val path = Path()
            val length = BASE_SIZE * variation.petalLength * scale * 0.8f
            val width = BASE_SIZE * variation.petalWidth * scale * 0.6f
            
            val angle = 70f * side
            val tipX = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * length
            val tipY = centerY - sin(Math.toRadians(angle.toDouble())).toFloat() * length * 0.4f
            
            path.moveTo(centerX, centerY)
            path.quadTo(centerX + side * width/2, centerY - length * 0.5f, tipX, tipY)
            path.quadTo(centerX + side * width/3, centerY - length * 0.3f, centerX, centerY)
            
            return path
        }
        
        private fun createDendrobiumTubeLabelle(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float
        ): Path {
            val path = Path()
            val size = BASE_SIZE * variation.labelleSize * scale * 0.7f
            val tubeLength = variation.throatDepth
            
            // Tube étroit caractéristique
            val tubeWidth = size * 0.4f
            val tubeHeight = size * tubeLength
            
            path.addRect(
                centerX - tubeWidth/2, 
                centerY + size * 0.1f, 
                centerX + tubeWidth/2, 
                centerY + tubeHeight, 
                Path.Direction.CW
            )
            
            // Arrondir le bout du tube
            addSubtleRounding(path, tubeWidth * 0.2f)
            
            return path
        }
        
        /**
         * Génère la forme caractéristique Cymbidium (bateau)
         */
        fun generateCymbidiumShape(
            variation: OrchideeShapeVariation,
            centerX: Float = 0f,
            centerY: Float = 0f,
            baseScale: Float = 1f
        ): OrchideeFlowerGeometry {
            
            val scale = baseScale * variation.labelleSize
            
            val dorsalSepal = createCymbidiumBoatSepal(variation, centerX, centerY, scale)
            val lateralSepals = createCymbidiumBoatSepals(variation, centerX, centerY, scale)
            
            val leftPetal = createCymbidiumBoatPetal(variation, centerX, centerY, scale, -1f)
            val rightPetal = createCymbidiumBoatPetal(variation, centerX, centerY, scale, 1f)
            
            // Labelle en forme de bateau
            val labelle = createCymbidiumBoatLabelle(variation, centerX, centerY, scale)
            
            val column = createCymbidiumColumn(variation, centerX, centerY, scale * 0.35f)
            
            return OrchideeFlowerGeometry(
                dorsalSepal = dorsalSepal,
                lateralSepals = lateralSepals,
                petals = listOf(leftPetal, rightPetal),
                labelle = labelle,
                column = column,
                centerPoint = PointF(centerX, centerY),
                boundingRadius = scale * 1.1f
            )
        }
        
        private fun createCymbidiumBoatSepal(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float
        ): Path {
            val path = Path()
            val length = BASE_SIZE * variation.sepalLength * scale * 1.1f
            val width = BASE_SIZE * variation.sepalWidth * scale * 0.9f
            
            // Forme incurvée comme une coque de bateau
            path.moveTo(centerX, centerY)
            path.cubicTo(
                centerX - width/2, centerY - length * 0.3f,
                centerX - width/3, centerY - length * 0.8f,
                centerX, centerY - length
            )
            path.cubicTo(
                centerX + width/3, centerY - length * 0.8f,
                centerX + width/2, centerY - length * 0.3f,
                centerX, centerY
            )
            
            return path
        }
        
        private fun createCymbidiumBoatSepals(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float
        ): List<Path> {
            return listOf(-50f, -130f).map { angle ->
                val path = createCymbidiumBoatSepal(variation, centerX, centerY, scale)
                val matrix = android.graphics.Matrix()
                matrix.setRotate(angle, centerX, centerY)
                val transformedPath = Path()
                path.transform(matrix, transformedPath)
                transformedPath
            }
        }
        
        private fun createCymbidiumBoatPetal(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float,
            side: Float
        ): Path {
            val path = Path()
            val length = BASE_SIZE * variation.petalLength * scale
            val width = BASE_SIZE * variation.petalWidth * scale * 0.8f
            
            val angle = 80f * side
            val tipX = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * length
            val tipY = centerY - sin(Math.toRadians(angle.toDouble())).toFloat() * length * 0.4f
            
            // Forme incurvée similaire aux sépales
            path.moveTo(centerX, centerY)
            path.cubicTo(
                centerX + side * width/3, centerY - length * 0.2f,
                tipX - side * width/4, tipY + length * 0.1f,
                tipX, tipY
            )
            path.cubicTo(
                tipX + side * width/4, tipY - length * 0.1f,
                centerX + side * width/2, centerY - length * 0.3f,
                centerX, centerY
            )
            
            return path
        }
        
        private fun createCymbidiumBoatLabelle(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float
        ): Path {
            val path = Path()
            val size = BASE_SIZE * variation.labelleSize * scale
            
            // Forme de bateau renversé
            val boatLength = size * 1.2f
            val boatWidth = size * 0.8f
            val boatDepth = size * variation.throatDepth * 0.5f
            
            path.moveTo(centerX - boatWidth/2, centerY + size * 0.2f)
            
            // Coque du bateau
            path.cubicTo(
                centerX - boatWidth/2, centerY + boatLength * 0.4f,
                centerX - boatWidth/3, centerY + boatLength * 0.8f,
                centerX, centerY + boatLength
            )
            path.cubicTo(
                centerX + boatWidth/3, centerY + boatLength * 0.8f,
                centerX + boatWidth/2, centerY + boatLength * 0.4f,
                centerX + boatWidth/2, centerY + size * 0.2f
            )
            
            // Intérieur du bateau (gorge)
            path.cubicTo(
                centerX + boatWidth/3, centerY + size * 0.3f,
                centerX - boatWidth/3, centerY + size * 0.3f,
                centerX - boatWidth/2, centerY + size * 0.2f
            )
            
            return path
        }
        
        // ==================== FONCTIONS DE CRÉATION DE COLONNES ====================
        
        private fun createPhalaenopsisColumn(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float
        ): Path {
            val path = Path()
            val height = scale * 0.8f
            val width = scale * 0.3f
            
            path.addOval(
                centerX - width/2, 
                centerY - height/2, 
                centerX + width/2, 
                centerY + height/2, 
                Path.Direction.CW
            )
            
            return path
        }
        
        private fun createCattleyaColumn(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float
        ): Path {
            val path = Path()
            val height = scale * 1.2f
            val width = scale * 0.5f
            
            // Colonne plus proéminente pour Cattleya
            path.addRect(
                centerX - width/2, 
                centerY - height/2, 
                centerX + width/2, 
                centerY + height/2, 
                Path.Direction.CW
            )
            
            addSubtleRounding(path, width * 0.2f)
            
            return path
        }
        
        private fun createVandaColumn(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float
        ): Path {
            val path = Path()
            val height = scale * 0.6f
            val width = scale * 0.2f
            
            // Colonne discrète et fine
            path.addOval(
                centerX - width/2, 
                centerY - height/2, 
                centerX + width/2, 
                centerY + height/2, 
                Path.Direction.CW
            )
            
            return path
        }
        
        private fun createOncidiumColumn(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float
        ): Path {
            return createPhalaenopsisColumn(variation, centerX, centerY, scale * 0.7f)
        }
        
        private fun createDendrobiumColumn(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float
        ): Path {
            val path = Path()
            val height = scale * 1.0f
            val width = scale * 0.25f
            
            path.addRect(
                centerX - width/2, 
                centerY - height/2, 
                centerX + width/2, 
                centerY + height/2, 
                Path.Direction.CW
            )
            
            return path
        }
        
        private fun createCymbidiumColumn(
            variation: OrchideeShapeVariation,
            centerX: Float,
            centerY: Float,
            scale: Float
        ): Path {
            return createCattleyaColumn(variation, centerX, centerY, scale * 0.8f)
        }
        
        // ==================== FONCTIONS D'EMBELLISSEMENT ====================
        
        /**
         * Ajoute des variations naturelles subtiles à un path
         */
        private fun addNaturalVariations(path: Path, intensity: Float, scale: Float) {
            // Cette fonction pourrait être étendue pour ajouter des micro-variations
            // Pour l'instant, les variations sont intégrées dans la génération des formes
        }
        
        /**
         * Ajoute des ondulations sur les bords des ailes
         */
        private fun addWingRuffles(path: Path, intensity: Float, scale: Float, side: Float) {
            // Les froufrous sont intégrés dans la création des pétales
        }
        
        /**
         * Ajoute des détails à la gorge
         */
        private fun addThroatDetails(path: Path, centerX: Float, centerY: Float, size: Float, intensity: Float) {
            // Ajouter un petit cercle pour la gorge profonde
            val throatRadius = size * intensity
            path.addCircle(centerX, centerY, throatRadius, Path.Direction.CW)
        }
        
        /**
         * Ajoute des froufrous intenses pour Cattleya
         */
        private fun addIntenseRuffles(path: Path, intensity: Float, scale: Float) {
            // Les froufrous intenses sont intégrés dans la génération Cattleya
        }
        
        /**
         * Ajoute des froufrous à une trompette
         */
        private fun addTrumpetRuffles(path: Path, centerX: Float, centerY: Float, width: Float, intensity: Float) {
            val ruffleCount = (intensity * 8).toInt()
            val angleStep = 180f / ruffleCount
            
            for (i in 0..ruffleCount) {
                val angle = -90f + angleStep * i
                val ruffleSize = width * 0.1f * intensity
                val x = centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * (width/2 + ruffleSize)
                val y = centerY + sin(Math.toRadians(angle.toDouble())).toFloat() * ruffleSize
                
                if (i == 0) {
                    path.lineTo(x, y)
                } else {
                    path.quadTo(centerX + cos(Math.toRadians(angle.toDouble())).toFloat() * width/2, centerY, x, y)
                }
            }
        }
        
        /**
         * Ajoute des froufrous à une jupe de danseuse
         */
        private fun addSkirtRuffles(
            path: Path, 
            startX: Float, 
            startY: Float, 
            endX: Float, 
            endY: Float, 
            fullness: Float
        ) {
            val steps = (fullness * 6).toInt()
            val stepX = (endX - startX) / steps
            val stepY = (endY - startY) / steps
            
            for (i in 1..steps) {
                val x = startX + stepX * i
                val y = startY + stepY * i
                val ruffleSize = fullness * 10f * sin(i * PI_F / 2)
                
                path.quadTo(x + ruffleSize, y - ruffleSize, x, y)
            }
        }
        
        /**
         * Ajoute un arrondi subtil aux angles
         */
        private fun addSubtleRounding(path: Path, radius: Float) {
            // Android Path ne permet pas facilement d'arrondir un path existant
            // Cette fonction pourrait être implémentée avec des opérations path plus complexes
        }
    }
}

// ==================== CLASSE DE GÉOMÉTRIE FINALE ====================

/**
 * Contient toute la géométrie d'une fleur d'orchidée
 */
data class OrchideeFlowerGeometry(
    val dorsalSepal: Path,           // Sépale dorsal (arrière)
    val lateralSepals: List<Path>,   // Sépales latéraux (côtés)
    val petals: List<Path>,          // Pétales principaux
    val labelle: Path,               // Labelle (lèvre de l'orchidée)
    val column: Path,                // Colonne centrale
    val centerPoint: PointF,         // Point central de la fleur
    val boundingRadius: Float        // Rayon de délimitation
) {
    
    /**
     * Retourne tous les paths dans l'ordre de rendu (arrière vers avant)
     */
    fun getAllPathsInRenderOrder(): List<Pair<String, Path>> {
        return listOf(
            "dorsalSepal" to dorsalSepal,
            *lateralSepals.mapIndexed { index, path -> "lateralSepal$index" to path }.toTypedArray(),
            *petals.mapIndexed { index, path -> "petal$index" to path }.toTypedArray(),
            "labelle" to labelle,
            "column" to column
        )
    }
    
    /**
     * Applique une transformation à toute la géométrie
     */
    fun transform(matrix: android.graphics.Matrix): OrchideeFlowerGeometry {
        val transformedDorsalSepal = Path().apply { dorsalSepal.transform(matrix, this) }
        val transformedLateralSepals = lateralSepals.map { path ->
            Path().apply { path.transform(matrix, this) }
        }
        val transformedPetals = petals.map { path ->
            Path().apply { path.transform(matrix, this) }
        }
        val transformedLabelle = Path().apply { labelle.transform(matrix, this) }
        val transformedColumn = Path().apply { column.transform(matrix, this) }
        
        val transformedCenter = floatArrayOf(centerPoint.x, centerPoint.y)
        matrix.mapPoints(transformedCenter)
        
        return OrchideeFlowerGeometry(
            dorsalSepal = transformedDorsalSepal,
            lateralSepals = transformedLateralSepals,
            petals = transformedPetals,
            labelle = transformedLabelle,
            column = transformedColumn,
            centerPoint = PointF(transformedCenter[0], transformedCenter[1]),
            boundingRadius = boundingRadius // Le rayon peut être affecté par l'échelle dans la matrice
        )
    }
}
