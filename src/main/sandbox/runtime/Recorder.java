package sandbox.runtime;

import sandbox.classloader.MyClassLoader;
import sandbox.instrumentation.ClassAdapter;
import sandbox.instrumentation.Transformer;

import java.util.HashMap;


/**
 * Runtime target for the instrumented bytecode to hit, by providing two
 * `checkAllocation` methods as targets which immediately delegate to the
 * current thread's Account object
 */
public class Recorder {

    final public static GhettoThreadLocal disabled = new GhettoThreadLocal();
    final public static GhettoThreadLocal disabled_cl = new GhettoThreadLocal();

    final public static GhettoThreadLocal disabled_ic = new GhettoThreadLocal();

    // Checks whether we can allocate count elements of size bytes.
    public static void checkAllocation(int count, int size) {


        if (!disabled.check()) disabled.enable();
        else return;

        // Print statements for debugging.
        // System.out.println("Printing the count:" + count);
        // System.out.println("Printing the type:" + type);
        sandbox.runtime.Account.get().memory.increment(count * size);

    }

    public static void checkAllocation(String className) {

        if (!disabled_cl.check()) disabled_cl.enable();
        else return;


        if (!ClassAdapter.fieldCountMap.containsKey(className)) {
            try {
                if (sandbox.runtime.Account.bcl == null) {
                    sandbox.runtime.Account.bcl = new MyClassLoader(new HashMap<String, byte[]>());
                }
                sandbox.runtime.Account.bcl.loadClassForAnalysis(className.replace("/", "."));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Exception trying to load class." + e.getMessage());
            }
        }
        //System.out.println("TFK: In recorder checkingClassAllocation "
        //  + className + " with field count "
        //  + ClassAdapter.fieldCountMap.get(className));
        int fieldCount;
        if (ClassAdapter.fieldCountMap.get(className) == null) {
            fieldCount = 0;
        } else {
            fieldCount = ClassAdapter.fieldCountMap.get(className);
        }
        sandbox.runtime.Account.get().memory.increment(fieldCount * 8);
        disabled_cl.disable();
    }

    public static void checkAllocation(int[] counts, int size) {
        if (!disabled.check()) disabled.enable();
        else return;
        sandbox.runtime.Account.get().memory.increment(counts, size);
        disabled.disable();
    }


    public static void checkInstructionCount(int count) {
        if (!disabled_ic.check()) disabled_ic.enable();
        else return;
        Account.get().instructions.increment(count);
        disabled_ic.disable();
    }
}
