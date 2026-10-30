package org.warehouse.measure.data.messaging

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.warehouse.measure.domain.model.Measurement
import org.warehouse.measure.domain.port.MeasurementPublisher
import org.warehouse.measure.domain.port.MeasurementSubscriber

class Messenger(
    bufferCapacity: Int = 256,
) : MeasurementPublisher, MeasurementSubscriber {

    private val flow = MutableSharedFlow<Measurement>(
        replay = 0,
        extraBufferCapacity = bufferCapacity,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )

    override suspend fun publish(measurement: Measurement) {
        flow.emit(measurement)
    }

    override fun measurements(): Flow<Measurement> {
        return flow.asSharedFlow()
    }
}