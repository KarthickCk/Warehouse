package org.warehouse.measure.data.parser

sealed interface ParseResult {
    data class Success(val sensorId: String, val value: Double) : ParseResult
    data class Failure(val raw: String, val reason: String) : ParseResult
}