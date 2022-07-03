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

    void setSerializationType(SerializationType serializationType);

    SerializationType getSerializationType();

    /**
     * 获取响应的超时时间(ms). 为0表示一直等待.
     */
    long getResponseTimeout();

    /**
     * 设置响应的超时时间(ms). 为0表示一直等待.
     */
    void setResponseTimeout(long timeout);
}
