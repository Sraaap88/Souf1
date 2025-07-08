package com.example.souffleforcetest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.*

class OrganicLineView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    
    // ==================== UI ELEMENTS ====================
    
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
    
    private val stemPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.rgb(50, 120, 50)
    }
    
    private val branchPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.rgb(40, 100, 40)
    }
    
    // AJOUT - Paint pour les feuilles
    private val leafPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.rgb(34, 139, 34)
    }
    
    // AJOUT - Paint pour les fleurs
    private val flowerPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        color = Color.WHITE
    }
    
    private val flowerCenterPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.rgb(255, 200, 50)
    }
    
    // ==================== ÉTATS DU SYSTÈME ====================
    
    private var showResetButton = false
    private var resetButtonX = 0f
    private var resetButtonY = 0f
    private val resetButtonRadius = 175f
    
    private var lightState = LightState.START  // MODIFIÉ : Nouvel état initial
    private var stateStartTime = 0L
    
    // ==================== LOGIQUE DE PLANTE ====================
    
    private var plantStem: PlantStem? = null
    
    enum class LightState {
        START,          // NOUVEAU : État initial avec bouton vert DÉMARRER
        YELLOW,         // Bouton jaune INSPIREZ (2s)
        GREEN_GROW,     // Croissance des tiges (5s)
        GREEN_LEAVES,   // Croissance des feuilles (4s)
        GREEN_FLOWER,   // Croissance des fleurs (5s)
        RED             // Reset
    }
    
    // ==================== GESTION DE L'ÉCRAN ====================
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        resetButtonX = w - resetButtonRadius - 50f
        resetButtonY = resetButtonRadius + 80f
        
        plantStem = PlantStem(w, h)
    }
    
    // ==================== CONTRÔLE DU CYCLE ====================
    
    fun startCycle() {
        lightState = LightState.START  // MODIFIÉ : Commencer par l'état START
        stateStartTime = System.currentTimeMillis()
        showResetButton = false
        plantStem?.resetStem()
        invalidate()
    }
    
    fun updateForce(force: Float) {
        // MODIFIÉ : Mettre à jour les états seulement si on n'est pas dans START
        if (lightState != LightState.START) {
            updateLightState()
            
            if (lightState == LightState.GREEN_GROW) {
                val phaseTime = System.currentTimeMillis() - stateStartTime
                plantStem?.processStemGrowth(force, phaseTime)
            }
            
            // AJOUT - Croissance des feuilles pendant GREEN_LEAVES
            if (lightState == LightState.GREEN_LEAVES) {
                plantStem?.processLeavesGrowth(force)
            }
            
            // AJOUT - Croissance des fleurs pendant GREEN_FLOWER
            if (lightState == LightState.GREEN_FLOWER) {
                plantStem?.processFlowerGrowth(force)
            }
            
            if (!showResetButton && (plantStem?.getStemHeight() ?: 0f) > 30f) {
                showResetButton = true
            }
        }
        
        invalidate()
    }
    
    private fun updateLightState() {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - stateStartTime
        
        when (lightState) {
            LightState.START -> {
                // Reste dans START jusqu'au clic sur DÉMARRER
            }
            LightState.YELLOW -> {
                if (elapsedTime >= 2000) {
                    lightState = LightState.GREEN_GROW
                    stateStartTime = currentTime
                }
            }
            LightState.GREEN_GROW -> {
                if (elapsedTime >= 5000) {  // 5 secondes
                    lightState = LightState.GREEN_LEAVES
                    stateStartTime = currentTime
                }
            }
            LightState.GREEN_LEAVES -> {
                if (elapsedTime >= 4000) { // 4 secondes
                    lightState = LightState.GREEN_FLOWER
                    stateStartTime = currentTime
                }
            }
            LightState.GREEN_FLOWER -> {
                if (elapsedTime >= 5000) { // 5 secondes
                    lightState = LightState.RED
                    stateStartTime = currentTime
                }
            }
            LightState.RED -> {}
        }
    }
    
    // ==================== AFFICHAGE ====================
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // MODIFIÉ : Dessiner la tige de départ dans l'état START aussi
        if (lightState == LightState.START ||
            lightState == LightState.YELLOW ||
            lightState == LightState.GREEN_GROW || 
            lightState == LightState.GREEN_LEAVES || 
            lightState == LightState.GREEN_FLOWER || 
            lightState == LightState.RED) {
            drawPlantStem(canvas)
        }
        
        drawTrafficLight(canvas)
    }
    
    private fun drawPlantStem(canvas: Canvas) {
        val stem = plantStem ?: return
        
        // AJOUT - Dessiner les fleurs de profil/arrière DERRIÈRE les tiges
        if (lightState == LightState.GREEN_FLOWER || 
            lightState == LightState.RED) {
            drawBackgroundFlowers(canvas, stem.getFlowers())
        }
        
        // Dessiner la tige principale
        drawMainStem(canvas, stem.mainStem)
        
        // Dessiner les branches
        drawBranches(canvas, stem.branches)
        
        // AJOUT - Dessiner les feuilles pendant GREEN_LEAVES et après
        if (lightState == LightState.GREEN_LEAVES || 
            lightState == LightState.GREEN_FLOWER || 
            lightState == LightState.RED) {
            drawLeaves(canvas, stem.getLeaves())
        }
        
        // AJOUT - Dessiner les fleurs de face/3-4 PAR-DESSUS les tiges
        if (lightState == LightState.GREEN_FLOWER || 
            lightState == LightState.RED) {
            drawForegroundFlowers(canvas, stem.getFlowers())
        }
    }
    
    private fun drawMainStem(canvas: Canvas, mainStem: List<PlantStem.StemPoint>) {
        if (mainStem.size < 2) return
        
        stemPaint.color = Color.rgb(50, 120, 50)
        
        for (i in 1 until mainStem.size) {
            val point = mainStem[i]
            val prevPoint = mainStem[i - 1]
            
            stemPaint.strokeWidth = point.thickness
            
            // Position avec oscillation + onde permanente
            val adjustedX = point.x + point.oscillation + point.permanentWave
            val prevAdjustedX = prevPoint.x + prevPoint.oscillation + prevPoint.permanentWave
            
            if (i == 1) {
                // Premier segment : ligne simple
                canvas.drawLine(prevAdjustedX, prevPoint.y, adjustedX, point.y, stemPaint)
            } else {
                // Segments suivants : courbes fluides
                val controlX = (prevAdjustedX + adjustedX) / 2f
                val controlY = (prevPoint.y + point.y) / 2f
                
                // Point de contrôle ajusté pour fluidité
                val curvatureOffset = (adjustedX - prevAdjustedX) * 0.3f
                val finalControlX = controlX + curvatureOffset
                
                // Courbe quadratique simulée avec 2 lignes
                canvas.drawLine(prevAdjustedX, prevPoint.y, finalControlX, controlY, stemPaint)
                canvas.drawLine(finalControlX, controlY, adjustedX, point.y, stemPaint)
            }
        }
    }
    
    private fun drawBranches(canvas: Canvas, branches: List<PlantStem.Branch>) {
        branchPaint.color = Color.rgb(40, 100, 40)
        
        for (branch in branches.filter { it.isActive }) {
            if (branch.points.size >= 2) {
                for (i in 1 until branch.points.size) {
                    val point = branch.points[i]
                    val prevPoint = branch.points[i - 1]
                    
                    branchPaint.strokeWidth = point.thickness
                    
                    if (i == 1 || branch.points.size <= 2) {
                        // Premier segment ou branche courte : ligne simple
                        canvas.drawLine(prevPoint.x, prevPoint.y, point.x, point.y, branchPaint)
                    } else {
                        // Courbe fluide pour les branches
                        val controlX = (prevPoint.x + point.x) / 2f
                        val controlY = (prevPoint.y + point.y) / 2f
                        canvas.drawLine(prevPoint.x, prevPoint.y, controlX, controlY, branchPaint)
                        canvas.drawLine(controlX, controlY, point.x, point.y, branchPaint)
                    }
                }
            }
        }
    }
    
    // AJOUT - Fonction pour dessiner les feuilles réalistes
    private fun drawLeaves(canvas: Canvas, leaves: List<PlantLeavesManager.Leaf>) {
        val stem = plantStem ?: return
        
        for (leaf in leaves) {
            if (leaf.currentSize > 0) {
                // Couleur unique pour chaque feuille
                leafPaint.color = stem.getLeavesManager().getLeafColor(leaf)
                
                // Créer le path de la feuille avec forme réaliste
                val leafPath = stem.getLeavesManager().createLeafPath(leaf)
                
                // Dessiner la feuille
                canvas.drawPath(leafPath, leafPaint)
                
                // Optionnel : contour plus foncé pour définition
                if (leaf.currentSize > leaf.maxSize * 0.7f) {
                    leafPaint.style = Paint.Style.STROKE
                    leafPaint.strokeWidth = 1.5f
                    leafPaint.color = Color.rgb(20, 80, 20)
                    canvas.drawPath(leafPath, leafPaint)
                    leafPaint.style = Paint.Style.FILL
                }
            }
        }
    }
    
    // AJOUT - Fonction pour dessiner les fleurs derrière les tiges
    private fun drawBackgroundFlowers(canvas: Canvas, flowers: List<FlowerManager.Flower>) {
        val stem = plantStem ?: return
        
        // Filtrer les fleurs qui doivent être derrière (angle de vue > 60°)
        val backgroundFlowers = flowers.filter { it.perspective.viewAngle > 60f }
        
        if (backgroundFlowers.isNotEmpty()) {
            stem.getFlowerManager().drawSpecificFlowers(canvas, backgroundFlowers, flowerPaint, flowerCenterPaint)
        }
    }
    
    // AJOUT - Fonction pour dessiner les fleurs devant les tiges
    private fun drawForegroundFlowers(canvas: Canvas, flowers: List<FlowerManager.Flower>) {
        val stem = plantStem ?: return
        
        // Filtrer les fleurs qui doivent être devant (angle de vue <= 60°)
        val foregroundFlowers = flowers.filter { it.perspective.viewAngle <= 60f }
        
        if (foregroundFlowers.isNotEmpty()) {
            stem.getFlowerManager().drawSpecificFlowers(canvas, foregroundFlowers, flowerPaint, flowerCenterPaint)
        }
    }
    
    private fun drawTrafficLight(canvas: Canvas) {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - stateStartTime
        
        // MODIFIÉ : Taille et position selon l'état
        val lightRadius = when (lightState) {
            LightState.START -> resetButtonRadius    // Bouton vert normal
            LightState.YELLOW -> width * 0.4f       // Grand bouton jaune central
            else -> resetButtonRadius               // Bouton normal coin
        }
        
        val lightX = when (lightState) {
            LightState.START -> resetButtonX        // Position coin
            LightState.YELLOW -> width / 2f         // Centre écran
            else -> resetButtonX                    // Position coin
        }
        
        val lightY = when (lightState) {
            LightState.START -> resetButtonY        // Position coin
            LightState.YELLOW -> height / 2f        // Centre écran
            else -> resetButtonY                    // Position coin
        }
        
        // Ombre
        resetButtonPaint.color = 0x40000000.toInt()
        canvas.drawCircle(lightX + 8f, lightY + 8f, lightRadius, resetButtonPaint)
        
        // MODIFIÉ : Couleur selon l'état
        resetButtonPaint.color = when (lightState) {
            LightState.START -> 0xFF00AA00.toInt()      // Vert pour DÉMARRER
            LightState.YELLOW -> 0xFFFFD700.toInt()     // Jaune pour INSPIREZ
            LightState.GREEN_GROW -> 0xFF2F4F2F.toInt()
            LightState.GREEN_LEAVES -> 0xFF00FF00.toInt()
            LightState.GREEN_FLOWER -> 0xFFFF69B4.toInt()
            LightState.RED -> 0xFFFF0000.toInt()
        }
        canvas.drawCircle(lightX, lightY, lightRadius, resetButtonPaint)
        
        // Bordure
        resetButtonPaint.color = 0xFF333333.toInt()
        resetButtonPaint.style = Paint.Style.STROKE
        resetButtonPaint.strokeWidth = 12f
        canvas.drawCircle(lightX, lightY, lightRadius, resetButtonPaint)
        resetButtonPaint.style = Paint.Style.FILL
        
        // MODIFIÉ : Texte et timer selon l'état
        when (lightState) {
            LightState.START -> {
                resetTextPaint.textAlign = Paint.Align.CENTER
                resetTextPaint.textSize = 120f
                resetTextPaint.color = 0xFFFFFFFF.toInt()
                canvas.drawText("DÉMARRER", lightX, lightY, resetTextPaint)
            }
            LightState.YELLOW -> {
                resetTextPaint.textAlign = Paint.Align.CENTER
                resetTextPaint.textSize = 180f
                resetTextPaint.color = 0xFF000000.toInt()
                canvas.drawText("INSPIREZ", lightX, lightY, resetTextPaint)
                
                val timeRemaining = max(0, 2 - (elapsedTime / 1000))
                if (timeRemaining > 0) {
                    resetTextPaint.textSize = 108f
                    canvas.drawText(timeRemaining.toString(), lightX, lightY + 144f, resetTextPaint)
                }
            }
            LightState.RED -> {
                resetTextPaint.textAlign = Paint.Align.CENTER
                resetTextPaint.textSize = 120f
                resetTextPaint.color = 0xFF000000.toInt()
                canvas.drawText("↻", lightX, lightY, resetTextPaint)
            }
            else -> {
                // Texte pour les phases vertes
                resetTextPaint.textAlign = Paint.Align.CENTER
                resetTextPaint.textSize = 60f
                resetTextPaint.color = 0xFF000000.toInt()
                
                val phaseText = when (lightState) {
                    LightState.GREEN_GROW -> "TIGE"
                    LightState.GREEN_LEAVES -> "FEUILLES"
                    LightState.GREEN_FLOWER -> "FLEUR"
                    else -> ""
                }
                
                canvas.drawText(phaseText, lightX, lightY, resetTextPaint)
                
                val timeRemaining = when (lightState) {
                    LightState.GREEN_GROW -> max(0, 5 - (elapsedTime / 1000))
                    LightState.GREEN_LEAVES -> max(0, 4 - (elapsedTime / 1000))
                    LightState.GREEN_FLOWER -> max(0, 5 - (elapsedTime / 1000))
                    else -> 0
                }
                
                if (timeRemaining > 0) {
                    resetTextPaint.textSize = 40f
                    canvas.drawText(timeRemaining.toString(), lightX, lightY + 50f, resetTextPaint)
                }
            }
        }
    }
    
    // ==================== GESTION DES ÉVÉNEMENTS ====================
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val dx = event.x - resetButtonX
            val dy = event.y - resetButtonY
            val distance = sqrt(dx * dx + dy * dy)
            
            // MODIFIÉ : Gérer les clics selon l'état
            when (lightState) {
                LightState.START -> {
                    // Clic sur DÉMARRER
                    if (distance <= resetButtonRadius) {
                        lightState = LightState.YELLOW
                        stateStartTime = System.currentTimeMillis()
                        return true
                    }
                }
                LightState.RED -> {
                    // Clic sur RESET
                    if (distance <= resetButtonRadius) {
                        startCycle()
                        return true
                    }
                }
                else -> {
                    // Pas de clic possible pendant les autres phases
                }
            }
        }
        return super.onTouchEvent(event)
    }
}
