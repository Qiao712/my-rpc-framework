package github.qiao712.rpc.cluster;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.loadbalance.LoadBalance;
import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.proto.RpcResponse;
import github.qiao712.rpc.proto.RpcResponseCode;
import github.qiao712.rpc.registry.ProviderURL;
import github.qiao712.rpc.registry.ServiceRegistry;
import github.qiao712.rpc.transport.RpcClient;

import java.util.List;

/**
 * 失败时不抛出异常，返回空对象
 * 应注意返回值类型为内置类型时，自动解包引发空指针异常
 */
public class FailsafeCluster extends AbstractCluster {
    public FailsafeCluster(RpcClient rpcClient, ServiceRegistry serviceRegistry, LoadBalance loadBalance) {
        super(rpcClient, serviceRegistry, loadBalance);
    }

    @Override
    protected RpcResponse doInvoke(List<ProviderURL> providers, RpcRequest rpcRequest) {
        if(providers.isEmpty()){
            return null;
        }

        ProviderURL selected = loadBalance.select(providers, rpcRequest);

        try{
            return doRequest(selected, rpcRequest);
        }catch (RpcException rpcException){
            //伪装成正确的调用，但返回空值
            return new RpcResponse(RpcResponseCode.SUCCESS, null);
        }
    }
}
