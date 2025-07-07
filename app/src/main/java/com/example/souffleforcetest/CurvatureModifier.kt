package com.example.souffleforcetest

import kotlin.math.*

class CurvatureModifier(private val plantStem: PlantStem) {
    
    // ==================== DATA CLASSES ====================
    
    data class CurvatureProfile(
        val stemIndex: Int,             // -1 = principale, 0+ = branches
        val baseDirection: Float,       // Direction de base (-1 à 1)
        val frequencyInfluence: Float,  // Influence des fréquences (0 à 1)
        val curvatureStrength: Float,   // Force de la courbure
        val segments: MutableList<SegmentCurvature> = mutableListOf()
    )
    
    data class SegmentCurvature(
        val segmentIndex: Int,          // Index du segment sur la tige
        val naturalCurve: Float,        // Courbure naturelle existante
        val frequencyModifier: Float,   // Modification par fréquence
        val finalCurve: Float          // Courbure finale appliquée
    )
    
    data class FrequencyEffect(
        val frequency: Float,           // Fréquence analysée
        val curvatureDirection: Float,  // Direction de courbure (-1 à 1)
        val intensity: Float,           // Intensité de l'effet (0 à 1)
        val smoothingFactor: Float      // Facteur de lissage
    )
    
    // ==================== VARIABLES ====================
    
    private val stemProfiles = mutableMapOf<Int, CurvatureProfile>()
    private val frequencyHistory = mutableListOf<Float>()
    private var lastFrequency = 440f
    private var baselineFrequency = 440f
    
    // ==================== PARAMÈTRES ====================
    
    private val frequencySmoothing = 0.8f       // Lissage des changements
    private val curvatureAmplification = 1.5f   // Amplification des effets
    private val maxCurvatureInfluence = 0.3f    // Influence max des fréquences
    private val historySize = 30                // Taille historique fréquences
    
    // Mapping fréquence → courbure
    private val lowFreqThreshold = 300f         // Seuil grave (ooo)
    private val highFreqThreshold = 600f        // Seuil aigu (iii)
    private val neutralFreq = 450f              // Fréquence neutre
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun initializeStem(stemIndex: Int, baseDirection: Float = 0f) {
        val profile = CurvatureProfile(
            stemIndex = stemIndex,
            baseDirection = baseDirection,
            frequencyInfluence = if (stemIndex == -1) 0.7f else 0.5f, // Principale plus sensible
            curvatureStrength = if (stemIndex == -1) 1.0f else 0.8f
        )
        stemProfiles[stemIndex] = profile
    }
    
    fun applyFrequencyEffect(
        stemIndex: Int, 
        frequency: Float, 
        trend: BreathAnalyzer.FrequencyTrend,
        force: Float
    ) {
        // Mettre à jour l'historique des fréquences
        updateFrequencyHistory(frequency)
        
        // Calculer l'effet de la fréquence
        val effect = calculateFrequencyEffect(frequency, trend, force)
        
        // Appliquer aux segments de la tige
        applyCurvatureToStem(stemIndex, effect)
    }
    
    fun updateAllStems(
        frequency: Float, 
        trend: BreathAnalyzer.FrequencyTrend, 
        force: Float
    ) {
        for (stemIndex in stemProfiles.keys) {
            applyFrequencyEffect(stemIndex, frequency, trend, force)
        }
    }
    
    fun reset() {
        stemProfiles.clear()
        frequencyHistory.clear()
        lastFrequency = 440f
        baselineFrequency = 440f
    }
    
    fun getStemCurvature(stemIndex: Int): Float {
        val profile = stemProfiles[stemIndex] ?: return 0f
        return profile.segments.lastOrNull()?.finalCurve ?: 0f
    }
    
    // ==================== FONCTIONS PRIVÉES ====================
    
    private fun updateFrequencyHistory(frequency: Float) {
        val smoothedFreq = lastFrequency * frequencySmoothing + frequency * (1f - frequencySmoothing)
        lastFrequency = smoothedFreq
        
        frequencyHistory.add(smoothedFreq)
        if (frequencyHistory.size > historySize) {
            frequencyHistory.removeAt(0)
        }
        
        // Calibrer la fréquence de base
        if (frequencyHistory.size >= 10) {
            baselineFrequency = frequencyHistory.take(10).average().toFloat()
        }
    }
    
    private fun calculateFrequencyEffect(
        frequency: Float, 
        trend: BreathAnalyzer.FrequencyTrend, 
        force: Float
    ): FrequencyEffect {
        // Calculer la direction de courbure selon la fréquence
        val direction = when {
            frequency < lowFreqThreshold -> {
                // Grave (ooo) → courbure vers la droite/bas
                val graveFactor = (lowFreqThreshold - frequency) / lowFreqThreshold
                graveFactor.coerceIn(0f, 1f)
            }
            frequency > highFreqThreshold -> {
                // Aigu (iii) → courbure vers la gauche/haut  
                val aiguFactor = (frequency - highFreqThreshold) / (800f - highFreqThreshold)
                -aiguFactor.coerceIn(0f, 1f)
            }
            else -> {
                // Neutre → pas de courbure supplémentaire
                0f
            }
        }
        
        // Calculer l'intensité selon la tendance et la force
        val trendMultiplier = when (trend) {
            BreathAnalyzer.FrequencyTrend.STABLE -> 0.5f
            BreathAnalyzer.FrequencyTrend.RISING -> 0.8f    // iii → plus d'effet
            BreathAnalyzer.FrequencyTrend.FALLING -> 0.9f   // ooo → plus d'effet
            BreathAnalyzer.FrequencyTrend.OSCILLATING -> 0.3f
        }
        
        val forceMultiplier = (force / 1f).coerceIn(0.2f, 1f)
        val intensity = trendMultiplier * forceMultiplier * curvatureAmplification
        
        // Facteur de lissage selon la stabilité
        val smoothing = when (trend) {
            BreathAnalyzer.FrequencyTrend.STABLE -> 0.9f
            BreathAnalyzer.FrequencyTrend.OSCILLATING -> 0.5f
            else -> 0.7f
        }
        
        return FrequencyEffect(
            frequency = frequency,
            curvatureDirection = direction,
            intensity = intensity.coerceIn(0f, maxCurvatureInfluence),
            smoothingFactor = smoothing
        )
    }
    
    private fun applyCurvatureToStem(stemIndex: Int, effect: FrequencyEffect) {
        val profile = stemProfiles[stemIndex] ?: return
        
        // Obtenir les points de la tige
        val stemPoints = when (stemIndex) {
            -1 -> plantStem.mainStem
            else -> {
                if (stemIndex < plantStem.branches.size) {
                    plantStem.branches[stemIndex].points
                } else emptyList()
            }
        }
        
        if (stemPoints.isEmpty()) return
        
        // Appliquer la courbure aux segments
        for (i in stemPoints.indices) {
            val point = stemPoints[i]
            val heightRatio = i.toFloat() / stemPoints.size.toFloat()
            
            // Calculer la courbure naturelle existante
            val naturalCurve = point.permanentWave
            
            // Calculer la modification par fréquence
            val heightEffect = calculateHeightEffect(heightRatio)
            val frequencyModifier = effect.curvatureDirection * effect.intensity * heightEffect * profile.frequencyInfluence
            
            // Lisser avec la courbure précédente
            val currentSegment = profile.segments.find { it.segmentIndex == i }
            val smoothedModifier = if (currentSegment != null) {
                currentSegment.frequencyModifier * effect.smoothingFactor + 
                frequencyModifier * (1f - effect.smoothingFactor)
            } else {
                frequencyModifier
            }
            
            // Combiner avec la courbure naturelle
            val finalCurve = naturalCurve + smoothedModifier * 15f // Amplification visuelle
            
            // Limiter les courbures excessives
            val limitedCurve = finalCurve.coerceIn(-30f, 30f)
            
            // Mettre à jour ou créer le segment
            val segmentIndex = profile.segments.indexOfFirst { it.segmentIndex == i }
            val newSegment = SegmentCurvature(
                segmentIndex = i,
                naturalCurve = naturalCurve,
                frequencyModifier = smoothedModifier,
                finalCurve = limitedCurve
            )
            
            if (segmentIndex >= 0) {
                profile.segments[segmentIndex] = newSegment
            } else {
                profile.segments.add(newSegment)
            }
            
            // Appliquer la courbure au point (modification directe)
            applyToStemPoint(stemIndex, i, limitedCurve - naturalCurve)
        }
    }
    
    private fun calculateHeightEffect(heightRatio: Float): Float {
        // Plus d'effet vers le sommet, moins à la base
        return heightRatio * heightRatio * 0.8f + 0.2f
    }
    
    private fun applyToStemPoint(stemIndex: Int, pointIndex: Int, additionalCurve: Float) {
        try {
            when (stemIndex) {
                -1 -> {
                    // Tige principale
                    if (pointIndex < plantStem.mainStem.size) {
                        val point = plantStem.mainStem[pointIndex]
                        val newPoint = PlantStem.StemPoint(
                            point.x,
                            point.y,
                            point.thickness,
                            point.oscillation,
                            point.permanentWave + additionalCurve * 0.3f // Application douce
                        )
                        plantStem.mainStem[pointIndex] = newPoint
                    }
                }
                else -> {
                    // Branches
                    if (stemIndex < plantStem.branches.size) {
                        val branch = plantStem.branches[stemIndex]
                        if (pointIndex < branch.points.size) {
                            val point = branch.points[pointIndex]
                            val newPoint = PlantStem.StemPoint(
                                point.x,
                                point.y,
                                point.thickness,
                                point.oscillation,
                                point.permanentWave + additionalCurve * 0.2f // Plus doux sur branches
                            )
                            branch.points[pointIndex] = newPoint
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignorer les erreurs d'accès (points en cours de création)
        }
    }
    
    // ==================== UTILITAIRES ====================
    
    fun getFrequencyRange(): Pair<Float, Float> {
        if (frequencyHistory.size < 5) return Pair(neutralFreq, neutralFreq)
        return Pair(
            frequencyHistory.minOrNull() ?: neutralFreq,
            frequencyHistory.maxOrNull() ?: neutralFreq
        )
    }
    
    fun getCurrentFrequencyDeviation(): Float {
        return abs(lastFrequency - baselineFrequency)
    }
    
    fun getEffectStrength(stemIndex: Int): Float {
        val profile = stemProfiles[stemIndex] ?: return 0f
        return profile.segments.maxOfOrNull { abs(it.frequencyModifier) } ?: 0f
    }
    
    fun debugInfo(): String {
        val freq = "Freq: %.0f Hz".format(lastFrequency)
        val deviation = "Dev: %.0f Hz".format(getCurrentFrequencyDeviation())
        val stemsCount = stemProfiles.size
        
        return "$freq, $deviation, Tiges: $stemsCount"
    }
    
    fun getStemProfile(stemIndex: Int): CurvatureProfile? {
        return stemProfiles[stemIndex]
    }
}
