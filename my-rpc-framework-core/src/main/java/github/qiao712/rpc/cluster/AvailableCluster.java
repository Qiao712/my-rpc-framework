package github.qiao712.rpc.cluster;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.loadbalance.LoadBalance;
import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.registry.ServiceDiscovery;
import github.qiao712.rpc.transport.RpcClient;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 不进行负载均衡，遍历整个列表，直到调用成功
 * 抛出最后一次失败的异常
 */
public class AvailableCluster extends AbstractCluster{
    public AvailableCluster(RpcClient rpcClient, ServiceDiscovery serviceDiscovery, LoadBalance loadBalance) {
        super(rpcClient, serviceDiscovery, loadBalance);
    }

    @Override
    protected Object doInvoke(List<InetSocketAddress> serviceInstances, RpcRequest rpcRequest) {
        if(serviceInstances.isEmpty()){
            throw new RpcException("请求失败: 无可用服务提供者");
        }

        RpcException lastException = null;
        for (InetSocketAddress serviceInstance : serviceInstances) {
            try{
                return doRequest(serviceInstance, rpcRequest).getData();
            }catch (RpcException e){
                lastException = e;
            }
        }

        throw lastException;
    }
}
