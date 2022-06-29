package github.qiao712.exception;

public class RpcSerializationException extends RpcException{
    public RpcSerializationException() {
    }

    public RpcSerializationException(String message) {
        super(message);
    }

    public RpcSerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcSerializationException(Throwable cause) {
        super(cause);
    }

    public RpcSerializationException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
