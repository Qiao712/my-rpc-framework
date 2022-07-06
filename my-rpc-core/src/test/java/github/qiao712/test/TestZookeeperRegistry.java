package github.qiao712.test;

import github.qiao712.rpc.registry.zookeeper.CuratorUtils;
import github.qiao712.rpc.registry.zookeeper.ZookeeperServiceDiscovery;
import github.qiao712.rpc.registry.zookeeper.ZookeeperServiceRegistry;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TestZookeeperRegistry {

    @Test
    public void testDiscovery() throws InterruptedException, IOException {
        ZookeeperServiceDiscovery discovery = new ZookeeperServiceDiscovery(new InetSocketAddress("8.141.151.176", 2181));

        discovery.subscribeService("test.Service1");
        discovery.subscribeService("test.Service2");

        System.out.println("test.Service1的实例列表:");
        Set<InetSocketAddress> serviceInstances = discovery.getServiceInstances("test.Service1");
        for (InetSocketAddress serviceAddress : serviceInstances) {
            System.out.println(serviceAddress);
        }

        System.in.read();

        System.out.println("test.Service1的实例列表:");
        serviceInstances = discovery.getServiceInstances("test.Service1");
        for (InetSocketAddress serviceAddress : serviceInstances) {
            System.out.println(serviceAddress);
        }

        Thread.sleep(10000000);
    }

    @Test
    public void testRegistry() throws InterruptedException {
        //注册
        ZookeeperServiceRegistry registry = new ZookeeperServiceRegistry(new InetSocketAddress("127.0.0.1", 1000),
                new InetSocketAddress("8.141.151.176", 2181));

        registry.register("test.Service1");
        registry.register("test.Service2");

        Thread.sleep(100000000);

        registry.close();
    }
}
