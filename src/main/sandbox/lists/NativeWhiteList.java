package sandbox.lists;

import sandbox.runtime.BannedNativeException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class NativeWhiteList {

    public static final String[] defaultWhitelist = {
            "java.lang.System.arraycopy",
            "java.lang.Object.hashCode",
            "java.lang.Object.getClass",

            "java.lang.reflect.Array.newArray",
            "java.lang.reflect.Array.multiNewArray"
    };

    public static final NativeWhiteList defaultList = new NativeWhiteList(null);

    private NativeWhiteList(Set<String> whitelist){
        this.whitelist = whitelist;
    }

    private final Set<String> whitelist;

    public boolean allowed(short id){

        if (whitelist == null) return true;

        for(String s: whitelist){
            if ((short)s.hashCode() == id) return true;
        }

        return false;
    }

    public NativeWhiteList fork(String[] newWhiteList){
        System.out.println("Fork");
        if (newWhiteList == null){
            System.out.println("A");
            return new NativeWhiteList(whitelist);
        }

        if (whitelist != null && whitelist.containsAll(Arrays.asList(newWhiteList))){
            System.out.println("B");
            throw new BannedNativeException("Cannot allocate natives you don't already own");
        }

        return new NativeWhiteList(new HashSet<>(Arrays.asList(newWhiteList)));
    }
    public String toString(){
        String out = "";
        if (whitelist != null){
            for (String s: whitelist){
                out += s + " " + s.hashCode() + "\n";

            }
        }else{
            out += "null";
        }
        return "Whitelist:\n" + out + "==================";
    }
}
