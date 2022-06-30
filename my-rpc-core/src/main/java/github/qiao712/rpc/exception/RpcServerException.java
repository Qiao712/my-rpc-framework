package github.qiao712.rpc.exception;

public class RpcServerException extends Exception {
    public RpcServerException() {
    }

    public RpcServerException(String message) {
        super(message);
    }

    public RpcServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcServerException(Throwable cause) {
        super(cause);
    }

    public RpcServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
