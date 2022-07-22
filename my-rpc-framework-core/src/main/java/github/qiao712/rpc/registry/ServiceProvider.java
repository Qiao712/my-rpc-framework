package github.qiao712.rpc.registry;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 服务对象的容器
 * 负载对象的发布
 */
public class ServiceProvider{
    private final ServiceRegistry serviceRegistry;
    private final ConcurrentMap<String, Object> serviceMap = new ConcurrentHashMap<>();

    public ServiceProvider(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public Object getService(String serviceName) {
        return serviceMap.get(serviceName);
    }

    /**
     * 添加服务对象，并注册
     */
    public void addService(Object service) {
        Objects.requireNonNull(service);

        String serviceName = getServiceName(service);
        serviceMap.put(serviceName, service);
        serviceRegistry.register(serviceName);
    }

    public String getServiceName(Object service){
        //以Service对类实现的第一个接口的全限定名作为服务名进行注册
        Class<?>[] interfaces = service.getClass().getInterfaces();
        if(interfaces.length == 0) throw new IllegalArgumentException("服务类必须实现一个接口");
        return interfaces[0].getCanonicalName();
    }
}
