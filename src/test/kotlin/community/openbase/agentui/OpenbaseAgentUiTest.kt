package community.openbase.agentui

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class OpenbaseAgentUiTest {

    @Before
    fun reset() {
        OpenbaseAgentUi.resetForTesting()
    }

    @Test
    fun configurationExposesConsumerAdapters() = runTest {
        val events = TestEventHandler()
        OpenbaseAgentUi.configure(
            configuration = TestConfiguration("wss://voice.example.test"),
            authProvider = TestAuthTokenProvider(),
            secureStorage = TestSecureStorage(),
            callTokenProvider = TestCallTokenProvider(),
            eventHandler = events,
        )

        assertTrue(OpenbaseAgentUi.isConfigured)
        assertTrue(OpenbaseAgentUi.isCallingEnabled())
        assertEquals("test-token", OpenbaseAgentUi.authProvider.authToken())
        assertNull(OpenbaseAgentUi.secureStorage.read("missing"))
        assertEquals(
            "room-token",
            OpenbaseAgentUi.callTokenProvider?.fetchRoomToken(
                roomName = "room",
                livekitDispatchAgentName = "dispatcher",
            ),
        )

        OpenbaseAgentUi.emit(OpenbaseAgentEvent.CallStarted(roomName = "room"))
        assertEquals(listOf<OpenbaseAgentEvent>(OpenbaseAgentEvent.CallStarted(roomName = "room")), events.events)
    }

    @Test
    fun requiredAdaptersThrowBeforeConfiguration() {
        assertThrows(OpenbaseAgentUiNotConfiguredException::class.java) {
            OpenbaseAgentUi.configuration
        }
        assertThrows(OpenbaseAgentUiNotConfiguredException::class.java) {
            OpenbaseAgentUi.authProvider
        }
        assertThrows(OpenbaseAgentUiNotConfiguredException::class.java) {
            OpenbaseAgentUi.secureStorage
        }
        assertNull(OpenbaseAgentUi.callTokenProvider)
        assertFalse(OpenbaseAgentUi.isConfigured)
    }

    @Test
    fun callTokenPreparationDefaultsToNoOp() = runTest {
        val provider = TestCallTokenProvider()
        assertFalse(provider.shouldPrepareRoomToken())
        assertNull(
            provider.prepareRoomTokenIfNeeded(
                roomName = "room",
                livekitDispatchAgentName = "dispatcher",
                onProgress = {},
            ),
        )
    }

    @Test
    fun sessionStateSemanticsRemainStable() {
        assertTrue(CallState.Connected.isActive)
        assertTrue(CallState.ActiveIncoming.isActive)
        assertFalse(CallState.Idle.isActive)
        assertEquals("test error", CallState.Errored(RuntimeException("test error")).errorMessage)
        assertTrue(CallPreparationState.StartingCloudWorkspace.isPreparing)
        assertFalse(CallPreparationState.Idle.isPreparing)
    }
}

private class TestConfiguration(private val url: String?) : OpenbaseAgentConfiguration {
    override suspend fun liveKitServerUrl(): String? = url
}

private class TestAuthTokenProvider : AuthTokenProvider {
    override suspend fun authToken(): String? = "test-token"

    override suspend fun authenticatedRequest(
        method: String,
        url: String,
        data: Map<String, Any?>?,
    ): String = ""
}

private class TestSecureStorage : SecureStorage {
    override fun save(key: String, value: String) {}
    override fun read(key: String): String? = null
    override fun delete(key: String) {}
}

private class TestCallTokenProvider : CallTokenProvider {
    override suspend fun fetchRoomToken(
        roomName: String,
        livekitDispatchAgentName: String,
    ): String = "room-token"
}

private class TestEventHandler : OpenbaseAgentEventHandler {
    val events = mutableListOf<OpenbaseAgentEvent>()

    override fun handleEvent(event: OpenbaseAgentEvent) {
        events.add(event)
    }
}
