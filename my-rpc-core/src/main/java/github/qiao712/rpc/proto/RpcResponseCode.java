package github.qiao712.rpc.proto;

public enum RpcResponseCode {
    SUCCESS,
    SERVICE_NOT_FOUND,
    METHOD_NOT_FOUND,
    METHOD_THROWING,    //函数抛出异常
    SERVER_ERROR,       //调用失败
}
