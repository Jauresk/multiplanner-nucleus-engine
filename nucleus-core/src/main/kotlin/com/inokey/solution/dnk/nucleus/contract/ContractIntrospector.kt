package com.inokey.solution.dnk.nucleus.contract

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.core.io.support.ResourcePatternResolver
import org.springframework.core.type.classreading.CachingMetadataReaderFactory
import org.springframework.core.type.classreading.MetadataReaderFactory
import org.springframework.stereotype.Component
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

/**
 * Décrit un paramètre d'une data class du contrat (requis/optionnel).
 */
data class ContractParamInfo(
    val name: String,
    val type: String,
    val nullable: Boolean,
    val hasDefault: Boolean,
    val required: Boolean  // true si pas de valeur par défaut
)

/**
 * Décrit une data class du contrat (modèle OpenAPI généré).
 */
data class ContractClassInfo(
    val name: String,
    val qualifiedName: String,
    val required: List<String>,
    val optional: List<String>,
    val params: List<ContractParamInfo>
)

/**
 * 🌐 Introspection du contrat OpenAPI — détecte les modèles (data classes)
 * et inférence des champs requis/optionnels.
 *
 * Règle: un paramètre est **requis** s'il n'a pas de valeur par défaut
 * dans le primary constructor.
 */
@Component
class ContractIntrospector(
    @Value("\${multiplanner.contract.model-package:com.inokey.solution.dnk.multiplanner.contract.model}")
    private val modelPackage: String
) {

    @Volatile private var cached: List<ContractClassInfo>? = null
    @Volatile private var scanError: Throwable? = null

    /**
     * Retourne un snapshot du contrat (count, classes, champs requis/optionnels).
     * Si un erreur de scan a été rencontrée, retourne un snapshot vide + erreur.
     */
    fun snapshot(): Map<String, Any> {
        // Si une erreur a déjà été enregistrée, retourne un snapshot vide
        if (scanError != null) {
            return mapOf(
                "package" to modelPackage,
                "count" to 0,
                "classes" to emptyList<ContractClassInfo>(),
                "error" to (scanError?.message ?: "Unknown error during model scanning")
            )
        }

        val classes = cached ?: runCatching { scan() }
            .onFailure { scanError = it }
            .getOrElse { emptyList() }
            .also { cached = it }

        return mapOf(
            "package" to modelPackage,
            "count" to classes.size,
            "classes" to classes
        )
    }

    /**
     * Scanne le package des modèles et infère la requiritude des paramètres.
     */
    private fun scan(): List<ContractClassInfo> {
        val resolver: ResourcePatternResolver = PathMatchingResourcePatternResolver()
        val factory: MetadataReaderFactory = CachingMetadataReaderFactory(resolver)
        val pattern = "classpath*:${modelPackage.replace('.', '/')}/**/*.class"

        val resources = try {
            resolver.getResources(pattern)
        } catch (e: Exception) {
            emptyArray()
        }

        val out = mutableListOf<ContractClassInfo>()

        resources.forEach { res ->
            if (!res.isReadable) return@forEach

            val metadata = try {
                factory.getMetadataReader(res)
            } catch (e: Exception) {
                return@forEach
            }

            val className = metadata.classMetadata.className

            // Charge la classe via reflection
            val kClass = runCatching {
                Class.forName(className).kotlin
            }.getOrNull() ?: return@forEach

            // Ne retient que les data classes (modèles OpenAPI générés)
            if (!kClass.isData) return@forEach

            val ctor = kClass.primaryConstructor ?: return@forEach

            // Décrit chaque paramètre du constructor
            val params = ctor.parameters
                .filter { it.kind == KParameter.Kind.VALUE }
                .map { p ->
                    val name = p.name ?: "_"
                    val typeStr = p.type.toString()
                    val nullable = p.type.isMarkedNullable
                    val hasDefault = p.isOptional  // true si valeur par défaut
                    val required = !hasDefault     // requis = pas de défaut

                    ContractParamInfo(name, typeStr, nullable, hasDefault, required)
                }

            val required = params.filter { it.required }.map { it.name }
            val optional = params.filter { !it.required }.map { it.name }

            out += ContractClassInfo(
                name = kClass.simpleName ?: className.substringAfterLast('.'),
                qualifiedName = kClass.qualifiedName ?: className,
                required = required,
                optional = optional,
                params = params
            )
        }

        return out.sortedBy { it.name }
    }
}

