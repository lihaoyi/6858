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

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        System.out.println("Loading " + name);
        if(this.findLoadedClass(name) != null){
            return this.findLoadedClass(name);
        }
        if (specialClasses.containsKey(name)){
            byte[] b = specialClasses.get(name);
            return defineClass(name, b, 0, b.length);
        }
        if (name.startsWith("sandbox")) return super.loadClass(name);
        if (!name.startsWith("java") && !name.startsWith("sun")) return super.findClass(name);
        return super.loadClass(name);



//        return instrument(super.loadClass(name));


    }


}
