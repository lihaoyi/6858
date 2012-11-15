package sandbox.runtime;



/**
 * Runtime target for the instrumented bytecode to hit, by providing two
 * `checkAllocation` methods as targets which immediately delegate to the
 * current thread's Account object
 */
public class Recorder {

    volatile static boolean disabled = false;
    volatile static boolean disabled_ic = false;

    // Checks whether we can allocate count elements of size bytes.
    public static void checkAllocation(int count, int size){
        if (!disabled) disabled = true;
        else return;
        // Print statements for debugging.
        // System.out.println("Printing the count:" + count);
        // System.out.println("Printing the type:" + type);
        sandbox.runtime.Account.get().memory.increment(count * size);
        disabled = false;
    }


    public static void checkInstructionCount(int count) {
        if (!disabled_ic) disabled_ic = true;
        else return;

        sandbox.runtime.Account.get().bytecodes.increment(count);
        disabled_ic = false;
    }

    public static void checkAllocation(int[] counts, int size){
        if (!disabled) disabled = true;
        else return;
        sandbox.runtime.Account.get().memory.increment(counts, size);
        disabled = false;
    }

}
