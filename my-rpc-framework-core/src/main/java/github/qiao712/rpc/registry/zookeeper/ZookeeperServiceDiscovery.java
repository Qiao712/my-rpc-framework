package github.qiao712.rpc.registry.zookeeper;

import com.alibaba.fastjson.JSON;
import github.qiao712.rpc.registry.ProviderURL;
import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.registry.ServiceDiscovery;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.AddWatchMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class ZookeeperServiceDiscovery implements ServiceDiscovery, Closeable {
    private final static String NAME_SPACE = "my-rpc";
    private final CuratorFramework client;

    //服务名 - 服务实例信息(提供者地址...)列表 Map
    private final ConcurrentMap<String, List<ProviderURL>> providerMap = new ConcurrentHashMap<>();
    //服务名 - 该服务的节点的Watcher
    private final ConcurrentMap<String, Watcher> watcherMap = new ConcurrentHashMap<>();

    public ZookeeperServiceDiscovery(InetSocketAddress... zookeeperAddresses) {
        this(CuratorUtils.getAddressString(zookeeperAddresses));
    }

    public ZookeeperServiceDiscovery(String connectString){
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
        this.client = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .retryPolicy(retryPolicy)
                .namespace(NAME_SPACE)
                .build();
        this.client.start();
    }

    /**
     * 非线程安全的，一个服务只能由一个线程进行订阅操作
     */
    @Override
    public void subscribeService(String serviceName) {
        if(providerMap.containsKey(serviceName)) {
            throw new RpcException("重复订阅服务(" + serviceName + ")");
        }

        //重新拉取服务列表
        List<ProviderURL> providers = fetchProviders(serviceName);
        providerMap.put(serviceName, providers);

        //监听 /service-name/providers 节点及其子节点，改变时更新服务实例表
        String serviceProvidersNodePath = getServiceProvidersNodePath(serviceName);
        Watcher watcher = new Watcher() {
            //在表示服务提供者的节点改变时，更新提供者地址列表
            @Override
            public void process(WatchedEvent event) {
                if(event.getPath() != null && event.getPath().startsWith(serviceProvidersNodePath)){
                    if(event.getType() == Event.EventType.NodeCreated){
                        log.debug("发现新的服务提供者: {}", event.getPath());

                        ProviderURL provider = getProviderURL(event.getPath());
                        providerMap.computeIfPresent(serviceName, (serviceName, providers) -> {
                            providers.add(provider);
                            return providers;
                        });
                    }else if(event.getType() == Event.EventType.NodeDeleted){
                        log.debug("服务提供者下线: {}", event.getPath());

                        providerMap.computeIfPresent(serviceName, (serviceName, providers) -> {
                            ProviderURL provider = getProviderURL(event.getPath());
                            providers.remove(provider);
                            return providers;
                        });
                    }
                }
            }
        };

        try {
            client.watchers().add().withMode(AddWatchMode.PERSISTENT_RECURSIVE).usingWatcher(watcher).forPath(serviceProvidersNodePath);
        } catch (Exception e) {
            providerMap.remove(serviceName);
            throw new RpcException("服务订阅失败", e);
        }
        watcherMap.put(serviceName, watcher);
    }

    @Override
    public void unsubscribeService(String serviceName) {
        Watcher watcher = watcherMap.remove(serviceName);
        client.watchers().remove(watcher);
        providerMap.remove(serviceName);
    }

    @Override
    public List<ProviderURL> getProviders(String serviceName) {
        List<ProviderURL> providerAddress = providerMap.get(serviceName);
        if(providerAddress == null){
            throw new RpcException("未订阅服务(" + serviceName + ")");
        }
        return providerAddress;
    }

    @Override
    public void close() {
        client.close();
    }

    /**
     * 拉取某服务的提供者列表
     */
    private List<ProviderURL> fetchProviders(String serviceName){
        try {
            log.debug("拉取服务{}的提供者列表", serviceName);
            List<ProviderURL> providers = new ArrayList<>();

            List<String> providerNodePaths = client.getChildren().forPath(getServiceProvidersNodePath(serviceName));
            for (String providerNodePath : providerNodePaths) {
                ProviderURL provider = getProviderURL(providerNodePath);
                if(provider != null) providers.add(provider);
            }

            return providers;
        } catch (Exception e) {
            log.error("获取服务提供者列表失败(service name = " + serviceName + ")", e);
        }

        return new ArrayList<>();
    }

    /**
     * 生成表示该服务的提供者的节点的路径
     *  "/service-name/providers"
     */
    private String getServiceProvidersNodePath(String serviceName){
        return '/' + serviceName + "/providers";
    }

    /**
     * 从节点路径中获取ProviderURL
     * "/service-name/providers/127.0.0.1:2022?service=service-name&weight=123"
     */
    private ProviderURL getProviderURL(String path){
        String[] nodeNames = path.split("/");
        if(nodeNames.length == 0) return null;

        String providerNodeName = nodeNames[nodeNames.length - 1];
        try{
            return ProviderURL.parseURL(providerNodeName);
        }catch (Throwable e){
            log.error("提供者URL格式错误", e);
        }
        return null;
    }
}
