package org.warehouse.measure.domain.rule

import org.warehouse.measure.domain.model.SensorType
import org.warehouse.measure.measurement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SimpleThresholdRuleTest {

    private val rule = SimpleThresholdRule(SensorType.TEMPERATURE, 35.0)

    @Test
    fun `below threshold is not an alarm`() {
        assertNull(rule.evaluate(measurement(SensorType.TEMPERATURE, 34.9)))
    }

    @Test
    fun `equal to threshold is not an alarm`() {
        assertNull(rule.evaluate(measurement(SensorType.TEMPERATURE, 35.0)))
    }

    @Test
    fun `above threshold is an alarm carrying the threshold`() {
        val alarm = assertNotNull(rule.evaluate(measurement(SensorType.TEMPERATURE, 35.1)))
        assertEquals(35.0, alarm.threshold)
        assertEquals(35.1, alarm.measurement.value)
    }

    @Test
    fun `other sensor types are ignored even when far above`() {
        assertNull(rule.evaluate(measurement(SensorType.HUMIDITY, 99.0)))
    }

    @Test
    fun `default threshold comes from the sensor type`() {
        val defaultRule = SimpleThresholdRule(SensorType.HUMIDITY)
        assertNull(defaultRule.evaluate(measurement(SensorType.HUMIDITY, 50.0)))
        assertNotNull(defaultRule.evaluate(measurement(SensorType.HUMIDITY, 50.5)))
    }

    @Test
    fun `forAllTypes builds one rule per type using the map or the default`() {
        val rules = SimpleThresholdRule.forAllTypes(mapOf(SensorType.TEMPERATURE to 40.0))

        assertEquals(SensorType.entries.size, rules.size)
        assertNull(rules.firstNotNullOfOrNull { it.evaluate(measurement(SensorType.TEMPERATURE, 39.0)) })
        assertNotNull(rules.firstNotNullOfOrNull { it.evaluate(measurement(SensorType.TEMPERATURE, 41.0)) })
        assertNull(rules.firstNotNullOfOrNull { it.evaluate(measurement(SensorType.HUMIDITY, 50.0)) })
        assertNotNull(rules.firstNotNullOfOrNull { it.evaluate(measurement(SensorType.HUMIDITY, 50.5)) })
    }
}
