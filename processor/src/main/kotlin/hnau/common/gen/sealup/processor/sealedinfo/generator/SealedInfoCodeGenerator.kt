package hnau.common.gen.sealup.processor.sealedinfo.generator

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toKModifier
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import hnau.common.gen.sealup.processor.sealedinfo.SealedInfo
import hnau.common.kotlin.foldNullable
import hnau.common.kotlin.ifNull

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
            addTypes(
                variants.map { variant ->
                    variant.toTypeSpec(
                        overrides = overrides,
                        parent = className,
                    )
                }
            )
        }
        .build()
}

private fun SealedInfo.Variant.toTypeSpec(
    overrides: List<SealedInfo.Override>,
    parent: ClassName,
): TypeSpec = TypeSpec
    .classBuilder(wrapperClassName)
    .apply {
        modifiers += KModifier.DATA
        addSuperinterface(parent)

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

        overrides.forEach { override ->
            override
                .createSpec(
                    wrappedValuePropertyName = wrappedValuePropertyName,
                )
                .fold(
                    ifLeft = ::addFunction,
                    ifRight = ::addProperty,
                )
        }
    }
    .build()

private fun SealedInfo.Override.createSpec(
    wrappedValuePropertyName: String,
): Either<FunSpec, PropertySpec> {

    val typeParamResolver: TypeParameterResolver = typeParameters.toTypeParameterResolver()

    val typeVariables: List<TypeVariableName> =
        typeParameters.map { it.toTypeVariableName(typeParamResolver) }

    return when (type) {
        is SealedInfo.Override.Type.Function -> createFunSpec(
            wrappedValuePropertyName = wrappedValuePropertyName,
            type = type,
            typeParamResolver = typeParamResolver,
            typeVariables = typeVariables,
        ).left()

        is SealedInfo.Override.Type.Property -> createPropertySpec(
            wrappedValuePropertyName = wrappedValuePropertyName,
            type = type,
            typeParamResolver = typeParamResolver,
            typeVariables = typeVariables,
        ).right()
    }
}

private fun SealedInfo.Override.createFunSpec(
    wrappedValuePropertyName: String,
    type: SealedInfo.Override.Type.Function,
    typeParamResolver: TypeParameterResolver,
    typeVariables: List<TypeVariableName>
): FunSpec = FunSpec
    .builder(name)
    .apply {
        modifiers += KModifier.OVERRIDE
        visibility.toKModifier()?.let { modifiers += it }
        this.typeVariables.addAll(typeVariables)
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
                    "return $wrappedValuePropertyName.$name(" to ")"
                },
                ifNotNull = {
                    "return with($wrappedValuePropertyName) { $name(" to ") }"
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
    wrappedValuePropertyName: String,
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
            mutable(type.mutable)

            receiver
                ?.toTypeName(typeParamResolver)
                ?.let(::receiver)

            getter(
                FunSpec
                    .getterBuilder()
                    .addStatement(
                        receiver.foldNullable(
                            ifNull = { "return $wrappedValuePropertyName.$name" },
                            ifNotNull = { "return with($wrappedValuePropertyName) { $name }" }
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
                                ifNull = { "$wrappedValuePropertyName.$name = newValue" },
                                ifNotNull = { "with($wrappedValuePropertyName) { $name = newValue }" }
                            )
                        )
                        .build()
                )
            }

        }
        .build()
}