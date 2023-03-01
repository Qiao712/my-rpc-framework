package github.qiao712.rpc.registry.zookeeper;

import github.qiao712.rpc.exception.RpcFrameworkException;
import github.qiao712.rpc.registry.AbstractServiceRegistry;
import github.qiao712.rpc.registry.ProviderURL;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.AddWatchMode;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class ZookeeperServiceRegistry extends AbstractServiceRegistry {
    private final static String NAME_SPACE = "my-rpc";
    private final CuratorFramework client;

    /**
     * 用于监听service_xxx/provider节点及其后代变更(与Watcher相比CuratorWatcher可以抛出异常)
     */
    CuratorWatcher watcher = new ServiceNodeWatcher();

    /**
     * 订阅的服务的提供者列表
     */
    private final ConcurrentMap<String, List<ProviderURL>> providerMap = new ConcurrentHashMap<>();

    public ZookeeperServiceRegistry(InetSocketAddress... zookeeperAddresses){
        this(CuratorUtils.getAddressString(zookeeperAddresses));
    }

    public ZookeeperServiceRegistry(String connectString){
        this.client = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .retryPolicy(new RetryNTimes(1, 1000))
                .namespace(NAME_SPACE)
                .sessionTimeoutMs(10000)    //60s
                .build();

        //监听连接状态
        this.client.getConnectionStateListenable().addListener(new ConnectionStateListenerImpl());
        this.client.start();
    }

    @Override
    protected void doRegister(ProviderURL providerURL) throws Exception {
        String providerNodePath = toServiceProvidersNodePath(providerURL.getService()) + "/" + providerURL;
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(providerNodePath);
    }

    @Override
    protected void doUnregister(ProviderURL providerURL) throws Exception {
        String providerNodePath = toServiceProvidersNodePath(providerURL.getService()) + "/" + providerURL;
        client.delete().forPath(providerNodePath);
    }

    @Override
    protected void doSubscribe(String serviceName) throws Exception {
        //全量拉取服务提供者列表
        providerMap.compute(serviceName, (k, v)->{
            try {
                return fetchProviders(k);
            } catch (Exception e) {
                throw new RpcFrameworkException("拉取服务提供者列表", e);
            }
        });

        //注册监听器
        client.watchers().add().withMode(AddWatchMode.PERSISTENT_RECURSIVE).usingWatcher(watcher).forPath(toServiceProvidersNodePath(serviceName));
    }

    @Override
    protected void doUnsubscribe(String serviceName) throws Exception {
        //移除Watcher
        client.watchers().remove(watcher).forPath(toServiceProvidersNodePath(serviceName));
        //删除本地列表
        providerMap.remove(serviceName);
    }

    @Override
    protected List<ProviderURL> doGetProviders(String serviceName) {
        return providerMap.getOrDefault(serviceName, Collections.emptyList());
    }

    @Override
    public void close() {
        client.close();
    }

    /**
     * 拉取所有服务提供者节点
     */
    private List<ProviderURL> fetchProviders(String serviceName) throws Exception {
        List<String> providerNodePaths = client.getChildren().forPath(toServiceProvidersNodePath(serviceName));
        return providerNodePaths.stream().map(this::toProviderURL).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 生成表示该服务的提供者的节点的路径
     *  "/service-name/providers"
     */
    private String toServiceProvidersNodePath(String serviceName){
        return '/' + serviceName + "/providers";
    }

    /**
     * 从节点路径中获取ProviderURL
     * "/service-name/providers/127.0.0.1:2022?service=service-name&weight=123"
     */
    private ProviderURL toProviderURL(String path){
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

    public class ServiceNodeWatcher implements CuratorWatcher {
        final Pattern pathPattern = Pattern.compile("/(.*)/providers/.*");

        @Override
        public void process(WatchedEvent event) throws Exception {
            String path = event.getPath();
            if(event.getPath() == null) return;

            //节点所在的服务名
            String serviceName = null;
            Matcher matcher = pathPattern.matcher(path);
            if(matcher.find()){
                serviceName = matcher.group(1);
            }
            log.debug("ServiceName: {}", serviceName);

            //若子节点有变化，则重新拉取
            if(event.getType() == Watcher.Event.EventType.NodeCreated ||
                    event.getType() == Watcher.Event.EventType.NodeDeleted ||
                    event.getType() == Watcher.Event.EventType.NodeChildrenChanged){
                log.debug("重新拉取服务提供者列表");
                providerMap.computeIfPresent(serviceName, (k, v) -> {       //只在订阅时才进行拉取
                    try {
                        return fetchProviders(k);
                    } catch (Exception e) {
                        throw new RpcFrameworkException("重新拉取服务提供者列表失败", e);
                    }
                });
            }
        }
    };

    /**
     * 用于监听Session的变化，以在Session失效后，重新注册\订阅
     */
    private class ConnectionStateListenerImpl implements ConnectionStateListener {
        //会话是否改变，重新注册
        private long lastSessionId = -1L;

        @Override
        public void stateChanged(CuratorFramework client, ConnectionState newState) {
            long currentSessionId = -1L;
            try {
                currentSessionId = client.getZookeeperClient().getZooKeeper().getSessionId();
            } catch (Exception e) {
                log.warn("获取session失败");
            }

            switch (newState){
                case CONNECTED:{
                    log.info("已连接至Zookeeper (SessionId: {})", currentSessionId);
                    break;
                }

                case SUSPENDED:{
                    log.info("与Zookeeper断开链接 (SessionId: {})", currentSessionId);
                    break;
                }

                case RECONNECTED:{
                    log.info("重新连接至Zookeeper (SessionId: {})", currentSessionId);

                    if(lastSessionId != currentSessionId){
                        log.info("Zookeeper会话变更");
                        lastSessionId = currentSessionId;
                        recover();
                    }
                    break;
                }

                case LOST:{
                    log.info("Zookeeper会话失效  (SessionId: {})", currentSessionId);
                    break;
                }
            }
        }
    }
}
