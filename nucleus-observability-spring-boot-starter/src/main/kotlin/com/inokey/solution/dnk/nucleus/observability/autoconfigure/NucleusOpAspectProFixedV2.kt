package com.inokey.solution.dnk.nucleus.observability.autoconfigure
import io.micrometer.observation.ObservationRegistry
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.stereotype.Component
import reactor.core.observability.micrometer.Micrometer
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.*
/**
 * ASPECT V2 -- Unifie TOUS les tags Micrometer pour eviter les conflits Prometheus.
 *
 * - Recupere endpoint/userId/traceId depuis le Reactor Context (injecte par NucleusWebFilter).
 * - Normalise le contexte (controller/service/repo/unknown).
 * - Compatible 100% avec Prometheus (tags coherents).
 *
 * Tag keys standard (JAMAIS changeantes) :
 *   endpoint, context, user_id, error_type
 */
@Aspect
@Component
class NucleusOpAspectProFixedV2(
    private val observationRegistry: ObservationRegistry
) {
    private val log = LoggerFactory.getLogger(javaClass)
    companion object {
        val STANDARD_TAG_KEYS = setOf("endpoint", "context", "user_id", "error_type")
    }
    @Suppress("ReactiveStreamsUnusedPublisher", "UNCHECKED_CAST")
    @Around("@annotation(com.inokey.solution.dnk.nucleus.observability.autoconfigure.NucleusOp)")
    fun around(pjp: ProceedingJoinPoint): Any {
        val signature = pjp.signature as? org.aspectj.lang.reflect.MethodSignature
            ?: return pjp.proceed()
        val nucleusOp = signature.method.getAnnotation(NucleusOp::class.java)
            ?: return pjp.proceed()
        val op = nucleusOp.value
        val requestId = UUID.randomUUID().toString().substring(0, 8)
        MDC.put("nucleus.requestId", requestId)
        try {
            val contextName = extractContextFromClass(pjp)
            val extraTags = nucleusOp.extraTags
                .mapNotNull {
                    it.split('=').takeIf { parts -> parts.size == 2 }?.let { (k, v) -> k to v }
                }
                .toMap()
                .toMutableMap()
            extraTags["context"] = extraTags["context"] ?: contextName
            extraTags["error_type"] = extraTags["error_type"] ?: "none"
            if (log.isDebugEnabled) {
                log.debug(
                    "[NUCLEUS_OP_V2] requestId={} op={} metricName={} extraTags={}",
                    requestId, op.name, op.metricName, extraTags
                )
            }
            val result = pjp.proceed()
            return when (result) {
                is Mono<*> -> (result as Mono<Any>)
                    .observedProV2(op, observationRegistry, extraTags)
                    .contextWrite { ctx ->
                        ctx.put("nucleus.requestId", requestId)
                            .put("nucleus.op", op.name)
                            .put("nucleus.context", contextName)
                    }
                is Flux<*> -> (result as Flux<Any>)
                    .observedProV2(op, observationRegistry, extraTags)
                    .contextWrite { ctx ->
                        ctx.put("nucleus.requestId", requestId)
                            .put("nucleus.op", op.name)
                            .put("nucleus.context", contextName)
                    }
                else -> result
            }
        } catch (e: Exception) {
            log.error(
                "[NUCLEUS_ASPECT_ERROR] requestId={} op={} error={}",
                requestId,
                op.name,
                e.message,
                e
            )
            throw e
        } finally {
            MDC.remove("nucleus.requestId")
        }
    }
    private fun extractContextFromClass(pjp: ProceedingJoinPoint): String {
        val className = pjp.target::class.simpleName?.lowercase() ?: "unknown"
        return when {
            "controller" in className -> "controller"
            "service" in className -> "service"
            "repository" in className || "repo" in className -> "repo"
            "manager" in className -> "manager"
            else -> "unknown"
        }
    }
}
/**
 * Extension Flux pour observation Micrometer V2.
 * Lit endpoint/user_id depuis le Reactor Context (NucleusWebFilter).
 * Tags stables : endpoint, context, user_id, error_type.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any> Flux<T>.observedProV2(
    op: MultiplannerOperation,
    registry: ObservationRegistry,
    extraTags: Map<String, String> = emptyMap()
): Flux<T> {
    val logger = LoggerFactory.getLogger("NucleusOp")
    return Flux.deferContextual { ctx ->
        val endpoint = ctx.getOrDefault("nucleus.endpoint", "unknown")
            .toString()
            .lowercase()
        val userId = ctx.getOrDefault("nucleus.userId", "anonymous")
            .toString()
        val contextTag = extraTags["context"] ?: "unknown"
        val errorType = extraTags["error_type"] ?: "none"
        val stableTags = mapOf(
            "endpoint" to endpoint,
            "context" to contextTag,
            "user_id" to userId,
            "error_type" to errorType
        ).filterKeys { it in NucleusOpAspectProFixedV2.STANDARD_TAG_KEYS }
        var flux: Flux<T> = this@observedProV2
            .name(op.metricName)
            .tag("module", op.module)
            .tag("op", op.action)
        stableTags.forEach { (k, v) ->
            flux = flux.tag(k, v)
        }
        flux
            .tap(Micrometer.observation(registry))
            .doOnSubscribe {
                logger.debug(
                    "[NUCLEUS_FLUX_START] span={} metric={} tags={}",
                    op.spanName,
                    op.metricName,
                    stableTags
                )
            }
            .doOnError { e ->
                logger.error("[NUCLEUS_FLUX_ERROR] span={} error={}", op.spanName, e.message, e)
            }
            .doFinally { signal ->
                logger.debug("[NUCLEUS_FLUX_END] span={} signal={}", op.spanName, signal)
            }
    }
}
/**
 * Extension Mono pour observation Micrometer V2.
 * Meme logique que Flux, mais pour Mono.
 */
@Suppress("UNCHECKED_CAST")
fun <T : Any> Mono<T>.observedProV2(
    op: MultiplannerOperation,
    registry: ObservationRegistry,
    extraTags: Map<String, String> = emptyMap()
): Mono<T> {
    val logger = LoggerFactory.getLogger("NucleusOp")
    return Mono.deferContextual { ctx ->
        val endpoint = ctx.getOrDefault("nucleus.endpoint", "unknown")
            .toString()
            .lowercase()
        val userId = ctx.getOrDefault("nucleus.userId", "anonymous")
            .toString()
        val contextTag = extraTags["context"] ?: "unknown"
        val errorType = extraTags["error_type"] ?: "none"
        val stableTags = mapOf(
            "endpoint" to endpoint,
            "context" to contextTag,
            "user_id" to userId,
            "error_type" to errorType
        )
        var mono: Mono<T> = this@observedProV2
            .name(op.metricName)
            .tag("module", op.module)
            .tag("op", op.action)
        stableTags.forEach { (k, v) ->
            mono = mono.tag(k, v)
        }
        mono
            .tap(Micrometer.observation(registry))
            .doOnSubscribe {
                logger.debug(
                    "[NUCLEUS_MONO_START] span={} metric={} tags={}",
                    op.spanName,
                    op.metricName,
                    stableTags
                )
            }
            .doOnError { e ->
                logger.error("[NUCLEUS_MONO_ERROR] span={} error={}", op.spanName, e.message, e)
            }
            .doFinally { signal ->
                logger.debug("[NUCLEUS_MONO_END] span={} signal={}", op.spanName, signal)
            }
    }
}
