package github.qiao712.rpc.transport;

import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.proto.RpcResponse;
import github.qiao712.rpc.proto.SerializationType;

/**
 * 负责向服务提供者发送请求，进行远程调用
 */
public interface RpcClient {
    /**
     * 向服务提供者发送调用请求
     */
    RpcResponse request(RpcRequest rpcRequest);

    /**
     * 通过服务名、方法名、参数进行远程调用
     */
    Object invoke(String serviceName, String methodName, Object[] args);

    /**
     * 设置请求的序列化方式
     */
    void setSerializationType(SerializationType serializationType);

    /**
     * 获取请求的序列化方式
     */
    SerializationType getSerializationType();
}
