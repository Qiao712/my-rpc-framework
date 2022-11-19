package github.qiao712.test;

import github.qiao712.rpc.registry.ProviderURL;
import github.qiao712.rpc.registry.zookeeper.ZookeeperServiceDiscovery;
import github.qiao712.rpc.registry.zookeeper.ZookeeperServiceRegistry;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

public class TestZookeeperRegistry {

    private InetSocketAddress zkAddress = new InetSocketAddress("114.116.245.83", 2181);
    //测试服务发现
    @Test
    public void testDiscovery() throws InterruptedException, IOException {
        ZookeeperServiceDiscovery discovery = new ZookeeperServiceDiscovery(zkAddress);

        discovery.subscribeService("test.Service1");

        List<ProviderURL> providers = discovery.getProviders("test.Service1");
        while(providers.isEmpty()){
            Thread.sleep(1000);
            providers = discovery.getProviders("test.Service1");
        }

        System.out.println("test.Service1的实例列表:");
        for (ProviderURL provider : providers) {
            System.out.println(provider);
        }

        Thread.sleep(10000000);
    }

    //测试服务注册
    @Test
    public void testRegistry() throws InterruptedException {
        ZookeeperServiceRegistry registry = new ZookeeperServiceRegistry(new InetSocketAddress("114.116.245.83", 2181));

        ProviderURL providerURL1 = new ProviderURL();
        providerURL1.setService("test.Service1");
        providerURL1.setAddress(new InetSocketAddress("localhost", 123));
        providerURL1.setWeight(123);
        registry.register(providerURL1);

        ProviderURL providerURL2 = new ProviderURL();
        providerURL2.setService("test.Service1");
        providerURL2.setAddress(new InetSocketAddress("localhost", 4444));
        providerURL2.setWeight(100);
        registry.register(providerURL2);

        Thread.sleep(100000000);

        registry.close();
    }
}
