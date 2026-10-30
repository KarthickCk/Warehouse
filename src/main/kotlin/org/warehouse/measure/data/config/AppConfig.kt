package org.warehouse.measure.data.config

import org.warehouse.measure.domain.model.SensorType

data class AppConfig(
    val warehouseIds: List<String>,
    val ports: Map<SensorType, Int>,
    val thresholds: Map<SensorType, Double>,
    val channelCapacity: Int,
    val datagramBufferSize: Int,
) {
    init {
        require(warehouseIds.isNotEmpty()) { "At least one warehouse id is required" }
        require(channelCapacity > 0) { "channelCapacity must be positive" }
        require(datagramBufferSize > 0) { "datagramBufferSize must be positive" }
        SensorType.entries.forEach { type ->
            require(type in ports) { "No port configured for ${type.name}" }
            require(type in thresholds) { "No threshold configured for ${type.name}" }
        }
    }

    companion object {
        private val DEFAULT_PORTS = mapOf(
            SensorType.TEMPERATURE to 3344,
            SensorType.HUMIDITY to 3355,
        )

        val DEFAULT = AppConfig(
            warehouseIds = listOf("wh-1"),
            ports = DEFAULT_PORTS,
            thresholds = SensorType.entries.associateWith { it.threshold },
            channelCapacity = 256,
            datagramBufferSize = 1024,
        )

        fun fromEnvironment(env: Map<String, String> = System.getenv()): AppConfig {
            val warehouseIds = env["WAREHOUSE_IDS"]
                ?.split(',')
                ?.map { it.trim() }
                ?.filter { it.isNotEmpty() }
                ?.takeIf { it.isNotEmpty() }
                ?: DEFAULT.warehouseIds

            val ports = SensorType.entries.associateWith { type ->
                env.int("${type.name}_PORT") ?: DEFAULT.ports.getValue(type)
            }
            val thresholds = SensorType.entries.associateWith { type ->
                env.double("${type.name}_THRESHOLD") ?: type.threshold
            }

            return AppConfig(
                warehouseIds = warehouseIds,
                ports = ports,
                thresholds = thresholds,
                channelCapacity = env.int("CHANNEL_CAPACITY") ?: DEFAULT.channelCapacity,
                datagramBufferSize = env.int("DATAGRAM_BUFFER_SIZE") ?: DEFAULT.datagramBufferSize,
            )
        }

        private fun Map<String, String>.int(key: String): Int? =
            this[key]?.let { raw ->
                raw.trim().toIntOrNull()
                    ?: throw IllegalArgumentException("Environment variable $key must be an integer, was '$raw'")
            }

        private fun Map<String, String>.double(key: String): Double? =
            this[key]?.let { raw ->
                raw.trim().toDoubleOrNull()
                    ?: throw IllegalArgumentException("Environment variable $key must be a number, was '$raw'")
            }
    }
}