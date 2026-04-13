package com.inokey.solution.dnk.nucleus.spi

import com.inokey.solution.dnk.nucleus.problem.Problem

/**
 * SPI — Convertit les exceptions applicatives en Problem standardisé.
 * Chaque application peut fournir un mapper pour normaliser ses erreurs métier.
 *
 * Retourne null si l'exception n'est pas gérée par ce mapper
 * (laisse la main au mapper suivant ou au handler par défaut).
 */
interface NucleusErrorMapper {
    fun mapError(ex: Throwable, path: String): Problem?
}

