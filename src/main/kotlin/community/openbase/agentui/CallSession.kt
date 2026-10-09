package community.openbase.agentui

data class PreparedCallToken(
    val roomName: String,
    val token: String,
)

enum class CallPreparationState {
    Idle,
    CheckingCloudWorkspace,
    StartingCloudWorkspace,
    WaitingForCloudWorkspace,
    ;

    val isPreparing: Boolean get() = this != Idle
}

/**
 * The conversation a call is started from. A call started from a thread talks
 * to that thread from its first word; a call started with no target talks to
 * the dispatcher.
 *
 * @param label the name the app shows for the thread, spoken when it cannot
 * be reached; the runtime derives one when null or blank.
 */
data class CallTargetThread(
    val threadId: String,
    val label: String? = null,
) {
    val normalizedLabel: String? get() = label?.trim()?.takeIf { it.isNotEmpty() }
}

/**
 * Mints session credentials and, when necessary, prepares the backing agent
 * runtime before a call connects.
 *
 * Override the `targetThread` variants to start calls inside a thread; the
 * defaults forward to the target-less methods, so an implementation of the
 * original methods still satisfies the contract (dispatcher calls only).
 */
interface CallTokenProvider {
    suspend fun fetchRoomToken(
        roomName: String,
        livekitDispatchAgentName: String,
    ): String

    suspend fun fetchRoomToken(
        roomName: String,
        livekitDispatchAgentName: String,
        targetThread: CallTargetThread?,
    ): String = fetchRoomToken(roomName, livekitDispatchAgentName)

    suspend fun shouldPrepareRoomToken(): Boolean = false

    suspend fun prepareRoomTokenIfNeeded(
        roomName: String,
        livekitDispatchAgentName: String,
        onProgress: (CallPreparationState) -> Unit,
    ): PreparedCallToken? = null

    suspend fun prepareRoomTokenIfNeeded(
        roomName: String,
        livekitDispatchAgentName: String,
        targetThread: CallTargetThread?,
        onProgress: (CallPreparationState) -> Unit,
    ): PreparedCallToken? = prepareRoomTokenIfNeeded(roomName, livekitDispatchAgentName, onProgress)
}

/** State exposed by a concrete call/session engine to its presentation layer. */
sealed class CallState {
    object Idle : CallState()
    data class Errored(val error: Throwable) : CallState()
    object ActiveIncoming : CallState()
    object ActiveOutgoing : CallState()
    object Connected : CallState()

    val isActive: Boolean
        get() = when (this) {
            is ActiveIncoming, is ActiveOutgoing, is Connected -> true
            is Idle, is Errored -> false
        }

    val errorMessage: String?
        get() = (this as? Errored)?.error?.let { it.localizedMessage ?: it.message }
}

sealed class CallManagerException(message: String) : Exception(message) {
    class TokenFetchFailed(detail: String) :
        CallManagerException("Failed to fetch token: $detail")

    class TokenRequestDenied(val code: String?, message: String) :
        CallManagerException(message)

    class NotAuthenticated :
        CallManagerException("You must be logged in to start a call")

    class NotConfigured :
        CallManagerException("Call functionality is not configured")

    class LiveKitNotAvailable :
        CallManagerException("LiveKit server URL is not configured")

    class InboundCallBusy :
        CallManagerException("Another voice call is already active")

    class InboundInvitationMissing :
        CallManagerException("The incoming call invitation is unavailable")

    class InboundInvitationExpired :
        CallManagerException("The incoming call invitation expired")
}

enum class ActiveVoiceSessionMuteToggleResult {
    Muted,
    Unmuted,
    NoActiveVoiceSession,
}
