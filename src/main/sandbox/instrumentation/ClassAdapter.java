// Copyright 2009 Google Inc. All Rights Reserved.

package sandbox.instrumentation;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.commons.JSRInlinerAdapter;

/**
 * In charge of instrumenting an entire class. Does nothing but hand off the
 * instrumenting of individual methods to MemoryMethodAdapter objects
 */
class ClassAdapter extends org.objectweb.asm.ClassVisitor{

    public ClassAdapter(ClassVisitor cv) {
        super(Opcodes.ASM4, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String base, String desc,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, base, desc, signature, exceptions);
        JSRInlinerAdapter jsria = new JSRInlinerAdapter(mv, access, base, desc, signature, exceptions);

        MemoryMethodAdapter mma = new MemoryMethodAdapter(jsria);
        BanNativesMethodAdapter bnma = new BanNativesMethodAdapter(mma);
        BytecodeMethodAdapter bma = new BytecodeMethodAdapter(bnma);
        return bma;
    }
}
