package github.qiao712.rpc.cluster;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.loadbalance.LoadBalance;
import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.registry.ServiceDiscovery;
import github.qiao712.rpc.transport.RpcClient;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 调用一次，若失败直接抛出异常
 */
public class FailfastCluster extends AbstractCluster{
    public FailfastCluster(RpcClient rpcClient, ServiceDiscovery serviceDiscovery, LoadBalance loadBalance) {
        super(rpcClient, serviceDiscovery, loadBalance);
    }

    @Override
    protected Object doInvoke(List<InetSocketAddress> serviceInstances, RpcRequest rpcRequest) {
        if(serviceInstances.isEmpty()){
            throw new RpcException("请求失败: 无可用服务提供者");
        }

        InetSocketAddress selected = loadBalance.select(serviceInstances, rpcRequest);

        return doRequest(selected, rpcRequest).getData();
    }
}
