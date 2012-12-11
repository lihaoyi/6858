package sandbox;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * All the boilerplate required for an in-memory compiler; takes the source
 * code of a single "file", the name of the class to be compiled and spits out
 * a byte[] containing the resultant bytecode.
 */
public class Compiler {
    public static boolean VERBOSE = false;

    public static byte[] compile(String fullName, String src) throws Exception {
        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    
            ClassFileManager fileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null));
    
            List<JavaFileObject> jfiles = new ArrayList<JavaFileObject>();
            jfiles.add(new CharSequenceJavaFileObject(fullName, src));
    
            List<String> options = new ArrayList<String>();
            //options.add("-g:none");
            compiler.getTask(null, fileManager, null, options, null, jfiles).call();
            return fileManager.getBytes();
        } catch (Throwable e) {
            return null;
        }
    }

    private static class CharSequenceJavaFileObject extends SimpleJavaFileObject {

        /**
         * CharSequence representing the source code to be compiled
         */
        private CharSequence content;

        /**
         * This constructor will store the source code in the
         * internal "content" variable and register it as a
         * source code, using a URI containing the class full name
         *
         * @param className name of the public class in the source code
         * @param content   source code to compile
         */
        public CharSequenceJavaFileObject(String className, CharSequence content) {
            super(URI.create("string:///" + className.replace('.', '/')
                    + Kind.SOURCE.extension), Kind.SOURCE);
            this.content = content;
        }

        /**
         * Answers the CharSequence to be compiled. It will give
         * the source code stored in variable "content"
         */
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return content;
        }
    }

    private static class JavaClassObject extends SimpleJavaFileObject {

        /**
         * Byte code created by the compiler will be stored in this
         * ByteArrayOutputStream so that we can later get the
         * byte array out of it
         * and put it in the memory as an instance of our class.
         */
        protected final ByteArrayOutputStream bos = new ByteArrayOutputStream();

        /**
         * Registers the compiled class object under URI
         * containing the class full name
         *
         * @param name Full name of the compiled class
         * @param kind Kind of the data. It will be CLASS in our case
         */
        public JavaClassObject(String name, Kind kind) {
            super(URI.create("string:///" + name.replace('.', '/')
                    + kind.extension), kind);
        }

        /**
         * Will be used by our file manager to get the byte code that
         * can be put into memory to instantiate our class
         *
         * @return compiled byte code
         */
        public byte[] getBytes() {
            return bos.toByteArray();
        }

        /**
         * Will provide the compiler with an output stream that leads
         * to our byte array. This way the compiler will write everything
         * into the byte array that we will instantiate later
         */
        @Override
        public OutputStream openOutputStream() {
            return bos;
        }
    }

    private static class ClassFileManager extends ForwardingJavaFileManager {
        /**
         * Instance of JavaClassObject that will store the
         * compiled bytecode of our class
         */
        private JavaClassObject jclassObject;

        /**
         * Will initialize the manager with the specified
         * standard java file manager
         *
         * @param standardManager
         */
        public ClassFileManager(StandardJavaFileManager standardManager) {
            super(standardManager);
        }

        /**
         * Will be used by us to get the class loader for our
         * compiled class. It creates an anonymous class
         * extending the SecureClassLoader which uses the
         * byte code created by the compiler and stored in
         * the JavaClassObject, and returns the Class for it
         */
        @Override
        public ClassLoader getClassLoader(Location location) {
            return new SecureClassLoader() {
                @Override
                protected Class<?> findClass(String name) throws ClassNotFoundException {
                    byte[] b = jclassObject.getBytes();

                    return super.defineClass(name, jclassObject.getBytes(), 0, b.length);
                }
            };
        }

        public byte[] getBytes() {
            return jclassObject.getBytes();
        }

        /**
         * Gives the compiler an instance of the JavaClassObject
         * so that the compiler can write the byte code into it.
         */
        @Override
        public JavaFileObject getJavaFileForOutput(Location location,
                                                   String className,
                                                   JavaFileObject.Kind kind,
                                                   FileObject sibling) throws IOException {
            jclassObject = new JavaClassObject(className, kind);
            return jclassObject;
        }
    }
}
