/**
 * This is an attempt to break out of the sandbox by repeatedly catching the
 * exception that our instrumentation code throws when it runs out of
 * resources to use. The test demonstrates that even if you put your code in a
 * try-catch block, the checks inserted into the bytecodes will re-throw the
 * instruction before the untrusted code can do anything.
 */
public class InfiniteMemory {
    public static String main() throws Exception {
        long x = 0;

        long[] xs = new long[1090000000];
        return "done";
    }
}
