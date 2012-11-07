// Copyright 2009 Google Inc. All Rights Reserved.

package sandbox.instrumentation;

import org.objectweb.asm.*;
import sandbox.lists.NativeWhiteList;

/**
 * In charge of instrumenting an entire class. Does nothing but hand off the
 * instrumenting of individual methods to MemoryMethodAdapter objects
 */
class ClassAdapter extends org.objectweb.asm.ClassVisitor{

    public ClassAdapter(ClassVisitor cv) {
        super(Opcodes.ASM4, cv);
    }

    String name;
    @Override
    public void visit(
            int version,
            int access,
            String name,
            String signature,
            String superName,
            String[] interfaces){

        this.name = name;
        cv.visit(version, access, name, signature, superName, interfaces);

    }
    @Override
    public MethodVisitor visitMethod(int access,
                                     String base,
                                     String desc,
                                     String signature,
                                     String[] exceptions) {

        // remove the `native` modifier on a method if it is not on the
        // whitelist for native methods
        MethodVisitor mv = cv.visitMethod(
                NativeWhiteList.allowed(name, base) ? access : access & ~Opcodes.ACC_NATIVE,
                base,
                desc,
                signature,
                exceptions
        );

        MemoryMethodAdapter mma = new MemoryMethodAdapter(mv);
        BanNativesMethodAdapter bnma = new BanNativesMethodAdapter(mma);
        BytecodeMethodAdapter bma = new BytecodeMethodAdapter(bnma);
        return bma;
    }
}
