package hnau.common.gen.sealup.processor.sealedinfo.generator

import arrow.core.toNonEmptyListOrNull
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.ksp.toClassName
import hnau.common.gen.sealup.processor.sealedinfo.SealedInfo
import hnau.common.gen.sealup.processor.sealedinfo.generator.utils.companionClassName
import hnau.common.gen.sealup.processor.sealedinfo.generator.utils.visibility
import hnau.common.gen.sealup.processor.sealedinfo.generator.utils.wrappedClassName
import hnau.common.gen.sealup.processor.sealedinfo.generator.utils.wrapperClassName
import hnau.common.kotlin.castOrThrow
import hnau.common.kotlin.foldBoolean
import hnau.common.kotlin.ifTrue

fun SealedInfo.toFactoriesFuncsSpec(
    parentExtension: SealedInfo.ParentExtension,
): List<FunSpec> = variants
    .toList()
    .flatMap { variant ->
        variant.toFactoriesFuncsSpec(
            info = this,
            parentExtension = parentExtension,
        )
    }

private fun SealedInfo.Variant.toFactoriesFuncsSpec(
    info: SealedInfo,
    parentExtension: SealedInfo.ParentExtension,
): List<FunSpec> {
    val wrapperClassName = wrapperClassName(info)
    return buildList {
        add(
            toFactoryFuncSpec(
                info = info,
                parentExtension = parentExtension,
                wrapperClassName = wrapperClassName,
            )
        )
        addAll(
            constructors
                .map { constructor ->
                    toConstructorFactoryFuncSpec(
                        info = info,
                        parentExtension = parentExtension,
                        wrapperClassName = wrapperClassName,
                        constructor = constructor,
                    )
                }
        )
    }
}

private fun SealedInfo.Variant.toFactoryFuncSpec(
    info: SealedInfo,
    parentExtension: SealedInfo.ParentExtension,
    wrapperClassName: ClassName,
): FunSpec = FunSpec
    .builder(identifier)
    .apply {
        info.visibility?.let { modifiers += it }

        receiver(parentExtension.companionClassName)
        returns(wrapperClassName)

        addParameter(
            identifier,
            wrappedClassName,
        )
    }
    .addCode(
        "return %T($wrappedValuePropertyName = $identifier)",
        wrapperClassName,
    )
    .build()

private fun SealedInfo.Variant.toConstructorFactoryFuncSpec(
    info: SealedInfo,
    parentExtension: SealedInfo.ParentExtension,
    wrapperClassName: ClassName,
    constructor: SealedInfo.Variant.Constructor,
): FunSpec {

    val allParametersHasNames = constructor
        .parameters
        .all { (name) -> name != null }

    return FunSpec
        .builder(identifier)
        .apply {
            info.visibility?.let { modifiers += it }

            receiver(parentExtension.companionClassName)
            returns(wrapperClassName)

            constructor
                .parameters
                .forEachIndexed { index, (nameOrNull, type) ->
                    val name = nameOrNull ?: "parameter$index"
                    addParameter(
                        name,
                        type.toClassName(),
                    )
                }

        }
        .addCode(
            Pair(
                "return $identifier(\n\t$identifier = %T(",
                ")\n)",
            ).let { (prefix, postfix) ->
                constructor
                    .parameters
                    .toNonEmptyListOrNull()
                    ?.withIndex()
                    ?.joinToString(
                        prefix = "$prefix\n",
                        postfix = "\n\t$postfix",
                        separator = "\n",
                    ) { (index, nameWithType) ->
                        val (nameOrNull) = nameWithType
                        val name = nameOrNull ?: "parameter$index"
                        val prefix = allParametersHasNames.ifTrue { "$name = " }.orEmpty()
                        "\t\t$prefix$name,"
                    }
                    ?: "$prefix$postfix"
            },
            wrappedClassName,
        )
        .build()
}