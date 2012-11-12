// Copyright 2009 Google Inc. All Rights Reserved.

package sandbox.instrumentation;

import org.objectweb.asm.*;
import org.objectweb.asm.util.TraceMethodVisitor;

/**
 * In charge of instrumenting an entire class. Does nothing but hand off the
 * instrumenting of individual methods to MemoryMethodAdapter objects
 */
class ClassAdapter extends org.objectweb.asm.ClassVisitor{
    ClassWriter cw;
    public ClassAdapter(ClassWriter cw) {
        super(Opcodes.ASM4, cw);
        this.cw = cw;
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
        System.out.println("Checking Class: " + name);
        cv.visit(version, access, name, signature, superName, interfaces);

    }
    @Override
    public MethodVisitor visitMethod(int access,
                                     String base,
                                     String desc,
                                     String signature,
                                     String[] exceptions) {


        MethodVisitor mv = cv.visitMethod(
                access ,
                base,
                desc,
                signature,
                exceptions
        );
        /* Only memory adapter */
        // return new MemoryMethodAdapter(new TraceMethodVisitor(mv, new CustomTextifier()));
        /* Only bytecode adapter */
        // return new BytecodeMethodAdapter(new TraceMethodVisitor(mv, new CustomTextifier()));
        /* Bytecode then memory */
        return new BytecodeMethodAdapter(new MemoryMethodAdapter(new TraceMethodVisitor(mv, new CustomTextifier())));
    }

}
