package hnau.common.gen.sealup.processor.sealedinfo.generator.utils

import com.google.devtools.ksp.getVisibility
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toKModifier
import hnau.common.gen.sealup.processor.sealedinfo.SealedInfo

val SealedInfo.packageName: String
    get() = parent.packageName.asString()

val SealedInfo.className: ClassName
    get() = ClassName(packageName, sealedInterfaceName)

val SealedInfo.visibility: KModifier?
    get() = parent.getVisibility().toKModifier()

val SealedInfo.parentClassName: ClassName
    get() = parent.toClassName()

val SealedInfo.ParentExtension.companionClassName: ClassName
    get() = companion.toClassName()

val SealedInfo.Variant.wrappedClassName: ClassName
    get() = wrappedType.toClassName()

fun SealedInfo.Variant.wrapperClassName(
    sealedInfo: SealedInfo,
): ClassName = sealedInfo
    .className
    .let { sealedClassName ->
        ClassName(
            packageName = sealedClassName.packageName,
            simpleNames = sealedClassName.simpleNames + wrapperClass,
        )
    }

val SealedInfo.Variant.uppercasedIdentifier: String
    get() = identifier.replaceFirstChar(Char::uppercase)