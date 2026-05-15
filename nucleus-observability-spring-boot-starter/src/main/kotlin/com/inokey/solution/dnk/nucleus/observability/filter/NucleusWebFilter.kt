package com.inokey.solution.dnk.nucleus.observability.filter

import com.inokey.solution.dnk.nucleus.core.NucleusHeaders
import com.inokey.solution.dnk.nucleus.observability.NucleusProperties
import com.inokey.solution.dnk.nucleus.spi.NucleusObservationContributor
import com.inokey.solution.dnk.nucleus.spi.NucleusOperationResolver
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.Ordered
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.util.UUID

/**
 * WebFilter réactif Nucleus — intercepte chaque requête HTTP pour :
 *
 * 1. Propager / générer le X-Correlation-Id
 * 2. Injecter le correlation id dans le MDC (logs structurés)
 * 3. Résoudre le code d'opération Nucleus (SPI)
 * 4. Enrichir les tags d'observabilité (SPI)
 * 5. Mesurer le temps de traitement
 * 6. Ajouter X-Request-Timing dans la réponse
 */
class NucleusWebFilter(
    private val properties: NucleusProperties,
    private val operationResolver: NucleusOperationResolver?,
    private val contributors: List<NucleusObservationContributor>
) : WebFilter, Ordered {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE + 10

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        if (!properties.observability.enabled) {
            return chain.filter(exchange)
        }

        val request = exchange.request
        val start = System.nanoTime()

        // 1. Correlation ID : propager ou générer
        val correlationId = request.headers.getFirst(properties.observability.correlationHeader)
            ?: UUID.randomUUID().toString()

        // 2. MDC pour les logs
        MDC.put("correlationId", correlationId)
        MDC.put("application", properties.applicationCode)

        // 3. Résoudre l'opération
        val path = request.uri.path
        val method = request.method.name()
        val operation = operationResolver?.resolve(path, method) ?: "unknown"
        MDC.put("operation", operation)

        // 4. Enrichir les tags
        val tags = mutableMapOf(
            "app" to properties.applicationCode,
            "operation" to operation,
            "method" to method,
            "path" to path
        )
        val queryParams = request.queryParams.toSingleValueMap()
        contributors.forEach { it.contribute(tags, path, method, queryParams) }

        // 5. Injecter le correlation id dans la requête si absent
        val mutatedRequest: ServerHttpRequest = exchange.request.mutate()
            .header(NucleusHeaders.CORRELATION_ID, correlationId)
            .build()

        val mutatedExchange = exchange.mutate().request(mutatedRequest).build()

        // 6. Enregistrer beforeCommit pour écrire les headers AVANT que la réponse soit verrouillée.
        //    Ne jamais écrire des headers dans doFinally (ReadOnlyHttpHeaders après commit).
        //    try-catch défensif : Spring 7 peut verrouiller les headers sur certains chemins d'erreur
        //    (DefaultErrorWebExceptionHandler, ServerResponse wrappers) même à l'intérieur de beforeCommit.
        mutatedExchange.response.beforeCommit {
            val elapsedMs = (System.nanoTime() - start) / 1_000_000
            try {
                mutatedExchange.response.headers.set(NucleusHeaders.REQUEST_TIMING, "${elapsedMs}ms")
                // Propager le correlation ID dans la réponse pour le client
                if (mutatedExchange.response.headers[NucleusHeaders.CORRELATION_ID] == null) {
                    mutatedExchange.response.headers.set(NucleusHeaders.CORRELATION_ID, correlationId)
                }
            } catch (e: UnsupportedOperationException) {
                log.trace(
                    "Nucleus: impossible d'injecter les headers de réponse — déjà verrouillés [{} {} {}ms] — {}",
                    method, path, elapsedMs, e.message
                )
            }
            Mono.empty()
        }

        return chain.filter(mutatedExchange)
            .doFinally {
                // doFinally = logs + MDC cleanup uniquement, jamais de mutation de headers
                val elapsedMs = (System.nanoTime() - start) / 1_000_000

                log.debug(
                    "Nucleus [{}] {} {} — {}ms — tags={}",
                    operation, method, path, elapsedMs, tags
                )
                MDC.clear()
            }
    }
}

