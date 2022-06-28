package github.qiao712.registry;

/**
 * 注册服务
 */
public interface ServiceRegistry {
    void register(String serviceName, Object service);

    Object getService(String serviceName);
}
