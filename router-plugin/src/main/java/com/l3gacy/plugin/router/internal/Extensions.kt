package com.l3gacy.plugin.router.internal

/**
 *
 * Created by J!nl!n on 2022/10/2.
 *
 * Copyright © 2022 J!nl!n™ Inc. All rights reserved.
 *
 */

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KaptExtension
import java.io.File

private const val APT_OPTION_MODULE_NAME = "moduleName"
private const val APT_OPTION_LOGGABLE = "loggable"

internal fun Project.apt(loggable: Boolean) {
//    val android = extensions.findByName("android") as? BaseExtension
    val android = extensions.findByType(CommonExtension::class.java)
    android?.apply {
        val options = mapOf(
            APT_OPTION_MODULE_NAME to name,
            APT_OPTION_LOGGABLE to loggable.toString()
        )
        defaultConfig.javaCompileOptions.annotationProcessorOptions.arguments(options)
        productFlavors.forEach { flavor ->
            flavor.javaCompileOptions.annotationProcessorOptions.arguments(options)
        }
    }
}

internal fun Project.kapt(loggable: Boolean) {
    // https://github.com/JetBrains/kotlin/blob/master/libraries/tools/kotlin-gradle-plugin/src/common/kotlin/org/jetbrains/kotlin/gradle/plugin/KaptExtension.kt
    val kapt = extensions.findByType(KaptExtension::class.java)
    kapt?.apply {
        arguments {
            arg(APT_OPTION_MODULE_NAME, name)
            arg(APT_OPTION_LOGGABLE, loggable)
        }
    }
}

internal fun String.separator(): String {
    return replace('.', File.separatorChar).replace('/', File.separatorChar)
}
