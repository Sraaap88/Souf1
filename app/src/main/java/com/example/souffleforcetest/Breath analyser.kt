package com.example.souffleforcetest

import kotlin.math.*

class BreathAnalyzer {
    
    // ==================== DATA CLASSES ====================
    
    data class BreathEvent(
        val timestamp: Long,
        val force: Float,
        val frequency: Float,
        val eventType: BreathEventType
    )
    
    enum class BreathEventType {
        BREATH_START,    // Début d'un souffle
        BREATH_PEAK,     // Pic de force
        BREATH_END,      // Fin d'un souffle
        SILENCE          // Pause entre souffles
    }
    
    data class BreathAnalysis(
        val breathCount: Int,           // Nombre de saccades détectées
        val avgForce: Float,            // Force moyenne globale
        val avgFrequency: Float,        // Fréquence moyenne globale
        val frequencyTrend: FrequencyTrend, // Tendance fréquentielle
        val isStable: Boolean           // Stabilité du souffle
    )
    
    enum class FrequencyTrend {
        STABLE,          // Fréquence constante
        RISING,          // Grave vers aigu (ooo → iii)
        FALLING,         // Aigu vers grave (iii → ooo)
        OSCILLATING      // Variations multiples
    }
    
    // ==================== VARIABLES ====================
    
    private val forceHistory = mutableListOf<Float>()
    private val frequencyHistory = mutableListOf<Float>()
    private val eventHistory = mutableListOf<BreathEvent>()
    
    private var lastBreathTime = 0L
    private var currentBreathStartTime = 0L
    private var isInBreath = false
    private var breathCount = 0
    
    // ==================== PARAMÈTRES ====================
    
    private val forceThreshold = 0.15f      // Seuil pour détecter un souffle
    private val silenceThreshold = 300L     // 300ms de silence = nouvelle saccade
    private val minBreathDuration = 150L    // Souffle minimum 150ms
    private val maxHistorySize = 90         // 3 secondes d'historique à 30 FPS
    private val frequencySmoothing = 0.7f   // Lissage des fréquences
    
    // Simulation FFT simple basée sur la force et les variations
    private var lastFrequency = 440f        // Fréquence de base (La)
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun analyzeBreath(force: Float, currentTime: Long): BreathAnalysis {
        // Simuler l'analyse de fréquence basée sur les variations de force
        val frequency = estimateFrequency(force)
        
        // Mettre à jour les historiques
        updateHistories(force, frequency)
        
        // Détecter les événements de souffle
        detectBreathEvents(force, frequency, currentTime)
        
        // Analyser les tendances
        return generateAnalysis()
    }
    
    fun reset() {
        forceHistory.clear()
        frequencyHistory.clear()
        eventHistory.clear()
        lastBreathTime = 0L
        currentBreathStartTime = 0L
        isInBreath = false
        breathCount = 0
        lastFrequency = 440f
    }
    
    fun getBreathCount(): Int = breathCount
    
    fun getLastEvent(): BreathEvent? = eventHistory.lastOrNull()
    
    fun getCurrentFrequencyTrend(): FrequencyTrend {
        if (frequencyHistory.size < 10) return FrequencyTrend.STABLE
        
        val recent = frequencyHistory.takeLast(10)
        val start = recent.take(3).average()
        val end = recent.takeLast(3).average()
        val diff = end - start
        
        return when {
            abs(diff) < 20f -> FrequencyTrend.STABLE
            diff > 20f -> FrequencyTrend.RISING
            diff < -20f -> FrequencyTrend.FALLING
            else -> FrequencyTrend.OSCILLATING
        }
    }
    
    // ==================== FONCTIONS PRIVÉES ====================
    
    private fun estimateFrequency(force: Float): Float {
        // Simulation simple de FFT basée sur les variations de force
        // Dans un vrai système, ici on ferait une vraie analyse spectrale
        
        if (forceHistory.size < 5) return lastFrequency
        
        // Calculer la variation de force (simule les oscillations vocales)
        val recent = forceHistory.takeLast(5)
        val variation = recent.zipWithNext { a, b -> abs(b - a) }.average().toFloat()
        
        // Mapper variation → fréquence (plus de variation = plus aigu)
        val baseFreq = 200f     // Fréquence grave de base
        val maxFreq = 800f      // Fréquence aiguë maximum
        val variationFactor = (variation * 10f).coerceIn(0f, 1f)
        
        val targetFrequency = baseFreq + (maxFreq - baseFreq) * variationFactor
        
        // Lissage pour éviter les sauts brusques
        lastFrequency = lastFrequency * frequencySmoothing + targetFrequency * (1f - frequencySmoothing)
        
        return lastFrequency
    }
    
    private fun updateHistories(force: Float, frequency: Float) {
        forceHistory.add(force)
        frequencyHistory.add(frequency)
        
        // Limiter la taille des historiques
        if (forceHistory.size > maxHistorySize) {
            forceHistory.removeAt(0)
        }
        if (frequencyHistory.size > maxHistorySize) {
            frequencyHistory.removeAt(0)
        }
    }
    
    private fun detectBreathEvents(force: Float, frequency: Float, currentTime: Long) {
        val wasInBreath = isInBreath
        val isCurrentlyBreathing = force > forceThreshold
        
        // Détection début de souffle
        if (!wasInBreath && isCurrentlyBreathing) {
            isInBreath = true
            currentBreathStartTime = currentTime
            
            // Vérifier si c'est une nouvelle saccade (après une pause)
            if (currentTime - lastBreathTime > silenceThreshold) {
                breathCount++
                addEvent(BreathEventType.BREATH_START, force, frequency, currentTime)
            }
        }
        
        // Détection fin de souffle
        else if (wasInBreath && !isCurrentlyBreathing) {
            // Vérifier durée minimum du souffle
            if (currentTime - currentBreathStartTime > minBreathDuration) {
                isInBreath = false
                lastBreathTime = currentTime
                addEvent(BreathEventType.BREATH_END, force, frequency, currentTime)
            }
        }
        
        // Détection des pics pendant le souffle
        else if (isInBreath && force > forceThreshold * 2f) {
            val lastEvent = eventHistory.lastOrNull()
            if (lastEvent?.eventType != BreathEventType.BREATH_PEAK || 
                currentTime - lastEvent.timestamp > 200L) {
                addEvent(BreathEventType.BREATH_PEAK, force, frequency, currentTime)
            }
        }
    }
    
    private fun addEvent(type: BreathEventType, force: Float, frequency: Float, timestamp: Long) {
        val event = BreathEvent(timestamp, force, frequency, type)
        eventHistory.add(event)
        
        // Limiter l'historique des événements
        if (eventHistory.size > 20) {
            eventHistory.removeAt(0)
        }
    }
    
    private fun generateAnalysis(): BreathAnalysis {
        val avgForce = if (forceHistory.isNotEmpty()) {
            forceHistory.average().toFloat()
        } else 0f
        
        val avgFrequency = if (frequencyHistory.isNotEmpty()) {
            frequencyHistory.average().toFloat()
        } else 440f
        
        val frequencyTrend = getCurrentFrequencyTrend()
        
        // Évaluer la stabilité (peu de variation dans la force)
        val isStable = if (forceHistory.size >= 10) {
            val recent = forceHistory.takeLast(10)
            val variance = recent.map { (it - avgForce) * (it - avgForce) }.average()
            variance < 0.1f // Seuil de stabilité
        } else true
        
        return BreathAnalysis(
            breathCount = breathCount,
            avgForce = avgForce,
            avgFrequency = avgFrequency,
            frequencyTrend = frequencyTrend,
            isStable = isStable
        )
    }
    
    // ==================== UTILITAIRES ====================
    
    fun getForceStability(): Float {
        if (forceHistory.size < 10) return 1f
        
        val recent = forceHistory.takeLast(10)
        val avg = recent.average().toFloat()
        val maxVariation = recent.maxOf { abs(it - avg) }
        
        return (1f - (maxVariation / 0.5f)).coerceIn(0f, 1f)
    }
    
    fun getFrequencyRange(): Pair<Float, Float> {
        if (frequencyHistory.size < 5) return Pair(440f, 440f)
        
        val recent = frequencyHistory.takeLast(20)
        return Pair(recent.minOrNull() ?: 440f, recent.maxOrNull() ?: 440f)
    }
    
    fun debugInfo(): String {
        return "Saccades: $breathCount, Force: ${forceHistory.lastOrNull()?.let { "%.2f".format(it) } ?: "0.00"}, " +
               "Freq: ${frequencyHistory.lastOrNull()?.let { "%.0f Hz".format(it) } ?: "440 Hz"}, " +
               "Trend: ${getCurrentFrequencyTrend()}"
    }
}
