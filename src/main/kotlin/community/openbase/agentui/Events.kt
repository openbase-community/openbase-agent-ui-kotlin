package community.openbase.agentui

/**
 * Lifecycle events emitted by an Openbase agent session for the consuming app
 * to route into navigation, notifications, diagnostics, or analytics.
 */
sealed class OpenbaseAgentEvent {
    data class IncomingCall(val callerId: String, val callerName: String) : OpenbaseAgentEvent()
    data class VoipTokenUpdated(val token: String) : OpenbaseAgentEvent()
    data class CallStarted(val roomName: String) : OpenbaseAgentEvent()
    data class CallEnded(val roomName: String) : OpenbaseAgentEvent()
    data class Error(val message: String) : OpenbaseAgentEvent()
}

interface OpenbaseAgentEventHandler {
    fun handleEvent(event: OpenbaseAgentEvent)
}
