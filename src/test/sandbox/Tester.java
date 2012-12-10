package sandbox;

import org.apache.commons.io.IOUtils;
import org.jruby.embed.ScriptingContainer;

import org.python.util.PythonInterpreter;
import sandbox.classloader.MyClassLoader;

import sandbox.runtime.Account;
import sandbox.runtime.Recorder;
import sandbox.runtime.ResourceLimitException;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

public class Tester {
    public static <T> T run (final String className, final int maxMemory, final int maxBytecodes) throws Exception{
        System.out.println("Loading Source...");
        final String sourceCode = loadFile("resources/"+className+".java");

        System.out.println("Compiling...");
        final byte[] byteCode = Compiler.compile(className, sourceCode);

        System.out.println("Setting up Classloader...");

        MyClassLoader bcl = new MyClassLoader(new HashMap<String, byte[]>() {{
            put(className, byteCode);
        }});

        System.setProperty("java.security.policy", "resources/Test.policy");
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }


        System.out.println("Executing Code...");
        System.out.println("============================================");

        Object key = Account.get().push(maxMemory, maxBytecodes);
        Account.bcl = bcl; // TODO(TFK): Remove this hack.

        Class c = bcl.loadClass(className);
        Method m = c.getMethod("main");
        T result = (T) m.invoke(null);

        System.out.println("RESULT: " + result);

        Account.get().pop(key);
        System.out.println("============================================");

        return result;
    }
    public static void main(String[] args) throws Exception {
//        System.out.println(run("HelloWorld", 50000, 50000000));
        try{
            System.out.println(run("ScriptsInfiniteLoop", 50000, 500000));
        }catch(InvocationTargetException e){
            System.out.println("Sucess! Exception caught from ScriptsInfiniteLoop");
        }
        try{
            System.out.println(run("ScriptsInfiniteMemory", 50000, 500000));
        }catch(InvocationTargetException e){
            System.out.println("Sucess! Exception caught from ScriptsInfiniteMemory");
        }
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

