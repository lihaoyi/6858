package sandbox.agent;

import sandbox.instrumentation.Transformer;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;

/**
 * Stub class to be used as the -javaagent. Does nothing except providing
 * a premain() to be called, and simply stores the Instrumentation object it
 * is passed for someone else to use later
 */
public class JavaAgent {
    public static Instrumentation instrumentation;

    private JavaAgent() {
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Agent Premain Start");
        instrumentation = inst;
        inst.addTransformer(
                new Transformer(),
                inst.isRetransformClassesSupported()
        );
//        Class<?>[] classes = inst.getAllLoadedClasses();
//        ArrayList<Class<?>> classList = new ArrayList<Class<?>>();
//        for (int i = 0; i < classes.length; i++) {
//            System.out.println(classes[i]);
//            if (inst.isModifiableClass(classes[i])) {
//                classList.add(classes[i]);
//            }else{
//                System.out.println("UNMODIFIABLE");
//            }
//        }
//
//        // Reload classes, if possible.
//        Class<?>[] workaround = new Class<?>[classList.size()];
//        try {
//            inst.retransformClasses(classList.toArray(workaround));
//        } catch (UnmodifiableClassException e) {
//            System.err.println("AllocationInstrumenter was unable to " +
//                    "retransform early loaded classes.");
//        }
        System.out.println("Agent Premain End");
    }

}
