package com.inokey.solution.dnk.nucleus.annotation

/**
 * Marque une méthode de contrôleur comme une opération Nucleus observable.
 * Le filtre d'observabilité utilise cette annotation pour enrichir les métriques
 * avec le code d'opération, la surface et l'application.
 *
 * @param code Code unique de l'opération (ex: "pertinence.feed.get")
 * @param surface Surface fonctionnelle optionnelle (ex: "FEED", "BANNER")
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class NucleusOp(
    val code: String,
    val surface: String = ""
)

