plugins {
    id("java-library")
    id("java-gradle-plugin")
//    id("org.jetbrains.kotlin.jvm")
//    id("org.jetbrains.kotlin.jvm") version "1.7.20"
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

group = "com.l3gacy.plugin"
version = "0.0.1"

gradlePlugin {
    plugins {
        create("simplePlugin") {
            id = "com.chenenyu.router"
            implementationClass = "com.l3gacy.plugin.router.RouterPlugin"
        }
    }
}

dependencies {
    implementation(gradleApi())

    implementation("com.joom.grip:grip:0.9.1")

    compileOnly("com.android.tools.build:gradle:8.1.1")
    compileOnly("com.android.tools:common:31.1.1")
//    compileOnly("com.android.tools.build:gradle-api:8.1.1")
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")

    // https://central.sonatype.com/artifact/org.ow2.asm/asm/9.4
//    implementation("org.ow2.asm:asm:9.4")
    // https://central.sonatype.com/artifact/org.ow2.asm/asm-commons/9.4
    implementation("org.ow2.asm:asm-commons:9.4") // 依赖 asm 以及 asm-tree
    // https://central.sonatype.com/artifact/org.ow2.asm/asm-util/9.4
//    implementation("org.ow2.asm:asm-util:9.4") // 依赖 asm 以及 asm-tree、asm-analysis
    // https://central.sonatype.com/artifact/org.ow2.asm/asm-tree/9.4
//    compileOnly("org.ow2.asm:asm-tree:9.4") // 依赖 asm

    testImplementation("junit:junit:4.13.2")
    testImplementation(gradleTestKit())
//    testImplementation("com.android.tools.build:gradle:8.1.1")
//    testImplementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
}
