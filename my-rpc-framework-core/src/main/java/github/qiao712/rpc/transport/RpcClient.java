package github.qiao712.rpc.transport;

import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.proto.RpcResponse;
import github.qiao712.rpc.proto.SerializationType;

import java.net.InetSocketAddress;

/**
 * 负责向服务提供者发送请求，进行远程调用
 */
public interface RpcClient {
    /**
     * 向服务提供者发送调用请求
     */
    RpcResponse request(InetSocketAddress providerAddress, RpcRequest rpcRequest);


    //各种属性---------------------------------------------------------
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

    /**
     * 获取心跳间隔时间
     */
    long getHeartbeatInterval();

    /**
     * 设置心跳间隔时间(ms)
     */
    void setHeartbeatInterval(long heartbeatInterval);
}
