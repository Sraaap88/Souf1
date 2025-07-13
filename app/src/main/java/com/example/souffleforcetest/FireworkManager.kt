package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import kotlin.math.*
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float,
    var maxLife: Float,
    val color: Int,
    var size: Float
)

class FireworkManager(private val screenWidth: Int, private val screenHeight: Int) {
    
    private val particles = mutableListOf<Particle>()
    private var isActive = false
    private var duration = 0L
    private val maxDuration = 3000L // 3 secondes
    
    private val sparkleColors = intArrayOf(
        Color.rgb(255, 215, 0),   // Or
        Color.rgb(255, 20, 147),  // Rose vif
        Color.rgb(0, 191, 255),   // Bleu ciel
        Color.rgb(50, 205, 50),   // Vert lime
        Color.rgb(255, 69, 0),    // Rouge orange
        Color.rgb(138, 43, 226),  // Violet
        Color.rgb(255, 255, 255), // Blanc
        Color.rgb(255, 192, 203)  // Rose pâle
    )
    
    fun startFirework() {
        isActive = true
        duration = 0L
        particles.clear()
        
        // Créer plusieurs explosions de paillettes
        for (i in 0..4) {
            val delay = i * 600L // Explosions échelonnées
            createBurst(
                Random.nextFloat() * screenWidth,
                screenHeight * (0.3f + Random.nextFloat() * 0.4f),
                delay
            )
        }
    }
    
    private fun createBurst(centerX: Float, centerY: Float, delay: Long = 0L) {
        val particleCount = 25 + Random.nextInt(15) // 25-40 particules par explosion
        
        for (i in 0 until particleCount) {
            val angle = Random.nextFloat() * 2 * PI
            val speed = 150f + Random.nextFloat() * 200f
            val vx = cos(angle).toFloat() * speed
            val vy = sin(angle).toFloat() * speed - 50f // Légère poussée vers le haut
            
            val particle = Particle(
                x = centerX,
                y = centerY,
                vx = vx,
                vy = vy,
                life = 1f,
                maxLife = 1.5f + Random.nextFloat() * 1f, // Durée variable
                color = sparkleColors[Random.nextInt(sparkleColors.size)],
                size = 8f + Random.nextFloat() * 12f
            )
            
            particles.add(particle)
        }
    }
    
    fun update(deltaTime: Float) {
        if (!isActive) return
        
        duration += (deltaTime * 1000).toLong()
        
        // Mettre à jour les particules
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val particle = iterator.next()
            
            // Physique
            particle.x += particle.vx * deltaTime
            particle.y += particle.vy * deltaTime
            particle.vy += 300f * deltaTime // Gravité
            
            // Réduction de la vitesse (friction air)
            particle.vx *= 0.98f
            particle.vy *= 0.98f
            
            // Durée de vie
            particle.life -= deltaTime / particle.maxLife
            
            // Supprimer si mort ou hors écran
            if (particle.life <= 0f || 
                particle.x < -50f || particle.x > screenWidth + 50f ||
                particle.y > screenHeight + 50f) {
                iterator.remove()
            }
        }
        
        // Ajouter de nouveaux bursts pendant la durée
        if (duration % 800L < 50L && duration < maxDuration) {
            createBurst(
                Random.nextFloat() * screenWidth,
                screenHeight * (0.2f + Random.nextFloat() * 0.5f)
            )
        }
        
        // Arrêter après la durée maximale
        if (duration >= maxDuration && particles.isEmpty()) {
            isActive = false
        }
    }
    
    fun draw(canvas: Canvas, paint: Paint) {
        if (!isActive || particles.isEmpty()) return
        
        paint.style = Paint.Style.FILL
        
        for (particle in particles) {
            // Alpha basé sur la durée de vie
            val alpha = (particle.life * 255).toInt().coerceIn(0, 255)
            val colorWithAlpha = Color.argb(
                alpha,
                Color.red(particle.color),
                Color.green(particle.color),
                Color.blue(particle.color)
            )
            
            paint.color = colorWithAlpha
            
            // Taille qui diminue avec le temps
            val currentSize = particle.size * particle.life
            
            // Dessiner la particule avec effet de scintillement
            canvas.drawCircle(particle.x, particle.y, currentSize, paint)
            
            // Effet de halo pour plus de brillance
            if (particle.life > 0.7f) {
                val haloAlpha = ((particle.life - 0.7f) * 3f * 100f).toInt().coerceIn(0, 100)
                val haloColor = Color.argb(
                    haloAlpha,
                    Color.red(particle.color),
                    Color.green(particle.color),
                    Color.blue(particle.color)
                )
                paint.color = haloColor
                canvas.drawCircle(particle.x, particle.y, currentSize * 2f, paint)
            }
        }
    }
    
    fun isPlaying(): Boolean = isActive
    
    fun stop() {
        isActive = false
        particles.clear()
    }
}
