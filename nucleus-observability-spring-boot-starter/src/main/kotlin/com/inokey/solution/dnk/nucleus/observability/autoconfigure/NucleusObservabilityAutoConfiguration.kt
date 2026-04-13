package com.inokey.solution.dnk.nucleus.observability.autoconfigure

import com.inokey.solution.dnk.nucleus.observability.NucleusProperties
import com.inokey.solution.dnk.nucleus.observability.filter.NucleusWebFilter
import com.inokey.solution.dnk.nucleus.spi.NucleusObservationContributor
import com.inokey.solution.dnk.nucleus.spi.NucleusOperationResolver
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Auto-configuration Spring Boot pour Nucleus Observability.
 * Active automatiquement le WebFilter si nucleus.enabled=true.
 *
 * Les beans NucleusOperationResolver et NucleusObservationContributor
 * sont injectés depuis l'application consommatrice (SPI).
 */
@Configuration
@ConditionalOnProperty(prefix = "nucleus", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@EnableConfigurationProperties(NucleusProperties::class)
class NucleusObservabilityAutoConfiguration {

    @Bean
    fun nucleusWebFilter(
        properties: NucleusProperties,
        operationResolver: NucleusOperationResolver?,
        contributors: List<NucleusObservationContributor>
    ): NucleusWebFilter {
        return NucleusWebFilter(properties, operationResolver, contributors)
    }
}

