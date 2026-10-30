package org.warehouse.measure.data.network

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class DatagramUdpListener(
    private val port: Int,
    private val bufferSize: Int = 1024,
    private val channelCapacity: Int = 256,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val onBound: (Int) -> Unit = {},
    private val log: Logger = LoggerFactory.getLogger(DatagramUdpListener::class.java),
) : UdpListener {

    override fun packets(): Flow<String> = callbackFlow {
        val socket = DatagramSocket(port)
        log.info("Listening for UDP on port {}", socket.localPort)
        onBound(socket.localPort)

        val reader = launch(ioDispatcher) {
            val buffer = ByteArray(bufferSize)
            val packet = DatagramPacket(buffer, buffer.size)
            while (isActive) {
                try {
                    socket.receive(packet)
                } catch (e: SocketException) {
                    if (socket.isClosed) break           // normal shutdown via awaitClose
                    throw e                              // real failure: fail the flow
                }
                val text = String(packet.data, packet.offset, packet.length, Charsets.UTF_8).trimEnd()
                log.debug("port {} <- {}", socket.localPort, text)
                trySend(text)
                packet.length = buffer.size
            }
            close()
        }

        awaitClose {
            log.info("Closing UDP port {}", socket.localPort)
            socket.close()
            reader.cancel()
        }
    }.buffer(capacity = channelCapacity, onBufferOverflow = BufferOverflow.DROP_OLDEST)
}