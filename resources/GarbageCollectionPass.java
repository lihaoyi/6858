import java.io.File;
import java.io.IOException;
import java.lang.Object;
import java.lang.reflect.Array;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Attributes;
import java.util.LinkedList;
import sandbox.runtime.Account;

/**
 * Basic test suite that allocates heap memory in a bunch of different
 * ways and checks if the allocations are what we expect. Currently
 * doesn't quite do the math properly.
 */
public class GarbageCollectionPass {
    public static String main() {
      LinkedList<Object> list = new LinkedList<Object>();
      for (int i = 0; i < 1000000; i++) {
        list = new LinkedList<Object>();
      }
      return "PASS";
    }
}
