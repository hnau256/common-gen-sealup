package hnau.common.gen.sealup.processor.sealedinfo.generator

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSAnnotation
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
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

fun SealedInfo.generateCode(
    codeGenerator: CodeGenerator,
) {
    val packageName = parent.packageName.asString()

    val file = FileSpec.builder(packageName, sealedInterfaceName)
        .addType(
            toTypeSpec(
                packageName = packageName,
                visibility = parent.getVisibility().toKModifier(),
            )
        )
        .build()

    codeGenerator
        .createNewFile(
            dependencies = Dependencies(
                aggregating = false,
                sources = listOfNotNull(parent.containingFile).toTypedArray(),
            ),
            packageName = packageName,
            fileName = sealedInterfaceName,
        )
        .use { out ->
            out
                .writer()
                .use { writer ->
                    file.writeTo(writer)
                }
        }
}

private fun SealedInfo.toTypeSpec(
    packageName: String,
    visibility: KModifier?,
): TypeSpec {
    val className = ClassName(packageName, sealedInterfaceName)
    return TypeSpec
        .interfaceBuilder(className)
        .apply {
            modifiers += KModifier.SEALED
            visibility?.let { modifiers += it }
            addSuperinterface(parent.toClassName())

            if (serializable) {
                annotations += AnnotationSpec
                    .builder(serializableClassName)
                    .build()
            }

            if (ordinal) {
                propertySpecs += PropertySpec
                    .builder(ordinalPropertyName, intClassName)
                    .build()
            }

            if (name) {
                propertySpecs += PropertySpec
                    .builder(namePropertyName, stringClassName)
                    .build()
            }

            addTypes(
                variants.mapIndexed { index, variant ->
                    variant.toTypeSpec(
                        index = index,
                        info = this@toTypeSpec,
                        parent = className,
                    )
                }
            )
        }
        .build()
}

private fun SealedInfo.Variant.toTypeSpec(
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
                .builder(serializableClassName)
                .build()
            annotations += AnnotationSpec
                .builder(serialNameClassName)
                .addMember("\"$serialName\"")
                .build()
        }

        if (info.ordinal) {
            propertySpecs += PropertySpec
                .builder(ordinalPropertyName, intClassName)
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
                .builder(namePropertyName, stringClassName)
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
                                ifNull = { "${variant.wrappedValuePropertyName}.$name = $setterParameterName" },
                                ifNotNull = { "with(${variant.wrappedValuePropertyName}) { $name = $setterParameterName }" }
                            )
                        )
                        .build()
                )
            }

        }
        .build()
}

private val serializableClassName = ClassName("kotlinx.serialization", "Serializable")
private val serialNameClassName = ClassName("kotlinx.serialization", "SerialName")

private val intClassName = ClassName("kotlin", "Int")

private val stringClassName = ClassName("kotlin", "String")

private const val setterParameterName = "newValue"

private const val ordinalPropertyName = "ordinal"

private const val namePropertyName = "name"