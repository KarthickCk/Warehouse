package org.warehouse.measure.domain.model

enum class SensorType(
    val unit: String,
    val threshold: Double,
    val idPrefix: Char
) {
    TEMPERATURE(unit = "°C", threshold = 35.0, idPrefix = 't'),
    HUMIDITY(unit = "%", threshold = 50.0, idPrefix = 'h');

    companion object {
        fun fromSensorId(id: String): SensorType? {
            val sensorPrefix = id.firstOrNull()?.lowercaseChar() ?: return null
            return entries.firstOrNull { type ->
                type.idPrefix == sensorPrefix
            }
        }
    }
}