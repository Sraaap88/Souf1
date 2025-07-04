package com.example.souffleforcetest

// ==================== ENUM ET TYPES ====================

enum class PlantType(
    val maxBranches: Int,
    val branchingFromBase: Boolean,
    val leafDensity: Float,
    val leafLengthMultiplier: Float,
    val stemStyle: StemStyle,
    val leafStyle: LeafStyle,
    val flowerStyle: FlowerStyle
) {
    MARGUERITE(
        maxBranches = 3,
        branchingFromBase = true,
        leafDensity = 1.5f,
        leafLengthMultiplier = 1.3f,
        stemStyle = StemStyle.STRAIGHT,
        leafStyle = LeafStyle.SERRATED,
        flowerStyle = FlowerStyle.DAISY
    ),
    ROSE(
        maxBranches = 5,
        branchingFromBase = false, // Ramification sur tiges
        leafDensity = 2.0f,
        leafLengthMultiplier = 0.8f,
        stemStyle = StemStyle.THORNY,
        leafStyle = LeafStyle.COMPOUND,
        flowerStyle = FlowerStyle.LAYERED_PETALS
    ),
    TOURNESOL(
        maxBranches = 1,
        branchingFromBase = true,
        leafDensity = 1.0f,
        leafLengthMultiplier = 2.0f,
        stemStyle = StemStyle.THICK,
        leafStyle = LeafStyle.HEART_SHAPED,
        flowerStyle = FlowerStyle.LARGE_CENTER
    )
}

enum class StemStyle {
    STRAIGHT,    // Marguerite - tige droite
    THORNY,      // Rose - avec épines
    THICK,       // Tournesol - tige épaisse
    CURVED,      // Lys - naturellement courbé
    BAMBOO       // Bambou - segments
}

enum class LeafStyle {
    SERRATED,     // Marguerite - bords dentelés
    COMPOUND,     // Rose - feuilles composées
    HEART_SHAPED, // Tournesol - en forme de cœur
    LONG_THIN,    // Lys - longues et fines
    OVAL          // Standard ovale
}

enum class FlowerStyle {
    DAISY,           // Marguerite - pétales fins + centre jaune
    LAYERED_PETALS,  // Rose - pétales en couches
    LARGE_CENTER,    // Tournesol - gros centre + pétales
    TRUMPET,         // Lys - forme de trompette
    BELL             // Campanule - forme de cloche
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
