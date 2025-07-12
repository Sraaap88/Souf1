// ==================== PARAMÈTRES ULTRA FACILES POUR LES TIGES ====================

// Croissance des tiges ÉNORMÉMENT augmentée
private val stemGrowthRate = 5000f     // ÉNORME augmentation (était 2500f)

// Seuil de force EXTRÊMEMENT bas
private fun growLatestStem(force: Float) {
    val latestStem = stems.lastOrNull() ?: return
    
    // CHANGEMENT MAJEUR : Force minimum TRÈS TRÈS basse
    if (latestStem.isActive && force > 0.01f && latestStem.currentHeight < latestStem.maxHeight) { // 0.05f → 0.01f
        
        // ÉNORME multiplicateur de croissance
        val baseGrowth = force * stemGrowthRate * 0.05f  // 0.025f → 0.05f (DOUBLE!)
        val individualGrowth = baseGrowth * latestStem.growthSpeedMultiplier
        latestStem.currentHeight = (latestStem.currentHeight + individualGrowth).coerceAtMost(latestStem.maxHeight)
        
        // Segments TRÈS courts pour progression rapide
        if (latestStem.points.size >= 2 && latestStem.currentHeight >= latestStem.points.size * 10f) { // segmentLength réduit à 10f
            val lastPoint = latestStem.points.last()
            
            val randomOffset = (Math.random().toFloat() - 0.5f) * 1f // Très petit offset
            val newX = latestStem.baseX + randomOffset
            val newY = lastPoint.y - 10f // Segments de 10f seulement
            val newThickness = (lastPoint.thickness * 0.98f).coerceAtLeast(2f)
            
            if (newX >= 0 && newX <= screenWidth && newY >= 0) {
                latestStem.points.add(StemPoint(newX, newY, newThickness))
            } else {
                latestStem.isActive = false
            }
        }
        
        // Arrêter TRÈS tard pour avoir de grandes tiges
        if (latestStem.currentHeight >= latestStem.maxHeight * 0.95f) {
            latestStem.isActive = false
        }
    }
}

// Création de tiges ULTRA facile
private fun detectSpikeAndCreateStem(force: Float) {
    val currentTime = System.currentTimeMillis()
    
    // Seuils EXTRÊMEMENT bas pour détecter les saccades
    val forceIncrease = force - lastForce
    val isSpike = forceIncrease > 0.05f && force > 0.02f  // TRÈS TRÈS bas
    val canCreateStem = currentTime - lastSpikeTime > 100L // 150ms → 100ms
    
    if (isSpike && canCreateStem && stems.size < maxStems) {
        val newStemX = baseX + (stems.size - 2) * 30f + (Math.random().toFloat() - 0.5f) * 15f
        createNewStem(newStemX, baseY)
        lastSpikeTime = currentTime
        
        println("Lupin - Nouvelle tige créée! Total: ${stems.size}/$maxStems")
    }
}

// Hauteur de tige RÉDUITE pour finir plus vite
private fun createNewStem(stemX: Float, stemY: Float) {
    val stem = LupinStem(
        maxHeight = screenHeight * 0.3f + Math.random().toFloat() * screenHeight * 0.1f, // TRÈS réduit
        baseX = stemX,
        baseY = stemY
    )
    
    // Point de base
    stem.points.add(StemPoint(stemX, stemY, 8f))
    // Deuxième point
    val secondY = stemY - 5f // Très petit segment initial
    stem.points.add(StemPoint(stemX, secondY, 7.5f))
    stem.currentHeight = 5f
    
    stems.add(stem)
    challengeManager?.notifyLupinSpikeCreated("NEW_STEM", stem.id)
}
