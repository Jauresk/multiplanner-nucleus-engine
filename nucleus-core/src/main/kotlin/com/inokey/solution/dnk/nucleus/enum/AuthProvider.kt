package com.inokey.solution.dnk.nucleus.enum

/**
 * Fournisseur d'authentification (émetteur du JWT).
 *
 * Dans le contexte MultiPlanner V2 :
 * - KEYCLOAK est toujours l'émetteur du JWT (même pour les logins sociaux)
 * - C'est le "authProvider" stocké dans mp_identities
 */
enum class AuthProvider {
    /**
     * Keycloak émet le JWT (toujours le cas en prod).
     * Même si l'utilisateur se connecte via Google/Apple,
     * c'est Keycloak qui signe le token.
     */
    KEYCLOAK,

    /**
     * Pour les tests uniquement.
     */
    TEST,

    /**
     * Utilisateur anonyme (non authentifié).
     * Utilisé pour le contexte AccessContext.ANONYMOUS.
     */
    ANONYMOUS;

    companion object {
        /**
         * Parse une chaîne vers AuthProvider (case-insensitive).
         * Retourne KEYCLOAK par défaut si la valeur est invalide.
         */
        fun fromString(value: String?): AuthProvider =
            entries.find { it.name.equals(value?.trim(), ignoreCase = true) } ?: KEYCLOAK

        /**
         * Détecte le provider depuis l'issuer URL.
         * En prod : toujours KEYCLOAK si issuer contient "keycloak" ou le realm.
         */
        fun fromIssuer(issuer: String?): AuthProvider {
            if (issuer.isNullOrBlank()) return KEYCLOAK
            return when {
                issuer.contains("keycloak", ignoreCase = true) -> KEYCLOAK
                issuer.contains("/realms/", ignoreCase = true) -> KEYCLOAK
                issuer.contains("test", ignoreCase = true) -> TEST
                else -> KEYCLOAK
            }
        }
    }
}
