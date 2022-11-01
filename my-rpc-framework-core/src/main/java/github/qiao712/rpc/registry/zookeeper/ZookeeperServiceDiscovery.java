package github.qiao712.rpc.registry.zookeeper;

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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Slf4j
public class ZookeeperServiceDiscovery implements ServiceDiscovery, Closeable {
    private final static String NAME_SPACE = "my-rpc";
    private final CuratorFramework client;

    //服务名 - 服务实例信息(提供者地址...)列表 Map
    private final ConcurrentMap<String, List<InetSocketAddress>> providerMap = new ConcurrentHashMap<>();
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
        List<InetSocketAddress> providerAddresses = fetchProviderAddresses(serviceName);
        providerMap.put(serviceName, providerAddresses);

        //监听 /service-name/providers 节点及其子节点，改变时更新服务实例表
        String serviceProvidersNodePath = getServiceProvidersNodePath(serviceName);
        Watcher watcher = new Watcher() {
            //在表示服务提供者的节点改变时，更新提供者地址列表
            @Override
            public void process(WatchedEvent event) {
                if(event.getPath() != null && event.getPath().startsWith(serviceProvidersNodePath)){
                    if(event.getType() == Event.EventType.NodeCreated){
                        log.debug("发现新的服务提供者: {}", event.getPath());

                        InetSocketAddress newProvider = getProviderAddress(event.getPath());
                        List<InetSocketAddress> providers = providerMap.get(serviceName);
                        if(providers.contains(newProvider)){
                            providerMap.get(serviceName).add(newProvider);
                        }
                    }else if(event.getType() == Event.EventType.NodeDeleted){
                        log.debug("服务提供者下线: {}", event.getPath());

                        providerMap.get(serviceName).remove(getProviderAddress(event.getPath()));
                    }else if(event.getType() == Event.EventType.NodeDataChanged){
                        log.debug("服务提供者数据改变: {}", event.getPath());
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
    public List<InetSocketAddress> getServiceInstances(String serviceName) {
        List<InetSocketAddress> providerAddress = providerMap.get(serviceName);
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
    private List<InetSocketAddress> fetchProviderAddresses(String serviceName){
        try {
            log.debug("拉取服务{}的实例列表", serviceName);

            List<String> providers = client.getChildren().forPath(getServiceProvidersNodePath(serviceName));
            return providers.stream().distinct().map(this::getProviderAddress).filter(Objects::nonNull).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("获取服务提供者列表失败(service name = " + serviceName + ")", e);
        }

        return Collections.emptyList();
    }

    /**
     * 生成表示该服务的提供者的节点的路径
     *  "/service-name/providers"
     */
    private String getServiceProvidersNodePath(String serviceName){
        return '/' + serviceName + "/providers";
    }

    /**
     * 从节点路径中提取地址
     * "/service-name/providers/127.0.0.1:2022" -> 127.0.0.1:2022
     */
    private InetSocketAddress getProviderAddress(String path){
        String[] nodeNames = path.split("/");
        if(nodeNames.length == 0) return null;

        String providerNodeName = nodeNames[nodeNames.length - 1];
        String[] hostAndPort = providerNodeName.split(":");
        if(hostAndPort.length == 2){
            try{
                int port = Integer.parseInt(hostAndPort[1]);
                return new InetSocketAddress(hostAndPort[0], port);
            } catch (Throwable e) {
                return null;
            }
        }

        return null;
    }
}
