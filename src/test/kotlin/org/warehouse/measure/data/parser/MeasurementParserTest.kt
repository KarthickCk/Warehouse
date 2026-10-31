package org.warehouse.measure.data.parser

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class MeasurementParserTest {

    private fun success(raw: String): ParseResult.Success = assertIs(MeasurementParser.parse(raw))
    private fun failure(raw: String): ParseResult.Failure = assertIs(MeasurementParser.parse(raw))

    @Test
    fun `parses the spec examples`() {
        assertEquals(ParseResult.Success("t1", 30.0), success("sensor_id=t1; value=30"))
        assertEquals(ParseResult.Success("h1", 40.0), success("sensor_id=h1; value=40"))
    }

    @Test
    fun `tolerates whitespace, field order, trailing newline and trailing semicolon`() {
        assertEquals(ParseResult.Success("t1", 30.0), success("  value = 30 ;sensor_id= t1 ;\n"))
    }

    @Test
    fun `parses decimals and negatives`() {
        assertEquals(ParseResult.Success("t1", -3.25), success("sensor_id=t1; value=-3.25"))
    }

    @Test
    fun `ignores unknown fields`() {
        assertEquals(ParseResult.Success("t1", 30.0), success("sensor_id=t1; value=30; battery=97"))
    }

    @Test
    fun `empty payload`() {
        assertEquals("Empty measurement", failure("").reason)
        assertEquals("Empty measurement", failure("   \n").reason)
    }

    @Test
    fun `missing sensor_id`() {
        assertEquals("missing sensor_id", failure("value=30").reason)
    }

    @Test
    fun `empty sensor_id`() {
        assertEquals("empty sensor_id", failure("sensor_id=; value=30").reason)
    }

    @Test
    fun `missing value`() {
        assertEquals("missing value", failure("sensor_id=t1").reason)
    }

    @Test
    fun `non-numeric value`() {
        assertEquals("value is not number", failure("sensor_id=t1; value=abc").reason)
    }

    @Test
    fun `malformed field keeps the raw payload`() {
        val f = failure("garbage")
        assertEquals("malformed field 'garbage'", f.reason)
        assertEquals("garbage", f.raw)
    }
}
