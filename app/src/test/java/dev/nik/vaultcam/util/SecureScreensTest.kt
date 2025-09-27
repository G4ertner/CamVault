package dev.nik.vaultcam.util

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SecureScreensTest {

    @Test
    fun `start and camera routes are not secure`() {
        assertFalse(SecureScreens.isSecureRoute("start"))
        assertFalse(SecureScreens.isSecureRoute("camera"))
    }

    @Test
    fun `vault route is secure`() {
        assertTrue(SecureScreens.isSecureRoute("vault"))
    }

    @Test
    fun `viewer route is secure`() {
        assertTrue(SecureScreens.isSecureRoute("viewer/{id}"))
    }

    @Test
    fun `null route is not secure`() {
        assertFalse(SecureScreens.isSecureRoute(null))
    }
}
