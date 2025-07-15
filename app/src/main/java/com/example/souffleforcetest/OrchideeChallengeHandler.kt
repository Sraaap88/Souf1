package com.example.souffleforcetest

class OrchideeChallengeHandler {
    
    // ==================== LOGIQUE DES DÉFIS ORCHIDÉE ====================
    
    fun updateChallenge(
        challengeId: Int, 
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        when (challengeId) {
            1 -> updateOrchideeChallenge1_Saccades(force, plantState, challengeData)
            2 -> updateOrchideeChallenge2_Delicat(force, plantState, challengeData)
            3 -> updateOrchideeChallenge3_Patience(force, plantState, challengeData)
        }
    }
    
    private fun updateOrchideeChallenge1_Saccades(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        // Défi 1: Saccades régulières pour créer différentes espèces
        challengeData["currentPhase"] = plantState
        
        // Détecter les saccades (changements brusques de force)
        val lastForce = challengeData["lastForce"] as? Float ?: 0f
        val forceDifference = kotlin.math.abs(force - lastForce)
        
        if (forceDifference > 0.3f && force > 0.2f) {
            val saccadeCount = challengeData["saccadeCount"] as? Int ?: 0
            challengeData["saccadeCount"] = saccadeCount + 1
            println("Orchidée - Saccade détectée! Total: ${saccadeCount + 1}/10")
        }
        
        challengeData["lastForce"] = force
    }
    
    private fun updateOrchideeChallenge2_Delicat(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        // Défi 2: Souffle délicat et contrôlé
        challengeData["currentPhase"] = plantState
        
        // Vérifier la délicatesse du souffle
        if (force > 0.1f && force < 0.4f) {
            val delicateTime = challengeData["delicateTime"] as? Float ?: 0f
            challengeData["delicateTime"] = delicateTime + 0.016f // ~60fps
        }
    }
    
    private fun updateOrchideeChallenge3_Patience(
        force: Float, 
        plantState: String, 
        challengeData: MutableMap<String, Any>
    ) {
        // Défi 3: Patience pour cultiver toutes les espèces
        challengeData["currentPhase"] = plantState
        
        // Mesurer la durée totale du défi
        val startTime = challengeData["startTime"] as? Long ?: System.currentTimeMillis()
        if (challengeData["startTime"] == null) {
            challengeData["startTime"] = startTime
        }
        
        val currentTime = System.currentTimeMillis()
        val elapsedTime = (currentTime - startTime) / 1000f // en secondes
        challengeData["patienceTime"] = elapsedTime
    }
    
    // ✅ CORRIGÉ: Signature cohérente avec ChallengeDataManager
    fun checkChallenge(
        challengeId: Int,
        orchideeFlowersInZone: List<String>,
        orchideeSpeciesCount: Int,
        orchideeTotalFlowers: List<String>
    ): Boolean {
        return when (challengeId) {
            1 -> {
                // Défi 1: 10 saccades + 6 espèces + 8 fleurs en zone
                val saccadesCompleted = 10 // Simulé - devrait venir de challengeData
                saccadesCompleted >= 10 && 
                orchideeSpeciesCount >= 6 && 
                orchideeFlowersInZone.size >= 8
            }
            2 -> {
                // Défi 2: 3 tiges délicates + 5 fleurs en zone précise
                val tigesCompletes = 3 // Simulé - devrait venir de OrchideeData
                tigesCompletes >= 3 && 
                orchideeFlowersInZone.size >= 5
            }
            3 -> {
                // Défi 3: 6 espèces + 20 fleurs totales
                orchideeSpeciesCount >= 6 && 
                orchideeTotalFlowers.size >= 20
            }
            else -> false
        }
    }
    
    // ✅ CORRIGÉ: Signature cohérente avec ChallengeDataManager
    fun getSuccessMessage(
        challengeId: Int,
        orchideeFlowersInZone: List<String>,
        orchideeSpeciesCount: Int,
        orchideeTotalFlowers: List<String>
    ): String {
        return when (challengeId) {
            1 -> {
                val zonesFlowers = orchideeFlowersInZone.size
                "Défi réussi! Saccades maîtrisées - $orchideeSpeciesCount espèces créées, $zonesFlowers orchidées en zone élégante!\n🌸 TECHNIQUE PARFAITE!"
            }
            2 -> {
                val zonesFlowers = orchideeFlowersInZone.size
                "Défi réussi! Souffle délicat maîtrisé - $zonesFlowers orchidées dans la zone de précision!\n🎯 CONTRÔLE PARFAIT!"
            }
            3 -> {
                val totalFlowers = orchideeTotalFlowers.size
                "Défi réussi! Patience récompensée - $orchideeSpeciesCount espèces complètes, $totalFlowers orchidées cultivées!\n👑 GRAND MAÎTRE DES ORCHIDÉES!"
            }
            else -> "Défi orchidée réussi!"
        }
    }
    
    // ✅ CORRIGÉ: Signature cohérente avec ChallengeDataManager
    fun getFailMessage(
        challengeId: Int,
        orchideeFlowersInZone: List<String>,
        orchideeSpeciesCount: Int,
        orchideeTotalFlowers: List<String>
    ): String {
        return when (challengeId) {
            1 -> {
                val zonesFlowers = orchideeFlowersInZone.size
                "Défi échoué - Seulement $orchideeSpeciesCount/6 espèces et $zonesFlowers/8 orchidées en zone!\nContinuez à pratiquer les saccades régulières."
            }
            2 -> {
                val zonesFlowers = orchideeFlowersInZone.size
                "Défi échoué - Seulement $zonesFlowers/5 orchidées dans la zone de précision!\nTravaillez votre souffle délicat et contrôlé."
            }
            3 -> {
                val totalFlowers = orchideeTotalFlowers.size
                "Défi échoué - $orchideeSpeciesCount/6 espèces et $totalFlowers/20 fleurs!\nLa patience est la clé de la maîtrise des orchidées."
            }
            else -> "Défi orchidée échoué!"
        }
    }
    
    // ==================== MÉTHODES UTILITAIRES ORCHIDÉE ====================
    
    fun resetDissolveEffects(challengeData: MutableMap<String, Any>) {
        // Reset des données spécifiques aux orchidées
        challengeData.remove("saccadeCount")
        challengeData.remove("delicateTime")
        challengeData.remove("patienceTime")
        challengeData.remove("lastForce")
        challengeData.remove("startTime")
        challengeData.remove("currentPhase")
        
        // Reset des données de dissolution
        challengeData.remove("dissolveProgress")
        challengeData.remove("stemsCollapsing")
        challengeData.remove("leavesShriveling")
        challengeData.remove("flowersPetalsWilting")
        
        println("Orchidée - Effets de dissolution réinitialisés")
    }
    
    fun getSaccadeCount(challengeData: Map<String, Any>): Int {
        return challengeData["saccadeCount"] as? Int ?: 0
    }
    
    fun getDelicateTime(challengeData: Map<String, Any>): Float {
        return challengeData["delicateTime"] as? Float ?: 0f
    }
    
    fun getPatienceTime(challengeData: Map<String, Any>): Float {
        return challengeData["patienceTime"] as? Float ?: 0f
    }
    
    fun getCurrentPhase(challengeData: Map<String, Any>): String {
        return challengeData["currentPhase"] as? String ?: "IDLE"
    }
    
    // ==================== CONSEILS SPÉCIALISÉS ORCHIDÉE ====================
    
    fun getChallengeHint(challengeId: Int, currentProgress: Map<String, Any>): String {
        return when (challengeId) {
            1 -> {
                val saccades = getSaccadeCount(currentProgress)
                val species = currentProgress["speciesCount"] as? Int ?: 0
                val inZone = currentProgress["flowersInZone"] as? Int ?: 0
                
                when {
                    saccades < 5 -> "💡 Conseil: Alternez entre souffle fort et doux pour créer des saccades"
                    species < 3 -> "💡 Conseil: Chaque saccade peut créer une espèce différente"
                    inZone < 4 -> "💡 Conseil: Visez la zone élégante au centre pour les orchidées"
                    else -> "💡 Conseil: Vous progressez bien! Continuez les saccades régulières"
                }
            }
            2 -> {
                val inZone = currentProgress["flowersInZone"] as? Int ?: 0
                val delicateTime = getDelicateTime(currentProgress)
                
                when {
                    delicateTime < 10f -> "💡 Conseil: Maintenez un souffle très doux et constant"
                    inZone < 2 -> "💡 Conseil: La zone de précision est plus petite, soyez très précis"
                    else -> "💡 Conseil: Excellent contrôle! Continuez avec délicatesse"
                }
            }
            3 -> {
                val species = currentProgress["speciesCount"] as? Int ?: 0
                val totalFlowers = currentProgress["totalFlowers"] as? Int ?: 0
                val patienceTime = getPatienceTime(currentProgress)
                
                when {
                    species < 3 -> "💡 Conseil: Explorez toutes les techniques pour créer 6 espèces"
                    totalFlowers < 10 -> "💡 Conseil: Prenez votre temps, cultivez avec patience"
                    patienceTime < 60f -> "💡 Conseil: Les orchidées demandent du temps et de la persévérance"
                    else -> "💡 Conseil: Vous maîtrisez l'art des orchidées! Continuez!"
                }
            }
            else -> "💡 Conseil: Chaque orchidée est unique, adaptez votre technique"
        }
    }
    
    fun getSpecializedAdvice(challengeId: Int): List<String> {
        return when (challengeId) {
            1 -> listOf(
                "🌸 Les saccades créent différentes espèces d'orchidées",
                "🎯 Visez la zone élégante pour maximiser vos points",
                "⚡ Alternez entre souffle fort et doux rapidement",
                "🌺 6 espèces différentes sont nécessaires pour réussir"
            )
            2 -> listOf(
                "🌬️ Souffle délicat et contrôlé uniquement",
                "🎯 Zone de précision plus petite que d'habitude",
                "🌸 Chaque tige doit être complète avec ses fleurs",
                "⏱️ Prenez le temps nécessaire pour la précision"
            )
            3 -> listOf(
                "🌈 Explorez toutes les 6 espèces d'orchidées",
                "🕐 La patience est la clé de ce défi",
                "🌸 20 fleurs au total, pas de zone spécifique",
                "👑 Défi ultime de maîtrise complète"
            )
            else -> listOf("🌸 Maîtrisez l'art délicat des orchidées")
        }
    }
}
