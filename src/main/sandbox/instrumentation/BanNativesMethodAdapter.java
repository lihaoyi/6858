package sandbox.instrumentation;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Method adapter that's meant to remove native methods
 */
public class BanNativesMethodAdapter extends MethodVisitor {
    public BanNativesMethodAdapter(MethodVisitor methodVisitor) {
        super(Opcodes.ASM4, methodVisitor);
    }
}
