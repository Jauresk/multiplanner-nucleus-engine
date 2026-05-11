package com.inokey.solution.dnk.nucleus.nucleus7
import com.inokey.solution.dnk.nucleus.enum.ConstantHeader
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.util.UUID
/**
 * Nucleus7 ConsentGuard (WebFilter).
 *
 * Bloque toute requete d'ecriture qui necessite un consentement explicite
 * si l'en-tete X-Consent-Version est manquant ou invalide.
 */
@Component
class ConsentGuardWebFilter(
    private val consentValidator: ConsentVersionValidator
) : WebFilter {
    private val log = LoggerFactory.getLogger(ConsentGuardWebFilter::class.java)
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val request = exchange.request
        val path = request.path.pathWithinApplication().value()
        val method = request.method.name()
        val isWriteEndpointNeedingConsent =
            method == "POST" && path.startsWith("/api/users/register/")
        if (!isWriteEndpointNeedingConsent) {
            return chain.filter(exchange)
        }
        val version = request.headers.getFirst(ConstantHeader.CONSENT_VERSION)
        if (version.isNullOrBlank()) {
            log.warn("[CONSENT_GUARD] Missing {} for {} {}", ConstantHeader.CONSENT_VERSION, method, path)
            return reject(exchange, HttpStatus.BAD_REQUEST, "CONSENT_MISSING")
        }
        if (!consentValidator.isAccepted(version)) {
            log.warn("[CONSENT_GUARD] Invalid consent version='{}' for {} {}", version, method, path)
            return reject(exchange, HttpStatus.FORBIDDEN, "CONSENT_INVALID")
        }
        return chain.filter(exchange)
    }
    private fun reject(
        exchange: ServerWebExchange,
        status: HttpStatus,
        code: String
    ): Mono<Void> {
        val response = exchange.response
        response.statusCode = status
        response.headers.contentType = MediaType.APPLICATION_JSON
        val correlationId =
            exchange.request.headers.getFirst(ConstantHeader.CORRELATION_ID)
                ?: UUID.randomUUID().toString()
        val details = mutableListOf<String>()
        when (code) {
            "CONSENT_MISSING" -> {
                details.add("Header '${ConstantHeader.CONSENT_VERSION}' manquant ou vide.")
                details.add("Action: ajoutez le header avec une version valide (ex: v1.0).")
            }
            "CONSENT_INVALID" -> {
                val received = exchange.request.headers.getFirst(ConstantHeader.CONSENT_VERSION)
                details.add("X-Consent-Version recu: '$received'.")
                details.add("Action: verifiez la version. Version attendue: v1.0.")
            }
            else -> details.add("Requete refusee pour cause de consentement.")
        }
        val message = when (code) {
            "CONSENT_MISSING" -> "Consentement manquant (${ConstantHeader.CONSENT_VERSION})."
            "CONSENT_INVALID" -> "Version de consentement invalide."
            else -> "Requete refusee pour cause de consentement."
        }
        val bufferFactory = response.bufferFactory()
        val detailsJson = details.joinToString(",") { "\"${it.replace("\"", "\\\"")}\"" }
        val bytes = """{"code":"$code","message":"$message","correlationId":"$correlationId","details":[$detailsJson]}"""
            .toByteArray(Charsets.UTF_8)
        val buffer = bufferFactory.wrap(bytes)
        return response.writeWith(Mono.just(buffer))
    }
}
