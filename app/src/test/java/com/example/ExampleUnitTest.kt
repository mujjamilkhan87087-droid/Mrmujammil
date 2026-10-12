package com.example

import com.example.vegetablerun.model.CharacterId
import com.example.vegetablerun.model.ObstacleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testCharacterProfiles() {
        assertEquals("Desi Aloo", CharacterId.ALOO.displayName)
        assertEquals(0, CharacterId.ALOO.basePrice)

        assertEquals("Babu Baingan", CharacterId.BAINGAN.displayName)
        assertEquals(300, CharacterId.BAINGAN.basePrice)

        assertEquals("Toofan Tamatar", CharacterId.TAMATAR.displayName)
        assertEquals(600, CharacterId.TAMATAR.basePrice)
    }

    @Test
    fun testObstacleBehaviors() {
        // Crates & Pumpkins must allow jumping
        assertTrue(ObstacleType.VEGETABLE_CRATE.canJumpOver)
        assertFalse(ObstacleType.VEGETABLE_CRATE.canSlideUnder)

        // Awnings & Bamboo must allow sliding
        assertFalse(ObstacleType.MARKET_AWNING.canJumpOver)
        assertTrue(ObstacleType.MARKET_AWNING.canSlideUnder)

        // Tractors & Cows require lane shifting
        assertTrue(ObstacleType.PARKED_TRACTOR.requiresLaneChange)
        assertFalse(ObstacleType.PARKED_TRACTOR.canJumpOver)
        assertFalse(ObstacleType.PARKED_TRACTOR.canSlideUnder)
    }
}
