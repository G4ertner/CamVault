package dev.nik.vaultcam.ui.camera

import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer

fun ImageProxy.toByteArray(): ByteArray {
    val buffer: ByteBuffer = planes.first().buffer
    buffer.rewind()
    val bytes = ByteArray(buffer.remaining())
    buffer.get(bytes)
    return bytes
}
