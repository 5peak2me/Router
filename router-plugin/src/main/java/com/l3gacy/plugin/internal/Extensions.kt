package com.l3gacy.plugin.internal

/**
 *
 * Created by J!nl!n on 2022/10/2.
 *
 * Copyright © 2022 J!nl!n™ Inc. All rights reserved.
 *
 */

import com.android.build.gradle.api.AndroidBasePlugin
import org.gradle.api.Project
import org.w3c.dom.NamedNodeMap
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.util.Locale
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.zip.ZipException

internal fun Closeable.closeQuietly() {
    try {
        close()
    } catch (exception: IOException) {
        // Ignore the exception.
    }
}

internal fun JarOutputStream.createFile(name: String, data: ByteArray) {
    try {
        putNextEntry(JarEntry(name.replace(File.separatorChar, '/')))
        write(data)
        closeEntry()
    } catch (e: ZipException) {
        // it's normal to have duplicated files in META-INF
        if (!name.startsWith("META-INF")) throw e
    }
}

internal fun JarOutputStream.createDirectory(name: String) {
    try {
        putNextEntry(JarEntry(name.replace(File.separatorChar, '/')))
        closeEntry()
    } catch (ignored: ZipException) {
        // it's normal that the directory already exists
    }
}

inline val String.capitalize: String
    get() = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

/**
 * 获取 Properties 属性
 *
 * @param key 键值
 * @param default 默认值
 *
 * @since 0.0.1
 */
//internal inline fun <reified T> Project.property(key: String, default: T): T =
//    project.findProperty(key) as? T ?: default
internal fun Project.property(key: String, default: Any): String =
    project.properties[key]?.toString() ?: default.toString()

@Suppress("NOTHING_TO_INLINE")
internal inline fun Project.isAndroid(): Boolean {
    return plugins.filterIsInstance(AndroidBasePlugin::class.java).isNotEmpty()
//    return project.plugins.toList().any { plugin -> plugin is AndroidBasePlugin }
}

inline fun NodeList.map(f: (Node) -> Unit) {
    // It's sad, but since we're modifying the Nodes in the list, we need to keep a copy to make
    // sure we actually visit all of them.
    val copy = ArrayList<Node>(length)
    for (i in 0 until length) copy.add(item(i))
    copy.forEach { f(it) }
}

inline fun NamedNodeMap.map(f: (Node) -> Unit) {
    // It's sad, but since we're modifying the Nodes in the map, we need to keep a copy to make
    // sure we actually visit all of them.
    val copy = ArrayList<Node>(length)
    for (i in 0 until length) copy.add(item(i))
    copy.forEach { f(it) }
}
