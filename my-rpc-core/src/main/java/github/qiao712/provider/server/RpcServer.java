package github.qiao712.provider.server;

import github.qiao712.proto.SerializationType;

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
