package github.qiao712.rpc.proto;

public enum RpcResponseCode {
    SUCCESS("调用成功"),
    SERVICE_NOT_FOUND("未找到服务"),
    METHOD_NOT_FOUND("未找到方法"),
    METHOD_THROWING("被调用函数抛出异常"),    //函数抛出异常
    SERVER_ERROR("调用失败");               //服务端调用失败

    private final String description;

    RpcResponseCode(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
