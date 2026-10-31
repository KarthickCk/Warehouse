package org.warehouse.measure.data.messaging

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.warehouse.measure.domain.model.Measurement
import org.warehouse.measure.domain.model.SensorType
import org.warehouse.measure.measurement
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class MessengerTest {

    @Test
    fun `subscriber receives a published measurement`() = runTest(UnconfinedTestDispatcher()) {
        val bus = Messenger()
        val m = measurement(SensorType.TEMPERATURE, 20.0)

        val received = async { bus.measurements().first() }
        bus.publish(m)

        assertEquals(m, received.await())
    }

    @Test
    fun `every subscriber receives every measurement`() = runTest(UnconfinedTestDispatcher()) {
        val bus = Messenger()
        val a = async { bus.measurements().take(2).toList() }
        val b = async { bus.measurements().take(2).toList() }
        val m1 = measurement(SensorType.TEMPERATURE, 1.0)
        val m2 = measurement(SensorType.HUMIDITY, 2.0)

        bus.publish(m1)
        bus.publish(m2)

        assertEquals(listOf(m1, m2), a.await())
        assertEquals(listOf(m1, m2), b.await())
    }

    @Test
    fun `no replay for late subscribers`() = runTest(UnconfinedTestDispatcher()) {
        val bus = Messenger()
        bus.publish(measurement(SensorType.TEMPERATURE, 1.0))

        val received = mutableListOf<Measurement>()
        val job = launch { bus.measurements().toList(received) }
        advanceUntilIdle()

        assertEquals(emptyList(), received)
        job.cancel()
    }
}
