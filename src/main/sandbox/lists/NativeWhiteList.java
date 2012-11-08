package sandbox.lists;

import java.util.Arrays;

public class NativeWhiteList {
    private static final String[] whitelist = {
            "java.lang.Object.hashCode",
            "java.lang.String.intern",
            "java.lang.reflect.Array.newArray",
            "java.lang.reflect.Array.multiNewArray",
    };

    public static boolean allowed(String className, String methodName){

        return Arrays.asList(whitelist).contains(className.replace('/', '.') + "." + methodName);
    }
}
