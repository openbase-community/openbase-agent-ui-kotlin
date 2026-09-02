package community.openbase.agentui

import java.lang.ref.WeakReference

/** Environment-specific settings required by an Openbase agent session. */
interface OpenbaseAgentConfiguration {
    /** The LiveKit server URL used for calls. Return `null` to disable calling. */
    suspend fun liveKitServerUrl(): String? = null
}

/**
 * Process-wide dependency registry used by the session implementation and UI.
 * Configure it once during app startup before accessing required providers.
 */
object OpenbaseAgentUi {
    private val lock = Any()
    private var storedConfiguration: OpenbaseAgentConfiguration? = null
    private var storedAuthProvider: AuthTokenProvider? = null
    private var storedSecureStorage: SecureStorage? = null
    private var storedCallTokenProvider: CallTokenProvider? = null
    private var storedEventHandler: WeakReference<OpenbaseAgentEventHandler>? = null

    fun configure(
        configuration: OpenbaseAgentConfiguration,
        authProvider: AuthTokenProvider,
        secureStorage: SecureStorage,
        callTokenProvider: CallTokenProvider? = null,
        eventHandler: OpenbaseAgentEventHandler? = null,
    ) {
        synchronized(lock) {
            storedConfiguration = configuration
            storedAuthProvider = authProvider
            storedSecureStorage = secureStorage
            storedCallTokenProvider = callTokenProvider
            storedEventHandler = eventHandler?.let(::WeakReference)
        }
    }

    val configuration: OpenbaseAgentConfiguration
        get() = synchronized(lock) {
            storedConfiguration ?: throw OpenbaseAgentUiNotConfiguredException()
        }

    val authProvider: AuthTokenProvider
        get() = synchronized(lock) {
            storedAuthProvider ?: throw OpenbaseAgentUiNotConfiguredException()
        }

    val secureStorage: SecureStorage
        get() = synchronized(lock) {
            storedSecureStorage ?: throw OpenbaseAgentUiNotConfiguredException()
        }

    val callTokenProvider: CallTokenProvider?
        get() = synchronized(lock) { storedCallTokenProvider }

    val isConfigured: Boolean
        get() = synchronized(lock) { storedConfiguration != null }

    suspend fun isCallingEnabled(): Boolean {
        val (callTokenProvider, configuration) = synchronized(lock) {
            storedCallTokenProvider to storedConfiguration
        }
        return callTokenProvider != null && configuration?.liveKitServerUrl() != null
    }

    fun emit(event: OpenbaseAgentEvent) {
        val eventHandler = synchronized(lock) { storedEventHandler?.get() }
        eventHandler?.handleEvent(event)
    }

    internal fun resetForTesting() {
        synchronized(lock) {
            storedConfiguration = null
            storedAuthProvider = null
            storedSecureStorage = null
            storedCallTokenProvider = null
            storedEventHandler = null
        }
    }
}

class OpenbaseAgentUiNotConfiguredException : IllegalStateException(
    "OpenbaseAgentUi has not been configured. " +
        "Call OpenbaseAgentUi.configure(...) during app initialization.",
)
