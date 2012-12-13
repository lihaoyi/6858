import java.io.*;
import java.net.*;
import java.security.AccessControlException;

public class SocketTest {

    public static String main() throws IOException {

        Socket socket = null;
        PrintWriter out = null;
        BufferedReader in = null;

        System.out.println("SocketTest: Should allow socket access to fake_good_address, but not to fake_bad_address");
        try {
            socket = new Socket("fake_good_address", 4444);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Socket connected correctly to fake_good_address!");

            out.close();
            in.close();
            socket.close();

        } catch (UnknownHostException e) {
            System.err.println("Socket was allowed to try connecting to fake_good_address!");
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to fake_good_address.");
        } catch (AccessControlException e) {
            System.out.println("Hmm...wrongly blocked access to fake_good_address");
        }

        try {
            socket = new Socket("fake_bad_address", 4444);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("Socket connected correctly to fake_bad_address...");

            out.close();
            in.close();
            socket.close();

        } catch (UnknownHostException e) {
            System.err.println("Socket was allowed to try connecting to fake_bad_address...");
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to fake_bad_address.");
        } catch (AccessControlException e) {
            System.out.println("Successfully blocked socket access to fake_bad_address!");
        }


        return "SocketTest complete!";
    }
}
