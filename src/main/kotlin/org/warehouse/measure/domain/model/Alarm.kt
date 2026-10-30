package org.warehouse.measure.domain.model

data class Alarm(
    val measurement: Measurement,
    val threshold: Double
) {
    val message: String
        get() = with(measurement) {
            "ALARM [warehouse=$warehouseId] [sensor=$sensorId] ${type.name} " +
                    "$value ${type.unit} exceeds threshold $threshold ${type.unit} at $timeStamp"
        }
}