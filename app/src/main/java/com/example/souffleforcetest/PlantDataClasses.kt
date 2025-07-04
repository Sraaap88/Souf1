package com.example.souffleforcetest

// ==================== ENUM ET TYPES ====================

enum class PlantType(
    val maxBranches: Int,
    val branchingFromBase: Boolean,
    val leafDensity: Float,
    val leafLengthMultiplier: Float
) {
    MARGUERITE(
        maxBranches = 3,
        branchingFromBase = true,
        leafDensity = 1.5f,
        leafLengthMultiplier = 1.3f
    )
}

// ==================== DATA CLASSES ====================

data class TracePoint(
    val x: Float, 
    val y: Float, 
    val strokeWidth: Float,
    val waveFrequency: Float, 
    val waveAmplitude: Float, 
    val curvature: Float
)

data class Branch(
    val id: Int,
    val startPoint: TracePoint,
    val tracedPath: MutableList<TracePoint>,
    var isActive: Boolean = true,
    val growthMultiplier: Float = 1f,
    var currentHeight: Float = 0f,
    var offsetX: Float = 0f,
    var currentStrokeWidth: Float = 0f,
    var fleur: Fleur? = null,
    val maxStrokeWidth: Float = 25.6f,
    val baseStrokeWidth: Float = 9.6f,
    val isFromBase: Boolean = true
)

data class Bourgeon(
    val x: Float, 
    val y: Float, 
    var taille: Float
)

data class Feuille(
    val bourgeon: Bourgeon, 
    var longueur: Float, 
    var largeur: Float, 
    val angle: Float, 
    var maxLargeurAtteinte: Boolean = false
)

data class Fleur(
    var x: Float, 
    var y: Float, 
    var taille: Float, 
    var petalCount: Int, 
    val sizeMultiplier: Float = 1f
)
