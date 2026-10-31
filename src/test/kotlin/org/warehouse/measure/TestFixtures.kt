package org.warehouse.measure

import org.warehouse.measure.domain.model.Measurement
import org.warehouse.measure.domain.model.SensorType
import kotlin.time.Instant

fun measurement(
    type: SensorType,
    value: Double,
    sensorId: String = if (type == SensorType.TEMPERATURE) "t1" else "h1",
    warehouseId: String = "wh-test",
    timeStamp: Instant = Instant.fromEpochSeconds(1_700_000_000),
) = Measurement(warehouseId, sensorId, type, value, timeStamp)
