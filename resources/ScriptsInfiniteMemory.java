import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;


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

        return "Success! Nothing broke";
    }
}
