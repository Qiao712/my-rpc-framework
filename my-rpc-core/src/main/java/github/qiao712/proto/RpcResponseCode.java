package github.qiao712.proto;

public enum RpcResponseCode {
    SUCCESS,
    FAILURE,
    SERVICE_NOT_FOUND,
    METHOD_NOT_FOUND,
    METHOD_THROWING,    //函数抛出异常
}
