package com.l3gacy.plugin.router.task

import com.android.build.gradle.internal.tasks.BuildAnalyzer
import com.android.buildanalyzer.common.TaskCategory
import com.joom.grip.Grip
import com.joom.grip.GripFactory
import com.joom.grip.classes
import com.joom.grip.interfaces
import com.joom.grip.mirrors.getType
import com.l3gacy.plugin.internal.Log
import com.l3gacy.plugin.router.internal.separator
import com.l3gacy.plugin.router.asm.RouterClassVisitor
import com.l3gacy.plugin.internal.Stopwatch
import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

/**
 *
 * Created by J!nl!n on 2022/12/9.
 *
 * Copyright © 2022 J!nl!n™ Inc. All rights reserved.
 *
 * - [Developing Parallel Tasks using the Worker API](https://docs.gradle.org/current/userguide/custom_tasks.html)
 * - [Developing Custom Gradle Task Types](https://docs.gradle.org/current/userguide/custom_tasks.html)
 */
@CacheableTask
@BuildAnalyzer(primaryTaskCategory = TaskCategory.SOURCE_PROCESSING)
internal abstract class RouterClassesTask : DefaultTask() {

    @get:Incremental
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val jars: ListProperty<RegularFile>

    @get:Incremental
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val dirs: ListProperty<Directory>

    @get:OutputFile
    abstract val output: RegularFileProperty

    @get:Optional
    @get:OutputFile
    abstract val doc: RegularFileProperty

//    @Inject
//    abstract fun getWorkerExecutor(): WorkerExecutor

    @TaskAction
    fun taskAction(inputChanges: InputChanges) {
        val timer = Stopwatch()

        timer.start("Router::: Transform started on thread: [${Thread.currentThread().name}]")

        val inputs = (jars.get() + dirs.get()).map { it.asFile.toPath() }

        val grip: Grip = GripFactory.newInstance(Opcodes.ASM9).create(inputs)
        val query = grip select classes from inputs where interfaces { _, interfaces ->
            descriptors.map(::getType).any(interfaces::contains)
        }
        val classes = query.execute().classes

        timer.splitTime("Router::: Prepare referenced classes")

        val map = classes.groupBy({ it.interfaces.first().className.separator() }, { it.name.separator() })

        val closure = { jarOutput: JarOutputStream, inputSource: InputStream ->
            val reader = ClassReader(inputSource)
            val writer = ClassWriter(reader, ClassWriter.COMPUTE_FRAMES)
            val visitor = RouterClassVisitor(writer, map.mapValues { v -> v.value.toSet() })
            reader.accept(visitor, 0)
            jarOutput.write(writer.toByteArray())
            timer.splitTime("Router::: ASM modify class")
        }

        JarOutputStream(BufferedOutputStream(FileOutputStream(output.get().asFile))).use { jarOutput ->
            processJars(jarOutput, closure)
            processDirs(jarOutput)
        }

        if (doc.isPresent) {
            dumpJson(map)
            timer.splitTime("Router::: Dump Router tables time")
            Log.v("Router::: Router tables path : ${doc.asFile.get().path}")
        }

        timer.stop()
    }

    private fun dumpJson(map: Map<String, List<String>>) = doc.asFile.get().writeText(JsonOutput.toJson(map))

    private fun processDirs(jarOutput: JarOutputStream) {
        dirs.get().forEach { directory ->
            directory.asFile.walk().forEach { file ->
                if (file.isFile) {
                    val relativePath = directory.asFile.toURI().relativize(file.toURI()).path
                    jarOutput.putNextEntry(JarEntry(relativePath.replace(File.separatorChar, '/')))
                    file.inputStream().use { inputStream ->
                        inputStream.copyTo(jarOutput)
                    }
                    jarOutput.closeEntry()
                }
            }
        }
    }

    private fun processJars(jarOutput: JarOutputStream, block: (JarOutputStream, InputStream) -> Unit) {
        jars.get().forEach { file ->
            JarFile(file.asFile).use { jarFile ->
                jarFile.entries().iterator().forEach { jarEntry ->
                    if (!jarEntry.isDirectory && jarEntry.name.contains(TARGET_CLASS)) {
                        jarOutput.putNextEntry(JarEntry(jarEntry.name))
                        jarFile.getInputStream(jarEntry).use {
                            // Transforming AptHub.class with RouterClassesTask
                            block(jarOutput, it)
                        }
                    } else {
                        runCatching {
                            jarOutput.putNextEntry(JarEntry(jarEntry.name))
                            jarFile.getInputStream(jarEntry).use {
                                it.copyTo(jarOutput)
                            }
                        }/*.onFailure { e ->
                            Log.e("Copy jar entry failed. [entry:${jarEntry.name}]", e)
                        }*/
                    }
                    jarOutput.closeEntry()
                }
            }
        }
    }

//    private fun <K, V> Map<K, V?>.print(mapper: (V?) -> String): String =
//        StringBuilder("\n{").also { sb ->
//            this.iterator().forEach { entry ->
//                sb.append("\n\t${entry.key} = ${mapper(entry.value)}")
//            }
//            sb.append("\n}")
//        }.toString()

    @Suppress("SpellCheckingInspection")
    private companion object {

        /**
         * 需要处理的字节码
         */
        private const val TARGET_CLASS = "com/chenenyu/router/AptHub"

        /**
         * 实现的接口描述符
         */
        private val descriptors = listOf(
            "Lcom/chenenyu/router/template/RouteTable;",
            "Lcom/chenenyu/router/template/InterceptorTable;",
            "Lcom/chenenyu/router/template/TargetInterceptorsTable;"
        )
    }

}
