package github.qiao712.rpc.proxy;

import github.qiao712.rpc.cluster.Cluster;

/**
 * 为接口创建代理
 */
public interface RpcProxyFactory {
    <T> T createProxy(Class<?> serviceClass, String serviceName, Cluster cluster);
}
