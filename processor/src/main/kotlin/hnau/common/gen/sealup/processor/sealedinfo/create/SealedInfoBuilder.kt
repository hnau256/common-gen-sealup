package hnau.common.gen.sealup.processor.sealedinfo.create

import arrow.core.Either
import arrow.core.toNonEmptyListOrNull
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.validate
import hnau.common.gen.kt.arguments
import hnau.common.gen.kt.nameWithoutPackage
import hnau.common.gen.sealup.processor.AnnotationInfo
import hnau.common.gen.sealup.processor.sealedinfo.SealedInfo
import hnau.common.kotlin.ifNull

fun SealedInfo.Companion.create(
    logger: KSPLogger,
    annotated: KSAnnotated,
): SealedInfo? {

    if (!annotated.validate()) {
        logger.error("Is not valid", annotated)
        return null
    }

    val classDeclaration = (annotated as? KSClassDeclaration).ifNull {
        logger.error("Is not class", annotated)
        return null
    }

    if (classDeclaration.classKind != ClassKind.INTERFACE) {
        logger.error("Is not interface", classDeclaration)
        return null
    }

    if (classDeclaration.typeParameters.isNotEmpty()) {
        logger.error("Unable to seal up interface with type parameters", classDeclaration)
        return null
    }

    val sealUpAnnotation = classDeclaration
        .annotations
        .firstOrNull { it.shortName.asString() == AnnotationInfo.simpleName }
        .ifNull {
            logger.error("Unable find @${AnnotationInfo.simpleName} annotation", classDeclaration)
            return null
        }

    val arguments = sealUpAnnotation.arguments(logger)

    val wrappedValuePropertyName = arguments
        .get<String>("wrappedValuePropertyName")
        ?: return null

    val variants = arguments
        .get<List<KSAnnotation>>("variants")
        .ifNull { return null }
        .toNonEmptyListOrNull()
        .ifNull {
            logger.error("Expected at least one variant", sealUpAnnotation)
            return null
        }
        .map { annotation ->
            SealedInfo.Variant
                .create(
                    logger = logger,
                    annotation = annotation,
                    wrappedValuePropertyName = wrappedValuePropertyName,
                )
                .ifNull { return null }
        }

    val overrides = classDeclaration
        .declarations
        .mapNotNull { declaration ->
            when (declaration) {
                is KSFunctionDeclaration -> Either.Right(declaration)
                is KSPropertyDeclaration -> Either.Left(declaration)
                else -> null
            }
        }
        .toList()
        .map { declaration: Either<KSPropertyDeclaration, KSFunctionDeclaration> ->
            SealedInfo.Override
                .create(
                    logger = logger,
                    declaration = declaration,
                )
                .ifNull { return null }
        }

    val sealedInterfaceName = arguments
        .get<String>("sealedInterfaceName")
        .ifNull { return null }
        .takeIf(String::isNotEmpty)
        .ifNull {
            classDeclaration
                .nameWithoutPackage(logger)
                .ifNull { return null }
                .replace(".", "")
                .plus("Sealed")
        }

    return SealedInfo(
        parent = classDeclaration,
        variants = variants,
        serializable = arguments.get<Boolean>("serializable") ?: return null,
        ordinal = arguments.get<Boolean>("ordinal") ?: return null,
        name = arguments.get<Boolean>("name") ?: return null,
        sealedInterfaceName = sealedInterfaceName,
        fold = arguments.get<Boolean>("fold") ?: return null,
        factoryMethods = arguments.get<Boolean>("serializable") ?: return null,
        overrides = overrides,
    )
}