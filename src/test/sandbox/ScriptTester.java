package sandbox;

import org.apache.commons.io.IOUtils;
import sandbox.classloader.MyClassLoader;
import sandbox.runtime.Account;

import java.io.FileReader;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.HashMap;

public class ScriptTester {
    public static void main(String[] args) throws Exception {
        System.out.println("ScriptTester");
        System.out.println("Loading Source...");
        final String sourceCode = loadFile("resources/Scripts.java");

        System.out.println("Compiling...");
        final byte[] byteCode = Compiler.compile("Scripts", sourceCode);

        System.out.println("Setting up Classloader...");
        MyClassLoader bcl = new MyClassLoader(new HashMap<String, byte[]>() {{
            put("Scripts", byteCode);
        }});


        System.out.println("Executing Code...");
        System.out.println("============================================");

        Object key = Account.get().push(90000, 100000000);
        Class c = bcl.loadClass("Scripts");
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

