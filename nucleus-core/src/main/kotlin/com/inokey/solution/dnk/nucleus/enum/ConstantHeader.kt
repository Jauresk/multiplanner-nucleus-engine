package com.inokey.solution.dnk.nucleus.enum

/**
 * 🌐 Constantes globales pour les en-têtes HTTP utilisés dans MultiPlanner.
 *
 * Organisés par les 7 principes Nucleus7 :
 * - LIFE (consentement/vie privée)
 * - ACTION (performance/SLO)
 * - PROTECTION (sécurité/justice)
 * - INTELLECT (traçabilité/explication)
 * - LOVE (accessibilité/équité)
 * - IMAGINATION (expérimentation/diversité)
 * - EMOTION (ton/personnalisation)
 *
 * Plus : VERSIONING (signature versionnelle MultiPlanner)
 *
 * **Utilisation** :
 * ```kotlin
 * @RequestHeader(ConstantHeader.CONSENT_VERSION)
 * request.headers.getFirst(ConstantHeader.CONSENT_VERSION)
 * response.headers.add(ConstantHeader.MULTIPLANNER_VERSION, "V1")
 * ```
 *
 * **Dépreciation** : Voir `NucleusHeader.kt` pour la version legacy enum.
 */
@Suppress("unused")  // Certaines constantes sont futures/optionnelles
object ConstantHeader {

    // ===== LIFE – consent / privacy =====
    const val CONSENT_VERSION: String = "X-Consent-Version"

    // ===== ACTION – performance / SLO =====
    const val LATENCY_OVERBUDGET: String = "X-Latency-Overbudget"
    const val LATENCY_BUDGET: String = "X-Latency-Budget"

    // ===== ACTION/RELIABILITY – idempotency =====
    const val IDEMPOTENCY_KEY: String = "Idempotency-Key"

    // ===== PROTECTION – sécurité / justice =====
    const val SAFETY_SCORE: String = "X-Safety-Score"
    const val SECURITY_TOKEN: String = "X-Security-Token"
    const val REQUEST_SIGNATURE: String = "X-Request-Signature"

    // ===== INTELLECT – traçabilité / explications =====
    const val TRACE_ID: String = "X-Trace-Id"
    const val CORRELATION_ID: String = "X-Correlation-Id"

    // ===== LOVE – accessibilité / équité =====
    const val USER_CONTEXT: String = "X-User-Context"

    // ===== IMAGINATION – expérimentation / diversité =====
    const val EXPERIMENT_ID: String = "X-Experiment-Id"

    // ===== EMOTION – ton / personnalisation =====
    const val USER_MOOD: String = "X-User-Mood"

    // ===== VERSIONING – signature versionnelle MultiPlanner =====
    const val MULTIPLANNER_VERSION: String = "X-Multiplanner-Version"
    const val MULTIPLANNER_MODULE: String = "X-Multiplanner-Module"
    const val MULTIPLANNER_VENDOR: String = "X-Multiplanner-Vendor"
    const val MULTIPLANNER_TIMESTAMP: String = "X-Multiplanner-Timestamp"
}
