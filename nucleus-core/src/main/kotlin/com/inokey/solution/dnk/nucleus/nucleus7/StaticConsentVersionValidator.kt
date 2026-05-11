package com.inokey.solution.dnk.nucleus.nucleus7

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * 🔐 Implémentation simple du validateur de consentement.
 *
 * Valide que la version fournie correspond exactement à celle configurée.
 * La version requise est lue depuis `application.yml` :
 *
 * ```yaml
 * multiplanner:
 *   consent:
 *     required-version: v1.0
 * ```
 *
 * Si le client envoie une autre version, elle est rejetée.
 * Pour un système plus avancé (historique, migration de versions, etc.),
 * remplace cette implémentation.
 */
@Component
class StaticConsentVersionValidator(
    @Value("\${multiplanner.consent.required-version}") private val required: String
) : ConsentVersionValidator {

    /**
     * Vérifie que la version fournie égale la version requise (après trim).
     *
     * @param version version envoyée par le client (ex: "v1.0")
     * @return true si version.trim() == required.trim()
     */
    override fun isAccepted(version: String): Boolean =
        version.trim() == required.trim()
}
