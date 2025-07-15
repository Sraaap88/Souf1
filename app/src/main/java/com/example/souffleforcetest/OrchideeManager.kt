package com.example.souffleforcetest

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import kotlin.random.Random
import kotlin.math.*

/**
 * GESTIONNAIRE PRINCIPAL DES ORCHIDÉES PROCÉDURALES
 * Basé sur le système de saccades de LupinManager avec croissance par espèce
 */
class OrchideeManager(private val screenWidth: Int, private val screenHeight: Int) {
    
    // ==================== VARIABLES PRINCIPALES ====================
    
    private val stems = mutableListOf<OrchideeStem>()
    private val flowers = mutableListOf<OrchideeFlower>()
    private val renderer = OrchideeRenderer()
    
    // Optimiseur de rendu
    private val optimizer = OrchideeOptimizer(screenWidth, screenHeight)
    
    private var baseX = 0f
    private var baseY = 0f
    private var lastForce = 0f
    private var challengeManager: ChallengeManager? = null
    
    // ==================== SYSTÈME DE SACCADES (COPIÉ DE LUPIN) ====================
    
    private var saccadeCount = 0
    private var isCurrentlyBreathing = false
    private var lastSaccadeTime = 0L
    private val saccadeCooldown = 400L // Plus lent pour orchidées
    private val breathStartThreshold = 0.25f
    private val breathEndThreshold = 0.15f
    
    private var stemOrderPool = mutableListOf<Int>()
    private var currentActiveStemIndex = -1
    
    // ==================== PARAMÈTRES DE CROISSANCE ====================
    
    private val forceThreshold = 0.12f // Plus sensible que lupin
    private val maxStemHeight = 0.4f // Plus petites que lupins
    private val baseThickness = 8f
    private val tipThickness = 2f
    private val growthRate = 3200f // Plus lent que lupins
    private val maxStems = 15 // Plus d'orchidées
    
    // Paramètres spécifiques aux orchidées
    private val baseFlowerSize = 60f // Plus grandes que lupins
    private val pseudobulbSize = 45f
    private val minimumHeightForFlowers = 30f
    
    // Marges pour espacement
    private val marginFromEdges = screenWidth * 0.1f
    
    // ==================== GÉNÉRATEUR D'IDS ====================
    
    private var stemIdCounter = 0
    private var flowerIdCounter = 0
    
    private fun generateOrchideeStemId(): String {
        stemIdCounter++
        return "orchideestem_$stemIdCounter"
    }
    
    private fun generateOrchideeFlowerId(): String {
        flowerIdCounter++
        return "orchideeflower_$flowerIdCounter"
    }
    
    init {
        setupRandomStemOrder()
    }
    
    // ==================== FONCTIONS PUBLIQUES ====================
    
    fun setChallengeManager(manager: ChallengeManager) {
        challengeManager = manager
    }
    
    fun initialize(centerX: Float, bottomY: Float) {
        baseX = centerX
        baseY = bottomY
        
        if (stems.isEmpty()) {
            createInitialOrchideeCluster()
        }
    }
    
    fun processStemGrowth(force: Float) {
        if (stems.isEmpty()) {
            createInitialOrchideeCluster()
            
            if (force > forceThreshold) {
                saccadeCount = 1
                currentActiveStemIndex = 0
                lastSaccadeTime = System.currentTimeMillis()
                isCurrentlyBreathing = true
            }
        }
        
        detectSaccadesAndActivateStems(force, System.currentTimeMillis())
        
        if (force > forceThreshold && currentActiveStemIndex >= 0) {
            growActiveOrchideeStems(force)
        }
        
        lastForce = force
    }
    
    fun processFlowerGrowth(force: Float) {
        createFlowersOnMatureStems()
        growExistingFlowers(force)
        updateFlowerPositions()
    }
    
    fun processLeafGrowth(force: Float) {
        createLeavesOnStems()
        growExistingLeaves(force)
    }
    
    fun reset() {
        stems.clear()
        flowers.clear()
        lastForce = 0f
        
        saccadeCount = 0
        isCurrentlyBreathing = false
        lastSaccadeTime = 0L
        currentActiveStemIndex = -1
        stemIdCounter = 0
        flowerIdCounter = 0
        setupRandomStemOrder()
    }
    
    fun drawOrchidees(
        canvas: Canvas, 
        stemPaint: Paint, 
        leafPaint: Paint, 
        flowerPaint: Paint, 
        dissolveInfo: ChallengeEffectsManager.DissolveInfo? = null
    ) {
        // Optimisation simple : filtrer les éléments hors écran
        val visibleStems = stems.filter { optimizer.isStemVisible(it) }
        val visibleFlowers = flowers.filter { optimizer.isFlowerVisible(it) }
        
        renderer.drawOrchidee(canvas, stemPaint, leafPaint, flowerPaint, 
                             visibleStems, visibleFlowers, dissolveInfo)
    }
    
    // ==================== SYSTÈME ORDRE ALÉATOIRE ====================
    
    private fun setupRandomStemOrder() {
        // 8 groupes d'orchidées avec espèces spécifiques
        stemOrderPool = mutableListOf(0, 1, 2, 3, 4, 5, 6, 7)
        stemOrderPool.shuffle()
    }
    
    private fun detectSaccadesAndActivateStems(force: Float, currentTime: Long) {
        val wasBreathing = isCurrentlyBreathing
        val isNowBreathing = force > breathStartThreshold
        
        if (!wasBreathing && isNowBreathing) {
            if (currentTime - lastSaccadeTime > saccadeCooldown) {
                saccadeCount++
                lastSaccadeTime = currentTime
                isCurrentlyBreathing = true
                activateNextOrchideeGroup()
            }
        }
        
        if (wasBreathing && force < breathEndThreshold) {
            isCurrentlyBreathing = false
        }
    }
    
    private fun activateNextOrchideeGroup() {
        if (saccadeCount <= stemOrderPool.size) {
            val groupTypeToActivate = stemOrderPool[saccadeCount - 1]
            currentActiveStemIndex = saccadeCount - 1
            
            if (groupTypeToActivate == 0) {
                println("Saccade $saccadeCount: Groupe ORCHIDÉES PRINCIPAL activé")
            } else {
                println("Saccade $saccadeCount: Nouveau groupe orchidées $groupTypeToActivate créé")
                createNewOrchideeGroup(groupTypeToActivate)
            }
        }
    }
    
    // ==================== CRÉATION DES TIGES D'ORCHIDÉES ====================
    
    private fun createInitialOrchideeCluster() {
        // Cluster principal avec Phalaenopsis (les plus communes)
        val initialStemCount = 3 + Random.nextInt(4) // 3-6 tiges
        val radius = 200f
        
        for (i in 0 until initialStemCount) {
            val angle = Random.nextDouble() * 2 * PI
            val distance = Random.nextDouble() * radius + 80f
            var stemX = baseX + (cos(angle) * distance).toFloat()
            var stemY = baseY + (Random.nextFloat() - 0.5f) * 60f
            
            // Garder dans les limites
            stemX = stemX.coerceIn(marginFromEdges, screenWidth - marginFromEdges)
            
            createOrchideeStem(stemX, stemY, OrchideeSpecies.PHALAENOPSIS)
        }
    }
    
    private fun createNewOrchideeGroup(groupNumber: Int) {
        // Déterminer l'espèce selon le groupe
        val species = when (groupNumber) {
            1 -> OrchideeSpecies.CATTLEYA
            2 -> OrchideeSpecies.DENDROBIUM
            3 -> OrchideeSpecies.VANDA
            4 -> OrchideeSpecies.ONCIDIUM
            5 -> OrchideeSpecies.CYMBIDIUM
            6 -> OrchideeSpecies.PHALAENOPSIS // Autre couleur
            else -> OrchideeSpecies.values().random()
        }
        
        val baseRadius = 250f + groupNumber * 120f
        val groupAngle = Random.nextDouble() * 2 * PI
        val groupDistance = Random.nextDouble() * baseRadius + 150f
        
        var groupBaseX = baseX + (cos(groupAngle) * groupDistance).toFloat()
        var groupBaseY = baseY + (Random.nextFloat() - 0.5f) * 80f
        
        groupBaseX = groupBaseX.coerceIn(marginFromEdges, screenWidth - marginFromEdges)
        
        // Nombre de tiges selon l'espèce
        val stemCount = when (species) {
            OrchideeSpecies.DENDROBIUM -> 4 + Random.nextInt(4) // Grappes
            OrchideeSpecies.CYMBIDIUM -> 2 + Random.nextInt(3) // Grandes
            OrchideeSpecies.ONCIDIUM -> 5 + Random.nextInt(3) // Nombreuses petites
            else -> 2 + Random.nextInt(4) // Standard
        }
        
        for (i in 0 until stemCount) {
            val localRadius = 150f + Random.nextFloat() * 100f
            val localAngle = Random.nextDouble() * 2 * PI
            val localDistance = Random.nextDouble() * localRadius + 60f
            
            var stemX = groupBaseX + (cos(localAngle) * localDistance).toFloat()
            var stemY = groupBaseY + (Random.nextFloat() - 0.5f) * 70f
            
            stemX = stemX.coerceIn(marginFromEdges, screenWidth - marginFromEdges)
            
            createOrchideeStem(stemX, stemY, species)
        }
    }
    
    private fun createOrchideeStem(stemX: Float, stemY: Float, species: OrchideeSpecies) {
        // Hauteur selon l'espèce
        val heightMultiplier = when (species) {
            OrchideeSpecies.CYMBIDIUM -> 1.2f // Plus hautes
            OrchideeSpecies.CATTLEYA -> 1.1f
            OrchideeSpecies.PHALAENOPSIS -> 1.0f
            OrchideeSpecies.VANDA -> 0.9f
            OrchideeSpecies.DENDROBIUM -> 0.8f
            OrchideeSpecies.ONCIDIUM -> 0.7f // Plus petites
        }
        
        val heightVariation = 0.7f + Random.nextFloat() * 0.6f
        val maxHeight = screenHeight * maxStemHeight * heightVariation * heightMultiplier
        
        val stem = OrchideeStem(
            id = generateOrchideeStemId(),
            maxHeight = maxHeight,
            baseX = stemX,
            baseY = stemY,
            species = species,
            growthSpeedMultiplier = 0.6f + Random.nextFloat() * 0.8f
        )
        
        // Point de base avec pseudobulbe
        stem.segments.add(PointF(stemX, stemY))
        stems.add(stem)
        
        // Créer les feuilles basales immédiatement
        createBasalLeaves(stem)
        
        // ✅ CORRIGÉ: Notification avec paramètres corrects
        challengeManager?.notifyOrchideeCreated(
            orchideeX = stemX,
            orchideeY = stemY,
            orchideeId = stem.id,
            species = species.displayName
        )
    }
    
    // ==================== CROISSANCE DES TIGES ====================
    
    private fun growActiveOrchideeStems(force: Float) {
        if (currentActiveStemIndex < 0) return
        
        val activeStems = getActiveStemsForCurrentGroup()
        
        for (activeStem in activeStems) {
            if (activeStem.currentHeight >= activeStem.maxHeight) continue
            
            // Qualité de la force (stabilité)
            val forceStability = 1f - abs(force - lastForce).coerceAtMost(0.5f) * 2f
            val qualityMultiplier = 0.6f + forceStability * 0.4f
            
            // Courbe de croissance progressive
            val growthProgress = activeStem.currentHeight / activeStem.maxHeight
            val progressCurve = 1f - growthProgress * growthProgress * 0.5f
            
            val adjustedGrowth = force * qualityMultiplier * progressCurve * 
                               growthRate * 0.008f * activeStem.growthSpeedMultiplier
            
            if (adjustedGrowth > 0) {
                activeStem.currentHeight += adjustedGrowth
                
                // Ajouter des segments plus organiques
                val segmentHeight = 5f + Random.nextFloat() * 3f
                val segments = (adjustedGrowth / segmentHeight).toInt().coerceAtLeast(1)
                
                for (i in 1..segments) {
                    val currentHeight = activeStem.currentHeight - adjustedGrowth + 
                                      (adjustedGrowth * i / segments)
                    
                    // Courbure naturelle selon l'espèce
                    val curvature = when (activeStem.species) {
                        OrchideeSpecies.PHALAENOPSIS -> 0.5f
                        OrchideeSpecies.CATTLEYA -> 0.3f
                        OrchideeSpecies.DENDROBIUM -> 0.8f // Plus droites
                        OrchideeSpecies.VANDA -> 0.7f
                        OrchideeSpecies.ONCIDIUM -> 0.6f
                        OrchideeSpecies.CYMBIDIUM -> 0.4f
                    }
                    
                    val currentX = activeStem.baseX + 
                                 sin(currentHeight * 0.02f) * curvature * 
                                 (Random.nextFloat() - 0.5f) * 4f
                    val currentY = activeStem.baseY - currentHeight
                    
                    activeStem.segments.add(PointF(currentX, currentY))
                }
            }
        }
    }
    
    private fun getActiveStemsForCurrentGroup(): List<OrchideeStem> {
        if (currentActiveStemIndex == 0) {
            // Groupe principal (Phalaenopsis initial)
            return stems.filter { it.species == OrchideeSpecies.PHALAENOPSIS }
                        .take(getMainGroupSize())
        } else {
            // Groupes spécifiques par espèce
            val targetSpecies = when (stemOrderPool.getOrNull(currentActiveStemIndex)) {
                1 -> OrchideeSpecies.CATTLEYA
                2 -> OrchideeSpecies.DENDROBIUM
                3 -> OrchideeSpecies.VANDA
                4 -> OrchideeSpecies.ONCIDIUM
                5 -> OrchideeSpecies.CYMBIDIUM
                else -> OrchideeSpecies.PHALAENOPSIS
            }
            
            return stems.filter { it.species == targetSpecies }
                        .takeLast(getGroupSize(targetSpecies))
        }
    }
    
    private fun getMainGroupSize(): Int = 3 + Random.nextInt(4) // 3-6 tiges
    
    private fun getGroupSize(species: OrchideeSpecies): Int {
        return when (species) {
            OrchideeSpecies.DENDROBIUM -> 4 + Random.nextInt(4)
            OrchideeSpecies.ONCIDIUM -> 5 + Random.nextInt(3)
            OrchideeSpecies.CYMBIDIUM -> 2 + Random.nextInt(3)
            else -> 2 + Random.nextInt(4)
        }
    }
    
    // ==================== SYSTÈME DE FEUILLES ====================
    
    private fun createBasalLeaves(stem: OrchideeStem) {
        // Feuilles basales selon l'espèce
        val leafCount = when (stem.species) {
            OrchideeSpecies.PHALAENOPSIS -> 3 + Random.nextInt(3) // 3-5 feuilles
            OrchideeSpecies.CATTLEYA -> 2 + Random.nextInt(2) // 2-3 feuilles
            OrchideeSpecies.DENDROBIUM -> 4 + Random.nextInt(3) // 4-6 feuilles
            OrchideeSpecies.VANDA -> 5 + Random.nextInt(4) // 5-8 feuilles
            OrchideeSpecies.ONCIDIUM -> 2 + Random.nextInt(2) // 2-3 feuilles
            OrchideeSpecies.CYMBIDIUM -> 4 + Random.nextInt(4) // 4-7 feuilles
        }
        
        for (i in 0 until leafCount) {
            val leafType = getLeafTypeForSpecies(stem.species)
            val angle = (i * 60f + Random.nextFloat() * 30f - 15f) % 360f
            val size = getLeafSizeForSpecies(stem.species) * (0.8f + Random.nextFloat() * 0.4f)
            
            val leaf = OrchideeLeaf(
                attachmentPoint = PointF(stem.baseX, stem.baseY),
                angle = angle,
                length = size,
                width = size * 0.4f,
                leafType = leafType,
                growthProgress = 0f
            )
            
            stem.leaves.add(leaf)
        }
    }
    
    private fun createLeavesOnStems() {
        for (stem in stems) {
            if (stem.segments.size < 3) continue
            if (stem.currentHeight < 20f) continue
            
            // Feuilles caulinaires selon l'espèce
            val needsCaulineLeaves = when (stem.species) {
                OrchideeSpecies.DENDROBIUM -> true
                OrchideeSpecies.CYMBIDIUM -> true
                else -> false
            }
            
            if (needsCaulineLeaves && stem.caulineLeaves.isEmpty()) {
                val leafCount = 2 + Random.nextInt(3)
                for (i in 0 until leafCount) {
                    val heightRatio = 0.3f + (i.toFloat() / leafCount) * 0.5f
                    val segmentIndex = (stem.segments.size * heightRatio).toInt()
                        .coerceIn(1, stem.segments.size - 1)
                    
                    val attachmentPoint = stem.segments[segmentIndex]
                    val angle = Random.nextFloat() * 360f
                    val size = getLeafSizeForSpecies(stem.species) * 0.6f
                    
                    val leaf = OrchideeLeaf(
                        attachmentPoint = attachmentPoint,
                        angle = angle,
                        length = size,
                        width = size * 0.3f,
                        leafType = getLeafTypeForSpecies(stem.species),
                        growthProgress = 0f
                    )
                    
                    stem.caulineLeaves.add(leaf)
                }
            }
        }
    }
    
    private fun growExistingLeaves(force: Float) {
        if (force <= forceThreshold) return
        
        for (stem in stems) {
            // Croissance des feuilles basales
            for (leaf in stem.leaves) {
                if (leaf.growthProgress < 1f) {
                    val growth = force * 600f * 0.008f
                    leaf.growthProgress = (leaf.growthProgress + growth).coerceAtMost(1f)
                }
            }
            
            // Croissance des feuilles caulinaires
            for (leaf in stem.caulineLeaves) {
                if (leaf.growthProgress < 1f) {
                    val growth = force * 400f * 0.008f
                    leaf.growthProgress = (leaf.growthProgress + growth).coerceAtMost(1f)
                }
            }
        }
    }
    
    private fun getLeafTypeForSpecies(species: OrchideeSpecies): OrchideeLeafType {
        return when (species) {
            OrchideeSpecies.PHALAENOPSIS -> OrchideeLeafType.STRAP_SHAPED
            OrchideeSpecies.CATTLEYA -> OrchideeLeafType.OVAL_THICK
            OrchideeSpecies.DENDROBIUM -> OrchideeLeafType.NEEDLE_THIN
            OrchideeSpecies.VANDA -> OrchideeLeafType.STRAP_SHAPED
            OrchideeSpecies.ONCIDIUM -> OrchideeLeafType.OVAL_THICK
            OrchideeSpecies.CYMBIDIUM -> OrchideeLeafType.BROAD_FLAT
        }
    }
    
    private fun getLeafSizeForSpecies(species: OrchideeSpecies): Float {
        return when (species) {
            OrchideeSpecies.PHALAENOPSIS -> 80f + Random.nextFloat() * 20f
            OrchideeSpecies.CATTLEYA -> 60f + Random.nextFloat() * 15f
            OrchideeSpecies.DENDROBIUM -> 40f + Random.nextFloat() * 10f
            OrchideeSpecies.VANDA -> 100f + Random.nextFloat() * 30f
            OrchideeSpecies.ONCIDIUM -> 50f + Random.nextFloat() * 12f
            OrchideeSpecies.CYMBIDIUM -> 120f + Random.nextFloat() * 40f
        }
    }
    
    // ==================== SYSTÈME DE FLEURS ====================
    
    private fun createFlowersOnMatureStems() {
        for (stem in stems) {
            if (stem.segments.size < 3) continue
            if (stem.currentHeight < minimumHeightForFlowers) continue
            if (stem.flowerSpikes.isNotEmpty()) continue // Déjà fleuri
            
            // Démarrer la floraison selon l'espèce
            when (stem.species) {
                OrchideeSpecies.DENDROBIUM -> createDendrobiumCluster(stem)
                OrchideeSpecies.CYMBIDIUM -> createCymbidiumSpike(stem)
                OrchideeSpecies.ONCIDIUM -> createOncidiumBranch(stem)
                else -> createSingleFlower(stem)
            }
        }
    }
    
    private fun createSingleFlower(stem: OrchideeStem) {
        // Une seule fleur pour Phalaenopsis, Cattleya, Vanda
        val genetics = OrchideeGeneticsGenerator.generate()
        val flowerPosition = getFlowerPosition(stem)
        val flowerId = generateOrchideeFlowerId()
        
        val flower = OrchideeFlower(
            genetics = genetics,
            position = flowerPosition,
            sizeMultiplier = 0.8f + Random.nextFloat() * 0.4f,
            angle = Random.nextFloat() * 360f,
            bloomProgress = 0.001f, // ✅ DÉMARRER AVEC UN TRÈS PETIT BOURGEON
            renderLayer = 30 + Random.nextInt(20),
            clusterId = stem.id,
            attachmentStemId = stem.id, // ✅ LIER À LA TIGE
            relativePosition = calculateRelativePosition(stem, flowerPosition) // ✅ POSITION RELATIVE
        )
        
        flowers.add(flower)
        stem.flowerSpikes.add(flower)
        
        // ✅ Notification seulement quand la fleur devient visible (bloomProgress > 0.1)
        // La notification se fera dans growExistingFlowers()
    }
    
    private fun createDendrobiumCluster(stem: OrchideeStem) {
        // Grappe de 3-8 petites fleurs
        val clusterSize = 3 + Random.nextInt(6)
        val genetics = OrchideeGeneticsGenerator.generateWithConstraints(
            forceSpecies = OrchideeSpecies.DENDROBIUM
        )
        
        for (i in 0 until clusterSize) {
            val basePosition = getFlowerPosition(stem)
            val clusterOffset = PointF(
                basePosition.x + (Random.nextFloat() - 0.5f) * 30f,
                basePosition.y + (Random.nextFloat() - 0.5f) * 20f
            )
            val flowerId = generateOrchideeFlowerId()
            
            val flower = OrchideeFlower(
                genetics = genetics,
                position = clusterOffset,
                sizeMultiplier = 0.6f + Random.nextFloat() * 0.3f,
                angle = Random.nextFloat() * 360f,
                bloomProgress = 0.001f, // ✅ DÉMARRER AVEC UN TRÈS PETIT BOURGEON
                renderLayer = 25 + Random.nextInt(15),
                clusterId = stem.id,
                attachmentStemId = stem.id, // ✅ LIER À LA TIGE
                relativePosition = calculateRelativePosition(stem, clusterOffset) // ✅ POSITION RELATIVE
            )
            
            flowers.add(flower)
            stem.flowerSpikes.add(flower)
            
            // ✅ Notification différée
        }
    }
    
    private fun createCymbidiumSpike(stem: OrchideeStem) {
        // Épi de 5-12 grandes fleurs
        val spikeSize = 5 + Random.nextInt(8)
        val genetics = OrchideeGeneticsGenerator.generateWithConstraints(
            forceSpecies = OrchideeSpecies.CYMBIDIUM
        )
        
        val spikeStart = getFlowerPosition(stem)
        val spikeLength = 80f + Random.nextFloat() * 40f
        
        for (i in 0 until spikeSize) {
            val ratio = i.toFloat() / (spikeSize - 1)
            val flowerPosition = PointF(
                spikeStart.x + (Random.nextFloat() - 0.5f) * 10f,
                spikeStart.y - ratio * spikeLength
            )
            val flowerId = generateOrchideeFlowerId()
            
            val flower = OrchideeFlower(
                genetics = genetics,
                position = flowerPosition,
                sizeMultiplier = 1.0f + Random.nextFloat() * 0.3f,
                angle = Random.nextFloat() * 360f,
                bloomProgress = 0.001f, // ✅ DÉMARRER AVEC UN TRÈS PETIT BOURGEON
                renderLayer = 35 + Random.nextInt(20),
                clusterId = stem.id,
                attachmentStemId = stem.id, // ✅ LIER À LA TIGE
                relativePosition = calculateRelativePosition(stem, flowerPosition) // ✅ POSITION RELATIVE
            )
            
            flowers.add(flower)
            stem.flowerSpikes.add(flower)
            
            // ✅ Notification différée
        }
    }
    
    private fun createOncidiumBranch(stem: OrchideeStem) {
        // Branches ramifiées avec nombreuses petites fleurs
        val branchCount = 2 + Random.nextInt(3)
        val genetics = OrchideeGeneticsGenerator.generateWithConstraints(
            forceSpecies = OrchideeSpecies.ONCIDIUM
        )
        
        val basePosition = getFlowerPosition(stem)
        
        for (branch in 0 until branchCount) {
            val branchAngle = branch * 60f + Random.nextFloat() * 30f
            val branchLength = 40f + Random.nextFloat() * 20f
            val flowersOnBranch = 4 + Random.nextInt(6)
            
            for (i in 0 until flowersOnBranch) {
                val distance = (i.toFloat() / flowersOnBranch) * branchLength
                val angleRad = Math.toRadians(branchAngle.toDouble())
                
                val flowerPosition = PointF(
                    basePosition.x + (cos(angleRad) * distance).toFloat(),
                    basePosition.y + (sin(angleRad) * distance).toFloat()
                )
                val flowerId = generateOrchideeFlowerId()
                
                val flower = OrchideeFlower(
                    genetics = genetics,
                    position = flowerPosition,
                    sizeMultiplier = 0.5f + Random.nextFloat() * 0.2f,
                    angle = Random.nextFloat() * 360f,
                    bloomProgress = 0.001f, // ✅ DÉMARRER AVEC UN TRÈS PETIT BOURGEON
                    renderLayer = 20 + Random.nextInt(15),
                    clusterId = stem.id,
                    attachmentStemId = stem.id, // ✅ LIER À LA TIGE
                    relativePosition = calculateRelativePosition(stem, flowerPosition) // ✅ POSITION RELATIVE
                )
                
                flowers.add(flower)
                stem.flowerSpikes.add(flower)
                
                // ✅ Notification différée
            }
        }
    }
    
    private fun getFlowerPosition(stem: OrchideeStem): PointF {
        if (stem.segments.isEmpty()) return PointF(stem.baseX, stem.baseY)
        
        // Position sur les 30% supérieurs de la tige
        val topSegments = stem.segments.takeLast((stem.segments.size * 0.3f).toInt().coerceAtLeast(1))
        val selectedSegment = topSegments.random()
        
        return PointF(
            selectedSegment.x + (Random.nextFloat() - 0.5f) * 20f,
            selectedSegment.y + (Random.nextFloat() - 0.5f) * 15f
        )
    }
    
    private fun growExistingFlowers(force: Float) {
        if (force <= forceThreshold) return
        
        for (flower in flowers) {
            if (flower.bloomProgress < 1f) {
                // ✅ CROISSANCE PLUS LENTE ET PROGRESSIVE
                val growth = force * 200f * 0.008f // Réduit de 400f à 200f
                val oldProgress = flower.bloomProgress
                flower.bloomProgress = (flower.bloomProgress + growth).coerceAtMost(1f)
                
                // ✅ Notification seulement quand la fleur devient visible (seuil 0.15)
                if (oldProgress < 0.15f && flower.bloomProgress >= 0.15f) {
                    val flowerId = generateOrchideeFlowerId()
                    challengeManager?.notifyFlowerCreated(flower.position.x, flower.position.y, flowerId)
                }
            }
        }
    }
    
    private fun updateFlowerPositions() {
        // ✅ FIXER LES POSITIONS RELATIVES AUX TIGES - PLUS DE DÉPLACEMENT
        for (flower in flowers) {
            val stem = stems.find { it.id == flower.attachmentStemId } ?: continue
            
            // ✅ Recalculer la position absolue basée sur la position relative fixe
            val attachmentPoint = getAttachmentPointFromRelative(stem, flower.relativePosition)
            
            // ✅ Mise à jour sans mouvement aléatoire
            flower.position.x = attachmentPoint.x
            flower.position.y = attachmentPoint.y
            
            // ✅ SUPPRIMÉ: Effet de vent qui causait le déplacement
            // val windEffect = sin(System.currentTimeMillis() * 0.001f) * 0.5f
            // flower.position.x += windEffect
        }
    }
    
    // ✅ NOUVELLES FONCTIONS POUR POSITIONS RELATIVES
    private fun calculateRelativePosition(stem: OrchideeStem, absolutePosition: PointF): PointF {
        if (stem.segments.isEmpty()) return PointF(0f, 0f)
        
        // Calculer la position relative par rapport à la base de la tige
        return PointF(
            absolutePosition.x - stem.baseX,
            absolutePosition.y - stem.baseY
        )
    }
    
    private fun getAttachmentPointFromRelative(stem: OrchideeStem, relativePosition: PointF): PointF {
        // Retourner la position absolue basée sur la position relative fixe
        return PointF(
            stem.baseX + relativePosition.x,
            stem.baseY + relativePosition.y
        )
    }
    
    // ==================== STATS ET DEBUGGING ====================
    
    fun getOrchideeStats(): String {
        val stemCount = stems.size
        val flowerCount = flowers.size
        val speciesCount = stems.map { it.species }.distinct().size
        
        return "Orchidées: $stemCount tiges, $flowerCount fleurs, $speciesCount espèces"
    }
    
    fun getCurrentSpecies(): List<String> {
        return stems.map { it.species.displayName }.distinct()
    }
}

// ==================== DATA CLASSES ORCHIDÉES ====================

data class OrchideeStem(
    val id: String,
    var currentHeight: Float = 0f,
    val maxHeight: Float,
    val baseX: Float,
    val baseY: Float,
    val species: OrchideeSpecies,
    val growthSpeedMultiplier: Float,
    val segments: MutableList<PointF> = mutableListOf(),
    val leaves: MutableList<OrchideeLeaf> = mutableListOf(),
    val caulineLeaves: MutableList<OrchideeLeaf> = mutableListOf(),
    val flowerSpikes: MutableList<OrchideeFlower> = mutableListOf()
)

data class OrchideeFlower(
    val genetics: OrchideeGenetics,
    val position: PointF,
    val sizeMultiplier: Float,
    val angle: Float,
    var bloomProgress: Float,
    val renderLayer: Int,
    val clusterId: String,
    val attachmentStemId: String, // ✅ ID de la tige d'attachement
    val relativePosition: PointF // ✅ Position relative fixe par rapport à la tige
)

data class OrchideeLeaf(
    val attachmentPoint: PointF,
    val angle: Float,
    val length: Float,
    val width: Float,
    val leafType: OrchideeLeafType,
    var growthProgress: Float
)

enum class OrchideeLeafType {
    STRAP_SHAPED,    // Phalaenopsis, Vanda
    OVAL_THICK,      // Cattleya, Oncidium  
    NEEDLE_THIN,     // Dendrobium
    BROAD_FLAT       // Cymbidium
}

// ==================== OPTIMISEUR DE RENDU ====================

class OrchideeOptimizer(private val screenWidth: Int, private val screenHeight: Int) {
    
    private val marginTop = -100f
    private val marginBottom = screenHeight + 100f
    private val marginLeft = -100f
    private val marginRight = screenWidth + 100f
    
    fun isStemVisible(stem: OrchideeStem): Boolean {
        if (stem.segments.isEmpty()) return false
        
        for (point in stem.segments) {
            if (point.x >= marginLeft && point.x <= marginRight &&
                point.y >= marginTop && point.y <= marginBottom) {
                return true
            }
        }
        return false
    }
    
    fun isFlowerVisible(flower: OrchideeFlower): Boolean {
        val flowerRadius = baseFlowerSize * flower.sizeMultiplier * 0.5f
        return flower.position.x + flowerRadius >= marginLeft && 
               flower.position.x - flowerRadius <= marginRight &&
               flower.position.y + flowerRadius >= marginTop && 
               flower.position.y - flowerRadius <= marginBottom
    }
    
    companion object {
        private const val baseFlowerSize = 60f
    }
}
