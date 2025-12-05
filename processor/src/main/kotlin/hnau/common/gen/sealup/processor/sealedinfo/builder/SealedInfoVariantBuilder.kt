package hnau.common.gen.sealup.processor.sealedinfo.builder

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSType
import hnau.common.gen.kt.arguments
import hnau.common.gen.kt.nameWithoutPackage
import hnau.common.gen.sealup.processor.sealedinfo.SealedInfo
import hnau.common.kotlin.ifNull

fun SealedInfo.Variant.Companion.create(
    logger: KSPLogger,
    annotation: KSAnnotation,
    wrappedValuePropertyName: String,
): SealedInfo.Variant? {

    val arguments = annotation.arguments(logger)

    val type = arguments
        .get<KSType>("type")
        .ifNull { return null }

    val stickedName = type
        .declaration
        .nameWithoutPackage(logger)
        .ifNull { return null }
        .replace(".", "")

    val identifier = arguments
        .get<String>("identifier")
        .ifNull { return null }
        .takeIf(String::isNotEmpty)
        .ifNull { stickedName.replaceFirstChar(Char::lowercase) }

    return SealedInfo.Variant(
        wrappedType = type,
        wrapperClass = arguments
            .get<String>("wrapperClassName")
            .ifNull { return null }
            .takeIf(String::isNotEmpty)
            .ifNull { stickedName + "Wrapper" },
        identifier = identifier,
        serialName = arguments
            .get<String>("serialName")
            .ifNull { return null }
            .takeIf(String::isNotEmpty)
            .ifNull { identifier },
        wrappedValuePropertyName = arguments
            .get<String>("wrappedValuePropertyName")
            .ifNull { return null }
            .takeIf(String::isNotEmpty)
            .ifNull { wrappedValuePropertyName },
    )
}