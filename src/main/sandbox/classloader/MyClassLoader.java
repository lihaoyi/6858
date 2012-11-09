package sandbox.classloader;

import sandbox.agent.JavaAgent;
import sandbox.instrumentation.Transformer;
import sandbox.lists.WhiteList;

import java.util.Map;

public class MyClassLoader extends ClassLoader {
    Map<String, byte[]> specialClasses;

    public MyClassLoader(Map<String, byte[]> specialClasses) {
        this.specialClasses = specialClasses;
    }

    public Class<?> instrument(Class<?> in){
        try{
            if(JavaAgent.instrumentation.isModifiableClass(in)){
                Transformer.transformMe.add(in);
                JavaAgent.instrumentation.retransformClasses(in);
                Transformer.transformMe.remove(in);
                System.out.println("Instrumented " + in.getName());
            }else{
                System.out.println("Cannot Instrument" + in.getName());
            }

            return in;
        }catch(Exception e){ return in;
        }catch(Error e){ return in;}
    }
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (specialClasses.containsKey(name)) return findClass(name);
        if (name.startsWith("sandbox")) return super.loadClass(name);
        if (!WhiteList.allow(name)) throw new ClassNotFoundException("Cannot load non-whitelisted class: " + name);
        return instrument(super.loadClass(name));
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
