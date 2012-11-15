package sandbox.classloader;

import sandbox.agent.JavaAgent;
import sandbox.instrumentation.Transformer;
import sandbox.lists.WhiteList;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

public class MyClassLoader extends URLClassLoader {
    Map<String, byte[]> specialClasses;

    public MyClassLoader(Map<String, byte[]> specialClasses) {

        super(((URLClassLoader)ClassLoader.getSystemClassLoader()).getURLs());
        this.specialClasses = specialClasses;
    }

    public Class<?> instrument(Class<?> in){
        try{
            if(JavaAgent.instrumentation.isModifiableClass(in)){
                Transformer.transformMe.add(in);
                JavaAgent.instrumentation.retransformClasses(in);
                Transformer.transformMe.remove(in);
                //System.out.println("Instrumented " + in.getName());
            }else{
                System.out.println("Cannot Instrument" + in.getName());
            }

            return in;
        }catch(Exception e){ return in;
        }catch(Error e){ return in;}
    }
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        System.out.println("Loading " + name);
        if(this.findLoadedClass(name) != null){
            return this.findLoadedClass(name);
        }
        if (specialClasses.containsKey(name)){
            byte[] b = specialClasses.get(name);
            return instrument(defineClass(name, b, 0, b.length));
        }
        if (name.startsWith("sandbox")) return super.loadClass(name);
        //if (!WhiteList.allow(name)) throw new ClassNotFoundException("Cannot load non-whitelisted class: " + name);
        if (!name.startsWith("java") && !name.startsWith("sun")) return instrument(super.findClass(name));
        return instrument(super.loadClass(name));



//        return instrument(super.loadClass(name));


    }


}
