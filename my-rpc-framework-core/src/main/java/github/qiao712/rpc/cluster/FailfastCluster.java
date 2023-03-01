package github.qiao712.rpc.cluster;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.loadbalance.LoadBalance;
import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.proto.RpcResponse;
import github.qiao712.rpc.registry.ProviderURL;
import github.qiao712.rpc.registry.ServiceRegistry;
import github.qiao712.rpc.transport.RpcClient;

import java.util.List;

/**
 * 调用一次，若失败直接抛出异常
 */
public class FailfastCluster extends AbstractCluster{
    public FailfastCluster(RpcClient rpcClient, ServiceRegistry serviceRegistry, LoadBalance loadBalance) {
        super(rpcClient, serviceRegistry, loadBalance);
    }

    @Override
    protected RpcResponse doInvoke(List<ProviderURL> providers, RpcRequest rpcRequest) {
        if(providers.isEmpty()){
            throw new RpcException("无可用服务提供者");
        }

        ProviderURL selected = loadBalance.select(providers, rpcRequest);

        return doRequest(selected, rpcRequest);
    }
}
