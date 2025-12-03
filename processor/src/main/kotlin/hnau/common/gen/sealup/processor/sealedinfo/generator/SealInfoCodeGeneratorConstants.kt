package hnau.common.gen.sealup.processor.sealedinfo.generator

import com.squareup.kotlinpoet.ClassName

object SealInfoCodeGeneratorConstants {

     val serializableClassName = ClassName("kotlinx.serialization", "Serializable")
     val serialNameClassName = ClassName("kotlinx.serialization", "SerialName")

     val intClassName = ClassName("kotlin", "Int")

     val stringClassName = ClassName("kotlin", "String")

     const val setterParameterName = "newValue"

     const val ordinalPropertyName = "ordinal"

     const val namePropertyName = "name"
}