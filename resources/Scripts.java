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

/**
 * Basic test suite that allocates heap memory in a bunch of different
 * ways and checks if the allocations are what we expect. Currently
 * doesn't quite do the math properly.
 */
public class Scripts {
    public static String main() throws Exception {

        ScriptEngineManager engineMgr = new ScriptEngineManager();
        ScriptEngine engine = engineMgr.getEngineByName("JavaScript");
        Bindings bindings = engine.createBindings();

        String script =
                "(function(){" +
                    "var y = [1, 2, 3, 4, 5]; " +
                    "var x = 2; " +
                    "while(x > 0){" +
                      "x = x + 1" + //  "y.push(1);}; return cow + moo" +
                    "}" +
                "})()";
        bindings.put("cow", 10);
        bindings.put("moo", 20);
        Object obj = null;
        try{
            obj = engine.eval(script, bindings);
        }catch(ResourceLimitException e){
            try{
                obj = engine.eval(script, bindings);
            }catch(ResourceLimitException a){
                try{
                    obj = engine.eval(script, bindings);
                }catch(ResourceLimitException x){
                    return "Haha i caught you";
                }
            }
        }

        return "Success! Nothing broke" + obj;
    }
}
