package com.inokey.solution.dnk.nucleus.annotation

/**
 * 🔖 Marque un contrôleur Spring avec la signature versionnelle de MultiPlanner.
 * Permet de tracer la version d'API et les modules dans les headers HTTP et les logs.
 *
 * Exemple :
 * ```kotlin
 * @MultiPlannerSignature(version = "V1", module = "Nucleus")
 * @RestController
 * @RequestMapping("/nucleus")
 * class NucleusProbeController { ... }
 * ```
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class MultiPlannerSignature(
    /**
     * Version du contrat API (ex: "V1", "V2", "BETA").
     * Par défaut : "V1"
     */
    val version: String = "V1",

    /**
     * Module ou domaine métier (ex: "Nucleus", "Registration", "Planning").
     * Par défaut : "CORE"
     */
    val module: String = "CORE"
)

