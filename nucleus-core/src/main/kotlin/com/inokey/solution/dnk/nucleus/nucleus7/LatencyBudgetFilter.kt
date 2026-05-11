package com.inokey.solution.dnk.nucleus.nucleus7

import com.inokey.solution.dnk.nucleus.enum.NucleusHeader
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Order(Ordered.HIGHEST_PRECEDENCE + 20)
@Principles(Principle.ACTION, Principle.PROTECTION)
@PrincipleNote("Mesure la latence et marque les dépassements de budget (performance + protection SLO)")
class LatencyBudgetFilter(
    private val props: LatencyProps,
    meter: MeterRegistry
) : WebFilter {

    private val over = meter.counter("nucleus7.latency.overbudget")

    override fun filter(ex: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        if (!props.enabled) return chain.filter(ex)
        val start = System.nanoTime()

        ex.response.beforeCommit {
            val ms = (System.nanoTime() - start) / 1_000_000
            // Résolution des principes au moment du commit, quand le handler est connu
            val principles = PrincipleResolver.resolve(ex)
            if (Principle.ACTION in principles && ms > props.budgetMs) {
                over.increment()
                ex.response.headers.add(NucleusHeader.LATENCY_OVERBUDGET.headerName, "true")
                if (props.hardBlock) {
                    ex.response.statusCode = HttpStatus.SERVICE_UNAVAILABLE
                }
            }
            Mono.empty()
        }
        return chain.filter(ex)
    }
}
