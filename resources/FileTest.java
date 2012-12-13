import java.io.File;
import java.io.IOException;
import java.lang.Object;
import java.lang.reflect.Array;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Attributes;

import sandbox.runtime.Account;

public class FileTest {
    public static String main() {

        File f;
        f = new File("good_file.txt");
        try {
            f.createNewFile();
//            System.out.println("Correctly created good_file.txt");
        } catch (IOException e) {
            System.err.println("IOException: ");
            System.err.println(e);
        } catch (SecurityException e) {
//            System.err.println("Hmm should have been able to write to good_file.txt, but could not");
        }

        f = new File("bad_file.txt");
        try {
            f.createNewFile();
//            System.out.println("Oh noes! Created bad_file.txt but shouldn't have!");
        } catch (IOException e) {
            System.err.println("IOException: ");
            System.err.println(e);
        } catch (SecurityException e) {
//            System.err.println("Good! Blocked creation of bad_file.txt");
        }

        return "FileTest complete";

    }
}
