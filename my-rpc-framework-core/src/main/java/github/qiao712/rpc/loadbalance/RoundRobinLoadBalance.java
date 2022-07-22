package github.qiao712.rpc.loadbalance;

import github.qiao712.rpc.proto.RpcRequest;

import java.net.InetSocketAddress;
import java.util.List;

public class RoundRobinLoadBalance implements LoadBalance{
    private int index;

    @Override
    public InetSocketAddress select(List<InetSocketAddress> serviceInstances, RpcRequest rpcRequest) {
        if(serviceInstances.isEmpty()) return null;
        index = index+1 >= serviceInstances.size() ? 0 : index+1;
        return serviceInstances.get(index);
    }
}
