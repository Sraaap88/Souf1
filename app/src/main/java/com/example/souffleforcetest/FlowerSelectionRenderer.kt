package com.example.souffleforcetest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix

class FlowerSelectionRenderer(private val context: Context, private val screenWidth: Int, private val screenHeight: Int) {
    
    // ==================== PAINTS SPÉCIALISÉS ====================
    
    private val flowerTextPaint = Paint().apply {
        color = 0xFFFFFFFF.toInt()
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }
    
    // ==================== IMAGE RESOURCES ====================
    
    private var daisyBitmap: Bitmap? = null
    
    init {
        // Charger l'image de marguerite
        try {
            daisyBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.marguerite)
        } catch (e: Exception) {
            // Si l'image n'est pas trouvée, on garde daisyBitmap = null
        }
    }
    
    // ==================== SÉLECTION DES FLEURS ====================
    
    fun drawFlowerChoice(canvas: Canvas, challengeManager: ChallengeManager) {
        // Titre
        flowerTextPaint.textSize = 150f
        flowerTextPaint.color = 0xFFFFFFFF.toInt()
        flowerTextPaint.isFakeBoldText = true
        canvas.drawText("CHOISIR FLEUR", screenWidth / 2f, screenHeight * 0.25f, flowerTextPaint)
        
        // Obtenir les fleurs débloquées
        val unlockedFlowers = getUnlockedFlowersList(challengeManager)
        val flowerButtonRadius = screenWidth * 0.18f
        val centerX = screenWidth / 2f
        val buttonY = screenHeight / 2f
        
        when (unlockedFlowers.size) {
            1 -> {
                // Seulement marguerite - centrée
                drawFlowerButton(canvas, centerX, buttonY, flowerButtonRadius, "MARGUERITE", challengeManager)
            }
            2 -> {
                // Marguerite + Rose - côte à côte
                val spacing = flowerButtonRadius * 3.5f
                val margueriteX = centerX - spacing / 2f
                val roseX = centerX + spacing / 2f
                
                drawFlowerButton(canvas, margueriteX, buttonY, flowerButtonRadius, "MARGUERITE", challengeManager)
                drawFlowerButton(canvas, roseX, buttonY, flowerButtonRadius, "ROSE", challengeManager)
            }
            3 -> {
                // Marguerite + Rose + Lupin - en triangle
                val spacing = flowerButtonRadius * 3.0f
                val topY = buttonY - spacing * 0.4f
                val bottomY = buttonY + spacing * 0.4f
                
                // Marguerite en haut au centre
                drawFlowerButton(canvas, centerX, topY, flowerButtonRadius, "MARGUERITE", challengeManager)
                
                // Rose en bas à gauche
                val roseX = centerX - spacing / 2f
                drawFlowerButton(canvas, roseX, bottomY, flowerButtonRadius, "ROSE", challengeManager)
                
                // Lupin en bas à droite
                val lupinX = centerX + spacing / 2f
                drawFlowerButton(canvas, lupinX, bottomY, flowerButtonRadius, "LUPIN", challengeManager)
            }
            4 -> {
                // 4 fleurs exactement - en carré - COMME POUR LE CAS 3 !
                val spacing = flowerButtonRadius * 2.8f
                
                // Marguerite en haut à gauche
                val margueriteX = centerX - spacing / 2f
                val margueriteY = buttonY - spacing / 2f
                drawFlowerButton(canvas, margueriteX, margueriteY, flowerButtonRadius * 0.9f, "MARGUERITE", challengeManager)
                
                // Rose en haut à droite
                val roseX = centerX + spacing / 2f
                val roseY = buttonY - spacing / 2f
                drawFlowerButton(canvas, roseX, roseY, flowerButtonRadius * 0.9f, "ROSE", challengeManager)
                
                // Lupin en bas à gauche
                val lupinX = centerX - spacing / 2f
                val lupinY = buttonY + spacing / 2f
                drawFlowerButton(canvas, lupinX, lupinY, flowerButtonRadius * 0.9f, "LUPIN", challengeManager)
                
                // Iris en bas à droite
                val irisX = centerX + spacing / 2f
                val irisY = buttonY + spacing / 2f
                drawFlowerButton(canvas, irisX, irisY, flowerButtonRadius * 0.9f, "IRIS", challengeManager)
            }
            5 -> {
                // 5 fleurs avec orchidée - disposition spéciale (pour plus tard)
                val spacing = flowerButtonRadius * 2.5f
                
                // Top row: Marguerite, Rose, Lupin
                val topY = buttonY - spacing * 0.3f
                drawFlowerButton(canvas, centerX - spacing, topY, flowerButtonRadius * 0.8f, "MARGUERITE", challengeManager)
                drawFlowerButton(canvas, centerX, topY, flowerButtonRadius * 0.8f, "ROSE", challengeManager)
                drawFlowerButton(canvas, centerX + spacing, topY, flowerButtonRadius * 0.8f, "LUPIN", challengeManager)
                
                // Bottom row: Iris, Orchidée
                val bottomY = buttonY + spacing * 0.3f
                val bottomSpacing = spacing * 0.8f
                drawFlowerButton(canvas, centerX - bottomSpacing / 2f, bottomY, flowerButtonRadius * 0.8f, "IRIS", challengeManager)
                drawFlowerButton(canvas, centerX + bottomSpacing / 2f, bottomY, flowerButtonRadius * 0.8f, "ORCHIDEE", challengeManager)
            }
        }
    }
    
    private fun drawFlowerButton(canvas: Canvas, x: Float, y: Float, radius: Float, flowerType: String, challengeManager: ChallengeManager) {
        val isUnlocked = challengeManager.isFlowerUnlocked(flowerType)
        
        // SI PAS DÉBLOQUÉE = RIEN AFFICHER (comme avant)
        if (!isUnlocked && flowerType != "MARGUERITE") return
        
        when (flowerType) {
            "MARGUERITE" -> {
                // GARDE TA MARGUERITE !
                drawMiniDaisy(canvas, x, y, radius * 1.5f)
            }
            "ROSE" -> {
                flowerTextPaint.textSize = radius * 1.6f
                flowerTextPaint.color = 0xFFFF69B4.toInt()
                canvas.drawText("🌹", x, y + 15f, flowerTextPaint)
            }
            "LUPIN" -> {
                flowerTextPaint.textSize = radius * 1.6f
                flowerTextPaint.color = 0xFF9370DB.toInt()
                canvas.drawText("🌾", x, y + 15f, flowerTextPaint)
            }
            "IRIS" -> {
                flowerTextPaint.textSize = radius * 1.6f
                flowerTextPaint.color = 0xFF4B0082.toInt()
                canvas.drawText("🌷", x, y + 15f, flowerTextPaint)
            }
        }
    }
    
    private fun drawLockedFlower(canvas: Canvas, x: Float, y: Float, radius: Float, text: String) {
        // Cadenas
        flowerTextPaint.textSize = radius * 1.4f
        flowerTextPaint.color = 0xAA888888.toInt()  // Gris
        canvas.drawText("🔒", x, y + 15f, flowerTextPaint)
    }
    
    private fun getUnlockedFlowersList(challengeManager: ChallengeManager): List<String> {
        val flowers = mutableListOf("MARGUERITE")  // Toujours débloquée
        
        if (challengeManager.isFlowerUnlocked("ROSE")) {
            flowers.add("ROSE")
        }
        
        if (challengeManager.isFlowerUnlocked("LUPIN")) {
            flowers.add("LUPIN")
        }
        
        if (challengeManager.isFlowerUnlocked("IRIS")) {
            flowers.add("IRIS")
        }
        
        println("Fleurs débloquées: $flowers") // DEBUG
        return flowers
    }
    
    private fun drawMiniDaisy(canvas: Canvas, centerX: Float, centerY: Float, size: Float) {
        if (daisyBitmap != null) {
            // Utiliser l'image de marguerite
            val matrix = Matrix()
            val scale = size / maxOf(daisyBitmap!!.width, daisyBitmap!!.height)
            matrix.setScale(scale, scale)
            matrix.postTranslate(
                centerX - (daisyBitmap!!.width * scale) / 2f,
                centerY - (daisyBitmap!!.height * scale) / 2f
            )
            
            val paint = Paint().apply {
                isAntiAlias = true
                isFilterBitmap = true
            }
            canvas.drawBitmap(daisyBitmap!!, matrix, paint)
        } else {
            // Fallback si l'image n'est pas trouvée
            val centerPaint = Paint().apply {
                isAntiAlias = true
                color = Color.rgb(255, 200, 50)
                style = Paint.Style.FILL
            }
            canvas.drawCircle(centerX, centerY, size * 0.3f, centerPaint)
            
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = size * 0.2f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("IMG", centerX, centerY, textPaint)
        }
    }
}
