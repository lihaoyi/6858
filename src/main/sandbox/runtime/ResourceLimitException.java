package sandbox.runtime;

/**
 * Exception that indicates that a sandbox.runtime-enforced resource limit
 * has been exceeded
 */
public class ResourceLimitException extends RuntimeException {
    public ResourceLimitException(String msg){
        super(msg);
    }
}
