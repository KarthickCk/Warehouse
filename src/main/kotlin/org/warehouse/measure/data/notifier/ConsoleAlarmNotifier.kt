package org.warehouse.measure.data.notifier

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.warehouse.measure.domain.model.Alarm
import org.warehouse.measure.domain.port.AlarmNotifier

class ConsoleAlarmNotifier(
    private val logger: Logger = LoggerFactory.getLogger(ConsoleAlarmNotifier::class.java),
) : AlarmNotifier {
    override fun notify(alarm: Alarm) {
        logger.warn(alarm.message)
    }
}