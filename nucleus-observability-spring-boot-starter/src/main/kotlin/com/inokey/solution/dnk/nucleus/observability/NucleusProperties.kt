package com.inokey.solution.dnk.nucleus.observability

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Propriétés de configuration Nucleus — exposées via application.yml.
 *
 * nucleus:
 *   enabled: true
 *   application-code: pertinence-engine
 *   log-level: DEBUG
 *   latency-budget-ms: 5000
 *   default-safety-score-threshold: 0.80
 *   tracing-enabled: true
 *   metrics-enabled: true
 *   observability:
 *     enabled: true
 *     capture-request-body: false
 *     capture-response-body: false
 *     correlation-header: X-Correlation-Id
 *     session-header: X-Session-Id
 *   guard:
 *     consent-check-enabled: true
 *     safety-check-enabled: false
 *     safety-score-threshold: 0.80
 *     latency-budget-ms: 5000
 *     metrics-enabled: true
 */
@ConfigurationProperties(prefix = "nucleus")
data class NucleusProperties(
    val enabled: Boolean = true,
    val applicationCode: String = "unknown",

    /** Log level for Nucleus components: DEBUG, INFO, WARN, ERROR */
    val logLevel: String = "INFO",

    /** Default latency budget in milliseconds (LatencyBudgetFilter). */
    val latencyBudgetMs: Long = 5000L,

    /** Minimum safety score threshold (SafetyShield). */
    val defaultSafetyScoreThreshold: Double = 0.80,

    /** Enable OpenTelemetry tracing (NucleusOpAspect). */
    val tracingEnabled: Boolean = true,

    /** Enable Micrometer metrics. */
    val metricsEnabled: Boolean = true,

    val observability: ObservabilityProperties = ObservabilityProperties(),
    val guard: GuardProperties = GuardProperties()
) {
    data class ObservabilityProperties(
        val enabled: Boolean = true,
        val captureRequestBody: Boolean = false,
        val captureResponseBody: Boolean = false,
        val correlationHeader: String = "X-Correlation-Id",
        val sessionHeader: String = "X-Session-Id"
    )

    data class GuardProperties(
        val consentCheckEnabled: Boolean = true,
        val safetyCheckEnabled: Boolean = false,
        val safetyScoreThreshold: Double = 0.80,
        val latencyBudgetMs: Long = 5000L,
        val metricsEnabled: Boolean = true
    )
}
