package com.inokey.solution.dnk.nucleus.nucleus7

/**
 * 🔐 Interface de validation de la version de consentement Nucleus7.
 *
 * Responsable de vérifier qu'une version de consentement fournie par le client
 * est acceptée par le serveur.
 *
 * Utilisation :
 * - ConsentGuard l'injecte pour bloquer les requêtes de modification sans consentement valide.
 * - Peut être remplacée par une implémentation custom (ex: BD, cache, etc.)
 */
interface ConsentVersionValidator {
    /**
     * Vérifie si une version de consentement est acceptée.
     *
     * @param version version du consentement (ex: "v1.0", "v1.1")
     * @return true si la version est acceptée, false sinon
     */
    fun isAccepted(version: String): Boolean
}
