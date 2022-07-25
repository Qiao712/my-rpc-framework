package github.qiao712.rpc.cluster;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.loadbalance.LoadBalance;
import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.registry.ServiceDiscovery;
import github.qiao712.rpc.transport.RpcClient;

import java.net.InetSocketAddress;
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
    protected Object doInvoke(List<InetSocketAddress> serviceInstances, RpcRequest rpcRequest) {
        if(serviceInstances.isEmpty()){
            throw new RpcException("请求失败: 无可用服务提供者.");
        }

        RpcException lastException = null;
        int i;
        for(i = 0; i < retries && !serviceInstances.isEmpty(); i++){
            InetSocketAddress selected = loadBalance.select(serviceInstances, rpcRequest);

            try{
                return doRequest(selected, rpcRequest).getData();
            }catch (RpcException e){
                serviceInstances.remove(selected);
                lastException = e;
            }
        }

        throw new RpcException("请求失败. 已尝试" + i + "次. 最后一次尝试失败原因:" + lastException, lastException);
    }

    public int getRetries() {
        return retries;
    }

    public void setRetries(int retries) {
        if(retries < 0){
            throw new IllegalArgumentException("retries小于0");
        }
        this.retries = retries;
    }
}
