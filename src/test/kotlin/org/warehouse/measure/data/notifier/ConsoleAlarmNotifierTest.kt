package org.warehouse.measure.data.notifier

import org.slf4j.helpers.NOPLogger
import org.warehouse.measure.domain.model.Alarm
import org.warehouse.measure.domain.model.SensorType
import org.warehouse.measure.measurement
import kotlin.test.Test
import kotlin.test.assertEquals

class ConsoleAlarmNotifierTest {

    @Test
    fun `logs the alarm message at warn`() {
        val lines = mutableListOf<String>()
        val logger = object : org.slf4j.Logger by NOPLogger.NOP_LOGGER {
            override fun warn(msg: String) { lines += msg }
        }
        val alarm = Alarm(measurement(SensorType.TEMPERATURE, 36.0), 35.0)

        ConsoleAlarmNotifier(logger).notify(alarm)

        assertEquals(listOf(alarm.message), lines)
    }
}
