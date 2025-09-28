package dev.nik.vaultcam.util

import android.view.Surface
import androidx.exifinterface.media.ExifInterface

object Orientation {
    fun displayRotationToDegrees(rotation: Int): Int = when (rotation) {
        Surface.ROTATION_0 -> 0
        Surface.ROTATION_90 -> 90
        Surface.ROTATION_180 -> 180
        Surface.ROTATION_270 -> 270
        else -> 0
    }

    fun degreesToExifOrientation(degrees: Int): Int = when (degrees) {
        0 -> ExifInterface.ORIENTATION_NORMAL
        90 -> ExifInterface.ORIENTATION_ROTATE_90
        180 -> ExifInterface.ORIENTATION_ROTATE_180
        270 -> ExifInterface.ORIENTATION_ROTATE_270
        else -> ExifInterface.ORIENTATION_UNDEFINED
    }

    fun fromSurfaceRotation(rotation: Int): Int {
        return degreesToExifOrientation(displayRotationToDegrees(rotation))
    }
}
