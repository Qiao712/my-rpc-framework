package github.qiao712.rpc.transport;

import github.qiao712.rpc.proto.SerializationType;

public interface RpcServer {
    void start();

    /**
     * 设置响应的序列化方式
     */
    void setSerializationType(SerializationType serializationType);

    /**
     * 获取响应的序列化方式
     */
    SerializationType getSerializationType();
}
