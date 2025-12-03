package hnau.common.gen.sealup.processor.sealedinfo.generator.variant.override

import com.google.devtools.ksp.symbol.KSAnnotation
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import hnau.common.gen.sealup.processor.sealedinfo.SealedInfo
import hnau.common.kotlin.foldNullable

fun SealedInfo.Override.createFunSpec(
    variant: SealedInfo.Variant,
    type: SealedInfo.Override.Type.Function,
    typeParamResolver: TypeParameterResolver,
    typeVariables: List<TypeVariableName>
): FunSpec = FunSpec
    .builder(name)
    .apply {
        modifiers += KModifier.OVERRIDE
        visibility.toKModifier()?.let { modifiers += it }
        this.typeVariables.addAll(typeVariables)
        annotations.addAll(this@createFunSpec.annotations.map(KSAnnotation::toAnnotationSpec))
        returns(result.toTypeName(typeParamResolver))

        receiver
            ?.toTypeName(typeParamResolver)
            ?.let(::receiver)

        type
            .arguments
            .forEach { argument ->
                addParameter(
                    name = argument.name,
                    type = argument.type.toTypeName(typeParamResolver),
                )
            }

        addStatement(
            receiver.foldNullable(
                ifNull = {
                    "return ${variant.wrappedValuePropertyName}.$name(" to ")"
                },
                ifNotNull = {
                    "return with(${variant.wrappedValuePropertyName}) { $name(" to ") }"
                }
            ).let { (prefix, postfix) ->
                type.arguments.joinToString(
                    prefix = prefix,
                    postfix = postfix,
                    transform = { argument -> argument.name },
                )
            }
        )
    }
    .build()