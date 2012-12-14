import java.io.File;
import java.io.IOException;
import java.lang.Object;
import java.lang.reflect.Array;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Attributes;

import sandbox.runtime.Account;

/*
* Unit test to test file access control by SecurityManager. The policy file
* should allow good_file.txt to be created, but not bad_file.txt
*/

public class FileTest {
    public static String main() {

        File good_file;
        good_file = new File("good_file.txt");
        try {
            good_file.createNewFile();
            System.out.println("Correctly created good_file.txt");
        } catch (IOException e) {
            System.err.println("IOException: ");
            System.err.println(e);
        } catch (SecurityException e) {
            System.out.println("Hmm should have been able to write to good_file.txt, but could not");
        }

        File bad_file;
        bad_file = new File("bad_file.txt");
        try {
            bad_file.createNewFile();
            System.out.println("Oh noes! Created bad_file.txt but shouldn't have!");
        } catch (IOException e) {
            System.err.println("IOException: ");
            System.err.println(e);
        } catch (SecurityException e) {
            System.out.println("Good! Blocked creation of bad_file.txt");
        }

        return "FileTest complete";

    }
}
