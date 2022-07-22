package github.qiao712.rpc.cluster;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.loadbalance.LoadBalance;
import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.registry.ServiceDiscovery;
import github.qiao712.rpc.transport.RpcClient;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 失败时不抛出异常，返回空对象
 * 应注意返回值类型为内置类型时，自动解包引发空指针异常
 */
public class FailsafeCluster extends AbstractCluster {
    public FailsafeCluster(RpcClient rpcClient, ServiceDiscovery serviceDiscovery, LoadBalance loadBalance) {
        super(rpcClient, serviceDiscovery, loadBalance);
    }

    @Override
    protected Object doInvoke(List<InetSocketAddress> serviceInstances, RpcRequest rpcRequest) {
        if(serviceInstances.isEmpty()){
            return null;
        }

        InetSocketAddress selected = loadBalance.select(serviceInstances, rpcRequest);

        try{
            return doRequest(selected, rpcRequest).getData();
        }catch (RpcException rpcException){
            return null;
        }
    }
}
