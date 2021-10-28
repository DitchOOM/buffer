@file:Suppress("EXPERIMENTAL_UNSIGNED_LITERALS")

package com.ditchoom.buffer

import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalUnsignedTypes
class BufferTests {

    @Test
    fun slice() {
        val platformBuffer = allocateNewBuffer(3u)
        platformBuffer.write((-1).toByte())
        platformBuffer.resetForRead()
        val slicedBuffer = platformBuffer.slice()
        assertEquals(1u, slicedBuffer.limit())
        assertEquals(-1, slicedBuffer.readByte())
    }


    @Test
    fun sliceFragmented() {
        val platformBuffer1 = allocateNewBuffer(3u)
        platformBuffer1.write(1.toByte())
        platformBuffer1.resetForRead()

        val platformBuffer2 = allocateNewBuffer(3u)
        platformBuffer2.write(2.toByte())
        platformBuffer2.resetForRead()

        val fragmentedBuffer = FragmentedReadBuffer(platformBuffer1, platformBuffer2)
        val slicedBuffer = fragmentedBuffer.slice()
        assertEquals(2u, slicedBuffer.limit())
        assertEquals(1, slicedBuffer.readByte())
        assertEquals(2, slicedBuffer.readByte())
    }

    @Test
    fun byte() {
        val platformBuffer = allocateNewBuffer(1u)
        val byte = (-1).toByte()
        platformBuffer.write(byte)
        platformBuffer.resetForRead()
        assertEquals(byte.toInt(), platformBuffer.readByte().toInt())
    }

    @Test
    fun byteArray() {
        val size = 200
        val platformBuffer = allocateNewBuffer(size.toUInt())
        val bytes = ByteArray(200) { -1 }
        platformBuffer.write(bytes)
        platformBuffer.resetForRead()
        val byteArray = platformBuffer.readByteArray(size.toUInt())
        assertEquals(bytes.count(), byteArray.count())
        var count = 0
        for (byte in bytes) {
            assertEquals(byte, byteArray[count++])
        }
    }

    @Test
    fun unsignedByte() {
        val platformBuffer = allocateNewBuffer(1u)
        val byte = (-1).toUByte()
        platformBuffer.write(byte)
        platformBuffer.resetForRead()
        assertEquals(byte.toInt(), platformBuffer.readUnsignedByte().toInt())
    }

    @Test
    fun unsignedShort() {
        val platformBuffer = allocateNewBuffer(2u)
        val uShort = UShort.MAX_VALUE.toInt() / 2
        platformBuffer.write(uShort.toUShort())
        platformBuffer.resetForRead()
        assertEquals(uShort, platformBuffer.readUnsignedShort().toInt())
        platformBuffer.resetForRead()
        val msb = platformBuffer.readByte()
        val lsb = platformBuffer.readByte()
        val value = ((0xff and msb.toInt() shl 8)
                or (0xff and lsb.toInt() shl 0)).toUShort()
        assertEquals(value.toInt(), uShort)
    }

    @Test
    fun allUShortValues() {
        val buffer = allocateNewBuffer(UShort.MAX_VALUE.toUInt() * UShort.SIZE_BYTES.toUInt())
        (0 until UShort.MAX_VALUE.toInt()).forEach {
            buffer.write(it.toUShort())
        }
        buffer.resetForRead()
        (0 until UShort.MAX_VALUE.toInt()).forEach {
            assertEquals(it, buffer.readUnsignedShort().toInt())
        }
    }

    @Test
    fun unsignedShortHalf() {
        val platformBuffer = allocateNewBuffer(2u)
        val uShort = (UShort.MAX_VALUE / 2u).toUShort()
        platformBuffer.write(uShort)
        platformBuffer.resetForRead()
        val actual = platformBuffer.readUnsignedShort().toInt()
        assertEquals(uShort.toInt(), actual)
        assertEquals(uShort.toString(), actual.toString())
    }

    @Test
    fun unsignedInt() {
        val platformBuffer = allocateNewBuffer(4u)
        val uInt = (-1).toUInt()
        platformBuffer.write(uInt)
        platformBuffer.resetForRead()
        assertEquals(uInt.toLong(), platformBuffer.readUnsignedInt().toLong())
    }

    @Test
    fun unsignedIntHalf() {
        val platformBuffer = allocateNewBuffer(4u)
        val uInt = Int.MAX_VALUE.toUInt() / 2u
        platformBuffer.write(uInt)
        platformBuffer.resetForRead()
        assertEquals(uInt.toLong(), platformBuffer.readUnsignedInt().toLong())
    }

    @Test
    fun long() {
        val platformBuffer = allocateNewBuffer(Long.SIZE_BYTES.toUInt())
        val long = (-1).toLong()
        platformBuffer.write(long)
        platformBuffer.resetForRead()
        assertEquals(long, platformBuffer.readLong())
    }

    @Test
    fun utf8String() {
        val string = "yolo swag lyfestyle"
        assertEquals(19, string.utf8Length().toInt())
        val platformBuffer = allocateNewBuffer(19u)
        platformBuffer.writeUtf8(string)
        platformBuffer.resetForRead()
        val actual = platformBuffer.readUtf8(19u).toString()
        assertEquals(string.length, actual.length)
        assertEquals(string, actual)
    }


    @Test
    fun readUtf8LineSingle() {
        val text = "hello"
        val buffer = text.toBuffer()
        assertEquals("hello", buffer.readUtf8Line().toString())
        assertEquals(buffer.remaining(), 0u)
    }

    @Test
    fun readUtf8LineDouble() {
        val text = "hello\r\n"
        val buffer = text.toBuffer()
        assertEquals("hello", buffer.readUtf8Line().toString())
        assertEquals("", buffer.readUtf8Line().toString())
        assertEquals(buffer.remaining(), 0u)
    }

    @Test
    fun readUtf8LineStart() {
        val text = "\r\nhello"
        val buffer = text.toBuffer()
        assertEquals("", buffer.readUtf8Line().toString())
        assertEquals("hello", buffer.readUtf8Line().toString())
        assertEquals(buffer.remaining(), 0u)
    }

    @Test
    fun readUtf8LineStartN() {
        val text = "\nhello"
        val buffer = text.toBuffer()
        assertEquals("", buffer.readUtf8Line().toString())
        assertEquals("hello", buffer.readUtf8Line().toString())
        assertEquals(buffer.remaining(), 0u)
    }

    @Test
    fun readUtf8LineMix() {
        val text = "\nhello\r\nhello\nhello\r\n"
        val buffer = text.toBuffer()
        assertEquals("", buffer.readUtf8Line().toString())
        assertEquals("hello", buffer.readUtf8Line().toString())
        assertEquals("hello", buffer.readUtf8Line().toString())
        assertEquals("hello", buffer.readUtf8Line().toString())
        assertEquals("", buffer.readUtf8Line().toString())
        assertEquals(buffer.remaining(), 0u)
    }


    @Test
    fun readUtf8LineMixMulti() {
        val text = "\nhello\r\n\nhello\n\nhello\r\n"
        val buffer = text.toBuffer()
        assertEquals("", buffer.readUtf8Line().toString())
        assertEquals("hello", buffer.readUtf8Line().toString())
        assertEquals("", buffer.readUtf8Line().toString())
        assertEquals("hello", buffer.readUtf8Line().toString())
        assertEquals("", buffer.readUtf8Line().toString())
        assertEquals("hello", buffer.readUtf8Line().toString())
        assertEquals("", buffer.readUtf8Line().toString())
        assertEquals(buffer.remaining(), 0u)
    }

    @Test
    fun readUtf8Line() {
        val stringArray = "yolo swag lyfestyle".split(' ')
        assertEquals(3, stringArray.size)
        val newLineString = stringArray.joinToString("\r\n")
        val stringBuffer = newLineString.toBuffer()
        stringArray.forEach {
            val line = stringBuffer.readUtf8Line()
            assertEquals(it, line.toString())
        }
    }
}