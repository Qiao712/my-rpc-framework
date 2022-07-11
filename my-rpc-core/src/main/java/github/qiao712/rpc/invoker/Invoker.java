package github.qiao712.rpc.invoker;

import github.qiao712.rpc.loadbalance.LoadBalance;
import github.qiao712.rpc.registry.ServiceDiscovery;
import github.qiao712.rpc.transport.RpcClient;

/**
 * 用于发起远程调用
 * 整合服务发现、负载均衡、发送调用请求等操作
 */
public abstract class Invoker {
    protected final RpcClient rpcClient;
    protected final ServiceDiscovery serviceDiscovery;
    protected final LoadBalance loadBalance;

    /**
     * 通过服务名、方法名、参数发起远程调用
     */
    public Invoker(RpcClient rpcClient, ServiceDiscovery serviceDiscovery, LoadBalance loadBalance) {
        this.rpcClient = rpcClient;
        this.serviceDiscovery = serviceDiscovery;
        this.loadBalance = loadBalance;
    }

    abstract public Object invoke(String serviceName, String methodName, Object[] args);
}
