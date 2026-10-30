package org.warehouse.measure.domain.port

import kotlinx.coroutines.flow.Flow
import org.warehouse.measure.domain.model.Measurement

interface MeasurementSubscriber {
    fun measurements(): Flow<Measurement>
}