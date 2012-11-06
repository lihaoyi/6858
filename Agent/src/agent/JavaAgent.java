
package agent;

import java.lang.instrument.Instrumentation;

/**
 * Stub class to be used as the -javaagent. Does nothing except providing
 * a premain() to be called, immediately passing the provided sandbox.instrumentation
 * object to Transformer
 */
public class JavaAgent {
    public static Instrumentation instrumentation;
    private JavaAgent() {}

    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Agent Premain Start");
        instrumentation = inst;
        System.out.println("Agent Premain End");
    }

}
