package com.inokey.solution.dnk.nucleus.enum

/**
 * Effet de policy (ALLOW/DENY).
 */
enum class PolicyEffect {
    ALLOW,
    DENY;

    companion object {
        fun fromString(value: String): PolicyEffect {
            return when (value.uppercase()) {
                "ALLOW" -> ALLOW
                "DENY" -> DENY
                else -> DENY // Default to DENY for unknown values
            }
        }
    }
}
