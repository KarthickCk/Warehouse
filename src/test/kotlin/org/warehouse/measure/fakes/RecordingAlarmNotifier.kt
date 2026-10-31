package org.warehouse.measure.fakes

import org.warehouse.measure.domain.model.Alarm
import org.warehouse.measure.domain.port.AlarmNotifier
import java.util.concurrent.CopyOnWriteArrayList

class RecordingAlarmNotifier : AlarmNotifier {
    private val recorded = CopyOnWriteArrayList<Alarm>()
    val alarms: List<Alarm> get() = recorded.toList()

    override fun notify(alarm: Alarm) {
        recorded += alarm
    }
}
