package hnau.common.gen.sealup.processor.sealedinfo.generator

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.toClassName
import hnau.common.gen.sealup.processor.sealedinfo.SealedInfo


fun SealedInfo.toTypeSpec(
    packageName: String,
    visibility: KModifier?,
): TypeSpec {
    val className = ClassName(packageName, sealedInterfaceName)
    return TypeSpec
        .interfaceBuilder(className)
        .apply {
            modifiers += KModifier.SEALED
            visibility?.let { modifiers += it }
            addSuperinterface(parent.toClassName())

            if (serializable) {
                annotations += AnnotationSpec
                    .builder(SealInfoCodeGeneratorConstants.serializableClassName)
                    .build()
            }

            if (ordinal) {
                propertySpecs += PropertySpec
                    .builder(
                        SealInfoCodeGeneratorConstants.ordinalPropertyName,
                        SealInfoCodeGeneratorConstants.intClassName,
                    )
                    .build()
            }

            if (name) {
                propertySpecs += PropertySpec
                    .builder(
                        SealInfoCodeGeneratorConstants.namePropertyName,
                        SealInfoCodeGeneratorConstants.stringClassName,
                    )
                    .build()
            }

            addTypes(
                variants.mapIndexed { index, variant ->
                    variant.toTypeSpec(
                        index = index,
                        info = this@toTypeSpec,
                        parent = className,
                    )
                }
            )
        }
        .build()
}