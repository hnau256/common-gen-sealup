plugins {
    kotlin("jvm")
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

application {
    mainClass.set("hnau.common.gen.sealup.test.AppKt")
}

dependencies {
    implementation(project(":annotations"))
    implementation(libs.kotlin.serialization.core)
    implementation(libs.arrow.core)
    ksp(project(":processor"))
}