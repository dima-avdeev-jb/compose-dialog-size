import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform") 
    id("org.jetbrains.compose")
}

kotlin {
    jvm {
        withJava()
    }
    sourceSets {
        named("jvmMain") {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material)
                implementation(compose.desktop.currentOs)
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
    }
}
