package github.qiao712.rpc.cluster;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.loadbalance.LoadBalance;
import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.proto.RpcResponse;
import github.qiao712.rpc.registry.ProviderURL;
import github.qiao712.rpc.registry.ServiceDiscovery;
import github.qiao712.rpc.transport.RpcClient;

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
    protected RpcResponse doInvoke(List<ProviderURL> providers, RpcRequest rpcRequest) {
        if(providers.isEmpty()){
            throw new RpcException("无可用服务提供者");
        }

        RpcException lastException = null;
        for (ProviderURL provider : providers) {
            try{
                return doRequest(provider, rpcRequest);
            }catch (RpcException e){
                lastException = e;
            }
        }

        throw lastException;
    }
}
