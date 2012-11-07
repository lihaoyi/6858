package sandbox.lists;


public class BlackList {
    /**
     * Checks whether the name of a class is allowed by seeing if it has a prefix
     * which exists in the private blacklist.
     */
    public static boolean allow(String s){
        for(String clsName: blacklistclasses) if (s.startsWith(clsName)) return false;
        return true;
    }

    private static final String[] blacklistclasses = {
          /*  "java.lang.System",
            "java.lang.Runtime",
            "java.io.File",
            "java.net",
            "java.rmi",
            "java.security",*/
    };
}
