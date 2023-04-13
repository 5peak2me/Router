package com.l3gacy.plugin.router

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 *
 * Created by J!nl!n on 2022/10/18.
 *
 * Copyright © 2022 J!nl!n™ Inc. All rights reserved.
 *
 */
internal class RouterClassVisitor(
    classVisitor: ClassVisitor,
    private val records: Map<String, Set<String>>,
) : ClassVisitor(Opcodes.ASM9, classVisitor) {

    /**
     * [Special Methods](https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-2.html#jvms-2.9)
     */
    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val oldVisitor = super.visitMethod(access, name, descriptor, signature, exceptions)
        // 字节码层面，方法名称是 静态代码块 static {  } 的标识 <clinit>
        // <clinit> 在 JVM 第一次加载class文件时调用，包括静态变量初始化语句和静态块的执行
        return if (name == "<clinit>") RouterMethodVisitor(
            oldVisitor, records, access, name, descriptor
        ) else oldVisitor
    }

}