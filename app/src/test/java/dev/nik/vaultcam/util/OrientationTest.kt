package dev.nik.vaultcam.util

import android.view.Surface
import androidx.exifinterface.media.ExifInterface
import org.junit.Assert.assertEquals
import org.junit.Test

class OrientationTest {

    @Test
    fun rotationToDegreesMapping() {
        assertEquals(0, Orientation.displayRotationToDegrees(Surface.ROTATION_0))
        assertEquals(90, Orientation.displayRotationToDegrees(Surface.ROTATION_90))
        assertEquals(180, Orientation.displayRotationToDegrees(Surface.ROTATION_180))
        assertEquals(270, Orientation.displayRotationToDegrees(Surface.ROTATION_270))
    }

    @Test
    fun degreesToExifMapping() {
        assertEquals(ExifInterface.ORIENTATION_NORMAL, Orientation.degreesToExifOrientation(0))
        assertEquals(ExifInterface.ORIENTATION_ROTATE_90, Orientation.degreesToExifOrientation(90))
        assertEquals(ExifInterface.ORIENTATION_ROTATE_180, Orientation.degreesToExifOrientation(180))
        assertEquals(ExifInterface.ORIENTATION_ROTATE_270, Orientation.degreesToExifOrientation(270))
    }

    @Test
    fun surfaceRotationToExifMapping() {
        assertEquals(ExifInterface.ORIENTATION_NORMAL, Orientation.fromSurfaceRotation(Surface.ROTATION_0))
        assertEquals(ExifInterface.ORIENTATION_ROTATE_90, Orientation.fromSurfaceRotation(Surface.ROTATION_90))
        assertEquals(ExifInterface.ORIENTATION_ROTATE_180, Orientation.fromSurfaceRotation(Surface.ROTATION_180))
        assertEquals(ExifInterface.ORIENTATION_ROTATE_270, Orientation.fromSurfaceRotation(Surface.ROTATION_270))
    }
}
