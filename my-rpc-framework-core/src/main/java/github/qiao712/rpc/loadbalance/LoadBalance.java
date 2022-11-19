package github.qiao712.rpc.loadbalance;

import github.qiao712.rpc.proto.RpcRequest;
import github.qiao712.rpc.registry.ProviderURL;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 负载均衡策略
 */
public interface LoadBalance {
    ProviderURL select(List<ProviderURL> providers, RpcRequest rpcRequest);
}
