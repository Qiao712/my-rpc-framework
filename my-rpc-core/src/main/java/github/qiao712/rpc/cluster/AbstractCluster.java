package github.qiao712.rpc.cluster;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.loadbalance.LoadBalance;
import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.proto.RpcResponse;
import github.qiao712.rpc.proto.RpcResponseCode;
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
    public Object invoke(String serviceName, String methodName, Object[] args) {
        //获取服务实例列表
        List<InetSocketAddress> serviceInstances = serviceDiscovery.getServiceInstances(serviceName);

        RpcRequest rpcRequest = new RpcRequest(serviceName, methodName, args);

        return doInvoke(serviceInstances, rpcRequest);
    }

    /**
     * 按容错策略进行调用
     */
    protected abstract Object doInvoke(List<InetSocketAddress> serviceInstances, RpcRequest rpcRequest);

    /**
     * 通过RpcClient发送调用请求，处理返回对象
     * @return RpcResponse 调用成功时返回
     * @throws RpcException 描述调用失败的原因
     */
    protected RpcResponse doRequest(InetSocketAddress selected, RpcRequest rpcRequest){
        RpcResponse rpcResponse = rpcClient.request(selected, rpcRequest);

        if(rpcResponse.getCode() == RpcResponseCode.SUCCESS){
            //调用成功
            return rpcResponse;
        }else if (rpcResponse.getCode() == RpcResponseCode.METHOD_THROWING){
            //重新抛出服务提供者函数抛出的异常
            throw new RpcException("异常返回(" + selected + "):" + rpcResponse.getCode(), (Throwable) rpcResponse.getData());
        }else{
            //调用失败
            throw new RpcException("调用失败(" + selected + "):" + rpcResponse.getCode());
        }
    }
}
