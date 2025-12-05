package hnau.common.gen.sealup.processor.sealedinfo.generator

import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.TypeVariableName
import hnau.common.gen.sealup.processor.sealedinfo.SealedInfo
import hnau.common.gen.sealup.processor.sealedinfo.generator.utils.className
import hnau.common.gen.sealup.processor.sealedinfo.generator.utils.uppercasedIdentifier
import hnau.common.gen.sealup.processor.sealedinfo.generator.utils.visibility
import hnau.common.gen.sealup.processor.sealedinfo.generator.utils.wrappedClassName
import hnau.common.gen.sealup.processor.sealedinfo.generator.utils.wrapperClassName

fun SealedInfo.toFoldFuncSpec(): FunSpec {
    val resultType = TypeVariableName("R")
    return FunSpec
        .builder("fold")
        .apply {
            modifiers += KModifier.INLINE
            visibility?.let { modifiers += it }

            typeVariables += resultType

            receiver(className)
            returns(resultType)

            variants.forEach { variant ->
                addParameter(
                    "if${variant.uppercasedIdentifier}",
                    LambdaTypeName.get(
                        parameters = listOf(variant.wrappedClassName).toTypedArray(),
                        returnType = resultType
                    )
                )
            }
        }
        .addCode(
            variants.joinToString(
                prefix = "return when (this) {\n",
                postfix = "\n\t}",
                separator = "\n",
            ) { variant ->
                "\t\tis %T -> if${variant.uppercasedIdentifier}(${variant.wrappedValuePropertyName})"
            },
            args = variants
                .map { it.wrapperClassName(this@toFoldFuncSpec) }
                .toTypedArray(),
        )
        .build()
}