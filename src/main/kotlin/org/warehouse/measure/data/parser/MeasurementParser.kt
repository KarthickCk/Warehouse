package org.warehouse.measure.data.parser

object MeasurementParser {
    private const val KEY_SENSOR_ID = "sensor_id"
    private const val KEY_VALUE = "value"

    fun parse(measurementString: String): ParseResult {
        val trimmed = measurementString.trim()
        if (trimmed.isEmpty())
            return ParseResult.Failure(raw = measurementString, reason = "Empty measurement")

        val fields = HashMap<String, String>()
        for (segment in trimmed.split(';')) {
            val field = segment.trim()
            if (field.isEmpty()) continue
            val parts = field.split('=', limit = 2)
            if (parts.size != 2)
                return ParseResult.Failure(measurementString, "malformed field '$field'")
            fields[parts[0].trim()] = parts[1].trim()
        }

        val sensorId = fields[KEY_SENSOR_ID] ?: return ParseResult.Failure(
            measurementString,
            "missing sensor_id"
        )
        if (sensorId.isEmpty()) {
            return ParseResult.Failure(measurementString, "empty sensor_id")
        }

        val valueString = fields[KEY_VALUE] ?: return ParseResult.Failure(
            measurementString,
            "missing value"
        )
        val value = valueString.toDoubleOrNull() ?: return ParseResult.Failure(
            measurementString,
            "value is not number"
        )

        return ParseResult.Success(
            sensorId = sensorId,
            value = value
        )
    }
}