package sandbox.instrumentation;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Method adapter meant to add bytecode-counting instrumentation to the
 * processed methods
 */
public class BytecodeMethodAdapter extends MethodVisitor {
    public BytecodeMethodAdapter(MethodVisitor methodVisitor) {
        super(Opcodes.ASM4, methodVisitor);
    }

}
