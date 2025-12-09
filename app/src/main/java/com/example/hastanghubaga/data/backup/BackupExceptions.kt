package com.example.hastanghubaga.data.backup

/** Thrown when backup file cannot be parsed or decrypted. */
class BackupInvalidException(message: String) : Exception(message)

/** Thrown when password is wrong. */
class BackupDecryptionException(message: String) : Exception(message)