package org.warehouse.measure.domain.rule

import org.warehouse.measure.domain.model.Alarm
import org.warehouse.measure.domain.model.Measurement

interface ThresholdRule {
    fun evaluate(measurement: Measurement): Alarm?
}