package github.qiao712.rpc.loadbalance;

import github.qiao712.rpc.proto.RpcRequest;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 负载均衡策略
 */
public interface LoadBalance {
    InetSocketAddress select(List<InetSocketAddress> serviceInstances, RpcRequest rpcRequest);
}
