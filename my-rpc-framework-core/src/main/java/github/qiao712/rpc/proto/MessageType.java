package github.qiao712.rpc.proto;

/**
 * 消息类型
 */
public enum MessageType{
    REQUEST(RpcRequest.class),
    RESPONSE(RpcResponse.class),
    PING(null),
    PONG(null);

    /**
     * 负载数据的对象类型
     */
    private final Class<?> payloadClass;

    MessageType(Class<?> payloadClass) {
        this.payloadClass = payloadClass;
    }

    public Class<?> getPayloadClass(){
        return payloadClass;
    }
}
