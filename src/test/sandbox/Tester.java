package sandbox;

import org.apache.commons.io.IOUtils;
import org.jruby.embed.ScriptingContainer;

import org.python.util.PythonInterpreter;
import sandbox.classloader.MyClassLoader;

import sandbox.runtime.Account;
import sandbox.runtime.Recorder;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;

public class Tester {
    public static void main(String[] args) throws Exception {

        System.out.println("Loading Source...");
        final String sourceCode = loadFile("resources/HelloWorld.java");

        System.out.println("Compiling...");
        final byte[] byteCode = Compiler.compile("HelloWorld", sourceCode);

        System.out.println("Setting up Classloader...");
        MyClassLoader bcl = new MyClassLoader(new HashMap<String, byte[]>() {{
            put("HelloWorld", byteCode);
        }});

        System.out.println("Executing Code...");
        System.out.println("============================================");

        Object key = Account.get().push(90000, 3000000);
        Class c = bcl.loadClass("HelloWorld");
        Method m = c.getMethod("main");
        String result = (String) m.invoke(null);

        System.out.println("RESULT: " + result);

        Account.get().pop(key);
        System.out.println("============================================");

    }

    public static String loadFile(String name) throws Exception {
        Reader input = new FileReader(name);
        StringWriter output = new StringWriter();
        try {
            IOUtils.copy(input, output);
        } finally {
            input.close();
        }
        String fileContents = output.toString();
        return fileContents;
    }

}

