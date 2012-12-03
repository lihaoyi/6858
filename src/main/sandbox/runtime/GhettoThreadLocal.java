package sandbox.runtime;

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
