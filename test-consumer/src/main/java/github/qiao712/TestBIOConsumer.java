package github.qiao712;

import github.qiao712.rpc.cluster.Cluster;
import github.qiao712.rpc.cluster.FailfastCluster;
import github.qiao712.rpc.loadbalance.ConsistentHashLoadBalance;
import github.qiao712.rpc.proto.SerializationType;
import github.qiao712.rpc.proxy.JDKRpcProxyFactory;
import github.qiao712.rpc.proxy.RpcProxyFactory;
import github.qiao712.rpc.registry.ServiceDiscovery;
import github.qiao712.rpc.registry.zookeeper.ZookeeperServiceDiscovery;
import github.qiao712.rpc.transport.RpcClient;
import github.qiao712.rpc.transport.bio.client.BIORpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qiao712.domain.Hello;
import qiao712.service.TestService;

import java.net.InetSocketAddress;

public class TestBIOConsumer {
    private static final Logger log = LoggerFactory.getLogger(TestBIOConsumer.class);

    public static void main(String[] args) {
        RpcClient rpcClient = new BIORpcClient("127.0.0.1", 9712);
        rpcClient.setSerializationType(SerializationType.HESSIAN_SERIALIZATION);

        ServiceDiscovery serviceDiscovery = new ZookeeperServiceDiscovery(new InetSocketAddress("8.141.151.176", 9712));

        Cluster cluster = new FailfastCluster(rpcClient, serviceDiscovery, new ConsistentHashLoadBalance());

        RpcProxyFactory rpcProxyFactory = new JDKRpcProxyFactory();

        TestService testService = rpcProxyFactory.createProxy(TestService.class, TestService.class.getCanonicalName(), cluster);

        System.out.println(testService.add(123, 123));
        System.out.println(testService.add(123,123,123));
        System.out.println(testService.hello(new Hello(23, "hello")));

        long n = 1000;
        long begin = System.nanoTime();
        for(int i = 0; i < n; i++){
            testService.add(231, 123);
        }
        long end = System.nanoTime();

        log.debug("{}次调用耗时{}ms", n, (end-begin)/1000000.0);
    }
}
