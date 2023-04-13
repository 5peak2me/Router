package com.l3gacy.plugin.router.task

import com.joom.grip.Grip
import com.joom.grip.GripFactory
import com.joom.grip.classes
import com.joom.grip.interfaces
import com.joom.grip.mirrors.getType
import com.l3gacy.plugin.internal.Log
import com.l3gacy.plugin.router.internal.separator
import com.l3gacy.plugin.router.RouterClassVisitor
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.CompileClasspath
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import javax.inject.Inject

/**
 * [Developing Parallel Tasks using the Worker API](https://docs.gradle.org/current/userguide/custom_tasks.html)
 *
 * [Developing Custom Gradle Task Types](https://docs.gradle.org/current/userguide/custom_tasks.html)
 */
abstract class RouterClassesTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val jars: ListProperty<RegularFile>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val dirs: ListProperty<Directory>

    @get:OutputFile
    abstract val output: RegularFileProperty

    @get:Classpath
    abstract val bootClasspath: ListProperty<RegularFile>

    @get:CompileClasspath
    abstract var classpath: FileCollection

    @Inject
    abstract fun getWorkerExecutor(): WorkerExecutor

    @TaskAction
    fun taskAction() {
        val inputs = (jars.get() + dirs.get()).map { it.asFile.toPath() }
        val classpaths = bootClasspath.get().map { it.asFile.toPath() }
            .toSet() + classpath.files.map { it.toPath() }

        println(bootClasspath.get().map { it.asFile.toPath() })

        val grip: Grip = GripFactory.newInstance(Opcodes.ASM9).create(classpaths + inputs)
        val query = grip select classes from inputs where interfaces { _, interfaces ->
            descriptors.map(::getType).any(interfaces::contains)
        }
        val classes = query.execute().classes

        val map = classes.groupBy({ it.interfaces.first().className.separator() },
            { it.name.separator() })

        Log.v(map.print { it.toString() })

        JarOutputStream(BufferedOutputStream(FileOutputStream(output.get().asFile))).use { jarOutput ->

            jars.get().forEach { file ->
//                println("handling jars:" + file.asFile.absolutePath)
                val jarFile = JarFile(file.asFile)
                jarFile.entries().iterator().forEach { jarEntry ->
                    if (jarEntry.isDirectory.not() &&
                        jarEntry.name.contains("com/chenenyu/router/AptHub", true)
                    ) {
                        println("Adding from jar ${jarEntry.name}")
                        jarOutput.putNextEntry(JarEntry(jarEntry.name))
                        jarFile.getInputStream(jarEntry).use {
                            val reader = ClassReader(it)
                            val writer = ClassWriter(reader, 0)
                            val visitor =
                                RouterClassVisitor(writer, map.mapValues { v -> v.value.toSet() })
                            reader.accept(visitor, 0)
                            jarOutput.write(writer.toByteArray())
                        }
                    } else {
                        kotlin.runCatching {
                            jarOutput.putNextEntry(JarEntry(jarEntry.name))
                            jarFile.getInputStream(jarEntry).use {
                                it.copyTo(jarOutput)
                            }
                        }
                    }
                    jarOutput.closeEntry()
                }
                jarFile.close()
            }
            dirs.get().forEach { directory ->
                println("handling " + directory.asFile.absolutePath)
                directory.asFile.walk().forEach { file ->
                    if (file.isFile) {
                        val relativePath = directory.asFile.toURI().relativize(file.toURI()).path
                        println("Adding from directory ${relativePath.replace(File.separatorChar, '/')}")
                        jarOutput.putNextEntry(JarEntry(relativePath.replace(File.separatorChar, '/')))
                        file.inputStream().use { inputStream ->
                            inputStream.copyTo(jarOutput)
                        }
                        jarOutput.closeEntry()
                    }
                }
            }
        }
    }

    private fun <K, V> Map<K, V?>.print(mapper: (V?) -> String): String =
        StringBuilder("\n{").also { sb ->
            this.iterator().forEach { entry ->
                sb.append("\n\t${entry.key} = ${mapper(entry.value)}")
            }
            sb.append("\n}")
        }.toString()

    companion object {
        @Suppress("SpellCheckingInspection")
        val descriptors = listOf(
            "Lcom/chenenyu/router/template/RouteTable;",
            "Lcom/chenenyu/router/template/InterceptorTable;",
            "Lcom/chenenyu/router/template/TargetInterceptorsTable;"
        )
    }

}
