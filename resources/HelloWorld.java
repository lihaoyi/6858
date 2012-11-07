import java.lang.Object;
import java.lang.reflect.Array;
import sandbox.runtime.Account;

/**
 * Basic test suite that allocates heap memory in a bunch of different
 * ways and checks if the allocations are what we expect. Currently
 * doesn't quite do the math properly.
 */
public class HelloWorld {
    public static String main(){

        // start actual accounting
        runningTotal = sandbox.runtime.Account.get().memory.current;

        checkIncrement(0);
        a = new int[10];
        checkIncrement(10 * 4);

        a = new int[10][10][10];

        checkIncrement(1000 * 4);

        java.lang.reflect.Array.newInstance(Object.class, 10);
        checkIncrement(10 * 8); // NOTE(TFK): Assume sizeof(T_REF) = 8
        int[] dims = {1, 2, 3, 4, 5};
        checkIncrement(5 * 4);
        java.lang.reflect.Array.newInstance(Object.class, dims);
        checkIncrement(120 * 8);
        a = new Object();


        return "Success! Nothing broke";
    }

    static Object a;
    static {
        // Initialize all classes by forcing classloading
        a = new int[0];
        a = new Object();
        a = "" + "a";
        Array.newInstance(Object.class, 0);
    }

    static long runningTotal = 0;

    /**
     * Helper method which asserts whether the expected number of bytes
     * of memory have been recorded as being allocated since the last time
     * this method was called.
     */
    private static void checkIncrement(int delta){
        long newTotal = Account.get().memory.current;
        long actualDelta = newTotal - runningTotal;
        assert actualDelta == delta: "memory incremented by " + actualDelta +" expected " + delta;
        // NOTE(TFK): The above assertion was not failing correctly so
        // I added this print statement to debug.
        if (delta != actualDelta) {
          System.out.println("Incorrect quota delta. Actual: "+actualDelta + " Expected: "+ delta);
        }
        runningTotal = newTotal;
    }
}
