package github.qiao712.rpc.proxy;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.proto.RpcResponse;
import github.qiao712.rpc.proto.RpcResponseCode;
import github.qiao712.rpc.registry.ServiceDiscovery;
import github.qiao712.rpc.transport.RpcClient;

import java.net.InetSocketAddress;
import java.util.Random;
import java.util.Set;

public class DefaultInvoker extends Invoker{
    private final Random random = new Random(System.nanoTime());

    public DefaultInvoker(RpcClient rpcClient, ServiceDiscovery serviceDiscovery) {
        super(rpcClient, serviceDiscovery);
    }

    @Override
    public Object invoke(String serviceName, String methodName, Object[] args) {
        //获取服务实例列表，并随机选择一个
        Set<InetSocketAddress> serviceInstances = serviceDiscovery.getServiceInstances(serviceName);
        if(serviceInstances.isEmpty()){
            throw new RpcException("无可用服务提供者(service name = " + serviceName + ")");
        }
        int instanceNum = serviceInstances.size();
        InetSocketAddress selected = (InetSocketAddress) serviceInstances.toArray()[random.nextInt(instanceNum)];

        //发送调用请求
        RpcRequest rpcRequest = new RpcRequest(serviceName, methodName, args);
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
