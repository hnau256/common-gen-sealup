package hnau.common.gen.sealup.processor.sealedinfo.generator.variant.override

import com.google.devtools.ksp.symbol.KSAnnotation
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import hnau.common.gen.sealup.processor.sealedinfo.SealedInfo
import hnau.common.gen.sealup.processor.sealedinfo.generator.utils.SealInfoCodeGeneratorConstants
import hnau.common.kotlin.foldNullable

fun SealedInfo.Override.createPropertySpec(
    variant: SealedInfo.Variant,
    type: SealedInfo.Override.Type.Property,
    typeParamResolver: TypeParameterResolver,
    typeVariables: List<TypeVariableName>,
): PropertySpec {
    val typeName = result.toTypeName(typeParamResolver)
    return PropertySpec
        .builder(
            name = name,
            type = typeName,
        )
        .apply {
            modifiers += KModifier.OVERRIDE
            visibility.toKModifier()?.let { modifiers += it }
            this.typeVariables.addAll(typeVariables)
            annotations.addAll(this@createPropertySpec.annotations.map(KSAnnotation::toAnnotationSpec))
            mutable(type.mutable)

            receiver
                ?.toTypeName(typeParamResolver)
                ?.let(::receiver)

            getter(
                FunSpec
                    .getterBuilder()
                    .addStatement(
                        receiver.foldNullable(
                            ifNull = { "return ${variant.wrappedValuePropertyName}.$name" },
                            ifNotNull = { "return with(${variant.wrappedValuePropertyName}) { $name }" }
                        )
                    )
                    .build()
            )

            if (type.mutable) {
                setter(
                    FunSpec
                        .setterBuilder()
                        .addParameter("newValue", typeName)
                        .addStatement(
                            receiver.foldNullable(
                                ifNull = { "${variant.wrappedValuePropertyName}.$name = ${SealInfoCodeGeneratorConstants.setterParameterName}" },
                                ifNotNull = { "with(${variant.wrappedValuePropertyName}) { $name = ${SealInfoCodeGeneratorConstants.setterParameterName} }" }
                            )
                        )
                        .build()
                )
            }

        }
        .build()
}