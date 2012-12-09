package sandbox.runtime;

/**
 * Models a simple resource, which is some value which is limited. Provides
 * functionality for checking if a resource can be incremented and for
 * incrementing it. Also provides for forking off some fraction of this
 * resource into a child resource, and joining it back later.
 */
public class Resource {
    public long max;
    public long current = 0;
    public String name;

    public Resource(String name) {
        this.name = name;
        this.max = Long.MAX_VALUE;
    }

    protected Resource(String name, long max) {
        this.name = name;
        this.max = max;
    }

    public void checkIncrement(long delta) {
        if (current + delta > max) {
            throw new sandbox.runtime.ResourceLimitException(
                    "Too Much " + name + " Use! Additional " + delta +
                            " + existing " + this.current +
                            " would exceed maximum " + this.max
            );
        }
    }

    public void increment(int[] counts, int size) {
        long product = 1;
        for (int i = 0; i < counts.length; i++) {
            if (counts[i] == 0) return;
            if (counts[i] > Long.MAX_VALUE / product) {
                throw new sandbox.runtime.ResourceLimitException("Trying to allocate more than 2^63 bytes of memory");
            }
            product = product * counts[i];
        }
        increment(product * size);
    }

    public void increment(long delta) {
        checkIncrement(delta);
        current = current + delta;
    }

    public void join(Resource child) {
        assert (this.name.equals(child.name));
        this.current = this.current - child.max + child.current;
    }

    public Resource fork(long newMax) {
        this.checkIncrement(newMax);
        current = current + newMax;
        return new Resource(name, newMax);
    }

    public String toString() {
        return name + ": " + current + "/" + max;
    }
}
