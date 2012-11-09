package sandbox;

import org.jruby.embed.ScriptingContainer;

public class JRubyHelloWorld {

    public static void main(String[] args) {
        ScriptingContainer container = new ScriptingContainer();
        container.runScriptlet("puts \"Hello World!\"");
    }
}