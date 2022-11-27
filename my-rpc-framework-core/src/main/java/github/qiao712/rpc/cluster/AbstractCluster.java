package github.qiao712.rpc.cluster;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.loadbalance.LoadBalance;
import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.proto.RpcResponse;
import github.qiao712.rpc.proto.RpcResponseCode;
import github.qiao712.rpc.registry.ProviderURL;
import github.qiao712.rpc.registry.ServiceDiscovery;
import github.qiao712.rpc.transport.RpcClient;

import java.net.InetSocketAddress;
import java.util.List;

public abstract class AbstractCluster implements Cluster{
    protected final RpcClient rpcClient;
    protected final ServiceDiscovery serviceDiscovery;
    protected final LoadBalance loadBalance;

    public AbstractCluster(RpcClient rpcClient, ServiceDiscovery serviceDiscovery, LoadBalance loadBalance) {
        this.rpcClient = rpcClient;
        this.serviceDiscovery = serviceDiscovery;
        this.loadBalance = loadBalance;
    }

    @Override
    public Object invoke(String serviceName, String methodName, Object[] args, Class<?>[] argTypes) {
        //获取服务实例列表
        List<ProviderURL> providers = serviceDiscovery.getProviders(serviceName);

        RpcRequest rpcRequest = new RpcRequest(serviceName, methodName, args, argTypes);

        return doInvoke(providers, rpcRequest).getData();
    }

    /**
     * 按容错策略进行调用
     * @return
     */
    protected abstract RpcResponse doInvoke(List<ProviderURL> providers, RpcRequest rpcRequest);

    /**
     * 通过RpcClient发送调用请求，处理返回对象
     * @return RpcResponse 调用成功时返回
     * @throws RpcException 描述调用失败的原因
     */
    protected RpcResponse doRequest(ProviderURL provider, RpcRequest rpcRequest){
        RpcResponse rpcResponse = rpcClient.request(provider.getAddress(), rpcRequest);

        if(rpcResponse.getCode() == RpcResponseCode.SUCCESS){
            //调用成功
            return rpcResponse;
        }else if (rpcResponse.getCode() == RpcResponseCode.METHOD_THROWING){
            //重新抛出服务提供者函数抛出的异常
            throw new RpcException("异常返回(" + provider + "):" + rpcResponse.getCode(), (Throwable) rpcResponse.getData());
        }else{
            //调用失败
            throw new RpcException("调用失败(" + provider + "):" + rpcResponse.getCode());
        }
    }
}
