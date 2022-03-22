import java.io.ByteArrayOutputStream

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
}

group = "nikky.moe"
version = "0.0.1"

fun execOutput(vararg args: String): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine(*args)
        standardOutput = stdout;
    }
    return stdout.toString()
}

repositories {
    mavenCentral()
    maven(url = "https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
}

val resourcesInclude = buildDir.resolve("resources/include")
val resourcesHeader = resourcesInclude.resolve("resources.h")
val resourcesLib = buildDir.resolve("resources/lib")
val resourceSrc = buildDir.resolve("resources/src")
val resourceArchive = buildDir.resolve("resources/lib/resources.a")

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
                    packageName("magickwand")

                    val compilerOpts = execOutput(
                        "MagickWand-config", "--cflags"
                    ).trim()
                    val linkerOpts = execOutput(
                        "MagickWand-config", "--ldflags"
                    ).trim()
                    defFile.parentFile.mkdirs()
                    defFile.writeText(
                        """
                        headers = MagickWand/MagickWand.h
                        
                        compilerOpts.linux = $compilerOpts
                        linkerOpts.linux = -L/usr/lib $linkerOpts
                        """.trimIndent()
                    )
                }
                val resources by creating {
                    packageName("resources")


                    defFile.parentFile.mkdirs()
                    defFile.writeText(
                        """
                        headers = ${resourcesHeader.name}
                        
                        staticLibraries = ${resourceArchive.name}
                        libraryPaths = ${resourceArchive.parentFile.toRelativeString(projectDir)}
                        
                        compilerOpts = -I${resourcesInclude.toRelativeString(projectDir)}
                        """.trimIndent()
                    )
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
                implementation("io.ktor:ktor-serialization-kotlinx-json:_")

                // caching
                implementation("io.ktor:ktor-server-conditional-headers:_")
                implementation("io.ktor:ktor-server-caching-headers:_")
                implementation("io.ktor:ktor-server-auto-head-response:_")

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


tasks.getByName("cinteropResourcesLinuxX64") {
    outputs.file(resourceArchive)
    outputs.dir(resourcesInclude)
    doFirst {
        resourcesInclude.deleteRecursively()
        resourcesInclude.mkdirs()
        resourceSrc.deleteRecursively()
        resourceSrc.mkdirs()
        exec {
            workingDir = rootDir.resolve("src/linuxX64Main/resources")
            commandLine(
                "bin2c",
                "-d", resourcesHeader,
                "-o", resourceSrc.resolve("resources.c"),
                *workingDir.list()
            )
        }
        val objectFile = buildDir.resolve("tmp_resources.o")
        exec {
            commandLine("gcc", "-o", objectFile, "-c", resourceSrc.resolve("resources.c"))
        }
        resourcesLib.deleteRecursively()
        resourcesLib.mkdirs()
        exec {
            commandLine(
                "ar", "-rcs",
                resourceArchive,
                objectFile
            )
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