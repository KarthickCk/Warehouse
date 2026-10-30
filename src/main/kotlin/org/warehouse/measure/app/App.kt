package org.warehouse.measure.app

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.warehouse.measure.data.config.AppConfig
import org.warehouse.measure.data.messaging.Messenger
import org.warehouse.measure.data.notifier.ConsoleAlarmNotifier
import org.warehouse.measure.data.network.DatagramUdpListener
import org.warehouse.measure.data.network.UdpMeasurementSource
import org.warehouse.measure.domain.model.SensorType
import org.warehouse.measure.domain.rule.SimpleThresholdRule
import org.warehouse.measure.domain.port.MeasurementPublisher
import org.warehouse.measure.domain.usecase.WarehouseMeasurementsUseCase
import org.warehouse.measure.domain.usecase.CentralMeasurementsUseCase

fun main() = runBlocking {

    val config = AppConfig.fromEnvironment()

    val messenger = Messenger(config.channelCapacity)
    val notifier = ConsoleAlarmNotifier()

    val rules = SimpleThresholdRule.forAllTypes(config.thresholds)
    val monitor = CentralMeasurementsUseCase(
        subscriber = messenger,
        rules = rules,
        notifier = notifier
    )
    val collectors = config.warehouseIds.associateWith { id ->
        buildWarehouseUseCase(id, config, messenger)
    }

    val root = CoroutineScope(SupervisorJob() + CoroutineName("App"))

    root.launch(CoroutineName("central")) {
        monitor.execute()
    }
    collectors.forEach { (id, collector) ->
        root.launch(CoroutineName("warehouse-$id")) {
            try {
                collector.execute()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Runtime.getRuntime().addShutdownHook(Thread {
        root.cancel()
    })

    root.coroutineContext.job.join()
}

private fun buildWarehouseUseCase(
    warehouseId: String,
    config: AppConfig,
    publisher: MeasurementPublisher,
): WarehouseMeasurementsUseCase {
    val sources = SensorType.entries.map { type ->
        UdpMeasurementSource(
            warehouseId = warehouseId,
            type = type,
            listener = DatagramUdpListener(
                port = config.ports.getValue(type),
                bufferSize = config.datagramBufferSize,
                channelCapacity = config.channelCapacity,
            )
        )
    }
    return WarehouseMeasurementsUseCase(sources, publisher)
}