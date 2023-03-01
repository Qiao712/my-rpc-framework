package github.qiao712.rpc.registry;

import java.io.Closeable;
import java.util.List;

/**
 * 注册中心接口
 * 提供对服务者地址的注册、订阅功能
 */
public interface ServiceRegistry extends Closeable {
    /**
     * 注册服务提供者地址
     */
    void register(ProviderURL providerURL);

    /**
     * 取消注册服务提供者地址
     */
    void unregister(ProviderURL providerURL);

    /**
     * 订阅某服务的提供者的地址
     */
    void subscribe(String serviceName);

    /**
     * 取消订阅某服务的提供者地址
     */
    void unsubscribe(String serviceName);

    /**
     * 获取服务提供者列表
     */
    List<ProviderURL> getProviders(String serviceName);

    void close();
}
