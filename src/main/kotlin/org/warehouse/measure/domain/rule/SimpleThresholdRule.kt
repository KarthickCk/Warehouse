package org.warehouse.measure.domain.rule

import org.warehouse.measure.domain.model.Alarm
import org.warehouse.measure.domain.model.Measurement
import org.warehouse.measure.domain.model.SensorType

class SimpleThresholdRule(
    private val sensorType: SensorType,
    private val threshold: Double = sensorType.threshold
) : ThresholdRule {

    override fun evaluate(measurement: Measurement): Alarm? {
        if (measurement.type == sensorType && measurement.value > threshold)
            return Alarm(
                measurement = measurement,
                threshold = threshold
            )
        return null
    }

    companion object {
        fun forAllTypes(thresholds: Map<SensorType, Double>): List<ThresholdRule> {
            return SensorType.entries.map { type ->
                SimpleThresholdRule(
                    sensorType = type,
                    threshold = thresholds[type] ?: type.threshold
                )
            }
        }
    }
}