package hnau.common.gen.sealup.annotations

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
annotation class Variant(
    val type: KClass<*>,
    val wrapperClassName: String = "",
    val identifier: String = "",
    val serialName: String = "",
    val wrappedValuePropertyName: String = "",
)
