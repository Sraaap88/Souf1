package com.example.souffleforcetest

import kotlin.random.Random
import kotlin.math.*

/**
 * GÉNÉRATEUR SPÉCIALISÉ DE COULEURS RÉALISTES POUR ORCHIDÉES
 * Crée des palettes harmonieuses basées sur la botanique réelle
 */
class OrchideeColorGenerator {
    
    companion object {
        
        // ==================== PALETTES SPÉCIALISÉES PAR ESPÈCE ====================
        
        /**
         * Génère des couleurs Phalaenopsis ultra-réalistes
         * Spécialité : Blancs, roses et violets avec centres jaunes
         */
        fun generatePhalaenopsisColors(rng: Random = Random): OrchideeColorPalette {
            val variantType = rng.nextFloat()
            
            return when {
                variantType < 0.35f -> createPhalaenopsisWhite(rng)
                variantType < 0.60f -> createPhalaenopsisPink(rng)
                variantType < 0.80f -> createPhalaenopsisStriped(rng)
                variantType < 0.92f -> createPhalaenopsisYellow(rng)
                else -> createPhalaenopsisNovelty(rng)
            }
        }
        
        private fun createPhalaenopsisWhite(rng: Random): OrchideeColorPalette {
            // Base blanche avec nuances subtiles
            val baseWhite = OrchideeColorHelper.rgb(252, 248, 255)
            val warmWhite = OrchideeColorHelper.rgb(255, 251, 245)
            val coolWhite = OrchideeColorHelper.rgb(248, 248, 255)
            
            // Centre jaune classique
            val centerYellow = generateYellowCenter(rng)
            
            // Accent rose délicat
            val pinkAccent = OrchideeColorHelper.rgb(
                200 + rng.nextInt(40),  // 200-240
                140 + rng.nextInt(30),  // 140-170
                160 + rng.nextInt(40)   // 160-200
            )
            
            // Nervures subtiles
            val veinColor = OrchideeColorHelper.blendColors(baseWhite, pinkAccent, 0.3f)
            
            return OrchideeColorPalette(
                primary = baseWhite,
                secondary = warmWhite,
                accent = pinkAccent,
                throat = centerYellow,
                veining = veinColor,
                spotColor = generateHarmoniousAccent(pinkAccent, rng),
                gradientStops = generateSmoothGradient(coolWhite, warmWhite, centerYellow, rng)
            )
        }
        
        private fun createPhalaenopsisPink(rng: Random): OrchideeColorPalette {
            // Rose de base avec variations
            val basePink = OrchideeColorHelper.rgb(
                255,
                180 + rng.nextInt(40),  // 180-220
                190 + rng.nextInt(30)   // 190-220
            )
            
            val deepPink = OrchideeColorHelper.rgb(
                255,
                120 + rng.nextInt(50),  // 120-170
                160 + rng.nextInt(40)   // 160-200
            )
            
            // Centre contrastant
            val centerColor = if (rng.nextFloat() < 0.7f) {
                generateYellowCenter(rng)
            } else {
                OrchideeColorHelper.rgb(255, 255, 255) // Centre blanc
            }
            
            return OrchideeColorPalette(
                primary = basePink,
                secondary = deepPink,
                accent = centerColor,
                throat = centerColor,
                veining = generateDarkerShade(deepPink, 0.3f),
                spotColor = generateComplementaryColor(basePink),
                gradientStops = generateSmoothGradient(centerColor, basePink, deepPink, rng)
            )
        }
        
        private fun createPhalaenopsisStriped(rng: Random): OrchideeColorPalette {
            // Base claire pour les rayures
            val baseColor = OrchideeColorHelper.rgb(248, 245, 255)
            
            // Couleur des rayures
            val stripeColors = listOf(
                OrchideeColorHelper.rgb(186, 85, 211),   // Violet
                OrchideeColorHelper.rgb(219, 112, 147),  // Rose foncé
                OrchideeColorHelper.rgb(147, 112, 219)   // Violet clair
            )
            val stripeColor = stripeColors[rng.nextInt(stripeColors.size)]
            
            return OrchideeColorPalette(
                primary = baseColor,
                secondary = stripeColor,
                accent = generateDarkerShade(stripeColor, 0.2f),
                throat = generateYellowCenter(rng),
                veining = stripeColor,
                spotColor = generateHarmoniousAccent(stripeColor, rng),
                gradientStops = createStripedGradient(baseColor, stripeColor, rng)
            )
        }
        
        private fun createPhalaenopsisYellow(rng: Random): OrchideeColorPalette {
            // Jaune rare et précieux
            val baseYellow = OrchideeColorHelper.rgb(
                255,
                240 + rng.nextInt(15),  // 240-255
                180 + rng.nextInt(40)   // 180-220
            )
            
            val deepYellow = OrchideeColorHelper.rgb(255, 215, 0)
            val redAccent = OrchideeColorHelper.rgb(220, 20, 60)
            
            return OrchideeColorPalette(
                primary = baseYellow,
                secondary = deepYellow,
                accent = redAccent,
                throat = generateOrangeCenter(rng),
                veining = OrchideeColorHelper.rgb(200, 150, 0),
                spotColor = redAccent,
                gradientStops = generateSmoothGradient(baseYellow, deepYellow, redAccent, rng)
            )
        }
        
        private fun createPhalaenopsisNovelty(rng: Random): OrchideeColorPalette {
            // Couleurs rares et fantaisie
            val noveltyBase = when (rng.nextInt(3)) {
                0 -> OrchideeColorHelper.rgb(240, 255, 240) // Vert clair
                1 -> OrchideeColorHelper.rgb(255, 248, 220) // Crème
                else -> OrchideeColorHelper.rgb(248, 248, 255) // Lavande très pâle
            }
            
            val accentColor = generateExoticAccent(rng)
            
            return OrchideeColorPalette(
                primary = noveltyBase,
                secondary = generateHarmoniousColor(noveltyBase, ColorHarmony.ANALOGOUS),
                accent = accentColor,
                throat = generateContrastingCenter(noveltyBase, rng),
                veining = generateDarkerShade(accentColor, 0.4f),
                spotColor = accentColor,
                gradientStops = generateExoticGradient(noveltyBase, accentColor, rng)
            )
        }
        
        /**
         * Génère des couleurs Cattleya spectaculaires
         * Spécialité : Pourpres royaux, lavandes et gorges colorées
         */
        fun generateCattleyaColors(rng: Random = Random): OrchideeColorPalette {
            val variantType = rng.nextFloat()
            
            return when {
                variantType < 0.30f -> createCattleyaPurple(rng)
                variantType < 0.55f -> createCattleyaLavender(rng)
                variantType < 0.75f -> createCattleyaOrange(rng)
                variantType < 0.90f -> createCattleyaWhite(rng)
                else -> createCattleyaExotic(rng)
            }
        }
        
        private fun createCattleyaPurple(rng: Random): OrchideeColorPalette {
            // Pourpre royal intense
            val basePurple = OrchideeColorHelper.rgb(
                120 + rng.nextInt(30),  // 120-150
                40 + rng.nextInt(20),   // 40-60
                200 + rng.nextInt(26)   // 200-226
            )
            
            val deepPurple = generateDarkerShade(basePurple, 0.4f)
            val goldThroat = OrchideeColorHelper.rgb(255, 215, 0)
            
            return OrchideeColorPalette(
                primary = basePurple,
                secondary = deepPurple,
                accent = OrchideeColorHelper.rgb(255, 182, 193),
                throat = goldThroat,
                veining = generateDarkerShade(basePurple, 0.6f),
                spotColor = OrchideeColorHelper.rgb(255, 105, 180),
                gradientStops = generateRichGradient(basePurple, deepPurple, goldThroat, rng)
            )
        }
        
        private fun createCattleyaLavender(rng: Random): OrchideeColorPalette {
            // Lavande sophistiquée
            val baseLavender = OrchideeColorHelper.rgb(
                200 + rng.nextInt(30),  // 200-230
                180 + rng.nextInt(40),  // 180-220
                230 + rng.nextInt(25)   // 230-255
            )
            
            val deepLavender = OrchideeColorHelper.rgb(147, 112, 219)
            val whiteAccent = OrchideeColorHelper.rgb(255, 248, 220)
            
            return OrchideeColorPalette(
                primary = baseLavender,
                secondary = deepLavender,
                accent = whiteAccent,
                throat = generateYellowCenter(rng),
                veining = generateDarkerShade(deepLavender, 0.3f),
                spotColor = OrchideeColorHelper.rgb(186, 85, 211),
                gradientStops = generateSmoothGradient(whiteAccent, baseLavender, deepLavender, rng)
            )
        }
        
        private fun createCattleyaOrange(rng: Random): OrchideeColorPalette {
            // Orange tropical vibrant
            val baseOrange = OrchideeColorHelper.rgb(
                255,
                140 + rng.nextInt(25),  // 140-165
                rng.nextInt(30)         // 0-30
            )
            
            val deepOrange = OrchideeColorHelper.rgb(255, 69, 0)
            val yellowAccent = OrchideeColorHelper.rgb(255, 255, 224)
            
            return OrchideeColorPalette(
                primary = baseOrange,
                secondary = deepOrange,
                accent = yellowAccent,
                throat = OrchideeColorHelper.rgb(255, 215, 0),
                veining = generateDarkerShade(deepOrange, 0.3f),
                spotColor = OrchideeColorHelper.rgb(220, 20, 60),
                gradientStops = generateWarmGradient(yellowAccent, baseOrange, deepOrange, rng)
            )
        }
        
        private fun createCattleyaWhite(rng: Random): OrchideeColorPalette {
            // Blanc pur avec gorge spectaculaire
            val pureWhite = OrchideeColorHelper.rgb(255, 255, 255)
            val creamWhite = OrchideeColorHelper.rgb(255, 253, 208)
            
            // Gorge colorée dramatique
            val throatOptions = listOf(
                OrchideeColorHelper.rgb(255, 0, 255),    // Magenta
                OrchideeColorHelper.rgb(255, 69, 0),     // Orange rouge
                OrchideeColorHelper.rgb(138, 43, 226),   // Violet intense
                OrchideeColorHelper.rgb(255, 215, 0)     // Or
            )
            val dramaticThroat = throatOptions[rng.nextInt(throatOptions.size)]
            
            return OrchideeColorPalette(
                primary = pureWhite,
                secondary = creamWhite,
                accent = dramaticThroat,
                throat = dramaticThroat,
                veining = generateLighterShade(dramaticThroat, 0.6f),
                spotColor = generateHarmoniousAccent(dramaticThroat, rng),
                gradientStops = generateDramaticGradient(pureWhite, creamWhite, dramaticThroat, rng)
            )
        }
        
        private fun createCattleyaExotic(rng: Random): OrchideeColorPalette {
            // Variétés rares et exotiques
            val exoticBase = OrchideeColorHelper.rgb(
                50 + rng.nextInt(50),   // 50-100 (sombres)
                20 + rng.nextInt(30),   // 20-50
                20 + rng.nextInt(30)    // 20-50
            )
            
            val goldAccent = OrchideeColorHelper.rgb(255, 215, 0)
            
            return OrchideeColorPalette(
                primary = exoticBase,
                secondary = generateDarkerShade(exoticBase, 0.3f),
                accent = goldAccent,
                throat = goldAccent,
                veining = generateLighterShade(exoticBase, 0.4f),
                spotColor = OrchideeColorHelper.rgb(220, 20, 60),
                gradientStops = generateExoticGradient(exoticBase, goldAccent, rng)
            )
        }
        
        /**
         * Génère des couleurs Vanda uniques
         * Spécialité : Bleus rares, tessellations et couleurs plates
         */
        fun generateVandaColors(rng: Random = Random): OrchideeColorPalette {
            val variantType = rng.nextFloat()
            
            return when {
                variantType < 0.20f -> createVandaBlue(rng)      // Très rare
                variantType < 0.45f -> createVandaTessellated(rng)
                variantType < 0.70f -> createVandaOrange(rng)
                variantType < 0.85f -> createVandaFuchsia(rng)
                else -> createVandaRainbow(rng)
            }
        }
        
        private fun createVandaBlue(rng: Random): OrchideeColorPalette {
            // Bleu rare et précieux
            val baseBlue = OrchideeColorHelper.rgb(
                40 + rng.nextInt(40),   // 40-80
                80 + rng.nextInt(40),   // 80-120
                200 + rng.nextInt(55)   // 200-255
            )
            
            val deepBlue = OrchideeColorHelper.rgb(25, 25, 112)
            val whiteAccent = OrchideeColorHelper.rgb(240, 248, 255)
            
            return OrchideeColorPalette(
                primary = baseBlue,
                secondary = deepBlue,
                accent = whiteAccent,
                throat = OrchideeColorHelper.rgb(255, 255, 255),
                veining = generateDarkerShade(baseBlue, 0.4f),
                spotColor = OrchideeColorHelper.rgb(72, 61, 139),
                gradientStops = generateCoolGradient(whiteAccent, baseBlue, deepBlue, rng)
            )
        }
        
        private fun createVandaTessellated(rng: Random): OrchideeColorPalette {
            // Motif tessellé caractéristique
            val lightBase = OrchideeColorHelper.rgb(
                220 + rng.nextInt(35),  // 220-255
                220 + rng.nextInt(35),  // 220-255
                240 + rng.nextInt(15)   // 240-255
            )
            
            val checkeredPurple = OrchideeColorHelper.rgb(
                75 + rng.nextInt(50),   // 75-125
                rng.nextInt(50),        // 0-50
                100 + rng.nextInt(50)   // 100-150
            )
            
            return OrchideeColorPalette(
                primary = lightBase,
                secondary = checkeredPurple,
                accent = generateDarkerShade(checkeredPurple, 0.2f),
                throat = generateYellowCenter(rng),
                veining = checkeredPurple,
                spotColor = generateHarmoniousAccent(checkeredPurple, rng),
                gradientStops = createTessellatedGradient(lightBase, checkeredPurple, rng)
            )
        }
        
        private fun createVandaOrange(rng: Random): OrchideeColorPalette {
            // Orange flamboyant
            val baseOrange = OrchideeColorHelper.rgb(
                255,
                130 + rng.nextInt(30),  // 130-160
                rng.nextInt(40)         // 0-40
            )
            
            val redOrange = OrchideeColorHelper.rgb(255, 69, 0)
            val yellowBase = OrchideeColorHelper.rgb(255, 255, 224)
            
            return OrchideeColorPalette(
                primary = yellowBase,
                secondary = baseOrange,
                accent = redOrange,
                throat = OrchideeColorHelper.rgb(255, 215, 0),
                veining = generateDarkerShade(redOrange, 0.3f),
                spotColor = redOrange,
                gradientStops = generateFireGradient(yellowBase, baseOrange, redOrange, rng)
            )
        }
        
        private fun createVandaFuchsia(rng: Random): OrchideeColorPalette {
            // Fuchsia vibrant
            val baseFuchsia = OrchideeColorHelper.rgb(
                255,
                10 + rng.nextInt(30),   // 10-40
                130 + rng.nextInt(40)   // 130-170
            )
            
            val deepFuchsia = OrchideeColorHelper.rgb(199, 21, 133)
            val pinkBase = OrchideeColorHelper.rgb(255, 182, 193)
            
            return OrchideeColorPalette(
                primary = pinkBase,
                secondary = baseFuchsia,
                accent = deepFuchsia,
                throat = OrchideeColorHelper.rgb(255, 255, 255),
                veining = generateDarkerShade(deepFuchsia, 0.2f),
                spotColor = baseFuchsia,
                gradientStops = generateVibrantGradient(pinkBase, baseFuchsia, deepFuchsia, rng)
            )
        }
        
        private fun createVandaRainbow(rng: Random): OrchideeColorPalette {
            // Arc-en-ciel exotique
            val colors = listOf(
                OrchideeColorHelper.rgb(255, 99, 71),   // Tomate
                OrchideeColorHelper.rgb(255, 215, 0),   // Or
                OrchideeColorHelper.rgb(124, 252, 0),   // Vert lime
                OrchideeColorHelper.rgb(65, 105, 225),  // Bleu royal
                OrchideeColorHelper.rgb(186, 85, 211)   // Violet moyen
            )
            
            val primaryColor = colors[rng.nextInt(colors.size)]
            val secondaryColor = colors[(colors.indexOf(primaryColor) + 1) % colors.size]
            val accentColor = colors[(colors.indexOf(primaryColor) + 2) % colors.size]
            
            return OrchideeColorPalette(
                primary = primaryColor,
                secondary = secondaryColor,
                accent = accentColor,
                throat = OrchideeColorHelper.rgb(255, 255, 255),
                veining = generateDarkerShade(primaryColor, 0.5f),
                spotColor = colors[rng.nextInt(colors.size)],
                gradientStops = generateRainbowGradient(colors, rng)
            )
        }
        
        // ==================== FONCTIONS GÉNÉRATRICES DE COULEURS ====================
        
        /**
         * Génère un centre jaune réaliste
         */
        fun generateYellowCenter(rng: Random = Random): Int {
            return OrchideeColorHelper.rgb(
                250 + rng.nextInt(5),   // 250-255
                200 + rng.nextInt(40),  // 200-240
                80 + rng.nextInt(60)    // 80-140
            )
        }
        
        /**
         * Génère un centre orange
         */
        fun generateOrangeCenter(rng: Random = Random): Int {
            return OrchideeColorHelper.rgb(
                255,
                130 + rng.nextInt(40),  // 130-170
                rng.nextInt(50)         // 0-50
            )
        }
        
        /**
         * Génère une couleur d'accent harmonieuse
         */
        fun generateHarmoniousAccent(baseColor: Int, rng: Random = Random): Int {
            val harmonies = ColorHarmony.values()
            val harmony = harmonies[rng.nextInt(harmonies.size)]
            return OrchideeColorHelper.generateHarmoniousColor(baseColor, harmony)
        }
        
        /**
         * Génère une couleur complémentaire
         */
        fun generateComplementaryColor(baseColor: Int): Int {
            return OrchideeColorHelper.generateHarmoniousColor(baseColor, ColorHarmony.COMPLEMENTARY)
        }
        
        /**
         * Génère une teinte plus sombre
         */
        fun generateDarkerShade(color: Int, factor: Float): Int {
            val clampedFactor = (1f - factor.coerceIn(0f, 1f))
            
            val r = (android.graphics.Color.red(color) * clampedFactor).toInt().coerceIn(0, 255)
            val g = (android.graphics.Color.green(color) * clampedFactor).toInt().coerceIn(0, 255)
            val b = (android.graphics.Color.blue(color) * clampedFactor).toInt().coerceIn(0, 255)
            
            return OrchideeColorHelper.rgb(r, g, b)
        }
        
        /**
         * Génère une teinte plus claire
         */
        fun generateLighterShade(color: Int, factor: Float): Int {
            val clampedFactor = factor.coerceIn(0f, 1f)
            
            val r = android.graphics.Color.red(color)
            val g = android.graphics.Color.green(color)
            val b = android.graphics.Color.blue(color)
            
            val newR = (r + (255 - r) * clampedFactor).toInt().coerceIn(0, 255)
            val newG = (g + (255 - g) * clampedFactor).toInt().coerceIn(0, 255)
            val newB = (b + (255 - b) * clampedFactor).toInt().coerceIn(0, 255)
            
            return OrchideeColorHelper.rgb(newR, newG, newB)
        }
        
        /**
         * Génère une couleur exotique rare
         */
        fun generateExoticAccent(rng: Random = Random): Int {
            val exoticColors = listOf(
                OrchideeColorHelper.rgb(50, 205, 50),   // Vert lime
                OrchideeColorHelper.rgb(255, 20, 147),  // Rose deep
                OrchideeColorHelper.rgb(0, 191, 255),   // Bleu ciel deep
                OrchideeColorHelper.rgb(148, 0, 211),   // Violet foncé
                OrchideeColorHelper.rgb(255, 140, 0),   // Orange foncé
                OrchideeColorHelper.rgb(220, 20, 60)    // Crimson
            )
            
            return exoticColors[rng.nextInt(exoticColors.size)]
        }
        
        /**
         * Génère un centre contrastant
         */
        fun generateContrastingCenter(baseColor: Int, rng: Random = Random): Int {
            val brightness = getBrightness(baseColor)
            
            return if (brightness > 0.5f) {
                // Base claire -> centre sombre
                val darkColors = listOf(
                    OrchideeColorHelper.rgb(139, 69, 19),   // Brun
                    OrchideeColorHelper.rgb(75, 0, 130),    // Indigo
                    OrchideeColorHelper.rgb(25, 25, 112)    // Bleu nuit
                )
                darkColors[rng.nextInt(darkColors.size)]
            } else {
                // Base sombre -> centre clair
                val lightColors = listOf(
                    OrchideeColorHelper.rgb(255, 255, 0),   // Jaune
                    OrchideeColorHelper.rgb(255, 255, 255), // Blanc
                    OrchideeColorHelper.rgb(255, 215, 0)    // Or
                )
                lightColors[rng.nextInt(lightColors.size)]
            }
        }
        
        // ==================== GÉNÉRATEURS DE DÉGRADÉS ====================
        
        /**
         * Génère un dégradé lisse à 3 couleurs
         */
        fun generateSmoothGradient(color1: Int, color2: Int, color3: Int, rng: Random = Random): List<Int> {
            val steps = 5 + rng.nextInt(3) // 5-7 étapes
            val gradient = mutableListOf<Int>()
            
            // Première moitié
            for (i in 0 until steps / 2) {
                val ratio = i.toFloat() / (steps / 2 - 1)
                gradient.add(OrchideeColorHelper.blendColors(color1, color2, ratio))
            }
            
            // Deuxième moitié
            for (i in 0 until steps - steps / 2) {
                val ratio = i.toFloat() / (steps - steps / 2 - 1)
                gradient.add(OrchideeColorHelper.blendColors(color2, color3, ratio))
            }
            
            return gradient
        }
        
        /**
         * Génère un dégradé dramatique avec contraste fort
         */
        fun generateDramaticGradient(light: Int, medium: Int, dark: Int, rng: Random = Random): List<Int> {
            return listOf(
                light,
                OrchideeColorHelper.blendColors(light, medium, 0.3f),
                medium,
                OrchideeColorHelper.blendColors(medium, dark, 0.7f),
                dark,
                generateExoticAccent(rng) // Accent surprise
            )
        }
        
        /**
         * Génère un dégradé pour motifs rayés
         */
        fun createStripedGradient(base: Int, stripe: Int, rng: Random = Random): List<Int> {
            val gradient = mutableListOf<Int>()
            val steps = 6 + rng.nextInt(4) // 6-9 étapes
            
            for (i in 0 until steps) {
                if (i % 2 == 0) {
                    gradient.add(base)
                } else {
                    val variation = 0.8f + rng.nextFloat() * 0.4f // 0.8-1.2
                    gradient.add(adjustColorBrightness(stripe, variation))
                }
            }
            
            return gradient
        }
        
        /**
         * Génère un dégradé tessellé
         */
        fun createTessellatedGradient(light: Int, dark: Int, rng: Random = Random): List<Int> {
            val gradient = mutableListOf<Int>()
            
            // Pattern tessellé : alternance avec variations subtiles
            for (i in 0 until 8) {
                when (i % 4) {
                    0, 3 -> gradient.add(light)
                    1, 2 -> gradient.add(OrchideeColorHelper.blendColors(light, dark, 0.6f + rng.nextFloat() * 0.3f))
                }
            }
            
            return gradient
        }
        
        /**
         * Génère un dégradé chaud (orange/rouge)
         */
        fun generateWarmGradient(yellow: Int, orange: Int, red: Int, rng: Random = Random): List<Int> {
            return listOf(
                yellow,
                OrchideeColorHelper.blendColors(yellow, orange, 0.4f),
                orange,
                OrchideeColorHelper.blendColors(orange, red, 0.3f),
                OrchideeColorHelper.blendColors(orange, red, 0.7f),
                red,
                generateDarkerShade(red, 0.3f)
            )
        }
        
        /**
         * Génère un dégradé froid (bleu)
         */
        fun generateCoolGradient(white: Int, blue: Int, darkBlue: Int, rng: Random = Random): List<Int> {
            return listOf(
                white,
                OrchideeColorHelper.blendColors(white, blue, 0.2f),
                OrchideeColorHelper.blendColors(white, blue, 0.5f),
                blue,
                OrchideeColorHelper.blendColors(blue, darkBlue, 0.4f),
                darkBlue,
                generateDarkerShade(darkBlue, 0.4f)
            )
        }
        
        /**
         * Génère un dégradé de feu
         */
        fun generateFireGradient(yellow: Int, orange: Int, red: Int, rng: Random = Random): List<Int> {
            return listOf(
                yellow,
                OrchideeColorHelper.blendColors(yellow, orange, 0.3f),
                orange,
                OrchideeColorHelper.blendColors(orange, red, 0.5f),
                red,
                OrchideeColorHelper.rgb(139, 0, 0), // Rouge foncé
                OrchideeColorHelper.rgb(255, 215, 0) // Retour à l'or
            )
        }
        
        /**
         * Génère un dégradé vibrant
         */
        fun generateVibrantGradient(light: Int, medium: Int, dark: Int, rng: Random = Random): List<Int> {
            val accent = generateExoticAccent(rng)
            
            return listOf(
                light,
                OrchideeColorHelper.blendColors(light, accent, 0.2f),
                medium,
                OrchideeColorHelper.blendColors(medium, dark, 0.6f),
                dark,
                accent
            )
        }
        
        /**
         * Génère un dégradé exotique
         */
        fun generateExoticGradient(base: Int, accent: Int, rng: Random = Random): List<Int> {
            val mystery = generateExoticAccent(rng)
            
            return listOf(
                base,
                OrchideeColorHelper.blendColors(base, mystery, 0.3f),
                mystery,
                OrchideeColorHelper.blendColors(mystery, accent, 0.5f),
                accent,
                OrchideeColorHelper.blendColors(accent, base, 0.7f)
            )
        }
        
        /**
         * Génère un dégradé riche pour Cattleya
         */
        fun generateRichGradient(purple: Int, darkPurple: Int, gold: Int, rng: Random = Random): List<Int> {
            return listOf(
                gold,
                OrchideeColorHelper.blendColors(gold, purple, 0.2f),
                purple,
                OrchideeColorHelper.blendColors(purple, darkPurple, 0.4f),
                darkPurple,
                OrchideeColorHelper.blendColors(darkPurple, gold, 0.1f),
                generateDarkerShade(darkPurple, 0.3f)
            )
        }
        
        /**
         * Génère un dégradé arc-en-ciel
         */
        fun generateRainbowGradient(colors: List<Int>, rng: Random = Random): List<Int> {
            val gradient = mutableListOf<Int>()
            
            for (i in colors.indices) {
                gradient.add(colors[i])
                if (i < colors.size - 1) {
                    // Ajouter une couleur intermédiaire
                    gradient.add(OrchideeColorHelper.blendColors(colors[i], colors[i + 1], 0.5f))
                }
            }
            
            return gradient
        }
        
        // ==================== FONCTIONS UTILITAIRES ====================
        
        /**
         * Calcule la luminosité d'une couleur
         */
        private fun getBrightness(color: Int): Float {
            val r = android.graphics.Color.red(color) / 255f
            val g = android.graphics.Color.green(color) / 255f
            val b = android.graphics.Color.blue(color) / 255f
            
            return (0.299f * r + 0.587f * g + 0.114f * b)
        }
        
        /**
         * Ajuste la luminosité d'une couleur
         */
        private fun adjustColorBrightness(color: Int, factor: Float): Int {
            val r = (android.graphics.Color.red(color) * factor).toInt().coerceIn(0, 255)
            val g = (android.graphics.Color.green(color) * factor).toInt().coerceIn(0, 255)
            val b = (android.graphics.Color.blue(color) * factor).toInt().coerceIn(0, 255)
            
            return OrchideeColorHelper.rgb(r, g, b)
        }
        
        /**
         * Génère une variation réaliste d'une couleur de base
         */
        fun generateRealisticVariation(baseColor: Int, maxVariation: Int = 30, rng: Random = Random): Int {
            val r = android.graphics.Color.red(baseColor)
            val g = android.graphics.Color.green(baseColor)
            val b = android.graphics.Color.blue(baseColor)
            
            val newR = (r + rng.nextInt(maxVariation * 2) - maxVariation).coerceIn(0, 255)
            val newG = (g + rng.nextInt(maxVariation * 2) - maxVariation).coerceIn(0, 255)
            val newB = (b + rng.nextInt(maxVariation * 2) - maxVariation).coerceIn(0, 255)
            
            return OrchideeColorHelper.rgb(newR, newG, newB)
        }
        
        /**
         * Génère des couleurs pour les autres espèces (implémentations simplifiées)
         */
        fun generateOncidiumColors(rng: Random = Random): OrchideeColorPalette {
            // Dominante jaune avec motifs bruns
            val baseYellow = OrchideeColorHelper.rgb(255, 215, 0)
            val brownSpots = OrchideeColorHelper.rgb(139, 69, 19)
            
            return OrchideeColorPalette(
                primary = baseYellow,
                secondary = OrchideeColorHelper.rgb(255, 255, 0),
                accent = brownSpots,
                throat = OrchideeColorHelper.rgb(255, 140, 0),
                veining = brownSpots,
                spotColor = OrchideeColorHelper.rgb(160, 82, 45),
                gradientStops = generateWarmGradient(
                    OrchideeColorHelper.rgb(255, 255, 0),
                    baseYellow,
                    brownSpots,
                    rng
                )
            )
        }
        
        fun generateDendrobiumColors(rng: Random = Random): OrchideeColorPalette {
            // Blancs, roses et violets délicats
            val baseWhite = OrchideeColorHelper.rgb(255, 255, 255)
            val lightPink = OrchideeColorHelper.rgb(255, 182, 193)
            
            return OrchideeColorPalette(
                primary = baseWhite,
                secondary = lightPink,
                accent = OrchideeColorHelper.rgb(186, 85, 211),
                throat = generateYellowCenter(rng),
                veining = OrchideeColorHelper.rgb(219, 112, 147),
                spotColor = lightPink,
                gradientStops = generateSmoothGradient(baseWhite, lightPink, 
                    OrchideeColorHelper.rgb(147, 112, 219), rng)
            )
        }
        
        fun generateCymbidiumColors(rng: Random = Random): OrchideeColorPalette {
            // Crèmes avec spots burgundy
            val cream = OrchideeColorHelper.rgb(255, 253, 208)
            val burgundy = OrchideeColorHelper.rgb(128, 0, 32)
            
            return OrchideeColorPalette(
                primary = cream,
                secondary = OrchideeColorHelper.rgb(255, 215, 0),
                accent = burgundy,
                throat = burgundy,
                veining = OrchideeColorHelper.rgb(139, 69, 19),
                spotColor = burgundy,
                gradientStops = generateRichGradient(cream, 
                    OrchideeColorHelper.rgb(255, 215, 0), burgundy, rng)
            )
        }
    }
}
