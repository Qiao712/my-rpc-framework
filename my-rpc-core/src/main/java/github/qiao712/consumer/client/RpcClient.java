package github.qiao712.consumer.client;

import github.qiao712.entity.RpcRequest;
import github.qiao712.entity.RpcResponse;

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
}
