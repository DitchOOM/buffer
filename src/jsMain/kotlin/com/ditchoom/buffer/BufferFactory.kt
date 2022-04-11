@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.ditchoom.buffer

import org.khronos.webgl.Int8Array
import org.khronos.webgl.Uint8Array

actual fun PlatformBuffer.Companion.allocate(
    size: UInt,
    byteOrder: ByteOrder
): PlatformBuffer {
    return JsBuffer(Uint8Array(size.toInt()), littleEndian = byteOrder == ByteOrder.LITTLE_ENDIAN)
}

// TODO: Wrap shouldn't duplicate data. Look into direct case to Uint8Array to wrap with the JsBuffer or use NativeBuffer
actual fun PlatformBuffer.Companion.wrap(array: ByteArray, byteOrder: ByteOrder): PlatformBuffer =
    //NativeBuffer(array, byteOrder = byteOrder)
    JsBuffer(Uint8Array(array.toTypedArray()), littleEndian = byteOrder == ByteOrder.LITTLE_ENDIAN)

actual fun String.toBuffer(): PlatformBuffer {
    val int8Array = encodeToByteArray().unsafeCast<Int8Array>()
    val uint8Array = Uint8Array(int8Array.buffer)
    return JsBuffer(uint8Array)
}

actual fun String.utf8Length(): UInt = encodeToByteArray().unsafeCast<Int8Array>().length.toUInt()