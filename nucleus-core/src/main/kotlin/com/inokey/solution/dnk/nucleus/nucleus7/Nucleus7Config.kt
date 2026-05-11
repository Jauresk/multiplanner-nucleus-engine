package com.inokey.solution.dnk.nucleus.nucleus7

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(ConsentProperties::class, SafetyProps::class, LatencyProps::class)
class Nucleus7Config {
    @Bean fun consentGuard(p: ConsentProperties, m: MeterRegistry) = ConsentGuard(p, m)
    @Bean fun safetyShield(p: SafetyProps, m: MeterRegistry) = SafetyShield(p, m)
    @Bean fun latencyBudgetFilter(p: LatencyProps, m: MeterRegistry) = LatencyBudgetFilter(p, m)
    @Bean fun jsonSchemaValidationFilter() = JsonSchemaValidationFilter()
}
