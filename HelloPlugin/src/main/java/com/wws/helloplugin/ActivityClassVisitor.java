package com.wws.helloplugin;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ActivityClassVisitor extends ClassVisitor{

    private static final String ACTIVITY_SUPER_NAME = "androidx/appcompat/app/AppCompatActivity";
    private static final String ON_PAUSE = "onPause";
    private static final String ON_RESUME = "onResume";

    private String superName = null;
    private boolean visitedOnPause = false;
    private boolean visitedOnResume = false;

    public ActivityClassVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    public ActivityClassVisitor(ClassVisitor classVisitor) {
        // ASM API的版本为API 9
        super(Opcodes.ASM9, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        // 排除掉抽象类
        if ((access & Opcodes.ACC_ABSTRACT) != Opcodes.ACC_ABSTRACT){
            this.superName = superName;
        }
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        // AppCompatActivity的子类
        if (superName != null && superName.equals(ACTIVITY_SUPER_NAME)) {


        }
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return super.visitMethod(access, name, descriptor, signature, exceptions);
    }



}
