package com.inokey.solution.dnk.nucleus.observability

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Propriétés de configuration Nucleus — exposées via application.yml.
 *
 * nucleus:
 *   enabled: true
 *   application-code: pertinence-engine
 *   observability:
 *     enabled: true
 *     capture-request-body: false
 *     capture-response-body: false
 *     correlation-header: X-Correlation-Id
 *     session-header: X-Session-Id
 */
@ConfigurationProperties(prefix = "nucleus")
data class NucleusProperties(
    val enabled: Boolean = true,
    val applicationCode: String = "unknown",
    val observability: ObservabilityProperties = ObservabilityProperties()
) {
    data class ObservabilityProperties(
        val enabled: Boolean = true,
        val captureRequestBody: Boolean = false,
        val captureResponseBody: Boolean = false,
        val correlationHeader: String = "X-Correlation-Id",
        val sessionHeader: String = "X-Session-Id"
    )
}

