package github.qiao712.rpc.proxy;

import github.qiao712.rpc.registry.ServiceDiscovery;
import github.qiao712.rpc.transport.RpcClient;

/**
 * 用于发起远程调用
 */
public abstract class Invoker {
    protected final RpcClient rpcClient;
    protected final ServiceDiscovery serviceDiscovery;

    /**
     * 通过服务名、方法名、参数发起远程调用
     */
    public Invoker(RpcClient rpcClient, ServiceDiscovery serviceDiscovery) {
        this.rpcClient = rpcClient;
        this.serviceDiscovery = serviceDiscovery;
    }

    abstract Object invoke(String serviceName, String methodName, Object[] args);
}
