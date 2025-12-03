package hnau.common.gen.sealup.processor.sealedinfo.generator.variant.override

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeVariableName
import hnau.common.gen.sealup.processor.sealedinfo.SealedInfo


fun SealedInfo.Override.createSpec(
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