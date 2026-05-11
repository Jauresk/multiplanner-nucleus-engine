package com.inokey.solution.dnk.nucleus.logging

import com.inokey.solution.dnk.nucleus.annotation.MultiPlannerSignature
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.getBeansOfType
import org.springframework.context.ApplicationContext
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping

/**
 * 🔍 Logger des signatures MultiPlanner au démarrage.
 * Récupère tous les RequestMappingHandlerMapping depuis le contexte pour éviter
 * toute ambiguïté (actuator ajoute aussi ses propres mappings).
 */
@Component
class MultiPlannerSignatureLogger(
    private val applicationContext: ApplicationContext
) {

    private val logger = LoggerFactory.getLogger(javaClass)

    @EventListener
    @Suppress("UNUSED_PARAMETER")
    fun onApplicationStart(event: ContextRefreshedEvent) {
        val mappings: Collection<RequestMappingHandlerMapping> =
            applicationContext.getBeansOfType<RequestMappingHandlerMapping>().values
        if (mappings.isEmpty()) return

        val signatures = mutableMapOf<String, MutableList<String>>()

        mappings.forEach { handlerMapping ->
            handlerMapping.handlerMethods.forEach { (info, handlerMethod) ->
                val signature = extractSignature(handlerMethod)
                val key = "${signature.version} | ${signature.module}"
                val patterns = extractPatterns(info)
                if (patterns.isNotEmpty()) signatures.getOrPut(key) { mutableListOf() }.addAll(patterns)
            }
        }

        if (signatures.isEmpty()) return

        logger.info(
            "\n" +
            "╔════════════════════════════════════════════════════════════════════════╗\n" +
            "║          🔖 MULTIPLANNER SIGNATURE REGISTRY — API VERSIONING          ║\n" +
            "╠════════════════════════════════════════════════════════════════════════╣\n"
        )
        signatures.forEach { (key, endpoints) ->
            logger.info("║ $key")
            endpoints.forEach { endpoint -> logger.info("║   └─ $endpoint") }
        }
        logger.info("╚════════════════════════════════════════════════════════════════════════╝\n")
    }

    private fun extractSignature(handlerMethod: HandlerMethod): SignatureTuple =
        handlerMethod.beanType.getAnnotation(MultiPlannerSignature::class.java)
            ?.let { SignatureTuple(it.version, it.module) }
            ?: SignatureTuple("UNSPECIFIED", "UNSPECIFIED")

    /**
     * Supporte PathPattern (Spring 6) avec fallback ANT (Spring 5) via réflexion sans dépendance directe.
     */
    private fun extractPatterns(info: Any): List<String> {
        // 1) PathPatternsCondition
        runCatching {
            val m = info.javaClass.getMethod("getPathPatternsCondition")
            val cond = m.invoke(info)
            val res = patternsFrom(cond)
            if (res.isNotEmpty()) return res
        }
        // 2) PatternsRequestCondition (ANT)
        runCatching {
            val m = info.javaClass.getMethod("getPatternsCondition")
            val cond = m.invoke(info)
            val res = patternsFrom(cond)
            if (res.isNotEmpty()) return res
        }
        return emptyList()
    }

    private fun patternsFrom(cond: Any?): List<String> =
        if (cond == null) emptyList()
        else runCatching {
            val pm = cond.javaClass.getMethod("getPatterns")
            val coll = pm.invoke(cond) as? Collection<*>
            coll?.map { it.toString() } ?: emptyList()
        }.getOrDefault(emptyList())

    private data class SignatureTuple(val version: String, val module: String)
}
