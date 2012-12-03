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

    private final String checkerSignature = "(II)V";

    private final String arrayCheckerSignature = "([II)V";

    private final String classCheckerSignature = "(Ljava/lang/String;)V";
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
    public MemoryMethodAdapter(MethodVisitor mv) {
        super(Opcodes.ASM4, mv);
    }

    /**
     * newarray shows up as an instruction taking an int operand (the primitive
     * element type of the array) so we hook it here.
     */
    @Override
    public void visitIntInsn(int opcode, int operand) {

        if (opcode == Opcodes.NEWARRAY) {
            if (operand >= 4 && operand <= 11) {
                // -> count
                checkProposedLength(operand);
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
                checkProposedLength(-1);
                // -> class count
                super.visitMethodInsn(opcode, owner, name, signature);

                return;
            } else if (signature.equals("(Ljava/lang/Class;[I)Ljava/lang/Object;")) {
                // -> class dimsArray
                super.visitInsn(Opcodes.DUP);
                // -> class dimsArray dimsArray
                // TODO(TFK): Would be nice to move this logic elsewhere.
                super.visitIntInsn(Opcodes.SIPUSH, sizeofAtype(-1));
                // -> class dimsArray dimsArray sizeof(REF_T)
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
                checkProposedLength(-1);
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
            // Check the size of the class.
            super.visitLdcInsn(typeName);
            super.visitMethodInsn(Opcodes.INVOKESTATIC, recorderClass,
                    checkerMethod, classCheckerSignature);
            super.visitTypeInsn(opcode, typeName);
        } else if (opcode == Opcodes.ANEWARRAY) {
            // ... len
            checkProposedLength(typeNameToInt(typeName));
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
        checkProposedMultiDims(dimCount, typeNameToInt(typeName));
        // -> dim1 dim2 dim3 ... dimN
        super.visitMultiANewArrayInsn(typeName, dimCount);
        // -> arrayRef
    }


    /**
     * Returns atype for primitive of a multi-dim array, given its typeName
     * <p/>
     * This method is currently just a hack.
     */
    private int typeNameToInt(String typeName) {
        // This method is a bit of a hack.
        char[] typeNameChars = typeName.toCharArray();
        int i;
        for (i = 0; i < typeNameChars.length; i++) {
            if (typeNameChars[i] != '[') {
                break;
            }
        }
        if (i != typeNameChars.length - 1) {
            return -1;
        }
        switch (typeNameChars[i]) {
            case 'Z':
                return 4; // T_BOOLEAN
            case 'C':
                return 5; // T_CHAR
            case 'F':
                return 6; // T_FLOAT
            case 'D':
                return 7; // T_DOUBLE
            case 'B':
                return 8; // T_BYTE
            case 'S':
                return 9; // T_SHORT
            case 'I':
                return 10; // T_INT
            case 'J':
                return 11; // T_LONG
            default:
                return -1; // T_REF
        }
    }

    /**
     * Returns the size in bytes of an atype operand.
     * <p/>
     * The atype operand of each newarray instruction must take one of the
     * values: T_BOOLEAN (4), T_CHAR (5), T_FLOAT (6), T_DOUBLE (7),
     * T_BYTE (8), T_SHORT (9), T_INT (10), or T_LONG (11).
     * <p/>
     * NOTE(TFK): This method uses a magic operand for object references T_REF (-1).
     */
    private int sizeofAtype(int operand) {
        switch (operand) {
            case 4:
                return 1; // T_BOOLEAN: 1 byte.
            case 5:
                return 2; // T_CHAR: 2 bytes.
            case 6:
                return 4; // T_FLOAT: 4 bytes.
            case 7:
                return 8; // T_DOUBLE: 8 bytes.
            case 8:
                return 1; // T_BYTE: 1 byte.
            case 9:
                return 2; // T_SHORT: 2 bytes.
            case 10:
                return 4; // T_INT: 4 bytes.
            case 11:
                return 8; // T_LONG: 8 bytes.
            case -1:
                // TODO(TFK): Object reference size probably depends on
                // JVM. Lets play it safe and have them be 8 bytes.
                return 8; // T_REF: 8 bytes.
            default:
                System.out.println(
                        "sizeofAtype called with an invalid atype operand " +
                                operand + ".  Assuming the type size is 1!");
                return 1;
        }
    }

    /**
     * Checks that the dim at the top of the stack is not too large
     */
    private void checkProposedLength(int operand) {
        // -> dim
        super.visitInsn(Opcodes.DUP);
        // -> dim dim
        super.visitIntInsn(Opcodes.SIPUSH, sizeofAtype(operand));
        // -> dim dim sizeof(operand)
        super.visitMethodInsn(Opcodes.INVOKESTATIC, recorderClass, checkerMethod, checkerSignature);
        // -> dim
    }

    /**
     * Takes a bunch of dimensions at the top of the stack and checks that
     * their total size isn't too large
     */
    private void checkProposedMultiDims(int dimCount, int operand) {
        // -> dim1 dim2 dim3 ... dimN
        super.visitIntInsn(Opcodes.BIPUSH, dimCount);
        // -> dim1 dim2 dim3 ... dimN numDims
        super.visitIntInsn(Opcodes.NEWARRAY, 10);
        // -> dim1 dim2 dim3 ... dimN arrayRef

        for (int i = 0; i < dimCount; i++) {
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
        super.visitIntInsn(Opcodes.SIPUSH, sizeofAtype(operand));
        // -> arrayRef arrayRef sizeof(T_REF)
        super.visitMethodInsn(Opcodes.INVOKESTATIC, recorderClass, checkerMethod, arrayCheckerSignature);
        // -> arrayRef

        for (int i = 0; i < dimCount; i++) {
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
