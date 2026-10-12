package com.example.vegetablerun.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import com.example.vegetablerun.model.CharacterId
import com.example.vegetablerun.model.CoinItem
import com.example.vegetablerun.model.EnvironmentTheme
import com.example.vegetablerun.model.Obstacle
import com.example.vegetablerun.model.ObstacleType
import com.example.vegetablerun.model.PowerUpItem
import com.example.vegetablerun.model.PowerUpType
import com.example.vegetablerun.model.RoadDecoration
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

object GameRenderer {

    fun drawGame(
        scope: DrawScope,
        snapshot: GameSnapshot,
        animationProgress: Float
    ) {
        with(scope) {
            val width = size.width
            val height = size.height

            val horizonY = height * 0.38f
            val vanishingPointX = width * 0.5f

            // 1. Sky & Horizon Gradient
            val theme = snapshot.currentTheme
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(theme.skyTopColor, theme.skyBottomColor),
                    startY = 0f,
                    endY = horizonY
                ),
                topLeft = Offset.Zero,
                size = Size(width, horizonY)
            )

            // Sun / Distant hills
            drawCircle(
                color = Color(0xFFFFD54F).copy(alpha = 0.85f),
                radius = width * 0.12f,
                center = Offset(width * 0.78f, horizonY * 0.55f)
            )

            // 2. Ground Terrain
            drawRect(
                color = theme.groundColor,
                topLeft = Offset(0f, horizonY),
                size = Size(width, height - horizonY)
            )

            // Ground perspective stripes (scrolling effect for speed sensation)
            val groundSegments = 16
            for (i in 0 until groundSegments) {
                val z = (i * 5f - (snapshot.distanceMeters % 5f)).coerceAtLeast(0.1f)
                val y1 = projectY(z, height, horizonY)
                val y2 = projectY(z + 2.5f, height, horizonY)
                if (y1 > horizonY && y2 > horizonY) {
                    drawRect(
                        color = Color.Black.copy(alpha = 0.04f),
                        topLeft = Offset(0f, minOf(y1, y2)),
                        size = Size(width, abs(y2 - y1))
                    )
                }
            }

            // 3. 3-Lane Road Geometry
            val bottomRoadWidth = width * 0.88f
            val topRoadWidth = width * 0.12f

            val roadPath = Path().apply {
                moveTo(vanishingPointX - topRoadWidth * 0.5f, horizonY)
                lineTo(vanishingPointX + topRoadWidth * 0.5f, horizonY)
                lineTo(width * 0.5f + bottomRoadWidth * 0.5f, height)
                lineTo(width * 0.5f - bottomRoadWidth * 0.5f, height)
                close()
            }
            drawPath(path = roadPath, color = theme.roadColor)

            // Road borders
            val leftBorderPath = Path().apply {
                moveTo(vanishingPointX - topRoadWidth * 0.5f - 4f, horizonY)
                lineTo(vanishingPointX - topRoadWidth * 0.5f, horizonY)
                lineTo(width * 0.5f - bottomRoadWidth * 0.5f, height)
                lineTo(width * 0.5f - bottomRoadWidth * 0.5f - 14f, height)
                close()
            }
            drawPath(path = leftBorderPath, color = theme.roadBorderColor)

            val rightBorderPath = Path().apply {
                moveTo(vanishingPointX + topRoadWidth * 0.5f, horizonY)
                lineTo(vanishingPointX + topRoadWidth * 0.5f + 4f, horizonY)
                lineTo(width * 0.5f + bottomRoadWidth * 0.5f + 14f, height)
                lineTo(width * 0.5f + bottomRoadWidth * 0.5f, height)
                close()
            }
            drawPath(path = rightBorderPath, color = theme.roadBorderColor)

            // Lane Divider Dashes
            val laneWidthBottom = bottomRoadWidth / 3f
            val laneWidthTop = topRoadWidth / 3f

            for (divider in listOf(-0.5f, 0.5f)) {
                val numDashes = 12
                for (d in 0 until numDashes) {
                    val z = (d * 6f - (snapshot.distanceMeters % 6f)).coerceAtLeast(0.5f)
                    val y = projectY(z, height, horizonY)
                    val scale = getScale(z)
                    val currentRoadW = topRoadWidth + (bottomRoadWidth - topRoadWidth) * (1f - scale)
                    val x = width * 0.5f + divider * (currentRoadW / 3f)

                    if (y > horizonY && y < height) {
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.7f),
                            topLeft = Offset(x - 2f * scale, y),
                            size = Size(4f * scale, 16f * scale)
                        )
                    }
                }
            }

            // 4. Roadside Scenery / Props
            drawDecorations(this, snapshot.decorations, snapshot.distanceMeters.toFloat(), width, height, horizonY, theme)

            // 5. Coins
            for (coin in snapshot.coins) {
                val relZ = coin.distanceZ - snapshot.distanceMeters
                if (relZ in 0.5f..70f) {
                    val scale = getScale(relZ)
                    val y = projectY(relZ, height, horizonY) - (coin.heightY * 60f * scale)
                    val x = projectX(coin.lane.toFloat(), relZ, width, bottomRoadWidth, topRoadWidth)

                    drawCoin(this, x, y, scale, animationProgress)
                }
            }

            // 6. Power-Ups
            for (pu in snapshot.powerUps) {
                val relZ = pu.distanceZ - snapshot.distanceMeters
                if (relZ in 0.5f..70f) {
                    val scale = getScale(relZ)
                    val y = projectY(relZ, height, horizonY) - (20f * scale)
                    val x = projectX(pu.lane.toFloat(), relZ, width, bottomRoadWidth, topRoadWidth)

                    drawPowerUpItem(this, x, y, scale, pu.type, animationProgress)
                }
            }

            // 7. Obstacles
            // Sort back to front so near obstacles draw over distant ones
            val sortedObstacles = snapshot.obstacles
                .filter { it.distanceZ - snapshot.distanceMeters in 0.2f..75f }
                .sortedByDescending { it.distanceZ }

            for (obs in sortedObstacles) {
                val relZ = obs.distanceZ - snapshot.distanceMeters
                val scale = getScale(relZ)
                val y = projectY(relZ, height, horizonY)
                val x = projectX(obs.lane.toFloat(), relZ, width, bottomRoadWidth, topRoadWidth)

                drawObstacle(this, x, y, scale, obs.type)
            }

            // 8. Player Character (Aloo, Baingan, Tamatar in Kurta-Pajama)
            val playerZ = 0f
            val playerScale = 1.0f
            val playerGroundY = height * 0.88f
            val playerX = projectX(snapshot.playerLane, playerZ, width, bottomRoadWidth, topRoadWidth)
            val jumpOffsetY = snapshot.playerY * 110f
            val playerActualY = playerGroundY - jumpOffsetY

            // Draw player ground shadow
            val shadowWidth = (64f * (1f - snapshot.playerY * 0.35f)).coerceAtLeast(20f)
            val shadowHeight = (18f * (1f - snapshot.playerY * 0.4f)).coerceAtLeast(8f)
            drawOval(
                color = Color.Black.copy(alpha = 0.28f),
                topLeft = Offset(playerX - shadowWidth * 0.5f, playerGroundY - shadowHeight * 0.5f),
                size = Size(shadowWidth, shadowHeight)
            )

            // Draw character
            drawVegetablePlayer(
                scope = this,
                character = snapshot.character,
                centerX = playerX,
                bottomY = playerActualY,
                isSliding = snapshot.isSliding,
                hasShield = snapshot.hasShield,
                hasMagnet = snapshot.magnetRemaining > 0f,
                runDistance = snapshot.distanceMeters.toFloat() + animationProgress * 2f
            )
        }
    }

    // Perspective projection helpers
    private fun getScale(relZ: Float): Float {
        return (1.0f / (1.0f + relZ * 0.055f)).coerceIn(0.02f, 1.0f)
    }

    private fun projectY(relZ: Float, screenHeight: Float, horizonY: Float): Float {
        val scale = getScale(relZ)
        return horizonY + (screenHeight - horizonY) * scale
    }

    private fun projectX(
        lane: Float,
        relZ: Float,
        screenWidth: Float,
        bottomRoadW: Float,
        topRoadW: Float
    ): Float {
        val scale = getScale(relZ)
        val currentRoadW = topRoadW + (bottomRoadW - topRoadW) * scale
        val laneSpacing = currentRoadW / 3f
        return screenWidth * 0.5f + lane * laneSpacing
    }

    // Roadside decorations
    private fun drawDecorations(
        scope: DrawScope,
        decorations: List<RoadDecoration>,
        playerDist: Float,
        width: Float,
        height: Float,
        horizonY: Float,
        theme: EnvironmentTheme
    ) {
        with(scope) {
            val bottomRoadWidth = width * 0.88f
            val topRoadWidth = width * 0.12f

            for (dec in decorations) {
                val relZ = dec.distanceZ - playerDist
                if (relZ in 1f..80f) {
                    val scale = getScale(relZ)
                    val y = projectY(relZ, height, horizonY)
                    val roadW = topRoadWidth + (bottomRoadWidth - topRoadWidth) * scale
                    val sideOffset = (roadW * 0.5f) + (35f * scale)
                    val x = if (dec.isLeft) (width * 0.5f) - sideOffset else (width * 0.5f) + sideOffset

                    when (theme) {
                        EnvironmentTheme.VILLAGE -> {
                            if (dec.typeIndex % 2 == 0) {
                                // Mud Hut with Clay roof
                                val hutW = 55f * scale
                                val hutH = 45f * scale
                                drawRect(
                                    color = Color(0xFFD7CCC8), // Clay wall
                                    topLeft = Offset(x - hutW * 0.5f, y - hutH),
                                    size = Size(hutW, hutH)
                                )
                                // Red clay triangular roof
                                val roofPath = Path().apply {
                                    moveTo(x - hutW * 0.6f, y - hutH)
                                    lineTo(x, y - hutH - 24f * scale)
                                    lineTo(x + hutW * 0.6f, y - hutH)
                                    close()
                                }
                                drawPath(roofPath, Color(0xFFBF360C))
                            } else {
                                // Banyan / Neem tree
                                val trunkW = 12f * scale
                                val trunkH = 50f * scale
                                drawRect(
                                    color = Color(0xFF5D4037),
                                    topLeft = Offset(x - trunkW * 0.5f, y - trunkH),
                                    size = Size(trunkW, trunkH)
                                )
                                drawCircle(
                                    color = Color(0xFF2E7D32),
                                    radius = 28f * scale,
                                    center = Offset(x, y - trunkH - 12f * scale)
                                )
                            }
                        }
                        EnvironmentTheme.FARMS -> {
                            // Sunflower / Sarson stalks & Scarecrow
                            drawCircle(
                                color = Color(0xFFFFB300),
                                radius = 22f * scale,
                                center = Offset(x, y - 35f * scale)
                            )
                            drawCircle(
                                color = Color(0xFF4E342E),
                                radius = 10f * scale,
                                center = Offset(x, y - 35f * scale)
                            )
                        }
                        EnvironmentTheme.BRIDGE -> {
                            // Stone pillar / wooden bridge fence
                            val postH = 40f * scale
                            drawRect(
                                color = Color(0xFF4E342E),
                                topLeft = Offset(x - 6f * scale, y - postH),
                                size = Size(12f * scale, postH)
                            )
                        }
                        EnvironmentTheme.MARKET -> {
                            // Colorful Market Canopy Stall
                            val stallW = 60f * scale
                            val stallH = 50f * scale
                            drawRect(
                                color = Color(0xFF8D6E63),
                                topLeft = Offset(x - stallW * 0.5f, y - stallH * 0.6f),
                                size = Size(stallW, stallH * 0.6f)
                            )
                            // Striped awning
                            drawRoundRect(
                                color = if (dec.typeIndex % 2 == 0) Color(0xFFE91E63) else Color(0xFFFF9800),
                                topLeft = Offset(x - stallW * 0.55f, y - stallH),
                                size = Size(stallW * 1.1f, 18f * scale)
                            )
                        }
                    }
                }
            }
        }
    }

    // Coin rendering (Golden Indian rupee coin with 3D spin)
    private fun drawCoin(
        scope: DrawScope,
        x: Float,
        y: Float,
        scale: Float,
        anim: Float
    ) {
        with(scope) {
            val radius = 14f * scale
            val spin = abs(cos((anim * 6f) % (2f * PI.toFloat())))
            val coinWidth = (radius * 2f * spin).coerceAtLeast(3f)

            // Golden body
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFEA00), Color(0xFFFFB300), Color(0xFFFF8F00)),
                    center = Offset(x, y),
                    radius = radius
                ),
                topLeft = Offset(x - coinWidth * 0.5f, y - radius),
                size = Size(coinWidth, radius * 2f)
            )
            // Coin edge outline
            drawOval(
                color = Color(0xFFFF6F00),
                topLeft = Offset(x - coinWidth * 0.5f, y - radius),
                size = Size(coinWidth, radius * 2f),
                style = Stroke(width = 2f * scale)
            )
        }
    }

    // PowerUp item
    private fun drawPowerUpItem(
        scope: DrawScope,
        x: Float,
        y: Float,
        scale: Float,
        type: PowerUpType,
        anim: Float
    ) {
        with(scope) {
            val radius = 18f * scale
            val pulse = 1f + 0.12f * sin(anim * 8f)

            // Glowing halo
            drawCircle(
                color = type.color.copy(alpha = 0.35f),
                radius = radius * 1.4f * pulse,
                center = Offset(x, y)
            )
            // Sphere body
            drawCircle(
                color = type.color,
                radius = radius * pulse,
                center = Offset(x, y)
            )
            // Inner white core
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = radius * 0.45f * pulse,
                center = Offset(x - radius * 0.25f, y - radius * 0.25f)
            )
        }
    }

    // Obstacle rendering
    private fun drawObstacle(
        scope: DrawScope,
        x: Float,
        y: Float,
        scale: Float,
        type: ObstacleType
    ) {
        with(scope) {
            when (type) {
                ObstacleType.VEGETABLE_CRATE -> {
                    // Wooden Sabzi Crate (jump over)
                    val w = 56f * scale
                    val h = 38f * scale
                    drawRoundRect(
                        color = Color(0xFF8D6E63),
                        topLeft = Offset(x - w * 0.5f, y - h),
                        size = Size(w, h)
                    )
                    // Crate wood slats
                    drawLine(
                        color = Color(0xFF5D4037),
                        start = Offset(x - w * 0.5f, y - h * 0.5f),
                        end = Offset(x + w * 0.5f, y - h * 0.5f),
                        strokeWidth = 3f * scale
                    )
                    // Veggies sticking out
                    drawCircle(
                        color = Color(0xFF43A047),
                        radius = 8f * scale,
                        center = Offset(x - 12f * scale, y - h)
                    )
                    drawCircle(
                        color = Color(0xFFE53935),
                        radius = 7f * scale,
                        center = Offset(x + 10f * scale, y - h)
                    )
                }

                ObstacleType.ROLLING_PUMPKIN -> {
                    // Desi Kaddu (jump over)
                    val radius = 22f * scale
                    drawCircle(
                        color = Color(0xFFFF9800),
                        radius = radius,
                        center = Offset(x, y - radius)
                    )
                    // Ridge lines
                    drawOval(
                        color = Color(0xFFE65100),
                        topLeft = Offset(x - radius * 0.45f, y - radius * 2f),
                        size = Size(radius * 0.9f, radius * 2f),
                        style = Stroke(width = 2f * scale)
                    )
                    // Green stem
                    drawRect(
                        color = Color(0xFF2E7D32),
                        topLeft = Offset(x - 3f * scale, y - radius * 2.2f),
                        size = Size(6f * scale, 8f * scale)
                    )
                }

                ObstacleType.MARKET_AWNING, ObstacleType.BAMBOO_BARRIER -> {
                    // High barrier (slide under)
                    val w = 68f * scale
                    val barH = 14f * scale
                    val postH = 68f * scale
                    val postW = 6f * scale

                    // Left & right bamboo posts
                    drawRect(
                        color = Color(0xFFC0CA33),
                        topLeft = Offset(x - w * 0.5f, y - postH),
                        size = Size(postW, postH)
                    )
                    drawRect(
                        color = Color(0xFFC0CA33),
                        topLeft = Offset(x + w * 0.5f - postW, y - postH),
                        size = Size(postW, postH)
                    )

                    // Top beam
                    drawRoundRect(
                        color = if (type == ObstacleType.MARKET_AWNING) Color(0xFFD81B60) else Color(0xFFAFB42B),
                        topLeft = Offset(x - w * 0.55f, y - postH),
                        size = Size(w * 1.1f, barH)
                    )

                    // Hanging cloth or marigold garland
                    val numFlowers = 5
                    for (f in 0 until numFlowers) {
                        val fx = x - w * 0.4f + f * (w * 0.8f / (numFlowers - 1))
                        drawCircle(
                            color = Color(0xFFFFB300),
                            radius = 4f * scale,
                            center = Offset(fx, y - postH + barH + 4f * scale)
                        )
                    }
                }

                ObstacleType.PARKED_TRACTOR -> {
                    // Village Tractor (tall/wide -> lane switch)
                    val tw = 72f * scale
                    val th = 70f * scale

                    // Big black rear wheels
                    drawCircle(
                        color = Color(0xFF212121),
                        radius = 20f * scale,
                        center = Offset(x - tw * 0.35f, y - 20f * scale)
                    )
                    drawCircle(
                        color = Color(0xFF212121),
                        radius = 20f * scale,
                        center = Offset(x + tw * 0.35f, y - 20f * scale)
                    )

                    // Tractor red bonnet/cabin
                    drawRoundRect(
                        color = Color(0xFFD32F2F),
                        topLeft = Offset(x - tw * 0.4f, y - th),
                        size = Size(tw * 0.8f, th * 0.65f)
                    )

                    // Yellow steering wheel / exhaust pipe
                    drawRect(
                        color = Color(0xFF424242),
                        topLeft = Offset(x + tw * 0.2f, y - th - 14f * scale),
                        size = Size(5f * scale, 16f * scale)
                    )
                }

                ObstacleType.DESI_COW -> {
                    // Gentle village cow (lane change)
                    val cw = 60f * scale
                    val ch = 48f * scale
                    drawRoundRect(
                        color = Color(0xFFFAFAFA),
                        topLeft = Offset(x - cw * 0.5f, y - ch),
                        size = Size(cw, ch)
                    )
                    // Brown cow patches
                    drawCircle(
                        color = Color(0xFF795548),
                        radius = 10f * scale,
                        center = Offset(x - 8f * scale, y - ch * 0.6f)
                    )
                }

                ObstacleType.MUD_PUDDLE -> {
                    // Slippery puddle (jump over)
                    val pw = 52f * scale
                    val ph = 16f * scale
                    drawOval(
                        color = Color(0xFF4E342E),
                        topLeft = Offset(x - pw * 0.5f, y - ph),
                        size = Size(pw, ph)
                    )
                    drawOval(
                        color = Color(0xFF8D6E63).copy(alpha = 0.6f),
                        topLeft = Offset(x - pw * 0.35f, y - ph * 0.85f),
                        size = Size(pw * 0.7f, ph * 0.7f)
                    )
                }
            }
        }
    }

    // Cute Indian Vegetable Runner in Kurta-Pajama!
    private fun drawVegetablePlayer(
        scope: DrawScope,
        character: CharacterId,
        centerX: Float,
        bottomY: Float,
        isSliding: Boolean,
        hasShield: Boolean,
        hasMagnet: Boolean,
        runDistance: Float
    ) {
        with(scope) {
            val runCycle = sin(runDistance * 10f)
            val legSwing = runCycle * 14f

            val baseWidth = 58f
            val baseHeight = 84f

            // Squash & stretch for sliding
            val charW = if (isSliding) baseWidth * 1.35f else baseWidth
            val charH = if (isSliding) baseHeight * 0.45f else baseHeight
            val charTopY = bottomY - charH

            // 1. Sliding Dust Particles
            if (isSliding) {
                drawCircle(
                    color = Color.White.copy(alpha = 0.6f),
                    radius = 8f,
                    center = Offset(centerX - 35f, bottomY - 6f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.4f),
                    radius = 12f,
                    center = Offset(centerX - 48f, bottomY - 10f)
                )
            }

            // 2. Legs in White Pajama Trousers + Mojari shoes (if not sliding)
            if (!isSliding) {
                val legH = 22f
                val legW = 10f

                // Left Leg
                val leftLegX = centerX - 14f
                val leftLegY = bottomY - legH + (legSwing * 0.3f)
                drawRoundRect(
                    color = Color(0xFFF5F5F5), // White pajama
                    topLeft = Offset(leftLegX - legW * 0.5f, leftLegY),
                    size = Size(legW, legH)
                )
                // Left shoe (brown mojari)
                drawRoundRect(
                    color = Color(0xFF5D4037),
                    topLeft = Offset(leftLegX - legW * 0.6f, bottomY - 6f),
                    size = Size(legW * 1.3f, 7f)
                )

                // Right Leg
                val rightLegX = centerX + 14f
                val rightLegY = bottomY - legH - (legSwing * 0.3f)
                drawRoundRect(
                    color = Color(0xFFF5F5F5), // White pajama
                    topLeft = Offset(rightLegX - legW * 0.5f, rightLegY),
                    size = Size(legW, legH)
                )
                // Right shoe
                drawRoundRect(
                    color = Color(0xFF5D4037),
                    topLeft = Offset(rightLegX - legW * 0.6f, bottomY - 6f),
                    size = Size(legW * 1.3f, 7f)
                )
            }

            // 3. Vegetable Body (Aloo, Baingan, Tamatar)
            when (character) {
                CharacterId.ALOO -> {
                    // Desi Aloo: Golden Oval Potato
                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFE9C496), Color(0xFFD4A373), Color(0xFFB07D49)),
                            center = Offset(centerX - 8f, charTopY + charH * 0.4f),
                            radius = charW * 0.6f
                        ),
                        topLeft = Offset(centerX - charW * 0.5f, charTopY),
                        size = Size(charW, charH)
                    )
                }

                CharacterId.BAINGAN -> {
                    // Babu Baingan: Slender Eggplant with Crown
                    val eggplantPath = Path().apply {
                        moveTo(centerX, charTopY + 10f)
                        cubicTo(
                            centerX - charW * 0.35f, charTopY + 20f,
                            centerX - charW * 0.55f, bottomY - charH * 0.3f,
                            centerX, bottomY
                        )
                        cubicTo(
                            centerX + charW * 0.55f, bottomY - charH * 0.3f,
                            centerX + charW * 0.35f, charTopY + 20f,
                            centerX, charTopY + 10f
                        )
                        close()
                    }
                    drawPath(
                        eggplantPath,
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF6A1B9A), Color(0xFF4A148C), Color(0xFF311B92)),
                            center = Offset(centerX - 6f, charTopY + charH * 0.5f),
                            radius = charW * 0.7f
                        )
                    )

                    // Green Calyx / Turban Crown on Top!
                    val crownPath = Path().apply {
                        moveTo(centerX, charTopY - 4f)
                        lineTo(centerX - 16f, charTopY + 12f)
                        lineTo(centerX - 6f, charTopY + 6f)
                        lineTo(centerX, charTopY + 14f)
                        lineTo(centerX + 6f, charTopY + 6f)
                        lineTo(centerX + 16f, charTopY + 12f)
                        close()
                    }
                    drawPath(crownPath, Color(0xFF00E676))
                }

                CharacterId.TAMATAR -> {
                    // Toofan Tamatar: Round Red Tomato
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFF5252), Color(0xFFE53935), Color(0xFFB71C1C)),
                            center = Offset(centerX - 10f, charTopY + charH * 0.4f),
                            radius = charW * 0.65f
                        ),
                        radius = charW * 0.52f,
                        center = Offset(centerX, charTopY + charH * 0.5f)
                    )

                    // Fresh Green Stem Hair
                    val stemPath = Path().apply {
                        moveTo(centerX, charTopY + 6f)
                        lineTo(centerX - 14f, charTopY)
                        lineTo(centerX - 4f, charTopY + 6f)
                        lineTo(centerX, charTopY - 8f)
                        lineTo(centerX + 4f, charTopY + 6f)
                        lineTo(centerX + 14f, charTopY)
                        close()
                    }
                    drawPath(stemPath, Color(0xFF43A047))
                }
            }

            // 4. Indian Kurta
            val kurtaH = if (isSliding) charH * 0.6f else charH * 0.52f
            val kurtaY = bottomY - kurtaH - (if (!isSliding) 14f else 0f)
            val kurtaW = charW * 0.88f

            drawRoundRect(
                color = character.kurtaColor,
                topLeft = Offset(centerX - kurtaW * 0.5f, kurtaY),
                size = Size(kurtaW, kurtaH)
            )

            // Kurta Golden Embroidered Collar & Buttons
            drawLine(
                color = character.accentColor,
                start = Offset(centerX, kurtaY),
                end = Offset(centerX, kurtaY + kurtaH * 0.7f),
                strokeWidth = 3f
            )
            // Golden buttons
            drawCircle(color = Color(0xFFFFD54F), radius = 2.5f, center = Offset(centerX, kurtaY + 8f))
            drawCircle(color = Color(0xFFFFD54F), radius = 2.5f, center = Offset(centerX, kurtaY + 18f))
            drawCircle(color = Color(0xFFFFD54F), radius = 2.5f, center = Offset(centerX, kurtaY + 28f))

            // 5. Cute Facial Expressions (Big cheerful eyes & smile)
            val eyeY = charTopY + (if (isSliding) charH * 0.35f else charH * 0.28f)
            val eyeRadius = if (isSliding) 3.5f else 5.5f

            // Left Eye
            drawCircle(color = Color.White, radius = eyeRadius, center = Offset(centerX - 10f, eyeY))
            drawCircle(color = Color(0xFF212121), radius = eyeRadius * 0.6f, center = Offset(centerX - 9f, eyeY))
            drawCircle(color = Color.White, radius = eyeRadius * 0.25f, center = Offset(centerX - 10f, eyeY - 1.5f))

            // Right Eye
            drawCircle(color = Color.White, radius = eyeRadius, center = Offset(centerX + 10f, eyeY))
            drawCircle(color = Color(0xFF212121), radius = eyeRadius * 0.6f, center = Offset(centerX + 11f, eyeY))
            drawCircle(color = Color.White, radius = eyeRadius * 0.25f, center = Offset(centerX + 10f, eyeY - 1.5f))

            // Rosy Cheeks
            drawCircle(color = Color(0xFFFF8A80).copy(alpha = 0.55f), radius = 4f, center = Offset(centerX - 16f, eyeY + 6f))
            drawCircle(color = Color(0xFFFF8A80).copy(alpha = 0.55f), radius = 4f, center = Offset(centerX + 16f, eyeY + 6f))

            // Happy Smile
            val smilePath = Path().apply {
                moveTo(centerX - 7f, eyeY + 8f)
                quadraticTo(centerX, eyeY + 14f, centerX + 7f, eyeY + 8f)
            }
            drawPath(smilePath, Color(0xFF212121), style = Stroke(width = 2.2f))

            // 6. Active Power-Up Auras
            if (hasShield) {
                // Cyan protective aura shield
                drawCircle(
                    color = Color(0xFF00E5FF).copy(alpha = 0.35f),
                    radius = charW * 0.75f,
                    center = Offset(centerX, bottomY - charH * 0.5f),
                    style = Stroke(width = 5f)
                )
            }

            if (hasMagnet) {
                // Green coin magnet pulses
                drawArc(
                    color = Color(0xFF00E676).copy(alpha = 0.6f),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = Offset(centerX - charW * 0.7f, charTopY - 16f),
                    size = Size(charW * 1.4f, 32f),
                    style = Stroke(width = 3.5f)
                )
            }
        }
    }
}
