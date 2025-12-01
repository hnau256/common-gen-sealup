package hnau.common.gen.sealup.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import hnau.common.gen.sealup.processor.sealedinfo.SealedInfo
import hnau.common.gen.sealup.processor.sealedinfo.build.create
import hnau.common.gen.sealup.processor.sealedinfo.generator.generateCode

class SymbolProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    override fun process(
        resolver: Resolver,
    ): List<KSAnnotated> {

        resolver
            .getSymbolsWithAnnotation(AnnotationInfo.nameWithPackage)
            .mapNotNull { annotated ->
                SealedInfo.create(
                    logger = logger,
                    annotated = annotated,
                )
            }
            .forEach { sealedInfo ->
                sealedInfo.generateCode(
                    codeGenerator = codeGenerator,
                )
            }

        return emptyList()
    }
}