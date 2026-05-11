package com.inokey.solution.dnk.nucleus.enum

/**
 * 🌐 Liste des en-têtes HTTP utilisés dans le noyau Nucleus7.
 * Chaque valeur représente un "édonyme" standard de communication entre filtres, contrôleurs et clients.
 */
enum class NucleusHeader(val headerName: String) {

    // 🔹 LIFE – consentement / privacy
    CONSENT_VERSION("X-Consent-Version"),

    // 🔹 ACTION – performance / SLO
    LATENCY_OVERBUDGET("X-Latency-Overbudget"),
    LATENCY_BUDGET("X-Latency-Budget"),

    // 🔹 PROTECTION – sécurité / justice
    SAFETY_SCORE("X-Safety-Score"),
    SECURITY_TOKEN("X-Security-Token"),
    REQUEST_SIGNATURE("X-Request-Signature"),

    // 🔹 INTELLECT – traçabilité / explications
    TRACE_ID("X-Trace-Id"),
    CORRELATION_ID("X-Correlation-Id"),

    // 🔹 LOVE – accessibilité / équité
    USER_CONTEXT("X-User-Context"),

    // 🔹 IMAGINATION – expérimentation / diversité
    EXPERIMENT_ID("X-Experiment-Id"),

    // 🔹 EMOTION – ton / personnalisation
    USER_MOOD("X-User-Mood"),

    // 🔹 VERSIONING – signature versionnelle MultiPlanner
    MULTIPLANNER_VERSION("X-Multiplanner-Version"),
    MULTIPLANNER_MODULE("X-Multiplanner-Module"),
    MULTIPLANNER_VENDOR("X-Multiplanner-Vendor"),
    MULTIPLANNER_TIMESTAMP("X-Multiplanner-Timestamp");

    companion object {
        /** Permet de retrouver un enum à partir du nom du header */
        fun from(name: String?): NucleusHeader? =
            NucleusHeader.entries.find { it.headerName.equals(name, ignoreCase = true) }
    }
}

