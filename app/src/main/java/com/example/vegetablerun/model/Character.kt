package com.example.vegetablerun.model

import androidx.compose.ui.graphics.Color

enum class CharacterId(
    val displayName: String,
    val hindiTitle: String,
    val description: String,
    val perk: String,
    val basePrice: Int,
    val primaryColor: Color,
    val kurtaColor: Color,
    val accentColor: Color
) {
    ALOO(
        displayName = "Desi Aloo",
        hindiTitle = "देसी आलू",
        description = "The humble, beloved potato. Always smiling, steady and reliable!",
        perk = "Balanced: Normal speed and jumping height.",
        basePrice = 0, // Free default
        primaryColor = Color(0xFFD4A373), // Warm Potato skin
        kurtaColor = Color(0xFFFFB703),   // Turmeric Yellow Kurta
        accentColor = Color(0xFFFB8500)   // Saffron trim
    ),
    BAINGAN(
        displayName = "Babu Baingan",
        hindiTitle = "बाबू बैंगन",
        description = "Royal and classy eggplant wearing a regal purple silk kurta.",
        perk = "Coin Magnet: +30% coin attraction range!",
        basePrice = 300,
        primaryColor = Color(0xFF4A154B), // Deep Brinjal Purple
        kurtaColor = Color(0xFF8338EC),   // Royal Purple Silk Kurta
        accentColor = Color(0xFF06D6A0)   // Green Crown Calyx
    ),
    TAMATAR(
        displayName = "Toofan Tamatar",
        hindiTitle = "तूफ़ान टमाटर",
        description = "Juicy, speedy red tomato ready to race through the village!",
        perk = "Score Booster: +50% bonus score multiplier!",
        basePrice = 600,
        primaryColor = Color(0xFFE63946), // Vibrant Tomato Red
        kurtaColor = Color(0xFFD90429),   // Crimson Festive Kurta
        accentColor = Color(0xFF2A9D8F)   // Fresh Green Stem
    );

    companion object {
        fun fromId(name: String?): CharacterId {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: ALOO
        }
    }
}
