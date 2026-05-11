package com.inokey.solution.dnk.nucleus.observability.autoconfigure

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * Service de métriques pour le tracking des quotas et appels APIs externes.
 *
 * Expose les métriques suivantes à Prometheus :
 * - multiplanner_quota_remaining : Quota restant par opération
 * - multiplanner_quota_consumed_total : Total quota consommé (counter)
 * - multiplanner_quota_cost_usd : Coût estimé en USD
 * - multiplanner_external_api_calls_total : Appels APIs externes
 * - multiplanner_external_api_latency_seconds : Latence des appels
 * - multiplanner_llm_tokens_total : Tokens LLM utilisés
 * - multiplanner_circuit_breaker_state : État des circuit breakers
 */
@Component
class QuotaMetricsService(
    private val meterRegistry: MeterRegistry
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // Cache pour les gauges de quota restant (par operation::tier::accountId)
    private val quotaRemainingGauges = ConcurrentHashMap<String, AtomicInteger>()

    // Cache pour les coûts par provider
    private val providerCosts = ConcurrentHashMap<String, AtomicLong>()

    // Cache pour les états circuit breaker
    private val circuitBreakerStates = ConcurrentHashMap<String, AtomicInteger>()

    companion object {
        const val METRIC_PREFIX = "multiplanner"

        // Opérations Découverte (alignées avec OperationCode)
        const val OP_SUGGESTIONS_IA = "PLANIDECOUVERTE__AI_SUGGESTIONS_POST"
        const val OP_METEO = "PLANIDECOUVERTE__METEO_GET"
        const val OP_TRAFIC = "PLANIDECOUVERTE__TRAFIC_GET"
        const val OP_BASIC_SEARCH = "PLANIDECOUVERTE__DISCOVERY_BASIC_SEARCH"

        // Providers
        const val PROVIDER_OPENAI = "openai"
        const val PROVIDER_OPENWEATHERMAP = "openweathermap"
        const val PROVIDER_GOOGLE_MAPS = "google-maps"
        const val PROVIDER_RESEND = "resend"

        // Circuit Breaker States
        const val CB_STATE_CLOSED = 0
        const val CB_STATE_HALF_OPEN = 1
        const val CB_STATE_OPEN = 2
    }

    // =========================================================================
    // QUOTA METRICS
    // =========================================================================

    /**
     * Met à jour le quota restant pour une opération/tier/account.
     */
    fun updateQuotaRemaining(operation: String, tier: String, accountId: Long, remaining: Int) {
        val key = "$operation::$tier::$accountId"
        val gauge = quotaRemainingGauges.computeIfAbsent(key) { _ ->
            val atomicValue = AtomicInteger(remaining)
            Gauge.builder("${METRIC_PREFIX}_quota_remaining", atomicValue) { it.get().toDouble() }
                .tag("operation", operation)
                .tag("tier", tier)
                .tag("account_id", accountId.toString())
                .description("Remaining quota units for operation")
                .register(meterRegistry)
            atomicValue
        }
        gauge.set(remaining)
    }

    /**
     * Incrémente le compteur de quota consommé.
     */
    fun incrementQuotaConsumed(operation: String, tier: String, units: Int) {
        Counter.builder("${METRIC_PREFIX}_quota_consumed_total")
            .tag("operation", operation)
            .tag("tier", tier)
            .description("Total quota units consumed")
            .register(meterRegistry)
            .increment(units.toDouble())
    }

    /**
     * Met à jour le coût estimé pour un provider.
     */
    fun updateProviderCost(provider: String, costUsdCents: Long) {
        val cost = providerCosts.computeIfAbsent(provider) { p ->
            val atomicValue = AtomicLong(0)
            Gauge.builder("${METRIC_PREFIX}_quota_cost_usd", atomicValue) { it.get() / 100.0 }
                .tag("provider", p)
                .description("Estimated cost in USD for external API calls")
                .register(meterRegistry)
            atomicValue
        }
        cost.addAndGet(costUsdCents)
    }

    // =========================================================================
    // EXTERNAL API METRICS
    // =========================================================================

    /**
     * Enregistre un appel API externe.
     */
    fun recordExternalApiCall(
        provider: String,
        operation: String,
        status: ApiCallStatus,
        latencyMs: Long
    ) {
        // Counter pour les appels
        Counter.builder("${METRIC_PREFIX}_external_api_calls_total")
            .tag("provider", provider)
            .tag("operation", operation)
            .tag("status", status.name.lowercase())
            .description("Total external API calls")
            .register(meterRegistry)
            .increment()

        // Timer/Histogram pour la latence
        Timer.builder("${METRIC_PREFIX}_external_api_latency_seconds")
            .tag("provider", provider)
            .tag("operation", operation)
            .description("External API call latency")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry)
            .record(latencyMs, TimeUnit.MILLISECONDS)
    }

    /**
     * Enregistre un appel API météo.
     */
    fun recordMeteoCall(status: ApiCallStatus, latencyMs: Long, cacheHit: Boolean = false) {
        val effectiveStatus = if (cacheHit) ApiCallStatus.CACHED else status
        recordExternalApiCall(PROVIDER_OPENWEATHERMAP, OP_METEO, effectiveStatus, latencyMs)
    }

    /**
     * Enregistre un appel API trafic.
     */
    fun recordTraficCall(status: ApiCallStatus, latencyMs: Long, cacheHit: Boolean = false) {
        val effectiveStatus = if (cacheHit) ApiCallStatus.CACHED else status
        recordExternalApiCall(PROVIDER_GOOGLE_MAPS, OP_TRAFIC, effectiveStatus, latencyMs)
    }

    /**
     * Enregistre un appel LLM.
     */
    fun recordLlmCall(
        model: String,
        status: ApiCallStatus,
        latencyMs: Long,
        inputTokens: Int = 0,
        outputTokens: Int = 0
    ) {
        recordExternalApiCall(PROVIDER_OPENAI, OP_SUGGESTIONS_IA, status, latencyMs)

        // Tokens counter
        if (inputTokens > 0) {
            Counter.builder("${METRIC_PREFIX}_llm_tokens_total")
                .tag("model", model)
                .tag("direction", "input")
                .description("Total LLM tokens used")
                .register(meterRegistry)
                .increment(inputTokens.toDouble())
        }
        if (outputTokens > 0) {
            Counter.builder("${METRIC_PREFIX}_llm_tokens_total")
                .tag("model", model)
                .tag("direction", "output")
                .description("Total LLM tokens used")
                .register(meterRegistry)
                .increment(outputTokens.toDouble())
        }

        // Calculer et enregistrer le coût (GPT-4o-mini pricing)
        // Input: $0.00015 / 1K tokens, Output: $0.0006 / 1K tokens
        val inputCostCents = (inputTokens / 1000.0 * 0.015).toLong()  // $0.00015 * 100
        val outputCostCents = (outputTokens / 1000.0 * 0.06).toLong() // $0.0006 * 100
        if (inputCostCents + outputCostCents > 0) {
            updateProviderCost(PROVIDER_OPENAI, inputCostCents + outputCostCents)
        }
    }

    // =========================================================================
    // CIRCUIT BREAKER METRICS
    // =========================================================================

    /**
     * Met à jour l'état du circuit breaker pour un service.
     */
    fun updateCircuitBreakerState(service: String, state: CircuitBreakerState) {
        val stateGauge = circuitBreakerStates.computeIfAbsent(service) { s ->
            val atomicValue = AtomicInteger(CB_STATE_CLOSED)
            Gauge.builder("${METRIC_PREFIX}_circuit_breaker_state", atomicValue) { it.get().toDouble() }
                .tag("service", s)
                .description("Circuit breaker state (0=closed, 1=half-open, 2=open)")
                .register(meterRegistry)
            atomicValue
        }
        stateGauge.set(state.ordinal)
    }

    // =========================================================================
    // QUOTA SUMMARY
    // =========================================================================

    /**
     * Crée un résumé des quotas pour une opération.
     */
    fun getQuotaSummary(operation: String): QuotaSummary {
        val matchingGauges = quotaRemainingGauges.filterKeys { it.startsWith("$operation::") }
        val totalRemaining = matchingGauges.values.sumOf { it.get() }
        val accountCount = matchingGauges.size

        return QuotaSummary(
            operation = operation,
            totalRemaining = totalRemaining,
            activeAccounts = accountCount
        )
    }

    /**
     * Résumé des quotas.
     */
    data class QuotaSummary(
        val operation: String,
        val totalRemaining: Int,
        val activeAccounts: Int
    )

    /**
     * Status d'un appel API.
     */
    enum class ApiCallStatus {
        SUCCESS,
        ERROR,
        CACHED,
        TIMEOUT,
        CIRCUIT_OPEN
    }

    /**
     * État du circuit breaker.
     */
    enum class CircuitBreakerState {
        CLOSED,
        HALF_OPEN,
        OPEN
    }
}

