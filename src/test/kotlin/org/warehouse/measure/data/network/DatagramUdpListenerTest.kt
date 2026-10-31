package org.warehouse.measure.data.network

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.time.Duration.Companion.seconds

class DatagramUdpListenerTest {

    private fun send(port: Int, text: String) {
        DatagramSocket().use { client ->
            val bytes = text.toByteArray(Charsets.UTF_8)
            client.send(DatagramPacket(bytes, bytes.size, InetAddress.getLoopbackAddress(), port))
        }
    }

    @Test
    fun `emits one datagram as trimmed text`() = runBlocking {
        val bound = CompletableDeferred<Int>()
        val listener = DatagramUdpListener(port = 0, onBound = { bound.complete(it) })

        val received = async(Dispatchers.Default) { listener.packets().first() }
        val port = withTimeout(2.seconds) { bound.await() }

        send(port, "sensor_id=t1; value=36 ")

        assertEquals("sensor_id=t1; value=36", withTimeout(2.seconds) { received.await() })
    }

    @Test
    fun `does not bind or start the receive loop until collected`() = runBlocking {
        val bound = CompletableDeferred<Int>()
        val listener = DatagramUdpListener(port = 0, onBound = { bound.complete(it) })

        listener.packets()          // build the flow but never collect it
        delay(200)

        assertFalse(bound.isCompleted, "socket must not be bound before collection starts")
    }

    @Test
    fun `cancelling the collector releases the port`() = runBlocking {
        val bound = CompletableDeferred<Int>()
        val listener = DatagramUdpListener(port = 0, onBound = { bound.complete(it) })

        val collector = async(Dispatchers.Default) { listener.packets().first() }
        val port = withTimeout(2.seconds) { bound.await() }

        collector.cancel()
        collector.join()

        DatagramSocket(port).close()
    }
}
