package com.inokey.solution.dnk.nucleus.observability.autoconfigure
import com.inokey.solution.dnk.nucleus.observability.NucleusProperties
import com.inokey.solution.dnk.nucleus.observability.filter.NucleusWebFilter
import com.inokey.solution.dnk.nucleus.spi.NucleusObservationContributor
import com.inokey.solution.dnk.nucleus.spi.NucleusOperationResolver
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.observation.ObservationRegistry
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
/**
 * Auto-configuration Spring Boot pour Nucleus Observability.
 *
 * Active automatiquement :
 *   - NucleusWebFilter (propagation correlationId / Reactor Context)
 *   - NucleusOpAspectProFixedV2 (AOP instrumentation Mono/Flux)
 *   - NucleusOpsInfoContributor (actuator /info)
 *   - QuotaMetricsService (metriques quotas/providers/LLM)
 */
@Configuration
@EnableAspectJAutoProxy
@ConditionalOnProperty(prefix = "nucleus", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
@EnableConfigurationProperties(NucleusProperties::class)
class NucleusObservabilityAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    fun nucleusWebFilter(
        properties: NucleusProperties,
        operationResolver: NucleusOperationResolver?,
        contributors: List<NucleusObservationContributor>
    ): NucleusWebFilter {
        return NucleusWebFilter(properties, operationResolver, contributors)
    }
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = ["io.micrometer.observation.ObservationRegistry"])
    fun nucleusOpAspect(observationRegistry: ObservationRegistry): NucleusOpAspectProFixedV2 {
        return NucleusOpAspectProFixedV2(observationRegistry)
    }
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(name = ["org.springframework.boot.actuate.info.InfoContributor"])
    fun nucleusOpsInfoContributor(): NucleusOpsInfoContributor {
        return NucleusOpsInfoContributor()
    }
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(MeterRegistry::class)
    fun quotaMetricsService(meterRegistry: MeterRegistry): QuotaMetricsService {
        return QuotaMetricsService(meterRegistry)
    }
}
