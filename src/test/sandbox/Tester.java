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
    static HashMap<String, byte[] > classMap = new HashMap<String, byte[]>();

    static MyClassLoader bcl = new MyClassLoader(classMap);
    public static String run (final String className, final int maxMemory, final int maxBytecodes) throws Exception{
        System.out.println("================================" + className + "================================");
        Object key = Account.get().push(maxMemory, maxBytecodes);
        Account.bcl = bcl; // TODO(TFK): Remove this hack.
        String resultString = "";
        try{
            Class c = bcl.loadClass(className);
            Method m = c.getMethod("main");

            resultString = "Result: " + m.invoke(null);
        }catch(Exception e){
            resultString = "Caught " + e.getClass();
        }finally {
            Account.get().pop(key);
        }

        return resultString;
    }
    public static void main(String[] args) throws Exception {

        prepareFile("ScriptsInfiniteLoop");
        prepareFile("ScriptsInfiniteMemory");
        prepareFile("InfiniteCatch");

        System.setProperty("java.security.policy", "resources/Test.policy");
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        System.out.println(run("ScriptsInfiniteLoop", Integer.MAX_VALUE, Integer.MAX_VALUE));
        System.out.println(run("ScriptsInfiniteMemory", 50000, Integer.MAX_VALUE));
        //System.out.println(run("InfiniteCatch", 50000, 50000));


    }
    public static void prepareFile(String className) throws Exception{
        System.out.println("Preparing " + className);
        classMap.put(
                className,
                Compiler.compile(
                        className,
                        loadFile("resources/" + className + ".java")
                )
        );
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

