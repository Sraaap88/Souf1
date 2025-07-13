package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import kotlin.math.*
import kotlin.random.Random

data class RainDrop(
    var x: Float,
    var y: Float,
    var speed: Float,
    var length: Float,
    var alpha: Int,
    var width: Float
)

data class Splash(
    var x: Float,
    var y: Float,
    var life: Float,
    var maxLife: Float,
    var radius: Float
)

class RainManager(private val screenWidth: Int, private val screenHeight: Int) {
    
    private val raindrops = mutableListOf<RainDrop>()
    private val splashes = mutableListOf<Splash>()
    private var isActive = false
    private var duration = 0L
    private val maxDuration = 4000L // 4 secondes
    private var rainIntensity = 0f // 0.0 à 1.0
    
    // Couleurs de la pluie
    private val rainColor = Color.argb(180, 100, 120, 150) // Bleu-gris transparent
    private val splashColor = Color.argb(120, 150, 170, 200) // Bleu clair
    private val skyColor = Color.argb(0, 60, 60, 80) // Gris foncé (alpha progressif)
    
    fun startRain() {
        isActive = true
        duration = 0L
        rainIntensity = 0f
        raindrops.clear()
        splashes.clear()
        
        println("🌧️ La pluie commence à tomber...")
    }
    
    fun update(deltaTime: Float) {
        if (!isActive) return
        
        duration += (deltaTime * 1000).toLong()
        
        // Intensité progressive de la pluie
        rainIntensity = when {
            duration < 500L -> duration / 500f // Montée progressive (0.5s)
            duration < 3000L -> 1f // Pluie intense (2.5s)
            duration < maxDuration -> 1f - ((duration - 3000f) / 1000f) // Diminution (1s)
            else -> 0f
        }.coerceIn(0f, 1f)
        
        // Créer de nouvelles gouttes de pluie
        if (rainIntensity > 0f) {
            val dropsToCreate = (rainIntensity * 15f).toInt() // Jusqu'à 15 gouttes par frame
            repeat(dropsToCreate) {
                createRaindrop()
            }
        }
        
        // Mettre à jour les gouttes existantes
        updateRaindrops(deltaTime)
        
        // Mettre à jour les éclaboussures
        updateSplashes(deltaTime)
        
        // Arrêter après la durée maximale
        if (duration >= maxDuration && raindrops.isEmpty() && splashes.isEmpty()) {
            isActive = false
            println("🌧️ La pluie s'arrête...")
        }
    }
    
    private fun createRaindrop() {
        // Gouttes qui tombent de haut en bas avec variation
        val drop = RainDrop(
            x = Random.nextFloat() * (screenWidth + 200f) - 100f, // Déborde un peu
            y = -Random.nextFloat() * 100f, // Commence au-dessus de l'écran
            speed = 800f + Random.nextFloat() * 400f, // Vitesse variable
            length = 15f + Random.nextFloat() * 25f, // Longueur de la goutte
            alpha = (120 + Random.nextInt(100)).coerceAtMost(220), // Transparence variable
            width = 1.5f + Random.nextFloat() * 1f // Épaisseur légère
        )
        
        raindrops.add(drop)
    }
    
    private fun updateRaindrops(deltaTime: Float) {
        val iterator = raindrops.iterator()
        while (iterator.hasNext()) {
            val drop = iterator.next()
            
            // Mouvement de la goutte
            drop.y += drop.speed * deltaTime
            drop.x += Random.nextFloat() * 20f - 10f // Léger mouvement horizontal (vent)
            
            // Créer éclaboussure quand la goutte touche le sol
            if (drop.y >= screenHeight - 100f) { // CORRIGÉ: Sol aligné avec marguerite (screenHeight - 100f)
                if (Random.nextFloat() < 0.3f) { // 30% de chance d'éclaboussure
                    createSplash(drop.x, screenHeight - 100f) // CORRIGÉ: Même hauteur que la marguerite
                }
                iterator.remove()
            }
            // Supprimer si hors écran
            else if (drop.x < -50f || drop.x > screenWidth + 50f || drop.y > screenHeight + 50f) {
                iterator.remove()
            }
        }
    }
    
    private fun createSplash(x: Float, y: Float) {
        val splash = Splash(
            x = x,
            y = y,
            life = 1f,
            maxLife = 0.3f + Random.nextFloat() * 0.2f, // Durée courte
            radius = 2f + Random.nextFloat() * 4f
        )
        
        splashes.add(splash)
    }
    
    private fun updateSplashes(deltaTime: Float) {
        val iterator = splashes.iterator()
        while (iterator.hasNext()) {
            val splash = iterator.next()
            
            // Diminuer la durée de vie
            splash.life -= deltaTime / splash.maxLife
            
            // Expansion de l'éclaboussure
            splash.radius += 30f * deltaTime
            
            // Supprimer si terminée
            if (splash.life <= 0f) {
                iterator.remove()
            }
        }
    }
    
    fun draw(canvas: Canvas, paint: Paint) {
        if (!isActive) return
        
        // Dessiner l'assombrissement du ciel
        if (rainIntensity > 0.2f) {
            val skyAlpha = ((rainIntensity - 0.2f) * 60f).toInt().coerceAtMost(60)
            val skyOverlay = Color.argb(skyAlpha, 60, 60, 80)
            paint.color = skyOverlay
            paint.style = Paint.Style.FILL
            canvas.drawRect(0f, 0f, screenWidth.toFloat(), screenHeight.toFloat(), paint)
        }
        
        // Dessiner les gouttes de pluie
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND
        
        for (drop in raindrops) {
            val alpha = (drop.alpha * rainIntensity).toInt()
            paint.color = Color.argb(alpha, 100, 120, 150)
            paint.strokeWidth = drop.width
            
            // Dessiner la goutte comme une ligne
            canvas.drawLine(
                drop.x, drop.y,
                drop.x - 5f, drop.y - drop.length, // Légère inclinaison
                paint
            )
        }
        
        // Dessiner les éclaboussures
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        
        for (splash in splashes) {
            val alpha = (splash.life * 120f).toInt().coerceIn(0, 120)
            paint.color = Color.argb(alpha, 150, 170, 200)
            
            // Petit cercle d'éclaboussure
            canvas.drawCircle(splash.x, splash.y, splash.radius, paint)
            
            // Petites gouttelettes autour
            for (i in 0..2) {
                val angle = Random.nextFloat() * 2 * PI
                val distance = splash.radius * 0.5f
                val dropX = splash.x + cos(angle).toFloat() * distance
                val dropY = splash.y + sin(angle).toFloat() * distance
                canvas.drawCircle(dropX, dropY, 1f, paint)
            }
        }
    }
    
    fun isPlaying(): Boolean = isActive
    
    fun stop() {
        isActive = false
        raindrops.clear()
        splashes.clear()
    }
    
    // Getter pour savoir l'intensité de dissolution des plantes
    fun getDissolveProgress(): Float {
        return if (!isActive) 0f else {
            when {
                duration < 1000L -> 0f // CORRIGÉ: Pas de dissolution pendant 1 seconde (au lieu de 2 secondes)
                duration < 3000L -> (duration - 1000f) / 2000f // CORRIGÉ: Dissolution progressive sur 2 secondes
                else -> 1f // Dissolution complète
            }.coerceIn(0f, 1f)
        }
    }
}
