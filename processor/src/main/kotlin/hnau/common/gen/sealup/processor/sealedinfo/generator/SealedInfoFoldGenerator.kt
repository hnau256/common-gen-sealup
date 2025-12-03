package hnau.common.gen.sealup.processor.sealedinfo.generator

import com.google.devtools.ksp.getVisibility
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.ksp.toKModifier
import hnau.common.gen.sealup.processor.sealedinfo.SealedInfo

fun SealedInfo.toFoldFuncSpec(
    selaedClassName: ClassName,
): FunSpec = FunSpec
    .builder("fold")
    .apply {

        parent
            .getVisibility()
            .toKModifier()
            ?.let { visibility -> modifiers += visibility }

        modifiers += KModifier.INLINE

        val resultType = TypeVariableName("R")

        typeVariables += resultType
    }
    .addModifiers(KModifier.INTERNAL, KModifier.INLINE)
    .addTypeVariable(TypeVariableName("R"))
    .receiver(ClassName("your.pkg", "AppStateSealedTest"))
    .returns(TypeVariableName("R"))
    .addParameter(
        "ifState1",
        LambdaTypeName.get(
            parameters = listOf(ClassName("your.pkg", "State1")).toTypedArray(),
            returnType = TypeVariableName("R")
        )
    )
    .addParameter(
        "ifState2",
        LambdaTypeName.get(
            parameters = listOf(ClassName("your.pkg", "State2")).toTypedArray(),
            returnType = TypeVariableName("R")
        )
    )
    .addCode(
        """
        return when (this) {
            is %T.State1Wrapper -> ifState1(value)
            is %T.State2Wrapper -> ifState2(value)
        }
        """.trimIndent(),
        ClassName("your.pkg", "AppStateSealedTest"),
        ClassName("your.pkg", "AppStateSealedTest")
    )
    .build()