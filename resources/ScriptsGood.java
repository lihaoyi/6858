import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

/**
 * This is a benign script that does not try to do anything nasty, and
 * should execute fine.
 */
public class ScriptsGood {
    public static String main() throws Exception {

        ScriptEngineManager engineMgr = new ScriptEngineManager();
        ScriptEngine engine = engineMgr.getEngineByName("JavaScript");
        Bindings bindings = engine.createBindings();

        String script =
                "(function(){" +
                        "var x = 2; " +
                        "while (x < 3) {" +
                        "x = x + 1" +
                        "}" +
                        "return x" +
                        "})()";

        return "Benign code PASS, returns " + engine.eval(script);
    }
}
