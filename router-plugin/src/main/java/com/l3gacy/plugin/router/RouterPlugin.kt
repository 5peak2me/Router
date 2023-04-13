package com.l3gacy.plugin.router

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BasePlugin
import com.l3gacy.plugin.internal.property
import com.l3gacy.plugin.router.internal.apt
import com.l3gacy.plugin.router.internal.kapt
import com.l3gacy.plugin.router.task.RouterClassesTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.register
import java.util.Locale

/**
 *
 * Created by J!nl!n on 2022/12/9.
 *
 * Copyright © 2022 J!nl!n™ Inc. All rights reserved.
 *
 */
@Suppress("UnstableApiUsage")
class RouterPlugin : Plugin<Project> {

    override fun apply(target: Project) {

        with(target) {
            // com.android.application                      -> AppPlugin::class.java
            // com.android.library                          -> LibraryPlugin::class.java
            // com.android.test                             -> TestPlugin::class.java
            // com.android.dynamic-feature, added in 3.2    -> DynamicFeaturePlugin::class.java
            require(plugins.hasPlugin(BasePlugin::class.java).not()) {
                "android plugin required."
            }

            val hasKotlin = KOTLIN_PLUGINS.any(plugins::hasPlugin)
            if (hasKotlin) {
                if (KAPT_PLUGINS.all(plugins::hasPlugin).not()) {
                    plugins.apply("kotlin-kapt")
                }
            }

            // Add dependencies
            dependencies.add("implementation", NOTATION_ROUTER)
            dependencies.add(
                if (hasKotlin) "kapt" else "annotationProcessor", NOTATION_COMPILER
            )

            val loggable = property(KEY_ROUTER_COMPILER_LOG, false)

            apt(loggable)

            kapt(loggable)

            plugins.withType(AppPlugin::class.java) {
                val androidComponents =
                    extensions.findByType(AndroidComponentsExtension::class.java)
                androidComponents?.onVariants { variant ->
                    val name = "gather${variant.name.capitalize(Locale.ROOT)}RouteTables"
                    val taskProvider = tasks.register<RouterClassesTask>(name) {
                        group = "router"
                        description = "Generate route tables for ${variant.name}"
                        bootClasspath.set(androidComponents.sdkComponents.bootClasspath)
                        classpath = variant.compileClasspath
                    }
                    variant.artifacts.forScope(ScopedArtifacts.Scope.ALL)
                        .use(taskProvider)
                        .toTransform(
                            ScopedArtifact.CLASSES,
                            RouterClassesTask::jars,
                            RouterClassesTask::dirs,
                            RouterClassesTask::output,
                        )
                }
            }
        }
    }

    @Suppress("SpellCheckingInspection")
    private companion object {

        private const val DEFAULT_ROUTER_RUNTIME_VERSION = "1.7.6"
        private const val DEFAULT_ROUTER_COMPILER_VERSION = "1.7.6"

        private const val NOTATION_ROUTER =
            "com.chenenyu.router:router:$DEFAULT_ROUTER_RUNTIME_VERSION"
        private const val NOTATION_COMPILER =
            "com.chenenyu.router:compiler:$DEFAULT_ROUTER_COMPILER_VERSION"

        /**
         * 打印生成器日志的键值，可通过在 gradle.properties 文件中声明
         * ```
         * zeppx.router.compiler.log = true
         * ```
         */
        private const val KEY_ROUTER_COMPILER_LOG = "zeppx.router.compiler.log"

        // https://github.com/JetBrains/kotlin/blob/master/libraries/tools/kotlin-gradle-plugin/src/common/resources/META-INF/gradle-plugins/kotlin-android.properties
        private val KOTLIN_PLUGINS = arrayOf(
            "kotlin-android",
            "org.jetbrains.kotlin.android"
        )

        // https://github.com/JetBrains/kotlin/blob/master/libraries/tools/kotlin-gradle-plugin/src/common/resources/META-INF/gradle-plugins/kotlin-kapt.properties
        private val KAPT_PLUGINS = arrayOf(
            "kotlin-kapt",
            "org.jetbrains.kotlin.kapt"
        )

        private val excludes = listOf(
            // 类库本身不做插桩操作
            "com/chenenyu/router/annotation/**",
            "org/objectweb/asm/**",
            "io/reactivex/**",
            "com/facebook/**",
            "com/google/**",
            "android/**",
            "androidx/**",
            "kotlin/**",
            "kotlinx/**",
            "com/android/**",
            "android/support/**",
            "okhttp/**",
            "okhttp3/**", // https://github.com/square/okhttp
            "retrofit2/**", // https://github.com/square/retrofit
            "okio/**", // https://github.com/square/okio
            "dagger/**",
            "org/**",
            "**/R",
            "**/R$**",
            "**/BuildConfig",
            "**/Manifest",
            "**/databinding/**",
            "com/bumptech/glide/**", // https://github.com/bumptech/glide
            "com/airbnb/lottie/**", // https://github.com/airbnb/lottie-android
            "com/squareup/**", // https://github.com/airbnb/lottie-android
            "org/koin/**", // https://github.com/InsertKoinIO/koin
            "coil/**", // https://github.com/coil-kt/coil
            "com/alipay/**",
            "com/amap/**",
            "com/tencent/**",
            "com/twitter/**",
            "com/datatheorem/android/**",
            "de/greenrobot/**",
            "io/github/**",
            "net/sourceforge/**",
            "com/mobeta/android/dslv/**",
            "com/beaglebuddy/**"
        )
    }

}
