package github.qiao712.rpc.exception;

/**
 * 客户端相关异常
 */
public class RpcClientException extends RpcFrameworkException{
    public RpcClientException() {
    }

    public RpcClientException(String message) {
        super(message);
    }

    public RpcClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcClientException(Throwable cause) {
        super(cause);
    }

    public RpcClientException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
