import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;
import org.python.util.PythonInterpreter;
import sandbox.runtime.Account;
import sandbox.runtime.ResourceLimitException;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.lang.reflect.Array;
import java.util.Date;
import java.util.jar.Attributes;


public class ScriptsInfiniteLoop {
    public static String main() throws Exception {

        ScriptEngineManager engineMgr = new ScriptEngineManager();
        ScriptEngine engine = engineMgr.getEngineByName("JavaScript");
        Bindings bindings = engine.createBindings();

        String script =
                "(function(){" +
                    "var x = 2; " +
                    "while(x > 0){" +
                      "x = x + 1" + //  "y.push(1);}; return cow + moo" +
                    "}" +
                "})()";
        engine.eval(script);
        return "Success! Nothing broke";
    }
}
