package com.inokey.solution.dnk.nucleus.nucleus7

import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.web.method.HandlerMethod
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.server.ServerWebExchange

object PrincipleResolver {
    fun resolve(exchange: ServerWebExchange): Set<Principle> {
        val handler = exchange.getAttribute<Any>(HandlerMapping.BEST_MATCHING_HANDLER_ATTRIBUTE)
        val hm = handler as? HandlerMethod ?: return emptySet()

        val methodSet = AnnotatedElementUtils
            .findMergedAnnotation(hm.method, Principles::class.java)
            ?.value?.toSet().orEmpty()

        val typeSet = AnnotatedElementUtils
            .findMergedAnnotation(hm.beanType, Principles::class.java)
            ?.value?.toSet().orEmpty()

        return typeSet + methodSet
    }
}

