package com.example.souffleforcetest

import android.graphics.Path
import kotlin.math.*
 
class LeafRenderer(private val plantStem: PlantStem) {
    
    // ==================== PARAMÈTRES DE RENDU ====================
    
    private val growthRate = 600f
    private val oscillationDecay = 0.96f
    private val forceThreshold = 0.25f
    
    // ==================== FONCTIONS DE RENDU ====================
    
    fun createRealisticLeafPath(path: Path, x: Float, y: Float, size: Float, personality: PlantLeavesManager.LeafPersonality, angle: Float): Path {
        // Forme unique spatulée pour toutes les feuilles avec extrémité arrondie ET courbure gravitationnelle
        val length = size * 1.4f
        val maxWidth = size * 0.4f
        
        val points = mutableListOf<Pair<Float, Float>>()
        val lobeCount = 6 + (personality.teethCount % 4)
        
        // Calculer la courbure gravitationnelle (plus la feuille est grande, plus elle se plie)
        val gravityEffect = (size / 100f).coerceAtMost(1.2f) // Effet proportionnel à la taille
        
        // Créer la forme spatulée réaliste avec extrémité arrondie ET arc gravitationnel
        for (i in 0..lobeCount) {
            val t = i.toFloat() / lobeCount
            
            // Forme spatulée : étroite à la base, large au sommet avec extrémité arrondie
            val widthAtT = if (t > 0.85f) {
                // Extrémité arrondie : réduction progressive de la largeur
                val tipT = (t - 0.85f) / 0.15f // 0 à 1 pour les derniers 15%
                val roundingFactor = 1f - tipT * tipT // Courbe quadratique pour arrondir
                maxWidth * (0.15f + 0.85f * 0.85f * 0.85f) * roundingFactor
            } else {
                maxWidth * (0.15f + 0.85f * t * t)
            }
            
            // Position Y avec courbure gravitationnelle progressive
            var yPos = -length * t
            
            // COURBURE EN ARC : la feuille se plie sous la gravité
            // Base (t=0) : pas de courbure
            // Milieu (t=0.5) : courbure modérée
            // Extrémité (t=1) : courbure maximale
            if (t > 0.1f) { // Garder la base droite
                val curveFactor = (t - 0.1f) / 0.9f // 0 à 1 pour la partie qui se courbe
                val gravityCurve = curveFactor * curveFactor * gravityEffect * 15f // Arc progressif
                yPos += gravityCurve // Décalage vers le bas (courbure)
            }
            
            // Lobes arrondis sur les côtés + 4 petites ondulations à l'extrémité
            val baseX = -widthAtT * 0.5f
            val lobePhase = t * PI * 3.5f
            var lobeDepth = personality.teethDepth * widthAtT * 0.5f * sin(lobePhase).toFloat()
            
            // Ajouter 4 petites ondulations à l'extrémité (85% à 100%)
            if (t > 0.85f) {
                val tipT = (t - 0.85f) / 0.15f
                val tipUndulation = sin(tipT * PI * 8).toFloat() * widthAtT * 0.12f // 4 ondulations (8 = 4*2)
                lobeDepth += tipUndulation
            }
            
            val leftX = baseX + lobeDepth
            points.add(Pair(leftX, yPos))
        }
        
        // Côté droit avec asymétrie naturelle + ondulations symétriques + même courbure
        for (i in lobeCount downTo 0) {
            val t = i.toFloat() / lobeCount
            
            val widthAtT = if (t > 0.85f) {
                val tipT = (t - 0.85f) / 0.15f
                val roundingFactor = 1f - tipT * tipT
                maxWidth * (0.15f + 0.85f * 0.85f * 0.85f) * roundingFactor
            } else {
                maxWidth * (0.15f + 0.85f * t * t)
            }
            
            // Position Y avec même courbure gravitationnelle
            var yPos = -length * t
            if (t > 0.1f) {
                val curveFactor = (t - 0.1f) / 0.9f
                val gravityCurve = curveFactor * curveFactor * gravityEffect * 15f
                yPos += gravityCurve
            }
            
            val baseX = widthAtT * 0.5f
            val lobePhase = t * PI * 3.5f
            var lobeDepth = personality.teethDepth * widthAtT * 0.5f * sin(lobePhase).toFloat()
            
            // Ondulations à l'extrémité côté droit (4 ondulations)
            if (t > 0.85f) {
                val tipT = (t - 0.85f) / 0.15f
                val tipUndulation = sin(tipT * PI * 8).toFloat() * widthAtT * 0.12f // 4 ondulations
                lobeDepth += tipUndulation
            }
            
            val asymmetryOffset = personality.asymmetry * widthAtT * t
            val rightX = baseX - lobeDepth + asymmetryOffset
            
            points.add(Pair(rightX, yPos))
        }
        
        // Rotation et construction du path
        val rad = Math.toRadians(angle.toDouble())
        val cos = cos(rad).toFloat()
        val sin = sin(rad).toFloat()
        
        if (points.isNotEmpty()) {
            val firstPoint = rotatePoint(points[0].first, points[0].second, cos, sin)
            path.moveTo(x + firstPoint.first, y + firstPoint.second)
            
            for (i in 1 until points.size) {
                val rotatedPoint = rotatePoint(points[i].first, points[i].second, cos, sin)
                path.lineTo(x + rotatedPoint.first, y + rotatedPoint.second)
            }
            path.close()
        }
        
        return path
    }
    
    fun calculateRealisticLeafAngle(leaf: PlantLeavesManager.Leaf): Float {
        val maturity = leaf.currentSize / leaf.maxSize
        val heightRatio = (plantStem.getStemBaseY() - leaf.y) / plantStem.getMaxPossibleHeight()
        
        // Commencer vertical (0°), puis s'écarter avec effet poids
        val baseAngle = 0f // Toutes commencent verticales
        
        // Écartement progressif selon la maturité
        val spreadAngle = if (leaf.side) 45f else -45f
        val maturitySpread = spreadAngle * maturity * maturity // Progression quadratique
        
        // Effet poids : les feuilles retombent
        val weightEffect = maturity * maturity * 30f // Plus matures = plus lourdes
        val finalWeight = if (heightRatio < 0.3f) weightEffect * 1.5f else weightEffect // Feuilles basses plus lourdes
        
        return baseAngle + maturitySpread + finalWeight
    }
    
    fun growExistingLeaves(leaves: MutableList<PlantLeavesManager.Leaf>, force: Float, lastForce: Float) {
        for (leaf in leaves) {
            if (leaf.currentSize < leaf.maxSize && force > forceThreshold) {
                val forceStability = 1f - abs(force - lastForce).coerceAtMost(0.5f) * 2f
                val qualityMultiplier = 0.5f + forceStability * 0.5f
                
                val growthProgress = leaf.currentSize / leaf.maxSize
                val progressCurve = 1f - growthProgress * growthProgress
                val adjustedGrowth = force * qualityMultiplier * progressCurve * growthRate * 0.008f
                
                leaf.currentSize = (leaf.currentSize + adjustedGrowth).coerceAtMost(leaf.maxSize)
                
                // Oscillation lors de la croissance
                val forceVariation = abs(force - lastForce) * 2f
                leaf.oscillation = sin(System.currentTimeMillis() * 0.003f) * forceVariation * 2f
            }
        }
    }
    
    fun updateLeafPhysics(leaves: MutableList<PlantLeavesManager.Leaf>) {
        for (leaf in leaves) {
            // Oscillation temporaire réduite
            leaf.oscillation *= oscillationDecay
            
            // Courbure permanente par poids (comme les tiges)
            val maturity = leaf.currentSize / leaf.maxSize
            val weightStrength = maturity * maturity * 2f // Plus matures = plus lourdes
            
            // Direction de courbure selon le côté et la taille
            val weightDirection = if (leaf.side) 1f else -1f
            val weightInfluence = weightStrength * weightDirection * 0.015f
            
            // Transfert oscillation → courbure permanente
            if (abs(leaf.oscillation) > 0.3f) {
                val transferRate = 0.01f
                leaf.permanentCurve += leaf.oscillation * transferRate
                leaf.oscillation *= (1f - transferRate)
            }
            
            // Ajouter effet poids progressif
            leaf.permanentCurve += weightInfluence
            
            // Limites pour éviter courbures excessives
            leaf.oscillation = leaf.oscillation.coerceIn(-4f, 4f)
            leaf.permanentCurve = leaf.permanentCurve.coerceIn(-15f, 15f)
        }
    }
    
    fun generateLeafPersonality(): PlantLeavesManager.LeafPersonality {
        return PlantLeavesManager.LeafPersonality(
            teethDepth = 0.2f + (Math.random() * 0.15f).toFloat(),
            teethCount = 5 + (Math.random() * 4).toInt(),
            widthRatio = 0.35f + (Math.random() * 0.15f).toFloat(),
            curvature = (Math.random() * 0.3f - 0.15f).toFloat(),
            asymmetry = (Math.random() * 0.12f).toFloat(),
            colorVariation = (Math.random() * 0.2f).toFloat()
        )
    }
    
    // ==================== UTILITAIRES ====================
    
    private fun rotatePoint(x: Float, y: Float, cos: Float, sin: Float): Pair<Float, Float> {
        return Pair(
            x * cos - y * sin,
            x * sin + y * cos
        )
    }
}
