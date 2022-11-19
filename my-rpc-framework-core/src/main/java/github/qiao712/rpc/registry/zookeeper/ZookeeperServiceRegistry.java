package github.qiao712.rpc.registry.zookeeper;

import github.qiao712.rpc.registry.ProviderURL;
import github.qiao712.rpc.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryForever;
import org.apache.zookeeper.CreateMode;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Slf4j
public class ZookeeperServiceRegistry implements ServiceRegistry, Closeable {
    private final static String NAME_SPACE = "my-rpc";
    private final CuratorFramework client;

    private final ConcurrentMap<String, ProviderURL> registeredServices = new ConcurrentHashMap<>();
    private final ConcurrentSkipListSet<String> failToDelete = new ConcurrentSkipListSet<>();   //删除失败的节点，等连接恢复后重试

    public ZookeeperServiceRegistry(InetSocketAddress... zookeeperAddresses){
        this(CuratorUtils.getAddressString(zookeeperAddresses));
    }

    public ZookeeperServiceRegistry(String connectString){

        RetryPolicy retryPolicy = new RetryForever(10000);
        this.client = CuratorFrameworkFactory.builder()
                .connectString(connectString)
                .retryPolicy(retryPolicy)
                .namespace(NAME_SPACE)
                .sessionTimeoutMs(60000)    //60s
                .build();

        this.client.getConnectionStateListenable().addListener(new SessionChangeListener());

        this.client.start();
    }

    @Override
    public void register(ProviderURL url) {
        //添加临时节点  /service-name(interface name)/providers/provider-address(host:port)
        try {
            //服务提供者元数据
            registeredServices.put(url.getService(), url);

            String providerNodePath = getProviderNodePath(url);
            failToDelete.remove(providerNodePath);
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(providerNodePath);
        } catch (Exception e) {
            log.error("注册服务 " + url + " 失败", e);
        }
    }

    @Override
    public void unregister(String serviceName) {
        ProviderURL provider = registeredServices.remove(serviceName);
        if(provider == null) return;
        String providerNodePath = getProviderNodePath(provider);

        try {
            client.delete().forPath(providerNodePath);
        } catch (Exception e) {
            failToDelete.add(providerNodePath);
            log.error("服务取消注册失败", e);
        }
    }

    @Override
    public void close() {
        client.close();
    }

    /**
     * 生成表示该提供者的节点的路径
     *  "/service-name(interface name)/providers/provider-address(host:port)"
     */
    private String getProviderNodePath(ProviderURL provider){
        return '/' + provider.getService() + "/providers/" + provider;
    }

    /**
     * 重新注册所有节点
     */
    private void registerAll(){
        registeredServices.forEach((serviceName, provider)->{
            try {
                client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(getProviderNodePath(provider));
            } catch (Exception e) {
                log.error("注册服务 " + serviceName + " 失败", e);
            }
        });
    }

    /**
     * 用于监听Session的变化，以重新注册服务信息
     */
    private class SessionChangeListener implements ConnectionStateListener {
        //会话是否改变，重新注册
        private boolean sessionChanged = false;

        @Override
        public void stateChanged(CuratorFramework client, ConnectionState newState) {
            switch (newState){
                case CONNECTED:{
                    log.info("已连接至Zookeeper");
                    break;
                }

                case RECONNECTED:{
                    log.info("重新连接至Zookeeper");
                    if(sessionChanged){
                        //重新注册服务
                        registerAll();
                        sessionChanged = false;
                    }

                    //重新删除 删除失败的节点
                    for (String nodePath : failToDelete) {
                        try {
                            client.delete().forPath(nodePath);
                        } catch (Exception e) {
                            log.error("服务取消注册失败", e);
                        }
                    }
                    break;
                }

                case LOST:{
                    log.info("会话失效");
                    sessionChanged = true;
                    break;
                }
            }
        }
    }
}
