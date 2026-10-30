package org.warehouse.measure.domain.model

import kotlin.time.Instant

data class Measurement(
    val warehouseId: String,
    val sensorId: String,
    val type: SensorType,
    val value: Double,
    val timeStamp: Instant
)
