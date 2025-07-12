package com.example.souffleforcetest

class ChallengeZoneHelper {
    
    // ==================== ZONES MARGUERITE MODIFIÉES ====================
    
    fun isInMargueriteZone(flowerY: Float, screenHeight: Float, challengeId: Int): Boolean {
        return when (challengeId) {
            1 -> {
                // DÉFI 1 MARGUERITE MODIFIÉ: Bande verte élargie
                // Ancienne bande: screenHeight / 3f - 60f à screenHeight / 3f + 360f = 420f de hauteur
                // Nouvelle bande: +420f vers le bas + 210f vers le haut = 1.5X plus épaisse
                val zoneTop = screenHeight / 3f - 60f - 210f  // Élargissement vers le haut (1/2 fois)
                val zoneBottom = screenHeight / 3f + 360f + 420f  // Élargissement vers le bas (1 fois)
                flowerY >= zoneTop && flowerY <= zoneBottom
            }
            3 -> {
                val zoneTop = screenHeight / 3f - 120f
                val zoneBottom = screenHeight / 3f + 120f
                flowerY >= zoneTop && flowerY <= zoneBottom
            }
            else -> false
        }
    }
    
    // ==================== ZONES LUPIN MODIFIÉES ====================
    
    fun isInLupinZone(flowerY: Float, screenHeight: Float, challengeId: Int): Boolean {
        return when (challengeId) {
            1 -> {
                // DÉFI 1 LUPIN: Zone identique à la marguerite modifiée
                val zoneTop = screenHeight / 3f - 60f - 210f  // Élargissement vers le haut (1/2 fois)
                val zoneBottom = screenHeight / 3f + 360f + 420f  // Élargissement vers le bas (1 fois)
                flowerY >= zoneTop && flowerY <= zoneBottom
            }
            3 -> {
                // DÉFI 3 LUPIN MODIFIÉ: Bande verte élargie vers le bas seulement
                // Zone centrale élargie d'une fois vers le bas
                val originalZoneHeight = 192f  // 2 pouces
                val zoneTop = (screenHeight - originalZoneHeight) / 2f
                val zoneBottom = zoneTop + originalZoneHeight + originalZoneHeight  // +1 fois vers le bas
                flowerY >= zoneTop && flowerY <= zoneBottom
            }
            else -> false
        }
    }
    
    // ==================== ZONES CENTRALES ====================
    
    fun isInCentralZone(flowerY: Float, screenHeight: Float): Boolean {
        val zoneHeight = 192f  // 2 pouces
        val zoneTop = (screenHeight - zoneHeight) / 2f
        val zoneBottom = zoneTop + zoneHeight
        return flowerY >= zoneTop && flowerY <= zoneBottom
    }

    fun isInRoseZone(flowerY: Float, screenHeight: Float): Boolean {
        return isInCentralZone(flowerY, screenHeight)
    }
    
    // NOUVEAU: Zone pour l'iris - Zone centrale comme Rose
    fun isInIrisZone(flowerY: Float, screenHeight: Float): Boolean {
        return isInCentralZone(flowerY, screenHeight)
    }
}
