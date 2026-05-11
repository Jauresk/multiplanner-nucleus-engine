package com.inokey.solution.dnk.nucleus.observability.autoconfigure

import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.stereotype.Component

/**
 * Contribue au endpoint `/actuator/info` en exposant le catalogue des opérations métier.
 *
 * Cela permet de découvrir dynamiquement les métriques et spans potentiels de l'application.
 */
@Component
class NucleusOpsInfoContributor : InfoContributor {
    override fun contribute(builder: Info.Builder) {
        builder.withDetail("nucleus.operations",
            MultiplannerOperation.entries.associate { it.name to mapOf("metric" to it.metricName, "span" to it.spanName) })
    }
}

