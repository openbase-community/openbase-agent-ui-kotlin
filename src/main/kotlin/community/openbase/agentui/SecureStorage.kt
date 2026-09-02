package community.openbase.agentui

/**
 * Secure persistence supplied by the consuming app, normally backed by the
 * Android Keystore (for example through EncryptedSharedPreferences). The
 * reusable package never chooses storage keys or persists data on its own.
 */
interface SecureStorage {
    fun save(key: String, value: String)
    fun read(key: String): String?
    fun delete(key: String)
}
