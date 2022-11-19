package github.qiao712.rpc.registry;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 服务对象的容器
 * 负载对象的发布
 */
public class ServiceProvider{
    private final InetSocketAddress serverAddress;        //对外暴露的地址
    private final ServiceRegistry serviceRegistry;
    private final ConcurrentMap<String, Object> serviceMap = new ConcurrentHashMap<>();

    public ServiceProvider(InetSocketAddress serverAddress, ServiceRegistry serviceRegistry) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
    }

    public Object getServiceObject(String serviceName) {
        return serviceMap.get(serviceName);
    }

    /**
     * 添加服务对象，并注册
     */
    public void addService(Object service, int weight) {
        Objects.requireNonNull(service);

        String serviceName = getServiceName(service);
        serviceMap.put(serviceName, service);

        //注册至注册中心
        ProviderURL url = new ProviderURL();
        url.setService(serviceName);
        url.setAddress(serverAddress);
        url.setWeight(weight);
        serviceRegistry.register(url);
    }

    public String getServiceName(Object service){
        //以Service对类实现的第一个接口的全限定名作为服务名进行注册
        Class<?>[] interfaces = service.getClass().getInterfaces();
        if(interfaces.length != 1) throw new IllegalArgumentException("服务类必须实现一个接口");
        return interfaces[0].getCanonicalName();
    }
}
