package com.inokey.solution.dnk.nucleus.error

import com.fasterxml.jackson.databind.exc.InvalidTypeIdException
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import org.slf4j.LoggerFactory
import org.springframework.core.codec.DecodingException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.server.ServerWebInputException
import reactor.core.publisher.Mono

/**
 * 🔧 Handler spécialisé pour les erreurs de désérialisation JSON / body reading.
 *
 * Responsabilités :
 * - Logger la **vraie cause** côté serveur (stacktrace complète)
 * - Exposer un message **utile mais safe** côté client (Postman/tests)
 * - Éviter de masquer les détails sous un generic "INVALID_INPUT"
 *
 * Ordre d'exécution : ce handler s'exécute **avant** les handlers plus génériques
 * grâce à `@RestControllerAdvice` et l'ordre des `@ExceptionHandler`.
 */
@RestControllerAdvice
class NucleusJsonErrorHandler {

    private val logger = LoggerFactory.getLogger(NucleusJsonErrorHandler::class.java)

    data class ErrorResponse(
        val code: String,
        val message: String,
        val details: List<String> = emptyList()
    )

    /**
     * 🔴 Gère les erreurs de lecture du body HTTP (DecodingException, ServerWebInputException).
     *
     * Ex : JSON mal formé, enum invalide, type non compatible avec le contrat.
     */
    @ExceptionHandler(
        DecodingException::class,
        ServerWebInputException::class
    )
    fun handleDecodingError(ex: Throwable): Mono<ResponseEntity<ErrorResponse>> {
        val root = ex.cause ?: ex
        val message = root.message ?: "Erreur de désérialisation inconnue"

        // 1️⃣ Log complet côté serveur (pour debug)
        logger.error(
            "[JSON_DESERIALIZATION_ERROR] Failed to read HTTP message: $message",
            ex
        )

        // 2️⃣ Extraire un détail utile du message Jackson
        val details = extractJsonErrorDetails(root, message)

        val body = ErrorResponse(
            code = "INVALID_INPUT",
            message = "Failed to read HTTP message",
            details = details
        )

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body))
    }

    /**
     * 🟡 Gère les erreurs de validation (JSR-380 / Jakarta Validation).
     *
     * Ex : @Valid @RequestBody rejette le DTO (champ obligatoire manquant, regex invalide…).
     */
    @ExceptionHandler(WebExchangeBindException::class)
    fun handleValidationError(ex: WebExchangeBindException): Mono<ResponseEntity<ErrorResponse>> {
        // Log côté serveur
        logger.warn(
            "[VALIDATION_ERROR] Validation failed for request body. Errors: {}",
            ex.allErrors.map { error ->
                val fieldName = if (error is org.springframework.validation.FieldError) {
                    error.field
                } else {
                    "global"
                }
                "${error.objectName}.$fieldName: ${error.defaultMessage}"
            }
        )

        // Détails pour le client
        val details = ex.fieldErrors.map { fe ->
            "field='${fe.field}' rejected_value='${fe.rejectedValue}' : ${fe.defaultMessage}"
        }.take(5) // Limite à 5 pour ne pas noyer le client

        val body = ErrorResponse(
            code = "VALIDATION_ERROR",
            message = "Requête invalide",
            details = details
        )

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body))
    }

    // ========== Helpers ==========

    /**
     * 🧬 Extrait un détail utile du message d'erreur Jackson.
     *
     * Cible les types d'erreurs courants :
     * - Unrecognized property (champ extra dans le JSON)
     * - Mismatched input (type ou enum invalide)
     * - Invalid type ID (polymorphic type error)
     */
    private fun extractJsonErrorDetails(root: Throwable, fallbackMessage: String): List<String> {
        return when (root) {
            is UnrecognizedPropertyException -> {
                val propName = root.propertyName
                val availableProps = root.knownPropertyIds?.take(3)?.joinToString(", ") ?: "???"
                listOf(
                    "Propriété inconnue : '$propName'",
                    "Propriétés valides : $availableProps"
                )
            }

            is MismatchedInputException -> {
                val targetType = root.targetType?.simpleName ?: "???"
                @Suppress("DEPRECATION")
                val actualType = root.location?.contentReference()?.rawContent?.javaClass?.simpleName ?: "???"
                listOf(
                    "Type invalide : attendu $targetType, reçu $actualType",
                    "Vous avez peut-être utilisé une mauvaise enum ou un type incompatible"
                )
            }

            is InvalidTypeIdException -> {
                val typeId = root.typeId
                listOf("Type ID invalide pour la sérialisation polymorphe : '$typeId'")
            }

            else -> {
                // Fallback : nettoie le message brut
                val clean = fallbackMessage
                    .replace(Regex("(at line.*|Location.*\\[.*].*|through reference.*)", RegexOption.DOT_MATCHES_ALL), "")
                    .trim()
                if (clean.isNotEmpty()) listOf(clean.take(300)) else listOf("Erreur de parsing JSON")
            }
        }
    }
}

