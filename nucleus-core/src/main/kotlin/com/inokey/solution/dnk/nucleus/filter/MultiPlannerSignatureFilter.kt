package com.inokey.solution.dnk.nucleus.filter

import com.inokey.solution.dnk.nucleus.annotation.MultiPlannerSignature
import com.inokey.solution.dnk.nucleus.enum.ConstantHeader
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * 🌐 Filtre Web réactif qui injecte automatiquement les headers de signature MultiPlanner
 * dans chaque réponse HTTP, basé sur l'annotation @MultiPlannerSignature du contrôleur.
 *
 * Headers injectés :
 * - X-Multiplanner-Version (ex: "V1")
 * - X-Multiplanner-Module (ex: "Nucleus")
 * - X-Multiplanner-Vendor (toujours "INOKEY-SOLUTION-DNK")
 * - X-Multiplanner-Timestamp (ISO-8601)
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100) // Après les filtres de sécurité Nucleus
class MultiPlannerSignatureFilter : WebFilter {

    private val vendor = "INOKEY-SOLUTION-DNK"

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return chain.filter(exchange).doFinally {
            // Injecte les headers APRÈS que la chaîne de filtres ait traité la requête
            // (mais avant que la réponse soit commitée, si possible)
            val headers = exchange.response.headers

            // Récupère la signature du contrôleur (via attribut stocké dans le handler)
            val handlerMethod = exchange.getAttribute<Any>("org.springframework.web.method.HandlerMethod")
            if (handlerMethod is org.springframework.web.method.HandlerMethod) {
                val signature = handlerMethod.beanType.getAnnotation(MultiPlannerSignature::class.java)
                if (signature != null) {
                    headers.set(ConstantHeader.MULTIPLANNER_VERSION, signature.version)
                    headers.set(ConstantHeader.MULTIPLANNER_MODULE, signature.module)
                    headers.set(ConstantHeader.MULTIPLANNER_VENDOR, vendor)
                    headers.set(ConstantHeader.MULTIPLANNER_TIMESTAMP, Instant.now().toString())
                } else {
                    // Signature par défaut pour les endpoints non-annotés
                    headers.set(ConstantHeader.MULTIPLANNER_VERSION, "UNSPECIFIED")
                    headers.set(ConstantHeader.MULTIPLANNER_VENDOR, vendor)
                }
            }
        }
    }
}

