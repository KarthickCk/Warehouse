package org.warehouse.measure.domain.usecase

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.warehouse.measure.data.messaging.Messenger
import org.warehouse.measure.domain.model.Measurement
import org.warehouse.measure.domain.model.SensorType
import org.warehouse.measure.domain.port.MeasurementSource
import org.warehouse.measure.fakes.FakeMeasurementSource
import org.warehouse.measure.measurement
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@OptIn(ExperimentalCoroutinesApi::class)
class WarehouseMeasurementsUseCaseTest {

    @Test
    fun `publishes every measurement from every source`() = runTest(UnconfinedTestDispatcher()) {
        val bus = Messenger()
        val received = mutableListOf<Measurement>()
        val subscriber = launch { bus.measurements().toList(received) }

        val temp = listOf(measurement(SensorType.TEMPERATURE, 20.0), measurement(SensorType.TEMPERATURE, 21.0))
        val hum = listOf(measurement(SensorType.HUMIDITY, 40.0), measurement(SensorType.HUMIDITY, 41.0))
        WarehouseMeasurementsUseCase(
            sources = listOf(FakeMeasurementSource(temp), FakeMeasurementSource(hum)),
            publisher = bus,
        ).execute()

        assertEquals((temp + hum).toSet(), received.toSet())
        subscriber.cancel()
    }

    @Test
    fun `a source that completes early does not stop the others`() = runTest(UnconfinedTestDispatcher()) {
        val bus = Messenger()
        val received = mutableListOf<Measurement>()
        val subscriber = launch { bus.measurements().toList(received) }

        val empty = object : MeasurementSource {
            override fun measurements() = flow<Measurement> { }
        }
        val one = FakeMeasurementSource(listOf(measurement(SensorType.HUMIDITY, 40.0)))
        WarehouseMeasurementsUseCase(listOf(empty, one), bus).execute()

        assertEquals(1, received.size)
        subscriber.cancel()
    }

    @Test
    fun `refuses an empty source list`() {
        assertFailsWith<IllegalArgumentException> {
            WarehouseMeasurementsUseCase(emptyList(), Messenger())
        }
    }
}
