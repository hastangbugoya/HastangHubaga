package com.example.hastanghubaga.core.backup

import kotlinx.serialization.json.Json

/**
 * Central JSON configuration used across the entire app.
 *
 * This ensures all encoding/decoding uses consistent rules, and makes it much
 * easier to modify serialization behavior globally (e.g., enabling pretty print,
 * ignoring unknown fields, strict mode, etc.).
 *
 * Usage:
 *    val jsonText = SerializationConfig.json.encodeToString(MyDataClass(...))
 *    val obj = SerializationConfig.json.decodeFromString<MyDataClass>(jsonText)
 */
object SerializationConfig {

    /**
     * Shared Json instance for the entire application.
     *
     * - `ignoreUnknownKeys = true`: allows decoding even if new fields are added later.
     * - `encodeDefaults = true`: writes default values so backups are stable.
     * - `prettyPrint = false`: compact output for storage (toggle to true if needed).
     * - `explicitNulls = false`: omit nulls to keep files smaller.
     */
    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = false
        explicitNulls = false
    }
}