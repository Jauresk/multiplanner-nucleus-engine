package com.inokey.solution.dnk.nucleus.spi

/**
 * SPI — Résout le code d'opération Nucleus à partir du chemin et de la méthode HTTP.
 * Chaque application fournit son implémentation.
 *
 * Exemple :
 *   path="/internal/feed", method="GET" → "pertinence.feed.get"
 */
interface NucleusOperationResolver {
    fun resolve(path: String, method: String): String?
}

