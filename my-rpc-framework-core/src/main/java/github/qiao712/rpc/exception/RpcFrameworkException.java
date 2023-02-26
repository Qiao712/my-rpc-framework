package github.qiao712.rpc.exception;

public class RpcFrameworkException extends RuntimeException{
    public RpcFrameworkException() {
    }

    public RpcFrameworkException(String message) {
        super(message);
    }

    public RpcFrameworkException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcFrameworkException(Throwable cause) {
        super(cause);
    }

    public RpcFrameworkException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
