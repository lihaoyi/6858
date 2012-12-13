package sandbox.agent;

import sandbox.instrumentation.Transformer;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;

/**
 * Class to be used as the -javaagent. This adds a Transformer object to
 * instrument all classes loaded in the future, as well as attempting
 * to load-instrument-reload all classes that were loaded in the past.
 */
public class JavaAgent {
    public static Instrumentation instrumentation;

    private JavaAgent() {
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Starting Rewriting");
        instrumentation = inst;
        inst.addTransformer(
                new Transformer(),
                inst.isRetransformClassesSupported()
        );
        Class<?>[] classes = inst.getAllLoadedClasses();
        //System.out.println(classes.length);
        ArrayList<Class<?>> classList = new ArrayList<Class<?>>();
        for (int i = 0; i < classes.length; i++) {
            //System.out.println(classes[i]);
            if (inst.isModifiableClass(classes[i])) {
                classList.add(classes[i]);
            } else {
                //      System.out.println("UNMODIFIABLE");
            }
        }

        // Reload classes, if possible.
        Class<?>[] workaround = new Class<?>[classList.size()];
        try {
            inst.retransformClasses(classList.toArray(workaround));
        } catch (UnmodifiableClassException e) {
            System.err.println("AllocationInstrumenter was unable to " +
                    "retransform early loaded classes.");
        }
        //System.out.println("Agent Premain End");
    }

}
