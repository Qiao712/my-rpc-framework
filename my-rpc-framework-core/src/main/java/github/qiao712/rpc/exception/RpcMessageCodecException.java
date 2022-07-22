package github.qiao712.rpc.exception;

public class RpcMessageCodecException extends RpcException{
    public RpcMessageCodecException() {
    }

    public RpcMessageCodecException(String message) {
        super(message);
    }

    public RpcMessageCodecException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcMessageCodecException(Throwable cause) {
        super(cause);
    }

    public RpcMessageCodecException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
