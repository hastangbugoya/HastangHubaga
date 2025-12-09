package com.example.hastanghubaga.data.backup

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Utility object for AES-256 encryption/decryption for backup files.
 *
 * Uses:
 *  • PBKDF2(HMAC-SHA256) for password → key derivation
 *  • AES/CBC/PKCS5Padding for encryption
 *  • 16-byte IV prepended to ciphertext
 */
object BackupCrypto {

    private const val ITERATIONS = 50_000
    private const val KEY_LENGTH = 256
    private const val SALT = "HASTANG_STATIC_SALT_01" // You may randomize & store separately

    /**
     * Derives a 256-bit AES key using PBKDF2.
     */
    private fun deriveKey(password: CharArray): SecretKeySpec {
        val spec = PBEKeySpec(password, SALT.toByteArray(), ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return SecretKeySpec(factory.generateSecret(spec).encoded, "AES")
    }

    /**
     * Encrypts raw bytes using AES-256 + random IV.
     *
     * @return ByteArray with IV prepended.
     */
    fun encrypt(data: ByteArray, password: CharArray): ByteArray {
        val key = deriveKey(password)
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))

        val encrypted = cipher.doFinal(data)
        return iv + encrypted
    }

    /**
     * Decrypts AES-256 bytes created by [encrypt].
     */
    fun decrypt(encrypted: ByteArray, password: CharArray): ByteArray {
        val iv = encrypted.copyOfRange(0, 16)
        val ciphertext = encrypted.copyOfRange(16, encrypted.size)

        val key = deriveKey(password)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))

        return cipher.doFinal(ciphertext)
    }
}

