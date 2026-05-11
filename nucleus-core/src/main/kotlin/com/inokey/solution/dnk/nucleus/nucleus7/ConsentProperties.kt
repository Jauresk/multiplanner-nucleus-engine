package com.inokey.solution.dnk.nucleus.nucleus7

import org.springframework.boot.context.properties.ConfigurationProperties
import com.inokey.solution.dnk.nucleus.enum.ConstantHeader

@ConfigurationProperties("nucleus7.consent")
data class ConsentProperties(
    val enabled: Boolean = true,
    val headerName: String = ConstantHeader.CONSENT_VERSION,
    val requiredOnWrite: Boolean = true,
    val whitelistPaths: List<String> = listOf("/actuator/**", "/auth/**")
)

