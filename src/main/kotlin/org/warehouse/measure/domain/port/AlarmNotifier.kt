package org.warehouse.measure.domain.port

import org.warehouse.measure.domain.model.Alarm

interface AlarmNotifier {
    fun notify(alarm: Alarm)
}