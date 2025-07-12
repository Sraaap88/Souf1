package com.example.souffleforcetest

class ChallengeZoneHelper {
    
    fun isInMargueriteZone(flowerY: Float, screenHeight: Float, challengeId: Int): Boolean {
        return when (challengeId) {
            1 -> {
                val zoneTop = screenHeight / 3f - 60f
                val zoneBottom = screenHeight / 3f + 360f
                flowerY >= zoneTop && flowerY <= zoneBottom
            }
            3 -> {
                val zoneTop = screenHeight / 3f - 120f
                val zoneBottom = screenHeight / 3f + 120f
                flowerY >= zoneTop && flowerY <= zoneBottom
            }
            else -> {
                val zoneTop = screenHeight / 3f - 60f
                val zoneBottom = screenHeight / 3f + 360f
                flowerY >= zoneTop && flowerY <= zoneBottom
            }
        }
    }
    
    fun isInCentralZone(flowerY: Float, screenHeight: Float): Boolean {
        val zoneHeight = 192f  // 2 pouces
        val zoneTop = (screenHeight - zoneHeight) / 2f
        val zoneBottom = zoneTop + zoneHeight
        return flowerY >= zoneTop && flowerY <= zoneBottom
    }
    
    fun isInRoseZone(flowerY: Float, screenHeight: Float): Boolean {
        return isInCentralZone(flowerY, screenHeight)
    }
    
    fun isInLupinZone(flowerY: Float, screenHeight: Float, challengeId: Int): Boolean {
        return isInCentralZone(flowerY, screenHeight)
    }
    
    fun isInIrisZone(flowerY: Float, screenHeight: Float): Boolean {
        // Zone centrale comme Rose et Lupin
        return isInCentralZone(flowerY, screenHeight)
    }
}
