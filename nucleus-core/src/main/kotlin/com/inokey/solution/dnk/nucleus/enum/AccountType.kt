package com.inokey.solution.dnk.nucleus.enum

/**
 * Type de compte MultiPlanner.
 *
 * Enum interne (dérivé de l'audience, optionnellement fourni dans la commande).
 *
 * Règles :
 * - PERSON : compte individuel (audience typiquement B2C)
 * - ORGANIZATION : compte entreprise/organisation (audiences B2B/B2G/B2J)
 *
 * Notes :
 * - Ce type n'est pas "source de vérité" sur CREATE : il est dérivé depuis l'audience.
 * - Si une valeur est fournie par le client, elle doit rester cohérente avec l'audience.
 */
enum class AccountType(val code: String) {
    PERSON("PERSON"),
    ORGANIZATION("ORGANIZATION");

    companion object {
        /**
         * Type par défaut lorsque le client ne fournit pas de valeur.
         * IMPORTANT : sur CREATE, la dérivation audience -> accountType reste prioritaire.
         */
        val DEFAULT: AccountType = PERSON

        /**
         * Décode une valeur optionnelle (null/blank -> null).
         * Retourne null si la valeur n'est pas reconnue.
         */
        fun decode(value: String?): AccountType? =
            if (value.isNullOrBlank()) null
            else entries.find {
                it.name.equals(value, ignoreCase = true) ||
                        it.code.equals(value, ignoreCase = true)
            }

        /**
         * Décode ou applique le type par défaut si null/blank ou non reconnu.
         * À utiliser seulement quand le fallback est acceptable.
         */
        fun decodeOrDefault(value: String?, default: AccountType = DEFAULT): AccountType =
            decode(value) ?: default

        /**
         * Décode en mode strict : null/blank -> null, mais valeur inconnue -> exception.
         * Utile quand tu veux distinguer "absent" vs "invalide".
         */
        fun requireValid(value: String?, fieldName: String = "accountType"): AccountType? {
            if (value.isNullOrBlank()) return null
            return decode(value) ?: throw IllegalArgumentException("$fieldName is invalid: '$value'")
        }
    }

    fun isOrganization(): Boolean = this == ORGANIZATION
}
