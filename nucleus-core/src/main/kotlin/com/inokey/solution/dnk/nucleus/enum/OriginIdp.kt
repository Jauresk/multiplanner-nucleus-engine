package com.inokey.solution.dnk.nucleus.enum

/**
 * Fournisseur d'identité d'origine (source réelle de l'authentification).
 *
 * **Différence avec AuthProvider** :
 * - AuthProvider = qui signe le JWT (toujours KEYCLOAK dans notre cas)
 * - OriginIdp = source réelle de l'identité (LOCAL, GOOGLE, APPLE, etc.)
 *
 * **Claim Keycloak** : `identity_provider` (ou `idp`)
 *
 * **Exemples** :
 * - Login email/password → OriginIdp.LOCAL
 * - Login via Google broker → OriginIdp.GOOGLE
 * - Login via Apple broker → OriginIdp.APPLE
 *
 * @author MultiPlanner Team
 */
enum class OriginIdp(
    val displayName: String,
    val keycloakAlias: String
) {
    /**
     * Authentification locale (email + password dans Keycloak).
     */
    LOCAL("Local", "local"),

    /**
     * Authentification via Google Identity.
     */
    GOOGLE("Google", "google"),

    /**
     * Authentification via Apple Sign In.
     */
    APPLE("Apple", "apple"),

    /**
     * Authentification via Facebook.
     */
    FACEBOOK("Facebook", "facebook"),

    /**
     * Authentification via Microsoft (Azure AD / Entra).
     */
    MICROSOFT("Microsoft", "microsoft"),

    /**
     * Authentification via OpenAI (future).
     */
    OPENAI("OpenAI", "openai"),

    /**
     * Fournisseur d'identité inconnu ou non mappé.
     */
    UNKNOWN("Unknown", "unknown");

    companion object {
        /**
         * Résout l'OriginIdp depuis le claim Keycloak `identity_provider`.
         *
         * @param keycloakClaim Valeur du claim `identity_provider` (nullable)
         * @return OriginIdp correspondant (LOCAL si null ou vide)
         */
        fun fromKeycloakClaim(keycloakClaim: String?): OriginIdp {
            if (keycloakClaim.isNullOrBlank()) {
                return LOCAL
            }
            val normalized = keycloakClaim.lowercase().trim()
            return entries.find { it.keycloakAlias == normalized }
                ?: entries.find { it.name.lowercase() == normalized }
                ?: UNKNOWN
        }

        /**
         * Résout l'OriginIdp depuis un nom (enum name ou alias).
         *
         * @param name Nom du fournisseur
         * @return OriginIdp correspondant
         */
        fun fromName(name: String): OriginIdp {
            val normalized = name.uppercase().trim()
            return entries.find { it.name == normalized }
                ?: fromKeycloakClaim(name)
        }

        /**
         * Vérifie si le fournisseur est un broker social (pas LOCAL).
         */
        fun OriginIdp.isSocialBroker(): Boolean =
            this != LOCAL && this != UNKNOWN
    }
}
