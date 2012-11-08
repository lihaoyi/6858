package sandbox.runtime;

import sandbox.instrumentation.BanNativesMethodAdapter;
import sandbox.instrumentation.Transformer;

/**
 * Runtime target for the instrumented bytecode to hit, by providing two
 * `checkAllocation` methods as targets which immediately delegate to the
 * current thread's Account object
 */
public class Recorder {

    volatile static boolean disabled = false;

    public static void checkAllocation(int count){
        if (!disabled) disabled = true;
        else return;
        Account.get().memory.increment(count);
        disabled = false;
    }

    public static void checkAllocation(int[] counts){
        if (!disabled) disabled = true;
        else return;
        Account.get().memory.increment(counts);
        disabled = false;
    }

    public static void checkNative(short id){
        if (!disabled) disabled = true;
        else return;

        if (!Account.get().nativeWhitelist.allowed(id)){

            throw new BannedNativeException(BanNativesMethodAdapter.cache.get(id));
        }
        disabled = false;
    }
}
