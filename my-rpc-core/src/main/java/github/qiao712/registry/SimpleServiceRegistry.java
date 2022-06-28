package github.qiao712.registry;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SimpleServiceRegistry implements ServiceRegistry{
    private final Map<String, Object> services = new ConcurrentHashMap<>();

    @Override
    public void register(String serviceName, Object service) {
        Objects.requireNonNull(serviceName, "服务名不可为空");
        Objects.requireNonNull(service, "服务对象不可为空");

        services.put(serviceName, service);
        log.debug("注册服务{}:{}", serviceName, service);
    }

    @Override
    public Object getService(String serviceName) {
        return services.get(serviceName);
    }
}
