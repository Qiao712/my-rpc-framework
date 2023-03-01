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

public abstract class AbstractCluster implements Cluster{
    protected final RpcClient rpcClient;
    protected final ServiceRegistry serviceRegistry;
    protected final LoadBalance loadBalance;

    public AbstractCluster(RpcClient rpcClient, ServiceRegistry serviceRegistry, LoadBalance loadBalance) {
        this.rpcClient = rpcClient;
        this.serviceRegistry = serviceRegistry;
        this.loadBalance = loadBalance;
    }

    @Override
    public Object invoke(String serviceName, String methodName, Object[] args, Class<?>[] argTypes) {
        //获取服务实例列表
        List<ProviderURL> providers = serviceRegistry.getProviders(serviceName);

        //组装请求对象
        RpcRequest rpcRequest = new RpcRequest(serviceName, methodName, args, argTypes);

        //交给容错策略进行调用
        RpcResponse rpcResponse = doInvoke(providers, rpcRequest);

        if(rpcResponse.getCode() == RpcResponseCode.SUCCESS){
            //成功返回结果
            return rpcResponse.getData();
        }else if(rpcResponse.getCode() == RpcResponseCode.METHOD_THROWING){
            //被调用的方法抛出异常，包装后抛出
            throw new RpcException("服务端异常:方法抛出异常", (Throwable) rpcResponse.getData());
        }else{
            //其他的错误原因
            throw new RpcException("服务端异常:" + rpcResponse.getCode().getDescription());
        }
    }

    /**
     * 按容错策略进行调用
     */
    protected abstract RpcResponse doInvoke(List<ProviderURL> providers, RpcRequest rpcRequest);

    /**
     * 通过RpcClient发送调用请求，处理返回对象
     */
    protected RpcResponse doRequest(ProviderURL provider, RpcRequest rpcRequest){
        return rpcClient.request(provider.getAddress(), rpcRequest);
    }
}
