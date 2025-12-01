package hnau.common.gen.sealup.processor.sealedinfo

import arrow.core.NonEmptyList
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.Visibility

data class SealedInfo(
    val parent: KSClassDeclaration,
    val variants: NonEmptyList<Variant>,
    val serializable: Boolean,
    val ordinal: Boolean,
    val name: Boolean,
    val sealedInterfaceName: String,
    val fold: Boolean,
    val factoryMethods: Boolean,
    val overrides: List<Override>,
) {

    data class Variant(
        val type: KSType,
        val wrapperClassName: String,
        val identifier: String,
        val serialName: String,
        val wrappedValuePropertyName: String,
    ) {

        companion object
    }

    data class Override(
        val name: String,
        val result: KSType,
        val type: Type,
        val visibility: Visibility,
        val receiver: KSType?,
        val typeParameters: List<KSTypeParameter>,
    ) {

        sealed interface Type {

            data class Function(
                val arguments: List<Argument>,
            ) : Type {

                data class Argument(
                    val name: String,
                    val type: KSType,
                )
            }

            data class Property(
                val mutable: Boolean,
            ) : Type
        }

        companion object
    }

    companion object
}