package github.qiao712.rpc.registry;

/**
 * 服务注册
 * 发布服务信息(地址、端口)，以使该服务可被发现
 */
public interface ServiceRegistry {
    void register(String serviceName);

    void unregister(String serviceName);
}
