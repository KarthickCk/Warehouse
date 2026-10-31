package org.warehouse.measure.data.config

import org.warehouse.measure.domain.model.SensorType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AppConfigTest {

    @Test
    fun `empty environment yields defaults`() {
        assertEquals(AppConfig.DEFAULT, AppConfig.fromEnvironment(emptyMap()))
    }

    @Test
    fun `defaults match the spec`() {
        val c = AppConfig.DEFAULT
        assertEquals(3344, c.ports[SensorType.TEMPERATURE])
        assertEquals(3355, c.ports[SensorType.HUMIDITY])
        assertEquals(35.0, c.thresholds[SensorType.TEMPERATURE])
        assertEquals(50.0, c.thresholds[SensorType.HUMIDITY])
    }

    @Test
    fun `one override is applied and the rest stay default`() {
        val c = AppConfig.fromEnvironment(mapOf("TEMPERATURE_THRESHOLD" to "40.5"))

        assertEquals(40.5, c.thresholds[SensorType.TEMPERATURE])
        assertEquals(50.0, c.thresholds[SensorType.HUMIDITY])
        assertEquals(AppConfig.DEFAULT.ports, c.ports)
    }

    @Test
    fun `warehouse ids are split and trimmed`() {
        val c = AppConfig.fromEnvironment(mapOf("WAREHOUSE_IDS" to " wh-1, wh-2 ,,"))
        assertEquals(listOf("wh-1", "wh-2"), c.warehouseIds)
    }

    @Test
    fun `port override is applied`() {
        val c = AppConfig.fromEnvironment(mapOf("HUMIDITY_PORT" to "4000"))
        assertEquals(4000, c.ports[SensorType.HUMIDITY])
        assertEquals(3344, c.ports[SensorType.TEMPERATURE])
    }

    @Test
    fun `bad number fails fast naming the variable`() {
        val e = assertFailsWith<IllegalArgumentException> {
            AppConfig.fromEnvironment(mapOf("TEMPERATURE_THRESHOLD" to "hot"))
        }
        assertTrue("TEMPERATURE_THRESHOLD" in e.message!!)
    }

    @Test
    fun `missing port for a sensor type is rejected`() {
        assertFailsWith<IllegalArgumentException> {
            AppConfig.DEFAULT.copy(ports = mapOf(SensorType.TEMPERATURE to 3344))
        }
    }
}
