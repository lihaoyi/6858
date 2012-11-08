package sandbox.instrumentation;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.sound.midi.SysexMessage;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * Method adapter that's meant to remove native methods
 */
public class BanNativesMethodAdapter extends MethodVisitor {

    public static HashMap<Short, String> cache = new HashMap<>();
    public BanNativesMethodAdapter(MethodVisitor methodVisitor) {
        super(Opcodes.ASM4, methodVisitor);
    }
    private final String recorderClass = "sandbox/runtime/Recorder";

    private final String checkerMethod = "checkNative";

    private final String checkerSignature = "(S)V";

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String signature) {

        String fullname = owner.replace('/', '.') + "." + name;
        short id = (short)fullname.hashCode();

        if (cache.containsKey(id)){
            if (cache.get(id) != null){
                super.visitIntInsn(Opcodes.SIPUSH, id);
                super.visitMethodInsn(Opcodes.INVOKESTATIC, recorderClass, checkerMethod, checkerSignature);
            }
        } else try {
            System.out.println("checking " + owner + "." + name);
            Method[] ms = Class.forName(owner.replace('/', '.')).getDeclaredMethods();
            cache.put(id, null);

            for(Method m: ms){
                if (m.getName().equals(name) && Modifier.isNative(m.getModifiers())){
                    System.out.println("Native!");
                    cache.put(id, fullname);
                    super.visitIntInsn(Opcodes.SIPUSH, id);
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, recorderClass, checkerMethod, checkerSignature);
                    break;
                }
            }

        }catch(Throwable e){
            System.out.println("B " + e);
        }



        super.visitMethodInsn(opcode, owner, name, signature);
    }
}
