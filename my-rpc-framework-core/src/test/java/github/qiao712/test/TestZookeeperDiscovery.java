package github.qiao712.test;

import github.qiao712.rpc.registry.ServiceDiscovery;
import github.qiao712.rpc.registry.zookeeper.ZookeeperServiceDiscovery;
import org.junit.Test;

import java.net.InetSocketAddress;

public class TestZookeeperDiscovery {
    @Test
    public void testDiscovery(){
        ServiceDiscovery serviceDiscovery = new ZookeeperServiceDiscovery(new InetSocketAddress("8.141.151.176", 2181));
        serviceDiscovery.subscribeService("qiao712.service.TestService");
        System.out.println("--------------------------------");
        for (InetSocketAddress serviceInstance : serviceDiscovery.getServiceInstances("qiao712.service.TestService")) {
            System.out.println(serviceInstance);
        }
    }
}
