package com.example.vegetablerun.engine

import com.example.vegetablerun.audio.SoundManager
import com.example.vegetablerun.model.CharacterId
import com.example.vegetablerun.model.CoinItem
import com.example.vegetablerun.model.EnvironmentTheme
import com.example.vegetablerun.model.Obstacle
import com.example.vegetablerun.model.ObstacleType
import com.example.vegetablerun.model.PowerUpItem
import com.example.vegetablerun.model.PowerUpType
import com.example.vegetablerun.model.RoadDecoration
import com.example.vegetablerun.storage.GameStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.random.Random

enum class GameState {
    READY,
    RUNNING,
    PAUSED,
    GAME_OVER
}

data class GameSnapshot(
    val state: GameState,
    val character: CharacterId,
    val currentTheme: EnvironmentTheme,
    val playerLane: Float, // Interpolated -1.0f .. 1.0f
    val targetLane: Int,   // -1, 0, 1
    val playerY: Float,    // Jump height (0.0 = ground, up to ~1.2)
    val isSliding: Boolean,
    val distanceMeters: Int,
    val score: Int,
    val runCoins: Int,
    val speed: Float,
    val hasShield: Boolean,
    val magnetRemaining: Float,
    val multiplierRemaining: Float,
    val shieldRemaining: Float,
    val obstacles: List<Obstacle>,
    val coins: List<CoinItem>,
    val powerUps: List<PowerUpItem>,
    val decorations: List<RoadDecoration>,
    val canRevive: Boolean,
    val slideCount: Int
)

class GameEngine(
    private val storage: GameStorage,
    private val soundManager: SoundManager
) {
    private var state = GameState.READY
    private var activeCharacter = storage.selectedCharacter
    private var currentTheme = EnvironmentTheme.VILLAGE

    // Player position
    private var playerLane = 0f
    private var targetLane = 0
    private var playerY = 0f
    private var jumpVelocity = 0f
    private var isSliding = false
    private var slideTimer = 0f

    // Run metrics
    private var playerZ = 0f
    private var baseSpeed = 16f
    private var currentSpeed = 16f
    private var distanceMeters = 0
    private var score = 0
    private var runCoins = 0
    private var slideCount = 0
    private var canRevive = true
    private var invulnerableTimer = 0f

    // Power-up durations
    private var magnetTimer = 0f
    private var shieldTimer = 0f
    private var multiplierTimer = 0f

    // Spawners
    private var nextObstacleZ = 25f
    private var nextCoinZ = 15f
    private var nextPowerUpZ = 60f
    private var nextDecorationZ = 10f

    private var nextEntityId = 1L

    private val obstacles = mutableListOf<Obstacle>()
    private val coins = mutableListOf<CoinItem>()
    private val powerUps = mutableListOf<PowerUpItem>()
    private val decorations = mutableListOf<RoadDecoration>()

    private val _snapshot = MutableStateFlow(createSnapshot())
    val snapshot: StateFlow<GameSnapshot> = _snapshot.asStateFlow()

    fun startRun(character: CharacterId = storage.selectedCharacter) {
        activeCharacter = character
        state = GameState.RUNNING
        playerLane = 0f
        targetLane = 0
        playerY = 0f
        jumpVelocity = 0f
        isSliding = false
        slideTimer = 0f
        playerZ = 0f
        baseSpeed = 16f
        currentSpeed = baseSpeed
        distanceMeters = 0
        score = 0
        runCoins = 0
        slideCount = 0
        canRevive = true
        invulnerableTimer = 0f

        magnetTimer = 0f
        shieldTimer = 0f
        multiplierTimer = 0f

        obstacles.clear()
        coins.clear()
        powerUps.clear()
        decorations.clear()

        nextObstacleZ = 28f
        nextCoinZ = 16f
        nextPowerUpZ = 75f
        nextDecorationZ = 5f

        currentTheme = EnvironmentTheme.VILLAGE

        soundManager.startBackgroundMusic()
        updateSnapshot()
    }

    fun pauseGame() {
        if (state == GameState.RUNNING) {
            state = GameState.PAUSED
            updateSnapshot()
        }
    }

    fun resumeGame() {
        if (state == GameState.PAUSED) {
            state = GameState.RUNNING
            updateSnapshot()
        }
    }

    fun moveLeft() {
        if (state != GameState.RUNNING) return
        if (targetLane > -1) {
            targetLane -= 1
            soundManager.playLaneChange()
            updateSnapshot()
        }
    }

    fun moveRight() {
        if (state != GameState.RUNNING) return
        if (targetLane < 1) {
            targetLane += 1
            soundManager.playLaneChange()
            updateSnapshot()
        }
    }

    fun jump() {
        if (state != GameState.RUNNING) return
        if (playerY <= 0.05f && !isSliding) {
            jumpVelocity = 7.5f
            soundManager.playJump()
            updateSnapshot()
        }
    }

    fun slide() {
        if (state != GameState.RUNNING) return
        if (playerY > 0.05f) {
            // Fast-drop if in air
            jumpVelocity = -8f
        }
        isSliding = true
        slideTimer = 0.65f
        slideCount++
        soundManager.playSlide()
        updateSnapshot()
    }

    fun revive() {
        if (state == GameState.GAME_OVER && canRevive) {
            canRevive = false
            state = GameState.RUNNING
            // Clear immediate obstacles in front of player
            obstacles.removeAll { it.distanceZ in playerZ..(playerZ + 35f) }
            invulnerableTimer = 3.0f // 3 seconds grace period
            shieldTimer = 3.0f
            updateSnapshot()
        }
    }

    fun update(deltaSeconds: Float) {
        if (state != GameState.RUNNING) return
        val dt = deltaSeconds.coerceIn(0.005f, 0.05f)

        // Ramp speed gradually with distance
        val speedIncrement = (distanceMeters / 150f) * 0.8f
        currentSpeed = (baseSpeed + speedIncrement).coerceAtMost(32f)

        // Progress distance
        val forwardDelta = currentSpeed * dt
        playerZ += forwardDelta
        distanceMeters = (playerZ).toInt()

        // Theme shift every 400 meters
        val themeIndex = ((distanceMeters / 400) % EnvironmentTheme.entries.size)
        currentTheme = EnvironmentTheme.entries[themeIndex]

        // Smooth lane interpolation
        val laneDiff = targetLane.toFloat() - playerLane
        playerLane += laneDiff * (dt * 14f)

        // Jump physics (gravity)
        if (playerY > 0f || jumpVelocity != 0f) {
            playerY += jumpVelocity * dt
            jumpVelocity -= 18.0f * dt // Gravity
            if (playerY <= 0f) {
                playerY = 0f
                jumpVelocity = 0f
            }
        }

        // Slide timer
        if (isSliding) {
            slideTimer -= dt
            if (slideTimer <= 0f) {
                isSliding = false
            }
        }

        // Power-up timers
        if (magnetTimer > 0f) magnetTimer = (magnetTimer - dt).coerceAtLeast(0f)
        if (multiplierTimer > 0f) multiplierTimer = (multiplierTimer - dt).coerceAtLeast(0f)
        if (shieldTimer > 0f) shieldTimer = (shieldTimer - dt).coerceAtLeast(0f)
        if (invulnerableTimer > 0f) invulnerableTimer = (invulnerableTimer - dt).coerceAtLeast(0f)

        // Calculate score
        val characterMultiplier = if (activeCharacter == CharacterId.TAMATAR) 1.5f else 1.0f
        val activeMultiplier = if (multiplierTimer > 0f) 2.0f else 1.0f
        val currentMultiplier = characterMultiplier * activeMultiplier
        score = ((distanceMeters * 5f * currentMultiplier) + (runCoins * 25f)).toInt()

        // Spawners & Cleanup
        spawnDecorations()
        spawnObstacles()
        spawnCoins()
        spawnPowerUps()

        // Magnet attraction
        val magnetRange = if (activeCharacter == CharacterId.BAINGAN) 18f else 14f
        if (magnetTimer > 0f) {
            for (coin in coins) {
                if (!coin.isCollected && coin.distanceZ > playerZ && coin.distanceZ < playerZ + magnetRange) {
                    // Pull coin toward current player lane and position
                    val coinLaneDiff = playerLane - coin.lane.toFloat()
                    coin.distanceZ -= (currentSpeed * 0.4f * dt)
                    if (abs(coin.distanceZ - playerZ) < 1.8f) {
                        collectCoin(coin)
                    }
                }
            }
        }

        // Coin pickups
        for (coin in coins) {
            if (!coin.isCollected) {
                val zDist = coin.distanceZ - playerZ
                if (zDist in -0.5f..1.6f) {
                    val laneDist = abs(coin.lane.toFloat() - playerLane)
                    val yDist = abs(coin.heightY - playerY)
                    if (laneDist < 0.55f && yDist < 0.85f) {
                        collectCoin(coin)
                    }
                }
            }
        }

        // PowerUp pickups
        for (pu in powerUps) {
            if (!pu.isCollected) {
                val zDist = pu.distanceZ - playerZ
                if (zDist in -0.5f..1.8f) {
                    val laneDist = abs(pu.lane.toFloat() - playerLane)
                    if (laneDist < 0.6f && playerY < 1.0f) {
                        collectPowerUp(pu)
                    }
                }
            }
        }

        // Obstacle Collisions
        if (invulnerableTimer <= 0f) {
            for (obs in obstacles) {
                val zDist = obs.distanceZ - playerZ
                // Collision window
                if (zDist in -0.4f..1.4f) {
                    val laneDist = abs(obs.lane.toFloat() - playerLane)
                    if (laneDist < 0.62f) {
                        // In same lane! Check height
                        val type = obs.type
                        var avoided = false
                        if (type.canJumpOver && playerY > 0.42f) {
                            avoided = true // Jumped successfully!
                        } else if (type.canSlideUnder && isSliding) {
                            avoided = true // Slid successfully!
                        }

                        if (!avoided) {
                            handleCollision()
                            break
                        }
                    }
                }
            }
        }

        // Garbage collection of old passed items
        val despawnZ = playerZ - 10f
        obstacles.removeAll { it.distanceZ < despawnZ }
        coins.removeAll { it.distanceZ < despawnZ || it.isCollected }
        powerUps.removeAll { it.distanceZ < despawnZ || it.isCollected }
        decorations.removeAll { it.distanceZ < despawnZ }

        updateSnapshot()
    }

    private fun collectCoin(coin: CoinItem) {
        coin.isCollected = true
        runCoins++
        soundManager.playCoin()
    }

    private fun collectPowerUp(powerUp: PowerUpItem) {
        powerUp.isCollected = true
        soundManager.playPowerUp()
        when (powerUp.type) {
            PowerUpType.MAGNET -> {
                val bonus = storage.getUpgradeLevel("magnet") * 1.5f
                magnetTimer = powerUp.type.durationSeconds + bonus
            }
            PowerUpType.SHIELD -> {
                val bonus = storage.getUpgradeLevel("shield") * 1.5f
                shieldTimer = powerUp.type.durationSeconds + bonus
            }
            PowerUpType.MULTIPLIER -> {
                val bonus = storage.getUpgradeLevel("multiplier") * 1.5f
                multiplierTimer = powerUp.type.durationSeconds + bonus
            }
        }
    }

    private fun handleCollision() {
        if (shieldTimer > 0f) {
            // Shield absorbs the blow!
            shieldTimer = 0f
            invulnerableTimer = 1.8f
            soundManager.playCrash()
            return
        }

        // Game Over!
        state = GameState.GAME_OVER
        soundManager.playCrash()

        // Persist records
        storage.coins += runCoins
        storage.highScore = score
        storage.bestDistanceMeters = distanceMeters
        storage.recordRunStats(runCoins, slideCount, distanceMeters)

        updateSnapshot()
    }

    private fun spawnDecorations() {
        while (nextDecorationZ < playerZ + 90f) {
            val type = Random.nextInt(4)
            decorations.add(RoadDecoration(nextEntityId++, isLeft = true, distanceZ = nextDecorationZ, typeIndex = type))
            decorations.add(RoadDecoration(nextEntityId++, isLeft = false, distanceZ = nextDecorationZ, typeIndex = type))
            nextDecorationZ += 12f
        }
    }

    private fun spawnObstacles() {
        while (nextObstacleZ < playerZ + 80f) {
            val roll = Random.nextFloat()
            val lane = Random.nextInt(-1, 2)

            val type = when {
                roll < 0.35f -> ObstacleType.VEGETABLE_CRATE
                roll < 0.55f -> ObstacleType.ROLLING_PUMPKIN
                roll < 0.70f -> ObstacleType.MARKET_AWNING
                roll < 0.82f -> ObstacleType.BAMBOO_BARRIER
                roll < 0.92f -> ObstacleType.MUD_PUDDLE
                else -> ObstacleType.PARKED_TRACTOR
            }

            obstacles.add(Obstacle(nextEntityId++, type, lane, nextObstacleZ))

            // Occasionally add a second obstacle in an adjacent lane, ensuring at least 1 lane is free
            if (distanceMeters > 200 && Random.nextFloat() < 0.30f && type != ObstacleType.PARKED_TRACTOR) {
                val secondLane = when (lane) {
                    -1 -> 0
                    0 -> if (Random.nextBoolean()) -1 else 1
                    else -> 0
                }
                obstacles.add(Obstacle(nextEntityId++, ObstacleType.VEGETABLE_CRATE, secondLane, nextObstacleZ))
            }

            val spacing = (18f - (distanceMeters / 250f)).coerceAtLeast(10f)
            nextObstacleZ += spacing + Random.nextFloat() * 6f
        }
    }

    private fun spawnCoins() {
        while (nextCoinZ < playerZ + 75f) {
            val lane = Random.nextInt(-1, 2)
            val count = Random.nextInt(3, 7)
            val hasArc = Random.nextBoolean() && distanceMeters > 50

            for (i in 0 until count) {
                val z = nextCoinZ + i * 2.2f
                val h = if (hasArc) {
                    val progress = i.toFloat() / (count - 1)
                    (sin(progress * Math.PI).toFloat() * 1.1f).coerceAtLeast(0f)
                } else 0f
                coins.add(CoinItem(nextEntityId++, lane, z, h))
            }

            nextCoinZ += count * 2.2f + Random.nextFloat() * 12f + 8f
        }
    }

    private fun spawnPowerUps() {
        while (nextPowerUpZ < playerZ + 85f) {
            val lane = Random.nextInt(-1, 2)
            val type = PowerUpType.entries.random()
            powerUps.add(PowerUpItem(nextEntityId++, type, lane, nextPowerUpZ))
            nextPowerUpZ += 55f + Random.nextFloat() * 30f
        }
    }

    private fun updateSnapshot() {
        _snapshot.value = createSnapshot()
    }

    private fun createSnapshot(): GameSnapshot {
        return GameSnapshot(
            state = state,
            character = activeCharacter,
            currentTheme = currentTheme,
            playerLane = playerLane,
            targetLane = targetLane,
            playerY = playerY,
            isSliding = isSliding,
            distanceMeters = distanceMeters,
            score = score,
            runCoins = runCoins,
            speed = currentSpeed,
            hasShield = shieldTimer > 0f,
            magnetRemaining = magnetTimer,
            multiplierRemaining = multiplierTimer,
            shieldRemaining = shieldTimer,
            obstacles = obstacles.toList(),
            coins = coins.toList(),
            powerUps = powerUps.toList(),
            decorations = decorations.toList(),
            canRevive = canRevive,
            slideCount = slideCount
        )
    }

    private fun sin(rad: Double): Double = Math.sin(rad)
}
