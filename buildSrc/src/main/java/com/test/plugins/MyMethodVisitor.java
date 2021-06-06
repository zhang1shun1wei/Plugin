package com.test.plugins;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class MyMethodVisitor extends MethodVisitor {
    public MyMethodVisitor(int api) {
        super(api);
    }

    public MyMethodVisitor(MethodVisitor mv) {
        super(Opcodes.ASM4, mv);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (name.contains("show") || name.contains("android/widget/Toast")
                || name.contains("makeText")) {
            return;
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
        System.out.println("owner= " + owner);
        System.out.println("name= " + name);
        System.out.println("desc= " + desc);
    }
}
