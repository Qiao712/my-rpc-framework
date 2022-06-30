package github.qiao712.rpc.proxy;

/**
 * 为接口创建代理
 */
public interface RpcProxyFactory {
    <T> T createProxy(String serviceName, Class<T> cls);
}
