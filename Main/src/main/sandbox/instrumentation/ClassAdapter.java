// Copyright 2009 Google Inc. All Rights Reserved.

package sandbox.instrumentation;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.objectweb.asm.commons.JSRInlinerAdapter;

/**
 * Instruments bytecodes that allocate heap memory to call a recording hook.
 * A <code>ClassAdapter</code> that processes methods with a
 * <code>MethodAdapter</code> to instrument heap allocations.
 *
 * @author jeremymanson@google.com (Jeremy Manson)
 * @author fischman@google.com (Ami Fischman) (Original Author)
 */
class ClassAdapter extends org.objectweb.asm.ClassVisitor{

    public ClassAdapter(ClassVisitor cv) {
        super(Opcodes.ASM4, cv);
    }

    /**
     * For each method in the class being instrumented, <code>visitMethod</code>
     * is called and the returned MethodVisitor is used to visit the method.
     * Note that a new MethodVisitor is constructed for each method.
     */
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
