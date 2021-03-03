package com.wws.helloplugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASM9;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.RETURN;

public class ActivityClassVisitor extends ClassVisitor {

    private static final String ACTIVITY_SUPER_NAME = "androidx/appcompat/app/AppCompatActivity";
    private static final String ON_PAUSE = "onPause";
    private static final String ON_RESUME = "onResume";

    private String superName = null;
    private boolean visitedOnPause = false;
    private boolean visitedOnResume = false;

    public ActivityClassVisitor(ClassVisitor classVisitor) {
        // ASM API的版本为API 9
        super(ASM9, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        // 排除掉抽象类
        if ((access & ACC_ABSTRACT) != ACC_ABSTRACT) {
            this.superName = superName;
        }
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        // AppCompatActivity的子类
        if (superName != null && superName.equals(ACTIVITY_SUPER_NAME)) {
            if (!visitedOnResume) {
                visitedOnResume = true;
                insertMethodAndLog(ON_RESUME);
            }

            if (!visitedOnPause) {
                visitedOnPause = true;
                insertMethodAndLog(ON_PAUSE);
            }

        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (superName != null && superName.equals(ACTIVITY_SUPER_NAME)) {
            if (ON_PAUSE.equals(name)) {
                visitedOnPause = true;
                addLogCodeForMethod(mv, name);

            } else if (ON_RESUME.equals(name)) {
                visitedOnResume = true;
                addLogCodeForMethod(mv, name);
            }
        }
        return mv;

    }

    private void addLogCodeForMethod(MethodVisitor mv, String methodName) {
        mv.visitLdcInsn("qiongtony");
        // 创建一个StringBuilder
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        // 调用StringBuilder的初始化方法
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        mv.visitVarInsn(ALOAD, 0);
        // 获取当前类的simplaName
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false);
        // 将当前类的simpleName追加到StringBuilder
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        // 将方法名追加进SB
        mv.visitLdcInsn("：" + methodName);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        // 调用 StringBuilder 的 toString 方法将 StringBuilder 转化为 String
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        // 调用Log.i
        mv.visitMethodInsn(INVOKESTATIC, "android/util/Log", "i", "(Ljava/lang/String;Ljava/lang/String;)I", false);
        mv.visitInsn(POP);
    }

    private void insertMethodAndLog(String methodName) {
        MethodVisitor mv = cv.visitMethod(ACC_PROTECTED, methodName, "()V", null, null);
        // 访问新方法填充方法逻辑，
        mv.visitCode();
        // 加载super方法
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "androidx/appcompat/app/AppCompatActivity", methodName, "()V", false);

        mv.visitLdcInsn("qiongtony");
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V", false);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getSimpleName", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitLdcInsn(": " + methodName);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKESTATIC, "android/util/Log", "d", "(Ljava/lang/String;Ljava/lang/String;)I", false);
        mv.visitInsn(POP);
        mv.visitInsn(RETURN);
        mv.visitEnd();
    }

}
