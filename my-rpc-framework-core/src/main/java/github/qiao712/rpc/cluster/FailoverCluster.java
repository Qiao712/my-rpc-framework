package github.qiao712.rpc.cluster;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.loadbalance.LoadBalance;
import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.proto.RpcResponse;
import github.qiao712.rpc.registry.ProviderURL;
import github.qiao712.rpc.registry.ServiceDiscovery;
import github.qiao712.rpc.transport.RpcClient;

import java.util.ArrayList;
import java.util.List;

/**
 * 若调用失败，尝试其他服务提供者
 * 抛出最后一次失败的异常
 */
public class FailoverCluster extends AbstractCluster{
    private final static int DEFAULT_RETRIES = 5;
    private int retries = DEFAULT_RETRIES;

    public FailoverCluster(RpcClient rpcClient, ServiceDiscovery serviceDiscovery, LoadBalance loadBalance) {
        super(rpcClient, serviceDiscovery, loadBalance);
    }

    @Override
    protected RpcResponse doInvoke(List<ProviderURL> providers, RpcRequest rpcRequest) {
        if(providers.isEmpty()){
            throw new RpcException("无可用服务提供者");
        }

        List<ProviderURL> providersCopy = providers;
        RpcException lastException = null;
        int i;
        for(i = 0; i < retries && !providers.isEmpty(); i++){
            ProviderURL selected = loadBalance.select(providersCopy, rpcRequest);

            try{
                return doRequest(selected, rpcRequest);
            }catch (RpcException e){
                lastException = e;

                if(providersCopy.size() == 1) break;

                //移除失败的节点,修改前先复制
                if(providersCopy == providers){
                    providersCopy = new ArrayList<>(providers);
                }
                providersCopy.remove(selected);
            }
        }

        throw new RpcException("请求失败. 已尝试" + i + "次. 最后一次尝试失败原因:" + lastException, lastException);
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        if(retries < 0){
            throw new IllegalArgumentException("retries < 0.");
        }
        this.retries = retries;
    }
}
