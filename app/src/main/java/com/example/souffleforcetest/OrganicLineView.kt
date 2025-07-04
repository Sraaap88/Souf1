package com.example.souffleforcetest

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Color
import android.graphics.Path
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
    
    private val leafPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = Color.rgb(34, 139, 34) // Vert forêt
    }
    
    private val leafStrokePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 2f
        color = Color.rgb(25, 100, 25) // Vert plus foncé pour contour
    }
    
    // ==================== ÉTATS DU SYSTÈME ====================
    
    private var showResetButton = false
    private var resetButtonX = 0f
    private var resetButtonY = 0f
    private val resetButtonRadius = 175f
    
    private var lightState = LightState.YELLOW
    private var stateStartTime = 0L
    
    // ==================== LOGIQUE DE PLANTE ====================
    
    private var plantStem: PlantStem? = null
    
    enum class LightState {
        YELLOW, GREEN_GROW, GREEN_LEAVES, GREEN_FLOWER, RED
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
        lightState = LightState.YELLOW
        stateStartTime = System.currentTimeMillis()
        showResetButton = false
        plantStem?.resetStem()
        invalidate()
    }
    
    fun updateForce(force: Float) {
        updateLightState()
        
        when (lightState) {
            LightState.GREEN_GROW -> {
                val phaseTime = System.currentTimeMillis() - stateStartTime
                plantStem?.processStemGrowth(force, phaseTime)
            }
            LightState.GREEN_LEAVES -> {
                // Phase feuilles : faire pousser les feuilles sur les tiges existantes
                plantStem?.processLeafGrowth(force)
            }
            else -> {}
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
            LightState.YELLOW -> {
                if (elapsedTime >= 2000) {
                    lightState = LightState.GREEN_GROW
                    stateStartTime = currentTime
                }
            }
            LightState.GREEN_GROW -> {
                if (elapsedTime >= 5000) {
                    lightState = LightState.GREEN_LEAVES
                    stateStartTime = currentTime
                }
            }
            LightState.GREEN_LEAVES -> {
                if (elapsedTime >= 3000) {
                    lightState = LightState.GREEN_FLOWER
                    stateStartTime = currentTime
                }
            }
            LightState.GREEN_FLOWER -> {
                if (elapsedTime >= 3000) {
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
        
        // Dessiner la plante dans toutes les phases après croissance
        if (lightState == LightState.GREEN_GROW || 
            lightState == LightState.GREEN_LEAVES || 
            lightState == LightState.GREEN_FLOWER || 
            lightState == LightState.RED) {
            drawPlantStem(canvas)
        }
        
        // Dessiner les feuilles à partir de la phase GREEN_LEAVES
        if (lightState == LightState.GREEN_LEAVES || 
            lightState == LightState.GREEN_FLOWER || 
            lightState == LightState.RED) {
            drawLeaves(canvas)
        }
        
        drawTrafficLight(canvas)
    }
    
    private fun drawPlantStem(canvas: Canvas) {
        val stem = plantStem ?: return
        
        // Dessiner la tige principale
        drawMainStem(canvas, stem.mainStem)
        
        // Dessiner les branches
        drawBranches(canvas, stem.branches)
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
    
    private fun drawLeaves(canvas: Canvas) {
        val stem = plantStem ?: return
        
        for (leaf in stem.leaves) {
            if (leaf.growthProgress > 0.1f) { // Afficher quand assez développée
                drawSingleLeaf(canvas, leaf)
            }
        }
    }
    
    private fun drawSingleLeaf(canvas: Canvas, leaf: PlantStem.Leaf) {
        val currentWidth = leaf.width * leaf.growthProgress
        val currentHeight = leaf.height * leaf.growthProgress
        
        if (currentWidth < 3f || currentHeight < 3f) return
        
        // Créer la forme de feuille découpée
        val path = createLeafPath(leaf, currentWidth, currentHeight)
        
        // Couleur selon le type de feuille
        val leafColor = when (leaf.leafType) {
            PlantStem.LeafType.BASAL_LARGE -> Color.rgb(34, 139, 34) // Vert forêt
            PlantStem.LeafType.STEM_MEDIUM -> Color.rgb(50, 150, 50) // Vert moyen
            PlantStem.LeafType.STEM_SMALL -> Color.rgb(60, 180, 60)  // Vert clair
        }
        
        leafPaint.color = leafColor
        
        // Dessiner la feuille
        canvas.drawPath(path, leafPaint)
        canvas.drawPath(path, leafStrokePaint) // Contour
    }
    
    private fun createLeafPath(leaf: PlantStem.Leaf, width: Float, height: Float): Path {
        val path = Path()
        
        // Nombre de segments selon le type de feuille
        val segments = when (leaf.leafType) {
            PlantStem.LeafType.BASAL_LARGE -> 12 // Très découpée
            PlantStem.LeafType.STEM_MEDIUM -> 8  // Moyennement découpée
            PlantStem.LeafType.STEM_SMALL -> 6   // Peu découpée
        }
        
        val centerX = leaf.x + leaf.oscillation
        val centerY = leaf.y
        
        // Premier point
        val startAngle = 0f
        val startRadius = width * 0.5f
        val startX = centerX + cos(startAngle) * startRadius
        val startY = centerY + sin(startAngle) * height * 0.5f
        path.moveTo(startX, startY)
        
        // Créer la forme lobée caractéristique des marguerites
        for (i in 1..segments) {
            val progress = i.toFloat() / segments
            val angle = progress * PI.toFloat() * 2f
            
            // Forme ovale de base
            val baseRadius = width * 0.5f
            val baseX = cos(angle) * baseRadius
            val baseY = sin(angle) * height * 0.5f
            
            // Ajout des lobes/dents caractéristiques
            val lobeFactor = when (leaf.leafType) {
                PlantStem.LeafType.BASAL_LARGE -> 1f + sin(angle * 3f) * 0.25f // Très lobée
                PlantStem.LeafType.STEM_MEDIUM -> 1f + sin(angle * 2f) * 0.15f // Moyennement lobée
                PlantStem.LeafType.STEM_SMALL -> 1f + sin(angle * 1.5f) * 0.1f // Peu lobée
            }
            
            val finalX = centerX + (baseX * lobeFactor)
            val finalY = centerY + (baseY * lobeFactor)
            
            path.lineTo(finalX, finalY)
        }
        
        path.close()
        return path
    }
    
    private fun drawTrafficLight(canvas: Canvas) {
        val currentTime = System.currentTimeMillis()
        val elapsedTime = currentTime - stateStartTime
        
        val lightRadius = if (lightState == LightState.YELLOW) width * 0.4f else resetButtonRadius
        val lightX = if (lightState == LightState.YELLOW) width / 2f else resetButtonX
        val lightY = if (lightState == LightState.YELLOW) height / 2f else resetButtonY
        
        // Ombre
        resetButtonPaint.color = 0x40000000.toInt()
        canvas.drawCircle(lightX + 8f, lightY + 8f, lightRadius, resetButtonPaint)
        
        // Couleur selon l'état
        resetButtonPaint.color = when (lightState) {
            LightState.YELLOW -> 0xFFFFD700.toInt()
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
        
        // Texte et timer
        val timeRemaining = when (lightState) {
            LightState.YELLOW -> max(0, 2 - (elapsedTime / 1000))
            LightState.GREEN_GROW -> max(0, 5 - (elapsedTime / 1000))
            LightState.GREEN_LEAVES -> max(0, 3 - (elapsedTime / 1000))
            LightState.GREEN_FLOWER -> max(0, 3 - (elapsedTime / 1000))
            LightState.RED -> 0
        }
        
        if (lightState == LightState.YELLOW) {
            resetTextPaint.textAlign = Paint.Align.CENTER
            resetTextPaint.textSize = 180f
            resetTextPaint.color = 0xFF000000.toInt()
            canvas.drawText("INSPIREZ", lightX, lightY, resetTextPaint)
            
            if (timeRemaining > 0) {
                resetTextPaint.textSize = 108f
                canvas.drawText(timeRemaining.toString(), lightX, lightY + 144f, resetTextPaint)
            }
        } else if (lightState == LightState.RED) {
            resetTextPaint.textAlign = Paint.Align.CENTER
            resetTextPaint.textSize = 120f
            resetTextPaint.color = 0xFF000000.toInt()
            canvas.drawText("↻", lightX, lightY, resetTextPaint)
        } else {
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
            
            if (timeRemaining > 0) {
                resetTextPaint.textSize = 40f
                canvas.drawText(timeRemaining.toString(), lightX, lightY + 50f, resetTextPaint)
            }
        }
    }
    
    // ==================== GESTION DES ÉVÉNEMENTS ====================
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && lightState == LightState.RED) {
            val dx = event.x - resetButtonX
            val dy = event.y - resetButtonY
            val distance = sqrt(dx * dx + dy * dy)
            
            if (distance <= resetButtonRadius) {
                startCycle()
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
