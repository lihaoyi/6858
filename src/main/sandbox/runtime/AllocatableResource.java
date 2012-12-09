package sandbox.runtime;

import java.util.Stack;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

/**
 * A resource that is associated with an allocation and
 * freed once it is garbage collected.
 */ 
public class AllocatableResource extends Resource {
    public static final ThreadLocal<Stack<Long>> lastAllocationSize = new ThreadLocal<Stack<Long>>();
    public static final ThreadLocal<Map<Object, Long>> allocationSizeMap = new ThreadLocal<Map<Object,Long>>();
    public static final ThreadLocal<ReferenceQueue<Object>> referenceQueue = new ThreadLocal<ReferenceQueue<Object>>();
    public static long lastGC = 0;

    public AllocatableResource(String name) {
        super(name);
        lastAllocationSize.set(new Stack<Long>());
        allocationSizeMap.set(new HashMap<Object, Long>());
        referenceQueue.set(new ReferenceQueue<Object>());
    }

    private AllocatableResource(String name, long max) {
        super(name, max);
        lastAllocationSize.set(new Stack<Long>());
        allocationSizeMap.set(new HashMap<Object, Long>());
        referenceQueue.set(new ReferenceQueue<Object>());
    }

    public void registerAllocation(Object o) {
      if (lastAllocationSize.get().size() == 0) {
        //System.out.println("TFKERROR: stack empty on register");
        return;
      }
      long value = lastAllocationSize.get().pop();
      Object ref = new WeakReference<Object>(o, referenceQueue.get());
      allocationSizeMap.get().put(ref, value);
    }

    @Override
    public void increment(long delta) {
        if (current + delta > 3 * max / 4) {
          if (System.currentTimeMillis() - lastGC > 1 &&
              Account.RECLAIM_ALLOCATABLE_RESOURCES) {
            System.gc();
            lastGC = System.currentTimeMillis();
          }
        // re-collect freed memory.
        Object o = referenceQueue.get().poll();
        while (o != null) {
          Long size = allocationSizeMap.get().get(o);
          if (size == null) {
            o = referenceQueue.get().poll();
            continue;
          }
          if (Account.RECLAIM_ALLOCATABLE_RESOURCES) {
            current = current - size;
          }
          // Remove associated entry in the allocationSizeMap.
          allocationSizeMap.get().remove(o);
          o = referenceQueue.get().poll();
        }
        }
        super.increment(delta);
        lastAllocationSize.get().push(new Long(delta));
    }

    @Override
    public Resource fork(long newMax) {
        this.checkIncrement(newMax);
        current = current + newMax;
        return new AllocatableResource(name, newMax);
    }
}
