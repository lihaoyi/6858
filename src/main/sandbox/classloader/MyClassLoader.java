package sandbox.classloader;

import sandbox.agent.JavaAgent;
import sandbox.instrumentation.Transformer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MyClassLoader extends ClassLoader {
    Map<String, byte[]> specialClasses;

    public MyClassLoader(Map<String, byte[]> specialClasses) {
        this.specialClasses = specialClasses;
    }

    public Class<?> instrument(Class<?> in){
        try{
            //-javaagent:out/artifacts/Main_jar/Main.jar
            if(JavaAgent.instrumentation.isModifiableClass(in)){
                //System.out.println("Classload Instrumenting " + in.getName());
                Transformer.transformMe.add(in);
                JavaAgent.instrumentation.retransformClasses(in);
                Transformer.transformMe.remove(in);
                System.out.println("Instrumented " + in.getName());
            }else{
                System.out.println("Cannot Instrument" + in.getName());
            }

            return in;
        }catch(Exception e){ return in; }
    }
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {

        if (specialClasses.containsKey(name)) return findClass(name);
        else if (!BlackList.allow(name)) throw new ClassNotFoundException("Cannot load blacklisted class: " + name);
        else if (name.startsWith("sandbox")) return super.loadClass(name);
        else return instrument(super.loadClass(name));

    }

    @Override
    protected Class findClass(String name) {
        byte[] b = loadClassData(name);
        return instrument(defineClass(name, b, 0, b.length));
    }

    private byte[] loadClassData(String name) {
        return specialClasses.get(name);
    }
}
