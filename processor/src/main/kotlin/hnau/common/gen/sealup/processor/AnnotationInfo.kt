package hnau.common.gen.sealup.processor

import hnau.common.gen.sealup.annotations.SealUp
import kotlin.reflect.KClass

internal object AnnotationInfo {

    private val annotationClass: KClass<SealUp> = SealUp::class

    val nameWithPackage: String
        get() = annotationClass.qualifiedName!!

    val simpleName: String
        get() = annotationClass.simpleName!!
}