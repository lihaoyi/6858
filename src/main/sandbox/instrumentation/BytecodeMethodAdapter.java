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
    private final String checkerMethod = "checkInstructionCount";
    private final String checkerSignature = "(I)V";

    private final int MAX_INSTRUCTION_BLOCK_SIZE = 100;

    private int instruction_count = 0;

    public BytecodeMethodAdapter(MethodVisitor methodVisitor) {
        super(Opcodes.ASM4, methodVisitor);
    }

    /* Insert check instruction count for the preceding basic block of instructions */
    private void checkCurrentInstructionCount() {
        super.visitIntInsn(Opcodes.SIPUSH, instruction_count);
        super.visitMethodInsn(Opcodes.INVOKESTATIC, recorderClass, checkerMethod, checkerSignature);
        instruction_count = 0;
    }

    /* Insert check instruction count, if our current block exceeds maximum
     * allowed instruction block size */
    private boolean checkBlockInstructionCount() {
        if (instruction_count >= MAX_INSTRUCTION_BLOCK_SIZE) {
            checkCurrentInstructionCount();
            return true;
        }
        return false;
    }

    @Override
    public void visitInsn(final int opcode) {
        checkBlockInstructionCount();

        instruction_count += 1;
        /* Control flow change */
        if (opcode == Opcodes.IRETURN || opcode == Opcodes.LRETURN ||
            opcode == Opcodes.FRETURN || opcode == Opcodes.DRETURN ||
            opcode == Opcodes.ARETURN || opcode == Opcodes.RETURN) {
            checkCurrentInstructionCount();
        }
        mv.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(final int opcode, final int operand) {
        checkBlockInstructionCount();

        instruction_count += 1;
        mv.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(final int opcode, final int var) {
        checkBlockInstructionCount();

        instruction_count += 1;
        /* Control flow change */
        if (opcode == Opcodes.RET) {
            checkCurrentInstructionCount();
        }
        mv.visitVarInsn(opcode, var);
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        checkBlockInstructionCount();

        instruction_count += 1;
        mv.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(final int opcode, final String owner,
            final String name, final String desc) {
        checkBlockInstructionCount();

        instruction_count += 1;
        mv.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner,
            final String name, final String desc) {
        checkBlockInstructionCount();

        instruction_count += 1;
        /* Control flow change */
        checkCurrentInstructionCount();
        mv.visitMethodInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm,
            Object... bsmArgs) {
        checkBlockInstructionCount();

        instruction_count += 1;
        /* Control flow change */
        checkCurrentInstructionCount();
        mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }

    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
        checkBlockInstructionCount();

        instruction_count += 1;
        /* Control flow change */
        checkCurrentInstructionCount();
        mv.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLdcInsn(final Object cst) {
        checkBlockInstructionCount();

        instruction_count += 1;
        mv.visitLdcInsn(cst);
    }

    @Override
    public void visitIincInsn(final int var, final int increment) {
        checkBlockInstructionCount();

        instruction_count += 1;
        mv.visitIincInsn(var, increment);
    }

    @Override
    public void visitLabel(Label label) {
        /* Check the block preceding the label */
        checkCurrentInstructionCount();
        mv.visitLabel(label);
    }

    @Override
    public void visitTableSwitchInsn(final int min, final int max,
            final Label dflt, final Label... labels) {
        checkBlockInstructionCount();

        instruction_count += 1;
        /* Control flow change */
        checkCurrentInstructionCount();
        mv.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
            final Label[] labels) {
        checkBlockInstructionCount();

        instruction_count += 1;
        /* Control flow change */
        checkCurrentInstructionCount();
        mv.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        checkBlockInstructionCount();

        instruction_count += 1;
        mv.visitMultiANewArrayInsn(desc, dims);
    }
}

