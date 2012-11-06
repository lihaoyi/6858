
package agent;

import java.lang.instrument.Instrumentation;

/**
 * Stub class to be used as the -javaagent. Does nothing except providing
 * a premain() to be called, and simply stores the Instrumentation object it
 * is passed for someone else to use later
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
