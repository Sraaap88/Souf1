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
                // 4 fleurs exactement - en carré
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
            else -> { // ✅ MODIFIÉ: Configuration 5+ fleurs avec ORCHIDÉE
                // 5+ fleurs avec orchidée - disposition pentagonale optimisée
                val spacing = flowerButtonRadius * 2.2f
                
                // ✅ NOUVEAU: Disposition en pentagone pour 5 fleurs
                val positions = listOf(
                    Triple("MARGUERITE", centerX, buttonY - spacing * 0.8f), // Haut centre
                    Triple("ROSE", centerX - spacing * 0.7f, buttonY - spacing * 0.2f), // Gauche haut
                    Triple("LUPIN", centerX + spacing * 0.7f, buttonY - spacing * 0.2f), // Droite haut
                    Triple("IRIS", centerX - spacing * 0.7f, buttonY + spacing * 0.4f), // Gauche bas
                    Triple("ORCHIDEE", centerX + spacing * 0.7f, buttonY + spacing * 0.4f) // Droite bas
                )
                
                for ((flower, x, y) in positions) {
                    if (unlockedFlowers.contains(flower)) {
                        drawFlowerButton(canvas, x, y, flowerButtonRadius * 0.8f, flower, challengeManager)
                    }
                }
            }
        }
    }
    
    private fun drawFlowerButton(canvas: Canvas, x: Float, y: Float, radius: Float, flowerType: String, challengeManager: ChallengeManager) {
        val isUnlocked = challengeManager.isFlowerUnlocked(flowerType)
        
        // SI PAS DÉBLOQUÉE = RIEN AFFICHER (sauf marguerite qui est toujours débloquée)
        if (!isUnlocked && flowerType != "MARGUERITE") return
        
        when (flowerType) {
            "MARGUERITE" -> {
                // Image de marguerite ou fallback
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
            "ORCHIDEE" -> { // ✅ NOUVEAU: Bouton orchidée
                drawOrchideeButton(canvas, x, y, radius)
            }
        }
    }
    
    // ✅ NOUVEAU: Fonction spécialisée pour dessiner le bouton orchidée
    private fun drawOrchideeButton(canvas: Canvas, x: Float, y: Float, radius: Float) {
        // Option 1: Emoji orchidée si disponible
        flowerTextPaint.textSize = radius * 1.6f
        flowerTextPaint.color = 0xFFFF1493.toInt() // Rose vif pour orchidées
        
        try {
            // Essayer l'emoji orchidée
            canvas.drawText("🌺", x, y + 15f, flowerTextPaint)
        } catch (e: Exception) {
            // Fallback: Dessiner une orchidée stylisée
            drawStylizedOrchidee(canvas, x, y, radius)
        }
        
        // ✅ NOUVEAU: Ajouter texte descriptif sous l'icône
        flowerTextPaint.textSize = radius * 0.25f
        flowerTextPaint.color = 0xAAFFFFFF.toInt() // Blanc semi-transparent
        canvas.drawText("ORCHIDÉE", x, y + radius * 1.2f, flowerTextPaint)
    }
    
    // ✅ NOUVEAU: Dessiner une orchidée stylisée si emoji pas disponible
    private fun drawStylizedOrchidee(canvas: Canvas, x: Float, y: Float, radius: Float) {
        val orchideePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        
        // Pétales (5 pétales stylisés)
        for (i in 0 until 5) {
            val angle = i * 72f // 360° / 5 pétales
            val petalLength = radius * 0.8f
            val petalWidth = radius * 0.3f
            
            canvas.save()
            canvas.translate(x, y)
            canvas.rotate(angle)
            
            // Couleur dégradée pour chaque pétale
            orchideePaint.color = when (i) {
                0 -> Color.rgb(255, 20, 147)  // Rose vif
                1 -> Color.rgb(255, 105, 180) // Rose clair
                2 -> Color.rgb(186, 85, 211)  // Violet
                3 -> Color.rgb(138, 43, 226)  // Violet foncé
                else -> Color.rgb(255, 182, 193) // Rose pâle
            }
            
            // Forme de pétale ovale
            canvas.drawOval(
                -petalWidth / 2f, -petalLength / 2f,
                petalWidth / 2f, petalLength / 2f,
                orchideePaint
            )
            
            canvas.restore()
        }
        
        // Centre de l'orchidée (colonne)
        orchideePaint.color = Color.rgb(255, 215, 0) // Or
        canvas.drawCircle(x, y, radius * 0.15f, orchideePaint)
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
        
        if (challengeManager.isFlowerUnlocked("ORCHIDEE")) { // ✅ NOUVEAU: Vérification orchidée
            flowers.add("ORCHIDEE")
        }
        
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
    
    // ==================== FONCTIONS UTILITAIRES ORCHIDÉES ====================
    
    // ✅ NOUVEAU: Fonction pour obtenir la couleur de l'orchidée selon l'espèce
    fun getOrchideeColor(species: String): Int {
        return when (species.uppercase()) {
            "PHALAENOPSIS" -> Color.rgb(255, 182, 193) // Rose pâle
            "CATTLEYA" -> Color.rgb(138, 43, 226)      // Violet royal
            "DENDROBIUM" -> Color.rgb(255, 255, 255)   // Blanc
            "VANDA" -> Color.rgb(65, 105, 225)         // Bleu royal
            "ONCIDIUM" -> Color.rgb(255, 215, 0)       // Jaune or
            "CYMBIDIUM" -> Color.rgb(255, 253, 208)    // Crème
            else -> Color.rgb(255, 20, 147)            // Rose vif par défaut
        }
    }
    
    // ✅ NOUVEAU: Fonction pour dessiner une mini-orchidée spécifique
    fun drawMiniOrchideeBySpecies(canvas: Canvas, x: Float, y: Float, radius: Float, species: String) {
        val orchideePaint = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            color = getOrchideeColor(species)
        }
        
        when (species.uppercase()) {
            "PHALAENOPSIS" -> {
                // Forme papillon
                drawButterflyShape(canvas, x, y, radius, orchideePaint)
            }
            "CATTLEYA" -> {
                // Forme ruffled (ondulée)
                drawRuffledShape(canvas, x, y, radius, orchideePaint)
            }
            "VANDA" -> {
                // Forme plate
                drawFlatShape(canvas, x, y, radius, orchideePaint)
            }
            "ONCIDIUM" -> {
                // Forme dancing lady
                drawDancingShape(canvas, x, y, radius, orchideePaint)
            }
            "DENDROBIUM" -> {
                // Forme en grappe
                drawClusterShape(canvas, x, y, radius, orchideePaint)
            }
            "CYMBIDIUM" -> {
                // Forme en bateau
                drawBoatShape(canvas, x, y, radius, orchideePaint)
            }
            else -> {
                // Forme générique
                drawStylizedOrchidee(canvas, x, y, radius)
            }
        }
    }
    
    // ✅ NOUVEAU: Formes spécialisées par espèce d'orchidée
    private fun drawButterflyShape(canvas: Canvas, x: Float, y: Float, radius: Float, paint: Paint) {
        // Phalaenopsis - forme papillon
        canvas.drawOval(x - radius * 0.6f, y - radius * 0.3f, x + radius * 0.6f, y + radius * 0.3f, paint)
        canvas.drawOval(x - radius * 0.3f, y - radius * 0.6f, x + radius * 0.3f, y + radius * 0.6f, paint)
    }
    
    private fun drawRuffledShape(canvas: Canvas, x: Float, y: Float, radius: Float, paint: Paint) {
        // Cattleya - forme ondulée
        for (i in 0 until 6) {
            val angle = i * 60f
            val ruffleRadius = radius * (0.6f + 0.2f * kotlin.math.sin(i * kotlin.math.PI / 3).toFloat())
            canvas.save()
            canvas.translate(x, y)
            canvas.rotate(angle)
            canvas.drawOval(-ruffleRadius * 0.3f, -ruffleRadius, ruffleRadius * 0.3f, 0f, paint)
            canvas.restore()
        }
    }
    
    private fun drawFlatShape(canvas: Canvas, x: Float, y: Float, radius: Float, paint: Paint) {
        // Vanda - forme plate
        for (i in 0 until 5) {
            val angle = i * 72f
            canvas.save()
            canvas.translate(x, y)
            canvas.rotate(angle)
            canvas.drawRect(-radius * 0.2f, -radius * 0.8f, radius * 0.2f, 0f, paint)
            canvas.restore()
        }
    }
    
    private fun drawDancingShape(canvas: Canvas, x: Float, y: Float, radius: Float, paint: Paint) {
        // Oncidium - forme dancing lady
        canvas.drawOval(x - radius * 0.8f, y + radius * 0.2f, x + radius * 0.8f, y + radius * 0.8f, paint)
        canvas.drawCircle(x, y - radius * 0.3f, radius * 0.3f, paint)
    }
    
    private fun drawClusterShape(canvas: Canvas, x: Float, y: Float, radius: Float, paint: Paint) {
        // Dendrobium - forme en grappe
        for (i in 0 until 4) {
            val offsetY = i * radius * 0.3f - radius * 0.4f
            canvas.drawCircle(x + (i % 2 - 0.5f) * radius * 0.3f, y + offsetY, radius * 0.2f, paint)
        }
    }
    
    private fun drawBoatShape(canvas: Canvas, x: Float, y: Float, radius: Float, paint: Paint) {
        // Cymbidium - forme en bateau
        canvas.drawOval(x - radius * 0.7f, y - radius * 0.2f, x + radius * 0.7f, y + radius * 0.5f, paint)
        canvas.drawOval(x - radius * 0.5f, y - radius * 0.4f, x + radius * 0.5f, y + radius * 0.2f, paint)
    }
    
    // ✅ NOUVEAU: Fonction pour afficher des infos sur les orchidées débloquées
    fun drawOrchideeInfo(canvas: Canvas, challengeManager: ChallengeManager) {
        if (!challengeManager.isFlowerUnlocked("ORCHIDEE")) return
        
        flowerTextPaint.textSize = 30f
        flowerTextPaint.color = 0x77FFFFFF.toInt()
        flowerTextPaint.textAlign = Paint.Align.CENTER
        
        canvas.drawText("6 espèces d'orchidées procédurales", screenWidth / 2f, screenHeight * 0.85f, flowerTextPaint)
        canvas.drawText("Chaque fleur est 100% unique", screenWidth / 2f, screenHeight * 0.88f, flowerTextPaint)
    }
}
