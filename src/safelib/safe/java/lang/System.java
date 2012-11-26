package safe.java.lang;

import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.channels.Channel;
import java.util.Properties;

public final class System {
    private System() {
    }

    public final static InputStream in = null;

    public final static PrintStream out = null;

    public final static PrintStream err = null;
    
    public static void setIn(InputStream in) {
        java.lang.System.setIn(in);
    }

    public static void setOut(PrintStream out) {
        java.lang.System.setOut(out);
    }

    public static void setErr(PrintStream err) {
        java.lang.System.setErr(err);
    }
    
    public static Console console() {
        return java.lang.System.console();
    }

    
    public static Channel inheritedChannel() throws IOException {
        return java.lang.System.inheritedChannel();
    }

    
    public static void setSecurityManager(final SecurityManager s) {
        java.lang.System.setSecurityManager(s);
    }

    
    public static SecurityManager getSecurityManager() {
        return java.lang.System.getSecurityManager();
    }

    public static long currentTimeMillis() {
        return java.lang.System.currentTimeMillis();
    }

    public static long nanoTime() {
        return java.lang.System.nanoTime();
    }
    
    public static void arraycopy(Object src,  int  srcPos,
                                        Object dest, int destPos,
                                        int length){
        java.lang.System.arraycopy(src, srcPos, dest, destPos, length);
    }

    public static int identityHashCode(Object x){
        return java.lang.System.identityHashCode(x);
    }
    
    public static Properties getProperties() {
        return java.lang.System.getProperties();
    }
    
    public static String lineSeparator() {
        return java.lang.System.lineSeparator();
    }

    
    public static void setProperties(Properties props) {
        java.lang.System.setProperties(props);
    }

    
    public static String getProperty(String key) {
        return java.lang.System.getProperty(key);
    }

    public static String getProperty(String key, String def) {
        return java.lang.System.getProperty(key, def);
    }

    public static String setProperty(String key, String value) {
        return java.lang.System.setProperty(key, value);
    }

    public static String clearProperty(String key) {
        return java.lang.System.clearProperty(key);
    }
    
    public static String getenv(String name) {
        return java.lang.System.getenv(name);
    }
    
    public static java.util.Map<String,String> getenv() {
        return java.lang.System.getenv();
    }

    public static void exit(int status) {
        java.lang.System.exit(status);
    }

    public static void gc() {
        java.lang.System.gc();
    }
    
    public static void runFinalization() {
        java.lang.System.runFinalization();
    }

    @Deprecated
    public static void runFinalizersOnExit(boolean value) {
        java.lang.System.runFinalizersOnExit(value);
    }

    public static void load(String filename) {
        java.lang.System.load(filename);
    }

    public static void loadLibrary(String libname) {
        java.lang.System.loadLibrary(libname);
    }

    public static String mapLibraryName(String libname){
        return java.lang.System.mapLibraryName(libname);
    }

}
