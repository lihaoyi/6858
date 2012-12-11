import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * This is an infinite loop, but creatd in Javascript using Rhino, rather
 * than in plain Java. Because the JS interpreter also gets instrumented,
 * the infinite loop in interpreting the Javascript also throws a
 * ResourceLimitException to return control back to the trusted code.
 */
public class ScriptsInfiniteLoop {
    public static String main() throws Exception {

        ScriptEngineManager engineMgr = new ScriptEngineManager();
        ScriptEngine engine = engineMgr.getEngineByName("JavaScript");
        Bindings bindings = engine.createBindings();

        String script =
                "(function(){" +
                    "var x = 2; " +
                    "while (true) {" +
                      "x = x + 1" +
                    "}" +
                "})()";
        engine.eval(script);
        return "Oh No!";
    }
}
