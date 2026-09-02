package community.openbase.agentui

/**
 * Supplies authentication to an Openbase agent integration without coupling
 * the package to a particular identity SDK.
 */
interface AuthTokenProvider {
    /** The current bearer-compatible token, or `null` when signed out. */
    suspend fun authToken(): String?

    /**
     * Performs an authenticated request using the consuming app's auth stack
     * and returns the response body.
     */
    suspend fun authenticatedRequest(
        method: String,
        url: String,
        data: Map<String, Any?>? = null,
    ): String
}
