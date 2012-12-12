package sandbox.runtime;

/**
 * A poor man's implementation of a ThreadLocal variable. This is needed
 * because normal thread-locals have dependencies on a large number of
 * other classes, and every single one of those classes will have to be
 * un-instrumented in order to prevent an infinite recursion (where
 * the instrumentation calls some method which is itself instrumented)
 */
public class GhettoThreadLocal {
    private long[] data = new long[1024];

    public void enable() {
        int id = (int) (Thread.currentThread().getId() % (1024 * 64));

        data[id >> 6] |= 1 << (id & 0b111111);
    }

    public void disable() {
        int id = (int) (Thread.currentThread().getId() % (1024 * 64));
        data[id >> 6] &= ~(1 << (id & 0b111111));
    }

    public boolean check() {
        int id = (int) (Thread.currentThread().getId() % (1024 * 64));
        return (data[id >> 6] & (1 << (id & 0b111111))) != 0;
    }
}
