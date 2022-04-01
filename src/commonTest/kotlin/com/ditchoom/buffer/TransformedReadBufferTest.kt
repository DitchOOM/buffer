@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.ditchoom.buffer

import kotlin.test.Test
import kotlin.test.assertEquals

class TransformedReadBufferTest {

    @Test
    fun byte() {
        val buffer = PlatformBuffer.allocate(Byte.SIZE_BYTES.toUInt())
        buffer.write(10.toByte())
        buffer.resetForRead()
        val add1TransformedReadBuffer = TransformedReadBuffer(buffer) { _, byte ->
            (byte + 1).toByte()
        }
        assertEquals(11.toByte(), add1TransformedReadBuffer.readByte())
    }
}