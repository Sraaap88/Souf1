package com.example.souffleforcetest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
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
    
    private var lightState = LightState.START
    private var stateStartTime = 0L
    private var selectedMode = ""  // "ZEN" ou "DÉFI"
    
    // ==================== LOGIQUE DE PLANTE ====================
    
    private var plantStem: PlantStem? = null
    private var daisyBitmap: Bitmap? = null
    
    enum class LightState {
        START, FLOWER_CHOICE, YELLOW, GREEN_GROW, GREEN_LEAVES, GREEN_FLOWER, RED
    }
    
    // ==================== GESTION DE L'ÉCRAN ====================
    
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        
        resetButtonX = w - resetButtonRadius - 50f
        resetButtonY = resetButtonRadius + 80f
        
        plantStem = PlantStem(w, h)
        
        // Charger l'image de marguerite (place ton image dans res/drawable/)
        try {
            daisyBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.marguerite)
            // REMPLACE android.R.drawable.ic_menu_gallery par R.drawable.ton_nom_image
            // Exemple: R.drawable.marguerite si ton fichier s'appelle marguerite.png
        } catch (e: Exception) {
            // Si l'image n'est pas trouvée, on garde daisyBitmap = null
        }
    }
    
    // ==================== CONTRÔLE DU CYCLE ====================
    
    fun startCycle() {
        lightState = LightState.START
        stateStartTime = System.currentTimeMillis()
        showResetButton = false
        selectedMode = ""
        plantStem?.resetStem()
        invalidate()
    }
    
    fun updateForce(force: Float) {
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
        
        invalidate()
    }
    
    private fun updateLightState() {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - stateStartTime
        
        when (lightState) {
            LightState.START -> {
                // Reste en START jusqu'à ce qu'on appuie sur le bouton
            }
            LightState.FLOWER_CHOICE -> {
                // Reste en FLOWER_CHOICE jusqu'à ce qu'on choisisse une fleur
            }
            LightState.YELLOW -> {
                if (elapsedTime >= 2000) { 
                    lightState = LightState.GREEN_GROW
                    stateStartTime = currentTime
                }
            }
            LightState.GREEN_GROW -> {
                if (elapsedTime >= 4000) { // 4 secondes au lieu de 5
                    lightState = LightState.GREEN_LEAVES
                    stateStartTime = currentTime
                }
            }
            LightState.GREEN_LEAVES -> {
                if (elapsedTime >= 3000) { // 3 secondes au lieu de 4
                    lightState = LightState.GREEN_FLOWER
                    stateStartTime = currentTime
                }
            }
            LightState.GREEN_FLOWER -> {
                if (elapsedTime >= 4000) { // 4 secondes au lieu de 5
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
        
        // Dessiner la tige dans toutes les phases après croissance
        if (lightState == LightState.GREEN_GROW || 
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
        
        // Calculer les positions selon l'état
        val lightRadius = if (lightState == LightState.START) width * 0.15f else resetButtonRadius
        val lightX = if (lightState == LightState.START) width * 0.4f else resetButtonX
        val lightY = if (lightState == LightState.START) height / 2f else resetButtonY
        
        // Timer pour tous les états
        val timeRemaining = when (lightState) {
            LightState.START -> 0
            LightState.FLOWER_CHOICE -> 0
            LightState.YELLOW -> max(0, 2 - (elapsedTime / 1000))      // 2 secondes
            LightState.GREEN_GROW -> max(0, 4 - (elapsedTime / 1000))  // 4 secondes
            LightState.GREEN_LEAVES -> max(0, 3 - (elapsedTime / 1000)) // 3 secondes
            LightState.GREEN_FLOWER -> max(0, 4 - (elapsedTime / 1000)) // 4 secondes
            LightState.RED -> 0
        }
        
        if (lightState == LightState.START) {
            // Calculer positions des deux boutons - VRAIMENT CENTRER L'ENSEMBLE
            val buttonRadius = width * 0.15f
            val spacing = buttonRadius * 2.5f  // Distance entre centres
            val centerX = width / 2f  // Centre de l'écran
            val zenButtonX = centerX - spacing / 2f  // Moitié de l'espacement vers la gauche
            val defiButtonX = centerX + spacing / 2f  // Moitié de l'espacement vers la droite
            val buttonY = height / 2f
            
            // Dessiner bouton ZEN (bleu marine pour meilleur contraste)
            drawSingleButton(canvas, zenButtonX, buttonY, buttonRadius, 0xFF1E3A8A.toInt(), "ZEN")
            
            // Dessiner bouton DÉFI (orange feu)
            drawSingleButton(canvas, defiButtonX, buttonY, buttonRadius, 0xFFFF4500.toInt(), "DÉFI")
            
        } else if (lightState == LightState.FLOWER_CHOICE) {
            // Écran de choix de fleur (mode ZEN seulement)
            drawFlowerChoice(canvas)
            
        } else if (lightState == LightState.YELLOW) {
            // Pas de cercle, juste le texte au centre en blanc
            resetTextPaint.textAlign = Paint.Align.CENTER
            resetTextPaint.textSize = 180f
            resetTextPaint.color = 0xFFFFFFFF.toInt() // Blanc
            resetTextPaint.isFakeBoldText = false
            canvas.drawText("INSPIREZ", width / 2f, height / 2f, resetTextPaint)
            
            if (timeRemaining > 0) {
                resetTextPaint.textSize = 108f
                canvas.drawText(timeRemaining.toString(), width / 2f, height / 2f + 144f, resetTextPaint)
            }
            
        } else if (lightState == LightState.RED) {
            // Dessiner le bouton RESET
            // Ombre
            resetButtonPaint.color = 0x40000000.toInt()
            canvas.drawCircle(lightX + 8f, lightY + 8f, lightRadius, resetButtonPaint)
            
            // Bouton rouge
            resetButtonPaint.color = 0xFFFF0000.toInt()
            canvas.drawCircle(lightX, lightY, lightRadius, resetButtonPaint)
            
            // Bordure
            resetButtonPaint.color = 0xFF333333.toInt()
            resetButtonPaint.style = Paint.Style.STROKE
            resetButtonPaint.strokeWidth = 12f
            canvas.drawCircle(lightX, lightY, lightRadius, resetButtonPaint)
            resetButtonPaint.style = Paint.Style.FILL
            
            // Texte reset - 1/4 de hauteur plus bas
            resetTextPaint.textAlign = Paint.Align.CENTER
            resetTextPaint.textSize = 120f
            resetTextPaint.color = 0xFF000000.toInt()
            resetTextPaint.isFakeBoldText = false
            canvas.drawText("↻", lightX, lightY + 30f, resetTextPaint)  // +30f au lieu de 0f
            
        } else {
            // Phases vertes - dessiner les cercles normaux
            // Ombre
            resetButtonPaint.color = 0x40000000.toInt()
            canvas.drawCircle(lightX + 8f, lightY + 8f, lightRadius, resetButtonPaint)
            
            // Couleur selon l'état
            resetButtonPaint.color = when (lightState) {
                LightState.GREEN_GROW -> 0xFF2F4F2F.toInt()
                LightState.GREEN_LEAVES -> 0xFF00FF00.toInt()
                LightState.GREEN_FLOWER -> 0xFFFF69B4.toInt()
                else -> 0xFF00AA00.toInt()
            }
            canvas.drawCircle(lightX, lightY, lightRadius, resetButtonPaint)
            
            // Bordure
            resetButtonPaint.color = 0xFF333333.toInt()
            resetButtonPaint.style = Paint.Style.STROKE
            resetButtonPaint.strokeWidth = 12f
            canvas.drawCircle(lightX, lightY, lightRadius, resetButtonPaint)
            resetButtonPaint.style = Paint.Style.FILL
            
            // Texte pour les phases vertes - plus grand et en gras
            resetTextPaint.textAlign = Paint.Align.CENTER
            resetTextPaint.textSize = 80f  // Augmenté de 60f à 80f
            resetTextPaint.color = 0xFF000000.toInt()
            resetTextPaint.isFakeBoldText = true  // En gras
            
            val phaseText = when (lightState) {
                LightState.GREEN_GROW -> "TIGE"
                LightState.GREEN_LEAVES -> "FEUILLES"
                LightState.GREEN_FLOWER -> "FLEUR"
                else -> ""
            }
            
            canvas.drawText(phaseText, lightX, lightY, resetTextPaint)
            
            if (timeRemaining > 0) {
                resetTextPaint.textSize = 50f  // Aussi un peu plus grand
                resetTextPaint.isFakeBoldText = false  // Timer pas en gras
                canvas.drawText(timeRemaining.toString(), lightX, lightY + 60f, resetTextPaint)  // Ajusté l'espacement
            }
        }
    }
    
    private fun drawSingleButton(canvas: Canvas, x: Float, y: Float, radius: Float, color: Int, text: String) {
        // Ombre
        resetButtonPaint.color = 0x40000000.toInt()
        canvas.drawCircle(x + 8f, y + 8f, radius, resetButtonPaint)
        
        // Bouton
        resetButtonPaint.color = color
        canvas.drawCircle(x, y, radius, resetButtonPaint)
        
        // Bordure
        resetButtonPaint.color = 0xFF333333.toInt()
        resetButtonPaint.style = Paint.Style.STROKE
        resetButtonPaint.strokeWidth = 8f
        canvas.drawCircle(x, y, radius, resetButtonPaint)
        resetButtonPaint.style = Paint.Style.FILL
        
        // Texte - 1/4 de hauteur plus bas
        resetTextPaint.textAlign = Paint.Align.CENTER
        resetTextPaint.textSize = 80f
        resetTextPaint.color = 0xFFFFFFFF.toInt()
        resetTextPaint.isFakeBoldText = false
        canvas.drawText(text, x, y + 30f, resetTextPaint)  // +30f au lieu de +10f (1/4 de hauteur plus bas)
    }
    
    private fun drawFlowerChoice(canvas: Canvas) {
        // Titre
        resetTextPaint.textAlign = Paint.Align.CENTER
        resetTextPaint.textSize = 150f
        resetTextPaint.color = 0xFFFFFFFF.toInt()
        resetTextPaint.isFakeBoldText = true
        canvas.drawText("CHOISIR FLEUR", width / 2f, height * 0.25f, resetTextPaint)
        
        // Bouton marguerite au centre
        val flowerButtonX = width / 2f
        val flowerButtonY = height / 2f
        val flowerButtonRadius = width * 0.2f  // Plus gros que les boutons start
        
        // Dessiner le bouton de base
        resetButtonPaint.color = 0x40000000.toInt()
        canvas.drawCircle(flowerButtonX + 8f, flowerButtonY + 8f, flowerButtonRadius, resetButtonPaint)
        
        resetButtonPaint.color = 0xFF2D5A27.toInt()  // Vert foncé pour la marguerite
        canvas.drawCircle(flowerButtonX, flowerButtonY, flowerButtonRadius, resetButtonPaint)
        
        resetButtonPaint.color = 0xFF333333.toInt()
        resetButtonPaint.style = Paint.Style.STROKE
        resetButtonPaint.strokeWidth = 8f
        canvas.drawCircle(flowerButtonX, flowerButtonY, flowerButtonRadius, resetButtonPaint)
        resetButtonPaint.style = Paint.Style.FILL
        
        // Dessiner une marguerite miniature à l'intérieur
        drawMiniDaisy(canvas, flowerButtonX, flowerButtonY, flowerButtonRadius * 0.8f)
        
        // Nom de la fleur en dessous
        resetTextPaint.textSize = 60f
        resetTextPaint.isFakeBoldText = false
        canvas.drawText("MARGUERITE", flowerButtonX, flowerButtonY + flowerButtonRadius + 80f, resetTextPaint)
    }
    
    private fun drawMiniDaisy(canvas: Canvas, centerX: Float, centerY: Float, size: Float) {
        if (daisyBitmap != null) {
            // Utiliser ton image de marguerite
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
    
    // ==================== GESTION DES ÉVÉNEMENTS ====================
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (lightState == LightState.START) {
                // Calculer positions des boutons - VRAIMENT CENTRER L'ENSEMBLE
                val buttonRadius = width * 0.15f
                val spacing = buttonRadius * 2.5f
                val centerX = width / 2f
                val zenButtonX = centerX - spacing / 2f
                val defiButtonX = centerX + spacing / 2f
                val buttonY = height / 2f
                
                // Vérifier clic sur bouton ZEN
                val zenDx = event.x - zenButtonX
                val zenDy = event.y - buttonY
                val zenDistance = sqrt(zenDx * zenDx + zenDy * zenDy)
                
                // Vérifier clic sur bouton DÉFI
                val defiDx = event.x - defiButtonX
                val defiDy = event.y - buttonY
                val defiDistance = sqrt(defiDx * defiDx + defiDy * defiDy)
                
                if (zenDistance <= buttonRadius) {
                    // Mode ZEN sélectionné - aller à l'écran de choix de fleur
                    selectedMode = "ZEN"
                    lightState = LightState.FLOWER_CHOICE
                    stateStartTime = System.currentTimeMillis()
                    return true
                } else if (defiDistance <= buttonRadius) {
                    // Mode DÉFI sélectionné - aller directement à INSPIREZ
                    selectedMode = "DÉFI"
                    lightState = LightState.YELLOW
                    stateStartTime = System.currentTimeMillis()
                    return true
                }
            } else if (lightState == LightState.FLOWER_CHOICE) {
                // Clic sur la marguerite
                val flowerButtonX = width / 2f
                val flowerButtonY = height / 2f
                val flowerButtonRadius = width * 0.2f
                
                val dx = event.x - flowerButtonX
                val dy = event.y - flowerButtonY
                val distance = sqrt(dx * dx + dy * dy)
                
                if (distance <= flowerButtonRadius) {
                    // Marguerite sélectionnée - aller à INSPIREZ
                    lightState = LightState.YELLOW
                    stateStartTime = System.currentTimeMillis()
                    return true
                }
            } else if (lightState == LightState.RED) {
                // Appui sur le bouton RESET
                val dx = event.x - resetButtonX
                val dy = event.y - resetButtonY
                val distance = sqrt(dx * dx + dy * dy)
                
                if (distance <= resetButtonRadius) {
                    startCycle()
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }
}
