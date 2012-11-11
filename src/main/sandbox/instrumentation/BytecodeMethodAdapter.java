package sandbox.instrumentation;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Label;
import org.objectweb.asm.Handle;

/* TODO:
 * - fix some FIXMEs to properly compute bytecode size based on opcode/operands
 * - decide what to do with long chains of instructions with no control transfer (e.g. check every 100 bytecodes)
 */

/**
 * Method adapter meant to add bytecode-counting instrumentation to the
 * processed methods
 *
 * Size counting code adapted from ASM4
 * src/org/objectweb/asm/commons/CodeSizeEvaluator.java
 */
public class BytecodeMethodAdapter extends MethodVisitor {

    private final String recorderClass = "sandbox/runtime/Recorder";
    private final String checkerMethod = "checkBytecodeCount";
    private final String checkerSignature = "(I)V";

    private int bytecode_count = 0;

    public BytecodeMethodAdapter(MethodVisitor methodVisitor) {
        super(Opcodes.ASM4, methodVisitor);
    }

    /* Insert check bytecode count for the previous block of bytecodes */
    private void checkCurrentBytecodeCount() {
        super.visitIntInsn(Opcodes.SIPUSH, bytecode_count);
        super.visitMethodInsn(Opcodes.INVOKESTATIC, recorderClass, checkerMethod, checkerSignature);
        bytecode_count = 0;
    }

    @Override
    public void visitInsn(final int opcode) {
        bytecode_count += 1;
        /* Control flow change */
        if (opcode == Opcodes.IRETURN || opcode == Opcodes.LRETURN ||
            opcode == Opcodes.FRETURN || opcode == Opcodes.DRETURN ||
            opcode == Opcodes.ARETURN || opcode == Opcodes.RETURN) {
            checkCurrentBytecodeCount();
        }
        mv.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(final int opcode, final int operand) {
        if (opcode == Opcodes.SIPUSH) {
            bytecode_count += 3;
        } else {
            bytecode_count += 2;
        }
        mv.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(final int opcode, final int var) {
        if (var < 4 && opcode != Opcodes.RET) {
            bytecode_count += 1;
        } else if (var >= 256) {
            bytecode_count += 4;
        } else {
            bytecode_count += 2;
        }

        /* Control flow change */
        if (opcode == Opcodes.RET) {
            checkCurrentBytecodeCount();
        }
        mv.visitVarInsn(opcode, var);
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        bytecode_count += 3;
        mv.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(final int opcode, final String owner,
            final String name, final String desc) {
        bytecode_count += 3;
        mv.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner,
            final String name, final String desc) {
        if (opcode == Opcodes.INVOKEINTERFACE) {
            bytecode_count += 5;
        } else {
            bytecode_count += 3;
        }
        /* Control flow change */
        checkCurrentBytecodeCount();
        mv.visitMethodInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm,
            Object... bsmArgs) {
        bytecode_count += 5;
        /* Control flow change */
        checkCurrentBytecodeCount();
        mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }

    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
        /* FIXME */
        // minSize += 3;
        if (opcode == Opcodes.GOTO || opcode == Opcodes.JSR) {
            // maxSize += 5;
        } else {
            // maxSize += 8;
        }
        bytecode_count += 11;
        /* Control flow change */
        checkCurrentBytecodeCount();
        mv.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLdcInsn(final Object cst) {
        if (cst instanceof Long || cst instanceof Double) {
            bytecode_count += 3;
        } else {
            /* FIXME */
            // minSize += 2;
            // maxSize += 3;
            bytecode_count += 3;
        }
        mv.visitLdcInsn(cst);
    }

    @Override
    public void visitIincInsn(final int var, final int increment) {
        if (var > 255 || increment > 127 || increment < -128) {
            bytecode_count += 6;
        } else {
            bytecode_count += 3;
        }
        mv.visitIincInsn(var, increment);
    }

    /* FIXME */
    @Override
    public void visitTableSwitchInsn(final int min, final int max,
            final Label dflt, final Label... labels) {
        // minSize += 13 + labels.length * 4;
        // maxSize += 16 + labels.length * 4;
        bytecode_count += 16 + labels.length * 4;
        /* Control flow change */
        checkCurrentBytecodeCount();
        mv.visitTableSwitchInsn(min, max, dflt, labels);
    }

    /* FIXME */
    @Override
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
            final Label[] labels) {
        // minSize += 9 + keys.length * 8;
        // maxSize += 12 + keys.length * 8;
        bytecode_count += 12 + keys.length * 8;
        /* Control flow change */
        checkCurrentBytecodeCount();
        mv.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        bytecode_count += 4;
        mv.visitMultiANewArrayInsn(desc, dims);
    }
}

