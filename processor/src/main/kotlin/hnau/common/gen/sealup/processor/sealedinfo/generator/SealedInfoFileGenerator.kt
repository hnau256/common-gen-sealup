package hnau.common.gen.sealup.processor.sealedinfo.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.squareup.kotlinpoet.FileSpec
import hnau.common.gen.sealup.processor.sealedinfo.SealedInfo
import hnau.common.gen.sealup.processor.sealedinfo.generator.utils.packageName

fun SealedInfo.generateCode(
    codeGenerator: CodeGenerator,
) {

    val file = FileSpec
        .builder(packageName, sealedInterfaceName)
        .apply {
            addType(
                toTypeSpec()
            )
            if (fold) {
                addFunction(
                    toFoldFuncSpec()
                )
            }
            factoryMethods?.let { parentExtension ->
                toFactoriesFuncsSpec(
                    parentExtension = parentExtension,
                ).forEach { factoryMethod ->
                    addFunction(factoryMethod)
                }
            }
        }
        .build()

    codeGenerator
        .createNewFile(
            dependencies = Dependencies(
                aggregating = false,
                sources = buildList {
                    add(parent.containingFile)
                    addAll(
                        variants.map { variant ->
                            variant.wrappedType.declaration.containingFile
                        }
                    )
                }
                    .filterNotNull()
                    .toTypedArray(),
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