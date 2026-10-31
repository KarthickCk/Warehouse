package org.warehouse.measure.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import org.warehouse.measure.domain.model.Measurement
import org.warehouse.measure.domain.port.MeasurementSource

class FakeMeasurementSource(private val items: List<Measurement>) : MeasurementSource {
    override fun measurements(): Flow<Measurement> = items.asFlow()
}
