package com.inokey.solution.dnk.nucleus.nucleus7

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.ApplicationContext

@ConditionalOnClass(RequestMappingHandlerMapping::class)
@Component
class PrincipleRegistry(
    private val applicationContext: ApplicationContext,
    private val meterRegistry: MeterRegistry
) {
    // Récupère TOUS les RequestMappingHandlerMapping disponibles via le contexte (évite l'injection ambiguë)
    private val mappingsList: List<RequestMappingHandlerMapping> by lazy {
        applicationContext.getBeansOfType(RequestMappingHandlerMapping::class.java).values.toList()
    }

    @Volatile private var cache: Map<Principle, Set<String>> = emptyMap()
    @Volatile private var lastRefresh: Instant? = null
    private val ttlSeconds = 60L

    private val endpointGauges = ConcurrentHashMap<Principle, AtomicInteger>()
    private val totalGaugeRef = AtomicInteger(0)

    init {
        Gauge.builder("nucleus7.principle.total.count", totalGaugeRef) { it.get().toDouble() }
            .register(meterRegistry)
    }

    fun rebuild(): Map<Principle, Set<String>> {
        val result = mutableMapOf<Principle, MutableSet<String>>()

        // Agrège tous les mappings disponibles (incluant actuator). On filtre nos contrôleurs applicatifs par package.
        mappingsList.forEach { mapping ->
            mapping.handlerMethods.forEach handlerLoop@{ (info, method) ->
                val beanPackage = method.beanType.packageName
                if (!beanPackage.startsWith("com.inokey.solution.dnk.multiplanner")) return@handlerLoop

                val principles = extractPrinciples(method)
                if (principles.isNotEmpty()) {
                    val patterns = extractPatterns(info)
                    if (patterns.isNotEmpty()) {
                        principles.forEach { p -> result.getOrPut(p) { mutableSetOf() }.addAll(patterns) }
                    }
                }
            }
        }

        cache = result.mapValues { it.value.toSortedSet() }
        lastRefresh = Instant.now()
        publishMetrics(cache)
        return cache
    }

    fun get(): Map<Principle, Set<String>> {
        val now = Instant.now()
        val expired = lastRefresh == null || lastRefresh!!.plusSeconds(ttlSeconds).isBefore(now)
        return if (cache.isEmpty() || expired) rebuild() else cache
    }

    fun info(): Map<String, Any?> = mapOf(
        "lastRefresh" to lastRefresh,
        "ttlSeconds" to ttlSeconds,
        "count" to cache.values.sumOf { it.size }
    )

    private fun publishMetrics(data: Map<Principle, Set<String>>) {
        data.forEach { (p, urls) ->
            val ref = endpointGauges.computeIfAbsent(p) {
                val holder = AtomicInteger(0)
                Gauge.builder("nucleus7.principle.endpoint.count", holder) { v -> v.get().toDouble() }
                    .tag("principle", p.name)
                    .register(meterRegistry)
                holder
            }
            ref.set(urls.size)
        }
        endpointGauges.keys.filter { it !in data.keys }.forEach { endpointGauges[it]?.set(0) }
        totalGaugeRef.set(data.values.sumOf { it.size })
    }

    private fun extractPrinciples(method: HandlerMethod): List<Principle> {
        val methodAnn = AnnotatedElementUtils
            .findMergedAnnotation(method.method, Principles::class.java)?.value?.toList().orEmpty()
        val typeAnn = AnnotatedElementUtils
            .findMergedAnnotation(method.beanType, Principles::class.java)?.value?.toList().orEmpty()
        return (typeAnn + methodAnn).distinct()
    }

    // Récupère les patterns en supportant PathPattern (Spring 6) et ANT (fallback) sans référence directe
    private fun extractPatterns(info: Any): List<String> {
        val pathPatterns = runCatching {
            val cond = info.javaClass.getMethod("getPathPatternsCondition").invoke(info)
            if (cond != null) {
                val coll = cond.javaClass.getMethod("getPatterns").invoke(cond) as? Collection<*>
                coll?.map { it.toString() }
            } else null
        }.getOrNull()
        if (!pathPatterns.isNullOrEmpty()) return pathPatterns

        val antPatterns = runCatching {
            val cond = info.javaClass.getMethod("getPatternsCondition").invoke(info)
            if (cond != null) {
                val coll = cond.javaClass.getMethod("getPatterns").invoke(cond) as? Collection<*>
                coll?.map { it.toString() }
            } else null
        }.getOrNull()
        return antPatterns ?: emptyList()
    }
}
