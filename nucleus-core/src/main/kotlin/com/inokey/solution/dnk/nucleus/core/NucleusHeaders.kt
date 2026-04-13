package com.inokey.solution.dnk.nucleus.core

/**
 * Constantes de headers HTTP propagés par Nucleus.
 * Stables — jamais modifiées par les applications consommatrices.
 */
object NucleusHeaders {
    const val CORRELATION_ID = "X-Correlation-Id"
    const val SESSION_ID     = "X-Session-Id"
    const val CONSENT_VERSION = "X-Consent-Version"
    const val IDEMPOTENCY_KEY = "Idempotency-Key"
    const val REQUEST_TIMING  = "X-Request-Timing"
}

