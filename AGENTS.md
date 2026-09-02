This repository contains the public OpenbaseAgentUI Kotlin package, the Kotlin counterpart of openbase-agent-ui-swift. Keep it dependency-light and suitable for third-party Android apps: public contracts and session-facing state belong here; Openbase app authentication, storage, networking, LiveKit implementation, and UI policy stay in consuming apps. Keep the API surface aligned with openbase-agent-ui-swift when either side changes.

The package is licensed AGPL-3.0-only. Never add secrets, app signing configuration, or machine-local paths.
