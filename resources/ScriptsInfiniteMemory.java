import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * This is an attempt to allocate infinite memory in Javascript using Rhino.
 * Because the Rhino engine also gets instrumented, the memory allocations are
 * accounted for and limited.
 */
public class ScriptsInfiniteMemory {
    public static String main() throws Exception {

        ScriptEngineManager engineMgr = new ScriptEngineManager();
        ScriptEngine engine = engineMgr.getEngineByName("JavaScript");
        Bindings bindings = engine.createBindings();

        String script =
                "(function(){" +
                        "var x = []; " +
                        "while(x > 0){" +
                        "x.push(x.length)" + //  "y.push(1);}; return cow + moo" +
                        "}" +
                        "})()";

        engine.eval(script);

        return "Oh No!";
    }
}
