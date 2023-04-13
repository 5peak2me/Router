plugins {
    id("java-library")
    id("java-gradle-plugin")
//    id("org.jetbrains.kotlin.jvm")
//    id("org.jetbrains.kotlin.jvm") version "1.7.20"
    id("com.jfrog.artifactory") version "4.29.2"
    `kotlin-dsl`
}

repositories {
    mavenCentral()
    google()
    gradlePluginPortal()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
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
//    implementation("com.chenenyu.router:router:1.7.6")

    implementation("com.joom.grip:grip:0.9.1")
//    implementation("org.json:json:20220924")

    implementation(gradleApi())

//    implementation("com.android.tools.build:gradle-api:7.4.2")
    // https://developer.android.com/studio/releases/gradle-plugin-api-updates?hl=zh-cn#support_for_transformations_based_on_whole_program_analysis
    implementation("com.android.tools.build:gradle:7.4.2")
//    compileOnly("com.android.tools:common:30.1.2")

    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.20")

    // https://search.maven.org/artifact/org.ow2.asm/asm-commons/9.4/jar
    implementation("org.ow2.asm:asm-commons:9.4") // 包含 asm 以及 asm-tree
    implementation("org.ow2.asm:asm-util:9.4") // 包含 asm 以及 asm-tree、asm-analysis
//    compileOnly("org.ow2.asm:asm-tree:9.4")
}
