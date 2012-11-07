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

        boolean isNative = (access & Opcodes.ACC_NATIVE) != 0;
        boolean rewrite = isNative && !NativeWhiteList.allowed(name, base);
        // remove the `native` modifier on a method if it is not on the
        // whitelist for native methods
        MethodVisitor mv = cv.visitMethod(
                rewrite ? access & ~Opcodes.ACC_NATIVE : access,
                base,
                desc,
                signature,
                exceptions
        );

        if(isNative && rewrite){
            // if this is a native method we want to rewrite, do something
            // about it toString() turn it into a safe, non-native method
            return new BanNativesMethodAdapter(mv);
        }else if (isNative && !rewrite){
            // if this is a native method we do not want to rewrite, don't
            // do anything
            return mv;
        }else{
            // if this is a java method, then do all the instrumentation
            // magic
            return new BytecodeMethodAdapter(new MemoryMethodAdapter(mv));
        }
    }
}
