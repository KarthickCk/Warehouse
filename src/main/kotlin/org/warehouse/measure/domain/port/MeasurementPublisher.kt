package org.warehouse.measure.domain.port

import org.warehouse.measure.domain.model.Measurement

interface MeasurementPublisher {
    suspend fun publish(measurement: Measurement)
}