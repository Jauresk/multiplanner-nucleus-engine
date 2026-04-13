package com.inokey.solution.dnk.nucleus.spi

/**
 * SPI — Contribue des tags/dimensions d'observabilité supplémentaires
 * à chaque requête HTTP interceptée.
 *
 * Chaque application fournit son implémentation pour ajouter
 * des dimensions métier (surface, city, candidateType, placement…).
 */
interface NucleusObservationContributor {

    /**
     * Enrichit le contexte d'observation avec des dimensions applicatives.
     *
     * @param context map mutable de tags existants
     * @param path chemin HTTP de la requête
     * @param method méthode HTTP (GET, POST…)
     * @param queryParams paramètres de requête bruts
     */
    fun contribute(
        context: MutableMap<String, String>,
        path: String,
        method: String,
        queryParams: Map<String, String> = emptyMap()
    )
}

