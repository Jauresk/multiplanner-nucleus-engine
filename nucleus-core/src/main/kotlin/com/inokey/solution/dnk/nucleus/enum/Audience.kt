package com.inokey.solution.dnk.nucleus.enum

/**
 * Audience cible d'un plan MultiPlanner.
 *
 * Détermine le segment marché pour la résolution des plans.
 *
 * - B2C : Business to Consumer (particuliers)
 * - B2B : Business to Business (entreprises)
 * - B2G : Business to Government (gouvernement/secteur public)
 */
enum class Audience(val code: String) {
    B2C("B2C"),
    B2B("B2B"),
    B2G("B2G"),
    B2J("B2J"),
    ADMIN("ADMIN"),
    PUBLIC("PUBLIC");

    companion object {
        fun decode(value: String?): Audience {
            if (value.isNullOrBlank()) throw IllegalArgumentException("Audience value is blank")
            return Audience.entries.find { it.name.equals(value, ignoreCase = true) || it.code.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown audience: $value")
        }
    }
}
