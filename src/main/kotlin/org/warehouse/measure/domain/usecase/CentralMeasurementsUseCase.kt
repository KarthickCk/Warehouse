package org.warehouse.measure.domain.usecase

import org.warehouse.measure.domain.port.AlarmNotifier
import org.warehouse.measure.domain.port.MeasurementSubscriber
import org.warehouse.measure.domain.rule.ThresholdRule

class CentralMeasurementsUseCase(
    private val subscriber: MeasurementSubscriber,
    private val rules: List<ThresholdRule>,
    private val notifier: AlarmNotifier
) {
    suspend fun execute() {
        subscriber.measurements()
            .collect { measurement ->
                rules.mapNotNull { rule ->
                    rule.evaluate(measurement)
                }
                    .onEach { alarm ->
                        notifier.notify(alarm)
                    }
            }
    }
}