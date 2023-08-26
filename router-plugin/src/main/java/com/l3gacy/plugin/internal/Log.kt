/*
 * Copyright (c) 2022 Zepp Health. All Rights Reserved.
 */

package com.l3gacy.plugin.internal

import groovy.json.JsonOutput
import groovy.xml.XmlUtil
import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logging
import org.slf4j.helpers.MessageFormatter

/**
 *
 * Created by J!nl!n on 2022/10/2.
 *
 * Copyright © 2022 J!nl!n™ Inc. All rights reserved.
 *
 * 控制台输出颜色日志工具类
 *
 * [How to print color in console using System.out.println?](https://stackoverflow.com/questions/5762491/how-to-print-color-in-console-using-system-out-println)
 *
 *
 * | Order | Name        | Command line option                   | Levels outputted       |
 * | ----- | ----------- | ------------------------------------- | ---------------------- |
 * | 6     | `ERROR`     | does not have option (always printed) | `ERROR`                |
 * | 5     | `QUIET`     | `-q` or `--quiet`                     | `QUIET` and higher     |
 * | 4     | `WARNING`   | `-w` or `--warn`                      | `WARNING` and higher   |
 * | 3     | `LIFECYCLE` | when no option is provided            | `LIFECYCLE` and higher |
 * | 2     | `INFO`      | `-i` or `--info`                      | `INFO` and higher      |
 * | 1     | `DEBUG`     | `-d` or `--debug`                     | `DEBUG` and higher     |
 *
 *
 * [Logging](https://docs.gradle.org/current/userguide/logging.html)
 * ```
 * error > quiet > warn > lifecycle > info > debug > [trace]
 *
 * logger.error("An error log message.")
 * logger.quiet("An info log message which is always logged.")
 * logger.warn("A warning log message.")
 * logger.lifecycle("A lifecycle info log message.")
 * logger.info("An info log message.")
 * logger.debug("A debug log message.")
 * logger.trace("A trace log message.") // Gradle never logs TRACE level logs
 * ```
 */
@Suppress("unused")
internal object Log {

    private const val TAG = "[>>> Router <<<]"

    private val logger = Logging.getLogger(TAG)

    private const val ANSI_RESET = "\u001B[0m"

    private enum class AnsiColor(val color: String) {
        RED("\u001B[31m"), // ANSI_RED
        PURPLE("\u001B[35m"), // ANSI_PURPLE
        YELLOW("\u001B[33m"), // ANSI_YELLOW
        CYAN("\u001B[36m"), // ANSI_CYAN
        GREEN("\u001B[32m"), // ANSI_GREEN
        BLUE("\u001B[34m"), // ANSI_BLUE
//        WHITE("\u001B[37m"), // ANSI_WHITE
    }

    private val LogLevel.color: String
        get() = when (this) {
            LogLevel.DEBUG -> AnsiColor.BLUE.color
            LogLevel.INFO -> AnsiColor.GREEN.color
            LogLevel.LIFECYCLE -> AnsiColor.CYAN.color
            LogLevel.WARN -> AnsiColor.YELLOW.color
            LogLevel.QUIET -> AnsiColor.PURPLE.color
            LogLevel.ERROR -> AnsiColor.RED.color
        }

    private fun log(level: LogLevel, message: Any, vararg args: Any?) {
        val tuple = MessageFormatter.arrayFormat(message.toString(), args)
        logger.log(level, "🐘 👉${level.color} {}$ANSI_RESET", tuple.message, line())
    }

    fun v(message: Any, vararg args: Any?) = log(LogLevel.QUIET, message, args)

    fun e(message: Any, vararg args: Any?) = log(LogLevel.ERROR, message, args)

    fun w(message: Any, vararg args: Any?) = log(LogLevel.WARN, message, args)

    fun wtf(message: Any, vararg args: Any?) = log(LogLevel.LIFECYCLE, message, args)

    fun i(message: Any, vararg args: Any?) = log(LogLevel.INFO, message, args)

    fun d(message: Any, vararg args: Any?) = log(LogLevel.DEBUG, message, args)

    /**
     * 格式化输出 JSON
     * @param value
     */
    fun json(value: Any) = log(LogLevel.QUIET, JsonOutput.prettyPrint(value.toString()))

    /**
     * 格式化输出 XML
     * @param value
     */
    fun xml(value: Any) = log(LogLevel.QUIET, XmlUtil.serialize(value.toString()))

    private fun line(): String {
        return Thread.currentThread().stackTrace[3].let {
            val link = "(${it.fileName}:${it.lineNumber})"
            val path = "${it.className.substringAfterLast(".")}.${it.methodName}"
//            if (path.length + link.length > 80) {
//                "${path.take(80 - link.length)}...${link}"
//            } else {
//                "$path$link"
//            }
            "$path$link"
        }
    }

}
