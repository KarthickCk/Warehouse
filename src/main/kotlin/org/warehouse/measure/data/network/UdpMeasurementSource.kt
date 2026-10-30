package org.warehouse.measure.data.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.warehouse.measure.data.parser.MeasurementParser
import org.warehouse.measure.data.parser.ParseResult
import org.warehouse.measure.domain.model.Measurement
import org.warehouse.measure.domain.model.SensorType
import org.warehouse.measure.domain.port.MeasurementSource
import org.slf4j.LoggerFactory
import kotlin.time.Clock

class UdpMeasurementSource(
    private val warehouseId: String,
    private val type: SensorType,
    private val listener: UdpListener,
    private val parser: MeasurementParser = MeasurementParser,
) : MeasurementSource {

    private val log = LoggerFactory.getLogger("${UdpMeasurementSource::class.java.name}[$warehouseId/${type.name}]")

    override fun measurements(): Flow<Measurement> {
        return listener.packets()
            .mapNotNull { data ->
                toMeasurement(data)
            }
    }

    private fun toMeasurement(raw: String): Measurement? =
        when (val result = parser.parse(raw)) {
            is ParseResult.Failure -> {
                log.warn("Dropping packet: {} [{}]", result.reason, result.raw)
                null
            }

            is ParseResult.Success -> {
                val idType = SensorType.fromSensorId(result.sensorId)
                if (idType != type) {
                    log.warn("Dropping packet: sensor id '{}' is not a {} id [{}]", result.sensorId, type.name, raw)
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