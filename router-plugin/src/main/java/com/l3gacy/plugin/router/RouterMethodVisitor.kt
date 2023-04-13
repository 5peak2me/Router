package com.l3gacy.plugin.router

import com.l3gacy.plugin.internal.Log
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

/**
 *
 * Created by J!nl!n on 2022/10/18.
 *
 * Copyright © 2022 J!nl!n™ Inc. All rights reserved.
 *
 */
internal class RouterMethodVisitor(
    methodVisitor: MethodVisitor,
    private val records: Map<String, Set<String>>,
    access: Int,
    name: String?,
    descriptor: String?
) : AdviceAdapter(ASM9, methodVisitor, access, name, descriptor) {

//    private val records = listOf(
//        "com/chenenyu/router/template/RouteTable" to setOf(
//            "com/chenenyu/router/apt/Module1RouteTable",
//            "com/chenenyu/router/apt/Module2RouteTable",
//            "com/chenenyu/router/apt/AppRouteTable",
//        ),
//        "com/chenenyu/router/template/InterceptorTable" to setOf("com/chenenyu/router/apt/AppInterceptorTable"),
//        "com/chenenyu/router/template/TargetInterceptorsTable" to setOf("com/chenenyu/router/apt/AppTargetInterceptorsTable")
//    )

    /**
     * Java code:
     * ``` java
     * static {
     *     new AppRouteTable().handle(routeTable);
     *     new AppInterceptorTable().handle(interceptorTable);
     *     new AppTargetInterceptorsTable().handle(targetInterceptorsTable);
     *     // other modules' table...
     *}
     * ```
     * ``` java
     * ASM code:
     *
     *     mv.visitTypeInsn(Opcodes.NEW, "com/chenenyu/router/apt/AppRouteTable");
     *     mv.visitInsn(Opcodes.DUP);
     *     mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/chenenyu/router/apt/AppRouteTable", "<init>", "()V", false);
     *     mv.visitFieldInsn(Opcodes.GETSTATIC, "com/chenenyu/router/app/AsmTest", "routeTable", "Ljava/util/Map;");
     *     mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/chenenyu/router/apt/AppRouteTable", "handle", "(Ljava/util/Map;)V", false);
     *     mv.visitTypeInsn(Opcodes.NEW, "com/chenenyu/router/apt/AppInterceptorTable");
     *     mv.visitInsn(Opcodes.DUP);
     *     mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/chenenyu/router/apt/AppInterceptorTable", "<init>", "()V", false);
     *     mv.visitFieldInsn(Opcodes.GETSTATIC, "com/chenenyu/router/app/AsmTest", "interceptorTable", "Ljava/util/Map;");
     *     mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/chenenyu/router/apt/AppInterceptorTable", "handle", "(Ljava/util/Map;)V", false);
     *     mv.visitTypeInsn(Opcodes.NEW, "com/chenenyu/router/apt/AppTargetInterceptorsTable");
     *     mv.visitInsn(Opcodes.DUP);
     *     mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/chenenyu/router/apt/AppTargetInterceptorsTable", "<init>", "()V", false);
     *     mv.visitFieldInsn(Opcodes.GETSTATIC, "com/chenenyu/router/app/AsmTest", "targetInterceptorsTable", "Ljava/util/Map;");
     *     mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "com/chenenyu/router/apt/AppTargetInterceptorsTable", "handle", "(Ljava/util/Map;)V", false);
     *     mv.visitInsn(Opcodes.RETURN);
     * ```
     */
    override fun visitInsn(opcode: Int) {
        if (opcode == RETURN) {
            Log.v("visitInsn 我被执行了")
            records.forEach { (key, value) ->
                Log.v("$key - $value")
                value.forEach { classname ->
                    mv.visitTypeInsn(NEW, classname)
                    mv.visitInsn(DUP)
                    mv.visitMethodInsn(INVOKESPECIAL, classname, "<init>", "()V", false)
                    mv.visitFieldInsn(
                        GETSTATIC,
                        TEMPLATE_APT_HUB,
                        key.getFieldNameByInterface(),
                        "Ljava/util/Map;"
                    )
                    mv.visitMethodInsn(INVOKEVIRTUAL, classname, "handle", "(Ljava/util/Map;)V", false)
                }
            }

//            records.forEach {
//                Log.d("${it.first} - ${it.second}")
//                it.second.forEach { classname ->
//                    mv.visitTypeInsn(NEW, classname)
//                    mv.visitInsn(DUP)
//                    mv.visitMethodInsn(INVOKESPECIAL, classname, "<init>", "()V", false)
//                    mv.visitFieldInsn(
//                        GETSTATIC,
//                        TEMPLATE_APT_HUB,
//                        it.getFieldNameByInterface(),
//                        "Ljava/util/Map;"
//                    )
//                    mv.visitMethodInsn(INVOKEVIRTUAL, classname, "handle", "(Ljava/util/Map;)V", false)
//                }
//            }
        }
        super.visitInsn(opcode)
    }

    @Synchronized
    private fun String.getFieldNameByInterface(): String {
        return when (this) {
            TEMPLATE_ROUTE_TABLE -> "routeTable"
            TEMPLATE_INTERCEPTOR_TABLE -> "interceptorTable"
            TEMPLATE_TARGET_INTERCEPTORS_TABLE -> "targetInterceptorsTable"
            else -> "error"
        }
    }

    @Synchronized
    private fun Pair<String, Set<String>>.getFieldNameByInterface(): String {
        return when (first) {
            TEMPLATE_ROUTE_TABLE -> "routeTable"
            TEMPLATE_INTERCEPTOR_TABLE -> "interceptorTable"
            TEMPLATE_TARGET_INTERCEPTORS_TABLE -> "targetInterceptorsTable"
            else -> "error"
        }
    }

    private companion object {
        const val TEMPLATE_APT_HUB = "com/chenenyu/router/AptHub"
        const val TEMPLATE_ROUTE_TABLE = "com/chenenyu/router/template/RouteTable"
        const val TEMPLATE_INTERCEPTOR_TABLE = "com/chenenyu/router/template/InterceptorTable"
        const val TEMPLATE_TARGET_INTERCEPTORS_TABLE =
            "com/chenenyu/router/template/TargetInterceptorsTable"
    }
}