package org.warehouse.measure.data.network

import kotlinx.coroutines.flow.Flow

interface UdpListener {
    fun packets(): Flow<String>
}