package github.qiao712.rpc.registry.zookeeper;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.registry.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.retry.RetryForever;
import org.apache.zookeeper.CreateMode;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@Slf4j
public class ZookeeperServiceRegistry implements ServiceRegistry, Closeable {
    private final static String NAME_SPACE = "my-rpc";
    private final CuratorFramework client;
    private final InetSocketAddress providerAddress;
    private final ConcurrentSkipListSet<String> registeredServices = new ConcurrentSkipListSet<>();

    public ZookeeperServiceRegistry(InetSocketAddress providerAddress, InetSocketAddress... zookeeperAddresses){
        this.providerAddress = providerAddress;

        RetryPolicy retryPolicy = new RetryForever(10000);
        this.client = CuratorFrameworkFactory.builder()
                .connectString(CuratorUtils.getAddressString(zookeeperAddresses))
                .retryPolicy(retryPolicy)
                .namespace(NAME_SPACE)
                .sessionTimeoutMs(60000)    //60s
                .build();

        this.client.getConnectionStateListenable().addListener(new SessionChangeListener());

        this.client.start();
    }

    public ZookeeperServiceRegistry(InetSocketAddress providerAddress, String... zookeeperAddresses){
        this.providerAddress = providerAddress;

        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 5);
        this.client = CuratorFrameworkFactory.builder()
                .connectString(CuratorUtils.getAddressString(zookeeperAddresses))
                .retryPolicy(retryPolicy)
                .namespace(NAME_SPACE)
                .build();

        this.client.start();
    }

    @Override
    public void register(String serviceName) {
        //添加临时节点  /service-name(interface name)/providers/provider-address(host:port)
        try {
            client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(getProviderNodePath(serviceName));
            registeredServices.add(serviceName);
        } catch (Exception e) {
            throw new RpcException("服务注册失败", e);
        }
    }

    @Override
    public void unregister(String serviceName) {
        try {
            client.delete().forPath(getProviderNodePath(serviceName));
            registeredServices.remove(serviceName);
        } catch (Exception e) {
            throw new RpcException("服务注销失败", e);
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
    private String getProviderNodePath(String serviceName){
        return '/' + serviceName + "/providers" + providerAddress;
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
                        for (String registeredService : registeredServices) {
                            try {
                                client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(getProviderNodePath(registeredService));
                            } catch (Exception e) {
                                log.error("重新注册服务 " + registeredService + " 失败", e);
                            }
                        }
                        sessionChanged = false;
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
