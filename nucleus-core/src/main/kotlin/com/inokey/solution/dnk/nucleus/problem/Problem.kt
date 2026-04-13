package com.inokey.solution.dnk.nucleus.problem

import java.time.Instant
import java.util.UUID

/**
 * Modèle d'erreur standardisé pour toutes les applications MultiPlanner.
 * Compatible RFC 9457 (Problem Details for HTTP APIs).
 */
data class Problem(
    val code: String,
    val message: String,
    val timestamp: Instant = Instant.now(),
    val path: String = "",
    val errorId: String = UUID.randomUUID().toString(),
    val details: List<Map<String, Any?>> = emptyList()
)

