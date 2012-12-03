package sandbox.runtime;

/**
 * Exception that indicates that the bytecode is trying to access some
 * prohibited native method call.
 */
public class BannedNativeException extends RuntimeException {
    public BannedNativeException(String msg) {
        super(msg);
    }
}
