package hnau.common.gen.sealup.processor.sealedinfo.create

import arrow.core.Either
import arrow.core.identity
import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import hnau.common.gen.kt.resolve
import hnau.common.gen.sealup.processor.sealedinfo.SealedInfo
import hnau.common.kotlin.ifNull

fun SealedInfo.Override.Companion.create(
    logger: KSPLogger,
    declaration: Either<KSPropertyDeclaration, KSFunctionDeclaration>,
): SealedInfo.Override? {
    val name = declaration
        .fold(::identity, ::identity)
        .simpleName
        .asString()
    return declaration.fold(
        ifLeft = { property ->
            SealedInfo.Override(
                name = name,
                result = property
                    .type
                    .resolve(logger)
                    ?: return null,
                typeParameters = property.typeParameters,
                type = SealedInfo.Override.Type.Property(
                    mutable = property.isMutable,
                ),
                visibility = property.getVisibility(),
                receiver = property
                    .extensionReceiver
                    ?.let { reference ->
                        reference
                            .resolve(logger)
                            ?: return null
                    },
                annotations = property
                    .annotations
                    .toList(),
            )
        },
        ifRight = { function ->
            SealedInfo.Override(
                name = name,
                result = function
                    .returnType
                    .ifNull {
                        logger.error("Unable resolve function return type", function)
                        return null
                    }
                    .resolve(logger)
                    ?: return null,
                typeParameters = function.typeParameters,
                type = SealedInfo.Override.Type.Function(
                    arguments = function.parameters.map { parameter ->
                        SealedInfo.Override.Type.Function.Argument(
                            name = parameter
                                .name
                                .ifNull {
                                    logger.error(
                                        "Unable resolve function parameter name",
                                        parameter
                                    )
                                    return null
                                }
                                .asString(),
                            type = parameter
                                .type
                                .resolve(logger)
                                ?: return null,
                        )
                    }
                ),
                visibility = function.getVisibility(),
                receiver = function
                    .extensionReceiver
                    ?.let { reference ->
                        reference
                            .resolve(logger)
                            ?: return null
                    },
                annotations = function
                    .annotations
                    .toList()
                    .also {
                        logger.info("Annotations: $it")
                    },
            )
        }
    )
}