package sandbox.classloader;

import sandbox.agent.JavaAgent;
import sandbox.instrumentation.Transformer;

import sandbox.Compiler;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

public class MyClassLoader extends URLClassLoader {
    Map<String, byte[]> specialClasses;

    public MyClassLoader(Map<String, byte[]> specialClasses) {

        super(((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs());
        this.specialClasses = specialClasses;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (Compiler.VERBOSE) {
          System.out.println("Loading " + name);
        }
        if (this.findLoadedClass(name) != null) {
            return this.findLoadedClass(name);
        }
        if (specialClasses.containsKey(name)) {
            byte[] b = specialClasses.get(name);
            return defineClass(name, b, 0, b.length);
        }
        if (name.startsWith("sandbox")) return super.loadClass(name);
        return super.loadClass(name);
//        return instrument(super.loadClass(name));
    }

    // Used by loadClassForAnalysis
    public Class<?> instrument(Class<?> in) {
        try {
            if (JavaAgent.instrumentation.isModifiableClass(in)) {
                JavaAgent.instrumentation.retransformClasses(in);
            } else {
                System.out.println("Cannot Instrument" + in.getName());
            }
            return in;
        } catch (Exception e) {
            return in;
        } catch (Error e) {
            return in;
        }
    }

    // This is currently a hack to call the ClassAdapter on the bytecode of a class being
    //  loaded at runtime. TODO(TFK): Clean this up to do minimal work.
    public Class<?> loadClassForAnalysis(String name) throws ClassNotFoundException {
        if (this.findLoadedClass(name) != null) {
            return this.findLoadedClass(name);
        }
        if (specialClasses.containsKey(name)) {
            byte[] b = specialClasses.get(name);
            return instrument(defineClass(name, b, 0, b.length));
        }
        if (name.startsWith("sandbox")) return super.loadClass(name);
        if (!name.startsWith("java") && !name.startsWith("sun")) return instrument(super.findClass(name));
        return instrument(super.loadClass(name));
    }
}
