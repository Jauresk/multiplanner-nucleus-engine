package com.inokey.solution.dnk.nucleus.nucleus7

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

/**
 * Stub de filtre INTELLECT : applique une validation schéma JSON sur les endpoints marqués INTELLECT.
 * Implémentation réelle à brancher (lookup schema + validation JSON) plus tard.
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 25)
@Principles(Principle.INTELLECT)
@PrincipleNote("Valide les payloads JSON selon un schéma si INTELLECT est présent")
class JsonSchemaValidationFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val principles = PrincipleResolver.resolve(exchange)
        if (Principle.INTELLECT !in principles) return chain.filter(exchange)
        // TODO: intégrer validation réelle ici
        return chain.filter(exchange)
    }
}

