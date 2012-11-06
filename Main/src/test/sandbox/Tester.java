package sandbox;

import org.apache.commons.io.IOUtils;
import sandbox.runtime.Account;
import sandbox.runtime.Recorder;

import java.io.FileReader;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.HashMap;

public class Tester {
    public static void main(String[] args) throws Exception {

        System.out.println("Loading Source...");
        final String sourceCode = loadFile("Main/resources/HelloWorld.java");

        System.out.println("Compiling...");
        final byte[] byteCode = Compiler.compile("HelloWorld", sourceCode);

        System.out.println("Setting up Classloader...");
        MyClassLoader bcl = new MyClassLoader(new HashMap<String, byte[]>() {{
            put("HelloWorld", byteCode);
        }});
        //bcl.loadClass("sandbox.runtime.Recorder");


        System.out.println("Executing Code...");
        System.out.println("============================================");

        Object key = Account.get().push(15000, 1000000);



        String result = (String) bcl.loadClass("HelloWorld").getMethod("main").invoke(null);


        Account.get().pop(key);
        System.out.println("============================================");
        System.out.println("RESULT: " + result);

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

