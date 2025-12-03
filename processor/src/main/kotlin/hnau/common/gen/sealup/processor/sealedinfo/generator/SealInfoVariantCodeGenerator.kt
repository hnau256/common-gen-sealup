package hnau.common.gen.sealup.processor.sealedinfo.generator

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.devtools.ksp.symbol.KSAnnotation
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toAnnotationSpec
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import hnau.common.gen.sealup.processor.sealedinfo.SealedInfo
import hnau.common.kotlin.foldNullable

fun SealedInfo.Variant.toTypeSpec(
    index: Int,
    info: SealedInfo,
    parent: ClassName,
): TypeSpec = TypeSpec
    .classBuilder(wrapperClassName)
    .apply {
        modifiers += KModifier.DATA
        addSuperinterface(parent)

        if (info.serializable) {
            annotations += AnnotationSpec
                .builder(SealInfoCodeGeneratorConstants.serializableClassName)
                .build()
            annotations += AnnotationSpec
                .builder(SealInfoCodeGeneratorConstants.serialNameClassName)
                .addMember("\"$serialName\"")
                .build()
        }

        if (info.ordinal) {
            propertySpecs += PropertySpec
                .builder(
                    SealInfoCodeGeneratorConstants.ordinalPropertyName,
                    SealInfoCodeGeneratorConstants.intClassName,
                )
                .addModifiers(KModifier.OVERRIDE)
                .getter(
                    FunSpec
                        .getterBuilder()
                        .addStatement("return $index")
                        .build()
                )
                .build()
        }

        if (info.name) {
            propertySpecs += PropertySpec
                .builder(
                    SealInfoCodeGeneratorConstants.namePropertyName,
                    SealInfoCodeGeneratorConstants.stringClassName,
                )
                .addModifiers(KModifier.OVERRIDE)
                .getter(
                    FunSpec
                        .getterBuilder()
                        .addStatement("return \"$identifier\"")
                        .build()
                )
                .build()
        }

        val typeClassName = type.toClassName()

        primaryConstructor(
            FunSpec
                .constructorBuilder()
                .addParameter(
                    ParameterSpec
                        .builder(
                            name = wrappedValuePropertyName,
                            type = typeClassName,
                        )
                        .build()
                )
                .build()
        )

        propertySpecs += PropertySpec
            .builder(
                name = wrappedValuePropertyName,
                type = typeClassName,
            )
            .initializer(wrappedValuePropertyName)
            .build()

        info
            .overrides
            .forEach { override ->
                override
                    .createSpec(
                        variant = this@toTypeSpec,
                    )
                    .fold(
                        ifLeft = ::addFunction,
                        ifRight = ::addProperty,
                    )
            }
    }
    .build()

private fun SealedInfo.Override.createSpec(
    variant: SealedInfo.Variant,
): Either<FunSpec, PropertySpec> {

    val typeParamResolver: TypeParameterResolver = typeParameters.toTypeParameterResolver()

    val typeVariables: List<TypeVariableName> =
        typeParameters.map { it.toTypeVariableName(typeParamResolver) }

    return when (type) {
        is SealedInfo.Override.Type.Function -> createFunSpec(
            variant = variant,
            type = type,
            typeParamResolver = typeParamResolver,
            typeVariables = typeVariables,
        ).left()

        is SealedInfo.Override.Type.Property -> createPropertySpec(
            variant = variant,
            type = type,
            typeParamResolver = typeParamResolver,
            typeVariables = typeVariables,
        ).right()
    }
}

private fun SealedInfo.Override.createFunSpec(
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

private fun SealedInfo.Override.createPropertySpec(
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
                                ifNotNull = { "with(${variant.wrappedValuePropertyName}) { $name = ${SealInfoCodeGeneratorConstants.setterParameterName}" }
                            )
                        )
                        .build()
                )
            }

        }
        .build()
}