package com.example.vegetablerun.model

import androidx.compose.ui.graphics.Color

enum class EnvironmentTheme(
    val title: String,
    val hindiTitle: String,
    val skyTopColor: Color,
    val skyBottomColor: Color,
    val groundColor: Color,
    val roadColor: Color,
    val roadBorderColor: Color,
    val sceneryItemName: String
) {
    VILLAGE(
        title = "Desi Gaon",
        hindiTitle = "देसी गाँव",
        skyTopColor = Color(0xFF64B5F6),
        skyBottomColor = Color(0xFFFFF9C4),
        groundColor = Color(0xFFC8E6C9), // Light green village plains
        roadColor = Color(0xFFD7CCC8),   // Mud-clay village track
        roadBorderColor = Color(0xFF8D6E63),
        sceneryItemName = "Hut & Banyan"
    ),
    FARMS(
        title = "Sarson Khet",
        hindiTitle = "सरसों का खेत",
        skyTopColor = Color(0xFF42A5F5),
        skyBottomColor = Color(0xFFFFE082),
        groundColor = Color(0xFFFFEE58), // Mustard yellow flower fields
        roadColor = Color(0xFFBCAAA4),   // Farm dirt road
        roadBorderColor = Color(0xFF6D4C41),
        sceneryItemName = "Tractor & Sarson"
    ),
    BRIDGE(
        title = "Nadi Pul",
        hindiTitle = "नदी पुल",
        skyTopColor = Color(0xFF29B6F6),
        skyBottomColor = Color(0xFF80DEEA),
        groundColor = Color(0xFF4DD0E1), // River water background
        roadColor = Color(0xFF8D6E63),   // Wooden plank bridge
        roadBorderColor = Color(0xFF4E342E),
        sceneryItemName = "River & Lotus"
    ),
    MARKET(
        title = "Sabzi Mandi",
        hindiTitle = "सब्ज़ी मंडी",
        skyTopColor = Color(0xFFFFB74D),
        skyBottomColor = Color(0xFFFFE0B2),
        groundColor = Color(0xFFFFCC80), // Busy market street
        roadColor = Color(0xFFE0E0E0),   // Cobblestone market lane
        roadBorderColor = Color(0xFFFF7043),
        sceneryItemName = "Stalls & Marigolds"
    )
}

enum class ObstacleType(
    val displayName: String,
    val canJumpOver: Boolean,
    val canSlideUnder: Boolean,
    val requiresLaneChange: Boolean,
    val color: Color
) {
    // Low obstacles: Jump over
    VEGETABLE_CRATE("Sabzi Crate", canJumpOver = true, canSlideUnder = false, requiresLaneChange = false, Color(0xFF8D6E63)),
    ROLLING_PUMPKIN("Desi Kaddu", canJumpOver = true, canSlideUnder = false, requiresLaneChange = false, Color(0xFFFF9800)),
    MUD_PUDDLE("Puddle", canJumpOver = true, canSlideUnder = false, requiresLaneChange = false, Color(0xFF5D4037)),

    // High obstacles: Slide under
    MARKET_AWNING("Mandi Awning", canJumpOver = false, canSlideUnder = true, requiresLaneChange = false, Color(0xFFE91E63)),
    BAMBOO_BARRIER("Bamboo Gate", canJumpOver = false, canSlideUnder = true, requiresLaneChange = false, Color(0xFFC0CA33)),

    // Tall/Wide obstacles: Switch lanes
    PARKED_TRACTOR("Gaon Tractor", canJumpOver = false, canSlideUnder = false, requiresLaneChange = true, Color(0xFFD32F2F)),
    DESI_COW("Gau Mata", canJumpOver = false, canSlideUnder = false, requiresLaneChange = true, Color(0xFFECEFF1))
}

data class Obstacle(
    val id: Long,
    val type: ObstacleType,
    val lane: Int, // -1: Left, 0: Center, 1: Right
    var distanceZ: Float // Distance ahead of player in game units
)

data class CoinItem(
    val id: Long,
    val lane: Int,
    var distanceZ: Float,
    val heightY: Float = 0f, // 0 = ground, 1 = in air (during jump arc)
    var isCollected: Boolean = false
)

enum class PowerUpType(
    val displayName: String,
    val hindiName: String,
    val iconName: String,
    val color: Color,
    val durationSeconds: Float
) {
    MAGNET("Nimbu-Mirchi Magnet", "नींबू-मिर्ची", "🧲", Color(0xFF00E676), 8f),
    SHIELD("Garam Masala Shield", "सुरक्षा ढाल", "🛡️", Color(0xFF00B0FF), 10f),
    MULTIPLIER("2X Ghee Boost", "2X घी पावर", "⚡", Color(0xFFFFD600), 10f)
}

data class PowerUpItem(
    val id: Long,
    val type: PowerUpType,
    val lane: Int,
    var distanceZ: Float,
    var isCollected: Boolean = false
)

data class RoadDecoration(
    val id: Long,
    val isLeft: Boolean, // Left or Right roadside
    var distanceZ: Float,
    val typeIndex: Int
)

data class Mission(
    val id: String,
    val title: String,
    val description: String,
    val target: Int,
    val current: Int,
    val rewardCoins: Int,
    val isClaimed: Boolean
)
