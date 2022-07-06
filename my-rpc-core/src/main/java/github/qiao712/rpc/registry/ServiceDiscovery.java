package github.qiao712.rpc.registry;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Set;

/**
 * 服务发现
 * 通过服务名获取服务信息(地址、端口)
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
     * 获取服务实例列表(当前即为提供者地址列表)
     */
    Set<InetSocketAddress> getServiceInstances(String serviceName);
}
