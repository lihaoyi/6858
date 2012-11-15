import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;
import org.python.util.PythonInterpreter;
import sandbox.runtime.Account;

import java.lang.reflect.Array;
import java.util.Date;
import java.util.jar.Attributes;

/**
 * Basic test suite that allocates heap memory in a bunch of different
 * ways and checks if the allocations are what we expect. Currently
 * doesn't quite do the math properly.
 */
public class Scripts {
    public static String main(){

//        PythonInterpreter p = new PythonInterpreter();
//        p.exec("x = 10");
        ScriptingContainer container = new ScriptingContainer(LocalVariableBehavior.PERSISTENT);
        container.runScriptlet("p=0.9");
        //container.runScriptlet("x=[0.9] * 10000000000");
        container.runScriptlet("nil while true");


        //container.runScriptlet("x = 'a' * 1000000; puts 'Ruby ' + File.read('.gitignore')");

        return "Success! Nothing broke" + container.get("p");
    }
}
