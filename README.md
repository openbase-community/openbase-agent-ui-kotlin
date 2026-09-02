# OpenbaseAgentUI for Kotlin

OpenbaseAgentUI is the dependency-light public contract used to connect an Android app to an Openbase agent session. It provides configuration, authentication and secure-storage adapters, call-token preparation, lifecycle events, and session-facing state and errors. It is the Kotlin counterpart of [openbase-agent-ui-swift](https://github.com/openbase-community/openbase-agent-ui-swift).

The package deliberately does not ship an authentication client, Keystore implementation, LiveKit session, or app UI. Consuming apps supply those concrete integrations, which keeps credentials and product-specific policy outside the reusable core.

## Installation

With the repository checked out next to your app, add a composite build in `settings.gradle.kts`:

```kotlin
includeBuild("../openbase-agent-ui-kotlin")
```

Then depend on the library by its coordinates (the composite build substitutes them):

```kotlin
implementation("community.openbase:openbase-agent-ui-kotlin:0.1.0")
```

The library is a plain Kotlin/JVM module with no runtime dependencies, so it can also be consumed from any JVM or Android project via a published artifact.

## Configuration

Implement the adapters owned by your app and configure the package during app startup:

```kotlin
import community.openbase.agentui.OpenbaseAgentUi

OpenbaseAgentUi.configure(
    configuration = AppAgentConfiguration(),
    authProvider = AppAuthTokenProvider(),
    secureStorage = AppSecureStorage(),
    callTokenProvider = AppCallTokenProvider(),
    eventHandler = appAgentEventHandler,
)
```

`CallTokenProvider` can optionally prepare a backend before returning a room token. The default implementation does no preparation, so simple integrations only need to implement `fetchRoomToken`.

## Boundary

This package owns stable interfaces and value types that a session UI can depend on. A consuming app remains responsible for:

- user authentication and authenticated HTTP requests;
- Android Keystore or other secure storage;
- minting LiveKit-compatible room tokens;
- the concrete call/session engine and presentation;
- handling emitted lifecycle events.

## License

OpenbaseAgentUI for Kotlin is licensed under AGPL-3.0-only. See [LICENSE](LICENSE).
