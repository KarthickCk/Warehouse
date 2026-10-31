package org.warehouse.measure.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.warehouse.measure.data.messaging.Messenger
import org.warehouse.measure.domain.model.Alarm
import org.warehouse.measure.domain.model.SensorType
import org.warehouse.measure.domain.rule.SimpleThresholdRule
import org.warehouse.measure.domain.rule.ThresholdRule
import org.warehouse.measure.fakes.RecordingAlarmNotifier
import org.warehouse.measure.measurement
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CentralMeasurementsUseCaseTest {

    private val rules = SimpleThresholdRule.forAllTypes(emptyMap())

    @Test
    fun `raises exactly one alarm for one breaching and one safe measurement`() = runTest(UnconfinedTestDispatcher()) {
        val bus = Messenger()
        val notifier = RecordingAlarmNotifier()
        val job = launch { CentralMeasurementsUseCase(bus, rules, notifier).execute() }

        bus.publish(measurement(SensorType.TEMPERATURE, 36.0))
        bus.publish(measurement(SensorType.HUMIDITY, 10.0))
        advanceUntilIdle()

        assertEquals(1, notifier.alarms.size)
        assertEquals("t1", notifier.alarms.single().measurement.sensorId)
        assertEquals(35.0, notifier.alarms.single().threshold)
        job.cancel()
    }

    @Test
    fun `one measurement breaching two rules raises two alarms`() = runTest(UnconfinedTestDispatcher()) {
        val bus = Messenger()
        val notifier = RecordingAlarmNotifier()
        val twoRules = listOf(
            SimpleThresholdRule(SensorType.TEMPERATURE, 30.0),
            SimpleThresholdRule(SensorType.TEMPERATURE, 35.0),
        )
        val job = launch { CentralMeasurementsUseCase(bus, twoRules, notifier).execute() }

        bus.publish(measurement(SensorType.TEMPERATURE, 40.0))
        advanceUntilIdle()

        assertEquals(listOf(30.0, 35.0), notifier.alarms.map { it.threshold })
        job.cancel()
    }

    @Test
    fun `measurement with no applicable rule raises nothing`() = runTest(UnconfinedTestDispatcher()) {
        val bus = Messenger()
        val notifier = RecordingAlarmNotifier()
        val onlyHumidity: List<ThresholdRule> = listOf(SimpleThresholdRule(SensorType.HUMIDITY, 50.0))
        val job = launch { CentralMeasurementsUseCase(bus, onlyHumidity, notifier).execute() }

        bus.publish(measurement(SensorType.TEMPERATURE, 99.0))
        advanceUntilIdle()

        assertEquals(emptyList(), notifier.alarms)
        job.cancel()
    }

    @Test
    fun `readings from several warehouses are judged on one stream`() = runTest(UnconfinedTestDispatcher()) {
        val bus = Messenger()
        val notifier = RecordingAlarmNotifier()
        val job = launch { CentralMeasurementsUseCase(bus, rules, notifier).execute() }

        bus.publish(measurement(SensorType.TEMPERATURE, 36.0, warehouseId = "wh-1"))
        bus.publish(measurement(SensorType.TEMPERATURE, 37.0, warehouseId = "wh-2"))
        advanceUntilIdle()

        assertEquals(listOf("wh-1", "wh-2"), notifier.alarms.map { it.measurement.warehouseId })
        job.cancel()
    }
}
