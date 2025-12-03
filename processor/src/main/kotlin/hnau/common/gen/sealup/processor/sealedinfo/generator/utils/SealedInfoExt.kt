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