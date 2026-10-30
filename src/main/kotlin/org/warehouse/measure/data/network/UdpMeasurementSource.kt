package org.warehouse.measure.data.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.warehouse.measure.data.parser.MeasurementParser
import org.warehouse.measure.data.parser.ParseResult
import org.warehouse.measure.domain.model.Measurement
import org.warehouse.measure.domain.model.SensorType
import org.warehouse.measure.domain.port.MeasurementSource
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.time.Clock

class UdpMeasurementSource(
    private val warehouseId: String,
    private val type: SensorType,
    private val listener: UdpListener,
    private val parser: MeasurementParser,
) : MeasurementSource {

    override fun measurements(): Flow<Measurement> {
        return listener.packets()
            .mapNotNull { data ->
                toMeasurement(data)
            }
    }

    private fun toMeasurement(raw: String): Measurement? =
        when (val result = parser.parse(raw)) {
            is ParseResult.Failure -> {
                Logger.getGlobal().log(
                    Level.WARNING,
                    "Dropping packet: ${result.reason}, ${result.raw}"
                )
                null
            }

            is ParseResult.Success -> {
                val idType = SensorType.fromSensorId(result.sensorId)
                if (idType != type) {
                    Logger.getGlobal().log(
                        Level.WARNING,
                        "Dropping packet: sensor id ${result.sensorId}, ${type.name} $raw"
                    )
                    null
                } else {
                    Measurement(
                        warehouseId = warehouseId,
                        sensorId = result.sensorId,
                        type = type,
                        value = result.value,
                        timeStamp = Clock.System.now(),
                    )
                }
            }
        }
}