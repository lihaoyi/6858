/**
 * This is the Infinite Loop test. It demonstrates the sandbox
 * instrumentation's ability to stop a program that takes unbounded computation
 * time.
 */
public class InfiniteLoop {
    public static void main() throws Exception {
        int x;
        while (true) {
            x = 5;
        }
    }
}
