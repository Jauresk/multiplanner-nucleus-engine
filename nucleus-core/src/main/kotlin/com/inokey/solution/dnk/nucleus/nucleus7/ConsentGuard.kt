package com.inokey.solution.dnk.nucleus.nucleus7

import com.inokey.solution.dnk.nucleus.enum.NucleusHeader
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.HttpStatus
import org.springframework.util.AntPathMatcher
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

private fun ServerWebExchange.getHeader(h: NucleusHeader): String? = request.headers.getFirst(h.headerName)

@Order(Ordered.HIGHEST_PRECEDENCE + 10) // fail fast
@Principles(Principle.LIFE, Principle.PROTECTION)
@PrincipleNote("Bloque les écritures sans consent explicite (X-Consent-Version)")
class ConsentGuard(
    private val props: ConsentProperties,
    meter: MeterRegistry
) : WebFilter {

    private val pm = AntPathMatcher()
    private val missing = meter.counter("nucleus7.consent.missing")
    private val accepted = meter.counter("nucleus7.consent.accepted")

    override fun filter(ex: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        if (!props.enabled) return chain.filter(ex)

        val path = ex.request.path.value()
        if (props.whitelistPaths.any { pm.match(it, path) }) return chain.filter(ex)

        val method = ex.request.method
        val isWrite = method == HttpMethod.POST || method == HttpMethod.PUT ||
                      method == HttpMethod.PATCH || method == HttpMethod.DELETE
        val principles = PrincipleResolver.resolve(ex)
        // Fallback: si les principes ne sont pas encore résolus (pré-mapping), exiger le consent sur les écritures
        val requiresConsent = isWrite && (
            principles.isEmpty() || Principle.LIFE in principles || Principle.PROTECTION in principles
        )
        if (requiresConsent) {
            val header = ex.getHeader(NucleusHeader.CONSENT_VERSION)
            if (header.isNullOrBlank()) {
                missing.increment()
                val resp = ex.response
                resp.statusCode = HttpStatus.BAD_REQUEST
                resp.headers.contentType = MediaType.APPLICATION_JSON
                val body = """
                  {"code":"PRINCIPLE_VIOLATION",
                   "message":"Missing consent header '${NucleusHeader.CONSENT_VERSION.headerName}'",
                   "details":["Provide a valid consent version for write operations."]}
                """.trimIndent()
                val buf = resp.bufferFactory().wrap(body.toByteArray())
                return resp.writeWith(Mono.just(buf))
            }
            accepted.increment()
        }
        return chain.filter(ex)
    }
}
