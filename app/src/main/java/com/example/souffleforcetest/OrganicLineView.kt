package com.example.souffleforcetest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class OrganicLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    // Renderer pour le dessin
    private val plantRenderer = PlantRenderer(context)
    
    // Chargement des bitmaps (gardés pour compatibilité mais pas utilisés)
    private lateinit var stemBitmap: Bitmap
    private lateinit var leafBitmap: Bitmap
    private lateinit var flowerBitmap: Bitmap
    
    private val resetButtonPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    
    private val resetTextPaint = Paint().apply {
        color = 0xFFFFFFFF.toInt()
        textSize = 80f
        isAntiAlias = true
        textAlign = Paint.Align.LEFT
    }
    
    // Variables de croissance - MODIFIABLES
    private var currentForce = 0.0f
    private var previousForce = 0.0f
    private var currentHeight = 0f
    private var currentStrokeWidth = 4f
    private var offsetX = 0f
    private var maxHeight = 0f
    private var baseX = 0f
    private var baseY = 0f
    private var showResetButton = false
    private var resetButtonX = 0f
    private var resetButtonY = 0f
    private val resetButtonRadius = 175f
    
    // États du système
    private var lightState = LightState.YELLOW
    private var stateStartTime = 0L
    private var canGrow = false
    
    enum class LightState {
        YELLOW, GREEN_GROW, GREEN_LEAVES, GREEN_FLOWER, RED
    }
    
    data class TracePoint(
        val x: Float, val y: Float, val strokeWidth: Float,
        val waveFrequency: Float, val waveAmplitude: Float, val curvature: Float
    )
    
    // NOUVEAU : Structure pour les branches
    data class Branch(
        val id: Int,
        val startPoint: TracePoint,
        val tracedPath: MutableList<TracePoint>,
        var isActive: Boolean = true,
        val growthMultiplier: Float = 1f, // Variation de vitesse
        var currentHeight: Float = 0f,
        var offsetX: Float = 0f,
        var currentStrokeWidth: Float = 0f,
        var fleur: Fleur? = null
    )
    
    data class Bourgeon(val x: Float, val y: Float, var taille: Float)
    data class Feuille(val bourgeon: Bourgeon, var longueur: Float, var largeur: Float, val angle: Float, var maxLargeurAtteinte: Boolean = false)
    data class Fleur(var x: Float, var y: Float, var taille: Float, var petalCount: Int, val sizeMultiplier: Float = 1f) // Variation de taille
    
    // NOUVEAU : Système de branches multiples
    private val branches = mutableListOf<Branch>()
    private var branchIdCounter = 0
    private var mainBranch: Branch? = null
    
    private val tracedPath = mutableListOf<TracePoint>() // Gardé pour compatibilité
    private val bourgeons = mutableListOf<Bourgeon>()
    private val feuilles = mutableListOf<Feuille>()
    private var fleur: Fleur? = null // Gardé pour compatibilité
    
    // PARAMÈTRES DE CROISSANCE - MODIFIABLES
    private val forceThreshold = 0.08f
    private val growthRate = 174.6f
    private val baseStrokeWidth = 9.6f
    private val maxStrokeWidth = 25.6f
    private val strokeDecayRate = 0.2f
    private val abruptThreshold = 0.15f
    private val centeringRate = 0.99f
    private val waveThreshold = 0.03f
    private val maxWaveAmplitude = 15f
    
    // NOUVEAUX paramètres pour feuilles réalistes réduites au 1/3
    private val maxLeafWidth = 150f  // 450f / 3 = 150f
    private val maxLeafLength = 400f // 1200f / 3 = 400f
    
    // Compteur pour alternance des feuilles
    private var leafSideCounter = 0
    
    init {
        try {
            stemBitmap = BitmapFactory.decodeResource(resources, R.drawable.stem_segment)
            leafBitmap = BitmapFactory.decodeResource(resources, R.drawable.leaf)
            flowerBitmap = BitmapFactory.decodeResource(resources, R.drawable.flower)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun drawTrafficLight(canvas: Canvas) {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - stateStartTime
        
        val lightRadius = when (lightState) {
            LightState.YELLOW -> width * 0.4f
            else -> resetButtonRadius
        }
        
        val lightX = if (lightState == LightState.YELLOW) width / 2f else resetButtonX
        val lightY = if (lightState == LightState.YELLOW) height / 2f else resetButtonY
        
        // Dessiner le cercle
        resetButtonPaint.color = 0x40000000.toInt()
        canvas.drawCircle(lightX + 8f, lightY + 8f, lightRadius, resetButtonPaint)
        
        when (lightState) {
            LightState.YELLOW -> resetButtonPaint.color = 0xFFFFD700.toInt()
            LightState.GREEN_GROW -> resetButtonPaint.color = 0xFF2F4F2F.toInt()
            LightState.GREEN_LEAVES -> resetButtonPaint.color = 0xFF00FF00.toInt()
            LightState.GREEN_FLOWER -> resetButtonPaint.color = 0xFFFF69B4.toInt()
            LightState.RED -> resetButtonPaint.color = 0xFFFF0000.toInt()
        }
        canvas.drawCircle(lightX, lightY, lightRadius, resetButtonPaint)
        
        resetButtonPaint.color = 0xFF333333.toInt()
        resetButtonPaint.style = Paint.Style.STROKE
        resetButtonPaint.strokeWidth = 12f
        canvas.drawCircle(lightX, lightY, lightRadius, resetButtonPaint)
        resetButtonPaint.style = Paint.Style.FILL
        
        val timeRemaining = when (lightState) {
            LightState.YELLOW -> kotlin.math.max(0, 2 - (elapsedTime / 1000))
            LightState.GREEN_GROW -> kotlin.math.max(0, 3 - (elapsedTime / 1000))
            LightState.GREEN_LEAVES -> kotlin.math.max(0, 3 - (elapsedTime / 1000))
            LightState.GREEN_FLOWER -> kotlin.math.max(0, 3 - (elapsedTime / 1000))
            LightState.RED -> 0
        }
        
        val mainText = when (lightState) {
            LightState.YELLOW -> "INSPIREZ"
            LightState.GREEN_GROW -> "SOUFFLEZ"
            LightState.GREEN_LEAVES -> "SOUFFLEZ"
            LightState.GREEN_FLOWER -> "SOUFFLEZ"
            LightState.RED -> "↻"
        }
        
        // Texte sur le cercle (centré)
        if (lightState == LightState.YELLOW) {
            resetTextPaint.textAlign = Paint.Align.CENTER
            resetTextPaint.textSize = 180f
            resetTextPaint.color = 0xFF000000.toInt()
            canvas.drawText(mainText, lightX, lightY, resetTextPaint)
            
            if (timeRemaining > 0) {
                resetTextPaint.textSize = 108f
                canvas.drawText(timeRemaining.toString(), lightX, lightY + 144f, resetTextPaint)
            }
        } else if (lightState == LightState.RED) {
            resetTextPaint.textAlign = Paint.Align.CENTER
            resetTextPaint.textSize = 120f
            resetTextPaint.color = 0xFF000000.toInt()
            canvas.drawText(mainText, lightX, lightY, resetTextPaint)
        }
    }
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        baseX = w / 2.0f
        baseY = h - 100f
        maxHeight = h - 150f
        
        resetButtonX = w - resetButtonRadius - 50f
        resetButtonY = resetButtonRadius + 80f
        
        if (tracedPath.isEmpty()) {
            val initialPoint = TracePoint(baseX, baseY, baseStrokeWidth, 0f, 0f, 0f)
            tracedPath.add(initialPoint)
            
            // NOUVEAU : Créer la branche principale
            mainBranch = Branch(
                id = branchIdCounter++,
                startPoint = initialPoint,
                tracedPath = mutableListOf(initialPoint),
                growthMultiplier = 1f,
                currentStrokeWidth = baseStrokeWidth
            )
            branches.add(mainBranch!!)
        }
    }
    
    fun startCycle() {
        lightState = LightState.YELLOW
        stateStartTime = System.currentTimeMillis()
        invalidate()
    }
    
    fun updateForce(force: Float) {
        updateLightState()
        
        when (lightState) {
            LightState.YELLOW -> return
            LightState.GREEN_GROW -> growAllBranches(force)
            LightState.GREEN_LEAVES -> growLeaves(force)
            LightState.GREEN_FLOWER -> growAllFlowers(force)
            LightState.RED -> return
        }
        
        invalidate()
    }
    
    // NOUVELLE logique de croissance multi-branches
    private fun growAllBranches(force: Float) {
        val rhythmIntensity = kotlin.math.abs(force - previousForce)
        previousForce = force
        
        // Créer nouvelle branche si bruit saccadé
        if (rhythmIntensity > abruptThreshold && branches.isNotEmpty()) {
            createNewBranch()
        }
        
        // Faire pousser toutes les branches actives
        for (branch in branches.filter { it.isActive }) {
            growBranch(branch, force, rhythmIntensity)
        }
        
        // Mettre à jour la compatibilité (branche principale)
        mainBranch?.let { main ->
            if (main.tracedPath.isNotEmpty()) {
                tracedPath.clear()
                tracedPath.addAll(main.tracedPath)
                currentHeight = main.currentHeight
                fleur = main.fleur
            }
        }
    }
    
    private fun createNewBranch() {
        // Choisir une branche existante aléatoirement pour la ramification
        val parentBranch = branches.filter { it.isActive && it.tracedPath.size > 5 }.randomOrNull()
        parentBranch?.let { parent ->
            // Point de ramification au milieu/haut de la branche parente
            val branchPointIndex = (parent.tracedPath.size * 0.6f).toInt().coerceAtMost(parent.tracedPath.size - 1)
            val branchPoint = parent.tracedPath[branchPointIndex]
            
            // Variations naturelles pour la nouvelle branche
            val growthVariation = 0.7f + (0..6).random() * 0.1f // 0.7x à 1.3x
            val sizeVariation = 0.8f + (0..4).random() * 0.1f   // 0.8x à 1.2x
            
            // Angle de ramification naturel
            val branchAngle = (30..60).random().toFloat() * if ((0..1).random() == 0) 1f else -1f
            val branchOffset = kotlin.math.sin(Math.toRadians(branchAngle.toDouble())).toFloat() * 20f
            
            val newBranchPoint = TracePoint(
                branchPoint.x + branchOffset,
                branchPoint.y,
                baseStrokeWidth * 0.8f, // Branches plus fines
                0f, 0f, 0f
            )
            
            val newBranch = Branch(
                id = branchIdCounter++,
                startPoint = newBranchPoint,
                tracedPath = mutableListOf(newBranchPoint),
                growthMultiplier = growthVariation,
                currentStrokeWidth = baseStrokeWidth * 0.8f
            )
            
            branches.add(newBranch)
        }
    }
    
    private fun growBranch(branch: Branch, force: Float, rhythmIntensity: Float) {
        if (force > forceThreshold) {
            val adjustedForce = (force - forceThreshold) * branch.growthMultiplier
            val previousHeight = branch.currentHeight
            branch.currentHeight += adjustedForce * growthRate * 0.8f // Branches un peu plus lentes
            branch.currentHeight = kotlin.math.min(branch.currentHeight, maxHeight)
            
            if (branch.currentHeight > previousHeight && branch.currentHeight > 0) {
                val lastPoint = branch.tracedPath.last()
                val currentY = lastPoint.y - (branch.currentHeight - previousHeight)
                val currentX = lastPoint.x + branch.offsetX
                
                var waveFreq = 0f
                var waveAmp = 0f
                var curvature = 0f
                
                // Réponse aux ondulations (chaque branche réagit différemment)
                if (rhythmIntensity > 0.01f && rhythmIntensity < abruptThreshold) {
                    waveFreq = (rhythmIntensity * 12f * branch.growthMultiplier) + 1f
                    waveAmp = (rhythmIntensity * 150f * branch.growthMultiplier).coerceAtMost(maxWaveAmplitude)
                }
                
                if (rhythmIntensity > 0.04f) {
                    curvature = (rhythmIntensity * 30f * branch.growthMultiplier).coerceAtMost(15f)
                    if ((0..1).random() == 0) curvature = -curvature
                }
                
                val newPoint = TracePoint(currentX, currentY, branch.currentStrokeWidth, waveFreq, waveAmp, curvature)
                branch.tracedPath.add(newPoint)
            }
        }
        
        // Oscillations individuelles par branche
        if (rhythmIntensity > abruptThreshold) {
            val displacement = if ((0..1).random() == 0) {
                (75f + rhythmIntensity * 100f) * branch.growthMultiplier
            } else {
                -(75f + rhythmIntensity * 100f) * branch.growthMultiplier
            }
            branch.offsetX += displacement
        }
        
        // Retour au centre pour chaque branche
        branch.offsetX *= centeringRate
        
        // Désactiver les branches trop courtes après un certain temps
        if (branch.currentHeight < 50f && branches.size > 3) {
            branch.isActive = false
        }
    }
    
    // NOUVELLE logique de croissance des feuilles en 2 phases avec meilleure répartition
    private fun growLeaves(force: Float) {
        if (force > forceThreshold) {
            val adjustedForce = force - forceThreshold
            val growthIncrement = adjustedForce * growthRate * 0.08f
            
            for (bourgeon in bourgeons) {
                if (bourgeon.taille > 2f) {
                    var feuille = feuilles.find { it.bourgeon == bourgeon }
                    if (feuille == null) {
                        val stemX = tracedPath.minByOrNull { 
                            val dx = it.x - bourgeon.x
                            val dy = it.y - bourgeon.y
                            dx * dx + dy * dy
                        }?.x ?: bourgeon.x
                        
                        val isRightSide = bourgeon.x > stemX
                        
                        // AMÉLIORATION : Angles plus naturels et variés
                        val baseAngle = if (isRightSide) {
                            -25f  // Angle de base pour droite
                        } else {
                            205f  // Angle de base pour gauche
                        }
                        
                        // Variation naturelle selon la hauteur
                        val heightFactor = bourgeon.y / height.toFloat()
                        val heightVariation = (heightFactor - 0.5f) * 30f  // -15° à +15° selon hauteur
                        val randomVariation = ((-15..15).random()).toFloat()
                        
                        val finalAngle = baseAngle + heightVariation + randomVariation
                        
                        feuille = Feuille(bourgeon, 0f, 0f, finalAngle, false)
                        feuilles.add(feuille)
                    }
                    
                    // PHASE 1 : Croissance largeur ET longueur jusqu'à largeur max
                    if (!feuille.maxLargeurAtteinte) {
                        val lengthGrowth = growthIncrement * 0.3f
                        val widthGrowth = growthIncrement * 0.35f  // Croît un peu plus vite en largeur
                        
                        feuille.longueur += lengthGrowth
                        feuille.largeur += widthGrowth
                        
                        // Vérifier si largeur max atteinte
                        if (feuille.largeur >= maxLeafWidth) {
                            feuille.largeur = maxLeafWidth
                            feuille.maxLargeurAtteinte = true
                        }
                        
                        // Limites pour phase 1 (réduites au 1/3)
                        feuille.longueur = kotlin.math.min(feuille.longueur, 200f)
                    } 
                    // PHASE 2 : Seulement croissance en longueur
                    else {
                        val lengthGrowth = growthIncrement * 0.5f  // Plus rapide maintenant
                        feuille.longueur += lengthGrowth
                        feuille.longueur = kotlin.math.min(feuille.longueur, maxLeafLength)
                        // Largeur reste fixe à maxLeafWidth
                    }
                }
            }
        }
    }
    
    private fun growAllFlowers(force: Float) {
        if (force > forceThreshold) {
            val adjustedForce = force - forceThreshold
            val growthIncrement = adjustedForce * growthRate * 0.08f
            
            // Faire pousser une fleur au bout de chaque branche active
            for (branch in branches.filter { it.isActive && it.tracedPath.isNotEmpty() }) {
                val topPoint = branch.tracedPath.last()
                
                if (branch.fleur == null) {
                    // Variation de taille finale pour chaque fleur
                    val sizeVariation = 0.7f + (0..6).random() * 0.1f // 0.7x à 1.3x
                    branch.fleur = Fleur(topPoint.x, topPoint.y, 0f, 6, sizeVariation)
                }
                
                branch.fleur?.let { flower ->
                    val branchGrowthIncrement = growthIncrement * branch.growthMultiplier
                    flower.taille += branchGrowthIncrement * 0.15f
                    flower.taille = kotlin.math.min(flower.taille, 175f * flower.sizeMultiplier)
                    flower.petalCount = kotlin.math.max(5, (flower.taille * 0.05f).toInt())
                    flower.x = topPoint.x
                    flower.y = topPoint.y
                }
            }
            
            // Mettre à jour la fleur principale pour compatibilité
            mainBranch?.fleur?.let { fleur = it }
        }
    }
    
    private fun updateLightState() {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - stateStartTime
        
        when (lightState) {
            LightState.YELLOW -> {
                canGrow = false
                if (elapsedTime >= 2000) {
                    lightState = LightState.GREEN_GROW
                    stateStartTime = currentTime
                }
            }
            LightState.GREEN_GROW -> {
                canGrow = true
                if (elapsedTime >= 3000) {
                    lightState = LightState.GREEN_LEAVES
                    stateStartTime = currentTime
                    canGrow = false
                }
            }
            LightState.GREEN_LEAVES -> {
                canGrow = false
                if (elapsedTime >= 3000) {
                    lightState = LightState.GREEN_FLOWER
                    stateStartTime = currentTime
                }
            }
            LightState.GREEN_FLOWER -> {
                canGrow = false
                if (elapsedTime >= 3000) {
                    lightState = LightState.RED
                    stateStartTime = currentTime
                }
            }
            LightState.RED -> {
                canGrow = false
            }
        }
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        val time = System.currentTimeMillis() * 0.002f
        
        // Dessiner toutes les branches
        for (branch in branches.filter { it.isActive }) {
            plantRenderer.drawRealisticStem(canvas, branch.tracedPath, time, baseStrokeWidth, maxStrokeWidth)
            
            if (branch.tracedPath.isNotEmpty()) {
                plantRenderer.drawGrowthPoint(canvas, branch.tracedPath.last(), time)
            }
            
            // Dessiner la fleur de cette branche
            plantRenderer.drawRealisticFlower(canvas, branch.fleur, time)
        }
        
        plantRenderer.drawAttachmentPoints(canvas, bourgeons, time)
        plantRenderer.drawRealistic3DLeaves(canvas, feuilles, time)
        
        drawTrafficLight(canvas)
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && lightState == LightState.RED) {
            val lightX = resetButtonX
            val lightY = resetButtonY
            val lightRadius = resetButtonRadius
            
            val dx = event.x - lightX
            val dy = event.y - lightY
            val distance = kotlin.math.sqrt(dx * dx + dy * dy)
            
            if (distance <= lightRadius) {
                resetPlant()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
    
    private fun resetPlant() {
        tracedPath.clear()
        bourgeons.clear()
        feuilles.clear()
        fleur = null
        branches.clear()
        branchIdCounter = 0
        mainBranch = null
        
        currentHeight = 0f
        currentStrokeWidth = baseStrokeWidth
        offsetX = 0f
        showResetButton = false
        leafSideCounter = 0
        
        lightState = LightState.YELLOW
        stateStartTime = System.currentTimeMillis()
        canGrow = false
        
        // Recréer la branche principale
        val initialPoint = TracePoint(baseX, baseY, baseStrokeWidth, 0f, 0f, 0f)
        tracedPath.add(initialPoint)
        mainBranch = Branch(
            id = branchIdCounter++,
            startPoint = initialPoint,
            tracedPath = mutableListOf(initialPoint),
            growthMultiplier = 1f,
            currentStrokeWidth = baseStrokeWidth
        )
        branches.add(mainBranch!!)
        
        invalidate()
    }
}
