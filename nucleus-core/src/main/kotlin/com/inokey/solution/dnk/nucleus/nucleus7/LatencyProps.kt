package com.inokey.solution.dnk.nucleus.nucleus7

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("nucleus7.latency")
data class LatencyProps(
    val enabled: Boolean = true,
    val budgetMs: Long = 250,
    val hardBlock: Boolean = false
)

