package sandbox.instrumentation;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Label;
import org.objectweb.asm.Handle;

/* Data structure wrapper to maintain the instruction counts and boundaries of
 * basic instruction blocks, as well which labels are jump targets, for an
 * analyzed jvm bytecode method. */
class BasicBlocksRecord {
    /* List of instruction counts, boundaries, and jump target labels */
    private List<Integer> bb_icounts, bb_boundaries, jump_targets;

    /* Boundary indicating the start of a method */
    public final static int BOUNDARY_START = -1;
    /* Boundary indicating a flow control change (e.g. an invoke or branch) */
    public final static int BOUNDARY_FLOW_CONTROL = -2;

    /* Initialize the underlying data structure */
    public BasicBlocksRecord() {
        bb_icounts = new ArrayList<Integer>();
        bb_boundaries = new ArrayList<Integer>();
        jump_targets = new ArrayList<Integer>();
    }

    /* Push the instruction count and boundary associated with the analyzed
     * basic block */
    public void pushBasicBlock(int icount, int boundary) {
        bb_icounts.add(icount);
        bb_boundaries.add(boundary);
    }

    /* Pop the next basic block instruction count and boundary in FIFO
     * fashion */
    public int[] popBasicBlock() {
        int[] bb = new int[2];
        bb[0] = bb_icounts.remove(0);
        bb[1] = bb_boundaries.remove(0);
        return bb;
    }

    /* Peek at the next basic block boundary without removing it */
    public int peekNextBasicBlockBoundary() {
        return bb_boundaries.get(0);
    }

    /* Get the number of recorded basic blocks */
    public int numBasicBlocks() {
        return bb_boundaries.size();
    }

    /* Mark a label as a jump target */
    public void markJumpTarget(int boundary) {
        jump_targets.add(boundary);
    }

    /* Check if a boundary is a jump target */
    public boolean checkJumpTarget(int boundary) {
        return boundaryIsLabel(boundary) && jump_targets.contains(boundary);
    }

    /* Check if a boundary is a label */
    public boolean boundaryIsLabel(int boundary) {
        return (boundary >= 0);
    }

    /* String representation of this basic blocks record for debugging purposes */
    public String toString() {
        int i;
        String s = "[";

        for (i = 0; i < bb_icounts.size(); i++) {
            s += "(count: " + bb_icounts.get(i) + ", label: " + bb_boundaries.get(i) + "), ";
        }

        s += "]\nJump Targets: ";

        for (i = 0; i < jump_targets.size(); i++) {
            s += "L" + jump_targets.get(i) + " ";
        }

        s += "\n";

        return s;
    }
}

/**
 * Method adapter meant to add instruction-counting instrumentation to the
 * processed methods
 * <p/>
 * First pass builds basic blocks record for method, second pass instruments
 * method.
 */
public class InstructionMethodAdapter extends MethodVisitor {

    private final String recorderClass = "sandbox/runtime/Recorder";
    private final String checkerMethod = "checkInstructionCount";
    private final String checkerSignature = "(I)V";

    private final boolean DEBUG = false;
    private boolean secondPass;

    private int icount = 0, lcount = 0;
    private HashMap<Label, Integer> labelIndices;
    private BasicBlocksRecord bbr;

    public InstructionMethodAdapter(MethodVisitor methodVisitor, String methodID, Map<String, BasicBlocksRecord> methodBasicBlocksMap) {
        super(Opcodes.ASM4, methodVisitor);
        labelIndices = new HashMap<Label, Integer>();

        /* Second pass for instrumentation */
        if (methodBasicBlocksMap.containsKey(methodID)) {
            bbr = methodBasicBlocksMap.get(methodID);
            secondPass = true;
            if (DEBUG)
                System.err.println("\n\n========== INSTRUMENTING " + methodID + " ==========\n\n");

        /* First pass for analysis */
        } else {
            bbr = new BasicBlocksRecord();
            methodBasicBlocksMap.put(methodID, bbr);
            secondPass = false;
            if (DEBUG)
                System.err.println("\n\n========== ANALYZING " + methodID + " ==========\n\n");
        }
    }

    private void indexLabel(Label label) {
        /* Give the label an integer index in our hash map, if we haven't seen
         * it yet */
        if (labelIndices.containsKey(label) == false)
            labelIndices.put(label, lcount++);
    }

    private void recordBasicBlock(int boundary) {
        if (secondPass)
            return;

        /* Mark the instruction count for this basic block */
        bbr.pushBasicBlock(icount, boundary);
        /* Reset instruction count */
        icount = 0;
    }

    private void insertBasicBlockCheck(int visitingBoundary) {
        int[] bb;
        int bb_icount = 0, bb_boundary = 0;

        if (!secondPass)
            return;

        /* Assert we have basic blocks left to instrument */
        if (bbr.numBasicBlocks() == 0)
            return;

        /* If we're visiting a non-jump-target-label (e.g. a label marking a
         * line annotation), we should have already optimized it into the
         * previous basic block */
        if (bbr.boundaryIsLabel(visitingBoundary) && !bbr.checkJumpTarget(visitingBoundary))
            return;

        /* Pop the next basic block instruction count and boundary */
        bb = bbr.popBasicBlock();
        bb_icount = bb[0];
        bb_boundary = bb[1];

        /* If this block does not end with a control flow change, or with a
         * label that is a jump target,
         *
         * then collapse all following non-jump-target-label boundary basic
         * blocks into this basic block. */
        while (bb_boundary != BasicBlocksRecord.BOUNDARY_FLOW_CONTROL && (bbr.boundaryIsLabel(bb_boundary) && !bbr.checkJumpTarget(bb_boundary))) {
            /* If we're out of basic blocks, break */
            if (bbr.numBasicBlocks() == 0)
                break;

            /* If the next basic block boundary is a jump target label, break */
            bb_boundary = bbr.peekNextBasicBlockBoundary();
            if (bbr.boundaryIsLabel(bb_boundary) && bbr.checkJumpTarget(bb_boundary))
                break;

            /* Incorporate its instruction count into this basic block */
            bb = bbr.popBasicBlock();
            bb_icount += bb[0];

            /* If we just added a control flow change to this basic block,
             * break */
            if (bb_boundary == BasicBlocksRecord.BOUNDARY_FLOW_CONTROL)
                break;
        }

        /* If this compressed basic block contains instructions, drop a
         * check instructions method invocation */
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
            recordBasicBlock(BasicBlocksRecord.BOUNDARY_FLOW_CONTROL);
        }
        mv.visitInsn(opcode);

        /* Insert an instructions check after a control flow change */
        if (opcode == Opcodes.IRETURN || opcode == Opcodes.LRETURN ||
                opcode == Opcodes.FRETURN || opcode == Opcodes.DRETURN ||
                opcode == Opcodes.ARETURN || opcode == Opcodes.RETURN) {
            insertBasicBlockCheck(BasicBlocksRecord.BOUNDARY_FLOW_CONTROL);
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
            recordBasicBlock(BasicBlocksRecord.BOUNDARY_FLOW_CONTROL);
        }
        mv.visitVarInsn(opcode, var);

        /* Insert an instructions check after a control flow change */
        if (opcode == Opcodes.RET) {
            insertBasicBlockCheck(BasicBlocksRecord.BOUNDARY_FLOW_CONTROL);
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
        recordBasicBlock(BasicBlocksRecord.BOUNDARY_FLOW_CONTROL);

        mv.visitMethodInsn(opcode, owner, name, desc);

        /* Insert an instructions check after a control flow change */
        insertBasicBlockCheck(BasicBlocksRecord.BOUNDARY_FLOW_CONTROL);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm,
                                       Object... bsmArgs) {
        /* Count the instruction */
        icount += 1;

        /* Control flow change */
        recordBasicBlock(BasicBlocksRecord.BOUNDARY_FLOW_CONTROL);

        mv.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);

        /* Insert an instructions check after a control flow change */
        insertBasicBlockCheck(BasicBlocksRecord.BOUNDARY_FLOW_CONTROL);
    }

    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
        /* Index the label */
        indexLabel(label);

        /* Count the instruction */
        icount += 1;

        /* Control flow change */
        recordBasicBlock(BasicBlocksRecord.BOUNDARY_FLOW_CONTROL);

        /* Mark the label as a jump target */
        bbr.markJumpTarget(labelIndices.get(label));

        mv.visitJumpInsn(opcode, label);

        /* Insert an instructions check after a control flow change */
        insertBasicBlockCheck(BasicBlocksRecord.BOUNDARY_FLOW_CONTROL);
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
        /* Index the label */
        indexLabel(label);

        /* Potential control flow change */
        recordBasicBlock(labelIndices.get(label));

        mv.visitLabel(label);

        /* Insert an instructions check, if it's a jump target label */
        insertBasicBlockCheck(labelIndices.get(label));
    }

    @Override
    public void visitTableSwitchInsn(final int min, final int max,
                                     final Label dflt, final Label... labels) {
        /* Count the instruction */
        icount += 1;

        /* Control flow change */
        recordBasicBlock(BasicBlocksRecord.BOUNDARY_FLOW_CONTROL);

        /* Index the labels, and mark jump target labels */
        indexLabel(dflt);
        for (Label l : labels) {
            indexLabel(l);
            bbr.markJumpTarget(labelIndices.get(l));
        }

        mv.visitTableSwitchInsn(min, max, dflt, labels);

        /* Insert an instructions check after a control flow change */
        insertBasicBlockCheck(BasicBlocksRecord.BOUNDARY_FLOW_CONTROL);
    }

    @Override
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
                                      final Label[] labels) {
        /* Count the instruction */
        icount += 1;

        /* Control flow change */
        recordBasicBlock(BasicBlocksRecord.BOUNDARY_FLOW_CONTROL);

        /* Index the labels, and mark jump target labels */
        indexLabel(dflt);
        for (Label l : labels) {
            indexLabel(l);
            bbr.markJumpTarget(labelIndices.get(l));
        }

        mv.visitLookupSwitchInsn(dflt, keys, labels);

        /* Insert an instructions check after a control flow change */
        insertBasicBlockCheck(BasicBlocksRecord.BOUNDARY_FLOW_CONTROL);
    }

    @Override
    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        /* Count the instruction */
        icount += 1;

        mv.visitMultiANewArrayInsn(desc, dims);
    }

    @Override
    public void visitCode() {
        /* Insert an instructions check at the start of the method, before any
         * target instructions are executed */
        insertBasicBlockCheck(BasicBlocksRecord.BOUNDARY_START);
    }

    @Override
    public void visitEnd() {
        if (DEBUG)
            System.err.println(bbr);

        mv.visitEnd();
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        /* Index the labels */
        indexLabel(start);
        indexLabel(end);

        mv.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        /* Index the labels */
        indexLabel(start);
        indexLabel(end);
        indexLabel(handler);

        mv.visitTryCatchBlock(start, end, handler, type);
    }
}

