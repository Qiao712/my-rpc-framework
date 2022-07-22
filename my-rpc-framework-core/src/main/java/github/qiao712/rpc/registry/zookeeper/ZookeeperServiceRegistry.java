package github.qiao712.rpc.registry.zookeeper;

import github.qiao712.rpc.exception.RpcException;
import github.qiao712.rpc.registry.ServiceRegistry;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.io.Closeable;
import java.net.InetSocketAddress;
import java.util.List;

public class ZookeeperServiceRegistry implements ServiceRegistry, Closeable {
    private final static String NAME_SPACE = "my-rpc";
    private final CuratorFramework client;
    private final InetSocketAddress providerAddress;

    public ZookeeperServiceRegistry(InetSocketAddress providerAddress, InetSocketAddress... zookeeperAddresses){
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
        } catch (Exception e) {
            throw new RpcException("服务注册失败", e);
        }
    }

    @Override
    public void unregister(String serviceName) {
        try {
            client.delete().forPath(getProviderNodePath(serviceName));
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
}
