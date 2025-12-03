package hnau.common.gen.sealup.processor.sealedinfo.generator

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ksp.toKModifier
import hnau.common.gen.sealup.processor.sealedinfo.SealedInfo

fun SealedInfo.generateCode(
    codeGenerator: CodeGenerator,
) {
    val packageName = parent.packageName.asString()

    val file = FileSpec
        .builder(packageName, sealedInterfaceName)
        .apply {
            addType(
                toTypeSpec(
                    packageName = packageName,
                    visibility = parent.getVisibility().toKModifier(),
                )
            )
        }
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
                .use { writer -> file.writeTo(writer) }
        }
}