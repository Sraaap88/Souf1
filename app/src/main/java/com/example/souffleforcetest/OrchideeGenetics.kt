package com.example.souffleforcetest

import kotlin.random.Random

// ==================== DATA CLASSES PRINCIPALES ====================

/**
 * Génétique complète d'une orchidée procédurale unique
 */
data class OrchideeGenetics(
    val species: OrchideeSpecies,
    val colorPalette: OrchideeColorPalette,
    val shapeVariation: OrchideeShapeVariation,
    val patternType: OrchideePatternType,
    val sizeCategory: OrchideeSizeCategory,
    val rarity: Float,
    val seed: Long,
    val id: String
)

// ==================== ESPÈCES D'ORCHIDÉES ====================

enum class OrchideeSpecies(val displayName: String, val baseRarity: Float) {
    PHALAENOPSIS("Phalaenopsis", 0.3f),     // Commune - papillon
    CATTLEYA("Cattleya", 0.5f),             // Modérée - grandes fleurs
    DENDROBIUM("Dendrobium", 0.4f),         // Modérée - grappes
    VANDA("Vanda", 0.7f),                   // Rare - bleues possibles
    ONCIDIUM("Oncidium", 0.6f),             // Assez rare - dancing lady
    CYMBIDIUM("Cymbidium", 0.8f)            // Très rare - grandes en épi
}

// ==================== PALETTES DE COULEURS ====================

data class OrchideeColorPalette(
    val primary: Int,           // Couleur principale des pétales
    val secondary: Int,         // Couleur secondaire/dégradé
    val accent: Int,            // Couleur d'accent (centre, bordures)
    val throat: Int,            // Couleur du labelle/gorge
    val veining: Int,           // Couleur des nervures/motifs
    val spotColor: Int,         // Couleur des taches si applicable
    val gradientStops: List<Int> // Points de dégradé pour transitions
)

// ==================== VARIATIONS DE FORME ====================

data class OrchideeShapeVariation(
    val sepalLength: Float,         // Longueur des sépales (0.7f - 1.3f)
    val sepalWidth: Float,          // Largeur des sépales (0.6f - 1.4f)
    val petalLength: Float,         // Longueur des pétales (0.8f - 1.2f)
    val petalWidth: Float,          // Largeur des pétales (0.7f - 1.3f)
    val petalCurvature: Float,      // Courbure des pétales (0.2f - 0.9f)
    val labelleSize: Float,         // Taille du labelle (0.6f - 1.5f)
    val labelleForm: LabelleForm,   // Forme du labelle
    val ruffledEdges: Float,        // Intensité des bords ondulés (0.0f - 1.0f)
    val throatDepth: Float,         // Profondeur de la gorge (0.3f - 1.0f)
    val spreadAngle: Float,         // Angle d'ouverture (60f - 120f)
    val flatness: Float,            // Aplatissement général (0.4f - 1.0f)
    val asymmetry: Float            // Asymétrie naturelle (0.0f - 0.3f)
)

enum class LabelleForm {
    BUTTERFLY_SPREAD,       // Papillon étalé (Phalaenopsis)
    TRUMPET_FLARED,         // Trompette évasée (Cattleya)
    SMALL_POINTED,          // Petit et pointu (Vanda)
    DANCING_SKIRT,          // Jupe dansante (Oncidium)
    TUBE_NARROW,            // Tube étroit (Dendrobium)
    BOAT_SHAPED             // Forme de bateau (Cymbidium)
}

// ==================== TYPES DE MOTIFS ====================

data class OrchideePatternType(
    val primary: PatternStyle,
    val secondary: PatternStyle?,
    val intensity: Float,           // Intensité du motif (0.2f - 1.0f)
    val coverage: Float,            // Couverture du motif (0.1f - 0.9f)
    val blendMode: PatternBlendMode
)

enum class PatternStyle {
    SOLID,                  // Couleur unie
    VEINED,                 // Nervures marquées
    SPOTTED,                // Tacheté
    STRIPED,                // Rayé
    GRADIENT_RADIAL,        // Dégradé radial
    GRADIENT_LINEAR,        // Dégradé linéaire
    MOTTLED,                // Marbré
    EDGED,                  // Bordures contrastées
    THROAT_BURST,           // Explosion de couleur au centre
    PICOTEE,                // Bordure fine colorée
    FLAMED,                 // Flammes de couleur
    WATERCOLOR              // Effet aquarelle
}

enum class PatternBlendMode {
    OVERLAY,                // Superposition
    MULTIPLY,               // Multiplication
    SOFT_LIGHT,             // Lumière douce
    COLOR_BURN,             // Densité couleur +
    NORMAL                  // Normal
}

// ==================== CATÉGORIES DE TAILLE ====================

enum class OrchideeSizeCategory(val sizeMultiplier: Float, val rarity: Float) {
    MINIATURE(0.4f, 0.8f),          // Très petites - rares
    COMPACT(0.7f, 0.4f),            // Petites - communes
    STANDARD(1.0f, 0.3f),           // Taille normale - très communes
    LARGE(1.4f, 0.6f),              // Grandes - modérément rares
    GIANT(1.8f, 0.9f)               // Géantes - très rares
}

// ==================== DONNÉES DE MOTIFS SPÉCIALISÉS ====================

data class VeinedPatternData(
    val veinDensity: Float,         // Densité des nervures (0.2f - 0.8f)
    val veinThickness: Float,       // Épaisseur des nervures (0.5f - 2.0f)
    val mainDirection: Float,       // Direction principale (0f - 360f)
    val branchingFactor: Float,     // Facteur de ramification (0.3f - 0.8f)
    val fadeToEdge: Boolean         // Estompage vers les bords
)

data class SpottedPatternData(
    val spotCount: Int,             // Nombre de taches (3 - 25)
    val spotSizes: List<Float>,     // Tailles des taches
    val spotDistribution: SpotDistribution,
    val edgeBlur: Float             // Flou des bords (0.0f - 1.0f)
)

enum class SpotDistribution {
    RANDOM,                 // Distribution aléatoire
    CENTERED,               // Concentré au centre
    EDGE_FOCUSED,           // Concentré sur les bords
    LINEAR_PATTERN,         // Motif linéaire
    CLUSTERED               // En groupes
}

data class GradientPatternData(
    val startColor: Int,
    val endColor: Int,
    val midPoints: List<Pair<Float, Int>>, // Points intermédiaires
    val direction: Float,           // Direction du dégradé (0f - 360f)
    val centerX: Float,            // Centre X pour dégradé radial (0.0f - 1.0f)
    val centerY: Float,            // Centre Y pour dégradé radial (0.0f - 1.0f)
    val fadeType: GradientFadeType
)

enum class GradientFadeType {
    LINEAR,                 // Linéaire
    RADIAL,                 // Radial
    DIAMOND,                // Diamant
    SWEPT,                  // Balayé
    REFLECTED               // Réfléchi
}

// ==================== DONNÉES SPÉCIALISÉES PAR ESPÈCE ====================

data class PhalaenopsisData(
    val butterflyWingSpread: Float, // Écartement des ailes (0.7f - 1.2f)
    val centralStripe: Boolean,     // Bande centrale
    val lipComplexity: Float        // Complexité du labelle (0.4f - 1.0f)
)

data class CattleyaData(
    val ruffleIntensity: Float,     // Intensité des froufrous (0.3f - 1.0f)
    val throatPattern: PatternStyle, // Motif de la gorge
    val petalOverlap: Float         // Chevauchement des pétales (0.2f - 0.8f)
)

data class VandaData(
    val tessellationPattern: Boolean, // Motif en tessellation
    val flatnessRatio: Float,        // Ratio d'aplatissement (0.8f - 1.0f)
    val checkeredIntensity: Float    // Intensité du motif damier (0.0f - 1.0f)
)

data class OncidiumData(
    val skirtFullness: Float,       // Ampleur de la "jupe" (0.6f - 1.3f)
    val dancingAngle: Float,        // Angle de "danse" (15f - 45f)
    val yellowIntensity: Float      // Intensité du jaune (0.7f - 1.0f)
)

data class DendrobiumData(
    val clusterSize: Int,           // Taille de la grappe (3 - 8)
    val flowerSpacing: Float,       // Espacement des fleurs (0.8f - 1.5f)
    val tubeLength: Float           // Longueur du tube (0.5f - 1.2f)
)

data class CymbidiumData(
    val spikeHeight: Float,         // Hauteur de l'épi (1.2f - 2.0f)
    val flowerCount: Int,           // Nombre de fleurs (5 - 12)
    val waxyFinish: Float           // Finition cireuse (0.6f - 1.0f)
)

// ==================== DONNÉES DE RARETÉ ====================

data class RarityModifiers(
    val colorRarity: Float,         // Rareté de la couleur
    val patternRarity: Float,       // Rareté du motif
    val sizeRarity: Float,          // Rareté de la taille
    val shapeRarity: Float,         // Rareté de la forme
    val overallRarity: Float        // Rareté globale calculée
)

// ==================== HELPER POUR COMPATIBILITÉ ANDROID ====================

object OrchideeColorHelper {
    /**
     * Convertit une couleur RGB en entier Android
     */
    fun rgb(red: Int, green: Int, blue: Int): Int {
        return android.graphics.Color.rgb(red, green, blue)
    }
    
    /**
     * Convertit une couleur ARGB en entier Android
     */
    fun argb(alpha: Int, red: Int, green: Int, blue: Int): Int {
        return android.graphics.Color.argb(alpha, red, green, blue)
    }
    
    /**
     * Mélange deux couleurs avec un ratio donné
     */
    fun blendColors(color1: Int, color2: Int, ratio: Float): Int {
        val clampedRatio = ratio.coerceIn(0f, 1f)
        val inverseRatio = 1f - clampedRatio
        
        val r1 = android.graphics.Color.red(color1)
        val g1 = android.graphics.Color.green(color1)
        val b1 = android.graphics.Color.blue(color1)
        
        val r2 = android.graphics.Color.red(color2)
        val g2 = android.graphics.Color.green(color2)
        val b2 = android.graphics.Color.blue(color2)
        
        val finalR = (r1 * inverseRatio + r2 * clampedRatio).toInt()
        val finalG = (g1 * inverseRatio + g2 * clampedRatio).toInt()
        val finalB = (b1 * inverseRatio + b2 * clampedRatio).toInt()
        
        return rgb(finalR, finalG, finalB)
    }
    
    /**
     * Génère une couleur harmonieuse basée sur une couleur de base
     */
    fun generateHarmoniousColor(baseColor: Int, harmony: ColorHarmony): Int {
        val hsl = rgbToHsl(baseColor)
        
        return when (harmony) {
            ColorHarmony.COMPLEMENTARY -> {
                val newHue = (hsl[0] + 180f) % 360f
                hslToRgb(floatArrayOf(newHue, hsl[1], hsl[2]))
            }
            ColorHarmony.ANALOGOUS -> {
                val newHue = (hsl[0] + Random.nextFloat() * 60f - 30f + 360f) % 360f
                hslToRgb(floatArrayOf(newHue, hsl[1], hsl[2]))
            }
            ColorHarmony.TRIADIC -> {
                val newHue = (hsl[0] + 120f) % 360f
                hslToRgb(floatArrayOf(newHue, hsl[1], hsl[2]))
            }
            ColorHarmony.MONOCHROMATIC -> {
                val newSaturation = (hsl[1] * Random.nextFloat() * 0.6f + 0.4f).coerceIn(0f, 1f)
                val newLightness = (hsl[2] * Random.nextFloat() * 0.6f + 0.4f).coerceIn(0f, 1f)
                hslToRgb(floatArrayOf(hsl[0], newSaturation, newLightness))
            }
        }
    }
    
    private fun rgbToHsl(color: Int): FloatArray {
        val r = android.graphics.Color.red(color) / 255f
        val g = android.graphics.Color.green(color) / 255f
        val b = android.graphics.Color.blue(color) / 255f
        
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val delta = max - min
        
        val lightness = (max + min) / 2f
        val saturation = if (delta == 0f) 0f else delta / (1f - kotlin.math.abs(2f * lightness - 1f))
        
        val hue = when {
            delta == 0f -> 0f
            max == r -> ((g - b) / delta) % 6f * 60f
            max == g -> ((b - r) / delta + 2f) * 60f
            else -> ((r - g) / delta + 4f) * 60f
        }
        
        return floatArrayOf(if (hue < 0) hue + 360f else hue, saturation, lightness)
    }
    
    private fun hslToRgb(hsl: FloatArray): Int {
        val h = hsl[0] / 360f
        val s = hsl[1]
        val l = hsl[2]
        
        val c = (1f - kotlin.math.abs(2f * l - 1f)) * s
        val x = c * (1f - kotlin.math.abs((h * 6f) % 2f - 1f))
        val m = l - c / 2f
        
        val (r, g, b) = when ((h * 6f).toInt()) {
            0 -> Triple(c, x, 0f)
            1 -> Triple(x, c, 0f)
            2 -> Triple(0f, c, x)
            3 -> Triple(0f, x, c)
            4 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        
        return rgb(
            ((r + m) * 255f).toInt().coerceIn(0, 255),
            ((g + m) * 255f).toInt().coerceIn(0, 255),
            ((b + m) * 255f).toInt().coerceIn(0, 255)
        )
    }
}

enum class ColorHarmony {
    COMPLEMENTARY,      // Couleurs complémentaires
    ANALOGOUS,          // Couleurs analogues
    TRIADIC,            // Couleurs triadiques
    MONOCHROMATIC       // Couleurs monochromatiques
}
