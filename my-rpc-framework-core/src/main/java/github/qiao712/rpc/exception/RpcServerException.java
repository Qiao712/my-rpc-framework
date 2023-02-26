package github.qiao712.rpc.exception;

/**
 * 服务端相关异常
 */
public class RpcServerException extends RpcFrameworkException{
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
