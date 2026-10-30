package org.warehouse.measure.domain.usecase

import kotlinx.coroutines.flow.merge
import org.warehouse.measure.domain.port.MeasurementPublisher
import org.warehouse.measure.domain.port.MeasurementSource

class WarehouseMeasurementsUseCase(
    private val sources: List<MeasurementSource>,
    private val publisher: MeasurementPublisher
) {
    init {
        require(sources.isNotEmpty()) { "A warehouse needs at least one measurement source" }
    }

    suspend fun execute() {
        sources.map { source ->
            source.measurements()
        }
            .merge()
            .collect { measurement ->
                publisher.publish(measurement)
            }
    }
}