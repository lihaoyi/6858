package sandbox.instrumentation;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Label;
import org.objectweb.asm.Handle;

/* TODO:
 * - decide what to do with long chains of instructions with no control transfer (e.g. check every 100 bytecodes)
 */

/**
 * Method adapter meant to add instruction-counting instrumentation to the
 * processed methods
 */
public class BytecodeMethodAdapter extends MethodVisitor {

    private final String recorderClass = "sandbox/runtime/Recorder";
    private final String checkerMethod = "checkBytecodeCount";
    private final String checkerSignature = "(I)V";

    private int instruction_count = 0;

    public BytecodeMethodAdapter(MethodVisitor methodVisitor) {
        super(Opcodes.ASM4, methodVisitor);
    }

    /* Insert check bytecode count for the preceding basic block of bytecodes */
    private void checkCurrentBytecodeCount() {
        super.visitIntInsn(Opcodes.SIPUSH, instruction_count);
        super.visitMethodInsn(Opcodes.INVOKESTATIC, recorderClass, checkerMethod, checkerSignature);
        instruction_count = 0;
    }

    @Override
    public void visitInsn(final int opcode) {
        instruction_count += 1;
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
        instruction_count += 1;
        mv.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(final int opcode, final int var) {
        instruction_count += 1;
        /* Control flow change */
        if (opcode == Opcodes.RET) {
            checkCurrentBytecodeCount();
        }
        mv.visitVarInsn(opcode, var);
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        instruction_count += 1;
        mv.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(final int opcode, final String owner,
            final String name, final String desc) {
        instruction_count += 1;
        mv.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner,
            final String name, final String desc) {
        instruction_count += 1;
        /* Control flow change */
        checkCurrentBytecodeCount();
        mv.visitMethodInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm,
            Object... bsmArgs) {
        instruction_count += 1;
        /* Control flow change */
        checkCurrentBytecodeCount();
        mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }

    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
        instruction_count += 1;
        /* Control flow change */
        checkCurrentBytecodeCount();
        mv.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLdcInsn(final Object cst) {
        instruction_count += 1;
        mv.visitLdcInsn(cst);
    }

    @Override
    public void visitIincInsn(final int var, final int increment) {
        instruction_count += 1;
        mv.visitIincInsn(var, increment);
    }

    @Override
    public void visitLabel(Label label) {
        /* Check the block preceding the label */
        checkCurrentBytecodeCount();
        mv.visitLabel(label);
    }

    @Override
    public void visitTableSwitchInsn(final int min, final int max,
            final Label dflt, final Label... labels) {
        instruction_count += 1;
        /* Control flow change */
        checkCurrentBytecodeCount();
        mv.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
            final Label[] labels) {
        instruction_count += 1;
        /* Control flow change */
        checkCurrentBytecodeCount();
        mv.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        instruction_count += 1;
        mv.visitMultiANewArrayInsn(desc, dims);
    }
}

