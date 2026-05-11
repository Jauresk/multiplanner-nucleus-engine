package com.inokey.solution.dnk.nucleus.observability.autoconfigure

/**
 * Annotation pour marquer une méthode comme une opération métier observable.
 * L'aspect [NucleusOpAspect] interceptera cette annotation pour instrumenter
 * automatiquement le Mono/Flux retourné.
 *
 * @param value L'opération du catalogue [MultiplannerOperation].
 * @param extraTags Tags additionnels au format `["key1=value1", "key2=value2"]`.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class NucleusOp(
    val value: MultiplannerOperation,
    val extraTags: Array<String> = []
)

