package sandbox.instrumentation;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Label;
import org.objectweb.asm.Handle;

class BasicBlocksRecord {
    private List<Integer> bb_icounts, bb_labels, bb_jump_targets;

    public BasicBlocksRecord() {
        bb_icounts = new ArrayList<Integer>();
        bb_labels = new ArrayList<Integer>();
        bb_jump_targets = new ArrayList<Integer>();
    }

    public void pushBasicBlock(int instructionCount, int labelIndex) {
        bb_icounts.add(instructionCount);
        bb_labels.add(labelIndex);
    }

    public int[] popBasicBlock() {
        int[] bb = new int[2];
        bb[0] = bb_icounts.remove(0);
        bb[1] = bb_labels.remove(0);
        return bb;
    }

    public int nextBasicBlockLabel() {
        return bb_labels.get(0);
    }

    public int numBasicBlocks() {
        return bb_labels.size();
    }

    public void markJumpTarget(int labelIndex) {
        bb_jump_targets.add(labelIndex);
    }

    public boolean checkJumpTarget(int labelIndex) {
        return bb_jump_targets.contains(labelIndex);
    }

    public String toString() {
        int i;
        String s = "";

        for (i = 0; i < bb_icounts.size(); i++) {
            s += "(count: " + bb_icounts.get(i) + ", label: " + bb_labels.get(i) + ", jump_target: " + bb_jump_targets.contains(bb_labels.get(i)) + "),";
        }

        return "\n[" + s + "]";
    }
}

/**
 * Method adapter meant to add instruction-counting instrumentation to the
 * processed methods
 *
 * First pass builds basic block record, second pass instruments.
 */
public class InstructionMethodAdapter extends MethodVisitor {

    private final String recorderClass = "sandbox/runtime/Recorder";
    private final String checkerMethod = "checkInstructionCount";
    private final String checkerSignature = "(I)V";

    private static HashMap<String,BasicBlocksRecord> MethodBasicBlocks = null;
    private boolean secondPass;

    private final int LABEL_INDEX_START = -1;
    private final int LABEL_INDEX_CALL  = -2;
    private final boolean DEBUG = true;

    private int icount = 0, lindex = LABEL_INDEX_START, lcount = 0;
    private HashMap<Label,Integer> labelIndices;
    private BasicBlocksRecord bbr;

    public InstructionMethodAdapter(MethodVisitor methodVisitor, String methodID) {
        super(Opcodes.ASM4, methodVisitor);
        labelIndices = new HashMap<Label,Integer>();

        if (MethodBasicBlocks == null) {
            MethodBasicBlocks = new HashMap<String,BasicBlocksRecord>();
        }

        if (MethodBasicBlocks.containsKey(methodID)) {
            bbr = MethodBasicBlocks.get(methodID);
            secondPass = true;
        } else {
            bbr = new BasicBlocksRecord();
            MethodBasicBlocks.put(methodID, bbr);
            secondPass = false;
        }

        if (!secondPass) {
            if (DEBUG) System.err.println("\n\n========== ANALYZING " + methodID + " ==========\n\n");
        } else {
            if (DEBUG) System.err.println("\n\n========== INSTRUMENTING " + methodID + " ==========\n\n");
        }
    }

    private void indexLabel(Label label) {
        /* Give the label an integer index in our hash map, if we haven't seen
         * it before */
        if (labelIndices.containsKey(label) == false) {
            labelIndices.put(label, lcount++);
        }
    }

    private void recordBasicBlock(int labelIndex) {
        if (secondPass) {
            return;
        }

        /* Mark the instruction count for this basic block */
        bbr.pushBasicBlock(icount, labelIndex);
        /* Reset instruction count */
        icount = 0;
    }

    private void insertBasicBlockCheck(int labelIndex) {
        int[] bb;
        int bb_icount = 0, bb_label_next = 0;

        if (!secondPass) {
            return;
        }

        if (bbr.numBasicBlocks() == 0)
            return;

        /* If we're visiting a non-jump target label, we should
         * have already collapsed it into the previous basic block */
        if (labelIndex >= 0 && !bbr.checkJumpTarget(labelIndex))
            return;

        /* Pop the next basic block instruction count and label index */
        bb = bbr.popBasicBlock();
        bb_icount += bb[0];

        /* Collapse all following non-jump-target, label basic blocks into
         * this basic block */
        while (bbr.numBasicBlocks() > 0 && bb[1] != LABEL_INDEX_CALL) {
            bb_label_next = bbr.nextBasicBlockLabel();

            /* If the next basic block is a jump target label, break */
            if (bb_label_next >= 0 && bbr.checkJumpTarget(bb_label_next))
                break;

            /* Incorporate it's instruction count into the current basic
             * block */
            bb = bbr.popBasicBlock();
            bb_icount += bb[0];

            /* If we just added a control flow to this basic block, break */
            if (bb_label_next < 0)
                break;
        }

        /* If this compressed basic block contains instructions, drop a
         * check method invocation */
        if (bb_icount > 0) {
            super.visitIntInsn(Opcodes.SIPUSH, bb_icount);
            super.visitMethodInsn(Opcodes.INVOKESTATIC, recorderClass, checkerMethod, checkerSignature);
        }
    }

    @Override
    public void visitInsn(final int opcode) {
        /* Count the instruction */
        icount += 1;
        /* Control flow change */
        if (opcode == Opcodes.IRETURN || opcode == Opcodes.LRETURN ||
            opcode == Opcodes.FRETURN || opcode == Opcodes.DRETURN ||
            opcode == Opcodes.ARETURN || opcode == Opcodes.RETURN) {
            recordBasicBlock(LABEL_INDEX_CALL);
        }
        mv.visitInsn(opcode);

        if (opcode == Opcodes.IRETURN || opcode == Opcodes.LRETURN ||
            opcode == Opcodes.FRETURN || opcode == Opcodes.DRETURN ||
            opcode == Opcodes.ARETURN || opcode == Opcodes.RETURN) {
            insertBasicBlockCheck(LABEL_INDEX_CALL);
        }
    }

    @Override
    public void visitIntInsn(final int opcode, final int operand) {
        /* Count the instruction */
        icount += 1;
        mv.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(final int opcode, final int var) {
        /* Count the instruction */
        icount += 1;
        /* Control flow change */
        if (opcode == Opcodes.RET) {
            recordBasicBlock(LABEL_INDEX_CALL);
        }
        mv.visitVarInsn(opcode, var);

        if (opcode == Opcodes.RET) {
            insertBasicBlockCheck(LABEL_INDEX_CALL);
        }
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type) {
        /* Count the instruction */
        icount += 1;
        mv.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(final int opcode, final String owner,
            final String name, final String desc) {
        /* Count the instruction */
        icount += 1;
        mv.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner,
            final String name, final String desc) {
        /* Count the instruction */
        icount += 1;

        /* Control flow change */
        recordBasicBlock(LABEL_INDEX_CALL);

        mv.visitMethodInsn(opcode, owner, name, desc);

        insertBasicBlockCheck(LABEL_INDEX_CALL);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm,
            Object... bsmArgs) {
        /* Count the instruction */
        icount += 1;

        /* Control flow change */
        recordBasicBlock(LABEL_INDEX_CALL);

        mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);

        insertBasicBlockCheck(LABEL_INDEX_CALL);
    }

    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
        indexLabel(label);

        /* Count the instruction */
        icount += 1;
        /* Control flow change */
        recordBasicBlock(LABEL_INDEX_CALL);

        /* Mark the label as a jump target */
        bbr.markJumpTarget(labelIndices.get(label));

        mv.visitJumpInsn(opcode, label);

        insertBasicBlockCheck(LABEL_INDEX_CALL);
    }

    @Override
    public void visitLdcInsn(final Object cst) {
        /* Count the instruction */
        icount += 1;
        mv.visitLdcInsn(cst);
    }

    @Override
    public void visitIincInsn(final int var, final int increment) {
        /* Count the instruction */
        icount += 1;
        mv.visitIincInsn(var, increment);
    }

    @Override
    public void visitLabel(Label label) {
        /* Index the seen label */
        indexLabel(label);
        /* Update our current label index */
        lindex = labelIndices.get(label);

        /* Potential control flow change */
        recordBasicBlock(lindex);

        mv.visitLabel(label);

        insertBasicBlockCheck(lindex);
    }

    @Override
    public void visitTableSwitchInsn(final int min, final int max,
            final Label dflt, final Label... labels) {
        /* Count the instruction */
        icount += 1;

        /* Control flow change */
        recordBasicBlock(LABEL_INDEX_CALL);

        /* Mark the labels as jump targets */
        for (Label l : labels) {
            indexLabel(l);
            bbr.markJumpTarget(labelIndices.get(l));
        }

        mv.visitTableSwitchInsn(min, max, dflt, labels);

        insertBasicBlockCheck(LABEL_INDEX_CALL);
    }

    @Override
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
            final Label[] labels) {
        /* Count the instruction */
        icount += 1;

        /* Control flow change */
        recordBasicBlock(LABEL_INDEX_CALL);

        /* Mark the labels as jump targets */
        for (Label l : labels) {
            indexLabel(l);
            bbr.markJumpTarget(labelIndices.get(l));
        }

        mv.visitLookupSwitchInsn(dflt, keys, labels);

        insertBasicBlockCheck(LABEL_INDEX_CALL);
    }

    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        /* Count the instruction */
        icount += 1;

        mv.visitMultiANewArrayInsn(desc, dims);
    }

    @Override
    public void visitCode() {
        insertBasicBlockCheck(LABEL_INDEX_START);
    }

    @Override
    public void visitEnd() {
        if (DEBUG) System.err.println(bbr);
        mv.visitEnd();
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        indexLabel(start);
        indexLabel(end);

        mv.visitLocalVariable(name, desc, signature, start, end, index);
    }
}

