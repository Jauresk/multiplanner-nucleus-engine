package com.inokey.solution.dnk.nucleus.nucleus7

import com.inokey.solution.dnk.nucleus.enum.NucleusHeader
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.util.AntPathMatcher
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Order(Ordered.HIGHEST_PRECEDENCE + 15)
@Principles(Principle.PROTECTION, Principle.LIFE)
@PrincipleNote("Bloque les requêtes si le score de sécurité est inférieur au seuil configuré")
class SafetyShield(
    private val props: SafetyProps,
    meter: MeterRegistry
) : WebFilter {

    private val blocked = meter.counter("nucleus7.safety.blocked")
    private val pm = AntPathMatcher()

    override fun filter(ex: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        if (!props.enabled) return chain.filter(ex)

        val path = ex.request.path.value()
        val enforceByPath = props.enforcePaths.any { pm.match(it, path) }

        val principles = PrincipleResolver.resolve(ex)
        val shouldEnforce = enforceByPath || (Principle.EMOTION in principles || Principle.LOVE in principles)
        val scoreHeader = ex.request.headers.getFirst(NucleusHeader.SAFETY_SCORE.headerName)
        val score = scoreHeader?.toDoubleOrNull()

        if (shouldEnforce && (score == null || score < props.minScore)) {
            blocked.increment()
            val resp = ex.response
            resp.statusCode = HttpStatus.FORBIDDEN
            resp.headers.contentType = MediaType.APPLICATION_JSON
            val body = """{"code":"SAFETY_BLOCKED","message":"Safety score ${score ?: "null"} < ${props.minScore}"}"""
            val buf = resp.bufferFactory().wrap(body.toByteArray())
            return resp.writeWith(Mono.just(buf))
        }
        return chain.filter(ex)
    }
}
