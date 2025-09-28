package dev.nik.vaultcam.data

data class VaultItem(
    val id: String,
    val file: java.io.File,
    val createdAt: Long
)
