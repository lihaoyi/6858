package sandbox.instrumentation;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.HashSet;
import java.util.Set;

/* TODO:
 * - decide what to do with long chains of instructions with no control transfer (e.g. check every 100 bytecodes)
 */

/**
 * Method adapter meant to add instruction-counting instrumentation to the
 * processed methods
 */
public class RedirectMethodAdapter extends MethodVisitor {

    Set<String> remapped = new HashSet<String>(){{
        add("java/io/File");
        add("java/lang/System");
    }};

    public String filter(String s){
        return remapped.contains(s) ? "safe/" + s : s;
    }
    public RedirectMethodAdapter(MethodVisitor methodVisitor) {
        super(Opcodes.ASM4, methodVisitor);
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        mv.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitMethodInsn(final int opcode,
                                final String owner,
                                final String name,
                                final String desc) {

        mv.visitMethodInsn(opcode, filter(owner), name, desc);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
        mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }


    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        mv.visitMultiANewArrayInsn(desc, dims);
    }
}

