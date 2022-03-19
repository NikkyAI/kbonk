import java.io.ByteArrayOutputStream

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
}

group = "nikky.moe"
version = "0.0.1"
//application {
//    mainClass.set("nikky.moe.ApplicationKt")
//}

fun execOutput(vararg args: String) : String {
    val stdout = ByteArrayOutputStream()
    exec{
        commandLine(*args)
        standardOutput = stdout;
    }
    return stdout.toString()
}

repositories {
    mavenCentral()
    maven(url ="https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
}

kotlin {
    linuxX64 {
        binaries {
            executable {
                entryPoint = "main"
            }
        }
        binaries.all {
            binaryOptions["memoryModel"] = "experimental"
//            linkerOpts.addAll(
//                execOutput("MagickWand-config", "--ldflags")
//                    .split(" ")
//            )
        }
        compilations.getByName("main") {
            cinterops {
                val magickwand by creating {
                    defFile(project.file("src/nativeInterop/cinterop/magickwand.def"))
                    packageName("magickwand")
//                    compilerOpts(
//                        execOutput("MagickWand-config", "--cflags").also {
//                            logger.lifecycle(it)
//                        }
//                    )
                }
            }
        }
    }

    sourceSets {
        val linuxX64Main by existing {
            dependencies {
                // basics
                implementation(Kotlin.stdlib)
                implementation("io.ktor:ktor-server-cio:_")
                implementation("io.ktor:ktor-server-status-pages:_")
                implementation("io.ktor:ktor-server-cors:_")

                // might be useful
//                implementation("io.ktor:ktor-server-call-id:_") // min 2.0.0-eap-315

                // json stuff
                implementation("io.ktor:ktor-server-content-negotiation:_") // min 2.0.0-eap-315
                implementation("io.ktor:ktor-server-auto-head-response:_")
                implementation("io.ktor:ktor-serialization-kotlinx-json:_")

                // fixing deps
                implementation("org.jetbrains.kotlin:kotlin-reflect:_")

                implementation(KotlinX.Coroutines.core)
                implementation("com.github.ajalt.clikt:clikt:_")
                implementation("io.github.microutils:kotlin-logging:_")
                implementation("com.github.ajalt.mordant:mordant:_")
                implementation("com.squareup.okio:okio-multiplatform:_")
            }
        }

        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("okio.ExperimentalFileSystem")
        }
    }
}

//dependencies {
//    implementation("io.ktor:ktor-server-cors-jvm:$ktor_version")
//    implementation("io.ktor:ktor-server-auto-head-response-jvm:$ktor_version")
//    implementation("io.ktor:ktor-server-call-logging-jvm:$ktor_version")
//    implementation("io.ktor:ktor-server-core-jvm:$ktor_version")
//    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.7.3")
//    implementation("io.ktor:ktor-server-html-builder-jvm:$ktor_version")
//    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktor_version")
//    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktor_version")
//    implementation("io.ktor:ktor-server-cio-jvm:$ktor_version")
//    implementation("ch.qos.logback:logback-classic:$logback_version")
//    testImplementation("io.ktor:ktor-server-tests-jvm:$ktor_version")
//    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
//}