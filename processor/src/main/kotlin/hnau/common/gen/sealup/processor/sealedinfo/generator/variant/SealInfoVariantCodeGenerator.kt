package hnau.common.gen.sealup.processor.sealedinfo.generator.variant

import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import hnau.common.gen.sealup.processor.sealedinfo.SealedInfo
import hnau.common.gen.sealup.processor.sealedinfo.generator.utils.SealInfoCodeGeneratorConstants
import hnau.common.gen.sealup.processor.sealedinfo.generator.utils.className
import hnau.common.gen.sealup.processor.sealedinfo.generator.utils.wrappedClassName
import hnau.common.gen.sealup.processor.sealedinfo.generator.variant.override.createSpec

fun SealedInfo.Variant.toTypeSpec(
    index: Int,
    info: SealedInfo,
): TypeSpec = TypeSpec
    .classBuilder(wrapperClass)
    .apply {
        modifiers += KModifier.DATA
        addSuperinterface(info.className)

        if (info.serializable) {
            annotations += AnnotationSpec
                .builder(SealInfoCodeGeneratorConstants.serializableClassName)
                .build()
            annotations += AnnotationSpec
                .builder(SealInfoCodeGeneratorConstants.serialNameClassName)
                .addMember("\"$serialName\"")
                .build()
        }

        if (info.ordinal) {
            propertySpecs += PropertySpec
                .builder(
                    SealInfoCodeGeneratorConstants.ordinalPropertyName,
                    SealInfoCodeGeneratorConstants.intClassName,
                )
                .addModifiers(KModifier.OVERRIDE)
                .getter(
                    FunSpec
                        .getterBuilder()
                        .addStatement("return $index")
                        .build()
                )
                .build()
        }

        if (info.name) {
            propertySpecs += PropertySpec
                .builder(
                    SealInfoCodeGeneratorConstants.namePropertyName,
                    SealInfoCodeGeneratorConstants.stringClassName,
                )
                .addModifiers(KModifier.OVERRIDE)
                .getter(
                    FunSpec
                        .getterBuilder()
                        .addStatement("return \"$identifier\"")
                        .build()
                )
                .build()
        }

        primaryConstructor(
            FunSpec
                .constructorBuilder()
                .addParameter(
                    ParameterSpec
                        .builder(
                            name = wrappedValuePropertyName,
                            type = wrappedClassName,
                        )
                        .build()
                )
                .build()
        )

        propertySpecs += PropertySpec
            .builder(
                name = wrappedValuePropertyName,
                type = wrappedClassName,
            )
            .initializer(wrappedValuePropertyName)
            .build()

        info
            .overrides
            .forEach { override ->
                override
                    .createSpec(
                        variant = this@toTypeSpec,
                    )
                    .fold(
                        ifLeft = ::addFunction,
                        ifRight = ::addProperty,
                    )
            }
    }
    .build()



