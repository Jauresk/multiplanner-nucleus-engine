package com.inokey.solution.dnk.nucleus.nucleus7

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("nucleus7.safety")
data class SafetyProps(
    val enabled: Boolean = true,
    val minScore: Double = 0.80,
    val enforcePaths: List<String> = emptyList() // nouveaux chemins à toujours vérifier
)
