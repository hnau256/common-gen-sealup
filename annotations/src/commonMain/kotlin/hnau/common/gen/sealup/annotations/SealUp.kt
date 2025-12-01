package hnau.common.gen.sealup.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class SealUp(
    val variants: Array<Variant>,
    val serializable: Boolean = false,
    val ordinal: Boolean = false,
    val name: Boolean = false,
    val sealedInterfaceName: String = "",
    val wrappedValuePropertyName: String = "value",
    val fold: Boolean = false,
    val factoryMethods: Boolean = false,
)