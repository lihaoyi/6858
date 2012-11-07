package sandbox.instrumentation;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.LocalVariablesSorter;

/**
 * In charge of instrumenting an individual method. Currently (imperfectly)
 * instruments the sites of memory allocation in order to track and limit
 * the number of allocations that can be done. Does a ton of bytecode mangling.
 */
public class MemoryMethodAdapter extends MethodVisitor {

    // Dictionary of primitive type opcode to English name.
    private static final String[] primitiveTypeNames = {
        "INVALID0", "INVALID1", "INVALID2", "INVALID3",
        "boolean", "char", "float", "double",
        "byte", "short", "int", "long"
    };

    private final String recorderClass = "sandbox/runtime/Recorder";

    private final String checkerMethod = "checkAllocation";

    private final String checkerSignature = "(I)V";

    private final String arrayCheckerSignature = "([I)V";

    /**
     * The LocalVariablesSorter used in this adapter.  Lame that it's public but
     * the ASM architecture requires setting it from the outside after this
     * MemoryMethodAdapter is fully constructed and the LocalVariablesSorter
     * constructor requires a reference to this adapter.  The only setter of
     * this should be ClassAdapter.visitMethod().
     */
    public LocalVariablesSorter lvs = null;

    /**
     * A new MemoryMethodAdapter is created for each method that gets visited.
     */
    public MemoryMethodAdapter(MethodVisitor mv) { super(Opcodes.ASM4, mv); }

    /**
     * newarray shows up as an instruction taking an int operand (the primitive
     * element type of the array) so we hook it here.
     */
    @Override
    public void visitIntInsn(int opcode, int operand) {

        if (opcode == Opcodes.NEWARRAY) {
            if (operand >= 4 && operand <= 11) {
                // -> count
                checkProposedLength();
                // -> count
                super.visitIntInsn(opcode, operand);
                // -> aref
            } else {
                System.out.println(
                    "NEWARRAY called with an invalid operand " +
                    operand + ".  Not instrumenting this allocation!"
                );
                super.visitIntInsn(opcode, operand);
            }
        } else {
            super.visitIntInsn(opcode, operand);
        }
    }


    /**
     * Reflection-based allocation (@see java.lang.reflect.Array#newInstance) is
     * triggered with a static method call (INVOKESTATIC), so we hook it here.
     * Class initialization is triggered with a constructor call (INVOKESPECIAL)
     * so we hook that here too as a proxy for the new bytecode which leaves an
     * uninitialized object on the stack that we're not allowed to touch.
     * {@link java.lang.Object#clone} is also a call to INVOKESPECIAL,
     * and is hooked here.  {@link java.lang.Class#newInstance} and
     * {@link java.lang.reflect.Constructor#newInstance} are both
     * INVOKEVIRTUAL calls, so they are hooked here, as well.
     */
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String signature) {


        // java.lang.reflect.Array does its own native allocation.  Grr.
        if (opcode == Opcodes.INVOKESTATIC && owner.equals("java/lang/reflect/Array") && name.equals("newInstance")) {
            if (signature.equals("(Ljava/lang/Class;I)Ljava/lang/Object;")) {
                // -> class count
                checkProposedLength();
                // -> class count
                super.visitMethodInsn(opcode, owner, name, signature);

                return;
            } else if (signature.equals("(Ljava/lang/Class;[I)Ljava/lang/Object;")) {
                // -> class dimsArray
                super.visitInsn(Opcodes.DUP);
                // -> class dimsArray dimsArray
                super.visitMethodInsn(Opcodes.INVOKESTATIC, recorderClass, checkerMethod, arrayCheckerSignature);
                // -> class dimsArray
                super.visitMethodInsn(opcode, owner, name, signature);
                // -> newobj
                return;
            }
        }

        if (opcode == Opcodes.INVOKEVIRTUAL) {
            if ("clone".equals(name) && owner.startsWith("[")) {
            
                // -> obj
                super.visitInsn(Opcodes.DUP);
                // -> obj newobj
                super.visitTypeInsn(Opcodes.CHECKCAST, owner);
                // -> obj arrayref
                super.visitInsn(Opcodes.ARRAYLENGTH);
                // -> obj length
                checkProposedLength();
                // -> obj length
                super.visitInsn(Opcodes.POP);
                // -> obj
                super.visitMethodInsn(opcode, owner, name, signature);
                // -> obj
                return;
            } else if ("newInstance".equals(name)) {
                if ("java/lang/Class".equals(owner) && "()Ljava/lang/Object;".equals(signature)) {
                    // -> class
                    super.visitMethodInsn(opcode, owner, name, signature);
                    // -> obj
                    return;
                } else if ("java/lang/reflect/Constructor".equals(owner) && "([Ljava/lang/Object;)Ljava/lang/Object;".equals(signature)) {
                    // -> class
                    super.visitMethodInsn(opcode, owner, name, signature);
                    // -> obj
                    return;
                }
            }
        }

        if (opcode == Opcodes.INVOKESPECIAL) {
            if ("clone".equals(name) && "java/lang/Object".equals(owner)) {
                // -> class
                super.visitMethodInsn(opcode, owner, name, signature);
                // -> obj
                return;
            }
        }

        super.visitMethodInsn(opcode, owner, name, signature);
    }

    /**
     * new and anewarray bytecodes take a String operand for the type of
     * the object or array element so we hook them here.  Note that new doesn't
     * actually result in any sandbox.instrumentation here; we just do a bit of
     * book-keeping and do the sandbox.instrumentation following the constructor call
     * (because we're not allowed to touch the object until it is initialized).
     */
    @Override
    public void visitTypeInsn(int opcode, String typeName) {

        if (opcode == Opcodes.NEW) {


            super.visitTypeInsn(opcode, typeName);

        } else if (opcode == Opcodes.ANEWARRAY) {
            // ... len
            checkProposedLength();
            // ... len
            super.visitTypeInsn(opcode, typeName);
            // ... arrayRef
        } else {
            super.visitTypeInsn(opcode, typeName);
        }
    }

    @Override
    public void visitMultiANewArrayInsn(String typeName, int dimCount) {
        // -> dim1 dim2 dim3 ... dimN
        checkProposedMultiDims(dimCount);
        // -> dim1 dim2 dim3 ... dimN
        super.visitMultiANewArrayInsn(typeName, dimCount);
        // -> arrayRef
    }

    /**
     * Checks that the dim at the top of the stack is not too large
     */
    private void checkProposedLength(){
        // -> dim
        super.visitInsn(Opcodes.DUP);
        // -> dim dim
        super.visitMethodInsn(Opcodes.INVOKESTATIC, recorderClass, checkerMethod, checkerSignature);
        // -> dim
    }

    /**
     * Takes a bunch of dimensions at the top of the stack and checks that
     * their total size isn't too large
     */
    private void checkProposedMultiDims(int dimCount){
        // -> dim1 dim2 dim3 ... dimN
        super.visitIntInsn(Opcodes.BIPUSH, dimCount);
        // -> dim1 dim2 dim3 ... dimN numDims
        super.visitIntInsn(Opcodes.NEWARRAY, 10);
        // -> dim1 dim2 dim3 ... dimN arrayRef

        for(int i = 0; i < dimCount; i++){
            super.visitInsn(Opcodes.DUP_X1);
            // -> dim1 dim2 dim3 ... dimN-1 arrayRef dimN arrayRef
            super.visitInsn(Opcodes.SWAP);
            // -> dim1 dim2 dim3 ... dimN-1 arrayRef arrayRef dimN
            super.visitIntInsn(Opcodes.BIPUSH, i);
            // -> dim1 dim2 dim3 ... dimN-1 arrayRef arrayRef dimN index
            super.visitInsn(Opcodes.SWAP);
            // -> dim1 dim2 dim3 ... dimN-1 arrayRef arrayRef index dimN
            super.visitInsn(Opcodes.IASTORE);
            // -> dim1 dim2 dim3 ... dimN-1 arrayRef
        }
        // -> arrayRef
        super.visitInsn(Opcodes.DUP);
        // -> arrayRef arrayRef
        super.visitMethodInsn(Opcodes.INVOKESTATIC, recorderClass, checkerMethod, arrayCheckerSignature);
        // -> arrayRef

        for(int i = 0; i < dimCount; i++){
            super.visitInsn(Opcodes.DUP);
            // -> arrayRef arrayRef
            super.visitIntInsn(Opcodes.BIPUSH, i);
            // -> arrayRef arrayRef index
            super.visitInsn(Opcodes.IALOAD);
            // -> arrayRef value
            super.visitInsn(Opcodes.SWAP);
            // -> value arrayRef
        }
        // -> dim1 dim2 dim3 ... dimN arrayRef
        super.visitInsn(Opcodes.POP);
        // -> dim1 dim2 dim3 ... dimN
    }

}
