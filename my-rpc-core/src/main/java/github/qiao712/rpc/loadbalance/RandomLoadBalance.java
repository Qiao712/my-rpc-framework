package github.qiao712.rpc.loadbalance;

import github.qiao712.rpc.proto.RpcRequest;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomLoadBalance implements LoadBalance{
    private final static ThreadLocalRandom random = ThreadLocalRandom.current();
    @Override
    public InetSocketAddress select(List<InetSocketAddress> serviceInstances, RpcRequest rpcRequest) {
        return serviceInstances.isEmpty() ? null : serviceInstances.get(random.nextInt(serviceInstances.size()));
    }
}