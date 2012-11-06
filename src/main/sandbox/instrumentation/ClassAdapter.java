// Copyright 2009 Google Inc. All Rights Reserved.

package sandbox.instrumentation;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.commons.JSRInlinerAdapter;

/**
 * In charge of instrumenting an entire class. Does nothing but hand off the
 * instrumenting of individual methods to MethodAdapter objects
 */
class ClassAdapter extends org.objectweb.asm.ClassVisitor{

    public ClassAdapter(ClassVisitor cv) {
        super(Opcodes.ASM4, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String base, String desc,
                                     String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, base, desc, signature, exceptions);

        if (mv != null) {

            JSRInlinerAdapter jsria = new JSRInlinerAdapter(mv, access, base, desc, signature, exceptions);

            MethodAdapter aimv = new MethodAdapter(jsria);

            LocalVariablesSorter lvs = new LocalVariablesSorter(access, desc, aimv);
            aimv.lvs = lvs;
            mv = lvs;
        }
        return mv;
    }
}
