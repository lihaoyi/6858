package sandbox.runtime;

import sandbox.instrumentation.Transformer;

/**
 * Runtime target for the instrumented bytecode to hit, by providing two
 * `recordAllocation` methods as targets.
 */
public class Recorder {

    /**
     * Used to disable triggers when a recording is taking place, to avoid having
     * infinite recording loops
     */
    public static final ThreadLocal<Boolean> disabled = new ThreadLocal<Boolean>();

    public static void checkAllocation(int count){
        sandbox.runtime.Account.get().memory.increment(count);
    }

    public static void checkAllocation(int[] counts){

        sandbox.runtime.Account.get().memory.increment(counts);
    }
    public static void recordAllocation(Class<?> cls, Object newObj) {
        // Static Target for Instrumented Code
        recordAllocation(-1, cls.getName(), newObj);
    }

    /**
     * Records the allocation.  This method is invoked on every allocation
     * performed by the system.
     *
     * @param count  the count of how many instances are being
     *               allocated, if an array is being allocated.  If an array is not being
     *               allocated, then this value will be -1.
     * @param desc   the descriptor of the class/primitive type
     *               being allocated.
     * @param newObj the new <code>Object</code> whose allocation is being
     *               recorded.
     */
    public static void recordAllocation(int count, String desc, Object newObj) {
        // Static Target for Instrumented Code

        if (disabled.get() == Boolean.TRUE) return;
        else disabled.set(Boolean.TRUE);
        String loc = new Throwable().getStackTrace()[1].toString();

        long size = agent.JavaAgent.instrumentation.getObjectSize(newObj);

        sandbox.runtime.Account.get().memory.increment(size);
        //Object.class.getDeclaredField("callback").set(newObj, "moo");

        disabled.set(Boolean.FALSE);

    }
}
