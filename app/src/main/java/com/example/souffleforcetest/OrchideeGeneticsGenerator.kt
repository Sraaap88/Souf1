package com.example.souffleforcetest

import kotlin.random.Random
import kotlin.math.*

/**
 * GÉNÉRATEUR PRINCIPAL D'ORCHIDÉES PROCÉDURALES
 * Crée des orchidées 100% uniques à chaque génération
 */
class OrchideeGeneticsGenerator {
    
    companion object {
        
        // ==================== GÉNÉRATION PRINCIPALE ====================
        
        /**
         * Génère une orchidée complètement unique
         * @param customSeed Seed optionnel pour reproductibilité
         * @return OrchideeGenetics complète
         */
        fun generate(customSeed: Long? = null): OrchideeGenetics {
            val seed = customSeed ?: (System.currentTimeMillis() + Random.nextLong())
            val rng = Random(seed)
            
            // Générer les composants dans l'ordre logique
            val species = generateSpecies(rng)
            val sizeCategory = generateSizeCategory(rng)
            val colorPalette = generateColors(species, rng)
            val shapeVariation = generateShape(species, sizeCategory, rng)
            val patternType = generatePattern(species, rng)
            
            // Calculer la rareté finale
            val rarity = calculateRarity(species, sizeCategory, colorPalette, patternType)
            
            val orchideeId = "orchidee_proc_${seed}_${species.name.lowercase()}"
            
            return OrchideeGenetics(
                species = species,
                colorPalette = colorPalette,
                shapeVariation = shapeVariation,
                patternType = patternType,
                sizeCategory = sizeCategory,
                rarity = rarity,
                seed = seed,
                id = orchideeId
            )
        }
        
        // ==================== GÉNÉRATION DES ESPÈCES ====================
        
        /**
         * Génère une espèce selon la rareté naturelle
         */
        fun generateSpecies(rng: Random = Random): OrchideeSpecies {
            val roll = rng.nextFloat()
            
            return when {
                roll < 0.30f -> OrchideeSpecies.PHALAENOPSIS  // 30% - Commune
                roll < 0.55f -> OrchideeSpecies.DENDROBIUM    // 25% - Modérée
                roll < 0.75f -> OrchideeSpecies.CATTLEYA      // 20% - Modérée
                roll < 0.88f -> OrchideeSpecies.ONCIDIUM      // 13% - Assez rare
                roll < 0.96f -> OrchideeSpecies.VANDA         // 8% - Rare
                else -> OrchideeSpecies.CYMBIDIUM             // 4% - Très rare
            }
        }
        
        // ==================== GÉNÉRATION DES COULEURS ====================
        
        /**
         * Génère une palette de couleurs réaliste selon l'espèce
         */
        fun generateColors(species: OrchideeSpecies, rng: Random = Random): OrchideeColorPalette {
            return when (species) {
                OrchideeSpecies.PHALAENOPSIS -> generatePhalaenopsisColors(rng)
                OrchideeSpecies.CATTLEYA -> generateCattleyaColors(rng)
                OrchideeSpecies.DENDROBIUM -> generateDendrobiumColors(rng)
                OrchideeSpecies.VANDA -> generateVandaColors(rng)
                OrchideeSpecies.ONCIDIUM -> generateOncidiumColors(rng)
                OrchideeSpecies.CYMBIDIUM -> generateCymbidiumColors(rng)
            }
        }
        
        private fun generatePhalaenopsisColors(rng: Random): OrchideeColorPalette {
            val colorVariant = rng.nextFloat()
            
            return when {
                colorVariant < 0.40f -> { // 40% - Blanc classique
                    val baseWhite = OrchideeColorHelper.rgb(248, 245, 255)
                    val yellowCenter = OrchideeColorHelper.rgb(255, 235, 120)
                    val pinkAccent = OrchideeColorHelper.rgb(220, 120, 180)
                    
                    OrchideeColorPalette(
                        primary = baseWhite,
                        secondary = OrchideeColorHelper.rgb(255, 250, 248),
                        accent = pinkAccent,
                        throat = yellowCenter,
                        veining = OrchideeColorHelper.rgb(180, 150, 200),
                        spotColor = OrchideeColorHelper.rgb(200, 100, 150),
                        gradientStops = listOf(baseWhite, yellowCenter, pinkAccent)
                    )
                }
                colorVariant < 0.65f -> { // 25% - Rose doux
                    val basePink = OrchideeColorHelper.rgb(255, 192, 203)
                    val deepPink = OrchideeColorHelper.rgb(255, 105, 180)
                    val whiteAccent = OrchideeColorHelper.rgb(255, 248, 220)
                    
                    OrchideeColorPalette(
                        primary = basePink,
                        secondary = deepPink,
                        accent = whiteAccent,
                        throat = OrchideeColorHelper.rgb(255, 215, 0),
                        veining = OrchideeColorHelper.rgb(200, 50, 120),
                        spotColor = OrchideeColorHelper.rgb(180, 20, 100),
                        gradientStops = listOf(whiteAccent, basePink, deepPink)
                    )
                }
                colorVariant < 0.85f -> { // 20% - Violet striée
                    val baseViolet = OrchideeColorHelper.rgb(186, 85, 211)
                    val whiteBase = OrchideeColorHelper.rgb(248, 245, 255)
                    val darkViolet = OrchideeColorHelper.rgb(138, 43, 226)
                    
                    OrchideeColorPalette(
                        primary = whiteBase,
                        secondary = baseViolet,
                        accent = darkViolet,
                        throat = OrchideeColorHelper.rgb(255, 200, 100),
                        veining = darkViolet,
                        spotColor = baseViolet,
                        gradientStops = listOf(whiteBase, baseViolet, darkViolet)
                    )
                }
                colorVariant < 0.95f -> { // 10% - Jaune rare
                    val baseYellow = OrchideeColorHelper.rgb(255, 255, 224)
                    val deepYellow = OrchideeColorHelper.rgb(255, 215, 0)
                    val redAccent = OrchideeColorHelper.rgb(220, 20, 60)
                    
                    OrchideeColorPalette(
                        primary = baseYellow,
                        secondary = deepYellow,
                        accent = redAccent,
                        throat = OrchideeColorHelper.rgb(255, 140, 0),
                        veining = OrchideeColorHelper.rgb(200, 100, 0),
                        spotColor = redAccent,
                        gradientStops = listOf(baseYellow, deepYellow, redAccent)
                    )
                }
                else -> { // 5% - Verte ultra-rare
                    val baseGreen = OrchideeColorHelper.rgb(240, 255, 240)
                    val deepGreen = OrchideeColorHelper.rgb(144, 238, 144)
                    val purpleAccent = OrchideeColorHelper.rgb(147, 112, 219)
                    
                    OrchideeColorPalette(
                        primary = baseGreen,
                        secondary = deepGreen,
                        accent = purpleAccent,
                        throat = OrchideeColorHelper.rgb(255, 255, 100),
                        veining = OrchideeColorHelper.rgb(100, 150, 100),
                        spotColor = purpleAccent,
                        gradientStops = listOf(baseGreen, deepGreen, purpleAccent)
                    )
                }
            }
        }
        
        private fun generateCattleyaColors(rng: Random): OrchideeColorPalette {
            val colorVariant = rng.nextFloat()
            
            return when {
                colorVariant < 0.35f -> { // 35% - Pourpre royal
                    val basePurple = OrchideeColorHelper.rgb(138, 43, 226)
                    val deepPurple = OrchideeColorHelper.rgb(75, 0, 130)
                    val goldThroat = OrchideeColorHelper.rgb(255, 215, 0)
                    
                    OrchideeColorPalette(
                        primary = basePurple,
                        secondary = deepPurple,
                        accent = OrchideeColorHelper.rgb(255, 182, 193),
                        throat = goldThroat,
                        veining = OrchideeColorHelper.rgb(50, 0, 80),
                        spotColor = OrchideeColorHelper.rgb(255, 105, 180),
                        gradientStops = listOf(basePurple, deepPurple, goldThroat)
                    )
                }
                colorVariant < 0.60f -> { // 25% - Lavande élégante
                    val baseLavender = OrchideeColorHelper.rgb(230, 230, 250)
                    val deepLavender = OrchideeColorHelper.rgb(147, 112, 219)
                    val whiteAccent = OrchideeColorHelper.rgb(255, 248, 220)
                    
                    OrchideeColorPalette(
                        primary = baseLavender,
                        secondary = deepLavender,
                        accent = whiteAccent,
                        throat = OrchideeColorHelper.rgb(255, 255, 150),
                        veining = OrchideeColorHelper.rgb(120, 80, 150),
                        spotColor = OrchideeColorHelper.rgb(186, 85, 211),
                        gradientStops = listOf(whiteAccent, baseLavender, deepLavender)
                    )
                }
                colorVariant < 0.80f -> { // 20% - Orange tropicale
                    val baseOrange = OrchideeColorHelper.rgb(255, 165, 0)
                    val deepOrange = OrchideeColorHelper.rgb(255, 69, 0)
                    val yellowAccent = OrchideeColorHelper.rgb(255, 255, 224)
                    
                    OrchideeColorPalette(
                        primary = baseOrange,
                        secondary = deepOrange,
                        accent = yellowAccent,
                        throat = OrchideeColorHelper.rgb(255, 215, 0),
                        veining = OrchideeColorHelper.rgb(200, 50, 0),
                        spotColor = OrchideeColorHelper.rgb(220, 20, 60),
                        gradientStops = listOf(yellowAccent, baseOrange, deepOrange)
                    )
                }
                colorVariant < 0.92f -> { // 12% - Blanc pur avec gorge colorée
                    val pureWhite = OrchideeColorHelper.rgb(255, 255, 255)
                    val creamWhite = OrchideeColorHelper.rgb(255, 253, 208)
                    val magentaThroat = OrchideeColorHelper.rgb(255, 0, 255)
                    
                    OrchideeColorPalette(
                        primary = pureWhite,
                        secondary = creamWhite,
                        accent = magentaThroat,
                        throat = magentaThroat,
                        veining = OrchideeColorHelper.rgb(200, 0, 200),
                        spotColor = OrchideeColorHelper.rgb(255, 105, 180),
                        gradientStops = listOf(pureWhite, creamWhite, magentaThroat)
                    )
                }
                else -> { // 8% - Noire rare
                    val darkMaroon = OrchideeColorHelper.rgb(85, 26, 26)
                    val blackishRed = OrchideeColorHelper.rgb(50, 20, 20)
                    val goldAccent = OrchideeColorHelper.rgb(255, 215, 0)
                    
                    OrchideeColorPalette(
                        primary = darkMaroon,
                        secondary = blackishRed,
                        accent = goldAccent,
                        throat = goldAccent,
                        veining = OrchideeColorHelper.rgb(100, 50, 50),
                        spotColor = OrchideeColorHelper.rgb(220, 20, 60),
                        gradientStops = listOf(darkMaroon, blackishRed, goldAccent)
                    )
                }
            }
        }
        
        private fun generateVandaColors(rng: Random): OrchideeColorPalette {
            val colorVariant = rng.nextFloat()
            
            return when {
                colorVariant < 0.25f -> { // 25% - Bleue rare
                    val baseBlue = OrchideeColorHelper.rgb(65, 105, 225)
                    val deepBlue = OrchideeColorHelper.rgb(25, 25, 112)
                    val whiteAccent = OrchideeColorHelper.rgb(240, 248, 255)
                    
                    OrchideeColorPalette(
                        primary = baseBlue,
                        secondary = deepBlue,
                        accent = whiteAccent,
                        throat = OrchideeColorHelper.rgb(255, 255, 255),
                        veining = OrchideeColorHelper.rgb(0, 0, 139),
                        spotColor = OrchideeColorHelper.rgb(72, 61, 139),
                        gradientStops = listOf(whiteAccent, baseBlue, deepBlue)
                    )
                }
                colorVariant < 0.50f -> { // 25% - Tessellée classique
                    val baseViolet = OrchideeColorHelper.rgb(147, 112, 219)
                    val checkeredPurple = OrchideeColorHelper.rgb(75, 0, 130)
                    val lightBase = OrchideeColorHelper.rgb(230, 230, 250)
                    
                    OrchideeColorPalette(
                        primary = lightBase,
                        secondary = baseViolet,
                        accent = checkeredPurple,
                        throat = OrchideeColorHelper.rgb(255, 255, 200),
                        veining = checkeredPurple,
                        spotColor = baseViolet,
                        gradientStops = listOf(lightBase, baseViolet, checkeredPurple)
                    )
                }
                colorVariant < 0.75f -> { // 25% - Orange flamboyante
                    val baseOrange = OrchideeColorHelper.rgb(255, 140, 0)
                    val redOrange = OrchideeColorHelper.rgb(255, 69, 0)
                    val yellowBase = OrchideeColorHelper.rgb(255, 255, 224)
                    
                    OrchideeColorPalette(
                        primary = yellowBase,
                        secondary = baseOrange,
                        accent = redOrange,
                        throat = OrchideeColorHelper.rgb(255, 215, 0),
                        veining = OrchideeColorHelper.rgb(200, 50, 0),
                        spotColor = redOrange,
                        gradientStops = listOf(yellowBase, baseOrange, redOrange)
                    )
                }
                colorVariant < 0.90f -> { // 15% - Rose fuchsia
                    val baseFuchsia = OrchideeColorHelper.rgb(255, 20, 147)
                    val deepFuchsia = OrchideeColorHelper.rgb(199, 21, 133)
                    val pinkBase = OrchideeColorHelper.rgb(255, 182, 193)
                    
                    OrchideeColorPalette(
                        primary = pinkBase,
                        secondary = baseFuchsia,
                        accent = deepFuchsia,
                        throat = OrchideeColorHelper.rgb(255, 255, 255),
                        veining = OrchideeColorHelper.rgb(139, 0, 139),
                        spotColor = baseFuchsia,
                        gradientStops = listOf(pinkBase, baseFuchsia, deepFuchsia)
                    )
                }
                else -> { // 10% - Multicolore arc-en-ciel
                    val rainbow1 = OrchideeColorHelper.rgb(255, 99, 71)
                    val rainbow2 = OrchideeColorHelper.rgb(255, 215, 0)
                    val rainbow3 = OrchideeColorHelper.rgb(124, 252, 0)
                    
                    OrchideeColorPalette(
                        primary = rainbow1,
                        secondary = rainbow2,
                        accent = rainbow3,
                        throat = OrchideeColorHelper.rgb(255, 105, 180),
                        veining = OrchideeColorHelper.rgb(75, 0, 130),
                        spotColor = OrchideeColorHelper.rgb(65, 105, 225),
                        gradientStops = listOf(rainbow1, rainbow2, rainbow3)
                    )
                }
            }
        }
        
        private fun generateOncidiumColors(rng: Random): OrchideeColorPalette {
            val colorVariant = rng.nextFloat()
            
            return when {
                colorVariant < 0.60f -> { // 60% - Jaune dancing lady classique
                    val baseYellow = OrchideeColorHelper.rgb(255, 215, 0)
                    val brightYellow = OrchideeColorHelper.rgb(255, 255, 0)
                    val brownSpots = OrchideeColorHelper.rgb(139, 69, 19)
                    
                    OrchideeColorPalette(
                        primary = baseYellow,
                        secondary = brightYellow,
                        accent = brownSpots,
                        throat = OrchideeColorHelper.rgb(255, 140, 0),
                        veining = brownSpots,
                        spotColor = OrchideeColorHelper.rgb(160, 82, 45),
                        gradientStops = listOf(brightYellow, baseYellow, brownSpots)
                    )
                }
                colorVariant < 0.80f -> { // 20% - Chocolat et or
                    val chocolate = OrchideeColorHelper.rgb(139, 69, 19)
                    val gold = OrchideeColorHelper.rgb(255, 215, 0)
                    val cream = OrchideeColorHelper.rgb(255, 253, 208)
                    
                    OrchideeColorPalette(
                        primary = chocolate,
                        secondary = gold,
                        accent = cream,
                        throat = gold,
                        veining = OrchideeColorHelper.rgb(101, 67, 33),
                        spotColor = OrchideeColorHelper.rgb(160, 82, 45),
                        gradientStops = listOf(cream, gold, chocolate)
                    )
                }
                colorVariant < 0.92f -> { // 12% - Rouge et jaune
                    val baseRed = OrchideeColorHelper.rgb(220, 20, 60)
                    val brightYellow = OrchideeColorHelper.rgb(255, 255, 0)
                    val orange = OrchideeColorHelper.rgb(255, 165, 0)
                    
                    OrchideeColorPalette(
                        primary = baseRed,
                        secondary = orange,
                        accent = brightYellow,
                        throat = brightYellow,
                        veining = OrchideeColorHelper.rgb(139, 0, 0),
                        spotColor = OrchideeColorHelper.rgb(255, 69, 0),
                        gradientStops = listOf(brightYellow, orange, baseRed)
                    )
                }
                else -> { // 8% - Blanc et bordeaux
                    val creamWhite = OrchideeColorHelper.rgb(255, 248, 220)
                    val burgundy = OrchideeColorHelper.rgb(128, 0, 32)
                    val gold = OrchideeColorHelper.rgb(255, 215, 0)
                    
                    OrchideeColorPalette(
                        primary = creamWhite,
                        secondary = burgundy,
                        accent = gold,
                        throat = gold,
                        veining = burgundy,
                        spotColor = OrchideeColorHelper.rgb(139, 0, 139),
                        gradientStops = listOf(creamWhite, gold, burgundy)
                    )
                }
            }
        }
        
        private fun generateDendrobiumColors(rng: Random): OrchideeColorPalette {
            val colorVariant = rng.nextFloat()
            
            return when {
                colorVariant < 0.30f -> { // 30% - Blanc pur
                    val pureWhite = OrchideeColorHelper.rgb(255, 255, 255)
                    val creamWhite = OrchideeColorHelper.rgb(255, 253, 208)
                    val yellowCenter = OrchideeColorHelper.rgb(255, 255, 224)
                    
                    OrchideeColorPalette(
                        primary = pureWhite,
                        secondary = creamWhite,
                        accent = yellowCenter,
                        throat = yellowCenter,
                        veining = OrchideeColorHelper.rgb(220, 220, 220),
                        spotColor = OrchideeColorHelper.rgb(255, 182, 193),
                        gradientStops = listOf(pureWhite, creamWhite, yellowCenter)
                    )
                }
                colorVariant < 0.55f -> { // 25% - Violet délicat
                    val lightViolet = OrchideeColorHelper.rgb(221, 160, 221)
                    val mediumViolet = OrchideeColorHelper.rgb(186, 85, 211)
                    val whiteBase = OrchideeColorHelper.rgb(248, 248, 255)
                    
                    OrchideeColorPalette(
                        primary = whiteBase,
                        secondary = lightViolet,
                        accent = mediumViolet,
                        throat = OrchideeColorHelper.rgb(255, 255, 200),
                        veining = mediumViolet,
                        spotColor = OrchideeColorHelper.rgb(147, 112, 219),
                        gradientStops = listOf(whiteBase, lightViolet, mediumViolet)
                    )
                }
                colorVariant < 0.75f -> { // 20% - Rose tendre
                    val lightPink = OrchideeColorHelper.rgb(255, 182, 193)
                    val mediumPink = OrchideeColorHelper.rgb(255, 105, 180)
                    val whiteBase = OrchideeColorHelper.rgb(255, 248, 220)
                    
                    OrchideeColorPalette(
                        primary = whiteBase,
                        secondary = lightPink,
                        accent = mediumPink,
                        throat = OrchideeColorHelper.rgb(255, 255, 224),
                        veining = OrchideeColorHelper.rgb(219, 112, 147),
                        spotColor = mediumPink,
                        gradientStops = listOf(whiteBase, lightPink, mediumPink)
                    )
                }
                colorVariant < 0.90f -> { // 15% - Jaune solaire
                    val lightYellow = OrchideeColorHelper.rgb(255, 255, 224)
                    val brightYellow = OrchideeColorHelper.rgb(255, 215, 0)
                    val orange = OrchideeColorHelper.rgb(255, 165, 0)
                    
                    OrchideeColorPalette(
                        primary = lightYellow,
                        secondary = brightYellow,
                        accent = orange,
                        throat = orange,
                        veining = OrchideeColorHelper.rgb(218, 165, 32),
                        spotColor = OrchideeColorHelper.rgb(255, 140, 0),
                        gradientStops = listOf(lightYellow, brightYellow, orange)
                    )
                }
                else -> { // 10% - Bicolore rare
                    val lavender = OrchideeColorHelper.rgb(230, 230, 250)
                    val deepPurple = OrchideeColorHelper.rgb(75, 0, 130)
                    val white = OrchideeColorHelper.rgb(255, 255, 255)
                    
                    OrchideeColorPalette(
                        primary = white,
                        secondary = lavender,
                        accent = deepPurple,
                        throat = OrchideeColorHelper.rgb(255, 215, 0),
                        veining = deepPurple,
                        spotColor = OrchideeColorHelper.rgb(138, 43, 226),
                        gradientStops = listOf(white, lavender, deepPurple)
                    )
                }
            }
        }
        
        private fun generateCymbidiumColors(rng: Random): OrchideeColorPalette {
            val colorVariant = rng.nextFloat()
            
            return when {
                colorVariant < 0.25f -> { // 25% - Crème et bordeaux
                    val cream = OrchideeColorHelper.rgb(255, 253, 208)
                    val burgundy = OrchideeColorHelper.rgb(128, 0, 32)
                    val gold = OrchideeColorHelper.rgb(255, 215, 0)
                    
                    OrchideeColorPalette(
                        primary = cream,
                        secondary = gold,
                        accent = burgundy,
                        throat = burgundy,
                        veining = OrchideeColorHelper.rgb(139, 69, 19),
                        spotColor = burgundy,
                        gradientStops = listOf(cream, gold, burgundy)
                    )
                }
                colorVariant < 0.45f -> { // 20% - Vert et bordeaux
                    val lightGreen = OrchideeColorHelper.rgb(240, 255, 240)
                    val mediumGreen = OrchideeColorHelper.rgb(144, 238, 144)
                    val burgundySpots = OrchideeColorHelper.rgb(128, 0, 32)
                    
                    OrchideeColorPalette(
                        primary = lightGreen,
                        secondary = mediumGreen,
                        accent = burgundySpots,
                        throat = OrchideeColorHelper.rgb(255, 255, 200),
                        veining = OrchideeColorHelper.rgb(85, 107, 47),
                        spotColor = burgundySpots,
                        gradientStops = listOf(lightGreen, mediumGreen, burgundySpots)
                    )
                }
                colorVariant < 0.65f -> { // 20% - Rose et blanc
                    val white = OrchideeColorHelper.rgb(255, 255, 255)
                    val lightPink = OrchideeColorHelper.rgb(255, 182, 193)
                    val deepPink = OrchideeColorHelper.rgb(255, 105, 180)
                    
                    OrchideeColorPalette(
                        primary = white,
                        secondary = lightPink,
                        accent = deepPink,
                        throat = OrchideeColorHelper.rgb(255, 255, 224),
                        veining = OrchideeColorHelper.rgb(219, 112, 147),
                        spotColor = deepPink,
                        gradientStops = listOf(white, lightPink, deepPink)
                    )
                }
                colorVariant < 0.80f -> { // 15% - Jaune intense
                    val paleYellow = OrchideeColorHelper.rgb(255, 255, 224)
                    val brightYellow = OrchideeColorHelper.rgb(255, 215, 0)
                    val redSpots = OrchideeColorHelper.rgb(220, 20, 60)
                    
                    OrchideeColorPalette(
                        primary = paleYellow,
                        secondary = brightYellow,
                        accent = redSpots,
                        throat = OrchideeColorHelper.rgb(255, 140, 0),
                        veining = OrchideeColorHelper.rgb(184, 134, 11),
                        spotColor = redSpots,
                        gradientStops = listOf(paleYellow, brightYellow, redSpots)
                    )
                }
                colorVariant < 0.92f -> { // 12% - Chocolat premium
                    val chocolate = OrchideeColorHelper.rgb(139, 69, 19)
                    val darkChocolate = OrchideeColorHelper.rgb(101, 67, 33)
                    val cream = OrchideeColorHelper.rgb(255, 248, 220)
                    
                    OrchideeColorPalette(
                        primary = chocolate,
                        secondary = darkChocolate,
                        accent = cream,
                        throat = OrchideeColorHelper.rgb(255, 215, 0),
                        veining = OrchideeColorHelper.rgb(160, 82, 45),
                        spotColor = cream,
                        gradientStops = listOf(cream, chocolate, darkChocolate)
                    )
                }
                else -> { // 8% - Multicolore arc-en-ciel
                    val pastelPink = OrchideeColorHelper.rgb(255, 182, 193)
                    val pastelBlue = OrchideeColorHelper.rgb(173, 216, 230)
                    val pastelYellow = OrchideeColorHelper.rgb(255, 255, 224)
                    
                    OrchideeColorPalette(
                        primary = pastelYellow,
                        secondary = pastelPink,
                        accent = pastelBlue,
                        throat = OrchideeColorHelper.rgb(255, 215, 0),
                        veining = OrchideeColorHelper.rgb(147, 112, 219),
                        spotColor = OrchideeColorHelper.rgb(186, 85, 211),
                        gradientStops = listOf(pastelYellow, pastelPink, pastelBlue)
                    )
                }
            }
        }
        
        // ==================== GÉNÉRATION DES FORMES ====================
        
        /**
         * Génère des variations de forme selon l'espèce et la taille
         */
        fun generateShape(
            species: OrchideeSpecies, 
            sizeCategory: OrchideeSizeCategory, 
            rng: Random = Random
        ): OrchideeShapeVariation {
            val baseShape = getBaseShapeForSpecies(species)
            val sizeMultiplier = sizeCategory.sizeMultiplier
            
            return OrchideeShapeVariation(
                sepalLength = (baseShape.sepalLength * sizeMultiplier * randomVariation(rng, 0.15f)).coerceIn(0.5f, 2.0f),
                sepalWidth = (baseShape.sepalWidth * sizeMultiplier * randomVariation(rng, 0.2f)).coerceIn(0.4f, 1.8f),
                petalLength = (baseShape.petalLength * sizeMultiplier * randomVariation(rng, 0.12f)).coerceIn(0.6f, 1.6f),
                petalWidth = (baseShape.petalWidth * sizeMultiplier * randomVariation(rng, 0.18f)).coerceIn(0.5f, 1.7f),
                petalCurvature = (baseShape.petalCurvature * randomVariation(rng, 0.25f)).coerceIn(0.1f, 1.0f),
                labelleSize = (baseShape.labelleSize * sizeMultiplier * randomVariation(rng, 0.3f)).coerceIn(0.4f, 2.0f),
                labelleForm = baseShape.labelleForm,
                ruffledEdges = (baseShape.ruffledEdges * randomVariation(rng, 0.4f)).coerceIn(0.0f, 1.0f),
                throatDepth = (baseShape.throatDepth * randomVariation(rng, 0.2f)).coerceIn(0.2f, 1.2f),
                spreadAngle = (baseShape.spreadAngle + rng.nextFloat() * 30f - 15f).coerceIn(45f, 135f),
                flatness = (baseShape.flatness * randomVariation(rng, 0.15f)).coerceIn(0.3f, 1.0f),
                asymmetry = rng.nextFloat() * 0.25f // Asymétrie naturelle
            )
        }
        
        private fun getBaseShapeForSpecies(species: OrchideeSpecies): OrchideeShapeVariation {
            return when (species) {
                OrchideeSpecies.PHALAENOPSIS -> OrchideeShapeVariation(
                    sepalLength = 1.0f, sepalWidth = 0.8f, petalLength = 1.0f, petalWidth = 0.9f,
                    petalCurvature = 0.4f, labelleSize = 0.8f, labelleForm = LabelleForm.BUTTERFLY_SPREAD,
                    ruffledEdges = 0.2f, throatDepth = 0.5f, spreadAngle = 85f, flatness = 0.7f, asymmetry = 0.1f
                )
                OrchideeSpecies.CATTLEYA -> OrchideeShapeVariation(
                    sepalLength = 1.2f, sepalWidth = 1.0f, petalLength = 1.3f, petalWidth = 1.2f,
                    petalCurvature = 0.7f, labelleSize = 1.4f, labelleForm = LabelleForm.TRUMPET_FLARED,
                    ruffledEdges = 0.8f, throatDepth = 0.9f, spreadAngle = 95f, flatness = 0.6f, asymmetry = 0.05f
                )
                OrchideeSpecies.VANDA -> OrchideeShapeVariation(
                    sepalLength = 0.9f, sepalWidth = 0.9f, petalLength = 0.9f, petalWidth = 0.9f,
                    petalCurvature = 0.2f, labelleSize = 0.6f, labelleForm = LabelleForm.SMALL_POINTED,
                    ruffledEdges = 0.1f, throatDepth = 0.4f, spreadAngle = 110f, flatness = 0.9f, asymmetry = 0.02f
                )
                OrchideeSpecies.ONCIDIUM -> OrchideeShapeVariation(
                    sepalLength = 0.7f, sepalWidth = 0.6f, petalLength = 0.7f, petalWidth = 0.5f,
                    petalCurvature = 0.3f, labelleSize = 1.2f, labelleForm = LabelleForm.DANCING_SKIRT,
                    ruffledEdges = 0.5f, throatDepth = 0.6f, spreadAngle = 75f, flatness = 0.8f, asymmetry = 0.15f
                )
                OrchideeSpecies.DENDROBIUM -> OrchideeShapeVariation(
                    sepalLength = 0.8f, sepalWidth = 0.7f, petalLength = 0.8f, petalWidth = 0.6f,
                    petalCurvature = 0.3f, labelleSize = 0.7f, labelleForm = LabelleForm.TUBE_NARROW,
                    ruffledEdges = 0.3f, throatDepth = 0.8f, spreadAngle = 70f, flatness = 0.5f, asymmetry = 0.08f
                )
                OrchideeSpecies.CYMBIDIUM -> OrchideeShapeVariation(
                    sepalLength = 1.1f, sepalWidth = 0.9f, petalLength = 1.0f, petalWidth = 0.8f,
                    petalCurvature = 0.4f, labelleSize = 1.0f, labelleForm = LabelleForm.BOAT_SHAPED,
                    ruffledEdges = 0.4f, throatDepth = 0.7f, spreadAngle = 90f, flatness = 0.7f, asymmetry = 0.12f
                )
            }
        }
        
        // ==================== GÉNÉRATION DES MOTIFS ====================
        
        /**
         * Génère un motif approprié selon l'espèce
         */
        fun generatePattern(species: OrchideeSpecies, rng: Random = Random): OrchideePatternType {
            val patterns = getPatternsForSpecies(species)
            val selectedPattern = patterns[rng.nextInt(patterns.size)]
            val secondaryPattern = if (rng.nextFloat() < 0.3f) {
                val remainingPatterns = patterns.filter { it != selectedPattern }
                if (remainingPatterns.isNotEmpty()) remainingPatterns[rng.nextInt(remainingPatterns.size)] else null
            } else null
            
            return OrchideePatternType(
                primary = selectedPattern,
                secondary = secondaryPattern,
                intensity = 0.3f + rng.nextFloat() * 0.6f, // 0.3 à 0.9
                coverage = 0.2f + rng.nextFloat() * 0.6f,   // 0.2 à 0.8
                blendMode = PatternBlendMode.values()[rng.nextInt(PatternBlendMode.values().size)]
            )
        }
        
        private fun getPatternsForSpecies(species: OrchideeSpecies): List<PatternStyle> {
            return when (species) {
                OrchideeSpecies.PHALAENOPSIS -> listOf(
                    PatternStyle.SOLID, PatternStyle.VEINED, PatternStyle.STRIPED,
                    PatternStyle.THROAT_BURST, PatternStyle.GRADIENT_RADIAL
                )
                OrchideeSpecies.CATTLEYA -> listOf(
                    PatternStyle.SOLID, PatternStyle.GRADIENT_LINEAR, PatternStyle.EDGED,
                    PatternStyle.FLAMED, PatternStyle.WATERCOLOR
                )
                OrchideeSpecies.VANDA -> listOf(
                    PatternStyle.SPOTTED, PatternStyle.MOTTLED, PatternStyle.STRIPED,
                    PatternStyle.GRADIENT_RADIAL, PatternStyle.VEINED
                )
                OrchideeSpecies.ONCIDIUM -> listOf(
                    PatternStyle.SPOTTED, PatternStyle.SOLID, PatternStyle.EDGED,
                    PatternStyle.THROAT_BURST
                )
                OrchideeSpecies.DENDROBIUM -> listOf(
                    PatternStyle.SOLID, PatternStyle.GRADIENT_LINEAR, PatternStyle.VEINED,
                    PatternStyle.PICOTEE
                )
                OrchideeSpecies.CYMBIDIUM -> listOf(
                    PatternStyle.SPOTTED, PatternStyle.STRIPED, PatternStyle.EDGED,
                    PatternStyle.MOTTLED, PatternStyle.GRADIENT_LINEAR
                )
            }
        }
        
        // ==================== GÉNÉRATION DES TAILLES ====================
        
        /**
         * Génère une catégorie de taille selon la rareté
         */
        fun generateSizeCategory(rng: Random = Random): OrchideeSizeCategory {
            val roll = rng.nextFloat()
            
            return when {
                roll < 0.50f -> OrchideeSizeCategory.STANDARD     // 50% - Normale
                roll < 0.70f -> OrchideeSizeCategory.COMPACT      // 20% - Petite
                roll < 0.85f -> OrchideeSizeCategory.LARGE        // 15% - Grande
                roll < 0.95f -> OrchideeSizeCategory.MINIATURE    // 10% - Miniature
                else -> OrchideeSizeCategory.GIANT                // 5% - Géante
            }
        }
        
        // ==================== CALCUL DE RARETÉ ====================
        
        /**
         * Calcule la rareté finale basée sur tous les composants
         */
        fun calculateRarity(
            species: OrchideeSpecies,
            sizeCategory: OrchideeSizeCategory,
            colorPalette: OrchideeColorPalette,
            patternType: OrchideePatternType
        ): Float {
            val speciesRarity = species.baseRarity
            val sizeRarity = sizeCategory.rarity
            val patternRarity = calculatePatternRarity(patternType)
            val colorRarity = calculateColorRarity(colorPalette)
            
            // Formule de rareté composite
            return (speciesRarity * 0.3f + 
                   sizeRarity * 0.2f + 
                   patternRarity * 0.25f + 
                   colorRarity * 0.25f).coerceIn(0f, 1f)
        }
        
        private fun calculatePatternRarity(patternType: OrchideePatternType): Float {
            val primaryRarity = when (patternType.primary) {
                PatternStyle.SOLID -> 0.1f
                PatternStyle.GRADIENT_LINEAR -> 0.2f
                PatternStyle.VEINED -> 0.3f
                PatternStyle.SPOTTED -> 0.4f
                PatternStyle.STRIPED -> 0.5f
                PatternStyle.EDGED -> 0.4f
                PatternStyle.THROAT_BURST -> 0.6f
                PatternStyle.PICOTEE -> 0.7f
                PatternStyle.MOTTLED -> 0.6f
                PatternStyle.FLAMED -> 0.8f
                PatternStyle.WATERCOLOR -> 0.9f
                PatternStyle.GRADIENT_RADIAL -> 0.7f
            }
            
            val secondaryBonus = if (patternType.secondary != null) 0.3f else 0f
            val intensityMultiplier = patternType.intensity
            
            return (primaryRarity + secondaryBonus) * intensityMultiplier
        }
        
        private fun calculateColorRarity(colorPalette: OrchideeColorPalette): Float {
            // Calcul basé sur la distance des couleurs par rapport aux couleurs communes
            val commonColors = listOf(
                OrchideeColorHelper.rgb(255, 255, 255), // Blanc
                OrchideeColorHelper.rgb(255, 182, 193), // Rose clair
                OrchideeColorHelper.rgb(186, 85, 211)   // Violet moyen
            )
            
            var minDistance = Float.MAX_VALUE
            for (commonColor in commonColors) {
                val distance = colorDistance(colorPalette.primary, commonColor)
                if (distance < minDistance) {
                    minDistance = distance
                }
            }
            
            // Plus la couleur est éloignée des communes, plus elle est rare
            return (minDistance / 255f).coerceIn(0f, 1f)
        }
        
        private fun colorDistance(color1: Int, color2: Int): Float {
            val r1 = android.graphics.Color.red(color1)
            val g1 = android.graphics.Color.green(color1)
            val b1 = android.graphics.Color.blue(color1)
            
            val r2 = android.graphics.Color.red(color2)
            val g2 = android.graphics.Color.green(color2)
            val b2 = android.graphics.Color.blue(color2)
            
            return sqrt(((r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2)).toFloat())
        }
        
        // ==================== FONCTIONS UTILITAIRES ====================
        
        private fun randomVariation(rng: Random, maxVariation: Float): Float {
            return 1f + (rng.nextFloat() - 0.5f) * 2f * maxVariation
        }
        
        /**
         * Génère une orchidée avec des contraintes spécifiques
         */
        fun generateWithConstraints(
            forceSpecies: OrchideeSpecies? = null,
            forceRarity: Float? = null,
            forceSeed: Long? = null
        ): OrchideeGenetics {
            var attempts = 0
            var result: OrchideeGenetics
            
            do {
                result = generate(forceSeed ?: Random.nextLong())
                attempts++
                
                if (attempts > 100) break // Éviter les boucles infinies
                
            } while ((forceSpecies != null && result.species != forceSpecies) ||
                    (forceRarity != null && abs(result.rarity - forceRarity) > 0.1f))
            
            return result
        }
    }
}
