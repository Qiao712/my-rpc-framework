package github.qiao712.rpc.invoker;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.loadbalance.LoadBalance;
import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.proto.RpcResponse;
import github.qiao712.rpc.proto.RpcResponseCode;
import github.qiao712.rpc.registry.ServiceDiscovery;
import github.qiao712.rpc.transport.RpcClient;

import java.net.InetSocketAddress;
import java.util.List;

public class DefaultInvoker extends Invoker{
    /**
     * 通过服务名、方法名、参数发起远程调用
     *
     * @param rpcClient
     * @param serviceDiscovery
     * @param loadBalance
     */
    public DefaultInvoker(RpcClient rpcClient, ServiceDiscovery serviceDiscovery, LoadBalance loadBalance) {
        super(rpcClient, serviceDiscovery, loadBalance);
    }

    @Override
    public Object invoke(String serviceName, String methodName, Object[] args) {
        //获取服务实例列表
        List<InetSocketAddress> serviceInstances = serviceDiscovery.getServiceInstances(serviceName);
        if(serviceInstances.isEmpty()){
            throw new RpcException("无可用服务提供者(service name = " + serviceName + ")");
        }

        RpcRequest rpcRequest = new RpcRequest(serviceName, methodName, args);

        //由负载均衡策略选择
        InetSocketAddress selected = loadBalance.select(serviceInstances, rpcRequest);

        RpcResponse rpcResponse = rpcClient.request(selected, rpcRequest);
        if(rpcResponse.getCode() != RpcResponseCode.SUCCESS){
            if (rpcResponse.getCode() == RpcResponseCode.METHOD_THROWING){
                //重新抛出服务提供者函数抛出的异常
                throw new RpcException("异常返回:" + rpcResponse.getCode(), (Throwable) rpcResponse.getData());
            }else{
                throw new RpcException("请求失败:" + rpcResponse.getCode());
            }
        }

        return rpcResponse.getData();
    }
}
