// Copyright 2009 Google Inc. All Rights Reserved.

package sandbox.instrumentation;

import org.objectweb.asm.*;
import org.objectweb.asm.util.TraceMethodVisitor;
import org.objectweb.asm.commons.JSRInlinerAdapter;

import java.util.Map;
import java.util.HashMap;

/**
 * In charge of instrumenting an entire class. Does nothing but hand off the
 * instrumenting of individual methods to MemoryMethodAdapter objects
 */
public class ClassAdapter extends org.objectweb.asm.ClassVisitor{
    public static Map<String, Integer> fieldCountMap;
    ClassWriter cw;
    public ClassAdapter(ClassWriter cw) {
        super(Opcodes.ASM4, cw);
        this.cw = cw;
        if (this.fieldCountMap == null) {
            this.fieldCountMap = new HashMap<String, Integer>();
        }
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
        this.fieldCountMap.put(name, 0);
    }

    @Override
    public FieldVisitor visitField (int access, String name, String desc,
        String signature, Object value) {
      //System.out.println("TFK: visiting field of class: name: " + name + 
      //    "desc: "+desc+ " signature " + signature);
      // Increment this class's field count.
      if (!fieldCountMap.containsKey(this.name)) {
        fieldCountMap.put(this.name, 0);
      }
      // TODO(TFK): Take into account type of field, if necessary.
      fieldCountMap.put(this.name, fieldCountMap.get(this.name) + 1);
      return super.visitField(access, name, desc, signature, value);
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

        String methodID = this.name + access + base + desc + signature + exceptions;

        JSRInlinerAdapter jia = new JSRInlinerAdapter(mv, access, base, desc, signature, exceptions);
        return new InstructionMethodAdapter(new MemoryMethodAdapter(new TraceMethodVisitor(jia, new CustomTextifier())), methodID);
    }
}
