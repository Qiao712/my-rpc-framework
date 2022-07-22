package github.qiao712.rpc.loadbalance;

import github.qiao712.rpc.proto.RpcRequest;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 平滑权重轮询算法(Smooth weighted round-robin balancing)
 */
public class SmoothWeightedRoundRobinLoadBalance implements LoadBalance{
    @Override
    public InetSocketAddress select(List<InetSocketAddress> serviceInstances, RpcRequest rpcRequest) {
        return null;
    }
}
