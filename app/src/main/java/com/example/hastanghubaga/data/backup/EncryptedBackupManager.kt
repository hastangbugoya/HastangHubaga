package com.example.hastanghubaga.core.backup

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.example.hastanghubaga.core.backup.SerializationConfig
import kotlinx.serialization.serializer
import java.io.File

/**
 * Manages encrypted backup and restore of app data using:
 * - Jetpack Security (AES-256 encrypted files)
 * - Kotlin Serialization (JSON encoding/decoding)
 *
 * @param context Application context used for file encryption.
 */
class EncryptedBackupManager(private val context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private fun encryptedFile(file: File): EncryptedFile =
        EncryptedFile.Builder(
            context,
            file,
            masterKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

    /**
     * Writes any serializable data object into an encrypted backup file.
     */
    suspend fun <T> createBackup(
        file: File,
        data: T
    ) {
        val json = SerializationConfig.json.encodeToString(
            serializer = SerializationConfig.json.serializersModule.serializer(data!!::class.java),
            value = data
        )

        encryptedFile(file).openFileOutput().use { out ->
            out.write(json.toByteArray())
            out.flush()
        }
    }

    /**
     * Restores an object of type [T] from the encrypted backup file.
     */
    suspend fun <T> restoreBackup(
        file: File,
        deserializer: kotlinx.serialization.DeserializationStrategy<T>
    ): T {
        val text = encryptedFile(file).openFileInput().use { input ->
            input.readBytes().decodeToString()
        }

        return SerializationConfig.json.decodeFromString(deserializer, text)
    }

    /**
     * Deletes the encrypted backup file if it exists.
     */
    fun deleteBackup(file: File): Boolean {
        return file.exists() && file.delete()
    }
}
