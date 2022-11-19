package github.qiao712.rpc.registry;

import java.util.List;

/**
 * 服务发现
 */
public interface ServiceDiscovery {
    /**
     * 订阅服务
     * 当服务发生变动时获取新的列表
     */
    void subscribeService(String serviceName);

    /**
     * 取消订阅
     */
    void unsubscribeService(String serviceName);

    /**
     * 获取服务提供者列表
     */
    List<ProviderURL> getProviders(String serviceName);
}
