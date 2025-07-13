package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color

class ChallengeZoneHelper {
    
    // ==================== CONSTANTES POUR ZONES CORRIGÉES ====================
    
    // Pour Huawei P30 Pro : 2340 × 1080 pixels, 6.47 pouces
    // 1 pouce ≈ 362 pixels de hauteur (2340/6.47)
    // Zone visible de ~1 pouce = 100 pixels (pour être sûr que c'est visible)
    private val ZONE_HEIGHT_1_INCH = 100f
    private val ZONE_HEIGHT_2_INCH = 200f // 2 pouces pour les zones centrales
    
    // ==================== ZONES MARGUERITE CORRIGÉES ====================
    
    fun isInMargueriteZone(flowerY: Float, screenHeight: Float, challengeId: Int): Boolean {
        return when (challengeId) {
            1 -> {
                // DÉFI 1 MARGUERITE CORRIGÉ: Zone verte de 1 pouce de haut
                // Position: 1/3 de l'écran depuis le haut + décalage
                val zoneCenterY = screenHeight / 3f + 150f  // Décalage pour bien positionner
                val zoneTop = zoneCenterY - ZONE_HEIGHT_1_INCH / 2f
                val zoneBottom = zoneCenterY + ZONE_HEIGHT_1_INCH / 2f
                flowerY >= zoneTop && flowerY <= zoneBottom
            }
            3 -> {
                // DÉFI 3 MARGUERITE CORRIGÉ: Zone verte de 1 pouce de haut
                val zoneCenterY = screenHeight / 3f + 150f
                val zoneTop = zoneCenterY - ZONE_HEIGHT_1_INCH / 2f
                val zoneBottom = zoneCenterY + ZONE_HEIGHT_1_INCH / 2f
                flowerY >= zoneTop && flowerY <= zoneBottom
            }
            else -> false
        }
    }
    
    // ==================== ZONES LUPIN CORRIGÉES ====================
    
    fun isInLupinZone(flowerY: Float, screenHeight: Float, challengeId: Int): Boolean {
        return when (challengeId) {
            1 -> {
                // DÉFI 1 LUPIN CORRIGÉ: Zone verte de 1 pouce de haut (identique à marguerite)
                val zoneCenterY = screenHeight / 3f + 150f
                val zoneTop = zoneCenterY - ZONE_HEIGHT_1_INCH / 2f
                val zoneBottom = zoneCenterY + ZONE_HEIGHT_1_INCH / 2f
                flowerY >= zoneTop && flowerY <= zoneBottom
            }
            3 -> {
                // DÉFI 3 LUPIN CORRIGÉ: Zone centrale de 2 pouces de haut
                val zoneCenterY = screenHeight / 2f
                val zoneTop = zoneCenterY - ZONE_HEIGHT_2_INCH / 2f
                val zoneBottom = zoneCenterY + ZONE_HEIGHT_2_INCH / 2f
                flowerY >= zoneTop && flowerY <= zoneBottom
            }
            else -> false
        }
    }
    
    // ==================== ZONES CENTRALES CORRIGÉES ====================
    
    fun isInCentralZone(flowerY: Float, screenHeight: Float): Boolean {
        // Zone centrale de 2 pouces de haut
        val zoneCenterY = screenHeight / 2f
        val zoneTop = zoneCenterY - ZONE_HEIGHT_2_INCH / 2f
        val zoneBottom = zoneCenterY + ZONE_HEIGHT_2_INCH / 2f
        return flowerY >= zoneTop && flowerY <= zoneBottom
    }

    fun isInRoseZone(flowerY: Float, screenHeight: Float): Boolean {
        return isInCentralZone(flowerY, screenHeight)
    }
    
    fun isInIrisZone(flowerY: Float, screenHeight: Float): Boolean {
        return isInCentralZone(flowerY, screenHeight)
    }
    
    // ==================== FONCTION DE DEBUG VISUEL CORRIGÉE ====================
    
    /**
     * Dessine la vraie zone de détection corrigée en rouge transparent pour debug
     * Appelez cette fonction dans votre UIDrawingManager pour vérifier les zones
     */
    fun drawDebugDetectionZones(canvas: Canvas, screenWidth: Int, screenHeight: Float, flowerType: String, challengeId: Int, debugPaint: Paint) {
        debugPaint.color = Color.argb(80, 255, 0, 0) // Rouge transparent
        debugPaint.style = Paint.Style.FILL
        
        when (flowerType) {
            "MARGUERITE" -> {
                when (challengeId) {
                    1, 3 -> {
                        val zoneCenterY = screenHeight / 3f + 150f
                        val zoneTop = zoneCenterY - ZONE_HEIGHT_1_INCH / 2f
                        val zoneBottom = zoneCenterY + ZONE_HEIGHT_1_INCH / 2f
                        canvas.drawRect(0f, zoneTop, screenWidth.toFloat(), zoneBottom, debugPaint)
                        
                        // Afficher les dimensions
                        debugPaint.color = Color.WHITE
                        debugPaint.textSize = 30f
                        canvas.drawText("Zone: ${ZONE_HEIGHT_1_INCH.toInt()}px (~1 pouce)", 50f, zoneTop - 10f, debugPaint)
                    }
                }
            }
            "LUPIN" -> {
                when (challengeId) {
                    1 -> {
                        val zoneCenterY = screenHeight / 3f + 150f
                        val zoneTop = zoneCenterY - ZONE_HEIGHT_1_INCH / 2f
                        val zoneBottom = zoneCenterY + ZONE_HEIGHT_1_INCH / 2f
                        canvas.drawRect(0f, zoneTop, screenWidth.toFloat(), zoneBottom, debugPaint)
                    }
                    3 -> {
                        val zoneCenterY = screenHeight / 2f
                        val zoneTop = zoneCenterY - ZONE_HEIGHT_2_INCH / 2f
                        val zoneBottom = zoneCenterY + ZONE_HEIGHT_2_INCH / 2f
                        canvas.drawRect(0f, zoneTop, screenWidth.toFloat(), zoneBottom, debugPaint)
                    }
                }
            }
            "ROSE", "IRIS" -> {
                val zoneCenterY = screenHeight / 2f
                val zoneTop = zoneCenterY - ZONE_HEIGHT_2_INCH / 2f
                val zoneBottom = zoneCenterY + ZONE_HEIGHT_2_INCH / 2f
                canvas.drawRect(0f, zoneTop, screenWidth.toFloat(), zoneBottom, debugPaint)
                
                debugPaint.color = Color.WHITE
                debugPaint.textSize = 30f
                canvas.drawText("Zone centrale: ${ZONE_HEIGHT_2_INCH.toInt()}px (~2 pouces)", 50f, zoneTop - 10f, debugPaint)
            }
        }
    }
    
    /**
     * Affiche les informations de debug corrigées
     */
    fun drawDebugInfo(canvas: Canvas, screenHeight: Float, flowerType: String, challengeId: Int, debugPaint: Paint) {
        debugPaint.color = Color.YELLOW
        debugPaint.textSize = 25f
        
        var yOffset = 100f
        canvas.drawText("Debug Zone CORRIGÉE:", 50f, yOffset, debugPaint)
        yOffset += 30f
        canvas.drawText("Fleur: $flowerType", 50f, yOffset, debugPaint)
        yOffset += 30f
        canvas.drawText("Défi: $challengeId", 50f, yOffset, debugPaint)
        yOffset += 30f
        canvas.drawText("Écran: ${screenHeight.toInt()}px", 50f, yOffset, debugPaint)
        yOffset += 30f
        
        when (flowerType) {
            "MARGUERITE" -> {
                if (challengeId == 1 || challengeId == 3) {
                    val zoneCenterY = screenHeight / 3f + 150f
                    val zoneTop = zoneCenterY - ZONE_HEIGHT_1_INCH / 2f
                    val zoneBottom = zoneCenterY + ZONE_HEIGHT_1_INCH / 2f
                    canvas.drawText("Zone: ${zoneTop.toInt()} à ${zoneBottom.toInt()}", 50f, yOffset, debugPaint)
                    yOffset += 30f
                    canvas.drawText("Hauteur: ${ZONE_HEIGHT_1_INCH.toInt()}px", 50f, yOffset, debugPaint)
                }
            }
            "LUPIN" -> {
                when (challengeId) {
                    1 -> {
                        canvas.drawText("Zone: ${ZONE_HEIGHT_1_INCH.toInt()}px (1 pouce)", 50f, yOffset, debugPaint)
                    }
                    3 -> {
                        canvas.drawText("Zone: ${ZONE_HEIGHT_2_INCH.toInt()}px (2 pouces)", 50f, yOffset, debugPaint)
                    }
                }
            }
            "ROSE", "IRIS" -> {
                canvas.drawText("Zone: ${ZONE_HEIGHT_2_INCH.toInt()}px (2 pouces)", 50f, yOffset, debugPaint)
            }
        }
    }
    
    // ==================== FONCTIONS UTILITAIRES ====================
    
    /**
     * Retourne les dimensions exactes d'une zone pour un type de fleur et défi donné
     */
    fun getZoneDimensions(flowerType: String, challengeId: Int, screenHeight: Float): Pair<Float, Float>? {
        return when (flowerType) {
            "MARGUERITE" -> {
                when (challengeId) {
                    1, 3 -> {
                        val zoneCenterY = screenHeight / 3f + 150f
                        val zoneTop = zoneCenterY - ZONE_HEIGHT_1_INCH / 2f
                        val zoneBottom = zoneCenterY + ZONE_HEIGHT_1_INCH / 2f
                        Pair(zoneTop, zoneBottom)
                    }
                    else -> null
                }
            }
            "LUPIN" -> {
                when (challengeId) {
                    1 -> {
                        val zoneCenterY = screenHeight / 3f + 150f
                        val zoneTop = zoneCenterY - ZONE_HEIGHT_1_INCH / 2f
                        val zoneBottom = zoneCenterY + ZONE_HEIGHT_1_INCH / 2f
                        Pair(zoneTop, zoneBottom)
                    }
                    3 -> {
                        val zoneCenterY = screenHeight / 2f
                        val zoneTop = zoneCenterY - ZONE_HEIGHT_2_INCH / 2f
                        val zoneBottom = zoneCenterY + ZONE_HEIGHT_2_INCH / 2f
                        Pair(zoneTop, zoneBottom)
                    }
                    else -> null
                }
            }
            "ROSE", "IRIS" -> {
                val zoneCenterY = screenHeight / 2f
                val zoneTop = zoneCenterY - ZONE_HEIGHT_2_INCH / 2f
                val zoneBottom = zoneCenterY + ZONE_HEIGHT_2_INCH / 2f
                Pair(zoneTop, zoneBottom)
            }
            else -> null
        }
    }
}
