import java.io.File;
import java.io.IOException;
import java.lang.Object;
import java.lang.reflect.Array;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Attributes;

import sandbox.runtime.Account;

/**
 * Basic test suite that allocates heap memory in a bunch of different
 * ways and checks if the allocations are what we expect. Currently
 * doesn't quite do the math properly.
 */
public class BasicMemoryDemos {
    public static String main() {
        runningTotal = sandbox.runtime.Account.get().memory.current;

        // Test array allocation
        checkIncrement(0);
        int[] prepareForTrouble = new int[1000];
        checkIncrement(1000 * 4);

        double[][] makeItDouble = new double[1000][1000];
        checkIncrement(1000 * 1000 * 8);

        // Test reflection based allocation
        java.lang.reflect.Array.newInstance(Object.class, 10);
        resetIncrement(); // Reset, due to class loading.
 
        java.lang.reflect.Array.newInstance(Object.class, 10);
        checkIncrement(10 * 8); // sizeof(T_REF) is estimated as 8.

        int[] dims = {1, 2, 3, 4, 5};
        checkIncrement(5 * 4);
        java.lang.reflect.Array.newInstance(Object.class, dims);
        checkIncrement(120 * 8); // sizeof(T_REF) is 8 for now.

        // Test allocation of objects
        String s = new String(); // String has 7 fields.
        checkIncrement(7 * 8);

        if (output == null) return "Success! Nothing broke";
        else return output;
    }


    static long runningTotal = 0;
    static String output = null;

    private static void resetIncrement() {
        runningTotal = Account.get().memory.current;
    }

    /**
     * Helper method which asserts whether the expected number of bytes
     * of memory have been recorded as being allocated since the last time
     * this method was called.
     */
    private static void checkIncrement(int delta) {
        long newTotal = Account.get().memory.current;
        long actualDelta = newTotal - runningTotal;
        if (actualDelta != delta) output = "memory incremented by " + actualDelta + " expected " + delta;
        runningTotal = newTotal;
    }
}
