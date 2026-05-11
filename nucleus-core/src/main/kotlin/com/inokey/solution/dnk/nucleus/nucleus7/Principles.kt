package com.inokey.solution.dnk.nucleus.nucleus7

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Principles(vararg val value: Principle)

